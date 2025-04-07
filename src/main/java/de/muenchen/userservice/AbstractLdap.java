package de.muenchen.userservice;

import org.springframework.ldap.core.LdapTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLdap {

    protected String ouSearchBase;
    protected String userSearchBase;
    protected LdapTemplate ldapTemplate;

}
