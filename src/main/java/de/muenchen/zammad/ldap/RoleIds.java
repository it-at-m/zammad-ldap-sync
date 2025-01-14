package de.muenchen.zammad.ldap;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.ldap.service.ZammadService;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class RoleIds {

    private ZammadProperties zammadProperties;
    private ZammadService zammadService;

    public boolean isAddAllRoleIdsSuccessful() {

        var roleProperty = zammadProperties.getAssignment().getRole();
        var zammadRoles = zammadService.getZammadRoles();

        var agentRole = zammadRoles.stream()
                .filter(role -> roleProperty.getNameAgent().strip().compareToIgnoreCase(role.getName().strip()) == 0)
                .findAny();
        if (agentRole.isEmpty()) {
            log.error("Zammad role 'Agent' not found with property value '{}'.", roleProperty.getNameAgent());
            return false;
        }

        roleProperty.setIdAgent(Integer.valueOf(agentRole.get().getId()));

        var erstellenRole = zammadRoles.stream().filter(
                role -> roleProperty.getNameErstellen().strip().compareToIgnoreCase(role.getName().strip()) == 0)
                .findAny();
        if (erstellenRole.isEmpty()) {
            log.error("Zammad role 'Erstellen' not found with property value '{}'.", roleProperty.getNameErstellen());
            return false;
        }

        roleProperty.setIdErstellen(Integer.valueOf(erstellenRole.get().getId()));

        var vollzugriffRole = zammadRoles.stream().filter(
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
