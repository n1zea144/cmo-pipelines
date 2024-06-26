/*
 * Copyright (c) 2016 - 2019, 2024 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal CMO-Pipelines.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cmo.ks.crdb.pipeline;

import java.net.MalformedURLException;
import javax.sql.DataSource;
import org.cbioportal.cmo.pipelines.common.util.EmailUtil;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBDataset;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXClinicalAnnotationMapping;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXClinicalPatientDataset;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXClinicalSampleDataset;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXSourceToDestinationMapping;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBPDXTimelineDataset;
import org.mskcc.cmo.ks.crdb.pipeline.model.CRDBSurvey;
import org.mskcc.cmo.ks.crdb.pipeline.util.CRDBUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration for running the CRDB clinical data fetcher.
 *
 * @author ochoaa
 */

@Configuration
public class BatchConfiguration {

    public static final String CRDB_IMPACT_JOB = "crdbImpactJob";
    public static final String CRDB_PDX_JOB = "crdbPDXJob";

    @Bean
    public CRDBUtils crdbUtils() {
        return new CRDBUtils();
    }

    @Bean
    public EmailUtil emailUtil() {
        return new EmailUtil();
    }

    @Bean
    public Job crdbImpactJob(JobRepository jobRepository,
                             @Qualifier("crdbSurveyStep") Step crdbSurveyStep,
                             @Qualifier("crdbDatasetStep") Step crdbDatasetStep) {
        return new JobBuilder(CRDB_IMPACT_JOB, jobRepository)
            .start(crdbSurveyStep)
            .next(crdbDatasetStep)
            .build();
    }

    @Bean
    public Job crdbPDXJob(JobRepository jobRepository,
                          @Qualifier("crdbPDXClinicalSampleStep") Step crdbPDXClinicalSampleStep,
                          @Qualifier("crdbPDXClinicalPatientStep") Step crdbPDXClinicalPatientStep,
                          @Qualifier("crdbPDXTimelineStep") Step crdbPDXTimelineStep,
                          @Qualifier("crdbPDXSourceToDestinationMappingStep") Step crdbPDXSourceToDestinationMappingStep,
                          @Qualifier("crdbPDXClinicalAnnotationMappingStep") Step crdbPDXClinicalAnnotationMappingStep) {
        return new JobBuilder(CRDB_PDX_JOB, jobRepository)
            .start(crdbPDXClinicalSampleStep)
            .next(crdbPDXClinicalPatientStep)
            .next(crdbPDXTimelineStep)
            .next(crdbPDXSourceToDestinationMappingStep)
            .next(crdbPDXClinicalAnnotationMappingStep)
            .build();
    }

