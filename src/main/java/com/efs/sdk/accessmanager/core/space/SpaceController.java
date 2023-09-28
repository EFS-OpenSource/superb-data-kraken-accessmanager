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
package com.efs.sdk.accessmanager.core.space;

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
@RequestMapping(value = SpaceController.ENDPOINT)
@RestController
@Tag(name = SpaceController.ENDPOINT)
@Deprecated(forRemoval = true)
public class SpaceController {

    /**
     * constant containing the space-endpoint
     */
    static final String ENDPOINT = "/api/v1.0/space";

    @Value("${accessmanager.organizationmanager-endpoints.space}")
    private String targetEndpoint;


    private final RestTemplate restTemplate;

    public SpaceController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Creates a space", description = "Creates a new `Space` to given `Organization` (only allowed, if user is admin or owner to the `Organization`), also calls dedicated services in order to create `Space`-context.")
    @PostMapping(path = "/{orgaId}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully created `Space`.", useReturnTypeSchema = true), @ApiResponse(responseCode = "403", description = "User does not have the required permissions.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> createSpace(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long orgaId, @RequestBody Map<String, Object> dto, @RequestHeader HttpHeaders headers) {
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(dto, headers);
        String url = String.format("%s/%d", targetEndpoint, orgaId);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.POST, request, responseType);
    }

    @Operation(summary = "Updates a Space", description = """
            Updates the given `Space` at given `Organization` (only allowed, if user is admin to the `Organization` or owner to the `Space`).
                        
            Updating `Capabilities` may lead to creating or destroying Context - so be cautious of possible information-loss!""")
    @PutMapping(path = "/{orgaId}/{spaceId}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully updated the `Space`."), @ApiResponse(responseCode = "403", description = "User does not have permissions to update the `Space`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` or `Space` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> updateSpace(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long orgaId, @PathVariable @Parameter(description = "The id of the `Space`.") long spaceId, @RequestBody Map<String, Object> dto, @RequestHeader HttpHeaders headers) {
        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(dto, headers);
        String url = String.format("%s/%d/%d", targetEndpoint, orgaId, spaceId);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
    }

    @Operation(summary = "Lists Spaces", description = """
            Lists all `Space`s the user has access to. 

            You can also specify `permissions`, then only those `Space`s are listed which the user has the appropriate permission. This feature can be used, for example, to generate a list of `Space`s to which the user is allowed to upload data.
            """)
    @GetMapping(path = "/{orgaId}", produces = "application/json")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully listed all `Space`s the user has access to."))
    public ResponseEntity<List<Map<String, Object>>> getSpaces(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long orgaId, @Parameter(description = "Name of the permissions.", schema = @Schema(type = "string", allowableValues = {"READ", "WRITE", "DELETE"}), in = ParameterIn.QUERY) @RequestParam(required = false) String permissions, @RequestHeader HttpHeaders headers) {
        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String url = String.format("%s/%d", targetEndpoint, orgaId);
        if (permissions != null) {
            String encodedPermissions = URLEncoder.encode(permissions);
            url += String.format("?permissions=%s", encodedPermissions);
        }
        ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }

    @Operation(summary = "Gets Space", description = "Gets the given `Space` if the user has access to.")
    @GetMapping(path = "/{orgaId}/{spaceId}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved `Space`-information."), @ApiResponse(responseCode = "403", description = "User does not have permissions to the `Organization` or `Space`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` or `Space` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> getSpace(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long orgaId, @PathVariable @Parameter(description = "The id of the `Space`.") long spaceId, @RequestHeader HttpHeaders headers) {
        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String url = String.format("%s/%d/%d", targetEndpoint, orgaId, spaceId);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }

    @Operation(summary = "Gets Space by name", description = "Gets `Space` by name of given `Organization` (only allowed, if user has access to `Organization` and `Space`.")
    @GetMapping(path = "/{orgaId}/name/{spaceName}", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved `Space`-information."), @ApiResponse(responseCode = "403", description = "User does not have permissions to the `Organization` or `Space`.", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "404", description = "`Organization` or `Space` was not found.", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseEntity<Map<String, Object>> getSpaceByName(@Parameter(hidden = true) JwtAuthenticationToken token, @PathVariable @Parameter(description = "The id of the `Organization`.") long orgaId, @PathVariable @Parameter(description = "The name of the `Space`.") String spaceName, @RequestHeader HttpHeaders headers) {
        final HttpEntity<String> request = new HttpEntity<>(null, headers);
        String encodedSpaceName = URLEncoder.encode(spaceName);
        String url = String.format("%s/%d/name/%s", targetEndpoint, orgaId, encodedSpaceName);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }
}
