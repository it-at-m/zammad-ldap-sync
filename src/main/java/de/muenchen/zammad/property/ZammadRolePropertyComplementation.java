package de.muenchen.zammad.property;

import java.util.Optional;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.ldap.branch.ZammadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class ZammadRolePropertyComplementation {

    private ZammadProperties zammadProperties;
    private ZammadService zammadService;

    public boolean completeRoleIdentifierWithRoleId() {

        var roleProperty = zammadProperties.getAssignment().getRole();
        var zammadRoles = zammadService.getZammadRoles();

        Optional<Role> agentRole = zammadRoles.stream()
                .filter(role -> roleProperty.getNameAgent().strip().compareToIgnoreCase(role.getName().strip()) == 0)
                .findAny();
        if (agentRole.isEmpty()) {
            log.error("Zammad role 'Agent' not found with property value '{}'.", roleProperty.getNameAgent());
            return false;
        }

        roleProperty.setIdAgent(Integer.valueOf(agentRole.get().getId()));

        Optional<Role> erstellenRole = zammadRoles.stream().filter(
                role -> roleProperty.getNameErstellen().strip().compareToIgnoreCase(role.getName().strip()) == 0)
                .findAny();
        if (erstellenRole.isEmpty()) {
            log.error("Zammad role 'Erstellen' not found with property value '{}'.", roleProperty.getNameErstellen());
            return false;
        }

        roleProperty.setIdErstellen(Integer.valueOf(erstellenRole.get().getId()));

        Optional<Role> vollzugriffRole = zammadRoles.stream().filter(
                role -> roleProperty.getNameVollzugriff().strip().compareToIgnoreCase(role.getName().strip()) == 0)
                .findAny();
        if (vollzugriffRole.isEmpty()) {
            log.error("Zammad role 'Vollzugriff' not found with property value '{}'.",
                    roleProperty.getNameVollzugriff());
            return false;
        }

        roleProperty.setIdVollzugriff(Integer.valueOf(vollzugriffRole.get().getId()));

        log.info("Zammad role ids found : {} .", roleProperty);

        return true;

    }

}
