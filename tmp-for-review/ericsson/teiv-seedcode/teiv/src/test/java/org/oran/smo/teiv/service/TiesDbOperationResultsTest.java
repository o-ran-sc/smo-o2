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

import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_A_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_B_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.oran.smo.teiv.exception.TiesException;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.exception.InvalidFieldInYangDataException;
import org.oran.smo.teiv.ingestion.DeadlockRetryPolicy;
import org.oran.smo.teiv.ingestion.validation.IngestionOperationValidatorFactory;
import org.oran.smo.teiv.ingestion.validation.MaximumCardinalityViolationException;
import org.oran.smo.teiv.service.models.OperationResult;
import org.oran.smo.teiv.schema.PostgresSchemaLoader;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.service.cloudevent.CloudEventParser;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;
import org.oran.smo.teiv.startup.SchemaHandler;
import org.oran.smo.teiv.utils.CloudEventTestUtil;
import org.oran.smo.teiv.utils.ConvertToJooqTypeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;

@Configuration
@SpringBootTest
@ActiveProfiles({ "test", "ingestion" })
public class TiesDbOperationResultsTest {
    public static TestPostgresqlContainer postgresqlContainer = TestPostgresqlContainer.getInstance();
    private static TiesDbService tiesDbService;
    private static TiesDbOperations tiesDbOperations;
    private static DSLContext dslContext;
    private static String VALIDATE_MANY_TO_ONE_DIR = "src/test/resources/cloudeventdata/validation/many-to-one/";
    private static String VALIDATE_ONE_TO_MANY_DIR = "src/test/resources/cloudeventdata/validation/one-to-many/";
    private static String VALIDATE_ONE_TO_ONE_DIR = "src/test/resources/cloudeventdata/validation/one-to-one/";

    @Autowired
    CloudEventParser cloudEventParser;
    @MockBean
    private SchemaHandler schemaHandler;

    @BeforeAll
    public static void beforeAll(@Autowired DeadlockRetryPolicy deadlockRetryPolicy) throws UnsupportedOperationException,
            SchemaLoaderException {
        String url = postgresqlContainer.getJdbcUrl();
        DataSource ds = DataSourceBuilder.create().url(url).username("test").password("test").build();
        dslContext = DSL.using(ds, SQLDialect.POSTGRES);
        tiesDbService = new TiesDbService(dslContext, dslContext, deadlockRetryPolicy);
        tiesDbOperations = new TiesDbOperations(tiesDbService, new IngestionOperationValidatorFactory(),
                new RelationshipMergeValidator());
        PostgresSchemaLoader postgresSchemaLoader = new PostgresSchemaLoader(dslContext, new ObjectMapper());
        postgresSchemaLoader.loadSchemaRegistry();
    }

    @BeforeEach
    public void deleteAll() {
        dslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(t -> dslContext
                .truncate(t).cascade().execute());
    }

    @Test
    void testMergeEntityRelationship() {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-create-one-to-many.json");

        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = assertDoesNotThrow(() -> tiesDbOperations
                .executeEntityAndRelationshipMergeOperations(parsedCloudEventData));

        assertEquals(3, mergeResult.size());
        assertEquals("ManagedElement_1", mergeResult.get(0).getId());
        assertEquals("ManagedElement", mergeResult.get(0).getEntryType());
        assertEquals(Map.of(), mergeResult.get(0).getContent());

        assertEquals("ENodeBFunction_1", mergeResult.get(1).getId());
        assertEquals("ENodeBFunction", mergeResult.get(1).getEntryType());
        assertEquals(Map.of(), mergeResult.get(1).getContent());

        assertEquals("relation_1", mergeResult.get(2).getId());
        assertEquals("MANAGEDELEMENT_MANAGES_ENODEBFUNCTION", mergeResult.get(2).getEntryType());
        assertEquals(Map.of("aSide", "ManagedElement_1", "bSide", "ENodeBFunction_1"), mergeResult.get(2).getContent());
    }

    @Test
    void testDeleteEntityById() {
        Map<String, Object> managedElementEntity = new HashMap<>();
        managedElementEntity.put("id", "managed_element_entity_id1");
        managedElementEntity.put("fdn", "fdn1");
        managedElementEntity.put("cmId", JSONB.jsonb("{\"name\":\"Hellmann1\"}"));
        tiesDbOperations.merge(dslContext, "ties_data.\"ManagedElement\"", managedElementEntity);

        // Delete operation - expected to succeed
        List<OperationResult> deleteResultMatch = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("ManagedElement"), "managed_element_entity_id1");

        assertFalse(deleteResultMatch.isEmpty(), "Delete operation should return a non-empty list");
        assertTrue(deleteResultMatch.contains(new OperationResult("managed_element_entity_id1", "ManagedElement", null)),
                "The list should contain the delete operation result with id: 'managed_element_entity_id1'");

