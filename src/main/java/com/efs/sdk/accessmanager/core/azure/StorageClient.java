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
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR;
import com.efs.sdk.accessmanager.core.azure.model.SASToken;
import com.efs.sdk.accessmanager.core.model.StorageTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType.READ;
import static com.efs.sdk.accessmanager.core.azure.model.SASToken.SASType.WRITE;
import static java.lang.String.format;

/**
 * Client for handling Storage-Data.
 *
 * @author e:fs TechHub GmbH
 */
@Component
@Profile("!s3")
public class StorageClient {

    /**
     * Instance of the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StorageClient.class);

    /**
     * Map for caching the BlobContainerClients by ContainerModelBase
     */
    private final Map<StorageTarget, BlobContainerClient> containerClients = new HashMap<>();

    @Value("${sas.token.expiration.read}")
    private int readExpiration;

    @Value("${sas.token.expiration.write}")
    private int writeExpiration;

    @Value("${sas.token.expiration.delete}")
    private int deleteExpiration;


    /**
     * Creates a Shared Access Signature-Token for updating files within the given container
     *
     * @param container The ContainerModelBase
     * @return The Shared Access Signature-Token for updating files
     */
    public SASToken createUploadToken(StorageAccount storageAccount, StorageTarget container) throws AccessManagerException {
        LOG.info("Creating upload-token for {}-{}", container.organization(), container.space());
        ensureContainerExist(storageAccount, container);

        BlobContainerClient containerClient = createContainerClient(storageAccount, container);

        LOG.info("Creating upload-token - Done!");
        String token = containerClient.generateSas(generateWriteSignature(writeExpiration));
        return new SASToken(WRITE, container.organization(), container.space(), token);
    }

    /**
     * Creates a Shared Access Signature-Token for reading files within the given container
     *
     * @param container The ContainerModelBase
     * @return The Shared Access Signature-Token for reading files
     */
    public SASToken createReadToken(StorageAccount storageAccount, StorageTarget container) throws AccessManagerException {
        LOG.info("Creating read-token for {}-{}", container.organization(), container.space());
        ensureContainerExist(storageAccount, container);

        BlobContainerClient containerClient = createContainerClient(storageAccount, container);

        LOG.info("Creating read-token - Done!");
        String token = containerClient.generateSas(generateReadSignature(readExpiration));
        return new SASToken(READ, container.organization(), container.space(), token);
    }

    /**
     * Creates a Shared Access Signature-Token for deleting files within the given container
     *
     * @param container The ContainerModelBase
     * @return The Shared Access Signature-Token for deleting files
     */
    public SASToken createDeleteToken(StorageAccount storageAccount, StorageTarget container) throws AccessManagerException {
        LOG.info("Creating delete-token for {}-{}", container.organization(), container.space());
        ensureContainerExist(storageAccount, container);
        BlobContainerClient containerClient = createContainerClient(storageAccount, container);

        LOG.info("Creating delete-token - Done!");
        String token = containerClient.generateSas(generateDeleteSignature(deleteExpiration));
        return new SASToken(READ, container.organization(), container.space(), token);
    }


    /**
     * Ensures the container exists in the cloud.
     *
     * @param container The container to check.
     */
    private void ensureContainerExist(StorageAccount storageAccount, StorageTarget container) throws AccessManagerException {
        LOG.info("Ensuring that container exists {}-{}", container.organization(), container.space());
        BlobContainerClient containerClient = createContainerClient(storageAccount, container);

        if (!containerClient.exists()) {
            throw new AccessManagerException(ACCESSMANAGER_ERROR.CONTAINER_NOT_EXISTS);
        }
    }


    /**
     * Creates the Azure Storage Container Client for interacting with Azure Blob storage.
     *
     * @param container The name of the container.
     * @return The created {@link BlobContainerClient}.
     */
    private BlobContainerClient createContainerClient(StorageAccount storageAccount, StorageTarget container) throws AccessManagerException {
        if (!containerClients.containsKey(container)) {
            String connectionStr = getConnectionString(storageAccount);
            BlobServiceClient serviceClient = new BlobServiceClientBuilder().connectionString(connectionStr).buildClient();
            containerClients.put(container, serviceClient.getBlobContainerClient(container.space()));
        }
        return containerClients.get(container);
    }

