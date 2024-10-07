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

import de.muenchen.oss.ezldap.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Slf4j
public class LdapService {

    private static final String ATTRIBUTE_OBJECT_CLASS = "objectClass";

    private static final String ATTRIBUTE_MODIFY_TIMESTAMP = "modifyTimestamp";
    private static final String LHM_ORGANIZATIONAL_UNIT = "lhmOrganizationalUnit";
    private static final String LHM_OBJECT_PATH = "lhmObjectPath";

    private final String ouSearchBase;
    private final String userSearchBase;

    private final LdapTemplate ldapTemplate;
    private final EnhancedLdapUserAttributesMapper enhancedLdapUserAttributesMapper;
    private final EnhancedLdapOuAttributesMapper enhancedLdapOuAttributesMapper;
    private final LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper;
    private final DtoMapper mapper;

    /**
     * Erzeugt eine neue Instanz.
     *
     * @param ldapTemplate                     ein {@link LdapTemplate} für LDAP
     * @param enhancedLdapUserAttributesMapper ein {@link LdapUserAttributesMapper}
     * @param ldapBaseUserAttributesMapper     ein
     *                                         {@link LdapBaseUserAttributesMapper}
     * @param enhancedLdapOuAttributesMapper   ein {@link LdapOuAttributesMapper}
     * @param modelMapper                      ein {@link DtoMapper}
     * @param userSearchBase                   Search-Base für User (DN)
     * @param ouSearchBase                     Search-Base für OUs (DN)
     */
    public LdapService(final LdapTemplate ldapTemplate, final EnhancedLdapUserAttributesMapper enhancedLdapUserAttributesMapper, final LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper,
            final EnhancedLdapOuAttributesMapper enhancedLdapOuAttributesMapper, final DtoMapper modelMapper, final String userSearchBase, final String ouSearchBase) {
        this.ldapTemplate = ldapTemplate;
        this.enhancedLdapUserAttributesMapper = enhancedLdapUserAttributesMapper;
        this.ldapBaseUserAttributesMapper = ldapBaseUserAttributesMapper;
        this.enhancedLdapOuAttributesMapper = enhancedLdapOuAttributesMapper;
        this.mapper = modelMapper;
        this.userSearchBase = userSearchBase;
        this.ouSearchBase = ouSearchBase;
    }

    /**
     * Erzeugt eine Instanz.
     *
     * @param ldapUrl        die LDAP-URL (z.B. 'ldaps://ldap.example.org:636')
     * @param ldapUserDn     LDAP-Zugangsuser (DN)
     * @param ldapPassword   LDAP-Zugangsuser Passwort
     * @param userSearchBase die Search-Base für User (z.B. 'o=example,c=org')
     * @param ouSearchBase   die Search-Base für OU's (z.B. 'o=example,c=org')
     */
    public LdapService(final String ldapUrl, final String ldapUserDn, final String ldapPassword, final String userSearchBase, final String ouSearchBase) {

        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(ldapUrl);
        ldapContextSource.setUserDn(ldapUserDn);
        ldapContextSource.setPassword(ldapPassword);
        ldapContextSource.afterPropertiesSet();
        this.ldapTemplate = new LdapTemplate(ldapContextSource);
        this.ldapBaseUserAttributesMapper = new LdapBaseUserAttributesMapper();
        this.enhancedLdapOuAttributesMapper = new EnhancedLdapOuAttributesMapper();
        this.enhancedLdapUserAttributesMapper = new EnhancedLdapUserAttributesMapper(this.ldapBaseUserAttributesMapper);
        this.mapper = new DtoMapperImpl();
        this.userSearchBase = userSearchBase;
        this.ouSearchBase = ouSearchBase;
    }

