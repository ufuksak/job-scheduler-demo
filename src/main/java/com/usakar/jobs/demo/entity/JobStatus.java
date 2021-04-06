package com.usakar.jobs.demo.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Entity
@Table(name = "JOB_STATUS")
@NoArgsConstructor
@AllArgsConstructor
public class JobStatus {

    @Id
    @GeneratedValue
    @Column(name = "JOB_ID")
    private Integer jobId;
    @NonNull
    @Column(name = "JOB_NAME")
    private String jobName;
    @Column(name = "ACTION_TIME")
    private ZonedDateTime actionTime;
    @Column(name = "ACTION_TYPE")
    private String type;
}
