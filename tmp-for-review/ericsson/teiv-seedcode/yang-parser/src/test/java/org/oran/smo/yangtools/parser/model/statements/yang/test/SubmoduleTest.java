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

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class SubmoduleTest extends YangTestCommon {

    @Test
    public void testSubmoduleByExplicitRevisionOk() {
        parseRelativeImplementsYangModels(Arrays.asList("submodule-test/explicit-revision-ok/module.yang",
                "submodule-test/explicit-revision-ok/submodule.yang"));
        assertNoFindings();

        final YModule yModule = getModule("submodule-test-module");
        assertTrue(yModule != null);

        final YContainer container1 = getContainer(yModule, "cont1");
        assertTrue(container1 != null);

        final YContainer container2 = getContainer(yModule, "cont2");
        assertTrue(container2 != null);

        final YLeaf leaf31 = getLeaf(yModule, "leaf31");
        assertTrue(leaf31 != null);
    }

    @Test
    public void testSubmoduleByExplicitRevisionWrongRevisionSupplied() {
        parseRelativeImplementsYangModels(Arrays.asList("submodule-test/explicit-revision-not-found/module.yang",
                "submodule-test/explicit-revision-not-found/submodule.yang"));

        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());
    }

    @Test
    public void testSubmoduleByExplicitRevisionNotSupplied() {
        parseRelativeImplementsYangModels(Arrays.asList("submodule-test/explicit-revision-not-found/module.yang"));

        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());
    }

    @Test
    public void test_multiple_submodules() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());

        parseRelativeImplementsYangModels(Arrays.asList("submodule-test/multiple-submodules/module.yang",
                "submodule-test/multiple-submodules/submodule1.yang", "submodule-test/multiple-submodules/submodule2.yang",
                "submodule-test/multiple-submodules/submodule3.yang",
                "submodule-test/multiple-submodules/importing-module.yang"));

        assertNoFindings();

        final YModule module = getModule("module");
        assertTrue(module != null);

        final YContainer cont1 = getContainer(module, "cont1");

        assertTrue(cont1 != null);
        assertTrue(getContainer(cont1, "cont51") == null);
        assertTrue(getContainer(cont1, "cont52") != null);
        assertTrue(getLeaf(cont1, "leaf11") != null);

        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);
        assertTrue(getLeaf(cont2, "leaf12") != null);
        assertTrue(getLeaf(cont2, "leaf81") != null);
        assertTrue(getLeaf(cont2, "leaf81").getEffectiveNamespace().equals("test:importing-module"));

        final YContainer cont11 = getContainer(module, "cont11");
        assertTrue(cont11 != null);
        assertTrue(cont11.getEffectiveNamespace().equals("test:module"));
        assertTrue(getLeaf(cont11, "leaf24") != null);

        final YContainer cont12 = getContainer(module, "cont12");
        assertTrue(cont12 != null);
        assertTrue(getContainer(cont12, "cont14") == null);
        assertTrue(getLeaf(cont12, "leaf25") != null);

        final YContainer cont22 = getContainer(module, "cont22");
        assertTrue(cont22 != null);
        assertTrue(cont22.getEffectiveNamespace().equals("test:module"));
        assertTrue(getContainer(cont22, "cont61") != null);

        final YContainer cont23 = getContainer(module, "cont23");
        assertTrue(cont23 != null);
        assertTrue(getLeaf(cont23, "leaf31") != null);
        assertTrue(getLeaf(cont23, "leaf82") != null);
        assertTrue(getLeaf(cont23, "leaf82").getEffectiveNamespace().equals("test:importing-module"));

        final YContainer cont33 = getContainer(module, "cont33");
        assertTrue(cont33 != null);
        assertTrue(cont33.getEffectiveNamespace().equals("test:module"));

        final YContainer cont34 = getContainer(module, "cont34");
        assertTrue(cont34 != null);
        assertTrue(getContainer(cont34, "cont37") == null);

        final YLeaf leaf1 = getLeaf(module, "leaf1");
        assertTrue(leaf1 != null);
        assertTrue(leaf1.getEffectiveNamespace().equals("test:module"));
        assertTrue(leaf1.getType().getDataType().equals("boolean"));

        final YLeaf leaf2 = getLeaf(module, "leaf2");
        assertTrue(leaf2 != null);
        assertTrue(leaf2.getEffectiveNamespace().equals("test:module"));
        assertTrue(leaf2.getType().getDataType().equals("boolean"));
    }

}
