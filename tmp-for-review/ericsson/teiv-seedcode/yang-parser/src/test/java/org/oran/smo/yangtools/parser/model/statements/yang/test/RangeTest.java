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
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class RangeTest extends YangTestCommon {

    @Test
    public void testRangeSimple() {

        parseRelativeImplementsYangModels(Arrays.asList("range-test/range-test-module-simple.yang"));
        assertTrue(findingsManager.getAllFindings().isEmpty());

        final YModule module = getModule("range-test-module-simple");
        assertTrue(module != null);

        // -------------------------- Simple ranges that are fine --------------------

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf1").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getRange() == null);

        assertTrue(getLeaf(cont1, "leaf2").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("127")));

        assertTrue(getLeaf(cont1, "leaf2").getType().getErrorMessageText() == null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getErrorMessage() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getErrorMessage().getErrorMessageText() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getErrorMessageText() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getErrorMessage().getErrorMessageText().equals("wrong!"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getErrorMessageText().equals("wrong!"));

        assertTrue(getLeaf(cont1, "leaf3").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("127")));

        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("127")));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("127")));
        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("int8"));

        assertTrue(getLeaf(cont1, "leaf5").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("127")));
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("127")));

        assertTrue(getLeaf(cont1, "leaf6").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-100")));
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("100")));

        assertTrue(getLeaf(cont1, "leaf7").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("-50")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("-20")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("60")));

        assertTrue(getLeaf(cont1, "leaf8").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("-20")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("127")));

        assertTrue(getLeaf(cont1, "leaf9").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("-20")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("80")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("80")));

        assertTrue(getLeaf(cont1, "leaf10").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-20")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("80")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("80")));

        assertTrue(getLeaf(cont1, "leaf11").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("40")));
        assertTrue(getLeaf(cont1, "leaf11").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("40")));

        assertTrue(getLeaf(cont1, "leaf12").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("70")));
        assertTrue(getLeaf(cont1, "leaf12").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("70")));

        assertTrue(getLeaf(cont1, "leaf13").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().size() == 4);
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("2")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("2")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("3")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("3")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(3).lower.equals(new BigDecimal(
                "127")));
        assertTrue(getLeaf(cont1, "leaf13").getType().getRange().getBoundaries().get(3).upper.equals(new BigDecimal(
                "127")));

    }

    @Test
    public void testRangeTypedefs() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        parseRelativeImplementsYangModels(Arrays.asList("range-test/range-test-module-typedefs.yang"));

        printFindings();

        assertTrue(findingsManager.getAllFindings().isEmpty());

        final YModule module = getModule("range-test-module-typedefs");
        assertTrue(module != null);

        // -------------------------- Simple ranges that are fine --------------------

        assertTrue(getTypedef(module, "typeInt8noRange") != null);
        assertTrue(getTypedef(module, "typeInt8noRange").getType().getDataType().equals("int8"));
        assertTrue(getTypedef(module, "typeInt8noRange").getType().getRange() == null);

        assertTrue(getTypedef(module, "typeInt8WithRange") != null);
        assertTrue(getTypedef(module, "typeInt8WithRange").getType().getDataType().equals("int8"));
        assertTrue(getTypedef(module, "typeInt8WithRange").getType().getRange() != null);
        assertTrue(getTypedef(module, "typeInt8WithRange").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getTypedef(module, "typeInt8WithRange").getType().getRange().getBoundaries().get(0).lower.equals(
                new BigDecimal("10")));
        assertTrue(getTypedef(module, "typeInt8WithRange").getType().getRange().getBoundaries().get(0).upper.equals(
                new BigDecimal("20")));

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf1").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getRange() == null);

        assertTrue(getLeaf(cont1, "leaf2").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-128")));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("127")));

        assertTrue(getLeaf(cont1, "leaf3").getType().getDataType().equals("int64"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal(
                "-9223372036854775808")));
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "9223372036854775807")));

        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("uint8"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("0")));
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("255")));

        assertTrue(getLeaf(cont1, "leaf5").getType().getDataType().equals("uint64"));
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("0")));
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal(
                "18446744073709551615")));

        assertTrue(getLeaf(cont1, "leaf6").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("20")));

        assertTrue(getLeaf(cont1, "leaf7").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("-50")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("-20")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("30")));
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("60")));

        assertTrue(getLeaf(cont1, "leaf8").getType().getDataType().equals("uint64"));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("5")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("5")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).lower.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).upper.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(2).lower.equals(new BigDecimal("70")));
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(2).upper.equals(new BigDecimal("70")));

        assertTrue(getLeaf(cont1, "leaf9").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("10")));
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("20")));

        assertTrue(getLeaf(cont1, "leaf10").getType().getDataType().equals("int8"));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange() != null);
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(0).lower.equals(new BigDecimal("17")));
        assertTrue(getLeaf(cont1, "leaf10").getType().getRange().getBoundaries().get(0).upper.equals(new BigDecimal("19")));
    }

    @Test
    public void testRangeSimpleFaulty() {

        parseRelativeImplementsYangModels(Arrays.asList("range-test/range-test-module-simple-faulty.yang"));

        final YModule module = getModule("range-test-module-simple-faulty");
        assertTrue(module != null);

        // -------------------------- Simple ranges that are faulty for a variety of reasons --------------------

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertStatementHasFindingOfType(getLeaf(cont1, "leaf1").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf2").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf3").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf4").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf7").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf8").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf9").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf10").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf11").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf12").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
    }

    @Test
    public void testRangeTypedefsFaulty() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        parseRelativeImplementsYangModels(Arrays.asList("range-test/range-test-module-typedefs-faulty.yang"));

        final YModule module = getModule("range-test-module-typedefs-faulty");
        assertTrue(module != null);

        // -------------------------- Ranges and typedefs that are incorrect --------------------

        assertNoFindingsOnStatement(getTypedef(module, "typeInt8noRange"));
        assertNoFindingsOnStatement(getTypedef(module, "typeInt64noRange"));
        assertNoFindingsOnStatement(getTypedef(module, "typeUint8noRange"));
        assertNoFindingsOnStatement(getTypedef(module, "typeUint64noRange"));
        assertStatementHasFindingOfType(getTypedef(module, "typeInt8WithRange").getType().getRange(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertNoFindingsOnStatement(getTypedef(module, "typeUsingTypeInt8WithRange"));

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertStatementHasFindingOfType(getLeaf(cont1, "leaf1").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf2").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf3").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf4").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getType().getRange(), ParserFindingType.P053_INVALID_VALUE
                .toString());
    }

}
