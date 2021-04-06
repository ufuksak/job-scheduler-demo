package com.usakar.jobs.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.usakar.jobs.demo.AbstractJobTest;
import com.usakar.jobs.demo.DemoApplication;
import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.job.OneTimeJob;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.context.TestPropertySource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:test.properties")
class ManagerServiceImplTest extends AbstractJobTest {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    private EmailServiceImpl emailService;
    private ManagerServiceImpl managerService;

    private static String jobName;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(scheduler, jobStatusRepository);
        managerService = new ManagerServiceImpl(scheduler, jobStatusRepository);
        jobName = UUID.randomUUID().toString();
    }

    @AfterAll
    void afterAll() throws SchedulerException {
        if (scheduler.checkExists(JobKey.jobKey(jobName))) {
            emailService.deleteJob(jobName);
        }
    }

    @Test
    void shouldCreateOneTimeJobThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());
        Date date = new Date();

        boolean status = managerService.scheduleOneTimeJob(jobDetails.getName(), OneTimeJob.class, date, jobDetails);

        assertThat(status).isEqualTo(true);
    }

    @Test
    void shouldCreateJobAndUnScheduleThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        boolean status = managerService.unScheduleJob(jobName);

        assertThat(status).isEqualTo(true);
    }

    @Test
    void shouldCreateJobAndCallStartJobThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        boolean status = managerService.startJobNow(jobName);

        assertThat(status).isEqualTo(true);
    }

    @Test
    void shouldCreateJobAndCallJobWithNamePresentThenVerifyTrue() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        boolean status = managerService.isJobWithNamePresent(jobName);

        assertThat(status).isEqualTo(true);
    }

    @Test
    void shouldCreateJobAndCallGetAllJobsThenVerifySizeGreaterThanZero() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        List<Map<String, Object>> response = managerService.getAllJobs();

        assertThat(response.size()).isGreaterThan(0);
    }

    @Test
    void shouldCreateJobAndCallUpdateJobThenVerifyFalse() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        boolean status = managerService.isJobRunning(jobName);

        assertThat(status).isEqualTo(false);
    }

    @Test
    void shouldCreateJobAndGetAllJobActionsThenVerifyActionListSizeNotEmpty() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        List<JobStatus> jobStatusList = managerService.getAllJobActions();

        assertThat(jobStatusList.size()).isGreaterThan(0);
    }

    @Test
    void shouldCreateJobAndMultipleActionsThenVerifyActionListSizeNotEmpty() throws SchedulerException {
        JobDetails jobDetails = getJobDetails(MESSAGE_BODY, jobName, CRON_EACH_MINUTE, LocalDateTime.now());

        emailService.createJob(jobDetails);
        managerService.startJobNow(jobName);
        emailService.pauseJob(jobName);
        emailService.resumeJob(jobName);
        managerService.unScheduleJob(jobName);
        List<Map<String, Object>> jobList = managerService.getAllJobs();

        assertThat(jobList.size()).isGreaterThan(0);
        assertThat(jobList.get(0).get("jobStatus")).isEqualTo("RUNNING");
    }
}
