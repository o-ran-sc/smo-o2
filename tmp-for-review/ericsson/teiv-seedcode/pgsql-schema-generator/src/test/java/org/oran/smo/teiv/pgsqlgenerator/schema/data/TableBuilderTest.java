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
package org.oran.smo.teiv.pgsqlgenerator.schema.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.oran.smo.teiv.pgsqlgenerator.Attribute;
import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.Entity;
import org.oran.smo.teiv.pgsqlgenerator.UniqueConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.ForeignKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.TestHelper;
import org.oran.smo.teiv.pgsqlgenerator.schema.model.HashInfoDataGenerator;
import lombok.extern.slf4j.Slf4j;

import static org.oran.smo.teiv.pgsqlgenerator.Constants.CLASSIFIERS;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.DECORATORS;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.JSONB;

@Slf4j
@SpringBootTest(classes = { TableBuilder.class, HashInfoDataGenerator.class })
class TableBuilderTest {
    @Autowired
    private TableBuilder tableBuilder;

    @BeforeEach
    void setUp() {

        //mocks
        MockitoAnnotations.openMocks(this);

    }

    //spotless:off
    List<Entity> entities = List.of(
            Entity.builder().entityName("Sector").attributes(
                List.of(
                    Attribute.builder().name("azimuth").dataType("DECIMAL").build(),
                    Attribute.builder().name("id").dataType("VARCHAR(511)").constraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                            .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                    Attribute.builder().name("geo-location").dataType("geography").build(),
                    Attribute.builder().name("sectorId").dataType("jsonb").build()))
                .moduleReferenceName("").build(),

            Entity.builder().entityName("Namespace").attributes(
                List.of(
                    Attribute.builder().name("id").dataType("VARCHAR(511)").constraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                            .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                    Attribute.builder().name("name").dataType("TEXT").build()))
                .moduleReferenceName("").build());

    List<List<Relationship>> relationships = List.of(
            List.of(Relationship.builder().name("oneToOne")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(1)
                    .bSideAssociationName("used-by-Namespace")
                    .bSideMOType("Namespace")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(1)
                    .relationshipDataLocation("A_SIDE")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(false)
                    .moduleReferenceName("").build()),
            List.of(Relationship.builder().name("oneToMany")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(1)
                    .bSideAssociationName("used-by-Namespace")
                    .bSideMOType("Namespace")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(100)
                    .relationshipDataLocation("B_SIDE")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(false)
                    .moduleReferenceName("").build()),
            List.of(Relationship.builder().name("manyToOne")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(100)
                    .bSideAssociationName("used-by-Namespace")
                    .bSideMOType("Namespace")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(1)
                    .relationshipDataLocation("A_SIDE")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(false)
                    .moduleReferenceName("").build()),
            List.of(Relationship.builder().name("manyToMany")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(100)
                    .bSideAssociationName("used-by-Namespace")
                    .bSideMOType("Namespace")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(100)
                    .relationshipDataLocation("RELATION")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(false)
                    .moduleReferenceName("").build()),
            // Relationship connecting same entity 1:1
            List.of(Relationship.builder().name("relationshipConnectingSameEntity")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(1)
                    .bSideAssociationName("used-by-Sector")
                    .bSideMOType("Sector")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(1)
                    .relationshipDataLocation("RELATION")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(true)
                    .moduleReferenceName("").build()),
            // Relationship connecting same entity 1:N
            List.of(Relationship.builder().name("relationshipConnectingSameEntity")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(1)
                    .bSideAssociationName("used-by-Sector")
                    .bSideMOType("Sector")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(100)
                    .relationshipDataLocation("RELATION")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(true)
                    .moduleReferenceName("").build()),
            // Relationship connecting same entity N:1
            List.of(Relationship.builder().name("relationshipConnectingSameEntity")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(100)
                    .bSideAssociationName("used-by-Sector")
                    .bSideMOType("Sector")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(1)
                    .relationshipDataLocation("RELATION")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(true)
                    .moduleReferenceName("").build()),
            // Relationship connecting same entity N:M
            List.of(Relationship.builder().name("relationshipConnectingSameEntity")
                    .aSideAssociationName("used-Sector")
                    .aSideMOType("Sector")
                    .aSideMinCardinality(0)
                    .aSideMaxCardinality(100)
                    .bSideAssociationName("used-by-Sector")
                    .bSideMOType("Sector")
                    .bSideMinCardinality(0)
                    .bSideMaxCardinality(100)
                    .relationshipDataLocation("RELATION")
                    .associationKind("BI_DIRECTIONAL")
                    .connectSameEntity(true)
                    .moduleReferenceName("").build()));
    //spotless:on

