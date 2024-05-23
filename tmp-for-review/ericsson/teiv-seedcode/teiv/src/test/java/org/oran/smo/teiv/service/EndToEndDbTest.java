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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.availability.DependentServiceAvailabilityKafka;
import org.oran.smo.teiv.config.KafkaConfig;
import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.listener.ListenerStarter;
import org.oran.smo.teiv.service.kafka.KafkaTopicService;
import org.oran.smo.teiv.startup.AppInit;
import org.oran.smo.teiv.utils.CloudEventTestUtil;
import org.oran.smo.teiv.utils.EndToEndExpectedResults;
import org.oran.smo.teiv.utils.EndToEndTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@EmbeddedKafka
@SpringBootTest(properties = {
        "kafka.server.bootstrap-server-host:#{environment.getProperty(\"spring.embedded.kafka.brokers\").split(\":\")[0]}",
        "kafka.server.bootstrap-server-port:#{environment.getProperty(\"spring.embedded.kafka.brokers\").split(\":\")[1]}",
        "kafka.availability.retryIntervalMs:10", "kafka.topic.replicas:1" })
@ActiveProfiles({ "test", "ingestion" })
public class EndToEndDbTest {
    public static TestPostgresqlContainer postgresqlContainer = TestPostgresqlContainer.getInstance();

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

    @Autowired
    private TiesDbService tiesDbService;

    private AppInit appInit;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private DSLContext writeDataDslContext;

    private final static String CNA_TABLE = "CloudNativeApplication";
    private final static String CNS_TABLE = "CloudNativeSystem";
    private final static String ME_TABLE = "ManagedElement";
    private final static String GNBDU_TABLE = "GNBDUFunction";
    private final static String GNBCUUP_TABLE = "GNBCUUPFunction";
    private final static String GNBCUCP_TABLE = "GNBCUCPFunction";
    private final static String NRCELLDU_TABLE = "NRCellDU";
    private final static String GNBDU_CNA_TABLE = "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";
    private final static String GNBCUUP_CNA_TABLE = "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";
    private final static String GNBCUCP_CNA_TABLE = "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";

    private static final String TEST_EVENT_FOLDER = "src/test/resources/cloudeventdata/end-to-end/";
    private static final String EXPECTED_RESULTS_FOLDER = "src/test/resources/cloudeventdata/end-to-end/expected-results/db/";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.read.jdbc-url", () -> postgresqlContainer.getJdbcUrl());
        registry.add("spring.datasource.read.username", () -> postgresqlContainer.getUsername());
        registry.add("spring.datasource.read.password", () -> postgresqlContainer.getPassword());

