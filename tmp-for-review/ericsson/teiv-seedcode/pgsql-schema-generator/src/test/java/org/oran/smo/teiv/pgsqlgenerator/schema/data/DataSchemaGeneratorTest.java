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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.oran.smo.teiv.pgsqlgenerator.schema.BackwardCompatibilityChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.oran.smo.teiv.pgsqlgenerator.TestHelper;
import org.oran.smo.teiv.pgsqlgenerator.schema.model.HashInfoDataGenerator;

@SpringBootTest(classes = { DataSchemaGenerator.class, ModelComparator.class, DataSchemaHelper.class, TableBuilder.class,
        HashInfoDataGenerator.class, BackwardCompatibilityChecker.class }, properties = {
                "green-field-installation=false" })
public class DataSchemaGeneratorTest {
    @Autowired
    private DataSchemaGenerator dataSchemaGenerator;
    @Value("${green-field-installation}")
    private boolean isGreenFieldInstallation;
    @Value("${schema.data.baseline}")
    private String baselineDataSqlFile;
    @Value("${schema.data.output}")
    private String actualResultSqlFile;

    @Test
    void prepareSchemaTest() throws IOException {
        //when
        dataSchemaGenerator.prepareSchema();
        //then
        Assertions.assertFalse(isGreenFieldInstallation);
        //exclude commit statement in the prepare statement as it will be added in the later stage at the end of the schema.
        Assertions.assertTrue(TestHelper.filesCompareByLine(Paths.get(baselineDataSqlFile), Paths.get(actualResultSqlFile),
                "COMMIT;", ""));
    }

    @AfterEach
    public void tearDown() {
        assertTrue(Paths.get(actualResultSqlFile).toFile().delete());
    }
}
