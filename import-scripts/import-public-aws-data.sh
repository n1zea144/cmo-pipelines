#!/bin/bash

# set necessary env variables with automation-environment.sh
if [[ -z $PORTAL_HOME || -z $JAVA_BINARY ]] ; then
    echo "Error : import-public-data.sh cannot be run without setting PORTAL_HOME and JAVA_BINARY environment variables. (Use automation-environment.sh)"
    exit 1
fi

tmp=$PORTAL_HOME/tmp/import-cron-public-aws
if [[ -d "$tmp" && "$tmp" != "/" ]]; then
    rm -rf "$tmp"/*
fi
email_list="cbioportal-pipelines@cbio.mskcc.org"
now=$(date "+%Y-%m-%d-%H-%M-%S")
IMPORTER_JAR_FILENAME="$PORTAL_HOME/lib/public-aws-importer.jar"
JAVA_DEBUG_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27182"
JAVA_IMPORTER_ARGS="$JAVA_PROXY_ARGS $JAVA_DEBUG_ARGS -Dspring.profiles.active=dbcp -Djava.io.tmpdir=$tmp -ea -cp $IMPORTER_JAR_FILENAME org.mskcc.cbio.importer.Admin"
public_portal_notification_file=$(mktemp $tmp/public-aws-portal-update-notification.$now.XXXXXX)
ONCOTREE_VERSION_TO_USE=oncotree_latest_stable

# wait for the local import of public data to complete before running the import to AWS database host
SECONDS_BETWEEN_PUBLIC_IMPORTER_CHECKS=15
local_import_public_data_still_running=1
while [ $local_import_public_data_still_running -eq 1 ] ; do
    hour=`date +%H`
    if [ $hour -gt 22 ] ; then
        echo terminating script now .. waiting for import-public-data.sh to end, but 11pm reached
        exit 3
    fi
    if ps aux | grep "import-public-data.sh" | grep "Failure in crontab" ; then
        sleep $SECONDS_BETWEEN_PUBLIC_IMPORTER_CHECKS
    else
        local_import_public_data_still_running=0
    fi
done

# refresh cdd and oncotree cache
# use the same cdd and oncotree version as was running during the import to the internal database (no update)

DB_VERSION_FAIL=0
# check database version before importing anything
echo "Checking if database version is compatible"
$JAVA_BINARY $JAVA_IMPORTER_ARGS --check-db-version
if [ $? -gt 0 ]; then
    echo "Database version expected by portal does not match version in database!"
    DB_VERSION_FAIL=1
fi

# all data fetches are ignored because we are using same data from the previous night's import

if [[ $DB_VERSION_FAIL -eq 0 ]]; then
    # import public studies into public portal
    echo "importing cancer type updates into public portal aws database..."
    $JAVA_BINARY -Xmx16g $JAVA_IMPORTER_ARGS --import-types-of-cancer --oncotree-version ${ONCOTREE_VERSION_TO_USE}
    echo "importing study data into public aws portal database..."
    IMPORT_FAIL=0
    $JAVA_BINARY -Xmx64g $JAVA_IMPORTER_ARGS --update-study-data --portal public-aws-portal --update-worksheet --notification-file "$public_portal_notification_file" --oncotree-version ${ONCOTREE_VERSION_TO_USE} --transcript-overrides-source uniprot
    if [ $? -gt 0 ]; then
        echo "Public aws import failed!"
        IMPORT_FAIL=1
        EMAIL_BODY="Public aws import failed"
        echo -e "Sending email $EMAIL_BODY"
        echo -e "$EMAIL_BODY" | mail -s "Import failure: public aws" $email_list
    fi
    num_studies_updated=`cat $tmp/num_studies_updated.txt`
    echo "'$num_studies_updated' studies have been updated"
fi

# no restart of aws tomcat yet

EMAIL_BODY="The Public aws database version is incompatible. Imports will be skipped until database is updated."
# send email if db version isn't compatible
if [ $DB_VERSION_FAIL -gt 0 ]; then
    echo -e "Sending email $EMAIL_BODY"
    echo -e "$EMAIL_BODY" | mail -s "Public AWS Update Failure: DB version is incompatible" $email_list
fi

if [ $IMPORT_FAIL -eq 0 ] ; then
    if [ -f "$public_portal_notification_file" ] ; then
        line_count=`wc -l $public_portal_notification_file`
        if [ "$line_count" != 0 ] ; then
            email_filename=${public_portal_notification_file}_emailtext
            echo The import of Public aws studies completed successfully. > $email_filename
            echo >> $email_filename
            echo Output generated by the cBioPortal importer: >> $email_filename
            echo -------------------------------------------- >> $email_filename
            cat $public_portal_notification_file >> $email_filename
            cat $email_filename | mail -s "Public aws cBioPortal import success" $email_list
            rm -f $email_filename
        fi
    fi
fi

echo "Cleaning up any untracked files from CBIO-PUBLIC import..."
bash $PORTAL_HOME/scripts/datasource-repo-cleanup.sh $PORTAL_DATA_HOME $PORTAL_DATA_HOME/impact $PORTAL_DATA_HOME/private $PORTAL_DATA_HOME/datahub
