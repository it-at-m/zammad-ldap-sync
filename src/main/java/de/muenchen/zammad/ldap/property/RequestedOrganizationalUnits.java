package de.muenchen.zammad.ldap.property;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "sync")
public class RequestedOrganizationalUnits {

    private Map<String, OrganizationalUnitProperties> organizationalUnits;

    public List<String> flatMapDistinguishedNames() {
        return organizationalUnits.values().stream().filter(i -> ! i.getOuSearchBase().trim().isEmpty()).map(OrganizationalUnitProperties::getDistinguishedNames).flatMap(List::stream).toList();
    }

}
