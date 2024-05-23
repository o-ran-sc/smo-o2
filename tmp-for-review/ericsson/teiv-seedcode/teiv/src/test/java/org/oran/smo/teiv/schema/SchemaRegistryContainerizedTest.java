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
package org.oran.smo.teiv.schema;

import static org.oran.smo.teiv.schema.RelationshipDataLocation.A_SIDE;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.B_SIDE;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.TEIV_DOMAIN;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.jooq.impl.DSL.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.oran.smo.teiv.db.TestPostgresqlContainer;
import org.oran.smo.teiv.exception.TiesException;

class SchemaRegistryContainerizedTest {
    public static TestPostgresqlContainer postgreSQLContainer = TestPostgresqlContainer.getInstance();
    private static DSLContext dslContext;

    @BeforeAll
    public static void beforeAll() throws UnsupportedOperationException, SchemaLoaderException {
        String url = postgreSQLContainer.getJdbcUrl();
        TestPostgresqlContainer.loadSampleData();
        DataSource ds = DataSourceBuilder.create().url(url).username("test").password("test").build();
        dslContext = DSL.using(ds, SQLDialect.POSTGRES);
        PostgresSchemaLoader postgresSchemaLoader = new PostgresSchemaLoader(dslContext, new ObjectMapper());
        postgresSchemaLoader.loadSchemaRegistry();
    }

    @BeforeEach
    public void deleteAll() {
        dslContext.meta().filterSchemas(s -> s.getName().equals(TIES_DATA_SCHEMA)).getTables().forEach(t -> dslContext
                .truncate(t).cascade().execute());
    }

    @Test
    void testGetModulesByName() {
        //given
        String expectedName = "o-ran-smo-teiv-cloud-to-ran";
        String expectedNamespace = "urn:o-ran:smo-teiv-cloud-to-ran";
        String expectedDomain = "CLOUD_TO_RAN";
        List<String> expectedIncludedModules = List.of("o-ran-smo-teiv-cloud", "o-ran-smo-teiv-ran");
        //when
        Module logicalToCloud = SchemaRegistry.getModuleByName("o-ran-smo-teiv-cloud-to-ran");
        //then
        assertEquals(9, SchemaRegistry.getModuleRegistry().size());
        assertTrue(SchemaRegistry.getModuleRegistry().containsValue(logicalToCloud));
        assertEquals(expectedName, logicalToCloud.getName());
        assertEquals(expectedNamespace, logicalToCloud.getNamespace());
        assertEquals(expectedDomain, logicalToCloud.getDomain());
        assertEquals(expectedIncludedModules, logicalToCloud.getIncludedModuleNames());
        assertThrows(TiesException.class, () -> SchemaRegistry.getModuleByName("invalid-module"));
    }

    @Test
    void testGetDomainsForModules() {
        //given

        Set<String> expectedDomains = Set.of(TEIV_DOMAIN, "OAM_TO_CLOUD", "EQUIPMENT_TO_RAN", "RAN", "OAM", "CLOUD",
                "EQUIPMENT", "CLOUD_TO_RAN", "OAM_TO_RAN");
        //when
        Set<String> actualDomains = SchemaRegistry.getDomains();
        //then
        assertEquals(expectedDomains, actualDomains);
    }

    @Test
    void testRootDomainIncludesAllAvailableDomains() {
        //given
        Set<String> availableDomains = SchemaRegistry.getDomains();
        availableDomains.remove(TEIV_DOMAIN);
        //when
        List<String> rootIncludedDomains = SchemaRegistry.getIncludedDomains(TEIV_DOMAIN);
        //then
        assertEquals(availableDomains.size(), rootIncludedDomains.size());
        assertTrue(availableDomains.containsAll(rootIncludedDomains));
    }

    @Test
    void testGetModuleByDomainTrowsUnknownDomainException() {
        assertThrows(TiesException.class, () -> SchemaRegistry.getModuleByDomain("throwError"));
    }

