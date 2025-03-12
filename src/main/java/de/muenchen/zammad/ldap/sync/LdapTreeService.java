package de.muenchen.zammad.ldap.sync;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import de.muenchen.zammad.ldap.property.LdapProperty;
import de.muenchen.zammad.ldap.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import de.muenchen.zammad.ldap.tree.LdapService;
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
