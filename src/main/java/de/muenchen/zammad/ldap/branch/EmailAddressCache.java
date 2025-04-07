package de.muenchen.zammad.ldap.branch;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.property.OrganizationalUnitsCommonProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class EmailAddressCache {

    private ZammadService zammadService;
    private OrganizationalUnitsCommonProperties commonProperties;

    private final HashMap<String, Integer> cache = new HashMap<>();

    public Integer findEmailAdressId(String emailAddressName) {

        if (emailAddressName == null) {
            log.warn("Find emailAdressId started with invalid identifier 'null'.");
            return null;
        }

        if (cache.containsKey(emailAddressName.toUpperCase())) {
            final var emailAddressID = cache.get(emailAddressName.toUpperCase());
            log.debug("Fetch emaildAddressId from cache : {}={}", emailAddressName, emailAddressID);
            return emailAddressID;
        } else {
            final var zammadServiceResponse = zammadService.getZammadChannelsEmail();
            if (zammadServiceResponse == null)
                cache.put(emailAddressName.toUpperCase(), null);
            else
                cache.put(emailAddressName.toUpperCase(), zammadServiceResponse
                        .findEmailsAddressId(emailAddressName, commonProperties.getMailStartsWith()));

            log.debug("EmaildAddressId account found in Zammad '{}={}' and added to cache.", emailAddressName,
                    cache.get(emailAddressName));
            return cache.get(emailAddressName);
        }
    }

}
