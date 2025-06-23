package de.muenchen.zammad.ad.ldap;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.muenchen.userservice.ActiveDirectoryService;
import de.muenchen.zammad.PrepareZammadTestEnvironment;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.branch.ZammadService;

public abstract class PrepareTestCrossFunctionalGroups extends PrepareZammadTestEnvironment {

    final Map<String, String> activeDirectoryUsers = Map.of("tick.duck", "CN=tick.duck,OU=d,OU=ITM,OU=c,DC=b,DC=a", "trick.duck", "CN=trick.duck,OU=kd,OU=ITM,OU=c,DC=b,DC=a", "track.duck", "CN=track.duck,OU=d,OU=ITM,OU=c,DC=bn,DC=a");

    protected List<EnhancedActiveDirectoryGroupDTO> createAndUpdateZammadRolesEveryUserOnlyInOneRole() {

        return List.of(new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle1-ig",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle1-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                List.of(activeDirectoryUsers.get("tick.duck"),
                        activeDirectoryUsers.get("trick.duck")),
                Map.of("lhmObjectIdTrickDuck",
                        new ActiveDirectoryUserDTO("trick.duck", "lhmObjectIdTrickDuck",
                                activeDirectoryUsers.get("trick.duck"),
                                "Trick Duck", "trick.duck", "trick.duck", "Trick", "Duck", "mail@", "ou"),
                        "lhmObjectIdTickDuck",
                        new ActiveDirectoryUserDTO("tick.duck", "lhmObjectIdTickDuck",
                                activeDirectoryUsers.get("tick.duck"), "Tick Duck",
                                "tick.duck", "tick.duck", "Tick", "Duck", "mail@", "ou"))),
                new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "CN=lhm-ab-dbsticketing-rit-testrolle2-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        List.of(activeDirectoryUsers.get("track.duck")),
                        Map.of("lhmObjectIdTrackDuck",
                                new ActiveDirectoryUserDTO("track.duck", "lhmObjectIdTrackDuck",
                                        activeDirectoryUsers.get("track.duck"),
                                        "Track Duck", "track.duck", "track.duck", "Track", "Duck", "mail@", "ou"))));
    }

    protected List<EnhancedActiveDirectoryGroupDTO> createAndUpdateZammadRolesUserInManyRoles() {

        return List.of(new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle1-ig",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle1-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                List.of(activeDirectoryUsers.get("tick.duck"),
                        activeDirectoryUsers.get("trick.duck"),
                        activeDirectoryUsers.get("track.duck")),
                Map.of("lhmObjectIdTrickDuck",
                        new ActiveDirectoryUserDTO("trick.duck", "lhmObjectIdTrickDuck",
                                activeDirectoryUsers.get("trick.duck"),
                                "Trick Duck", "trick.duck", "trick.duck", "Trick", "Duck", "mail@", "ou"),
                        "lhmObjectIdTickDuck",
                        new ActiveDirectoryUserDTO("tick.duck", "lhmObjectIdTickDuck",
                                activeDirectoryUsers.get("tick.duck"), "Tick Duck",
                                "tick.duck", "tick.duck", "Tick", "Duck", "mail@", "ou"),
                        "lhmObjectIdTrackDuck",
                        new ActiveDirectoryUserDTO("track.duck", "lhmObjectIdTrackDuck",
                                activeDirectoryUsers.get("track.duck"),
                                "Track Duck", "track.duck", "track.duck", "Track", "Duck", "mail@", "ou"))),
                new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "CN=lhm-ab-dbsticketing-rit-testrolle2-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        List.of(activeDirectoryUsers.get("track.duck")),
                        Map.of("lhmObjectIdTrackDuck",
                                new ActiveDirectoryUserDTO("track.duck", "lhmObjectIdTrackDuck",
                                        activeDirectoryUsers.get("track.duck"),
                                        "Track Duck", "track.duck", "track.duck", "Track", "Duck", "mail@", "ou"))));
    }

    protected List<EnhancedActiveDirectoryGroupDTO> removedZammadRoleUsers() {

        return List.of(new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle1-ig",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle1-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                List.of(activeDirectoryUsers.get("trick.duck")),
                Map.of("lhmObjectIdTrickDuck",
                        new ActiveDirectoryUserDTO("trick.duck", "lhmObjectIdTrickDuck",
                                activeDirectoryUsers.get("trick.duck"),
                                "Trick Duck", "trick.duck", "trick.duck", "Trick", "Duck", "mail@", "ou"))),
        new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle2-ig",
                "lhm-ab-dbsticketing-rit-testrolle2-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle2-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle2-ig",
                List.of(),
                Map.of()));
    }

    protected void mockUserLookUps(ActiveDirectoryService activeDirectoryService) {

        when(activeDirectoryService.lookupUser("CN=tick.duck,OU=d,OU=ITM,OU=c,DC=b,DC=a")).thenReturn(new ActiveDirectoryUserDTO("cn", "lhmObjectIdTickDuck", "distinguishedName", "displayName", "name", "uid", "Ticke", "Duck", "mail", "ou"));
        when(activeDirectoryService.lookupUser("CN=trick.duck,OU=kd,OU=ITM,OU=c,DC=b,DC=a")).thenReturn(new ActiveDirectoryUserDTO("cn", "lhmObjectIdTrickDuck", "distinguishedName", "displayName", "name", "uid", "Trick", "Duck", "mail", "ou"));
        when(activeDirectoryService.lookupUser("CN=track.duck,OU=d,OU=ITM,OU=c,DC=bn,DC=a")).thenReturn(new ActiveDirectoryUserDTO("cn", "lhmObjectIdTrackDuck", "distinguishedName", "displayName", "name", "uid", "Track", "Duck", "mail", "ou"));

    }

    protected void mockZammadServiceActions(ZammadService zammadService) {

        when(zammadService.createZammadUser(new User(null, "Trick", "Duck", "lhmObjectIdTrickDuck", true, "mail", "ou", "lhmObjectIdTrickDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998)), null, null, true, null))).thenReturn(new User(1, "Trick", "Duck", "lhmObjectIdTrickDuck", true, "mail", "ou", "lhmObjectIdTrickDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998)), null, null, true, null));
        when(zammadService.createZammadUser(new User(null, "Track", "Duck", "lhmObjectIdTrackDuck", true, "mail", "ou", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998)), null, null, true, null))).thenReturn(new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", true, "mail", "ou", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998)), null, null, true, null));

        when(zammadService.updateZammadUser(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", false, null, "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0, 1)), null, null, false, null))).thenReturn(new User(2, "Tick", "Duck", "lhmObjectIdTickDuck", false, null, "ITM", "lhmObjectIdTickDuck", new ArrayList<Integer>(Arrays.asList(0, 1)), null, null, false, null));
        when(zammadService.updateZammadUser(new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", false, null, "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1)), null, null, false, null))).thenReturn(new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", false, null, "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1)), null, null, false, null));
        when(zammadService.updateZammadUser(new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", false, null, "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998, 999)), null, null, true, null))).thenReturn(new User(3, "Track", "Duck", "lhmObjectIdTrackDuck", false, null, "ITM", "lhmObjectIdTrackDuck", new ArrayList<Integer>(Arrays.asList(0, 1, 998, 999)), null, null, true, null));

    }

}
