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
package com.efs.sdk.accessmanager;

import com.efs.sdk.accessmanager.security.oauth.OAuth2Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for the accessmanager backend application.
 *
 * @author e:fs TechHub GmbH
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(OAuth2Properties.class)
public class AccessManagerApplication {

    /**
     * Main method.
     *
     * @param args Arguments for the call.
     */
    public static void main(String[] args) {
        SpringApplication.run(AccessManagerApplication.class, args);
    }

    /**
     * Creates an instance of the ObjectMapper.
     *
     * @return		The created {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Creates an instance of the RestTemplate.
     *
     * @return		The created {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
