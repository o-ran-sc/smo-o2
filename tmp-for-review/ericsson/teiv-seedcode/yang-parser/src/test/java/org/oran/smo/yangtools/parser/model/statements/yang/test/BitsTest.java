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
import java.util.Map;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YTypedef;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

/**
 * This test verifies bits/bit statements
 */
public class BitsTest extends YangTestCommon {

    @Test
    public void testBits() {

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        parseRelativeImplementsYangModels(Arrays.asList("bits-test/bits-test-module.yang"));

        final YModule module = getModule("bits-test-module");
        assertTrue(module != null);

        final YTypedef typedef1 = getTypedefForModule("bits-test-module", "typedef1");
        assertTrue(typedef1 != null);
        final Map<String, Long> positionOfBitsTypedef1 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedef1.getType(), null);
        assertTrue(positionOfBitsTypedef1.containsKey("first"));
        assertTrue(positionOfBitsTypedef1.containsKey("second"));
        assertTrue(positionOfBitsTypedef1.containsKey("third"));
        assertTrue(positionOfBitsTypedef1.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedef1.get("second").longValue() == 1);
        assertTrue(positionOfBitsTypedef1.get("third").longValue() == 2);
        assertSubTreeNoFindings(typedef1);

        final YTypedef typedef2 = getTypedefForModule("bits-test-module", "typedef2");
        assertTrue(typedef2 != null);
        final Map<String, Long> positionOfBitsTypedef2 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedef2.getType(), null);
        assertTrue(positionOfBitsTypedef2.containsKey("first"));
        assertTrue(positionOfBitsTypedef2.containsKey("second"));
        assertTrue(positionOfBitsTypedef2.containsKey("third"));
        assertTrue(positionOfBitsTypedef2.get("first").longValue() == 7);
        assertTrue(positionOfBitsTypedef2.get("second").longValue() == 23);
        assertTrue(positionOfBitsTypedef2.get("third").longValue() == 195);
        assertSubTreeNoFindings(typedef2);

        final YTypedef typedef3 = getTypedefForModule("bits-test-module", "typedef3");
        assertTrue(typedef3 != null);
        final Map<String, Long> positionOfBitsTypedef3 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedef3.getType(), null);
        assertTrue(positionOfBitsTypedef3.containsKey("first"));
        assertTrue(positionOfBitsTypedef3.containsKey("second"));
        assertTrue(positionOfBitsTypedef3.containsKey("third"));
        assertTrue(positionOfBitsTypedef3.get("first").longValue() == 98);
        assertTrue(positionOfBitsTypedef3.get("second").longValue() == 99);
        assertTrue(positionOfBitsTypedef3.get("third").longValue() == 100);
        assertSubTreeNoFindings(typedef3);

        final YTypedef typedef4 = getTypedefForModule("bits-test-module", "typedef4");
        assertTrue(typedef4 != null);
        final Map<String, Long> positionOfBitsTypedef4 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedef4.getType(), null);
        assertTrue(positionOfBitsTypedef4.containsKey("first"));
        assertTrue(positionOfBitsTypedef4.containsKey("second"));
        assertTrue(positionOfBitsTypedef4.containsKey("third"));
        assertTrue(positionOfBitsTypedef4.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedef4.get("second").longValue() == 10);
        assertTrue(positionOfBitsTypedef4.get("third").longValue() == 11);
        assertSubTreeNoFindings(typedef4);

        final YTypedef typedefDuplicatePosition1 = getTypedefForModule("bits-test-module", "typedef-duplicate-position1");
        assertTrue(typedefDuplicatePosition1 != null);
        final Map<String, Long> positionOfBitsTypedefDuplicatePosition1 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedefDuplicatePosition1.getType(), null);
        assertTrue(positionOfBitsTypedefDuplicatePosition1.containsKey("first"));
        assertTrue(positionOfBitsTypedefDuplicatePosition1.containsKey("second"));
        assertTrue(positionOfBitsTypedefDuplicatePosition1.containsKey("third"));
        assertTrue(positionOfBitsTypedefDuplicatePosition1.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedefDuplicatePosition1.get("second").longValue() == 10);
        assertTrue(positionOfBitsTypedefDuplicatePosition1.get("third").longValue() == 10);
        assertStatementHasFindingOfType(getChild(typedefDuplicatePosition1.getType(), CY.BIT, "third"),
                ParserFindingType.P053_INVALID_VALUE.toString());

        final YTypedef typedefDuplicatePosition2 = getTypedefForModule("bits-test-module", "typedef-duplicate-position2");
        assertTrue(typedefDuplicatePosition2 != null);
        final Map<String, Long> positionOfBitsTypedefDuplicatePosition2 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedefDuplicatePosition2.getType(), null);
        assertTrue(positionOfBitsTypedefDuplicatePosition2.containsKey("first"));
        assertTrue(positionOfBitsTypedefDuplicatePosition2.containsKey("second"));
        assertTrue(positionOfBitsTypedefDuplicatePosition2.containsKey("third"));
        assertTrue(positionOfBitsTypedefDuplicatePosition2.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedefDuplicatePosition2.get("second").longValue() == 0);
        assertTrue(positionOfBitsTypedefDuplicatePosition2.get("third").longValue() == 1);
        assertStatementHasFindingOfType(getChild(typedefDuplicatePosition2.getType(), CY.BIT, "second"),
                ParserFindingType.P053_INVALID_VALUE.toString());

        final YTypedef typedefTooLarge1 = getTypedefForModule("bits-test-module", "typedef-too-large1");
        assertTrue(typedefTooLarge1 != null);
        final Map<String, Long> positionOfBitsTypedefTooLarge1 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedefTooLarge1.getType(), null);
        assertTrue(positionOfBitsTypedefTooLarge1.containsKey("first"));
        assertTrue(positionOfBitsTypedefTooLarge1.containsKey("second"));
        assertTrue(positionOfBitsTypedefTooLarge1.containsKey("third"));
        assertTrue(positionOfBitsTypedefTooLarge1.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedefTooLarge1.get("second").longValue() == 4294967295L);
        assertTrue(positionOfBitsTypedefTooLarge1.get("third").longValue() == 4294967296L);
        assertStatementHasFindingOfType(getChild(typedefTooLarge1.getType(), CY.BIT, "third"),
                ParserFindingType.P053_INVALID_VALUE.toString());

        final YTypedef typedefTooLarge2 = getTypedefForModule("bits-test-module", "typedef-too-large2");
        assertTrue(typedefTooLarge2 != null);
        final Map<String, Long> positionOfBitsTypedefTooLarge2 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typedefTooLarge2.getType(), null);
        assertTrue(positionOfBitsTypedefTooLarge2.containsKey("first"));
        assertTrue(positionOfBitsTypedefTooLarge2.containsKey("second"));
        assertTrue(positionOfBitsTypedefTooLarge2.containsKey("third"));
        assertTrue(positionOfBitsTypedefTooLarge2.get("first").longValue() == 0);
        assertTrue(positionOfBitsTypedefTooLarge2.get("second").longValue() == -1);
        assertTrue(positionOfBitsTypedefTooLarge2.get("third").longValue() == 0);
        assertStatementHasFindingOfType(getChild(typedefTooLarge2.getType(), CY.BIT, "third"),
                ParserFindingType.P053_INVALID_VALUE.toString());

        // ------------------------------- leafs tests using the typedefs above --------------------------

        final YLeaf leaf1 = getLeaf(module, "leaf1");
        assertTrue(leaf1 != null);
        assertTrue(leaf1.getType() != null);
        assertTrue(leaf1.getType().getDataType().equals("bits"));
        final Map<String, Long> positionOfBitsLeaf1 = DataTypeHelper.calculatePositionOfBits(context.getFindingsManager(),
                leaf1.getType(), positionOfBitsTypedef1);
        assertTrue(positionOfBitsLeaf1.containsKey("first"));
        assertTrue(positionOfBitsLeaf1.containsKey("second"));
        assertTrue(positionOfBitsLeaf1.containsKey("third"));
        assertTrue(positionOfBitsLeaf1.get("first").longValue() == 0);
        assertTrue(positionOfBitsLeaf1.get("second").longValue() == 1);
        assertTrue(positionOfBitsLeaf1.get("third").longValue() == 2);
        assertSubTreeNoFindings(leaf1);

        final YLeaf leaf2 = getLeaf(module, "leaf2");
        assertTrue(leaf2 != null);
        assertTrue(leaf2.getType() != null);
        final Map<String, Long> positionOfBitsLeaf2 = DataTypeHelper.calculatePositionOfBits(context.getFindingsManager(),
                leaf2.getType(), positionOfBitsTypedef2);
        assertTrue(positionOfBitsLeaf2.containsKey("first") == false);
        assertTrue(positionOfBitsLeaf2.containsKey("second"));
        assertTrue(positionOfBitsLeaf2.containsKey("third") == false);
        assertTrue(positionOfBitsLeaf2.get("second").longValue() == 23);
        assertSubTreeNoFindings(leaf2);

        final YLeaf leafWrongPosition1 = getLeaf(module, "leaf-wrong-position1");
        assertTrue(leafWrongPosition1 != null);
        assertTrue(leafWrongPosition1.getType() != null);
        final Map<String, Long> positionOfBitsLeafWrongPosition1 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), leafWrongPosition1.getType(), positionOfBitsTypedef2);
        assertTrue(positionOfBitsLeafWrongPosition1.containsKey("first"));
        assertTrue(positionOfBitsLeafWrongPosition1.containsKey("second"));
        assertTrue(positionOfBitsLeafWrongPosition1.containsKey("third") == false);
        assertTrue(positionOfBitsLeafWrongPosition1.get("first").longValue() == 7);
        assertTrue(positionOfBitsLeafWrongPosition1.get("second").longValue() == 24);
        assertStatementHasFindingOfType(leafWrongPosition1.getType().getBits().get(1),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YLeaf leafWrongPosition2 = getLeaf(module, "leaf-wrong-position2");
        assertTrue(leafWrongPosition2 != null);
        assertTrue(leafWrongPosition2.getType() != null);
        final Map<String, Long> positionOfBitsLeafWrongPosition2 = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), leafWrongPosition2.getType(), positionOfBitsTypedef2);
        assertTrue(positionOfBitsLeafWrongPosition2.containsKey("first"));
        assertTrue(positionOfBitsLeafWrongPosition2.containsKey("second"));
        assertTrue(positionOfBitsLeafWrongPosition2.containsKey("third") == false);
        assertTrue(positionOfBitsLeafWrongPosition2.get("first").longValue() == 7);
        assertTrue(positionOfBitsLeafWrongPosition2.get("second").longValue() == 24);
        assertStatementHasFindingOfType(leafWrongPosition2.getType().getBits().get(1),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YLeaf leafNewBit = getLeaf(module, "leaf-new-bit");
        assertTrue(leafNewBit != null);
        assertTrue(leafNewBit.getType() != null);
        final Map<String, Long> positionOfBitsLeafNewBit = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), leafNewBit.getType(), positionOfBitsTypedef2);
        assertTrue(positionOfBitsLeafNewBit.containsKey("first"));
        assertTrue(positionOfBitsLeafNewBit.containsKey("second"));
        assertTrue(positionOfBitsLeafNewBit.containsKey("third"));
        assertTrue(positionOfBitsLeafNewBit.containsKey("fourth"));
        assertTrue(positionOfBitsLeafNewBit.get("first").longValue() == 7);
        assertTrue(positionOfBitsLeafNewBit.get("second").longValue() == 23);
        assertTrue(positionOfBitsLeafNewBit.get("third").longValue() == 195);
        assertTrue(positionOfBitsLeafNewBit.get("fourth").longValue() == 34);
        assertStatementHasFindingOfType(leafNewBit.getType().getBits().get(3),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YLeaf leafNotBits = getLeaf(module, "leaf-not-bits");
        assertStatementHasFindingOfType(leafNotBits, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
    }
}