        // Delete operation with the same EIID - expected to fail
        List<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("ManagedElement"), "managed_element_entity_id1");
        assertTrue(deleteResultNoMatch.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");
    }

    @Test
    void testDeleteOneToOneByRelationId() {
        Map<String, Object> managedElementEntity = new HashMap<>();
        managedElementEntity.put("id", "managed_element_entity_id1");
        managedElementEntity.put("fdn", "fdn1");
        managedElementEntity.put("cmId", JSONB.jsonb("{\"name\":\"Hellmann1\"}"));
        managedElementEntity.put("REL_FK_deployed-as-cloudNativeSystem", "cloud_native_system_entity_id1");
        managedElementEntity.put("REL_ID_MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM", "relation_eiid1");

        Map<String, Object> cloudNativeSystemEntity = new HashMap<>();
        cloudNativeSystemEntity.put("id", "cloud_native_system_entity_id1");

        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeSystem\"", cloudNativeSystemEntity);
        tiesDbOperations.merge(dslContext, "ties_data.\"ManagedElement\"", managedElementEntity);

        cloudNativeSystemEntity.put("name", "CloudNativeSystem");
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeSystem\"", cloudNativeSystemEntity);

        // Delete operation for aSide - expected to succeed
        Optional<OperationResult> deleteASideResultMatch = tiesDbOperations.deleteRelationFromEntityTableByRelationId(
                dslContext, "relation_eiid1", SchemaRegistry.getRelationTypeByName(
                        "MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM"));

        assertTrue(deleteASideResultMatch.isPresent(), "Delete operation should return a present Optional");
        assertEquals(new OperationResult("relation_eiid1", "MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM", null),
                deleteASideResultMatch.get(), "The delete operation result should be present for: 'relation_eiid1'");

        // Delete operation with the same EIID - expected to fail
        Optional<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteRelationFromEntityTableByRelationId(
                dslContext, "relation_eiid1", SchemaRegistry.getRelationTypeByName(
                        "MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM"));
        assertTrue(deleteResultNoMatch.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");

        Result<Record> rows = tiesDbService.selectAllRowsFromTable("ties_data.\"ManagedElement\"");
        Result<Record> rowsOnBSide = tiesDbService.selectAllRowsFromTable("ties_data.\"CloudNativeSystem\"");
        assertEquals("managed_element_entity_id1", rows.get(0).get("id"));
        assertEquals("fdn1", rows.get(0).get("fdn"));
        assertEquals("cloud_native_system_entity_id1", rowsOnBSide.get(0).get("id"));
        assertEquals("CloudNativeSystem", rowsOnBSide.get(0).get("name"));
        assertNull(rows.get(0).get("REL_FK_deployed-as-cloudNativeSystem"));
        assertNull(rows.get(0).get("REL_ID_MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM"));
        assertNull(rowsOnBSide.get(0).get("REL_FK_deployed-managedElement"));
        assertEquals(ConvertToJooqTypeUtil.toJsonb(List.of()), rows.get(0).get(
                "REL_CD_sourceIds_MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM"));
    }

    @Test
    void testDeleteOneToManyByManySideEntityId() {
        Map<String, Object> managedElement1 = new HashMap<>();
        managedElement1.put("id", "managed_element_entity_id1");
        managedElement1.put("fdn", "fdn1");
        managedElement1.put("cmId", JSONB.jsonb("{\"name\":\"Hellmann1\"}"));

        Map<String, Object> cna1 = new HashMap<>();
        cna1.put("id", "cna_entity_id1");
        cna1.put("name", "CloudNativeApplication");
        cna1.put("REL_FK_realised-managedElement", "managed_element_entity_id1");
        cna1.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_eiid1");

        tiesDbOperations.merge(dslContext, "ties_data.\"ManagedElement\"", managedElement1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna1);

        // Delete operation with existing relationship
        List<OperationResult> deleteResultMatch = tiesDbOperations.deleteRelationshipByManySideEntityId(dslContext,
                "cna_entity_id1", "id", SchemaRegistry.getRelationTypeByName(
                        "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION"));

        assertFalse(deleteResultMatch.isEmpty(), "Delete operation should return a non-empty list");
        assertTrue(deleteResultMatch.contains(new OperationResult("relation_eiid1",
                "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'relation_eiid1'");

        // Delete operation with the same entity ID - expected to return an empty list
        List<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteRelationshipByManySideEntityId(dslContext,
                "cna_entity_id1", "id", SchemaRegistry.getRelationTypeByName(
                        "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION"));

        assertTrue(deleteResultNoMatch.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");
    }

    @Test
    void testDeleteOneToManyByOneSideEntityId() {
        Map<String, Object> managedElement1 = new HashMap<>();
        managedElement1.put("id", "managed_element_entity_id1");
        managedElement1.put("fdn", "fdn1");
        managedElement1.put("cmId", JSONB.jsonb("{\"name\":\"Hellmann1\"}"));

        Map<String, Object> cna1 = new HashMap<>();
        cna1.put("id", "cna_entity_id1");
        cna1.put("name", "CloudNativeApplication");
        cna1.put("REL_FK_realised-managedElement", "managed_element_entity_id1");
        cna1.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_eiid1");

        Map<String, Object> cna2 = new HashMap<>();
        cna2.put("id", "cna_entity_id2");
        cna2.put("name", "CloudNativeApplication");
        cna2.put("REL_FK_realised-managedElement", "managed_element_entity_id1");
        cna2.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_eiid2");

        Map<String, Object> cna3 = new HashMap<>();
        cna3.put("id", "cna_entity_id3");
        cna3.put("name", "CloudNativeApplication");
        cna3.put("REL_FK_realised-managedElement", "managed_element_entity_id1");
        cna3.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_eiid3");

        tiesDbOperations.merge(dslContext, "ties_data.\"ManagedElement\"", managedElement1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna2);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna3);

        // Delete operation for managed_element_entity_id1
        List<OperationResult> deleteResultMatch = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("ManagedElement"), "managed_element_entity_id1");
        assertFalse(deleteResultMatch.isEmpty(), "Delete operation should return a non-empty list");

        // Check if all expected IDs are present in the deletion result
        assertEquals(4, deleteResultMatch.size(), "Delete operation should match expected size");
        assertTrue(deleteResultMatch.contains(new OperationResult("managed_element_entity_id1", "ManagedElement", null)),
                "The list should contain the delete operation result with id: 'managed_element_entity_id1'");

        assertTrue(deleteResultMatch.contains(new OperationResult("relation_eiid1",
                "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'relation_eiid1'");
        assertTrue(deleteResultMatch.contains(new OperationResult("relation_eiid2",
                "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'relation_eiid2'");
        assertTrue(deleteResultMatch.contains(new OperationResult("relation_eiid3",
                "MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'relation_eiid3'");

        // Verify all related entities have their relationships deleted
        Result<Record> rows = tiesDbService.selectAllRowsFromTable("ties_data.\"CloudNativeApplication\"");
        assertEquals(3, rows.size());
        for (Record row : rows) {
            assertNull(row.get("REL_FK_realised-managedElement"),
                    "REL_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION should be null");
            assertNull(row.get("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION"),
                    "REL_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION_EIID should be null");
        }
    }

    @Test
    void testDeleteManyToManyByRelationshipId() {
        Map<String, Object> gnbcucp1 = new HashMap<>();
        gnbcucp1.put("id", "gnbcucp_id1");
        gnbcucp1.put("fdn", "fdn1");
        gnbcucp1.put("gNBCUName", "gNBCUName");
        gnbcucp1.put("gNBId", 1);
        gnbcucp1.put("gNBIdLength", 1);
        gnbcucp1.put("pLMNId", JSONB.jsonb("{\"name\":\"pLMNId1\"}"));
        gnbcucp1.put("cmId", JSONB.jsonb("{\"name\":\"cmId1\"}"));

        Map<String, Object> cna1 = new HashMap<>();
        cna1.put("id", "cloud_native_id1");
        cna1.put("name", "CloudNativeApplication");

        Map<String, Object> cna2 = new HashMap<>();
        cna2.put("id", "cloud_native_id2");
        cna2.put("name", "CloudNativeApplication");

        Map<String, Object> rel1 = new HashMap<>();
        rel1.put("id", "rel_id1");
        rel1.put("aSide_GNBCUCPFunction", "gnbcucp_id1");
        rel1.put("bSide_CloudNativeApplication", "cloud_native_id1");

        Map<String, Object> rel2 = new HashMap<>();
        rel2.put("id", "rel_id2");
        rel2.put("aSide_GNBCUCPFunction", "gnbcucp_id1");
        rel2.put("bSide_CloudNativeApplication", "cloud_native_id2");

        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFunction\"", gnbcucp1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna2);
        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", rel1);
        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", rel2);

        Result<Record> row1 = tiesDbService.selectAllRowsFromTable("ties_data.\"GNBCUCPFunction\"");
        assertEquals(1, row1.size());
        Result<Record> row2 = tiesDbService.selectAllRowsFromTable("ties_data.\"CloudNativeApplication\"");
        assertEquals(2, row2.size());
        Result<Record> row3 = tiesDbService.selectAllRowsFromTable(
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"");
        assertEquals(2, row3.size());

        // Test deletion of a relationship by ID (expected success)
        Optional<OperationResult> deleteResultMatch = tiesDbOperations.deleteManyToManyRelationByRelationId(dslContext,
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", "rel_id1");
        assertTrue(deleteResultMatch.isPresent(), "Delete operation should return a present Optional");
        assertEquals(new OperationResult("rel_id1", "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", null),
                deleteResultMatch.get(), "Deleted relationship ID should match 'rel_id1'");

        // Test deletion of the same relationship ID again (expected failure)
        Optional<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteManyToManyRelationByRelationId(dslContext,
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", "rel_id1");
        assertTrue(deleteResultNoMatch.isEmpty(), "Delete operation should not return a present Optional");
    }

    @Test
    void testDeleteManyToManyByEntityId() {
        Map<String, Object> gnbcucp1 = new HashMap<>();
        gnbcucp1.put("id", "gnbcucp_id1");
        gnbcucp1.put("fdn", "fdn1");
        gnbcucp1.put("gNBCUName", "gNBCUName");
        gnbcucp1.put("gNBId", 1);
        gnbcucp1.put("gNBIdLength", 1);
        gnbcucp1.put("pLMNId", JSONB.jsonb("{\"name\":\"pLMNId1\"}"));
        gnbcucp1.put("cmId", JSONB.jsonb("{\"name\":\"cmId1\"}"));

        Map<String, Object> cna1 = new HashMap<>();
        cna1.put("id", "cloud_native_id1");
        cna1.put("name", "CloudNativeApplication");

        Map<String, Object> cna2 = new HashMap<>();
        cna2.put("id", "cloud_native_id2");
        cna2.put("name", "CloudNativeApplication");

        Map<String, Object> rel1 = new HashMap<>();
        rel1.put("id", "rel_id1");
        rel1.put("aSide_GNBCUCPFunction", "gnbcucp_id1");
        rel1.put("bSide_CloudNativeApplication", "cloud_native_id1");

        Map<String, Object> rel2 = new HashMap<>();
        rel2.put("id", "rel_id2");
        rel2.put("aSide_GNBCUCPFunction", "gnbcucp_id1");
        rel2.put("bSide_CloudNativeApplication", "cloud_native_id2");

        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFunction\"", gnbcucp1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna2);
        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", rel1);
        tiesDbOperations.merge(dslContext, "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", rel2);

        assertEquals(1, tiesDbService.selectAllRowsFromTable("ties_data.\"GNBCUCPFunction\"").size(),
                "Expected one GNBCUCPFunction record");
        assertEquals(2, tiesDbService.selectAllRowsFromTable("ties_data.\"CloudNativeApplication\"").size(),
                "Expected two CloudNativeApplication records");
        assertEquals(2, tiesDbService.selectAllRowsFromTable(
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"").size(),
                "Expected two GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION relations");

        // Test deletion of relations by entity ID (expected to delete two relations)
        List<OperationResult> deleteResultMatch = tiesDbOperations.deleteManyToManyRelationByEntityId(dslContext,
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", "gnbcucp_id1", "aSide_GNBCUCPFunction",
                "bSide_CloudNativeApplication");
        assertEquals(2, deleteResultMatch.size(), "Expected two relations to be deleted");
        assertTrue(deleteResultMatch.contains(new OperationResult("rel_id1",
                "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'rel_id1'");

        assertTrue(deleteResultMatch.contains(new OperationResult("rel_id2",
                "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", null)),
                "The list should contain the delete operation result with id: 'rel_id2'");

        // Test deletion of relations by the same entity ID again (expected to find no
        // relations to delete)
        List<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteManyToManyRelationByEntityId(dslContext,
                "ties_data.\"GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"", "gnbcucp_id1", "aSide_GNBCUCPFunction",
                "bSide_CloudNativeApplication");
        assertTrue(deleteResultNoMatch.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");
    }

    @Test
    void testDeleteRelConnectingSameEntityByRelationshipId() {
        Map<String, Object> antennaModule1 = new HashMap<>();
        antennaModule1.put("id", "module_id1");
        antennaModule1.put("mechanicalAntennaTilt", 400);
        antennaModule1.put("fdn", "fdn_1");
        antennaModule1.put("cmId", JSONB.jsonb("{\"name\":\"cmId1\"}"));
        antennaModule1.put("antennaModelNumber", "['123-abc']");
        antennaModule1.put("totalTilt", 10);
        antennaModule1.put("mechanicalAntennaBearing", 123);
        antennaModule1.put("positionWithinSector", "['123', '456', '789']");
        antennaModule1.put("electricalAntennaTilt", 1);

        Map<String, Object> antennaModule2 = new HashMap<>();
        antennaModule2.put("id", "module_id2");
        antennaModule2.put("mechanicalAntennaTilt", 401);
        antennaModule2.put("fdn", "fdn_2");
        antennaModule2.put("cmId", JSONB.jsonb("{\"name\":\"cmId2\"}"));
        antennaModule2.put("antennaModelNumber", "['456-abc']");
        antennaModule2.put("totalTilt", 11);
        antennaModule2.put("mechanicalAntennaBearing", 456);
        antennaModule2.put("positionWithinSector", "['123', '456', '789']");
        antennaModule2.put("electricalAntennaTilt", 2);

        Map<String, Object> rel1 = new HashMap<>();
        rel1.put("id", "rel_id1");
        rel1.put("aSide_AntennaModule", "module_id1");
        rel1.put("bSide_AntennaModule", "module_id2");

        Map<String, Object> rel2 = new HashMap<>();
        rel2.put("id", "rel_id2");
        rel2.put("bSide_AntennaModule", "module_id2");
        rel2.put("aSide_AntennaModule", "module_id1");

        tiesDbOperations.merge(dslContext, "ties_data.\"AntennaModule\"", antennaModule1);
        tiesDbOperations.merge(dslContext, "ties_data.\"AntennaModule\"", antennaModule2);
        tiesDbOperations.merge(dslContext, "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", rel1);
        tiesDbOperations.merge(dslContext, "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", rel2);

        Result<Record> row1 = tiesDbService.selectAllRowsFromTable("ties_data.\"AntennaModule\"");
        assertEquals(2, row1.size());
        Result<Record> row2 = tiesDbService.selectAllRowsFromTable("ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"");
        assertEquals(2, row2.size());

        // Test deletion of a relationship by ID (expected success)
        Optional<OperationResult> deleteResultMatch = tiesDbOperations.deleteManyToManyRelationByRelationId(dslContext,
                "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", "rel_id1");
        assertTrue(deleteResultMatch.isPresent(), "Delete operation should return a present Optional");
        assertEquals(new OperationResult("rel_id1", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", null), deleteResultMatch
                .get(), "Deleted relationship ID should match 'rel_id1'");

        // Test deletion of the same relationship ID again (expected failure)
        Optional<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteManyToManyRelationByRelationId(dslContext,
                "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", "rel_id1");
        assertTrue(deleteResultNoMatch.isEmpty(), "Delete operation should not return a present Optional");

    }

    @Test
    void testDeleteRelConnectingSameEntityByEntityId() {
        Map<String, Object> antennaModule1 = new HashMap<>();
        antennaModule1.put("id", "module_id1");
        antennaModule1.put("mechanicalAntennaTilt", 400);
        antennaModule1.put("fdn", "fdn_1");
        antennaModule1.put("cmId", JSONB.jsonb("{\"name\":\"cmId1\"}"));
        antennaModule1.put("antennaModelNumber", "['123-abc']");
        antennaModule1.put("totalTilt", 10);
        antennaModule1.put("mechanicalAntennaBearing", 123);
        antennaModule1.put("positionWithinSector", "['123', '456', '789']");
        antennaModule1.put("electricalAntennaTilt", 1);

        Map<String, Object> antennaModule2 = new HashMap<>();
        antennaModule2.put("id", "module_id2");
        antennaModule2.put("mechanicalAntennaTilt", 401);
        antennaModule2.put("fdn", "fdn_2");
        antennaModule2.put("cmId", JSONB.jsonb("{\"name\":\"cmId2\"}"));
        antennaModule2.put("antennaModelNumber", "['456-abc']");
        antennaModule2.put("totalTilt", 11);
        antennaModule2.put("mechanicalAntennaBearing", 456);
        antennaModule2.put("positionWithinSector", "['123', '456', '789']");
        antennaModule2.put("electricalAntennaTilt", 2);

        Map<String, Object> rel1 = new HashMap<>();
        rel1.put("id", "rel_id1");
        rel1.put("aSide_AntennaModule", "module_id1");
        rel1.put("bSide_AntennaModule", "module_id2");

        Map<String, Object> rel2 = new HashMap<>();
        rel2.put("id", "rel_id2");
        rel2.put("bSide_AntennaModule", "module_id2");
        rel2.put("aSide_AntennaModule", "module_id1");

        tiesDbOperations.merge(dslContext, "ties_data.\"AntennaModule\"", antennaModule1);
        tiesDbOperations.merge(dslContext, "ties_data.\"AntennaModule\"", antennaModule2);
        tiesDbOperations.merge(dslContext, "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", rel1);
        tiesDbOperations.merge(dslContext, "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", rel2);

        assertEquals(2, tiesDbService.selectAllRowsFromTable("ties_data.\"AntennaModule\"").size(),
                "Expected two AntennaModule records");
        assertEquals(2, tiesDbService.selectAllRowsFromTable("ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"")
                .size(), "Expected two ANTENNAMODULE_REALISED_BY_ANTENNAMODULE relations");

        // Test deletion of relations by entity ID (expected to delete two relations)
        List<OperationResult> deleteResultMatch = tiesDbOperations.deleteManyToManyRelationByEntityId(dslContext,
                "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", "module_id1", "aSide_AntennaModule",
                "bSide_AntennaModule");
        assertEquals(2, deleteResultMatch.size(), "Expected two relations to be deleted");
        assertTrue(deleteResultMatch.contains(new OperationResult("rel_id1", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE",
                null)), "The list should contain the delete operation result with id: 'rel_id1'");

        assertTrue(deleteResultMatch.contains(new OperationResult("rel_id2", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE",
                null)), "The list should contain the delete operation result with id: 'rel_id2'");

        // Test deletion of relations by the same entity ID again (expected to find no
        // relations to delete)
        List<OperationResult> deleteResultNoMatch = tiesDbOperations.deleteManyToManyRelationByEntityId(dslContext,
                "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"", "module_id1", "aSide_AntennaModule",
                "bSide_AntennaModule");
        assertTrue(deleteResultNoMatch.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");

    }

    @Test
    void testMergeEntityRelationshipWithLongNames() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-merge-long-names.json");

        // Merge entities and relationship
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(40, mergeResult.size());
    }

    @Test
    void testDeleteASideEntityWithLongNames() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-merge-long-names.json");

        // Merge topology data
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(40, mergeResult.size());

        // Entity with One_To_One relationship
        List<OperationResult> deleteEntityResult1 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt"),
                "ManagedElement_2");
        assertEquals(2, deleteEntityResult1.size());

        // Entity with One_To_Many relationship
        List<OperationResult> deleteEntityResult2 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "GNBDUFunction_2");
        assertEquals(2, deleteEntityResult2.size());

        // Entity and Many_To_One relationship
        List<OperationResult> deleteEntityResult3 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "CloudNativeApplication_3");
        assertEquals(2, deleteEntityResult3.size());

        // Entity with Many_To_Many relationship
        List<OperationResult> deleteEntityResult4 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "GNBDUFunction_1");
        assertEquals(7, deleteEntityResult4.size());

        // Entity with One_To_Many relationship ConnectingSameEntity
        List<OperationResult> deleteEntityResult5 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"),
                "AntennaModule_1");
        assertEquals(2, deleteEntityResult5.size());

        // Entity with One_To_One relationship ConnectingSameEntity
        List<OperationResult> deleteEntityResult6 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"),
                "AntennaModule_5");
        assertEquals(2, deleteEntityResult6.size());
    }

    @Test
    void testDeleteBSideEntityWithLongNames() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-merge-long-names.json");

        // Merge topology data
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(40, mergeResult.size());

        // Entity with One_To_One relationship
        List<OperationResult> deleteEntityResult1 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("CloudNativeSystemmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"),
                "CloudNativeSystem_1");
        assertEquals(2, deleteEntityResult1.size());

        // Entity with One_To_Many relationship
        List<OperationResult> deleteEntityResult2 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("NRCellDUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"), "NRCellDU_3");
        assertEquals(2, deleteEntityResult2.size());

        // Entity with Many_To_One relationship
        List<OperationResult> deleteEntityResult3 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("Namespaceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"), "Namespace_1");
        assertEquals(3, deleteEntityResult3.size());

        // Entity with Many_To_Many relationship
        List<OperationResult> deleteEntityResult4 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "CloudNativeApplication_2");
        assertEquals(2, deleteEntityResult4.size());

        // Entity with One_To_Many relationship ConnectingSameEntity
        List<OperationResult> deleteEntityResult5 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"),
                "AntennaModule_2");
        assertEquals(2, deleteEntityResult5.size());

        // Entity with One_To_One relationship ConnectingSameEntity
        List<OperationResult> deleteEntityResult6 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"),
                "AntennaModule_6");
        assertEquals(2, deleteEntityResult6.size());

        // Again delete CloudNativeApplication(id=CloudNativeApplication_2) should
        // return empty result list
        List<OperationResult> deleteEntityResult7 = tiesDbOperations.deleteEntity(dslContext, SchemaRegistry
                .getEntityTypeByName("CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"),
                "CloudNativeApplication_2");
        assertTrue(deleteEntityResult7.isEmpty(),
                "Delete operation should return an empty list for already deleted/non existing ID");
    }

    @Test
    void testDeleteRelationshipWithLongNames() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-merge-long-names.json");

        // Merge topology data
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(40, mergeResult.size());

        //One_To_One Relationship
        Relationship oneToOneRelationship = new Relationship("o-ran-smo-teiv-oam-to-cloud",
                "MANAGEDELEMENTTTTTTTTTTT_DEPLOYED_AS_CLOUDNATIVESYSTEMMMMMMMMMMM",
                "MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM_relation_3", "ManagedElement_3", "CloudNativeSystem_3", List
                        .of());
        RelationType oneToOneRelationType = SchemaRegistry.getRelationTypeByName(oneToOneRelationship.getType());
        Optional<OperationResult> deleteOneToOneRelationshipResult = tiesDbOperations
                .deleteRelationFromEntityTableByRelationId(dslContext, oneToOneRelationship.getId(), oneToOneRelationType);
        assertTrue(deleteOneToOneRelationshipResult.isPresent(), "Delete operation should return a present Optional");

        //One_To_Many Relationship
        Relationship oneToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU",
                "GNBDUFUNCTION_PROVIDES_NRCELLDU_relation_2", "GNBDUFunction_1", "NRCellDU_2", List.of());
        RelationType oneToManyRelationType = SchemaRegistry.getRelationTypeByName(oneToManyRelationship.getType());
        Optional<OperationResult> deleteOneToManyRelationshipResult = tiesDbOperations
                .deleteRelationFromEntityTableByRelationId(dslContext, oneToManyRelationship.getId(),
                        oneToManyRelationType);
        assertTrue(deleteOneToManyRelationshipResult.isPresent(), "Delete operation should return a present Optional");

        //Many_To_One Relationship
        Relationship manyToOneRelationship = new Relationship("o-ran-smo-teiv-cloud",
                "CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE",
                "CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE_relation_3", "CloudNativeApplication_3", "Namespace_3", List
                        .of());
        RelationType manyToOneRelationType = SchemaRegistry.getRelationTypeByName(manyToOneRelationship.getType());
        Optional<OperationResult> deleteManyToOneRelationshipResult = tiesDbOperations
                .deleteRelationFromEntityTableByRelationId(dslContext, manyToOneRelationship.getId(),
                        manyToOneRelationType);
        assertTrue(deleteManyToOneRelationshipResult.isPresent(), "Delete operation should return a present Optional");

        //Many_To_Many Relationship
        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-cloud-to-ran",
                "GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN",
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION_relation_1", "GNBDUFunction_1",
                "CloudNativeApplication_1", List.of());
        RelationType manyToManyRelationType = SchemaRegistry.getRelationTypeByName(manyToManyRelationship.getType());
        Optional<OperationResult> deleteManyToManyRelationshipResult = tiesDbOperations
                .deleteManyToManyRelationByRelationId(dslContext, manyToManyRelationType.getTableName(),
                        manyToManyRelationship.getId());
        assertTrue(deleteManyToManyRelationshipResult.isPresent(), "Delete operation should return a present Optional");

        //One_To_One Relationship ConnectingSameEntity
        Relationship connectingSameEntityOneToOneRelationship = new Relationship("o-ran-smo-teiv-equipment",
                "ANTENNAMODULEEEEEEEEEEEE_DEPLOYED_ON_ANTENNAMODULEEEEEEEEEEEEEEE",
                "ANTENNAMODULE_DEPLOYED_ON_ANTENNAMODULE_relation_1", "AntennaModule_5", "AntennaModule_6", List.of());
        RelationType connectingSameEntityType = SchemaRegistry.getRelationTypeByName(
                connectingSameEntityOneToOneRelationship.getType());
        Optional<OperationResult> deleteConnectingSameEntityOneToOneRelationshipResult = tiesDbOperations
                .deleteManyToManyRelationByRelationId(dslContext, connectingSameEntityType.getTableName(),
                        connectingSameEntityOneToOneRelationship.getId());
        assertTrue(deleteConnectingSameEntityOneToOneRelationshipResult.isPresent(),
                "Delete operation should return a present Optional");

        //One_To_Many Relationship ConnectingSameEntity
        Relationship connectingSameEntityOneToManyRelationship = new Relationship("o-ran-smo-teiv-equipment",
                "ANTENNAMODULEEEEEEEEEEEE_REALISED_BY_ANTENNAMODULEEEEEEEEEEEEEEE",
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE_relation_1", "AntennaModule_1", "AntennaModule_2", List.of());
        connectingSameEntityType = SchemaRegistry.getRelationTypeByName(connectingSameEntityOneToManyRelationship
                .getType());
        Optional<OperationResult> deleteConnectingSameEntityOneToManyRelationshipResult = tiesDbOperations
                .deleteManyToManyRelationByRelationId(dslContext, connectingSameEntityType.getTableName(),
                        connectingSameEntityOneToManyRelationship.getId());
        assertTrue(deleteConnectingSameEntityOneToManyRelationshipResult.isPresent(),
                "Delete operation should return a present Optional");
    }

    @Test
    void testSelectByCmHandleFormSourceIds() {
        Map<String, Object> cna1 = new HashMap<>();
        cna1.put("id", "cloud_native_id1");
        cna1.put("name", "CloudNativeApplication");
        cna1.put("CD_sourceIds", JSONB.jsonb(
                "[\"urn:3gpp:dn:/fdn\"," + "\"urn:cmHandle:/395221E080CCF0FD1924103B15873814\"]"));

        Map<String, Object> cna2 = new HashMap<>();
        cna2.put("id", "cloud_native_id2");
        cna2.put("name", "CloudNativeApplication");
        cna2.put("CD_sourceIds", JSONB.jsonb(
                "[\"urn:3gpp:dn:/fdn\"," + "\"urn:cmHandle:/395221E080CCF0FD1924103B15873815\"]"));

        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna1);
        tiesDbOperations.merge(dslContext, "ties_data.\"CloudNativeApplication\"", cna2);

        List<String> ids = tiesDbOperations.selectByCmHandleFormSourceIds(dslContext,
                "ties_data.\"CloudNativeApplication\"", "395221E080CCF0FD1924103B15873814");
        assertEquals(List.of("cloud_native_id1"), ids);
    }

    @Test
    void testMergeManyToManyRelationshipWithExistingId_SidesNotUpdatable() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-create-many-to-many.json");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(12, mergeResult.size());

        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_2", "GNBCUUP_1", "CloudNativeApplication_3",
                new ArrayList<>());
        final ParsedCloudEventData finalParsedCloudEventData = new ParsedCloudEventData(new ArrayList<>(), List.of(
                manyToManyRelationship));
        assertThrows(TiesException.class, () -> tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                finalParsedCloudEventData));
    }

    @Test
    void testMergeManyToManyRelationshipWithExistingId_SidesSameAsUpdatables() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-create-many-to-many.json");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(12, mergeResult.size());

        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_2", "GNBCUUP_1", "CloudNativeApplication_2",
                new ArrayList<>());
        final ParsedCloudEventData finalParsedCloudEventData = new ParsedCloudEventData(new ArrayList<>(), List.of(
                manyToManyRelationship));
        List<OperationResult> result = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                finalParsedCloudEventData);

        assertEquals(1, result.size());
    }

    @Test
    void testMergeManyToManyWithNonExistingEntities() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-create-many-to-many.json");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(12, mergeResult.size());

        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_4", "GNBCUUP_3", "CloudNativeApplication_4",
                new ArrayList<>());
        parsedCloudEventData = new ParsedCloudEventData(new ArrayList<>(), List.of(manyToManyRelationship));
        List<OperationResult> result = tiesDbOperations.executeEntityAndRelationshipMergeOperations(parsedCloudEventData);

        assertEquals(3, result.size());
    }

    @Test
    void testMergeManyToManyWithOneExistingEntity() throws InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(
                "src/test/resources/cloudeventdata/end-to-end/ce-create-many-to-many.json");
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        List<OperationResult> mergeResult = tiesDbOperations.executeEntityAndRelationshipMergeOperations(
                parsedCloudEventData);
        assertEquals(12, mergeResult.size());

        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_4", "GNBCUUP_1", "CloudNativeApplication_4",
                new ArrayList<>());
        parsedCloudEventData = new ParsedCloudEventData(new ArrayList<>(), List.of(manyToManyRelationship));
        List<OperationResult> result = tiesDbOperations.executeEntityAndRelationshipMergeOperations(parsedCloudEventData);

        assertEquals(2, result.size());
    }

    @Test // Both endpoints exist, and a new relationship ID is received.
    void testMergeWithNewRelationshipId() throws MaximumCardinalityViolationException, InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);

    }

    @Test // Same relationship data is received twice.
    void testMergeWithSameData() throws MaximumCardinalityViolationException, InvalidFieldInYangDataException {

        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        manyToOneResult = mergeSingleTestEvent(VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        oneToManyResult = mergeSingleTestEvent(VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);
    }

    @Test // Existing but free endpoints and an existing relationship ID is received.
    void testMergeWithExistingFreeEndpoints() throws MaximumCardinalityViolationException, InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);

        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one2.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many2.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one2.json"));
    }

    @Test // Used "many" side endpoint with a new relationship ID.
    void testMergeWithUsedManySideAndNewRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);

        assertThrows(MaximumCardinalityViolationException.class, () -> mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one3.json"));
        assertThrows(MaximumCardinalityViolationException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many3.json"));
        assertThrows(MaximumCardinalityViolationException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one3.json"));
    }

    @Test // Used "many" side endpoint with an existing relationship ID.
    void testMergeWithUsedManySideAndExistingRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);

        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one4.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many4.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one4.json"));
    }

    @Test // Missing "one" side endpoint with a new relationship ID.
    void testMergeWithMissingOneSideAndNewRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one5.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many5.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one5.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);
    }

    @Test // Missing "many" side endpoint with a new relationship ID.
    void testMergeWithMissingManySideAndNewRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one6.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many6.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one6.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);
    }

    @Test // Missing both "one" and "many" side endpoints with a new relationship ID.
    void testMergeWithMissingEndpointsAndNewRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one7.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many7.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one7.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);
    }

    @Test // Missing "one" side endpoint with an existing relationship ID.
    void testMergeWithMissingOneSideAndExistingRelationshipId() throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        List<OperationResult> manyToOneResult = mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one.json");
        List<OperationResult> oneToManyResult = mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many.json");
        List<OperationResult> oneToOneResult = mergeSingleTestEvent(VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one.json");

        assertEquals(3, manyToOneResult.size());
        assertEquals(3, oneToManyResult.size());
        assertEquals(3, oneToOneResult.size());

        assertDbContainsOperationResults(manyToOneResult);
        assertDbContainsOperationResults(oneToManyResult);
        assertDbContainsOperationResults(oneToOneResult);

        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_MANY_TO_ONE_DIR + "ce-create-many-to-one8.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_MANY_DIR + "ce-create-one-to-many8.json"));
        assertThrows(TiesException.class, () -> mergeSingleTestEvent(
                VALIDATE_ONE_TO_ONE_DIR + "ce-create-one-to-one8.json"));
    }

    @Test
    void testOperationResultFromRelationship() {
        Relationship relationship = new Relationship("o-ran-smo-teiv-equipment", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE",
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE_relation_1", "AntennaModule_1", "AntennaModule_2", List.of());

        Map<String, Object> relationshipSides = new HashMap<>();
        relationshipSides.put("aSide", relationship.getASide());
        relationshipSides.put("bSide", relationship.getBSide());

        OperationResult result = OperationResult.createFromRelationship(relationship);
        assertEquals(result.getId(), relationship.getId());
        assertEquals(result.getEntryType(), relationship.getType());
        assertEquals(result.getContent(), relationshipSides);
    }

    @Test
    void testRelationRelatedMethodsWhenRelationshipIsStoredInSeparateTable() {

        Relationship manyToManyRelationship = new Relationship("o-ran-smo-teiv-ran",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "relation_4", "GNBCUUP_1", "CloudNativeApplication_4",
                new ArrayList<>());

        assertNull(manyToManyRelationship.getStoringSideEntityId());
        assertNull(manyToManyRelationship.getNotStoringSideEntityId());

        RelationType manyToManyRelationType = SchemaRegistry.getRelationTypeByName(manyToManyRelationship.getType());

        assertNull(manyToManyRelationType.getNotStoringSideTableName());
        assertNull(manyToManyRelationType.getNotStoringSideEntityIdColumnNameInStoringSideTable());
        assertNull(manyToManyRelationType.getStoringSideEntityType());
        assertNull(manyToManyRelationType.getNotStoringSideEntityType());

    }

    @Test
    void testRelationRelatedMethodsWhenRelationshipIsStoredOnBSide() {

        Relationship relationship = new Relationship("o-ran-smo-teiv-ran", "GNBDUFUNCTION_PROVIDES_NRCELLDU", "relation_2",
                "GNBDUFunction_1", "NRCellDU_5", new ArrayList<>());

        assertEquals("NRCellDU_5", relationship.getStoringSideEntityId());
        assertEquals("GNBDUFunction_1", relationship.getNotStoringSideEntityId());

        RelationType relationType = SchemaRegistry.getRelationTypeByName(relationship.getType());

        assertEquals("ties_data.\"GNBDUFunction\"", relationType.getNotStoringSideTableName());
        assertEquals("REL_FK_provided-by-gnbduFunction", relationType
                .getNotStoringSideEntityIdColumnNameInStoringSideTable());
        assertEquals("NRCellDU", relationType.getStoringSideEntityType());
        assertEquals("GNBDUFunction", relationType.getNotStoringSideEntityType());

    }

    private List<OperationResult> mergeSingleTestEvent(String path) throws MaximumCardinalityViolationException,
            InvalidFieldInYangDataException {
        CloudEvent cloudEvent = CloudEventTestUtil.getCloudEventFromJsonFile(path);
        ParsedCloudEventData parsedCloudEventData = cloudEventParser.getCloudEventData(cloudEvent);
        return tiesDbOperations.executeEntityAndRelationshipMergeOperations(parsedCloudEventData);
    }

    private void assertDbContainsOperationResults(List<OperationResult> results) {
        for (OperationResult result : results) {
            boolean isRelation = result.getContent().containsKey(PROPERTY_A_SIDE) && result.getContent().containsKey(
                    PROPERTY_B_SIDE);
            if (isRelation) {
                RelationType relationType = SchemaRegistry.getRelationTypeByName(result.getEntryType());
                tableContainsId(relationType.getTableName(), relationType.getIdColumnName(), result);
            } else {
                tableContainsId("ties_data.\"" + result.getEntryType() + "\"", "id", result);
            }
        }
    }

    private void tableContainsId(String tableName, String idColumn, OperationResult result) {
        Result<Record> dbResults = tiesDbService.selectAllRowsFromTable(tableName);
        final boolean contains = dbResults.stream().map(row -> row.get(idColumn)).filter(Objects::nonNull).map(
                Object::toString).anyMatch(result.getId()::equals);
        assertTrue(contains);
    }
}
