package de.muenchen.mpdz.zammad.ldapAnbindung.service;

import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadRoleDTO;
import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ZammadService {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${zammad.token}")
    private String authorization;
    @Value("${zammad.url.base}")
    private String zammadBaseURL;

    @Value("${zammad.url.groups}")
    private String zammadGroupsURL;

    @Value("${zammad.url.users}")
    private String zammadUsersURL;
    @Value("${zammad.url.roles}")
    private String zamamdRolesURL;

    public List<ZammadGroupDTO> getZammadGroups() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        boolean found = true;
        int i = 0;
        List<ZammadGroupDTO> result = new ArrayList<>();
        while (found) {
            i = i + 1;
            log.info("Fetching groups page " + i);
            ResponseEntity<ZammadGroupDTO[]> entity = restTemplate.exchange(
                    zammadBaseURL + zammadGroupsURL + "?page=" + i + "&per_page=500", HttpMethod.GET, new HttpEntity<Object>(headers),
                    ZammadGroupDTO[].class);
            if (entity.hasBody() && entity.getBody() != null) {
                if (entity.getBody().length > 0) {
                    result.addAll(Arrays.asList(entity.getBody()));
                } else {
                    found = false;
                }
            }
        }
        return result;
    }

    public ZammadGroupDTO updateZammadGroup(ZammadGroupDTO zammadGroupDTO) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(zammadGroupDTO, headers);

        String userId = zammadGroupDTO.getId();

        ResponseEntity<ZammadGroupDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadGroupsURL + "/" + userId, HttpMethod.PUT, requestEntity,
                ZammadGroupDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public String deleteZammadGroup(String id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadGroupsURL + "/" + id, HttpMethod.DELETE, requestEntity,
                String.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public ZammadGroupDTO createZammadGroup(ZammadGroupDTO zammadGroupDTO) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(zammadGroupDTO, headers);

        ResponseEntity<ZammadGroupDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadGroupsURL, HttpMethod.POST, requestEntity,
                ZammadGroupDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public List<ZammadUserDTO> getZammadUsers() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        boolean found = true;
        int i = 0;
        List<ZammadUserDTO> result = new ArrayList<>();
        while (found) {
            i = i + 1;
            log.info("Fetching users page " + i);
            ResponseEntity<ZammadUserDTO[]> entity = restTemplate.exchange(
                    zammadBaseURL + zammadUsersURL + "?page=" + i + "&per_page=500", HttpMethod.GET, new HttpEntity<Object>(headers),
                    ZammadUserDTO[].class);
            if (entity.hasBody() && entity.getBody() != null) {
                if (entity.getBody().length > 0) {
                    result.addAll(Arrays.asList(entity.getBody()));
                } else {
                    found = false;
                }
            }
        }
        return result;
    }


    public ZammadUserDTO updateZammadUser(ZammadUserDTO zammadUserDTO) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(zammadUserDTO, headers);

        String userId = zammadUserDTO.getId();

        ResponseEntity<ZammadUserDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadUsersURL + "/" + userId, HttpMethod.PUT, requestEntity,
                ZammadUserDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public ZammadUserDTO createZammadUser(ZammadUserDTO zammadUserDTO) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(zammadUserDTO, headers);

        ResponseEntity<ZammadUserDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadUsersURL, HttpMethod.POST, requestEntity,
                ZammadUserDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public String deleteZammadUser(String id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                zammadBaseURL + zammadUsersURL + "/" + id, HttpMethod.DELETE, requestEntity,
                String.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public ZammadRoleDTO getZammadRole(String id) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadRoleDTO> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ZammadRoleDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zamamdRolesURL + "/" + id, HttpMethod.GET, requestEntity,
                ZammadRoleDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }


    public ZammadRoleDTO updateZammadRole(ZammadRoleDTO zammadRoleDTO) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", authorization);

        HttpEntity<ZammadRoleDTO> requestEntity = new HttpEntity<>(zammadRoleDTO, headers);

        String userId = zammadRoleDTO.getId();

        ResponseEntity<ZammadRoleDTO> responseEntity = restTemplate.exchange(
                zammadBaseURL + zamamdRolesURL + "/" + userId, HttpMethod.PUT, requestEntity,
                ZammadRoleDTO.class);
        if (responseEntity.hasBody() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

}
