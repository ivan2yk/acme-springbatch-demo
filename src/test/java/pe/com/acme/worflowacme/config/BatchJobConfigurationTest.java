package pe.com.acme.worflowacme.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pe.com.acme.worflowacme.WorflowacmeApplication;
import pe.com.acme.worflowacme.util.Constants;

import static org.junit.Assert.*;

/**
 * Created by Ivan on 24/05/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchJobConfigurationTest {

    @Autowired
    private Job job;

    @Test
    public void test() {
        assertNotNull(job);
        assertEquals(Constants.JOB_NAME, job.getName());
    }

}