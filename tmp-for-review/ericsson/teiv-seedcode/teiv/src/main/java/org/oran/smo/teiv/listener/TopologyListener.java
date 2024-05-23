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

import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.SerializationUtils;
import org.springframework.stereotype.Component;
import io.cloudevents.CloudEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.CustomMetrics;

@Component
@Slf4j
@Profile("ingestion")
@AllArgsConstructor
public class TopologyListener {
    private final CustomMetrics metrics;
    private final TopologyProcessorRegistry topologyProcessorRegistry;
    private final LogAccessor logger = new LogAccessor(TopologyListener.class);

    @KafkaListener(id = "${kafka.topology-ingestion.consumer.group-id}", topics = "${kafka.topology-ingestion.consumer.topic.name}", containerFactory = "topologyListenerContainerFactory", autoStartup = "false")
    public void processEvents(List<ConsumerRecord<String, CloudEvent>> events) {
        log.info("Processing events: {}", events.size());
        for (ConsumerRecord<String, CloudEvent> rec : events) {
            if (rec.value() == null) {
                metrics.incrementNumReceivedCloudEventNotSupported();
                Optional<DeserializationException> deserializationException = Optional.ofNullable(SerializationUtils
                        .getExceptionFromHeader(rec, SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER, this.logger));
                deserializationException.ifPresent(exception -> {
                    logger.error(exception.getCause().getLocalizedMessage());
                    logger.error(exception, "Record at offset " + rec.offset() + " could not be deserialized");
                });
            } else {
                String messageKey = rec.key();
                topologyProcessorRegistry.getProcessor(rec.value()).process(rec.value(), messageKey);
            }
        }
    }
}
