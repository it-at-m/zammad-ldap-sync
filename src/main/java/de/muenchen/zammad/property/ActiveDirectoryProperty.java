package de.muenchen.zammad.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "active-directory")
public class ActiveDirectoryProperty {

    private String url;
    private String userDn;
    private String password;
    private String distinguishedName;
    private String groupNamePrefix;

}
