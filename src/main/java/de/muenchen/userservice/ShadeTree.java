package de.muenchen.userservice;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;

public class ShadeTree {

    private ShadeTree() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, EnhancedLdapUserDTO> collectUserFromAllBranches(Map<String, LdapOuNode> ldapShadetrees) {

        Map<String, EnhancedLdapUserDTO> collection = new TreeMap<>();
        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {
            collection.putAll(entry.getValue().flatListLdapUserDTO().stream()
                    .collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity())));
        }
        return collection;
    }

}