        registry.add("spring.datasource.write.jdbc-url", () -> postgresqlContainer.getJdbcUrl());
        registry.add("spring.datasource.write.username", () -> postgresqlContainer.getUsername());
        registry.add("spring.datasource.write.password", () -> postgresqlContainer.getPassword());
    }

    @BeforeEach
    void setUp() {
        writeDataDslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(
                t -> writeDataDslContext.truncate(t).cascade().execute());
        appInit = new AppInit(dependentServiceAvailabilityKafka, kafkaTopicService, listenerStarter);
        appInit.startUpHandler();
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void testEndToEndDb() throws InterruptedException, IOException {
        final String CREATE_MANY_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-create-many-to-many.json";
        final String CREATE_SECOND_CASE_PATH = TEST_EVENT_FOLDER + "ce-create-second-case.json";
        final String MERGE_ONE_TO_MANY_PATH = TEST_EVENT_FOLDER + "ce-merge-one-to-many-deprecated-structure.json";
        final String DELETE_EVENT_PATH = TEST_EVENT_FOLDER + "ce-delete-many-to-many.json";
        final String DELETE_EVENT_ONE_TO_ONE_PATH = TEST_EVENT_FOLDER + "ce-delete-one-to-one.json";
        final String DELETE_EVENT_CMHANDLE_PATH = TEST_EVENT_FOLDER + "ce-source-entity-delete-cm-handle.json";
        final String CREATE_ONE_TO_ONE_PATH = TEST_EVENT_FOLDER + "ce-create-one-to-one.json";

        final String EXP_CREATE_MANY_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-many-to-many.json";
        final String EXP_MERGE_ONE_TO_MANY_PATH = EXPECTED_RESULTS_FOLDER + "exp-merge-one-to-many.json";
        final String EXP_CREATE_SECOND_CASE_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-second-case.json";
        final String EXP_CREATE_ONE_TO_ONE_PATH = EXPECTED_RESULTS_FOLDER + "exp-create-one-to-one.json";
        final String EXP_ONE_TO_ONE_PATH_AFTER_DELETE = EXPECTED_RESULTS_FOLDER + "exp-delete-one-to-one.json";

        validateReceivedCloudEventMetrics(0, 0, 0, 0);
        sendEventFromFile(CREATE_ONE_TO_ONE_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                EndToEndExpectedResults values = getExpectedResults(EXP_CREATE_ONE_TO_ONE_PATH);
                validateDbResultsAfterOneToOneCE(values);
                validateReceivedCloudEventMetrics(1, 0, 0, 0);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Create one to one event: " + e.getMessage(),
                        e);
            }
        });

        sendEventFromFile(CREATE_MANY_TO_MANY_PATH);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(Duration.ofSeconds(5)).untilAsserted(
                () -> assertWithErrorMessage(() -> {
                    EndToEndExpectedResults values = getExpectedResults(EXP_CREATE_MANY_TO_MANY_PATH);
                    validateDbResultsAfterCreate(values);
                    validateReceivedCloudEventMetrics(2, 0, 0, 0);
                }, "Assertion failed during validation of Create many to many event"));

        sendEventFromFile(MERGE_ONE_TO_MANY_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> assertWithErrorMessage(() -> {
            EndToEndExpectedResults values = getExpectedResults(EXP_MERGE_ONE_TO_MANY_PATH);
            validateDbResultsAfterMerge(values);
            validateReceivedCloudEventMetrics(2, 1, 0, 0);
        }, "Assertion failed during validation of Merge one to many event"));

        sendEventFromFile(CREATE_SECOND_CASE_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> assertWithErrorMessage(() -> {
            EndToEndExpectedResults values = getExpectedResults(EXP_CREATE_SECOND_CASE_PATH);
            validateDbResultsAfterCmHandle(values);
            validateReceivedCloudEventMetrics(3, 1, 0, 0);
        }, "Assertion failed during validation of Create with CmHandle event"));

        sendEventFromFile(DELETE_EVENT_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> assertWithErrorMessage(() -> {
            validateDbResultsAfterDelete();
            validateReceivedCloudEventMetrics(3, 1, 1, 0);
        }, "Assertion failed during validation of Delete event"));

        sendEventFromFile(DELETE_EVENT_ONE_TO_ONE_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                EndToEndExpectedResults values = getExpectedResults(EXP_ONE_TO_ONE_PATH_AFTER_DELETE);
                validateDbResultsAfterDeleteOneToOne(values);
                validateReceivedCloudEventMetrics(3, 1, 2, 0);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Delete one to one relationship event: " + e
                        .getMessage(), e);
            }
        });

        sendEventFromFile(DELETE_EVENT_CMHANDLE_PATH);
        Awaitility.await().pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            try {
                validateDbResultsAfterDeleteCmHandle();
                validateReceivedCloudEventMetrics(3, 1, 2, 1);
            } catch (AssertionError e) {
                throw new AssertionError("Assertion failed during validation of Delete with CmHandle event: " + e
                        .getMessage(), e);
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

    private void validateDbResultsAfterCreate(final EndToEndExpectedResults values) {
        assertDatabaseContainsValues(CNA_TABLE, values.get("entity_map_CNA_1"));
        assertDatabaseContainsValues(CNA_TABLE, values.get("entity_map_CNA_2"));
        assertDatabaseContainsValues(CNA_TABLE, values.get("entity_map_CNA_3"));
        assertDatabaseContainsValues(GNBCUUP_TABLE, values.get("entity_map_GNBCUUP_1"));
        assertDatabaseContainsValues(GNBCUCP_TABLE, values.get("entity_map_GNBCUCP_1"));
        assertDatabaseContainsValues(GNBDU_TABLE, values.get("entity_map_GNBDU_1"));

        assertDatabaseContainsValues(GNBDU_CNA_TABLE, values.get("relation_map_GNBDU_CNA_1"));
        assertDatabaseContainsValues(GNBDU_CNA_TABLE, values.get("relation_map_GNBDU_CNA_4"));
        assertDatabaseContainsValues(GNBCUUP_CNA_TABLE, values.get("relation_map_GNBCUUP_CNA_2"));
        assertDatabaseContainsValues(GNBCUUP_CNA_TABLE, values.get("relation_map_GNBCUUP_CNA_5"));
        assertDatabaseContainsValues(GNBCUCP_CNA_TABLE, values.get("relation_map_GNBCUCP_CNA_3"));
        assertDatabaseContainsValues(GNBCUCP_CNA_TABLE, values.get("relation_map_GNBCUCP_CNA_6"));

    }

    private void validateDbResultsAfterOneToOneCE(final EndToEndExpectedResults values) throws IOException {
        assertDatabaseContainsValues(CNS_TABLE, values.get("entity_map_CNS_1"));
        assertDatabaseContainsValues(ME_TABLE, values.get("entity_map_ME_1"));
        assertDatabaseContainsValues(CNS_TABLE, values.get("entity_map_CNS_2"));
        assertDatabaseContainsValues(ME_TABLE, values.get("entity_map_ME_2"));
    }

    private void validateDbResultsAfterMerge(final EndToEndExpectedResults values) {
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_1"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_2"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_3"));

        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_1"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_2"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_3"));
    }

    private void validateDbResultsAfterCmHandle(final EndToEndExpectedResults values) {
        assertDatabaseContainsValues(CNA_TABLE, values.get("entity_map_CNA_4"));
        assertDatabaseContainsValues(GNBDU_TABLE, values.get("entity_map_GNBDU_1"));
        assertDatabaseContainsValues(GNBDU_TABLE, values.get("entity_map_GNBDU_2"));
        assertDatabaseContainsValues(GNBCUUP_TABLE, values.get("entity_map_GNBCUUP_1"));
        assertDatabaseContainsValues(GNBCUCP_TABLE, values.get("entity_map_GNBCUCP_1"));
        assertDatabaseContainsValues(GNBCUCP_TABLE, values.get("entity_map_GNBCUCP_2"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_1"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_2"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("entity_map_NRCellDU_3"));
        assertDatabaseContainsValues(GNBDU_CNA_TABLE, values.get("relation_map_GNBDU_CNA_1"));
        assertDatabaseContainsValues(GNBCUUP_CNA_TABLE, values.get("relation_map_GNBCUUP_CNA_2"));
        assertDatabaseContainsValues(GNBCUCP_CNA_TABLE, values.get("relation_map_GNBCUCP_CNA_3"));
        assertDatabaseContainsValues(GNBDU_CNA_TABLE, values.get("relation_map_GNBDU_CNA_4"));
        assertDatabaseContainsValues(GNBCUUP_CNA_TABLE, values.get("relation_map_GNBCUUP_CNA_5"));
        assertDatabaseContainsValues(GNBCUCP_CNA_TABLE, values.get("relation_map_GNBCUCP_CNA_6"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_1"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_2"));
        assertDatabaseContainsValues(NRCELLDU_TABLE, values.get("relation_map_GNBDU_NRCellDU_3"));
    }

    private void validateDbResultsAfterDelete() {
        assertDatabaseDoesNotContainRecord(CNA_TABLE, "CloudNativeApplication_3");
        assertDatabaseDoesNotContainRecord(GNBCUCP_TABLE, "GNBCUCP_1");
        assertDatabaseDoesNotContainRecord(GNBDU_CNA_TABLE, "relation_1");
        assertDatabaseDoesNotContainRecord(GNBCUUP_CNA_TABLE, "relation_5");
        assertDatabaseDoesNotContainRecord(GNBCUCP_CNA_TABLE, "relation_3");
        assertDatabaseDoesNotContainRecord(GNBCUCP_CNA_TABLE, "relation_6");
    }

    private void validateDbResultsAfterDeleteCmHandle() {
        assertDatabaseDoesNotContainRecord(GNBDU_TABLE, "GNBDU_SED_1");
        assertDatabaseDoesNotContainRecord(GNBCUCP_TABLE, "GNBCUCP_SED_1");
        assertDatabaseDoesNotContainRecord(GNBCUCP_TABLE, "GNBCUCP_SED_2");
        assertDatabaseDoesNotContainRecord(GNBCUCP_CNA_TABLE, "relation_sed_1");
        assertDatabaseDoesNotContainRecord(GNBDU_CNA_TABLE, "relation_sed_2");
    }

    private void validateDbResultsAfterDeleteOneToOne(final EndToEndExpectedResults values) {
        // Test case 1: Delete an entity - relationship removed from an existing entity
        assertDatabaseDoesNotContainRecord(CNS_TABLE, "relation_11");
        assertDatabaseDoesNotContainRecord(ME_TABLE, "relation_11");
        assertDatabaseDoesNotContainRecord(ME_TABLE, "ManagedElement_2");
        assertDatabaseContainsValues(CNS_TABLE, values.get("entity_map_CNS_1"));
        // Test case 2: Delete a relationship - relationship removed from both existing
        // entities
        assertDatabaseDoesNotContainRecord(CNS_TABLE, "relation_12");
        assertDatabaseDoesNotContainRecord(ME_TABLE, "relation_12");
        assertDatabaseContainsValues(CNS_TABLE, values.get("entity_map_CNS_2"));
        assertDatabaseContainsValues(ME_TABLE, values.get("entity_map_ME_2"));
    }

    private void assertDatabaseContainsValues(final String table, final Map<String, Object> attributes) {
        Result<Record> results = tiesDbService.selectAllRowsFromTable("ties_data.\"" + table + "\"");
        assertTrue(results.isNotEmpty(), String.format("Database table \"%s\" is empty, but it should not.", table));
        assertTrue(results.stream().anyMatch(row -> attributes.keySet().stream().allMatch(attr -> Objects.equals(attributes
                .get(attr), row.get(attr)))), String.format(
                        "Database table \"%s\" does not contain expected data, but it should.", table));
    }

    private void assertDatabaseDoesNotContainRecord(final String table, final String id) {
        Result<Record> results = tiesDbService.selectAllRowsFromTable("ties_data.\"" + table + "\"");
        if (results.isNotEmpty()) {
            boolean contains = results.stream().map(row -> row.get("id")).filter(Objects::nonNull).map(Object::toString)
                    .anyMatch(id::equals);
            assertFalse(contains, String.format("Database table \"%s\" contains record: \"%s\", but it should not.", table,
                    id));
        }
    }

    private void sendEventFromFile(final String path) {
        assertDoesNotThrow(() -> EndToEndTestUtil.sendEventList(List.of(CloudEventTestUtil.getCloudEventFromJsonFile(path)),
                embeddedKafkaBroker, kafkaConfig));
    }

    private EndToEndExpectedResults getExpectedResults(final String path) {
        return assertDoesNotThrow(() -> new EndToEndExpectedResults(path));
    }

    private void assertWithErrorMessage(Runnable assertion, String errorMessage) {
        try {
            assertion.run();
        } catch (Error e) {
            throw new AssertionError(errorMessage + ": " + e.getMessage(), e);
        }
    }
}
