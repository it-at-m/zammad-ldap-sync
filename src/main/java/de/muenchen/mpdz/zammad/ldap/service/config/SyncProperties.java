package de.muenchen.mpdz.zammad.ldap.service.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

	Integer dateTimeMinusDay;
	List<String> ouBases;

}
