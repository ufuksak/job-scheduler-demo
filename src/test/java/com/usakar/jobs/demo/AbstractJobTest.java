package com.usakar.jobs.demo;

import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.model.TriggerDetails;
import java.time.LocalDateTime;
import java.util.Collections;

public abstract class AbstractJobTest {

    protected static final String SUCCESS_MESSAGE = "Action is:  %1$s success";
    protected static final String ERROR_MESSAGE = "Action is:  %1$s failed, error: %2$s";
    protected static final String NO_JOB_EXISTS = "No Job Exists with %1$s";
    protected static final String ACTION_RESUME = "resume";
    protected static final String ACTION_PAUSE = "pause";
    protected static final String ACTION_DELETE = "delete";
    protected static final String MESSAGE_BODY = "messageBody";
    protected static final String MESSAGE_BODY_NEW = "messageBodyNew";
    protected static final String CRON_EACH_MINUTE = "0 * * ? * *";

    protected JobDetails getJobDetails(
            final String messageBody,
            final String jobName,
            final String cronFormula,
            final LocalDateTime triggerTime) {
        TriggerDetails triggerDetails =
                TriggerDetails.builder()
                        .cron(cronFormula)
                        .name(jobName)
                        .fireTime(triggerTime)
                        .build();
        return JobDetails.builder()
                .messageBody(messageBody)
                .to(Collections.singletonList("test@test.com"))
                .name(jobName)
                .subject("subject")
                .triggerDetails(Collections.singletonList(triggerDetails))
                .build();
    }
}
