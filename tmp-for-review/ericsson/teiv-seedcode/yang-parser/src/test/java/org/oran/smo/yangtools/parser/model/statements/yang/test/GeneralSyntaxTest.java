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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement.StatementArgumentType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YCase;
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YLength;
import org.oran.smo.yangtools.parser.model.statements.yang.YList;
import org.oran.smo.yangtools.parser.model.statements.yang.YLength.BoundaryPair;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YOrderedBy;
import org.oran.smo.yangtools.parser.model.statements.yang.YRange;
import org.oran.smo.yangtools.parser.model.statements.yang.YRpc;

public class GeneralSyntaxTest extends YangTestCommon {

    @Test
    public void testGeneralSyntaxRpc() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        parseRelativeImplementsYangModels(Arrays.asList("general-syntax-test/general-syntax-test-rpc.yang"));

        assertNoFindings();

        final YModule yModule = getModule("general-syntax-test-rpc");
        assertTrue(yModule != null);

        // -------------- RPCs -------------------

        final YRpc rpc1 = getChild(yModule, CY.RPC, "rpc1");
        assertTrue(rpc1 != null);

        final YRpc rpc2 = getChild(yModule, CY.RPC, "rpc2");
        assertTrue(rpc2 != null);
        assertTrue(rpc2.getInput() != null);
        assertTrue(rpc2.getOutput() != null);		// missing in YAM, auto-created by parser
        assertTrue(rpc2.getInput().getContainers().get(0) != null);
        assertTrue(rpc2.getInput().getContainers().get(0).getStatementIdentifier().equals("cont1"));

        final YRpc rpc3 = getChild(yModule, CY.RPC, "rpc3");
        assertTrue(rpc3 != null);
        assertTrue(rpc3.getInput() != null);		// missing in YAM, auto-created by parser
        assertTrue(rpc3.getOutput() != null);
        assertTrue(rpc3.getOutput().getContainers().get(0) != null);
        assertTrue(rpc3.getOutput().getContainers().get(0).getStatementIdentifier().equals("cont2"));

        final YRpc rpc4 = getChild(yModule, CY.RPC, "rpc4");
        assertTrue(rpc4 != null);
        assertTrue(rpc4.getStatus() != null);
        assertTrue(rpc4.getStatus().isDeprecated());
        assertTrue(rpc4.getIfFeatures().get(0) != null);
        assertTrue(rpc4.getIfFeatures().get(0).getValue().equals("feature1"));
        assertTrue(rpc4.getTypedefs().get(0) != null);
        assertTrue(rpc4.getTypedefs().get(0).getTypedefName().equals("typedef1"));
        assertTrue(rpc4.getGroupings().get(0) != null);
        assertTrue(rpc4.getGroupings().get(0).getGroupingName().equals("grouping1"));

        assertTrue(rpc4.getOutput() != null);
        assertTrue(rpc4.getOutput().getLeafs().get(0) != null);
        assertTrue(rpc4.getOutput().getLeafs().get(0).getType().getDataType().equals("string"));
        assertTrue(rpc4.getOutput().getAnyxmls().get(0) != null);
        assertTrue(rpc4.getOutput().getAnyxmls().get(0).getStatementIdentifier().equals("anyxml1"));
        assertTrue(rpc4.getOutput().getAnydata().get(0) != null);
        assertTrue(rpc4.getOutput().getAnydata().get(0).getStatementIdentifier().equals("anydata1"));
        assertTrue(rpc4.getOutput().getLists().get(0) != null);
        assertTrue(rpc4.getOutput().getLists().get(0).getStatementIdentifier().equals("list1"));
        assertTrue(rpc4.getOutput().getLeafLists().get(0) != null);
        assertTrue(rpc4.getOutput().getLeafLists().get(0).getStatementIdentifier().equals("leaflist3"));
        assertTrue(rpc4.getOutput().getChoices().get(0) != null);
        assertTrue(rpc4.getOutput().getChoices().get(0).getStatementIdentifier().equals("choice1"));
        assertTrue(rpc4.getOutput().getMusts().get(0) != null);
        assertTrue(rpc4.getOutput().getMusts().get(0).getXpathExpression().equals("../contX"));
        assertTrue(rpc4.getOutput().getTypedefs().get(0) != null);
        assertTrue(rpc4.getOutput().getTypedefs().get(0).getTypedefName().equals("typedef2"));

