package de.muenchen.zammad.ldap.service;

import static org.junit.Assert.assertEquals;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateUpdateGroupAndUserTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> createGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> updateGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadUserDTO> createUserCaptor;

    @Captor
    private ArgumentCaptor<ZammadUserDTO> updateUserCaptor;

    /*
     * Test update manually changed zammad group entries. Reset manually changed zammad values to ldap values.
     */
	@Test
	void createUpdateTest() {

		var zammadService = mock(ZammadService.class);
		when(zammadService.getZammadGroups()).thenReturn(List.of(zammadGroup_lhmobjectId_1_1_reset(), zammadGroup_lhmobjectId_2_2_2_reset()));
        when(zammadService.getZammadUsers()).thenReturn(List.of(zamadUser_lhmobjectId_2_2_3_reset()));

        userAndGroupMocks(zammadService);

		var zammadSyncService = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());

		var ldapTree = createLdapTree();

		zammadSyncService.updateZammadGroupsWithUsers(ldapTree);

		assertEquals(2, zammadService.getZammadGroups().size());
		assertEquals(1, zammadService.getZammadUsers().size());

		verify(zammadService, times(5)).createZammadGroup(createGroupCaptor.capture());
		assertEquals("lhmobjectId_1_3", createGroupCaptor.getAllValues().get(4).getLhmobjectid());

		verify(zammadService, times(2)).updateZammadGroup(updateGroupCaptor.capture());
        assertEquals("lhmobjectId_1_1", updateGroupCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals("shortname_0_1::shortname_1_1", updateGroupCaptor.getAllValues().get(0).getName());
        assertEquals("shortname_0_1::shortname_1_2::shortname_2_2", updateGroupCaptor.getAllValues().get(1).getName());

		verify(zammadService, times(20)).createZammadUser(createUserCaptor.capture());
		assertEquals("lhmobjectId_2_2_1", createUserCaptor.getAllValues().get(12).getLhmobjectid());
		verify(zammadService, times(1)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmobjectId_2_2_3", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals("nachname_2_2_3", updateUserCaptor.getAllValues().get(0).getLastname());

	}

	/*
	 * The zammad-ldap-sync uses the data model attribute extension 'lhmobjectid' to merge zammad and ldap groups and users.
	 * When a user logs in to zammad without having been synchronized by the zammad-ldap-sync beforehand,
	 * he will be automatically recreated by zammad. In this case, zammad will store the lhmobjectid in the attribute 'login'.
	 * Test merge an already existing user and do not create a new duplicate.
	 */
	@Test
    void updateUserCreatedByZammadLoginTest() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());

        var modifiedUserList = zammdUsers();
        modifiedUserList.remove(17);
        modifiedUserList.add(zamadUser_lhmobjectId_2_2_3_reset());
        when(zammadService.getZammadUsers()).thenReturn(modifiedUserList);

        userAndGroupMocks(zammadService);

        var zammadSyncService = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());

        var ldapTree = createLdapTree();

        zammadSyncService.updateZammadGroupsWithUsers(ldapTree);

        assertEquals(0, zammadService.getZammadGroups().size());
        assertEquals(21, zammadService.getZammadUsers().size());

        verify(zammadService, times(4)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmobjectId_0_0_1", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals("nachname_0_0_1", updateUserCaptor.getAllValues().get(0).getLastname());
        assertEquals("lhmobjectId_1_1_2", updateUserCaptor.getAllValues().get(1).getLhmobjectid());
        assertEquals("nachname_1_1_2", updateUserCaptor.getAllValues().get(1).getLastname());
        assertEquals("lhmobjectId_2_1_1", updateUserCaptor.getAllValues().get(2).getLhmobjectid());
        assertEquals("nachname_2_1_1", updateUserCaptor.getAllValues().get(2).getLastname());
        assertEquals("lhmobjectId_2_2_3", updateUserCaptor.getAllValues().get(3).getLhmobjectid());
        assertEquals("nachname_2_2_3", updateUserCaptor.getAllValues().get(3).getLastname());

    }

}
