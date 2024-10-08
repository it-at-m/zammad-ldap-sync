package de.muenchen.zammad.ldap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateGroupAndUserTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> createGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> updateGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadUserDTO> userUserCaptor;

    /*
     * Test create zammad rest representations for a given ldap shadow tree (see tree dump) in an empty zammad group/user manager.
     */
	@Test
	void createTest() {

		var zammadService = mock(ZammadService.class);
		when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadUsers()).thenReturn(List.of());

        userAndGroupMocks(zammadService);

		var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());

		zammadSyncServiceSubtree.updateZammadGroupsWithUsers(createLdapTree());

		assertEquals(0, zammadService.getZammadGroups().size());
		assertEquals(0, zammadService.getZammadUsers().size());

		verify(zammadService, times(7)).createZammadGroup(createGroupCaptor.capture());
		assertEquals("lhmobjectId_1_3", createGroupCaptor.getAllValues().get(6).getLhmobjectid());

		verify(zammadService, times(21)).createZammadUser(userUserCaptor.capture());
		assertEquals("lhmobjectId_2_2_1", userUserCaptor.getAllValues().get(12).getLhmobjectid());

	}

	 /*
     * Test update zammad group.name hierarchy entries when adding new ldap parent groups for an already existing zammad group entry.
     */
	@Test
    void createParentNodeTest() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of( new ZammadGroupDTO("1", "1", "shortname_2_1", true, true, "lhmobjectId_2_1", null, null)));
        when(zammadService.getZammadUsers()).thenReturn(List.of());

        assertEquals(1, zammadService.getZammadGroups().size());
        assertEquals(0, zammadService.getZammadUsers().size());

        groupMocksCreateParentNodeTest(zammadService);

        var zammadSyncService = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());

        var childTree_level_2 = new TreeMap<String, LdapOuNode>();
        var number = 1;
        var level = 2;
        var dn = String.format("dn_level_%d_no_%d", level, number);
        var child_level_2 = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), null);
        childTree_level_2.put(dn,  child_level_2);

        var childTree_level_1 = new TreeMap<String, LdapOuNode>();
        level = 1;
        dn = String.format("dn_level_%d_no_%d", level, number);
        var child_level_1 = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), null);
        child_level_1.setChildNodes(childTree_level_2);
        childTree_level_1.put(dn,  child_level_1);

        var childTree_level_0 = new TreeMap<String, LdapOuNode>();
        level = 0;
        dn = String.format("dn_level_%d_no_%d", level, number);
        var child_level_0 = new LdapOuNode(dn, createEnhancedLdapOuSearchResultDTO(level,number), new TreeMap<String, LdapOuNode>(), null);
        child_level_0.setChildNodes(childTree_level_1);
        childTree_level_0.put(dn,  child_level_0);

        log.info(child_level_0.toString());
        zammadSyncService.updateZammadGroupsWithUsers(childTree_level_0);

        verify(zammadService, times(2)).createZammadGroup(createGroupCaptor.capture());
        assertNull(createGroupCaptor.getAllValues().get(0).getParentId());
        assertEquals("shortname_0_1", createGroupCaptor.getAllValues().get(0).getName());
        assertEquals("2", createGroupCaptor.getAllValues().get(1).getParentId());
        assertEquals("shortname_0_1::shortname_1_1", createGroupCaptor.getAllValues().get(1).getName());

        verify(zammadService, times(1)).updateZammadGroup(updateGroupCaptor.capture());
        assertEquals("shortname_0_1::shortname_1_1::shortname_2_1", updateGroupCaptor.getAllValues().get(0).getName());
        assertEquals("3", updateGroupCaptor.getAllValues().get(0).getParentId());

    }

}
