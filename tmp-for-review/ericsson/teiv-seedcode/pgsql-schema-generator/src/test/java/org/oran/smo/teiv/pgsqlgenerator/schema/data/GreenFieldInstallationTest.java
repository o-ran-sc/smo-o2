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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.oran.smo.teiv.pgsqlgenerator.TestHelper;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;
import org.oran.smo.teiv.pgsqlgenerator.schema.BackwardCompatibilityChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import org.oran.smo.teiv.pgsqlgenerator.schema.model.HashInfoDataGenerator;

@SpringBootTest(classes = { DataSchemaGenerator.class, ModelComparator.class, DataSchemaHelper.class, TableBuilder.class,
        HashInfoDataGenerator.class, BackwardCompatibilityChecker.class }, properties = { "green-field-installation=true" })
public class GreenFieldInstallationTest {

    static File outputSqlFile;
    @Autowired
    private DataSchemaGenerator dataSchemaGenerator;
    @Autowired
    private BackwardCompatibilityChecker nbcChecker;
    @Value("${green-field-installation}")
    private boolean isGreenFieldInstallation;
    @Value("${schema.data.skeleton}")
    private String skeletonDataSqlFile;
    @Value("${schema.data.output}")
    private String actualResultSqlFile;
    @Value("${schema.model.temp-baseline}")
    private String tempBaselineModelSqlFile;

    @BeforeEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(tempBaselineModelSqlFile));
    }

    @Test
    void prepareSchemaTest() throws IOException {
        //when
        dataSchemaGenerator.prepareSchema();
        File skeletonFile = new ClassPathResource(skeletonDataSqlFile).getFile();
        outputSqlFile = new File(actualResultSqlFile);

        //then
        Assertions.assertFalse(new File(tempBaselineModelSqlFile).exists());
        Assertions.assertTrue(outputSqlFile.exists());
        Assertions.assertTrue(isGreenFieldInstallation);
        Assertions.assertTrue(TestHelper.filesCompareByLine(skeletonFile.toPath(), outputSqlFile.toPath()));
        Assertions.assertTrue(outputSqlFile.delete());
    }

    @Test
    void verifyNoExceptionsThrownForNBCWhenGreenfieldEnabled() {
        //Given
        List<Table> mockModelServiceEntities = List.of(Table.builder().name("Sector").columns(List.of(Column.builder().name(
                "azimuth").dataType("DECIMAL").build(), Column.builder().name("id").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(PrimaryKeyConstraint.builder().tableName("Sector").constraintName(
                                "PK_Source_id").columnToAddConstraintTo("id").build())).build(), Column.builder().name(
                                        "sectorId").dataType("jsonb").defaultValue("101").build(), Column.builder().name(
                                                "geo-location").dataType("geography").build())).build());

        // When
        List<Table> mockBaselineEntitiesTableDeleted = List.of();
        //Then
        // Renaming Sector table won't throw exception when green field is enabled
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInData(mockBaselineEntitiesTableDeleted,
                mockModelServiceEntities));

        // When
        List<Table> mockBaselineEntitiesColumnRenamed = List.of(Table.builder().name("Sector").columns(List.of(Column
                .builder().name("azimuth").dataType("DECIMAL").build(), Column.builder().name("id").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(PrimaryKeyConstraint.builder().tableName("Sector").constraintName(
                                "PK_Source_id").columnToAddConstraintTo("id").build())).build(), Column.builder().name(
                                        "sectorId123").dataType("jsonb").defaultValue("101").build(), Column.builder().name(
                                                "geo-location").dataType("geography").build())).build());
        //Then
        // Renaming Sector.sectorId won't throw exception when green field is enabled
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInData(mockBaselineEntitiesColumnRenamed,
                mockModelServiceEntities));

        // When
        List<Table> mockBaselineEntitiesColumnDeleted = List.of(Table.builder().name("Sector").columns(List.of(Column
                .builder().name("azimuth").dataType("DECIMAL").build(), Column.builder().name("id").dataType("VARCHAR(511)")
                        .postgresConstraints(List.of(PrimaryKeyConstraint.builder().tableName("Sector").constraintName(
                                "PK_Source_id").columnToAddConstraintTo("id").build())).build(), Column.builder().name(
                                        "geo-location").dataType("geography").build())).build());
        //Then
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInData(mockBaselineEntitiesColumnDeleted,
                mockModelServiceEntities));

        //When
        //ONE_TO_ONE
        List<Relationship> baselineRel = List.of(Relationship.builder().name("ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER")
                .aSideAssociationName("provided-lteSectorCarrier").aSideMOType("ENodeBFunction").aSideMinCardinality(0)
                .aSideMaxCardinality(1).bSideAssociationName("provided-by-enodebFunction").bSideMOType("LTESectorCarrier")
                .bSideMinCardinality(0).bSideMaxCardinality(1).associationKind("BI_DIRECTIONAL").relationshipDataLocation(
                        "B_SIDE").connectSameEntity(false).moduleReferenceName("o-ran-smo-teiv-ran").build());

        //Deleted relationship ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER
        //Then
        List<Relationship> mockModelSvcRel1 = List.of(Relationship.builder().name(
                "LTESECTORCARRIER_PROVIDES_ENODEBFUNCTION").aSideAssociationName("provided-eNodeBFunction").aSideMOType(
                        "LTESectorCarrier").aSideMinCardinality(0).aSideMaxCardinality(100).bSideAssociationName(
                                "provided-by-lteSectorCarrier").bSideMOType("ENodeBFunction").bSideMinCardinality(0)
                .bSideMaxCardinality(100).associationKind("BI_DIRECTIONAL").relationshipDataLocation("RELATION")
                .connectSameEntity(false).moduleReferenceName("o-ran-smo-teiv-ran").build());
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInModel(baselineRel, mockModelSvcRel1));

        //from ONE_TO_ONE to MANY_TO_MANY
        List<Relationship> mockModelSvcRel2 = List.of(Relationship.builder().name(
                "ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER").aSideAssociationName("provided-lteSectorCarrier").aSideMOType(
                        "ENodeBFunction").aSideMinCardinality(1).aSideMaxCardinality(100).bSideAssociationName(
                                "provided-by-enodebFunction").bSideMOType("LTESectorCarrier").bSideMinCardinality(0)
                .bSideMaxCardinality(100).associationKind("BI_DIRECTIONAL").relationshipDataLocation("A_SIDE")
                .connectSameEntity(false).moduleReferenceName("o-ran-smo-teiv-ran").build());
        Assertions.assertDoesNotThrow(() -> nbcChecker.checkForNBCChangesInModel(baselineRel, mockModelSvcRel2));

    }

}
