package de.muenchen.zammad.domain;

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
public class User {

    private Integer id;
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

    public User(String firstname, String lastname, String login, String email,
            String department, String lhmobjectid) {
        super();
        this.firstname = firstname;
        this.lastname = lastname;
        this.login = login;
        this.email = email;
        this.department = department;
        this.lhmobjectid = lhmobjectid;
    }

    public User(Integer id, String firstname, String lastname, String login, String department, String lhmobjectid, List<Integer> roleIds) {
        super();
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.login = login;
        this.department = department;
        this.lhmobjectid = lhmobjectid;
        this.roleIds = roleIds;
        this.active = true;
        this.ldapsyncupdate= true;
    }

}
