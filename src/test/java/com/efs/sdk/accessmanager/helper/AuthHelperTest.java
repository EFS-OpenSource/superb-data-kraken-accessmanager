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
package com.efs.sdk.accessmanager.helper;

import com.efs.sdk.accessmanager.clients.OrganizationManagerClient;
import com.efs.sdk.accessmanager.clients.OrganizationManagerClient.Permissions;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class AuthHelperTest {

    private AuthHelper authHelper;

    @MockBean
    private JwtAuthenticationToken token;
    @MockBean
    private Jwt jwt;
    @MockBean
    private OrganizationManagerClient client;

    @BeforeEach
    void setup() {
        this.client = Mockito.mock(OrganizationManagerClient.class);
        this.token = Mockito.mock(JwtAuthenticationToken.class);
        this.jwt = Mockito.mock(Jwt.class);
        this.authHelper = new AuthHelper(client);
    }

    @Test
    void givenSpaceListed_whenIsAllowed_thenTrue() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        given(client.getOrganization(token, organization)).willReturn(orgaDto);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertTrue(authHelper.isAllowed(token, organization, space, Permissions.WRITE));
    }

    @Test
    void givenSpaceNotListed_whenIsAllowed_thenFalse() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        List<Map<String, Object>> spaces = new ArrayList<>();

        given(client.getOrganization(token, organization)).willReturn(orgaDto);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertFalse(authHelper.isAllowed(token, organization, space, Permissions.WRITE));
    }

    @Test
    void givenGetOrganizationForbidden_whenIsAllowed_thenFalse() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertFalse(authHelper.isAllowed(token, organization, space, Permissions.WRITE));
    }

    @Test
    void givenGetOrganizationNotFound_whenIsAllowed_thenError() {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        AccessManagerException exception = assertThrows(AccessManagerException.class, () -> authHelper.isAllowed(token, organization, space,
                Permissions.WRITE));
        assertTrue(exception.getMessage().startsWith(new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATION_NOT_FOUND).getMessage()));
    }

    @Test
    void givenGetOrganizationHttpStatusCodeError_whenIsAllowed_thenError() {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        AccessManagerException exception = assertThrows(AccessManagerException.class, () -> authHelper.isAllowed(token, organization, space,
                Permissions.WRITE));
        assertTrue(exception.getMessage().startsWith(new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATIONMANAGER_ERROR).getMessage()));
    }

    @Test
    void givenPreferredUsername_whenGetUsername_thenOk() {
        Jwt jwt = new Jwt("any token value", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"), Map.of("preferred_username", "testuser"));
        given(token.getToken()).willReturn(jwt);
        assertEquals("testuser", authHelper.getUserName(token));
    }

    @Test
    void givenNoPreferredUsername_whenGetUsername_thenNull() {
        Jwt jwt = new Jwt("any token value", Instant.now(), Instant.now().plusSeconds(30), Map.of("alg", "none"), Map.of("any_key", "any_value"));
        given(token.getToken()).willReturn(jwt);
        assertNull(authHelper.getUserName(token));
    }

    @Test
    void givenAnySpaceListed_whenCanAccessOrganization_thenTrue() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        given(client.getOrganization(token, organization)).willReturn(orgaDto);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertTrue(authHelper.canAccessOrganization(token, organization, Permissions.WRITE));
    }

    @Test
    void givenNoSpaceListed_whenCanAccessOrganization_thenFalse() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        List<Map<String, Object>> spaces = new ArrayList<>();

        given(client.getOrganization(token, organization)).willReturn(orgaDto);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertFalse(authHelper.canAccessOrganization(token, organization, Permissions.WRITE));
    }

    @Test
    void givenGetOrganizationForbidden_whenCanAccessOrganization_thenFalse() throws Exception {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.FORBIDDEN);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        assertFalse(authHelper.canAccessOrganization(token, organization, Permissions.WRITE));
    }

    @Test
    void givenGetOrganizationNotFound_CanAccessOrganization_thenError() {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpClientErrorException except = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        AccessManagerException exception = assertThrows(AccessManagerException.class, () -> authHelper.canAccessOrganization(token, organization,
                Permissions.WRITE));
        assertTrue(exception.getMessage().startsWith(new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATION_NOT_FOUND).getMessage()));
    }

    @Test
    void givenGetOrganizationHttpStatusCodeError_whenCanAccessOrganization_thenError() {
        String username = "testuser";
        given(token.getToken()).willReturn(jwt);
        given(jwt.getClaimAsString("preferred_username")).willReturn(username);

        String organization = "test";
        String space = "test";

        Map<String, Object> orgaDto = new HashMap<>();
        orgaDto.put("id", 1L);
        orgaDto.put("name", organization);

        Map<String, Object> spaceDto = new HashMap<>();
        spaceDto.put("id", 1L);
        spaceDto.put("name", space);

        List<Map<String, Object>> spaces = List.of(spaceDto);

        HttpServerErrorException except = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        given(client.getOrganization(token, organization)).willThrow(except);
        given(client.getSpaces(eq(token), eq(Long.parseLong(orgaDto.get("id").toString())), any())).willReturn(spaces);

        AccessManagerException exception = assertThrows(AccessManagerException.class, () -> authHelper.canAccessOrganization(token, organization,
                Permissions.WRITE));
        assertTrue(exception.getMessage().startsWith(new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATIONMANAGER_ERROR).getMessage()));
    }

}
