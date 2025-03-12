package de.muenchen.zammad.ad.ldap;

import java.util.List;
import java.util.Map;

import de.muenchen.zammad.PrepareTestShadetree;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;

public abstract class PrepareTestCrossFunctionalGroups extends PrepareTestShadetree {

    protected List<EnhancedActiveDirectoryGroupDTO> createCrossFunctionalGroups() {

        return List.of(new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle1-ig",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                "CN=lhm-ab-dbsticketing-rit-testrolle1-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                "lhm-ab-dbsticketing-rit-testrolle1-ig",
                List.of("CN=tick.duck,OU=d,OU=ITM,OU=c,DC=b,DC=a",
                        "CN=trick.duck,OU=kd,OU=ITM,OU=c,DC=b,DC=a"),
                Map.of("lhmobjectId_2_1_1",
                        new ActiveDirectoryUserDTO("trick.duck", "lhmobjectId_2_1_1",
                                "CN=trick.duck,OU=d,OU=ITM,OU=c,DC=b,DC=a",
                                "Trick Duck", "trick.duck", "trick.duck", "ITM"),
                        "lhmobjectId_2_3_1",
                        new ActiveDirectoryUserDTO("tick.duck", "lhmobjectId_2_3_1",
                                "CN=tick.duck,OU=d,OU=ITM,OU=c,DC=b,DC=a", "Tick Duck",
                                "tick.duck", "tick.duck", "ITM"))),
                new EnhancedActiveDirectoryGroupDTO("lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        "CN=lhm-ab-dbsticketing-rit-testrolle2-ig,OU=f,OU=d,OU=c,DC=b,DC=a",
                        "lhm-ab-dbsticketing-rit-testrolle2-ig",
                        List.of("CN=track.duck,OU=d,OU=ITM,OU=c,DC=bn,DC=a"),
                        Map.of("lhmobjectId_2_3_1",
                                new ActiveDirectoryUserDTO("track.duck", "lhmobjectId_2_3_1",
                                        "CN=track.duck,OU=Users,OU=ITM,OU=Bereiche,DC=muenchen,DC=de",
                                        "Track Duck", "track.duck", "track.duck", "ITM"))));
    }

}
