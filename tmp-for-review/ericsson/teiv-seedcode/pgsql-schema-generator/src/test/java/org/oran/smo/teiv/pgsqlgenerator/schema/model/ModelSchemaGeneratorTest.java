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
package org.oran.smo.teiv.pgsqlgenerator.schema.model;

import java.io.File;
import java.io.IOException;

import org.oran.smo.teiv.pgsqlgenerator.schema.BackwardCompatibilityChecker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import org.oran.smo.teiv.pgsqlgenerator.TestHelper;
import org.oran.smo.teiv.pgsqlgenerator.YangParser;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = { HashInfoDataGenerator.class, ModelSchemaGenerator.class, YangParser.class,
        BackwardCompatibilityChecker.class }, properties = { "green-field-installation=false" })
public class ModelSchemaGeneratorTest {

    @Autowired
    private ModelSchemaGenerator modelSchemaGenerator;

    static File generatedResultFile;
    static File tempBaselineFile;
    @Value("${test-result.model}")
    private String expectedResultSqlFile;
    @Value("${schema.model.output}")
    private String actualResultSqlFile;
    @Value("${schema.model.skeleton}")
    private String skeletonModelSqlFile;
    @Value("${schema.model.baseline}")
    private String baselineModelSqlFile;
    @Value("${schema.model.temp-baseline}")
    private String tempBaselineModelSqlFile;

    @Test
    void prepareSchemaTest() throws IOException {
        //when
        modelSchemaGenerator.prepareSchema();
        File skeletionFile = new ClassPathResource(skeletonModelSqlFile).getFile();
        generatedResultFile = new File(actualResultSqlFile);
        tempBaselineFile = new File(tempBaselineModelSqlFile);

        //then
        assertTrue(generatedResultFile.exists());
        assertTrue(tempBaselineFile.exists());
        assertTrue(TestHelper.filesCompareByLine(skeletionFile.toPath(), generatedResultFile.toPath()));
        assertTrue(TestHelper.filesCompareByLine(new File(baselineModelSqlFile).toPath(), tempBaselineFile.toPath()));
    }

    @AfterAll
    public static void teardown() {
        assertTrue(generatedResultFile.delete());
        assertTrue(tempBaselineFile.delete());
    }

}
