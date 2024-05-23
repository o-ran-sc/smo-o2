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

import org.oran.smo.teiv.schema.MockSchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.service.TiesDbService;
import org.oran.smo.teiv.service.cloudevent.CloudEventParser;
import org.oran.smo.teiv.service.cloudevent.data.Entity;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.startup.SchemaHandler;
import org.oran.smo.teiv.utils.CloudEventTestUtil;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles({ "test", "ingestion" })
class DeleteTopologyProcessorTest {

    @Autowired
    private DeleteTopologyProcessor deleteTopologyProcessor;

    @MockBean
    private CloudEventParser cloudEventParser;
    @MockBean
    private TiesDbService tiesDbService;
    @MockBean
    private SchemaHandler schemaHandler;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        SchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
    }

    @Test
    void testDeleteCloudNativeApplicationEntity1() {
        CloudEvent event = CloudEventTestUtil.getCloudEvent("delete", "{}");
        String entityType = "CloudNativeApplication";
        Map<String, Object> yangParserOutputMapBSide = new HashMap<>();
        Entity entity = new Entity("", entityType, "cloud_id_1", yangParserOutputMapBSide, List.of());

        ParsedCloudEventData parsedData = new ParsedCloudEventData(List.of(entity), List.of());
        when(cloudEventParser.getCloudEventData(any())).thenReturn(parsedData);

        doThrow(new RuntimeException("test error")).when(tiesDbService).execute(anyList());
        Assertions.assertDoesNotThrow(() -> deleteTopologyProcessor.process(event, anyString()));

        Mockito.verify(tiesDbService, times(1)).execute(anyList());
    }

}
