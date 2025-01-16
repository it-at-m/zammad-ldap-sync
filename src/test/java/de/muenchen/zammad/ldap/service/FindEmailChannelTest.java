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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.domain.Assets;
import de.muenchen.zammad.domain.Channel;
import de.muenchen.zammad.domain.ChannelsEmail;
import de.muenchen.zammad.domain.EmailAddress;
import de.muenchen.zammad.ldap.sync.EmailAddressCache;
import de.muenchen.zammad.ldap.sync.ZammadService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindEmailChannelTest extends PrepareTestEnvironment {

    final static Integer ORGANIZATIONAL_UNIT_CHANNEL_ID = 1;
    final static Integer STANDARD_UNIT_CHANNEL_ID = 2;
    final static Integer ORGANIZATIONAL_EMAIL_ID = 5;
    final static Integer STANDARD_EMAIL_ID = 6;

    @Test
    void channelsEmailEmptyTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(new ChannelsEmail());

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());

        assertNull(cache.findEmailAdressId("Value does not matter"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void channelsEmailZammadServiceResponseNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(null);

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());
        assertNull(cache.findEmailAdressId("Value does not matter"));

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

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());

        // Only one call, first response is cached.
        assertNull(cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertNull(cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
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

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());

        // Only one call, first response is cached.
        assertEquals(ORGANIZATIONAL_EMAIL_ID, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(ORGANIZATIONAL_EMAIL_ID, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
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

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());

        // Only one call, first response is cached.
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
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

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), cache.findEmailAdressId("FOO"));
        assertEquals(Integer.valueOf(STANDARD_EMAIL_ID), cache.findEmailAdressId("FOO"));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void invalidNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelsEmailResponse());

        var cache = new EmailAddressCache(zammadService, standardDefaultMock());
        assertNull(cache.findEmailAdressId(null));

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
