package de.muenchen.zammad.ldap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ZammadGroupDTO {

    private String id;
    @JsonProperty("parent_id")
    private String parentId;
    private String name;
    @JsonProperty("ldapsyncupdate_group")
    private boolean ldapsyncupdate;
    private boolean active;
    @JsonProperty("lhmobjectid_group")
    private String lhmobjectid;
    @JsonProperty("updated_at")
    private String updatedAt;


}
