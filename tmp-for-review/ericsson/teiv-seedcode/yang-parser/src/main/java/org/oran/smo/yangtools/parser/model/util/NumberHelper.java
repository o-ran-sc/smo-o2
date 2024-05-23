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
package org.oran.smo.yangtools.parser.model.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;

/**
 * Utility class to deal with numbers in Yang.
 *
 * @author Mark Hollmann
 */
public abstract class NumberHelper {

    /*
     * integer-value = ("-" non-negative-integer-value) / non-negative-integer-value
     * non-negative-integer-value = "0" / positive-integer-value
     * positive-integer-value = (non-zero-digit *DIGIT)
     * non-zero-digit = %x31-39
     * decimal-value = integer-value ("." zero-integer-value)
     * zero-integer-value = 1*DIGIT
     */

    /**
     * Extracts a decimal number representing a yang "integer-value" or "decimal-value". Note that a BigDecimal
     * will be returned even if the underlying number is mathematically integer.
     */
    public static BigDecimal extractYangIntegerValueOrDecimalValue(final String string) {
        return new BigDecimal(string);
    }

    public static final BigDecimal INT8_MIN_VALUE = new BigDecimal("-128");
    public static final BigDecimal INT16_MIN_VALUE = new BigDecimal("-32768");
    public static final BigDecimal INT32_MIN_VALUE = new BigDecimal("-2147483648");
    public static final BigDecimal INT64_MIN_VALUE = new BigDecimal("-9223372036854775808");

    public static final BigDecimal UINT8_MAX_VALUE = new BigDecimal("255");
    public static final BigDecimal UINT16_MAX_VALUE = new BigDecimal("65535");
    public static final BigDecimal UINT32_MAX_VALUE = new BigDecimal("4294967295");
    public static final BigDecimal UINT64_MAX_VALUE = new BigDecimal("18446744073709551615");

    public static final BigDecimal INT8_MAX_VALUE = new BigDecimal("127");
    public static final BigDecimal INT16_MAX_VALUE = new BigDecimal("32767");
    public static final BigDecimal INT32_MAX_VALUE = new BigDecimal("2147483647");
    public static final BigDecimal INT64_MAX_VALUE = new BigDecimal("9223372036854775807");

    public static BigDecimal getMinValueForYangIntegerDataType(final YangDataType yangDataType) {

        switch (yangDataType) {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return BigDecimal.ZERO;
            case INT8:
                return INT8_MIN_VALUE;
            case INT16:
                return INT16_MIN_VALUE;
            case INT32:
                return INT32_MIN_VALUE;
            case INT64:
                return INT64_MIN_VALUE;
            default:
                throw new RuntimeException("Not an integer data type");
        }
    }

    public static BigDecimal getMaxValueForYangIntegerDataType(final YangDataType yangDataType) {

        switch (yangDataType) {
            case UINT8:
                return UINT8_MAX_VALUE;
            case UINT16:
                return UINT16_MAX_VALUE;
            case UINT32:
                return UINT32_MAX_VALUE;
            case UINT64:
                return UINT64_MAX_VALUE;
            case INT8:
                return INT8_MAX_VALUE;
            case INT16:
                return INT16_MAX_VALUE;
            case INT32:
                return INT32_MAX_VALUE;
            case INT64:
                return INT64_MAX_VALUE;
            default:
                throw new RuntimeException("Not an integer data type");
        }
    }

    public static BigDecimal getMinValueForYangDecimalDataType(final int digits) {
        return (digits >= 1 && digits <= 18) ?
                new BigDecimal("-9223372036854775808").divide(BigDecimal.valueOf(10L).pow(digits)) :
                null;
    }

    public static BigDecimal getMaxValueForYangDecimalDataType(final int digits) {
        return (digits >= 1 && digits <= 18) ?
                new BigDecimal("9223372036854775807").divide(BigDecimal.valueOf(10L).pow(digits)) :
                null;
    }

    /**
     * Returns the integer value (in a BigInteger) of a default value. Will return null
     * if the supplied string cannot be translated to a BigInteger.
     * <p>
     * Special handling applies as follows (taken from the RFC):
     * <p>
     * "For convenience, when specifying a default value for an integer in a
     * YANG module, an alternative lexical representation can be used that
     * represents the value in a hexadecimal or octal notation. The
     * hexadecimal notation consists of an optional sign ("+" or "-"),
     * followed by the characters "0x", followed by a number of hexadecimal
     * digits where letters may be uppercase or lowercase. The octal
     * notation consists of an optional sign ("+" or "-"), followed by the
     * character "0", followed by a number of octal digits.
     * <p>
     * Note that if a default value in a YANG module has a leading zero
     * ("0"), it is interpreted as an octal number. In the XML encoding, an
     * integer is always interpreted as a decimal number, and leading zeros
     * are allowed."
     */
    public static BigInteger getIntegerDefaultValue(final String stringefiedValue) {

        try {
            if (stringefiedValue.equals("0") || stringefiedValue.equals("+0") || stringefiedValue.equals("-0")) {
                return BigInteger.ZERO;
            } else if (stringefiedValue.startsWith("-0x")) {
                return new BigInteger(stringefiedValue.substring(3), 16).negate();
            } else if (stringefiedValue.startsWith("+0x")) {
                return new BigInteger(stringefiedValue.substring(3), 16);
            } else if (stringefiedValue.startsWith("0x")) {
                return new BigInteger(stringefiedValue.substring(2), 16);
            } else if (stringefiedValue.startsWith("-0")) {
                return new BigInteger(stringefiedValue.substring(2), 8).negate();
            } else if (stringefiedValue.startsWith("+0")) {
                return new BigInteger(stringefiedValue.substring(2), 8);
            } else if (stringefiedValue.startsWith("0")) {
                return new BigInteger(stringefiedValue.substring(1), 8);
            }

            return getIntegerValue(stringefiedValue);

        } catch (final Exception ignored) {
            /* no-op */
        }

        return null;
    }

    /**
     * Returns the integer value (in a BigInteger) of a value. Will return null
     * if the supplied string cannot be translated to a BigInteger.
     * <p>
     * Note that special default value handling is <b>not</b> applied by this
     * method, i.e. a leading zero is interpreted as the number zero.
     */
    public static BigInteger getIntegerValue(final String stringefiedValue) {
        try {
            return new BigInteger(stringefiedValue);
        } catch (final Exception ignored) {
            /* no-op */
        }

        return null;
    }

    /**
     * Returns the decimal value (in a BigDecimal) of a value. Will return null
     * if the supplied string cannot be translated to a BigDecimal.
     * <p>
     * Note that special handling (as for integers above) does NOT apply for
     * decimal values.
     */
    public static BigDecimal getDecimalValue(final String stringefiedValue) {
        try {
            return new BigDecimal(stringefiedValue);
        } catch (final Exception ex) {
            /* no-op */
        }

        return null;
    }
}
