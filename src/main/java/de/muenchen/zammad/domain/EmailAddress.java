package de.muenchen.zammad.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmailAddress {

    private Integer id;
    @JsonProperty("channel_id")
    private Integer channelId;
    private String name;
    private String email;
    private boolean active;
    private String note;
    private String preferences;
    @JsonProperty("updated_by_id")
    private Integer updatedById;
    @JsonProperty("created_by_id")
    private Integer createdById;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("group_ids")
    private final List<Integer> groupIds;

    public EmailAddress(Integer id, Integer channelId, String name) {

        super();
        this.id = id;
        this.channelId = channelId;
        this.name = name;

        this.email = "";
        this.note = "";
        this.preferences = "";
        this.updatedById = null;
        this.createdById = null;
        this.createdAt = "";
        this.updatedAt = "";
        this.groupIds = null;
    }



}
