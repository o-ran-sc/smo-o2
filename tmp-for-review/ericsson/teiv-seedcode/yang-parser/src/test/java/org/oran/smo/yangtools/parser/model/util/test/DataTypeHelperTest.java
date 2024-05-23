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
package org.oran.smo.yangtools.parser.model.util.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class DataTypeHelperTest {

    private YangDeviceModel yangDeviceModel;
    private ModifyableFindingSeverityCalculator severityCalculator;
    private FindingsManager findingsManager;
    private ParserExecutionContext context;

    @Before
    public void setUp() {
        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);
        context = new ParserExecutionContext(findingsManager);
    }

    @Test
    public void test_is_yang_integer_type() {

        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.UINT8) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.UINT16) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.UINT32) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.UINT64) == true);

        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.INT8) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.INT16) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.INT32) == true);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.INT64) == true);

        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.DECIMAL64) == false);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.STRING) == false);
        assertTrue(DataTypeHelper.isYangIntegerType(YangDataType.BOOLEAN) == false);
    }

    @Test
    public void test_get_yang_data_type() {

        assertTrue(DataTypeHelper.getYangDataType("uint8") == YangDataType.UINT8);
        assertTrue(DataTypeHelper.getYangDataType("UINT8") == YangDataType.UINT8);
        assertTrue(DataTypeHelper.getYangDataType("uInt8") == YangDataType.UINT8);

        assertTrue(DataTypeHelper.getYangDataType("uint16") == YangDataType.UINT16);
        assertTrue(DataTypeHelper.getYangDataType("uint32") == YangDataType.UINT32);
        assertTrue(DataTypeHelper.getYangDataType("uint64") == YangDataType.UINT64);

        assertTrue(DataTypeHelper.getYangDataType("int8") == YangDataType.INT8);
        assertTrue(DataTypeHelper.getYangDataType("int16") == YangDataType.INT16);
        assertTrue(DataTypeHelper.getYangDataType("int32") == YangDataType.INT32);
        assertTrue(DataTypeHelper.getYangDataType("int64") == YangDataType.INT64);

        assertTrue(DataTypeHelper.getYangDataType("decimal64") == YangDataType.DECIMAL64);
        assertTrue(DataTypeHelper.getYangDataType("string") == YangDataType.STRING);
        assertTrue(DataTypeHelper.getYangDataType("boolean") == YangDataType.BOOLEAN);
        assertTrue(DataTypeHelper.getYangDataType("enumeration") == YangDataType.ENUMERATION);
        assertTrue(DataTypeHelper.getYangDataType("bits") == YangDataType.BITS);
        assertTrue(DataTypeHelper.getYangDataType("binary") == YangDataType.BINARY);
        assertTrue(DataTypeHelper.getYangDataType("leafref") == YangDataType.LEAFREF);
        assertTrue(DataTypeHelper.getYangDataType("identityref") == YangDataType.IDENTITYREF);
        assertTrue(DataTypeHelper.getYangDataType("empty") == YangDataType.EMPTY);
        assertTrue(DataTypeHelper.getYangDataType("union") == YangDataType.UNION);
        assertTrue(DataTypeHelper.getYangDataType("instance-identifier") == YangDataType.INSTANCE_IDENTIFIER);

        assertTrue(DataTypeHelper.getYangDataType("") == YangDataType.DERIVED____TYPE);
        assertTrue(DataTypeHelper.getYangDataType("prefix:someType") == YangDataType.DERIVED____TYPE);

        assertTrue(DataTypeHelper.isBuiltInType("decimal64") == true);
        assertTrue(DataTypeHelper.isBuiltInType("prefix:someType") == false);
    }

    @Test
    public void test_calculate_position_of_bits() {

        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        final List<YangModel> yangFiles = new ArrayList<>();
        yangFiles.add(new YangModel(new FileBasedYangInput(new File("src/test/resources/model-util/module1.yang")),
                ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        final YModule module1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot()
                .getModule();

        final YLeaf leaf1 = module1.getLeafs().get(0);
        final YLeaf leaf2 = module1.getLeafs().get(1);

        Map<String, Long> positionOfBitsLeaf1 = DataTypeHelper.calculatePositionOfBits(null, leaf1.getType(), null);

        assertTrue(positionOfBitsLeaf1.size() == 3);
        assertTrue(positionOfBitsLeaf1.containsKey("one"));
        assertTrue(positionOfBitsLeaf1.containsKey("two"));
        assertTrue(positionOfBitsLeaf1.containsKey("three"));
        assertTrue(positionOfBitsLeaf1.get("one") == 0);
        assertTrue(positionOfBitsLeaf1.get("two") == 1);
        assertTrue(positionOfBitsLeaf1.get("three") == 2);

        Map<String, Long> positionOfBitsLeaf2 = DataTypeHelper.calculatePositionOfBits(null, leaf2.getType(), null);

        assertTrue(positionOfBitsLeaf2.size() == 3);
        assertTrue(positionOfBitsLeaf2.containsKey("one"));
        assertTrue(positionOfBitsLeaf2.containsKey("two"));
        assertTrue(positionOfBitsLeaf2.containsKey("three"));
        assertTrue(positionOfBitsLeaf2.get("one") == 71);
        assertTrue(positionOfBitsLeaf2.get("two") == 72);
        assertTrue(positionOfBitsLeaf2.get("three") == 402);

        final Map<String, Long> predefinedMapping = new HashMap<>();
        predefinedMapping.put("zero", 999L);
        predefinedMapping.put("one", 36L);

        positionOfBitsLeaf1 = DataTypeHelper.calculatePositionOfBits(null, leaf1.getType(), predefinedMapping);

        assertTrue(positionOfBitsLeaf1.get("one") == 36);
        assertTrue(positionOfBitsLeaf1.get("two") == 37);
        assertTrue(positionOfBitsLeaf1.get("three") == 38);

        predefinedMapping.put("two", 99999999999999999L);
        predefinedMapping.put("three", 36L);

        positionOfBitsLeaf1 = DataTypeHelper.calculatePositionOfBits(null, leaf1.getType(), predefinedMapping);

        assertTrue(positionOfBitsLeaf1.size() == 3);
        assertTrue(positionOfBitsLeaf1.get("one") == 36);
        assertTrue(positionOfBitsLeaf1.get("two") == 99999999999999999L);
        assertTrue(positionOfBitsLeaf1.get("three") == 36);

        assertTrue(findingsManager.getAllFindings().isEmpty());

        positionOfBitsLeaf1 = DataTypeHelper.calculatePositionOfBits(findingsManager, leaf1.getType(), predefinedMapping);

        assertTrue(positionOfBitsLeaf1.size() == 3);
        assertTrue(positionOfBitsLeaf1.get("one") == 36);
        assertTrue(positionOfBitsLeaf1.get("two") == 99999999999999999L);
        assertTrue(positionOfBitsLeaf1.get("three") == 36);

        assertTrue(findingsManager.hasFindingOfType(ParserFindingType.P053_INVALID_VALUE.toString()));
    }

    @Test
    public void test_calculate_values_of_enums() {

        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        final List<YangModel> yangFiles = new ArrayList<>();
        yangFiles.add(new YangModel(new FileBasedYangInput(new File("src/test/resources/model-util/module1.yang")),
                ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        final YModule module1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot()
                .getModule();

        final YLeaf leaf3 = module1.getLeafs().get(2);
        final YLeaf leaf4 = module1.getLeafs().get(3);

        Map<String, Long> valuesOfEnumsLeaf3 = DataTypeHelper.calculateValuesOfEnums(null, leaf3.getType(), null);

        assertTrue(valuesOfEnumsLeaf3.size() == 3);
        assertTrue(valuesOfEnumsLeaf3.containsKey("one"));
        assertTrue(valuesOfEnumsLeaf3.containsKey("two"));
        assertTrue(valuesOfEnumsLeaf3.containsKey("three"));
        assertTrue(valuesOfEnumsLeaf3.get("one") == 0);
        assertTrue(valuesOfEnumsLeaf3.get("two") == 1);
        assertTrue(valuesOfEnumsLeaf3.get("three") == 2);

        Map<String, Long> valuesOfEnumsLeaf4 = DataTypeHelper.calculateValuesOfEnums(null, leaf4.getType(), null);

        assertTrue(valuesOfEnumsLeaf4.size() == 3);
        assertTrue(valuesOfEnumsLeaf4.containsKey("one"));
        assertTrue(valuesOfEnumsLeaf4.containsKey("two"));
        assertTrue(valuesOfEnumsLeaf4.containsKey("three"));
        assertTrue(valuesOfEnumsLeaf4.get("one") == 71);
        assertTrue(valuesOfEnumsLeaf4.get("two") == 72);
        assertTrue(valuesOfEnumsLeaf4.get("three") == 402);

        final Map<String, Long> predefinedMapping = new HashMap<>();
        predefinedMapping.put("zero", 999L);
        predefinedMapping.put("one", 36L);

        valuesOfEnumsLeaf3 = DataTypeHelper.calculateValuesOfEnums(null, leaf3.getType(), predefinedMapping);

        assertTrue(valuesOfEnumsLeaf3.get("one") == 36);
        assertTrue(valuesOfEnumsLeaf3.get("two") == 37);
        assertTrue(valuesOfEnumsLeaf3.get("three") == 38);

        predefinedMapping.put("two", 99999999999999999L);
        predefinedMapping.put("three", 36L);

        valuesOfEnumsLeaf3 = DataTypeHelper.calculateValuesOfEnums(null, leaf3.getType(), predefinedMapping);

        assertTrue(valuesOfEnumsLeaf3.size() == 3);
        assertTrue(valuesOfEnumsLeaf3.get("one") == 36);
        assertTrue(valuesOfEnumsLeaf3.get("two") == 99999999999999999L);
        assertTrue(valuesOfEnumsLeaf3.get("three") == 36);

        assertTrue(findingsManager.getAllFindings().isEmpty());

        valuesOfEnumsLeaf3 = DataTypeHelper.calculateValuesOfEnums(findingsManager, leaf3.getType(), predefinedMapping);

        assertTrue(valuesOfEnumsLeaf3.size() == 3);
        assertTrue(valuesOfEnumsLeaf3.get("one") == 36);
        assertTrue(valuesOfEnumsLeaf3.get("two") == 99999999999999999L);
        assertTrue(valuesOfEnumsLeaf3.get("three") == 36);

        assertTrue(findingsManager.hasFindingOfType(ParserFindingType.P053_INVALID_VALUE.toString()));
    }

    @Test
    public void test_stringefied_values() {

        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        final List<YangModel> yangFiles = new ArrayList<>();
        yangFiles.add(new YangModel(new FileBasedYangInput(new File(
                "src/test/resources/model-util/stringefied-values-test.yang")), ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        assertTrue(findingsManager.getAllFindings().isEmpty());

        final YModule module = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule();

        // ------------------------- integers ------------------------------

        final YType type11 = YangTestCommon.getLeaf(module, "leaf11").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type11) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("not a number", type11) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-123456", type11) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("123456", type11) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("0", type11) == true);

        final YType type12 = YangTestCommon.getLeaf(module, "leaf12").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("-123456", type12) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("123456", type12) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("9", type12) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("21", type12) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("10", type12) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("20", type12) == true);

        final YType type13 = YangTestCommon.getLeaf(module, "leaf13").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("-123456", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("123456", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-1", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("21", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("39", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("61", type13) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("0", type13) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("20", type13) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("40", type13) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("60", type13) == true);

        // ------------------------- decimal64 ------------------------------

        final YType type21 = YangTestCommon.getLeaf(module, "leaf21").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type21) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("not a number", type21) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("1.11", type21) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-1.11", type21) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("1", type21) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("1.1", type21) == true);

        final YType type22 = YangTestCommon.getLeaf(module, "leaf22").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("5.000001", type22) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-0.001", type22) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("10.001", type22) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("0.000", type22) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("10.000", type22) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("20.199", type22) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("20.200", type22) == true);

        final YType type23 = YangTestCommon.getLeaf(module, "leaf23").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("123.45", type23) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-123.45", type23) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("1.2345", type23) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("-1.2345", type23) == true);

        // ------------------------- boolean ------------------------------

        final YType type31 = YangTestCommon.getLeaf(module, "leaf31").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type31) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("not a boolean", type31) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type31) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("TRUE", type31) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("true", type31) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("false", type31) == true);

        // ------------------------- enumeration ------------------------------

        final YType type41 = YangTestCommon.getLeaf(module, "leaf41").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type41) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type41) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("BLACK", type41) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("         RED        ", type41) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("red", type41) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("RED", type41) == true);

        // ------------------------- bits ------------------------------

        final YType type51 = YangTestCommon.getLeaf(module, "leaf51").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type51) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ZERO", type51) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("one", type51) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("         ONE        ", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ONE", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ONE TWO", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("TWO ONE", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("THREE ONE", type51) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("THREE one", type51) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ONE ONE", type51) == false);

        // ------------------------- string ------------------------------

        final YType type61 = YangTestCommon.getLeaf(module, "leaf61").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type61) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type61) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("hello", type61) == true);

        final YType type62 = YangTestCommon.getLeaf(module, "leaf62").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type62) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCD", type62) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFGHIJK", type62) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDE", type62) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFGHIJ", type62) == true);

        final YType type63 = YangTestCommon.getLeaf(module, "leaf63").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type63) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("A", type63) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDE", type63) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFG", type63) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFGHIJK", type63) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("AB", type63) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCD", type63) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFGH", type63) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ABCDEFGHIJ", type63) == true);

        final YType type64 = YangTestCommon.getLeaf(module, "leaf64").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type64) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("a", type64) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("c", type64) == false);
        assertTrue(DataTypeHelper.isStringefiedValueValid("ac", type64) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("abc", type64) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("abbc", type64) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("abbc", type64) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("aBc", type64) == false);

        // ------------------------- empty ------------------------------

        final YType type71 = YangTestCommon.getLeaf(module, "leaf71").getType();
        assertTrue(DataTypeHelper.isStringefiedValueValid(null, type71) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("", type71) == true);
        assertTrue(DataTypeHelper.isStringefiedValueValid("hello", type71) == false);

        // ------------------------- union ------------------------------

        final YType type81 = YangTestCommon.getLeaf(module, "leaf81").getType();

        try {
            DataTypeHelper.isStringefiedValueValid("blurb", type81);
            fail();
        } catch (final Exception expected) {
        }
    }

}
