package de.muenchen.zammad.ldap.service.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZammadUrlProperties {

	private String base;
	private String groups;
	private String users;
	private String roles;

}
