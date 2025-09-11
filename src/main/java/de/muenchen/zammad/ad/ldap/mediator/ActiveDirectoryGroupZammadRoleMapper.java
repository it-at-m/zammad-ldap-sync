package de.muenchen.zammad.ad.ldap.mediator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.muenchen.userservice.ActiveDirectoryService;
import de.muenchen.zammad.ad.ActiveDirectoryGroupService;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.branch.OrgUnitBranchSynchronization;
import de.muenchen.zammad.ldap.branch.SimpleZammadUserFactory;
import de.muenchen.zammad.ldap.branch.ZammadService;
import de.muenchen.zammad.property.ActiveDirectoryProperty;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActiveDirectoryGroupZammadRoleMapper {

    private final ActiveDirectoryGroupService activeDirectoryGroupService;
    private final ZammadService zammadService;
    private final ZammadProperties zammadProperties;
    private final ActiveDirectoryProperty activeDirectoryProperties;
    private final ActiveDirectoryService adService;
    private Map<String, List<User>> zammadUsers;

    public ActiveDirectoryGroupZammadRoleMapper(ActiveDirectoryService adService, ZammadProperties zammadProperties,
            ActiveDirectoryProperty activeDirectoryProperties, ActiveDirectoryGroupService activeDirectoryGroupService,
            ZammadService zammadService) {

        super();
        this.adService = adService;
        this.activeDirectoryGroupService = activeDirectoryGroupService;
        this.zammadService = zammadService;
        this.zammadProperties = zammadProperties;
        this.activeDirectoryProperties = activeDirectoryProperties;
    }

    public void syncAdGroupsToLdapRoles() {

        zammadUsers = OrgUnitBranchSynchronization.generatelhmObjectIdZammadUserMap(zammadService.getZammadCache().flatMapUsersByLhmObjectId());

        log.info("Start active directory user lookup ...");
        Optional<List<EnhancedActiveDirectoryGroupDTO>> adGroups = activeDirectoryGroupService.crossOrganizationalGroups();
        log.info("End active directory user lookup.");

        Map<String, Role> zammadRoles = zammadService.getZammadRoles().stream().collect(
                Collectors.toMap(role -> role.getName().replace(groupNamePrefixNullCheck(), ""), role -> role));

        adGroups.ifPresent(activeDirectoryGroups -> {

            for (EnhancedActiveDirectoryGroupDTO adGroup : activeDirectoryGroups) {

                var zammadRole = Optional
                        .ofNullable(zammadRoles.get(adGroup.getName().replace(groupNamePrefixNullCheck(), "")));
                zammadRole.ifPresentOrElse(role -> {

                    var zammadRoleId = Optional.of(role.getId());

                    zammadRoleId.ifPresent(roleId -> {

                        log.info("Processing role name '{}' with zammad id '{}' ... ", role.getName(), roleId);

                        List<ActiveDirectoryUserDTO> activeDirectoryUsers = adGroup.getAdUserByLhmObjectId().values()
                                .stream().map(user -> adService.lookupUser(user.getDistinguishedName())).toList();

                        log.debug("'{}' Users found in role name '{}' with zammad id '{}'.",
                                activeDirectoryUsers.size(), role.getName(), roleId);

                        activeDirectoryUsers.forEach(activeDirectoryUser -> {

                            Optional<List<User>> zammadUser = Optional
                                    .ofNullable(zammadUsers.get(activeDirectoryUser.getLhmObjectId()));
                            zammadUser.ifPresentOrElse(users -> this.updateUser(users, roleId),
                                    () -> this.newUser(activeDirectoryUser, roleId));
                        });

                        zammadUsers.values().forEach(users -> removeUserRoleId(users, adGroup, roleId));

                    });
                }, () -> log.warn(
                        "Active directory group/role '{}' not found in current zammad instance. The search applies 'group-name-pefix' : '{}'.",
                        adGroup.getName().replace(activeDirectoryProperties.getGroupNamePrefix(), ""),
                        activeDirectoryProperties.getGroupNamePrefix()));
            }
        });
    }

    protected void removeUserRoleId(List<User> zammadUsers, EnhancedActiveDirectoryGroupDTO adGroup, Integer roleId) {

        var zammadUsersWithRoleId = zammadUsers.stream().filter(user -> user.getRoleIds().contains(roleId)).toList();
        var removeRoleIdFromUsers = zammadUsersWithRoleId.stream()
                .filter(user -> adGroup.getAdUserByLhmObjectId().get(user.getLhmobjectid()) == null).toList();
        removeRoleIdFromUsers.forEach(user -> {
            user.getRoleIds().remove(roleId);
            var updatedUser = zammadService.updateZammadUser(user);
            log.debug("RoleId '{}' removed from user '{}'.", roleId, updatedUser.getLhmobjectid());
        });
    }

    private void updateUser(List<User> users, Integer roleId) {
        users.forEach(user -> {
            if (user.isLdapsyncupdate()) {
                if (!user.getRoleIds().contains(roleId) || !user.isActive()) {
                    if (!user.getRoleIds().contains(roleId)) {
                        user.getRoleIds().add(roleId);
                        log.debug("User '{}' with zammad id '{}' roleIds '{}' updated.", user.getLhmobjectid(),
                                user.getId(), user.getRoleIds().toString());
                    }
                    if (!user.isActive()) {
                        user.setActive(true);
                        user.setLdapsyncstate(null);
                        log.debug("Reactivate User (active=true, ldapsyncstate='') '{}' with zammad id '{}'.",
                                user.getLhmobjectid(), user.getId());
                    }
                    zammadService.updateZammadUser(user);
                } else {
                    log.debug("User '{}' with zammad id '{}' already has roleIds '{}' ({}).", user.getLhmobjectid(),
                            user.getId(), roleId, user.getRoleIds().toString());
                }
            }
            else {
                log.warn("User with zammad id {} (lhmobjectid={}) and roleId {} : ldapsyncupdate = false, skip update. ", user.getId(), user.getLhmobjectid(), roleId);
            }
        });

    }

    private void newUser(ActiveDirectoryUserDTO activeDirectoryUser, Integer roleId) {

        SimpleZammadUserFactory simpleBuilder = new SimpleZammadUserFactory(this.zammadProperties);
        var newUser = simpleBuilder.mapToZammadUser(activeDirectoryUser, Optional.empty());
        newUser.getRoleIds().add(roleId);
        SimpleZammadUserFactory.activate(newUser);
        var addedUser = zammadService.createZammadUser(newUser);
        log.debug("New user '{}' with zammad id '{}' added.", addedUser.getLhmobjectid(), addedUser.getId());
        var shouldBeNull = Optional.ofNullable(zammadUsers.putIfAbsent(addedUser.getLhmobjectid(), List.of(addedUser)));
        shouldBeNull.ifPresent(
                users -> log.warn("User '{}' already exists.", users.stream().map(User::getLhmobjectid).toList()));

    }

    protected CharSequence groupNamePrefixNullCheck() {
        return activeDirectoryProperties.getGroupNamePrefix() != null ? activeDirectoryProperties.getGroupNamePrefix()
                : "";
    }

}
