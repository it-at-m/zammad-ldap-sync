package de.muenchen.zammad.ldap.service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ldap.service.config.GroupAssignmentAuthorizations;
import de.muenchen.zammad.ldap.service.config.LdapSearch;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
@AllArgsConstructor
public class ZammadSyncService {

    private LdapSearch organizationalUnits;

    private ZammadProperties zammadProperties;

    private ZammadService zammadService;

    private ZammadLdapService zammadLdapService;

    private ZammadSyncServiceSubtree subtree;

    private GroupAssignmentAuthorizations groupAssignmentAuthorizations;

    private Validation validation;

    /**
     * Calculate ldap subtree with users based on distinguished name. Add/update
     * zammad groups. Update zammad assignment role for each role. Add/update zammad
     * users.
     */
    public void syncSubtreeByDn() {

        var ldapSyncDistinguishedNames = getOrganizationalUnits().listDistinguishedNames();
        log.info("OuBases :");
        ldapSyncDistinguishedNames.forEach(dn -> log.info("   {}", dn));

        log.info("Start sychronize Zammad groups, user and roles ...");

        log.debug("1/4 Start LDAP operations ...");
        var ldapShadetrees = zammadLdapService.buildLdapTreesWithDistinguishedNames(null, organizationalUnits);
        var allLdapUsers = allLdapUsersWithDistinguishedNames(ldapShadetrees);

        validation.checkOuBases(ldapSyncDistinguishedNames, ldapShadetrees);

        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {

            log.info("Begin synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

            log.trace(entry.getValue().toString());

            log.debug("2/4 Update zammad groups and users ...");
            var map = new HashMap<String, LdapOuNode>();
            map.put(entry.getKey(), entry.getValue());
            getSubtree().updateZammadGroupsWithUsers(map);

            log.debug("3/4 Mark user for deletion ...");
            getSubtree().assignDeletionFlagZammadUser(entry.getValue().findLdapOuNode(entry.getKey()), allLdapUsers);

            log.info("End sychronize Zammad groups and users with ouBase : {}.", entry.getKey());
        }

        if (!ldapShadetrees.isEmpty()) {
            log.debug("4/4 Sync assignment roles for all ouBases ...");
            groupAssignmentAuthorizations.assignRoleAuthorizations();
        }

        log.info("End sychronize Zammad groups, user and roles all ouBases.");

    }

    public static Map<String, EnhancedLdapUserDto> allLdapUsersWithDistinguishedNames(
            Map<String, LdapOuNode> ldapShadetrees) {

        Map<String, EnhancedLdapUserDto> list = new TreeMap<>();
        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {
            list.putAll(entry.getValue().flatListLdapUserDTO().stream()
                    .collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity())));
        }
        return list;
    }

}
