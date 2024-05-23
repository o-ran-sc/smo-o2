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
package org.oran.smo.teiv.controller.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.availability.DependentServiceAvailabilityKafka;

/**
 * Health Check component for TIES ingestion.
 */

@RequiredArgsConstructor
@Component
@Slf4j
@Profile("ingestion")
public class TiesIngestionHealthIndicator implements HealthIndicator {

    private final HealthStatus healthStatus;

    private final DependentServiceAvailabilityKafka dependentServiceAvailabilityKafka;

    private static final String SERVICE_NAME = "top-exp-inv-ingestion";

    @Override
    public Health health() {
        String errorMessage = SERVICE_NAME + " is DOWN because:";
        boolean isHealthy = true;

        if (!healthStatus.isSchemaInitialized()) {
            errorMessage += " Schema is yet to be initialized.";
            isHealthy = false;
        }

        if (!checkKafkaHealth()) {
            errorMessage += " Kafka is unavailable.";
            isHealthy = false;
        }

        if (!isHealthy) {
            log.error(errorMessage);
            return Health.down().withDetail("Error", errorMessage).build();
        } else {
            log.debug(SERVICE_NAME + " is UP and Healthy.");
            return Health.up().withDetail("UP", SERVICE_NAME + " is UP and Healthy.").build();
        }
    }

    private boolean checkKafkaHealth() {
        return dependentServiceAvailabilityKafka.checkService();
    }
}
