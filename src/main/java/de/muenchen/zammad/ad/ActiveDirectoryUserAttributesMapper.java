package de.muenchen.zammad.ad;

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class ActiveDirectoryUserAttributesMapper implements AttributesMapper<ActiveDirectoryUserDTO> {

    @Override
    public ActiveDirectoryUserDTO mapFromAttributes(Attributes attributes) throws NamingException {

        var user = new ActiveDirectoryUserDTO();

        user.setCn(safelyGet("cn", attributes));
        user.setDisplayName(safelyGet("displayName", attributes));
        user.setDistinguishedName(safelyGet("distinguishedName", attributes));
        user.setName(safelyGet("name", attributes));
        user.setLhmObjectId(safelyGet("lhmObjectID", attributes));
        user.setUid(safelyGet("uid", attributes));

        return user;
    }

}
