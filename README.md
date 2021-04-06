# h2 console
http://localhost:8080/h2-console
# h2 configurations
https://www.baeldung.com/spring-boot-h2-database
# quartz reference project
https://github.com/quartz-scheduler/quartz/blob/quartz-2.3.1/quartz-jobs/src/main/java/org/quartz/jobs/ee/mail/SendMailJob.java
# table queries
```
SELECT * FROM QRTZ_BLOB_TRIGGERS;
SELECT * FROM QRTZ_CALENDARS;
SELECT * FROM QRTZ_CRON_TRIGGERS;
SELECT * FROM QRTZ_FIRED_TRIGGERS;
SELECT * FROM QRTZ_JOB_DETAILS ;
SELECT * FROM QRTZ_LOCKS ;
SELECT * FROM QRTZ_PAUSED_TRIGGER_GRPS ;
SELECT * FROM QRTZ_SCHEDULER_STATE ;
SELECT * FROM QRTZ_SIMPLE_TRIGGERS ;
SELECT * FROM QRTZ_SIMPROP_TRIGGERS ;
SELECT * FROM QRTZ_TRIGGERS ;
SELECT * FROM JOB_STATUS ;
```

# Flexibility
The types of possible actions performed by the Jobs are not known to the Job Management System. In the future, new Jobs should be supported without re-developing the Job Management System (optional).
JobSystem does not which Job class is called when the schedule time seen in the system. Any job requests are
 registered to system and then the registered job routines are searched and triggered 
# Reliability
Each Job should either complete successfully or perform no action at all. (I.e. there should be no side-effects created by a Job that fails.)
Any of Jobs can work async with given Job class name. For instance, If EMailSchedulerJob class has failures to
 process the job, it does not effect the workflow of OneTimeJob class processing. Any of new Job classes can be
  easily adapted to the backbone.
# Internal Consistency
At any one time a Job has one of four states: QUEUED, RUNNING, SUCCESS, FAILED. Following the execution of a Job, it should be left in an appropriate state.
JobUtil.proceedJobList method has the list of all Quartz Job statuses.
# Scheduling
A Job can be executed immediately(oneTimeSchedule endpoint) or according to a schedule.(jobs endpoint)

# Sample Workflow
1. Call createJob endpoint with a Json RequestBody
1. Call job-find endpoint to verify whether the job is created or not
1. Call updateJob endpoint to verify whether Job details are updatable or not
1. Call pause job to stop the job
1. Call manager jobs endpoint to check the PAUSED job existed
1. Call resume job to restart the job
1. Call manager jobs endpoint to check the QUEUED job existed
1. Call oneTimeSchedule endpoint and check the email is delivered asap
1. Call manager jobs endpoint to check the any second job is registered to the system (immediate email sending is
 done on the fly)
1. Call manager start endpoint to run scheduled registered job asap and check the mail inbox, a new mail delivered.
1. Call unScheduleJob endpoint to stop scheduling on the existing registered job.
1. Call getAllJobActions endpoint to monitor all the listed actions are recorded to h2 database, the data could be
 used report the all activities in the system.

##### Notes
1. Sample code formatting PMD, checkstyle files are added, log4j2 logging config added
1. application.properties covers the all config parameters in one place. Environmental parameters could overwrite the
 existing given configs
1. For possible endpoint calls with sample, quartz.postman_collection.json can be imported to postman application
1. All Unit test classes cover the cases with corner cases, Approximately %100 coverage is provided. 
1. data.sql file has the table mappings for h2 db config tables.

##### Drive link
https://drive.google.com/file/d/12MAv8q0zXXmIYy8tTKWRtOhKzUDvEZYB/view
##### Github repo link
https://github.com/ufuksak/job-scheduler-demo
