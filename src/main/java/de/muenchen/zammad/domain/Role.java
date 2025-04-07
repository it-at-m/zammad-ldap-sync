package de.muenchen.zammad.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Role {

    private final String id;
    private final String name;

    @Setter
    @JsonProperty("group_ids")
    private Map<String, List<String>> groupIds;
}