    public Optional<Map<String, LdapOuNode>> buildSubtree(String distinguishedName, String modifyTimeStamp) {

        var distinguishedNameIsValid = ldapQuery(distinguishedName);
        if (distinguishedNameIsValid == null || distinguishedNameIsValid.size() == 0)
            return Optional.empty();

        if (distinguishedName.endsWith(ouSearchBase) && distinguishedName.trim().length() > ouSearchBase.trim().length()) {
            var rootNode = buildParentCollectorTree(distinguishedName, null);
            var collectorTreeNodes = rootNode.flatListLdapOuNode();
            var lastCollectorTreeNode = collectorTreeNodes.get(collectorTreeNodes.size() - 1);
            lastCollectorTreeNode.setChildNodes(buildSubtreeWithUsers(distinguishedName, modifyTimeStamp));
            return Optional.of(Map.of(distinguishedName, rootNode));
        }
        else {
            return Optional.of(buildSubtreeWithUsers(distinguishedName, modifyTimeStamp));
        }
    }

    private LdapOuNode buildParentCollectorTree(String distinguishedName, LdapOuNode rootNode) {

        var searchResults = ldapQuery(ouSearchBase);
        if (searchResults != null && searchResults.size() == 1) {

            rootNode = new LdapOuNode();
            rootNode.setNode(searchResults.get(0));
            rootNode.setDistinguishedName(ouSearchBase);

            LdapOuNode lastNode = rootNode;

            var zammadRootIdentifier = distinguishedName.replace("," + ouSearchBase, "");
            var zammadRootIdentifiers = new ArrayList<>(Arrays.asList(zammadRootIdentifier.split(",")));
            zammadRootIdentifiers.remove(0);
            Collections.reverse(zammadRootIdentifiers);
            String parentCollector = "";

            for (String identifier : zammadRootIdentifiers) {
                parentCollector = parentCollector.isEmpty() ? identifier : identifier + "," + parentCollector;
                distinguishedName = parentCollector + "," + ouSearchBase;
                searchResults = ldapQuery(distinguishedName);
                if (searchResults != null && searchResults.size() == 1) {
                    var node = new LdapOuNode();
                    node.setNode(searchResults.get(0));
                    node.setDistinguishedName(distinguishedName);
                    lastNode.setChildNodes(Map.of(identifier, node));
                    lastNode = node;
                } else {
                    log.error("Ambiguous DN entries found : " + distinguishedName);
                }
            }
        } else {
            log.error("Collector rootNode not found : " + distinguishedName);
        }
        return rootNode;
    }

    private List<EnhancedLdapOuSearchResultDTO> ldapQuery(String distinguishedName) {
        try {
            final LdapQuery ouObjectReferenceQuery = query().searchScope(SearchScope.OBJECT).base(distinguishedName).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(ATTRIBUTE_OBJECT_CLASS)
                    .is(LHM_ORGANIZATIONAL_UNIT);
            final List<EnhancedLdapOuSearchResultDTO> searchResults = this.ldapTemplate.search(ouObjectReferenceQuery, this.enhancedLdapOuAttributesMapper);
            if (searchResults.size() == 1) {
                return searchResults;
            } else {
                log.error("Ambiguous DN entries found : " + distinguishedName);
            }
            return null;
        } catch (Exception ex) {
            log.error(String.format("LDAP search error with dn=%s", distinguishedName), ex);
            return null;
        }
    }

    /**
     * Set distinguished name entry point as root. Calculate ou ldap subtree child
     * entries (LdapOuNodes). Add ou assigned users. Returns a nested map
     * <distinguishedName, LdapOuNode>.
     *
     * @param distinguishedName Start tree ldap root entry
     * @param modifyTimeStamp   Optional ldap search attribute
     * @return OU Tree
     */
    private Map<String, LdapOuNode> buildSubtreeWithUsers(String distinguishedName, String modifyTimeStamp) {

        var subtree = new TreeMap<String, LdapOuNode>();
        try {
            log.trace("Searching for dn='{} & objectClass='{}' ...", distinguishedName, LHM_ORGANIZATIONAL_UNIT);
            final LdapQuery ouObjectReferenceQuery = query().searchScope(SearchScope.OBJECT).base(distinguishedName).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(ATTRIBUTE_OBJECT_CLASS)
                    .is(LHM_ORGANIZATIONAL_UNIT);

            final List<EnhancedLdapOuSearchResultDTO> searchResults = this.ldapTemplate.search(ouObjectReferenceQuery, this.enhancedLdapOuAttributesMapper);

            if (searchResults.size() == 1) {
                var object = searchResults.get(0);
                var rootNode = new LdapOuNode();
                rootNode.setNode(object);
                rootNode.setDistinguishedName(distinguishedName);
                addUsers(this.userSearchBase, rootNode, modifyTimeStamp);
                subtree.put(distinguishedName, rootNode);

                searchResults.forEach(o -> addSubtree(distinguishedName, rootNode, modifyTimeStamp));

            } else {
                log.error("Ambiguous DN entries found : " + distinguishedName);
            }

        } catch (final NameNotFoundException ex) {
            log.error("No LDAP entry found with DN = '{}'. Query failed with exception '{}'", distinguishedName, ex.getClass().getName());
        }

        return subtree;
    }

