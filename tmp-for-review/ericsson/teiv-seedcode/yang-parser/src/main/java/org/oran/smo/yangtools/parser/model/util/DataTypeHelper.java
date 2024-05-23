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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YBit;
import org.oran.smo.yangtools.parser.model.statements.yang.YEnum;
import org.oran.smo.yangtools.parser.model.statements.yang.YLength;
import org.oran.smo.yangtools.parser.model.statements.yang.YPattern;
import org.oran.smo.yangtools.parser.model.statements.yang.YPosition;
import org.oran.smo.yangtools.parser.model.statements.yang.YRange.BoundaryPair;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.statements.yang.YValue;

/**
 * Utility class handling data types.
 *
 * @author Mark Hollmann
 */
public abstract class DataTypeHelper {

    public static boolean isYangNumericType(final String dataType) {
        return isYangNumericType(getYangDataType(dataType));
    }

    public static boolean isYangNumericType(final YangDataType yangDataType) {
        return isYangIntegerType(yangDataType) || isYangDecimal64Type(yangDataType);
    }

    public static boolean isYangIntegerType(final String dataType) {
        return isYangIntegerType(getYangDataType(dataType));
    }

    public static boolean isYangIntegerType(final YangDataType yangDataType) {
        return isYangSignedIntegerType(yangDataType) || isYangUnsignedIntegerType(yangDataType);
    }

    public static boolean isYangSignedIntegerType(final String dataType) {
        return isYangSignedIntegerType(getYangDataType(dataType));
    }

    public static boolean isYangSignedIntegerType(final YangDataType yangDataType) {
        switch (yangDataType) {
            case INT8:
            case INT16:
            case INT32:
            case INT64:
                return true;
            default:
                return false;
        }
    }

    public static boolean isYangUnsignedIntegerType(final String dataType) {
        return isYangUnsignedIntegerType(getYangDataType(dataType));
    }

    public static boolean isYangUnsignedIntegerType(final YangDataType yangDataType) {
        switch (yangDataType) {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return true;
            default:
                return false;
        }
    }

    public static boolean isYangDecimal64Type(final String dataType) {
        return isYangDecimal64Type(getYangDataType(dataType));
    }

    public static boolean isYangDecimal64Type(final YangDataType yangDataType) {
        return yangDataType == YangDataType.DECIMAL64;
    }

    /**
     * Returns whether the datatype is one of the built-in YANG data types.
     */
    public static boolean isBuiltInType(final String dataType) {
        return getYangDataType(dataType) != YangDataType.DERIVED____TYPE;
    }

    /**
     * Returns whether the datatype is a derived type.
     */
    public static boolean isDerivedType(final String dataType) {
        return getYangDataType(dataType) == YangDataType.DERIVED____TYPE;
    }

    public static boolean isUnionType(final String dataType) {
        return isUnionType(getYangDataType(dataType));
    }

    public static boolean isUnionType(final YangDataType yangDataType) {
        return yangDataType == YangDataType.UNION;
    }

    public static boolean isEmptyType(final String dataType) {
        return isEmptyType(getYangDataType(dataType));
    }

    public static boolean isEmptyType(final YangDataType yangDataType) {
        return yangDataType == YangDataType.EMPTY;
    }

    public static boolean isStringType(final String dataType) {
        return isStringType(getYangDataType(dataType));
    }

    public static boolean isStringType(final YangDataType yangDataType) {
        return yangDataType == YangDataType.STRING;
    }

    public static boolean isBinaryType(final String dataType) {
        return isBinaryType(getYangDataType(dataType));
    }

    public static boolean isBinaryType(final YangDataType yangDataType) {
        return yangDataType == YangDataType.BINARY;
    }

    /**
     * Returns the YANG datatype, which can be one of the build-in data types, or a
     * derived data type.
     */
    public static YangDataType getYangDataType(final String dataType) {

        if (dataType.equals("instance-identifier")) {
            return YangDataType.INSTANCE_IDENTIFIER;
        }

        if (dataType.contains(":")) {
            return YangDataType.DERIVED____TYPE;
        }

        try {
            return YangDataType.valueOf(dataType.toUpperCase());
        } catch (final Exception ex) {
            /* no-op */
        }

        /*
         * Derived type being used without prefix. According to RFC this seems to be
         * allowed...? Really?
         */
        return YangDataType.DERIVED____TYPE;
    }

