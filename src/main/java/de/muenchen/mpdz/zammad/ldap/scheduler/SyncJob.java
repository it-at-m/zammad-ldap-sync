package de.muenchen.mpdz.zammad.ldap.scheduler;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import de.muenchen.mpdz.zammad.ldap.service.ZammadSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SyncJob implements Job {

	private ZammadSyncService myService;

	private SyncProperties syncProperties;

	public void execute(JobExecutionContext context) throws JobExecutionException {

		log.info("Scheduler starts synchronisation at : " + context.getFireTime().toString());
		this.myService.syncSubtreeByDn(syncProperties.getOuBase(), createModifyTimestamp());
	}

	private String createModifyTimestamp() {
		var ldapFormatter = DateTimeFormatter.ofPattern(syncProperties.getDateTimeFormat());
		return ldapFormatter.format(LocalDateTime.now().minusDays(syncProperties.getDateTimeMinusDay()).atOffset(ZoneOffset.UTC));
	}

}