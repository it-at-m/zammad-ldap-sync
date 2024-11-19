package de.muenchen.zammad.ldap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChannelsEmail {

    @JsonProperty("assets")
    private Assets assets;

    public Integer findEmailsAdressId(String name) {

        if (name == null) {
            return null;
        }

        if (getAssets() == null)
            return null;

         return getAssets().findID(name);
    }

}
