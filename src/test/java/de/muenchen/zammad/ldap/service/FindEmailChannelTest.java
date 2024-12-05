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
import de.muenchen.zammad.ldap.domain.Channel;
import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.EmailAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindEmailChannelTest extends PrepareTestEnvironment {

    final static Integer ORGANIZATIONAL_UNIT_CHANNEL_ID = 1;
    final static Integer STANDARD_UNIT_CHANNEL_ID = 2;
    final static Integer ORGANIZATIONAL_EMAIL_ID = 5;
    final static Integer STANDARD_EMAIL_ID = 6;

    @Captor
    private ArgumentCaptor<ChannelsEmail> channelEmailCaptor;

    @Test
    void channelsEmailEmptyTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(new ChannelsEmail());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertNull(zammadSyncServiceSubtree.findEmailAdressId("Value does not matter"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void channelsEmailZammadServiceResponseNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(null);

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertNull(zammadSyncServiceSubtree.findEmailAdressId("Value does not matter"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitAndStandardChannelInactiveTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var mockChannelIsActive = mockChannelsEmailResponse();
        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(false);
        organizationalUnitChannel.setId(ORGANIZATIONAL_UNIT_CHANNEL_ID);
        var standardChannel = new Channel();
        standardChannel.setActive(false);
        standardChannel.setId(STANDARD_UNIT_CHANNEL_ID);
        mockChannelIsActive.getAssets().setChannel(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, organizationalUnitChannel, STANDARD_UNIT_CHANNEL_ID, standardChannel));

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        // Only one call, first response is cached.
        assertNull(zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertNull(zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitAndStandardChannelActiveTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var mockChannelIsActive = mockChannelsEmailResponse();
        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(true);
        organizationalUnitChannel.setId(ORGANIZATIONAL_UNIT_CHANNEL_ID);
        var standardChannel = new Channel();
        standardChannel.setActive(true);
        standardChannel.setId(STANDARD_UNIT_CHANNEL_ID);
        mockChannelIsActive.getAssets().setChannel(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, organizationalUnitChannel, STANDARD_UNIT_CHANNEL_ID, standardChannel));

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        // Only one call, first response is cached.
        assertEquals(ORGANIZATIONAL_EMAIL_ID, zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(ORGANIZATIONAL_EMAIL_ID, zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitChannelInactiveStandardActiveTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var mockChannelIsActive = mockChannelsEmailResponse();
        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(false);
        organizationalUnitChannel.setId(ORGANIZATIONAL_UNIT_CHANNEL_ID);
        var standardChannel = new Channel();
        standardChannel.setActive(true);
        standardChannel.setId(STANDARD_UNIT_CHANNEL_ID);
        mockChannelIsActive.getAssets().setChannel(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, organizationalUnitChannel, STANDARD_UNIT_CHANNEL_ID, standardChannel));

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        // Only one call, first response is cached.
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), zammadSyncServiceSubtree.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitNotFoundStandardEmailTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var onlyStandardEmailChannelExists = mockChannelsEmailResponse();
        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(false);
        organizationalUnitChannel.setId(ORGANIZATIONAL_UNIT_CHANNEL_ID);
        var standardChannel = new Channel();
        standardChannel.setActive(true);
        standardChannel.setId(STANDARD_UNIT_CHANNEL_ID);
        onlyStandardEmailChannelExists.getAssets().setChannel(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, standardChannel, STANDARD_UNIT_CHANNEL_ID, standardChannel));

        when(zammadService.getZammadChannelsEmail()).thenReturn(onlyStandardEmailChannelExists);

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), zammadSyncServiceSubtree.findEmailAdressId("FOO"));
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), zammadSyncServiceSubtree.findEmailAdressId("FOO"));
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
        var organizationalUnitEmailAddress = new EmailAddress();
        organizationalUnitEmailAddress.setId(ORGANIZATIONAL_EMAIL_ID);
        organizationalUnitEmailAddress.setChannelId(ORGANIZATIONAL_UNIT_CHANNEL_ID);
        organizationalUnitEmailAddress.setName(ORGANIZATIONAL_UNIT_CHANNEL);

        var standardEmailAddress = new EmailAddress();
        standardEmailAddress.setId(STANDARD_EMAIL_ID);
        standardEmailAddress.setChannelId(STANDARD_UNIT_CHANNEL_ID);
        standardEmailAddress.setName(STANDARD_EMAIL_CHANNEL);

        emailChannels.setAssets(new Assets());
        emailChannels.getAssets().setEmailAddress(Map.of("5", organizationalUnitEmailAddress, "6", standardEmailAddress));

        return emailChannels;

    }

}
