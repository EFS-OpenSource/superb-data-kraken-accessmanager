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
package com.efs.sdk.accessmanager.core.azure;

import com.efs.sdk.accessmanager.clients.OrganizationManagerClient.Permissions;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.helper.AuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR.SAVE_ACCESS_DENIED;
import static com.efs.sdk.accessmanager.core.AccessManagementControllerV2.ENDPOINT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessManagementControllerV2Azure.class)
@ActiveProfiles({"test"})
class AccessManagementControllerV2AzureTest {

    private static final String DEF_CONNECTION_ID = "testconn";
    private static final String EXAMPLE_SPACE = "qwer";
    private static final String EXAMPLE_ORGANIZATION = "asdf";
    private static final String SPACE_LOADINGZONE = "loadingzone";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthHelper authHelper;

    @MockBean
    private AccessManagementServiceAzure service;

    /* required for security tests to run. Do not remove! */
    @MockBean
    private JwtDecoder decoder;

    @Test
    void givenNoAuthentication_whenGetReadToken_thenError() throws Exception {
        mvc.perform(post(ENDPOINT + "/read")).andExpect(status().is4xxClientError());
    }

    @Test
    void givenCanRead_whenGetReadToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.READ))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/read").with(jwt()).queryParam("organization", DEF_CONNECTION_ID).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanNotRead_whenGetReadToken_thenError() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.READ))).willReturn(false);
        AccessManagerException ame = new AccessManagerException(AccessManagerException.ACCESSMANAGER_ERROR.READ_ACCESS_DENIED);
        given(service.createReadToken(anyString(), anyString(), anyBoolean())).willThrow(ame);
        mvc.perform(post(ENDPOINT + "/read").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is4xxClientError());
    }

    @Test
    void givenCanWrite_whenGetWriteToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/upload").with(jwt()).queryParam("organization", DEF_CONNECTION_ID).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanNotWrite_whenGetWriteToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(false);
        AccessManagerException ame = new AccessManagerException(SAVE_ACCESS_DENIED);
        given(service.createUploadToken(anyString(), anyString(), anyBoolean())).willThrow(ame);
        mvc.perform(post(ENDPOINT + "/upload").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is4xxClientError());
    }


    @Test
    void givenCanWrite_whenGetWriteMainToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/upload/main").with(jwt()).queryParam("organization", DEF_CONNECTION_ID).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanNotWrite_whenGetWriteMainToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.READ))).willReturn(true);
        AccessManagerException ame = new AccessManagerException(SAVE_ACCESS_DENIED);
        given(service.createUploadToken(anyString(), anyString(), anyBoolean())).willThrow(ame);
        mvc.perform(post(ENDPOINT + "/upload/main").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is4xxClientError());
    }

    @Test
    void givenCanDelete_whenGetDeleteToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.DELETE))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/delete").with(jwt()).queryParam("organization", DEF_CONNECTION_ID).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanNotDelete_whenGetDeleteToken_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.DELETE))).willReturn(false);
        AccessManagerException ame = new AccessManagerException(AccessManagerException.ACCESSMANAGER_ERROR.DELETE_ACCESS_DENIED);
        given(service.createDeleteToken(anyString(), anyString(), anyBoolean())).willThrow(ame);
        mvc.perform(post(ENDPOINT + "/delete").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is4xxClientError());
    }

    @Test
    void givenCanDelete_whenCanReadGetDeleteTokenLoadingZone_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.DELETE))).willReturn(false);
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        given(service.createDeleteToken(anyString(), anyString(), anyBoolean())).willReturn("token");
        mvc.perform(post(ENDPOINT + "/delete").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", SPACE_LOADINGZONE)).andExpect(status().is2xxSuccessful());
    }


    @Test
    void givenCanNotDelete_whenCanNotReadGetDeleteTokenSpace_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.DELETE))).willReturn(false);
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        AccessManagerException ame = new AccessManagerException(AccessManagerException.ACCESSMANAGER_ERROR.DELETE_ACCESS_DENIED);
        given(service.createDeleteToken(anyString(), anyString(), eq(false))).willThrow(ame);
        mvc.perform(post(ENDPOINT + "/delete").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE)).andExpect(status().is4xxClientError());
    }
}
