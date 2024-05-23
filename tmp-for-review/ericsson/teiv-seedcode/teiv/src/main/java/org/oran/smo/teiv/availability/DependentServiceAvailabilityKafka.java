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
package org.oran.smo.teiv.availability;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.config.KafkaAdminConfig;
import org.oran.smo.teiv.exception.UnsatisfiedExternalDependencyException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile("ingestion")
public class DependentServiceAvailabilityKafka extends DependentServiceAvailability {

    @Getter
    private final KafkaAdminConfig kafkaAdminConfig;

    @Getter
    private final KafkaAdmin kafkaAdmin;

    @Setter
    private Integer listTopicTimeout = null;

    public DependentServiceAvailabilityKafka(KafkaAdminConfig kafkaAdminConfig, KafkaAdmin kafkaAdmin) {
        this.kafkaAdminConfig = kafkaAdminConfig;
        this.kafkaAdmin = kafkaAdmin;
        serviceName = "Kafka";
    }

    @Value("${kafka.availability.retry-interval-ms}")
    protected void setRetryInterval(int retryIntervalMs) {
        super.retryIntervalMs = retryIntervalMs;
    }

    @Value("${kafka.availability.retry-attempts}")
    protected void setRetryAttempts(int retryAttempts) {
        super.retryAttempts = retryAttempts;
    }

    @Override
    boolean isServiceAvailable() throws UnsatisfiedExternalDependencyException {
        log.debug("Checking if Kafka is reachable, bootstrap server: '{}'", kafkaAdminConfig.getBootstrapServer());

        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsResult topics = client.listTopics(new ListTopicsOptions().timeoutMs(listTopicTimeout));
            topics.names().get();
            return true;
        } catch (InterruptedException e) {
            log.error("Interrupted Error Reaching Kafka {}: {}", kafkaAdminConfig.getBootstrapServer(), e.getMessage());
            Thread.currentThread().interrupt();
            throw new UnsatisfiedExternalDependencyException("Interrupted Error Reaching Kafka", e);
        } catch (Exception e) {
            log.error("Execution Error Reaching Kafka {}: ", kafkaAdminConfig.getBootstrapServer(), e);
            throw new UnsatisfiedExternalDependencyException("Kafka Unreachable", e);
        }
    }
}
