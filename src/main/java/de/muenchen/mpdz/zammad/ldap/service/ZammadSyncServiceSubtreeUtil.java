package de.muenchen.mpdz.zammad.ldap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.muenchen.mpdz.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.mpdz.zammad.ldap.tree.LdapOuNode;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZammadSyncServiceSubtreeUtil {

    public ZammadSyncServiceSubtreeUtil(ZammadService zammadService, ZammadLdapService zammadLdapService,
			ZammadSyncContext context) {
		super();
		this.zammadService = zammadService;
		this.zammadLdapService = zammadLdapService;
		this.context = context;
	}

    @Value("${zammad.assignment.role.id-agent}")
	private String assignmentRoleIdAgent;

	@Value("${zammad.assignment.role.id-erstellen}")
	private String assignmentRoleIdErstellen;

    @Getter
    private long ouSize;

    @Getter
    private long userSize;

    @Getter
    private AtomicLong currentOuCount = new AtomicLong() ;

    @Getter
    private AtomicLong currentUserCount = new AtomicLong();

    public ZammadService zammadService;

    public ZammadLdapService zammadLdapService;

    public ZammadSyncContext context;

    @Getter
    @Setter
    Map<String, List<ZammadGroupDTO>> zammadGroupsByLhmObjectId;

    @Getter
    @Setter
    Map<String, List<ZammadUserDTO>> zammadUsersByLhmObjectId;

    public void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree) {

            setZammadGroupsByLhmObjectId(getCurrentZammadGroups());
            setZammadUsersByLhmObjectId(getCurrentZammadUsers());

            logStatistic(shadeLdapSubtree.entrySet().stream().findFirst().get().getValue());

            updateZammadGroupsWithUsers(shadeLdapSubtree, null, null);

    }

    private void logStatistic(LdapOuNode root) {

    	ouSize = root.flatListLdapOuDTO().size();
    	userSize = root.flatListLdapUserDTO().size();

    	log.info(String.format("Start processing '%o' ldap ou with '%o' user.", getOuSize(), getUserSize()));
    }

    private void updateZammadGroupsWithUsers(Map<String, LdapOuNode> shadeLdapSubtree, String zammadGroupName, final String parentGroupID) {


        shadeLdapSubtree.forEach((ou, node) -> {

            log.debug("------------------------");
            log.debug("Processing update Zammad Ou and User ou '{}' lhmObjectId: '{}'.", node.getNode().getOu(), node.getNode().getLhmObjectId());

            //Create new ZammadGroupDTO out of LDAP-OU
            var ldapOuDto = node.getNode();
            var zammadCurrentGroupName = zammadGroupName != null ? zammadGroupName + "::" + ldapOuDto.getLhmOUShortname() : ldapOuDto.getLhmOUShortname();
            var zammadGroupCompareDTO = mapToZammadGroup(node.getNode(), zammadCurrentGroupName, parentGroupID);
            log.debug(zammadGroupCompareDTO.toString());

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
                    if (zammadLdapSyncGroup.isLdapsyncupdate()) {
                        log.debug("Attribute doNotUpdate=false --> check for update.");
                        // To compare add Id and updated_at
                        zammadGroupCompareDTO.setId(zammadLdapSyncGroup.getId());
                        zammadGroupCompareDTO.setUpdatedAt(zammadLdapSyncGroup.getUpdatedAt());
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

            	getCurrentUserCount().addAndGet(node.getUsers().size());
            }

            if (!node.getChildNodes().isEmpty())
                updateZammadGroupsWithUsers(node.getChildNodes(), zammadCurrentGroupName, ongoingZammadGroupId);


            getCurrentOuCount().getAndIncrement();
         	log.info(String.format("Processed ou %o/%o. Processed user %o/%o", getCurrentOuCount().get(), getOuSize(), getCurrentUserCount().get(), getUserSize() ));

        });
    }

    private void updateZammadGroupUsers(List<EnhancedLdapUserDto> ldapBaseUserDTOs, String zammadUserGroupId) {

        ldapBaseUserDTOs.forEach(user -> {

            log.debug("------------------------");
            log.debug("Processing: lhmObjectId: '{}'" , user.getLhmObjectId());

            //Create new LdapBaseUserDTO out of LDAP-OU and create zammadGroupId
            var zammadUserCompareDTO = mapToZammadUser(user, zammadUserGroupId);
            log.trace(zammadUserCompareDTO.toString());

            //Find zammad-user with lhmObjectID
            String lhmObjectIdToFind = user.getLhmObjectId();
            var foundZammadUser = getZammadUsersByLhmObjectId().get(lhmObjectIdToFind);
            if (foundZammadUser != null && foundZammadUser.size() > 1)
                log.error("Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}'.", lhmObjectIdToFind);
            else if (foundZammadUser != null && foundZammadUser.size() == 1) {
                var zammadLdapSyncUser = foundZammadUser.get(0);
                if (zammadLdapSyncUser != null) {
                    log.debug("Zammad user found with ldap id : " + lhmObjectIdToFind);

                    if (zammadLdapSyncUser.isLdapsyncupdate()) {
                        log.debug("Attribute doNotUpdate=false --> check for update.");
                        //Update Id, updated_at und role_ids in case updateZammadUser
                        prepareUserForComparison(zammadUserCompareDTO, zammadLdapSyncUser);
                        if (zammadLdapSyncUser.compareTo(zammadUserCompareDTO) != 0) {
                            log.debug("Something has changed --> updating.");
                            log.debug("zammadLdapSyncUser   '{}'" , zammadLdapSyncUser);
                            log.debug("zammadUserCompareDTO '{}'" , zammadUserCompareDTO);
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


    public void assignDeletionFlagZammadUser(LdapOuNode rootNode) {

        log.debug("=============================");
        log.debug("Full-Sync requested - Checking for update and deletions of users ...");
        var ldapUserMap = rootNode.flatListLdapUserDTO().stream().collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity()));

        getCurrentZammadUsers().forEach((lhmObjectId, list) -> {

            if (list.size() > 1) {
                log.error("Inconsistent Zammad state. More than one zammad user found for lhmObjectId '{}'.", lhmObjectId);
                return;
            }

            var zammadUser = list.get(0);
            log.debug("---------------------------");
            log.debug("Checking ZammadUser " + zammadUser.getFirstname() + " " + zammadUser.getLastname());

            if (zammadUser.isLdapsyncupdate()) {

                if (lhmObjectId == null || lhmObjectId.isEmpty()) {
                    log.debug("No lhmObjectId - skipping.");
                } else {
                    var ldapBaseUserDTO = ldapUserMap.get(lhmObjectId);
                    if (ldapBaseUserDTO == null) {
                        log.debug("Did not find ZammadUser in LDAP-Users.");
                        if (zammadUser.isActive()) {
                            log.debug("User in Zammad is active - setting to inactive as a first step.");
                            zammadUser.setActive(false);
                            zammadUser.setLdapsyncstate("delete");
                            zammadService.updateZammadUser(zammadUser);
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

    private Map<String, List<ZammadGroupDTO>> getCurrentZammadGroups() {
        return generatelhmObjectIdZammadGroupMap(zammadService.getZammadGroups());
    }

    private Map<String, List<ZammadGroupDTO>> generatelhmObjectIdZammadGroupMap(List<ZammadGroupDTO> zammadGroupDTOs) {
        return zammadGroupDTOs.stream().filter(g -> g.getLhmobjectid() != null).collect(Collectors.groupingBy(ZammadGroupDTO::getLhmobjectid));
    }

    private ZammadGroupDTO mapToZammadGroup(LdapOuSearchResultDTO ldapOuSearchResultDTO, String groupName, String parentGroupId) {
        ZammadGroupDTO zammadGroupDTO = new ZammadGroupDTO();
        zammadGroupDTO.setName(groupName);
        zammadGroupDTO.setParentId(parentGroupId);
        zammadGroupDTO.setActive(true);
        zammadGroupDTO.setLdapsyncupdate(true);
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
        zammadUserDTO.setLogin(ldapBaseUserDTO.getLhmObjectId());
        zammadUserDTO.setEmail(ldapBaseUserDTO.getMail());
        zammadUserDTO.setFirstname(ldapBaseUserDTO.getVorname());
        zammadUserDTO.setLastname(ldapBaseUserDTO.getNachname());

        Map<String, List<String>> newGroupIds = new HashMap<>();
        newGroupIds.put(zammadGroupId, List.of("full"));
        zammadUserDTO.setGroupIds(newGroupIds);

        return zammadUserDTO;
    }

    private void prepareUserForComparison(ZammadUserDTO zammadUserCompareDTO, ZammadUserDTO foundZammadUser) {
        zammadUserCompareDTO.setId(foundZammadUser.getId());
        zammadUserCompareDTO.setUpdatedAt(foundZammadUser.getUpdatedAt());
        zammadUserCompareDTO.setRoleIds(foundZammadUser.getRoleIds());
        zammadUserCompareDTO.setActive(foundZammadUser.isActive());
        zammadUserCompareDTO.setLdapsyncupdate(true);
    }

    private void prepareUserForCreation(ZammadUserDTO zammadUserDTO) {
        zammadUserDTO.setActive(true);
        List<String> roleIds = new ArrayList<>();
        roleIds.add(assignmentRoleIdAgent);
        roleIds.add(assignmentRoleIdErstellen);
        zammadUserDTO.setRoleIds(roleIds);
//        Map<String, List<String>> groupIds = zammadUserDTO.getGroupIds() == null ? new HashMap<>() : zammadUserDTO.getGroupIds();
//        groupIds.put("1", List.of("full")); //Users
//        zammadUserDTO.setGroup_ids(groupIds);
        zammadUserDTO.setLdapsyncupdate(true);
    }


}
