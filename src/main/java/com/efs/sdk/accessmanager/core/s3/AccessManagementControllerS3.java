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
package com.efs.sdk.accessmanager.core.s3;

import com.efs.sdk.accessmanager.core.AccessManagementControllerV2;
import com.efs.sdk.accessmanager.helper.AuthHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default-Implementation of AccessManagementControllerV2 (required for dependency-injection to work)
 */
@RequestMapping(value = AccessManagementControllerV2.ENDPOINT)
@RestController
@Tag(name = AccessManagementControllerV2.ENDPOINT)
@Profile({"s3", "test-s3"})
public class AccessManagementControllerS3 extends AccessManagementControllerV2 {

    /**
     * Constructor.
     *
     * @param authHelper Instance of the Authentication utils.
     * @param service    Instance of the AccessManagementService.
     */
    public AccessManagementControllerS3(AuthHelper authHelper, AccessManagementServiceS3 service) {
        super(authHelper, service);
    }
}
