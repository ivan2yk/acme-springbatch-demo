package pe.com.acme.worflowacme.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acme.worflowacme.domain.PatientEntity;
import pe.com.acme.worflowacme.dto.PatientDTO;
import pe.com.acme.worflowacme.repository.PatientRepository;
import pe.com.acme.worflowacme.util.Constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by Ivan on 24/05/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@Slf4j
@Transactional
public class BatchJobConfigurationTest {

    @Autowired
    private Job job;

    @Autowired
    private JobParametersValidator validator;

    @Autowired
    private FlatFileItemReader<PatientDTO> reader;

    @Autowired
    private Function<PatientDTO, PatientEntity> processor;

    @Autowired
    private JpaItemWriter<PatientEntity> writer;

    @Autowired
    private PatientRepository patientRepository;

    private JobParameters jobParameters;

    @Before
    public void setUp() throws Exception {
        Map<String, JobParameter> params = new HashMap<>();
        params.put(Constants.JOB_PARAM_FILE_NAME, new JobParameter("test-unit-testing.csv"));
        jobParameters = new JobParameters(params);
    }

    @Test
    public void test() {
        assertNotNull(job);
        assertEquals(Constants.JOB_NAME, job.getName());
    }

    @Test(expected = JobParametersInvalidException.class)
    public void givenNotJobParam_whenValidator_thenJobParametersInvalidException() throws JobParametersInvalidException {
        Map<String, JobParameter> params = new HashMap<>();
        params.put(Constants.JOB_PARAM_FILE_NAME, new JobParameter(""));
        jobParameters = new JobParameters(params);

        validator.validate(jobParameters);
    }

    @Test(expected = JobParametersInvalidException.class)
    public void givenFileNotFound_whenValidator_thenJobParametersInvalidException() throws JobParametersInvalidException {
        Map<String, JobParameter> params = new HashMap<>();
        params.put(Constants.JOB_PARAM_FILE_NAME, new JobParameter("test-unit-testing.txt"));
        jobParameters = new JobParameters(params);

        validator.validate(jobParameters);
    }

    @Test
    public void whenReader_thenOk() {
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        int count = 0;

        try {
            count = StepScopeTestUtils.doInStepScope(stepExecution, () -> {
                int numPatients = 0;
                PatientDTO patientDTO;

                try {
                    reader.open(stepExecution.getExecutionContext());

                    while ((patientDTO = reader.read()) != null) {
                        System.out.println(patientDTO);
                        assertNotNull(patientDTO);
                        assertEquals("Hettie", patientDTO.getFirstName());
                        assertEquals("Schmidt", patientDTO.getLastName());
                        numPatients++;
                    }
                } finally {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e.getCause());
                        fail(e.getMessage());
                    }
                }
                return numPatients;
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            fail(e.getMessage());
        }

        assertEquals(1, count);
    }

    @Test
    public void whenProcessor_thenOk() {
        PatientDTO dto = PatientDTO.builder()
                .sourceId("1212121")
                .firstName("Ivan")
                .middleInitial("L")
                .lastName("Leiva")
                .birthdate("10/10/2010")
                .city("Lima")
                .zip("Lima19")
                .state("Lima")
                .build();

        PatientEntity entity = processor.apply(dto);

        assertNotNull(entity);
        assertEquals(dto.getSourceId(), entity.getSourceId());
        assertEquals(dto.getFirstName(), entity.getFirstName());
        assertEquals(dto.getLastName(), entity.getLastName());
        assertEquals(dto.getCity(), entity.getCity());
        assertEquals(dto.getBirthdate(), entity.getBirthDate().format(DateTimeFormatter.ofPattern("M/dd/yyyy")));
    }

    @Test
    @Ignore
    public void whenWriter_thenOk() throws Exception {
        PatientEntity entity = PatientEntity.builder()
                .sourceId("1213")
                .firstName("Ivan")
                .lastName("Leiva")
                .middleInitial("L")
                .birthDate(LocalDate.of(2010, 10, 10))
                .city("Lima")
                .state("Lima")
                .zipCode("Lima18")
                .creationDateTime(LocalDateTime.now()).build();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                writer.write(Arrays.asList(entity));
                return 1;
            }
        });

        assertTrue(patientRepository.findAll().size() > 0);
    }


}