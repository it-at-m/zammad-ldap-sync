package de.muenchen.zammad.ldap.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Validation {

	void checkOuBases(List<String> ldapSyncDistinguishedNames, Map<String, LdapOuNode> ldapShadetrees) {

		try {

			if (ldapShadetrees.size() != ldapSyncDistinguishedNames.size()) {

				var trees = Arrays.asList(ldapShadetrees.keySet().toArray());
				var differences = ldapSyncDistinguishedNames.stream().filter(element -> !trees.contains(element))
						.collect(Collectors.toList());

				var sb = new StringBuilder();
				sb.append(System.lineSeparator() + System.lineSeparator());
				sb.append(
						" !!!  No ldap nodes for all ouBases found. Please check the ouBase(s) (ldap distinguished name) availability. Maybe part of a distinguished name was renamed in ldap :");
				sb.append(System.lineSeparator());
				differences.forEach(dn -> {
					sb.append(" !!!    - ");
					sb.append(dn);
					sb.append(System.lineSeparator());
				});

				log.error(sb.toString());
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
