package com.usakar.jobs.demo.util;

import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.job.EmailSchedulerJob;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.model.TriggerDetails;
import com.usakar.jobs.demo.repository.JobStatusRepository;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Slf4j
@UtilityClass
public class JobUtil {

    /**
     * Create Job.
     */
    public static JobDetail createJob(
            final Class<? extends QuartzJobBean> jobClass,
            final ApplicationContext context,
            final String jobName,
            final JobDetails jobDetails) {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(jobClass);
        jobDetailFactoryBean.setDurability(false);
        jobDetailFactoryBean.setApplicationContext(context);
        jobDetailFactoryBean.setName(jobName);

        JobDataMap jobDataMap = getJobDataMap(jobDetails);
        jobDetailFactoryBean.setJobDataMap(jobDataMap);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    /**
     * Create a Single trigger.
     */
    public static Trigger createOneTimeTrigger(
            final String triggerName,
            final Date startTime) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setName(triggerName);
        simpleTriggerFactoryBean.setStartTime(startTime);
        simpleTriggerFactoryBean.setMisfireInstruction(MISFIRE_INSTRUCTION_FIRE_NOW);
        simpleTriggerFactoryBean.setRepeatCount(0);
        simpleTriggerFactoryBean.afterPropertiesSet();
        return simpleTriggerFactoryBean.getObject();
    }

    /**
     * JobDetail object implementation
     */
    public static JobDetail buildJobDetail(final JobDetails jobDetails) {
        JobDataMap jobDataMap = getJobDataMap(jobDetails);
        return newJob(EmailSchedulerJob.class)
                .withIdentity(jobDetails.getName())
                .usingJobData(jobDataMap)
                .build();
    }

    /**
     * Send email function.
     */
    public static String sendEmail(
            final JobDataMap map,
            final JavaMailSender mailSender) {
        final String subject = map.getString("subject");
        final String messageBody = map.getString("messageBody");
        String status;
        try {
            final List<String> recipientList = (List<String>) map.get("to");
            final List<String> ccList = (List<String>) map.get("cc");
            final List<String> bccList = (List<String>) map.get("bcc");
            status = proceedEmailSend(mailSender, subject, messageBody, recipientList, ccList, bccList);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            log.error("An error occurred: {}", e.getLocalizedMessage());
            status = "FAIL";
        }
        return status;
    }

