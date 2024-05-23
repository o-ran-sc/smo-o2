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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.Data;

import org.oran.smo.teiv.service.kafka.KafkaAddressSupplier;

@Configuration
@Data
@Profile("ingestion")
@Slf4j
public class KafkaAdminConfig {

    private String bootstrapServer;

    @Value("${kafka.admin.retry}")
    private int retries;

    @Value("${kafka.admin.retry-backoff-ms}")
    private int retryBackoffMs;

    @Value("${kafka.admin.reconnect-backoff-ms}")
    private int reconnectBackoffMs;

    @Value("${kafka.admin.reconnect-backoff-max-ms}")
    private int reconnectBackoffMaxMs;

    @Value("${kafka.admin.request-timeout-ms}")
    private int requestTimeoutMs;

    public KafkaAdminConfig(KafkaAddressSupplier messageBus) {
        bootstrapServer = messageBus.getBootstrapServer();
        log.info("The following address will be used for Kafka: {}", bootstrapServer);
    }
}
