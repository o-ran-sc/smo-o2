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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.service.TiesDbService;
import org.oran.smo.teiv.service.cloudevent.CloudEventParser;
import org.oran.smo.teiv.service.cloudevent.data.Entity;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.startup.SchemaHandler;
import org.oran.smo.teiv.utils.CloudEventTestUtil;

import org.oran.smo.teiv.schema.MockSchemaLoader;
import io.cloudevents.CloudEvent;

@SpringBootTest
@ActiveProfiles({ "test", "ingestion" })
class CreateTopologyProcessorTest {
    @Autowired
    private CreateTopologyProcessor createTopologyProcessor;

    @MockBean
    private CloudEventParser cloudEventParser;
    @MockBean
    private TiesDbService tiesDbService;
    @MockBean
    private SchemaHandler schemaHandler;

    @Autowired
    private CustomMetrics metrics;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        SchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
    }

    @Test
    void testCreateCloudNativeApplicationEntity1() {
        CloudEvent event = CloudEventTestUtil.getCloudEvent("create", "{}");
        String entityType = "CloudNativeApplication";
        Map<String, Object> yangParserOutputMapBSide = new HashMap<>();
        Entity entity = new Entity("", entityType, "cloud_id_1", yangParserOutputMapBSide, List.of());

        ParsedCloudEventData parsedData = new ParsedCloudEventData(List.of(entity), List.of());
        when(cloudEventParser.getCloudEventData(any())).thenReturn(parsedData);

        doThrow(new RuntimeException("test error")).when(tiesDbService).execute(anyList());
        Assertions.assertDoesNotThrow(() -> createTopologyProcessor.process(event, anyString()));

        Mockito.verify(tiesDbService, times(1)).execute(anyList());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testInvalidAttribute() {
        CloudEvent event = CloudEventTestUtil.getCloudEvent("create", "{}");
        String entityType = "CloudNativeApplication";
        Map<String, Object> yangParserOutputMap = new HashMap<>();
        yangParserOutputMap.put("invalidfield", "value1");
        Entity entity = new Entity("", entityType, "id1", yangParserOutputMap, List.of());
        ParsedCloudEventData parsedData = new ParsedCloudEventData(List.of(entity), List.of());
        when(cloudEventParser.getCloudEventData(ArgumentMatchers.any())).thenReturn(parsedData);

        createTopologyProcessor.process(event, anyString());
        verifyNoInteractions(tiesDbService);

        assertEquals(1, metrics.getNumSuccessfullyParsedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyParsedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyParsedDeleteCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedDeleteCloudEvents().count());
        assertEquals(0, metrics.getCloudEventCreatePersistTime().count());
        assertEquals(0, metrics.getCloudEventMergePersistTime().count());
        assertEquals(0, metrics.getCloudEventDeletePersistTime().count());
        assertEquals(1, metrics.getCloudEventCreateParseTime().count());
        assertEquals(0, metrics.getCloudEventMergeParseTime().count());
        assertEquals(0, metrics.getCloudEventDeleteParseTime().count());

        assertEquals(0, metrics.getNumUnsuccessfullyParsedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyParsedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyParsedDeleteCloudEvents().count());
        assertEquals(1, metrics.getNumUnsuccessfullyPersistedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyPersistedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyPersistedDeleteCloudEvents().count());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testInvalidGeoLocationAttribute() {
        CloudEvent event = CloudEventTestUtil.getCloudEvent("create", "{}");
        String entityType = "PhysicalNetworkAppliance";
        Map<String, Object> yangParserOutputMap = new HashMap<>();
        yangParserOutputMap.put("geo-location", 0);
        Entity entity = new Entity("", entityType, "id1", yangParserOutputMap, List.of());
        ParsedCloudEventData parsedData = new ParsedCloudEventData(List.of(entity), List.of());
        when(cloudEventParser.getCloudEventData(ArgumentMatchers.any())).thenReturn(parsedData);

        createTopologyProcessor.process(event, anyString());
        verifyNoInteractions(tiesDbService);

        assertEquals(1, metrics.getNumSuccessfullyParsedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyParsedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyParsedDeleteCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumSuccessfullyPersistedDeleteCloudEvents().count());
        assertEquals(0, metrics.getCloudEventCreatePersistTime().count());
        assertEquals(0, metrics.getCloudEventMergePersistTime().count());
        assertEquals(0, metrics.getCloudEventDeletePersistTime().count());
        assertEquals(1, metrics.getCloudEventCreateParseTime().count());
        assertEquals(0, metrics.getCloudEventMergeParseTime().count());
        assertEquals(0, metrics.getCloudEventDeleteParseTime().count());

        assertEquals(0, metrics.getNumUnsuccessfullyParsedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyParsedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyParsedDeleteCloudEvents().count());
        assertEquals(1, metrics.getNumUnsuccessfullyPersistedCreateCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyPersistedMergeCloudEvents().count());
        assertEquals(0, metrics.getNumUnsuccessfullyPersistedDeleteCloudEvents().count());
    }
}
