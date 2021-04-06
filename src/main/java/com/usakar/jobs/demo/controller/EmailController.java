package com.usakar.jobs.demo.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Email Jobs Rest Controller
 */
@RestController
@RequestMapping("api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final static String ERROR = "error";

    @Lazy
    private final EmailService emailService;

    @PostMapping(path = "/jobs/createJob")
    public ResponseEntity<JobDetails> createJob(@RequestBody JobDetails jobDetails) throws SchedulerException {
        return new ResponseEntity<>(emailService.createJob(jobDetails), CREATED);
    }

    @GetMapping(path = "/jobs/getJob/{name}")
    public ResponseEntity<JobDetails> getJob(
            @PathVariable String name) {
        return emailService.findJob(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/jobs/updateJob/{name}")
    public ResponseEntity<String> updateJob(
            @PathVariable String name,
            @RequestBody JobDetails descriptor) throws SchedulerException {
        emailService.updateJob(name, descriptor);
        return new ResponseEntity<>("Job Updated", HttpStatus.OK);
    }

    @DeleteMapping(path = "/jobs/deleteJob/{name}")
    public ResponseEntity<String> deleteJob(@PathVariable String name) {
        String response = emailService.deleteJob(name);
        return new ResponseEntity<>(response,
                response.contains(ERROR) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }

    @PatchMapping(path = "/jobs/{name}/pause/")
    public ResponseEntity<String> pauseJob(@PathVariable String name) {
        String response = emailService.pauseJob(name);
        return new ResponseEntity<>(response,
                response.contains(ERROR) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }

    @PatchMapping(path = "/jobs/{name}/resume")
    public ResponseEntity<String> resumeJob(@PathVariable String name) {
        String response = emailService.resumeJob(name);
        return new ResponseEntity<>(response,
                response.contains(ERROR) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }
}
