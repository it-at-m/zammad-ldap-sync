package de.muenchen.zammad.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Assets {

    @JsonProperty("EmailAddress")
    private final Map<String, EmailAddress> emailAddress;

    @JsonProperty("Channel")
    private final Map<Integer, Channel> channel;

    public Integer findID(String name, String defaultName) {

        if (getEmailAddress() == null || getEmailAddress().isEmpty())
            return null;

        var address = getEmailAddress().values().stream()
                .filter(adress -> adress.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst()
                .orElse(null);

        // organizational-units email
        if (isChannelActive(address))
            return address.getId();

        // default email
        if (defaultName != null) {
            address = getEmailAddress().values().stream()
                    .filter(adress -> adress.getName().toLowerCase().startsWith(defaultName.toLowerCase())).findFirst()
                    .orElse(null);
            if (isChannelActive(address))
                return address.getId();

        }
        return null;
    }

    private boolean isChannelActive(EmailAddress address) {
        if (address != null && getChannel() != null) {
            final Integer emailAddressChannelId = address.getChannelId();

            var channel = getChannel().values().stream().filter(chnl -> chnl.getId().equals(emailAddressChannelId))
                    .findFirst().orElse(null);

            if (channel != null)
                return channel.getActive();

        }
        return false;
    }

}
