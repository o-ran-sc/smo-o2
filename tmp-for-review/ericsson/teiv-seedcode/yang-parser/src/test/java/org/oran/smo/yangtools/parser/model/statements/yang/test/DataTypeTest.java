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
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class DataTypeTest extends YangTestCommon {

    @Test
    public void testDataTypes() {

        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        parseRelativeImplementsYangModels(Arrays.asList("data-type-test/data-type-test.yang"));

        final YModule module = getModule("data-type-test-module");
        final YContainer cont1 = getContainer(module, "cont1");

        assertSubTreeNoFindings(cont1);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf1").getType().getDataType()) == YangDataType.INT8);

        assertTrue(getLeaf(cont1, "leaf1").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf1").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                -128)) == 0);
        assertTrue(getLeaf(cont1, "leaf1").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                127)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf2").getType().getDataType()) == YangDataType.INT16);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                10)) == 0);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                20)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf3").getType().getDataType()) == YangDataType.INT32);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                -30)) == 0);
        assertTrue(getLeaf(cont1, "leaf3").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                20)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf4").getType().getDataType()) == YangDataType.INT64);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                20)) == 0);
        assertTrue(getLeaf(cont1, "leaf4").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                Long.MAX_VALUE)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf5").getType().getDataType()) == YangDataType.UINT8);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                10)) == 0);
        assertTrue(getLeaf(cont1, "leaf5").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                10)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf6").getType().getDataType()) == YangDataType.UINT16);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                0)) == 0);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                20)) == 0);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(1).lower.compareTo(BigDecimal.valueOf(
                40)) == 0);
        assertTrue(getLeaf(cont1, "leaf6").getType().getRange().getBoundaries().get(1).upper.compareTo(BigDecimal.valueOf(
                40)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf7").getType().getDataType()) == YangDataType.UINT32);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                0)) == 0);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                20)) == 0);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).lower.compareTo(BigDecimal.valueOf(
                40)) == 0);
        assertTrue(getLeaf(cont1, "leaf7").getType().getRange().getBoundaries().get(1).upper.compareTo(BigDecimal.valueOf(
                60)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf8").getType().getDataType()) == YangDataType.UINT64);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                0)) == 0);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                20)) == 0);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).lower.compareTo(BigDecimal.valueOf(
                30)) == 0);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(1).upper.compareTo(BigDecimal.valueOf(
                30)) == 0);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(2).lower.compareTo(BigDecimal.valueOf(
                40)) == 0);
        assertTrue(getLeaf(cont1, "leaf8").getType().getRange().getBoundaries().get(2).upper.compareTo(BigDecimal.valueOf(
                60)) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf9").getType()
                .getDataType()) == YangDataType.DECIMAL64);
        assertTrue(getLeaf(cont1, "leaf9").getType().getFractionDigits().getFractionDigits() == 1);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                0)) == 0);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                10)) == 0);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(1).lower.compareTo(BigDecimal.valueOf(
                20.2d)) == 0);
        assertTrue(getLeaf(cont1, "leaf9").getType().getRange().getBoundaries().get(1).upper.compareTo(new BigDecimal(
                "922337203685477580.7")) == 0);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf21").getType().getDataType()) == YangDataType.STRING);
        assertTrue(getLeaf(cont1, "leaf21").getType().getLength().getBoundaries().get(0).lower == 10L);
        assertTrue(getLeaf(cont1, "leaf21").getType().getLength().getBoundaries().get(0).upper == 20L);
        assertTrue(getLeaf(cont1, "leaf21").getType().getPatterns().get(0).getPattern().equals("ab*c"));
        assertTrue(getLeaf(cont1, "leaf21").getType().getPatterns().get(0).getModifier().getValue().equals("invert-match"));

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf22").getType()
                .getDataType()) == YangDataType.BOOLEAN);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf23").getType()
                .getDataType()) == YangDataType.ENUMERATION);
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(0).getEnumName().equals("one"));
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(0).getValue().getEnumValue() == 10);
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(0).getStatus().isDeprecated());
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(0).getIfFeatures().get(0).getValue().equals(
                "feature1"));
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(1).getEnumName().equals("two"));
        assertTrue(getLeaf(cont1, "leaf23").getType().getEnums().get(2).getEnumName().equals("three"));

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf24").getType().getDataType()) == YangDataType.BITS);
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(0).getBitName().equals("one"));
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(0).getPosition().getPosition() == 10);
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(0).getStatus().isDeprecated());
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(0).getIfFeatures().get(0).getValue().equals(
                "feature1"));
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(1).getBitName().equals("two"));
        assertTrue(getLeaf(cont1, "leaf24").getType().getBits().get(2).getBitName().equals("three"));

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf25").getType().getDataType()) == YangDataType.BINARY);
        assertTrue(getLeaf(cont1, "leaf25").getType().getLength().getBoundaries().get(0).lower == 90);
        assertTrue(getLeaf(cont1, "leaf25").getType().getLength().getBoundaries().get(0).upper == 1010);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf26").getType()
                .getDataType()) == YangDataType.LEAFREF);
        assertTrue(getLeaf(cont1, "leaf26").getType().getPath().getValue().equals("/this:cont1/this:leaf1"));
        assertTrue(getLeaf(cont1, "leaf26").getType().getRequireInstance().isRequireInstanceTrue());

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf27").getType()
                .getDataType()) == YangDataType.IDENTITYREF);
        assertTrue(getLeaf(cont1, "leaf27").getType().getBases().get(0).getValue().equals("identity1"));

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf28").getType().getDataType()) == YangDataType.EMPTY);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf29").getType().getDataType()) == YangDataType.UNION);
        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf29").getType().getTypes().get(0)
                .getDataType()) == YangDataType.INT32);
        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf29").getType().getTypes().get(1)
                .getDataType()) == YangDataType.STRING);

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont1, "leaf30").getType()
                .getDataType()) == YangDataType.INSTANCE_IDENTIFIER);
        assertTrue(getLeaf(cont1, "leaf30").getType().getRequireInstance().isRequireInstanceFalse());

        // - - - - - container2 - - - - -

        final YContainer cont2 = getContainer(module, "cont2");

        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont2, "leaf51").getType()
                .getDataType()) == YangDataType.DECIMAL64);
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf51").getType().getFractionDigits(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont2, "leaf52").getType()
                .getDataType()) == YangDataType.DECIMAL64);
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf52").getType().getFractionDigits(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont2, "leaf53").getType()
                .getDataType()) == YangDataType.DECIMAL64);
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf53").getType().getFractionDigits(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertTrue(DataTypeHelper.getYangDataType(getLeaf(cont2, "leaf54").getType()
                .getDataType()) == YangDataType.DECIMAL64);
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf54").getType().getFractionDigits(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertStatementHasFindingOfType(getLeaf(cont2, "leaf58").getType().getBits().get(0).getPosition(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf58").getType().getBits().get(1).getPosition(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf58").getType().getBits().get(2).getPosition(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf58").getType().getBits().get(3).getPosition(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertNoFindingsOnStatement(getLeafList(cont2, "leaflist62").getMinElements());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist63").getMinElements(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist64").getMinElements(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist65").getMinElements(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertNoFindingsOnStatement(getLeafList(cont2, "leaflist71").getMaxElements());
        assertNoFindingsOnStatement(getLeafList(cont2, "leaflist72").getMaxElements());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist73").getMaxElements(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist74").getMaxElements(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist75").getMaxElements(),
                ParserFindingType.P053_INVALID_VALUE.toString());
        assertStatementHasFindingOfType(getLeafList(cont2, "leaflist76").getMaxElements(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertNoFindingsOnStatement(getLeaf(cont2, "leaf81").getType().getPatterns().get(0).getModifier());
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf82").getType().getPatterns().get(0).getModifier(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(getLeaf(cont2, "leaf83").getType().getPatterns().get(0).getModifier(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }
}
