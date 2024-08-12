package de.muenchen.zammad.ldap.service;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.muenchen.zammad.ldap.service.config.LdapSearch;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import de.muenchen.zammad.ldap.tree.LdapService;
import lombok.Getter;


@Service
public class ZammadLdapService {

	@Getter
	@Value("${ldap.url}")
    private String ldapUrl;

    public Map<String, LdapOuNode> buildLdapTreesWithDistinguishedNames(String dateTime,
			LdapSearch organizationalUnits) {

		Map<String, LdapOuNode> shadeTrees = new TreeMap<String, LdapOuNode>();
		organizationalUnits.getOrganizationalUnits().forEach((k, v) -> {
			if (! v.getOuSearchBase().trim().isEmpty()) {
				var service = new LdapService(getLdapUrl(), "", "", v.getUserSearchBase(), v.getOuSearchBase());
				for (String dn : v.getDistinguishedNames()) {
					var tree = service.createSubtreeWithUsers(dn, dateTime);
					if (tree.isPresent())
						shadeTrees.putAll(tree.get());
				}
			}
		});
		return shadeTrees;
	}


}
