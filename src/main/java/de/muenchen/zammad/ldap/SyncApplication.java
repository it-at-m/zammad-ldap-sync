package de.muenchen.zammad.ldap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import de.muenchen.zammad.ldap.service.ZammadSyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan()
public class SyncApplication {

	public static void main(String[] args) {

		var context = SpringApplication.run(SyncApplication.class, args);
		var syncService = context.getBean(ZammadSyncService.class);
		if (syncService.checkRoleAssignments())
			syncService.syncSubtreeByDn();
		else
			log.error("Roles not found. Check if roles referenced in application properties exist in Zammad (e.g. Agent, Erstellen, Vollzugriff).");
	}

}
