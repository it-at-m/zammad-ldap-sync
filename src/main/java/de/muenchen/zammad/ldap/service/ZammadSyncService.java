package de.muenchen.zammad.ldap.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadRoleDTO;
import de.muenchen.zammad.ldap.service.config.SyncProperties;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class ZammadSyncService {

	private SyncProperties syncProperties;

	private ZammadProperties zammadProperties;

	public ZammadService zammadService;

	public ZammadLdapService zammadLdapService;

	private ZammadSyncServiceSubtreeUtil subtreeUtil;

	public ZammadSyncService(ZammadService zammadService, ZammadLdapService zammadLdapService,
			ZammadProperties zammadProperties, SyncProperties syncProperties,
			ZammadSyncServiceSubtreeUtil subtreeUtil) {

		this.zammadService = zammadService;
		this.zammadLdapService = zammadLdapService;
		this.subtreeUtil = subtreeUtil;
		this.zammadProperties = zammadProperties;
		this.syncProperties = syncProperties;

	}

	/**
	 * Calculate ldap subtree with users based on distinguished name. Add/update
	 * zammad groups. Update zammad assignment role for each role. Add/update zammad
	 * users.
	 */
	public void syncSubtreeByDn() {

		// String distinguishedName, String modifyTimeStamp
		var dateTime = calculateLdapUserSearchTimeStamp();

		log.info("Searching for user with ldap modifyTimeStamp > '{}'. 'null' means no restriction.", dateTime);

		var ldapSyncDistinguishedNames = syncProperties.getOuBases();
		log.info("OuBases :");
		ldapSyncDistinguishedNames.forEach(dn -> log.info("   {}", dn));

		log.debug("1/4 Start LDAP operations ...");
		var ldapShadetrees = buildLdapTreesWithDistinguishedNames(dateTime, ldapSyncDistinguishedNames);
		var allLdapUsers = allLdapUsersWithDistinguishedNames(ldapShadetrees);

		log.info("");
		for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {

			log.info("*****************************************");

			log.info("START synchronize Zammad groups and users with ouBase : {}. ", entry.getKey());

			var treeView = entry.getValue().toString();
			log.trace(treeView);

			log.debug("2/4 Update zammad groups and users ...");
			var map = new HashMap<String, LdapOuNode>();
			map.put(entry.getKey(), entry.getValue());
			getSubtreeUtil().updateZammadGroupsWithUsers(map);

			log.debug("3/4 Mark user for deletion ...");
			// var deleteEntry = shadeDnSubtree.get().entrySet().iterator().next();
			getSubtreeUtil().assignDeletionFlagZammadUser(entry.getValue(), allLdapUsers);

			log.info("END sychronize Zammad groups and users with ouBase : {}.", entry.getKey());
		}

		log.info("");
		log.debug("4/4 Sync assignment roles for all ouBases ...");
		syncAssignmentRoles();

		log.info("");
		log.info("END sychronize Zammad groups, user and roles all ouBases.");

	}

	private String calculateLdapUserSearchTimeStamp() {
		String calculatedTimeStamp = null;
		if (getSyncProperties().getDateTimeMinusDay() > 0) {
			var ldapUserSearchDate = LocalDateTime.now().minusDays(getSyncProperties().getDateTimeMinusDay());
			var ldapFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			calculatedTimeStamp = ldapFormatter.format(ldapUserSearchDate) + "Z";
		}
		return calculatedTimeStamp;
	}

	public void syncAssignmentRoles() {

		// Fetch all zammad groups
		log.debug("Getting all zammad groups");
		List<ZammadGroupDTO> zammadGroupDTOs = getZammadService().getZammadGroups();

		// Fetch asignmentrole Erstellen
		log.debug("Getting assignment role Erstellen");
		ZammadRoleDTO zammadRoleDTOErstellen = getZammadService()
				.getZammadRole(getZammadProperties().getAssignment().getRole().getIdErstellen());

		// Create group-map
		Map<String, List<String>> newGroupIdsErstellen = new HashMap<>();
		for (ZammadGroupDTO zammadGroupDTO : zammadGroupDTOs) {
			newGroupIdsErstellen.put(zammadGroupDTO.getId(), List.of("create"));
		}

		// Update AssignmentRole Erstellen
		log.debug("Updating assignment role Zweisung with \"create\" for all groups");
		zammadRoleDTOErstellen.setGroupIds(newGroupIdsErstellen);
		zammadService.updateZammadRole(zammadRoleDTOErstellen);

		// Fetch asignmentrole Vollzugriff
		log.debug("Getting assignment role Vollzugriff");
		ZammadRoleDTO zammadRoleDTOVollzugriff = getZammadService()
				.getZammadRole(getZammadProperties().getAssignment().getRole().getIdVollzugriff());

		// Create group-map
		Map<String, List<String>> newGroupIdsVollzugriff = new HashMap<>();
		for (ZammadGroupDTO zammadGroupDTO : zammadGroupDTOs) {
			newGroupIdsVollzugriff.put(zammadGroupDTO.getId(), List.of("full"));
		}

		// Update AssignmentRole
		log.debug("Updating assignment role Vollzugriff with \"create\" for all groups");
		zammadRoleDTOVollzugriff.setGroupIds(newGroupIdsVollzugriff);
		getZammadService().updateZammadRole(zammadRoleDTOVollzugriff);

	}

	/**
	 * Calculate ldap subtree with users based on distinguished name.
	 *
	 * @param distinguishedName
	 * @param modifyTimeStamp   Optional search attribute for ldap ou und user
	 * @return ldap tree as json
	 * @throws JsonProcessingException
	 */
	public String subtreeAsJson(String distinguishedName, String modifyTimeStamp) throws JsonProcessingException {

		var dn = distinguishedName;
		log.info("*****************************************");
		log.info("START sychronize Zammad groups and users with LDAP DN : " + dn);

		log.debug("Calculate LDAP Subtree with DN ... " + dn);
		var shadeDnSubtree = getZammadLdapService().calculateOuSubtreeWithUsersByDn(dn, modifyTimeStamp);

		var json = shadeDnSubtree.get().values().iterator().next().json();
		log.debug(json);

		return json;
	}

	public boolean checkRoleAssignments() {

		var roleProperty = getZammadProperties().getAssignment().getRole();
		var zammadRoles = getZammadService().getZammadRoles();

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

		log.info("Zammad role ids found : {} .", roleProperty.toString());

		return true;

	}

	private Map<String, LdapOuNode> buildLdapTreesWithDistinguishedNames(String dateTime,	List<String> ldapSyncDistinguishedNames) {

		Map<String, LdapOuNode> shadeTrees = new TreeMap<String, LdapOuNode>();
		for (String dn : ldapSyncDistinguishedNames) {
			var tree = getZammadLdapService().calculateOuSubtreeWithUsersByDn(dn, dateTime);
			if (tree.isPresent())
				shadeTrees.putAll(tree.get());
		}
		return shadeTrees;
	}


	private Map<String, EnhancedLdapUserDto> allLdapUsersWithDistinguishedNames(Map<String, LdapOuNode> ldapShadetrees ) {

		 Map<String, EnhancedLdapUserDto> list = new TreeMap<String, EnhancedLdapUserDto>();
		 for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {
			 list.putAll(entry.getValue().flatListLdapUserDTO().stream().collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity())));
		 }
		return list;
	}

}
