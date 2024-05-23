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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class RemoveProtocolAccessibleObjectsTest extends YangTestCommon {

    private static final String TEST_DIR = "src/test/resources/model-schema/remove-protocol-accessible-objects/";

    private static final String BASE_NS = "test:base-module";
    private static final String OTHER_NS = "test:other-module";
    private static final String AUGMENTING_NS = "test:augmenting-module";

    @Test
    public void test___ignore_true___base_other_implement() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang",
                TEST_DIR + "other-module.yang");
        final List<String> absoluteImportsFilePath = Collections.emptyList();

        context.setIgnoreImportedProtocolAccessibleObjects(true);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        assertEquals(BASE_NS, cont1.getEffectiveNamespace());
        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);
        assertEquals(BASE_NS, cont2.getEffectiveNamespace());

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");
        assertNotNull(cont5);
        assertEquals(OTHER_NS, cont5.getEffectiveNamespace());
        final YContainer cont6 = getContainer(otherModule, "cont6");
        assertNotNull(cont6);
        assertEquals(OTHER_NS, cont6.getEffectiveNamespace());
    }

    @Test
    public void test___ignore_true___base_implements_other_imports() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(TEST_DIR + "other-module.yang");

        context.setIgnoreImportedProtocolAccessibleObjects(true);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");		// Shouldn't exist
        assertNull(cont5);
        final YContainer cont6 = getContainer(otherModule, "cont6");		// Shouldn't exist
        assertNull(cont6);
    }

    @Test
    public void test___ignore_false___base_implements___other_imports() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(TEST_DIR + "other-module.yang");

        context.setIgnoreImportedProtocolAccessibleObjects(false);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");		// Should exist as flag = false
        assertNotNull(cont5);
        final YContainer cont6 = getContainer(otherModule, "cont6");		// Should exist as flag = false
        assertNotNull(cont6);
    }

    @Test
    public void test___ignore_true___base_other_augments_implement() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang",
                TEST_DIR + "other-module.yang", TEST_DIR + "augmenting-module.yang");
        final List<String> absoluteImportsFilePath = Collections.emptyList();

        context.setIgnoreImportedProtocolAccessibleObjects(true);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        assertEquals(BASE_NS, cont1.getEffectiveNamespace());
        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertNotNull(leaf11);
        assertEquals(BASE_NS, leaf11.getEffectiveNamespace());
        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertNotNull(leaf12);
        assertEquals(AUGMENTING_NS, leaf12.getEffectiveNamespace());

        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);
        assertEquals(BASE_NS, cont2.getEffectiveNamespace());

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");
        assertNotNull(cont5);
        assertEquals(OTHER_NS, cont5.getEffectiveNamespace());
        final YLeaf leaf51 = getLeaf(cont5, "leaf51");
        assertNotNull(leaf51);
        assertEquals(OTHER_NS, leaf51.getEffectiveNamespace());
        final YLeaf leaf52 = getLeaf(cont5, "leaf52");
        assertNotNull(leaf52);
        assertEquals(AUGMENTING_NS, leaf52.getEffectiveNamespace());

        final YContainer cont6 = getContainer(otherModule, "cont6");
        assertNotNull(cont6);
        assertEquals(OTHER_NS, cont6.getEffectiveNamespace());
    }

    @Test
    public void test___ignore_true___base_other_implement___augments_imports() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang",
                TEST_DIR + "other-module.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(TEST_DIR + "augmenting-module.yang");

        context.setIgnoreImportedProtocolAccessibleObjects(true);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        assertEquals(BASE_NS, cont1.getEffectiveNamespace());
        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertNotNull(leaf11);
        assertEquals(BASE_NS, leaf11.getEffectiveNamespace());
        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertNull(leaf12);

        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);
        assertEquals(BASE_NS, cont2.getEffectiveNamespace());

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");
        assertNotNull(cont5);
        assertEquals(OTHER_NS, cont5.getEffectiveNamespace());
        final YLeaf leaf51 = getLeaf(cont5, "leaf51");
        assertNotNull(leaf51);
        assertEquals(OTHER_NS, leaf51.getEffectiveNamespace());
        final YLeaf leaf52 = getLeaf(cont5, "leaf52");
        assertNull(leaf52);

        final YContainer cont6 = getContainer(otherModule, "cont6");
        assertNotNull(cont6);
        assertEquals(OTHER_NS, cont6.getEffectiveNamespace());
    }

    @Test
    public void test___ignore_false___base_other_implement___augments_imports() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(TEST_DIR + "base-module.yang",
                TEST_DIR + "other-module.yang");
        final List<String> absoluteImportsFilePath = Arrays.asList(TEST_DIR + "augmenting-module.yang");

        context.setIgnoreImportedProtocolAccessibleObjects(false);
        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final YModule baseModule = getModule("base-module");
        assertNotNull(baseModule);

        final YContainer cont1 = getContainer(baseModule, "cont1");
        assertNotNull(cont1);
        assertEquals(BASE_NS, cont1.getEffectiveNamespace());
        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertNotNull(leaf11);
        assertEquals(BASE_NS, leaf11.getEffectiveNamespace());
        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertNotNull(leaf12);
        assertEquals(AUGMENTING_NS, leaf12.getEffectiveNamespace());

        final YContainer cont2 = getContainer(baseModule, "cont2");
        assertNotNull(cont2);
        assertEquals(BASE_NS, cont2.getEffectiveNamespace());

        final YModule otherModule = getModule("other-module");
        assertNotNull(otherModule);

        final YContainer cont5 = getContainer(otherModule, "cont5");
        assertNotNull(cont5);
        assertEquals(OTHER_NS, cont5.getEffectiveNamespace());
        final YLeaf leaf51 = getLeaf(cont5, "leaf51");
        assertNotNull(leaf51);
        assertEquals(OTHER_NS, leaf51.getEffectiveNamespace());
        final YLeaf leaf52 = getLeaf(cont5, "leaf52");
        assertNotNull(leaf52);
        assertEquals(AUGMENTING_NS, leaf52.getEffectiveNamespace());

        final YContainer cont6 = getContainer(otherModule, "cont6");
        assertNotNull(cont6);
        assertEquals(OTHER_NS, cont6.getEffectiveNamespace());
    }
}
