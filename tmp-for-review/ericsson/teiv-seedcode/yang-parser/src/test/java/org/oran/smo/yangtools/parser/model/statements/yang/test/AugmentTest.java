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
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YInput;
import org.oran.smo.yangtools.parser.model.statements.yang.YList;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YRpc;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class AugmentTest extends YangTestCommon {

    @Test
    public void testAugmentIntoEmptyActionAndRpc() {

        severityCalculator.suppressFinding(ParserFindingType.P152_AUGMENT_TARGET_NODE_IN_SAME_MODULE.toString());

        parseRelativeImplementsYangModels(Arrays.asList("augment-test/augment-test-module.yang",
                "augment-test/augment-test-module2.yang"));

        final YModule augmentTestModule = getModule("augment-test-module");
        assertTrue(augmentTestModule != null);

        assertTrue(augmentTestModule.getRpcs().size() == 1);
        assertTrue(augmentTestModule.getRpcs().get(0).getInput() != null);
        assertTrue(augmentTestModule.getRpcs().get(0).getOutput() != null);

        assertTrue(augmentTestModule.getRpcs().get(0).getInput().getLeafs().size() == 1);
        assertTrue(augmentTestModule.getRpcs().get(0).getOutput().getLeafs().size() == 1);

        final YContainer cont1 = getContainer(augmentTestModule, "cont1");

        assertTrue(cont1.getActions().size() == 1);
        assertTrue(cont1.getActions().get(0).getInput() != null);
        assertTrue(cont1.getActions().get(0).getOutput() != null);

        assertTrue(cont1.getActions().get(0).getInput().getLeafs().size() == 1);
        assertTrue(cont1.getActions().get(0).getOutput().getLeafs().size() == 1);

        // Tests added to handle augments into a choice

        final YRpc rpc1 = getRpc(augmentTestModule, "rpc1");
        final YInput rpc1input = rpc1.getInput();
        final YContainer rpc1cont1 = getContainer(rpc1input, "cont1");
        final YChoice choice1 = getChoice(rpc1cont1, "choice1");

        assertTrue(getCase(choice1, "case1") != null);
        assertTrue(getCase(choice1, "case2") != null);
        assertTrue(getCase(choice1, "inserted-case-leaf") != null);
        assertTrue(getCase(choice1, "inserted-case-leaf-from-second-module") != null);

        // - - - - some other different target nodes - - - - - -

        final YContainer cont2 = getContainer(augmentTestModule, "cont2");
        assertTrue(getList(cont2, "list1") != null);
        assertTrue(getLeaf(getList(cont2, "list1"), "leaf11") != null);
        assertTrue(getLeaf(getList(cont2, "list1"), "leaf15") != null);

        assertTrue(getLeaf(getNotification(cont2, "notification1"), "leaf18") != null);

        // - - - - - status - - - - - -

        final YContainer cont3 = getContainer(augmentTestModule, "cont3");
        assertTrue(cont3.getEffectiveStatus().equals(YStatus.CURRENT));

        assertTrue(getContainer(cont3, "cont11") != null);
        assertTrue(getContainer(cont3, "cont11").getStatus().isDeprecated());
        assertTrue(getContainer(cont3, "cont11").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getContainer(cont3, "cont12") != null);
        assertTrue(getContainer(cont3, "cont12").getStatus().isDeprecated());
        assertTrue(getContainer(cont3, "cont12").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getContainer(cont3, "cont13") != null);
        assertTrue(getContainer(cont3, "cont13").getStatus().isObsolete());
        assertTrue(getContainer(cont3, "cont13").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getContainer(cont3, "cont15") != null);
        assertTrue(getContainer(cont3, "cont15").getStatus().isCurrent());
        assertTrue(getContainer(cont3, "cont15").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getContainer(cont3, "cont16") != null);
        assertTrue(getContainer(cont3, "cont16").getStatus().isDeprecated());
        assertTrue(getContainer(cont3, "cont16").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getContainer(cont3, "cont17") != null);
        assertTrue(getContainer(cont3, "cont17").getStatus().isObsolete());
        assertTrue(getContainer(cont3, "cont17").getEffectiveStatus().equals(YStatus.OBSOLETE));

        // - - - - - strange path syntax - - - - - -

        assertTrue(getContainer(cont1, "cont18") != null);
        assertNoFindingsOnStatement(getAugment(augmentTestModule, "/this:cont1/"));

        assertTrue(getContainer(cont1, "cont19") != null);
        assertNoFindingsOnStatement(getAugment(augmentTestModule, "/:cont1"));

        assertTrue(getContainer(cont1, "cont20") != null);
        assertNoFindingsOnStatement(getAugment(augmentTestModule, "/cont1"));

        assertTrue(getContainer(cont1, "cont21") == null);
        assertStatementHasFindingOfType(getAugment(augmentTestModule, "//this:cont1"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertTrue(getContainer(cont1, "cont22") == null);
        assertStatementHasFindingOfType(getAugment(augmentTestModule, "//this:cont1/"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertTrue(getContainer(cont1, "cont23") == null);
        assertStatementHasFindingOfType(getAugment(augmentTestModule, "/"), ParserFindingType.P054_UNRESOLVABLE_PATH
                .toString());

        assertTrue(getContainer(cont1, "cont24") == null);
        assertStatementHasFindingOfType(getAugment(augmentTestModule, null),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(getContainer(cont1, "cont25") == null);
        assertStatementHasFindingOfType(getAugment(augmentTestModule, null),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        // - - - - - - - - - issues with prefixes - - - - - -

        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
        assertStatementHasFindingOfType(getAugment(augmentTestModule, "/nsm:some-cont"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertStatementHasFindingOfType(getAugment(augmentTestModule, "/this:cont1/nsm:some-cont"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertStatementHasFindingOfType(getAugment(augmentTestModule, "/this:cont1/unknown-prefix:some-cont"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertStatementHasFindingOfType(getAugment(augmentTestModule, "/this:cont1/:some-cont"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());
    }

    @Test
    public void testAugment3() {

        parseRelativeImplementsYangModels(Arrays.asList("augment-test/augment-test-module3.yang"));

        final YModule module = getModule("augment-test-module3");
        assertTrue(module != null);

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getAction(cont1, "action1") != null);
        assertTrue(getAction(cont1, "action1").getInput() != null);

        assertTrue(getContainer(getAction(cont1, "action1").getInput(), "input-container") != null);
        assertTrue(getContainer(getAction(cont1, "action1").getInput(), "input-container").getWhens().size() == 1);
        assertTrue(getContainer(getAction(cont1, "action1").getInput(), "input-container").getWhens().get(0).getValue()
                .equals("leaf-at-root = true"));

        final YList list2 = getList(cont1, "list2");
        assertTrue(list2 != null);

        assertTrue(getLeaf(list2, "leaf6") != null);
        assertTrue(getLeaf(list2, "leaf7") != null);
        assertTrue(getAction(list2, "action3") != null);

        final YChoice choice8 = getChoice(cont1, "choice8");
        assertTrue(choice8 != null);

        assertTrue(getCase(choice8, "shorthand-container") != null);
        assertTrue(getCase(choice8, "shorthand-container").getIfFeatures().size() == 1);
        assertTrue(getContainer(getCase(choice8, "shorthand-container"), "shorthand-container") != null);

        assertTrue(getCase(choice8, "case3") != null);
        assertTrue(getCase(choice8, "case3").getIfFeatures().size() == 2);
        assertTrue(getContainer(getCase(choice8, "case3"), "case-container") != null);
        assertTrue(getContainer(getCase(choice8, "case3"), "second-case-container") != null);

        // ------------------------ failures ------------------------

        assertStatementHasFindingOfType(getAugment(module, "/leaf-at-root"),
                ParserFindingType.P151_TARGET_NODE_CANNOT_BE_AUGMENTED.toString());
        assertStatementHasFindingOfType(getAugment(module, "/unknownprefix:somenode"),
                ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString());
        assertStatementHasFindingOfType(getAugment(module, "/this:unknown-node"), ParserFindingType.P054_UNRESOLVABLE_PATH
                .toString());
        assertStatementHasFindingOfType(getAugment(module, ""), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertStatementHasFindingOfType(getAugment(module, "cont1"), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertStatementHasFindingOfType(getAugment(module, null), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        // -------------------------- not-allowed statements -------------------------

        assertStatementHasFindingOfType(getAugment(module, "/this:cont1/choice8/case1").getActions().get(0),
                ParserFindingType.P151_TARGET_NODE_CANNOT_BE_AUGMENTED.toString());
        assertStatementHasFindingOfType(getAugment(module, "/this:cont1/choice8/case2").getNotifications().get(0),
                ParserFindingType.P151_TARGET_NODE_CANNOT_BE_AUGMENTED.toString());
    }

    @Test
    public void testAugmentIntoSubModule() {

        //   	severityCalculator.suppressFinding(ParserFindingType.P152_AUGMENT_TARGET_NODE_IN_SAME_MODULE.toString());

        parseRelativeImplementsYangModels(Arrays.asList("augment-test/augment-intosub-module.yang",
                "augment-test/augment-intosub-submodule.yang", "augment-test/augment-intosub-augmenting-module.yang"));

        final YModule module = getModule("augment-intosub-module");
        assertTrue(module != null);

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf11") != null);
        assertTrue(getLeaf(cont1, "leaf11").getEffectiveNamespace().equals("test:augment-intosub-augmenting-module"));

        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);

        assertTrue(getLeaf(cont2, "leaf12") != null);
        assertTrue(getLeaf(cont2, "leaf12").getEffectiveNamespace().equals("test:augment-intosub-augmenting-module"));

        printFindings();
    }

}
