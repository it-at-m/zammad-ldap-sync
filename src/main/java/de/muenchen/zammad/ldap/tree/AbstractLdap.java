package de.muenchen.zammad.ldap.tree;

import org.springframework.ldap.core.LdapTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLdap {

    protected String ouSearchBase;
    protected String userSearchBase;
    protected LdapTemplate ldapTemplate;

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
