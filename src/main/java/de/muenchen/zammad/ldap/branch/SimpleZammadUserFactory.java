package de.muenchen.zammad.ldap.branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimpleZammadUserFactory {

    ZammadProperties zammadProperties;


    public User mapToZammadUser(ActiveDirectoryUserDTO activeDirectoryUserDTO, Optional<Integer> zammadGroupId) {

        User zammadUser = new User(
                activeDirectoryUserDTO.getGivenName(),
                activeDirectoryUserDTO.getSn(),
                activeDirectoryUserDTO.getLhmObjectId(),
                activeDirectoryUserDTO.getMail(),
                activeDirectoryUserDTO.getOu(),
                activeDirectoryUserDTO.getLhmObjectId());

                zammadUser.setRoleIds(defaultSynchronizationRoles());
                zammadGroupId.ifPresent(id -> zammadUser.setGroupIds(Map.of(id.toString(), List.of("full"))));
                return zammadUser;
    }


    public User mapToZammadUser(LdapUserDTO ldapBaseUserDTO, Optional<Integer> zammadGroupId) {

        User zammadUser = new User(
                                    ldapBaseUserDTO.getVorname(),
                                    ldapBaseUserDTO.getNachname(),
                                    ldapBaseUserDTO.getLhmObjectId(),
                                    ldapBaseUserDTO.getMail(),
                                    ldapBaseUserDTO.getOu(),
                                    ldapBaseUserDTO.getLhmObjectId());

        zammadUser.setRoleIds(defaultSynchronizationRoles());
        zammadGroupId.ifPresent(id -> zammadUser.setGroupIds(Map.of(id.toString(), List.of("full"))));
          return zammadUser;
    }

    private List<Integer> defaultSynchronizationRoles() {
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(zammadProperties.getAssignment().getRole().getIdAgent());
        roleIds.add(zammadProperties.getAssignment().getRole().getIdErstellen());
        return roleIds;
    }

    public static void activate(User zammadUser) {
        zammadUser.setActive(true);
        zammadUser.setLdapsyncupdate(true);
    }


}
