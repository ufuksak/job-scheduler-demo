package com.usakar.jobs.demo.service;

import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.model.JobDetails;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public interface ManagerService {

    boolean scheduleOneTimeJob(
            final String jobName,
            final Class<? extends QuartzJobBean> jobClass,
            final Date date,
            final JobDetails jobDetails) throws SchedulerException;

    boolean unScheduleJob(final String jobName) throws SchedulerException;

    boolean startJobNow(final String jobName) throws SchedulerException;

    boolean isJobWithNamePresent(final String jobName) throws SchedulerException;

    List<Map<String, Object>> getAllJobs() throws SchedulerException;

    boolean isJobRunning(final String jobName) throws SchedulerException;

    List<JobStatus> getAllJobActions();
}
