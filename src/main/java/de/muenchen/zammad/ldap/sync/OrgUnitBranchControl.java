package de.muenchen.zammad.ldap.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.muenchen.zammad.ad.ActiveDirectoryGroupService;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryShadeTreeInclusion;
import de.muenchen.zammad.ldap.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class OrgUnitBranchControl {

    private RequestedOrganizationalUnits requestedOrgUnits;

    private LdapTreeService ldapTreeService;

    private OrgUnitBranchSynchronization subtree;

    private EliminatedLdapUser deletedLdapUser;

    private GroupAssignmentAuthorizations groupAssignmentAuthorizations;

    private DistinguishedNameCheck dnValidation;

    private ActiveDirectoryGroupService activeDirectoryGroupService;

    private ActiveDirectoryShadeTreeInclusion activeDirectoryGroups;

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

        log.debug("1/6 Start LDAP operations ...");
        Map<String, LdapOuNode> ldapShadeTrees = ldapTreeService.buildLdapTrees(null, requestedOrgUnits);

        log.debug("2/6 Check completeness of the requested ldap ous ...");
        dnValidation.warnIncompleteness(ldapDistinguishedNames, ldapShadeTrees);

        log.info("3/6 Merge cross functional groups from active directory into 'ldapShadeTree' ...");
        Optional<List<EnhancedActiveDirectoryGroupDTO>> crossOrgGroups = activeDirectoryGroupService.crossOrganizationalGroups();
        activeDirectoryGroups.merge(crossOrgGroups.get(), ldapShadeTrees);

        for (Map.Entry<String, LdapOuNode> entry : ldapShadeTrees.entrySet()) {

            log.info("Begin synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

            log.trace(entry.getValue().toString());

            log.debug("4/6 Update zammad groups and users ...");
            var map = new HashMap<String, LdapOuNode>();
            map.put(entry.getKey(), entry.getValue());
            subtree.updateZammadGroupsWithUsers(map);

            log.debug("5/6 Mark user for deletion ...");
            deletedLdapUser.checkForRemoval(entry, Optional.ofNullable(activeDirectoryGroups.getAllUsersFromAllBranches()));

            log.info("End sychronize Zammad groups and users with ouBase : {}.", entry.getKey());
        }

        if (!ldapShadeTrees.isEmpty()) {
            log.debug("6/6 Sync assignment roles for all ouBases ...");
            groupAssignmentAuthorizations.assignRoleAuthorizations();
        }

        log.info("End sychronize Zammad groups, user and roles all ouBases.");

    }


}
