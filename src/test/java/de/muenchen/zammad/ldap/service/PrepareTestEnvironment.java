package de.muenchen.zammad.ldap.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.zammad.ldap.service.config.Assignment;
import de.muenchen.zammad.ldap.service.config.ZammadProperties;
import de.muenchen.zammad.ldap.service.config.ZammadRoleProperties;
import de.muenchen.zammad.ldap.service.config.ZammadUrlProperties;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.extern.log4j.Log4j2;

@Log4j2
class PrepareTestEnvironment {

    protected void userAndGroupMocks(ZammadService zammadService) {
        // Groups
        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, null, "shortname_0_1", true, true, "lhmobjectId_0_1", null))).thenReturn(new ZammadGroupDTO("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null));

        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null))).thenReturn(new ZammadGroupDTO("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null));
        when(zammadService.updateZammadGroup(zammadGroup_lhmobjectId_1_1_reset())).thenReturn(new ZammadGroupDTO("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null));
        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null))).thenReturn(new ZammadGroupDTO("3", "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null));
        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null))).thenReturn(new ZammadGroupDTO("4", "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null));

        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "3", "shortname_0_1::shortname_1_2::shortname_2_1", true, true, "lhmobjectId_2_1", null))).thenReturn(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_1", true, true, "lhmobjectId_2_1", null));
        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "3", "shortname_0_1::shortname_1_2::shortname_2_2", true, true, "lhmobjectId_2_2", null))).thenReturn(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2", true, true, "lhmobjectId_2_2", null));
        when(zammadService.updateZammadGroup(zammadGroup_lhmobjectId_2_2_2_reset())).thenReturn(new ZammadGroupDTO("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null));
        when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "3", "shortname_0_1::shortname_1_2::shortname_2_3", true, true, "lhmobjectId_2_3", null))).thenReturn(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_3", true, true, "lhmobjectId_2_3", null));

        // User
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, "lhmobjectId_0_0_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, "lhmobjectId_0_0_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("2", "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("3", "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("4", "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, "lhmobjectId_1_1_2", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("5", "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, "lhmobjectId_1_1_2", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("4", "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("5", "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("4", "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("5", "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, "lhmobjectId_2_1_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, "lhmobjectId_2_1_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.updateZammadUser(zamadUser_lhmobjectId_2_2_3_reset())).thenReturn(new ZammadUserDTO("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("6", "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

    }

    protected void groupMocksCreateParentNodeTest(ZammadService zammadService) {

      when(zammadService.createZammadGroup(new ZammadGroupDTO(null, null, "shortname_2_1", true, true, "lhmobjectId_2_1", null))).thenReturn(new ZammadGroupDTO("1", null, "shortname_2_1", true, true, "lhmobjectId_2_1", null));
      when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("1", "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
      when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("2", "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
      when(zammadService.createZammadUser(new ZammadUserDTO(null, "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new ZammadUserDTO("2", "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));

      when(zammadService.createZammadGroup(new ZammadGroupDTO(null, null, "shortname_0_1", true, true, "lhmobjectId_0_1", null))).thenReturn(new ZammadGroupDTO("2", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null));
      when(zammadService.createZammadGroup(new ZammadGroupDTO(null, "2", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null))).thenReturn(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null));

    }


	protected  Map<String, LdapOuNode> createLdapTree() {

	    var dn = "dn_level_0_no_1";
	    var rootNode = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(0,1), createNextDnLevel(1), createLdapOuUser(0, 0) );

	    var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

	    return root;
	}

	protected ZammadGroupDTO zammadGroup_lhmobjectId_1_1_reset() {
        return new ZammadGroupDTO("5", "1", "shortname_0_1::shortname_1_1_reset", true, true, "lhmobjectId_1_1", "changed");
    }

	protected ZammadGroupDTO zammadGroup_lhmobjectId_2_2_2_reset() {
        return new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2_reset", true, true, "lhmobjectId_2_2", null);
    }

    protected ZammadUserDTO zamadUser_lhmobjectId_2_2_3_reset() {
        return new ZammadUserDTO("6", "vorname_2_2_3", "nachname_2_2_3_reset", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null);
    }


	protected Map<String, LdapOuNode> reducedLdapTree() {

	    var dn = "dn_level_0_no_1";
        var rootNode = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(0,1), createNextDnLevel(1), createLdapOuUser(0, 0) );

        rootNode.getUsers().remove(2);
        rootNode.getChildNodes().get("dn_level_1_no_2").getUsers().remove(2);
        rootNode.getChildNodes().get("dn_level_1_no_2").getChildNodes().get("dn_level_2_no_2").getUsers().remove(2);
        rootNode.getChildNodes().get("dn_level_1_no_3").getUsers().remove(2);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

        var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        return root;
	}

	protected List<ZammadGroupDTO> zammdGroups() {

        var zammadGroup = new ArrayList<ZammadGroupDTO>();
        zammadGroup.add(new ZammadGroupDTO("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null));
        zammadGroup.add(new ZammadGroupDTO("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null));
        zammadGroup.add(new ZammadGroupDTO("3", "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null));
        zammadGroup.add(new ZammadGroupDTO("4", "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null));

        zammadGroup.add(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_1", true, true, "lhmobjectId_2_1", null));
        zammadGroup.add(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2", true, true, "lhmobjectId_2_2", null));
        zammadGroup.add(new ZammadGroupDTO("3", "2", "shortname_0_1::shortname_1_1::shortname_2_3", true, true, "lhmobjectId_2_3", null));

        return zammadGroup;
    }


	protected List<ZammadUserDTO> zammdUsers() {

	    var zammadUser = new ArrayList<ZammadUserDTO>();
	    zammadUser.add(new ZammadUserDTO("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, null, List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("2", "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("3", "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("4", "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("5", "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, null, List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("4", "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("5", "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("4", "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("5", "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, null, List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new ZammadUserDTO("6", "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    return zammadUser;

	}

	private Map<String, LdapOuNode> createNextDnLevel(Integer level) {

	    int number = 0;
	    var nodes = new TreeMap<String, LdapOuNode>();

	    var dn = String.format("dn_level_%d_no_%d", level, ++number);
	    nodes.put(dn,  new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), createLdapOuUser(level, 1)));

	    dn = String.format("dn_level_%d_no_%d", level, ++number);
	    var node = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), createLdapOuUser(level, 2));
	    if (level == 1 && number == 2) {
    	    node.setChildNodes(createNextDnLevel(2));
        }
	    nodes.put(dn, node);

	    dn = String.format("dn_level_%d_no_%d", level, ++number);
        nodes.put(dn,  new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), createLdapOuUser(level, 3)));

        return nodes;
    }


	protected List<EnhancedLdapUserDto> createLdapOuUser(Integer level, Integer no) {

	    var userNo = 0;
	    var user = new ArrayList<EnhancedLdapUserDto>();

	    var user1 = new EnhancedLdapUserDto(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
	    user1.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
	    user1.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
	    user1.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
	    user.add(user1);

	    var user2 = new EnhancedLdapUserDto(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
        user2.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
        user2.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
        user2.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
        user.add(user2);

        var user3 = new EnhancedLdapUserDto(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
        user3.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
        user3.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
        user3.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
	    user.add(user3);

	    return user;
	}

	protected EnhancedLdapOuSearchResultDTO createEnhancedLdapOuSearchResultDTO(Integer level, Integer no) {

	    var ou = new EnhancedLdapOuSearchResultDTO();

	    ou.setLhmObjectId(String.format("lhmobjectId_%d_%d", level, no));
	    ou.setOu(String.format("ou_%d_%d", level, no));
	    ou.setLhmOUShortname(String.format("shortname_%d_%d", level, no));

	    return ou;
	}

	protected ZammadProperties createZammadProperties() {

	    var zammadProperties = new ZammadProperties();
        zammadProperties.setToken("123456");

        var zammadUrlProperties = new ZammadUrlProperties();
        zammadUrlProperties.setBase("http://<url>");
        zammadUrlProperties.setGroups("groups");
        zammadUrlProperties.setUsers("users");
        zammadUrlProperties.setRoles("roles");
        zammadProperties.setUrl(zammadUrlProperties);

        var zammadRoleProperties = new ZammadRoleProperties();
        zammadRoleProperties.setIdAgent(0);
        zammadRoleProperties.setNameAgent("Agent");
        zammadRoleProperties.setIdErstellen(1);
        zammadRoleProperties.setNameErstellen("Erstellen");
        zammadRoleProperties.setIdVollzugriff(2);
        zammadRoleProperties.setNameVollzugriff("Vollzugriff");

        var assignment = new Assignment();
        assignment.setRole(zammadRoleProperties);
        zammadProperties.setAssignment(assignment);

        return zammadProperties;
	}

	protected  Map<String, LdapOuNode> createChildLdapTree() {

        var dn = "dn_level_0_no_1";
        var rootNode = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(0,1), createNextDnLevel(1), createLdapOuUser(0, 0) );

        var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

        return root;
    }

}
