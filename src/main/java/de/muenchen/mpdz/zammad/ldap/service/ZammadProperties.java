package de.muenchen.mpdz.zammad.ldap.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "zammad")
public class ZammadProperties {

	private String token;
	private ZammadUrlProperties url;

}
