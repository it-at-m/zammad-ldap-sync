package de.muenchen.zammad.ldap.branch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.property.ZammadProperties;

public class AbstractTree {

    protected ZammadService zammadService;
    protected ZammadProperties zammadProperties;

    protected static final String LOG_ID = " - ID : {}";
    protected static final String LOG_DIVIDER = "------------------------";

    protected Map<String, List<Group>> getCurrentZammadGroups() {
        return generatelhmObjectIdZammadGroupMap(zammadService.getZammadGroups());
    }

    private Map<String, List<Group>> generatelhmObjectIdZammadGroupMap(List<Group> zammadGroups) {
        return zammadGroups.stream().filter(g -> g.getLhmobjectid() != null && !g.getLhmobjectid().isBlank())
                .collect(Collectors.groupingBy(Group::getLhmobjectid));
    }

}
