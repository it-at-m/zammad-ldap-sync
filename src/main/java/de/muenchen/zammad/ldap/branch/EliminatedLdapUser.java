package de.muenchen.zammad.ldap.branch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.userservice.LdapOuNode;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EliminatedLdapUser extends AbstractTree {

    public EliminatedLdapUser(ZammadService zammadService) {
        this.zammadService = zammadService;
    }

    public void checkForRemoval(Map.Entry<String, LdapOuNode> ldapBranchEntry,
            Optional<Map<String, EnhancedLdapUserDTO>> allBranchesUsers) {

       if (allBranchesUsers.isPresent()) {
           final var allUsers = allBranchesUsers.get();
            if (allUsers.isEmpty())
                log.warn("Ldap branch user list is empty. The execution would set all found zammad users inactive.");
            else
                checkLdapUsers(ldapBranchEntry, allUsers);
    } else
        log.warn("Ldap user list is null. Can not compare zammad and ldap users.");
    }

    protected void checkLdapUsers(Map.Entry<String, LdapOuNode> entry, Map<String, EnhancedLdapUserDTO> allLdapUsers) {

        Optional<LdapOuNode> optionalNode = findNode(entry);
        optionalNode.ifPresent(node -> {
            try {

                var zammadBranchGroupUsers = findAllZammadBranchGroupUsers(node.getNode().getLhmObjectId());

                zammadBranchGroupUsers.forEach((lhmObjectId, users) -> {

                    if (users.size() > 1) {
                        log.error("Inconsistent Zammad state. More than one zammad user found for lhmObjectId '{}' :",
                                lhmObjectId);
                        log.error("The list should not contain dublicate users: {}." , users.toString());
                        return;
                    }

                    var zammadUser = users.get(0);
                    log.debug("---------------------------");
                    log.debug("Checking ZammadUser with lhmObjectId '{}'.", zammadUser.getLhmobjectid());

                    if (zammadUser.isLdapsyncupdate()) {

                        if (lhmObjectId == null || lhmObjectId.isEmpty()) {
                            log.debug("No lhmObjectId - skipping.");
                        } else {
                            assignDeletion(allLdapUsers, lhmObjectId, zammadUser);
                        }
                    } else {
                        log.debug("isLdapsyncupdate is '{}' - skipping.", zammadUser.isLdapsyncupdate());
                    }
                });

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    private void assignDeletion(Map<String, EnhancedLdapUserDTO> allLdapUsers, String lhmObjectId, User zammadUser) {
        final var ldapBaseUserDTO = allLdapUsers.get(lhmObjectId);
        if (ldapBaseUserDTO == null) {
            log.debug("Do not find ZammadUser in LDAP-Users.");
            if (zammadUser.isActive()) {
                log.debug("User in Zammad is active '{}' - setting to inactive as a first step.",
                        zammadUser.isActive());
                zammadUser.setActive(false);
                zammadUser.setLdapsyncstate("delete");
                zammadService.updateZammadUser(zammadUser);
            }
        } else {
            log.debug("User exists in LDAP - not deleting.");
        }
    }

    private Map<String, List<User>> findAllZammadBranchGroupUsers(String ldapOuRootLhmObjectId) {

        final var zammadGroups = new ArrayList<>(findRootZammadGroup(ldapOuRootLhmObjectId));

        if (zammadGroups.isEmpty())
            return new HashMap<>();
        else {

            findChildGroups(zammadService.getZammadGroups(), zammadGroups.get(0).getId(), zammadGroups);

            final var zammadBranchUsers = new ArrayList<User>();
            final var zammadUsers =  zammadService.getZammadCache().flatMapUsersByLhmObjectId();
            zammadGroups.forEach(g -> zammadBranchUsers.addAll(findUsers(zammadUsers, g.getId())));

            Collections.sort(zammadBranchUsers, Comparator.comparing(User::getId));
            var uniqueZammadBranchUsers = zammadBranchUsers.stream().collect(Collectors.toMap(User::getId, p -> p, (existing, replacement) -> existing))
                    .values().stream().toList();

            return uniqueZammadBranchUsers.stream().filter(u -> u.getLhmobjectid() != null && !u.getLhmobjectid().isBlank())
                    .collect(Collectors.groupingBy(User::getLhmobjectid));
        }
    }

    private List<Group> findRootZammadGroup(String ldapOuRootLhmObjectId) {

        final var rootZammadGroups = getCurrentZammadGroups().get(ldapOuRootLhmObjectId);
        if (rootZammadGroups == null) {
            log.debug("No zammad root group found '{}'.", ldapOuRootLhmObjectId);
            return new ArrayList<>();
        } else if (rootZammadGroups.size() > 1) {
            log.error("Inconsistent Zammad state. More than one zammad group found for lhmObjectId '{}' :",
                    ldapOuRootLhmObjectId);
            rootZammadGroups.forEach(item -> log.error(LOG_ID, item.getId()));
            return new ArrayList<>();
        }
        return rootZammadGroups;

    }

    private void findChildGroups(List<Group> zammadServiceGroups, Integer zammadGroupId,
            List<Group> allZammadBranchGroups) {

        final var childGroups = zammadServiceGroups.stream()
                .filter(g -> (g.getParentId() != null && g.getParentId().equals(zammadGroupId))).toList();
        if (!childGroups.isEmpty()) {
            allZammadBranchGroups.addAll(childGroups);
            childGroups.forEach(g -> findChildGroups(zammadServiceGroups, g.getId(), allZammadBranchGroups));
        }
    }

    private List<User> findUsers(List<User> zammadServiceUsers, Integer zammadGroupId) {
        return zammadServiceUsers.stream().filter(u -> u.getGroupIds().containsKey(zammadGroupId.toString())).toList();
    }

    private Optional<LdapOuNode> findNode(Map.Entry<String, LdapOuNode> entry) {

        final var optional = entry.getValue().findLdapOuNode(entry.getKey());
        if (optional.isEmpty()) {
            log.error("User removal check failed. No ldap node found with key '{}' !", entry.getKey());
        }
        return optional;
    }

}
