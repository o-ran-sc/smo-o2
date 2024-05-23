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
package org.oran.smo.yangtools.parser.model.schema.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.schema.ModuleRegistry;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class ModuleRegistryTest extends YangTestCommon {

    @Test
    public void test_two_modules_implement_allOk() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang",
                "src/test/resources/model-schema/module-registry-test/module2-2020-01-01.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 2);

        assertTrue(moduleRegistry.find("module1", "2020-01-01") != null);
        assertTrue(moduleRegistry.find("module2", "2020-01-01") != null);
        assertTrue(moduleRegistry.find(new ModuleIdentity("module1")) != null);
        assertTrue(moduleRegistry.find(new ModuleIdentity("module2")) != null);
        assertTrue(moduleRegistry.find("module1", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("module2", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.find(new ModuleIdentity("module1", "2020-01-01")) != null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 1);
        assertTrue(moduleRegistry.byModuleName("module2").size() == 1);
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_two_modules_implement_same_revision() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang",
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertHasFindingOfType(ParserFindingType.P003_DUPLICATE_INPUT.toString());

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 1);

        assertTrue(moduleRegistry.find(new ModuleIdentity("module1")) != null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 1);
        assertTrue(moduleRegistry.byModuleName("module2").isEmpty());
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_two_modules_implement_same_empty_revision() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-no-revision.yang",
                "src/test/resources/model-schema/module-registry-test/module1-no-revision.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertHasFindingOfType(ParserFindingType.P003_DUPLICATE_INPUT.toString());

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 1);

        assertTrue(moduleRegistry.find(new ModuleIdentity("module1")) != null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 1);
        assertTrue(moduleRegistry.byModuleName("module2").isEmpty());
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_two_modules_implement_different_revisions() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang",
                "src/test/resources/model-schema/module-registry-test/module1-no-revision.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertHasFindingOfType(ParserFindingType.P004_SAME_MODULE_DUPLICATE_IMPLEMENTS.toString());

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 2);

        assertTrue(moduleRegistry.find("module1", "2020-01-01") != null);
        assertTrue(moduleRegistry.find("module1", null) != null);
        assertTrue(moduleRegistry.find("module1", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 2);
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_two_modules_different_revisions_onebeingempty_one_implement_one_import() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-no-revision.yang");

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 2);

        assertTrue(moduleRegistry.find("module1", "2020-01-01") != null);
        assertTrue(moduleRegistry.find("module1", null) != null);
        assertTrue(moduleRegistry.find("module1", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 2);
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_two_modules_different_revision_one_implement_one_import() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-01-01.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module1-2020-02-02.yang");

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 2);

        assertTrue(moduleRegistry.find("module1", "2020-01-01") != null);
        assertTrue(moduleRegistry.find("module1", "2020-02-02") != null);
        assertTrue(moduleRegistry.find("module1", null) == null);
        assertTrue(moduleRegistry.find("module1", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module1").size() == 2);
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

    @Test
    public void test_submodules_implement_allOk() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/module-registry-test/module3-2020-01-01.yang",
                "src/test/resources/model-schema/module-registry-test/module3-submodule1-1999-09-09.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        assertTrue(moduleRegistry.getAllYangModels().size() == 2);

        assertTrue(moduleRegistry.find("module3", "2020-01-01") != null);
        assertTrue(moduleRegistry.find("module3-submodule1", "1999-09-09") != null);
        assertTrue(moduleRegistry.find(new ModuleIdentity("module3")) != null);
        assertTrue(moduleRegistry.find(new ModuleIdentity("module3-submodule1")) != null);
        assertTrue(moduleRegistry.find("module3", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("module3-submodule1", "1987-12-12") == null);
        assertTrue(moduleRegistry.find("XXX", "2020-01-01") == null);
        assertTrue(moduleRegistry.find("XXX", null) == null);
        assertTrue(moduleRegistry.find("XXX", "1987-12-12") == null);

        assertTrue(moduleRegistry.byModuleName("module3").size() == 1);
        assertTrue(moduleRegistry.byModuleName("module3-submodule1").size() == 1);
        assertTrue(moduleRegistry.byModuleName("XXX").isEmpty());
    }

}
