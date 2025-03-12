package de.muenchen.zammad.ad.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryShadeTreeInclusion;
import de.muenchen.zammad.ldap.property.OrganizationalUnitProperties;
import de.muenchen.zammad.ldap.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import de.muenchen.zammad.ldap.tree.LdapService;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MergeAdToShadetreeTest extends PrepareTestCrossFunctionalGroups {

    @Test
    void userInvalidLhmObjectPathTest() {

        var userSearchBase = "ou=d,ou=c,o=b,c=a";

        var ldapService = mock(LdapService.class);
        var p1 = new EnhancedLdapUserDTO();
        p1.setLhmObjectId("lhmobjectId_2_1_1");
        when(ldapService.lookupUser("uid=trick.duck," + userSearchBase)).thenReturn(Optional.of(p1));
        var p2 = new EnhancedLdapUserDTO();
        p2.setLhmObjectId("lhmobjectId_2_2_1");
        when(ldapService.lookupUser("uid=tick.duck," + userSearchBase)).thenReturn(Optional.of(p2));
        var p3 = new EnhancedLdapUserDTO();
        p3.setLhmObjectId("lhmobjectId_2_3_1");
        when(ldapService.lookupUser("uid=track.duck," + userSearchBase)).thenReturn(Optional.of(p3));

        var requestedOrganizationalUnits = new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, userSearchBase,  null)));

        var inclusionService = new ActiveDirectoryShadeTreeInclusion(requestedOrganizationalUnits, ldapService);
        var shadeTree = createLdapTree();

        var longname21 = findNode(shadeTree, "longname_2_1").get();
        var longname22 = findNode(shadeTree, "longname_2_2").get();
        var longname23 = findNode(shadeTree, "longname_2_3").get();

        assertEquals(3, longname21.getUsers().get().size());
        longname21.getUsers().get().forEach(user -> user.setLhmObjectPath(null));
        assertEquals(3, longname22.getUsers().get().size());
        longname22.getUsers().get().forEach(user -> user.setLhmObjectPath(null));
        assertEquals(3, longname23.getUsers().get().size());
        longname23.getUsers().get().forEach(user -> user.setLhmObjectPath(null));

        inclusionService.merge(createCrossFunctionalGroups(), shadeTree);

        var ritTestrolle1Ig = findNode(shadeTree, "lhm-ab-dbsticketing-rit-testrolle1-ig");
        assertTrue(ritTestrolle1Ig.isEmpty());

        var ritTestrolle2Ig = findNode(shadeTree, "lhm-ab-dbsticketing-rit-testrolle2-ig");
        assertTrue(ritTestrolle2Ig.isEmpty());

        assertEquals(3, longname21.getUsers().get().size());
        assertEquals(3, longname22.getUsers().get().size());
        assertEquals(3, longname23.getUsers().get().size());

    }

    @Test
    void mergeTreeNodesTest() {

        var userSearchBase = "ou=d,ou=c,o=b,c=a";

        var ldapService = mock(LdapService.class);
        var p1 = new EnhancedLdapUserDTO();
        p1.setLhmObjectId("lhmobjectId_2_1_1");
        p1.setLhmObjectPath("dn_level_2_no_1,dn_level_1_no_2,dn_level_0_no_1");
        when(ldapService.lookupUser("uid=trick.duck," + userSearchBase)).thenReturn(Optional.of(p1));
        var p2 = new EnhancedLdapUserDTO();
        p2.setLhmObjectId("lhmobjectId_2_2_1");
        p2.setLhmObjectPath("dn_level_2_no_2,dn_level_1_no_2,dn_level_0_no_1");
        when(ldapService.lookupUser("uid=tick.duck," + userSearchBase)).thenReturn(Optional.of(p2));
        var p3 = new EnhancedLdapUserDTO();
        p3.setLhmObjectId("lhmobjectId_2_3_1");
        p3.setLhmObjectPath("dn_level_2_no_3,dn_level_1_no_2,dn_level_0_no_1");
       when(ldapService.lookupUser("uid=track.duck," + userSearchBase)).thenReturn(Optional.of(p3));

        var requestedOrganizationalUnits = new RequestedOrganizationalUnits(Map.of("ITM", new OrganizationalUnitProperties(null, userSearchBase,  null)));

        var inclusionService = new ActiveDirectoryShadeTreeInclusion(requestedOrganizationalUnits, ldapService);
        var shadeTree = createLdapTree();

        var longname21 = findNode(shadeTree, "longname_2_1").get();
        var longname22 = findNode(shadeTree, "longname_2_2").get();
        var longname23 = findNode(shadeTree, "longname_2_3").get();

        assertEquals(3, longname21.getUsers().get().size());
        assertEquals(3, longname22.getUsers().get().size());
        assertEquals(3, longname23.getUsers().get().size());

        inclusionService.merge(createCrossFunctionalGroups(), shadeTree);

        var ritTestrolle1Ig = findNode(shadeTree, "lhm-ab-dbsticketing-rit-testrolle1-ig").get();
        assertEquals("rit-testrolle1-ig", ritTestrolle1Ig.getNode().getLhmOUShortname());
        assertEquals("ou=lhm-ab-dbsticketing-rit-testrolle1-ig,dn_level_1_no_2,dn_level_0_no_1", ritTestrolle1Ig.getDistinguishedName());
        assertEquals(2, ritTestrolle1Ig.getUsers().get().size());
        assertTrue(ritTestrolle1Ig.getUsers().get().stream().filter(user -> user.getLhmObjectId().equals("lhmobjectId_2_1_1")).findFirst().isPresent());
        assertTrue(ritTestrolle1Ig.getUsers().get().stream().filter(user -> user.getLhmObjectId().equals("lhmobjectId_2_2_1")).findFirst().isPresent());

        var ritTestrolle2Ig = findNode(shadeTree, "lhm-ab-dbsticketing-rit-testrolle2-ig").get();
        assertEquals("rit-testrolle2-ig",ritTestrolle2Ig.getNode().getLhmOUShortname());
        assertEquals("ou=lhm-ab-dbsticketing-rit-testrolle2-ig,dn_level_2_no_3,dn_level_1_no_2,dn_level_0_no_1", ritTestrolle2Ig.getDistinguishedName());
        assertEquals(1, ritTestrolle2Ig.getUsers().get().size());
        assertTrue(ritTestrolle2Ig.getUsers().get().stream().filter(user -> user.getLhmObjectId().equals("lhmobjectId_2_3_1")).findFirst().isPresent());

        assertEquals(2, longname21.getUsers().get().size());
        assertEquals(2, longname22.getUsers().get().size());
        assertEquals(2, longname23.getUsers().get().size());

    }

    private Optional<LdapOuNode> findNode(Map<String, LdapOuNode> shadeTree, String lhmOULongname) {
        return ((LdapOuNode)shadeTree.values().toArray()[0]).flatListLdapOuNode().stream().filter(node -> node.getNode().getLhmOULongname() != null && node.getNode().getLhmOULongname().equals(lhmOULongname)).findFirst();
    }

}
