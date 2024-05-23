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
package org.oran.smo.teiv.service.cloudevent;

import java.util.List;
import java.util.Map;

import org.oran.smo.teiv.startup.SchemaHandler;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.oran.smo.teiv.service.cloudevent.data.Entity;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;
import org.oran.smo.teiv.utils.CloudEventTestUtil;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
class CloudEventParserTest {

    @Autowired
    private CloudEventParser cloudEventParser;

    @MockBean
    private SchemaHandler schemaHandler;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testParseCloudEventData() {
        final CloudEvent cloudEvent = cloudEventFromJson("src/test/resources/cloudeventdata/common/ce-with-data.json");
        final ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);

        validateEntity(parsedCloudEventData.getEntities().get(0), "o-ran-smo-teiv-ran", "NRCellDU", "entityId_1", 6, Map.of(
                "cellLocalId", 4589L, "nRPCI", 12L, "nRTAC", 310L, "primitiveArray", "[1, 2, 3]", "singleList", "12",
                "jsonObjectArray", "[{\"test2\":\"49\",\"test1\":\"128\"}, {\"test2\":\"50\",\"test1\":\"129\"}]"));

        validateEntity(parsedCloudEventData.getEntities().get(1), "o-ran-smo-teiv-ran", "NRCellDU", "entityId_3", 6, Map.of(
                "cellLocalId", 45891L, "nRPCI", 121L, "nRTAC", 3101L, "primitiveArray", "[1, 2, 3]", "singleList", "121",
                "jsonObjectArray", "[{\"test2\":\"491\",\"test1\":\"1281\"}, {\"test2\":\"501\",\"test1\":\"1291\"}]"));

        validateEntity(parsedCloudEventData.getEntities().get(2), "o-ran-smo-teiv-ran", "NRSectorCarrier", "entityId_2", 4,
                Map.of("arfcnDL", 4590L, "testDouble", 32.5, "testBoolean", true, "cmId",
                        "{\"option1\":\"test_option1\",\"option2\":\"test_option2\"}"));

        final List<Relationship> relationships = parsedCloudEventData.getRelationships();
        Assertions.assertEquals(4, relationships.size());

        Relationship relationship = parsedCloudEventData.getRelationships().get(0);
        assertEquals("o-ran-smo-teiv-ran", relationship.getModule());
        assertEquals("NRCELLDU_USES_NRSECTORCARRIER", relationship.getType());
        assertEquals("entityId_1", relationship.getASide());
        assertEquals("entityId_2", relationship.getBSide());

        relationship = parsedCloudEventData.getRelationships().get(1);
        assertEquals("o-ran-smo-teiv-ran", relationship.getModule());
        assertEquals("NRCELLDU_USES_NRSECTORCARRIER", relationship.getType());
        assertEquals("entityId_3", relationship.getASide());
        assertEquals("entityId_4", relationship.getBSide());

        relationship = parsedCloudEventData.getRelationships().get(2);
        assertEquals("o-ran-smo-teiv-ran", relationship.getModule());
        assertEquals("GNBDUFunctionRealisedByCloudNativeApplication", relationship.getType());
        assertEquals("entityId_5", relationship.getASide());
        assertEquals("entityId_6", relationship.getBSide());

