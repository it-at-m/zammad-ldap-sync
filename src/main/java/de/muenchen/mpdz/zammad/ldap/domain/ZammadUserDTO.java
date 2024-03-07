package de.muenchen.mpdz.zammad.ldap.domain;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ZammadUserDTO implements Comparable<ZammadUserDTO>{

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
    private String deleteldapsync;


	@Override
	public int compareTo(ZammadUserDTO o) {

		if (getFirstname().compareTo(o.getFirstname()) != 0)
			return getFirstname().compareTo(o.getFirstname());
		if (getLastname().compareTo(o.getLastname()) != 0)
			return getLastname().compareTo(o.getLastname());
		if (getEmail().compareTo(o.getEmail()) != 0)
			return getEmail().compareTo(o.getEmail());
		if (getDepartment().compareTo(o.getDepartment()) != 0)
			return getDepartment().compareTo(o.getDepartment());
		if (getLhmobjectid().compareTo(o.getLhmobjectid()) != 0)
			return getLhmobjectid().compareTo(o.getLhmobjectid());

		return 0;
	}








}
