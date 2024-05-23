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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.service.TiesDbService;
import org.oran.smo.teiv.startup.SchemaHandler;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

@SpringBootTest
@AutoConfigureJson
@ActiveProfiles({ "test", "ingestion" })
class SourceEntityDeleteTopologyProcessorTest {
    @Autowired
    private SourceEntityDeleteTopologyProcessor sourceEntityDeleteTopologyProcessor;

    @Autowired
    CustomMetrics metrics;
    @MockBean
    private TiesDbService tiesDbService;
    @MockBean
    private SchemaHandler schemaHandler;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testSourceEntityDeleteWithInvalidEventData() {
        //given
        try (MockedStatic<SchemaRegistry> mockedSchemaRegistry = Mockito.mockStatic(SchemaRegistry.class)) {
            CloudEvent event = CloudEventBuilder.v1().withId("test-id").withType("ran-logical.source-entity-delete")
                    .withSource(URI.create("http://localhost:8080/test-source")).withDataContentType("application/json")
                    .withDataSchema(URI.create("http://localhost:8080/schema/v1/source-entity-delete")).withData(
                            "{\"type\":\"cmHandle\",\"invalid\":\"abc\"}".getBytes(StandardCharsets.UTF_8)).build();
            //when
            assertDoesNotThrow(() -> sourceEntityDeleteTopologyProcessor.process(event, "messageKey"));
            //then
            mockedSchemaRegistry.verifyNoInteractions();
            verifyNoInteractions(tiesDbService);

            assertEquals(0, metrics.getNumSuccessfullyParsedSourceEntityDeleteCloudEvents().count());
            assertEquals(1, metrics.getNumUnsuccessfullyParsedSourceEntityDeleteCloudEvents().count());
            assertEquals(0, metrics.getNumSuccessfullyPersistedSourceEntityDeleteCloudEvents().count());
            assertEquals(0, metrics.getNumUnsuccessfullyPersistedSourceEntityDeleteCloudEvents().count());
            assertEquals(0, metrics.getCloudEventSourceEntityDeleteParseTime().count());
            assertEquals(0, metrics.getCloudEventSourceEntityDeletePersistTime().count());
        }
    }

    @Test
    void testSourceEntityDeleteWithUnsupportedEntityType() {
        //given
        try (MockedStatic<SchemaRegistry> mockedSchemaRegistry = Mockito.mockStatic(SchemaRegistry.class)) {
            CloudEvent event = CloudEventBuilder.v1().withId("test-id").withType("ran-logical.source-entity-delete")
                    .withSource(URI.create("http://localhost:8080/test-source")).withDataContentType("application/json")
                    .withDataSchema(URI.create("http://localhost:8080/schema/v1/source-entity-delete")).withData(
                            "{\"type\":\"unsupported-type\",\"value\":\"abc\"}".getBytes(StandardCharsets.UTF_8)).build();
            //when
            assertDoesNotThrow(() -> sourceEntityDeleteTopologyProcessor.process(event, "messageKey"));
            //then
            mockedSchemaRegistry.verifyNoInteractions();
            verifyNoInteractions(tiesDbService);
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testSourceEntityDeleteUponRuntimeExceptionDuringDeletion() {
        //given
        try (MockedStatic<SchemaRegistry> mockedSchemaRegistry = Mockito.mockStatic(SchemaRegistry.class)) {
            EntityType entityType = Mockito.mock(EntityType.class);
            CloudEvent event = CloudEventBuilder.v1().withId("test-id").withType("ran-logical.source-entity-delete")
                    .withSource(URI.create("http://localhost:8080/test-source")).withDataContentType("application/json")
                    .withDataSchema(URI.create("http://localhost:8080/schema/v1/source-entity-delete")).withData(
                            "{\"type\":\"cmHandle\",\"value\":\"abc\"}".getBytes(StandardCharsets.UTF_8)).build();
            mockedSchemaRegistry.when(SchemaRegistry::getEntityTypesWithCmId).thenReturn(List.of(entityType));
            doThrow(new RuntimeException()).when(tiesDbService).execute(anyList());
            //when
            assertDoesNotThrow(() -> sourceEntityDeleteTopologyProcessor.process(event, "messageKey"));
            //then
            mockedSchemaRegistry.verify(SchemaRegistry::getEntityTypesWithCmId, times(1));
            verify(tiesDbService, times(1)).execute(anyList());

            assertEquals(1, metrics.getNumSuccessfullyParsedSourceEntityDeleteCloudEvents().count());
            assertEquals(0, metrics.getNumUnsuccessfullyParsedSourceEntityDeleteCloudEvents().count());
            assertEquals(0, metrics.getNumSuccessfullyPersistedSourceEntityDeleteCloudEvents().count());
            assertEquals(1, metrics.getNumUnsuccessfullyPersistedSourceEntityDeleteCloudEvents().count());
            assertEquals(1, metrics.getCloudEventSourceEntityDeleteParseTime().count());
            assertTrue(metrics.getCloudEventSourceEntityDeleteParseTime().totalTime(TimeUnit.NANOSECONDS) > 0);
            assertEquals(0, metrics.getCloudEventSourceEntityDeletePersistTime().count());
        }
    }
}
