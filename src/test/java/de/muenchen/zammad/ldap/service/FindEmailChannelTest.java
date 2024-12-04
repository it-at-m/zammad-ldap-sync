package de.muenchen.zammad.ldap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.ldap.domain.Assets;
import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.EmailAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindEmailChannelTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ChannelsEmail> channelEmailCaptor;

	@Test
	void findChannelEmailsTest() {

		var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelsEmailResponse());

    	var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

    	// Only one call, first response is chached.
    	assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT));
		assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT));
		verify(zammadService, times(1)).getZammadChannelsEmail();
	}

	@Test
    void noMatchTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelsEmailResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertNull(zammadSyncServiceSubtree.findEmailAdressId("FOO"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

	@Test
    void invalidNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelsEmailResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertNull(zammadSyncServiceSubtree.findEmailAdressId(null));

        verify(zammadService, times(0)).getZammadChannelsEmail();
    }


	private ChannelsEmail mockChannelsEmailResponse() {

	    var emailChannels = new ChannelsEmail();
	    var emailAddress = new EmailAddress();
        emailAddress.setId(5);
        emailAddress.setName(ORGANIZATIONAL_UNIT);
        emailChannels.setAssets(new Assets());
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress));

        return emailChannels;

	}


}
