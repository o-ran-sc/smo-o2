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

import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
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

import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;

import java.util.*;

@Slf4j
@ActiveProfiles({ "test", "exposure" })
@SpringBootTest
public class DataControllerE2EContainerizedTest {

    public static final String ACCEPT_TYPE = "application/yang+json";

    @Autowired
    public DataRestController underTest;
    public static TestPostgresqlContainer postgresSQLContainer = TestPostgresqlContainer.getInstance();

    @Autowired
    DSLContext writeDataDslContext;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.read.jdbc-url", () -> postgresSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.read.username", () -> postgresSQLContainer.getUsername());
        registry.add("spring.datasource.read.password", () -> postgresSQLContainer.getPassword());

        registry.add("spring.datasource.write.jdbc-url", () -> postgresSQLContainer.getJdbcUrl());
        registry.add("spring.datasource.write.username", () -> postgresSQLContainer.getUsername());
        registry.add("spring.datasource.write.password", () -> postgresSQLContainer.getPassword());
    }

    @BeforeAll
    public static void beforeAll() throws UnsupportedOperationException, SchemaLoaderException {
        String url = postgresSQLContainer.getJdbcUrl();
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
    public void getRelationshipsByTypeWithoutScopeFilterTest() {
        final OranTeivRelationshipsResponseMessage responseMessage1 = underTest.getRelationshipsByType(ACCEPT_TYPE, "RAN",
                "NRCELLDU_USES_NRSECTORCARRIER", null, "", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());
        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("bSide", "E49D942C16E0364E1E0788138916D70C");
        innerResponse.put("aSide", "B480427E8A0C0B8D994E437784BB382F");
        innerResponse.put("id",
                "urn:base64:TlJDZWxsRFU6QjQ4MDQyN0U4QTBDMEI4RDk5NEU0Mzc3ODRCQjM4MkY6VVNFUzpOUlNlY3RvckNhcnJpZXI6RTQ5RDk0MkMxNkUwMzY0RTFFMDc4ODEzODkxNkQ3MEM=");
        Map<String, Object> response = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:NRCELLDU_USES_NRSECTORCARRIER", List.of(innerResponse));
        Assertions.assertEquals(response, responseMessage1.getItems().get(0));
    }

    @Test
    public void getRelationshipsByTypeWithScopeFilterTest() {
        final OranTeivRelationshipsResponseMessage responseMessage1 = underTest.getRelationshipsByType(ACCEPT_TYPE, "RAN",
                "SECTOR_GROUPS_NRCELLDU", null, "/NRCellDU/attributes[@cellLocalId=1]", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());
        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("bSide", "98C3A4591A37718E1330F0294E23B62A");
        innerResponse.put("aSide", "F5128C172A70C4FCD4739650B06DE9E2");
        innerResponse.put("id",
                "urn:base64:U2VjdG9yOkY1MTI4QzE3MkE3MEM0RkNENDczOTY1MEIwNkRFOUUyOkdST1VQUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==");
        Map<String, Object> response = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:SECTOR_GROUPS_NRCELLDU", List.of(innerResponse));
        Assertions.assertEquals(response, responseMessage1.getItems().get(0));
    }

    @Test
    public void getTopologyByEntityTypeNameWithScopeFilterTest() {
        final OranTeivEntitiesResponseMessage responseMessage1 = underTest.getTopologyByEntityTypeName(

                ACCEPT_TYPE, "RAN", "NRCellDU", null, "/NRCellDU/attributes[@cellLocalId=1]", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());
        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("id", "98C3A4591A37718E1330F0294E23B62A");

        Map<String, Object> response = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:NRCellDU", List.of(innerResponse));
        Assertions.assertEquals(response, responseMessage1.getItems().get(0));
    }

    @Test
    public void getTopologyByEntityTypeWithoutScopeFilterTest() {
        final OranTeivEntitiesResponseMessage responseMessage1 = underTest.getTopologyByEntityTypeName(

                ACCEPT_TYPE, "RAN", "NRCellDU", null, "", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());

        List<String> ids = new ArrayList<>();
        ids.add("67A1BDA10B5AF43028D07C7BE5CB1AE2");
        ids.add("76E9F605D4F37330BF0B505E94F64F11");
        ids.add("98C3A4591A37718E1330F0294E23B62A");
        ids.add("B3B0A1939EFCA654A37005B6A7F24BD7");
        ids.add("B480427E8A0C0B8D994E437784BB382F");
        ids.add("F9546E82313AC1D5E690DCD7BE55606F");

        List<Map<String, String>> idList = new ArrayList<>();
        for (String id : ids) {
            Map<String, String> idMap = new HashMap<>();
            idMap.put("id", id);
            idList.add(idMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:NRCellDU", idList);
        Assertions.assertEquals(response, responseMessage1.getItems().get(0));
    }

    @Test
    public void getEntitiesByDomainWithoutScopeFilter() {
        final OranTeivEntitiesResponseMessage responseMessage1 = underTest.getEntitiesByDomain(ACCEPT_TYPE, "RAN",
                "/NRCellDU/attributes/nCI", "", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());

        @SuppressWarnings("unchecked") List<Map<String, Object>> actualList = (List<Map<String, Object>>) ((Map<String, Object>) responseMessage1
                .getItems().get(0)).get("o-ran-smo-teiv-ran:NRCellDU");

        List<Map<String, Object>> expectedList = new ArrayList<>();
        Map<String, Object> entity = new HashMap<>();

        entity.put("attributes", Map.of("nCI", 1));
        entity.put("id", "98C3A4591A37718E1330F0294E23B62A");
        expectedList.add(entity);

        entity = new HashMap<>();
        entity.put("attributes", Map.of("nCI", 2));
        entity.put("id", "F9546E82313AC1D5E690DCD7BE55606F");
        expectedList.add(entity);

        entity = new HashMap<>();
        entity.put("attributes", Map.of("nCI", 3));
        entity.put("id", "B480427E8A0C0B8D994E437784BB382F");
        expectedList.add(entity);

        entity = new HashMap<>();
        entity.put("attributes", Map.of("nCI", 91));
        entity.put("id", "76E9F605D4F37330BF0B505E94F64F11");
        expectedList.add(entity);

        entity = new HashMap<>();
        entity.put("attributes", Map.of("nCI", 92));
        entity.put("id", "67A1BDA10B5AF43028D07C7BE5CB1AE2");
        expectedList.add(entity);

        entity = new HashMap<>();
        entity.put("attributes", Map.of("nCI", 93));
        entity.put("id", "B3B0A1939EFCA654A37005B6A7F24BD7");
        expectedList.add(entity);

        Assertions.assertEquals(expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            Map<String, Object> actualEntity = actualList.get(i);
            Map<String, Object> expectedEntity = expectedList.get(i);

            Map<String, Object> actualAttributes = (Map<String, Object>) actualEntity.get("attributes");
            Map<String, Object> expectedAttributes = (Map<String, Object>) expectedEntity.get("attributes");

            Assertions.assertEquals(((Number) expectedAttributes.get("nCI")).intValue(), ((Number) actualAttributes.get(
                    "nCI")).intValue());

        }
    }

    @Test
    public void getEntitiesByDomainWithScopeFilter() {
        final String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU";
        final OranTeivEntitiesResponseMessage responseMessage1 = underTest.getEntitiesByDomain(ACCEPT_TYPE, "RAN",
                "/NRCellDU/attributes/nCI", "/NRCellDU/attributes[@cellLocalId=1]", 0, 20).getBody();

        Assertions.assertNotNull(responseMessage1);
        Assertions.assertEquals(1, responseMessage1.getItems().size());

        Map<String, Object> actualResponseMap = new HashMap<>((Map<String, Object>) responseMessage1.getItems().get(0));

        Map<String, Object> expectedResponse = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("nCI", 1);
        innerMap.put("id", "98C3A4591A37718E1330F0294E23B62A");
        innerMap.put("attributes", attributes);
        expectedResponse.put("o-ran-smo-teiv-ran:NRCellDU", List.of(innerMap));

        Assertions.assertEquals(expectedResponse.toString(), actualResponseMap.toString());
    }
}
