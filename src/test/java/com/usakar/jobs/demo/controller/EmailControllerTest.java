package com.usakar.jobs.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.usakar.jobs.demo.AbstractJobTest;
import com.usakar.jobs.demo.DemoApplication;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.service.EmailService;
import java.time.LocalDateTime;
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
class EmailControllerTest extends AbstractJobTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private EmailService emailService;

    private EmailController emailController;

    private static String jobName;

    @BeforeEach
    void setUp() {
        emailController = new EmailController(emailService);
        jobName = UUID.randomUUID().toString();
    }

    @AfterAll
    void afterAll() throws SchedulerException {
        if (scheduler.checkExists(JobKey.jobKey(jobName))) {
            emailController.deleteJob(jobName);
        }
    }

    @Test
    void shouldCreateJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        ResponseEntity<JobDetails> jobDetailsResult = emailController.createJob(jobDetails);

        assertThat(jobDetailsResult.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    void shouldCreateJobAndCallFindJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailController.createJob(jobDetails);
        ResponseEntity<JobDetails> jobDetailsFindJobResult = emailController.getJob(jobName);

        assertThat(jobDetailsFindJobResult.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldCreateJobAndCallDeleteJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailController.createJob(jobDetails);
        ResponseEntity<String> response = emailController.deleteJob(jobName);

        assertThat(response.getBody()).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_DELETE));
    }

    @Test
    void shouldCreateJobAndCallPauseJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailController.createJob(jobDetails);
        ResponseEntity<String> response = emailController.pauseJob(jobName);

        assertThat(response.getBody()).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_PAUSE));
    }

    @Test
    void shouldCreateJobAndCallResumeJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailController.createJob(jobDetails);
        ResponseEntity<String> response = emailController.resumeJob(jobName);

        assertThat(response.getBody()).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_RESUME));
    }

    @Test
    void shouldCreateJobAndCallUpdateJobThenVerifyResponseSuccess() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailController.createJob(jobDetails);
        jobDetails = getJobDetails(MESSAGE_BODY_NEW, jobName, CRON_EACH_MINUTE, LocalDateTime.now());
        ResponseEntity<String> response = emailController.updateJob(jobName, jobDetails);

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }
}
