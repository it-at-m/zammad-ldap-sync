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
package de.muenchen.userservice;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private String organizationalUnit;
    private String distinguishedName;
    private EnhancedLdapOuSearchResultDTO node;
    private Optional<Map<String, LdapOuNode>> childNodes = Optional.of(new TreeMap<>());
    private Optional<List<EnhancedLdapUserDTO>> users = Optional.of(new ArrayList<>());

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

        getUsers().ifPresent(nodeUsers -> nodeUsers.forEach(u -> tree.append(tab).append(u.toString()).append(System.lineSeparator())));
        getChildNodes().ifPresent(nodeChildNodes -> nodeChildNodes.forEach((k, v) -> tree.append(v.formatTree(tab + "     "))));

        return tree.toString();
    }

    /**
     * Creates a list of all LdapUserDTOs contained in the subtree
     *
     * @return list
     */
    public List<EnhancedLdapUserDTO> flatListLdapUserDTO() {
        var enhancedLdapUsers = new ArrayList<EnhancedLdapUserDTO>();
        getUsers().ifPresent(nodeUsers -> enhancedLdapUsers.addAll(nodeUsers));
        getChildNodes().ifPresent(nodeChildNodes -> enhancedLdapUsers.addAll(flatListLdapUserDTO(nodeChildNodes)));
        return enhancedLdapUsers;
    }

    private List<EnhancedLdapUserDTO> flatListLdapUserDTO(Map<String, LdapOuNode> subtree) {

        var enhancedLdapUsers = new ArrayList<EnhancedLdapUserDTO>();

        subtree.forEach((key, nodeEntry) -> {
            nodeEntry.getUsers().ifPresent(nodeUsers -> enhancedLdapUsers.addAll(nodeUsers));
            nodeEntry.getChildNodes().ifPresent(nodeChildNodes -> enhancedLdapUsers.addAll(flatListLdapUserDTO(nodeChildNodes)));
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
        getChildNodes().ifPresent(nodeChildNodes -> ous.addAll(flatListLdapOuDTO(nodeChildNodes)));
        return ous;
    }

    private List<LdapOuSearchResultDTO> flatListLdapOuDTO(Map<String, LdapOuNode> subtree) {

        var ous = new ArrayList<LdapOuSearchResultDTO>();
        subtree.forEach((key, nodeEntry) -> {
            ous.add(nodeEntry.getNode());
            nodeEntry.getChildNodes().ifPresent(nodeChildNodes -> ous.addAll(flatListLdapOuDTO(nodeChildNodes)));
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

        getChildNodes().ifPresent(nodeChildNodes -> ous.addAll(flatListLdapOuNode(nodeChildNodes)));
        return ous;
    }

    private List<LdapOuNode> flatListLdapOuNode(Map<String, LdapOuNode> subtree) {

        var ous = new ArrayList<LdapOuNode>();
        subtree.forEach((key, nodeEntry) -> {
            ous.add(nodeEntry);
            nodeEntry.getChildNodes().ifPresent(nodeChildNodes -> ous.addAll(flatListLdapOuNode(nodeChildNodes)));
        });
        return ous;
    }

    /*
     * Find ldap node by distinguished name
     */
    public Optional<LdapOuNode> findLdapOuNode(String distinguishedName) {
        var nodes = flatListLdapOuNode();
        return nodes.stream().filter(currentNode -> currentNode.getDistinguishedName().equals(distinguishedName)).findFirst();
    }


    public String json() throws JsonProcessingException {
    	return new ObjectMapper().writeValueAsString(childNodes);
    }

}
