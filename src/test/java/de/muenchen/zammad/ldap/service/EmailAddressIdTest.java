package de.muenchen.zammad.ldap.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.zammad.ldap.domain.Assets;
import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.EmailAddress;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailAddressIdTest {

    @Test
    void nameIsNullTest() {

        var emailChannels = new ChannelsEmail();
        assertNull(emailChannels.findEmailsAdressId(null, null));
    }

    @Test
    void notCompleteZammadResponseTest() {

        var emailChannels = new ChannelsEmail();
        assertNull(emailChannels.findEmailsAdressId("FOO", "FOO"));

        emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        assertNull(emailChannels.findEmailsAdressId("FOO", null));

        emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        emailChannels.getAssets().setEmailAddress(Map.of());
        assertNull(emailChannels.findEmailsAdressId("FOO", "FOO"));
    }

    @Test
    void nameNotFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress = new EmailAddress();
        emailAddress.setId(5);
        emailAddress.setName("ITM");
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress));
        assertNull(emailChannels.findEmailsAdressId("FOO", null));
    }

    @Test
    void emailIdIgnoreCaseFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress1 = new EmailAddress();
        emailAddress1.setId(5);
        emailAddress1.setName("ItM");
        var emailAddress2 = new EmailAddress();
        emailAddress2.setId(6);
        emailAddress2.setName("FOO");
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress1, "6", emailAddress2));
        assertEquals(Integer.valueOf(5), emailChannels.findEmailsAdressId("iTM", "lHM"));
    }

    @Test
    void defaultEmailIdIgnoreCaseFoundTest() {

        var emailChannels = new ChannelsEmail();
        emailChannels.setAssets(new Assets());
        var emailAddress1 = new EmailAddress();
        emailAddress1.setId(5);
        emailAddress1.setName("FOO");
        var emailAddress2 = new EmailAddress();
        emailAddress2.setId(6);
        emailAddress2.setName("LhM");
        emailChannels.getAssets().setEmailAddress(Map.of("5", emailAddress1, "6", emailAddress2));
        assertEquals(Integer.valueOf(6), emailChannels.findEmailsAdressId("iTM", "lHM"));
    }

}
