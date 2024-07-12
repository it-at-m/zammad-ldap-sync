package de.muenchen.zammad.ldap.service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ZammadRoleProperties {

	/*
	 * Default values in application.yaml
	 */
	private String nameAgent;
	private String nameErstellen;
	private String nameVollzugriff;

	private Integer idAgent;
	private Integer idErstellen;
	private Integer idVollzugriff;

}
