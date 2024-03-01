package de.muenchen.mpdz.zammad.ldapAnbindung.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode
public class ZammadUserDTO {

    private String id;
    private String firstname;
    private String lastname;
    private boolean donotupdate;
    private String email;
    private String department;
    private String lhmobjectid;
    private List<String> role_ids;
    private Map<String, List<String>> group_ids;
    private String updated_at;
    private boolean active;
}
