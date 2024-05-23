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
package org.oran.smo.yangtools.parser.model.statements.yang;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;
import org.oran.smo.yangtools.parser.model.util.NumberHelper;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YRange extends AbstractStatement {

    public YRange(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_RANGE;
    }

    public String getRangeValues() {
        return domElement.getTrimmedValueOrEmpty();
    }

    public YErrorAppTag getErrorAppTag() {
        return getChild(CY.STMT_ERROR_APP_TAG);
    }

    public YErrorMessage getErrorMessage() {
        return getChild(CY.STMT_ERROR_MESSAGE);
    }

    protected void validate(final ParserExecutionContext context) {
        validateArgumentNotNullNotEmpty(context);
        checkUsedUnderIntegerOrDecimal64(context);
        validateBoundaries(context);
    }

    public void validateBoundaries(final ParserExecutionContext context) {
        final List<BoundaryPair> boundaries = getBoundaries();
        checkBoundaries(context, boundaries);
    }

    /**
     * Returns the boundaries for this range. "min" and "max" are resolved to the
     * minimum / maximum values for the data type.
     * <p>
     * The returned list contains pairings for each boundary part of the range.
     * Both upper and lower boundary are expressed as BigDecimal. Where the type
     * owning this range statement is integer, a client should invoke toBigInteger
     * to get the boundaries as integer values.
     */
    public List<BoundaryPair> getBoundaries() {
        /*
         * A range can only sit under type, so it's safe to do the typecast below.
         */
        return getBoundaries((YType) getParentStatement());
    }

    public List<BoundaryPair> getBoundaries(final YType forType) {
        final YangDataType yangDataType = DataTypeHelper.getYangDataType(forType.getDataType());
        final int fractionDigits = forType.hasAtLeastOneChildOf(CY.STMT_FRACTION_DIGITS) ?
                ((YFractionDigits) forType.getChild(CY.STMT_FRACTION_DIGITS)).getFractionDigits() :
                0;
        return getBoundaries(yangDataType, fractionDigits);
    }

    private List<BoundaryPair> getBoundaries(final YangDataType forDataType, final int fractionDigits) {

        /*
         * From the RFC:
         *
         * "A range consists of an explicit value, or a lower-inclusive bound,
         * two consecutive dots "..", and an upper-inclusive bound. Multiple
         * values or ranges can be given, separated by "|". If multiple values
         * or ranges are given, they all MUST be disjoint and MUST be in
         * ascending order. If a range restriction is applied to a type that is
         * already range-restricted, the new restriction MUST be equally
         * limiting or more limiting, i.e., raising the lower bounds, reducing
         * the upper bounds, removing explicit values or ranges, or splitting
         * ranges into multiple ranges with intermediate gaps. Each explicit
         * value and range boundary value given in the range expression MUST
         * match the type being restricted or be one of the special values "min"
         * or "max". "min" and "max" mean the minimum and maximum values
         * accepted for the type being restricted, respectively."
         *
         * Official definition is:
         *
         * range-boundary = min-keyword / max-keyword / integer-value / decimal-value
         */

        /*
         * The first problem that we are faced with is to know what actual values we
         * should use for min and max. That solely depends on the data type of the
         * parent 'type' statement. If this cannot be resolved for whatever reason we
         * return empty boundaries so that a client doesn't have to worry about null values.
         */
        final BigDecimal minValueForRange = getMinValueForRange(forDataType, fractionDigits);
        final BigDecimal maxValueForRange = getMaxValueForRange(forDataType, fractionDigits);

        if (minValueForRange == null || maxValueForRange == null) {
            return Collections.<BoundaryPair> emptyList();
        }

        /*
         * Now parse the range values string. This may very well throw somewhere if
         * the syntax has not been adhered to.
         */
        try {

            final List<BoundaryPair> result = new ArrayList<>();
            final String stringefiedRangeValues = getRangeValues().trim();

            if (stringefiedRangeValues.isEmpty() || stringefiedRangeValues.startsWith("|") || stringefiedRangeValues
                    .endsWith("|")) {
                // syntax error
                return Collections.<BoundaryPair> emptyList();
            }

            final String[] ranges = stringefiedRangeValues.contains("|") ?
                    stringefiedRangeValues.split("\\|") :
                    new String[] { stringefiedRangeValues };
            for (final String oneRange : ranges) {

                if (oneRange.trim().startsWith("..") || oneRange.trim().endsWith("..")) {
                    // syntax error
                    return Collections.<BoundaryPair> emptyList();
                }

                final String[] boundary = oneRange.contains("..") ?
                        oneRange.split("\\.\\.") :
                        new String[] { oneRange, oneRange };

                final String lowerBoundary = boundary[0].trim();
                final String upperBoundary = boundary[1].trim();

                BigDecimal lowerValue = null;
                if (lowerBoundary.equals("min")) {
                    lowerValue = minValueForRange;
                } else if (lowerBoundary.equals("max")) {
                    lowerValue = maxValueForRange;
                } else {
                    lowerValue = NumberHelper.extractYangIntegerValueOrDecimalValue(lowerBoundary);		// Note: May throw
                }

                BigDecimal upperValue = null;
                if (upperBoundary.equals("min")) {
                    upperValue = minValueForRange;
                } else if (upperBoundary.equals("max")) {
                    upperValue = maxValueForRange;
                } else {
                    upperValue = NumberHelper.extractYangIntegerValueOrDecimalValue(upperBoundary);		// Note: May throw
                }

                result.add(new BoundaryPair(lowerValue, upperValue));
            }

            return result;

        } catch (final Exception ex) {
            /* no-op */
        }

        /*
         * If we get here then an issue has occurred somewhere. We return empty boundaries
         * as these cannot be reliably established.
         */
        return Collections.<BoundaryPair> emptyList();
    }

    /**
     * Returns the data type of the parent 'type' statement.
     */
    private YangDataType getParentTypeDataType() {
        final AbstractStatement parentStatement = getParentStatement();
        return DataTypeHelper.getYangDataType(((YType) parentStatement).getDataType());
    }

    public void checkBoundaries(final ParserExecutionContext context, final List<BoundaryPair> boundaries) {
        /*
         * A range can only sit under type, so it's safe to do the typecast below.
         */
        checkBoundaries(context, boundaries, (YType) getParentStatement());
    }

    public void checkBoundaries(final ParserExecutionContext context, final List<BoundaryPair> boundaries,
            final YType forType) {

        final YangDataType yangDataType = DataTypeHelper.getYangDataType(forType.getDataType());
        final int fractionDigits = forType.hasAtLeastOneChildOf(CY.STMT_FRACTION_DIGITS) ?
                ((YFractionDigits) forType.getChild(CY.STMT_FRACTION_DIGITS)).getFractionDigits() :
                0;
        checkBoundaries(context, boundaries, yangDataType, fractionDigits);
    }

    private void checkBoundaries(final ParserExecutionContext context, final List<BoundaryPair> boundaries,
            final YangDataType forDataType, final int fractionDigits) {

        /*
         * Establish min/max values for the data type. If this cannot be done for whatever reason
         * (e.g. non-numeric data type) then the boundaries cannot be checked, and we return out.
         */
        final BigDecimal minValueForRange = getMinValueForRange(forDataType, fractionDigits);
        final BigDecimal maxValueForRange = getMaxValueForRange(forDataType, fractionDigits);

        if (minValueForRange == null || maxValueForRange == null) {
            return;
        }

        /*
         * If we don't have boundaries that would be a finding.
         */
        if (boundaries.isEmpty()) {
            context.addFinding(new Finding(this, ParserFindingType.P053_INVALID_VALUE,
                    "value '" + getRangeValues() + "' not valid for range."));
            return;
        }

        final boolean isIntegerType = DataTypeHelper.isYangIntegerType(getParentTypeDataType());

        /*
         * Rules:
         * - Each value must be within min/max values
         * - In each pair, upper must be >= lower.
         * - The lower of a pair must be >= the upper of the previous pair.
         */
        for (int i = 0; i < boundaries.size(); ++i) {
            boolean allOk = true;

            if (isIntegerType && boundaries.get(i).lower.stripTrailingZeros().scale() > 0) {
                allOk = false;
            }
            if (isIntegerType && boundaries.get(i).upper.stripTrailingZeros().scale() > 0) {
                allOk = false;
            }

            if (boundaries.get(i).lower.compareTo(minValueForRange) < 0) {
                allOk = false;
            }
            if (boundaries.get(i).upper.compareTo(minValueForRange) < 0) {
                allOk = false;
            }
            if (boundaries.get(i).lower.compareTo(maxValueForRange) > 0) {
                allOk = false;
            }
            if (boundaries.get(i).upper.compareTo(maxValueForRange) > 0) {
                allOk = false;
            }

            // lower <= upper ?
            if (boundaries.get(i).lower.compareTo(boundaries.get(i).upper) > 0) {
                allOk = false;
            }

            if (i > 0) {
                if (boundaries.get(i).lower.compareTo(boundaries.get(i - 1).upper) <= 0) {
                    allOk = false;
                }
            }

            if (!allOk) {
                context.addFinding(new Finding(this, ParserFindingType.P053_INVALID_VALUE,
                        "value '" + getRangeValues() + "' not valid for range."));
                return;
            }
        }
    }

    private void checkUsedUnderIntegerOrDecimal64(final ParserExecutionContext context) {
        final YangDataType dataType = getParentTypeDataType();

        if (dataType != YangDataType.DERIVED____TYPE && !dataTypeIsIntegerOrDecimal64(dataType)) {
            context.addFinding(new Finding(this, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT,
                    "'range' statement not allowed under type '" + dataType + "'."));
        }
    }

    private static boolean dataTypeIsIntegerOrDecimal64(final YangDataType dataType) {
        switch (dataType) {
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
            case DECIMAL64:
                return true;
            default:
                return false;
        }
    }

    public boolean isWithinRangeBoundaries(final BigDecimal toCheck) {
        for (final BoundaryPair boundaryPair : getBoundaries()) {
            if (toCheck.compareTo(boundaryPair.lower) >= 0 && toCheck.compareTo(boundaryPair.upper) <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the smallest possible value for the data type. May return null if this cannot be resolved.
     */
    private static BigDecimal getMinValueForRange(final YangDataType forDataType, final int fractionDigits) {

        if (DataTypeHelper.isYangIntegerType(forDataType)) {

            return (NumberHelper.getMinValueForYangIntegerDataType(forDataType));

        } else if (DataTypeHelper.isYangDecimal64Type(forDataType)) {
            /*
             * Type is decimal64.
             */
            return NumberHelper.getMinValueForYangDecimalDataType(fractionDigits);
        }

        return null;
    }

    /**
     * Returns the largest possible value for the data type. May return null if this cannot be resolved.
     */
    private static BigDecimal getMaxValueForRange(final YangDataType forDataType, final int fractionDigits) {

        if (DataTypeHelper.isYangIntegerType(forDataType)) {

            return (NumberHelper.getMaxValueForYangIntegerDataType(forDataType));

        } else if (DataTypeHelper.isYangDecimal64Type(forDataType)) {
            /*
             * Type is decimal64.
             */
            return NumberHelper.getMaxValueForYangDecimalDataType(fractionDigits);
        }

        return null;
    }

    public static class BoundaryPair {
        public final BigDecimal lower;
        public final BigDecimal upper;

        public BoundaryPair(final BigDecimal lower, final BigDecimal upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
}
