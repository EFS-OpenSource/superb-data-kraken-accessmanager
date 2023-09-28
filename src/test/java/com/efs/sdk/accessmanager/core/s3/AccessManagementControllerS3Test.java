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
package com.efs.sdk.accessmanager.core.s3;

import com.efs.sdk.accessmanager.clients.OrganizationManagerClient.Permissions;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.helper.AuthHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(AccessManagementControllerS3.class)
@ActiveProfiles({"test", "test-s3"})
class AccessManagementControllerS3Test {

    private static final String EXAMPLE_SPACE = "qwer";
    private static final String EXAMPLE_ORGANIZATION = "asdf";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthHelper authHelper;

    @MockBean
    private AccessManagementServiceS3 service;

    /* required for security tests to run. Do not remove! */
    @MockBean
    private JwtDecoder decoder;

    @Test
    void givenCanWrite_whenCommit_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/commit").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE).queryParam(
                "rootDir", "test")).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanCreate_whenCommit_thenOk() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(true);
        mvc.perform(post(ENDPOINT + "/commit").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE).queryParam(
                "rootDir", "test")).andExpect(status().is2xxSuccessful());
    }

    @Test
    void givenCanNotWriteOrCreate_whenCommit_thenError() throws Exception {
        given(authHelper.isAllowed(any(), anyString(), anyString(), eq(Permissions.WRITE))).willReturn(false);
        given(authHelper.getUserName(any())).willReturn("any");

        Mockito.when(service.commit(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenThrow(new AccessManagerException(SAVE_ACCESS_DENIED));
        mvc.perform(post(ENDPOINT + "/commit").with(jwt()).queryParam("organization", EXAMPLE_ORGANIZATION).queryParam("space", EXAMPLE_SPACE).queryParam(
                "rootDir", "test")).andExpect(status().is4xxClientError());
    }
}
