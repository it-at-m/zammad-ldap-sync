package de.muenchen.zammad.ldap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserLoginNoLhmObjectIdAndRoleTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> createGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> updateGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadUserDTO> updateUserCaptor;

    /*
     * Test update ZammadUser with own login in Zammad before the first
     * synchronization. - In this case user has no valid lhmobjectid and is
     * identified by lhmobjectid in loginid. - Reset roleid to default
     * synchronisation roleid (Agent, Erstellen). - Reset groupid to ldap state.
     */
    @Test
    void resetRoleAndGroupTest() {

        var zammadService = mock(ZammadService.class);

        when(zammadService.getZammadGroups()).thenReturn(List.of(new ZammadGroupDTO("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null)));
        when(zammadService.getZammadUsers()).thenReturn(
                List.of(new ZammadUserDTO("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, null, List.of(8), Map.of("10", List.of("full")), null, true, null)));

        when(zammadService.getZammadChannelsEmail()).thenReturn(new ChannelsEmail());
        when(zammadService.getZammadSignatures()).thenReturn(List.of());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        zammadSyncServiceSubtree.updateZammadGroupsWithUsers(createResetLdapTree());

        assertEquals(1, zammadService.getZammadGroups().size());
        assertEquals(1, zammadService.getZammadUsers().size());

        verify(zammadService, times(0)).createZammadGroup(createGroupCaptor.capture());
        verify(zammadService, times(0)).updateZammadGroup(updateGroupCaptor.capture());

        verify(zammadService, times(1)).updateZammadUser(updateUserCaptor.capture());
        var capturedUser = updateUserCaptor.getAllValues().get(0);
        assertEquals("lhmobjectId_0_0_1", capturedUser.getLhmobjectid());
        assertEquals(List.of(0, 1), capturedUser.getRoleIds());
        assertEquals(Map.of("1", List.of("full")), capturedUser.getGroupIds());

    }

    @Test
    void resetRoleAndGroupLdapsyncUpdateFalseTest() {

        var zammadService = mock(ZammadService.class);

        when(zammadService.getZammadGroups()).thenReturn(List.of(new ZammadGroupDTO("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null)));
        when(zammadService.getZammadUsers()).thenReturn(
                List.of(new ZammadUserDTO("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", false, null, null, null, List.of(8), Map.of("10", List.of("full")), null, true, null)));

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        zammadSyncServiceSubtree.updateZammadGroupsWithUsers(createResetLdapTree());

        assertEquals(1, zammadService.getZammadGroups().size());
        assertEquals(1, zammadService.getZammadUsers().size());

        verify(zammadService, times(0)).createZammadGroup(createGroupCaptor.capture());
        verify(zammadService, times(0)).updateZammadGroup(updateGroupCaptor.capture());

        verify(zammadService, times(0)).updateZammadUser(updateUserCaptor.capture());

    }

    private Map<String, LdapOuNode> createResetLdapTree() {

        var dn = "dn_level_0_no_1";
        var rootNode = new LdapOuNode("orgUnit", dn, createEnhancedLdapOuSearchResultDTO(0, 1), null, createResetLdapOuUser(0, 0));

        var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

        return root;
    }

    protected List<EnhancedLdapUserDto> createResetLdapOuUser(Integer level, Integer no) {

        var userNo = 0;
        var user = new ArrayList<EnhancedLdapUserDto>();

        var user1 = new EnhancedLdapUserDto(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo);
        user1.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
        user1.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
        user1.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
        user.add(user1);

        return user;
    }

}
