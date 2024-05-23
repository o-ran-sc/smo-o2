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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;
import org.oran.smo.yangtools.parser.model.util.NumberHelper;

public class NumberHelperTest {

    @Test
    public void test_get_min_value_for_integer_type() {

        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.UINT8).equals(BigDecimal.ZERO));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.UINT16).equals(BigDecimal.ZERO));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.UINT32).equals(BigDecimal.ZERO));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.UINT64).equals(BigDecimal.ZERO));

        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.INT8).equals(BigDecimal.valueOf(
                Byte.MIN_VALUE)));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.INT16).equals(BigDecimal.valueOf(
                Short.MIN_VALUE)));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.INT32).equals(BigDecimal.valueOf(
                Integer.MIN_VALUE)));
        assertTrue(NumberHelper.getMinValueForYangIntegerDataType(YangDataType.INT64).equals(BigDecimal.valueOf(
                Long.MIN_VALUE)));

        try {
            NumberHelper.getMinValueForYangIntegerDataType(YangDataType.STRING);
            fail("Should have thrown");
        } catch (final Exception ignore) {
        }
    }

    @Test
    public void test_get_max_value_for_integer_type() {

        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.UINT8).equals(BigDecimal.valueOf(
                ((long) Byte.MAX_VALUE) * 2 + 1)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.UINT16).equals(BigDecimal.valueOf(
                ((long) Short.MAX_VALUE) * 2 + 1)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.UINT32).equals(BigDecimal.valueOf(
                ((long) Integer.MAX_VALUE) * 2 + 1)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.UINT64).equals(new BigDecimal(
                "18446744073709551615")));

        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.INT8).equals(BigDecimal.valueOf(
                Byte.MAX_VALUE)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.INT16).equals(BigDecimal.valueOf(
                Short.MAX_VALUE)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.INT32).equals(BigDecimal.valueOf(
                Integer.MAX_VALUE)));
        assertTrue(NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.INT64).equals(BigDecimal.valueOf(
                Long.MAX_VALUE)));

        try {
            NumberHelper.getMaxValueForYangIntegerDataType(YangDataType.STRING);
            fail("Should have thrown");
        } catch (final Exception ignore) {
        }
    }

    @Test
    public void test_get_integer_default_value() {
        assertTrue(NumberHelper.getIntegerDefaultValue("0").equals(BigInteger.valueOf(0)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+0").equals(BigInteger.valueOf(0)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-0").equals(BigInteger.valueOf(0)));

        assertTrue(NumberHelper.getIntegerDefaultValue("00").equals(BigInteger.valueOf(0)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+00").equals(BigInteger.valueOf(0)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-00").equals(BigInteger.valueOf(0)));

        assertTrue(NumberHelper.getIntegerDefaultValue("10").equals(BigInteger.valueOf(10)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+10").equals(BigInteger.valueOf(10)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-10").equals(BigInteger.valueOf(-10)));

        assertTrue(NumberHelper.getIntegerDefaultValue("0x10").equals(BigInteger.valueOf(16)));
        assertTrue(NumberHelper.getIntegerDefaultValue("0x100").equals(BigInteger.valueOf(256)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+0x10").equals(BigInteger.valueOf(16)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+0x100").equals(BigInteger.valueOf(256)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-0x10").equals(BigInteger.valueOf(-16)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-0x100").equals(BigInteger.valueOf(-256)));

        assertTrue(NumberHelper.getIntegerDefaultValue("010").equals(BigInteger.valueOf(8)));
        assertTrue(NumberHelper.getIntegerDefaultValue("0100").equals(BigInteger.valueOf(64)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+010").equals(BigInteger.valueOf(8)));
        assertTrue(NumberHelper.getIntegerDefaultValue("+0100").equals(BigInteger.valueOf(64)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-010").equals(BigInteger.valueOf(-8)));
        assertTrue(NumberHelper.getIntegerDefaultValue("-0100").equals(BigInteger.valueOf(-64)));

        assertTrue(NumberHelper.getIntegerDefaultValue("0x") == null);
        assertTrue(NumberHelper.getIntegerDefaultValue("") == null);
        assertTrue(NumberHelper.getIntegerDefaultValue(" ") == null);
        assertTrue(NumberHelper.getIntegerDefaultValue("ABC") == null);
    }

    @Test
    public void test_get_min_value_for_yang_decimal_data_type() {
        assertTrue(NumberHelper.getMinValueForYangDecimalDataType(0) == null);
        assertTrue(NumberHelper.getMinValueForYangDecimalDataType(1).equals(BigDecimal.valueOf(Long.MIN_VALUE).divide(
                BigDecimal.valueOf(10))));
        assertTrue(NumberHelper.getMinValueForYangDecimalDataType(2).equals(BigDecimal.valueOf(Long.MIN_VALUE).divide(
                BigDecimal.valueOf(100))));
        assertTrue(NumberHelper.getMinValueForYangDecimalDataType(3).equals(BigDecimal.valueOf(Long.MIN_VALUE).divide(
                BigDecimal.valueOf(1000))));
    }

    @Test
    public void test_get_max_value_for_yang_decimal_data_type() {
        assertTrue(NumberHelper.getMaxValueForYangDecimalDataType(0) == null);
        assertTrue(NumberHelper.getMaxValueForYangDecimalDataType(1).equals(BigDecimal.valueOf(Long.MAX_VALUE).divide(
                BigDecimal.valueOf(10))));
        assertTrue(NumberHelper.getMaxValueForYangDecimalDataType(2).equals(BigDecimal.valueOf(Long.MAX_VALUE).divide(
                BigDecimal.valueOf(100))));
        assertTrue(NumberHelper.getMaxValueForYangDecimalDataType(3).equals(BigDecimal.valueOf(Long.MAX_VALUE).divide(
                BigDecimal.valueOf(1000))));
    }

}
