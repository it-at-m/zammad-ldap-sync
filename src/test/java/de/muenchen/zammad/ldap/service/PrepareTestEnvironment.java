package de.muenchen.zammad.ldap.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.muenchen.zammad.PrepareTestShadetree;
import de.muenchen.zammad.domain.ChannelsEmail;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.property.Assignment;
import de.muenchen.zammad.ldap.property.OrganizationalUnitsCommonProperties;
import de.muenchen.zammad.ldap.property.ZammadProperties;
import de.muenchen.zammad.ldap.property.ZammadRoleProperties;
import de.muenchen.zammad.ldap.property.ZammadUrlProperties;
import de.muenchen.zammad.ldap.sync.ZammadService;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.extern.log4j.Log4j2;

@Log4j2
class PrepareTestEnvironment extends PrepareTestShadetree {

    public static final String ORGANIZATIONAL_UNIT_CHANNEL = "ITM";
    public static final String STANDARD_EMAIL_CHANNEL = "LHM";

    protected void userAndGroupMocks(ZammadService zammadService) {
        // Groups
        when(zammadService.createZammadGroup(new Group(null, null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null))).thenReturn(new Group("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null));

        when(zammadService.createZammadGroup(new Group(null, "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null))).thenReturn(new Group("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null));
        when(zammadService.updateZammadGroup(zammadGroup_lhmobjectId_1_1_reset())).thenReturn(new Group("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null));
        when(zammadService.createZammadGroup(new Group(null, "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null, null, null))).thenReturn(new Group("3", "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null, null, null));
        when(zammadService.createZammadGroup(new Group(null, "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null, null, null))).thenReturn(new Group("4", "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null, null, null));

        when(zammadService.createZammadGroup(new Group(null, "3", "shortname_0_1::shortname_1_2::shortname_2_1", true, true, "lhmobjectId_2_1", null, null, null))).thenReturn(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_1", true, true, "lhmobjectId_2_1", null, null, null));
        when(zammadService.createZammadGroup(new Group(null, "3", "shortname_0_1::shortname_1_2::shortname_2_2", true, true, "lhmobjectId_2_2", null, null, null))).thenReturn(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2", true, true, "lhmobjectId_2_2", null, null, null));
        when(zammadService.updateZammadGroup(zammadGroup_lhmobjectId_2_2_2_reset())).thenReturn(new Group("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null));
        when(zammadService.createZammadGroup(new Group(null, "3", "shortname_0_1::shortname_1_2::shortname_2_3", true, true, "lhmobjectId_2_3", null, null, null))).thenReturn(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_3", true, true, "lhmobjectId_2_3", null, null, null));

        // User
        when(zammadService.createZammadUser(new User(null, "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, "lhmobjectId_0_0_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new User("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, "lhmobjectId_0_0_1", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new User("2", "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null))).thenReturn(new User("3", "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new User("4", "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, "lhmobjectId_1_1_2", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new User("5", "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, "lhmobjectId_1_1_2", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("4", "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("5", "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new User("4", "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new User("5", "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, "lhmobjectId_2_1_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, "lhmobjectId_2_1_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.updateZammadUser(zammadUser_lhmobjectId_2_2_3_reset())).thenReturn(new User("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

        when(zammadService.createZammadUser(new User(null, "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
        when(zammadService.createZammadUser(new User(null, "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null))).thenReturn(new User("6", "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
    }


    protected void groupMocksCreateParentNodeTest(ZammadService zammadService) {

      when(zammadService.createZammadGroup(new Group(null, null, "shortname_2_1", true, true, "lhmobjectId_2_1", null, null, null))).thenReturn(new Group("1", null, "shortname_2_1", true, true, "lhmobjectId_2_1", null, null, null));
      when(zammadService.createZammadGroup(new Group(null, null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null))).thenReturn(new Group("2", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null));
      when(zammadService.createZammadGroup(new Group(null, "2", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null))).thenReturn(new Group("3", "2", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null));
    }

    protected void channelsMock(ZammadService zammadService) {

        // Email Channel
        var channelsMock = mock(ChannelsEmail.class);
        when(zammadService.getZammadChannelsEmail()).thenReturn(channelsMock);
        when(channelsMock.findEmailsAddressId(anyString(), anyString())).thenReturn(null);
        when(channelsMock.getAssets()).thenReturn(null);
    }

    protected OrganizationalUnitsCommonProperties standardDefaultMock() {

        var standardMock = mock(OrganizationalUnitsCommonProperties.class);
        when(standardMock.getMailStartsWith()).thenReturn(STANDARD_EMAIL_CHANNEL);
        when(standardMock.getSignatureStartsWith()).thenReturn(STANDARD_EMAIL_CHANNEL);
        return standardMock;
    }

	protected Map<String, LdapOuNode> reducedLdapTree() {

	    var dn = "dn_level_0_no_1";
	    var rootNode = new LdapOuNode(ORGANIZATIONAL_UNIT_CHANNEL, dn, createEnhancedLdapOuSearchResultDTO(0,1), Optional.of(createNextDnLevel(1, dn)), Optional.of(createLdapOuUser(0, 0, dn) ));

        rootNode.getUsers().get().remove(2);
        rootNode.getChildNodes().get().get("dn_level_1_no_2,dn_level_0_no_1").getUsers().get().remove(2);
        rootNode.getChildNodes().get().get("dn_level_1_no_2,dn_level_0_no_1").getChildNodes().get().get("dn_level_2_no_2,dn_level_1_no_2,dn_level_0_no_1").getUsers().get().remove(2);
        rootNode.getChildNodes().get().get("dn_level_1_no_3,dn_level_0_no_1").getUsers().get().remove(2);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

        var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        return root;
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

}
