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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YLength extends AbstractStatement {

    private static final long MIN_VALUE_FOR_LENGTH = 0;
    private static final long MAX_VALUE_FOR_LENGTH = Long.MAX_VALUE;

    public YLength(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_LENGTH;
    }

    public String getLengthValue() {
        return domElement.getTrimmedValueOrEmpty();
    }

    public YErrorAppTag getErrorAppTag() {
        return getChild(CY.STMT_ERROR_APP_TAG);
    }

    public YErrorMessage getErrorMessage() {
        return getChild(CY.STMT_ERROR_MESSAGE);
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        validateBoundaries(context);
    }

    public void validateBoundaries(final ParserExecutionContext context) {
        final List<BoundaryPair> boundaries = getBoundaries();
        checkBoundaries(context, boundaries);
    }

    /**
     * Returns the boundaries for this length. "min" and "max" are zero / Long.MAX respectively.
     */
    public List<BoundaryPair> getBoundaries() {
        /*
         * From the RFC:
         *
         * "A length range consists of an explicit value, or a lower bound, two
         * consecutive dots "..", and an upper bound. Multiple values or ranges
         * can be given, separated by "|". Length-restricting values MUST NOT
         * be negative. If multiple values or ranges are given, they all MUST
         * be disjoint and MUST be in ascending order.
         *
         * Official definition is:
         *
         * length-boundary = min-keyword / max-keyword / non-negative-integer-value
        // From the RFC:
        //
        // length-arg = length-part *(optsep "|" optsep length-part)
        //
        // length-part = length-boundary [optsep ".." optsep length-boundary]
        //
        // length-boundary = min-keyword / max-keyword / non-negative-integer-value
        //
        // max-keyword = "max"
        // min-keyword = "min"
         */

        try {
            final List<BoundaryPair> boundaries = new ArrayList<>();
            final String stringefiedLengthValues = getLengthValue().trim();
            if (stringefiedLengthValues.isEmpty() || stringefiedLengthValues.startsWith("|") || stringefiedLengthValues
                    .endsWith("|")) {
                return Collections.<BoundaryPair> emptyList();
            }

            final String[] lengths = stringefiedLengthValues.contains("|") ?
                    stringefiedLengthValues.split("\\|") :
                    new String[] { stringefiedLengthValues };
            for (final String oneLength : lengths) {

                if (oneLength.trim().startsWith("..") || oneLength.trim().endsWith("..")) {
                    return Collections.<BoundaryPair> emptyList();
                }

                final String[] boundary = oneLength.contains("..") ?
                        oneLength.split("\\.\\.") :
                        new String[] { oneLength, oneLength };

                final String lowerBoundary = boundary[0].trim();
                final String upperBoundary = boundary[1].trim();

                long lowerValue = 0;
                if (lowerBoundary.equals("min")) {
                    lowerValue = MIN_VALUE_FOR_LENGTH;
                } else if (lowerBoundary.equals("max")) {
                    lowerValue = MAX_VALUE_FOR_LENGTH;
                } else {
                    lowerValue = Long.parseLong(lowerBoundary);		// this may throw
                }

                long upperValue = 0;
                if (upperBoundary.equals("min")) {
                    upperValue = MIN_VALUE_FOR_LENGTH;
                } else if (upperBoundary.equals("max")) {
                    upperValue = MAX_VALUE_FOR_LENGTH;
                } else {
                    upperValue = Long.parseLong(upperBoundary);		// this may throw
                }

                boundaries.add(new BoundaryPair(lowerValue, upperValue));
            }

            return boundaries;

        } catch (final Exception ex) {
            /* no-op */
        }

        /*
         * If we get here then an issue has occurred somewhere. We return empty boundaries
         * as these cannot be reliably established.
         */
        return Collections.<BoundaryPair> emptyList();
    }

    public void checkBoundaries(final ParserExecutionContext context, final List<BoundaryPair> boundaries) {

        if (boundaries.isEmpty()) {
            context.addFinding(new Finding(this, ParserFindingType.P053_INVALID_VALUE,
                    "value '" + getLengthValue() + "' not valid for length."));
            return;
        }

        /*
         * Rules:
         * - Each value must be >= 0
         * - In each pair, upper must be >= lower.
         * - The lower of a pair must be >= the upper of the previous pair.
         */
        for (int i = 0; i < boundaries.size(); ++i) {
            boolean allOk = true;

            if (boundaries.get(i).lower < 0) {
                allOk = false;
            }
            if (boundaries.get(i).upper < 0) {
                allOk = false;
            }

            // lower <= upper ?
            if (boundaries.get(i).lower > boundaries.get(i).upper) {
                allOk = false;
            }

            if (i > 0 && boundaries.get(i).lower <= boundaries.get(i - 1).upper) {
                allOk = false;
            }

            if (!allOk) {
                context.addFinding(new Finding(this, ParserFindingType.P053_INVALID_VALUE,
                        "value '" + getLengthValue() + "' not valid for 'length'."));
                return;
            }
        }
    }

    public boolean isWithinLengthBoundaries(final long toCheck) {
        for (final BoundaryPair boundaryPair : getBoundaries()) {
            if (toCheck >= boundaryPair.lower && toCheck <= boundaryPair.upper) {
                return true;
            }
        }
        return false;
    }

    public static class BoundaryPair {
        public final long lower;
        public final long upper;

        public BoundaryPair(final long lower, final long upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
}
