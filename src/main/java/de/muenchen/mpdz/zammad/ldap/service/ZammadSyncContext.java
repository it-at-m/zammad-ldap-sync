package de.muenchen.mpdz.zammad.ldap.service;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.muenchen.mpdz.zammad.ldap.tree.LdapService;

@Configuration
public class ZammadSyncContext {

    @Getter
    @Value("${ldap.url}")
    private String LDAP_URL;

    @Getter
    @Value("${ldap.userSearchBase}")
    private String USER_SEARCH_BASE;

    @Getter
    @Value("${ldap.ouSearchBase}")
    private String OU_SEARCH_BASE;

    @Bean
    public LdapService ldapService(){
        return new LdapService(LDAP_URL, "", "", USER_SEARCH_BASE, OU_SEARCH_BASE);
    }

}
