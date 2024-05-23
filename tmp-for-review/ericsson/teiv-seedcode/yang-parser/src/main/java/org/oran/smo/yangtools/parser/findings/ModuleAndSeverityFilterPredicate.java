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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.oran.smo.yangtools.parser.model.ModuleIdentity;

/**
 * A predicate that takes one or more modules, and one or more severities.
 *
 * @Author Mark Hollmann
 */
public class ModuleAndSeverityFilterPredicate implements FindingFilterPredicate {

    /**
     * Parses the supplied string into an instance of ModuleAndSeverityFilterPredicate.
     * <p>
     * Module names are separated from severities by the ";" character.
     * <p>
     * Module names are separated by the "," character. Severities are likewise separated by
     * the "," character. A value must be supplied for both module names and severities.
     * <p>
     * The only allowable wildcard character is a "*", denoting any "character sequence".
     * <p>
     * Example: The following will suppress all findings of severity INFO and WARNING in any IETF
     * and IANA modules: "ietf-*,iana-*;INFO,WARNING"
     */
    public static ModuleAndSeverityFilterPredicate fromString(final String s,
            final FindingSeverityCalculator findingSeverityCalculator) {

        final String[] split = s.split(";");
        if (split.length != 2) {
            throw new RuntimeException("Invalid string format for ModuleAndSeverityFilterPredicate.");
        }

        final List<Pattern> moduleNames = new ArrayList<>();
        if (!split[0].equals("*")) {
            final String[] moduleNamesSplit = split[0].contains(",") ? split[0].split(",") : new String[] { split[0] };
            for (final String stringPattern : moduleNamesSplit) {
                moduleNames.add(Pattern.compile(stringPattern.trim().replace(".", "[.]").replace("*", ".*")));
            }
        }

        final Set<FindingSeverity> severities = new HashSet<>();
        if (!split[1].equals("*")) {
            final String[] severitiesSplit = split[1].contains(",") ? split[1].split(",") : new String[] { split[1] };
            for (final String severity : severitiesSplit) {
                severities.add(FindingSeverity.valueOf(severity.trim().toUpperCase()));
            }
        }

        return new ModuleAndSeverityFilterPredicate(moduleNames, severities, findingSeverityCalculator);
    }

    private final List<Pattern> moduleNames;
    private final Set<FindingSeverity> severities;
    private final FindingSeverityCalculator findingSeverityCalculator;

    /**
     * A finding will be filtered if the statement is part of any of the supplied modules,
     * and has any of the supplied severities.
     * <p>
     * More formally, the name of the module in which the offending statement sits must be
     * matchable against any of the module name patterns, and the severity of the finding must
     * be part of the supplied set of severities.
     * <p>
     * Supplying an empty list for module names, or empty set for severities, will match-all
     * for that parameter.
     */
    public ModuleAndSeverityFilterPredicate(final List<Pattern> moduleNames, final Set<FindingSeverity> severities,
            final FindingSeverityCalculator findingSeverityCalculator) {
        this.moduleNames = Objects.requireNonNull(moduleNames);
        this.severities = Objects.requireNonNull(severities);
        this.findingSeverityCalculator = Objects.requireNonNull(findingSeverityCalculator);
    }

    @Override
    public boolean test(final Finding f) {
        return matchOnModule(f) && matchOnSeverity(f);
    }

    private boolean matchOnModule(final Finding finding) {

        if (moduleNames.isEmpty()) {
            return true;
        }

        /*
         * If the finding does not relate to a YAM then obviously we cannot match.
         */
        if (finding.getYangModel() == null) {
            return false;
        }

        /*
         * It can happen that we don't have a module identity yet, because a finding was found
         * before we actually got a chance to extract the module name. In this case we use the
         * name of the input. This is not foolproof, of course.
         */
        final ModuleIdentity moduleIdentity = finding.getYangModel().getModuleIdentity();
        final String moduleOrSubModuleName = moduleIdentity == null ?
                finding.getYangModel().getYangInput().getName() :
                moduleIdentity.getModuleName();

        for (final Pattern pattern : moduleNames) {
            if (pattern.matcher(moduleOrSubModuleName).matches()) {
                return true;
            }
        }

        return false;
    }

    private boolean matchOnSeverity(final Finding finding) {

        if (severities.isEmpty()) {
            return true;
        }

        return severities.contains(findingSeverityCalculator.calculateSeverity(finding.getFindingType()));
    }
}
