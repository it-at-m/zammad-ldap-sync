package de.muenchen.mpdz.zammad.ldap.scheduler;

import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class SchedulerContext {

	@Bean
	public JobDetailFactoryBean jobDetail() {
	    var jobDetailFactory = new JobDetailFactoryBean();
	    jobDetailFactory.setJobClass(SyncJob.class);
	    jobDetailFactory.setDescription("Invoke Zammad Ldap Synchronisation");
	    jobDetailFactory.setDurability(true);
	    return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean cron(JobDetail job, SyncProperties properties) {

		var cron = new CronTriggerFactoryBean();
		cron.setJobDetail(job);
		cron.setGroup("ZammadLdapSynchronisation");
		cron.setCronExpression(properties.getCronExpression());
		return cron;
	}

}
