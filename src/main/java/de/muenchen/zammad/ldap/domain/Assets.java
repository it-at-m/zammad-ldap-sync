package de.muenchen.zammad.ldap.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Assets {

    @JsonProperty("EmailAddress")
    private Map<String, EmailAddress> emailAddress;

    public Integer findID(String name, String defaultName) {

        if (getEmailAddress() == null || getEmailAddress().isEmpty())
            return null;

         var emailAddress = getEmailAddress().values().stream().filter(adress -> adress.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst().orElse(null);

         if (emailAddress == null && defaultName != null)
             emailAddress = getEmailAddress().values().stream().filter(adress -> adress.getName().toLowerCase().startsWith(defaultName.toLowerCase())).findFirst().orElse(null);

         return emailAddress == null ? null : emailAddress.getId();
    }

}