        assertTrue(rpc4.getInput() != null);
        assertTrue(rpc4.getInput().getContainers().get(0) != null);
        assertTrue(rpc4.getInput().getContainers().get(0).getContainerName().equals("cont4"));
        assertTrue(rpc4.getInput().getLeafs().get(0) != null);
        assertTrue(rpc4.getInput().getLeafs().get(0).getLeafName().equals("grouping-leaf"));
        assertTrue(rpc4.getInput().getAnyxmls().get(0) != null);
        assertTrue(rpc4.getInput().getAnyxmls().get(0).getStatementIdentifier().equals("anyxml1"));
        assertTrue(rpc4.getInput().getAnydata().get(0) != null);
        assertTrue(rpc4.getInput().getAnydata().get(0).getStatementIdentifier().equals("anydata1"));
        assertTrue(rpc4.getInput().getLists().get(0) != null);
        assertTrue(rpc4.getInput().getLists().get(0).getStatementIdentifier().equals("list1"));
        assertTrue(rpc4.getInput().getLeafLists().get(0) != null);
        assertTrue(rpc4.getInput().getLeafLists().get(0).getStatementIdentifier().equals("leaflist3"));
        assertTrue(rpc4.getInput().getChoices().get(0) != null);
        assertTrue(rpc4.getInput().getChoices().get(0).getStatementIdentifier().equals("choice1"));
        assertTrue(rpc4.getInput().getMusts().get(0) != null);
        assertTrue(rpc4.getInput().getMusts().get(0).getXpathExpression().equals("../contX"));
        assertTrue(rpc4.getInput().getTypedefs().get(0) != null);
        assertTrue(rpc4.getInput().getTypedefs().get(0).getTypedefName().equals("typedef2"));
    }

    @Test
    public void testGeneralSyntaxConstraints() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        parseRelativeImplementsYangModels(Arrays.asList("general-syntax-test/general-syntax-test-constraints.yang"));

        final YModule module = getModule("general-syntax-test-constraints");
        assertTrue(module != null);

        // -------------- Length -------------------

        {
            final YContainer cont1 = getContainer(module, "cont1");

            final YLeaf leaf1 = getLeaf(cont1, "leaf1");
            final YLength length1 = leaf1.getType().getLength();

            assertTrue(length1.getErrorAppTag() != null);
            assertTrue(length1.getErrorAppTag().getValue().equals("some-app-tag"));
            assertTrue(length1.getErrorMessage() != null);
            assertTrue(length1.getErrorMessage().getValue().equals("wrong length"));
            assertNoFindingsOnStatement(getLeaf(cont1, "leaf1").getType().getLength());

            final List<BoundaryPair> boundaries1 = length1.getBoundaries();
            assertTrue(boundaries1.size() == 1);
            assertTrue(boundaries1.get(0).lower == 1L);
            assertTrue(boundaries1.get(0).upper == 10L);

            final List<BoundaryPair> boundaries2 = getLeaf(cont1, "leaf2").getType().getLength().getBoundaries();
            assertTrue(boundaries2.size() == 1);
            assertTrue(boundaries2.get(0).lower == 0L);
            assertTrue(boundaries2.get(0).upper == Long.MAX_VALUE);
            assertNoFindingsOnStatement(getLeaf(cont1, "leaf2").getType().getLength());

            final List<BoundaryPair> boundaries3 = getLeaf(cont1, "leaf3").getType().getLength().getBoundaries();
            assertTrue(boundaries3.size() == 3);
            assertTrue(boundaries3.get(0).lower == 0L);
            assertTrue(boundaries3.get(0).upper == 10L);
            assertTrue(boundaries3.get(1).lower == 15L);
            assertTrue(boundaries3.get(1).upper == 15L);
            assertTrue(boundaries3.get(2).lower == 20L);
            assertTrue(boundaries3.get(2).upper == Long.MAX_VALUE);
            assertNoFindingsOnStatement(getLeaf(cont1, "leaf3").getType().getLength());

            assertTrue(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(0));
            assertTrue(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(10));
            assertTrue(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(15));
            assertTrue(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(20));
            assertTrue(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(40404040404L));

            assertFalse(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(-1));
            assertFalse(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(11));
            assertFalse(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(14));
            assertFalse(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(16));
            assertFalse(getLeaf(cont1, "leaf3").getType().getLength().isWithinLengthBoundaries(19));

            final List<BoundaryPair> boundaries4 = getLeaf(cont1, "leaf4").getType().getLength().getBoundaries();
            assertTrue(boundaries4.size() == 2);
            assertTrue(boundaries4.get(0).lower == 0L);
            assertTrue(boundaries4.get(0).upper == 0L);
            assertTrue(boundaries4.get(1).lower == Long.MAX_VALUE);
            assertTrue(boundaries4.get(1).upper == Long.MAX_VALUE);
            assertNoFindingsOnStatement(getLeaf(cont1, "leaf4").getType().getLength());

            final List<BoundaryPair> boundaries5 = getLeaf(cont1, "leaf5").getType().getLength().getBoundaries();
            assertTrue(boundaries5.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries6 = getLeaf(cont1, "leaf6").getType().getLength().getBoundaries();
            assertTrue(boundaries6.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries7 = getLeaf(cont1, "leaf7").getType().getLength().getBoundaries();
            assertTrue(boundaries7.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf7").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries8 = getLeaf(cont1, "leaf8").getType().getLength().getBoundaries();
            assertTrue(boundaries8.size() == 2);
            assertTrue(boundaries8.get(0).lower == 10L);
            assertTrue(boundaries8.get(0).upper == 20L);
            assertTrue(boundaries8.get(1).lower == 21L);
            assertTrue(boundaries8.get(1).upper == 30L);
            assertNoFindingsOnStatement(getLeaf(cont1, "leaf8").getType().getLength());

            final List<BoundaryPair> boundaries9 = getLeaf(cont1, "leaf9").getType().getLength().getBoundaries();
            assertTrue(boundaries9.size() == 2);
            assertTrue(boundaries9.get(0).lower == 10L);
            assertTrue(boundaries9.get(0).upper == 20L);
            assertTrue(boundaries9.get(1).lower == 20L);
            assertTrue(boundaries9.get(1).upper == 30L);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf9").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries10 = getLeaf(cont1, "leaf10").getType().getLength().getBoundaries();
            assertTrue(boundaries10.size() == 2);
            assertTrue(boundaries10.get(0).lower == 19L);
            assertTrue(boundaries10.get(0).upper == 30L);
            assertTrue(boundaries10.get(1).lower == 10L);
            assertTrue(boundaries10.get(1).upper == 20L);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf10").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries11 = getLeaf(cont1, "leaf11").getType().getLength().getBoundaries();
            assertTrue(boundaries11.size() == 1);
            assertTrue(boundaries11.get(0).lower == -1L);
            assertTrue(boundaries11.get(0).upper == -3L);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf11").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries12 = getLeaf(cont1, "leaf12").getType().getLength().getBoundaries();
            assertTrue(boundaries12.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf12").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<BoundaryPair> boundaries13 = getLeaf(cont1, "leaf13").getType().getLength().getBoundaries();
            assertTrue(boundaries13.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont1, "leaf13").getType().getLength(),
                    ParserFindingType.P053_INVALID_VALUE.toString());
        }

        // -------------- Range for integer -------------------

        {
            final YContainer cont2 = getContainer(module, "cont2");
            assertTrue(cont2 != null);

            final YLeaf leaf1 = getLeaf(cont2, "leaf1");
            assertTrue(leaf1 != null);
            final YRange range1 = leaf1.getType().getRange();
            assertTrue(range1 != null);

            assertTrue(range1.getErrorAppTag() != null);
            assertTrue(range1.getErrorAppTag().getValue().equals("some-app-tag"));
            assertTrue(range1.getErrorMessage() != null);
            assertTrue(range1.getErrorMessage().getValue().equals("wrong length"));
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf1").getType().getRange());

            final List<YRange.BoundaryPair> boundaries1 = range1.getBoundaries();
            assertTrue(boundaries1.size() == 1);
            assertTrue(boundaries1.get(0).lower.compareTo(BigDecimal.valueOf(1L)) == 0);
            assertTrue(boundaries1.get(0).upper.compareTo(BigDecimal.valueOf(10L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf1").getType().getRange());

            assertTrue(getLeaf(cont2, "leaf1").getType().getRange().isWithinRangeBoundaries(BigDecimal.valueOf(5L)));
            assertFalse(getLeaf(cont2, "leaf1").getType().getRange().isWithinRangeBoundaries(BigDecimal.valueOf(-20L)));
            assertFalse(getLeaf(cont2, "leaf1").getType().getRange().isWithinRangeBoundaries(BigDecimal.valueOf(90L)));

            final List<YRange.BoundaryPair> boundaries2 = getLeaf(cont2, "leaf2").getType().getRange().getBoundaries();
            assertTrue(boundaries2.get(0).lower.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries2.get(0).upper.compareTo(BigDecimal.valueOf(255L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf2").getType().getRange());

            final List<YRange.BoundaryPair> boundaries3 = getLeaf(cont2, "leaf3").getType().getRange().getBoundaries();
            assertTrue(boundaries3.get(0).lower.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries3.get(0).upper.compareTo(BigDecimal.valueOf(65535L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf3").getType().getRange());

            final List<YRange.BoundaryPair> boundaries4 = getLeaf(cont2, "leaf4").getType().getRange().getBoundaries();
            assertTrue(boundaries4.get(0).lower.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries4.get(0).upper.compareTo(BigDecimal.valueOf(4294967295L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf4").getType().getRange());

            final List<YRange.BoundaryPair> boundaries5 = getLeaf(cont2, "leaf5").getType().getRange().getBoundaries();
            assertTrue(boundaries5.get(0).lower.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries5.get(0).upper.compareTo(new BigDecimal("18446744073709551615")) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf5").getType().getRange());

            final List<YRange.BoundaryPair> boundaries6 = getLeaf(cont2, "leaf6").getType().getRange().getBoundaries();
            assertTrue(boundaries6.get(0).lower.compareTo(BigDecimal.valueOf(-128L)) == 0);
            assertTrue(boundaries6.get(0).upper.compareTo(BigDecimal.valueOf(127L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf6").getType().getRange());

            final List<YRange.BoundaryPair> boundaries7 = getLeaf(cont2, "leaf7").getType().getRange().getBoundaries();
            assertTrue(boundaries7.get(0).lower.compareTo(BigDecimal.valueOf(-32768L)) == 0);
            assertTrue(boundaries7.get(0).upper.compareTo(BigDecimal.valueOf(32767L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf7").getType().getRange());

            final List<YRange.BoundaryPair> boundaries8 = getLeaf(cont2, "leaf8").getType().getRange().getBoundaries();
            assertTrue(boundaries8.get(0).lower.compareTo(BigDecimal.valueOf(-2147483648L)) == 0);
            assertTrue(boundaries8.get(0).upper.compareTo(BigDecimal.valueOf(2147483647L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf8").getType().getRange());

            final List<YRange.BoundaryPair> boundaries9 = getLeaf(cont2, "leaf9").getType().getRange().getBoundaries();
            assertTrue(boundaries9.get(0).lower.compareTo(BigDecimal.valueOf(-9223372036854775808L)) == 0);
            assertTrue(boundaries9.get(0).upper.compareTo(BigDecimal.valueOf(9223372036854775807L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf9").getType().getRange());

            final List<YRange.BoundaryPair> boundaries10 = getLeaf(cont2, "leaf10").getType().getRange().getBoundaries();
            assertTrue(boundaries10.size() == 1);
            assertTrue(boundaries10.get(0).lower.compareTo(BigDecimal.valueOf(10L)) == 0);
            assertTrue(boundaries10.get(0).upper.compareTo(BigDecimal.valueOf(20L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf10").getType().getRange());

            final List<YRange.BoundaryPair> boundaries11 = getLeaf(cont2, "leaf11").getType().getRange().getBoundaries();
            assertTrue(boundaries11.size() == 2);
            assertTrue(boundaries11.get(0).lower.compareTo(BigDecimal.valueOf(10L)) == 0);
            assertTrue(boundaries11.get(0).upper.compareTo(BigDecimal.valueOf(20L)) == 0);
            assertTrue(boundaries11.get(1).lower.compareTo(BigDecimal.valueOf(30L)) == 0);
            assertTrue(boundaries11.get(1).upper.compareTo(BigDecimal.valueOf(40L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf11").getType().getRange());

            final List<YRange.BoundaryPair> boundaries12 = getLeaf(cont2, "leaf12").getType().getRange().getBoundaries();
            assertTrue(boundaries12.size() == 3);
            assertTrue(boundaries12.get(0).lower.compareTo(BigDecimal.valueOf(10L)) == 0);
            assertTrue(boundaries12.get(0).upper.compareTo(BigDecimal.valueOf(20L)) == 0);
            assertTrue(boundaries12.get(1).lower.compareTo(BigDecimal.valueOf(25L)) == 0);
            assertTrue(boundaries12.get(1).upper.compareTo(BigDecimal.valueOf(25L)) == 0);
            assertTrue(boundaries12.get(2).lower.compareTo(BigDecimal.valueOf(30L)) == 0);
            assertTrue(boundaries12.get(2).upper.compareTo(BigDecimal.valueOf(40L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf12").getType().getRange());

            final List<YRange.BoundaryPair> boundaries13 = getLeaf(cont2, "leaf13").getType().getRange().getBoundaries();
            assertTrue(boundaries13.size() == 2);
            assertTrue(boundaries13.get(0).lower.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries13.get(0).upper.compareTo(BigDecimal.valueOf(0L)) == 0);
            assertTrue(boundaries13.get(1).lower.compareTo(BigDecimal.valueOf(255L)) == 0);
            assertTrue(boundaries13.get(1).upper.compareTo(BigDecimal.valueOf(255L)) == 0);
            assertNoFindingsOnStatement(getLeaf(cont2, "leaf13").getType().getRange());

            final List<YRange.BoundaryPair> boundaries14 = getLeaf(cont2, "leaf14").getType().getRange().getBoundaries();
            assertTrue(boundaries14.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf14").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries15 = getLeaf(cont2, "leaf15").getType().getRange().getBoundaries();
            assertTrue(boundaries15.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf15").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries16 = getLeaf(cont2, "leaf16").getType().getRange().getBoundaries();
            assertTrue(boundaries16.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf16").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries17 = getLeaf(cont2, "leaf17").getType().getRange().getBoundaries();
            assertTrue(boundaries17.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf17").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries18 = getLeaf(cont2, "leaf18").getType().getRange().getBoundaries();
            assertTrue(boundaries18.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf18").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries19 = getLeaf(cont2, "leaf19").getType().getRange().getBoundaries();
            assertTrue(boundaries19.size() == 2);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf19").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries20 = getLeaf(cont2, "leaf20").getType().getRange().getBoundaries();
            assertTrue(boundaries20.size() == 2);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf20").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries21 = getLeaf(cont2, "leaf21").getType().getRange().getBoundaries();
            assertTrue(boundaries21.size() == 2);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf21").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries22 = getLeaf(cont2, "leaf22").getType().getRange().getBoundaries();
            assertTrue(boundaries22.size() == 2);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf22").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries23 = getLeaf(cont2, "leaf23").getType().getRange().getBoundaries();
            assertTrue(boundaries23.size() == 2);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf23").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries24 = getLeaf(cont2, "leaf24").getType().getRange().getBoundaries();
            assertTrue(boundaries24.size() == 1);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf24").getType().getRange(),
                    ParserFindingType.P053_INVALID_VALUE.toString());

            final List<YRange.BoundaryPair> boundaries25 = getLeaf(cont2, "leaf25").getType().getRange().getBoundaries();
            assertTrue(boundaries25.size() == 0);
            assertStatementHasFindingOfType(getLeaf(cont2, "leaf25").getType().getRange(),
                    ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
        }

        // -------------- Range for decimal64 -------------------

        {
            final YContainer cont3 = getContainer(module, "cont3");
            assertTrue(cont3 != null);

            final YLeaf leaf1 = getLeaf(cont3, "leaf1");
            assertTrue(leaf1 != null);
            final YRange range1 = leaf1.getType().getRange();
            assertTrue(range1 != null);

            final List<YRange.BoundaryPair> boundaries1 = range1.getBoundaries();
            assertTrue(boundaries1.size() == 1);
            assertTrue(boundaries1.get(0).lower.compareTo(new BigDecimal("-922337203685477580.8")) == 0);
            assertTrue(boundaries1.get(0).upper.compareTo(new BigDecimal("922337203685477580.7")) == 0);
            assertNoFindingsOnStatement(getLeaf(cont3, "leaf1").getType().getRange());

            final List<YRange.BoundaryPair> boundaries2 = getLeaf(cont3, "leaf2").getType().getRange().getBoundaries();
            assertTrue(boundaries2.get(0).lower.compareTo(new BigDecimal("-9.223372036854775808")) == 0);
            assertTrue(boundaries2.get(0).upper.compareTo(new BigDecimal("9.223372036854775807")) == 0);
            assertNoFindingsOnStatement(getLeaf(cont3, "leaf2").getType().getRange());

            final List<YRange.BoundaryPair> boundaries3 = getLeaf(cont3, "leaf3").getType().getRange().getBoundaries();
            assertTrue(boundaries3.size() == 0);
            assertNoFindingsOnStatement(getLeaf(cont3, "leaf3").getType().getRange());
        }
    }

    @Test
    public void testGeneralVarious() {

        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseRelativeImplementsYangModels(Arrays.asList("general-syntax-test/general-syntax-test-various.yang"));

        final YModule module = getModule("general-syntax-test-various");
        assertTrue(module != null);

        // - - - - enums - - - - -

        final YContainer cont1 = getContainer(module, "cont1");

        final YLeaf leaf1 = getLeaf(cont1, "leaf1");

        assertNoFindingsOnStatement(leaf1.getType().getEnums().get(0));
        assertNoFindingsOnStatement(leaf1.getType().getEnums().get(1));
        assertNoFindingsOnStatement(leaf1.getType().getEnums().get(2));
        assertStatementHasFindingOfType(leaf1.getType().getEnums().get(3), ParserFindingType.P141_WHITESPACE_IN_ENUM
                .toString());
        assertNoFindingsOnStatement(leaf1.getType().getEnums().get(4));
        assertStatementHasFindingOfType(leaf1.getType().getEnums().get(5), ParserFindingType.P142_UNUSUAL_CHARACTERS_IN_ENUM
                .toString());
        assertStatementHasFindingOfType(leaf1.getType().getEnums().get(6).getValue(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(leaf1.getType().getEnums().get(6).getValue().getEnumValue() == 0);

        assertTrue(leaf1.getType().getEnums().get(0).getValue().getValue().equals("0"));
        assertTrue(leaf1.getType().getEnums().get(1).getStatus().isDeprecated());
        assertTrue(leaf1.getType().getEnums().get(2).getStatementIdentifier().equals("two"));

        // - - - - status - - - - -

        final YContainer cont2 = getContainer(module, "cont2");

        assertNoFindingsOnStatement(getLeaf(cont2, "leaf11").getStatus());
        assertTrue(getLeaf(cont2, "leaf11").getStatus().isCurrent());
        assertNoFindingsOnStatement(getLeaf(cont2, "leaf12").getStatus());
        assertTrue(getLeaf(cont2, "leaf12").getStatus().isDeprecated());
        assertNoFindingsOnStatement(getLeaf(cont2, "leaf13").getStatus());
        assertTrue(getLeaf(cont2, "leaf13").getStatus().isObsolete());

        assertStatementHasFindingOfType(getLeaf(cont2, "leaf14").getStatus(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertFalse(getLeaf(cont2, "leaf14").getStatus().isCurrent());
        assertFalse(getLeaf(cont2, "leaf14").getStatus().isDeprecated());
        assertFalse(getLeaf(cont2, "leaf14").getStatus().isObsolete());

        assertStatementHasFindingOfType(getLeaf(cont2, "leaf15").getStatus(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertFalse(getLeaf(cont2, "leaf15").getStatus().isCurrent());
        assertFalse(getLeaf(cont2, "leaf15").getStatus().isDeprecated());
        assertFalse(getLeaf(cont2, "leaf15").getStatus().isObsolete());

        // - - - - - container - - - - -

        final YContainer cont3 = getContainer(module, "cont3");
        assertSubTreeNoFindings(cont3);

        assertTrue(cont3.definesDataNode());
        assertTrue(cont3.definesSchemaNode());

        assertTrue(cont3.getGroupings().size() == 1);
        assertTrue(cont3.getTypedefs().size() == 1);
        assertTrue(cont3.getLeafs().size() == 2);
        assertTrue(cont3.getChoices().size() == 1);
        assertTrue(cont3.getLists().size() == 1);
        assertTrue(cont3.getNotifications().size() == 1);
        assertTrue(cont3.getLeafLists().size() == 1);
        assertTrue(cont3.getAnydata().size() == 2);
        assertTrue(cont3.getAnyxmls().size() == 3);

        // - - - - - choice and case - - - - -

        final YChoice choice1 = getChoice(module, "choice1");
        assertSubTreeNoFindings(choice1);

        assertTrue(choice1.getCases().size() == 8);
        assertTrue(choice1.getChoices().size() == 0);
        assertTrue(choice1.getConfig().getValue().equals("true"));
        assertTrue(choice1.getDefault().getValue().equals("case2"));
        assertTrue(choice1.getIfFeatures().size() == 0);
        assertTrue(choice1.getMandatory() == null);
        assertTrue(choice1.getStatus().isDeprecated());
        assertTrue(choice1.getWhens().size() == 1);

        // will always return empty lists as shorthand notation is resolved.
        assertTrue(choice1.getAnydata().size() == 0);
        assertTrue(choice1.getAnyxmls().size() == 0);
        assertTrue(choice1.getContainers().size() == 0);
        assertTrue(choice1.getLists().size() == 0);
        assertTrue(choice1.getLeafs().size() == 0);
        assertTrue(choice1.getLeafLists().size() == 0);

        final YCase case2 = getCase(choice1, "case2");

        assertTrue(case2.getAnydata().size() == 1);
        assertTrue(case2.getAnyxmls().size() == 2);
        assertTrue(case2.getChoices().size() == 0);
        assertTrue(case2.getContainers().size() == 0);
        assertTrue(case2.getLeafs().size() == 1);
        assertTrue(case2.getLeafLists().size() == 1);
        assertTrue(case2.getLists().size() == 0);
        assertTrue(case2.getStatus().isObsolete());
        assertTrue(case2.getUses().size() == 0);
        assertTrue(case2.getWhens().size() == 1);

        // - - - - - list - - - - -

        final YList list4 = getList(module, "list4");
        assertSubTreeNoFindings(list4);

        assertTrue(list4.definesDataNode());
        assertTrue(list4.definesSchemaNode());
        assertTrue(list4.getConfig().getValue().equals("true"));
        assertTrue(list4.getOrderedBy().isOrderedBySystem());
        assertTrue(list4.getStatus().isCurrent());

        assertTrue(list4.getUses().size() == 0);
        assertTrue(list4.getContainers().size() == 0);
        assertTrue(list4.getIfFeatures().size() == 0);
        assertTrue(list4.getGroupings().size() == 1);
        assertTrue(list4.getTypedefs().size() == 1);
        assertTrue(list4.getActions().size() == 3);
        assertTrue(list4.getLeafs().size() == 2);
        assertTrue(list4.getChoices().size() == 1);
        assertTrue(list4.getLists().size() == 1);
        assertTrue(list4.getNotifications().size() == 1);
        assertTrue(list4.getLeafLists().size() == 1);
        assertTrue(list4.getAnydata().size() == 2);
        assertTrue(list4.getAnyxmls().size() == 3);
        assertTrue(list4.getMusts().size() == 0);
        assertTrue(list4.getWhens().size() == 0);

        // - - - - - leaf-list - - - - -

        final YContainer cont5 = getContainer(module, "cont5");

        final YOrderedBy yOrderedBy = getLeafList(cont5, "leaflist51").getOrderedBy();
        assertEquals(StatementArgumentType.VALUE, yOrderedBy.getArgumentType());
        assertEquals(CY.STMT_ORDERED_BY, yOrderedBy.getStatementModuleAndName());

        assertTrue(getLeafList(cont5, "leaflist51").getOrderedBy().isOrderedByUser());
        assertFalse(getLeafList(cont5, "leaflist51").getOrderedBy().isOrderedBySystem());
        assertSubTreeNoFindings(getLeafList(cont5, "leaflist51"));

        assertFalse(getLeafList(cont5, "leaflist52").getOrderedBy().isOrderedByUser());
        assertTrue(getLeafList(cont5, "leaflist52").getOrderedBy().isOrderedBySystem());
        assertSubTreeNoFindings(getLeafList(cont5, "leaflist52"));

        assertFalse(getLeafList(cont5, "leaflist53").getOrderedBy().isOrderedByUser());
        assertFalse(getLeafList(cont5, "leaflist53").getOrderedBy().isOrderedBySystem());
        assertStatementHasFindingOfType(getLeafList(cont5, "leaflist53").getOrderedBy(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertFalse(getLeafList(cont5, "leaflist54").getOrderedBy().isOrderedByUser());
        assertFalse(getLeafList(cont5, "leaflist54").getOrderedBy().isOrderedBySystem());
        assertStatementHasFindingOfType(getLeafList(cont5, "leaflist54").getOrderedBy(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

}
