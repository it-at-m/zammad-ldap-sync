package de.muenchen.zammad.ad;

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import java.util.ArrayList;
import java.util.Collections;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class ActiveDirectoryGroupAttributesMapper implements AttributesMapper<EnhancedActiveDirectoryGroupDTO> {

    @Override
    public EnhancedActiveDirectoryGroupDTO mapFromAttributes(Attributes attributes) throws NamingException {

        var group = new EnhancedActiveDirectoryGroupDTO();

        group.setCn(safelyGet("cn", attributes));
        group.setDisplayName(safelyGet("displayName", attributes));
        group.setDistinguishedName(safelyGet("distinguishedName", attributes));
        group.setName(safelyGet("name", attributes));
        var member = attributes.get("member");
        group.setMember((ArrayList<String>) Collections.list(member.getAll()));

        return group;
    }

}
