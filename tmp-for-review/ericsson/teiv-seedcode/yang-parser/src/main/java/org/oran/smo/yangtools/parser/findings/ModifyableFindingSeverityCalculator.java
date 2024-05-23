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

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that can be populated with a mapping from finding types to severities.
 *
 * @author Mark Hollmann
 */
public class ModifyableFindingSeverityCalculator implements FindingSeverityCalculator {

    private final Map<String, FindingSeverity> findingTypeToSeverity = new HashMap<>();

    public void errorForFinding(final String findingType) {
        findingTypeToSeverity.put(findingType, FindingSeverity.ERROR);
    }

    public void warningForFinding(final String findingType) {
        findingTypeToSeverity.put(findingType, FindingSeverity.WARNING);
    }

    public void infoForFinding(final String findingType) {
        findingTypeToSeverity.put(findingType, FindingSeverity.INFO);
    }

    public void suppressFinding(final String findingType) {
        findingTypeToSeverity.put(findingType, FindingSeverity.SUPPRESS);
    }

    public void setSeverityForFindingType(final String findingType, final FindingSeverity severity) {
        findingTypeToSeverity.put(findingType, severity);
    }

    @Override
    public FindingSeverity calculateSeverity(final String findingType) {
        final FindingSeverity severity = findingTypeToSeverity.get(findingType);
        return severity == null ? FindingSeverity.ERROR : severity;
    }

}
