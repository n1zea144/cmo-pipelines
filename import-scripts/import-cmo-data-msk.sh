#!/bin/bash

# set necessary env variables with automation-environment.sh

tmp=$PORTAL_HOME/tmp/import-cron-cmo-msk
if [[ -d "$tmp" && "$tmp" != "/" ]]; then
	rm -rf "$tmp"/*
fi
email_list="cbioportal-cmo-importer@cbio.mskcc.org"
now=$(date "+%Y-%m-%d-%H-%M-%S")
msk_automation_notification_file=$(mktemp $tmp/msk-automation-portal-update-notification.$now.XXXXXX)
ONCOTREE_VERSION_TO_USE=oncotree_candidate_release

DB_VERSION_FAIL=0
# check database version before importing anything
echo "Checking if database version is compatible"
$JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-dmp-importer.jar org.mskcc.cbio.importer.Admin --check-db-version
if [ $? -gt 0 ]; then
    echo "Database version expected by portal does not match version in database!"
    DB_VERSION_FAIL=1
fi

if [ $DB_VERSION_FAIL -eq 0 ]; then
    # import vetted studies into MSK portal
    echo "importing cancer type updates into msk portal database..."
    $JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx16g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin --import-types-of-cancer --oncotree-version ${ONCOTREE_VERSION_TO_USE}
    echo "importing study data into msk portal database..."
    IMPORT_FAIL=0
    $JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx64g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin --update-study-data --portal msk-automation-portal --update-worksheet --notification-file "$msk_automation_notification_file" --use-never-import --oncotree-version ${ONCOTREE_VERSION_TO_USE} --transcript-overrides-source mskcc
    if [ $? -gt 0 ]; then
        echo "MSK CMO import failed!"
        IMPORT_FAIL=1
        EMAIL_BODY="MSK CMO import failed"
        echo -e "Sending email $EMAIL_BODY"
        echo -e "$EMAIL_BODY" | mail -s "Import failure: MSK CMO" $pipelines_email_list
    fi

    num_studies_updated=`cat $tmp/num_studies_updated.txt`

    # redeploy war
    if [[ $IMPORT_FAIL -eq 0 && $num_studies_updated -gt 0 ]]; then
	    echo "'$num_studies_updated' studies have been updated, requesting redeployment of msk portal war..."
        ssh -i $HOME/.ssh/id_rsa_msk_tomcat_restarts_key cbioportal_importer@dashi.cbio.mskcc.org touch /srv/data/portal-cron/msk-tomcat-restart
        ssh -i $HOME/.ssh/id_rsa_msk_tomcat_restarts_key cbioportal_importer@dashi2.cbio.mskcc.org touch /srv/data/portal-cron/msk-tomcat-restart
	    echo "'$num_studies_updated' studies have been updated (no longer need to restart schultz-tomcat server...)"
    else
	    echo "No studies have been updated, skipping redeploy of msk portal war..."
    fi
fi

EMAIL_BODY="The GDAC database version is incompatible. Imports will be skipped until database is updated."
# send email if db version isn't compatible
if [ $DB_VERSION_FAIL -gt 0 ]; then
    echo -e "Sending email $EMAIL_BODY"
    echo -e "$EMAIL_BODY" | mail -s "GDAC Update Failure: DB version is incompatible" $email_list
fi

$JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=27184 -Xmx16g -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir="$tmp" -cp $PORTAL_HOME/lib/msk-cmo-importer.jar org.mskcc.cbio.importer.Admin --send-update-notification --portal msk-automation-portal --notification-file "$msk_automation_notification_file"

if [[ -d "$tmp" && "$tmp" != "/" ]]; then
	rm -rf "$tmp"/*
fi
