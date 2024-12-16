package de.muenchen.zammad.ldap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ldap.domain.Signatures;
import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.zammad.ldap.service.config.OrganizationalUnitsCommonProperties;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class ZammadSyncServiceSubtree {

    public ZammadSyncServiceSubtree(ZammadService zammadService, ZammadProperties zammadProperties,
            OrganizationalUnitsCommonProperties standardProperties) {
        this.zammadService = zammadService;
        this.zammadProperties = zammadProperties;
        this.standardProperties = standardProperties;
    }

    private static final String LOG_ID = " - ID : {}";
    private static final String LOG_DIVIDER = "------------------------";

    @Getter
    private long ouSize;

    @Getter
    private long userSize;

    @Getter
    private AtomicLong currentOuCount = new AtomicLong();

    @Getter
    private AtomicLong currentUserCount = new AtomicLong();

    private final ZammadService zammadService;
    private final ZammadProperties zammadProperties;
    private final OrganizationalUnitsCommonProperties standardProperties;

    private final HashMap<String, Integer> emailAddressCache = new HashMap<>();
    private final HashMap<String, Integer> signatureCache = new HashMap<>();

    @Setter
    Map<String, List<ZammadGroupDTO>> zammadGroupsByLhmObjectId;

    @Setter
    Map<String, List<ZammadUserDTO>> zammadUsersByLhmObjectId;

    public void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree) {

        getCurrentOuCount().set(0);
        getCurrentUserCount().set(0);

        setZammadGroupsByLhmObjectId(getCurrentZammadGroups());
        setZammadUsersByLhmObjectId(getCurrentZammadUsers());

        shadeLdapSubtree.entrySet().stream().findFirst().ifPresent(finding -> logStatistic(finding.getValue()));

        updateZammadGroupsWithUsers(shadeLdapSubtree, null, null);

    }

    private void logStatistic(LdapOuNode root) {

        ouSize = root.flatListLdapOuDTO().size();
        userSize = root.flatListLdapUserDTO().size();

        log.info(String.format("Start processing '%o' ldap ou with '%o' user.", getOuSize(), getUserSize()));
    }

    private void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree, String zammadGroupName,
            final String parentGroupID) {

        try {

            shadeLdapSubtree.forEach((ou, node) -> {

                log.debug(LOG_DIVIDER);
                log.debug("Processing update Zammad Ou and User ou '{}' lhmObjectId: '{}'.", node.getNode().getOu(),
                        node.getNode().getLhmObjectId());

                // Create new ZammadGroupDTO out of LDAP-OU
                var ldapOuDto = node.getNode();
                var zammadCurrentGroupName = zammadGroupName != null
                        ? zammadGroupName + "::" + ldapOuDto.getLhmOUShortname()
                        : ldapOuDto.getLhmOUShortname();
                var zammadGroupCompareDTO = mapToZammadGroup(node.getNode(), zammadCurrentGroupName, parentGroupID);
                zammadGroupCompareDTO.setEmailAddressId(findEmailAdressId(node.getOrganizationalUnit()));
                zammadGroupCompareDTO.setSignatureId(findEmailSignatureId(node.getOrganizationalUnit()));
                log.debug(zammadGroupCompareDTO.toString());

                // Find zammad group with lhmObjectID
                var lhmObjectIdToFind = ldapOuDto.getLhmObjectId();
                var zammadGroupList = getZammadGroupsByLhmObjectId().get(lhmObjectIdToFind);

                String ongoingZammadGroupId = null;
                if (zammadGroupList != null && zammadGroupList.size() > 1) {
                    log.error(
                            "Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :",
                            lhmObjectIdToFind);
                    zammadGroupList.forEach(item -> log.error(LOG_ID, item.getId()));
                } else if (zammadGroupList != null && zammadGroupList.size() == 1) {
                    ZammadGroupDTO zammadLdapSyncGroup = zammadGroupList.get(0);
                    if (zammadLdapSyncGroup != null) {
                        log.debug("Zammad group '{}' found with lhmObjectId '{}'.", zammadLdapSyncGroup.getName(),
                                lhmObjectIdToFind);
                        ongoingZammadGroupId = zammadLdapSyncGroup.getId();
                        if (zammadLdapSyncGroup.isLdapsyncupdate()) {
                            log.debug("Zammad group isLdapsyncupdate={} - check for update.",
                                    zammadLdapSyncGroup.isLdapsyncupdate());
                            // To compare add Id and updated_at
                            zammadGroupCompareDTO.setId(zammadLdapSyncGroup.getId());
                            zammadGroupCompareDTO.setUpdatedAt(zammadLdapSyncGroup.getUpdatedAt());
                            log.trace("Zammad : {}.", zammadLdapSyncGroup);
                            log.trace("LDAP   : {}.", zammadGroupCompareDTO);
                            if (!zammadLdapSyncGroup.equals(zammadGroupCompareDTO)) {
                                log.debug("Something has changed - updating.");
                                getZammadService().updateZammadGroup(zammadGroupCompareDTO);
                            } else {
                                log.debug("No change - skipping.");
                            }
                        }
                    }
                } else {
                    log.debug("Group not found in Zammad with lhmObjectId '{}' - creating.", lhmObjectIdToFind);
                    // Not found: create new with isLdapsyncupdate=true
                    ZammadGroupDTO createdZammadGroupDTO = getZammadService().createZammadGroup(zammadGroupCompareDTO);
                    log.trace("Zammad group created : '{}'", createdZammadGroupDTO);
                    ongoingZammadGroupId = createdZammadGroupDTO.getId();
                    log.debug("Zammad group with ID '{}' created.", ongoingZammadGroupId);
                }

                if (node.getUsers() != null) {
                    if (ongoingZammadGroupId == null)
                        log.error("'{}' : GROUP_ID is NULL for user: '{}'.", ldapOuDto.getLhmOULongname(),
                                node.getUsers().stream().map(String::valueOf).collect(Collectors.joining("; ")));
                    updateZammadGroupUsers(node.getUsers(), ongoingZammadGroupId);
                    getCurrentUserCount().addAndGet(node.getUsers().size());
                }

                if (node.getChildNodes() != null && !node.getChildNodes().isEmpty())
                    updateZammadGroupsWithUsers(node.getChildNodes(), zammadCurrentGroupName, ongoingZammadGroupId);

                getCurrentOuCount().getAndIncrement();
                log.info(String.format("Processed ou %o/%o. Processed user %o/%o", getCurrentOuCount().get(),
                        getOuSize(), getCurrentUserCount().get(), getUserSize()));

            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    private void updateZammadGroupUsers(List<EnhancedLdapUserDto> ldapBaseUserDTOs, String zammadUserGroupId) {

        try {

            log.debug(LOG_DIVIDER);

            ldapBaseUserDTOs.forEach(user -> {

                log.debug("Processing: lhmObjectId: '{}'.", user.getLhmObjectId());

                // Create new LdapBaseUserDTO out of LDAP-OU and create zammadGroupId
                var zammadUserCompareDTO = mapToZammadUser(user);
                setDefaultRoleIdAndGroupId(zammadUserCompareDTO, zammadUserGroupId);
                log.trace(zammadUserCompareDTO.toString());
                // Find zammad-user with lhmObjectID
                String lhmObjectIdToFind = user.getLhmObjectId();
                var foundZammadUser = getZammadUsersByLhmObjectId().get(lhmObjectIdToFind);
                if (foundZammadUser != null && foundZammadUser.size() > 1) {
                    log.error(
                            "Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :",
                            lhmObjectIdToFind);
                    foundZammadUser.forEach(item -> log.error(LOG_ID, item.getId()));
                } else if (foundZammadUser != null && foundZammadUser.size() == 1) {
                    var zammadLdapSyncUser = foundZammadUser.get(0);
                    if (zammadLdapSyncUser != null) {
                        log.debug("Zammad user found with lhmObjectId '{}'.", lhmObjectIdToFind);
                        if (zammadLdapSyncUser.isLdapsyncupdate()) {
                            log.debug("User isLdapsyncupdate={} - check for update.",
                                    zammadLdapSyncUser.isLdapsyncupdate());
                            // Update Id, updated_at und role_ids in case updateZammadUser
                            prepareUserForComparison(zammadUserCompareDTO, zammadLdapSyncUser);
                            log.trace("LDAP   : {}.", zammadUserCompareDTO);
                            log.trace("Zammad : {}.", zammadLdapSyncUser);
                            if (!zammadUserCompareDTO.equals(zammadLdapSyncUser)) {
                                log.debug("Something has changed - updating.");
                                getZammadService().updateZammadUser(zammadUserCompareDTO);
                            } else {
                                log.debug("No change - skipping.");
                            }
                        } else {
                            log.debug("isLdapsyncupdate={} - skipping.", zammadLdapSyncUser.isLdapsyncupdate());
                        }
                    }
                } else {
                    log.debug("User not found in Zammad with lhmObjectid '{}' - creating.", lhmObjectIdToFind);
                    // Not found: create new with isLdapsyncupdate=true
                    prepareUserForCreation(zammadUserCompareDTO);
                    ZammadUserDTO zammadUserDTO = getZammadService().createZammadUser(zammadUserCompareDTO);
                    log.trace("Zammad user created : '{}'", zammadUserDTO);
                    log.debug("Zammad user with ID '{}' created.", zammadUserDTO.getId());
                }
                log.debug(LOG_DIVIDER);
            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void assignDeletionFlagZammadUser(LdapOuNode rootNode, Map<String, EnhancedLdapUserDto> allLdapUsers) {

        try {

            var zammadBranchGroupUsers = findAllZammadBranchGroupUsers(rootNode.getNode().getLhmObjectId());

            zammadBranchGroupUsers.forEach((lhmObjectId, list) -> {

                if (list.size() > 1) {
                    log.error("Inconsistent Zammad state. More than one zammad user found for lhmObjectId '{}' :",
                            lhmObjectId);
                    list.forEach(item -> log.error(LOG_ID, item.getId()));
                    return;
                }

                var zammadUser = list.get(0);
                log.debug("---------------------------");
                log.debug("Checking ZammadUser with lhmObjectId '{}'.", zammadUser.getLhmobjectid());

                if (zammadUser.isLdapsyncupdate()) {

                    if (lhmObjectId == null || lhmObjectId.isEmpty()) {
                        log.debug("No lhmObjectId - skipping.");
                    } else {
                        var ldapBaseUserDTO = allLdapUsers.get(lhmObjectId);
                        if (ldapBaseUserDTO == null) {
                            log.debug("Do not find ZammadUser in LDAP-Users.");
                            if (zammadUser.isActive()) {
                                log.debug("User in Zammad is active '{}' - setting to inactive as a first step.",
                                        zammadUser.isActive());
                                zammadUser.setActive(false);
                                zammadUser.setLdapsyncstate("delete");
                                getZammadService().updateZammadUser(zammadUser);
                            }
                        } else {
                            log.debug("User exists in LDAP - not deleting.");
                        }
                    }
                } else {
                    log.debug("isLdapsyncupdate is '{}' - skipping.", zammadUser.isLdapsyncupdate());
                }
            });

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private Map<String, List<ZammadGroupDTO>> getCurrentZammadGroups() {
        return generatelhmObjectIdZammadGroupMap(getZammadService().getZammadGroups());
    }

    private Map<String, List<ZammadGroupDTO>> generatelhmObjectIdZammadGroupMap(List<ZammadGroupDTO> zammadGroupDTOs) {
        return zammadGroupDTOs.stream().filter(g -> g.getLhmobjectid() != null && !g.getLhmobjectid().isBlank())
                .collect(Collectors.groupingBy(ZammadGroupDTO::getLhmobjectid));
    }

    private ZammadGroupDTO mapToZammadGroup(LdapOuSearchResultDTO ldapOuSearchResultDTO, String groupName,
            String parentGroupId) {
        ZammadGroupDTO zammadGroupDTO = new ZammadGroupDTO();
        zammadGroupDTO.setName(groupName);
        zammadGroupDTO.setParentId(parentGroupId);
        zammadGroupDTO.setActive(true);
        zammadGroupDTO.setLdapsyncupdate(true);
        zammadGroupDTO.setLhmobjectid(ldapOuSearchResultDTO.getLhmObjectId());
        return zammadGroupDTO;
    }

    private Map<String, List<ZammadUserDTO>> getCurrentZammadUsers() {
        return generatelhmObjectIdZammadUserMap(getZammadService().getZammadUsers());
    }

    private Map<String, List<ZammadUserDTO>> generatelhmObjectIdZammadUserMap(List<ZammadUserDTO> zammadUserDTOs) {
        var listLhmobjectid = zammadUserDTOs.stream()
                .filter(u -> u.getLhmobjectid() != null && !u.getLhmobjectid().isBlank())
                .collect(Collectors.groupingBy(ZammadUserDTO::getLhmobjectid));
        var listLogin = zammadUserDTOs.stream().filter(
                u -> (u.getLhmobjectid() == null || u.getLhmobjectid().isBlank())
                        && u.getLogin() != null && !u.getLogin().isBlank())
                .collect(Collectors.groupingBy(ZammadUserDTO::getLogin));
        listLhmobjectid.putAll(listLogin);
        return listLhmobjectid;
    }

    private ZammadUserDTO mapToZammadUser(LdapUserDTO ldapBaseUserDTO) {

        ZammadUserDTO zammadUserDTO = new ZammadUserDTO();
        zammadUserDTO.setDepartment(ldapBaseUserDTO.getOu());
        zammadUserDTO.setLhmobjectid(ldapBaseUserDTO.getLhmObjectId());
        zammadUserDTO.setLogin(ldapBaseUserDTO.getLhmObjectId());
        zammadUserDTO.setEmail(ldapBaseUserDTO.getMail());
        zammadUserDTO.setFirstname(ldapBaseUserDTO.getVorname());
        zammadUserDTO.setLastname(ldapBaseUserDTO.getNachname());
        return zammadUserDTO;
    }

    private void setDefaultRoleIdAndGroupId(ZammadUserDTO user, String zammadGroupId) {
        user.setRoleIds(defaultSynchronizationRoles());
        Map<String, List<String>> newGroupIds = new HashMap<>();
        newGroupIds.put(zammadGroupId, List.of("full"));
        user.setGroupIds(newGroupIds);
    }

    private void prepareUserForComparison(ZammadUserDTO zammadUserCompareDTO, ZammadUserDTO foundZammadUser) {
        zammadUserCompareDTO.setId(foundZammadUser.getId());
        zammadUserCompareDTO.setUpdatedAt(foundZammadUser.getUpdatedAt());
        zammadUserCompareDTO.setActive(foundZammadUser.isActive());
        zammadUserCompareDTO.setLdapsyncupdate(true);

    }

    private void prepareUserForCreation(ZammadUserDTO zammadUserDTO) {
        zammadUserDTO.setActive(true);
        zammadUserDTO.setLdapsyncupdate(true);
    }

    private List<Integer> defaultSynchronizationRoles() {
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(getZammadProperties().getAssignment().getRole().getIdAgent());
        roleIds.add(getZammadProperties().getAssignment().getRole().getIdErstellen());
        return roleIds;
    }

    private Map<String, List<ZammadUserDTO>> findAllZammadBranchGroupUsers(String ldapOuRootLhmObjectId) {

        var zammadGroups = new ArrayList<>(findRootZammadGroup(ldapOuRootLhmObjectId));

        if (zammadGroups.isEmpty())
            return new HashMap<>();
        else {

            findChildGroups(getZammadService().getZammadGroups(), zammadGroups.get(0).getId(), zammadGroups);

            var zammadBranchUsers = new ArrayList<ZammadUserDTO>();
            var zammadUsers = getZammadService().getZammadUsers();
            zammadGroups.forEach(g -> zammadBranchUsers.addAll(findUsers(zammadUsers, g.getId())));

            return zammadBranchUsers.stream().filter(u -> u.getLhmobjectid() != null && !u.getLhmobjectid().isBlank())
                    .collect(Collectors.groupingBy(ZammadUserDTO::getLhmobjectid));
        }
    }

    private List<ZammadGroupDTO> findRootZammadGroup(String ldapOuRootLhmObjectId) {

        var rootZammadGroups = getCurrentZammadGroups().get(ldapOuRootLhmObjectId);
        if (rootZammadGroups == null) {
            log.debug("No zammad root group found '{}'.", ldapOuRootLhmObjectId);
            return new ArrayList<>();
        } else if (rootZammadGroups.size() > 1) {
            log.error("Inconsistent Zammad state. More than one zammad group found for lhmObjectId '{}' :",
                    ldapOuRootLhmObjectId);
            rootZammadGroups.forEach(item -> log.error(LOG_ID, item.getId()));
            return new ArrayList<>();
        }
        return rootZammadGroups;

    }

    private void findChildGroups(List<ZammadGroupDTO> zammadServiceGroups, String zammadGroupId,
            List<ZammadGroupDTO> allZammadBranchGroups) {

        var childGroups = zammadServiceGroups.stream()
                .filter(g -> (g.getParentId() != null && g.getParentId().equals(zammadGroupId))).toList();
        if (!childGroups.isEmpty()) {
            allZammadBranchGroups.addAll(childGroups);
            childGroups.forEach(g -> findChildGroups(zammadServiceGroups, g.getId(), allZammadBranchGroups));
        }
    }

    private List<ZammadUserDTO> findUsers(List<ZammadUserDTO> zammadServiceUsers, String zammadGroupId) {
        return zammadServiceUsers.stream().filter(u -> u.getGroupIds().containsKey(zammadGroupId)).toList();
    }

    public Integer findEmailAdressId(String emailAddressName) {

        if (emailAddressName == null) {
            log.warn("Find emailAdressId started with invalid identifier 'null'.");
            return null;
        }

        if (getEmailAddressCache().containsKey(emailAddressName.toUpperCase())) {
            var emailAddressID = getEmailAddressCache().get(emailAddressName.toUpperCase());
            log.debug("Fetch emaildAddressId from cache : {}={}", emailAddressName, emailAddressID);
            return emailAddressID;
        } else {
            var zammadServiceResponse = getZammadService().getZammadChannelsEmail();
            if (zammadServiceResponse == null)
                getEmailAddressCache().put(emailAddressName.toUpperCase(), null);
            else
                getEmailAddressCache().put(emailAddressName.toUpperCase(), zammadServiceResponse
                        .findEmailsAddressId(emailAddressName, getStandardProperties().getMailStartsWith()));

            log.debug("EmaildAddressId account found in Zammad '{}={}' and added to cache.", emailAddressName,
                    getEmailAddressCache().get(emailAddressName));
            return getEmailAddressCache().get(emailAddressName);
        }
    }

    public Integer findEmailSignatureId(String signatureName) {

        if (signatureName == null) {
            log.warn("Find signatureName started with invalid identifier 'null'.");
            return null;
        }

        if (getSignatureCache().containsKey(signatureName.toUpperCase())) {
            var signature = getSignatureCache().get(signatureName.toUpperCase());
            log.debug("Fetch signature from cache : {}={}", signatureName, signature);
            return signature;
        } else {
            getSignatureCache().put(signatureName.toUpperCase(),
                    findSignatureId(getZammadService().getZammadEmailSignatures(), signatureName,
                            getStandardProperties().getSignatureStartsWith()));
            log.debug("EmailSignatureId account found in Zammad '{}={}' and added to cache.", signatureName,
                    getSignatureCache().get(signatureName));
            return getSignatureCache().get(signatureName);
        }
    }

    private Integer findSignatureId(List<Signatures> signatures, String name, String defaultName) {

        var signature = signatures.stream()
                .filter(signat -> signat.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst()
                .orElse(null);

        if (signature == null && defaultName != null)
            signature = signatures.stream()
                    .filter(signat -> signat.getName().toLowerCase().startsWith(defaultName.toLowerCase())).findFirst()
                    .orElse(null);

        return signature == null ? null : signature.getId();

    }

}
