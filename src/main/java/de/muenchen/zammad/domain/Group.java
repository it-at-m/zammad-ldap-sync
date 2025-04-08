package de.muenchen.zammad.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Group {

    @JsonProperty("id")
    private String id;
    @JsonProperty("parent_id")
    private final String parentId;
    @JsonProperty("name")
    private final String name;
    @JsonProperty("ldapsyncupdate_group")
    private boolean ldapsyncupdate;
    @JsonProperty("active")
    private boolean active;
    @JsonProperty("lhmobjectid_group")
    private final String lhmobjectid;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("email_address_id")
    private Integer emailAddressId;
    @JsonProperty("signature_id")
    private Integer signatureId;


}
