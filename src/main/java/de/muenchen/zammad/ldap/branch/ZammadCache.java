package de.muenchen.zammad.ldap.branch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ZammadCache {

    private Map<String, List<Group>> zammadGroupsByLhmObjectId = new HashMap<>();
    private Map<String, List<User>> zammadUsersByLhmObjectId = new HashMap<>();

    public boolean isEmptyZammadGroupsByLhmObjectId() {
        return zammadGroupsByLhmObjectId.isEmpty();
    }

    public boolean isEmptyZammadUsersByLhmObjectId() {
        return zammadUsersByLhmObjectId.isEmpty();
    }

    public List<Group> putGroup(Group group) {
        return zammadGroupsByLhmObjectId.put(String.valueOf(group.getLhmobjectid()), new ArrayList<>(Arrays.asList(group)));
    }

    public List<User> putUser(User user) {
        return zammadUsersByLhmObjectId.put(String.valueOf(user.getLhmobjectid()), new ArrayList<>(Arrays.asList(user)));
    }

    public List<User> flatMapUsersByLhmObjectId() {
        return zammadUsersByLhmObjectId.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

}
