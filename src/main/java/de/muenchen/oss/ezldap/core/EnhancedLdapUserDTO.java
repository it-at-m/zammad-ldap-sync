package de.muenchen.oss.ezldap.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnhancedLdapUserDTO extends LdapUserDTO {

    @Serial
    private static final long serialVersionUID = 1L;

	private String modifyTimeStamp;

    @ToString.Include(rank = 4)
    private String lhmObjectReference;

    @Override public String toString() {
        return this.getClass().getSimpleName() + "(lhmObjectId=" + super.getLhmObjectId() + ")";
      }

}
