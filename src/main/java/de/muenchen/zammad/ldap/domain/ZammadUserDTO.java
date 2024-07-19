package de.muenchen.zammad.ldap.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ZammadUserDTO implements Comparable<ZammadUserDTO>{

    private String id;
    private String firstname;
    private String lastname;
    private String login;
    private boolean ldapsyncupdate;
    private String email;
    private String department;
    private String lhmobjectid;
    @JsonProperty("role_ids")
    private List<Integer> roleIds;
    @JsonProperty("group_ids")
    private Map<String, List<String>> groupIds;
    @JsonProperty("updated_at")
    private String updatedAt;
    private boolean active;
    private String ldapsyncstate;


	@Override
	public int compareTo(ZammadUserDTO o) {

		if (getFirstname() != null && o.getFirstname() != null && ! getFirstname().equals(o.getFirstname()))
			return 1;
		if (getLastname() != null && o.getLastname() != null && ! getLastname().equals(o.getLastname()) )
			return 1;
		if (getEmail() != null && o.getEmail() != null && ! getEmail().equals(o.getEmail()) )
			return 1;
		if (getDepartment() != null && o.getDepartment() != null && ! getDepartment().equals(o.getDepartment()) )
			return 1;
		if (getLhmobjectid() != null && o.getLhmobjectid() != null && ! getLhmobjectid().equals(o.getLhmobjectid()))
			return 1;
		if (getGroupIds() != null && o.getGroupIds() != null && ! getGroupIds().equals(o.getGroupIds()))
			return 1;

		return 0;
	}








}
