package com.usakar.jobs.demo.service;

import com.usakar.jobs.demo.model.JobDetails;
import java.util.Optional;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

public interface EmailService {

    JobDetails createJob(JobDetails descriptor) throws SchedulerException;

    Optional<JobDetails> findJob(String name);

    JobDataMap updateJob(String name, JobDetails descriptor) throws SchedulerException;

    String deleteJob(String name);

    String pauseJob(String name);

    String resumeJob(String name);
}
