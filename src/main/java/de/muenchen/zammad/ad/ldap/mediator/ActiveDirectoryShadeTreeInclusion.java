package de.muenchen.zammad.ad.ldap.mediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuSearchResultDTO;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserDTO;
import de.muenchen.oss.ezldap.core.LdapUserDTO;
import de.muenchen.zammad.ad.ActiveDirectoryUserDTO;
import de.muenchen.zammad.ad.EnhancedActiveDirectoryGroupDTO;
import de.muenchen.zammad.ldap.property.LdapProperty;
import de.muenchen.zammad.ldap.property.RequestedOrganizationalUnits;
import de.muenchen.zammad.ldap.tree.LdapOuNode;
import de.muenchen.zammad.ldap.tree.LdapService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActiveDirectoryShadeTreeInclusion {

    private List<EnhancedActiveDirectoryGroupMediatorDTO> mediatorGroups;

    @Getter
    private Map<String, LdapOuNode> branches = new HashMap<>();

    @Getter
    private Map<String, EnhancedLdapUserDTO> allUsersFromAllBranches;

    private RequestedOrganizationalUnits requestedOuUnits;

    private LdapService ldapService;

    @Autowired
    public ActiveDirectoryShadeTreeInclusion(LdapProperty ldapProperty, RequestedOrganizationalUnits requestedOuUnits) {
        super();
        this.ldapService = new LdapService(ldapProperty.getUrl(), null, null, null, null);
        this.requestedOuUnits = requestedOuUnits;
    }

    public ActiveDirectoryShadeTreeInclusion(RequestedOrganizationalUnits requestedOuUnits, LdapService ldapService) {
        super();
        this.requestedOuUnits = requestedOuUnits;
        this.ldapService = ldapService;
    }

    public void merge(List<EnhancedActiveDirectoryGroupDTO> adLdapGroups, Map<String, LdapOuNode> ldapShadeTrees) {

        this.allUsersFromAllBranches = collectUserFromAllBranches(ldapShadeTrees);
        this.mediatorGroups = enrichGroupsToMergeIntoLdapTree(adLdapGroups);
        this.branches = mergeAdGroups(ldapShadeTrees);

        if (log.isDebugEnabled())
            this.branches.values().forEach(node -> log.debug(node.toString()));

    }

    private Map<String, LdapOuNode> mergeAdGroups(Map<String, LdapOuNode> ldapShadeTrees) {

        for (Entry<String, LdapOuNode> entry : ldapShadeTrees.entrySet()) {
            var node = entry.getValue();
            for (EnhancedActiveDirectoryGroupMediatorDTO mediatorGroup : mediatorGroups) {
                Optional<LdapOuNode> mediatorParentNode = node
                        .findLdapOuNode(mediatorGroup.getParentLdapDistinguishedName());

                mediatorParentNode.ifPresentOrElse(ouNode -> {
                    var crossFunctionalGroup = new LdapOuNode();
                    crossFunctionalGroup.setDistinguishedName(mediatorGroup.getLdapDistinguishedName());
                    crossFunctionalGroup.setOrganizationalUnit(ouNode.getOrganizationalUnit());
                    EnhancedLdapOuSearchResultDTO ldapOuDTO = MediatorDTOMapper.INSTANCE.ldapOuDTO(mediatorGroup);
                    crossFunctionalGroup.setNode(ldapOuDTO);
                    crossFunctionalGroup.setUsers(Optional.of(mergeUsers(mediatorGroup.getAdUserByLhmObjectId())));
                    crossFunctionalGroup.getUsers()
                            .ifPresent(nodUsers -> removeCrossFunctionalUsersFromTheirOriginalLdapOu(nodUsers, node));
                    ouNode.getChildNodes()
                            .ifPresent(childNodes -> childNodes.put(mediatorGroup.getName(), crossFunctionalGroup));
                    log.info("AD group added : {}", crossFunctionalGroup.toString());
                }, () ->
                {
                    var keys = new ArrayList<>(mediatorGroup.getAdUserByLhmObjectId().keySet());
                    log.error(
                            "AdGroup '{}' is not added ! Please check the validity of the ldap.lhmObjectPaths of the users ('{}') from the AdGroup'.",
                             mediatorGroup.getName(), keys);
                }
                );
            }
        }
        return ldapShadeTrees;
    }

    /*
     * An lhmobjectid may only be assigned to one group created in Zammad.
     */
    private void removeCrossFunctionalUsersFromTheirOriginalLdapOu(List<EnhancedLdapUserDTO> users, LdapOuNode node) {
        for (EnhancedLdapUserDTO user : users) {
            var parent = node.findLdapOuNode(user.getLhmObjectPath());
            parent.ifPresentOrElse(parentNode -> {
                boolean removed = parentNode.getUsers().get()
                        .removeIf(listUser -> listUser.getLhmObjectId().equals(user.getLhmObjectId()));
                log.debug("User '{}' removed from list '{}' : {} ", user.getLhmObjectId(), parentNode.getUsers(),
                        removed);
            }, () -> log.error(
                    "Could not delete user. Parent group node for user '{}' not found. Check validity of user.lhmObjectPath '{}'",
                    user.getLhmObjectId(), user.getLhmObjectPath()));

        }
    }

    private List<EnhancedLdapUserDTO> mergeUsers(Map<String, ActiveDirectoryUserDTO> adUserByLhmObjectId) {
        return adUserByLhmObjectId.entrySet().stream()
                .map(entry -> ldapService.lookupUser(investigateLdapUserDn(entry.getValue())).get()).toList();
    }

    /*
     * Determine the ldap search base using the department abbreviation in the
     * configuration, add uid and return complete ldap user distinguished name. This
     * assumes that the OU identifiers in the configuration correspond to the
     * department AD identifiers.
     */
    private String investigateLdapUserDn(ActiveDirectoryUserDTO user) {
        return String.join(",", "uid=" + user.getUid(),
                requestedOuUnits.getOrganizationalUnits().get(user.getLhmReferatName()).getUserSearchBase());
    }

    private List<EnhancedActiveDirectoryGroupMediatorDTO> enrichGroupsToMergeIntoLdapTree(
            List<EnhancedActiveDirectoryGroupDTO> adGroups) {

        List<EnhancedActiveDirectoryGroupMediatorDTO> enrichmentGroups = MediatorDTOMapper.INSTANCE
                .mediatorDTO(adGroups);
        for (EnhancedActiveDirectoryGroupMediatorDTO enrichmentGroup : enrichmentGroups) {
            enrichDistinguishedName(enrichmentGroup);
        }
        return enrichmentGroups;
    }

    /*
     * To locate active directory group in the ldap tree the parent group is
     * determined from the lowest common denominator of the distinguished names of
     * the assigned users.
     */
    private void enrichDistinguishedName(EnhancedActiveDirectoryGroupMediatorDTO mediatorGroup) {

        List<String> lhmObjectIds = mediatorGroup.getAdUserByLhmObjectId().values().stream()
                .map(ActiveDirectoryUserDTO::getLhmObjectId).toList();
        List<EnhancedLdapUserDTO> mediatorGroupLdapUsers = allUsersFromAllBranches.entrySet().stream()
                .filter(entry -> lhmObjectIds.contains(entry.getKey())).map(Map.Entry::getValue).toList();
        List<String> lhmObjectPaths = mediatorGroupLdapUsers.stream().filter(user -> user.getLhmObjectPath() != null)
                .map(EnhancedLdapUserDTO::getLhmObjectPath).toList();
        String mediatorGroupLdapDistinguishedName = findParentDistinguishedNameDenominator(lhmObjectPaths);
        mediatorGroup.setParentLdapDistinguishedName(mediatorGroupLdapDistinguishedName);
        log.info("Active directory group '{}' is assigned ldap distinguished name '{}'.",
                mediatorGroup.getDisplayName(), mediatorGroupLdapDistinguishedName);
    }

    public static String findParentDistinguishedNameDenominator(List<String> objectPaths) {

        if (objectPaths.isEmpty()) {
            log.warn("Empty list of object paths.");
            return null;
        }

        var listOfTokenizedDns = objectPaths.stream().map(s -> Arrays.asList(s.split("(?<!\\\\),"))).map(list -> {
            Collections.reverse(list);
            return list;
        }).toList();

        List<String> immutableList = null;
        for (List<String> list : listOfTokenizedDns) {
            if (immutableList == null)
                immutableList = list;
            else
                immutableList = findEqualElementsOfSortedLists(immutableList, list);
        }
        List<String> commonElements = new ArrayList<>(immutableList);
        Collections.reverse(commonElements);
        return String.join(",", commonElements);
    }

    private static List<String> findEqualElementsOfSortedLists(List<String> list1, List<String> list2) {
        return IntStream.range(0, Math.min(list1.size(), list2.size())).filter(i -> list1.get(i).equals(list2.get(i)))
                .mapToObj(list1::get).toList();
    }

    public static Map<String, EnhancedLdapUserDTO> collectUserFromAllBranches(Map<String, LdapOuNode> ldapShadetrees) {

        Map<String, EnhancedLdapUserDTO> collection = new TreeMap<>();
        for (Map.Entry<String, LdapOuNode> entry : ldapShadetrees.entrySet()) {
            collection.putAll(entry.getValue().flatListLdapUserDTO().stream()
                    .collect(Collectors.toMap(LdapUserDTO::getLhmObjectId, Function.identity())));
        }
        return collection;
    }

}
