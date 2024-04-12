package de.muenchen.mpdz.zammad.ldap.service;

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
