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
package com.efs.sdk.accessmanager.mock;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.azure.resourcemanager.storage.models.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record StorageAccountMock(String name) implements StorageAccount {

    @Override
    public AccountStatuses accountStatuses() {
        return null;
    }

    @Override
    public StorageAccountSkuType skuType() {
        return null;
    }

    @Override
    public Kind kind() {
        return null;
    }

    @Override
    public OffsetDateTime creationTime() {
        return null;
    }

    @Override
    public CustomDomain customDomain() {
        return null;
    }

    @Override
    public OffsetDateTime lastGeoFailoverTime() {
        return null;
    }

    @Override
    public ProvisioningState provisioningState() {
        return null;
    }

    @Override
    public PublicEndpoints endPoints() {
        return null;
    }

    @Override
    public StorageAccountEncryptionKeySource encryptionKeySource() {
        return null;
    }

    @Override
    public Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses() {
        return null;
    }

    @Override
    public boolean infrastructureEncryptionEnabled() {
        return false;
    }

    @Override
    public AccessTier accessTier() {
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        return null;
    }

    @Override
    public boolean isAccessAllowedFromAllNetworks() {
        return false;
    }

    @Override
    public List<String> networkSubnetsWithAccess() {
        return null;
    }

    @Override
    public List<String> ipAddressesWithAccess() {
        return null;
    }

    @Override
    public List<String> ipAddressRangesWithAccess() {
        return null;
    }

    @Override
    public boolean canReadLogEntriesFromAnyNetwork() {
        return false;
    }

    @Override
    public boolean canReadMetricsFromAnyNetwork() {
        return false;
    }

    @Override
    public boolean canAccessFromAzureServices() {
        return false;
    }

    @Override
    public boolean isAzureFilesAadIntegrationEnabled() {
        return false;
    }

    @Override
    public boolean isHnsEnabled() {
        return false;
    }

    @Override
    public boolean isLargeFileSharesEnabled() {
        return false;
    }

    @Override
    public MinimumTlsVersion minimumTlsVersion() {
        return null;
    }

    @Override
    public boolean isHttpsTrafficOnly() {
        return false;
    }

    @Override
    public boolean isBlobPublicAccessAllowed() {
        return false;
    }

    @Override
    public boolean isSharedKeyAccessAllowed() {
        return false;
    }

    @Override
    public List<StorageAccountKey> getKeys() {
        return null;
    }

    @Override
    public Mono<List<StorageAccountKey>> getKeysAsync() {
        return null;
    }

    @Override
    public List<StorageAccountKey> regenerateKey(String s) {
        return null;
    }

    @Override
    public Mono<List<StorageAccountKey>> regenerateKeyAsync(String s) {
        return null;
    }

    @Override
    public StorageManager manager() {
        return null;
    }

    @Override
    public String resourceGroupName() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public String regionName() {
        return null;
    }

    @Override
    public Region region() {
        return null;
    }

    @Override
    public Map<String, String> tags() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return null;
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        return null;
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return null;
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        return null;
    }

    @Override
    public void approvePrivateEndpointConnection(String s) {

    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String s) {
        return null;
    }

    @Override
    public void rejectPrivateEndpointConnection(String s) {

    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String s) {
        return null;
    }

    @Override
    public StorageAccountInner innerModel() {
        return null;
    }

    @Override
    public String key() {
        return null;
    }

    @Override
    public StorageAccount refresh() {
        return null;
    }

    @Override
    public Mono<StorageAccount> refreshAsync() {
        return null;
    }

    @Override
    public Update update() {
        return null;
    }
}
