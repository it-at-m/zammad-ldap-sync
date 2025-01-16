package de.muenchen.zammad.ldap.sync;

import java.util.HashMap;
import java.util.List;
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
public class TreeControl {

    private RequestedOrganizationalUnits requestedOrgUnits;

    private LdapTreeService zammadLdapService;

    private TreeSynchronization subtree;

    private DeletedLdapUser deletedLdapUser;

    private GroupAssignmentAuthorizations groupAssignmentAuthorizations;

    private RequestedDistinguishedNames dnValidation;

    /**
     * Use the requested ldap distinguished names to determine the organizational unit ldap (shade) trees.
     * Warn if not for all required distinguished names a shade tree exists.
     * Synchronize each shade tree branching with Zammad parent group attribute.
     * Synchronize Zammad domain group attributes and assigned group users with organizational units in each shade tree.
     * Add/update Zammad domain user attributes.
     * Update Zammad assignment role for each role.
     */
    public void synchronizationControl() {

        List<String> ldapDistinguishedNames = requestedOrgUnits.flatMapDistinguishedNames();
        log.info("OuBases :");
        ldapDistinguishedNames.forEach(dn -> log.info("   {}", dn));

        log.info("Start sychronize Zammad groups, user and roles ...");

        log.debug("1/4 Start LDAP operations ...");
        Map<String, LdapOuNode> ldapShadeTrees = zammadLdapService.buildLdapTrees(null, requestedOrgUnits);
        Map<String, EnhancedLdapUserDto> completeLdapUser = collectUserFromAllBranches(ldapShadeTrees);

        dnValidation.warnAboutIncompleteness(ldapDistinguishedNames, ldapShadeTrees);

        for (Map.Entry<String, LdapOuNode> entry : ldapShadeTrees.entrySet()) {

            log.info("Begin synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

            log.trace(entry.getValue().toString());

            log.debug("2/4 Update zammad groups and users ...");
            var map = new HashMap<String, LdapOuNode>();
            map.put(entry.getKey(), entry.getValue());
            subtree.updateZammadGroupsWithUsers(map);

            log.debug("3/4 Mark user for deletion ...");
            deletedLdapUser.checkForRemoval(entry.getValue().findLdapOuNode(entry.getKey()), completeLdapUser);

            log.info("End sychronize Zammad groups and users with ouBase : {}.", entry.getKey());
        }

        if (!ldapShadeTrees.isEmpty()) {
            log.debug("4/4 Sync assignment roles for all ouBases ...");
            groupAssignmentAuthorizations.assignRoleAuthorizations();
        }

        log.info("End sychronize Zammad groups, user and roles all ouBases.");

    }

    public static Map<String, EnhancedLdapUserDto> collectUserFromAllBranches(
            Map<String, LdapOuNode> ldapShadetrees) {

        Map<String, EnhancedLdapUserDto> collection = new TreeMap<>();
        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {
            collection.putAll(entry.getValue().flatListLdapUserDTO().stream()
                    .collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity())));
        }
        return collection;
    }

}
