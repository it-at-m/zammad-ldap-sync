package de.muenchen.zammad.ad.ldap.mediator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.userservice.LdapService;
import de.muenchen.zammad.ad.ActiveDirectoryGroupService;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.branch.SimpleZammadUserFactory;
import de.muenchen.zammad.ldap.branch.ZammadService;
import de.muenchen.zammad.property.ActiveDirectoryProperty;
import de.muenchen.zammad.property.LdapProperty;
import de.muenchen.zammad.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActiveDirectoryGroupZammadRoleMapper {

    private ActiveDirectoryGroupService activeDirectoryGroupService;
    private ZammadService zammadService;
    private ZammadProperties zammadProperties;
    private ActiveDirectoryProperty activeDirectoryProperties;

    private RequestedOrganizationalUnits requestedOuUnits;
    private LdapService ldapService;

    @Autowired
    public ActiveDirectoryGroupZammadRoleMapper(LdapProperty ldapProperty, ZammadProperties zammadProperties, ActiveDirectoryProperty activeDirectoryProperties,
            ActiveDirectoryGroupService activeDirectoryGroupService, ZammadService zammadService,
            RequestedOrganizationalUnits requestedOuUnits) {
        super();
        this.ldapService = new LdapService(ldapProperty.getUrl(), null, null, null, null);
        this.activeDirectoryGroupService = activeDirectoryGroupService;
        this.zammadService = zammadService;
        this.requestedOuUnits = requestedOuUnits;
        this.zammadProperties = zammadProperties;
        this.activeDirectoryProperties = activeDirectoryProperties;

    }

    public void syncAdGroupsToLdapRoles() {

        Map<String, User> zammadUsers = zammadService.getZammadUsers().stream()
                .filter(user -> user.getLhmobjectid() != null && !user.getLhmobjectid().isEmpty())
                .collect(Collectors.toMap(User::getLhmobjectid, user -> user));

        Optional<List<EnhancedActiveDirectoryGroupDTO>> adGroups = activeDirectoryGroupService
                .crossOrganizationalGroups();

        Map<String, Role> zammadRoles = zammadService.getZammadRoles().stream()
                .collect(Collectors.toMap(role -> role.getName().replace(activeDirectoryProperties.getGroupNamePrefix(), ""), role -> role));

        adGroups.ifPresent(activeDirectoryGroups -> {

            for (EnhancedActiveDirectoryGroupDTO adGroup : activeDirectoryGroups) {

                var zammadRole = Optional.ofNullable(zammadRoles.get(adGroup.getName().replace(activeDirectoryProperties.getGroupNamePrefix(), "")));
                zammadRole.ifPresentOrElse(role -> {

                    var zammadRoleId = Optional.of(Integer.parseInt(role.getId()));

                    zammadRoleId.ifPresent(roleId -> {

                        log.info("Processing role name '{}' with zammad id '{}' ... ", role.getName(), roleId);

                        List<EnhancedLdapUserDTO> activeDirectoryLdapUsers = lookupLdapUsers(
                                adGroup.getAdUserByLhmObjectId());

                        log.debug("'{}' Users found in role name '{}' with zammad id '{}'.", activeDirectoryLdapUsers.size(), role.getName(), roleId);

                        activeDirectoryLdapUsers.forEach(activeDirectoryLdapUser -> {

                            Optional<User> currentUser = Optional
                                    .ofNullable(zammadUsers.get(activeDirectoryLdapUser.getLhmObjectId()));
                            currentUser.ifPresentOrElse(user -> {
                                user.getRoleIds().add(roleId);
                                zammadService.updateZammadUser(user);
                                log.debug("User '{}' with zammad id '{}' roleIds '{}' updated.", user.getLhmobjectid(),
                                        user.getId(), user.getRoleIds().toString());
                            }, () -> {
                                SimpleZammadUserFactory simpleBuilder = new SimpleZammadUserFactory(
                                        this.zammadProperties);
                                var newUser = simpleBuilder.mapToZammadUser(activeDirectoryLdapUser, Optional.empty());
                                newUser.getRoleIds().add(roleId);
                                SimpleZammadUserFactory.activate(newUser);
                                var addedUser = zammadService.createZammadUser(newUser);
                                log.debug("New user '{}' with zammad id '{}' added.", addedUser.getLhmobjectid(),
                                        addedUser.getId());
                            });
                        });

                        var zammadUsersWithRoleId = zammadUsers.values().stream()
                                .filter(user -> user.getRoleIds().contains(roleId)).toList();
                        var removeRoleIdFromUsers = zammadUsersWithRoleId.stream()
                                .filter(user -> adGroup.getAdUserByLhmObjectId().get(user.getLhmobjectid()) == null)
                                .toList();
                        removeRoleIdFromUsers.forEach(user -> {
                            user.getRoleIds().remove(roleId);
                            var updatedUser = zammadService.updateZammadUser(user);
                            log.debug("RoleId '{}' removed from user '{}'.", roleId, updatedUser.getLhmobjectid());
                        });

                    });
                }, () -> log.warn("Active directory group/role '{}' not found in current zammad instance.",
                        adGroup.getName()));
            }
        });
    }

    private List<EnhancedLdapUserDTO> lookupLdapUsers(Map<String, ActiveDirectoryUserDTO> adUserByLhmObjectId) {
        return adUserByLhmObjectId.entrySet().stream()
                .map(entry -> ldapService.lookupUser(investigateLdapUserDn(entry.getValue())).get()).toList();
    }

    /*
     * Determine the ldap search base using the department abbreviation in the
     * configuration, add uid and return complete ldap user distinguished name. This
     * assumes that the OU identifiers in the configuration correspond to the
     * department AD identifiers.
     */
    private String investigateLdapUserDn(ActiveDirectoryUserDTO user) {
        return String.join(",", "uid=" + user.getUid(),
                requestedOuUnits.getOrganizationalUnits().get(user.getLhmReferatName()).getUserSearchBase());
    }

}
