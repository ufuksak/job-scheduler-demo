package com.usakar.jobs.demo.job;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

@AllArgsConstructor
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:test.properties")
class OneTimeJobTest {

    private JavaMailSender javaMailSender;
    private OneTimeJob oneTimeJob;

    @BeforeEach
    void setUp() {
        oneTimeJob = new OneTimeJob(javaMailSender);
    }

    @Test
    void shouldInterruptThenVerifyToStopFlagFalse() {
        oneTimeJob.interrupt();

        assertThat((Boolean) Whitebox.getInternalState(OneTimeJob.class, "toStopFlag")).isEqualTo(Boolean.FALSE);
    }
}
