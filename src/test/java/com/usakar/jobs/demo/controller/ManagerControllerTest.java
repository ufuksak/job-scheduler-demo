package com.usakar.jobs.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.usakar.jobs.demo.AbstractJobTest;
import com.usakar.jobs.demo.DemoApplication;
import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.model.ServerResponse;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import com.usakar.jobs.demo.service.EmailServiceImpl;
import com.usakar.jobs.demo.service.ManagerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class ManagerControllerTest extends AbstractJobTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private ManagerService managerService;

    private EmailServiceImpl emailServiceImpl;
    private ManagerController managerController;

    private static String jobName;

    @BeforeEach
    void setUp() {
        emailServiceImpl = new EmailServiceImpl(scheduler, jobStatusRepository);
        managerController = new ManagerController(managerService);
        jobName = UUID.randomUUID().toString();
    }

    @AfterAll
    void afterAll() throws SchedulerException {
        if (scheduler.checkExists(JobKey.jobKey(jobName))) {
            emailServiceImpl.deleteJob(jobName);
        }
    }

    @Test
    void shouldCreateOneTimeJobThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        ServerResponse serverResponse = managerController.schedule(jobDetails);

        assertThat(serverResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldCreateJobAndUnScheduleThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailServiceImpl.createJob(jobDetails);
        ResponseEntity<String> response = managerController.unScheduleJob(jobName);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldCreateJobAndCallStartJobThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailServiceImpl.createJob(jobDetails);
        ServerResponse serverResponse = managerController.startJobNow(jobName);

        assertThat(serverResponse.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldCreateJobAndCallGetAllJobsThenVerifySizeThree() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailServiceImpl.createJob(jobDetails);
        ServerResponse response = managerController.getAllJobs();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldCreateJobAndGetAllJobActionsThenVerifyActionListSizeNotEmpty() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailServiceImpl.createJob(jobDetails);
        List<JobStatus> jobStatusList = managerController.getAllJobActions();

        assertThat(jobStatusList.size()).isGreaterThan(0);
    }
}