    /**
     * Step 1 reads, processes, and writes the CRDB Survey query results
     */
    @Bean(name = "crdbSurveyStep")
    public Step crdbSurveyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbSurveyStep", jobRepository)
            .<CRDBSurvey, String> chunk(10, transactionManager)
            .reader(crdbSurveyReader())
            .processor(crdbSurveyProcessor())
            .writer(crdbSurveyWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBSurvey> crdbSurveyReader() {
        return new CRDBSurveyReader();
    }

    @Bean
    public CRDBSurveyProcessor crdbSurveyProcessor() {
        return new CRDBSurveyProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbSurveyWriter() {
        return new CRDBSurveyWriter();
    }

    /**
     * Step 2 reads, processes, and writes the CRDB Dataset query results
     */
    @Bean(name = "crdbDatasetStep")
    public Step crdbDatasetStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbDatasetStep", jobRepository)
            .<CRDBDataset, String> chunk(10, transactionManager)
            .reader(crdbDatasetReader())
            .processor(crdbDatasetProcessor())
            .writer(crdbDatasetWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBDataset> crdbDatasetReader() {
        return new CRDBDatasetReader();
    }

    @Bean
    public CRDBDatasetProcessor crdbDatasetProcessor() {
        return new CRDBDatasetProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbDatasetWriter() {
        return new CRDBDatasetWriter();
    }

    @Bean(name = "crdbPDXClinicalAnnotationMappingStep")
    public Step crdbPDXClinicalAnnotationMappingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbPDXClinicalAnnotationMappingStep", jobRepository)
            .<CRDBPDXClinicalAnnotationMapping, String> chunk(10, transactionManager)
            .reader(crdbPDXClinicalAnnotationMappingReader())
            .processor(crdbPDXClinicalAnnotationMappingProcessor())
            .writer(crdbPDXClinicalAnnotationMappingWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBPDXClinicalAnnotationMapping> crdbPDXClinicalAnnotationMappingReader() {
        return new CRDBPDXClinicalAnnotationMappingReader();
    }

    @Bean
    @StepScope
    public CRDBPDXClinicalAnnotationMappingProcessor crdbPDXClinicalAnnotationMappingProcessor() {
        return new CRDBPDXClinicalAnnotationMappingProcessor();
    }

    @Bean
    @StepScope
    public CRDBPDXClinicalAnnotationMappingWriter crdbPDXClinicalAnnotationMappingWriter() {
        return new CRDBPDXClinicalAnnotationMappingWriter();
    }

    @Bean(name = "crdbPDXSourceToDestinationMappingStep")
    public Step crdbPDXSourceToDestinationMappingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbPDXSourceToDestinationMappingStep", jobRepository)
            .<CRDBPDXSourceToDestinationMapping, String> chunk(10, transactionManager)
            .reader(crdbPDXSourceToDestinationMappingReader())
            .processor(crdbPDXSourceToDestinationMappingProcessor())
            .writer(crdbPDXSourceToDestinationMappingWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBPDXSourceToDestinationMapping> crdbPDXSourceToDestinationMappingReader() {
        return new CRDBPDXSourceToDestinationMappingReader();
    }

    @Bean
    @StepScope
    public CRDBPDXSourceToDestinationMappingProcessor crdbPDXSourceToDestinationMappingProcessor() {
        return new CRDBPDXSourceToDestinationMappingProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbPDXSourceToDestinationMappingWriter() {
        return new CRDBPDXSourceToDestinationMappingWriter();
    }

    @Bean(name = "crdbPDXClinicalSampleStep")
    public Step crdbPDXClinicalSampleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbPDXClinicalSampleStep", jobRepository)
            .<CRDBPDXClinicalSampleDataset, String> chunk(10, transactionManager)
            .reader(crdbPDXClinicalSampleReader())
            .processor(crdbPDXClinicalSampleProcessor())
            .writer(crdbPDXClinicalSampleWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBPDXClinicalSampleDataset> crdbPDXClinicalSampleReader() {
        return new CRDBPDXClinicalSampleReader();
    }

    @Bean
    @StepScope
    public CRDBPDXClinicalSampleProcessor crdbPDXClinicalSampleProcessor() {
        return new CRDBPDXClinicalSampleProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbPDXClinicalSampleWriter() {
        return new CRDBPDXClinicalSampleWriter();
    }

    @Bean(name = "crdbPDXClinicalPatientStep")
    public Step crdbPDXClinicalPatientStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbPDXClinicalPatientStep", jobRepository)
            .<CRDBPDXClinicalPatientDataset, String> chunk(10, transactionManager)
            .reader(crdbPDXClinicalPatientReader())
            .processor(crdbPDXClinicalPatientProcessor())
            .writer(crdbPDXClinicalPatientWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBPDXClinicalPatientDataset> crdbPDXClinicalPatientReader() {
        return new CRDBPDXClinicalPatientReader();
    }

    @Bean
    @StepScope
    public CRDBPDXClinicalPatientProcessor crdbPDXClinicalPatientProcessor() {
        return new CRDBPDXClinicalPatientProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbPDXClinicalPatientWriter() {
        return new CRDBPDXClinicalPatientWriter();
    }

    @Bean(name = "crdbPDXTimelineStep")
    public Step crdbPDXTimelineStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("crdbPDXTimelineStep", jobRepository)
            .<CRDBPDXTimelineDataset, String> chunk(10, transactionManager)
            .reader(crdbPDXTimelineReader())
            .processor(crdbPDXTimelineProcessor())
            .writer(crdbPDXTimelineWriter())
            .listener(crdbPDXTimelineListener())
            .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<CRDBPDXTimelineDataset> crdbPDXTimelineReader() {
        return new CRDBPDXTimelineReader();
    }

    @Bean
    @StepScope
    public CRDBPDXTimelineProcessor crdbPDXTimelineProcessor() {
        return new CRDBPDXTimelineProcessor();
    }

    @Bean
    @StepScope
    public ItemStreamWriter<String> crdbPDXTimelineWriter() {
        return new CRDBPDXTimelineWriter();
    }

    @Bean
    @StepScope
    public StepExecutionListener crdbPDXTimelineListener() {
        return new CRDBPDXTimelineListener();
    }

    // general spring batch configuration
    @Value("org/springframework/batch/core/schema-drop-sqlite.sql")
    private Resource dropRepositoryTables;

    @Value("org/springframework/batch/core/schema-sqlite.sql")
    private Resource dataRepositorySchema;

    /**
     * Spring Batch datasource.
     * @return DataSource
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:repository.sqlite");
        return dataSource;
    }

    /**
     * Spring Batch datasource initializer.
     * @param dataSource
     * @return DataSourceInitializer
     * @throws MalformedURLException
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) throws MalformedURLException {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(dropRepositoryTables);
        databasePopulator.addScript(dataRepositorySchema);
        databasePopulator.setIgnoreFailedDrops(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator);
        return initializer;
    }

    /**
     * Spring Batch job repository.
     * @return JobRepository
     * @throws Exception
     */
    private JobRepository getJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource());
        factory.setTransactionManager(getTransactionManager());
        factory.afterPropertiesSet();
        return (JobRepository) factory.getObject();
    }

    /**
     * Spring Batch transaction manager.
     * @return PlatformTransactionManager
     */
    private PlatformTransactionManager getTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    /**
     * Spring Batch job launcher.
     * @return JobLauncher
     * @throws Exception
     */
    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}