    @Test
    void checkOneToOneRelationshipMappingTest() {

        //spotless:off

        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name("REL_FK_used-Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Sector_REL_FK_used-Sector").tableName("Sector")
                                .referencedTable("Namespace").columnToAddConstraintTo("REL_FK_used-sector").build())).build(),
                Column.builder().name("REL_ID_oneToOne").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_oneToOne").tableName("Sector")
                                .columnToAddConstraintTo("REL_ID_oneToOne").build())).build(),
                Column.builder().name("REL_CD_sourceIds_oneToOne").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType(JSONB).defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType(JSONB).defaultValue("{}").build(),
                    Column.builder().name(String.format("REL_CD_%s_oneToOne", CLASSIFIERS)).dataType(JSONB).defaultValue("[]").build(),
                    Column.builder().name(String.format("REL_CD_%s_oneToOne", DECORATORS)).dataType(JSONB).defaultValue("{}").build())).build(),

        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build());

        //spotless:on

        // When
        List<Table> actualResult = tableBuilder.getTables(entities, relationships.get(0));

        // Then
        runTest(actualResult, expectedResult);
    }

    @Test
    void checkOneToManyRelationshipMappingTest() {

        //spotless:off

        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build(),

        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name("REL_FK_used-by-Namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Namespace_REL_FK_used-by-Namespace")
                                .tableName("Namespace").referencedTable("Sector").columnToAddConstraintTo("REL_FK_used-by-Namespace").build())).build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build(),
                Column.builder().name("REL_ID_oneToMany").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Namespace_REL_ID_oneToMany").tableName("Namespace")
                                .columnToAddConstraintTo("REL_ID_oneToMany").build())).build(),
                Column.builder().name("REL_CD_sourceIds_oneToMany").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("REL_CD_%s_oneToMany", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("REL_CD_%s_oneToMany", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build());
        //spotless:on

        // When
        List<Table> actualResult = tableBuilder.getTables(entities, relationships.get(1));

        // Then
        runTest(actualResult, expectedResult);

    }

    @Test
    void checkManyToOneRelationshipMappingTest() {

        //spotless:off

        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build(),
                Column.builder().name("REL_FK_used-Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Sector_REL_FK_used-Sector").tableName("Sector")
                                .referencedTable("Namespace").columnToAddConstraintTo("REL_FK_used-sector").build())).build(),
                Column.builder().name("REL_ID_manyToOne").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_manyToOne").tableName("Sector")
                                .columnToAddConstraintTo("REL_ID_oneToOne").build())).build(),
                Column.builder().name("REL_CD_sourceIds_manyToOne").dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("REL_CD_%s_manyToOne", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("REL_CD_%s_manyToOne", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build(),

        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build());
        //spotless:on

        // When
        List<Table> actualResult = tableBuilder.getTables(entities, relationships.get(2));

        // Then
        runTest(actualResult, expectedResult);

    }

    @Test
    void checkManyToManyRelationshipMappingTest() {
        //spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build(),

        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build(),

        Table.builder().name("manyToMany").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_manyToMany_id")
                        .tableName("manyToMany").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("aSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_manyToMany_aSide_Sector")
                                .tableName("manyToMany").referencedTable("Sector").columnToAddConstraintTo("aSide_Sector").build())).build(),
                Column.builder().name("bSide_Namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_manyToMany_bSide_Namespace")
                                .tableName("manyToMany").referencedTable("Namespace").columnToAddConstraintTo("bSide_Namespace").build())).build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                    Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build());
        //spotless:on

        // When
        List<Table> actualResult = tableBuilder.getTables(entities, relationships.get(3));

        // Then
        runTest(actualResult, expectedResult);
    }

    @Test
    void checkRelationshipConnectingSameEntityMappingTest() {
        //spotless:off
        List<Entity> sameEntities = List.of(

            Entity.builder().entityName("Sector").attributes(
                List.of(Attribute.builder().name("azimuth").dataType("DECIMAL").build(),
                    Attribute.builder().name("id").dataType("VARCHAR(511)").constraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                            .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                    Attribute.builder().name("sectorId").dataType("jsonb").build(),
                    Attribute.builder().name("geo-location").dataType("geography").build()))
                .moduleReferenceName("").build());

        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build(),

        Table.builder().name("relationshipConnectingSameEntity").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder()
                        .constraintName("PK_relationshipConnectingSameEntity_id").tableName("relationshipConnectingSameEntity").columnToAddConstraintTo("id")
                        .build())).build(),
                Column.builder().name("aSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_relationshipConnectingSameEntity_aSide_Sector")
                                .tableName("relationshipConnectingSameEntity").referencedTable("Sector").columnToAddConstraintTo("aSide_Sector").build())).build(),
                Column.builder().name("bSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_relationshipConnectingSameEntity_bSide_Sector")
                                .tableName("relationshipConnectingSameEntity").referencedTable("Sector").columnToAddConstraintTo("bSide_Sector").build())).build(),
                Column.builder().name("CD_sourceIds").dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", CLASSIFIERS)).dataType("jsonb").defaultValue("[]").build(),
                Column.builder().name(String.format("CD_%s", DECORATORS)).dataType("jsonb").defaultValue("{}").build())).build());
        //spotless:on

        // When
        List<Table> actualResultOneToOne = tableBuilder.getTables(sameEntities, relationships.get(4));
        List<Table> actualResultOneToMany = tableBuilder.getTables(sameEntities, relationships.get(5));
        List<Table> actualResultManyToOne = tableBuilder.getTables(sameEntities, relationships.get(6));
        List<Table> actualResultManyToMany = tableBuilder.getTables(sameEntities, relationships.get(7));

        // Then
        // Test relationship connecting same entity (one to one)
        runTest(actualResultOneToOne, expectedResult);

        // Test relationship connecting same entity (one to many)
        runTest(actualResultOneToMany, expectedResult);

        // Test relationship connecting same entity (many to one)
        runTest(actualResultManyToOne, expectedResult);

        // Test relationship connecting same entity (many to many)
        runTest(actualResultManyToMany, expectedResult);

    }

    void runTest(List<Table> generatedTables, List<Table> expectedTables) {

        List<String> allTableNamesFromGeneratedResult = TestHelper.extractTableNames(generatedTables);
        List<String> allTableNamesFromExpectedResult = TestHelper.extractTableNames(expectedTables);

        // Check if generatedResult contains all tables
        Assertions.assertEquals(allTableNamesFromExpectedResult, allTableNamesFromGeneratedResult);

        //spotless:off
        expectedTables.forEach(expectedTable -> {
            generatedTables.stream().filter(generatedTable -> generatedTable.getName().equals(expectedTable.getName())).findFirst()
                .ifPresent(generatedTable -> {
                    List<Column> columnsInExpected = expectedTable.getColumns();
                    List<Column> columnsInGenerated = generatedTable.getColumns();

                    // Check if all columns for each table were added correctly
                    Assertions.assertEquals(columnsInExpected.size(), columnsInGenerated.size());

                    List<String> allColumnNamesForATableInGeneratedResult = TestHelper.extractColumnNamesForATable(columnsInGenerated);
                    List<String> allColumnNamesForATableInExpectedResult = TestHelper.extractColumnNamesForATable(columnsInExpected);

                    // Check if generatedResult contains all columns for a table
                    Assertions.assertEquals(allColumnNamesForATableInExpectedResult, allColumnNamesForATableInGeneratedResult);

                    columnsInExpected.forEach(columnInExpected -> {
                        columnsInGenerated.stream().filter(columnInGenerated -> columnInGenerated.getName().equals(columnInExpected.getName()))
                            .findFirst().ifPresent(columnInGenerated -> {

                                if (columnInExpected.getName().equals("id")) {
                                    Assertions.assertEquals("VARCHAR(511)", columnInGenerated.getDataType());
                                    Assertions.assertTrue(TestHelper.checkIfColumnIsPrimaryKey(columnInGenerated.getPostgresConstraints()));
                                }

                                // Check if each attributes' datatype is picked correctly
                                Assertions.assertEquals(columnInExpected.getDataType().replace("\"", ""), columnInGenerated.getDataType());

                                // Check if each attributes' default value is picked correctly
                                Assertions.assertEquals(columnInExpected.getDefaultValue(), columnInGenerated.getDefaultValue());

                                if (!columnInExpected.getPostgresConstraints().isEmpty()) {

                                    Assertions.assertEquals(columnInExpected.getPostgresConstraints().size(), columnInGenerated.getPostgresConstraints().size());

                                    List<String> expectedConstraintNames = TestHelper.extractConstraintName(columnInExpected.getPostgresConstraints());
                                    List<String> actualConstraintNames = TestHelper.extractConstraintName(columnInGenerated.getPostgresConstraints());

                                    // Check if constraint names in expected result match with those in actual result
                                    Assertions.assertEquals(expectedConstraintNames, actualConstraintNames);

                                    columnInExpected.getPostgresConstraints().forEach(constraint -> {
                                        columnInGenerated.getPostgresConstraints().stream()
                                            .filter(constraint1 -> constraint1.getConstraintName().equals(constraint.getConstraintName())).findFirst()
                                            .ifPresent(constraint1 -> {

                                                // Check table name where constraint is to be added
                                                Assertions.assertEquals(constraint.getTableToAddConstraintTo(), constraint.getTableToAddConstraintTo());

                                                // Check column where constraint is to be added
                                                Assertions.assertEquals(constraint.getColumnToAddConstraintTo(), constraint.getColumnToAddConstraintTo());

                                                if (constraint instanceof ForeignKeyConstraint expectedFk) {
                                                    ForeignKeyConstraint actualFK = (ForeignKeyConstraint) constraint1;

                                                    Assertions.assertEquals(expectedFk.getReferencedTable(), actualFK.getReferencedTable());
                                                }
                                            });
                                    });
                                }
                            });
                    });
                });
        });
        //spotless:on
    }

    Collection<Object> addEModelPrimaryKeyConstraint() {
        Collection<Object> eModelPrimaryKeyConstraint = new ArrayList<>();
        try {
            Constructor<PrimaryKeyConstraint> constructor = PrimaryKeyConstraint.class.getDeclaredConstructor();
            eModelPrimaryKeyConstraint.add(constructor.newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Failure in tests --> Error while adding primary key constraint: " + e.getMessage());
        }
        return eModelPrimaryKeyConstraint;
    }

}