    /**
     * Calculate ldap child entries for each parent and concatenate LdapOuNodes.
     * Build ou tree, filter with modifyTimeStamp user only. Add users to child
     * entries.
     *
     * @param distinguishedName Ldap search base
     * @param modifyTimeStamp   Optional ldap search attribute
     */
    private void addSubtree(String distinguishedName, LdapOuNode parent, String modifyTimeStamp) {

        try {

            log.trace("Searching for dn='{}' & objectClass='{}' ...", distinguishedName, LHM_ORGANIZATIONAL_UNIT);
            final LdapQuery ouObjectReferenceQuery = getOuObjectReferenceQuery(distinguishedName, null);

            final List<EnhancedLdapOuSearchResultDTO> searchResults = this.ldapTemplate.search(ouObjectReferenceQuery, this.enhancedLdapOuAttributesMapper);

            searchResults.forEach(o -> {
                var dn = String.format("ou=%s,%s", o.getOu().replace(",", "\\,"), distinguishedName);
                var node = new LdapOuNode();
                node.setNode(o);
                node.setDistinguishedName(dn);
                parent.getChildNodes().put(o.getOu(), node);
                addUsers(this.userSearchBase, node, modifyTimeStamp);
                addSubtree(dn, node, modifyTimeStamp);
            });

        } catch (final NameNotFoundException ex) {
            log.warn("No LDAP Entry found with DN = '{}' Query failed with exception '{}", distinguishedName, ex.getClass().getName());
        }
    }

    /**
     * @param distinguishedName Ldap search base
     * @param modifyTimeStamp   Optional ldap search attribute
     */
    private static ContainerCriteria getOuObjectReferenceQuery(String distinguishedName, String modifyTimeStamp) {

        if (modifyTimeStamp != null)
            return query().searchScope(SearchScope.ONELEVEL).base(distinguishedName).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT)
                    .and(ATTRIBUTE_MODIFY_TIMESTAMP).gte(modifyTimeStamp);
        else
            return query().searchScope(SearchScope.ONELEVEL).base(distinguishedName).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(ATTRIBUTE_OBJECT_CLASS).is(LHM_ORGANIZATIONAL_UNIT);
    }

    /**
     * @param searchBase      Ldap search base
     * @param node            Shade tree node
     * @param modifyTimeStamp Optional ldap search attribute
     */
    private void addUsers(String searchBase, LdapOuNode node, String modifyTimeStamp) {

        final LdapQuery ouObjectReferenceQuery = getOuObjectReferenceQuery(searchBase, node, modifyTimeStamp);

        final List<EnhancedLdapUserDto> searchResults = this.ldapTemplate.search(ouObjectReferenceQuery, this.enhancedLdapUserAttributesMapper);
        node.setUsers(searchResults);
    }

    /**
     * @param searchBase      Ldap search base
     * @param node            Shade tree node
     * @param modifyTimeStamp Optional ldap search attribute
     */
    private static ContainerCriteria getOuObjectReferenceQuery(String searchBase, LdapOuNode node, String modifyTimeStamp) {

        if (modifyTimeStamp != null)
            return query().searchScope(SearchScope.ONELEVEL).base(searchBase).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(LHM_OBJECT_PATH).is(node.getDistinguishedName())
                    .and(ATTRIBUTE_MODIFY_TIMESTAMP).gte(modifyTimeStamp);
        else
            return query().searchScope(SearchScope.ONELEVEL).base(searchBase).attributes(ATTRIBUTE_MODIFY_TIMESTAMP, "*").where(LHM_OBJECT_PATH).is(node.getDistinguishedName());
    }

}
