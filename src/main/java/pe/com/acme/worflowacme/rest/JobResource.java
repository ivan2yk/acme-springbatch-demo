package pe.com.acme.worflowacme.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.com.acme.worflowacme.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 26/05/2019.
 */
@RestController
@RequestMapping(value = "job")
@Slf4j
public class JobResource {

    private JobLauncher jobLauncher;

    private Job job;

    @Autowired
    public JobResource(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<String> runJob(@PathVariable("fileName") String fileName){
        log.debug("fileName: {}", fileName);

        Map<String, JobParameter> parameterMap = new HashMap<>();
        parameterMap.put(Constants.JOB_PARAM_FILE_NAME, new JobParameter(fileName));

        try {
            jobLauncher.run(job, new JobParameters(parameterMap));
            log.debug("Job has been executed");
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
            log.error(e.getMessage(), e.getCause());
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Success");
    }


}
