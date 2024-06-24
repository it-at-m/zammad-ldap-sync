package de.muenchen.mpdz.zammad.ldap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import de.muenchen.mpdz.zammad.ldap.service.ZammadSyncService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan()
public class SyncApplication {

	public static void main(String[] args) {

		var context = SpringApplication.run(SyncApplication.class, args);
		var syncService = context.getBean(ZammadSyncService.class);
		if (syncService.isRoleIdErstellen() && syncService.isRoleIdVollzugriff())
			syncService.syncSubtreeByDn();
		else
			log.error("Does zammad role id 'id-erstellen' / 'id-vollzugriff' contain role name 'Erstellen' / 'Vollzugriff' ?");
	}

}
