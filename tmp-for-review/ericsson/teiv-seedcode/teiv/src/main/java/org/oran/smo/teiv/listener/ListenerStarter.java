/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Ericsson
 *  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.oran.smo.teiv.listener;

import java.text.MessageFormat;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.config.KafkaConfig;
import org.oran.smo.teiv.service.kafka.KafkaTopicService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
@Profile("ingestion")
public class ListenerStarter {

    private KafkaListenerEndpointRegistry registry;

    private KafkaConfig kafkaConfig;

    private KafkaTopicService kafkaTopicService;

    //spotless:off
    public void startKafkaListeners() {
        startKafkaListener(kafkaTopicService.checkTopologyIngestionTopic(), kafkaConfig.getTopologyIngestion().getGroupId());
    }
    //spotless:on

    private void startKafkaListener(boolean isTopicCreated, String groupId) {
        if (isTopicCreated) {
            log.info("Starting Kafka Listener. GroupId: {}", groupId);
            MessageListenerContainer messageListenerContainer = registry.getListenerContainer(groupId);

            if (Objects.nonNull(messageListenerContainer) && !messageListenerContainer
                    .isAutoStartup() && !messageListenerContainer.isRunning()) {
                messageListenerContainer.start();
                log.info("Kafka Listener Started. {}", groupId);
            }
        } else {
            log.error(MessageFormat.format("Failed to start topology listener with {0} groupId", groupId));
        }
    }

}
