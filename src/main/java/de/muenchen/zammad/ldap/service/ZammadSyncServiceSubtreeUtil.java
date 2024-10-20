package de.muenchen.zammad.ldap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class ZammadSyncServiceSubtreeUtil {

    public ZammadSyncServiceSubtreeUtil(ZammadService zammadService, ZammadProperties zammadProperties) {
		super();
		this.zammadService = zammadService;
		this.zammadProperties = zammadProperties;

	}

    @Getter
    private long ouSize;

    @Getter
    private long userSize;

    @Getter
    private AtomicLong currentOuCount = new AtomicLong() ;

    @Getter
    private AtomicLong currentUserCount = new AtomicLong();

    private final ZammadService zammadService;

    private final ZammadProperties zammadProperties;

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

            var zammadParentGroup = findZammadParentGroupIfExists(shadeLdapSubtree);
    		if (zammadParentGroup.isPresent()) {
    			updateZammadGroupsWithUsers(shadeLdapSubtree, zammadParentGroup.get().getName(), zammadParentGroup.get().getId());
    		}
    		else
    			updateZammadGroupsWithUsers(shadeLdapSubtree, null, null);

    }

    private void logStatistic(LdapOuNode root) {

    	ouSize = root.flatListLdapOuDTO().size();
    	userSize = root.flatListLdapUserDTO().size();

    	log.info(String.format("Start processing '%o' ldap ou with '%o' user.", getOuSize(), getUserSize()));
    }

    private Optional<ZammadGroupDTO> findZammadParentGroupIfExists(Map<String, LdapOuNode> shadeLdapSubtree) {
		var rootNode = shadeLdapSubtree.entrySet().iterator().next();
		var ldapOuRootLhmObjectId = rootNode.getValue().getNode().getLhmObjectId();
		var zammadGroup = findRootZammadGroup(ldapOuRootLhmObjectId);
		if (zammadGroup.isEmpty())
			return Optional.empty();
		else {
			return getZammadService().getZammadGroups().stream().filter(g -> g.getId().equals(zammadGroup.get(0).getParentId())).findFirst();
		}
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
            if (zammadGroupList != null && zammadGroupList.size() > 1) {
                log.error("Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :", lhmObjectIdToFind);
                zammadGroupList.forEach(item -> log.error(" - ID : {}", item.getId()));
            }
            else if (zammadGroupList != null && zammadGroupList.size() == 1) {
                ZammadGroupDTO zammadLdapSyncGroup = zammadGroupList.get(0);
                if (zammadLdapSyncGroup != null) {
                    log.debug("Zammad group '{}' found with lhmObjectId '{}'.", zammadLdapSyncGroup.getName(), lhmObjectIdToFind);
                    ongoingZammadGroupId = zammadLdapSyncGroup.getId();
                    if (zammadLdapSyncGroup.isLdapsyncupdate()) {
                        log.debug("Zammad group isLdapsyncupdate={} - check for update.", zammadLdapSyncGroup.isLdapsyncupdate());
                        // To compare add Id and updated_at
                        zammadGroupCompareDTO.setId(zammadLdapSyncGroup.getId());
                        zammadGroupCompareDTO.setUpdatedAt(zammadLdapSyncGroup.getUpdatedAt());
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
                //Not found: create new with isLdapsyncupdate=true
                ZammadGroupDTO createdZammadGroupDTO = getZammadService().createZammadGroup(zammadGroupCompareDTO);
                ongoingZammadGroupId = createdZammadGroupDTO.getId();
                log.debug("Zammad group with ID '{}' created.", ongoingZammadGroupId);
            }

            if (node.getUsers() != null) {
            	if (ongoingZammadGroupId == null)
            		log.error("'{}' : GROUP_ID is NULL for user: '{}'.", ldapOuDto.getLhmOULongname() ,node.getUsers().stream().map(String::valueOf).collect(Collectors.joining("; ")));
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

    	log.debug("------------------------");

        ldapBaseUserDTOs.forEach(user -> {

            log.debug("Processing: lhmObjectId: '{}'." , user.getLhmObjectId());

            //Create new LdapBaseUserDTO out of LDAP-OU and create zammadGroupId
            var zammadUserCompareDTO = mapToZammadUser(user, zammadUserGroupId);
            log.trace(zammadUserCompareDTO.toString());

            //Find zammad-user with lhmObjectID
            String lhmObjectIdToFind = user.getLhmObjectId();
            var foundZammadUser = getZammadUsersByLhmObjectId().get(lhmObjectIdToFind);
            if (foundZammadUser != null && foundZammadUser.size() > 1) {
                log.error("Inconsistent Zammad state. More than one zammad group entry found for lhmObjectId '{}' :", lhmObjectIdToFind);
                foundZammadUser.forEach(item -> log.error(" - ID : {}", item.getId()));
            }
            else if (foundZammadUser != null && foundZammadUser.size() == 1) {
                var zammadLdapSyncUser = foundZammadUser.get(0);
                if (zammadLdapSyncUser != null) {
                    log.debug("Zammad user found with lhmObjectId '{}'.", lhmObjectIdToFind);

                    if (zammadLdapSyncUser.isLdapsyncupdate()) {
                        log.debug("User isLdapsyncupdate={} - check for update.", zammadLdapSyncUser.isLdapsyncupdate());
                        //Update Id, updated_at und role_ids in case updateZammadUser
                        prepareUserForComparison(zammadUserCompareDTO, zammadLdapSyncUser);
                        if (!zammadLdapSyncUser.equals(zammadUserCompareDTO)) {
                            log.debug("Something has changed - updating.");
                            log.debug("zammadLdapSyncUser   '{}'." , zammadLdapSyncUser);
                            log.debug("zammadUserCompareDTO '{}'." , zammadUserCompareDTO);
                            getZammadService().updateZammadUser(zammadUserCompareDTO);
                        } else {
                            log.debug("No change - skipping.");
                        }
                    } else {
                    	log.debug("isLdapsyncupdate={} - skipping.", zammadLdapSyncUser.isLdapsyncupdate());
                    }
                }
            } else {
                log.debug("User not found in Zammad with lhmObjectid '{}' - creating.", lhmObjectIdToFind );
                //Not found: create new with isLdapsyncupdate=true
                prepareUserForCreation(zammadUserCompareDTO);
                ZammadUserDTO zammadUserDTO = getZammadService().createZammadUser(zammadUserCompareDTO);
                log.debug("Zammad user with ID '{}' created.", zammadUserDTO.getId());
            }
            log.debug("------------------------");
        });
    }

    public void assignDeletionFlagZammadUser(LdapOuNode rootNode, Map<String, EnhancedLdapUserDto> allLdapUsers) {

        var zammadBranchGroupUsers = findAllZammadBranchGroupUsers(rootNode.getNode().getLhmObjectId());

        zammadBranchGroupUsers.forEach((lhmObjectId, list) -> {

            if (list.size() > 1) {
                log.error("Inconsistent Zammad state. More than one zammad user found for lhmObjectId '{}' :", lhmObjectId);
                list.forEach(item -> log.error(" - ID : {}", item.getId()));
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
                            log.debug("User in Zammad is active '{}' - setting to inactive as a first step.", zammadUser.isActive());
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
    }

    private Map<String, List<ZammadGroupDTO>> getCurrentZammadGroups() {
        return generatelhmObjectIdZammadGroupMap(getZammadService().getZammadGroups());
    }

    private Map<String, List<ZammadGroupDTO>> generatelhmObjectIdZammadGroupMap(List<ZammadGroupDTO> zammadGroupDTOs) {
        return zammadGroupDTOs.stream().filter(g -> g.getLhmobjectid() != null && ! g.getLhmobjectid().isBlank()).collect(Collectors.groupingBy(ZammadGroupDTO::getLhmobjectid));
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
        return generatelhmObjectIdZammadUserMap(getZammadService().getZammadUsers());
    }

    private Map<String, List<ZammadUserDTO>> generatelhmObjectIdZammadUserMap(List<ZammadUserDTO> zammadUserDTOs) {
        return zammadUserDTOs.stream().filter(u -> u.getLhmobjectid() != null && ! u.getLhmobjectid().isBlank()).collect(Collectors.groupingBy(ZammadUserDTO::getLhmobjectid));
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
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(getZammadProperties().getAssignment().getRole().getIdAgent());
        roleIds.add(getZammadProperties().getAssignment().getRole().getIdErstellen());
        zammadUserDTO.setRoleIds(roleIds);
//        Map<String, List<String>> groupIds = zammadUserDTO.getGroupIds() == null ? new HashMap<>() : zammadUserDTO.getGroupIds();
//        groupIds.put("1", List.of("full")); //Users
//        zammadUserDTO.setGroup_ids(groupIds);
        zammadUserDTO.setLdapsyncupdate(true);
    }

    private Map<String, List<ZammadUserDTO>> findAllZammadBranchGroupUsers(String ldapOuRootLhmObjectId) {

        var zammadGroups = new ArrayList<>(findRootZammadGroup(ldapOuRootLhmObjectId));

        if (zammadGroups.isEmpty())
        	return new HashMap<>();
        else {

        	findChildGroups(getZammadService().getZammadGroups(), zammadGroups.get(0).getId(), zammadGroups );

	        var zammadBranchUsers = new ArrayList<ZammadUserDTO>();
	        var zammadUsers = getZammadService().getZammadUsers();
	        zammadGroups.forEach(g -> zammadBranchUsers.addAll(findUsers(zammadUsers, g.getId())));

	       	return zammadBranchUsers.stream().filter(u -> u.getLhmobjectid() != null && ! u.getLhmobjectid().isBlank()).collect(Collectors.groupingBy(ZammadUserDTO::getLhmobjectid));
        }
    }

    private List<ZammadGroupDTO> findRootZammadGroup(String ldapOuRootLhmObjectId) {

       	var rootZammadGroups = getCurrentZammadGroups().get(ldapOuRootLhmObjectId);
       	if (rootZammadGroups == null) {
            log.debug("No zammad root group found '{}'.", ldapOuRootLhmObjectId);
            return new ArrayList<>();
        } else if (rootZammadGroups.size() > 1) {
            log.error("Inconsistent Zammad state. More than one zammad group found for lhmObjectId '{}' :", ldapOuRootLhmObjectId);
            rootZammadGroups.forEach(item -> log.error(" - ID : {}", item.getId()));
            return new ArrayList<>();
        }
    	return rootZammadGroups;

    }

    private void findChildGroups(List<ZammadGroupDTO> zammadServiceGroups, String zammadGroupId, List<ZammadGroupDTO>allZammadBranchGroups) {

    	var childGroups = zammadServiceGroups.stream().filter(g -> (g.getParentId() != null && g.getParentId().equals(zammadGroupId))).toList();
    	if ( ! childGroups.isEmpty()) {
	    	allZammadBranchGroups.addAll(childGroups);
	    	childGroups.forEach(g -> findChildGroups(zammadServiceGroups, g.getId(), allZammadBranchGroups));
    	}
    }

    private List<ZammadUserDTO> findUsers(List<ZammadUserDTO> zammadServiceUsers, String zammadGroupId) {
    	return zammadServiceUsers.stream().filter(u -> u.getGroupIds().containsKey(zammadGroupId)).toList();
    }

}
