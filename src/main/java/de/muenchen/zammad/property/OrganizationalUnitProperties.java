package de.muenchen.zammad.property;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrganizationalUnitProperties {

		private List<String> distinguishedNames;
		private String userSearchBase;
		private String ouSearchBase;

}
