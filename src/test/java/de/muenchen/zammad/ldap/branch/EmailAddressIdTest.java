package de.muenchen.zammad.ldap.branch;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailAddressIdTest {

    @Test
    void nameIsNullTest() {
        var emailChannels = new ChannelsEmail(null);
        assertNull(emailChannels.findEmailsAddressId(null, null));
    }

    @Test
    void notCompleteZammadResponseTest() {
        var emailChannels = new ChannelsEmail(null);
        assertNull(emailChannels.findEmailsAddressId("FOO", "FOO"));

        emailChannels = new ChannelsEmail(new Assets(null, null));
        assertNull(emailChannels.findEmailsAddressId("FOO", null));

        emailChannels = new ChannelsEmail(new Assets(Map.of(), null));
        assertNull(emailChannels.findEmailsAddressId("FOO", "FOO"));
    }

    @Test
    void nameNotFoundTest() {
        var emailChannels = new ChannelsEmail(new Assets(Map.of("5", new EmailAddress(5, null, "ITM")), null));
        assertNull(emailChannels.findEmailsAddressId("FOO", null));
    }

    @Test
    void organizationalUnitIgnoreCaseFoundTest() {
        var emailChannels = new ChannelsEmail(new Assets(Map.of("5", new EmailAddress(5, 1, "ItM"), "6", new EmailAddress(6, 2, "FOO")), Map.of(1, new Channel(1, true), 2, new Channel(2, true))));
        assertEquals(Integer.valueOf(5), emailChannels.findEmailsAddressId("iTM", "lHM"));
    }


    @Test
    void standardIgnoreCaseFoundTest() {
        var emailChannels = new ChannelsEmail(new Assets(Map.of("5", new EmailAddress(5, 1, "FOO"), "6", new EmailAddress(6, 2, "LhM")), Map.of(1, new Channel(1, true), 2, new Channel(2, true))));
        assertEquals(Integer.valueOf(6), emailChannels.findEmailsAddressId("iTM", "lHM"));
    }
}
