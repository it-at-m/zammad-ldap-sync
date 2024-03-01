package de.muenchen.mpdz.zammad.ldapAnbindung.service;

import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadUserDTO;
import de.muenchen.oss.ezldap.core.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ZammadSyncServiceSubtreeUtil {

    @Autowired
    public ZammadService zammadService;

    @Autowired
    public ZammadLdapService zammadLdapService;

    @Autowired
    public ZammadSyncContext context;

    @Getter
    @Setter
    Map<String, List<ZammadGroupDTO>> zammadGroupsByLhmObjectId;

    @Getter
    @Setter
    Map<String, List<ZammadUserDTO>> zammadUsersByLhmObjectId;

    public void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree, String zammadGroupName, final String parentGroupID) {

        if (zammadGroupName == null) { // Only once
            setZammadGroupsByLhmObjectId(getCurrentZammadGroups());
            setZammadUsersByLhmObjectId(getCurrentZammadUsers());
        }

        shadeLdapSubtree.forEach((ou, node) -> {

            log.debug("------------------------");
            log.debug("Processing update Zammad Ou and User ou '{}' lhmObjectId: '{}'.", node.getNode().getOu(), node.getNode().getLhmObjectId());

            //Create new ZammadGroupDTO out of LDAP-OU
            var ldapOuDto = node.getNode();
            var zammadCurrentGroupName = zammadGroupName != null ? zammadGroupName + "::" + ldapOuDto.getLhmOUShortname() : ldapOuDto.getLhmOUShortname();
            var zammadGroupCompareDTO = mapToZammadGroup(node.getNode(), zammadCurrentGroupName, parentGroupID);

            //Find zammad group with lhmObjectID
            var lhmObjectIdToFind = ldapOuDto.getLhmObjectId();
            var zammadGroupList = getZammadGroupsByLhmObjectId().get(lhmObjectIdToFind);

            String ongoingZammadGroupId = null;
            if (zammadGroupList != null && zammadGroupList.size() > 1)
                log.error("Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}'.", lhmObjectIdToFind);
            else if (zammadGroupList != null && zammadGroupList.size() == 1) {
                ZammadGroupDTO zammadLdapSyncGroup = zammadGroupList.get(0);
                if (zammadLdapSyncGroup != null) {
                    log.debug("Zammad group found with ldap id : " + lhmObjectIdToFind);
                    ongoingZammadGroupId = zammadLdapSyncGroup.getId();
                    if (!zammadLdapSyncGroup.isDonotupdate()) {
                        log.debug("Attribute doNotUpdate=false --> check for update.");
                        // To compare add Id and updated_at
                        zammadGroupCompareDTO.setId(zammadLdapSyncGroup.getId());
                        zammadGroupCompareDTO.setUpdated_at(zammadLdapSyncGroup.getUpdated_at());
                        if (!zammadLdapSyncGroup.equals(zammadGroupCompareDTO)) {
                            log.debug("Something has changed --> updating.");
                            zammadService.updateZammadGroup(zammadGroupCompareDTO);
                        } else {
                            log.debug("No change --> skipping.");
                        }
                    }
                }
            } else {
                log.debug("Group not found in Zammad --> creating.");
                //Not found: create new with doNotUpdate=false
                ZammadGroupDTO createdZammadGroupDTO = zammadService.createZammadGroup(zammadGroupCompareDTO);
                ongoingZammadGroupId = createdZammadGroupDTO.getId();
                log.debug("Creating group with ID " + createdZammadGroupDTO.getId());
            }

            if (node.getUsers() != null) {
            	if (ongoingZammadGroupId == null)
            		log.error("'{}' : GROUPID is NULL for user: '{}'", ldapOuDto.getLhmOULongname() ,node.getUsers().stream().map(n -> String.valueOf(n)).collect(Collectors.joining("; ")));

            	updateZammadGroupUsers(node.getUsers(), ongoingZammadGroupId);
            }

            if (!node.getChildNodes().isEmpty())
                updateZammadGroupsWithUsers(node.getChildNodes(), zammadCurrentGroupName, ongoingZammadGroupId);
        });
    }

    private void updateZammadGroupUsers(List<LdapUserDTO> ldapBaseUserDTOs, String zammadUserGroupId) {

        ldapBaseUserDTOs.forEach(user -> {

            log.debug("------------------------");
            log.debug("Processing: '{}' lhmObjectId: '{}'" , user.getUid(), user.getLhmObjectId());

            //Create new LdapBaseUserDTO out of LDAP-OU and create zammadGroupId
            var zammadUserCompareDTO = mapToZammadUser(user, zammadUserGroupId);

            //Find zammad-user with lhmObjectID
            String lhmObjectIdToFind = user.getLhmObjectId();
            var foundZammadUser = getZammadUsersByLhmObjectId().get(lhmObjectIdToFind);
            if (foundZammadUser != null && foundZammadUser.size() > 1)
                log.error("Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}'.", lhmObjectIdToFind);
            else if (foundZammadUser != null && foundZammadUser.size() == 1) {
                var zammadLdapSyncUser = foundZammadUser.get(0);
                if (zammadLdapSyncUser != null) {
                    log.debug("Zammad user found with ldap id : " + lhmObjectIdToFind);

                    if (!zammadLdapSyncUser.isDonotupdate()) {
                        log.debug("Attribute doNotUpdate=false --> check for update.");

                        //To compare add Id, updated_at und role_ids
                        prepareUserForComparison(zammadUserCompareDTO, zammadLdapSyncUser);
                        if (!foundZammadUser.equals(zammadUserCompareDTO)) {
                            log.debug("Something has changed --> updating.");
                            log.debug("foundZammadUser '{}' zammadUserCompareDTO '{}'" , foundZammadUser, zammadUserCompareDTO);
                            zammadService.updateZammadUser(zammadUserCompareDTO);
                        } else {
                            log.debug("No change --> skipping.");
                        }
                    }
                }
            } else {
                log.debug("User not found in Zammad --> creating.");
                //Not found: create new with doNotUpdate=false
                prepareUserForCreation(zammadUserCompareDTO);
                ZammadUserDTO zammadUserDTO = zammadService.createZammadUser(zammadUserCompareDTO);
                log.debug("Creating user with ID " + zammadUserDTO.getId());
            }
        });
    }

    public void deleteUpdateZammadUser(Map<String, LdapOuNode> subtree) {

        log.debug("=============================");
        log.debug("Full-Sync requested - Checking for update and deletions of users ...");
        var ldapUserMap = flatMapLdapUserDTO(subtree);

        getCurrentZammadUsers().forEach((lhmObjectId, list) -> {

            if (list.size() > 1) {
                log.error("Inconsistent Zammad state. More than one zammad user found for lhmObjectId '{}'.", lhmObjectId);
                return;
            }

            var zammadUser = list.get(0);
            log.debug("---------------------------");
            log.debug("Checking ZammadUser " + zammadUser.getFirstname() + " " + zammadUser.getLastname());

            if (!zammadUser.isDonotupdate()) {

                if (lhmObjectId == null || lhmObjectId.isEmpty()) {
                    log.debug("No lhmObjectId - skipping.");
                } else {
                    LdapBaseUserDTO ldapBaseUserDTO = ldapUserMap.get(lhmObjectId);
                    if (ldapBaseUserDTO == null) {
                        log.debug("Did not find ZammadUser in LDAP-Users.");
                        if (zammadUser.isActive()) {
                            log.debug("User in Zammad is active - setting to inactive as a first step.");
                            zammadUser.setActive(false);
                            zammadService.updateZammadUser(zammadUser);
                        } else {
                            log.debug("User in Zammad is inactive - deleting.");
                            /*
                             * TODO: Not practicable. Set inactiv instead.
                             * Before any change check if user has tickets ?
                             */
                            zammadService.deleteZammadUser(zammadUser.getId());
                        }
                    } else {
                        log.debug("Found user in LDAP - not deleting.");
                    }
                }
            } else {
                log.debug("Found doNotUpdate Flag - skipping.");
            }
        });
    }

    /*
     *
     */
    public List<String> deleteSubtree(Map<String, LdapOuNode> subtree) {

        var deleteLog = new ArrayList<String>();

        log.debug("------------------------");
        log.debug("Processing delete Zammad user ... ");

        setZammadUsersByLhmObjectId(getCurrentZammadUsers());
        var ldapUsers = flatMapLdapUserDTO(subtree);

        ldapUsers.forEach((lhmObjectId, user) -> {
            var zammadUsers = getCurrentZammadUsers().get(lhmObjectId);
            if (zammadUsers != null)
                zammadUsers.forEach(zammadUser -> deleteLog.add(zammadService.deleteZammadUser(zammadUser.getId())));
            else
                deleteLog.add("Zammd user with lhmObjectId not found: " + lhmObjectId);
        });

        log.debug("Processing delete Zammad groups ... ");
        setZammadGroupsByLhmObjectId(getCurrentZammadGroups());

        var ldapOus = flatMapLdapOuDTO(subtree);
        Collections.reverse(ldapOus);    // Remove tree leafs at first
        ldapOus.forEach(ou -> {
            var zammadGroups = getCurrentZammadGroups().get(ou.getLhmObjectId());
            if (zammadGroups != null)
            	/*
            	 * TODO: Not practicable. Set inactiv instead.
            	 */
                zammadGroups.forEach(zammadGroup -> deleteLog.add(zammadService.deleteZammadGroup(zammadGroup.getId())));
            else
                deleteLog.add("Zammd group/ou with lhmObjectId not found: " + ou.getLhmObjectId());
        });

        return deleteLog;
    }

    private Map<String, LdapUserDTO> flatMapLdapUserDTO(Map<String, LdapOuNode> subtree) {

        var users = new HashMap<String, LdapUserDTO>();
        subtree.forEach((key, node) -> {
            if (node.getUsers() != null)
                node.getUsers().forEach(u -> users.put(u.getLhmObjectId(), u));

            users.putAll(flatMapLdapUserDTO(node.getChildNodes()));
        });
        return users;
    }

    private List<LdapOuSearchResultDTO> flatMapLdapOuDTO(Map<String, LdapOuNode> subtree) {

        var ous = new ArrayList<LdapOuSearchResultDTO>();
        subtree.forEach((key, node) -> {
               ous.add(node.getNode());
               ous.addAll(flatMapLdapOuDTO(node.getChildNodes()));
        });
        return ous;
    }

    public Set<String> userLhmObjectIdsSet(Map<String, LdapOuNode> subtree) {
        return flatMapLdapUserDTO(subtree).keySet();
    }

    private Map<String, List<ZammadGroupDTO>> getCurrentZammadGroups() {
        return generatelhmObjectIdZammadGroupMap(zammadService.getZammadGroups());
    }

    private Map<String, List<ZammadGroupDTO>> generatelhmObjectIdZammadGroupMap(List<ZammadGroupDTO> zammadGroupDTOs) {
        return zammadGroupDTOs.stream().filter(g -> g.getLhmobjectid() != null).collect(Collectors.groupingBy(ZammadGroupDTO::getLhmobjectid));
    }

    private ZammadGroupDTO mapToZammadGroup(LdapOuSearchResultDTO ldapOuSearchResultDTO, String groupName, String parentGroupId) {
        ZammadGroupDTO zammadGroupDTO = new ZammadGroupDTO();
        zammadGroupDTO.setName(groupName);
        zammadGroupDTO.setParent_id(parentGroupId);
        zammadGroupDTO.setActive(true);
        zammadGroupDTO.setDonotupdate(false);
        zammadGroupDTO.setLhmobjectid(ldapOuSearchResultDTO.getLhmObjectId());
        return zammadGroupDTO;
    }

    private Map<String, List<ZammadUserDTO>> getCurrentZammadUsers() {
        return generatelhmObjectIdZammadUserMap(zammadService.getZammadUsers());
    }

    private Map<String, List<ZammadUserDTO>> generatelhmObjectIdZammadUserMap(List<ZammadUserDTO> zammadUserDTOs) {
        return zammadUserDTOs.stream().filter(u -> u.getLhmobjectid() != null).collect(Collectors.groupingBy(ZammadUserDTO::getLhmobjectid));
    }

    private ZammadUserDTO mapToZammadUser(LdapUserDTO ldapBaseUserDTO, String zammadGroupId) {

        ZammadUserDTO zammadUserDTO = new ZammadUserDTO();
        zammadUserDTO.setDepartment(ldapBaseUserDTO.getOu());
        zammadUserDTO.setLhmobjectid(ldapBaseUserDTO.getLhmObjectId());
        zammadUserDTO.setEmail(ldapBaseUserDTO.getMail());
        zammadUserDTO.setFirstname(ldapBaseUserDTO.getVorname());
        zammadUserDTO.setLastname(ldapBaseUserDTO.getNachname());

        Map<String, List<String>> new_group_ids = new HashMap<>();
        new_group_ids.put(zammadGroupId, List.of("full"));
        zammadUserDTO.setGroup_ids(new_group_ids);

        return zammadUserDTO;
    }

    private void prepareUserForComparison(ZammadUserDTO zammadUserCompareDTO, ZammadUserDTO foundZammadUser) {
        zammadUserCompareDTO.setId(foundZammadUser.getId());
        zammadUserCompareDTO.setUpdated_at(foundZammadUser.getUpdated_at());
        zammadUserCompareDTO.setRole_ids(foundZammadUser.getRole_ids());
 //       zammadUserCompareDTO.getGroup_ids().put("1", List.of("full")); // Users add group
        zammadUserCompareDTO.setActive(foundZammadUser.isActive());
        zammadUserCompareDTO.setDonotupdate(false); // Must be false
    }

    private void prepareUserForCreation(ZammadUserDTO zammadUserDTO) {
        zammadUserDTO.setActive(true);
        List<String> role_ids = new ArrayList<>();
        role_ids.add("2"); // Agent
        role_ids.add("4"); // Assignment role
        zammadUserDTO.setRole_ids(role_ids);
//        Map<String, List<String>> group_ids = zammadUserDTO.getGroup_ids() == null ? new HashMap<>() : zammadUserDTO.getGroup_ids();
//        group_ids.put("1", List.of("full")); //Users
//        zammadUserDTO.setGroup_ids(group_ids);
        zammadUserDTO.setDonotupdate(false);
    }


}
