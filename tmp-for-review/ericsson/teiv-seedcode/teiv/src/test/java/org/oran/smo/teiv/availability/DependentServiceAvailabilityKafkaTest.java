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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ActiveProfiles;

import org.oran.smo.teiv.config.KafkaAdminConfig;
import org.oran.smo.teiv.exception.UnsatisfiedExternalDependencyException;
import org.oran.smo.teiv.service.kafka.KafkaFactory;
import org.oran.smo.teiv.startup.SchemaHandler;
import lombok.Getter;

@EmbeddedKafka
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({ "test", "ingestion" })
public class DependentServiceAvailabilityKafkaTest {
    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${spring.embedded.kafka.brokers}")
    @Getter
    private String embeddedKafkaServer;

    @Autowired
    private KafkaAdminConfig kafkaAdminConfig;

    @Autowired
    KafkaFactory factory;

    @Autowired
    private DependentServiceAvailabilityKafka dependentServiceAvailabilityKafka;

    @MockBean
    private SchemaHandler schemaHandler;

    @BeforeEach
    public void setup() {
        Supplier<String> brokers = () -> getEmbeddedKafkaServer();
        dependentServiceAvailabilityKafka.getKafkaAdmin().setBootstrapServersSupplier(brokers);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("When checkKafkaHealth not throws exception, expect to pass, means kafka broker is up")
    @Order(1)
    void test_input_topic_does_exist() {
        kafkaAdminConfig.setBootstrapServer(embeddedKafkaServer);
        final boolean result = dependentServiceAvailabilityKafka.checkService();
        assertTrue(result);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("When checkKafkaHealth throws exception, expect to fail, means kafka broker is down")
    @Order(2)
    void testKafkaNotReachable() {
        embeddedKafkaBroker.destroy();
        dependentServiceAvailabilityKafka.setListTopicTimeout(100);
        final boolean result = dependentServiceAvailabilityKafka.checkService();
        assertFalse(result);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @Order(3)
    void testIsServiceAvailable_ThrowsInterruptedException(CapturedOutput output) throws Exception {
        Node controller = new Node(0, "localhost", 8121);
        List<Node> brokers = Arrays.asList(controller, new Node(1, "localhost", 8122), new Node(2, "localhost", 8123));
        AdminClient mockedAdminClient = new MockAdminClient(brokers, controller);
        AdminClient spiedAdminClient = Mockito.spy(mockedAdminClient);
        final NewTopic newTopic = new NewTopic("test_topic", 1, (short) 1);
        mockedAdminClient.createTopics(List.of(newTopic));

        ListTopicsResult topicListresult = Mockito.spy(mockedAdminClient.listTopics());
        KafkaFuture<Set<String>> kafkaFutures = Mockito.spy(topicListresult.names());
        doReturn(topicListresult).when(spiedAdminClient).listTopics();
        doReturn(kafkaFutures).when(topicListresult).names();
        doThrow(InterruptedException.class).when(kafkaFutures).get();

        MockedStatic<AdminClient> mockedStaticAdminClient = Mockito.mockStatic(AdminClient.class);
        mockedStaticAdminClient.when(() -> AdminClient.create(ArgumentMatchers.any(Properties.class)).listTopics().names())
                .thenReturn(spiedAdminClient);

        DependentServiceAvailabilityKafka spiedDependentServiceAvailabilityKafka = Mockito.spy(
                dependentServiceAvailabilityKafka);

        assertThrows(UnsatisfiedExternalDependencyException.class,
                spiedDependentServiceAvailabilityKafka::isServiceAvailable);

        Mockito.reset(spiedAdminClient, topicListresult, kafkaFutures, spiedDependentServiceAvailabilityKafka);
        mockedStaticAdminClient.close();
    }
}
