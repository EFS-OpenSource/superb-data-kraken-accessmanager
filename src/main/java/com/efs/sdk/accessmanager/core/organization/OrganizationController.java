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
package com.efs.sdk.accessmanager.core.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Functionality moved to organizationmanager
 */
@RequestMapping(value = OrganizationController.ENDPOINT)
@RestController
@Tag(name = OrganizationController.ENDPOINT)
@Deprecated(forRemoval = true)
public class OrganizationController {
    /**
     * constant containing the controller-endpoint
     */
    static final String ENDPOINT = "/api/v1.0/organization";

    @Value("${accessmanager.organizationmanager-endpoints.organization}")
    private String targetEndpoint;

    private final RestTemplate restTemplate;

    public OrganizationController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Create a new Organization", description = "Create a new `Organization`, also calls dedicated services in order to create `Organization`-context. This feature is only applicable for users with the role 'org_create_permission'.")
    @PostMapping(produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully created `Organization`.", useReturnTypeSchema = true), @ApiResponse(responseCode = "403", description = "User does not have the required permissions.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> createOrganization(@Parameter(hidden = true) JwtAuthenticationToken token, @RequestBody Map<String, Object> dto, @RequestHeader HttpHeaders headers) {
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(dto, headers);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(targetEndpoint, HttpMethod.POST, request, responseType);
    }

    @Operation(summary = "Updates an Organization", description = "Updates the given `Organization` if the user has appropriate permissions.")
    @PutMapping(path = "/{id}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully updated the `Organization`."), @ApiResponse(responseCode = "403", description = "User does not have permissions to update the `Organization`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> updateOrganization(@Parameter(hidden = true) JwtAuthenticationToken token, @RequestBody Map<String, Object> dto, @PathVariable @Parameter(description = "The id of the `Organization`.") long id, @RequestHeader HttpHeaders headers) {

        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(dto, headers);
        String url = String.format("%s/%d", targetEndpoint, id);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
    }

    @Operation(summary = "Lists all Organizations", description = """
            Lists all `Organization`s the user has access to. 
                        
            You can also specify `permissions`, then only those `Organization`s are listed that contain a `Space` to which the user has the appropriate permission. This feature can be used, for example, to generate a list of `Organization`s to which the user is allowed to upload data.
            """)
    @GetMapping(produces = "application/json")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully listed all `Organization`s the user has access to."))
    public ResponseEntity<List<Map<String, Object>>> getAllOrganizations(@Parameter(hidden = true) JwtAuthenticationToken token, @Parameter(description = "Name of the permissions.", schema = @Schema(type = "string", allowableValues = {"READ", "WRITE", "DELETE"}), in = ParameterIn.QUERY) String permissions, @RequestHeader HttpHeaders headers) {

        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String url = targetEndpoint;
        if (permissions != null) {
            String encodedPermissions = URLEncoder.encode(permissions);
            url += String.format("?permissions=%s", encodedPermissions);
        }

        ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }

    @Operation(summary = "Gets Organization by id", description = "Gets the given `Organization` if the user has access to.")
    @GetMapping(path = "/{id}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved `Organization`-information."), @ApiResponse(responseCode = "403", description = "User does not have permissions to the `Organization`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> getOrganization(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long id, @RequestHeader HttpHeaders headers) {

        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String url = String.format("%s/%d", targetEndpoint, id);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }

    @Operation(summary = "Gets Organization by name", description = "Gets the given `Organization` if the user has access to.")
    @GetMapping(path = "/name/{name}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved `Organization`-information."), @ApiResponse(responseCode = "403", description = "User does not have permissions to the `Organization`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> getOrganizationByName(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The name of the `Organization`.") String name, @RequestHeader HttpHeaders headers) {

        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String encodedName = URLEncoder.encode(name);
        String url = String.format("%s/name/%s", targetEndpoint, encodedName);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }
}
