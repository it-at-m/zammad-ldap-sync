package de.muenchen.zammad.ad;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Map;

import de.muenchen.oss.ezldap.core.LdapOuSearchResultDTO;
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
public class EnhancedActiveDirectoryGroupDTO extends LdapOuSearchResultDTO {

    @Serial
    private static final long serialVersionUID = 1L;
    private String cn;
    private String displayName;
    private String distinguishedName;
    private String name;
    private ArrayList<String> member;
    private Map<String, ActiveDirectoryUserDTO> userByLhmObjectId;

}
