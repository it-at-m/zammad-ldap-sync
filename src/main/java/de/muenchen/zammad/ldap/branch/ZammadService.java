package de.muenchen.zammad.ldap.branch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.muenchen.zammad.domain.ChannelsEmail;
import de.muenchen.zammad.domain.Group;
import de.muenchen.zammad.domain.Role;
import de.muenchen.zammad.domain.Signatures;
import de.muenchen.zammad.domain.User;
import de.muenchen.zammad.property.ZammadProperties;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZammadService {

    private static final String AUTHORIZATION = "Authorization";

    private final RestTemplate restTemplate = new RestTemplate();

    private final ZammadProperties zammadProperties;

    public ZammadService(ZammadProperties zammadProperties) {
        this.zammadProperties = zammadProperties;
    }

    public List<Group> getZammadGroups() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        boolean found = true;
        int i = 0;
        List<Group> result = new ArrayList<>();
        while (found) {
            i = i + 1;
            log.debug("Fetching groups page {}", i);
            ResponseEntity<Group[]> entity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups() + "?page=" + i + "&per_page=500",
                    HttpMethod.GET, new HttpEntity<>(headers), Group[].class);

            if (entity.hasBody() && entity.getBody().length > 0) {
                result.addAll(Arrays.asList(entity.getBody()));
            } else {
                found = false;
            }
        }
        return result;
    }

    public Group updateZammadGroup(Group zammadGroup) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Group> requestEntity = new HttpEntity<>(zammadGroup, headers);

        String userId = zammadGroup.getId();

        ResponseEntity<Group> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups() + "/" + userId, HttpMethod.PUT, requestEntity,
                Group.class);

        return responseEntity.getBody();
    }

    public String deleteZammadGroup(String id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Group> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups() + "/" + id, HttpMethod.DELETE, requestEntity,
                String.class);

        return responseEntity.getBody();
    }

    public Optional<Group> createZammadGroup(Group zammadGroup) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Group> requestEntity = new HttpEntity<>(zammadGroup, headers);
        ResponseEntity<Group> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups(), HttpMethod.POST, requestEntity,
                Group.class);
            log.trace(responseEntity.toString());
            return Optional.of(responseEntity.getBody());

        } catch (Exception ex)
        {
            log.error("Create Zammad Group '{}' failed. Message : {}", zammadGroup.toString(), ex.getMessage());
            return Optional.empty();
        }
    }

    public List<User> getZammadUsers() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        boolean found = true;
        int i = 0;
        List<User> result = new ArrayList<>();
        while (found) {
            i = i + 1;
            log.debug("Fetching users page {}", i);
            ResponseEntity<User[]> entity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers() + "?page=" + i + "&per_page=500", HttpMethod.GET,
                    new HttpEntity<>(headers), User[].class);
            if (entity.hasBody() && entity.getBody().length > 0) {
                result.addAll(Arrays.asList(entity.getBody()));
            } else {
                found = false;
            }
        }
        return result;
    }

    public User updateZammadUser(User zammadUser) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<User> requestEntity = new HttpEntity<>(zammadUser, headers);

        String userId = zammadUser.getId();

        ResponseEntity<User> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers() + "/" + userId, HttpMethod.PUT, requestEntity,
                User.class);

        return responseEntity.getBody();
    }

    public User createZammadUser(User zammadUser) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<User> requestEntity = new HttpEntity<>(zammadUser, headers);

        ResponseEntity<User> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers(), HttpMethod.POST, requestEntity,
                User.class);

        return responseEntity.getBody();
    }

    public String deleteZammadUser(String id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<User> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers() + "/" + id, HttpMethod.DELETE, requestEntity,
                String.class);

        return responseEntity.getBody();
    }

    public Role getZammadRole(int id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Role> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Role> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getRoles() + "/" + id, HttpMethod.GET, requestEntity,
                Role.class);

        return responseEntity.getBody();
    }

    public List<Role> getZammadRoles() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Role> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Role[]> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getRoles(), HttpMethod.GET, requestEntity,
                Role[].class);

        return Arrays.asList(responseEntity.getBody());
    }

    public Role updateZammadRole(Role zammadRole) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<Role> requestEntity = new HttpEntity<>(zammadRole, headers);

        String userId = zammadRole.getId();

        ResponseEntity<Role> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getRoles() + "/" + userId, HttpMethod.PUT, requestEntity,
                Role.class);

        return responseEntity.getBody();
    }

    public ChannelsEmail getZammadChannelsEmail() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<ChannelsEmail> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ChannelsEmail> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getChannelsEmail(), HttpMethod.GET, requestEntity,
                ChannelsEmail.class);

        return (ChannelsEmail) responseEntity.getBody();
    }

    public List<Signatures> getZammadEmailSignatures() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(AUTHORIZATION, zammadProperties.getToken());

        HttpEntity<List<Signatures>> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Signatures[]> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getSignatures(), HttpMethod.GET, requestEntity,
                Signatures[].class);

        return  Arrays.asList(responseEntity.getBody());
    }
}
