package de.muenchen.zammad.ldap.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ldap.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class OuTreeControl {

    private RequestedOrganizationalUnits requestedOrgUnits;

    private LdapOuTreeService zammadLdapService;

    private OuTreeSynchronization subtree;

    private GroupAssignmentAuthorizations groupAssignmentAuthorizations;

    private RequestedDnCompleteness validation;

    /**
     * Use the ldap distinguished name to determine the organizational unit ldap (shade) tree.
     * Warn if not for all required distinguished names a shade tree exists.
     * Synchronize shade tree branching with Zammad parent group attribute.
     * Synchronize Zammad domain group attributes and assigned group users with organizational units in shade tree.
     * Add/update Zammad domain user attributes.
     * Update zammad assignment role for each role.
     */
    public void synchronizationControl() {

        var ldapSyncDistinguishedNames = requestedOrgUnits.flatMapDistinguishedNames();
        log.info("OuBases :");
        ldapSyncDistinguishedNames.forEach(dn -> log.info("   {}", dn));

        log.info("Start sychronize Zammad groups, user and roles ...");

        log.debug("1/4 Start LDAP operations ...");
        var ldapShadetrees = zammadLdapService.buildLdapTreesWithDistinguishedNames(null, requestedOrgUnits);
        var allLdapUsers = allLdapUsersWithDistinguishedNames(ldapShadetrees);

        validation.validate(ldapSyncDistinguishedNames, ldapShadetrees);

        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {

            log.info("Begin synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

            log.trace(entry.getValue().toString());

            log.debug("2/4 Update zammad groups and users ...");
            var map = new HashMap<String, LdapOuNode>();
            map.put(entry.getKey(), entry.getValue());
            subtree.updateZammadGroupsWithUsers(map);

            log.debug("3/4 Mark user for deletion ...");
            subtree.assignDeletionFlagZammadUser(entry.getValue().findLdapOuNode(entry.getKey()), allLdapUsers);

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