    private static String proceedEmailSend(
            final JavaMailSender mailSender,
            final String subject,
            final String messageBody,
            final List<String> recipientList,
            final List<String> ccList,
            final List<String> bccList)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, false);
        for (String recipient : recipientList) {
            mimeMessageHelper.setFrom("test@test.com", "Test Mail");
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(messageBody);
            if (!isEmpty(ccList)) {
                mimeMessageHelper.setCc(ccList.toArray(new String[0]));
            }
            if (!isEmpty(bccList)) {
                mimeMessageHelper.setBcc(bccList.toArray(new String[0]));
            }
            mailSender.send(message);
        }
        return "SUCCESS";
    }

    /**
     * Get all jobs
     */
    public List<Map<String, Object>> getAllJobs(final Scheduler scheduler) throws SchedulerException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                list = proceedJobList(scheduler, jobKey
                        , jobName, jobGroup);
            }
        }
        return list;
    }

    /**
     * Check if job is already running
     */
    public boolean isJobRunning(
            final String jobName,
            final Scheduler scheduler) throws SchedulerException {

        log.debug("Parameters received for checking job is running now : jobKey :" + jobName);
        List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
        if (currentJobs != null) {
            for (JobExecutionContext jobExecutionContext : currentJobs) {
                String jobNameDB = jobExecutionContext.getJobDetail().getKey().getName();
                if (jobName.equalsIgnoreCase(jobNameDB)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Build Trigger
     */
    public static Trigger buildTrigger(final TriggerDetails triggerDetails) {
        if (!Strings.isEmpty(triggerDetails.getCron())) {
            if (!isValidExpression(triggerDetails.getCron())) {
                throw new IllegalArgumentException(
                        "Provided expression " + triggerDetails.getCron() + " is not a valid cron expression");
            }
            return newTrigger()
                    .withIdentity(buildName(triggerDetails.getName()))
                    .withSchedule(cronSchedule(triggerDetails.getCron())
                            .withMisfireHandlingInstructionFireAndProceed()
                            .inTimeZone(TimeZone.getTimeZone(systemDefault())))
                    .usingJobData("cron", triggerDetails.getCron())
                    .build();
        } else if (triggerDetails.getFireTime() != null) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("fireTime", triggerDetails.getFireTime());
            return newTrigger()
                    .withIdentity(buildName(triggerDetails.getName()))
                    .withSchedule(simpleSchedule()
                            .withMisfireHandlingInstructionNextWithExistingCount())
                    .startAt(java.sql.Date.from(triggerDetails.getFireTime().atZone(systemDefault()).toInstant()))
                    .usingJobData(jobDataMap)
                    .build();
        }
        throw new IllegalStateException("unsupported trigger descriptor ");
    }

    @SuppressWarnings("unchecked")
    public static JobDetails buildDescriptor(
            final JobDetail jobDetail,
            final List<? extends Trigger> triggersOfJob) {
        List<TriggerDetails> triggerDetails = new ArrayList<>();

        for (Trigger trigger : triggersOfJob) {
            triggerDetails.add(JobUtil.buildDescriptor(trigger));
        }

        return JobDetails.builder().name(jobDetail.getKey().getName())
                .subject(jobDetail.getJobDataMap().getString("subject"))
                .messageBody(jobDetail.getJobDataMap().getString("messageBody"))
                .to((List<String>) jobDetail.getJobDataMap().get("to"))
                .cc((List<String>) jobDetail.getJobDataMap().get("cc"))
                .bcc((List<String>) jobDetail.getJobDataMap().get("bcc"))
                .triggerDetails(triggerDetails).build();
    }

    /**
     * Add job actions to database
     */
    public static void addJobActionRecord(
            final JobStatusRepository jobStatusRepository,
            final String name,
            final String type) {
        jobStatusRepository.save(
                new JobStatus(-1, name, ZonedDateTime.now(), type));
    }

    private static List<Map<String, Object>> proceedJobList(
            final Scheduler scheduler,
            final JobKey jobKey,
            final String jobName,
            final String jobGroup) throws SchedulerException {
        final List<Map<String, Object>> list = new ArrayList<>();
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.size() > 0) {
            Date scheduleTime = triggers.get(0).getStartTime();
            Date nextFireTime = triggers.get(0).getNextFireTime();
            Date lastFiredTime = triggers.get(0).getPreviousFireTime();

            Map<String, Object> map = new HashMap<>();
            map.put("jobName", jobName);
            map.put("groupName", jobGroup);
            map.put("scheduleTime", scheduleTime);
            map.put("lastFiredTime", lastFiredTime);
            map.put("nextFireTime", nextFireTime);

            if (isJobRunning(jobName, scheduler)) {
                map.put("jobStatus", "RUNNING");
            } else {
                String jobState = getJobState(jobName, scheduler);
                map.put("jobStatus", jobState);
            }

            list.add(map);
        }
        return list;
    }

    /**
     * building TriggerDescriptor
     */
    private static TriggerDetails buildDescriptor(final Trigger trigger) {
        return TriggerDetails.builder()
                .name(trigger.getKey().getName())
                .fireTime((LocalDateTime) trigger.getJobDataMap().get("fireTime"))
                .cron(trigger.getJobDataMap().getString("cron")).build();
    }

    /**
     * Get the current state of job
     */
    private String getJobState(
            final String jobName,
            final Scheduler scheduler) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        String state = "";
        if (jobDetail != null && jobDetail.getKey() != null) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (triggers != null && triggers.size() > 0) {
                state = getTriggerState(triggers, scheduler);
            }
        }
        return state;
    }

    private static String getTriggerState(
            final List<? extends Trigger> triggers,
            final Scheduler scheduler) throws SchedulerException {
        String status = "";
        for (Trigger trigger : triggers) {
            TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            if (TriggerState.PAUSED.equals(triggerState)) {
                status = "PAUSED";
                break;
            } else if (TriggerState.COMPLETE.equals(triggerState)) {
                status = "SUCCESS";
                break;
            } else if (TriggerState.BLOCKED.equals(triggerState) || TriggerState.ERROR.equals(triggerState)) {
                status = "FAILED";
                break;
            } else if (TriggerState.NORMAL.equals(triggerState)) {
                status = "QUEUED";
                break;
            }
        }
        return status;
    }

    private static JobDataMap getJobDataMap(final JobDetails jobDetails) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("subject", jobDetails.getSubject());
        jobDataMap.put("messageBody", jobDetails.getMessageBody());
        jobDataMap.put("to", jobDetails.getTo());
        jobDataMap.put("cc", jobDetails.getCc());
        jobDataMap.put("bcc", jobDetails.getBcc());
        return jobDataMap;
    }

    private String buildName(final String name) {
        return Strings.isEmpty(name) ? randomUUID().toString() : name;
    }
}
