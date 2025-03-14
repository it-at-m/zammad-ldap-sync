package de.muenchen.zammad.ad;

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class ActiveDirectoryGroupAttributesMapper implements AttributesMapper<EnhancedActiveDirectoryGroupDTO> {

    @Override
    public EnhancedActiveDirectoryGroupDTO mapFromAttributes(Attributes attributes) throws NamingException {

        var group = new EnhancedActiveDirectoryGroupDTO();

        group.setCn(safelyGet("cn", attributes));
        group.setDisplayName(safelyGet("displayName", attributes));
        group.setAdDistinguishedName(safelyGet("distinguishedName", attributes));
        group.setName(safelyGet("name", attributes));
        group.setLhmObjectId(getGuidFromByteArray(safelyGet("objectguid", attributes).getBytes()));
        var members = Optional.ofNullable(attributes.get("member"));
        if (members.isPresent())
           group.setMember(Collections.list(members.get().getAll()).stream().map(o -> (String) o).toList());
        else
            group.setMember(List.of());

        return group;
    }

    private static String getGuidFromByteArray(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

}
