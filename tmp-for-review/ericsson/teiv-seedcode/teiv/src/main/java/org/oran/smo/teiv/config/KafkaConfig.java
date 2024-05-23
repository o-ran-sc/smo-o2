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
package org.oran.smo.teiv.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Data
@Profile("ingestion")
public class KafkaConfig {

    private final TopologyIngestion topologyIngestion;

    @Data
    @Configuration
    public static class TopologyIngestion {
        @Value("${kafka.topology-ingestion.consumer.topic.name}")
        private String topicName;

        @Value("${kafka.topology-ingestion.consumer.topic.partitions}")
        private int partitions;

        @Value("${kafka.topology-ingestion.consumer.topic.replicas}")
        private int replicas;

        @Value("${kafka.topology-ingestion.consumer.topic.retention-ms}")
        private String retention;

        @Value("${kafka.topology-ingestion.consumer.group-id}")
        private String groupId;

        @Value("${kafka.topology-ingestion.consumer.auto-offset-reset}")
        private String autoOffsetReset;

        @Value("${kafka.topology-ingestion.consumer.max-poll-records}")
        private int maxPollRecords;

        @Value("${kafka.topology-ingestion.consumer.max-poll-interval-ms}")
        private int maxPollIntervalMs;

        @Value("${kafka.topology-ingestion.consumer.fetch-min-bytes}")
        private int fetchMinBytes;

        @Value("${kafka.topology-ingestion.consumer.fetch-max-wait-ms}")
        private int fetchMaxWaitMs;

        @Value("${kafka.topology-ingestion.consumer.retry-attempts}")
        private int retryAttempts;

        @Value("${kafka.topology-ingestion.consumer.retry-backoff-ms}")
        private int retryBackoffMs;

        @Value("${kafka.topology-ingestion.consumer.concurrency}")
        private int concurrency;
    }
}
