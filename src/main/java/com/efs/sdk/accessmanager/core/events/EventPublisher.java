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
package com.efs.sdk.accessmanager.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Service for Event-publishing
 *
 * @author e:fs TechHub GmbH
 */
@Service
public class EventPublisher {

    /**
     * Instance of the logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);

    /**
     * Instance of the KafkaTemplate
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TaskExecutor taskExecutor;

    /**
     * Constructor.
     *
     * @param kafkaTemplate The KafkaTemplate
     * @param taskExecutor  The TaskExecutor
     */
    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate, TaskExecutor taskExecutor) {
        this.kafkaTemplate = kafkaTemplate;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Sends the message via StringSerializer
     *
     * @param message   The Event-Message
     * @param topicName The name of the Kafka-topic
     */
    public void sendMessage(String message, String topicName) {
        this.taskExecutor.execute(new EventTask(kafkaTemplate, topicName, message));
    }

    /**
     * Runnable for publishing event
     */
    private record EventTask(KafkaTemplate<String, String> kafkaTemplate, String topicName,
                             String message) implements Runnable {

        /**
         * Publish message
         */
        @Override
        public void run() {

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);
            LOG.debug("sending commit-message {}", message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    LOG.debug("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
                } else {
                    LOG.error("Unable to send message=[{}] due to : {}", message, ex.getMessage(), ex);
                }
            });
        }
    }
}