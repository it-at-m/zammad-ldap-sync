package de.muenchen.mpdz.zammad.ldap.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.muenchen.mpdz.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldap.domain.ZammadRoleDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZammadSyncService {

    public ZammadSyncService(ZammadService zammadService, ZammadLdapService zammadLdapService,
			ZammadSyncContext context, ZammadSyncServiceSubtreeUtil subtreeUtil) {
		this.zammadService = zammadService;
		this.zammadLdapService = zammadLdapService;
		this.context = context;
		this.subtreeUtil = subtreeUtil;
	}

	@Value("${zammad.assignment.role.id}")
    private String assignment_role_id;

    public ZammadService zammadService;

    public ZammadLdapService zammadLdapService;

    public ZammadSyncContext context;

    private ZammadSyncServiceSubtreeUtil subtreeUtil;

    public void syncAssignmentRole() {
        log.debug("*****************************************");
        log.debug("Starting AssignmentRole Sync.");
        //Fetch all zammad groups
        log.debug("Getting all zammad groups");
        List<ZammadGroupDTO> zammadGroupDTOs = zammadService.getZammadGroups();

        //Fetch asignmentrole
        log.debug("Getting assignment role");
        ZammadRoleDTO zammadRoleDTO = zammadService.getZammadRole(assignment_role_id);

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
     * @throws ZammadLdapException
     * @throws Exception
     */
    public String syncSubtreeByDn(String distinguishedName, String modifyTimeStamp) {

        var dn = distinguishedName;
        log.info("*****************************************");
        log.info("START synchronize Zammad groups and users with LDAP DN : " + dn);

        log.debug("Calculate LDAP Subtree with DN ... " + dn);
        var shadeDnSubtree = zammadLdapService.calculateOuSubtreeWithUsersByDn(dn, modifyTimeStamp);

        var treeView = shadeDnSubtree.get().values().iterator().next().toString();
        log.debug(treeView);

        log.debug("Update zammad groups and users ...");
        subtreeUtil.updateZammadGroupsWithUsers(shadeDnSubtree.get());

        log.debug("Sync assignment roles ...");
        syncAssignmentRole();

        log.info("END sychronize Zammad groups and users with LDAP DN : " + dn);

        return treeView;
    }

    /**
     *
     * In Zammad it is recommended to delete users for privacy issues only (https://admin-docs.zammad.org/en/latest/system/data-privacy.html).
     *
     * Be careful using this option. Every zammad user not found in DN subtree will be marked for deletion.
     * Use only with DN as short as possible (or as close at DN root as possible) to fetch all user you need in Zammad ! Do not use with DNs selecting limited subtrees only !
     *
     * To delete users finally use Zammad automation with condition : user.ldapsync = "delete" (https://admin-docs.zammad.org/en/latest/manage/scheduler.html).
     *
     * @param distinguishedName
     * @return ldapTreeView
     */
    public String markZammadUserToDelete(String distinguishedName) {

    	var dn = distinguishedName;
        log.info("*****************************************");
        log.info("START assign deletion flag Zammad to users with LDAP DN : " + dn);

        log.debug("Calculate LDAP Subtree with DN ... " + dn);
        var shadeDnSubtree = zammadLdapService.calculateOuSubtreeWithUsersByDn(dn, null);

        var rootEntry = shadeDnSubtree.get().entrySet().iterator().next();
        subtreeUtil.assignDeletionFlagZammadUser(rootEntry.getValue());

        var treeView = rootEntry.getValue().toString();
        log.debug(treeView);

        log.info("END assign deletion flag with LDAP DN : " + dn);

        return treeView;

    }

    /**
     * Calculate ldap subtree with users based on distinguished name.
     *
     * @param distinguishedName
     * @param modifyTimeStamp   Optional search attribute for ldap ou und user
     * @return ldap tree as json
     * @throws JsonProcessingException
     */
    public String subtreeAsJson(String distinguishedName, String modifyTimeStamp) throws JsonProcessingException  {

        var dn = distinguishedName;
        log.info("*****************************************");
        log.info("START sychronize Zammad groups and users with LDAP DN : " + dn);

        log.debug("Calculate LDAP Subtree with DN ... " + dn);
        var shadeDnSubtree = zammadLdapService.calculateOuSubtreeWithUsersByDn(dn, modifyTimeStamp);

        var json = shadeDnSubtree.get().values().iterator().next().json();
        log.debug(json);

        return json;
    }

}
