package de.muenchen.zammad.ldap.sync;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.ldap.property.ZammadProperties;

public class AbstractTree {

    protected Map<String, List<Group>> zammadGroupsByLhmObjectId;
    protected Map<String, List<User>> zammadUsersByLhmObjectId;

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
