package de.muenchen.zammad.ad.ldap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

import de.muenchen.userservice.ActiveDirectoryService;
import de.muenchen.userservice.ShadeTree;
import de.muenchen.zammad.ad.ActiveDirectoryGroupService;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryGroupZammadRoleMapper;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.branch.EliminatedLdapUser;
import de.muenchen.zammad.ldap.branch.ZammadService;
import de.muenchen.zammad.property.ActiveDirectoryProperty;

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
        when(zammadService.getZammadRoles()).thenReturn(
                List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));

        var users = List.of(
                new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1))),
                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1))));
        mockUsersCache(zammadService, users);

        mockZammadServiceActions(zammadService);

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups())
                .thenReturn(Optional.of(createAndUpdateZammadRolesEveryUserOnlyInOneRole()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(1)).createZammadUser(createUserCaptor.capture());
        assertEquals("lhmObjectIdTrickDuck", createUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0, 1, 998), createUserCaptor.getAllValues().get(0).getRoleIds());

        verify(zammadService, times(2)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmObjectIdTickDuck", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0, 1, 998), updateUserCaptor.getAllValues().get(0).getRoleIds());
        assertEquals("lhmObjectIdTrackDuck", updateUserCaptor.getAllValues().get(1).getLhmobjectid());
        assertEquals(List.of(0, 1, 999), updateUserCaptor.getAllValues().get(1).getRoleIds());

    }

    @Test
    void createAndRemoveRoles() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(
                List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));

        var users = List.of(
                new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1, 998))),
                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1, 999))));
        mockUsersCache(zammadService, users);

        mockZammadServiceActions(zammadService);

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(removedZammadRoleUsers()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(1)).createZammadUser(createUserCaptor.capture());
        assertEquals("lhmObjectIdTrickDuck", createUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0, 1, 998), createUserCaptor.getAllValues().get(0).getRoleIds());

        verify(zammadService, times(2)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("lhmObjectIdTickDuck", updateUserCaptor.getAllValues().get(0).getLhmobjectid());
        assertEquals(List.of(0, 1), updateUserCaptor.getAllValues().get(0).getRoleIds());
        assertEquals("lhmObjectIdTrackDuck", updateUserCaptor.getAllValues().get(1).getLhmobjectid());
        assertEquals(List.of(0, 1), updateUserCaptor.getAllValues().get(1).getRoleIds());

    }

    @Test
    void zammadRolesNotExistDoNotCreateUpdateRoles() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(List.of());
        var users = List.of(
                new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1))),
                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1))));

        mockUsersCache(zammadService, users);
        mockZammadServiceActions(zammadService);

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups())
                .thenReturn(Optional.of(createAndUpdateZammadRolesEveryUserOnlyInOneRole()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);
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
        when(zammadService.getZammadRoles()).thenReturn(
                List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));
        var users = List.of(
                new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", "ITM", "lhmObjectIdTickDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1, 998))),
                new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", "ITM", "lhmObjectIdTrackDuck",
                        new ArrayList<Integer>(Arrays.asList(0, 1, 999))));
        mockUsersCache(zammadService, users);
        mockZammadServiceActions(zammadService);

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups()).thenReturn(Optional.of(List.of()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(0)).createZammadUser(createUserCaptor.capture());
        verify(zammadService, times(0)).updateZammadUser(updateUserCaptor.capture());
    }

    /*
     * Add users not exist in ldap and are present in more than one active directory
     * group.
     */
    @Test
    void userExistsInManyActiveDirectoryGroups() {

        var zammadService = mock(ZammadService.class);
        when(zammadService.getZammadGroups()).thenReturn(List.of());
        when(zammadService.getZammadRoles()).thenReturn(
                List.of(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null)));
    var users = List.of(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck",
                "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998))));

        mockUsersCache(zammadService, users);
        mockZammadServiceActions(zammadService);

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);
        when(activeDirectoryGroupService.crossOrganizationalGroups())
                .thenReturn(Optional.of(createAndUpdateZammadRolesUserInManyRoles()));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);
        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(2)).createZammadUser(createUserCaptor.capture());
        var track = createUserCaptor.getAllValues().stream().filter(user -> user.getFirstname().equals("Track"))
                .toList();
        assertEquals("Track", track.get(0).getFirstname());
        assertEquals(List.of(0, 1, 998), track.get(0).getRoleIds());
        verify(zammadService, times(1)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("Track", updateUserCaptor.getAllValues().get(0).getFirstname());
        assertEquals(List.of(0, 1, 998, 999), updateUserCaptor.getAllValues().get(0).getRoleIds());

    }

    /*
     * User is deactivated because they left their ldap organization unit and then reactivated through an active directory role.
     */
     @Test
    void deactivationResetByActiveDirectorRoleTest() {

        // Deactivate by ldap

        var zammadService = mock(ZammadService.class);
        var groups = List.of(new Group(1, null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null));
        mockGroupsCache(zammadService, groups);

        var zammadUsers = new ArrayList<User>(Arrays.asList(new User(1, "trick", "duck", "lhmObjectIdTrickDuck", true, null, null, "lhmObjectIdTrickDuck", new ArrayList<>(Arrays.asList(0, 1)), Map.of("1", List.of("full")), null, true, null)));
        mockUsersCache(zammadService, zammadUsers);

        userAndGroupMocks(zammadService);
        channelsMock(zammadService);

        assertEquals(1, zammadService.getZammadGroups().size());
        assertEquals(1, zammadService.getZammadUsers().size());

        var deletedLdapUser = new EliminatedLdapUser(zammadService);

        var reducedLdapTree = reducedLdapTree();
        var rootNode = reducedLdapTree.entrySet().iterator().next().getValue();

        assertEquals(17, rootNode.flatListLdapUserDTO().size());

        var reducedEnhancedLdapUserDTO = ShadeTree.collectUserFromAllBranches(reducedLdapTree);

        deletedLdapUser.checkForRemoval(Map.of(rootNode.getDistinguishedName(), rootNode).entrySet().iterator().next(), Optional.of(reducedEnhancedLdapUserDTO));

        verify(zammadService, times(1)).updateZammadUser(updateUserCaptor.capture());
        assertEquals("delete", updateUserCaptor.getAllValues().get(0).getLdapsyncstate());
        assertFalse(updateUserCaptor.getAllValues().get(0).isActive());

        // Activate by active directory

        var activeDirectoryService = mock(ActiveDirectoryService.class);
        mockUserLookUps(activeDirectoryService);

        when(zammadService.getZammadRoles()).thenReturn(new ArrayList<>(Arrays.asList(new Role(998, "rit-testrolle1-ig", null), new Role(999, "rit-testrolle2-ig", null))));

        var activeDirectoryGroupService = mock(ActiveDirectoryGroupService.class);

        var adRole = new ArrayList<>(Arrays.asList(new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle1-ig",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle1-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                List.of(activeDirectoryUsers.get("tick.duck"),
                        activeDirectoryUsers.get("trick.duck")),
                Map.of("lhmObjectIdTrickDuck",
                        new ActiveDirectoryUserDTO("trick.duck", "lhmObjectIdTrickDuck",
                                activeDirectoryUsers.get("trick.duck"),
                                "Trick Duck", "trick.duck", "trick.duck", "Trick", "Duck", "mail@", "ou")))));

        when(activeDirectoryGroupService.crossOrganizationalGroups())
                .thenReturn(Optional.of(adRole));

        var activeDirectoryRoleMapper = new ActiveDirectoryGroupZammadRoleMapper(activeDirectoryService,
                createZammadProperties(), new ActiveDirectoryProperty(null, null, null, null, "lhm-ab-dbsticketing-"),
                activeDirectoryGroupService, zammadService);

        activeDirectoryRoleMapper.syncAdGroupsToLdapRoles();

        verify(zammadService, times(2)).updateZammadUser(updateUserCaptor.capture());
        var updates = updateUserCaptor.getAllValues();
        assertEquals("lhmObjectIdTrickDuck", updates.get(0).getLhmobjectid());
        assertTrue("Should be reset to activate.", updates.get(0).isActive());
        assertNull("Should be reset.", updates.get(0).getLdapsyncstate());

    }

}
