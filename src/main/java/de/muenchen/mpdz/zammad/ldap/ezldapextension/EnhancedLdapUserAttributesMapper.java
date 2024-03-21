package de.muenchen.mpdz.zammad.ldap.ezldapextension;

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import de.muenchen.oss.ezldap.core.AnschriftDTO;
import de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapUserAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;

public class EnhancedLdapUserAttributesMapper implements AttributesMapper<EnhancedLdapUserDto> {

    private LdapUserAttributesMapper ldapUserAttributesMapper;

    public EnhancedLdapUserAttributesMapper(LdapBaseUserAttributesMapper ldapBaseUserAttributesMapper) {
        this.ldapUserAttributesMapper = new LdapUserAttributesMapper(ldapBaseUserAttributesMapper);
    }

    @Override
    public EnhancedLdapUserDto mapFromAttributes(Attributes attributes) throws NamingException {
        LdapUserDTO ldapUserDTO = ldapUserAttributesMapper.mapFromAttributes(attributes);

        EnhancedLdapUserDto userDto = new EnhancedLdapUserDto(
                safelyGet("modifyTimestamp", attributes),
                safelyGet("lhmObjectReference", attributes)
        );

        // BaseUser
        userDto.setLhmObjectId(ldapUserDTO.getLhmObjectId());
        userDto.setUid(ldapUserDTO.getUid());
        userDto.setAnrede(ldapUserDTO.getAnrede());
        userDto.setVorname(ldapUserDTO.getVorname());
        userDto.setNachname(ldapUserDTO.getNachname());
        userDto.setCn(ldapUserDTO.getCn());
        userDto.setOu(ldapUserDTO.getOu());

        // LdapUser
        userDto.setBueroanschrift(new AnschriftDTO());
        userDto.setPostanschrift(new AnschriftDTO());
        userDto.setLhmOULongname(ldapUserDTO.getLhmOULongname());
        userDto.setLhmObjectPath(ldapUserDTO.getLhmObjectPath());
        userDto.setLhmOberOrga(ldapUserDTO.getLhmOberOrga());
        userDto.setLhmReferatName(ldapUserDTO.getLhmReferatName());
        userDto.setLhmFunctionalTitle(ldapUserDTO.getLhmFunctionalTitle());
        userDto.setAmtsbezeichnung(ldapUserDTO.getAmtsbezeichnung());
        userDto.setErreichbarkeit(ldapUserDTO.getErreichbarkeit());
        userDto.setMail(ldapUserDTO.getMail());
        userDto.setLhmOrgaMail(ldapUserDTO.getLhmOrgaMail());
        userDto.setTelephoneNumber(ldapUserDTO.getTelephoneNumber());
        userDto.setFacsimileTelephoneNumber(ldapUserDTO.getFacsimileTelephoneNumber());
        userDto.setMobile(ldapUserDTO.getMobile());
        userDto.setZimmer(ldapUserDTO.getZimmer());
        userDto.setPersonalTitle(ldapUserDTO.getPersonalTitle());
        userDto.setPostanschrift(ldapUserDTO.getPostanschrift());
        userDto.setBueroanschrift(ldapUserDTO.getBueroanschrift());

        return userDto;
    }

}
