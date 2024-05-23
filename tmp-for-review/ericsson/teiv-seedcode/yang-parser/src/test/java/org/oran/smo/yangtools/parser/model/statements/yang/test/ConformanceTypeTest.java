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
package org.oran.smo.yangtools.parser.model.statements.yang.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class ConformanceTypeTest extends YangTestCommon {

    @Test
    public void test2ModulesImports() {

        context.setFailFast(false);
        context.setIgnoreImportedProtocolAccessibleObjects(false);

        parseRelativeYangModels(Collections.<String> emptyList(), Arrays.asList("conformance-type-test/module1.yang",
                "conformance-type-test/module2.yang"));

        assertHasFindingOfType(ParserFindingType.P005_NO_IMPLEMENTS.toString());

        final YModule module1 = getModule("module1");
        final YModule module2 = getModule("module2");

        assertTrue(module1 != null);
        assertTrue(module2 != null);

        final YContainer container1 = getContainer(module1, "cont1");
        final YContainer container2 = getContainer(module2, "cont2");

        assertTrue(container1.getEffectiveConformanceType() == ConformanceType.IMPORT);
        assertTrue(container2.getEffectiveConformanceType() == ConformanceType.IMPORT);

        final YContainer contGroup2InModule1 = getContainer(module1, "contgroup2");
        final YContainer contGroup2InModule2 = getContainer(module2, "contgroup2");

        assertTrue(contGroup2InModule1.getEffectiveConformanceType() == ConformanceType.IMPORT);
        assertTrue(contGroup2InModule2.getEffectiveConformanceType() == ConformanceType.IMPORT);
    }

    @Test
    public void test2ModulesImplements() {

        parseRelativeImplementsYangModels(Arrays.asList("conformance-type-test/module1.yang",
                "conformance-type-test/module2.yang"));

        assertHasNotFindingOfType(ParserFindingType.P005_NO_IMPLEMENTS.toString());

        final YModule module1 = getModule("module1");
        final YModule module2 = getModule("module2");

        assertTrue(module1 != null);
        assertTrue(module2 != null);

        final YContainer container1 = getContainer(module1, "cont1");
        final YContainer container2 = getContainer(module2, "cont2");

        assertTrue(container1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(container2.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);

        final YContainer contGroup2InModule1 = getContainer(module1, "contgroup2");
        final YContainer contGroup2InModule2 = getContainer(module2, "contgroup2");

        assertTrue(contGroup2InModule1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(contGroup2InModule2.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
    }

    @Test
    public void test2ModulesOneImplementsOneImports() {

        context.setIgnoreImportedProtocolAccessibleObjects(false);
        parseRelativeYangModels(Arrays.asList("conformance-type-test/module1.yang"), Arrays.asList(
                "conformance-type-test/module2.yang"));

        final YModule module1 = getModule("module1");
        final YModule module2 = getModule("module2");

        assertTrue(module1 != null);
        assertTrue(module2 != null);

        final YContainer container1 = getContainer(module1, "cont1");
        final YContainer container2 = getContainer(module2, "cont2");

        assertTrue(container1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(container2.getEffectiveConformanceType() == ConformanceType.IMPORT);

        final YContainer contGroup2InModule1 = getContainer(module1, "contgroup2");
        final YContainer contGroup2InModule2 = getContainer(module2, "contgroup2");

        assertTrue(contGroup2InModule1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(contGroup2InModule2.getEffectiveConformanceType() == ConformanceType.IMPORT);
    }

    /*
     * Tests for "ignoreImportedProtocolAccessibleObjects"
     */

    @Test
    public void test2ModulesImplementsIgnoreImportedProtocolAccessibleObjects() {

        context.setIgnoreImportedProtocolAccessibleObjects(true);

        parseRelativeImplementsYangModels(Arrays.asList("conformance-type-test/module1.yang",
                "conformance-type-test/module2.yang"));

        printFindings();
        assertNoFindings();

        final YModule module1 = getModule("module1");
        final YModule module2 = getModule("module2");

        assertTrue(module1 != null);
        assertTrue(module2 != null);

        final YContainer container1 = getContainer(module1, "cont1");
        final YContainer container2 = getContainer(module2, "cont2");
        assertTrue(container1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(container2.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);

        final YContainer contGroup2InModule1 = getContainer(module1, "contgroup2");
        final YContainer contGroup2InModule2 = getContainer(module2, "contgroup2");
        assertTrue(contGroup2InModule1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(contGroup2InModule2.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);

        assertTrue(getChild(module2, CY.RPC, "rpc1") != null);
        assertTrue(getChild(module2, CY.LIST, "list1") != null);

        assertTrue(getChild(module2, CY.FEATURE, "feature1") != null);
        assertTrue(getChild(module2, "this:ext", "ext1") != null);
    }

    @Test
    public void test2ModulesOneImplementsOneImportsIgnoreImportedProtocolAccessibleObjects() {

        context.setIgnoreImportedProtocolAccessibleObjects(true);

        parseRelativeYangModels(Arrays.asList("conformance-type-test/module1.yang"), Arrays.asList(
                "conformance-type-test/module2.yang"));

        final YModule module1 = getModule("module1");
        final YModule module2 = getModule("module2");

        assertTrue(module1 != null);
        assertTrue(module2 != null);

        final YContainer container1 = getContainer(module1, "cont1");
        final YContainer container2 = getContainer(module2, "cont2");

        assertTrue(container1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(container2 == null);

        final YContainer contGroup2InModule1 = getContainer(module1, "contgroup2");
        final YContainer contGroup2InModule2 = getContainer(module2, "contgroup2");

        assertTrue(contGroup2InModule1.getEffectiveConformanceType() == ConformanceType.IMPLEMENT);
        assertTrue(contGroup2InModule2 == null);

        assertTrue(getChild(module2, CY.RPC, "rpc1") == null);
        assertTrue(getChild(module2, CY.LIST, "list1") == null);

        assertTrue(getChild(module2, CY.FEATURE, "feature1") != null);
        assertTrue(getChild(module2, "this:ext", "ext1") != null);
    }

    // - - - - test crossing conformance type for module / submodule

    @Test
    public void test2ModuleAndSubmoduleSameConformance() {

        parseRelativeYangModels(Arrays.asList("conformance-type-test/including-module.yang",
                "conformance-type-test/submodule.yang"), Collections.emptyList());

        final YModule module = getModule("including-module");
        assertTrue(module != null);

        assertNoFindings();
    }

    @Test
    public void test2ModuleAndSubmoduleDifferentConformance() {

        parseRelativeYangModels(Arrays.asList("conformance-type-test/including-module.yang"), Arrays.asList(
                "conformance-type-test/submodule.yang"));

        final YModule module = getModule("including-module");
        assertTrue(module != null);

        assertHasFindingOfType(ParserFindingType.P006_IMPLEMENT_IMPORT_MISMATCH.toString());
    }

}