    //Entities
    @Test
    void testGetEntityNames() {
        //given
        Set<String> expectedEntityName = Set.of("Site", "ManagedElementtttttttttttttttttttttttttttttttttttttttttttttttttt",
                "ENodeBFunction", "CloudNativeApplication", "AntennaModule", "Sector",
                "Namespaceeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", "NRCellDU", "LTESectorCarrier",
                "ManagedElement", "NRCellCU", "NRSectorCarrier", "PhysicalNetworkAppliance", "Namespace", "GNBCUUPFunction",
                "NodeCluster", "CloudNativeSystem", "NRCellDUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU",
                "CloudNativeSystemmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm",
                "CloudNativeApplicationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                "ANTENNAMODULEEEEEEEEEEEE_REALISED_BY_ANTENNAMODULEEEEEEEEEEEEEEE", "GNBDUFunction", "CloudSite",
                "EUtranCell", "GNBCUCPFunction", "AntennaCapability", "TestEntityA", "TestEntityB");
        //when
        Set<String> actualEntityName = SchemaRegistry.getEntityNames();
        //then
        assertEquals(expectedEntityName.size(), actualEntityName.size());
        assertEquals(expectedEntityName, actualEntityName);
    }

    @Test
    void testGetTableNameForEntity() {
        //given
        EntityType gnbduFunction = SchemaRegistry.getEntityTypeByName("GNBDUFunction");
        //then
        assertEquals("ties_data.\"GNBDUFunction\"", gnbduFunction.getTableName());
    }

    @Test
    void testGetFieldsForEntity() {
        //given
        EntityType gnbduFunction = SchemaRegistry.getEntityTypeByName("GNBDUFunction");
        //then
        assertEquals(Set.of(field("dUpLMNId", JSONB.class).as("dUpLMNId"), field("gNBDUId").as("gNBDUId"), field("gNBId")
                .as("gNBId"), field("gNBIdLength").as("gNBIdLength"), field("cmId", JSONB.class).as("cmId"), field("fdn")
                        .as("fdn"), field("id").as("id"), field("CD_sourceIds").as("CD_sourceIds")), new HashSet<>(
                                gnbduFunction.getAllFieldsWithId()));
    }

    @Test
    void testGetAttrColumnsForEntity() {
        //given
        List<String> expectedColumns = List.of("ties_data.\"GNBDUFunction\".\"gNBDUId\"",
                "ties_data.\"GNBDUFunction\".\"gNBId\"", "ties_data.\"GNBDUFunction\".\"gNBIdLength\"",
                "ties_data.\"GNBDUFunction\".\"dUpLMNId\"", "ties_data.\"GNBDUFunction\".\"cmId\"",
                "ties_data.\"GNBDUFunction\".\"fdn\"", "ties_data.\"GNBDUFunction\".\"id\"");
        EntityType gnbduFunction = SchemaRegistry.getEntityTypeByName("GNBDUFunction");
        //then
        List<String> columns = gnbduFunction.getAttributeColumnsWithId();
        assertEquals(expectedColumns.size(), columns.size());
        assertTrue(expectedColumns.containsAll(columns));
    }

