package com.usakar.jobs.demo.job;

import com.usakar.jobs.demo.service.ManagerService;
import com.usakar.jobs.demo.util.JobUtil;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class OneTimeJob extends QuartzJobBean implements InterruptableJob {

    private static volatile boolean toStopFlag = true;

    private JavaMailSender mailSender;

    public OneTimeJob(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    ManagerService managerService;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        log.info(
                "Simple Job key :" + key.getName() + ", Group :" + key.getGroup() + " , Thread :"
                        + Thread.currentThread().getName());

        log.info("All Jobs: " + managerService.getAllJobs());
        List<Map<String, Object>> list = managerService.getAllJobs();
        log.info("Job list :" + list);

        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
        JobUtil.sendEmail(dataMap, mailSender);
    }

    @Override
    public void interrupt() {
        log.info("Stopping thread... ");
        toStopFlag = false;
    }
}
