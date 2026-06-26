package de.muenchen.zammad.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "sync.organizational-units-common")
public class OrganizationalUnitsCommonProperties {

    /**
     * Name of the Email Channel in Zammad used to send notifications of ticket updates to clients of a group.
     */
    private String emailChannelOutboundName;
    private String signatureStartsWith;

}
