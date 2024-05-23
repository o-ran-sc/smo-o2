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
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class LengthTest extends YangTestCommon {

    @Test
    public void testLengthSimple() {

        parseRelativeImplementsYangModels(Arrays.asList("length-test/length-test-module-simple.yang"));
        assertTrue(findingsManager.getAllFindings().isEmpty());

        final YModule module = getModule("length-test-module-simple");
        assertTrue(module != null);

        // -------------------------- Simple lengths that are fine --------------------

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf1").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getLength() == null);

        assertTrue(getLeaf(cont1, "leaf2").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().get(0).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf3").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().get(0).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(0).upper == 0L);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(1).lower == Long.MAX_VALUE);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(1).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf5").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(0).upper == 0L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(1).lower == Long.MAX_VALUE);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(1).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf6").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().get(0).upper == 100L);

        assertTrue(getLeaf(cont1, "leaf7").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(1).lower == 30L);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(1).upper == 60L);

        assertTrue(getLeaf(cont1, "leaf8").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength().getBoundaries().get(1).lower == 30L);
        assertTrue(getLeaf(cont1, "leaf8").getType().getLength().getBoundaries().get(1).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf9").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(1).lower == 30L);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(1).upper == 30L);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(2).lower == 80L);
        assertTrue(getLeaf(cont1, "leaf9").getType().getLength().getBoundaries().get(2).upper == 80L);

        assertTrue(getLeaf(cont1, "leaf10").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(1).lower == 30L);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(1).upper == 30L);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(2).lower == 80L);
        assertTrue(getLeaf(cont1, "leaf10").getType().getLength().getBoundaries().get(2).upper == 80L);

        assertTrue(getLeaf(cont1, "leaf11").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(0).upper == 0L);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(1).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(1).upper == 10L);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(2).lower == 40L);
        assertTrue(getLeaf(cont1, "leaf11").getType().getLength().getBoundaries().get(2).upper == 40L);

        assertTrue(getLeaf(cont1, "leaf12").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(0).upper == 0L);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(1).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(1).upper == 10L);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(2).lower == 70L);
        assertTrue(getLeaf(cont1, "leaf12").getType().getLength().getBoundaries().get(2).upper == 70L);

        assertTrue(getLeaf(cont1, "leaf13").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().size() == 4);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(0).upper == 0L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(1).lower == 2L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(1).upper == 2L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(2).lower == 3L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(2).upper == 3L);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(3).lower == Long.MAX_VALUE);
        assertTrue(getLeaf(cont1, "leaf13").getType().getLength().getBoundaries().get(3).upper == Long.MAX_VALUE);
    }

    @Test
    public void testLengthTypedefs() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        parseRelativeImplementsYangModels(Arrays.asList("length-test/length-test-module-typedefs.yang"));

        printFindings();

        assertTrue(findingsManager.getAllFindings().isEmpty());

        final YModule module = getModule("length-test-module-typedefs");
        assertTrue(module != null);

        // -------------------------- Lengths and typedefs that are fine --------------------

        assertTrue(getTypedef(module, "typeStringNoLength") != null);
        assertTrue(getTypedef(module, "typeStringNoLength").getType().getDataType().equals("string"));
        assertTrue(getTypedef(module, "typeStringNoLength").getType().getLength() == null);

        assertTrue(getTypedef(module, "typeStringWithLength") != null);
        assertTrue(getTypedef(module, "typeStringWithLength").getType().getDataType().equals("string"));
        assertTrue(getTypedef(module, "typeStringWithLength").getType().getLength() != null);
        assertTrue(getTypedef(module, "typeStringWithLength").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getTypedef(module, "typeStringWithLength").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getTypedef(module, "typeStringWithLength").getType().getLength().getBoundaries().get(0).upper == 20L);

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf1").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getLength() == null);

        assertTrue(getLeaf(cont1, "leaf2").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().get(0).lower == 0L);
        assertTrue(getLeaf(cont1, "leaf2").getType().getLength().getBoundaries().get(0).upper == Long.MAX_VALUE);

        assertTrue(getLeaf(cont1, "leaf3").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf3").getType().getLength().getBoundaries().get(0).upper == 20L);

        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().size() == 2);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(1).lower == 30L);
        assertTrue(getLeaf(cont1, "leaf4").getType().getLength().getBoundaries().get(1).upper == 60L);

        assertTrue(getLeaf(cont1, "leaf5").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().size() == 3);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(0).upper == 10L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(1).lower == 12L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(1).upper == 12L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(2).lower == 17L);
        assertTrue(getLeaf(cont1, "leaf5").getType().getLength().getBoundaries().get(2).upper == 17L);

        assertTrue(getLeaf(cont1, "leaf6").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf6").getType().getLength().getBoundaries().get(0).upper == 20L);

        assertTrue(getLeaf(cont1, "leaf7").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength() != null);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(0).lower == 17L);
        assertTrue(getLeaf(cont1, "leaf7").getType().getLength().getBoundaries().get(0).upper == 19L);
    }

    @Test
    public void testLengthSimpleFaulty() {

        parseRelativeImplementsYangModels(Arrays.asList("length-test/length-test-module-simple-faulty.yang"));

        final YModule module = getModule("length-test-module-simple-faulty");
        assertTrue(module != null);

        // -------------------------- Simple lengths that are faulty for a variety of reasons --------------------

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertStatementHasFindingOfType(getLeaf(cont1, "leaf1").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf2").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf3").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf4").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf7").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf8").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf9").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf10").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf11").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf12").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
    }

    @Test
    public void testLengthTypedefsFaulty() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        parseRelativeImplementsYangModels(Arrays.asList("length-test/length-test-module-typedefs-faulty.yang"));

        final YModule module = getModule("length-test-module-typedefs-faulty");
        assertTrue(module != null);

        // -------------------------- Lengths and typedefs that are incorrect --------------------

        assertNoFindingsOnStatement(getTypedef(module, "typeStringNoLength"));
        assertStatementHasFindingOfType(getTypedef(module, "typeStringWithLength").getType().getLength(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertNoFindingsOnStatement(getTypedef(module, "typeUsingTypeStringWithLength"));

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertStatementHasFindingOfType(getLeaf(cont1, "leaf1").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf2").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf3").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf4").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getType().getLength(), ParserFindingType.P053_INVALID_VALUE
                .toString());
    }

}
