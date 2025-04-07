package de.muenchen.zammad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.userservice.LdapOuNode;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrepareLdapTestShadetree {

    public static final String ORGANIZATIONAL_UNIT_CHANNEL = "ITM";
    public static final String STANDARD_EMAIL_CHANNEL = "LHM";

	protected  Map<String, LdapOuNode> createLdapTree() {

	    var dn = "dn_level_0_no_1";
	    var rootNode = new LdapOuNode(ORGANIZATIONAL_UNIT_CHANNEL, dn, createEnhancedLdapOuSearchResultDTO(0,1), Optional.of(createNextDnLevel(1, dn)), Optional.of(createLdapOuUser(0, 0, dn) ));

	    var root = new HashMap<String, LdapOuNode>();
        root.put(dn, rootNode);

        log.info("Test groups created: " + rootNode.flatListLdapOuDTO().size());
        log.info("Test user created: " + rootNode.flatListLdapUserDTO().size());
        log.info(rootNode.toString());

	    return root;
	}

	protected Group zammadGroup_lhmobjectId_1_1_reset() {
        return new Group("5", "1", "shortname_0_1::shortname_1_1_reset", true, true, "lhmobjectId_1_1", "changed", null, null);
    }

	protected Group zammadGroup_lhmobjectId_2_2_2_reset() {
        return new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2_reset", true, true, "lhmobjectId_2_2", null, null, null);
    }

    protected User zammadUser_lhmobjectId_2_2_3_reset() {
        return new User("6", "vorname_2_2_3", "nachname_2_2_3_reset", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null);
    }

	protected List<Group> zammadGroups() {

        var zammadGroup = new ArrayList<Group>();
        zammadGroup.add(new Group("1", null, "shortname_0_1", true, true, "lhmobjectId_0_1", null, null, null));
        zammadGroup.add(new Group("5", "1", "shortname_0_1::shortname_1_1", true, true, "lhmobjectId_1_1", null, null, null));
        zammadGroup.add(new Group("3", "1", "shortname_0_1::shortname_1_2", true, true, "lhmobjectId_1_2", null, null, null));
        zammadGroup.add(new Group("4", "1", "shortname_0_1::shortname_1_3", true, true, "lhmobjectId_1_3", null, null, null));

        zammadGroup.add(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_1", true, true, "lhmobjectId_2_1", null, null, null));
        zammadGroup.add(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_2", true, true, "lhmobjectId_2_2", null, null, null));
        zammadGroup.add(new Group("3", "2", "shortname_0_1::shortname_1_1::shortname_2_3", true, true, "lhmobjectId_2_3", null, null, null));

        return zammadGroup;
    }


	protected List<User> zammadUsers() {

	    var zammadUser = new ArrayList<User>();
	    zammadUser.add(new User("1", "vorname_0_0_1", "nachname_0_0_1", "lhmobjectId_0_0_1", true, null, null, null, List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
	    zammadUser.add(new User("2", "vorname_0_0_2", "nachname_0_0_2", "lhmobjectId_0_0_2", true, null, null, "lhmobjectId_0_0_2", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));
	    zammadUser.add(new User("3", "vorname_0_0_3", "nachname_0_0_3", "lhmobjectId_0_0_3", true, null, null, "lhmobjectId_0_0_3", List.of(0, 1), Map.of("1", List.of("full")), null, true, null));

	    zammadUser.add(new User("4", "vorname_1_1_1", "nachname_1_1_1", "lhmobjectId_1_1_1", true, null, null, "lhmobjectId_1_1_1", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
	    zammadUser.add(new User("5", "vorname_1_1_2", "nachname_1_1_2", "lhmobjectId_1_1_2", true, null, null, null, List.of(0, 1), Map.of("5", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_1_1_3", "nachname_1_1_3", "lhmobjectId_1_1_3", true, null, null, "lhmobjectId_1_1_3", List.of(0, 1), Map.of("5", List.of("full")), null, true, null));

	    zammadUser.add(new User("4", "vorname_1_2_1", "nachname_1_2_1", "lhmobjectId_1_2_1", true, null, null, "lhmobjectId_1_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("5", "vorname_1_2_2", "nachname_1_2_2", "lhmobjectId_1_2_2", true, null, null, "lhmobjectId_1_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_1_2_3", "nachname_1_2_3", "lhmobjectId_1_2_3", true, null, null, "lhmobjectId_1_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new User("4", "vorname_1_3_1", "nachname_1_3_1", "lhmobjectId_1_3_1", true, null, null, "lhmobjectId_1_3_1", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
	    zammadUser.add(new User("5", "vorname_1_3_2", "nachname_1_3_2", "lhmobjectId_1_3_2", true, null, null, "lhmobjectId_1_3_2", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_1_3_3", "nachname_1_3_3", "lhmobjectId_1_3_3", true, null, null, "lhmobjectId_1_3_3", List.of(0, 1), Map.of("4", List.of("full")), null, true, null));

	    zammadUser.add(new User("6", "vorname_2_1_1", "nachname_2_1_1", "lhmobjectId_2_1_1", true, null, null, null, List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_1_2", "nachname_2_1_2", "lhmobjectId_2_1_2", true, null, null, "lhmobjectId_2_1_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_1_3", "nachname_2_1_3", "lhmobjectId_2_1_3", true, null, null, "lhmobjectId_2_1_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new User("6", "vorname_2_2_1", "nachname_2_2_1", "lhmobjectId_2_2_1", true, null, null, "lhmobjectId_2_2_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_2_2", "nachname_2_2_2", "lhmobjectId_2_2_2", true, null, null, "lhmobjectId_2_2_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_2_3", "nachname_2_2_3", "lhmobjectId_2_2_3", true, null, null, "lhmobjectId_2_2_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    zammadUser.add(new User("6", "vorname_2_3_1", "nachname_2_3_1", "lhmobjectId_2_3_1", true, null, null, "lhmobjectId_2_3_1", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_3_2", "nachname_2_3_2", "lhmobjectId_2_3_2", true, null, null, "lhmobjectId_2_3_2", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));
	    zammadUser.add(new User("6", "vorname_2_3_3", "nachname_2_3_3", "lhmobjectId_2_3_3", true, null, null, "lhmobjectId_2_3_3", List.of(0, 1), Map.of("3", List.of("full")), null, true, null));

	    return zammadUser;

	}

	protected Map<String, LdapOuNode> createNextDnLevel(Integer level, String parentDn) {

	    int number = 0;
	    var nodes = new TreeMap<String, LdapOuNode>();

	    var dn = createCurrentDn(String.format("dn_level_%d_no_%d", level, ++number), parentDn);
	    nodes.put(dn,  new LdapOuNode(ORGANIZATIONAL_UNIT_CHANNEL, dn, createEnhancedLdapOuSearchResultDTO(level,number), Optional.of(new TreeMap<>()), Optional.of(createLdapOuUser(level, 1, dn))));

	    dn = createCurrentDn(String.format("dn_level_%d_no_%d", level, ++number), parentDn);
	    var node = new LdapOuNode(ORGANIZATIONAL_UNIT_CHANNEL, dn, createEnhancedLdapOuSearchResultDTO(level,number), Optional.of(new TreeMap<>()), Optional.of(createLdapOuUser(level, 2, dn)));
	    if (level == 1 && number == 2) {
		    node.setChildNodes(Optional.of(createNextDnLevel(2, dn)));
        }
	    nodes.put(dn, node);

	    dn = createCurrentDn(String.format("dn_level_%d_no_%d", level, ++number), parentDn);
        nodes.put(dn,  new LdapOuNode(ORGANIZATIONAL_UNIT_CHANNEL, dn, createEnhancedLdapOuSearchResultDTO(level,number), Optional.of(new TreeMap<>()), Optional.of(createLdapOuUser(level, 3, dn))));

        return nodes;
    }


	protected List<EnhancedLdapUserDTO> createLdapOuUser(Integer level, Integer no, String lhmObjectPath) {

	    var userNo = 0;
	    var user = new ArrayList<EnhancedLdapUserDTO>();

	    var user1 = new EnhancedLdapUserDTO(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
	    user1.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
	    user1.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
	    user1.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
	    user1.setLhmObjectPath(lhmObjectPath);
	    user.add(user1);

	    var user2 = new EnhancedLdapUserDTO(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
        user2.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
        user2.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
        user2.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
        user2.setLhmObjectPath(lhmObjectPath);
        user.add(user2);

        var user3 = new EnhancedLdapUserDTO(null, "lhmObjectUserReference_" + level + "_" + no + "_" + ++userNo );
        user3.setLhmObjectId(String.format("lhmobjectId_%d_%d_%d", level, no, userNo));
        user3.setNachname(String.format("nachname_%d_%d_%d", level, no, userNo));
        user3.setVorname(String.format("vorname_%d_%d_%d", level, no, userNo));
        user3.setLhmObjectPath(lhmObjectPath);
	    user.add(user3);

	    return user;
	}

	protected EnhancedLdapOuSearchResultDTO createEnhancedLdapOuSearchResultDTO(Integer level, Integer no) {

	    var ou = new EnhancedLdapOuSearchResultDTO();

	    ou.setLhmObjectId(String.format("lhmobjectId_%d_%d", level, no));
	    ou.setOu(String.format("ou_%d_%d", level, no));
	    ou.setLhmOUShortname(String.format("shortname_%d_%d", level, no));
	    ou.setLhmOULongname(String.format("longname_%d_%d", level, no));

	    return ou;
	}

	private String createCurrentDn(String dn, String parentDn) {
	    return parentDn != null ? dn.concat("," + parentDn) : dn;
	}

}
