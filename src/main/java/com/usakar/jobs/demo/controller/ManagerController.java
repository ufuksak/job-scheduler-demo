package com.usakar.jobs.demo.controller;

import com.usakar.jobs.demo.entity.JobStatus;
import com.usakar.jobs.demo.job.OneTimeJob;
import com.usakar.jobs.demo.model.JobDetails;
import com.usakar.jobs.demo.model.ServerResponse;
import com.usakar.jobs.demo.service.ManagerService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/manager/")
@AllArgsConstructor
public class ManagerController {

    @Lazy
    private final ManagerService managerService;

    @RequestMapping("oneTimeSchedule")
    public ServerResponse schedule(@RequestBody JobDetails jobDetails) throws SchedulerException {
        Date date = new Date();
        managerService.scheduleOneTimeJob(jobDetails.getName(), OneTimeJob.class, date, jobDetails);
        return getServerResponse(managerService.getAllJobs());
    }

    @RequestMapping("getAllJobActions")
    public List<JobStatus> getAllJobActions() {
        return managerService.getAllJobActions();
    }

    @RequestMapping("unScheduleJob")
    public ResponseEntity<String> unScheduleJob(@RequestParam("jobName") String jobName) throws SchedulerException {
        managerService.unScheduleJob(jobName);
        return new ResponseEntity<>("Job UnScheduled", HttpStatus.OK);
    }

    @RequestMapping("jobs")
    public ServerResponse getAllJobs() throws SchedulerException {
        List<Map<String, Object>> list = managerService.getAllJobs();
        return getServerResponse(list);
    }

    @RequestMapping("start")
    public ServerResponse startJobNow(@RequestParam("jobName") String jobName) throws SchedulerException {
        managerService.startJobNow(jobName);
        return getServerResponse(true);
    }

    private ServerResponse getServerResponse(Object data) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatusCode(200);
        serverResponse.setData(data);
        return serverResponse;
    }
}