    public enum YangDataType {
        INT8,
        INT16,
        INT32,
        INT64,
        UINT8,
        UINT16,
        UINT32,
        UINT64,
        DECIMAL64,
        STRING,
        BOOLEAN,
        ENUMERATION,
        BITS,
        BINARY,
        LEAFREF,
        IDENTITYREF,
        EMPTY,
        UNION,
        INSTANCE_IDENTIFIER,
        DERIVED____TYPE
    }

    /**
     * Given a 'type' statement, computes the position for each of the bits within.
     * The rules as outlined in chapter 9.7.4.2 of the RFC are applied.
     * <p>
     * This method allows a pre-defined mapping to be supplied. This may be used in
     * a situation where a bits data type has been restricted, and the numeric
     * position has to be used from the typedef where the bits have been defined.
     * <p>
     * The returned value maps bit names to their position value.
     *
     * @param predefinedMapping
     *     may be null
     */
    public static Map<String, Long> calculatePositionOfBits(final FindingsManager findingsManager, final YType type,
            final Map<String, Long> predefinedMapping) {

        final Map<String, Long> result = new HashMap<>();

        /*
         * Calculate the numeric position of each bit in the type.
         */
        long nextImplicitBitPosition = 0;
        for (final YBit bit : type.getBits()) {

            final String bitName = bit.getBitName();

            /*
             * Position can be either explicit or implicit. If it is implicit, then it is
             * "one higher than the previous value". If it is explicit, then there is a
             * 'position' statement under the bit statement. If neither is the case, it will
             * be taken from the predefined mapping, if possible.
             */
            Long positionOfThisBit = Long.valueOf(nextImplicitBitPosition);

            if (bit.getPosition() != null) {
                final YPosition yPosition = bit.getPosition();
                positionOfThisBit = Long.valueOf(yPosition.getPosition());
            } else if (predefinedMapping != null && predefinedMapping.containsKey(bitName)) {
                positionOfThisBit = predefinedMapping.get(bitName);
            }

            if (positionOfThisBit.longValue() > YPosition.MAX_POSITION_VALUE && findingsManager != null) {
                findingsManager.addFinding(new Finding(bit, ParserFindingType.P053_INVALID_VALUE,
                        "bit position value larger than '" + YPosition.MAX_POSITION_VALUE + "'."));
            }

            if (result.containsValue(positionOfThisBit) && findingsManager != null) {
                findingsManager.addFinding(new Finding(bit, ParserFindingType.P053_INVALID_VALUE,
                        "Duplicate bit position value '" + positionOfThisBit + "'."));
            }

            result.put(bitName, positionOfThisBit);

            nextImplicitBitPosition = positionOfThisBit.longValue() + 1;
        }

        return result;
    }

    /**
     * Does the exact same thing as the previous method, but for data type
     * enumeration.
     *
     * @param predefinedMapping
     *     may be null
     */
    public static Map<String, Long> calculateValuesOfEnums(final FindingsManager findingsManager, final YType type,
            final Map<String, Long> predefinedMapping) {

        final Map<String, Long> result = new HashMap<>();

        /*
         * Calculate the numeric value of each enum in the type.
         */
        long nextImplicitEnumValue = 0;
        for (final YEnum oneEnum : type.getEnums()) {

            final String enumName = oneEnum.getEnumName();

            /*
             * Value can be either explicit or implicit. If it is implicit, then it is "one
             * higher than the previous value". If it is explicit, then there is a 'value'
             * statement under the enum statement. If neither is the case, it will be taken
             * from the predefined mapping, if possible.
             */
            Long valueOfThisEnum = Long.valueOf(nextImplicitEnumValue);

            if (oneEnum.getValue() != null) {
                final YValue value = oneEnum.getValue();
                valueOfThisEnum = Long.valueOf(value.getEnumValue());
            } else if (predefinedMapping != null && predefinedMapping.containsKey(enumName)) {
                valueOfThisEnum = predefinedMapping.get(enumName);
            }

            if ((valueOfThisEnum.longValue() < (long) Integer.MIN_VALUE || valueOfThisEnum
                    .longValue() > (long) Integer.MAX_VALUE) && findingsManager != null) {
                findingsManager.addFinding(new Finding(oneEnum, ParserFindingType.P053_INVALID_VALUE,
                        "enum value outside allowed range."));
            }

            if (result.containsValue(valueOfThisEnum) && findingsManager != null) {
                findingsManager.addFinding(new Finding(oneEnum, ParserFindingType.P053_INVALID_VALUE,
                        "Duplicate enum value '" + valueOfThisEnum + "'."));
            }

            result.put(enumName, valueOfThisEnum);

            nextImplicitEnumValue = valueOfThisEnum.longValue() + 1;
        }

        return result;
    }

