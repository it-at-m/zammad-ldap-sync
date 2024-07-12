package de.muenchen.zammad.ldap.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.muenchen.zammad.ldap.tree.LdapOuNode;
import de.muenchen.zammad.ldap.tree.LdapService;


@Service
public class ZammadLdapService {

    LdapService ldapService;
    public ZammadLdapService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    public Optional<Map<String, LdapOuNode>> calculateOuSubtreeWithUsersByDn(String distinguishedName, String modifyTimeStamp) {

        var subtree =  ldapService.createSubtreeWithUsers(distinguishedName, modifyTimeStamp);

        if (subtree.isEmpty()) {
            return Optional.empty();
        } else {
            return subtree;
        }
    }


}
