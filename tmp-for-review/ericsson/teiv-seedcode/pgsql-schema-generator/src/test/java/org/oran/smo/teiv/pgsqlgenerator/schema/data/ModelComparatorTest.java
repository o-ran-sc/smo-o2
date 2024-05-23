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

import java.util.List;
import java.util.Map;

import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.TestHelper;

@SpringBootTest(classes = { ModelComparator.class, DataSchemaHelper.class })
class ModelComparatorTest {

    @Autowired
    private DataSchemaHelper dataSchemaHelper;
    @Autowired
    private ModelComparator modelComparator;
    //spotless:off
    private final List<Table> mockModelServiceEntities = List.of(
        Table.builder().name("Sector").columns(
            List.of(
                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                        PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build(),
        Table.builder().name("Namespace").columns(
            List.of(
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                        PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());
    //spotless:on

    /*
     * Find differences between extracted data from module service and baseline
     * Then check if the schema generated for both are the same
     */

    // Test will all present except table "Namespace". The algorithm should correctly identify difference as well as generate correct schema for the same.
    @Test
    void identifyDifferencesInBaselineAndGeneratedWithTableMissingTest() {

        //spotless:off

        // Given baseline mock data
        List<Table> baselineEntitiesTableMissing = List.of(Table.builder().name("Sector").columns(
            List.of(Column.builder().name("azimuth").dataType("DECIMAL").build(),
                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                        PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build());

        // Correct result of difference below
        Map<String, List<Table>> correctMappedDifferences = TestHelper.identifiedModelChangeMapping();
        correctMappedDifferences.get("CREATE").add(Table.builder().name("Namespace").columns(
            List.of(Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                            PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());

        //spotless:on

        // When
        Map<String, List<Table>> mappedDifferences = modelComparator.identifyDifferencesInBaselineAndGenerated(
                mockModelServiceEntities, baselineEntitiesTableMissing);

        // Then - check if the schema for diff identified above match with the correct result
        Assertions.assertEquals(dataSchemaHelper.generateSchemaFromDifferences(correctMappedDifferences).toString(),
                dataSchemaHelper.generateSchemaFromDifferences(mappedDifferences).toString());

    }

    // Test will all present except table "Namespace" & column "Sector.azimuth"
    @Test
    void identifyDifferencesInBaselineAndGeneratedWithTableAndAttributesMissingTest() {

        //spotless:off

        // Given baseline mock data
        List<Table> baselineEntitiesWIthColumnAndTableMissing = List.of(Table.builder().name("Sector").columns(
            List.of(Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                            PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build());

        // Correct result of difference below
        Map<String, List<Table>> correctMappedDifferences = TestHelper.identifiedModelChangeMapping();
        correctMappedDifferences.get("CREATE").add(Table.builder().name("Namespace").columns(
            List.of(Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                            PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());
        correctMappedDifferences.get("ALTER")
            .add(Table.builder().name("Sector").columns(List.of(Column.builder().name("azimuth").dataType("DECIMAL")
                    .build())).build());

        //spotless:on

        // When
        Map<String, List<Table>> mappedDifferences = modelComparator.identifyDifferencesInBaselineAndGenerated(
                mockModelServiceEntities, baselineEntitiesWIthColumnAndTableMissing);

        // Then - check if the schema for diff identified above match with the correct result
        Assertions.assertEquals(dataSchemaHelper.generateSchemaFromDifferences(correctMappedDifferences).toString(),
                dataSchemaHelper.generateSchemaFromDifferences(mappedDifferences).toString());
    }

    // Test will all present except table "Namespace", column "Sector.azimuth" & No default value set to "Sector.sectorId"
    @Test
    void identifyDifferencesInBaselineAndGeneratedWithTableAttributeAndDefaultMissingTest() {

        //spotless:off

        // Given baseline mock data
        List<Table> baselineEntitiesWIthColumnAndTableAndDefaultValueMissing = List.of(Table.builder().name("Sector").columns(
            List.of(Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                            PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("sectorId").dataType("jsonb").build(),
                Column.builder().name("geo-location").dataType("geography").build())).build());

        // Correct result of difference below
        Map<String, List<Table>> correctMappedDifferences = TestHelper.identifiedModelChangeMapping();
        correctMappedDifferences.get("CREATE").add(Table.builder().name("Namespace").columns(
            List.of(Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                            PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id").columnToAddConstraintTo("id").build())).build(),
                Column.builder().name("name").dataType("TEXT").build())).build());
        correctMappedDifferences.get("ALTER")
            .add(Table.builder().name("Sector").columns(List.of(Column.builder().name("azimuth").dataType("DECIMAL")
                    .build())).build());
        correctMappedDifferences.get("DEFAULT").add(Table.builder().name("Sector").columns(
            List.of(Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build())).build());
        //spotless:on

        // When
        Map<String, List<Table>> mappedDifferences = modelComparator.identifyDifferencesInBaselineAndGenerated(
                mockModelServiceEntities, baselineEntitiesWIthColumnAndTableAndDefaultValueMissing);

        // Then - check if the schema for diff identified above match with the correct result
        Assertions.assertEquals(dataSchemaHelper.generateSchemaFromDifferences(correctMappedDifferences).toString(),
                dataSchemaHelper.generateSchemaFromDifferences(mappedDifferences).toString());
    }

}
