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
package org.oran.smo.teiv.pgsqlgenerator.schema;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.oran.smo.teiv.pgsqlgenerator.Relationship;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.UniqueConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.TestHelper;
import org.oran.smo.teiv.pgsqlgenerator.ForeignKeyConstraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaParserTest {

    private String mockForEntities;
    private String mockForModelRels;
    private String mockForRelOneToOne;
    private String mockForRelOneToMany;
    private String mockForRelManyToOne;
    private String mockForRelManyToMany;
    private String mockForRelSameEntities;
    private String testSqlFileForProcessorTest;

    @BeforeEach
    void setUp() {
        String mockDir = "src/test/resources/SchemaParserTest/";
        mockForModelRels = mockDir + "model/mock_01_init-oran-smo-teiv-model.sql";
        mockForEntities = mockDir + "data/entities/mock_00_init-oran-smo-teiv-data.sql";
        mockForRelOneToOne = mockDir + "data/relationships/oneToOne/mock_00_init-oran-smo-teiv-data.sql";
        mockForRelOneToMany = mockDir + "data/relationships/oneToMany/mock_00_init-oran-smo-teiv-data.sql";
        mockForRelManyToOne = mockDir + "data/relationships/manyToOne/mock_00_init-oran-smo-teiv-data.sql";
        mockForRelManyToMany = mockDir + "data/relationships/manyToMany/mock_00_init-oran-smo-teiv-data.sql";
        mockForRelSameEntities = mockDir + "data/relationships/sameEntities/mock_00_init-oran-smo-teiv-data.sql";
        testSqlFileForProcessorTest = "target/TEST_00_init-oran-smo-teiv-data.sql";
    }

    @Test
    void checkIfRelationshipsAreExtractedCorrectlyFromBaselineModelTest() {
        //Given
        List<Relationship> expectedRelationships = List.of(Relationship.builder().name(
                "ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER").aSideAssociationName("provided-lteSectorCarrier").aSideMOType(
                        "ENodeBFunction").aSideMinCardinality(1).aSideMaxCardinality(1).bSideAssociationName(
                                "provided-by-enodebFunction").bSideMOType("LTESectorCarrier").bSideMinCardinality(0)
                .bSideMaxCardinality(100).associationKind("BI_DIRECTIONAL").relationshipDataLocation("B_SIDE")
                .connectSameEntity(false).moduleReferenceName("o-ran-smo-teiv-ran").build(), Relationship.builder().name(
                        "LTESECTORCARRIER_USES_ANTENNACAPABILITY").aSideAssociationName("used-antennaCapability")
                        .aSideMOType("LTESectorCarrier").aSideMinCardinality(0).aSideMaxCardinality(1).bSideAssociationName(
                                "used-by-lteSectorCarrier").bSideMOType("AntennaCapability").bSideMinCardinality(0)
                        .bSideMaxCardinality(1).associationKind("BI_DIRECTIONAL").relationshipDataLocation("A_SIDE")
                        .connectSameEntity(false).moduleReferenceName("o-ran-smo-teiv-ran").build());

        //When
        List<Relationship> actualResult = SchemaParser.extractFromModelBaseline(mockForModelRels);

        //Then
        expectedRelationships.forEach(expectedRel -> {
            actualResult.stream().filter(rel -> rel.getName().equals(expectedRel.getName())).findFirst().ifPresent(
                    extractedRel -> {
                        Assertions.assertEquals(expectedRel.getName(), extractedRel.getName());
                        Assertions.assertEquals(expectedRel.getASideAssociationName(), extractedRel
                                .getASideAssociationName());
                        Assertions.assertEquals(expectedRel.getASideMOType(), extractedRel.getASideMOType());
                        Assertions.assertEquals(expectedRel.getASideMinCardinality(), extractedRel
                                .getASideMinCardinality());
                        Assertions.assertEquals(expectedRel.getASideMaxCardinality(), extractedRel
                                .getASideMaxCardinality());
                        Assertions.assertEquals(expectedRel.getBSideAssociationName(), extractedRel
                                .getBSideAssociationName());
                        Assertions.assertEquals(expectedRel.getBSideMOType(), extractedRel.getBSideMOType());
                        Assertions.assertEquals(expectedRel.getBSideMinCardinality(), extractedRel
                                .getBSideMinCardinality());
                        Assertions.assertEquals(expectedRel.getBSideMaxCardinality(), extractedRel
                                .getBSideMaxCardinality());
                        Assertions.assertEquals(expectedRel.getAssociationKind(), extractedRel.getAssociationKind());
                        Assertions.assertEquals(expectedRel.getRelationshipDataLocation(), extractedRel
                                .getRelationshipDataLocation());
                        Assertions.assertEquals(expectedRel.isConnectSameEntity(), extractedRel.isConnectSameEntity());
                        Assertions.assertEquals(expectedRel.getModuleReferenceName(), extractedRel
                                .getModuleReferenceName());
                    });
        });
    }

    /*
     * These tests check if the data extracted from mocked sql file by the algorithm is correct
     * This includes checking if each Table, their Columns and each Columns' name, datatype or default value are picked up as expected
     */

    @Test
    void checkIfAllTablesAndItsColumnsAreParsedCorrectlyTest() {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),

        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());
        // spotless:on

        // When
        // Test extractDataFromBaseline with a sample file path
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForEntities);

        // Then
        assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    /*
     * This test checks if algorithm can correctly pick new default value 'hello-to-the-world' for column 'name' in table 'Namespace'
     */
    @Test
    void checkIfColumnsWithDefaultValuesAreParsedCorrectlyTest() throws IOException {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").defaultValue("hello-to-the-world").build())).build());
        // spotless:on

        // Create a copy to perform tests on
        File mockSqlFile = new File(mockForEntities);
        File testSqlFile = new File(testSqlFileForProcessorTest);
        Files.copy(mockSqlFile.toPath(), testSqlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Add to copy
        String sampleLine = "ALTER TABLE ONLY ties_data.\"Namespace\" ALTER COLUMN \"name\" SET DEFAULT 'hello-to-the-world';\n\n";

        // When
        TestHelper.appendToFile(testSqlFileForProcessorTest, sampleLine);
        List<Table> entities = SchemaParser.extractDataFromBaseline(testSqlFileForProcessorTest);

        // Then
        for (int i = 0; i < expectedResult.size(); i++) {
            for (int j = 0; j < expectedResult.get(i).getColumns().size(); j++) {
                // Check if each Columns' default value is picked correctly
                Assertions.assertEquals(expectedResult.get(i).getColumns().get(j).getDefaultValue(), entities.get(i)
                        .getColumns().get(j).getDefaultValue());
            }
        }

        Assertions.assertTrue(testSqlFile.delete());

    }

    /*
     * This test checks if algorithm can correctly pick new Column 'azimuth' with datatype 'DECIMAL' for table 'Sector'
     */
    @Test
    void checkIfAlterTableAddColumnAreParsedCorrectlyTest() throws IOException {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("namespaceId").dataType("DECIMAL").build())).build());
        // spotless:on

        // Create a copy to perform tests on
        File mockSqlFile = new File(mockForEntities);
        File testSqlFile = new File(testSqlFileForProcessorTest);
        Files.copy(mockSqlFile.toPath(), testSqlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Add to copy
        String sampleLine = "ALTER TABLE ties_data.\"Namespace\" ADD COLUMN IF NOT EXISTS \"namespaceId\" DECIMAL;";

        // When
        TestHelper.appendToFile(testSqlFileForProcessorTest, sampleLine);
        List<Table> entities = SchemaParser.extractDataFromBaseline(testSqlFileForProcessorTest);

        // Then
        for (int i = 0; i < expectedResult.size(); i++) {
            Assertions.assertEquals(expectedResult.get(i).getColumns().size(), entities.get(i).getColumns().size());
        }

        Assertions.assertTrue(testSqlFile.delete());

    }

    /*
     * This test checks if algorithm can correctly pick foreign key constraint for 1:1 relationships
     * The parser must also be able to pick up constraint name, which table pointsTo and pointsFrom as well as column that have constraints
     */
    @Test
    void checkIfTableConstraintsAreParsedCorrectlyForOneToOneRelationshipTest() {

        // spotless:off
        // Given

        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                                .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("REL_FK_serviced-sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Sector_REL_FK_serviced-sector").tableName("Sector")
                                .referencedTable("Namespace").columnToAddConstraintTo("REL_FK_serviced-sector").build())).build(),
                Column.builder().name("REL_ID_serviced-sector_serving-namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_serviced-sector_serving-namespace")
                                .tableName("Sector").columnToAddConstraintTo("REL_ID_serviced-sector_serving-namespace").build())).build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("REL_FK_serving-namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Namespace_REL_FK_serving-namespace").tableName("Namespace")
                                .referencedTable("Sector").columnToAddConstraintTo("REL_FK_serving-namespace").build())).build())).build());

        // spotless:on

        // When
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForRelOneToOne);

        // Then
        Assertions.assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    /*
     * This test checks if algorithm can correctly pick foreign key constraint for 1:N relationships
     * The parser must also be able to pick up constraint name, which table pointsTo and pointsFrom as well as column that have constraints
     */
    @Test
    void checkIfTableConstraintsAreParsedCorrectlyForOneToManyRelationshipTest() {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build(),
                Column.builder().name("REL_FK_serving-namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Namespace_REL_FK_serving-namespace")
                                .tableName("Namespace").referencedTable("Sector").columnToAddConstraintTo("REL_FK_serving-namespace").build())).build(),
                Column.builder().name("REL_ID_serviced-sector_serving-namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Namespace_REL_ID_serviced-sector_serving-namespace")
                                .tableName("Namespace").columnToAddConstraintTo("REL_ID_serviced-sector_serving-namespace").build())).build())).build());

        // spotless:on

        // When
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForRelOneToMany);

        // Then
        Assertions.assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    /*
     * This test checks if algorithm can correctly pick foreign key constraint for N:1 relationships
     * The parser must also be able to pick up constraint name, which table pointsTo and pointsFrom as well as column that have constraints
     */
    @Test
    void checkIfTableConstraintsForManyToOneAreParsedCorrectlyForManyToOneRelationshipTest() {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                    .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build(),
                Column.builder().name("REL_FK_serviced-sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_Sector_REL_FK_serviced-sector").tableName("Sector").referencedTable("Namespace")
                                .columnToAddConstraintTo("REL_FK_serviced-sector").build())).build(),
                Column.builder().name("REL_ID_serviced-sector_serving-namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_serviced-sector_serving-namespace").tableName("Sector")
                                .columnToAddConstraintTo("REL_ID_serviced-sector_serving-namespace").build())).build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());
        // spotless:on

        // When
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForRelManyToOne);

        // Then
        Assertions.assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    /*
     * This test checks if algorithm can correctly pick foreign key constraint for M:N relationships
     * The parser must also be able to pick up constraint name, which table pointsTo and pointsFrom as well as column that have constraints
     */
    @Test
    void checkIfTableConstraintsAreParsedCorrectlyForManyToManyRelationshipTest() {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Namespace_id")
                        .tableName("Namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build(),
        Table.builder().name("REL_serviced-sector_serving-namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_REL_serviced-sector_serving-namespace_id")
                        .tableName("REL_serviced-sector_serving-namespace").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("aSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_REL_serviced-sector_serving-namespace_aSide_Sector")
                                .tableName("REL_serviced-sector_serving-namespace").referencedTable("Sector").columnToAddConstraintTo("aSide_Sector").build())).build(),
                Column.builder().name("bSide_Namespace").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_REL_serviced-sector_serving-namespace_bSide_Namespace")
                                .tableName("REL_serviced-sector_serving-namespace").referencedTable("Namespace").columnToAddConstraintTo("bSide_Namespace").build())).build())).build());
        // spotless:on

        // When
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForRelManyToMany);

        // Then
        Assertions.assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    /*
     * This test checks if algorithm can correctly pick foreign key constraint for relationships connecting same entity
     * The parser must also be able to pick up constraint name, which table pointsTo and pointsFrom as well as column that have constraints
     */
    @Test
    void checkIfTableConstraintsAreParsedCorrectlyForRelationshipWithSameEntityTest() {

        // spotless:off
        // Given
        List<Table> expectedResult = List.of(Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_Sector_id")
                        .tableName("Sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("REL_serviced-sector_serving-sector").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(PrimaryKeyConstraint.builder().constraintName("PK_REL_serviced-sector_serving-sector_id")
                        .tableName("REL_serviced-sector_serving-sector").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("aSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_REL_serviced-sector_serving-sector_aSide_Sector")
                                .tableName("REL_serviced-sector_serving-sector").referencedTable("Sector").columnToAddConstraintTo("aSide_Sector").build())).build(),
                Column.builder().name("bSide_Sector").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(ForeignKeyConstraint.builder().constraintName("FK_REL_serviced-sector_serving-sector_bSide_Sector")
                                .tableName("REL_serviced-sector_serving-sector").referencedTable("Sector").columnToAddConstraintTo("bSide_Sector").build())).build())).build());
        // spotless:on

        // When
        List<Table> actualResult = SchemaParser.extractDataFromBaseline(mockForRelSameEntities);

        // Then
        Assertions.assertNotNull(actualResult);
        runTest(actualResult, expectedResult);

    }

    void runTest(List<Table> actualResult, List<Table> expectedResult) {

        List<String> allEntitiesFromActualResult = TestHelper.extractTableNames(actualResult);
        List<String> allEntitiesFromExpectedResult = TestHelper.extractTableNames(expectedResult);

        // Check if all tables are picked correctly by the algorithm
        Assertions.assertEquals(allEntitiesFromExpectedResult, allEntitiesFromActualResult);

        //spotless:off
        expectedResult.forEach(expectedTable -> {
            actualResult.stream().filter(generatedTable -> generatedTable.getName().equals(expectedTable.getName())).findFirst()
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
                                    Assertions.assertEquals(constraint.getTableToAddConstraintTo(), constraint1.getTableToAddConstraintTo());

                                    // Check column where constraint is to be added
                                    Assertions.assertEquals(constraint.getColumnToAddConstraintTo(), constraint1.getColumnToAddConstraintTo());

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
}
