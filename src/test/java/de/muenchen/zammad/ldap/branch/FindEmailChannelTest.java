package de.muenchen.zammad.ldap.branch;

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

import de.muenchen.zammad.PrepareZammadTestEnvironment;
import de.muenchen.zammad.domain.Assets;
import de.muenchen.zammad.domain.Channel;
import de.muenchen.zammad.domain.ChannelsEmail;
import de.muenchen.zammad.domain.EmailAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindEmailChannelTest extends PrepareZammadTestEnvironment {

    final static Integer ORGANIZATIONAL_UNIT_CHANNEL_ID = 1;
    final static Integer STANDARD_UNIT_CHANNEL_ID = 2;
    final static Integer ORGANIZATIONAL_EMAIL_ID = 5;
    final static Integer STANDARD_EMAIL_ID = 6;

    @Test
    void channelsEmailEmptyTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(new ChannelsEmail(null));

        var cache = new EmailAddressCache(zammadService);

        assertNull(cache.findEmailAdressId("Value does not matter"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void channelsEmailZammadServiceResponseNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(null);

        var cache = new EmailAddressCache(zammadService);
        assertNull(cache.findEmailAdressId("Value does not matter"));

        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitAndStandardChannelInactiveTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var mockChannelIsActive = mockChannelsEmailResponse(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, new Channel(ORGANIZATIONAL_UNIT_CHANNEL_ID, false), STANDARD_UNIT_CHANNEL_ID, new Channel(STANDARD_UNIT_CHANNEL_ID, false)));
        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var cache = new EmailAddressCache(zammadService);

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

        var mockChannelIsActive = mockChannelsEmailResponse(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, new Channel(ORGANIZATIONAL_UNIT_CHANNEL_ID, true), STANDARD_UNIT_CHANNEL_ID, new Channel(STANDARD_UNIT_CHANNEL_ID, true)));
        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var cache = new EmailAddressCache(zammadService);

        // Only one call, first response is cached.
        assertEquals(ORGANIZATIONAL_EMAIL_ID, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(ORGANIZATIONAL_EMAIL_ID, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitChannelInactiveReturnsNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var mockChannelIsActive = mockChannelsEmailResponse(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, new Channel(ORGANIZATIONAL_UNIT_CHANNEL_ID, false), STANDARD_UNIT_CHANNEL_ID, new Channel(STANDARD_UNIT_CHANNEL_ID, true)));
        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelIsActive);

        var cache = new EmailAddressCache(zammadService);

        // Only one call, first response is cached.
        assertEquals(null, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        assertEquals(null, cache.findEmailAdressId(ORGANIZATIONAL_UNIT_CHANNEL));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void organizationalUnitNotFoundNullReturnTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        var onlyStandardEmailChannelExists = mockChannelsEmailResponse(Map.of(ORGANIZATIONAL_UNIT_CHANNEL_ID, new Channel(STANDARD_UNIT_CHANNEL_ID, true), STANDARD_UNIT_CHANNEL_ID, new Channel(STANDARD_UNIT_CHANNEL_ID, true)));
        when(zammadService.getZammadChannelsEmail()).thenReturn(onlyStandardEmailChannelExists);

        var cache = new EmailAddressCache(zammadService);
        assertEquals(null, cache.findEmailAdressId("FOO"));
        assertEquals(null, cache.findEmailAdressId("FOO"));
        verify(zammadService, times(1)).getZammadChannelsEmail();
    }

    @Test
    void invalidNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadChannelsEmail()).thenReturn(mockChannelsEmailResponse(null));

        var cache = new EmailAddressCache(zammadService);
        assertNull(cache.findEmailAdressId(null));

        verify(zammadService, times(0)).getZammadChannelsEmail();
    }

    private ChannelsEmail mockChannelsEmailResponse(Map<Integer, Channel> channels) {
        return new ChannelsEmail(new Assets(Map.of("5", new EmailAddress(ORGANIZATIONAL_EMAIL_ID,ORGANIZATIONAL_UNIT_CHANNEL_ID, ORGANIZATIONAL_UNIT_CHANNEL), "6", new EmailAddress(STANDARD_EMAIL_ID, STANDARD_UNIT_CHANNEL_ID, STANDARD_EMAIL_CHANNEL)), channels));
    }

}
