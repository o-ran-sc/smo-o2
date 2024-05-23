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
package org.oran.smo.teiv.e2e;

import org.oran.smo.teiv.api.model.OranTeivDomains;
import org.oran.smo.teiv.api.model.OranTeivDomainsItemsInner;
import org.oran.smo.teiv.api.model.OranTeivEntityTypes;
import org.oran.smo.teiv.api.model.OranTeivEntityTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;

import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.exposure.data.rest.controller.DataRestController;
import org.oran.smo.teiv.schema.PostgresSchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.oran.smo.teiv.utils.TiesConstants.TEIV_DOMAIN;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;

@Slf4j
@ActiveProfiles({ "test", "exposure" })
@SpringBootTest
public class DataControllerE2EContainerizedNonXPathTest {

    public static final String ACCEPT_TYPE = "application/yang+json";

    @Autowired
    public DataRestController underTest;
    public static TestPostgresqlContainer postgreSQLContainer = TestPostgresqlContainer.getInstance();

    @Autowired
    DSLContext writeDataDslContext;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.read.jdbc-url", () -> postgreSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.read.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.datasource.read.password", () -> postgreSQLContainer.getPassword());

        registry.add("spring.datasource.write.jdbc-url", () -> postgreSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.write.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.datasource.write.password", () -> postgreSQLContainer.getPassword());
    }

    @BeforeAll
    public static void beforeAll() throws UnsupportedOperationException, SchemaLoaderException {
        String url = postgreSQLContainer.getJdbcUrl();
        DataSource ds = DataSourceBuilder.create().url(url).username("test").password("test").build();
        DSLContext dslContext = DSL.using(ds, SQLDialect.POSTGRES);
        PostgresSchemaLoader postgresSchemaLoader = new PostgresSchemaLoader(dslContext, new ObjectMapper());
        postgresSchemaLoader.loadSchemaRegistry();
    }

    @BeforeEach
    public void deleteAll() {
        writeDataDslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(
                t -> writeDataDslContext.truncate(t).cascade().execute());
        TestPostgresqlContainer.loadSampleData();
    }

    @Test
    public void testGetAllDomains() {
        final OranTeivDomains responseMessage = underTest.getAllDomains(ACCEPT_TYPE, 0, 20).getBody();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertEquals(9, responseMessage.getItems().size());

        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("name", TEIV_DOMAIN);
        expectedData.put("entityTypes", "/domains/" + TEIV_DOMAIN + "/entity-types");
        expectedData.put("relationshipTypes", "/domains/" + TEIV_DOMAIN + "/relationship-types");

        OranTeivDomainsItemsInner actualItem = responseMessage.getItems().get(8);

        Assertions.assertEquals(expectedData.get("name"), actualItem.getName());
        Assertions.assertEquals(expectedData.get("entityTypes"), actualItem.getEntityTypes().getHref());
        Assertions.assertEquals(expectedData.get("relationshipTypes"), actualItem.getRelationshipTypes().getHref());

        Map<String, String> expectedDataCloud = new HashMap<>();
        expectedDataCloud.put("name", "CLOUD");
        expectedDataCloud.put("entityTypes", "/domains/CLOUD/entity-types");
        expectedDataCloud.put("relationshipTypes", "/domains/CLOUD/relationship-types");

        OranTeivDomainsItemsInner actualItemCloud = responseMessage.getItems().get(0);

        Assertions.assertEquals(expectedDataCloud.get("name"), actualItemCloud.getName());
        Assertions.assertEquals(expectedDataCloud.get("entityTypes"), actualItemCloud.getEntityTypes().getHref());
        Assertions.assertEquals(expectedDataCloud.get("relationshipTypes"), actualItemCloud.getRelationshipTypes()
                .getHref());

        Map<String, String> expectedDataLogicalToEquipment = new HashMap<>();
        expectedDataLogicalToEquipment.put("name", "EQUIPMENT_TO_RAN");
        expectedDataLogicalToEquipment.put("entityTypes", "/domains/EQUIPMENT_TO_RAN/entity-types");
        expectedDataLogicalToEquipment.put("relationshipTypes", "/domains/EQUIPMENT_TO_RAN/relationship-types");

        OranTeivDomainsItemsInner actualItemLogicalToEquipment = responseMessage.getItems().get(3);

        Assertions.assertEquals(expectedDataLogicalToEquipment.get("name"), actualItemLogicalToEquipment.getName());
        Assertions.assertEquals(expectedDataLogicalToEquipment.get("entityTypes"), actualItemLogicalToEquipment
                .getEntityTypes().getHref());
        Assertions.assertEquals(expectedDataLogicalToEquipment.get("relationshipTypes"), actualItemLogicalToEquipment
                .getRelationshipTypes().getHref());
    }

    @Test
    public void testGetTopologyEntityTypes() {
        final OranTeivEntityTypes responseMessage = underTest.getTopologyEntityTypes(ACCEPT_TYPE, "EQUIPMENT_TO_RAN", 0, 20)
                .getBody();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertEquals(20, responseMessage.getItems().size());

        Map<String, String> expectedSite = new HashMap<>();
        expectedSite.put("name", "Site");
        expectedSite.put("entities", "/domains/EQUIPMENT_TO_RAN/entity-types/Site/entities");

        OranTeivEntityTypesItemsInner item1 = responseMessage.getItems().stream().filter(items -> {
            return "Site".equals(items.getName());
        }).findFirst().get();
        Assertions.assertEquals(expectedSite.get("name"), item1.getName());
        Assertions.assertEquals(expectedSite.get("entities"), item1.getEntities().getHref());

        Map<String, String> expectedNRCellCu = new HashMap<>();
        expectedNRCellCu.put("name", "NRCellCU");
        expectedNRCellCu.put("entities", "/domains/EQUIPMENT_TO_RAN/entity-types/NRCellCU/entities");

        OranTeivEntityTypesItemsInner item2 = responseMessage.getItems().stream().filter(items -> {
            return "NRCellCU".equals(items.getName());
        }).findFirst().get();
        Assertions.assertEquals(expectedNRCellCu.get("name"), item2.getName());
        Assertions.assertEquals(expectedNRCellCu.get("entities"), item2.getEntities().getHref());

        Map<String, String> expectedGNBCUUFunction = new HashMap<>();
        expectedGNBCUUFunction.put("name", "GNBCUUPFunction");
        expectedGNBCUUFunction.put("entities", "/domains/EQUIPMENT_TO_RAN/entity-types/GNBCUUPFunction/entities");

        OranTeivEntityTypesItemsInner item3 = responseMessage.getItems().stream().filter(items -> {
            return "GNBCUUPFunction".equals(items.getName());
        }).findFirst().get();
        Assertions.assertEquals(expectedGNBCUUFunction.get("name"), item3.getName());
        Assertions.assertEquals(expectedGNBCUUFunction.get("entities"), item3.getEntities().getHref());
    }

    @Test
    public void testGetTopologyRelationshipTypes() {
        final OranTeivRelationshipTypes responseMessage = underTest.getTopologyRelationshipTypes(ACCEPT_TYPE,
                "EQUIPMENT_TO_RAN", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertEquals(20, responseMessage.getItems().size());

        Map<String, String> expectedData1 = new HashMap<>();
        expectedData1.put("name", "NRCELLDU_USES_NRSECTORCARRIER");
        expectedData1.put("relationships",
                "/domains/EQUIPMENT_TO_RAN/relationship-types/NRCELLDU_USES_NRSECTORCARRIER/relationships");

        OranTeivRelationshipTypesItemsInner item1 = responseMessage.getItems().get(14);
        Assertions.assertEquals(expectedData1.get("name"), item1.getName());
        Assertions.assertEquals(expectedData1.get("relationships"), item1.getRelationships().getHref());

        Map<String, String> expectedData2 = new HashMap<>();
        expectedData2.put("name", "ANTENNAMODULEEEEEEEEEEEE_DEPLOYED_ON_ANTENNAMODULEEEEEEEEEEEEEEE");
        expectedData2.put("relationships",
                "/domains/EQUIPMENT_TO_RAN/relationship-types/ANTENNAMODULEEEEEEEEEEEE_DEPLOYED_ON_ANTENNAMODULEEEEEEEEEEEEEEE/relationships");

        OranTeivRelationshipTypesItemsInner item2 = responseMessage.getItems().get(1);
        Assertions.assertEquals(expectedData2.get("name"), item2.getName());
        Assertions.assertEquals(expectedData2.get("relationships"), item2.getRelationships().getHref());

        Map<String, String> expectedData3 = new HashMap<>();
        expectedData3.put("name", "SECTOR_GROUPS_ANTENNAMODULE");
        expectedData3.put("relationships",
                "/domains/EQUIPMENT_TO_RAN/relationship-types/SECTOR_GROUPS_ANTENNAMODULE/relationships");

        OranTeivRelationshipTypesItemsInner item3 = responseMessage.getItems().get(17);
        Assertions.assertEquals(expectedData3.get("name"), item3.getName());
        Assertions.assertEquals(expectedData3.get("relationships"), item3.getRelationships().getHref());
    }

    @Test
    public void testGetTopologyById() {
        final Object responseMessage = underTest.getTopologyById(ACCEPT_TYPE, "RAN", "Sector",
                "2F445AA5744FA3D230FD6838531F1407").getBody();

        Assertions.assertNotNull(responseMessage);

        Map<String, Object> expectedResponse = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("sectorId", 1);
        attributes.put("geo-location", "POINT(59.4019881 17.9419888)");
        attributes.put("sectorId", 1);
        attributes.put("azimuth", 1);

        innerMap.put("attributes", attributes);
        innerMap.put("id", "2F445AA5744FA3D230FD6838531F1407");
        innerMap.put("sourceIds", Collections.EMPTY_LIST);

        expectedResponse.put("o-ran-smo-teiv-ran:Sector", Collections.singletonList(innerMap));

        Map<String, Object> actualResponse = (Map<String, Object>) responseMessage;

        Assertions.assertEquals(expectedResponse.size(), actualResponse.size());
        for (Map.Entry<String, Object> entry : expectedResponse.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = actualResponse.get(key);
            if (expectedValue instanceof List && actualValue instanceof List) {
                List<?> expectedList = (List<?>) expectedValue;
                List<?> actualList = (List<?>) actualValue;
                Assertions.assertEquals(expectedList.size(), actualList.size());
                for (int i = 0; i < expectedList.size(); i++) {
                    Object expectedMap = expectedList.get(i);
                    Object actualMap = actualList.get(i);
                    Assertions.assertTrue(expectedMap instanceof Map);
                    Assertions.assertTrue(actualMap instanceof Map);
                    String expectedAttributesString = ((Map<String, Object>) expectedMap).get("attributes").toString();
                    String actualAttributesString = ((Map<String, Object>) actualMap).get("attributes").toString();
                    Assertions.assertEquals(expectedAttributesString, actualAttributesString);
                }
            } else {
                Assertions.assertEquals(expectedValue, actualValue);
            }
        }
    }

    @Test
    public void testGetAllRelationshipsForEntityId() {
        final OranTeivRelationshipsResponseMessage responseMessage = underTest.getAllRelationshipsForEntityId(ACCEPT_TYPE,
                "RAN", "Sector", "2F445AA5744FA3D230FD6838531F1407", 0, 10).getBody();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertEquals(0, responseMessage.getTotalCount());
        Assertions.assertTrue(responseMessage.getItems().isEmpty());

        final OranTeivRelationshipsResponseMessage responseMessage2 = underTest.getAllRelationshipsForEntityId(ACCEPT_TYPE,
                "RAN", "GNBDUFunction", "1050570EBB1315E1AE7A9FD5E1400A00", 0, 10).getBody();

        Assertions.assertNotNull(responseMessage2);
        Assertions.assertEquals(1, responseMessage2.getTotalCount());

        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("bSide", "1050570EBB1315E1AE7A9FD5E1400A00");
        innerResponse.put("aSide", "6F02817AFE4D53237DB235EBE5378613");
        innerResponse.put("id",
                "urn:base64:TWFuYWdlZEVsZW1lbnQ6NkYwMjgxN0FGRTRENTMyMzdEQjIzNUVCRTUzNzg2MTM6TUFOQUdFUzpHTkJEVUZ1bmN0aW9uOjEwNTA1NzBFQkIxMzE1RTFBRTdBOUZENUUxNDAwQTAw");
        Map<String, Object> response = new HashMap<>();
        response.put("o-ran-smo-teiv-oam-to-ran:MANAGEDELEMENT_MANAGES_GNBDUFUNCTION", List.of(innerResponse));
        Assertions.assertEquals(response, responseMessage2.getItems().get(0));
    }

    @Test
    public void testGetRelationshipById() {
        final Object responseMessage = underTest.getRelationshipById(ACCEPT_TYPE, "CLOUD",
                "NODECLUSTER_LOCATED_AT_CLOUDSITE",
                "urn:base64:Tm9kZUNsdXN0ZXI6MDE1QzJEREJEN0FDNzIyQjM0RUQ2QTIwRURFRUI5QzM6TE9DQVRFRF9BVDpDbG91ZFNpdGU6MTZFRTE3QUU4OURGMTFCNjlFOTRCM0Y2ODI3QzJDMEU=")
                .getBody();

        Assertions.assertNotNull(responseMessage);
        Assertions.assertTrue(responseMessage instanceof Map);

        Map<String, List<Map<String, Object>>> responseMap = (Map<String, List<Map<String, Object>>>) responseMessage;

        Assertions.assertTrue(responseMap.containsKey("o-ran-smo-teiv-cloud:NODECLUSTER_LOCATED_AT_CLOUDSITE"));
        List<Map<String, Object>> relationshipList = responseMap.get(
                "o-ran-smo-teiv-cloud:NODECLUSTER_LOCATED_AT_CLOUDSITE");

        Assertions.assertNotNull(relationshipList);
        Assertions.assertFalse(relationshipList.isEmpty());

        for (Map<String, Object> relationship : relationshipList) {
            Assertions.assertTrue(relationship.containsKey("bSide"));
            Assertions.assertTrue(relationship.containsKey("aSide"));
            Assertions.assertTrue(relationship.containsKey("id"));
            Assertions.assertTrue(relationship.containsKey("sourceIds"));
        }
    }
}