    @Test
    void testGetAttrColumnsForEntityWithLongNames() {
        //given
        List<String> expectedColumns = List.of("ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"gNBDUId\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"gNBId\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"gNBIdLength\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"dUpLMNId\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"cmId\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"3786A6CA64C9422F9E7FC35B7B039F345BBDDA65\"",
                "ties_data.\"7D7AACEBB0E4E4732835BA4BFE708DDD3738962D\".\"id\"");
        EntityType gnbduFunction = SchemaRegistry.getEntityTypeByName(
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        //then
        List<String> columns = gnbduFunction.getAttributeColumnsWithId();
        assertEquals(expectedColumns.size(), columns.size());
        assertTrue(expectedColumns.containsAll(columns));
    }

    @Test
    void testGetEntityTypesByDomain() {
        //given
        List<String> expectedEntities = List.of("Site", "ENodeBFunction", "AntennaModule", "Sector", "NRCellDU",
                "LTESectorCarrier", "NRCellCU", "NRSectorCarrier", "PhysicalNetworkAppliance", "GNBCUUPFunction",
                "GNBDUFunction", "EUtranCell", "GNBCUCPFunction", "AntennaCapability",
                "GNBDUFunctionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "AntennaModuleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                "ANTENNAMODULEEEEEEEEEEEE_REALISED_BY_ANTENNAMODULEEEEEEEEEEEEEEE",
                "NRCellDUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU", "TestEntityA", "TestEntityB");
        //when
        List<String> ranLogicalToEquipmentEntityTypes = SchemaRegistry.getEntityNamesByDomain("EQUIPMENT_TO_RAN");
        //then
        assertEquals(expectedEntities.size(), ranLogicalToEquipmentEntityTypes.size());
        assertTrue(expectedEntities.containsAll(ranLogicalToEquipmentEntityTypes));
    }

    //Relations
    @Test
    void getRelationNames() {
        //when
        SchemaRegistry.getRelationNames();
        //then
        assertEquals(41, SchemaRegistry.getRelationNames().size());
    }

    @Test
    void testGetRelationTypeByName() {
        //when
        RelationType managedElementManagesEnodebfunction = SchemaRegistry.getRelationTypeByName(
                "MANAGEDELEMENT_MANAGES_ENODEBFUNCTION");
        //then
        assertTrue(SchemaRegistry.getRelationTypes().contains(managedElementManagesEnodebfunction));
        Association expectedAssociation = new Association("managed-enodebFunction", 1, 1);
        assertEquals(expectedAssociation.toString(), managedElementManagesEnodebfunction.getASideAssociation().toString());

    }

    @Test
    void testGetRelationTypesByEntityType() {
        //given
        List<RelationType> expectedRelationsList = new ArrayList<>();
        expectedRelationsList.add(SchemaRegistry.getRelationTypeByName(
                "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION"));
        expectedRelationsList.add(SchemaRegistry.getRelationTypeByName("GNBCUCPFUNCTION_PROVIDES_NRCELLCU"));
        expectedRelationsList.add(SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION"));
        //then
        assertEquals(3, SchemaRegistry.getRelationTypesByEntityName("GNBCUCPFunction").size());
        assertTrue(SchemaRegistry.getRelationTypesByEntityName("GNBCUCPFunction").containsAll(expectedRelationsList));
    }

    @Test
    void testGetFullyQualifiedNameForRelation() {
        //given
        RelationType gnbduFunctionRealisedByCloudnativeapplication = SchemaRegistry.getRelationTypeByName(
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        //then
        assertEquals("o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION",
                gnbduFunctionRealisedByCloudnativeapplication.getFullyQualifiedName());
    }

    @Test
    void testGetTableNameForRelation() {
        //given
        String expectedManyToMany = "ties_data.\"GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION\"";
        String expectedOneToOne = "ties_data.\"ManagedElement\"";
        String expectedOneToMany = "ties_data.\"GNBDUFunction\"";
        String expectedManyToOne = "ties_data.\"NodeCluster\"";
        String expectedRelConnectingSameEntity = "ties_data.\"ANTENNAMODULE_REALISED_BY_ANTENNAMODULE\"";
        //when
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        RelationType oneToOne = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");
        RelationType manyToOne = SchemaRegistry.getRelationTypeByName("NODECLUSTER_LOCATED_AT_CLOUDSITE");
        RelationType relConnectingSameEntity = SchemaRegistry.getRelationTypeByName(
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");
        //then
        assertEquals(expectedManyToMany, manyToMany.getTableName());
        assertEquals(expectedOneToOne, oneToOne.getTableName());
        assertEquals(expectedOneToMany, oneToMany.getTableName());
        assertEquals(expectedManyToOne, manyToOne.getTableName());
        assertEquals(expectedRelConnectingSameEntity, relConnectingSameEntity.getTableName());
    }

    @Test
    void testGetRelationshipDataLocationForRelation() {
        //when
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        RelationType oneToOne = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");
        RelationType manyToOne = SchemaRegistry.getRelationTypeByName("NODECLUSTER_LOCATED_AT_CLOUDSITE");
        RelationType relConnectingSameEntity = SchemaRegistry.getRelationTypeByName(
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");
        //then
        assertEquals(RELATION, manyToMany.getRelationshipStorageLocation());
        assertEquals(A_SIDE, oneToOne.getRelationshipStorageLocation());
        assertEquals(B_SIDE, oneToMany.getRelationshipStorageLocation());
        assertEquals(A_SIDE, manyToOne.getRelationshipStorageLocation());
        assertEquals(RELATION, relConnectingSameEntity.getRelationshipStorageLocation());
    }

    @Test
    void testGetIdColumnNameRelationTest() {
        //given
        String expectedManyToManyId = "id";
        String expectedOneToManyId = "REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION";
        //when
        RelationType manyToManyRelation = SchemaRegistry.getRelationTypeByName(
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");
        //then
        assertEquals(expectedManyToManyId, manyToManyRelation.getIdColumnName());
        assertEquals(expectedOneToManyId, oneToMany.getIdColumnName());
    }

    @Test
    void testGetASideColumnNameForRelationType() {
        //given
        String expectedManyToMAny = "aSide_GNBDUFunction";
        String expectedOneToOne = "id";
        String expectedOneToMany = "REL_FK_managed-by-managedElement";
        //when
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        RelationType oneToOne = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");
        //then
        assertEquals(expectedManyToMAny, manyToMany.aSideColumnName());
        assertEquals(expectedOneToOne, oneToOne.aSideColumnName());
        assertEquals(expectedOneToMany, oneToMany.aSideColumnName());
    }

    @Test
    void testGetBSideColumnNameForRelationType() {
        //given
        String expectedManyToMAny = "bSide_CloudNativeApplication";
        String expectedOneToOne = "REL_FK_deployed-as-cloudNativeSystem";
        String expectedOneToMany = "id";
        //when
        RelationType manyToMany = SchemaRegistry.getRelationTypeByName("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        RelationType oneToOne = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM");
        RelationType oneToMany = SchemaRegistry.getRelationTypeByName("MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");
        //then
        assertEquals(expectedManyToMAny, manyToMany.bSideColumnName());
        assertEquals(expectedOneToOne, oneToOne.bSideColumnName());
        assertEquals(expectedOneToMany, oneToMany.bSideColumnName());
    }

    @Test
    void testGetFieldsForRelationType() {
        //given
        RelationType gnbduFunctionRealisedByCloudnativeapplication = SchemaRegistry.getRelationTypeByName(
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        //then
        assertEquals(List.of(field(String.format(TIES_DATA,
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(QUOTED_STRING,
                        "aSide_GNBDUFunction")), field(String.format(TIES_DATA,
                                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(QUOTED_STRING,
                                        "bSide_CloudNativeApplication")), field(String.format(TIES_DATA,
                                                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(
                                                        QUOTED_STRING, "id")), field(String.format(TIES_DATA,
                                                                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String
                                                                        .format(QUOTED_STRING, "CD_sourceIds"))),
                gnbduFunctionRealisedByCloudnativeapplication.getAllFieldsWithId());
    }

    @Test
    void testGetRelationTypesByDomain() {
        //given
        List<String> expectedRelations = List.of("CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION",
                "CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE", "NRCELLDU_USES_NRSECTORCARRIER",
                "GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", "ENODEBFUNCTION_PROVIDES_EUTRANCELL",
                "NRSECTORCARRIER_USES_ANTENNACAPABILITY", "GNBDUFUNCTION_PROVIDES_NRCELLDU",
                "GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "EUTRANCELL_USES_LTESECTORCARRIER",
                "NODECLUSTER_LOCATED_AT_CLOUDSITE", "NAMESPACE_DEPLOYED_ON_NODECLUSTER",
                "GNBCUCPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION", "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION",
                "ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER", "GNBCUCPFUNCTION_PROVIDES_NRCELLCU",
                "LTESECTORCARRIER_USES_ANTENNACAPABILITY",
                "GNBDUFUNCTIONNNNNNNNNNNNNNUUU_PROVIDES_NRCELLDUUUUUUUUUUUUUUUUUU",
                "GNBDUFUNCTIONNNNNNNNN_REALISED_BY_CLOUDNATIVEAPPLICATIONNNNNNNNN",
                "CLOUDNATIVEAPPLICATIONNNNNNNNNNN_DEPLOYED_ON_NAMESPACEEEEEEEEEEE", "TESTENTITYA_GROUPS_TESTENTITYB",
                "TESTENTITYA_USES_TESTENTITYB", "TESTENTITYA_PROVIDES_TESTENTITYB", "SECTOR_GROUPS_NRCELLDU");
        //when
        List<String> ranLogicalToCloudRelations = SchemaRegistry.getRelationNamesByDomain("CLOUD_TO_RAN");
        //then
        assertEquals(expectedRelations.size(), ranLogicalToCloudRelations.size());
        assertTrue(expectedRelations.containsAll(ranLogicalToCloudRelations));
    }

    @Test
    void testGetIncludedModules() {
        //when
        Module module = SchemaRegistry.getModuleByName("o-ran-smo-teiv-oam-to-ran");
        //then
        assertEquals(2, module.getIncludedModuleNames().size());
        assertTrue(List.of("o-ran-smo-teiv-oam", "o-ran-smo-teiv-ran").containsAll(module.getIncludedModuleNames()));
    }

    @Test
    void testGetIncludedDomains() {
        //when
        List<String> includedDomains = SchemaRegistry.getIncludedDomains("CLOUD_TO_RAN");
        //then
        assertEquals(2, includedDomains.size());
        assertTrue(List.of("RAN", "CLOUD").containsAll(includedDomains));
    }
}
