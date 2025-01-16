package de.muenchen.zammad.ldap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import de.muenchen.zammad.ldap.config.RoleIds;
import de.muenchen.zammad.ldap.sync.TreeControl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan()
public class SyncApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(SyncApplication.class, args);

		RoleIds complementRoleIds = context.getBean(RoleIds.class);
		if (complementRoleIds.isAddAllRoleIdsSuccessful()) {
		    TreeControl syncService = context.getBean(TreeControl.class);
			syncService.synchronizationControl();;
		}
		else
			log.error("Roles not found. Check if roles referenced in application properties exist in Zammad (e.g. Agent, Erstellen, Vollzugriff).");
	}

}
