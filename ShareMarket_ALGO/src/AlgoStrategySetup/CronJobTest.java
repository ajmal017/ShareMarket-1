package AlgoStrategySetup;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

public class CronJobTest implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    JobKey jobKey = context.getJobDetail().getKey();
	    System.out.println(jobKey);
	}
}
