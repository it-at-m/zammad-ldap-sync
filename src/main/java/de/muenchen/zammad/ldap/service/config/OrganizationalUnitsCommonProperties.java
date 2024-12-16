package de.muenchen.zammad.ldap.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "sync.organizational-units-common")
public class OrganizationalUnitsCommonProperties {

    private String mailStartsWith;
    private String signatureStartsWith;

}
