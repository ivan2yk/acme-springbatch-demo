package pe.com.acme.worflowacme.config;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Created by Ivan on 22/05/2019.
 */
@Component
@EnableBatchProcessing
// @EnableBatchProcessing. triggers spring batch to include and configure its features at runtime as beans
// Interface BatchConfigurer.  defines getters for JobRepository, JobExplorer, JobLauncher and TransactionManager
// Spring batch provide a default BatchConfigurer implementation but its recommended to implement your own
public class BatchConfiguration implements BatchConfigurer {

    private JobRepository jobRepository; //responsible for persist metadata about batch jobs
    private JobExplorer jobExplorer; // retrieves metadata from the JobRepository
    private JobLauncher jobLauncher; // runs jobs with given parameters

    private DataSource batchDataSource;
    private PlatformTransactionManager batchTransactionManager;

    @Autowired
    public BatchConfiguration(@Qualifier(value = "batchDataSource") DataSource batchDataSource,
                              @Qualifier(value = "batchTransactionManager") PlatformTransactionManager batchTransactionManager) {
        this.batchDataSource = batchDataSource;
        this.batchTransactionManager = batchTransactionManager;
    }

    @Override
    public JobRepository getJobRepository() throws Exception {
        return this.jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() throws Exception {
        return this.batchTransactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() throws Exception {
        return this.jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return this.jobExplorer;
    }

    // SimpleJobLauncher. Executes a job on demand
    private JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository);
        simpleJobLauncher.afterPropertiesSet();
        return simpleJobLauncher;
    }

    private JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(batchDataSource);
        jobRepositoryFactoryBean.setTransactionManager(batchTransactionManager);
        jobRepositoryFactoryBean.afterPropertiesSet();// to ensure dependencies have been set
        return jobRepositoryFactoryBean.getObject();
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.jobRepository = this.createJobRepository();
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(this.batchDataSource);
        jobExplorerFactoryBean.afterPropertiesSet();
        this.jobExplorer = jobExplorerFactoryBean.getObject();
        this.jobLauncher = createJobLauncher();
    }

}
