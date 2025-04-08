package de.muenchen.zammad.ldap.branch;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import de.muenchen.userservice.LdapOuNode;
import de.muenchen.userservice.LdapService;
import de.muenchen.zammad.property.LdapProperty;
import de.muenchen.zammad.property.RequestedOrganizationalUnits;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Service
@Getter
public class LdapTreeService {

	private LdapProperty ldapProperty;

	public Map<String, LdapOuNode> buildLdapTrees(String dateTime,
			RequestedOrganizationalUnits organizationalUnits) {

		Map<String, LdapOuNode> shadeTrees = new TreeMap<>();
		if (organizationalUnits.getOrganizationalUnits() != null) {
			organizationalUnits.getOrganizationalUnits().forEach((k, v) -> {
				if (!v.getOuSearchBase().trim().isEmpty()) {
					var service = new LdapService(ldapProperty.getUrl(), "", "", v.getUserSearchBase(), v.getOuSearchBase());
					for (String dn : v.getDistinguishedNames()) {
						Optional<Map<String, LdapOuNode>> tree = service.buildSubtree(k, dn, dateTime);
						  tree.ifPresent(shadeTrees::putAll);
					}
				}
			});
		}
		return shadeTrees;
	}

}
