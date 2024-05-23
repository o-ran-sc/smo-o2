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

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YWhen;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class GroupingTest extends YangTestCommon {

    @Test
    public void testGrouping() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        parseRelativeImplementsYangModels(Arrays.asList("grouping-test/uses-module.yang",
                "grouping-test/grouping-module.yang"));

        assertNoFindings();

        final YModule usesModule = getModule("uses-module");
        assertTrue(usesModule != null);

        final YContainer cont1 = getContainer(usesModule, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf11") != null);
        assertTrue(getLeaf(cont1, "leaf11").getIfFeatures().size() == 0);
        assertTrue(getContainer(cont1, "contgroup1") != null);
        assertTrue(getContainer(cont1, "contgroup1").getIfFeatures().size() == 1);
        assertTrue(getContainer(cont1, "contgroup1").getIfFeatures().get(0).getValue().equals("feature1 or feature2"));

        assertTrue(getLeafUnderContainer(usesModule, "cont2", "leaf21") != null);
        assertTrue(getLeafFromContainerContainer(usesModule, "cont2", "contgroup1", "leafgroup1") != null);

        final YWhen when = getContainerUnderContainer(usesModule, "cont2", "contgroup1").getWhens().get(0);
        assertTrue(when != null);
        assertTrue(when.appliesToParentSchemaNode() == true);

        final YContainer cont3 = getContainer(usesModule, "cont3");
        assertTrue(cont3 != null);
        assertTrue(cont3.getEffectiveNamespace().equals("urn:o-ran:yang:uses-module"));

        assertTrue(getLeaf(cont3, "leaf31") != null);
        assertTrue(getContainer(cont3, "contgroup1") != null);
        assertTrue(getContainer(cont3, "contgroup1").getEffectiveNamespace().equals("urn:o-ran:yang:uses-module"));
        assertTrue(getContainer(cont3, "contgroup1").getWhens().size() == 0);
        assertTrue(getLeaf(getContainer(cont3, "contgroup1"), "leaf32") != null);
        assertTrue(getLeaf(getContainer(cont3, "contgroup1"), "leaf32").getEffectiveNamespace().equals(
                "urn:o-ran:yang:uses-module"));
        assertTrue(getLeaf(getContainer(cont3, "contgroup1"), "leaf32").getWhens().size() == 1);
        assertTrue(getLeaf(getContainer(cont3, "contgroup1"), "leaf32").getWhens().get(0).getValue().equals("abc > 10"));

        // - - - augmenting a choice

        final YContainer cont4 = getContainer(usesModule, "cont4");
        assertTrue(cont4 != null);

        assertTrue(getLeaf(cont4, "leaf21") != null);
        assertTrue(getLeaf(cont4, "leaf22") != null);

        final YChoice choice23 = getChoice(cont4, "choice23");
        assertTrue(choice23 != null);

        assertTrue(getCase(choice23, "leaf41") != null);
        assertTrue(getCase(choice23, "leaf42") != null);
        assertTrue(getCase(choice23, "case43") != null);
        assertTrue(getCase(choice23, "cont44") != null);

        // - - - - refine handling (without refine first, then with refine)

        {
            final YContainer cont5 = getContainer(usesModule, "cont5");
            assertTrue(cont5 != null);

            final YContainer cont31 = getContainer(cont5, "cont31");
            assertTrue(cont31 != null);

            assertTrue(getLeaf(cont31, "leaf35").getDefault().getValue().equals("hello"));
            assertTrue(getLeaf(cont31, "leaf35").getMandatory() == null);

            assertTrue(getLeaf(cont31, "leaf36").getConfig().getValue().equals("true"));
            assertTrue(getLeaf(cont31, "leaf36").getMandatory().isMandatoryTrue());

            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().size() == 3);
            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().get(0).getDecimalDefaultValue().compareTo(BigDecimal
                    .valueOf(10L)) == 0);
            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().get(1).getDecimalDefaultValue().compareTo(BigDecimal
                    .valueOf(20L)) == 0);
            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().get(2).getDecimalDefaultValue().compareTo(BigDecimal
                    .valueOf(30L)) == 0);
            assertTrue(getLeafList(cont31, "leaflist37").getMinElements().getMinValue() == 2);
            assertTrue(getLeafList(cont31, "leaflist37").getMaxElements().getMaxValue() == 5);

            final YContainer cont32 = getContainer(cont5, "cont32");
            assertTrue(cont32 != null);

            assertTrue(cont32.getPresence() == null);
            assertTrue(cont32.getIfFeatures().size() == 1);
            assertTrue(cont32.getIfFeatures().get(0).getValue().equals("feature1"));
            assertTrue(cont32.getMusts().size() == 1);
            assertTrue(cont32.getMusts().get(0).getXpathExpression().equals("abc > 10"));
        }

        {
            final YContainer cont6 = getContainer(usesModule, "cont6");
            assertTrue(cont6 != null);

            final YContainer cont31 = getContainer(cont6, "cont31");
            assertTrue(cont31 != null);

            assertTrue(getLeaf(cont31, "leaf35").getDefault().getValue().equals("world"));
            assertTrue(getLeaf(cont31, "leaf35").getMandatory().isMandatoryTrue());

            assertTrue(getLeaf(cont31, "leaf36").getConfig().getValue().equals("false"));
            assertTrue(getLeaf(cont31, "leaf36").getMandatory().isMandatoryFalse());

            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().size() == 2);
            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().get(0).getDecimalDefaultValue().compareTo(BigDecimal
                    .valueOf(80L)) == 0);
            assertTrue(getLeafList(cont31, "leaflist37").getDefaults().get(1).getDecimalDefaultValue().compareTo(BigDecimal
                    .valueOf(90L)) == 0);
            assertTrue(getLeafList(cont31, "leaflist37").getMinElements().getMinValue() == 1);
            assertTrue(getLeafList(cont31, "leaflist37").getMaxElements().getMaxValue() == 6);

            final YContainer cont32 = getContainer(cont6, "cont32");
            assertTrue(cont32 != null);

            assertTrue(cont32.getPresence() != null);
            assertTrue(cont32.getIfFeatures().size() == 2);
            assertTrue(cont32.getIfFeatures().get(0).getValue().equals("feature1"));
            assertTrue(cont32.getIfFeatures().get(1).getValue().equals("feature99"));
            assertTrue(cont32.getMusts().size() == 2);
            assertTrue(cont32.getMusts().get(0).getXpathExpression().equals("abc > 10"));
            assertTrue(cont32.getMusts().get(1).getXpathExpression().equals("xyz > 99"));
        }

        // - - - - refine handling (extensions)

        final YContainer cont7 = getContainer(usesModule, "cont7");
        assertTrue(cont7 != null);

        final YContainer cont41 = getContainer(cont7, "cont41");
        assertTrue(cont41 != null);

        assertTrue(getExtension(cont41, "grouping-module", "extension1", null) != null);
        assertTrue(getExtension(cont41, "grouping-module", "extension2", "hello world") != null);
        assertTrue(getExtension(cont41, "grouping-module", "extension2", "hello") == null);
        assertTrue(getExtension(cont41, "grouping-module", "extension2", "world") == null);
        assertTrue(getExtension(cont41, "grouping-module", "extension3", null) != null);

        final YContainer cont42 = getContainer(cont7, "cont42");
        assertTrue(cont42 != null);

        assertTrue(getExtension(cont42, "grouping-module", "extension1", null) != null);
        assertTrue(getExtension(cont42, "grouping-module", "extension2", "abc") != null);
        assertTrue(getExtension(cont42, "grouping-module", "extension2", "def") != null);
        assertTrue(getExtension(cont42, "grouping-module", "extension3", null) != null);
    }

    @Test
    public void testGroupingFindings___do_not_suppress_unused() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        context.setSuppressFindingsOnUnusedSchemaNodes(false);
        parseRelativeImplementsYangModels(Arrays.asList("grouping-test/faulty-grouping-module.yang"));

        final YModule module = getModule("faulty-grouping-module");

        assertStatementHasFindingOfType(getGrouping(module, "grouping1").getUses().get(0),
                ParserFindingType.P121_CIRCULAR_USES_REFERENCES.toString());

        // - - - - circular usage

        assertTrue(getGrouping(module, "grouping2") != null);
        assertStatementHasFindingOfType(getGrouping(module, "grouping2").getUses().get(0),
                ParserFindingType.P121_CIRCULAR_USES_REFERENCES.toString());

        assertTrue(getGrouping(module, "grouping3") != null);
        assertStatementHasFindingOfType(getGrouping(module, "grouping3").getUses().get(0),
                ParserFindingType.P121_CIRCULAR_USES_REFERENCES.toString());

        // ------ augment path cannot be found or syntax error

        assertTrue(getContainer(module, "cont5") != null);
        assertHasFindingOfTypeAndContainsMessage(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                "'/absolute-path'");

        assertTrue(getContainer(module, "cont6") != null);
        assertHasFindingOfTypeAndContainsMessage(ParserFindingType.P054_UNRESOLVABLE_PATH.toString(),
                "'container-that-does-not-exist-in-simple-grouping'");

        // - - - - - - Invalid paths - - - - -

        //        printFindings();

        assertStatementHasFindingOfType(getChild(module, CY.USES, null), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        assertStatementHasFindingOfType(getChild(module, CY.USES, ""), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        assertStatementHasFindingOfType(getChild(module, CY.USES, " "), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        assertStatementHasFindingOfType(getChild(module, CY.USES, "nsm:non-existing-grouping"),
                ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString());

        final YContainer cont8 = getContainer(module, "cont8");

        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(0).getChildren().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(0).getChildren().get(1),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(0).getChildren().get(2),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(0).getChildren().get(3),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(1).getChildren().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(1).getChildren().get(1),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(1).getChildren().get(2),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont8.getDomElement().getChildren().get(1).getChildren().get(3),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        // - - - - - - Nested unresolvable uses - - - - -

        assertTrue(getContainer(module, "cont9") != null);
        assertStatementHasFindingOfType(getContainer(module, "cont9").getUses().get(0),
                ParserFindingType.P134_NESTED_USES_NOT_RESOLVABLE.toString());
        assertFindingCountOnStatement(getContainer(module, "cont9").getUses().get(0), 2);
    }

    @Test
    public void test_uses_refine() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        parseRelativeImplementsYangModels(Arrays.asList("grouping-test/uses-refine-module.yang"));

        final YModule module = getModule("uses-refine-module");
        assertTrue(module != null);

        // - - - - should be all ok - - - - - -

        final YContainer cont1 = getContainer(module, "cont1");

        assertTrue(getLeaf(cont1, "leaf1").getDescription().getValue().equals("new description"));
        assertTrue(getLeaf(cont1, "leaf1").getReference().getValue().equals("new reference"));
        assertTrue(getLeaf(cont1, "leaf1").getConfig().isConfigTrue());
        assertTrue(getLeaf(cont1, "leaf1").getDefault().getValue().equals("world"));
        assertTrue(getLeaf(cont1, "leaf1").getMandatory().isMandatoryTrue());
        assertTrue(getLeaf(cont1, "leaf1").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf1").getIfFeatures().get(0).getValue().equals("feature2"));

        assertTrue(getLeaf(cont1, "leaf2").getDescription().getValue().equals("old description"));
        assertTrue(getLeaf(cont1, "leaf2").getReference().getValue().equals("old reference"));
        assertTrue(getLeaf(cont1, "leaf2").getConfig().isConfigFalse());
        assertTrue(getLeaf(cont1, "leaf2").getDefault().getValue().equals("world"));
        assertTrue(getLeaf(cont1, "leaf2").getMandatory().isMandatoryFalse());
        assertTrue(getLeaf(cont1, "leaf2").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getIfFeatures().get(0).getValue().equals("feature1"));

        assertTrue(getLeaf(cont1, "leaf5").getMusts().size() == 2);
        assertTrue(getLeaf(cont1, "leaf5").getMusts().get(0).getXpathExpression().equals(". > ../leaf3"));
        assertTrue(getLeaf(cont1, "leaf5").getMusts().get(1).getXpathExpression().equals(". > ../leaf4"));

        assertTrue(getLeafList(cont1, "leaflist14").getDefaults().size() == 2);
        assertTrue(getLeafList(cont1, "leaflist14").getDefaults().get(0).getDecimalDefaultValue().compareTo(BigDecimal
                .valueOf(50L)) == 0);
        assertTrue(getLeafList(cont1, "leaflist14").getDefaults().get(1).getDecimalDefaultValue().compareTo(BigDecimal
                .valueOf(60L)) == 0);
        assertTrue(getLeafList(cont1, "leaflist14").getMinElements().getMinValue() == 2);
        assertTrue(getLeafList(cont1, "leaflist14").getMaxElements().getMaxValue() == 8);
        assertTrue(getLeafList(cont1, "leaflist14").getIfFeatures().size() == 2);
        assertTrue(getLeafList(cont1, "leaflist14").getIfFeatures().get(0).getValue().equals("feature1"));
        assertTrue(getLeafList(cont1, "leaflist14").getIfFeatures().get(1).getValue().equals("feature2"));

        assertTrue(getLeafList(cont1, "leaflist15").getDefaults().size() == 3);
        assertTrue(getLeafList(cont1, "leaflist15").getDefaults().get(0).getDecimalDefaultValue().compareTo(BigDecimal
                .valueOf(50L)) == 0);
        assertTrue(getLeafList(cont1, "leaflist15").getDefaults().get(1).getDecimalDefaultValue().compareTo(BigDecimal
                .valueOf(60L)) == 0);
        assertTrue(getLeafList(cont1, "leaflist15").getDefaults().get(2).getDecimalDefaultValue().compareTo(BigDecimal
                .valueOf(70L)) == 0);
        assertTrue(getLeafList(cont1, "leaflist15").getMinElements().getMinValue() == 3);
        assertTrue(getLeafList(cont1, "leaflist15").getMaxElements().getMaxValue() == 9);

        assertTrue(getContainer(cont1, "cont74").getPresence().getValue().equals("meaningful!"));

        // - - - - - - error scenarios - - - - - -

        final YContainer cont2 = getContainer(module, "cont2");

        assertStatementHasFindingOfType(cont2, ParserFindingType.P124_INVALID_REFINE_TARGET_NODE.toString());
        assertStatementHasFindingOfType(cont2, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }
}
