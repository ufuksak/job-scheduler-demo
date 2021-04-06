package com.usakar.jobs.demo.service;

import static org.quartz.JobKey.jobKey;

import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import com.usakar.jobs.demo.util.JobUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Scheduler scheduler;
    private final JobStatusRepository jobStatusRepository;

    private static final String ERROR_MESSAGE = "Action is:  %1$s failed, error: %2$s";
    private static final String SUCCESS_MESSAGE = "Action is:  %1$s success";

    /**
     * Create Job
     * @param jobDetails
     * @return
     * @throws SchedulerException
     */
    public JobDetails createJob(final JobDetails jobDetails) throws SchedulerException {
        JobDetail jobDetail = JobUtil.buildJobDetail(jobDetails);
        Set<Trigger> triggersForJob = jobDetails.buildTriggers();
        scheduler.scheduleJob(jobDetail, triggersForJob, false);
        log.info("job is with key - {}", jobDetail.getKey());
        JobUtil.addJobActionRecord(jobStatusRepository, jobDetails.getName(), "CREATE");
        return jobDetails;
    }

    /**
     * Find Job
     * @param jobName
     * @return
     */
    public Optional<JobDetails> findJob(final String jobName) {
        Optional<JobDetails> jobDetails = Optional.empty();
        try {
            verifyJobExists(jobName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey(jobName));
            if (Objects.nonNull(jobDetail)) {
                jobDetails = Optional.of(
                        JobUtil.buildDescriptor(jobDetail,
                                scheduler.getTriggersOfJob(jobKey(jobName))));
            }
            JobUtil.addJobActionRecord(jobStatusRepository, jobName, "FIND");
        } catch (SchedulerException e) {
            log.error("fail to find job with key - {} , error - {}", jobName, e.getLocalizedMessage());
            return jobDetails;
        }
        return jobDetails;
    }

    /**
     * Update Job
     * @param name
     * @param descriptor
     * @return
     * @throws SchedulerException
     */
    public JobDataMap updateJob(
            final String name,
            final JobDetails descriptor) throws SchedulerException {
        JobDetail oldJobDetail = scheduler.getJobDetail(jobKey(name));
        JobDataMap jobDataMap = new JobDataMap();
        if (Objects.nonNull(oldJobDetail)) {
            jobDataMap = oldJobDetail.getJobDataMap();
            jobDataMap.put("subject", descriptor.getSubject());
            jobDataMap.put("messageBody", descriptor.getMessageBody());
            jobDataMap.put("to", descriptor.getTo());
            jobDataMap.put("cc", descriptor.getCc());
            jobDataMap.put("bcc", descriptor.getBcc());
            JobBuilder jb = oldJobDetail.getJobBuilder();
            JobDetail newJobDetail = jb.usingJobData(jobDataMap).storeDurably().build();
            scheduler.addJob(newJobDetail, true);
            JobUtil.addJobActionRecord(jobStatusRepository, name, "UPDATE");
            log.info("Updated job key - {}", newJobDetail.getKey());
        }
        return jobDataMap;
    }

    /**
     * Delete Job
     * @param name
     * @return
     */
    public String deleteJob(final String name) {
        String action = "delete";
        try {
            verifyJobExists(name);
            scheduler.deleteJob(jobKey(name));
            log.info("Deleted job with key - {}", name);
            JobUtil.addJobActionRecord(jobStatusRepository, name, "DELETE");
            return String.format(SUCCESS_MESSAGE, action);
        } catch (SchedulerException e) {
            log.error("Could not delete job with key - {} due to error - {}", name, e.getLocalizedMessage());
            return String.format(ERROR_MESSAGE, action, e.getMessage());
        }
    }

    /**
     * Pause Job
     * @param name
     * @return
     */
    public String pauseJob(final String name) {
        String action = "pause";
        try {
            verifyJobExists(name);
            scheduler.pauseJob(jobKey(name));
            log.info("Paused job with key - {}", name);
            JobUtil.addJobActionRecord(jobStatusRepository, name, "PAUSE");
            return String.format(SUCCESS_MESSAGE, action);
        } catch (SchedulerException e) {
            log.error("Could not pause job with key - {} due to error - {}", name, e.getLocalizedMessage());
            return String.format(ERROR_MESSAGE, action, e.getMessage());
        }
    }

    /**
     * Resume Job
     * @param name
     * @return
     */
    public String resumeJob(final String name) {
        String action = "resume";
        try {
            verifyJobExists(name);
            scheduler.resumeJob(jobKey(name));
            log.info("Resumed job with key - {}", name);
            JobUtil.addJobActionRecord(jobStatusRepository, name, "RESUME");
            return String.format(SUCCESS_MESSAGE, action);
        } catch (SchedulerException e) {
            log.error("Could not resume job with key - {} due to error - {}", name, e.getLocalizedMessage());
            return String.format(ERROR_MESSAGE, action, e.getMessage());
        }
    }

    private void verifyJobExists(final String name) throws SchedulerException {
        if (!scheduler.checkExists(jobKey(name))) {
            throw new SchedulerException(String.format("No Job Exists with %1$s", name));
        }
    }
}
