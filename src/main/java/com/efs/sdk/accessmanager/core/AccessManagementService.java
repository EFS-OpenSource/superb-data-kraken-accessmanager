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

import com.efs.sdk.accessmanager.commons.AccessManagerException;
import com.efs.sdk.accessmanager.core.events.EventPublisher;
import com.efs.sdk.accessmanager.core.model.CommitModel;
import com.efs.sdk.logging.AuditLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import static com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR.SAVE_ACCESS_DENIED;
import static com.efs.sdk.accessmanager.commons.AccessManagerException.ACCESSMANAGER_ERROR.UNABLE_COMMIT_TRANSACTION;

public class AccessManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(AccessManagementService.class);

    /**
     * Instance of the EventPublisher
     */
    public final EventPublisher publisher;

    public final ObjectMapper objectMapper;

    /**
     * Name of the Kafka-Topic
     */
    @Value("${accessmanager.topic.upload-complete}")
    public String topicName;

    public AccessManagementService(ObjectMapper objectMapper, EventPublisher publisher) {
        this.objectMapper = objectMapper;
        this.publisher = publisher;
    }

    public Void commit(String organization, String uploadStorage, String mainStorage, String userName, boolean canWrite, String rootDir) throws AccessManagerException {
        AuditLogger.info(LOG, "Requesting the file upload transaction for organization {} to the storagelocation {} that is supposed to be processed to end up in {}", userName, organization, uploadStorage, mainStorage);
        if (!canWrite) {
            AuditLogger.warning(LOG, "User does not have the permission to trigger commit.", userName);
            throw new AccessManagerException(SAVE_ACCESS_DENIED);
        }
        try {
            CommitModel commitModel = new CommitModel(organization, mainStorage, uploadStorage, userName, rootDir);
            String containerJson = objectMapper.writeValueAsString(commitModel);
            publisher.sendMessage(containerJson, topicName);
            return null;
        } catch (IOException e) {
            throw new AccessManagerException(UNABLE_COMMIT_TRANSACTION);
        }
    }
}
