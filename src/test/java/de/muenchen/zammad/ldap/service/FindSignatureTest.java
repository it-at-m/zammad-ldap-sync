package de.muenchen.zammad.ldap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import de.muenchen.zammad.ldap.domain.ChannelsEmail;
import de.muenchen.zammad.ldap.domain.Signatures;
import de.muenchen.zammad.ldap.service.config.StandardProperties;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindSignatureTest extends PrepareTestEnvironment {

    private static final String DEFAULT_SIGNATURE_STARTS_WITH = "LHM";

    @Captor
    private ArgumentCaptor<ChannelsEmail> signaturesCaptor;

	@Test
	void findChannelEmailsTest() {

		var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

    	var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

    	  // Only one call, first response is cached.

    	assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findSignatureId(ORGANIZATIONAL_UNIT_CHANNEL));
		assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findSignatureId(ORGANIZATIONAL_UNIT_CHANNEL));

		verify(zammadService, times(1)).getZammadSignatures();
	}

	@Test
    void findDefaultChannelEmailsTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());

        // Only one call, first response is cached.
        assertEquals(Integer.valueOf(6), zammadSyncServiceSubtree.findSignatureId(DEFAULT_SIGNATURE_STARTS_WITH));
        assertEquals(Integer.valueOf(6), zammadSyncServiceSubtree.findSignatureId("lHm"));
        verify(zammadService, times(1)).getZammadSignatures();
    }


	@Test
    void noMatchTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

        var defaultSignatureNoMatchMock = mock(StandardProperties.class);
        when(defaultSignatureNoMatchMock.getSignatureStartsWith()).thenReturn("FOO"); // Not included in mockSignatureResponse()

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), defaultSignatureNoMatchMock);
        assertNull(zammadSyncServiceSubtree.findSignatureId("FOO"));

        verify(zammadService, times(1)).getZammadSignatures();
    }

	@Test
    void invalidNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties(), standardDefaultMock());
        assertNull(zammadSyncServiceSubtree.findSignatureId(null));

        verify(zammadService, times(0)).getZammadSignatures();
    }


	private List<Signatures> mockSignatureResponse() {

	    var signatureITM = new Signatures();
        signatureITM.setId(5);

        signatureITM.setName(ORGANIZATIONAL_UNIT_CHANNEL);

        var signatureDefault = new Signatures();
        signatureDefault.setId(6);
        signatureDefault.setName(DEFAULT_SIGNATURE_STARTS_WITH);

        return List.of(signatureITM, signatureDefault);

	}


}
