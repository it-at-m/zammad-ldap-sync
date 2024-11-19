package de.muenchen.zammad.ldap.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Assets {

    @JsonProperty("EmailAddress")
    private Map<String, EmailAddress> emailAddress;

    public Integer findID(String name) {

        if (getEmailAddress() == null || getEmailAddress().isEmpty())
            return null;

         var emailAddress = getEmailAddress().values().stream().filter(adress -> adress.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
         return emailAddress == null ? null : emailAddress.getId();
    }

}
