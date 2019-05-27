package pe.com.acme.worflowacme.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pe.com.acme.worflowacme.dto.PatientDTO;
import pe.com.acme.worflowacme.util.Constants;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Ivan on 24/05/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class BatchJobConfigurationTest {

    @Autowired
    private Job job;

    @Autowired
    private FlatFileItemReader<PatientDTO> reader;

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


}