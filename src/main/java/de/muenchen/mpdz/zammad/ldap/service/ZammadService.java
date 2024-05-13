package de.muenchen.mpdz.zammad.ldap.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.muenchen.mpdz.zammad.ldap.domain.ZammadGroupDTO;
import de.muenchen.mpdz.zammad.ldap.domain.ZammadRoleDTO;
import de.muenchen.mpdz.zammad.ldap.domain.ZammadUserDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZammadService {

	public ZammadService(ZammadProperties zammadProperties) {
		this.zammadProperties = zammadProperties;
	}

	private RestTemplate restTemplate = new RestTemplate();

	private ZammadProperties zammadProperties;

	public List<ZammadGroupDTO> getZammadGroups() {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		boolean found = true;
		int i = 0;
		List<ZammadGroupDTO> result = new ArrayList<>();
		while (found) {
			i = i + 1;
			log.debug("Fetching groups page " + i);
			ResponseEntity<ZammadGroupDTO[]> entity = restTemplate.exchange(
					zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups() + "?page=" + i + "&per_page=500", HttpMethod.GET,
					new HttpEntity<Object>(headers), ZammadGroupDTO[].class);

			if (entity.hasBody() && entity.getBody().length > 0) {
				result.addAll(Arrays.asList(entity.getBody()));
			} else {
				found = false;
			}
		}
		return result;
	}

	public ZammadGroupDTO updateZammadGroup(ZammadGroupDTO zammadGroupDTO) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(zammadGroupDTO, headers);

		String userId = zammadGroupDTO.getId();

		ResponseEntity<ZammadGroupDTO> responseEntity = restTemplate.exchange(
				zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups()  + "/" + userId, HttpMethod.PUT, requestEntity, ZammadGroupDTO.class);

		return responseEntity.getBody();
	}

	public String deleteZammadGroup(String id) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups()  + "/" + id,
				HttpMethod.DELETE, requestEntity, String.class);

		return responseEntity.getBody();
	}

	public ZammadGroupDTO createZammadGroup(ZammadGroupDTO zammadGroupDTO) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadGroupDTO> requestEntity = new HttpEntity<>(zammadGroupDTO, headers);

		ResponseEntity<ZammadGroupDTO> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getGroups() ,
				HttpMethod.POST, requestEntity, ZammadGroupDTO.class);

		return responseEntity.getBody();

	}

	public List<ZammadUserDTO> getZammadUsers() {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		boolean found = true;
		int i = 0;
		List<ZammadUserDTO> result = new ArrayList<>();
		while (found) {
			i = i + 1;
			log.debug("Fetching users page " + i);
			ResponseEntity<ZammadUserDTO[]> entity = restTemplate.exchange(
					zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers()  + "?page=" + i + "&per_page=500", HttpMethod.GET,
					new HttpEntity<Object>(headers), ZammadUserDTO[].class);
			if (entity.hasBody() && entity.getBody().length > 0) {
				result.addAll(Arrays.asList(entity.getBody()));
			} else {
				found = false;
			}
		}
		return result;
	}

	public ZammadUserDTO updateZammadUser(ZammadUserDTO zammadUserDTO) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(zammadUserDTO, headers);

		String userId = zammadUserDTO.getId();

		ResponseEntity<ZammadUserDTO> responseEntity = restTemplate.exchange(
				zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers() + "/" + userId, HttpMethod.PUT, requestEntity, ZammadUserDTO.class);

		return responseEntity.getBody();
	}

	public ZammadUserDTO createZammadUser(ZammadUserDTO zammadUserDTO) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(zammadUserDTO, headers);

		ResponseEntity<ZammadUserDTO> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers(),
				HttpMethod.POST, requestEntity, ZammadUserDTO.class);

		return responseEntity.getBody();
	}

	public String deleteZammadUser(String id) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadUserDTO> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getUsers() + "/" + id,
				HttpMethod.DELETE, requestEntity, String.class);

		return responseEntity.getBody();
	}

	public ZammadRoleDTO getZammadRole(String id) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadRoleDTO> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<ZammadRoleDTO> responseEntity = restTemplate.exchange(zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getRoles() + "/" + id,
				HttpMethod.GET, requestEntity, ZammadRoleDTO.class);

		return responseEntity.getBody();
	}

	public ZammadRoleDTO updateZammadRole(ZammadRoleDTO zammadRoleDTO) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", zammadProperties.getToken());

		HttpEntity<ZammadRoleDTO> requestEntity = new HttpEntity<>(zammadRoleDTO, headers);

		String userId = zammadRoleDTO.getId();

		ResponseEntity<ZammadRoleDTO> responseEntity = restTemplate.exchange(
				zammadProperties.getUrl().getBase() + zammadProperties.getUrl().getRoles() + "/" + userId, HttpMethod.PUT, requestEntity, ZammadRoleDTO.class);

		return responseEntity.getBody();
	}

}
