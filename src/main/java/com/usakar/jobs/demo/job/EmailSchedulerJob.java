package com.usakar.jobs.demo.job;

import com.usakar.jobs.demo.util.JobUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
public class EmailSchedulerJob implements Job {

    private JavaMailSender mailSender;
    private String status = "";

    public EmailSchedulerJob(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void execute(JobExecutionContext context) {
        log.info("email send Job triggered ");
        JobDataMap map = context.getMergedJobDataMap();
        status = JobUtil.sendEmail(map, mailSender);
        log.info("completed Job");
    }

    public String getStatus() {
        return status;
    }
}
