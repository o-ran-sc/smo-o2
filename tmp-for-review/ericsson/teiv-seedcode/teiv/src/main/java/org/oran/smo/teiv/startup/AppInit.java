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
package org.oran.smo.teiv.startup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oran.smo.teiv.availability.DependentServiceAvailabilityKafka;
import org.oran.smo.teiv.listener.ListenerStarter;
import org.oran.smo.teiv.service.kafka.KafkaTopicService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Profile("!test & ingestion")
@Slf4j
public class AppInit {

    private final DependentServiceAvailabilityKafka dependentServiceAvailabilityKafka;

    @Getter
    private final KafkaTopicService kafkaTopicService;

    private final ListenerStarter listenerStarter;

    /**
     * Async Listener for when the spring boot application is started and ready.
     * Needs to be Async so that liveliness and readiness probes are
     * unaffected by retries.
     */
    @Async
    @Order(value = 20)
    @EventListener(value = ApplicationReadyEvent.class)
    public void startUpHandler() {
        if (dependentServiceAvailabilityKafka.checkService()) {
            log.info("Building Topology and Inventory Exposure topics.");
            kafkaTopicService.buildTopics();
            log.info("Starting Topology and Inventory Exposure kafka listeners.");
            listenerStarter.startKafkaListeners();
        }
    }
}
