package com.usakar.jobs.demo.service;

import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import com.usakar.jobs.demo.util.JobUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final Scheduler scheduler;
    private final JobStatusRepository jobStatusRepository;

    @Autowired
    private ApplicationContext context;

    /**
     * Schedule a job by jobName at given date.
     */
    @Override
    public boolean scheduleOneTimeJob(
            final String jobName,
            final Class<? extends QuartzJobBean> quartzJobBean,
            final Date date,
            final JobDetails jobDetails) throws SchedulerException {

        JobDetail jobDetail = JobUtil.createJob(quartzJobBean, context, jobName, jobDetails);

        log.debug("creating trigger for key :" + jobName + " at date :" + date);
        Trigger cronTriggerBean = JobUtil
                .createOneTimeTrigger(jobName, date);
        JobUtil.addJobActionRecord(jobStatusRepository, jobName, "ONE_TIME_JOB");
        return proceedScheduleJob(jobName, jobDetail, cronTriggerBean);
    }

    /**
     * unSchedule jobs
     */
    @Override
    public boolean unScheduleJob(final String jobName) throws SchedulerException {
        TriggerKey tkey = new TriggerKey(jobName);
        log.debug("Parameters received for unscheduled job : tkey :" + jobName);
        boolean status = scheduler.unscheduleJob(tkey);
        log.debug("Trigger associated with jobKey :" + jobName + " unscheduled with status :" + status);
        JobUtil.addJobActionRecord(jobStatusRepository, jobName, "UN_SCHEDULE_JOB");
        return status;
    }

    /**
     * Start a job now
     */
    @Override
    public boolean startJobNow(final String jobName) throws SchedulerException {
        JobKey jKey = new JobKey(jobName);
        log.debug("Parameters received for starting job now : jobKey :" + jobName);
        scheduler.triggerJob(jKey);
        log.debug("Job with jobKey :" + jobName + " started now succesfully.");
        JobUtil.addJobActionRecord(jobStatusRepository, jobName, "START_JOB_NOW");
        return true;
    }

    /**
     * Check job exist with given name
     */
    @Override
    public boolean isJobWithNamePresent(final String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        return scheduler.checkExists(jobKey);
    }

    /**
     * get all jobs
     */
    @Override
    public List<Map<String, Object>> getAllJobs() throws SchedulerException {
        return JobUtil.getAllJobs(scheduler);
    }

    /**
     * check job running
     */
    public boolean isJobRunning(final String jobName) throws SchedulerException {
        return JobUtil.isJobRunning(jobName, scheduler);
    }

    /**
     * List all job actions
     */
    public List<JobStatus> getAllJobActions() {
        return jobStatusRepository.findAll();
    }

    private boolean proceedScheduleJob(
            final String jobName,
            final JobDetail jobDetail,
            final Trigger cronTriggerBean) throws SchedulerException {

        Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
        log.debug("Job with key jobKey :" + jobName + " scheduled successfully for date :" + dt);
        return true;
    }
}
