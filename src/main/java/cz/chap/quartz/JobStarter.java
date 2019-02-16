package cz.chap.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JobStarter {

    private static Logger logger = LoggerFactory.getLogger(JobStarter.class);

    public static void runJob(Scheduler scheduler, Class <? extends Job> jobClass, String jobId, Map<String,Object> data, int second) {
        JobDetail jobDetail = buildJobDetail(jobClass, jobId, data);
        Trigger trigger = buildJobTrigger(jobDetail, second );
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            logger.error(ex.getMessage());
        }
    }

    private static JobDetail buildJobDetail(Class <? extends Job> jobClass, String jobId, Map<String, Object> data) {
        JobDataMap jobDataMap = new JobDataMap();
        if (data != null) {
            jobDataMap.putAll(data);
        }

        return JobBuilder.newJob(jobClass)
                .withIdentity(jobId, "jobs")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private static Trigger buildJobTrigger(JobDetail jobDetail, int seconds) {

        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .repeatForever()
                .withMisfireHandlingInstructionFireNow()
                .withIntervalInSeconds(seconds);

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "triggers")
                .startAt(Date.from(Instant.now()))
                .withSchedule(scheduleBuilder)
                .build();
    }
}
