package de.muenchen.mpdz.zammad.ldapAnbindung.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode
public class ZammadRoleDTO {
    private String id;
    private String name;
    private Map<String, List<String>> group_ids;
}
