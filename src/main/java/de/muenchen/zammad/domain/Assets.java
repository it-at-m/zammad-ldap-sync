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

}
