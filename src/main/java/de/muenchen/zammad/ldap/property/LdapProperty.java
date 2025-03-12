package de.muenchen.zammad.ldap.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ldap")
public class LdapProperty {

    private String url;

}
