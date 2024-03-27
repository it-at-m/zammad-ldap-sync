package de.muenchen.oss.ezldap.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 *  @ToString(callSuper = true, onlyExplicitlyIncluded = true) : log all ldap user data
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
//@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class EnhancedLdapUserDto extends LdapUserDTO {

    private static final long serialVersionUID = 1L;

	private String modifyTimeStamp;

    @ToString.Include(rank = 4)
    private String lhmObjectReference;

    @Override public String toString() {
        return this.getClass().getSimpleName() + "(lhmObjectId=" + super.getLhmObjectId() + ")";
      }

}
