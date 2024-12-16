package de.muenchen.zammad.ldap.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.ldap.domain.Assets;
import de.muenchen.zammad.ldap.domain.Channel;
import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.EmailAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailAddressIdTest {

    @Test
    void nameIsNullTest() {

        var emailChannels = new ChannelsEmail();
        assertNull(emailChannels.findEmailsAddressId(null, null));
    }

    @Test
    void notCompleteZammadResponseTest() {

        var emailChannels = new ChannelsEmail();
        assertNull(emailChannels.findEmailsAddressId("FOO", "FOO"));

        emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        assertNull(emailChannels.findEmailsAddressId("FOO", null));

        emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        emailChannels.getAssets().setEmailAddress(Map.of());
        assertNull(emailChannels.findEmailsAddressId("FOO", "FOO"));
    }

    @Test
    void nameNotFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress = new EmailAddress();
        emailAddress.setId(5);
        emailAddress.setName("ITM");
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress));
        assertNull(emailChannels.findEmailsAddressId("FOO", null));
    }

    @Test
    void organizationalUnitIgnoreCaseFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress1 = new EmailAddress();
        emailAddress1.setId(5);
        emailAddress1.setName("ItM");
        emailAddress1.setChannelId(1);
        var emailAddress2 = new EmailAddress();
        emailAddress2.setId(6);
        emailAddress2.setName("FOO");
        emailAddress2.setChannelId(2);
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress1, "6", emailAddress2));

        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(true);
        organizationalUnitChannel.setId(1);

        var standardChannel = new Channel();
        standardChannel.setActive(true);
        standardChannel.setId(2);
        emailChannels.getAssets().setChannel(Map.of(1, organizationalUnitChannel, 2, standardChannel));

        assertEquals(Integer.valueOf(5), emailChannels.findEmailsAddressId("iTM", "lHM"));
    }


    @Test
    void standardIgnoreCaseFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress1 = new EmailAddress();
        emailAddress1.setId(5);
        emailAddress1.setName("FOO");
        emailAddress1.setChannelId(1);

        var emailAddress2 = new EmailAddress();
        emailAddress2.setId(6);
        emailAddress2.setName("LhM");
        emailAddress2.setChannelId(2);
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress1, "6", emailAddress2));

        var organizationalUnitChannel = new Channel();
        organizationalUnitChannel.setActive(true);
        organizationalUnitChannel.setId(1);

        var standardChannel = new Channel();
        standardChannel.setActive(true);
        standardChannel.setId(2);
        emailChannels.getAssets().setChannel(Map.of(1, organizationalUnitChannel, 2, standardChannel));

        assertEquals(Integer.valueOf(6), emailChannels.findEmailsAddressId("iTM", "lHM"));
    }
}
