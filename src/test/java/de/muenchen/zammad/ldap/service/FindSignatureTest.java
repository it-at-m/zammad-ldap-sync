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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FindSignatureTest extends PrepareTestEnvironment {

    @Captor
    private ArgumentCaptor<ChannelsEmail> signaturesCaptor;

	@Test
	void findChannelEmailsTest() {

		var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

    	var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());
		assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findSignatureId(ORGANIZATIONAL_UNIT));

		assertEquals(Integer.valueOf(5), zammadSyncServiceSubtree.findSignatureId(ORGANIZATIONAL_UNIT));

		// Only one call, first response is chached.
		verify(zammadService, times(1)).getZammadSignatures();
	}

	@Test
    void noMatchTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());
        assertNull(zammadSyncServiceSubtree.findSignatureId("FOO"));

        verify(zammadService, times(1)).getZammadSignatures();
    }

	@Test
    void invalidNullTest() {

        var zammadService = mock(ZammadService.class);

        userAndGroupMocks(zammadService);

        when(zammadService.getZammadSignatures()).thenReturn(mockSignatureResponse());

        var zammadSyncServiceSubtree = new ZammadSyncServiceSubtree(zammadService, createZammadProperties());
        assertNull(zammadSyncServiceSubtree.findSignatureId(null));

        verify(zammadService, times(0)).getZammadSignatures();
    }


	private List<Signatures> mockSignatureResponse() {


	    var signature = new Signatures();
        signature.setId(5);
        signature.setName(ORGANIZATIONAL_UNIT);

        return List.of(signature);

	}


}
