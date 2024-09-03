package de.muenchen.zammad.ldap.service.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@ConfigurationProperties(prefix = "sync")
@Getter
@Setter
public class LdapSearch {

    //	Integer dateTimeMinusDay;
    private Map<String, OrganizationalUnit> organizationalUnits;

    public List<String> listDistinguishedNames() {

        var dns = new ArrayList<String>();
        if (getOrganizationalUnits() != null) {
            getOrganizationalUnits().forEach((k, v) -> {
                if (!v.getOuSearchBase().trim().isEmpty())
                    dns.addAll(v.getDistinguishedNames());
            });
        }

        return dns;
    }

}
