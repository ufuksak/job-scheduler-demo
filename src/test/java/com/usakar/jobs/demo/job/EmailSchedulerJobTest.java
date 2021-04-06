package com.usakar.jobs.demo.job;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.impl.JobExecutionContextImpl;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

@AllArgsConstructor
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:test.properties")
class EmailSchedulerJobTest {

    private static final String MESSAGE_BODY = "messageBody";
    private static final String SUBJECT = "subject";

    private MimeMessage mimeMessage;
    private JavaMailSender javaMailSender;

    private EmailSchedulerJob emailSchedulerJob;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage((Session)null);
        javaMailSender = mock(JavaMailSender.class);
        emailSchedulerJob = new EmailSchedulerJob(javaMailSender);
    }

    @Test
    void shouldSendMailThenVerifyStatusSuccess() {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        JobExecutionContext jobExecutionContext = getJobExecutionContext();

        emailSchedulerJob.execute(jobExecutionContext);

        assertThat(emailSchedulerJob.getStatus()).isEqualTo("SUCCESS");
    }

    @Nested
    class CornerCases {
        @Test
        void shouldFailSendMailThenVerifyStatusFail() {
            JobExecutionContext jobExecutionContext = getJobExecutionContext();

            emailSchedulerJob.execute(jobExecutionContext);

            assertThat(emailSchedulerJob.getStatus()).isEqualTo("FAIL");
        }
    }

    private JobExecutionContext getJobExecutionContext() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("subject", SUBJECT);
        jobDataMap.put("messageBody", MESSAGE_BODY);
        jobDataMap.put("to", Collections.singletonList("testtopuptest@gmail.com"));
        jobDataMap.put("cc", Collections.singletonList("test1@gmail.com"));
        jobDataMap.put("bcc", Collections.singletonList("test2@gmail.com"));
        JobExecutionContext jobExecutionContext = mock(JobExecutionContextImpl.class);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        return jobExecutionContext;
    }
}
