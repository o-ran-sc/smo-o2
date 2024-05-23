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
package org.oran.smo.yangtools.parser.findings;

import java.util.Objects;

/**
 * A predicate that is based on a severity calculator. If the calculator denotes the
 * severity of the finding to be SUPPRESS, the finding will be filtered.
 *
 * @author Mark Hollmann
 */
public class SeverityCalculatorFilterPredicate implements FindingFilterPredicate {

    private final FindingSeverityCalculator findingSeverityCalculator;

    public SeverityCalculatorFilterPredicate(final FindingSeverityCalculator findingSeverityCalculator) {
        this.findingSeverityCalculator = Objects.requireNonNull(findingSeverityCalculator);
    }

    public boolean findingTypeSuppressed(final String findingType) {
        return findingSeverityCalculator.calculateSeverity(findingType) == FindingSeverity.SUPPRESS;
    }

    @Override
    public boolean test(final Finding f) {
        return findingTypeSuppressed(f.getFindingType());
    }
}
