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
package org.oran.smo.teiv.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.web.client.RestTemplate;

import org.oran.smo.teiv.config.KafkaConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndToEndTestUtil {
    public static void sendEventList(final List<CloudEvent> events, EmbeddedKafkaBroker embeddedKafkaBroker,
            KafkaConfig kafkaConfig) {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        Producer<String, CloudEvent> producer = new DefaultKafkaProducerFactory<>(configs, new StringSerializer(),
                new CloudEventSerializer()).createProducer();
        for (CloudEvent event : events) {
            ProducerRecord<String, CloudEvent> producerRecord = new ProducerRecord<String, CloudEvent>(kafkaConfig
                    .getTopologyIngestion().getTopicName(), null, event);
            producer.send(producerRecord);
        }
    }

    public static String getResponseFromApi(final String uri) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }

    public static String processApiCall(final String uri) {
        log.info("(processApiCall) Sending request for: {}", uri);
        String responseBody = getResponseFromApi(uri);
        log.info("(processApiCall) Response: {}", responseBody);
        return responseBody;
    }

    public static Map<String, Object> processNode(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(node, new TypeReference<>() {
        });
    }

}
