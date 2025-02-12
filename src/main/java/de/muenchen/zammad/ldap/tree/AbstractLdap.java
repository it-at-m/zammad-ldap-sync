package de.muenchen.zammad.ldap.tree;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Collections;
import java.util.List;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLdap<T> {

    protected String ouSearchBase;
    protected String userSearchBase;
    protected LdapTemplate ldapTemplate;

    protected List<T> ldapQuery(String distinguishedName, String[] receiveAttributes, LdapAttribute attribute, AttributesMapper<T> mapper) {
        try {
            final LdapQuery ouObjectReferenceQuery = query().searchScope(SearchScope.OBJECT).base(distinguishedName).attributes(receiveAttributes).where(attribute.getDescription())
                    .is(attribute.getIdentifier());
            final List<T> searchResults = this.ldapTemplate.search(ouObjectReferenceQuery, mapper);
            if (searchResults.size() == 1) {
                return searchResults;
            } else {
                log.error("Ambiguous DN entries found : " + distinguishedName);
            }
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error(String.format("LDAP search error with dn=%s", distinguishedName), ex);
            return Collections.emptyList();
        }
    }

    protected boolean directoryServiceEntryNotExists(String distinguishedName) {
        try {
            if (ldapTemplate.lookup(distinguishedName) == null) {
                log.warn("Distinguished name lookup is null");
                return true;
            }
        } catch (Exception e) {
            log.warn(String.format("Distinguished name not found '%s'", distinguishedName), e);
            return true;
        }
        return false;
    }

}
