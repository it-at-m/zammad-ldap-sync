package de.muenchen.zammad.ad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import de.muenchen.zammad.ldap.tree.ActiveDirectoryService;
import lombok.AllArgsConstructor;


@Service
@Configuration
@AllArgsConstructor
public class ActiveDirectoryGroupService {

    private ActiveDirectoryService adService;

    public Optional<List<EnhancedActiveDirectoryGroupDTO>> crossOrganizationalGroups() {

         Optional<List<EnhancedActiveDirectoryGroupDTO>>  optionalGroups = adService.groupsWithoutLdapEquivalent();
         if (optionalGroups.isPresent() ) {
             List<EnhancedActiveDirectoryGroupDTO> groupDTOs = optionalGroups.get();
             for (EnhancedActiveDirectoryGroupDTO groupDTO : groupDTOs) {
                 Map<String, ActiveDirectoryUserDTO> users = new HashMap<>();
                 for (String distinguishedName : groupDTO.getMember()) {
                      var user = this.adService.lookupUser(distinguishedName);
                      users.put(user.getLhmObjectId(), user);
                 }
                 groupDTO.setAdUserByLhmObjectId(users);
            }
         }
         return optionalGroups;
    }



}
