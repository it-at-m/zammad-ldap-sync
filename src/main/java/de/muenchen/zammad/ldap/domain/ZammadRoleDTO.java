package de.muenchen.zammad.ldap.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode
public class ZammadRoleDTO {
    private String id;
    private String name;
    @JsonProperty("group_ids")
    private Map<String, List<String>> groupIds;
}
