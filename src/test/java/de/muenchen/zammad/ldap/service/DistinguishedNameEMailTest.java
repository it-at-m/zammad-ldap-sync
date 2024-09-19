package de.muenchen.zammad.ldap.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DistinguishedNameEMailTest {

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;


	@Test
	void notFoundTest() {

		var javaMailSender = mock(JavaMailSender.class);

		var validation = new Validation(javaMailSender);

		validation.checkOuBases(List.of("ou1dn1", "ou1dn2", "ou2dn1", "ou2dn2"), Map.of());

		verify(javaMailSender, times(1)).send(mailCaptor.capture());
		var text = mailCaptor.getAllValues().get(0).getText();
		assertTrue(text.contains("!!!  No ldap nodes for all ouBases found. Please check the ouBase(s) (ldap distinguished name) availability. Maybe part of a distinguished name was renamed in ldap :"));
		assertTrue(text.contains("ou2dn1"));
	}

}
