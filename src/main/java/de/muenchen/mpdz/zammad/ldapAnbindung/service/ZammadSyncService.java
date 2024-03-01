package de.muenchen.mpdz.zammad.ldapAnbindung.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadRoleDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZammadSyncService {

    @Value("${zammad.assignment.role.id}")
    String ASSIGNMENT_ROLE_ID;

    @Autowired
    public ZammadService zammadService;

    @Autowired
    public ZammadLdapService zammadLdapService;

    @Autowired
    public ZammadSyncContext context;

    @Autowired
    ZammadSyncServiceSubtreeUtil subtreeUtil;

    public void syncAssignmentRole() {
        log.debug("*****************************************");
        log.debug("Starting AssignmentRole Sync.");
        //Fetch all zammad groups
        log.debug("Getting all zammad groups");
        List<ZammadGroupDTO> zammadGroupDTOs = zammadService.getZammadGroups();

        //Fetch asignmentrole
        log.debug("Getting assignment role");
        ZammadRoleDTO zammadRoleDTO = zammadService.getZammadRole(ASSIGNMENT_ROLE_ID);

        //Create group-map
        Map<String, List<String>> new_group_ids = new HashMap<>();
        for (ZammadGroupDTO zammadGroupDTO : zammadGroupDTOs) {
            new_group_ids.put(zammadGroupDTO.getId(), List.of("create"));
        }

        //Update AssignmentRole
        log.debug("Updating assignment role with \"create\" for all groups");
        zammadRoleDTO.setGroup_ids(new_group_ids);
        zammadService.updateZammadRole(zammadRoleDTO);
    }

    /**
     * Calculate ldap subtree with users based on distinguished name.
     * Add/update zammad groups.
     * Update zammad assignment role for each role.
     * Add/update zammad users.
     *
     * @param distinguishedName
     * @param modifyTimeStamp   Optional search attribute for ldap ou und user
     * @return ldapTreeView
     */
    public String syncSubtreeByDn(String distinguishedName, String modifyTimeStamp, boolean fullsync) {

        var dn = distinguishedName;
        log.info("*****************************************");
        log.info("START sychronize Zammad groups and users with LDAP DN : " + dn);

        log.debug("Calculate LDAP Subtree with DN ... " + dn);
        var shadeDnSubtree = zammadLdapService.calculateOuSubtreeWithUsersByDn(dn, modifyTimeStamp);

        var treeView = shadeDnSubtree.get().values().iterator().next().logTree("");
        log.debug(treeView);

        log.debug("Update zammad groups and users ...");
        subtreeUtil.updateZammadGroupsWithUsers(shadeDnSubtree.get(), null, null);

        log.debug("Sync assignment roles ...");
        syncAssignmentRole();

        if (fullsync) {
            subtreeUtil.deleteUpdateZammadUser(shadeDnSubtree.get());
        }

        log.info("END sychronize Zammad groups and users with LDAP DN : " + dn);

        return treeView;
    }



}
