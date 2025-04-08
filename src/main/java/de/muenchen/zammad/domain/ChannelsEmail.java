package de.muenchen.zammad.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChannelsEmail {

    @JsonProperty("assets")
    private final Assets assets;

    public Integer findEmailsAddressId(String name, String defaultName) {

        if (name == null) {
            return null;
        }

        if (getAssets() == null)
            return null;

         return getAssets().findID(name, defaultName);
    }

}
