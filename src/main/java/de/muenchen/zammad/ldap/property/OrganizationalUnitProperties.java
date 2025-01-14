package de.muenchen.zammad.ldap.property;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationalUnitProperties {

		private List<String> distinguishedNames;
		private String userSearchBase;
		private String ouSearchBase;

}
