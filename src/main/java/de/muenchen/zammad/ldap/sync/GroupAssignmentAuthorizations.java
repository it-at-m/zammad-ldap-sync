package de.muenchen.zammad.ldap.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.ldap.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GroupAssignmentAuthorizations {

    private ZammadProperties zammadProperties;
    private ZammadService zammadService;

    public GroupAssignmentAuthorizations(ZammadProperties zammadProperties, ZammadService zammadService) {
        super();
        this.zammadProperties = zammadProperties;
        this.zammadService = zammadService;
    }

    List<Group> zammadGroups;

    public void assignRoleAuthorizations() {

     // Fetch all zammad groups
        log.debug("Getting all zammad groups");
        zammadGroups = zammadService.getZammadGroups();

        assignRolesTicketGroupAssignment();
        assignRolesTechnicalUser();
    }

    private void assignRolesTicketGroupAssignment() {

        // Fetch Assignmentrole Erstellen
        log.debug("Getting assignment role Erstellen");
        Role assignmentRole = zammadService
                .getZammadRole(zammadProperties.getAssignment().getRole().getIdErstellen());

        // Create group-map
        Map<String, List<String>> groupIdsAuthorization = new HashMap<>();
        for (Group zammadGroup : zammadGroups) {
            groupIdsAuthorization.put(zammadGroup.getId(), List.of("create"));
        }

        // Update AssignmentRole Erstellen
        log.debug("Updating assignment role Zweisung with \"create\" for all groups");
        assignmentRole.setGroupIds(groupIdsAuthorization);
        zammadService.updateZammadRole(assignmentRole);

    }

    private void assignRolesTechnicalUser() {

        // Fetch Assignmentrole Vollzugriff
        log.debug("Getting assignment role Vollzugriff");
        Role technicalUserRole = zammadService
                .getZammadRole(zammadProperties.getAssignment().getRole().getIdVollzugriff());

        // Create group-map
        Map<String, List<String>> groupIdsAuthorization = new HashMap<>();
        for (Group zammadGroup : zammadGroups) {
            groupIdsAuthorization.put(zammadGroup.getId(), List.of("full"));
        }

        // Update AssignmentRole
        log.debug("Updating assignment role Vollzugriff with \"full\" for all groups");
        technicalUserRole.setGroupIds(groupIdsAuthorization);
        zammadService.updateZammadRole(technicalUserRole);

    }

}
