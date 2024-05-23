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
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * A predicate that takes one or more modules, one or more finding types, and one or more
 * schema node paths.
 *
 * @author Mark Hollmann
 */
public class ModuleAndFindingTypeAndSchemaNodePathFilterPredicate implements FindingFilterPredicate {

    /**
     * Parses the supplied string into an instance of ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.
     * <p>
     * Module names are separated from finding types, and from the node path, by the ";" character.
     * <p>
     * Module names are separated by the "," character. Finding types are likewise separated by
     * the "," character. Only one path may be supplied at most. A value must be supplied for module
     * names, finding types and path.
     * <p>
     * The only allowable wildcard character is a "*", denoting any "character sequence".
     * <p>
     * Example 1: The following will suppress all P114 findings in any IETF and IANA modules:
     * "ietf-*,iana-*;P114_TYPEDEF_NOT_USED;*"
     * <p>
     * Example 2: The following will suppress all findings within the "modules-state" container
     * within the IETF yang library: "ietf-yang-library;*;/container=modules-state"
     * <p>
     * Example 3: The following will suppress all P115 findings, in all modules: "*;P115_*;*"
     */
    public static ModuleAndFindingTypeAndSchemaNodePathFilterPredicate fromString(final String s) {

        final String[] split = s.split(";");
        if (split.length != 3) {
            throw new RuntimeException("Invalid string format for ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.");
        }

        final List<Pattern> moduleNames = new ArrayList<>();
        if (!split[0].equals("*")) {
            final String[] moduleNamesSplit = split[0].contains(",") ? split[0].split(",") : new String[] { split[0] };
            for (final String stringPattern : moduleNamesSplit) {
                moduleNames.add(Pattern.compile(stringPattern.trim().replace(".", "[.]").replace("*", ".*")));
            }
        }

        final List<Pattern> findingTypes = new ArrayList<>();
        if (!split[1].equals("*")) {
            final String[] findingTypesSplit = split[1].contains(",") ? split[1].split(",") : new String[] { split[1] };
            for (final String stringPattern : findingTypesSplit) {
                findingTypes.add(Pattern.compile(stringPattern.trim().replace(".", "[.]").replace("*", ".*")));
            }
        }

        final String schemaNodePath = split[2].equals("*") ? null : split[2];

        return new ModuleAndFindingTypeAndSchemaNodePathFilterPredicate(moduleNames, findingTypes, schemaNodePath);
    }

    private final List<Pattern> moduleNames;
    private final List<Pattern> findingTypes;
    private final String schemaNodePath;

    /**
     * A finding will be filtered if the statement is part of any of the supplied modules,
     * and if the finding is of any of the supplied types, and if the path to the statement
     * is a sub-path of the supplied paths.
     * <p>
     * More formally, the name of the module in which the offending statement sits must be
     * matchable against any of the module name patterns, and the type of the finding must
     * be matchable against any of the finding type patterns, and the schema node path of
     * the offending statement must be the same, or a sub-path, of the supplied path.
     * <p>
     * Supplying an empty list for module names or finding types will match-all for that
     * parameter. Supplying null as schema node path will match-all paths.
     */
    public ModuleAndFindingTypeAndSchemaNodePathFilterPredicate(final List<Pattern> moduleNames,
            final List<Pattern> findingTypes, final String schemaNodePath) {
        this.moduleNames = Objects.requireNonNull(moduleNames);
        this.findingTypes = Objects.requireNonNull(findingTypes);
        this.schemaNodePath = schemaNodePath;
    }

    @Override
    public boolean test(final Finding f) {
        return matchOnModule(f) && matchOnFindingType(f) && matchOnSchemaNode(f);
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

    private boolean matchOnFindingType(final Finding finding) {

        if (findingTypes.isEmpty()) {
            return true;
        }

        final String findingType = finding.getFindingType();

        for (final Pattern pattern : findingTypes) {
            if (pattern.matcher(findingType).matches()) {
                return true;
            }
        }

        return false;
    }

    private boolean matchOnSchemaNode(final Finding finding) {

        if (schemaNodePath == null) {
            return true;
        }

        if (finding.getStatement() == null) {
            return false;
        }

        final YangDomElement domElement = finding.getStatement().getDomElement();
        if (domElement == null) {
            return false;
        }

        return domElement.getSimplifiedPath().startsWith(schemaNodePath);
    }
}
