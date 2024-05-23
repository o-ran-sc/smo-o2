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
import org.oran.smo.yangtools.parser.model.statements.threegpp.C3GPP;
import org.oran.smo.yangtools.parser.model.statements.threegpp.Y3gppInitialValue;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviate.DeviateType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviation;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;

public class DeviationTest extends YangTestCommon {

    @Test
    public void testDeviateAdd() {

        parseImplementsYangModels(Arrays.asList("deviation-test/deviation-host-test-module.yang",
                "deviation-test/deviate-add-test-module.yang"), Arrays.asList(THREEGPP_YANG_EXT_PATH));

        final YModule hostModule = getModule("deviation-host-test-module");
        final YModule deviateAddModule = getModule("deviate-add-test-module");

        // ------------------------- All of these are just fine -------------------------------

        assertTrue(getContainer(hostModule, "cont1") != null);
        assertSubTreeNoFindings(getContainer(hostModule, "cont1"));

        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getType().getDataType().equals("string"));
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getDefault() != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getDefault().getValue().equals("Hello World!"));
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getMusts().size() == 1);
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getMusts().get(0).getXpathExpression().equals(
                "strlen(.) > 5"));
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getUnits() != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getUnits().getValue().equals("seconds"));
        assertTrue(getLeafUnderContainer(hostModule, "cont1", "leaf11").getChild(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE) != null);
        assertTrue(((Y3gppInitialValue) getLeafUnderContainer(hostModule, "cont1", "leaf11").getChild(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE)).getInitialValue().equals("Hello World"));

        assertTrue(getList(hostModule, "list11") != null);
        assertSubTreeNoFindings(getList(hostModule, "list11"));

        assertTrue(getList(hostModule, "list11").getUniques().size() == 1);
        assertTrue(getList(hostModule, "list11").getUniques().get(0).getValue().equals("leaf111 leaf112"));

        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113") != null);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().size() == 3);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(0).getValue().equals("10"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(1).getValue().equals("13"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(2).getValue().equals("18"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().size() == 2);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().get(0).getXpathExpression().equals(
                ". > 4"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().get(1).getXpathExpression().equals(
                ". < 98"));

        assertSubTreeNoFindings(getChild(deviateAddModule, "deviation", "/host:cont1/host:leaf11"));
        assertSubTreeNoFindings(getChild(deviateAddModule, "deviation", "/host:list11"));
        assertSubTreeNoFindings(getChild(deviateAddModule, "deviation", "/host:list11/host:leaflist113"));

        // ------------------------- Trying to add things that already exist -------------------------------

        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getType().getDataType().equals("int16"));
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getConfig().getValue().equals("false"));
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getMandatory().isMandatoryTrue());
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getUnits().getValue().equals("minutes"));

        final YDeviation deviationCont2Leaf21 = (YDeviation) getChild(deviateAddModule, "deviation",
                "/host:cont2/host:leaf21");
        assertTrue(deviationCont2Leaf21 != null);
        assertTrue(deviationCont2Leaf21.getDeviates().size() == 1);
        assertTrue(deviationCont2Leaf21.getDeviates().get(0).getDeviateType() == DeviateType.ADD);

        assertTrue(deviationCont2Leaf21.getDeviates().get(0).getType().getDataType().equals("string"));
        assertStatementHasFindingOfType(deviationCont2Leaf21.getDeviates().get(0).getType(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());
        assertStatementHasFindingOfType(deviationCont2Leaf21.getDeviates().get(0).getConfig(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());
        assertStatementHasFindingOfType(deviationCont2Leaf21.getDeviates().get(0).getMandatory(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());
        assertStatementHasFindingOfType(deviationCont2Leaf21.getDeviates().get(0).getUnits(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        final YDeviation deviationList21 = (YDeviation) getChild(deviateAddModule, "deviation", "/host:list21");
        assertTrue(deviationList21 != null);
        assertTrue(deviationList21.getDeviates().size() == 1);
        assertTrue(deviationList21.getDeviates().get(0).getDeviateType() == DeviateType.ADD);

        assertTrue(deviationList21.getDeviates().get(0).getMinElements().getMinValue() == 67);
        assertStatementHasFindingOfType(deviationList21.getDeviates().get(0).getMinElements(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());
        assertTrue(deviationList21.getDeviates().get(0).getMaxElements().getMaxValue() == 82);
        assertStatementHasFindingOfType(deviationList21.getDeviates().get(0).getMaxElements(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        // ------------------------- Various other findings -------------------------------

        final YDeviation deviationUnknownElement = (YDeviation) getChild(deviateAddModule, "deviation",
                "/host:unknownelement");
        assertTrue(deviationUnknownElement != null);
        assertStatementHasFindingOfType(deviationUnknownElement, ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        final YDeviation deviationCont4 = (YDeviation) getChild(deviateAddModule, "deviation", "/host:cont4");
        assertTrue(deviationCont4 != null);
        assertStatementHasFindingOfType(deviationCont4.getDeviates().get(0),
                ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());

        final YDeviation deviationCont3 = (YDeviation) getChild(deviateAddModule, "deviation", "/host:cont3");
        assertTrue(deviationCont3 != null);
        assertStatementHasFindingOfType(deviationCont3.getDeviates().get(0).getMinElements(),
                ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
        assertStatementHasFindingOfType(deviationCont3.getDeviates().get(0).getMaxElements(),
                ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());

        final YDeviation deviationList11Leaf112 = (YDeviation) getChild(deviateAddModule, "deviation",
                "/host:list11/host:leaf112");
        assertTrue(deviationList11Leaf112 != null);
        assertStatementHasFindingOfType(deviationList11Leaf112.getDeviates().get(0).getDefaults().get(0),
                ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION.toString());
    }

    @Test
    public void testDeviateReplace() {

        parseImplementsYangModels(Arrays.asList("deviation-test/deviation-host-test-module.yang",
                "deviation-test/deviate-replace-test-module.yang"), Arrays.asList(THREEGPP_YANG_EXT_PATH));

        final YModule hostModule = getModule("deviation-host-test-module");
        final YModule deviateReplaceModule = getModule("deviate-replace-test-module");

        // ------------------------- All of these are just fine -------------------------------

        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getType().getDataType().equals("int16"));
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getConfig().getValue().equals("true"));
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getMandatory().isMandatoryFalse());
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getUnits().getValue().equals("days"));
        assertSubTreeNoFindings(getContainer(hostModule, "cont2"));

        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113") != null);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getType().getDataType().equals("int16"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().size() == 2);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(0).getValue().equals(
                "1234"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(1).getValue().equals(
                "7890"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().size() == 1);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().get(0).getXpathExpression().equals(
                ". > 4"));
        assertTrue(((Y3gppInitialValue) getLeafListUnderList(hostModule, "list11", "leaflist113").getChild(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE)).getValue().equals("70"));
        assertSubTreeNoFindings(getLeafListUnderList(hostModule, "list11", "leaflist113"));

        assertTrue(getList(hostModule, "list21") != null);
        assertTrue(getList(hostModule, "list21").getMinElements().getMinValue() == 2);
        assertTrue(getList(hostModule, "list21").getMaxElements().getMaxValue() == 36);
        assertSubTreeNoFindings(getList(hostModule, "list21"));

        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51").getType().getDataType().equals("string"));
        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51").getType().getLength() != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51").getType().getLength().getBoundaries().get(
                0).lower == 10);
        assertTrue(getLeafUnderContainer(hostModule, "cont5", "leaf51").getType().getLength().getBoundaries().get(
                0).upper == 40);
        assertSubTreeNoFindings(getLeafUnderContainer(hostModule, "cont5", "leaf51"));

        assertSubTreeNoFindings(getChild(deviateReplaceModule, "deviation", "/host:cont2/host:leaf21"));
        assertSubTreeNoFindings(getChild(deviateReplaceModule, "deviation", "/host:list11/host:leaflist113"));
        assertSubTreeNoFindings(getChild(deviateReplaceModule, "deviation", "/host:list21"));
        assertSubTreeNoFindings(getChild(deviateReplaceModule, "deviation", "/host:cont5/host:leaf51"));

        // ------------------------------- Trying to replace things that don't exist -----------------------

        final YDeviation deviationCont1Leaf11 = (YDeviation) getChild(deviateReplaceModule, "deviation",
                "/host:cont1/host:leaf11");
        assertTrue(deviationCont1Leaf11 != null);
        assertTrue(deviationCont1Leaf11.getDeviates().size() == 1);
        assertTrue(deviationCont1Leaf11.getDeviates().get(0).getDeviateType() == DeviateType.REPLACE);
        assertStatementHasFindingOfType(deviationCont1Leaf11.getDeviates().get(0).getUnits(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        // ---------------- Various other findings ---------------------

        final YDeviation deviationCont4 = (YDeviation) getChild(deviateReplaceModule, "deviation", "/host:cont4");
        assertTrue(deviationCont4 != null);
        assertStatementHasFindingOfType(deviationCont4.getDeviates().get(0),
                ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());

        final YDeviation deviationList11Leaf112 = (YDeviation) getChild(deviateReplaceModule, "deviation",
                "/host:list11/host:leaf112");
        assertTrue(deviationList11Leaf112 != null);
        assertStatementHasFindingOfType(deviationList11Leaf112.getDeviates().get(0).getType(),
                ParserFindingType.P057_DATA_TYPE_CHANGED.toString());

        final YDeviation deviationList22 = (YDeviation) getChild(deviateReplaceModule, "deviation", "/host:list22");
        assertTrue(deviationList22 != null);
        assertStatementHasFindingOfType(deviationList22.getDeviates().get(0).getMinElements(),
                ParserFindingType.P056_CONSTRAINT_NARROWED.toString());
        assertStatementHasFindingOfType(deviationList22.getDeviates().get(0).getMaxElements(),
                ParserFindingType.P056_CONSTRAINT_NARROWED.toString());

        final YDeviation deviationList11Leaf114 = (YDeviation) getChild(deviateReplaceModule, "deviation",
                "/host:list11/host:leaf114");
        assertTrue(deviationList11Leaf114 != null);
        assertStatementHasFindingOfType(deviationList11Leaf114.getDeviates().get(0).getDefaults().get(0),
                ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION.toString());
    }

    @Test
    public void testDeviateDelete() {

        parseImplementsYangModels(Arrays.asList("deviation-test/deviation-host-test-module.yang",
                "deviation-test/deviate-delete-test-module.yang"), Arrays.asList(THREEGPP_YANG_EXT_PATH));

        final YModule hostModule = getModule("deviation-host-test-module");
        final YModule deviateDeleteModule = getModule("deviate-delete-test-module");

        // ------------------------- All of these are just fine -------------------------------

        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getType().getDataType().equals("int16"));
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getConfig() != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getMandatory() != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21").getUnits() == null);
        assertSubTreeNoFindings(getLeafUnderContainer(hostModule, "cont2", "leaf21"));

        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113") != null);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getType().getDataType().equals("int16"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().size() == 2);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(0).getValue().equals("10"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getDefaults().get(1).getValue().equals("13"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().size() == 1);
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getMusts().get(0).getXpathExpression().equals(
                ". > 4"));
        assertTrue(getLeafListUnderList(hostModule, "list11", "leaflist113").getChild(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE) == null);
        assertSubTreeNoFindings(getLeafListUnderList(hostModule, "list11", "leaflist113"));

        assertSubTreeNoFindings(getChild(deviateDeleteModule, "deviation", "/host:cont2/host:leaf21"));
        assertSubTreeNoFindings(getChild(deviateDeleteModule, "deviation", "/host:list11/host:leaflist113"));

        // ---------------- These deviations try to delete things that don't exist in the host model ---------------------

        final YDeviation deviationCont1Leaf11 = (YDeviation) getChild(deviateDeleteModule, "deviation",
                "/host:cont1/host:leaf11");
        assertTrue(deviationCont1Leaf11 != null);
        assertTrue(deviationCont1Leaf11.getDeviates().size() == 1);
        assertTrue(deviationCont1Leaf11.getDeviates().get(0).getDeviateType() == DeviateType.DELETE);
        assertStatementHasFindingOfType(deviationCont1Leaf11.getDeviates().get(0).getConfig(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        final YDeviation deviationList11 = (YDeviation) getChild(deviateDeleteModule, "deviation", "/host:list11");
        assertTrue(deviationList11 != null);
        assertStatementHasFindingOfType(deviationList11.getDeviates().get(0).getMinElements(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());
        assertStatementHasFindingOfType(deviationList11.getDeviates().get(0).getMaxElements(),
                ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        // ---------------- Various other findings ---------------------

        final YDeviation deviationCont4 = (YDeviation) getChild(deviateDeleteModule, "deviation", "/host:cont4");
        assertTrue(deviationCont4 != null);
        assertStatementHasFindingOfType(deviationCont4.getDeviates().get(0),
                ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());

        final YDeviation deviationList11Leaf112 = (YDeviation) getChild(deviateDeleteModule, "deviation",
                "/host:list11/host:leaf112");
        assertTrue(deviationList11Leaf112 != null);
        assertStatementHasFindingOfType(deviationList11Leaf112.getDeviates().get(0).getType(),
                ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION.toString());
    }

    @Test
    public void testDeviateNotSupported() {

        parseImplementsYangModels(Arrays.asList("deviation-test/deviation-host-test-module.yang",
                "deviation-test/deviate-not-supported-test-module.yang"), Arrays.asList(THREEGPP_YANG_EXT_PATH));

        final YModule hostModule = getModule("deviation-host-test-module");
        final YModule deviateNotSupportedModule = getModule("deviate-not-supported-test-module");

        // ------------------------- All of these are just fine -------------------------------

        assertTrue(getContainer(hostModule, "cont2") != null);
        assertTrue(getLeafUnderContainer(hostModule, "cont2", "leaf21") == null);
        assertTrue(getList(hostModule, "list21") == null);
        assertTrue(getList(hostModule, "cont1") == null);

        assertSubTreeNoFindings(getChild(deviateNotSupportedModule, "deviation", "/host:cont2/host:leaf21"));
        assertSubTreeNoFindings(getChild(deviateNotSupportedModule, "deviation", "/host:list21"));
        assertSubTreeNoFindings(getChild(deviateNotSupportedModule, "deviation", "/host:cont1"));

        // ---------------- These deviations try to not-support things that don't exist in the host model ---------------------

        final YDeviation deviationUnknownElement = (YDeviation) getChild(deviateNotSupportedModule, "deviation",
                "/host:unknownElement");
        assertTrue(deviationUnknownElement != null);
        assertTrue(deviationUnknownElement.getDeviates().size() == 1);
        assertTrue(deviationUnknownElement.getDeviates().get(0).getDeviateType() == DeviateType.NOT_SUPPORTED);
        assertStatementHasFindingOfType(deviationUnknownElement, ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        final YDeviation deviationCont3UnknownElement = (YDeviation) getChild(deviateNotSupportedModule, "deviation",
                "/host:cont3/host:unknownElement");
        assertTrue(deviationCont3UnknownElement != null);
        assertStatementHasFindingOfType(deviationCont3UnknownElement, ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        // ---------------- Various other findings ---------------------

        final YDeviation deviationCont4 = (YDeviation) getChild(deviateNotSupportedModule, "deviation", "/host:cont4");
        assertTrue(deviationCont4 != null);
        assertStatementHasFindingOfType(deviationCont4.getDeviates().get(0).getType(),
                ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
    }

    @Test
    public void testDeviateOthers() {

        severityCalculator.suppressFinding(ParserFindingType.P152_AUGMENT_TARGET_NODE_IN_SAME_MODULE.toString());

        parseImplementsYangModels(Arrays.asList("deviation-test/deviate-other-tests-module.yang"), Arrays.asList(
                THREEGPP_YANG_EXT_PATH));

        final YModule module = getModule("deviate-other-tests-module");
        assertTrue(module != null);

        // ---------------- Deviate something augmented ---------------------

        assertTrue(getContainer(module, "cont01") != null);
        assertSubTreeNoFindings(getContainer(module, "cont01"));

        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11") != null);
        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11").getType().getDataType().equals("string"));
        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11").getMandatory() != null);
        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11").getMandatory().isMandatoryTrue());
        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11").getConfig() != null);
        assertTrue(getLeafUnderContainer(module, "cont01", "leaf11").getConfig().getValue().equals("false"));
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12") != null);
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12").getType().getDataType().equals("int16"));
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12").getMinElements() != null);
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12").getMinElements().getMinValue() == 20);
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12").getMaxElements() != null);
        assertTrue(getLeafListUnderContainer(module, "cont01", "leaflist12").getMaxElements().getMaxValue() == 25);

        assertStatementHasSingleFindingOfType(getChild(module, "deviation", "/this:cont01/this:leaf11"),
                ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());
        assertStatementHasSingleFindingOfType(getChild(module, "deviation", "/this:cont01/this:leaflist12"),
                ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());

        // ---------------- Deviate-replace something that was deviate-added ---------------------

        assertTrue(getContainer(module, "cont02") != null);
        assertSubTreeNoFindings(getContainer(module, "cont02"));

        assertTrue(getContainer(module, "cont02").getConfig() != null);
        assertTrue(getContainer(module, "cont02").getConfig().getValue().equals("true"));

        assertStatementHasFindingOfType(getChild(module, "deviation", "/this:cont02"),
                ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());
        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont02")).getDeviates().get(1)
                .getConfig(), ParserFindingType.P164_DEVIATE_REPLACE_OF_DEVIATE_ADDED_STATEMENT.toString());

        // ---------------- Deviate-add something that was deviate-added ---------------------

        assertTrue(getContainer(module, "cont03") != null);
        assertSubTreeNoFindings(getContainer(module, "cont03"));

        assertTrue(getContainer(module, "cont03").getConfig() != null);
        assertTrue(getContainer(module, "cont03").getConfig().getValue().equals("false"));

        assertStatementHasFindingOfType(getChild(module, "deviation", "/this:cont03"),
                ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());
        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont03")).getDeviates().get(1)
                .getConfig(), ParserFindingType.P161_INVALID_DEVIATE_OPERATION.toString());

        // ---------------- Deviate-replace something that was deviate-replaced ---------------------

        assertTrue(getContainer(module, "cont04") != null);
        assertSubTreeNoFindings(getContainer(module, "cont04"));

        assertTrue(getContainer(module, "cont04").getConfig() != null);
        assertTrue(getContainer(module, "cont04").getConfig().getValue().equals("true"));

        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont04")).getDeviates().get(1)
                .getConfig(), ParserFindingType.P163_AMBIGUOUS_DEVIATE_REPLACE_OF_SAME_STATEMENT.toString());

        // ---------------- Deviate-delete something that was deviate-replaced ---------------------

        assertTrue(getContainer(module, "cont05") != null);
        assertSubTreeNoFindings(getContainer(module, "cont05"));

        assertTrue(getContainer(module, "cont05").getConfig() == null);

        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont05")).getDeviates().get(1)
                .getConfig(), ParserFindingType.P165_DEVIATE_DELETE_OF_DEVIATED_STATEMENT.toString());

        // ---------------- Deviate-delete something that was deviate-added ---------------------

        assertTrue(getContainer(module, "cont06") != null);
        assertSubTreeNoFindings(getContainer(module, "cont06"));

        assertTrue(getContainer(module, "cont06").getConfig() == null);

        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont06")).getDeviates().get(1)
                .getConfig(), ParserFindingType.P165_DEVIATE_DELETE_OF_DEVIATED_STATEMENT.toString());

        // ---------------- Some error scenarios ---------------------

        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont07")).getDeviates().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertStatementHasFindingOfType(((YDeviation) getChild(module, "deviation", "/this:cont08")).getDeviates().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertStatementHasFindingOfType(getChild(module, "deviation", "//this:cont08"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertStatementHasNotFindingOfType(getChild(module, "deviation", "/this:cont09/"),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());

        assertStatementHasFindingOfType(getChild(module, "deviation", "/"), ParserFindingType.P054_UNRESOLVABLE_PATH
                .toString());

        assertStatementHasFindingOfType(getChild(module, "deviation", ""), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        assertStatementHasFindingOfType(getChild(module, "deviation", null),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertStatementHasNotFindingOfType(getChild(module, "deviation", "  /this:cont10  "),
                ParserFindingType.P054_UNRESOLVABLE_PATH.toString());
    }

    @Test
    public void testDeviateNotSupportedMultiLevel() {

        severityCalculator.suppressFinding(ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());

        parseImplementsYangModels(Arrays.asList("deviation-test/deviate-not-supported-multi-level-test-module.yang"), Arrays
                .asList(THREEGPP_YANG_EXT_PATH));

        final YModule module = getModule("deviate-not-supported-multi-level-test-module");
        assertTrue(module != null);

        assertNoFindings();

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YContainer cont2 = getContainer(cont1, "cont2");
        assertTrue(cont2 == null);
    }
}
