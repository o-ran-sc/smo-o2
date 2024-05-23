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
package org.oran.smo.yangtools.parser.data.util.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.oran.smo.yangtools.parser.data.util.BinaryValue;
import org.oran.smo.yangtools.parser.data.util.BitsValue;
import org.oran.smo.yangtools.parser.data.util.ValueHelper;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;

public class ValueHelperTest {

    @Before
    public void setUp() {
        new YangModel(new FileBasedYangInput(new File("src/test/resources/model-util/module1.yang")),
                ConformanceType.IMPLEMENT);
    }

    @Test
    public void test_integer_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.UINT8) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.UINT8) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("blurb", YangDataType.UINT8) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("10.0", YangDataType.UINT8) == null);

        assertTrue(ValueHelper.fromLexicalRepresentation("+10", YangDataType.UINT8) instanceof BigInteger);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.UINT8)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.UINT16)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.UINT32)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.UINT64)).compareTo(BigInteger
                .valueOf(10L)) == 0);

        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.INT8)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.INT16)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.INT32)).compareTo(BigInteger
                .valueOf(10L)) == 0);
        assertTrue(((BigInteger) ValueHelper.fromLexicalRepresentation("+10", YangDataType.INT64)).compareTo(BigInteger
                .valueOf(10L)) == 0);
    }

    @Test
    public void test_decimal_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.DECIMAL64) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.DECIMAL64) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("blurb", YangDataType.DECIMAL64) == null);

        assertTrue(ValueHelper.fromLexicalRepresentation("20.03", YangDataType.DECIMAL64) instanceof BigDecimal);
        assertTrue(((BigDecimal) ValueHelper.fromLexicalRepresentation("20.03", YangDataType.DECIMAL64)).compareTo(
                BigDecimal.valueOf(20.03d)) == 0);
    }

    @Test
    public void test_string_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.STRING) == null);

        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.STRING) instanceof String);
        assertTrue(((String) ValueHelper.fromLexicalRepresentation("ABC", YangDataType.STRING)).equals("ABC"));
    }

    @Test
    public void test_boolean_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.BOOLEAN) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.BOOLEAN) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("blurb", YangDataType.BOOLEAN) == null);

        assertTrue(ValueHelper.fromLexicalRepresentation("false", YangDataType.BOOLEAN) instanceof Boolean);
        assertTrue(((Boolean) ValueHelper.fromLexicalRepresentation("false", YangDataType.BOOLEAN)).equals(Boolean.FALSE));
        assertTrue(((Boolean) ValueHelper.fromLexicalRepresentation("true", YangDataType.BOOLEAN)).equals(Boolean.TRUE));
    }

    @Test
    public void test_enumeration_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.ENUMERATION) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.ENUMERATION) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("ONE", YangDataType.ENUMERATION) instanceof String);
        assertTrue(((String) ValueHelper.fromLexicalRepresentation("ONE", YangDataType.ENUMERATION)).equals("ONE"));
    }

    @Test
    public void test_bits_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.BITS) == null);

        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.BITS) instanceof BitsValue);
        assertTrue(((BitsValue) ValueHelper.fromLexicalRepresentation("ONE", YangDataType.BITS)).getSetBits().size() == 1);
        assertTrue(((BitsValue) ValueHelper.fromLexicalRepresentation("  ONE   TWO 		", YangDataType.BITS))
                .getSetBits().size() == 2);
        assertTrue(((BitsValue) ValueHelper.fromLexicalRepresentation("ONE TWO", YangDataType.BITS)).getSetBits().contains(
                "ONE"));
        assertTrue(((BitsValue) ValueHelper.fromLexicalRepresentation("  ONE   TWO  ", YangDataType.BITS)).getSetBits()
                .contains("TWO"));
    }

    @Test
    public void test_binary_type() {
        assertTrue(ValueHelper.fromLexicalRepresentation(null, YangDataType.BINARY) == null);
        assertTrue(ValueHelper.fromLexicalRepresentation("", YangDataType.BINARY) instanceof BinaryValue);

        assertTrue(((BinaryValue) ValueHelper.fromLexicalRepresentation("SGVsbG8gV29ybGQh", YangDataType.BINARY))
                .getBinaryValue().length == 12);
        assertTrue(Arrays.equals(((BinaryValue) ValueHelper.fromLexicalRepresentation("SGVsbG8gV29ybGQh",
                YangDataType.BINARY)).getBinaryValue(), "Hello World!".getBytes()));
    }

}