    /**
     * Given a value as string, is this value a valid value in respect of the
     * non-union data type supplied, considering constraints?
     */
    public static boolean isStringefiedValueValid(final String stringefiedValue, final YType yType) {

        final YangDataType yangDataType = DataTypeHelper.getYangDataType(yType.getDataType());

        if (yangDataType == YangDataType.UNION) {
            throw new RuntimeException("This method does not handle union.");
        }

        switch (yangDataType) {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
            case INT8:
            case INT16:
            case INT32:
            case INT64:
                return isValidIntegerValue(stringefiedValue, yType);
            case DECIMAL64:
                return isValidDecimalValue(stringefiedValue, yType);
            case BOOLEAN:
                return isValidBooleanValue(stringefiedValue, yType);
            case ENUMERATION:
                return isValidEnumerationValue(stringefiedValue, yType);
            case BITS:
                return isValidBitsValue(stringefiedValue, yType);
            case STRING:
                return isValidStringValue(stringefiedValue, yType);
            case EMPTY:
                return isValidEmptyValue(stringefiedValue, yType);
            default:
                break;
        }

        return true;
    }

    public static boolean isValidIntegerValue(final String stringefiedValue, final YType yType) {

        if (stringefiedValue == null) {
            return false;
        }

        final BigInteger integerValue = NumberHelper.getIntegerDefaultValue(stringefiedValue);
        if (integerValue == null) {
            return false;
        }

        if (!isIntegerValueCorrectInRespectOfSize(integerValue, yType)) {
            return false;
        }

        return isIntegerValueCorrectInRespectOfConstrainedRange(integerValue, yType);
    }

    public static boolean isIntegerValueCorrectInRespectOfSize(final BigInteger integerValue, final YType yType) {

        final YangDataType yangDataType = DataTypeHelper.getYangDataType(yType.getDataType());
        final BigInteger integerMinValue = NumberHelper.getMinValueForYangIntegerDataType(yangDataType).toBigIntegerExact();
        final BigInteger integerMaxValue = NumberHelper.getMaxValueForYangIntegerDataType(yangDataType).toBigIntegerExact();

        return (integerValue.compareTo(integerMinValue) >= 0 && integerValue.compareTo(integerMaxValue) <= 0);
    }

    public static boolean isIntegerValueCorrectInRespectOfConstrainedRange(final BigInteger integerValue,
            final YType yType) {

        if (yType.getRange() == null) {
            return true;
        }

        boolean withinOneOfTheBoundaries = false;
        for (final BoundaryPair boundary : yType.getRange().getBoundaries()) {
            if (integerValue.compareTo(boundary.lower.toBigIntegerExact()) >= 0 && integerValue.compareTo(boundary.upper
                    .toBigIntegerExact()) <= 0) {
                withinOneOfTheBoundaries = true;
                break;
            }
        }
        return withinOneOfTheBoundaries;
    }

    public static boolean isValidDecimalValue(final String stringefiedValue, final YType yType) {

        if (stringefiedValue == null) {
            return false;
        }

        if (yType.getFractionDigits() == null) {
            /*
             * We cannot ascertain if the default value is ok, as the fraction-digits
             * statement is missing. That would have caused a separate finding anyway.
             */
            return true;
        }

        final BigDecimal decimalValue = NumberHelper.getDecimalValue(stringefiedValue);
        if (decimalValue == null) {
            return false;
        }

        if (!isDecimalValueCorrectInRespectOfSize(decimalValue, yType)) {
            return false;
        }

        return isDecimalValueCorrectInRespectOfConstrainedRange(decimalValue, yType);
    }

    public static boolean isDecimalValueCorrectInRespectOfSize(final BigDecimal decimalValue, final YType yType) {

        final int fractionDigits = yType.getFractionDigits().getFractionDigits();
        final BigDecimal decimalMinValue = NumberHelper.getMinValueForYangDecimalDataType(fractionDigits);
        final BigDecimal decimalMaxValue = NumberHelper.getMaxValueForYangDecimalDataType(fractionDigits);
        if (decimalValue.compareTo(decimalMinValue) < 0 || decimalValue.compareTo(decimalMaxValue) > 0) {
            return false;
        }

        return (decimalValue.stripTrailingZeros().scale() <= yType.getFractionDigits().getFractionDigits());
    }

