package de.muenchen.zammad.ldap.service.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationalUnit {

		List<String> distinguishedNames;
		String userSearchBase;
		String ouSearchBase;

}
