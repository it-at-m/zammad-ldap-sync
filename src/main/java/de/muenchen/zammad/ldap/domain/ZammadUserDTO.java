package de.muenchen.zammad.ldap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class ZammadUserDTO {

    private String id;
    @EqualsAndHashCode.Include
    private String firstname;
    @EqualsAndHashCode.Include
    private String lastname;
    private String login;
    private boolean ldapsyncupdate;
    @EqualsAndHashCode.Include
    private String email;
    @EqualsAndHashCode.Include
    private String department;
    @EqualsAndHashCode.Include
    private String lhmobjectid;
    @EqualsAndHashCode.Include
    @JsonProperty("role_ids")
    private List<Integer> roleIds;
    @JsonProperty("group_ids")
    @EqualsAndHashCode.Include
    private Map<String, List<String>> groupIds;
    @JsonProperty("updated_at")
    private String updatedAt;
    private boolean active;
    private String ldapsyncstate;

}
