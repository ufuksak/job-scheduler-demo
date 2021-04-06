package com.usakar.jobs.demo.model;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class TriggerDetails {

    @NotBlank
    private String name;
    private LocalDateTime fireTime;
    private String cron;
}
