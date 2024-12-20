package de.muenchen.zammad.ldap.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.zammad.ldap.domain.ZammadUserDTO;


/*
 * For reasons of data consistency, it is difficult to delete groups or users in zammad.
 * Therefore, the zammad-ldap-sync only marks groups or users when they have disappeared from the ldap.
 * The actual deletion is carried out by zammad in a second step.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteGroupAndUserTest extends PrepareTestEnvironment {


    @Captor
    private ArgumentCaptor<ZammadUserDTO> updateUserCaptor;

    /*
     * In case you do not want to synchronize the entire ldap hierarchy, the zammad-ldap-sync allows you to synchronize only branches.
     * In this case each branch must be considered individually.
     * Test consider branch individually when mark for deletion.
     */
	@Test
	void deleteOneGroupTest() {

		var zammadService = mock(ZammadService.class);
		when(zammadService.getZammadGroups()).thenReturn(List.of(new ZammadGroupDTO("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null)));
        when(zammadService.getZammadUsers()).thenReturn(zammadUsers());

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        assertEquals(1, zammadService.getZammadGroups().size());
        assertEquals(21, zammadService.getZammadUsers().size());

		var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

		var reducedLdapTree = reducedLdapTree();
		var rootNode = reducedLdapTree.entrySet().iterator().next().getValue();
		assertEquals(17, rootNode.flatListLdapUserDTO().size());

		var reducedEnhancedLdapUserDTO = ZammadSyncService.allLdapUsersWithDistinguishedNames(reducedLdapTree);

		zammadSyncServiceSubtree.assignDeletionFlagZammadUser(rootNode, reducedEnhancedLdapUserDTO);

		verify(zammadService, times(1)).updateZammadUser(updateUserCaptor.capture());
		assertEquals("delete", updateUserCaptor.getAllValues().get(0).getLdapsyncstate());

	}

	@Test
    void deleteAllGroupsTest() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(zammadGroups());
        when(zammadService.getZammadUsers()).thenReturn(zammadUsers());

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        assertEquals(7, zammadService.getZammadGroups().size());
        assertEquals(21, zammadService.getZammadUsers().size());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        var reducedLdapTree = reducedLdapTree();
        var rootNode = reducedLdapTree.entrySet().iterator().next().getValue();
        assertEquals(17, rootNode.flatListLdapUserDTO().size());

        var reducedEnhancedLdapUserDTO = ZammadSyncService.allLdapUsersWithDistinguishedNames(reducedLdapTree);

        zammadSyncServiceSubtree.assignDeletionFlagZammadUser(rootNode, reducedEnhancedLdapUserDTO);

        verify(zammadService, times(4)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("delete", updateUserCaptor.getAllValues().get(0).getLdapsyncstate());
        assertEquals("vorname_2_2_3", updateUserCaptor.getAllValues().get(0).getFirstname());
        assertEquals("delete", updateUserCaptor.getAllValues().get(1).getLdapsyncstate());
        assertEquals("vorname_0_0_3", updateUserCaptor.getAllValues().get(1).getFirstname());
        assertEquals("delete", updateUserCaptor.getAllValues().get(2).getLdapsyncstate());
        assertEquals("vorname_1_2_3", updateUserCaptor.getAllValues().get(2).getFirstname());
        assertEquals("delete", updateUserCaptor.getAllValues().get(3).getLdapsyncstate());
        assertEquals("vorname_1_3_3", updateUserCaptor.getAllValues().get(3).getFirstname());

    }


}
