package com.usakar.jobs.demo.repository;

import com.usakar.jobs.demo.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, String> {

}
