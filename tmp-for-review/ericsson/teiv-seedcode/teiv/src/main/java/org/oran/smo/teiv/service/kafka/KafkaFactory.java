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

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.oran.smo.teiv.config.KafkaAdminConfig;
import org.oran.smo.teiv.config.KafkaConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

@Component
@EnableKafka
@AllArgsConstructor
@Profile("ingestion")
public class KafkaFactory {

    private final KafkaConfig kafkaConfig;
    private final KafkaAdminConfig kafkaAdminConfig;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> adminConfig = new HashMap<>(6);
        adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdminConfig.getBootstrapServer());
        adminConfig.put(AdminClientConfig.RETRIES_CONFIG, kafkaAdminConfig.getRetries());
        adminConfig.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, kafkaAdminConfig.getRetryBackoffMs());
        adminConfig.put(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaAdminConfig.getReconnectBackoffMs());
        adminConfig.put(AdminClientConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaAdminConfig.getReconnectBackoffMaxMs());
        adminConfig.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaAdminConfig.getRequestTimeoutMs());
        return new KafkaAdmin(adminConfig);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> topologyListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerTopologyListenerFactory());
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(AckMode.BATCH);
        factory.setConcurrency(kafkaConfig.getTopologyIngestion().getConcurrency());
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(kafkaConfig.getTopologyIngestion()
                .getRetryBackoffMs(), kafkaConfig.getTopologyIngestion().getRetryAttempts())));
        factory.getContainerProperties().setAuthExceptionRetryInterval(Duration.ofMillis(kafkaConfig.getTopologyIngestion()
                .getRetryBackoffMs()));

        return factory;
    }

    private ConsumerFactory<String, CloudEvent> consumerTopologyListenerFactory() {
        final KafkaConfig.TopologyIngestion topologyIngestionConfig = kafkaConfig.getTopologyIngestion();
        Map<String, Object> config = getBaseKafkaConfig();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, topologyIngestionConfig.getGroupId());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, topologyIngestionConfig.getAutoOffsetReset());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, CloudEventDeserializer.class);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, topologyIngestionConfig.getMaxPollRecords());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, topologyIngestionConfig.getMaxPollIntervalMs());
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, topologyIngestionConfig.getFetchMinBytes());
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, topologyIngestionConfig.getFetchMaxWaitMs());
        config.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, topologyIngestionConfig.getRetryBackoffMs());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    private Map<String, Object> getBaseKafkaConfig() {
        final Map<String, Object> config = new HashMap<>(5);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdminConfig.getBootstrapServer());
        config.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaAdminConfig.getRetryBackoffMs());
        config.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaAdminConfig.getReconnectBackoffMs());
        config.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaAdminConfig.getReconnectBackoffMaxMs());
        config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaAdminConfig.getRequestTimeoutMs());
        return config;
    }
}
