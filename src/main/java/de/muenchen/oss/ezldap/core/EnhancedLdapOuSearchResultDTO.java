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
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class EnhancedLdapOuSearchResultDTO extends LdapOuSearchResultDTO {

    @Serial
    private static final long serialVersionUID = 1L;
	private String modifyTimeStamp;

}
