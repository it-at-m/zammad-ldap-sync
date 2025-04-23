package de.muenchen.zammad.ad.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.userservice.LdapService;
import de.muenchen.zammad.ad.ActiveDirectoryGroupService;
import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryGroupZammadRoleMapper;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.branch.ZammadService;
import de.muenchen.zammad.property.ActiveDirectoryProperty;
import de.muenchen.zammad.property.OrganizationalUnitProperties;
import de.muenchen.zammad.property.RequestedOrganizationalUnits;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActiveDirectoryGroupsTest extends PrepareTestCrossFunctionalGroups {

    public static final String LDAP_USER_SEARCH_BASE = "ou=users,ou=ITM,o=b,c=a";

    @Captor
    private ArgumentCaptor<User> createUserCaptor;

    @Captor
    private ArgumentCaptor<User> updateUserCaptor;

    @Test
    void createAndUpdateRoles() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));
        when(zammadService.getZammadUsers()).thenReturn(List.of(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0,1))),
                                                                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0,1)))));
        mockZammadServiceActions(zammadService);

        var ldapService = mock(LdapService.class);
        mockUserLookUps(ldapService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(createAndUpdateZammadRoles()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(ldapService, createZammadProperties(), new ActiveDirectoryProperty(null, null, null,null, "lhm-ab-dbsticketing-"), activeDirectoryGroupService, zammadService, new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, LDAP_USER_SEARCH_BASE, null))));
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(1)).createZammadUser(createUserCaptor.capture());
        assertEquals("lhmObjectIdTrickDuck", createUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0,1,998), createUserCaptor.getAllValues().get(0).getRoleIds());

        verify(zammadService, times(2)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmObjectIdTickDuck", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0,1,998), updateUserCaptor.getAllValues().get(0).getRoleIds());
        assertEquals("lhmObjectIdTrackDuck", updateUserCaptor.getAllValues().get(1).getLhmobjectid());
        assertEquals(List.of(0,1,999), updateUserCaptor.getAllValues().get(1).getRoleIds());

    }

    @Test
    void createAndRemoveRoles() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));
        when(zammadService.getZammadUsers()).thenReturn(List.of(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0,1,998))),
                                                                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0,1,999)))));
        mockZammadServiceActions(zammadService);

        var ldapService = mock(LdapService.class);
        mockUserLookUps(ldapService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(removedZammadRoleUsers()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(ldapService, createZammadProperties(), new ActiveDirectoryProperty(null, null, null,null, "lhm-ab-dbsticketing-"), activeDirectoryGroupService, zammadService, new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, LDAP_USER_SEARCH_BASE, null))));
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(1)).createZammadUser(createUserCaptor.capture());
        assertEquals("lhmObjectIdTrickDuck", createUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0,1,998), createUserCaptor.getAllValues().get(0).getRoleIds());

        verify(zammadService, times(2)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmObjectIdTickDuck", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0,1), updateUserCaptor.getAllValues().get(0).getRoleIds());
        assertEquals("lhmObjectIdTrackDuck", updateUserCaptor.getAllValues().get(1).getLhmobjectid());
        assertEquals(List.of(0,1), updateUserCaptor.getAllValues().get(1).getRoleIds());

    }

    @Test
    void zammadRolesNotExistDoNotCreateUpdateRoles() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(List.of());
        when(zammadService.getZammadUsers()).thenReturn(List.of(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0,1))),
                                                                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0,1)))));
        mockZammadServiceActions(zammadService);

        var ldapService = mock(LdapService.class);
        mockUserLookUps(ldapService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(createAndUpdateZammadRoles()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(ldapService, createZammadProperties(), new ActiveDirectoryProperty(null, null, null,null, "lhm-ab-dbsticketing-"), activeDirectoryGroupService, zammadService, new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, LDAP_USER_SEARCH_BASE, null))));
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(0)).createZammadUser(createUserCaptor.capture());
        verify(zammadService, times(0)).updateZammadUser(updateUserCaptor.capture());

    }

    /*
     * Only active directory group removed. Nothing to do, zammad role still exists.
     */
    @Test
    void activeDirectoryGroupsRemoved() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));
        when(zammadService.getZammadUsers()).thenReturn(List.of(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0,1,998))),
                                                                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0,1,999)))));
        mockZammadServiceActions(zammadService);

        var ldapService = mock(LdapService.class);
        mockUserLookUps(ldapService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(List.of()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(ldapService, createZammadProperties(), new ActiveDirectoryProperty(null, null, null,null, "lhm-ab-dbsticketing-"), activeDirectoryGroupService, zammadService, new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, LDAP_USER_SEARCH_BASE, null))));
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(0)).createZammadUser(createUserCaptor.capture());
        verify(zammadService, times(0)).updateZammadUser(updateUserCaptor.capture());
    }

}
