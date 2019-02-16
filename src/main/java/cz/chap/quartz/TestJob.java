package cz.chap.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PersistJobDataAfterExecution
public class TestJob implements Job {

    private Logger logger = LoggerFactory.getLogger(TestJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("start");

        JobDataMap data = context.getJobDetail().getJobDataMap();

        int count = 0;
        if (data.containsKey("count")) {
            count = data.getInt("count");
        }
        data.put("count", count++);

        logger.info("start");
        logger.info("--------job " + context.getJobDetail().getKey().getName() + " ...  count: " + count
                + " time: " + System.currentTimeMillis());
    }
}
