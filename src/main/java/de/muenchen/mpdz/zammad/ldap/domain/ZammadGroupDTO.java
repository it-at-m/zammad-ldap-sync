package de.muenchen.mpdz.zammad.ldap.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ZammadGroupDTO {

    private String id;
    private String parent_id;
    private String name;
    private boolean donotupdate;
    private boolean active;
    private String lhmobjectid;
    private String updated_at;

}
