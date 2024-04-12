package de.muenchen.mpdz.zammad.ldap.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

	private String dateTimeFormat = "yyyyMMddHHmmss" ;
	private Integer dateTimeMinusDay = 7;
	private String ouBase = "Set up ou base";
	private String cronExpression = "0 15 2 ? * *"; // Default once a day at 2:15 AM

}
