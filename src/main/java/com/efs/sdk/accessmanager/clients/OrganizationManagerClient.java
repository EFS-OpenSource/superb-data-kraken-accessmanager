/*
Copyright (C) 2023 e:fs TechHub GmbH (sdk@efs-techhub.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.efs.sdk.accessmanager.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OrganizationManagerClient {

    private final RestTemplate restTemplate;
    private final String organizationEndpoint;
    private final String spaceEndpoint;

    public enum Permissions {
        READ,
        WRITE,
        DELETE,
        GET
    }

    public OrganizationManagerClient(RestTemplate restTemplate, @Value("${accessmanager.organizationmanager-endpoints.organization}") String organizationEndpoint, @Value("${accessmanager.organizationmanager-endpoints.space}") String spaceEndpoint) {
        this.restTemplate = restTemplate;
        this.organizationEndpoint = organizationEndpoint;
        this.spaceEndpoint = spaceEndpoint;
    }

    /**
     * Get organization by name
     * <p>
     * Gets a given organization, if public or where the user has access to.
     *
     * @param token the (user) token that is used to make the request
     * @param organizationName the name of the organization to get
     * @return the organization
     * @throws RestClientException on 4xx client error or 5xx server error
     */
    public Map<String, Object> getOrganization(JwtAuthenticationToken token, String organizationName) throws RestClientException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token.getToken().getTokenValue());
        String url = String.format("%s/name/%s", organizationEndpoint, organizationName);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
        return response.getBody();
    }

    /**
     * Get spaces (by permissions)
     * <p>
     * Lists all spaces of given organization
     * (only allowed, if user has access to the organization or if space and organization are public).
     * If permissions is set, list only the spaces the user has requested permissions to.
     * 
     * @param token the (user) token that is used to make the request
     * @param organizationId ID of the organization containing the spaces
     * @param permissions name of the permissions (can be one of READ, WRITE or DELETE)
     * @return the spaces
     * @throws RestClientException on 4xx client error or 5xx server error
     */
    public List<Map<String, Object>> getSpaces(JwtAuthenticationToken token, Long organizationId, Permissions permissions) throws RestClientException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token.getToken().getTokenValue());
        String url = String.format("%s/%d", spaceEndpoint, organizationId);
        if (permissions != null) {
            url += String.format("?permissions=%s", permissions);
        }

        ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
        return response.getBody();
    }
    
}
