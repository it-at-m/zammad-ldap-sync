package de.muenchen.zammad.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChannelsEmail {

    @JsonProperty("assets")
    private final Assets assets;

    public Integer findEmailsAddressId(String name) {

        if (name == null) {
            return null;
        }

        if (getAssets() == null || getAssets().getEmailAddress() == null)
            return null;

        var address = getAssets().getEmailAddress().values().stream()
                .filter(adress -> adress.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst()
                .orElse(null);
        if (isChannelActive(address))
            return address.getId();
        else
            return null;

    }


    private boolean isChannelActive(EmailAddress address) {
        if (address != null && getAssets().getChannel() != null) {
            final Integer emailAddressChannelId = address.getChannelId();

            var channel = getAssets().getChannel().values().stream().filter(chnl -> chnl.getId().equals(emailAddressChannelId))
                    .findFirst().orElse(null);

            if (channel != null)
                return channel.getActive();

        }
        return false;
    }

}
