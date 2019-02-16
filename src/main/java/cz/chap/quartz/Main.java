package cz.chap.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            for (int i =0; i< 50; i++) {
                // generate set of jobs
                //JobStarter.runJob(scheduler, TestJob.class, "testJob-" + i, new HashMap<>(), 5);
            }

            scheduler.start();

            //scheduler.shutdown();

        } catch (SchedulerException se) {
            logger.error(se.getMessage());
        }
    }
}
