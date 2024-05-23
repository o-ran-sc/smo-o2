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

import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PgSchemaGeneratorException;
import org.oran.smo.teiv.pgsqlgenerator.UniqueConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = { BackwardCompatibilityChecker.class }, properties = { "green-field-installation=false" })
class BackwardCompatibilityCheckerTest {

    @Autowired
    private BackwardCompatibilityChecker nbcChecker;

    //spotless:off

    private List<Table> mockModelServiceEntities = List.of(
            Table.builder().name("Sector").columns(
                List.of(
                        Column.builder().name("azimuth").dataType("DECIMAL").build(),
                        Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                        .columnToAddConstraintTo("id").build())).build(),
                        Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                        Column.builder().name("geo-location").dataType("geography").build())).build(),
            Table.builder().name("Namespace").columns(
                List.of(
                        Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                        .columnToAddConstraintTo("id").build())).build(),
                        Column.builder().name("name").dataType("TEXT").build())).build());

    //Data:Entities
    @Test
    void verifyExceptionThrownOnDeletedTableWhenGreenfieldDisabledTest()  {

        // When - baseline
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed table(NRCellDU) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());

    }

    @Test
    void verifyExceptionThrownOnDeletedColumnWhenGreenfieldDisabledTest()  {

        // Given baseline
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // When - entities from Model svc with Sector.sectorId deleted
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed column(Sector.sectorId) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());

    }

    @Test
    void verifyExceptionThrownOnColumnNameModifiedWhenGreenfieldDisabledTest()  {
        // rename column name
        // Given baseline
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // When - entities from Model svc with renamed column name
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId123").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed column(Sector.sectorId) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());
    }

    @Test
    void verifyExceptionThrownOnColumnDataModifiedWhenGreenfieldDisabledTest()  {
        // Given baseline
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // When - entities from Model svc with modified column datatype
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("SMALLINT").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed datatype for column(Namespace.id) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());
    }

    @Test
    void verifyExceptionThrownOnColumnConstraintModifiedWhenGreenfieldDisabledTest()  {

        // Given baseline
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // When - entities from Model svc with modified column constraints
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed constraint for column(Sector.id) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());

        // When - entities from Model svc with Sector.id's pk constraint name changed
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                        List.of(
                                Column.builder().name("azimuth").dataType("DECIMAL").build(),
                                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                        PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id_123")
                                                .columnToAddConstraintTo("id").build())).build(),
                                Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                                Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                        List.of(
                                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                        PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                                .columnToAddConstraintTo("id").build())).build(),
                                Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("NRCellDU").columns(
                        List.of(
                                Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                        PrimaryKeyConstraint.builder().tableName("NRCellDU").constraintName("PK_NRCellDU_id")
                                                .columnToAddConstraintTo("id").build())).build(),
                                Column.builder().name("name").dataType("TEXT").build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed constraint for column(Sector.id) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());

    }

    //Data:Relationships
    @Test
    void verifyExceptionThrownOnModifiedRelationshipCardinalityWhenGreenfieldDisabledTest() {
        // Given
        // ONE_TO_ONE
        List<Table> mockBaselineEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build(),
                            Column.builder().name("REL_FK_managed-Namespace").dataType("VARCHAR(511)").postgresConstraints(List.of()).build(),
                            Column.builder().name("REL_ID_SECTOR_MANAGES_NAMESPACE").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_SECTOR_MANAGES_NAMESPACE").tableName("Sector")
                                            .columnToAddConstraintTo("REL_ID_SECTOR_MANAGES_NAMESPACE").build())).build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // When
        // Changing SECTOR_MANAGES_NAMESPACE from ONE_TO_ONE to ONE_TO_MANY
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build(),
                            Column.builder().name("REL_FK_managed-by-Sector").dataType("VARCHAR(511)").postgresConstraints(List.of()).build(),
                            Column.builder().name("REL_ID_SECTOR_MANAGES_NAMESPACE").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("UNIQUE_Namespace_REL_ID_SECTOR_MANAGES_NAMESPACE").tableName("Namespace")
                                            .columnToAddConstraintTo("REL_ID_SECTOR_MANAGES_NAMESPACE").build())).build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed column(Sector.REL_FK_managed-Namespace) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());

        // When
        // Changing SECTOR_MANAGES_NAMESPACE rel from ONE_TO_ONE to MANY_TO_MANY
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                    List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                    List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build(),
                Table.builder().name("SECTOR_MANAGES_NAMESPACE").columns(
                        List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("SECTOR_MANAGES_NAMESPACE").constraintName("PK_SECTOR_MANAGES_NAMESPACE_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("aSide_Sector").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("FK_SECTOR_MANAGES_NAMESPACE_aSide_Sector")
                                            .tableName("SECTOR_MANAGES_NAMESPACE").columnToAddConstraintTo("aSide_Sector").build())).build(),
                            Column.builder().name("bSide_Namespace").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("FK_SECTOR_MANAGES_NAMESPACE_bSide_Namespace")
                                            .tableName("SECTOR_MANAGES_NAMESPACE").columnToAddConstraintTo("bSide_Namespace").build())).build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed column(Sector.REL_FK_managed-Namespace) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaselineEntities, mockModelServiceEntities)).getMessage());


        // Given
        // Changing SECTOR_MANAGES_NAMESPACE rel from ONE_TO_MANY to ONE_TO_ONE - Changing cardinality
        List<Table> mockBaseline = List.of(
                Table.builder().name("Sector").columns(
                        List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build())).build(),
                Table.builder().name("Namespace").columns(
                        List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build(),
                            Column.builder().name("REL_FK_managed-by-Sector").dataType("VARCHAR(511)").postgresConstraints(List.of()).build(),
                            Column.builder().name("REL_ID_SECTOR_MANAGES_NAMESPACE").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("UNIQUE_Namespace_REL_ID_SECTOR_MANAGES_NAMESPACE").tableName("Namespace")
                                            .columnToAddConstraintTo("REL_ID_SECTOR_MANAGES_NAMESPACE").build())).build())).build());

        // When
        mockModelServiceEntities = List.of(
                Table.builder().name("Sector").columns(
                        List.of(
                            Column.builder().name("azimuth").dataType("DECIMAL").build(),
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Sector").constraintName("PK_Source_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("sectorId").dataType("jsonb").defaultValue("101").build(),
                            Column.builder().name("geo-location").dataType("geography").build(),
                            Column.builder().name("REL_FK_managed-Namespace").dataType("VARCHAR(511)").postgresConstraints(List.of()).build(),
                            Column.builder().name("REL_ID_SECTOR_MANAGES_NAMESPACE").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    UniqueConstraint.builder().constraintName("UNIQUE_Sector_REL_ID_SECTOR_MANAGES_NAMESPACE").tableName("Sector")
                                            .columnToAddConstraintTo("REL_ID_SECTOR_MANAGES_NAMESPACE").build())).build())).build(),
                Table.builder().name("Namespace").columns(
                        List.of(
                            Column.builder().name("id").dataType("VARCHAR(511)").postgresConstraints(List.of(
                                    PrimaryKeyConstraint.builder().tableName("Namespace").constraintName("PK_Namespace_id")
                                            .columnToAddConstraintTo("id").build())).build(),
                            Column.builder().name("name").dataType("TEXT").build())).build());

        // Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed column(Namespace.REL_FK_managed-by-Sector) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInData(
                        mockBaseline, mockModelServiceEntities)).getMessage());
    }

    //Model
    @Test
    void verifyExceptionThrownOnChangingRelAttributesWhenGreenfieldDisabledTest() {

        //from ONE_TO_ONE to MANY_TO_ONE
        //Given
        List<Relationship> baselineRel = List.of(
                Relationship.builder().name("ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER")
                        .aSideAssociationName("provided-lteSectorCarrier")
                        .aSideMOType("ENodeBFunction")
                        .aSideMinCardinality(0)
                        .aSideMaxCardinality(1)
                        .bSideAssociationName("provided-by-enodebFunction")
                        .bSideMOType("LTESectorCarrier")
                        .bSideMinCardinality(0)
                        .bSideMaxCardinality(1)
                        .associationKind("BI_DIRECTIONAL")
                        .relationshipDataLocation("B_SIDE")
                        .connectSameEntity(false)
                        .moduleReferenceName("o-ran-smo-teiv-ran").build());
        //When
        List<Relationship> mockModelSvcRel1 = List.of(
                Relationship.builder().name("ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER")
                        .aSideAssociationName("provided-lteSectorCarrier")
                        .aSideMOType("ENodeBFunction")
                        .aSideMinCardinality(0)
                        .aSideMaxCardinality(100)
                        .bSideAssociationName("provided-by-enodebFunction")
                        .bSideMOType("LTESectorCarrier")
                        .bSideMinCardinality(0)
                        .bSideMaxCardinality(1)
                        .associationKind("BI_DIRECTIONAL")
                        .relationshipDataLocation("A_SIDE")
                        .connectSameEntity(false)
                        .moduleReferenceName("o-ran-smo-teiv-ran").build());
        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified cardinalities for relationship(ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER), please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInModel(
                        baselineRel, mockModelSvcRel1)).getMessage());

        //from ONE_TO_ONE to MANY_TO_MANY
        //When
        List<Relationship> mockModelSvcRel2 = List.of(
                Relationship.builder().name("ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER")
                        .aSideAssociationName("provided-lteSectorCarrier")
                        .aSideMOType("ENodeBFunction")
                        .aSideMinCardinality(1)
                        .aSideMaxCardinality(100)
                        .bSideAssociationName("provided-by-enodebFunction")
                        .bSideMOType("LTESectorCarrier")
                        .bSideMinCardinality(0)
                        .bSideMaxCardinality(100)
                        .associationKind("BI_DIRECTIONAL")
                        .relationshipDataLocation("A_SIDE")
                        .connectSameEntity(false)
                        .moduleReferenceName("o-ran-smo-teiv-ran").build());
        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified cardinalities for relationship(ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER), please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInModel(
                        baselineRel, mockModelSvcRel2)).getMessage());

        //Change aSide and bSide
        //When
        List<Relationship> mockModelSvcRel3 = List.of(
                Relationship.builder().name("LTESECTORCARRIER_PROVIDES_ENODEBFUNCTION")
                        .aSideAssociationName("provided-eNodeBFunction")
                        .aSideMOType("LTESectorCarrier")
                        .aSideMinCardinality(0)
                        .aSideMaxCardinality(100)
                        .bSideAssociationName("provided-by-lteSectorCarrier")
                        .bSideMOType("ENodeBFunction")
                        .bSideMinCardinality(0)
                        .bSideMaxCardinality(100)
                        .associationKind("BI_DIRECTIONAL")
                        .relationshipDataLocation("RELATION")
                        .connectSameEntity(false)
                        .moduleReferenceName("o-ran-smo-teiv-ran").build());
        //Then
        Assertions.assertEquals(
                String.format("NBC change has been introduced: modified/removed relationship(ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER) present in baseline, please make sure you've enabled green-field installation!!%nFor more info please refer to README"),
                Assertions.assertThrowsExactly(PgSchemaGeneratorException.class, () -> nbcChecker.checkForNBCChangesInModel(
                        baselineRel, mockModelSvcRel3)).getMessage());

        //When
        List<Relationship> mockModelSvcRel4 = List.of(
                Relationship.builder().name("ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER")
                        .aSideAssociationName("provided-lteSectorCarrier")
                        .aSideMOType("ENodeBFunction")
                        .aSideMinCardinality(0)
                        .aSideMaxCardinality(1)
                        .bSideAssociationName("provided-by-enodebFunction")
                        .bSideMOType("LTESectorCarrier")
                        .bSideMinCardinality(0)
                        .bSideMaxCardinality(1)
                        .associationKind("BI_DIRECTIONAL")
                        .relationshipDataLocation("A_SIDE")
                        .connectSameEntity(false)
                        .moduleReferenceName("o-ran-smo-teiv-ran").build());
        //Then
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInModel(
                        baselineRel, mockModelSvcRel4));
    }
    //spotless:on
}