        relationship = parsedCloudEventData.getRelationships().get(3);
        assertEquals("o-ran-smo-teiv-ran", relationship.getModule());
        assertEquals("GNBDUFunctionRealisedByCloudNativeApplication", relationship.getType());
        assertEquals("entityId_5", relationship.getASide());
        assertEquals("entityId_7", relationship.getBSide());
    }

    @Test
    void testEmptyCloudEventData() {
        final CloudEvent cloudEvent = CloudEventTestUtil.getCloudEvent("create", "{}");
        final ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        Assertions.assertTrue(parsedCloudEventData.getEntities().isEmpty());
        Assertions.assertTrue(parsedCloudEventData.getRelationships().isEmpty());
    }

    @Test
    void testNoRelationshipInCloudEvent() {
        final CloudEvent cloudEvent = cloudEventFromJson("src/test/resources/cloudeventdata/common/ce-one-entity.json");
        final ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        validateEntity(parsedCloudEventData.getEntities().get(0), "o-ran-smo-teiv-ran", "NRCellDU", "entityId_1", 3, Map.of(
                "cellLocalId", 4589L, "nRPCI", 12L, "nRTAC", 310L));
        Assertions.assertTrue(parsedCloudEventData.getRelationships().isEmpty());
    }

    @Test
    void testCloudEventDataIsNotAValidJson() {
        final CloudEvent cloudEvent = CloudEventTestUtil.getCloudEvent("create", "{invalidjson");
        final ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        Assertions.assertNull(parsedCloudEventData);
    }

    @Test
    void testEntitiesAndRelationshipsAreArrays() {
        final CloudEvent cloudEvent = CloudEventTestUtil.getCloudEvent("create", "{\"entities\":[],\"relationships\":[]}");
        final ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        assertEquals(new ParsedCloudEventData(List.of(), List.of()), parsedCloudEventData);
    }

    @Test
    void testInvalidYangDataInEvent() {
        final CloudEvent arrayEntitiesCloudEvent = CloudEventTestUtil.getCloudEvent("create",
                "{\"entities\":[{\"some_entity_field\": 54321}]}");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(arrayEntitiesCloudEvent);
        Assertions.assertNull(parsedCloudEventData);

        final CloudEvent objectEntitiesCloudEvent = CloudEventTestUtil.getCloudEvent("create",
                "{\"entities\":{\"some_entity_field\": 54321}}");
        parsedCloudEventData = cloudEventParser.getCloudEventData(objectEntitiesCloudEvent);
        Assertions.assertNull(parsedCloudEventData);

        final CloudEvent arrayRelationshipsCloudEvent = CloudEventTestUtil.getCloudEvent("create",
                "{\"relationships\":[{\"some_relationship_field\": 54321}]}");
        parsedCloudEventData = cloudEventParser.getCloudEventData(arrayRelationshipsCloudEvent);
        Assertions.assertNull(parsedCloudEventData);

        final CloudEvent objectRelationshipsCloudEvent = CloudEventTestUtil.getCloudEvent("create",
                "{\"relationships\":{\"some_relationship_field\": 54321}}");
        parsedCloudEventData = cloudEventParser.getCloudEventData(objectRelationshipsCloudEvent);
        Assertions.assertNull(parsedCloudEventData);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testRelationshipsIsNotAValidYangData() {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEvent("create", "{\"relationships\":{\"a_field\": 123}}");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);

        Assertions.assertNull(parsedCloudEventData);

        cloudEvent = CloudEventTestUtil.getCloudEvent("merge", "{\"relationships\":{\"a_field\": 123}}");
        parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);

        Assertions.assertNull(parsedCloudEventData);

        cloudEvent = CloudEventTestUtil.getCloudEvent("delete", "{\"relationships\":{\"a_field\": 123}}");
        parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);

        Assertions.assertNull(parsedCloudEventData);
    }

    private void validateEntity(final Entity entity, String expectedModuleReference, String expectedEntityName,
            String expectedId, int expectedAttributeCount, Map<String, Object> expectedAttributes) {
        assertEquals(expectedModuleReference, entity.getModule());
        assertEquals(expectedEntityName, entity.getType());
        assertEquals(expectedId, entity.getId());
        final Map<String, Object> attributes = entity.getAttributes();
        assertEquals(expectedAttributeCount, attributes.size());
        for (Map.Entry<String, Object> entry : expectedAttributes.entrySet()) {
            assertTrue(attributes.containsKey(entry.getKey()));
            assertEquals(entry.getValue(), attributes.get(entry.getKey()));
        }
    }

    private CloudEvent cloudEventFromJson(String path) {
        return assertDoesNotThrow(() -> CloudEventTestUtil.getCloudEventFromJsonFile(path),
                "Reading CloudEvent from JSON resulted in error.");

    }
}
