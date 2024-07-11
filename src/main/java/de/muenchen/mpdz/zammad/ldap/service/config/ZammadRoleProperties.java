package de.muenchen.mpdz.zammad.ldap.service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ZammadRoleProperties {

	private String nameAgent;
	private String nameErstellen;
	private String nameVollzugriff;

	private Integer idAgent;
	private Integer idErstellen;
	private Integer idVollzugriff;

}
