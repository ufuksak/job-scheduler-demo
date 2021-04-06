package com.usakar.jobs.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.usakar.jobs.demo.AbstractJobTest;
import com.usakar.jobs.demo.DemoApplication;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class EmailServiceImplTest extends AbstractJobTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    private EmailServiceImpl emailService;

    private static String jobName;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(scheduler, jobStatusRepository);
        jobName = UUID.randomUUID().toString();
    }

    @AfterAll
    void afterAll() throws SchedulerException {
        if (scheduler.checkExists(JobKey.jobKey(jobName))) {
            emailService.deleteJob(jobName);
        }
    }

    @Test
    void shouldCreateJobThenVerifyJobName() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        JobDetails jobDetailsResult = emailService.createJob(jobDetails);

        assertThat(jobDetailsResult.getName()).isEqualTo(jobName);
    }

    @Test
    void shouldCreateJobAndCallFindJobThenVerifyJobName() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        Optional<JobDetails> jobDetailsFindJobResult = emailService.findJob(jobName);

        jobDetailsFindJobResult.ifPresent(details -> assertThat(details.getName()).isEqualTo(jobName));
    }

    @Test
    void shouldCreateJobAndCallDeleteJobThenVerifyJobName() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        String response = emailService.deleteJob(jobName);

        assertThat(response).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_DELETE));
    }

    @Test
    void shouldCreateJobAndCallPauseJobThenVerifyJobName() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        String response = emailService.pauseJob(jobName);

        assertThat(response).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_PAUSE));
    }

    @Test
    void shouldCreateJobAndCallResumeJobThenVerifyJobName() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        String response = emailService.resumeJob(jobName);

        assertThat(response).isEqualTo(String.format(SUCCESS_MESSAGE, ACTION_RESUME));
    }

    @Test
    void shouldCreateJobAndCallUpdateJobThenVerifyMessageBody() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        jobDetails = getJobDetails(MESSAGE_BODY_NEW, jobName, CRON_EACH_MINUTE, LocalDateTime.now());
        JobDataMap response = emailService.updateJob(jobName, jobDetails);

        assertThat(response.get("messageBody")).isEqualTo(MESSAGE_BODY_NEW);
    }

    @Test
    void shouldCallCreateJobWithDateScriptThenVerifyJob() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, "", LocalDateTime.now());

        JobDetails jobDetailsResult = emailService.createJob(jobDetails);

        assertThat(jobDetailsResult.getName()).isEqualTo(jobName);
    }

    @Nested
    class CornerCases {

        @Test
        void shouldCallFindJobWhenNoJobExistsThenVerifyFailure() {
            Optional<JobDetails> response = emailService.findJob(jobName);

            assertThat(response.isPresent()).isEqualTo(false);
        }

        @Test
        void shouldCallDeleteJobWhenNoJobExistsThenVerifyFailure() {
            String response = emailService.deleteJob(jobName);

            assertThat(response).isEqualTo(
                    String.format(ERROR_MESSAGE, ACTION_DELETE, String.format(NO_JOB_EXISTS, jobName)));
        }

        @Test
        void shouldCallPauseJobWhenNoJobExistsThenVerifyFailure() {
            String response = emailService.pauseJob(jobName);

            assertThat(response).isEqualTo(
                    String.format(ERROR_MESSAGE, ACTION_PAUSE, String.format(NO_JOB_EXISTS, jobName)));
        }

        @Test
        void shouldCallResumeJobWhenNoJobExistsThenVerifyFailure() {
            String response = emailService.resumeJob(jobName);

            assertThat(response).isEqualTo(
                    String.format(ERROR_MESSAGE, ACTION_RESUME, String.format(NO_JOB_EXISTS, jobName)));
        }

        @Test
        void shouldCallUpdateJobWhenNoJobExistsThenVerifyFailure() throws SchedulerException {
            JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

            JobDataMap response = emailService.updateJob(jobName, jobDetails);

            assertThat(response.size()).isEqualTo(0);
        }

        @Test
        void shouldCallCreateJobWithWrongCronScriptThenVerifyException() throws SchedulerException {
            JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, "XYZ", LocalDateTime.now());

            assertThrows(IllegalArgumentException.class,
                    () -> emailService.createJob(jobDetails));
        }

        @Test
        void shouldCallCreateJobWithMissingCronScriptAndTriggerTimeThenVerifyException() throws SchedulerException {
            JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, "", null);

            assertThrows(IllegalStateException.class,
                    () -> emailService.createJob(jobDetails));
        }
    }
}
