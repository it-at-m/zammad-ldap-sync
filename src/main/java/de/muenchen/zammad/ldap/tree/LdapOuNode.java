/*
 * The MIT License
 * Copyright © 2023 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.zammad.ldap.tree;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDto;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import de.muenchen.zammad.ldap.service.ZammadLdapException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Node class to create ldap shade tree representation
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LdapOuNode {

    private String distinguishedName;
    private EnhancedLdapOuSearchResultDTO node;
    private Map<String, LdapOuNode> childNodes = new TreeMap<>();
    private List<EnhancedLdapUserDto> users;

    /**
     * Creates formatted representation of the subtree
     *
     * @return String Representation
     */
    @Override
    public String toString() {
        return formatTree("");
    }

    private String formatTree(String tab) {

        var tree = new StringBuilder();
        tree.append(tab).append("***** New LDAP entry : ").append(getNode().getLhmOUShortname()).append(" ").append(getNode().getOu()).append(" *****")
                .append(System.lineSeparator());
        tree.append(tab).append(getDistinguishedName()).append(System.lineSeparator());
        tree.append(tab).append(getNode().toString()).append(System.lineSeparator());

        if (getUsers() != null)
            getUsers().forEach(u -> tree.append(tab).append(u.toString()).append(System.lineSeparator()));

        getChildNodes().forEach((k, v) -> tree.append(v.formatTree(tab + "     ")));

        return tree.toString();
    }

    /**
     * Creates a list of all LdapUserDTOs contained in the subtree
     *
     * @return list
     */
    public List<EnhancedLdapUserDto> flatListLdapUserDTO() {
        var enhancedLdapUsers = new ArrayList<EnhancedLdapUserDto>();
        if (this.getUsers() != null)
            enhancedLdapUsers.addAll(this.getUsers());

        enhancedLdapUsers.addAll(flatListLdapUserDTO(this.getChildNodes()));

        return enhancedLdapUsers;
    }

    private List<EnhancedLdapUserDto> flatListLdapUserDTO(Map<String, LdapOuNode> subtree) {

        var enhancedLdapUsers = new ArrayList<EnhancedLdapUserDto>();
        subtree.forEach((key, nodeEntry) -> {
            if (nodeEntry.getUsers() != null)
                enhancedLdapUsers.addAll(nodeEntry.getUsers());

            enhancedLdapUsers.addAll(flatListLdapUserDTO(nodeEntry.getChildNodes()));
        });
        return enhancedLdapUsers;
    }

    /**
     * Creates a list of all LdapOuSearchResultDTO contained in the subtree
     *
     * @return list
     */
    public List<LdapOuSearchResultDTO> flatListLdapOuDTO() {

        var ous = new ArrayList<LdapOuSearchResultDTO>();
        ous.add(this.getNode());

        ous.addAll(flatListLdapOuDTO(this.getChildNodes()));
        return ous;
    }

    private List<LdapOuSearchResultDTO> flatListLdapOuDTO(Map<String, LdapOuNode> subtree) {

        var ous = new ArrayList<LdapOuSearchResultDTO>();
        subtree.forEach((key, nodeEntry) -> {
            ous.add(nodeEntry.getNode());
            ous.addAll(flatListLdapOuDTO(nodeEntry.getChildNodes()));
        });
        return ous;
    }

    /**
     * Creates a list of all LdapOuNode contained in the subtree
     *
     * @return list
     */
    public List<LdapOuNode> flatListLdapOuNode() {

        var ous = new ArrayList<LdapOuNode>();
        ous.add(this);

        ous.addAll(flatListLdapOuNode(this.getChildNodes()));
        return ous;
    }

    private List<LdapOuNode> flatListLdapOuNode(Map<String, LdapOuNode> subtree) {

        var ous = new ArrayList<LdapOuNode>();
        subtree.forEach((key, nodeEntry) -> {
            ous.add(nodeEntry);
            ous.addAll(flatListLdapOuNode(nodeEntry.getChildNodes()));
        });
        return ous;
    }

    /*
     * Find ldap node by distinguished name
     */
    public LdapOuNode findLdapOuNode(String distinguishedName) {
        var nodes = flatListLdapOuNode();
        return nodes.stream().filter(node -> node.getDistinguishedName().equals(distinguishedName)).findFirst().orElseThrow();
    }


    public String json() throws JsonProcessingException {
    	return new ObjectMapper().writeValueAsString(childNodes);
    }

}
