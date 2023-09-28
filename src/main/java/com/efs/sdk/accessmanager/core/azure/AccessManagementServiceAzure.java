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

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.efs.sdk.accessmanager.clients.AzureResourceManagerProvider;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.core.AccessManagementService;
import com.efs.sdk.accessmanager.core.azure.model.SASToken;
import com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType;
import com.efs.sdk.accessmanager.core.events.EventPublisher;
import com.efs.sdk.accessmanager.core.model.StorageTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR.*;
import static com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType.*;

/**
 * Service for managing Storage-access.
 *
 * @author e:fs TechHub GmbH
 */
@Service
@Profile("!s3")
public class AccessManagementServiceAzure extends AccessManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessManagementServiceAzure.class);

    /**
     * Instance of the StorageClient
     */
    private final StorageClient storageClient;

    private final List<SASToken> sasModels = new ArrayList<>();

    @Value("${accessmanager.storage.resourcegroup:SDK}")
    private String resourceGroup;
    @Value("${sas.token.cache.buffer}")
    private int cacheBuffer;
    private final AzureResourceManagerProvider azureProvider;

    /**
     * Constructor.
     *
     * @param storageClient The StorageClient
     * @param publisher     The EventPublisher
     */
    public AccessManagementServiceAzure(StorageClient storageClient, ObjectMapper objectMapper, EventPublisher publisher, AzureResourceManagerProvider azureProvider) {
        super(objectMapper, publisher);
        this.storageClient = storageClient;
        this.azureProvider = azureProvider;
    }

    /**
     * Creates a read-token
     *
     * @param organization the organization.
     * @param space        The name of the container.
     * @param canRead      whether the user has the right to read from the storage
     * @return the read-token
     * @throws AccessManagerException thrown on missing rights
     */
    public String createReadToken(String organization, String space, boolean canRead) throws AccessManagerException {
        LOG.info("Requesting a read-token for space {} in organization {}", space, organization);
        if (!canRead) {
            throw new AccessManagerException(READ_ACCESS_DENIED);
        }
        Optional<String> storedToken = getTokenFromCache(READ, organization, space);
        if (storedToken.isPresent()) {
            return storedToken.get();
        }

        StorageTarget container = new StorageTarget(organization, space);
        Optional<StorageAccount> accountOpt = getStorageAccount(organization);
        if (accountOpt.isEmpty()) {
            throw new AccessManagerException(UNABLE_FIND_ACCOUNT);
        }
        StorageAccount account = accountOpt.get();
        SASToken sasToken = storageClient.createReadToken(account, container);
        sasModels.add(sasToken);
        return sasToken.token();
    }

    private Optional<StorageAccount> getStorageAccount(String organization) {
        AzureResourceManager azure = azureProvider.azure();
        StorageAccounts storageAccountsImpl = azure.storageAccounts();
        PagedIterable<StorageAccount> storageAccounts = storageAccountsImpl.listByResourceGroup(resourceGroup);
        return storageAccounts.stream().filter(a -> a.name().equalsIgnoreCase(organization)).findFirst();
    }

    /**
     * Creates a delete-token
     *
     * @param organization the organization.
     * @param space        The name of the container.
     * @param canDelete    whether the user has the right to delete from the storage
     * @return the delete-token
     * @throws AccessManagerException thrown on missing rights
     */
    public String createDeleteToken(String organization, String space, boolean canDelete) throws AccessManagerException {
        LOG.info("Requesting a delete-token for space {} in organization {}", space, organization);

        if (!canDelete) {
            throw new AccessManagerException(DELETE_ACCESS_DENIED);
        }
        Optional<String> storedToken = getTokenFromCache(DELETE, organization, space);
        if (storedToken.isPresent()) {
            return storedToken.get();
        }

        StorageTarget container = new StorageTarget(organization, space);
        Optional<StorageAccount> accountOpt = getStorageAccount(organization);
        if (accountOpt.isEmpty()) {
            throw new AccessManagerException(UNABLE_FIND_ACCOUNT);
        }
        StorageAccount account = accountOpt.get();
        SASToken sasToken = storageClient.createDeleteToken(account, container);
        sasModels.add(sasToken);
        return sasToken.token();
    }

    /**
     * Creates a upload-token
     *
     * @param organization the organization.
     * @param space        The name of the container.
     * @param canWrite     whether the user has the right to upload to the storage
     * @return the upload-token
     * @throws AccessManagerException thrown on missing rights
     */
    public String createUploadToken(String organization, String space, boolean canWrite) throws AccessManagerException {
        LOG.info("Requesting a upload-token for space {} in organization {}", space, organization);

        if (!canWrite) {
            throw new AccessManagerException(SAVE_ACCESS_DENIED);
        }
        Optional<String> storedToken = getTokenFromCache(WRITE, organization, space);
        if (storedToken.isPresent()) {
            return storedToken.get();
        }

        StorageTarget container = new StorageTarget(organization, space);
        Optional<StorageAccount> accountOpt = getStorageAccount(organization);
        if (accountOpt.isEmpty()) {
            throw new AccessManagerException(UNABLE_FIND_ACCOUNT);
        }
        StorageAccount account = accountOpt.get();
        SASToken sasToken = storageClient.createUploadToken(account, container);
        sasModels.add(sasToken);
        return sasToken.token();
    }

    private Optional<String> getTokenFromCache(SASType type, String organization, String space) {
        Optional<SASToken> sasTokenOpt = findToken(type, organization, space);
        if (sasTokenOpt.isPresent() && sasTokenOpt.get().isValid(cacheBuffer)) {
            return Optional.of(sasTokenOpt.get().token());
        }
        return Optional.empty();
    }

    private Optional<SASToken> findToken(SASType type, String orgaName, String spaceName) {
        return sasModels.stream().filter(model -> model.organization().equalsIgnoreCase(orgaName) && model.space().equalsIgnoreCase(spaceName) && type == model.type()).findFirst();
    }

    /**
     * Clear token-cache every 10 seconds to prevent it from being cluttered by outdated tokens (e.g. tokens from
     * LOCKED spaces)
     */
    @Scheduled(fixedRate = 10_000L)
    public void clearTokenCache() {
        sasModels.removeIf(sas -> !sas.isValid(cacheBuffer));
    }
}
