package de.muenchen.zammad.ad.ldap.mediator;

import java.io.Serial;

import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper=false)
public class EnhancedActiveDirectoryGroupMediatorDTO extends EnhancedActiveDirectoryGroupDTO {

    @Serial
    private static final long serialVersionUID = 1L;
    private String parentLdapDistinguishedName;

    public String getLdapDistinguishedName() {
        return getParentLdapDistinguishedName() != null ? "ou=".concat(getName()).concat(",").concat(getParentLdapDistinguishedName()) : "ou=".concat(getName()) ;
    }

}
