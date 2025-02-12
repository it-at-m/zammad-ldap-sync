package de.muenchen.zammad.ldap.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.Getter;


public class CrossOrganizationalGroupsLdapOuNodeMapper {

    private List<EnhancedActiveDirectoryGroupDTO> groups;

    @Getter
    private Map<String, LdapOuNode> branches = new HashMap<>();

    public CrossOrganizationalGroupsLdapOuNodeMapper(List<EnhancedActiveDirectoryGroupDTO> groups) {
        super();
        this.groups = groups;
        mapper();

    }


    private void mapper() {

        for(EnhancedActiveDirectoryGroupDTO group : this.groups) {
            var node = new LdapOuNode();
     //       node.setNode(group);
            node.setDistinguishedName(null);
        }

    }



}
