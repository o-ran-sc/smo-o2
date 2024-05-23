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
package org.oran.smo.teiv.service.kafka;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.TopicConfig;
import org.oran.smo.teiv.config.KafkaAdminConfig;
import org.oran.smo.teiv.config.KafkaConfig;
import org.oran.smo.teiv.utils.RetryOperationUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@Slf4j
@Profile("ingestion")
public class KafkaTopicService {

    private final KafkaAdminConfig kafkaAdminConfig;

    @Getter
    private final KafkaAdmin kafkaAdmin;

    @Getter
    private final KafkaConfig kafkaConfig;

    public boolean checkTopologyIngestionTopic() {
        return checkTopicCreated(kafkaConfig.getTopologyIngestion().getTopicName());
    }

    public boolean isTopicCreated(String topicName) {
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> existingTopics = client.listTopics().names().get();
            if (!existingTopics.contains(topicName)) {
                log.info("Topic does not exist: {}", topicName);
                throw new KafkaException("Topic does not exist");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error checking if topic exists: {}", topicName, e);
            Thread.currentThread().interrupt();
            throw new KafkaException("Error checking if topic exists: " + topicName);
        }
        return true;
    }

    public boolean checkTopicCreated(String topicName) {
        RetryTemplate retryTemplate = RetryOperationUtils.getRetryTemplate("kafkaListener", KafkaException.class,
                kafkaAdminConfig.getRetries(), kafkaAdminConfig.getRetryBackoffMs());
        boolean result = false;
        try {
            result = retryTemplate.execute(retryContext -> isTopicCreated(topicName));
        } catch (KafkaException e) {
            log.error("Hit max retry attempts {}: {}", kafkaAdminConfig.getRetries(), e);
        }
        return result;
    }

    public boolean buildTopics() {
        RetryTemplate retryTemplate = RetryOperationUtils.getRetryTemplate("kafkaListener", KafkaException.class,
                kafkaAdminConfig.getRetries(), kafkaAdminConfig.getRetryBackoffMs());
        boolean result = false;
        try {
            result = retryTemplate.execute(retryContext -> createTopologyIngestionTopic());
        } catch (KafkaException e) {
            log.error("Hit max retry attempts {}: {}", kafkaAdminConfig.getRetries(), e);
        }
        return result;
    }

    private boolean createTopologyIngestionTopic() {
        final KafkaConfig.TopologyIngestion topologyIngestionConfig = kafkaConfig.getTopologyIngestion();
        NewTopic topic = TopicBuilder.name(topologyIngestionConfig.getTopicName()).partitions(topologyIngestionConfig
                .getPartitions()).replicas(topologyIngestionConfig.getReplicas()).config(TopicConfig.RETENTION_MS_CONFIG,
                        topologyIngestionConfig.getRetention()).config(TopicConfig.CLEANUP_POLICY_CONFIG,
                                TopicConfig.CLEANUP_POLICY_DELETE).build();
        return createTopic(topic, topologyIngestionConfig.getTopicName());
    }

    private boolean createTopic(NewTopic topic, String outputName) {
        try {
            kafkaAdmin.createOrModifyTopics(topic);
            return isTopicCreated(outputName);
        } catch (Exception e) {
            log.error("Execution Error When Creating Kafka Topic {}: ", outputName);
            throw new KafkaException("Kafka cannot create topic", e);
        }
    }
}
