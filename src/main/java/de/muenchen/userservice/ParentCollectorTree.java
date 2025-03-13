package de.muenchen.userservice;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuAttributesMapper;
import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParentCollectorTree extends AbstractLdap {

    private static final String ATTRIBUTE_OBJECT_CLASS = "objectClass";

    protected static final String ATTRIBUTE_MODIFY_TIMESTAMP = "modifyTimestamp";
    protected static final String LHM_ORGANIZATIONAL_UNIT = "lhmOrganizationalUnit";

    protected EnhancedLdapOuAttributesMapper enhancedLdapOuAttributesMapper;

    protected LdapOuNode buildParentCollectorTree(String organizationalUnit, String distinguishedName, LdapOuNode rootNode) {

        var searchResults = ldapQuery(ouSearchBase);
        if (searchResults != null && searchResults.size() == 1) {

            rootNode = new LdapOuNode();
            rootNode.setNode(searchResults.get(0));
            rootNode.setDistinguishedName(ouSearchBase);
            rootNode.setOrganizationalUnit(organizationalUnit);

            LdapOuNode lastNode = rootNode;

            var zammadRootIdentifier = distinguishedName.replace("," + ouSearchBase, "");
            var zammadRootIdentifiers = new ArrayList<>(Arrays.asList(zammadRootIdentifier.split(",ou=")));
            zammadRootIdentifiers.replaceAll(ou -> ou.startsWith("ou=") ? ou : "ou=" + ou);
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
                    node.setOrganizationalUnit(organizationalUnit);
                    lastNode.setChildNodes(Optional.of(Map.of(identifier, node)));
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


}
