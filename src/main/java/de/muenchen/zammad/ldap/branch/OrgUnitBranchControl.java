package de.muenchen.zammad.ldap.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.muenchen.userservice.LdapOuNode;
import de.muenchen.userservice.ShadeTree;
import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryGroupZammadRoleMapper;
import de.muenchen.zammad.property.RequestedOrganizationalUnits;
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

    private ActiveDirectoryGroupZammadRoleMapper activeDirectoryRoleMapper;

    /**
     * Use the requested ldap distinguished names to determine the organizational
     * unit ldap (shade) trees. Warn if not for all required distinguished names a
     * shade tree exists. Synchronize each shade tree branching with Zammad parent
     * group attribute. Synchronize Zammad domain group attributes and assigned
     * group users with organizational units in each shade tree. Add/update Zammad
     * domain user attributes. Update Zammad assignment role for each role.
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

        for (Map.Entry<String, LdapOuNode> entry : ldapShadeTrees.entrySet()) {

            log.info("Begin synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

            log.trace(entry.getValue().toString());

            log.debug("3/6 Update zammad groups and users ...");
            final var map = new HashMap<String, LdapOuNode>();
            map.put(entry.getKey(), entry.getValue());
            subtree.updateZammadGroupsWithUsers(map);

            log.debug("4/6 Mark user for deletion ...");
            deletedLdapUser.checkForRemoval(entry,
                    Optional.ofNullable(ShadeTree.collectUserFromAllBranches(ldapShadeTrees)));

            log.info("End sychronize Zammad groups and users with ouBase : {}.", entry.getKey());
        }

        log.info("5/6 Sync active directory groups and zammad roles ...");
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        if (!ldapShadeTrees.isEmpty()) {
            log.debug("6/6 Sync assignment roles for all ouBases ...");
            groupAssignmentAuthorizations.assignRoleAuthorizations();
        }

        log.info("End sychronize Zammad groups, user and roles all ouBases.");

    }

}
