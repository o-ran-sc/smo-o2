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
package org.oran.smo.teiv.exposure.spi.impl;

import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.oran.smo.teiv.utils.TiesConstants.ATTRIBUTES;
import static org.oran.smo.teiv.utils.TiesConstants.ITEMS;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.oran.smo.teiv.utils.exposure.PaginationVerifierTestUtil.verifyResponse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.oran.smo.teiv.schema.DataType;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.spi.mapper.MapperUtility;
import org.oran.smo.teiv.exposure.spi.mapper.PaginationMetaData;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.PostgresSchemaLoader;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.startup.SchemaHandler;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import java.util.HashSet;

@Configuration
@SpringBootTest
class DataPersistenceServiceImplGETRequestsContainerizedTest {
    public static TestPostgresqlContainer postgreSQLContainer = TestPostgresqlContainer.getInstance();
    private static DataPersistanceServiceImpl underTest;
    private static MapperUtility mapperUtility;
    private static DSLContext dslContext;
    private static DSLContext writeDataDslContext;
    private static final String REL_ID_PREFIX = "urn:base64:";

    @MockBean
    private SchemaHandler schemaHandler;

    @BeforeAll
    public static void beforeAll() throws UnsupportedOperationException, SchemaLoaderException {
        String url = postgreSQLContainer.getJdbcUrl();
        DataSource ds = DataSourceBuilder.create().url(url).username("test").password("test").build();
        dslContext = DSL.using(ds, SQLDialect.POSTGRES);
        writeDataDslContext = DSL.using(ds, SQLDialect.POSTGRES);
        mapperUtility = new MapperUtility();
        underTest = new DataPersistanceServiceImpl(dslContext, writeDataDslContext, mapperUtility);
        PostgresSchemaLoader postgresSchemaLoader = new PostgresSchemaLoader(dslContext, new ObjectMapper());
        postgresSchemaLoader.loadSchemaRegistry();
        TestPostgresqlContainer.loadSampleData();
    }

    @BeforeEach
    public void deleteAll() {
        writeDataDslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(
                t -> writeDataDslContext.truncate(t).cascade().execute());
        TestPostgresqlContainer.loadSampleData();
    }

