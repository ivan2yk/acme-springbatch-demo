package pe.com.acme.worflowacme.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;
import pe.com.acme.worflowacme.domain.PatientEntity;
import pe.com.acme.worflowacme.dto.PatientDTO;
import pe.com.acme.worflowacme.util.Constants;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Created by Ivan on 22/05/2019.
 */
@Configuration
@Slf4j
public class BatchJobConfiguration {

    private String inputPath;

    // factory for getting the type of builder required for job configuration
    // provide the dsl for configure the job
    private JobBuilderFactory jobBuilderFactory;

    // StepBuilderFactory. factory for getting the type of builder required for step configuration
    // steps are configured to use the concept of a tasklet. tasklet defines what task the step will perform
    private StepBuilderFactory stepBuilderFactory;

    private EntityManagerFactory acmeEntityManager;

    private PlatformTransactionManager acmeTransactionManager;

    @Autowired
    public BatchJobConfiguration(JobBuilderFactory jobBuilderFactory,
                                 @Value("${application.batch.inputPath}") String inputPath,
                                 StepBuilderFactory stepBuilderFactory,
                                 @Qualifier("acmeEntityManager") EntityManagerFactory acmeEntityManager,
                                 @Qualifier("acmeTransactionManager") PlatformTransactionManager acmeTransactionManager) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.inputPath = inputPath;
        this.stepBuilderFactory = stepBuilderFactory;
        this.acmeEntityManager = acmeEntityManager;
        this.acmeTransactionManager = acmeTransactionManager;
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

    /**
     * The purpose of the validator is to validate the job parameters that are input to the job.
     * </br>
     * In this case we will validate if the parameter FILE_NAME is valid and if the input file if refers exists.
     *
     * @return Parameters validator
     */
    @Bean
    public JobParametersValidator validator() {
        return new JobParametersValidator() {
            @Override
            public void validate(JobParameters jobParameters) throws JobParametersInvalidException {
                String fileName = jobParameters.getString(Constants.JOB_PARAM_FILE_NAME);

                log.debug("Starting validator with: {}", fileName);

                if (!StringUtils.hasLength(fileName)) {
                    throw new JobParametersInvalidException("The patient-batch-loader.fileName parameter is required");
                }

                Path file = Paths.get(inputPath + File.separator + fileName);
                if (!file.toFile().exists() || !Files.isReadable(file)) {
                    throw new JobParametersInvalidException("The 'input path' + 'fileName' parameter needs to be a valid file location");
                }
            }
        };
    }

    // chunk processing requires a reader, processor and writer
    @Bean
    public Step step(ItemReader<PatientDTO> itemReader,
                     Function<PatientDTO, PatientEntity> processor,
                     JpaItemWriter<PatientEntity> writer) throws Exception {
        return this.stepBuilderFactory
                .get(Constants.STEP_NAME)
                .transactionManager(acmeTransactionManager)//solve no active transaction
                .<PatientDTO, PatientEntity>chunk(1000)
                .reader(itemReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope //allows to inject values from the step context
    public FlatFileItemReader<PatientDTO> itemReader(@Value("#{jobParameters['" + Constants.JOB_PARAM_FILE_NAME + "']}") String fileName) {

        return new FlatFileItemReaderBuilder<PatientDTO>()
                .name(Constants.ITEM_READER_NAME)
                .resource(new FileSystemResource(
                        Paths.get(inputPath + File.separator + fileName)
                ))
                .linesToSkip(1)
                .lineMapper(lineMapper())
                .build();
    }

    @Bean
    public LineMapper<PatientDTO> lineMapper() {
        DefaultLineMapper<PatientDTO> mapper = new DefaultLineMapper<>();
        mapper.setFieldSetMapper(fieldSet -> PatientDTO.builder()
                .sourceId(fieldSet.readString(0))
                .firstName(fieldSet.readString(1))
                .middleInitial(fieldSet.readString(2))
                .lastName(fieldSet.readString(3))
                .emailAddress(fieldSet.readString(4))
                .phoneNumber(fieldSet.readString(5))
                .street(fieldSet.readString(6))
                .city(fieldSet.readString(7))
                .state(fieldSet.readString(8))
                .zip(fieldSet.readString(9))
                .birthdate(fieldSet.readString(10))
                .action(fieldSet.readString(11))
                .ssn(fieldSet.readString(12))
                .build());
        mapper.setLineTokenizer(new DelimitedLineTokenizer());// ',' by default
        return mapper;
    }

    @Bean
    @StepScope
    public Function<PatientDTO, PatientEntity> processor() {
        return patientDTO -> {
            return PatientEntity.builder()
                    .sourceId(patientDTO.getSourceId())
                    .firstName(patientDTO.getFirstName())
                    .middleInitial(patientDTO.getMiddleInitial())
                    .lastName(patientDTO.getLastName())
                    .emailAddress(patientDTO.getEmailAddress())
                    .phoneNumber(patientDTO.getPhoneNumber())
                    .street(patientDTO.getStreet())
                    .city(patientDTO.getCity())
                    .state(patientDTO.getState())
                    .zipCode(patientDTO.getZip())
                    .birthDate(LocalDate.parse(patientDTO.getBirthdate(), DateTimeFormatter.ofPattern("M/dd/yyyy")))
                    .socialSecurityNumber(patientDTO.getSsn())
                    .creationDateTime(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<PatientEntity> writer() throws Exception {
        JpaItemWriter<PatientEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(acmeEntityManager);
        return writer;
    }

}
