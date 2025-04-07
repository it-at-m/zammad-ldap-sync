package de.muenchen.userservice;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.List;
import java.util.Optional;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;

import de.muenchen.zammad.ad.ActiveDirectoryGroupAttributesMapper;
import de.muenchen.zammad.ad.ActiveDirectoryUserAttributesMapper;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.property.ActiveDirectoryProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ActiveDirectoryService extends AbstractLdap {

    private static final String[] ATTRIBUTE_LIST = new String[] {"*"};
    private final ActiveDirectoryProperty adProperty;

    private final ActiveDirectoryGroupAttributesMapper activeDirectoryGroupAttributesMapper;
    private final ActiveDirectoryUserAttributesMapper activeDirectoryUserAttributesMapper;

    public ActiveDirectoryService(ActiveDirectoryProperty property) {

        this.adProperty = property;

        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(this.adProperty.getUrl());
        ldapContextSource.setUserDn(this.adProperty.getUserDn());
        ldapContextSource.setPassword(this.adProperty.getPassword());
        ldapContextSource.afterPropertiesSet();

        this.ldapTemplate = new LdapTemplate(ldapContextSource);

        this.activeDirectoryGroupAttributesMapper = new ActiveDirectoryGroupAttributesMapper();
        this.activeDirectoryUserAttributesMapper = new ActiveDirectoryUserAttributesMapper();

  }

    public Optional<List<EnhancedActiveDirectoryGroupDTO>> groupsWithoutLdapEquivalent() {

        if (directoryServiceEntryNotExists(adProperty.getDistinguishedName()))
            return Optional.empty();

        var attribute = LdapAttribute.fromIdentifier(LdapAttribute.GROUP.getIdentifier());
        final LdapQuery ouObjectReferenceQuery = query().searchScope(SearchScope.ONELEVEL).base(adProperty.getDistinguishedName()).attributes(ATTRIBUTE_LIST).where(attribute.getDescription())
                .is(attribute.getIdentifier());

        final List<EnhancedActiveDirectoryGroupDTO> activeDirectoryGroups = this.ldapTemplate.search(ouObjectReferenceQuery, activeDirectoryGroupAttributesMapper);

        return Optional.of(activeDirectoryGroups);

    }

    public ActiveDirectoryUserDTO lookupUser(String distinguishedName ) {
        return this.ldapTemplate.lookup(distinguishedName, this.activeDirectoryUserAttributesMapper);
    }

}
