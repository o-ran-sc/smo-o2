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
package org.oran.smo.teiv.pgsqlgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.oran.smo.teiv.pgsqlgenerator.schema.SchemaParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndTest {

    @Autowired
    private Processor processor;
    @Value("${test-result.data}")
    private String expectedDataSql;
    @Value("${schema.data.output}")
    private String actualDataSql;
    @Value("${test-result.model}")
    private String expectedModelSql;
    @Value("${schema.model.output}")
    private String actualModelSql;

    @Test
    @Order(1)
    void generateDataAndModelSchemaTest() throws IOException {
        //when
        //Generate new sql file from mocked models
        processor.process();
        File generatedDataSql = new File(actualDataSql);
        File generatedModelSql = new File(actualModelSql);

        //then
        Assertions.assertTrue(generatedDataSql.exists());
        Assertions.assertTrue(generatedModelSql.exists());
    }

    @Test
    @Order(2)
    void verifyGeneratedDataSqlFileTest() {
        //then
        List<Table> expectedTables = SchemaParser.extractDataFromBaseline(expectedDataSql);
        List<Table> generatedTables = SchemaParser.extractDataFromBaseline(actualDataSql);

        // Check if the generated sql is as expected
        // Check if all table/entities were added correctly
        Assertions.assertEquals(expectedTables.size(), generatedTables.size());

        List<String> allTableNamesFromGeneratedResult = TestHelper.extractTableNames(generatedTables);
        List<String> allTableNamesFromExpectedResult = TestHelper.extractTableNames(expectedTables);

        // Check if generatedResult contains all tables
        Assertions.assertEquals(allTableNamesFromExpectedResult, allTableNamesFromGeneratedResult);

        //spotless:off
        expectedTables.forEach(expectedTable -> {
            generatedTables.stream().filter(generatedTable -> generatedTable.getName().equals(expectedTable.getName()))
                .findFirst().ifPresent(generatedTable -> {
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
                                columnInGenerated.getPostgresConstraints().stream().filter(constraint1 ->
                                    constraint1.getConstraintName().equals(constraint.getConstraintName())).findFirst().ifPresent(constraint1 -> {

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

    @Test
    @Order(3)
    void verifyGeneratedModelSqlFileTest() {
        //when
        Set<String> expectedSqlStatements = TestHelper.readFile(expectedModelSql);
        Set<String> actualSqlStatements = TestHelper.readFile(actualModelSql);
        //then
        Assertions.assertEquals(expectedSqlStatements.size(), actualSqlStatements.size());
        Assertions.assertEquals(expectedSqlStatements, actualSqlStatements);
    }

    @Test
    @Order(5)
    void verifyGeneratedAlterStatementsSchemaTest() {
        //when
        Set<String> expectedSqlStatements = TestHelper.readFile(expectedDataSql);
        Set<String> actualSqlStatements = TestHelper.readFile(actualDataSql);

        HashMap<String, Long> expectedAlterStatements = new HashMap<>();
        expectedAlterStatements.put("ALTER ADD DEFAULT", expectedSqlStatements.stream().filter(s -> s.contains(
                "ALTER TABLE") && s.contains("SET DEFAULT")).count());
        expectedAlterStatements.put("ALTER ADD CONSTRAINT", expectedSqlStatements.stream().filter(s -> s.contains(
                "ALTER TABLE") && s.contains("ADD CONSTRAINT")).count());

        HashMap<String, Long> actualAlterStatements = new HashMap<>();
        actualAlterStatements.put("ALTER ADD COLUMN", actualSqlStatements.stream().filter(s -> s.contains(
                "ALTER TABLE") && s.contains("ADD COLUMN")).count());
        actualAlterStatements.put("ALTER ADD DEFAULT", actualSqlStatements.stream().filter(s -> s.contains(
                "ALTER TABLE") && s.contains("SET DEFAULT")).count());
        actualAlterStatements.put("ALTER ADD CONSTRAINT", actualSqlStatements.stream().filter(s -> s.contains(
                "ALTER TABLE") && s.contains("ADD CONSTRAINT")).count());

        //then
        Assertions.assertEquals(0, actualAlterStatements.get("ALTER ADD COLUMN"));
        Assertions.assertEquals(expectedAlterStatements.get("ALTER ADD DEFAULT"), actualAlterStatements.get(
                "ALTER ADD DEFAULT"));
        Assertions.assertEquals(expectedAlterStatements.get("ALTER ADD CONSTRAINT"), actualAlterStatements.get(
                "ALTER ADD CONSTRAINT"));

    }

    @Test
    void storeRelatedModuleRefsFromIncludedModulesTest() {
        //spotless:off
        List<Module> mockModuleRefFromYangParser = List.of(
            Module.builder().name("o-ran-smo-teiv-ran-equipment")
                    .namespace("urn:rdns:o-ran:smo:teiv:o-ran-smo-teiv-ran-equipment")
                    .domain("RAN_EQUIPMENT")
                    .includedModules(new ArrayList<>(List.of( "o-ran-smo-teiv-common-yang-types",
                            "o-ran-smo-teiv-common-yang-extensions", "ietf-geo-location"))).build(),
            Module.builder().name("o-ran-smo-teiv-ran-equipment-to-logical")
                    .namespace("urn:rdns:o-ran:smo:teiv:ericsson-topologyandinventory-ran-logical-to-equipment")
                    .domain("EQUIPMENT_TO_RAN_LOGICAL")
                    .includedModules(new ArrayList<>(List.of( "o-ran-smo-teiv-common-yang-types",
                    "o-ran-smo-teiv-common-yang-extensions", "o-ran-smo-teiv-ran-logical",
                    "o-ran-smo-teiv-ran-equipment"))).build(),
            Module.builder().name("o-ran-smo-teiv-ran-oam-to-cloud")
                    .namespace("urn:rdns:o-ran:smo:teiv:o-ran-smo-teiv-ran-oam-to-cloud")
                    .domain("RAN_OAM_TO_CLOUD")
                    .includedModules(new ArrayList<>(List.of( "o-ran-smo-teiv-common-yang-types",
                    "o-ran-smo-teiv-common-yang-extensions", "o-ran-smo-teiv-ran-oam",
                    "o-ran-smo-teiv-ran-cloud"))).build()
        );

        List<Entity> mockEntitiesFromModelSvc = List.of(
                Entity.builder().entityName("AntennaModule").moduleReferenceName("o-ran-smo-teiv-ran-equipment")
                        .attributes(List.of()).build(),
                Entity.builder().entityName("AntennaCapability").moduleReferenceName("o-ran-smo-teiv-ran-logical")
                        .attributes(List.of()).build(),
                Entity.builder().entityName("CloudNativeSystem").moduleReferenceName("o-ran-smo-teiv-ran-cloud")
                        .attributes(List.of()).build(),
                Entity.builder().entityName("ManagedElement").moduleReferenceName("o-ran-smo-teiv-ran-oam")
                        .attributes(List.of()).build(),
                Entity.builder().entityName("Sector").moduleReferenceName("o-ran-smo-teiv-ran-equipment-to-logical")
                        .attributes(List.of()).build(),
                Entity.builder().entityName("NRCellDU").moduleReferenceName("o-ran-smo-teiv-ran-logical")
                        .attributes(List.of()).build()
        );

        List<Module> expectedResult = List.of(
            Module.builder().name("o-ran-smo-teiv-ran-equipment")
                    .namespace("urn:rdns:o-ran:smo:teiv:o-ran-smo-teiv-ran-equipment")
                    .domain("RAN_EQUIPMENT").build(),
            Module.builder().name("o-ran-smo-teiv-ran-equipment-to-logical")
                    .namespace("urn:rdns:o-ran:smo:teiv:ericsson-topologyandinventory-ran-logical-to-equipment")
                    .domain("EQUIPMENT_TO_RAN_LOGICAL")
                    .includedModules(new ArrayList<>(List.of("o-ran-smo-teiv-ran-logical",
                            "o-ran-smo-teiv-ran-equipment"))).build(),
            Module.builder().name("o-ran-smo-teiv-ran-oam-to-cloud")
                    .namespace("urn:rdns:o-ran:smo:teiv:o-ran-smo-teiv-ran-oam-to-cloud")
                    .domain("RAN_OAM_TO_CLOUD")
                    .includedModules(new ArrayList<>(List.of("o-ran-smo-teiv-ran-oam",
                            "o-ran-smo-teiv-ran-cloud"))).build()
        );
        //spotless:on

        List<Module> actualResult = Processor.storeRelatedModuleRefsFromIncludedModules(mockEntitiesFromModelSvc,
                mockModuleRefFromYangParser);

        actualResult.forEach(actual -> {
            expectedResult.stream().filter(expected -> expected.getName().equals(actual.getName())).findFirst().ifPresent(
                    expected -> {
                        Assertions.assertEquals(actual.getIncludedModules(), expected.getIncludedModules());
                    });
        });
    }
}
