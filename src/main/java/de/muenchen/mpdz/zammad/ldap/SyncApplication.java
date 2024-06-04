package de.muenchen.mpdz.zammad.ldap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import de.muenchen.mpdz.zammad.ldap.service.ZammadSyncService;

@SpringBootApplication
@ConfigurationPropertiesScan()
public class SyncApplication {

	public static void main(String[] args) {

		var context = SpringApplication.run(SyncApplication.class, args);
		var syncService = context.getBean(ZammadSyncService.class);
		syncService.syncSubtreeByDn();

	}

}
