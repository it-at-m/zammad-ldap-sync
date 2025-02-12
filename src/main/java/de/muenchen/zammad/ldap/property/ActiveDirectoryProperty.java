package de.muenchen.zammad.ldap.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "active-directory")
public class ActiveDirectoryProperty {

    private String url;
    private String userDn;
    private String password;
    private String distinguishedName;

}
