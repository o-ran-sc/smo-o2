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

import static org.oran.smo.teiv.utils.TiesConstants.REQUEST_MAPPING;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.awaitility.Awaitility;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.service.kafka.KafkaTopicService;
import org.oran.smo.teiv.startup.AppInit;
import org.oran.smo.teiv.availability.DependentServiceAvailabilityKafka;
import org.oran.smo.teiv.config.KafkaConfig;
import org.oran.smo.teiv.listener.ListenerStarter;
import org.oran.smo.teiv.utils.CloudEventTestUtil;
import org.oran.smo.teiv.utils.EndToEndExpectedResults;
import org.oran.smo.teiv.utils.EndToEndTestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@EmbeddedKafka
@SpringBootTest(properties = {
        "kafka.server.bootstrap-server-host:#{environment.getProperty(\"spring.embedded.kafka.brokers\").split(\":\")[0]}",
        "kafka.server.bootstrap-server-port:#{environment.getProperty(\"spring.embedded.kafka.brokers\").split(\":\")[1]}",
        "kafka.availability.retryIntervalMs:10",
        "kafka.topic.replicas:1" }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test", "ingestion" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class EndToEndApiTest {
    private static final TestPostgresqlContainer postgreSQLContainer = TestPostgresqlContainer.getInstance();

    @Autowired
    private DependentServiceAvailabilityKafka dependentServiceAvailabilityKafka;

    @Autowired
    private KafkaTopicService kafkaTopicService;

    @Autowired
    private ListenerStarter listenerStarter;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private CustomMetrics customMetrics;

    @LocalServerPort
    private int port;

    private AppInit appInit;

    private static String URI;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private DSLContext writeDataDslContext;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.read.jdbc-url", () -> postgreSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.read.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.datasource.read.password", () -> postgreSQLContainer.getPassword());

        registry.add("spring.datasource.write.jdbc-url", () -> postgreSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.write.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.datasource.write.password", () -> postgreSQLContainer.getPassword());
    }

    @BeforeEach
    void setUp() {
        writeDataDslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(
                t -> writeDataDslContext.truncate(t).cascade().execute());
        appInit = new AppInit(dependentServiceAvailabilityKafka, kafkaTopicService, listenerStarter);
        appInit.startUpHandler();
        URI = String.format("http://localhost:%s%s/domains/", port, REQUEST_MAPPING);
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void testEndToEndApi() {
        final String TEST_EVENT_FOLDER = "src/test/resources/cloudeventdata/end-to-end/";
        final String EXPECTED_RESULTS_FOLDER = "src/test/resources/cloudeventdata/end-to-end/expected-results/api/";

        final String CREATE_MANY_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-create-many-to-many.json";
        final String CREATE_MANY_TO_ONE_PATH = TEST_EVENT_FOLDER + "ce-create-many-to-one.json";
        final String CREATE_ONE_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-create-one-to-many.json";
        final String CREATE_SECOND_CASE_PATH = TEST_EVENT_FOLDER + "ce-create-second-case.json";
        final String CREATE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH = TEST_EVENT_FOLDER + "ce-create-relationship-connecting-same-entity.json";

        final String MERGE_ONE_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-merge-one-to-many-deprecated-structure.json";

        final String DELETE_MANY_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-delete-many-to-many.json";
        final String DELETE_MANY_TO_ONE_PATH = TEST_EVENT_FOLDER + "ce-delete-many-to-one.json";
        final String DELETE_ONE_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-delete-one-to-many.json";
        final String DELETE_EVENT_CMHANDLE_PATH = TEST_EVENT_FOLDER + "ce-source-entity-delete-cm-handle.json";
        final String DELETE_EVENT_CMHANDLE_PATH_2 = TEST_EVENT_FOLDER + "ce-source-entity-delete-cm-handle2.json";
        final String DELETE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH = TEST_EVENT_FOLDER + "ce-delete-relationship-connecting-same-entity.json";

        final String EXP_CREATE_MANY_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-many-to-many.json";
        final String EXP_CREATE_MANY_TO_ONE_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-many-to-one.json";
        final String EXP_CREATE_ONE_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-one-to-many.json";
        final String EXP_CREATE_SECOND_CASE_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-second-case.json";
        final String EXP_CREATE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-relationship-connecting-same-entity.json";

        final String EXP_MERGE_ONE_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-merge-one-to-many.json";

        final String EXP_DELETE_MANY_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-delete-many-to-many.json";
        final String EXP_DELETE_ONE_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-delete-one-to-many.json";
        final String EXP_DELETE_MANY_TO_ONE_PATH = EXPECTED_RESULTS_FOLDER + "exp-delete-many-to-one.json";
        final String EXP_SOURCE_ENTITY_DELETE_CM_HANDLE = EXPECTED_RESULTS_FOLDER + "exp-source-entity-delete-cm-handle.json";
        final String EXP_DELETE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH = EXPECTED_RESULTS_FOLDER + "exp-delete-relationship-connecting-same-entity.json";

        validateReceivedCloudEventMetrics(0, 0, 0, 0);

        sendEventFromFile(CREATE_MANY_TO_MANY_PATH);
        sendEventFromFile(CREATE_ONE_TO_MANY_PATH);
        sendEventFromFile(CREATE_MANY_TO_ONE_PATH);
        sendEventFromFile(CREATE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                validateApiResultsWithValues(EXP_CREATE_MANY_TO_MANY_PATH);
                validateApiResultsWithValues(EXP_CREATE_ONE_TO_MANY_PATH);
                validateApiResultsWithValues(EXP_CREATE_MANY_TO_ONE_PATH);
                validateApiResultsWithValues(EXP_CREATE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH);
                validateReceivedCloudEventMetrics(4, 0, 0, 0);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Create many to many event: " + e
                        .getMessage(), e);
            }
        });

        sendEventFromFile(MERGE_ONE_TO_MANY_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                validateApiResultsWithValues(EXP_MERGE_ONE_TO_MANY_PATH);
                validateReceivedCloudEventMetrics(4, 1, 0, 0);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Merge one to many event: " + e.getMessage(),
                        e);
            }

        });

        sendEventFromFile(CREATE_SECOND_CASE_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                validateApiResultsWithValues(EXP_CREATE_SECOND_CASE_PATH);
                validateReceivedCloudEventMetrics(5, 1, 0, 0);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Create with CmHandle event: " + e
                        .getMessage(), e);
            }
        });

        sendEventFromFile(DELETE_MANY_TO_MANY_PATH);
        sendEventFromFile(DELETE_ONE_TO_MANY_PATH);
        sendEventFromFile(DELETE_MANY_TO_ONE_PATH);
        sendEventFromFile(DELETE_EVENT_CMHANDLE_PATH);
        sendEventFromFile(DELETE_EVENT_CMHANDLE_PATH_2);
        sendEventFromFile(DELETE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                validateApiResultsAfterDelete(EXP_DELETE_MANY_TO_MANY_PATH);
                validateApiResultsAfterDelete(EXP_DELETE_ONE_TO_MANY_PATH);
                validateApiResultsAfterDelete(EXP_DELETE_MANY_TO_ONE_PATH);
                validateApiResultsAfterDelete(EXP_SOURCE_ENTITY_DELETE_CM_HANDLE);
                validateApiResultsAfterDelete(EXP_DELETE_RELATIONSHIP_CONNECTING_SAME_ENTITY_PATH);
                validateReceivedCloudEventMetrics(5, 1, 4, 2);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Delete event: " + e.getMessage(), e);
            }
        });
    }

    private void validateReceivedCloudEventMetrics(final int create, final int merge, final int delete,
            final int sourceDelete) {
        assertEquals(create, customMetrics.getNumReceivedCloudEventCreate().count());
        assertEquals(merge, customMetrics.getNumReceivedCloudEventMerge().count());
        assertEquals(delete, customMetrics.getNumReceivedCloudEventDelete().count());
        assertEquals(sourceDelete, customMetrics.getNumReceivedCloudEventSourceEntityDelete().count());
    }

    private void validateApiResultsWithValues(final String expectedValuesCollectionPath) throws JsonMappingException,
            JsonProcessingException, IOException {
        EndToEndExpectedResults values = getExpectedResults(expectedValuesCollectionPath);
        values.getAll().forEach((requestSubUri, responseResult) -> {
            String apiRes = processApiCall(URI + requestSubUri);
            String expectedRes = responseResult.toString();
            assertEquals(JSONB.jsonb(expectedRes), JSONB.jsonb(apiRes));
        });
    }

    private void validateApiResultsAfterDelete(String requestUrlCollectionPath) {
        EndToEndExpectedResults values = getExpectedResults(requestUrlCollectionPath);
        assertDoesNotThrow(() -> values.getAll(), "Reading expected results resulted in error.").keySet().forEach((key) -> {
            assertResponseNotContainId(URI + key);
        });
    };

    private void assertResponseNotContainId(final String requestUri) {
        HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> EndToEndTestUtil.processApiCall(
                requestUri));
        assertEquals(HttpStatusCode.valueOf(404), e.getStatusCode(), "API Response status is not 404, but it should");
    }

    private void sendEventFromFile(final String path) {
        assertDoesNotThrow(() -> EndToEndTestUtil.sendEventList(List.of(CloudEventTestUtil.getCloudEventFromJsonFile(path)),
                embeddedKafkaBroker, kafkaConfig), "Sending cloud event from file resulted in error.");
    }

    private EndToEndExpectedResults getExpectedResults(final String path) {
        return assertDoesNotThrow(() -> new EndToEndExpectedResults(path), "Reading expected values resulted in error.");

    }

    private String processApiCall(final String uri) {
        return assertDoesNotThrow(() -> EndToEndTestUtil.processApiCall(uri), "Processing api call resulted in error.");
    }
}
