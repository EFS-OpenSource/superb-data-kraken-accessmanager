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

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.efs.sdk.accessmanager.clients.AzureResourceManagerProvider;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.core.azure.model.SASToken;
import com.efs.sdk.accessmanager.core.events.EventPublisher;
import com.efs.sdk.accessmanager.mock.StorageAccountMock;
import com.efs.sdk.accessmanager.mock.SupplierMock;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType.DELETE;
import static com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType.WRITE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

class AccessManagementServiceAzureTest {

    private static final String USER_NAME = "someUser";
    private static final String EXAMPLE_SPACE = "qwer";
    private static final String EXAMPLE_ORGANIZATION = "asdf";

    @MockBean
    private StorageClient mockStorage;
    @MockBean
    private ObjectMapper objectMapper;
    @MockBean
    private EventPublisher publisher;
    @MockBean
    private AzureResourceManagerProvider azureProvider;
    @MockBean
    private AzureResourceManager azure;
    @MockBean
    private StorageAccounts storageAccounts;

    private AccessManagementServiceAzure service;

    @BeforeEach
    void setup() {
        this.mockStorage = Mockito.mock(StorageClient.class);
        this.publisher = Mockito.mock(EventPublisher.class);
        this.objectMapper = Mockito.mock(ObjectMapper.class);
        this.azureProvider = Mockito.mock(AzureResourceManagerProvider.class);
        this.azure = Mockito.mock(AzureResourceManager.class);
        this.storageAccounts = Mockito.mock(StorageAccounts.class);

        this.service = new AccessManagementServiceAzure(mockStorage, objectMapper, publisher, azureProvider);
    }

    @Test
    void givenCanRead_whenGetReadToken_thenOk() throws Exception {
        String token = EXAMPLE_ORGANIZATION;
        String containerName = "container";
        String accountName = "test";
        SASToken sasToken = new SASToken(WRITE, accountName, containerName, token);

        given(azureProvider.azure()).willReturn(azure);
        given(azure.storageAccounts()).willReturn(storageAccounts);

        StorageAccount account = new StorageAccountMock(accountName);
        SupplierMock<StorageAccount> accountSupplier = new SupplierMock<>(account);
        PagedFlux<StorageAccount> accountFlux = new PagedFlux<>(accountSupplier);
        PagedIterable<StorageAccount> accountList = new PagedIterable<>(accountFlux);
        given(storageAccounts.listByResourceGroup(any())).willReturn(accountList);
        given(mockStorage.createReadToken(any(), any())).willReturn(sasToken);

        assertEquals(token, service.createReadToken(accountName, containerName, true));
    }

    @Test
    void givenCanNotRead_whenGetReadToken_thenError() {
        String containerName = "container";

        assertThrows(AccessManagerException.class, () -> service.createReadToken("testconn", containerName, false));
    }

    @Test
    void givenCanWrite_whenGetWriteToken_thenOk() throws Exception {
        String token = EXAMPLE_ORGANIZATION;
        String containerName = "container";
        String accountName = "test";
        SASToken sasToken = new SASToken(WRITE, accountName, containerName, token);

        given(azureProvider.azure()).willReturn(azure);
        given(azure.storageAccounts()).willReturn(storageAccounts);

        StorageAccount account = new StorageAccountMock(accountName);
        SupplierMock<StorageAccount> accountSupplier = new SupplierMock<>(account);
        PagedFlux<StorageAccount> accountFlux = new PagedFlux<>(accountSupplier);
        PagedIterable<StorageAccount> accountList = new PagedIterable<>(accountFlux);
        given(storageAccounts.listByResourceGroup(any())).willReturn(accountList);

        given(mockStorage.createUploadToken(any(), any())).willReturn(sasToken);

        assertEquals(token, service.createUploadToken(accountName, containerName, true));
    }

    @Test
    void givenCanNotWrite_whenGetWriteToken_thenOk() {
        String containerName = "container";

        assertThrows(AccessManagerException.class, () -> service.createUploadToken("testconn", containerName, false));
    }

    @Test
    void givenCanDelete_whenGetDeleteToken_thenOk() throws Exception {
        String token = EXAMPLE_ORGANIZATION;
        String containerName = "container";
        String accountName = "test";
        SASToken sasToken = new SASToken(DELETE, accountName, containerName, token);

        given(azureProvider.azure()).willReturn(azure);
        given(azure.storageAccounts()).willReturn(storageAccounts);

        StorageAccount account = new StorageAccountMock(accountName);
        SupplierMock<StorageAccount> accountSupplier = new SupplierMock<>(account);
        PagedFlux<StorageAccount> accountFlux = new PagedFlux<>(accountSupplier);
        PagedIterable<StorageAccount> accountList = new PagedIterable<>(accountFlux);
        given(storageAccounts.listByResourceGroup(any())).willReturn(accountList);

        given(mockStorage.createDeleteToken(any(), any())).willReturn(sasToken);

        assertEquals(token, service.createDeleteToken(accountName, containerName, true));
    }

    @Test
    void givenCanNotDelete_whenGetDeleteToken_thenError() {
        String containerName = "container";

        assertThrows(AccessManagerException.class, () -> service.createDeleteToken("testconn", containerName, false));
    }

    @Test
    void givenCanWrite_whenCommit_thenOk() throws Exception {
        Set<String> files = new HashSet<>(Arrays.asList(EXAMPLE_ORGANIZATION, EXAMPLE_SPACE));
        String containerName = "container";
        String accountName = "test";

        given(azureProvider.azure()).willReturn(azure);
        given(azure.storageAccounts()).willReturn(storageAccounts);

        StorageAccount account = new StorageAccountMock(accountName);
        SupplierMock<StorageAccount> accountSupplier = new SupplierMock<>(account);
        PagedFlux<StorageAccount> accountFlux = new PagedFlux<>(accountSupplier);
        PagedIterable<StorageAccount> accountList = new PagedIterable<>(accountFlux);
        given(storageAccounts.listByResourceGroup(any())).willReturn(accountList);

        given(mockStorage.listFiles(any(), any(), anyString(), anyString())).willReturn(files);

        assertDoesNotThrow(() -> service.commit(accountName, "loadingzone", containerName, USER_NAME, true, "dirname"));
    }


    @Test
    void givenCanNotWriteOrCreate_whenCommit_thenError() {
        String containerName = "container";

        assertThrows(AccessManagerException.class, () -> service.commit("testconn", "loadingzone", containerName, USER_NAME, false, "dirname"));
    }
}
