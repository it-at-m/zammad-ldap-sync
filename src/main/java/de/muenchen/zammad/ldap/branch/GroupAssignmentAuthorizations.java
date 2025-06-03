package de.muenchen.zammad.ldap.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GroupAssignmentAuthorizations {

    private final ZammadProperties zammadProperties;
    private final ZammadService zammadService;

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
        Map<Integer, List<String>> groupIdsAuthorization = new HashMap<>();
        for (Group zammadGroup : zammadGroups) {
            groupIdsAuthorization.put(zammadGroup.getId(), List.of("create"));
        }

        // Update AssignmentRole Erstellen
        log.debug("Updating assignment role Zweisung with \"create\" for all groups");
        assignmentRole.setGroupIds(groupIdsAuthorization);
        zammadService.updateZammadRole(assignmentRole);

    }

    private void assignRolesTechnicalUser() {

        // Fetch Assignmentrole Ticket-Zugriff EAI
        log.debug("Getting assignment role Vollzugriff");
        Role technicalUserRole = zammadService
                .getZammadRole(zammadProperties.getAssignment().getRole().getIdTicketAccess());

        // Create group-map
        Map<Integer, List<String>> groupIdsAuthorization = new HashMap<>();
        for (Group zammadGroup : zammadGroups) {
            groupIdsAuthorization.put(zammadGroup.getId(), List.of("read", "create", "change"));
        }

        // Update AssignmentRole
        log.debug("Updating assignment role 'Ticket-Zugriff EAI' with \"read\", \"create\", \"change\" for all groups");
        technicalUserRole.setGroupIds(groupIdsAuthorization);
        zammadService.updateZammadRole(technicalUserRole);

    }

}
