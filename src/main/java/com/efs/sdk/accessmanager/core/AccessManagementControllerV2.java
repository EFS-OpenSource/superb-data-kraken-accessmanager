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
package com.efs.sdk.accessmanager.core;


import com.efs.sdk.accessmanager.clients.OrganizationManagerClient;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.helper.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class AccessManagementControllerV2 {

    /**
     * constant containing the controller-endpoint
     */
    public static final String ENDPOINT = "/api/v2.0/accessmanager";

    private static final String SPACE_LOADINGZONE = "loadingzone";

    private static final Logger LOG = LoggerFactory.getLogger(AccessManagementControllerV2.class);
    /**
     * Instance of the Authentication utils.
     */
    public final AuthHelper authHelper;
    /**
     * Instance of the AccessManagementService.
     */
    private final AccessManagementService service;

    /**
     * Constructor.
     *
     * @param authHelper Instance of the Authentication utils.
     * @param service    Instance of the AccessManagementService.
     */
    public AccessManagementControllerV2(AuthHelper authHelper, AccessManagementService service) {
        this.authHelper = authHelper;
        this.service = service;
    }

    /**
     * Commit File-Transaction
     *
     * @param token        The authorization token.
     * @param organization name of the organization.
     * @param space        The space-name
     * @param rootDir      The root directory
     * @return files within the root directory in the storage-container
     */
    @Operation(summary = "Commit File-Transaction", description = "Signalizes, that all file-transaction is complete and notifies further processing.")
    @PostMapping(path = "commit", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Successfully committed upload."), @ApiResponse(responseCode = "403", description = "User does not have permissions to commit upload.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Void> commit(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "The name of the `Organization`.", required = true) @RequestParam String organization, @Parameter(description = "The name of the `Space`", required = true) @RequestParam String space, @Parameter(description = "Directory of this transaction", required = true) @RequestParam(defaultValue = "none") String rootDir) throws AccessManagerException {
        LOG.info("committing dataset {}/{}/{}...", organization, space, rootDir);
        boolean canWrite = authHelper.isAllowed(token, organization, space, OrganizationManagerClient.Permissions.WRITE);
        String userName = authHelper.getUserName(token);

        service.commit(organization, SPACE_LOADINGZONE, space, userName, canWrite, rootDir);
        LOG.info("committing dataset {}/{}/{}...done", organization, space, rootDir);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
