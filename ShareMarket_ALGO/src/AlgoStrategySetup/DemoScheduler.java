package AlgoStrategySetup;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.SimpleTrigger;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;
 
/**
 * This is a sample class to execute scheduler on specific date based on
 * <code>java.util.Calendar</code>. Over here, <code>executor</code> is a
 * runnable which run on everyday basis from 8:00 AM to 5:00 PM.
 * 
 * @author Chintan Patel
 */

public class DemoScheduler {
	static String upstoxEntry="UPSTOX", zerodhaEntry="ZEntry", zerodhaExit="ZExit";
	
    public static void main(String[] args) {
    	DemoScheduler demo = new DemoScheduler();
    	try {
			demo.CronJob();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void CronJob() throws SchedulerException{
    	SchedulerFactory sf=new StdSchedulerFactory();
        Scheduler sched=sf.getScheduler();
        
        JobDetail job = newJob(Selenium.class)
        	    .withIdentity(Selenium.upstox, "group1")
        	    .build();
        CronTrigger trigger = newTrigger()
        	    .withIdentity("trigger1", "group1")
        	    .withSchedule(cronSchedule("0 21 23 1/1 * ? *"))
        	    .build();

//        sched.scheduleJob(job, trigger);
//        sched.start();
        //---------------------------
        job = newJob(Selenium.class)
        	    .withIdentity(Selenium.zEntry, "group2")
        	    .build();
        trigger = newTrigger()
        	    .withIdentity("trigger2", "group2")
        	    .withSchedule(cronSchedule("0 44 23 1/1 * ? *"))
        	    .build();

        sched.scheduleJob(job, trigger);
        sched.start();
        //---------------------------
        job = newJob(Selenium.class)
        	    .withIdentity(Selenium.zExit, "group3")
        	    .build();
        trigger = newTrigger()
        	    .withIdentity("trigger3", "group3")
        	    .withSchedule(cronSchedule("0 55 23 1/1 * ? *"))
        	    .build();

        sched.scheduleJob(job, trigger);
        sched.start();
    }
}
