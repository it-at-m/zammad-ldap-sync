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
class CreateGroupAndUserTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ZammadGroupDTO> createGroupCaptor;

    @Captor
    private ArgumentCaptor<ZammadUserDTO> userUserCaptor;

	@Test
	void createTest() {

		var zammadService = mock(ZammadService.class);
		when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadUsers()).thenReturn(List.of());

        userAndGroupMocks(zammadService);

		var zammadSyncService = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());

		zammadSyncService.updateZammadGroupsWithUsers(createLdapTree());

		assertEquals(0, zammadService.getZammadGroups().size());
		assertEquals(0, zammadService.getZammadUsers().size());

		verify(zammadService, times(7)).createZammadGroup(createGroupCaptor.capture());
		assertEquals("lhmobjectId_1_3", createGroupCaptor.getAllValues().get(6).getLhmobjectid());

		verify(zammadService, times(21)).createZammadUser(userUserCaptor.capture());
		assertEquals("lhmobjectId_2_2_1", userUserCaptor.getAllValues().get(12).getLhmobjectid());

	}

}
