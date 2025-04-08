package de.muenchen.zammad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import de.muenchen.zammad.ldap.branch.OrgUnitBranchControl;
import de.muenchen.zammad.property.ZammadRolePropertyComplementation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan()
@ComponentScan(basePackages = {"de.muenchen.userservice", "de.muenchen.zammad" })
public class SyncApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(SyncApplication.class, args);

		ZammadRolePropertyComplementation complementRoleIds = context.getBean(ZammadRolePropertyComplementation.class);
		if (complementRoleIds.completeRoleIdentifierWithRoleId()) {
		    OrgUnitBranchControl syncService = context.getBean(OrgUnitBranchControl.class);
			syncService.synchronizationControl();
		}
		else
			log.error("Roles not found. Check if roles referenced in application properties exist in Zammad (e.g. Agent, Erstellen, Vollzugriff).");
	}

}
