package de.muenchen.zammad.ldap.branch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import de.muenchen.userservice.LdapOuNode;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrgUnitBranchSynchronization extends AbstractTree {

    private final EmailAddressCache emailAddressCache;
    private final SignatureCache signatureCache;
    private Statistic statistic;

    public OrgUnitBranchSynchronization(ZammadService zammadService, ZammadProperties zammadProperties,
            EmailAddressCache emailAddress, SignatureCache signature) {
        this.zammadService = zammadService;
        this.zammadProperties = zammadProperties;
        this.emailAddressCache = emailAddress;
        this.signatureCache = signature;
    }

    public void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree) {

        this.statistic = new Statistic();
        zammadGroupsByLhmObjectId = getCurrentZammadGroups();
        zammadUsersByLhmObjectId = getCurrentZammadUsers();

        shadeLdapSubtree.entrySet().stream().findFirst()
                .ifPresent(finding -> statistic.logInfoStartProcessing(finding.getValue()));

        handleChildBranch(shadeLdapSubtree, null, null);
    }

    private void handleChildBranch(Map<String, LdapOuNode> shadeLdapSubtree, String zammadGroupName,
            final Integer parentGroupID) {

        try {
            shadeLdapSubtree.forEach((ou, node) -> {

                log.debug(LOG_DIVIDER);
                log.debug("Processing update Zammad Ou and User ou '{}' lhmObjectId: '{}'.", node.getNode().getOu(),
                        node.getNode().getLhmObjectId());

                // Create new ZammadGroup out of LDAP-OU
                final var ldapOuDto = node.getNode();
                final var zammadCurrentGroupName = createGroupName(zammadGroupName, ldapOuDto);
                final var zammadGroupCompare = mapToZammadGroup(node.getNode(), zammadCurrentGroupName, parentGroupID);
                zammadGroupCompare.setEmailAddressId(emailAddressCache.findEmailAdressId(node.getOrganizationalUnit()));
                zammadGroupCompare.setSignatureId(signatureCache.findEmailSignatureId(node.getOrganizationalUnit()));
                log.debug(zammadGroupCompare.toString());

                // Find zammad group with lhmObjectID
                final var lhmObjectIdToFind = ldapOuDto.getLhmObjectId();
                final var zammadGroupList = zammadGroupsByLhmObjectId.get(lhmObjectIdToFind);

                Integer currentZammadGroupId = null;
                if (zammadGroupList != null && zammadGroupList.size() > 1) {
                    log.error(
                            "Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :",
                            lhmObjectIdToFind);
                    zammadGroupList.forEach(item -> log.error(LOG_ID, item.getId()));
                } else if (zammadGroupList != null && zammadGroupList.size() == 1) {
                    currentZammadGroupId = updateZammadGroup(zammadGroupCompare, lhmObjectIdToFind, zammadGroupList,
                            currentZammadGroupId);
                } else {
                    final var zammadId = createNewZammadGroup(zammadGroupCompare, lhmObjectIdToFind);
                    if (zammadId.isEmpty())
                        return;

                    currentZammadGroupId = zammadId.orElseThrow();
                }

                handleGroupUser(node, ldapOuDto, currentZammadGroupId);

                Optional<Optional<Map<String, LdapOuNode>>> optionalValue = Optional.ofNullable(node.getChildNodes())
                        .filter(value -> !value.isEmpty());
                if (!optionalValue.isEmpty())
                    handleChildBranch(node.getChildNodes().get(), zammadCurrentGroupName, currentZammadGroupId);

                statistic.getCurrentOuCount().getAndIncrement();
                statistic.logInfoProcessStatusOuAndUser();

            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    private void handleGroupUser(LdapOuNode node, EnhancedLdapOuSearchResultDTO ldapOuDto,
            Integer currentZammadGroupId) {
        if (currentZammadGroupId == null)
            log.error("'{}' : GROUP_ID is NULL for user: '{}'.", ldapOuDto.getLhmOULongname(),
                    node.getUsers().stream().map(String::valueOf).collect(Collectors.joining("; ")));
        node.getUsers().ifPresent(nodeUsers -> {
            updateZammadGroupUsers(nodeUsers, currentZammadGroupId);
            statistic.getCurrentUserCount().addAndGet(nodeUsers.size());
        });

    }

    private String createGroupName(String zammadGroupName, EnhancedLdapOuSearchResultDTO ldapOuDto) {
        return zammadGroupName != null ? zammadGroupName + "::" + ldapOuDto.getLhmOUShortname()
                : ldapOuDto.getLhmOUShortname();
    }

    private Optional<Integer> createNewZammadGroup(Group zammadGroupCompare, String lhmObjectIdToFind) {

        StringBuilder zammadId = new StringBuilder();
        log.debug("Group not found in Zammad with lhmObjectId '{}' - creating.", lhmObjectIdToFind);
        // Not found: create new with isLdapsyncupdate=true
        Optional<Group> zammadGroup = zammadService.createZammadGroup(zammadGroupCompare);
        if (zammadGroup.isPresent()) {
           log.debug("Zammad group with ID '{}' created.", zammadGroup.get().getId());
           return Optional.of(zammadGroup.get().getId());
        } else
            return Optional.empty();
    }

    private Integer updateZammadGroup(Group zammadGroupCompare, String lhmObjectIdToFind, List<Group> zammadGroupList,
            Integer currentZammadGroupId) {

        Group zammadGroup = zammadGroupList.get(0);
        if (zammadGroup != null) {
            log.debug("Zammad group '{}' found with lhmObjectId '{}'.", zammadGroup.getName(), lhmObjectIdToFind);
            currentZammadGroupId = zammadGroup.getId();
            if (zammadGroup.isLdapsyncupdate()) {
                log.debug("Zammad group isLdapsyncupdate={} - check for update.", zammadGroup.isLdapsyncupdate());
                // To compare add Id and updated_at
                zammadGroupCompare.setId(zammadGroup.getId());
                zammadGroupCompare.setUpdatedAt(zammadGroup.getUpdatedAt());
                log.trace("Zammad : {}.", zammadGroup);
                log.trace("LDAP   : {}.", zammadGroupCompare);
                if (!zammadGroup.equals(zammadGroupCompare)) {
                    log.debug("Something has changed - updating.");
                    zammadService.updateZammadGroup(zammadGroupCompare);
                } else {
                    log.debug("No change - skipping.");
                }
            }
        }
        return currentZammadGroupId;
    }

    private void updateZammadGroupUsers(List<EnhancedLdapUserDTO> ldapBaseUserDTOs, Integer zammadUserGroupId) {

        try {

            log.debug(LOG_DIVIDER);

            ldapBaseUserDTOs.forEach(user -> {

                log.debug("Processing: lhmObjectId: '{}'.", user.getLhmObjectId());

                // Create new LdapBaseUserDTO out of LDAP-OU and create zammadGroupId
                SimpleZammadUserFactory userFactory = new SimpleZammadUserFactory(zammadProperties);
                final var zammadUserCompare = userFactory.mapToZammadUser(user, Optional.of(zammadUserGroupId));
                log.trace(zammadUserCompare.toString());
                // Find zammad-user with lhmObjectID
                String lhmObjectIdToFind = user.getLhmObjectId();
                final var foundZammadUser = zammadUsersByLhmObjectId.get(lhmObjectIdToFind);
                if (foundZammadUser != null && foundZammadUser.size() > 1) {
                    log.error(
                            "Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :",
                            lhmObjectIdToFind);
                    foundZammadUser.forEach(item -> log.error(LOG_ID, item.getId()));
                } else if (foundZammadUser != null && foundZammadUser.size() == 1) {
                    final var zammadLdapSyncUser = foundZammadUser.get(0);
                    if (zammadLdapSyncUser != null) {
                        updateZammadUser(zammadUserCompare, lhmObjectIdToFind, zammadLdapSyncUser);
                    }
                } else {
                    createZammadUser(zammadUserCompare, lhmObjectIdToFind);
                }
                log.debug(LOG_DIVIDER);
            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void createZammadUser(User zammadUserCompare, String lhmObjectIdToFind) {
        log.debug("User not found in Zammad with lhmObjectid '{}' - creating.", lhmObjectIdToFind);
        // Not found: create new with isLdapsyncupdate=true
        SimpleZammadUserFactory.activate(zammadUserCompare);
        User zammadUserDTO = zammadService.createZammadUser(zammadUserCompare);
        log.trace("Zammad user created : '{}'", zammadUserDTO);
        log.debug("Zammad user with ID '{}' created.", zammadUserDTO.getId());
    }

    private void updateZammadUser(User zammadUserCompare, String lhmObjectIdToFind, User zammadLdapSyncUser) {
        log.debug("Zammad user found with lhmObjectId '{}'.", lhmObjectIdToFind);
        if (zammadLdapSyncUser.isLdapsyncupdate()) {
            log.debug("User isLdapsyncupdate={} - check for update.", zammadLdapSyncUser.isLdapsyncupdate());
            // Update Id, updated_at und role_ids in case updateZammadUser
            prepareUserForComparison(zammadUserCompare, zammadLdapSyncUser);
            log.trace("LDAP   : {}.", zammadUserCompare);
            log.trace("Zammad : {}.", zammadLdapSyncUser);
            if (!zammadUserCompare.equals(zammadLdapSyncUser)) {
                log.debug("Something has changed - updating.");
                zammadService.updateZammadUser(zammadUserCompare);
            } else {
                log.debug("No change - skipping.");
            }
        } else {
            log.debug("isLdapsyncupdate={} - skipping.", zammadLdapSyncUser.isLdapsyncupdate());
        }
    }

    private Group mapToZammadGroup(LdapOuSearchResultDTO ldapOuSearchResultDTO, String groupName,
            Integer parentGroupId) {

        return new Group(null, parentGroupId, groupName, true, true, ldapOuSearchResultDTO.getLhmObjectId(), null, null,
                null);
    }

    private Map<String, List<User>> getCurrentZammadUsers() {
        return generatelhmObjectIdZammadUserMap(zammadService.getZammadUsers());
    }

    private Map<String, List<User>> generatelhmObjectIdZammadUserMap(List<User> zammadUsers) {
        var listLhmobjectid = zammadUsers.stream()
                .filter(u -> u.getLhmobjectid() != null && !u.getLhmobjectid().isBlank())
                .collect(Collectors.groupingBy(User::getLhmobjectid));
        var listLogin = zammadUsers
                .stream().filter(u -> (u.getLhmobjectid() == null || u.getLhmobjectid().isBlank())
                        && u.getLogin() != null && !u.getLogin().isBlank())
                .collect(Collectors.groupingBy(User::getLogin));
        listLhmobjectid.putAll(listLogin);
        return listLhmobjectid;
    }

    private void prepareUserForComparison(User zammadUserCompare, User foundZammadUser) {
        zammadUserCompare.setId(foundZammadUser.getId());
        zammadUserCompare.setUpdatedAt(foundZammadUser.getUpdatedAt());
        zammadUserCompare.setActive(foundZammadUser.isActive());
        zammadUserCompare.setLdapsyncupdate(true);
    }

}
