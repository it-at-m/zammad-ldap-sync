package de.muenchen.oss.ezldap.core;

import de.muenchen.oss.ezldap.core.LdapUserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = false, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
public class EnhancedLdapUserDto extends LdapUserDTO {

    private String modifyTimeStamp;

    @ToString.Include(rank = 4)
    private String lhmObjectReference;

}