    @Test
    void testGetTopology() {
        Map<String, List<Object>> response = new HashMap<>();
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> attributesMap = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:GNBDUFunction", List.of(responseData));
        attributesMap.put("gNBDUId", null);
        attributesMap.put("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=16");
        attributesMap.put("dUpLMNId", Map.of("mcc", "456", "mnc", "82"));
        attributesMap.put("gNBId", 16L);
        attributesMap.put("gNBIdLength", 2L);
        attributesMap.put("cmId", null);
        responseData.put("attributes", attributesMap);
        responseData.put("id", "5A548EA9D166341776CA0695837E55D8");
        responseData.put("sourceIds", List.of(
                "urn:3gpp:dn:/SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary" + "/GNBDUFunction=16",
                "urn:cmHandle:/395221E080CCF0FD1924103B15873814"));

        Assertions.assertEquals(response, underTest.getTopology(SchemaRegistry.getEntityTypeByName("GNBDUFunction"),
                "5A548EA9D166341776CA0695837E55D8"));
        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getTopology(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunction"), "-1"));

        response.clear();
        responseData.clear();
        attributesMap.clear();

        response.put("o-ran-smo-teiv-ran:AntennaCapability", List.of(responseData));
        attributesMap.put("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/AntennaCapability=1");
        attributesMap.put("eUtranFqBands", List.of("123", "4564", "789"));
        attributesMap.put("geranFqBands", List.of("123", "456", "789"));
        attributesMap.put("nRFqBands", List.of("123", "456", "789"));
        attributesMap.put("cmId", null);
        responseData.put("attributes", attributesMap);
        responseData.put("id", "5835F77BE9D4E102316BD59195F6370B");
        responseData.put("sourceIds", Collections.EMPTY_LIST);

        Assertions.assertEquals(response, underTest.getTopology(SchemaRegistry.getEntityTypeByName("AntennaCapability"),
                "5835F77BE9D4E102316BD59195F6370B"));
    }

    @Test
    void testGetAllRelationships_manyToOneAndOneToMany() {
        List<Map<String, List<Object>>> mapsList = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/NRCellDU/B480427E8A0C0B8D994E437784BB382F/relationships").addPathParameters("eiid",
                        "B480427E8A0C0B8D994E437784BB382F").addPathParameters("domain", "RAN").addPathParameters(
                                "entityType", "NRCellDU").build();

        paginationDTO1.setTotalSize(3);
        response.putAll(new PaginationMetaData().getObjectList(paginationDTO1));

        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/NRCellDU/NON_EXISTING/relationships").addPathParameters("eiid", "NON_EXISTING")
                .addPathParameters("domain", "RAN").addPathParameters("entityType", "NRCellDU").build();

        mapsList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "D3215E08570BE58339C7463626B50E37", "B480427E8A0C0B8D994E437784BB382F",
                REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg=="))));

        mapsList.add(Map.of("o-ran-smo-teiv-ran:SECTOR_GROUPS_NRCELLDU", List.of(generateResponse(
                "F5128C172A70C4FCD4739650B06DE9E2", "B480427E8A0C0B8D994E437784BB382F",
                REL_ID_PREFIX + "U2VjdG9yOkY1MTI4QzE3MkE3MEM0RkNENDczOTY1MEIwNkRFOUUyOkdST1VQUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg=="))));

        mapsList.add(Map.of("o-ran-smo-teiv-ran:NRCELLDU_USES_NRSECTORCARRIER", List.of(generateResponse(
                "B480427E8A0C0B8D994E437784BB382F", "E49D942C16E0364E1E0788138916D70C",
                REL_ID_PREFIX + "TlJDZWxsRFU6QjQ4MDQyN0U4QTBDMEI4RDk5NEU0Mzc3ODRCQjM4MkY6VVNFUzpOUlNlY3RvckNhcnJpZXI6RTQ5RDk0MkMxNkUwMzY0RTFFMDc4ODEzODkxNkQ3MEM="))));

        response.put(ITEMS, mapsList);

        verifyResponse(response, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName("NRCellDU"),
                SchemaRegistry.getRelationTypesByEntityName("NRCellDU"), "B480427E8A0C0B8D994E437784BB382F",
                paginationDTO1));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(SchemaRegistry
                .getEntityTypeByName("NRCellDU"), SchemaRegistry.getRelationTypesByEntityName("NRCellDU"), "NON_EXISTING",
                paginationDTO2));
    }

    @Test
    void testGetAllRelationships_manyToMany() {
        Map<String, Object> response = new HashMap<>();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/GNBCUUPFunction/BFEEAC2CE60273CB0A78319CC201A7FE/relationships").addPathParameters(
                        "eiid", "BFEEAC2CE60273CB0A78319CC201A7FE").addPathParameters("domain", "RAN").addPathParameters(
                                "entityType", "GNBCUUPFunction").build();
        paginationDTO1.setTotalSize(2);
        response.putAll(new PaginationMetaData().getObjectList(paginationDTO1));

        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/NRCellDU/NON_EXISTING/relationships").addPathParameters("eiid", "NON_EXISTING")
                .addPathParameters("domain", "RAN").addPathParameters("entityType", "NRCellDU").build();

        List<Map<String, List<Object>>> mapsList = new ArrayList<>();

        mapsList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", List.of(
                generateResponse("BFEEAC2CE60273CB0A78319CC201A7FE", "AD42D90497E93D276215DF6D3B899E17",
                        REL_ID_PREFIX + "R05CQ1VVUEZ1bmN0aW9uOkJGRUVBQzJDRTYwMjczQ0IwQTc4MzE5Q0MyMDFBN0ZFOlJFQUxJU0VEX0JZOkNsb3VkTmF0aXZlQXBwbGljYXRpb246QUQ0MkQ5MDQ5N0U5M0QyNzYyMTVERjZEM0I4OTlFMTc="))));

        mapsList.add(Map.of("o-ran-smo-teiv-oam-to-ran:MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION", List.of(generateResponse(
                "E64371CD4D12ED0CED200DD3A7591784", "BFEEAC2CE60273CB0A78319CC201A7FE",
                REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6RTY0MzcxQ0Q0RDEyRUQwQ0VEMjAwREQzQTc1OTE3ODQ6TUFOQUdFUzpHTkJDVVVQRnVuY3Rpb246QkZFRUFDMkNFNjAyNzNDQjBBNzgzMTlDQzIwMUE3RkU="))));

        response.put(ITEMS, mapsList);

        verifyResponse(response, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName("GNBCUUPFunction"),
                SchemaRegistry.getRelationTypesByEntityName("GNBCUUPFunction"), "BFEEAC2CE60273CB0A78319CC201A7FE",
                paginationDTO1));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(SchemaRegistry
                .getEntityTypeByName("GNBCUUPFunction"), SchemaRegistry.getRelationTypesByEntityName("GNBCUUPFunction"),
                "NON_EXISTING", paginationDTO2));
    }

    @Test
    void testGetAllRelationshipsManyToMany_filteredByRelationship() {
        List<Map<String, List<Object>>> mapsList = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();

        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/GNBCUUPFunction/BFEEAC2CE60273CB0A78319CC201A7FE/relationships").addPathParameters(
                        "eiid", "BFEEAC2CE60273CB0A78319CC201A7FE").addPathParameters("domain", "RAN").addPathParameters(
                                "entityType", "GNBCUUPFunction").build();
        paginationDTO1.setTotalSize(1);
        paginationDTO1.setQueryParameters(Map.of("relationshipType", "MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION"));

        response.putAll(new PaginationMetaData().getObjectList(paginationDTO1));
        mapsList.add(Map.of("o-ran-smo-teiv-oam-to-ran:MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION", List.of(generateResponse(
                "E64371CD4D12ED0CED200DD3A7591784", "BFEEAC2CE60273CB0A78319CC201A7FE",
                REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6RTY0MzcxQ0Q0RDEyRUQwQ0VEMjAwREQzQTc1OTE3ODQ6TUFOQUdFUzpHTkJDVVVQRnVuY3Rpb246QkZFRUFDMkNFNjAyNzNDQjBBNzgzMTlDQzIwMUE3RkU="))));
        mapsList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", List.of(
                generateResponse("BFEEAC2CE60273CB0A78319CC201A7FE", "AD42D90497E93D276215DF6D3B899E17",
                        REL_ID_PREFIX + "R05CQ1VVUEZ1bmN0aW9uOkJGRUVBQzJDRTYwMjczQ0IwQTc4MzE5Q0MyMDFBN0ZFOlJFQUxJU0VEX0JZOkNsb3VkTmF0aXZlQXBwbGljYXRpb246QUQ0MkQ5MDQ5N0U5M0QyNzYyMTVERjZEM0I4OTlFMTc="))));

        response.put(ITEMS, List.of(mapsList.get(0)));
        verifyResponse(response, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName("GNBCUUPFunction"), List
                .of(SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION")),
                "BFEEAC2CE60273CB0A78319CC201A7FE", paginationDTO1));

        response.replace(ITEMS, List.of(mapsList.get(0)), List.of(mapsList.get(1)));
        paginationDTO1.setQueryParameters(Map.of("relationshipType", "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION"));
        response.putAll(new PaginationMetaData().getObjectList(paginationDTO1));
        verifyResponse(response, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName("GNBCUUPFunction"), List
                .of(SchemaRegistry.getRelationTypeByName("GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION")),
                "BFEEAC2CE60273CB0A78319CC201A7FE", paginationDTO1));

        paginationDTO1.setQueryParameters(Map.of("relationshipType", "NON_EXISTING"));
        response.putAll(new PaginationMetaData().getObjectList(paginationDTO1));
        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(SchemaRegistry
                .getEntityTypeByName("GNBCUUPFunction"), List.of(SchemaRegistry.getRelationTypeByName(
                        "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION")), "NON_EXISTING", paginationDTO1));
    }

    @Test
    void testGetAllRelationshipsConnectingSameEntity_filteredByRelationship() {
        String idConnectingSameEntity = REL_ID_PREFIX + "QW50ZW5uYU1vZHVsZToyNzhBMDVDNjdENDdEMTE3QzJEQzVCREY1RTAwQUU3MDpSRUFMSVNFRF9CWTpBbnRlbm5hTW9kdWxlOjI3OEEwNUM2N0Q0N0QxMTdDMkRDNUJERjVFMDBBRTcwCg==";
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN_EQUIPMENT/entities/AntennaModule/278A05C67D47D117C2DC5BDF5E00AE70/relationships")
                .addPathParameters("eiid", "278A05C67D47D117C2DC5BDF5E00AE70").addPathParameters("domain", "RAN_EQUIPMENT")
                .addPathParameters("entityType", "AntennaModule").build();
        paginationDTO1.setTotalSize(1);
        paginationDTO1.setQueryParameters(Map.of("relationshipType", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE"));

        Map<String, Object> response = new HashMap<>(new PaginationMetaData().getObjectList(paginationDTO1));
        Map<String, List<Object>> mapsList = Map.of("o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", List
                .of(generateResponse("278A05C67D47D117C2DC5BDF5E00AE70", "279A05C67D47D117C2DC5BDF5E00AE70",
                        idConnectingSameEntity)));
        response.put(ITEMS, List.of(mapsList));

        RelationType relSameEntity = SchemaRegistry.getRelationTypeByName("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");
        EntityType entity = SchemaRegistry.getEntityTypeByName("AntennaModule");

        Assertions.assertNotNull(relSameEntity);
        Assertions.assertNotNull(entity);

        verifyResponse(response, underTest.getAllRelationships(entity, List.of(relSameEntity),
                "278A05C67D47D117C2DC5BDF5E00AE70", paginationDTO1));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(entity, List.of(
                relSameEntity), "NON_EXISTING", paginationDTO1));
    }

    @Test
    void testGetRelationshipWithSpecifiedId() {
        String idOneToMany = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==";
        Map<String, List<Object>> relationshipOneToMany = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List
                .of(generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A", idOneToMany,
                        Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipOneToMany, underTest.getRelationshipWithSpecifiedId(idOneToMany, SchemaRegistry
                .getRelationTypeByName("GNBDUFUNCTION_PROVIDES_NRCELLDU")));

        String idManyToOne = REL_ID_PREFIX + "Tm9kZUNsdXN0ZXI6MDE1QzJEREJEN0FDNzIyQjM0RUQ2QTIwRURFRUI5QzM6TE9DQVRFRF9BVDpDbG91ZFNpdGU6MTZFRTE3QUU4OURGMTFCNjlFOTRCM0Y2ODI3QzJDMEU=";
        Map<String, List<Object>> relationshipManyToOne = Map.of("o-ran-smo-teiv-cloud:NODECLUSTER_LOCATED_AT_CLOUDSITE",
                List.of(generateResponse("015C2DDBD7AC722B34ED6A20EDEEB9C3", "16EE17AE89DF11B69E94B3F6827C2C0E",
                        idManyToOne, Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipManyToOne, underTest.getRelationshipWithSpecifiedId(idManyToOne, SchemaRegistry
                .getRelationTypeByName("NODECLUSTER_LOCATED_AT_CLOUDSITE")));

        String idManyToMany = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE";
        Map<String, List<Object>> relationshipManyToMany = Map.of(
                "o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", List.of(generateResponse(
                        "4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D", idManyToMany,
                        Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipManyToMany, underTest.getRelationshipWithSpecifiedId(idManyToMany,
                SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION")));

        String idConnectingSameEntity = REL_ID_PREFIX + "QW50ZW5uYU1vZHVsZToyNzhBMDVDNjdENDdEMTE3QzJEQzVCREY1RTAwQUU3MDpSRUFMSVNFRF9CWTpBbnRlbm5hTW9kdWxlOjI3OEEwNUM2N0Q0N0QxMTdDMkRDNUJERjVFMDBBRTcwCg==";
        Map<String, List<Object>> relationshipConnectingSameEntity = Map.of(
                "o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", List.of(generateResponse(
                        "278A05C67D47D117C2DC5BDF5E00AE70", "279A05C67D47D117C2DC5BDF5E00AE70", idConnectingSameEntity,
                        Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipConnectingSameEntity, underTest.getRelationshipWithSpecifiedId(
                idConnectingSameEntity, SchemaRegistry.getRelationTypeByName("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE")));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getRelationshipWithSpecifiedId("NOT_EXISTING",
                SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_PROVIDES_NRCELLDU")));
    }

    @Test
    void testGetRelationshipsByType() {
        Map<String, Object> oneToManyResult = new HashMap<>();
        Map<String, Object> manyToOneResult = new HashMap<>();
        Map<String, Object> manyToManyResult = new HashMap<>();

        Map<String, Object> oneToManyQueryMap = new HashMap<>();
        oneToManyQueryMap.put("query", Map.of("scopeFilter", "/attributes[@cellLocalId=2]"));
        oneToManyResult.putAll(oneToManyQueryMap);

        oneToManyResult.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
                generateResponse("D3215E08570BE58339C7463626B50E37", "F9546E82313AC1D5E690DCD7BE55606F",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTpGOTU0NkU4MjMxM0FDMUQ1RTY5MERDRDdCRTU1NjA2Rg==")))));

        PaginationMetaData metadataOneToMany = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/attributes[@cellLocalId=2]").build();
        paginationDTO1.setTotalSize(1);
        oneToManyResult.putAll(metadataOneToMany.getObjectList(paginationDTO1));

        Map<String, Object> manyToOneQueryMap = new HashMap<>();
        manyToOneQueryMap.put("query", Map.of("scopeFilter", "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]"));
        manyToOneResult.putAll(manyToOneQueryMap);

        manyToOneResult.put("items", List.of(Map.of("o-ran-smo-teiv-cloud:CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE",
                List.of(generateResponse("719BD5C7CD8A939D76A83DA95DA45C01", "1C02E96B2AAE036C7AE404BC38C308E0",
                        REL_ID_PREFIX + "Q2xvdWROYXRpdmVBcHBsaWNhdGlvbjo3MTlCRDVDN0NEOEE5MzlENzZBODNEQTk1REE0NUMwMTpERVBMT1lFRF9PTjpOYW1lc3BhY2U6MUMwMkU5NkIyQUFFMDM2QzdBRTQwNEJDMzhDMzA4RTA=")))));

        PaginationMetaData metadataManyToOne = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]").build();
        paginationDTO2.setTotalSize(1);
        manyToOneResult.putAll(metadataManyToOne.getObjectList(paginationDTO2));

        Map<String, Object> manyToManyQueryMap = new HashMap<>();
        manyToManyQueryMap.put("query", Map.of("scopeFilter",
                "/CloudNativeApplication/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"]"));
        manyToManyResult.putAll(manyToManyQueryMap);

        manyToManyResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", List.of(generateResponse(
                        "4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE")))));

        PaginationMetaData metadataManyToMany = new PaginationMetaData();
        PaginationDTO paginationDTO3 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/CloudNativeApplication/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"]").build();
        paginationDTO3.setTotalSize(1);
        manyToManyResult.putAll(metadataManyToMany.getObjectList(paginationDTO3));

        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        RelationType manyToOne = SchemaRegistry.getRelationTypeByName("CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE");
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");

        verifyResponse(oneToManyResult, underTest.getRelationshipsByType(oneToMany, "/attributes[@cellLocalId=2]",
                PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter", "/attributes[@cellLocalId=2]")
                        .build()));

        verifyResponse(manyToOneResult, underTest.getRelationshipsByType(manyToOne,
                "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]", PaginationDTO.builder().offset(0).limit(5)
                        .addQueryParameters("scopeFilter", "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]").build()));

        verifyResponse(manyToManyResult, underTest.getRelationshipsByType(manyToMany,
                "/CloudNativeApplication/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"]", PaginationDTO.builder().offset(0)
                        .limit(5).addQueryParameters("scopeFilter",
                                "/CloudNativeApplication/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"]").build()));

        assertThatThrownBy(() -> underTest.getRelationshipsByType(oneToMany, "/attributes[@celllocalid=2]", PaginationDTO
                .builder().offset(0).limit(5).build())).isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(oneToMany, null, PaginationDTO.builder()
                .offset(0).limit(5).build()));
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(oneToMany,
                "/GNBDUFunction/attributes[contains (@fdn, \"GNBDUFunction=16\")]", PaginationDTO.builder().offset(0).limit(
                        5).build()));
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(manyToMany,
                "/CloudNativeApplication/attributes[@name=\"Example Cloud App/10\"] | /GNBDUFunction/id[@id=\"5A548EA9D166341776CA0695837E55D8\"]",
                PaginationDTO.builder().offset(0).limit(5).build()));
    }

    @Test
    void testGetRelationshipsByType_relConnectingSameEntity() {

        Map<String, Object> resultOneScopeFilter = new HashMap<>();
        Map<String, Object> resultTwoScopeFilter = new HashMap<>();
        RelationType relationshipConnectingSameEntity = SchemaRegistry.getRelationTypeByName(
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");
        String idConnectingSameEntity = REL_ID_PREFIX + "QW50ZW5uYU1vZHVsZToyNzhBMDVDNjdENDdEMTE3QzJEQzVCREY1RTAwQUU3MDpSRUFMSVNFRF9CWTpBbnRlbm5hTW9kdWxlOjI3OEEwNUM2N0Q0N0QxMTdDMkRDNUJERjVFMDBBRTcwCg==";
        PaginationMetaData metadataRelConnectingSameEntity = new PaginationMetaData();

        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]").build();
        paginationDTO1.setTotalSize(1);
        resultOneScopeFilter.putAll(metadataRelConnectingSameEntity.getObjectList(paginationDTO1));

        Map<String, Object> queryMapOneScope = new HashMap<>();
        queryMapOneScope.put("query", Map.of("scopeFilter", "/AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]"));
        resultOneScopeFilter.putAll(queryMapOneScope);

        resultOneScopeFilter.put("items", List.of(Map.of("o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE",
                List.of(generateResponse("278A05C67D47D117C2DC5BDF5E00AE70", "279A05C67D47D117C2DC5BDF5E00AE70",
                        idConnectingSameEntity)))));

        verifyResponse(resultOneScopeFilter, underTest.getRelationshipsByType(relationshipConnectingSameEntity,
                "/AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]", PaginationDTO.builder().offset(0).limit(5)
                        .addQueryParameters("scopeFilter", "/AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]")
                        .build()));

        Map<String, Object> queryMapTwoScopes = new HashMap<>();
        queryMapTwoScopes.put("query", Map.of("scopeFilter",
                "/AntennaModule/id[@id=\"278A05C67D47D117C2DC5BDF5E00AE70\"] | /AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]"));
        resultTwoScopeFilter.putAll(queryMapTwoScopes);

        resultTwoScopeFilter.put("items", List.of(Map.of("o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE",
                List.of(generateResponse("278A05C67D47D117C2DC5BDF5E00AE70", "279A05C67D47D117C2DC5BDF5E00AE70",
                        idConnectingSameEntity)))));

        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/AntennaModule/id[@id=\"278A05C67D47D117C2DC5BDF5E00AE70\"] | /AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]")
                .build();
        paginationDTO2.setTotalSize(1);
        resultTwoScopeFilter.putAll(metadataRelConnectingSameEntity.getObjectList(paginationDTO2));

        verifyResponse(resultTwoScopeFilter, underTest.getRelationshipsByType(relationshipConnectingSameEntity,
                "/AntennaModule/id[@id=\"278A05C67D47D117C2DC5BDF5E00AE70\"]| /AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]",
                PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                        "/AntennaModule/id[@id=\"278A05C67D47D117C2DC5BDF5E00AE70\"] | /AntennaModule/id[@id=\"279A05C67D47D117C2DC5BDF5E00AE70\"]")
                        .build()));

    }

    @Test
    void testGetTopologyByType() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> queryMap = new HashMap<>();

        queryMap.put("query", Map.of("targetFilter", "/attributes(fdn)", "scopeFilter",
                "/attributes[contains(@fdn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']"));

        result.putAll(queryMap);
        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/RAN/entities/GNBDUFunction").offset(0)
                .limit(2).addQueryParameters("targetFilter", "/attributes(fdn)").addQueryParameters("scopeFilter",
                        "/attributes[contains(@fdn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']").build();
        paginationDTO.setTotalSize(2);
        result.putAll(new PaginationMetaData().getObjectList(paginationDTO));

        result.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of(ATTRIBUTES, Map.of("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=13"), "id",
                "25E690E22BDA90B9C4FEE1F083CBA597"), Map.of(ATTRIBUTES, Map.of("fdn",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=14"),
                        "id", "5A3085C3400C3096E2ED2321452766B1")))));

        verifyResponse(result, underTest.getTopologyByType("GNBDUFunction", "/attributes(fdn)",
                "/attributes[contains(@fdn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']", PaginationDTO.builder()
                        .offset(0).limit(2).basePath("/domains/RAN/entities/GNBDUFunction").addQueryParameters(
                                "targetFilter", "/attributes(fdn)").addQueryParameters("scopeFilter",
                                        "/attributes[contains(@fdn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']")
                        .build()));

        Assertions.assertThrows(TiesPathException.class, () -> underTest.getTopologyByType("GNBDUFunction",
                "/attributes(fdn)", "/attributes[contains(@fdn, \"1000493\")] ; /attributes[@gNBId=4000259]", PaginationDTO
                        .builder().offset(0).limit(2).build()));
    }

    @Test
    void testGetEntitiesByDomain() {
        Map<String, Object> reference1 = new HashMap<>();
        Map<String, Object> query = new HashMap<>();

        query.put("targetFilter", "/attributes(azimuth)");
        query.put("scopeFilter", "/attributes[@azimuth=3]");

        PaginationMetaData pmd1 = new PaginationMetaData();
        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter", "/attributes(azimuth)").addQueryParameters("scopeFilter",
                        "/attributes[@azimuth=3]").build();
        paginationDTO.setTotalSize(1);
        reference1.putAll(pmd1.getObjectList(paginationDTO));
        reference1.put("query", query);

        BigDecimal azimuth = new BigDecimal(3);
        reference1.put("items", List.of(Map.of("o-ran-smo-teiv-ran:Sector", List.of(Map.of("id",
                "ADB1BAAC878C0BEEFE3175C60F44BB1D", ATTRIBUTES, Map.of("azimuth", azimuth))))));
        verifyResponse(reference1, underTest.getEntitiesByDomain("RAN", "/attributes(azimuth)", "/attributes[@azimuth=3]",
                paginationDTO));

        Map<String, Object> reference2 = new HashMap<>();
        Map<String, Object> query2 = new HashMap<>();

        query2.put("targetFilter", "/id");
        query2.put("scopeFilter", "/id[contains(@id,\"F1407\")]");

        PaginationMetaData pmd2 = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath("/domains/RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter", "/id").addQueryParameters("scopeFilter", "/id[contains(@id,\"F1407\")]")
                .build();
        paginationDTO2.setTotalSize(1);
        pmd2.getObjectList(paginationDTO2);

        reference2.put("query", query2);
        PaginationDTO paginationDTO3 = PaginationDTO.builder().basePath("/domains/RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter", "/id").addQueryParameters("scopeFilter", "/id[contains(@id,\"F1407\")]")
                .build();
        paginationDTO3.setTotalSize(1);
        reference2.putAll(pmd2.getObjectList(paginationDTO3));

        reference2.put("items", List.of(Map.of("o-ran-smo-teiv-ran:Sector", List.of(Map.of("id",
                "2F445AA5744FA3D230FD6838531F1407")))));

        verifyResponse(reference2, underTest.getEntitiesByDomain("RAN", "/id", "/id[contains(@id,\"F1407\")]",
                paginationDTO2));

        Map<String, Object> reference3 = new HashMap<>();
        Map<String, Object> query3 = new HashMap<>();

        query3.put("targetFilter", "/GNBDUFunction/attributes/fdn");
        query3.put("scopeFilter", "/GNBDUFunction/attributes[contains(@fdn,\"GNBDUFunction=10\")]");

        PaginationMetaData pmd3 = new PaginationMetaData();

        reference3.put("query", query3);
        PaginationDTO paginationDTO4 = PaginationDTO.builder().basePath("/domains/RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter", "/GNBDUFunction/attributes/fdn").addQueryParameters("scopeFilter",
                        "/GNBDUFunction/attributes[contains(@fdn,\"GNBDUFunction=10\")]").build();
        paginationDTO4.setTotalSize(1);
        reference3.putAll(pmd3.getObjectList(paginationDTO4));

        reference3.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                "1050570EBB1315E1AE7A9FD5E1400A00", ATTRIBUTES, Map.of("fdn",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=10"))))));

        verifyResponse(reference3, underTest.getEntitiesByDomain("RAN", "/GNBDUFunction/attributes/fdn",
                "/GNBDUFunction/attributes[contains(@fdn,\"GNBDUFunction=10\")]", paginationDTO4));

        Object result = underTest.getEntitiesByDomain("OAM_TO_RAN", "/id", "", PaginationDTO.builder().offset(0).limit(500)
                .build());
        Assertions.assertEquals(10, ((Map) ((List) ((HashMap) result).get("items")).get(0)).size());
        Assertions.assertTrue(((Map) ((List) ((HashMap) result).get("items")).get(0)).containsKey(
                "o-ran-smo-teiv-oam:ManagedElement"));

        Assertions.assertTrue(((Map) ((List) ((HashMap) result).get("items")).get(0)).containsKey(
                "o-ran-smo-teiv-ran:GNBDUFunction"));
    }

    @Test
    void testGetTopologyWithLongNames() {
        Map<String, List<Object>> response = new HashMap<>();
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> attributesMap = new HashMap<>();
        response.put("o-ran-smo-teiv-ran:GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", List.of(
                responseData));
        attributesMap.put("gNBDUId", null);
        attributesMap.put("fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=16");
        attributesMap.put("dUpLMNId", Map.of("mcc", "456", "mnc", "82"));
        attributesMap.put("gNBId", 16L);
        attributesMap.put("gNBIdLength", 2L);
        attributesMap.put("cmId", null);
        responseData.put("attributes", attributesMap);
        responseData.put("id", "5A548EA9D166341776CA0695837E55D8");
        responseData.put("sourceIds", Collections.EMPTY_LIST);

        Assertions.assertEquals(response, underTest.getTopology(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), "5A548EA9D166341776CA0695837E55D8"));
        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getTopology(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), "-1"));
    }

    @Test
    void testGetAllRelationshipsWithLongNames_manyToOne_oneToMany_manyToMany() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, List<Object>>> mapsList = new ArrayList<>();

        PaginationDTO paginationDTO = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/4CFF136200A2DE36205A13559C55DB2A/relationships")
                .addPathParameters("eiid", "4CFF136200A2DE36205A13559C55DB2A").addPathParameters("domain", "RAN")
                .addPathParameters("entityType", "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                .build();
        paginationDTO.setTotalSize(6);
        response.putAll(new PaginationMetaData().getObjectList(paginationDTO));

        mapsList.add(Map.of("o-ran-smo-teiv-oam-to-ran:MANAGEDELEMENTTTTTTTTTTTTTTT_MANAGES_GNBDUFUNCTIONNNNNNNNNNNNNNN",
                List.of(generateResponse("8D51EFC759166044DACBCA63C4EDFC51", "4CFF136200A2DE36205A13559C55DB2A",
                        REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6OEQ1MUVGQzc1OTE2NjA0NERBQ0JDQTYzQzRFREZDNTE6TUFOQUdFUzpHTkJEVUZ1bmN0aW9uOjRDRkYxMzYyMDBBMkRFMzYyMDVBMTM1NTlDNTVEQjJB"))));

        mapsList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU", List.of(
                generateResponse("4CFF136200A2DE36205A13559C55DB2A", "76E9F605D4F37330BF0B505E94F64F11",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpQUk9WSURFUzpOUkNlbGxEVTo3NkU5RjYwNUQ0RjM3MzMwQkYwQjUwNUU5NEY2NEYxMQ=="),
                generateResponse("4CFF136200A2DE36205A13559C55DB2A", "67A1BDA10B5AF43028D07C7BE5CB1AE2",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpQUk9WSURFUzpOUkNlbGxEVTo2N0ExQkRBMTBCNUFGNDMwMjhEMDdDN0JFNUNCMUFFMg=="),
                generateResponse("4CFF136200A2DE36205A13559C55DB2A", "B3B0A1939EFCA654A37005B6A7F24BD7",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpQUk9WSURFUzpOUkNlbGxEVTpCM0IwQTE5MzlFRkNBNjU0QTM3MDA1QjZBN0YyNEJENw=="),
                generateResponse("4CFF136200A2DE36205A13559C55DB2A", "F26F279E91D0941DB4F646E707EA403A",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpQUk9WSURFUzpOUkNlbGxEVTpGMjZGMjc5RTkxRDA5NDFEQjRGNjQ2RTcwN0VBNDAzQQ=="))));

        mapsList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN",
                List.of(generateResponse("4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE"))));

        response.put(ITEMS, mapsList);

        verifyResponse(response, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), SchemaRegistry
                        .getRelationTypesByEntityName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "4CFF136200A2DE36205A13559C55DB2A", paginationDTO));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(SchemaRegistry
                .getEntityTypeByName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), SchemaRegistry
                        .getRelationTypesByEntityName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "NON_EXISTING", paginationDTO));
    }

    @Test
    void testGetAllRelationshipsWithLongNames_filteredByRelationship() {
        List<Map<String, List<Object>>> mapsList = new ArrayList<>();

        //One_To_One
        Map<String, Object> expectedResponse1 = new HashMap<>();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN_OAM/entities/ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt/45EF31D8A1FD624D7276390A1215BFC3/relationships")
                .addPathParameters("eiid", "45EF31D8A1FD624D7276390A1215BFC3").addPathParameters("domain", "RAN")
                .addPathParameters("entityType", "ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt")
                .build();
        paginationDTO1.setTotalSize(1);
        paginationDTO1.setQueryParameters(Map.of("relationshipType",
                "MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM"));
        expectedResponse1.putAll(new PaginationMetaData().getObjectList(paginationDTO1));
        mapsList.add(Map.of("o-ran-smo-teiv-oam-to-cloud:MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM",
                List.of(generateResponse("45EF31D8A1FD624D7276390A1215BFC3", "C4E311A55666726FD9FE25CA572AFAF9",
                        REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6NDVFRjMxRDhBMUZENjI0RDcyNzYzOTBBMTIxNUJGQzM6REVQTE9ZRURfQVM6Q2xvdWROYXRpdmVTeXN0ZW06QzRFMzExQTU1NjY2NzI2RkQ5RkUyNUNBNTcyQUZBRjk="))));
        expectedResponse1.put(ITEMS, List.of(mapsList.get(0)));
        verifyResponse(expectedResponse1, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName(
                "ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt"), List.of(SchemaRegistry
                        .getRelationTypeByName("MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM")),
                "45EF31D8A1FD624D7276390A1215BFC3", paginationDTO1));

        //One_To_Many
        Map<String, Object> expectedResponse2 = new HashMap<>();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/45EF31D8A1FD624D7276390A1215BFC3/relationships")
                .addPathParameters("eiid", "45EF31D8A1FD624D7276390A1215BFC3").addPathParameters("domain", "RAN")
                .addPathParameters("entityType", "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                .build();
        paginationDTO2.setTotalSize(1);
        paginationDTO2.setQueryParameters(Map.of("relationshipType",
                "MANAGEDELEMENTTTTTTTTTTTTTTT_MANAGES_GNBDUFUNCTIONNNNNNNNNNNNNNN"));
        expectedResponse2.putAll(new PaginationMetaData().getObjectList(paginationDTO2));
        mapsList.add(Map.of("o-ran-smo-teiv-oam-to-ran:MANAGEDELEMENTTTTTTTTTTTTTTT_MANAGES_GNBDUFUNCTIONNNNNNNNNNNNNNN",
                List.of(generateResponse("8D51EFC759166044DACBCA63C4EDFC51", "4CFF136200A2DE36205A13559C55DB2A",
                        REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6OEQ1MUVGQzc1OTE2NjA0NERBQ0JDQTYzQzRFREZDNTE6TUFOQUdFUzpHTkJEVUZ1bmN0aW9uOjRDRkYxMzYyMDBBMkRFMzYyMDVBMTM1NTlDNTVEQjJB"))));
        expectedResponse2.put(ITEMS, List.of(mapsList.get(1)));
        verifyResponse(expectedResponse2, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), List.of(SchemaRegistry
                        .getRelationTypeByName("MANAGEDELEMENTTTTTTTTTTTTTTT_MANAGES_GNBDUFUNCTIONNNNNNNNNNNNNNN")),
                "4CFF136200A2DE36205A13559C55DB2A", paginationDTO2));

        //Many_To_One
        Map<String, Object> expectedResponse3 = new HashMap<>();
        PaginationDTO paginationDTO3 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN_CLOUD/entities/Namespaceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee/719BD5C7CD8A939D76A83DA95DA45C01/relationships")
                .addPathParameters("eiid", "719BD5C7CD8A939D76A83DA95DA45C01").addPathParameters("domain", "RAN_CLOUD")
                .addPathParameters("entityType", "Namespaceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                .build();
        paginationDTO3.setTotalSize(1);
        paginationDTO3.setQueryParameters(Map.of("relationshipType",
                "CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE"));
        expectedResponse3.putAll(new PaginationMetaData().getObjectList(paginationDTO3));
        mapsList.add(Map.of("o-ran-smo-teiv-cloud:CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE", List
                .of(generateResponse("719BD5C7CD8A939D76A83DA95DA45C01", "1C02E96B2AAE036C7AE404BC38C308E0",
                        REL_ID_PREFIX + "Q2xvdWROYXRpdmVBcHBsaWNhdGlvbjo3MTlCRDVDN0NEOEE5MzlENzZBODNEQTk1REE0NUMwMTpERVBMT1lFRF9PTjpOYW1lc3BhY2U6MUMwMkU5NkIyQUFFMDM2QzdBRTQwNEJDMzhDMzA4RTA="))));
        expectedResponse3.put(ITEMS, List.of(mapsList.get(2)));
        verifyResponse(expectedResponse3, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName(
                "Namespaceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"), List.of(SchemaRegistry
                        .getRelationTypeByName("CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE")),
                "1C02E96B2AAE036C7AE404BC38C308E0", paginationDTO3));

        //Many_To_Many
        Map<String, Object> expectedResponse4 = new HashMap<>();
        PaginationDTO paginationDTO4 = PaginationDTO.builder().offset(0).limit(100).basePath(
                "/domains/RAN/entities/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/4CFF136200A2DE36205A13559C55DB2A/relationships")
                .addPathParameters("eiid", "4CFF136200A2DE36205A13559C55DB2A").addPathParameters("domain", "CLOUD_TO_RAN")
                .addPathParameters("entityType", "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                .build();
        paginationDTO4.setTotalSize(1);
        paginationDTO4.setQueryParameters(Map.of("relationshipType",
                "GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN"));
        expectedResponse4.putAll(new PaginationMetaData().getObjectList(paginationDTO4));
        mapsList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN",
                List.of(generateResponse("4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D",
                        REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE"))));
        expectedResponse4.put(ITEMS, List.of(mapsList.get(3)));
        verifyResponse(expectedResponse4, underTest.getAllRelationships(SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), List.of(SchemaRegistry
                        .getRelationTypeByName("GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN")),
                "4CFF136200A2DE36205A13559C55DB2A", paginationDTO4));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getAllRelationships(SchemaRegistry
                .getEntityTypeByName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"), List.of(
                        SchemaRegistry.getRelationTypeByName(
                                "GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN")), "NON_EXISTING",
                paginationDTO4));
    }

    @Test
    void testGetRelationshipWithSpecifiedIdWithLongNames() {
        String idOneToOne = REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6NDVFRjMxRDhBMUZENjI0RDcyNzYzOTBBMTIxNUJGQzM6REVQTE9ZRURfQVM6Q2xvdWROYXRpdmVTeXN0ZW06QzRFMzExQTU1NjY2NzI2RkQ5RkUyNUNBNTcyQUZBRjk=";
        Map<String, List<Object>> relationshipOneToOne = Map.of(
                "o-ran-smo-teiv-oam-to-cloud:MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM", List.of(
                        generateResponse("45EF31D8A1FD624D7276390A1215BFC3", "C4E311A55666726FD9FE25CA572AFAF9", idOneToOne,
                                Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipOneToOne, underTest.getRelationshipWithSpecifiedId(idOneToOne, SchemaRegistry
                .getRelationTypeByName("MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM")));

        String idOneToMany = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==";
        Map<String, List<Object>> relationshipOneToMany = Map.of(
                "o-ran-smo-teiv-ran:GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU", List.of(
                        generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A",
                                idOneToMany, Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipOneToMany, underTest.getRelationshipWithSpecifiedId(idOneToMany, SchemaRegistry
                .getRelationTypeByName("GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU")));

        String idManyToOne = REL_ID_PREFIX + "Q2xvdWROYXRpdmVBcHBsaWNhdGlvbjo3MTlCRDVDN0NEOEE5MzlENzZBODNEQTk1REE0NUMwMTpERVBMT1lFRF9PTjpOYW1lc3BhY2U6MUMwMkU5NkIyQUFFMDM2QzdBRTQwNEJDMzhDMzA4RTA=";
        Map<String, List<Object>> relationshipManyToOne = Map.of(
                "o-ran-smo-teiv-cloud:CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE", List.of(
                        generateResponse("719BD5C7CD8A939D76A83DA95DA45C01", "1C02E96B2AAE036C7AE404BC38C308E0",
                                idManyToOne, Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipManyToOne, underTest.getRelationshipWithSpecifiedId(idManyToOne, SchemaRegistry
                .getRelationTypeByName("CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE")));

        String idManyToMany = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE";
        Map<String, List<Object>> relationshipManyToMany = Map.of(
                "o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN", List.of(
                        generateResponse("4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D",
                                idManyToMany, Collections.EMPTY_LIST)));

        Assertions.assertEquals(relationshipManyToMany, underTest.getRelationshipWithSpecifiedId(idManyToMany,
                SchemaRegistry.getRelationTypeByName("GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN")));

        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getRelationshipWithSpecifiedId("NOT_EXISTING",
                SchemaRegistry.getRelationTypeByName("GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU")));
    }

    @Test
    void testGetRelationshipsByTypeWithLongNames() {
        Map<String, Object> oneToOneResult = new HashMap<>();
        Map<String, Object> oneToManyResult = new HashMap<>();
        Map<String, Object> manyToOneResult = new HashMap<>();
        Map<String, Object> manyToManyResult = new HashMap<>();

        oneToOneResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-oam-to-cloud:MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM", List.of(
                        generateResponse("45EF31D8A1FD624D7276390A1215BFC3", "C4E311A55666726FD9FE25CA572AFAF9",
                                REL_ID_PREFIX + "TWFuYWdlZEVsZW1lbnQ6NDVFRjMxRDhBMUZENjI0RDcyNzYzOTBBMTIxNUJGQzM6REVQTE9ZRURfQVM6Q2xvdWROYXRpdmVTeXN0ZW06QzRFMzExQTU1NjY2NzI2RkQ5RkUyNUNBNTcyQUZBRjk=")))));
        PaginationMetaData metadataOneToOne = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(5).build();
        paginationDTO1.setTotalSize(1);
        oneToOneResult.putAll(metadataOneToOne.getObjectList(paginationDTO1));

        oneToManyResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-ran:GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU", List.of(
                        generateResponse("D3215E08570BE58339C7463626B50E37", "F9546E82313AC1D5E690DCD7BE55606F",
                                REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTpGOTU0NkU4MjMxM0FDMUQ1RTY5MERDRDdCRTU1NjA2Rg==")))));
        PaginationMetaData metadataOneToMany = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/attributes[@cellLocalIdddddddddddddddddddddddddddddddddddddddddddddddddddddd=2]").build();
        paginationDTO2.setTotalSize(1);
        oneToManyResult.putAll(metadataOneToMany.getObjectList(paginationDTO2));

        manyToOneResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-cloud:CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE", List.of(
                        generateResponse("719BD5C7CD8A939D76A83DA95DA45C01", "1C02E96B2AAE036C7AE404BC38C308E0",
                                REL_ID_PREFIX + "Q2xvdWROYXRpdmVBcHBsaWNhdGlvbjo3MTlCRDVDN0NEOEE5MzlENzZBODNEQTk1REE0NUMwMTpERVBMT1lFRF9PTjpOYW1lc3BhY2U6MUMwMkU5NkIyQUFFMDM2QzdBRTQwNEJDMzhDMzA4RTA=")))));
        PaginationMetaData metadataManyToOne = new PaginationMetaData();
        PaginationDTO paginationDTO3 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]").build();
        paginationDTO3.setTotalSize(1);
        manyToOneResult.putAll(metadataManyToOne.getObjectList(paginationDTO3));

        manyToManyResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN", List.of(
                        generateResponse("4CFF136200A2DE36205A13559C55DB2A", "C549905CF3CC890CE5746C5E10ACF00D",
                                REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjo0Q0ZGMTM2MjAwQTJERTM2MjA1QTEzNTU5QzU1REIyQTpSRUFMSVNFRF9CWTpDbG91ZE5hdGl2ZUFwcGxpY2F0aW9uOkM1NDk5MDVDRjNDQzg5MENFNTc0NkM1RTEwQUNGMDBE")))));
        PaginationMetaData metadataManyToMany = new PaginationMetaData();
        PaginationDTO paginationDTO4 = PaginationDTO.builder().offset(0).limit(5).addQueryParameters("scopeFilter",
                "/CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"] | /GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/id[@id=\"4CFF136200A2DE36205A13559C55DB2A\"]")
                .build();
        paginationDTO4.setTotalSize(1);
        manyToManyResult.putAll(metadataManyToMany.getObjectList(paginationDTO4));

        RelationType oneToOne = SchemaRegistry.getRelationTypeByName(
                "MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName(
                "GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU");
        RelationType manyToOne = SchemaRegistry.getRelationTypeByName(
                "CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE");
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName(
                "GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN");

        verifyResponse(oneToOneResult, underTest.getRelationshipsByType(oneToOne, null, paginationDTO1));
        verifyResponse(oneToManyResult, underTest.getRelationshipsByType(oneToMany,
                "/attributes[@cellLocalIdddddddddddddddddddddddddddddddddddddddddddddddddddddd=2]", paginationDTO2));
        verifyResponse(manyToOneResult, underTest.getRelationshipsByType(manyToOne,
                "/id[@id=\"719BD5C7CD8A939D76A83DA95DA45C01\"]", paginationDTO3));
        verifyResponse(manyToManyResult, underTest.getRelationshipsByType(manyToMany,
                "/CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/id[@id=\"C549905CF3CC890CE5746C5E10ACF00D\"] | /GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/id[@id=\"4CFF136200A2DE36205A13559C55DB2A\"]",
                paginationDTO4));

        assertThatThrownBy(() -> underTest.getRelationshipsByType(oneToMany, "/attributes[@celllocalid=2]", PaginationDTO
                .builder().offset(0).limit(5).build())).isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(oneToMany, null, PaginationDTO.builder()
                .offset(0).limit(5).build()));
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(oneToMany,
                "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn, \"GNBDUFunction=16\")]",
                PaginationDTO.builder().offset(0).limit(5).build()));
        Assertions.assertDoesNotThrow(() -> underTest.getRelationshipsByType(manyToMany,
                "/CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes[@name=\"Example Cloud App/10\"] | /GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/id[@id=\"5A548EA9D166341776CA0695837E55D8\"]",
                PaginationDTO.builder().offset(0).limit(5).build()));
    }

    @Test
    void testGetRelationshipsByTypeWithLongNames_relConnectingSameEntity() {
        Map<String, Object> sameEntityOneToOneResult = new HashMap<>();
        Map<String, Object> sameEntityOneToManyResult = new HashMap<>();

        // Rel connectingSameEntity ONE_TO_ONE
        RelationType relSameEntityOneToOne = SchemaRegistry.getRelationTypeByName(
                "ANTENNAMODULEEEEEEEEEEEE_DEPLOYED_ON_ANTENNAMODULEEEEEEEEEEEEEEE");
        sameEntityOneToOneResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-equipment:ANTENNAMODULEEEEEEEEEEEE_DEPLOYED_ON_ANTENNAMODULEEEEEEEEEEEEEEE", List.of(
                        generateResponse("478A05C67D47D117C2DC5BDF5E00AE70", "479A05C67D47D117C2DC5BDF5E00AE70",
                                REL_ID_PREFIX + "QW50ZW5uYU1vZHVsZTo0NzhBMDVDNjdENDdEMTE3QzJEQzVCREY1RTAwQUU3MDpERVBMT1lFRF9CWTpBbnRlbm5hTW9kdWxlOjQ3OUEwNUM2N0Q0N0QxMTdDMkRDNUJERjVFMDBBRTcwCg==")))));
        PaginationMetaData metadataOneToMany = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().offset(0).limit(5).build();
        paginationDTO2.setTotalSize(1);
        sameEntityOneToOneResult.putAll(metadataOneToMany.getObjectList(paginationDTO2));

        verifyResponse(sameEntityOneToOneResult, underTest.getRelationshipsByType(relSameEntityOneToOne, null, PaginationDTO
                .builder().offset(0).limit(5).build()));

        // Rel connectingSameEntity ONE_TO_MANY
        RelationType relSameEntityOneToMany = SchemaRegistry.getRelationTypeByName(
                "ANTENNAMODULEEEEEEEEEEEE_REALISED_BY_ANTENNAMODULEEEEEEEEEEEEEEE");
        sameEntityOneToManyResult.put("items", List.of(Map.of(
                "o-ran-smo-teiv-equipment:ANTENNAMODULEEEEEEEEEEEE_REALISED_BY_ANTENNAMODULEEEEEEEEEEEEEEE", List.of(
                        generateResponse("378A05C67D47D117C2DC5BDF5E00AE70", "379A05C67D47D117C2DC5BDF5E00AE70",
                                REL_ID_PREFIX + "QW50ZW5uYU1vZHVsZTozNzhBMDVDNjdENDdEMTE3QzJEQzVCREY1RTAwQUU3MDpSRUFMSVNFRF9CWTpBbnRlbm5hTW9kdWxlOjM3OUEwNUM2N0Q0N0QxMTdDMkRDNUJERjVFMDBBRTcwCg==")))));
        PaginationMetaData metadataOneToOne = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().offset(0).limit(5).build();
        paginationDTO1.setTotalSize(1);
        sameEntityOneToManyResult.putAll(metadataOneToOne.getObjectList(paginationDTO1));

        verifyResponse(sameEntityOneToManyResult, underTest.getRelationshipsByType(relSameEntityOneToMany, null,
                PaginationDTO.builder().offset(0).limit(5).build()));
    }

    @Test
    void testGetTopologyByTypeWithLongNames() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> queryMap = new HashMap<>();

        queryMap.put("query", Map.of("targetFilter",
                "/attributes(fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn)", "scopeFilter",
                "/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']"));

        result.putAll(queryMap);

        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/RAN/entities/GNBDUFunction").offset(0)
                .limit(2).addQueryParameters("targetFilter",
                        "/attributes(fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn)").addQueryParameters(
                                "scopeFilter",
                                "/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']")
                .build();
        paginationDTO.setTotalSize(2);
        result.putAll(new PaginationMetaData().getObjectList(paginationDTO));

        result.put("items", List.of(Map.of(
                "o-ran-smo-teiv-ran:GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", List.of(Map.of(
                        ATTRIBUTES, Map.of("fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=13"),
                        "id", "25E690E22BDA90B9C4FEE1F083CBA597"), Map.of(ATTRIBUTES, Map.of(
                                "fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=14"),
                                "id", "5A3085C3400C3096E2ED2321452766B1")))));

        verifyResponse(result, underTest.getTopologyByType(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "/attributes(fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn)",
                "/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn, \"13\")] | /id[@id='5A3085C3400C3096E2ED2321452766B1']",
                paginationDTO));
        Assertions.assertThrows(TiesPathException.class, () -> underTest.getTopologyByType(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "/attributes(fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn)",
                "/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn, \"1000493\")] ; /attributes[@gNBId=4000259]",
                PaginationDTO.builder().offset(0).limit(2).build()));
    }

    @Test
    void testGetEntitiesByDomainWithLongNames() {
        Map<String, Object> reference1 = new HashMap<>();
        Map<String, String> query = new LinkedHashMap<>();

        query.put("scopeFilter", "/attributes[@cellLocalIdddddddddddddddddddddddddddddddddddddddddddddddddddddd=3]");
        query.put("targetFilter", "/attributes(cellLocalIdddddddddddddddddddddddddddddddddddddddddddddddddddddd)");

        PaginationMetaData pmd1 = new PaginationMetaData();
        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/EQUIPMENT_TO_RAN").offset(0).limit(5)
                .build();
        paginationDTO.setQueryParameters(query);
        paginationDTO.setTotalSize(1);
        reference1.putAll(pmd1.getObjectList(paginationDTO));

        reference1.put("query", query);

        reference1.put("items", List.of(Map.of(
                "o-ran-smo-teiv-ran:NRCellDUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU", List.of(Map.of("id",
                        "B480427E8A0C0B8D994E437784BB382F", ATTRIBUTES, Map.of(
                                "cellLocalIdddddddddddddddddddddddddddddddddddddddddddddddddddddd", "3"))))));

        Map<String, Object> reference2 = new HashMap<>();
        Map<String, Object> query2 = new HashMap<>();

        query2.put("targetFilter", "/id");
        query2.put("scopeFilter", "/id[contains(@id,\"EA403A\")]");

        PaginationMetaData pmd2 = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath("/domains/EQUIPMENT_TO_RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter", "/id").addQueryParameters("scopeFilter",
                        "/id[contains(@id,\"EA403A\")]").build();
        paginationDTO2.setTotalSize(1);
        reference1.putAll(pmd2.getObjectList(paginationDTO2));

        reference2.put("query", query2);

        reference2.put("items", List.of(Map.of(
                "o-ran-smo-teiv-ran:NRCellDUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU", List.of(Map.of("id",
                        "F26F279E91D0941DB4F646E707EA403A")))));

        verifyResponse(reference2, underTest.getEntitiesByDomain("EQUIPMENT_TO_RAN", "/id", "/id[contains(@id,\"EA403A\")]",
                paginationDTO2));

        Map<String, Object> reference3 = new HashMap<>();
        Map<String, Object> query3 = new HashMap<>();

        query3.put("targetFilter",
                "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes/fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        query3.put("scopeFilter",
                "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn,\"GNBDUFunction=10\")]");

        PaginationMetaData pmd3 = new PaginationMetaData();
        PaginationDTO paginationDTO3 = PaginationDTO.builder().basePath("/domains/EQUIPMENT_TO_RAN").offset(0).limit(5)
                .addQueryParameters("targetFilter",
                        "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes/fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
                .addQueryParameters("scopeFilter",
                        "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn,\"GNBDUFunction=10\")]")
                .build();
        paginationDTO3.setTotalSize(1);
        reference1.putAll(pmd3.getObjectList(paginationDTO3));

        reference3.put("query", query3);

        reference3.put("items", List.of(Map.of(
                "o-ran-smo-teiv-ran:GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", List.of(Map.of("id",
                        "1050570EBB1315E1AE7A9FD5E1400A00", ATTRIBUTES, Map.of(
                                "fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=10"))))));

        verifyResponse(reference3, underTest.getEntitiesByDomain("RAN",
                "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes/fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "/GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn/attributes[contains(@fdnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn,\"GNBDUFunction=10\")]",
                paginationDTO3));

        Object result = underTest.getEntitiesByDomain("OAM_TO_RAN", "/id", "", PaginationDTO.builder().offset(0).limit(500)
                .build());
        Assertions.assertEquals(10, ((Map) ((List) ((HashMap) result).get("items")).get(0)).size());
        Assertions.assertTrue(((Map) ((List) ((HashMap) result).get("items")).get(0)).containsKey(
                "o-ran-smo-teiv-oam:ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt"));

        Assertions.assertTrue(((Map) ((List) ((HashMap) result).get("items")).get(0)).containsKey(
                "o-ran-smo-teiv-ran:GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"));
    }

    @Test
    void testAvailableClassifiers() {
        Assertions.assertEquals(Set.of("gnbdu-function-model:Rural", "gnbcucp-gnbcuup-model:Weekend"), underTest
                .loadClassifiers());
    }

    @Test
    void testAvailableDecorators() {
        Assertions.assertEquals(Map.of("gnbdu-function-model:location", DataType.PRIMITIVE,
                "gnbcucp-gnbcuup-model:metadata", DataType.CONTAINER), underTest.loadDecorators());
    }

    @Test
    void testGetSchemasByDomain() {
        Set<Map<String, Object>> expected = new HashSet<>();
        for (StoredSchema schema : getSchemasList()) {
            if (schema.getDomain() == null || !schema.getDomain().equals("RAN_OAM_TO_CLOUD")) {
                continue;
            }
            Map<String, Object> exp = new HashMap<>();
            exp.put("domain", Collections.singletonList(schema.getDomain()));
            exp.put("name", schema.getName());
            exp.put("revision", schema.getRevision());
            exp.put("content", Collections.singletonMap("href", "/schemas/" + schema.getName() + "/content"));
            expected.add(exp);
        }

        Assertions.assertEquals(expected, new HashSet<>((List) underTest.getSchemas("RAN_OAM_TO_CLOUD", PaginationDTO
                .builder().offset(0).limit(15).build()).get("items")));
    }

    @Test
    void testGetSchemasWithPartialDomain() {
        Set<Map<String, Object>> expected = new HashSet<>();
        for (StoredSchema schema : getSchemasList()) {
            if (schema.getDomain() == null || !schema.getDomain().matches("RAN_.*O.*")) {
                continue;
            }
            Map<String, Object> exp = new HashMap<>();
            exp.put("domain", Collections.singletonList(schema.getDomain()));
            exp.put("name", schema.getName());
            exp.put("revision", schema.getRevision());
            exp.put("content", Collections.singletonMap("href", "/schemas/" + schema.getName() + "/content"));
            expected.add(exp);
        }

        Assertions.assertEquals(expected, new HashSet<>((List) underTest.getSchemas("RAN_.*O.*", PaginationDTO.builder()
                .offset(0).limit(15).build()).get("items")));
    }

    @Test
    void testGetSchema() {
        StoredSchema expected = null;
        for (StoredSchema schema : getSchemasList()) {
            if (schema.getName().equals("o-ran-smo-teiv-oam")) {
                expected = schema;
                break;
            }
        }

        StoredSchema actual = underTest.getSchema("o-ran-smo-teiv-oam");
        Assertions.assertTrue(actual.getContent().contains("o-ran-smo-teiv-oam"));

        actual.setContent("yang model o-ran-smo-teiv-oam {}");
        Assertions.assertEquals(expected, actual);

        Assertions.assertNull(underTest.getSchema("o-ran-smo-teiv-invalid"));
    }

    @Test
    void testSchemaCRUD() {
        Assertions.assertNull(underTest.getSchema("newSchema"));

        StoredSchema expected = new StoredSchema();
        expected.setName("newSchema");
        expected.setNamespace("new-namespace");
        expected.setDomain("NEW_DOMAIN");
        expected.setIncludedModules(new ArrayList<>());
        expected.setContent("yang content {} \n\n \t\t\t;");
        expected.setOwnerAppId("additional");
        expected.setStatus("IN_USAGE");

        underTest.postSchema("newSchema", "new-namespace", "NEW_DOMAIN", new ArrayList<>(), "yang content {} \n\n \t\t\t;",
                "additional");

        Assertions.assertEquals(expected, underTest.getSchema("newSchema"));

        underTest.setSchemaToDeleting("newSchema");

        expected.setStatus("DELETING");
        Assertions.assertEquals(expected, underTest.getSchema("newSchema"));

        underTest.deleteSchema("newSchema");

        Assertions.assertNull(underTest.getSchema("newSchema"));
    }

    private List<StoredSchema> getSchemasList() {
        final List<StoredSchema> schemas = new ArrayList<>();

        StoredSchema ranLogicalToCloud = new StoredSchema();
        ranLogicalToCloud.setName("o-ran-smo-teiv-cloud-to-ran");
        ranLogicalToCloud.setNamespace("urn:o-ran:smo-teiv-cloud-to-ran");
        ranLogicalToCloud.setDomain("CLOUD_TO_RAN");
        ranLogicalToCloud.setRevision("2023-10-24");

        StoredSchema ranEquipment = new StoredSchema();
        ranEquipment.setName("o-ran-smo-teiv-equipment");
        ranEquipment.setNamespace("urn:o-ran:smo-teiv-equipment");
        ranEquipment.setDomain("EQUIPMENT");
        ranEquipment.setRevision("2023-06-26");

        StoredSchema ranOamToCloud = new StoredSchema();
        ranOamToCloud.setName("o-ran-smo-teiv-oam-to-cloud");
        ranOamToCloud.setNamespace("urn:o-ran:smo-teiv-oam-to-cloud");
        ranOamToCloud.setDomain("OAM_TO_CLOUD");
        ranOamToCloud.setRevision("2023-10-24");

        StoredSchema ranOamToLogical = new StoredSchema();
        ranOamToLogical.setName("o-ran-smo-teiv-oam-to-ran");
        ranOamToLogical.setNamespace("urn:o-ran:smo-teiv-oam-to-ran");
        ranOamToLogical.setDomain("OAM_TO_RAN");
        ranOamToLogical.setRevision("2023-10-24");

        StoredSchema ranCloud = new StoredSchema();
        ranCloud.setName("o-ran-smo-teiv-cloud");
        ranCloud.setNamespace("urn:o-ran:smo-teiv-cloud");
        ranCloud.setDomain("CLOUD");
        ranCloud.setRevision("2023-06-26");

        StoredSchema ranOam = new StoredSchema();
        ranOam.setName("o-ran-smo-teiv-oam");
        ranOam.setNamespace("urn:o-ran:smo-teiv-oam");
        ranOam.setDomain("OAM");
        ranOam.setRevision("2024-05-02");
        ranOam.setIncludedModules(new ArrayList<>());
        ranOam.setOwnerAppId("BUILT_IN_MODULE");
        ranOam.setStatus("IN_USAGE");
        ranOam.setContent("yang model o-ran-smo-teiv-oam {}");

        StoredSchema yangTypes = new StoredSchema();
        yangTypes.setName("o-ran-smo-teiv-common-yang-types");
        yangTypes.setRevision("2023-07-04");

        StoredSchema ranLogicalToEquipment = new StoredSchema();
        ranLogicalToEquipment.setName("o-ran-smo-teiv-equipment-to-ran");
        ranLogicalToEquipment.setDomain("EQUIPMENT_TO_RAN");
        ranLogicalToEquipment.setRevision("2023-10-24");

        StoredSchema gnbcucpGnbcuupModel = new StoredSchema();
        gnbcucpGnbcuupModel.setName("gnbcucp-gnbcuup-model");

        StoredSchema yangExtensions = new StoredSchema();
        yangExtensions.setName("o-ran-smo-teiv-common-yang-extensions");
        yangExtensions.setRevision("2023-07-04");

        StoredSchema ranLogical = new StoredSchema();
        ranLogical.setName("o-ran-smo-teiv-ran");
        ranLogical.setDomain("RAN");
        ranLogical.setRevision("2023-11-03");

        StoredSchema gnbduFunctionModel = new StoredSchema();
        gnbduFunctionModel.setName("gnbdu-function-model");

        schemas.add(ranLogicalToCloud);
        schemas.add(ranEquipment);
        schemas.add(ranOamToCloud);
        schemas.add(ranOamToLogical);
        schemas.add(ranCloud);
        schemas.add(ranOam);
        schemas.add(yangTypes);
        schemas.add(ranLogicalToEquipment);
        schemas.add(gnbcucpGnbcuupModel);
        schemas.add(yangExtensions);
        schemas.add(ranLogical);
        schemas.add(gnbduFunctionModel);

        return schemas;
    }
}
