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
package com.efs.sdk.accessmanager.client;

import com.efs.sdk.accessmanager.clients.OrganizationManagerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class OrganizationManagerClientTest {

    private OrganizationManagerClient client;

    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private JwtAuthenticationToken token;
    @MockBean
    private Jwt jwt;

    @BeforeEach
    void setup() {
        this.restTemplate = Mockito.mock(RestTemplate.class);
        this.token = Mockito.mock(JwtAuthenticationToken.class);
        this.jwt = Mockito.mock(Jwt.class);
        this.client = new OrganizationManagerClient(restTemplate, "organizationEndpoint", "spaceEndpoint");
    }

    @Test
    void givenGetOrganizationOk_whenGetOrganization_thenOk() {
        given(token.getToken()).willReturn(jwt);
        given(jwt.getTokenValue()).willReturn("any value");

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", 1L);
        dto.put("name", "test");
        dto.put("description", "test description");

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willReturn(ResponseEntity.ok(dto));

        assertEquals(1L, client.getOrganization(token, "test").get("id"));
    }

    @Test
    void givenGetOrganizationRestClientException_whenGetOrganization_thenError() {
        given(token.getToken()).willReturn(jwt);
        given(jwt.getTokenValue()).willReturn("any value");

        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<Map<String, Object>>() {
        }))).willThrow(exception);

        assertThrows(HttpServerErrorException.class, () -> client.getOrganization(token, "test"));
    }

    @Test
    void givenGetSpacesOk_whenGetSpaces_thenOk() {
        given(token.getToken()).willReturn(jwt);
        given(jwt.getTokenValue()).willReturn("any value");

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", 1L);
        dto.put("name", "test");
        dto.put("description", "test description");

        List<Map<String, Object>> spaces = List.of(dto);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willReturn(ResponseEntity.ok(spaces));

        assertEquals(1, client.getSpaces(token, 1L, null).size());
    }

    @Test
    void givenGetSpacesRestClientException_whenGetSpaces_thenError() {
        given(token.getToken()).willReturn(jwt);
        given(jwt.getTokenValue()).willReturn("any value");

        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(new ParameterizedTypeReference<List<Map<String, Object>>>() {
        }))).willThrow(exception);

        assertThrows(HttpServerErrorException.class, () -> client.getSpaces(token, 1L, null));
    }

}
