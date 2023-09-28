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
package com.efs.sdk.accessmanager.helper;

import com.efs.sdk.accessmanager.clients.OrganizationManagerClient;
import com.efs.sdk.accessmanager.clients.OrganizationManagerClient.Permissions;
import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR;
import com.efs.sdk.logging.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Helper class for extracting values from JWT tokens.
 *
 * @author e:fs TechHub GmbH
 */
@Component
public class AuthHelper {

    private static final String SPACE_LOADINGZONE = "loadingzone";

    private static final Logger LOG = LoggerFactory.getLogger(AuthHelper.class);

    private final OrganizationManagerClient organizationManagerClient;

    public AuthHelper(OrganizationManagerClient organizationManagerClient) {
        this.organizationManagerClient = organizationManagerClient;
    }

    /**
     * Checks if the calling user is allowed to perform the requested operation.
     *
     * @param token        The user token.
     * @param organization The connection id.
     * @param space        The container name.
     * @param permissions  The requested permissions.
     * @return If operation is allowed or not.
     */
    public boolean isAllowed(JwtAuthenticationToken token, String organization, String space, Permissions permissions) throws AccessManagerException {
        try {
            // special case: loadingzone is no real space and filtered out when listing spaces
            // user has read access to loadingzone, if write access to any other space within organization
            if (space.equalsIgnoreCase(SPACE_LOADINGZONE) && permissions == Permissions.READ) {
                return canAccessOrganization(token, organization, Permissions.WRITE);
            }
            String username = getUserName(token);

            LOG.debug("Fetching organization {} for user {}", organization, username);
            Long orgaId = getLongProperty(organizationManagerClient.getOrganization(token, organization), "id");
            LOG.debug("Fetching spaces of the organization with id {} for user {} with {} permissions", orgaId, username, permissions);
            List<Map<String, Object>> spaces = organizationManagerClient.getSpaces(token, orgaId, permissions);

            // if user can list the space with requested permissions, then he is allowed to do the operation
            AuditLogger.info(LOG, "Checking if user has {} permissions on space {}", username, permissions, space);
            return spaces.stream().anyMatch(s -> getStringProperty(s, "name").equalsIgnoreCase(space));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                AuditLogger.info(LOG, "User has no {} permissions on space {}", getUserName(token), permissions, space);
                return false;
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATION_NOT_FOUND);
            } else {
                throw new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATIONMANAGER_ERROR, e.getMessage());
            }
        }
    }


    private Object getProperty(Map<String, Object> objectMap, String property) {
        if (!objectMap.containsKey(property)) {
            throw new IllegalArgumentException(format("unable to find '%s' in object '%s'", property, objectMap.keySet().stream().map(key -> key + "=" + objectMap.get(key)).collect(joining(", ", "{", "}"))));
        }
        return objectMap.get(property);
    }

    private String getStringProperty(Map<String, Object> objectMap, String property) {
        Object o = getProperty(objectMap, property);
        return String.valueOf(o);
    }

    private Long getLongProperty(Map<String, Object> objectMap, String property) {
        Object o = getProperty(objectMap, property);
        return castLong(o);
    }

    private Long castLong(Object o) {
        Long value = null;
        if (o != null) {
            value = Long.parseLong(o.toString());
        }
        return value;
    }

    /**
     * returns the user name from the jwt;
     *
     * @param token the jwt (as JwtAuthenticationToken)
     * @return the preferred username from the jwt or null if it does not exist
     */
    public String getUserName(JwtAuthenticationToken token) {
        return token.getToken().getClaimAsString("preferred_username");
    }

    /**
     * Check, if user has given access to any space within the given organization
     *
     * @param token        the jwt (as JwtAuthenticationToken)
     * @param organization The name of the organization
     * @param permissions  The given permissions
     * @return whether user has access to organization
     */
    public boolean canAccessOrganization(JwtAuthenticationToken token, String organization, Permissions permissions) throws AccessManagerException {
        try {
            String username = getUserName(token);
            LOG.debug("Fetching organization {} for user {}", organization, username);
            Long orgaId = getLongProperty(organizationManagerClient.getOrganization(token, organization), "id");
            LOG.debug("Fetching spaces of the organization with id {} for user {} with {} permissions", orgaId, username, permissions);
            List<Map<String, Object>> spaces = organizationManagerClient.getSpaces(token, orgaId, permissions);

            // if user gets any space within the organization for requested permissions, then he is allowed to access the organiztaion
            AuditLogger.info(LOG, "Checking if user has {} permissions on any space of the organization {}", username, permissions, organization);
            return !spaces.isEmpty();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                AuditLogger.info(LOG, "User has no {} permissions on any space of the organization {}", getUserName(token), permissions, organization);
                return false;
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATION_NOT_FOUND);
            } else {
                throw new AccessManagerException(ACCESSMANAGER_ERROR.ORGANIZATIONMANAGER_ERROR, e.getMessage());
            }
        }
    }
}