    /**
     * Creates BlobServiceSasSignatureValues with read permission, that is valid for the given period of time (in minutes).
     *
     * @param minutes Time the BlobServiceSasSignatureValues remain valid in minutes
     * @return BlobServiceSasSignatureValues
     */
    private BlobServiceSasSignatureValues generateReadSignature(long minutes) {
        BlobContainerSasPermission permission = getReadPermissions();
        return generateSignature(permission, minutes);
    }

    /**
     * Creates a BlobContainerSasPermission with list-rights
     *
     * @return BlobContainerSasPermission with list-rights
     */
    private BlobContainerSasPermission getListPermissions() {
        return new BlobContainerSasPermission().setListPermission(true);
    }

    /**
     * Creates a BlobContainerSasPermission which expands list-rights by read-rights
     *
     * @return BlobContainerSasPermission with list-rights
     */
    private BlobContainerSasPermission getReadPermissions() {
        return getListPermissions().setReadPermission(true);
    }

    /**
     * Creates BlobServiceSasSignatureValues with write permission, that is valid for the given period of time (in minutes).
     *
     * @param minutes Time the BlobServiceSasSignatureValues remain valid in minutes
     * @return BlobServiceSasSignatureValues
     */
    private BlobServiceSasSignatureValues generateWriteSignature(long minutes) {
        BlobContainerSasPermission permission = getUploadPermissions();
        return generateSignature(permission, minutes);
    }

    /**
     * Creates a BlobContainerSasPermission which expands read-rights by create- and write-rights
     *
     * @return BlobContainerSasPermission with list-rights
     */
    private BlobContainerSasPermission getUploadPermissions() {
        return getReadPermissions().setAddPermission(true).setCreatePermission(true).setWritePermission(true);
    }

    /**
     * Creates BlobServiceSasSignatureValues with delete permission, that is valid for the given period of time (in minutes).
     *
     * @param minutes Time the BlobServiceSasSignatureValues remain valid in minutes
     * @return BlobServiceSasSignatureValues
     */
    private BlobServiceSasSignatureValues generateDeleteSignature(long minutes) {
        BlobContainerSasPermission permission = getDeletePermissions();
        return generateSignature(permission, minutes);
    }

    /**
     * Creates a BlobContainerSasPermission which expands upload-rights by delete-rights
     *
     * @return BlobContainerSasPermission with list-rights
     */
    private BlobContainerSasPermission getDeletePermissions() {
        return getUploadPermissions().setDeletePermission(true);
    }

    /**
     * Creates BlobServiceSasSignatureValues with the given permissions, that is valid for the given period of time (in minutes).
     *
     * @param permission The BlobContainerSasPermission
     * @param minutes    Time the BlobServiceSasSignatureValues remain valid in minutes
     * @return BlobServiceSasSignatureValues
     */
    private BlobServiceSasSignatureValues generateSignature(BlobContainerSasPermission permission, long minutes) {
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(minutes);
        return new BlobServiceSasSignatureValues(expiryTime, permission).setStartTime(OffsetDateTime.now());
    }

    /**
     * Lists files within the given container matching the given filePattern
     *
     * @param container   The ContainerModelBase
     * @param filePattern the file-pattern
     * @param rootDir     the root-dir
     * @return files
     */
    public Set<String> listFiles(StorageAccount storageAccount, StorageTarget container, String filePattern, String rootDir) throws AccessManagerException {
        Pattern pattern = Pattern.compile(filePattern);
        BlobContainerClient containerClient = createContainerClient(storageAccount, container);
        //v1
        if (rootDir == null || rootDir.isEmpty()) {
            PagedIterable<BlobItem> blobItems = containerClient.listBlobs();
            return blobItems.stream().filter(bi -> {
                Matcher m = pattern.matcher(bi.getName());
                return m.matches();
            }).map(BlobItem::getName).collect(Collectors.toSet());
        }
        //v2
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(rootDir + "/");
        PagedIterable<BlobItem> blobItems = containerClient.listBlobs(options, Duration.ofMinutes(5L));
        return blobItems.stream().map(BlobItem::getName).collect(Collectors.toSet());
    }

    private String getConnectionString(StorageAccount storageAccount) throws AccessManagerException {
        List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
        Optional<StorageAccountKey> first = storageAccountKeys.stream().findFirst();
        if (first.isPresent()) {
            StorageAccountKey key = first.get();
            return format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net", storageAccount.name(), key.value());
        }
        throw new AccessManagerException("no connection-key found!");
    }

}
