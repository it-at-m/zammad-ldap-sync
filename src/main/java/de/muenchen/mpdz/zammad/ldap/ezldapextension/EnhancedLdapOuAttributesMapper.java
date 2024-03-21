package de.muenchen.mpdz.zammad.ldap.ezldapextension;

import static de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper.safelyGet;

import de.muenchen.oss.ezldap.core.LdapOuAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.AttributesMapper;

public class EnhancedLdapOuAttributesMapper implements AttributesMapper<EnhancedLdapOuSearchResultDTO> {

    private LdapOuAttributesMapper ldapOuAttributesMapper;

    public EnhancedLdapOuAttributesMapper() {
        this.ldapOuAttributesMapper = new LdapOuAttributesMapper();
    }

    @Override
    public EnhancedLdapOuSearchResultDTO mapFromAttributes(Attributes attributes) throws NamingException {
        LdapOuSearchResultDTO ldapOuSearchResultDTO = ldapOuAttributesMapper.mapFromAttributes(attributes);

        EnhancedLdapOuSearchResultDTO ouDto = new EnhancedLdapOuSearchResultDTO(safelyGet("modifyTimestamp", attributes));

        //copy super class data
        ouDto.setLhmObjectId(ldapOuSearchResultDTO.getLhmObjectId());
        ouDto.setOu(ldapOuSearchResultDTO.getOu());

        ouDto.setLhmOUKey(ldapOuSearchResultDTO.getLhmOUKey());
        ouDto.setLhmOULongname(ldapOuSearchResultDTO.getLhmOULongname());
        ouDto.setLhmOUShortname(ldapOuSearchResultDTO.getLhmOUShortname());

        ouDto.setPostalCode(ldapOuSearchResultDTO.getPostalCode());
        ouDto.setStreet(ldapOuSearchResultDTO.getStreet());

        ouDto.setMail(ldapOuSearchResultDTO.getMail());
        ouDto.setTelephoneNumber(ldapOuSearchResultDTO.getTelephoneNumber());
        ouDto.setFacsimileTelephoneNumber(ldapOuSearchResultDTO.getFacsimileTelephoneNumber());

        ouDto.setLhmOUManager(ldapOuSearchResultDTO.getLhmOUManager());
        ouDto.setLhmOU2ndManager(ldapOuSearchResultDTO.getLhmOU2ndManager());

        return ouDto;
    }

}
