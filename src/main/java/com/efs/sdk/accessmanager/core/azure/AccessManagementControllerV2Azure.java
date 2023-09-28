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
import com.efs.sdk.accessmanager.core.AccessManagementControllerV2;
import com.efs.sdk.accessmanager.helper.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Storage-access.
 *
 * @author e:fs TechHub GmbH
 */
@RequestMapping(value = AccessManagementControllerV2.ENDPOINT)
@RestController
@Tag(name = AccessManagementControllerV2.ENDPOINT)
@Profile("!s3")
public class AccessManagementControllerV2Azure extends AccessManagementControllerV2 {

    private static final String SPACE_LOADINGZONE = "loadingzone";

    private static final Logger LOG = LoggerFactory.getLogger(AccessManagementControllerV2Azure.class);
    /**
     * Instance of the AccessManagementService.
     */
    private final AccessManagementServiceAzure service;

    /**
     * Constructor.
     *
     * @param authHelper Instance of the Authentication utils.
     * @param service    Instance of the AccessManagementService.
     */
    public AccessManagementControllerV2Azure(AuthHelper authHelper, AccessManagementServiceAzure service) {
        super(authHelper, service);
        this.service = service;
    }

    /**
     * Create SAS-Token for downloading
     *
     * @param token        The authorization token.
     * @param organization The name of the organization.
     * @param space        The space-name
     * @return the SAS-token
     */
    @Operation(summary = "Create SAS-Token for downloading", description = "Generating Shared Access Signature-Token for read-actions, which consists of list and read permissions.")
    @PostMapping(path = "read")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully committed upload."), @ApiResponse(responseCode = "403", description = "User does not have permissions to commit upload.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<String> createReadToken(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "The name of the `Organization`.", required = true) @RequestParam String organization, @Parameter(description = "The name of the `Space`.", required = true) @RequestParam String space) throws AccessManagerException {
        boolean canRead = authHelper.isAllowed(token, organization, space, Permissions.READ);
        return ResponseEntity.ok(service.createReadToken(organization, space, canRead));
    }

    /**
     * Create SAS-Token for uploading
     *
     * @param token        The authorization token.
     * @param organization Name of the organization.
     * @param space        The space-name
     * @return the SAS-token
     */
    @Operation(summary = "Create SAS-Token for uploading", description = "Generating Shared Access Signature-Token for upload-actions to loadingzone, which expands download-actions by create and add permissions.")
    @PostMapping(path = "upload")
    public ResponseEntity<String> createUploadToken(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "The name of the `Organization`.", required = true) @RequestParam String organization, @Parameter(description = "The name of the `Space`.", required = true) @RequestParam String space) throws AccessManagerException {
        return createUploadToken(token, organization, space, SPACE_LOADINGZONE);
    }

    /**
     * Create SAS-Token for uploading
     *
     * @param token        The authorization token.
     * @param organization Name of the organization.
     * @param space        The space-name
     * @return the SAS-token
     */
    @Operation(summary = "Create SAS-Token for uploading to main storage", description = "Generating Shared Access Signature-Token for upload-actions to main storage, which expands download-actions by create and add permissions. Only viable with DELETE-Permission(!).")
    @PostMapping(path = "upload/main")
    public ResponseEntity<String> createMainStorageUploadToken(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "The name of the `Organization`.", required = true) @RequestParam String organization, @Parameter(description = "The name of the `Space`.", required = true) @RequestParam String space) throws AccessManagerException {
        return createUploadToken(token, organization, space, space);
    }

    private ResponseEntity<String> createUploadToken(JwtAuthenticationToken token, String organization, String space, String uploadSpace) throws AccessManagerException {
        // check role for desired main space
        boolean canUpload = authHelper.isAllowed(token, organization, space, Permissions.WRITE);
        // create upload-token for main storage!!
        return ResponseEntity.ok(service.createUploadToken(organization, uploadSpace, canUpload));
    }


    /**
     * Create SAS-Token for deleting
     *
     * @param token        The authorization token.
     * @param organization name of the organization.
     * @param space        The space-name
     * @return the SAS-token
     */
    @Operation(summary = "Create SAS-Token for deleting", description = "Generating Shared Access Signature-Token for delete-actions, which expands upload-actions by delete permissions.")
    @PostMapping(path = "delete")
    public ResponseEntity<String> createDeleteToken(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "The name of the `Organization`.", required = true) @RequestParam String organization, @Parameter(description = "The name of the `Space`.", required = true) @RequestParam String space) throws AccessManagerException {
        // can delete directly or from loadingzone, if user has write-permission to any of the organizations spaces
        boolean canDelete = authHelper.isAllowed(token, organization, space, Permissions.DELETE) || (space.equals(SPACE_LOADINGZONE) && authHelper.canAccessOrganization(token, organization, Permissions.WRITE));
        return ResponseEntity.ok(service.createDeleteToken(organization, space, canDelete));
    }
}
