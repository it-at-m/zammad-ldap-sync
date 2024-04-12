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
    private String ldapUrl;

    @Getter
    @Value("${ldap.user-search-base}")
    private String userSearchBase;

    @Getter
    @Value("${ldap.ou-search-base}")
    private String ouSearchBase;

    @Bean
    public LdapService ldapService(){
        return new LdapService(ldapUrl, "", "", userSearchBase, ouSearchBase);
    }

}
