package de.muenchen.mpdz.zammad.ldap.ezldapextension;

import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class EnhancedLdapOuSearchResultDTO extends LdapOuSearchResultDTO {

    private String modifyTimeStamp;

}