    public static boolean isDecimalValueCorrectInRespectOfConstrainedRange(final BigDecimal decimalValue,
            final YType yType) {

        if (yType.getRange() == null) {
            return true;
        }

        boolean withinOneOfTheBoundaries = false;
        for (final BoundaryPair boundary : yType.getRange().getBoundaries()) {
            if (decimalValue.compareTo(boundary.lower) >= 0 && decimalValue.compareTo(boundary.upper) <= 0) {
                withinOneOfTheBoundaries = true;
                break;
            }
        }

        return withinOneOfTheBoundaries;
    }

    public static boolean isValidBooleanValue(final String stringefiedValue, final YType yType) {
        return ("true".equals(stringefiedValue) || "false".equals(stringefiedValue));
    }

    public static boolean isValidEnumerationValue(final String stringefiedValue, final YType yType) {

        if (stringefiedValue == null) {
            return false;
        }

        return findEnum(stringefiedValue, yType) != null;
    }

    public static YEnum findEnum(final String stringefiedValue, final YType yType) {
        for (final YEnum enumStatement : yType.getEnums()) {
            if (enumStatement.getEnumName().equals(stringefiedValue)) {
                return enumStatement;
            }
        }
        return null;
    }

    public static boolean isValidBitsValue(final String stringefiedValue, final YType yType) {

        if (stringefiedValue == null) {
            return false;
        }

        if (!isBitsValueCorrectInRespectOfUniqueness(stringefiedValue, yType)) {
            return false;
        }

        return isBitsValueCorrectInRespectOfNames(stringefiedValue, yType);
    }

    public static boolean isBitsValueCorrectInRespectOfUniqueness(final String stringefiedValue, final YType yType) {
        final List<String> stringList = GrammarHelper.parseToStringList(stringefiedValue);
        return stringList.size() == new HashSet<>(stringList).size();
    }

    public static boolean isBitsValueCorrectInRespectOfNames(final String stringefiedValue, final YType yType) {

        final Set<String> bitsValue = new HashSet<>(GrammarHelper.parseToStringList(stringefiedValue));
        final Set<String> bitNames = yType.getBits().stream().map(bit -> bit.getBitName()).collect(Collectors.toSet());

        bitsValue.removeAll(bitNames);
        return bitsValue.isEmpty();
    }

    private static boolean isValidStringValue(final String stringefiedValue, final YType yType) {

        if (stringefiedValue == null) {
            return false;
        }

        if (!isStringValueCorrectInRespectOfConstrainedLength(stringefiedValue, yType)) {
            return false;
        }

        return isStringValueCorrectInRespectOfPatterns(stringefiedValue, yType);
    }

    public static boolean isStringValueCorrectInRespectOfConstrainedLength(final String stringValue, final YType yType) {

        if (yType.getLength() == null) {
            return true;
        }

        boolean withinOneOfTheBoundaries = false;
        for (final YLength.BoundaryPair boundary : yType.getLength().getBoundaries()) {
            if (stringValue.length() >= boundary.lower && stringValue.length() <= boundary.upper) {
                withinOneOfTheBoundaries = true;
                break;
            }
        }

        return withinOneOfTheBoundaries;
    }

    private static final String COMPILED_PATTERN = "COMPILED_PATTERN";

    public static boolean isStringValueCorrectInRespectOfPatterns(final String stringValue, final YType yType) {

        for (final YPattern yPattern : yType.getPatterns()) {

            try {
                /*
                 * Performance improvement to re-use a compiled pattern.
                 */
                Pattern compiledPattern = yPattern.getCustomAppData(COMPILED_PATTERN);
                if (compiledPattern == null) {
                    compiledPattern = Pattern.compile(yPattern.getPattern());
                    yPattern.setCustomAppData(COMPILED_PATTERN, compiledPattern);
                }

                if (!compiledPattern.matcher(stringValue).matches()) {
                    return false;
                }
            } catch (final PatternSyntaxException ignored) {
                /* ignore - a finding would have been issued on this already */
            }
        }

        return true;
    }

    public static boolean isValidEmptyValue(final String stringefiedValue, final YType yType) {
        /*
         * An empty value is represented as empty element in XML when transferred
         * over NETCONF, so we need to handle it.
         */
        return stringefiedValue == null || stringefiedValue.isEmpty();
    }
}
