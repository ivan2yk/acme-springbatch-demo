package pe.com.acme.worflowacme.config;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import pe.com.acme.worflowacme.util.Constants;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Ivan on 22/05/2019.
 */
@Configuration
public class BatchJobConfiguration {

    private String inputPath;

    // factory for getting the type of builder required for job configuration
    // provide the dsl for configure the job
    private JobBuilderFactory jobBuilderFactory;

    // factory for getting the type of builder required for step configuration
    // steps are configured to use the concept of a tasklet. tasklet defines what task the step will perform
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public BatchJobConfiguration(JobBuilderFactory jobBuilderFactory,
                                 @Value("application.batch.inputPath") String inputPath,
                                 StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.inputPath = inputPath;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    // the goal of JobRegistryBeanPostProcessor is to register all jobs with the job registry as they are created.
    // provide support for registering our job with the JobRepository.
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public Job job(Step step) throws Exception {
        return this.jobBuilderFactory
                .get(Constants.JOB_NAME)
                .validator(validator())
                .start(step)//start. creates and instance of SimpleJobBuilder which will then execute the step
                .build();
    }

    @Bean
    public JobParametersValidator validator() {
        return jobParameters -> {
            String fileName = jobParameters.getString(Constants.JOB_PARAM_FILE_NAME);
            if (!StringUtils.hasLength(fileName)) {
                throw new JobParametersInvalidException("The patient-batch-loader.fileName parameter is required");
            }

            Path file = Paths.get(inputPath + File.separator + fileName);
            if (Files.notExists(file) || !Files.isReadable(file)) {
                throw new JobParametersInvalidException("The 'input path' + 'fileName' parameter needs to be a valid file location");
            }
        };
    }

    @Bean
    public Step step() throws Exception {
        return this.stepBuilderFactory
                .get(Constants.STEP_NAME)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.err.println("Hello world!");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

}
