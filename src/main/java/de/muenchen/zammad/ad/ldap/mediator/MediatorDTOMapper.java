package de.muenchen.zammad.ad.ldap.mediator;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;

@Service
@Mapper
public interface MediatorDTOMapper {

    MediatorDTOMapper INSTANCE = Mappers.getMapper(MediatorDTOMapper.class);

    List<EnhancedActiveDirectoryGroupMediatorDTO> mediatorDTO(List<EnhancedActiveDirectoryGroupDTO> adGroups);

    @Mapping(target = "modifyTimeStamp", ignore = true)
    @Mapping(target = "lhmOULongname", source="name")
    @Mapping(target = "lhmOUShortname", source="name", qualifiedByName = "shortname" )
    @Mapping(target = "ou", source="name", qualifiedByName = "shortname" )
    EnhancedLdapOuSearchResultDTO ldapOuDTO(EnhancedActiveDirectoryGroupDTO adGroup);

    @Named(value = "shortname")
    public static String extractShortname(String longname) {
        return longname.replace("lhm-ab-dbsticketing-", "");
    }
}
