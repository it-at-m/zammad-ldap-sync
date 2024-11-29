package de.muenchen.zammad.ldap.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "sync.standard")
public class StandardProperties {

    private String mailStartsWith;
    private String signatureStartsWith;

}
