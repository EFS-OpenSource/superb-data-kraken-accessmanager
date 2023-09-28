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
package com.efs.sdk.accessmanager.commons;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for errors in the access-manager service.
 *
 * @author e:fs TechHub GmbH
 */
public class AccessManagerException extends Exception {

    private final HttpStatus httpStatus;
    private final int errorCode;
    private String message;

    public AccessManagerException(ACCESSMANAGER_ERROR error) {
        super(error.code + ": " + error.msg);
        httpStatus = error.status;
        errorCode = error.code;
    }

    public AccessManagerException(ACCESSMANAGER_ERROR error, String additionalMessage) {
        super(error.code + ": " + error.msg + " " + additionalMessage);
        httpStatus = error.status;
        errorCode = error.code;
    }

    public AccessManagerException(String message) {
        this(ACCESSMANAGER_ERROR.UNKNOWN_ERROR, message);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Provides the errors to the application.
     *
     * @author e:fs TechHub GmbH
     */
    public enum ACCESSMANAGER_ERROR {
        // Save file errors
        SAVE_ACCESS_DENIED(10001, HttpStatus.FORBIDDEN, "Missing permission to save file."),

        // Read file errors
        READ_ACCESS_DENIED(10011, HttpStatus.FORBIDDEN, "Missing permission to download file."),

        // Delete file errors
        DELETE_ACCESS_DENIED(10021, HttpStatus.FORBIDDEN, "Missing permission to delete file."),

        //commit errors
        UNABLE_COMMIT_TRANSACTION(10031, HttpStatus.FORBIDDEN, "Missing permission to commit transaction."),

        // Unknown connection id
        UNABLE_FIND_ACCOUNT(10041, HttpStatus.NOT_FOUND, "Unable to find the given Storage Account."),

        // Unknown connection id
        CONTAINER_NOT_EXISTS(10051, HttpStatus.BAD_REQUEST, "The requested container does not exist!"),

        // Unknown organization or space
        ORGANIZATION_NOT_FOUND(10061, HttpStatus.NOT_FOUND, "Unable to find the given organization."),

        // Error communicating with organizationmanager
        ORGANIZATIONMANAGER_ERROR(10071, HttpStatus.BAD_REQUEST, "Error communicating with organizationmanager."),

        UNKNOWN_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "something unexpected happened.");

        private final int code;
        private final HttpStatus status;
        private final String msg;

        ACCESSMANAGER_ERROR(int code, HttpStatus status, String msg) {
            this.code = code;
            this.status = status;
            this.msg = msg;
        }

    }

    public int getErrorCode() {
        return errorCode;
    }

}
