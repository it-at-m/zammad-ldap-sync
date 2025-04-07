package de.muenchen.zammad.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Signatures {

    private Integer id;
    private String name;
    private String body;
    private boolean active;
    @JsonProperty("updated_by_id")
    private Integer updatedById;
    @JsonProperty("created_by_id")
    private Integer createdById;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("group_ids")
    private List<Integer> groupIds;

    public Signatures(Integer id, String name) {
        super();
        this.id = id;
        this.name = name;

        this.body = "";
        this.active = false;
        this.updatedById = null;
        this.createdById = null;
        this.createdAt = "";
        this.updatedAt = "";
        this.groupIds = null;
    }
}
