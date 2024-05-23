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
package org.oran.smo.teiv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.oran.smo.teiv.startup.SchemaHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ActiveProfiles;
import lombok.Getter;

import org.oran.smo.teiv.service.kafka.KafkaTopicService;

@EmbeddedKafka
@SpringBootTest
@ActiveProfiles({ "test", "ingestion" })
class KafkaTopicServiceTest {
    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private KafkaTopicService kafkaTopicService;

    @MockBean
    private SchemaHandler schemaHandler;

    @Value("${spring.embedded.kafka.brokers}")
    @Getter
    private String embeddedKafkaServer;

    @BeforeEach
    public void setup() {
        Supplier<String> brokers = () -> getEmbeddedKafkaServer();
        kafkaAdmin.setBootstrapServersSupplier(brokers);
    }

    @AfterEach
    protected void tearDown() {
        AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().timeoutMs(1000));
        try {
            adminClient.deleteTopics(listTopicsResult.names().get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    void test_setupOutputTopics() {
        assertTrue(kafkaTopicService.buildTopics());

        String topologyIngestionTopicName = "topology-inventory-ingestion";
        Map<String, TopicDescription> topics = kafkaAdmin.describeTopics(topologyIngestionTopicName);
        assertNotNull(topics);
        assertEquals(4, topics.get(topologyIngestionTopicName).partitions().size());
        assertEquals(1, topics.get(topologyIngestionTopicName).partitions().get(0).replicas().size());
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    void test_isAllTopicCreatedPass() {
        String topologyIngestionTopicName = "test-topology-ingestion";
        kafkaTopicService.getKafkaConfig().getTopologyIngestion().setTopicName(topologyIngestionTopicName);
        assertThrows(KafkaException.class, () -> kafkaTopicService.isTopicCreated(topologyIngestionTopicName));
        kafkaTopicService.buildTopics();
        assertTrue(kafkaTopicService.isTopicCreated(topologyIngestionTopicName));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    void test_topologyIngestionTopicCreation_Interrupted() {
        String topicName = "test-topology-ingestion";
        kafkaTopicService.getKafkaConfig().getTopologyIngestion().setTopicName(topicName);
        Thread.currentThread().interrupt();
        assertThrows(KafkaException.class, () -> kafkaTopicService.isTopicCreated("test-topology-ingestion"));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    void test_isTopicCreated_ExecutionException() throws Exception {

        Node controller = new Node(0, "localhost", 8121);
        List<Node> brokers = Arrays.asList(controller, new Node(1, "localhost", 8122), new Node(2, "localhost", 8123));
        AdminClient mockedAdminClient = new MockAdminClient(brokers, controller);
        AdminClient spiedAdminClient = Mockito.spy(mockedAdminClient);
        final NewTopic newTopic = new NewTopic("test_topic", 1, (short) 1);
        mockedAdminClient.createTopics(List.of(newTopic));

        ListTopicsResult topicListResult = Mockito.spy(mockedAdminClient.listTopics());
        KafkaFuture<Set<String>> kafkaFutures = Mockito.spy(topicListResult.names());
        doReturn(topicListResult).when(spiedAdminClient).listTopics();
        doReturn(kafkaFutures).when(topicListResult).names();
        doThrow(ExecutionException.class).when(kafkaFutures).get();

        MockedStatic<AdminClient> mockedStaticAdminClient = Mockito.mockStatic(AdminClient.class);
        mockedStaticAdminClient.when(() -> AdminClient.create(kafkaAdmin.getConfigurationProperties()).listTopics().names())
                .thenReturn(spiedAdminClient);

        KafkaTopicService spiedTopicService = Mockito.spy(kafkaTopicService);

        assertThrows(KafkaException.class, () -> spiedTopicService.isTopicCreated("topology-inventory-ingestion"));

        Mockito.reset(spiedAdminClient, topicListResult, kafkaFutures, spiedTopicService);
        mockedStaticAdminClient.close();

    }
}
