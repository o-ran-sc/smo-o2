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
package org.oran.smo.yangtools.parser.model.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;

/**
 * Keeps track of all the YAMs for a given schema.
 *
 * @author Mark Hollmann
 */
public class ModuleRegistry {

    private final List<YangModel> yangModels = new ArrayList<>();
    private final Set<String> duplicatedModuleNames = new HashSet<>();

    public ModuleRegistry() {
    }

    public void addModule(final FindingsManager findingsManager, final YangModel yangModelToAdd) {

        final String toAddModuleName = yangModelToAdd.getModuleIdentity().getModuleName();
        final String toAddRevision = yangModelToAdd.getModuleIdentity().getRevision();

        /*
         * Make sure that this very module has not been added twice, or more than once as IMPLEMENTS.
         */
        for (final YangModel validYangModel : yangModels) {
            if (validYangModel.getModuleIdentity().getModuleName().equals(toAddModuleName)) {

                duplicatedModuleNames.add(toAddModuleName);

                if (validYangModel.getModuleIdentity().getRevision() == null && toAddRevision == null) {
                    findingsManager.addFinding(new Finding(yangModelToAdd, ParserFindingType.P003_DUPLICATE_INPUT,
                            "Same module '" + toAddModuleName + "' provided twice as input. Remove the duplicate."));
                } else if (validYangModel.getModuleIdentity()
                        .getRevision() != null && toAddRevision != null && validYangModel.getModuleIdentity().getRevision()
                                .equals(toAddRevision)) {
                    findingsManager.addFinding(new Finding(yangModelToAdd, ParserFindingType.P003_DUPLICATE_INPUT,
                            "Same module '" + toAddModuleName + "/" + toAddRevision + "' provided twice as input. Remove the duplicate."));
                } else {
                    /*
                     * We are not out of the woods yet. So the revisions are not the same - but then at
                     * most one of them can be IMPLEMENTS, the others have to be IMPORTS.
                     */
                    checkForDuplicateImplements(findingsManager, toAddModuleName, yangModelToAdd);
                }
            }
        }

        yangModels.add(yangModelToAdd);
    }

    private void checkForDuplicateImplements(final FindingsManager findingsManager, final String toAddModuleName,
            final YangModel modelInputToAdd) {

        final List<YangModel> allYangFilesOfSameModuleName = byModuleName(toAddModuleName);
        allYangFilesOfSameModuleName.add(modelInputToAdd);

        int conformImplementsCount = 0;

        for (final YangModel yangFileOfSameName : allYangFilesOfSameModuleName) {
            if (yangFileOfSameName.getConformanceType() == ConformanceType.IMPLEMENT) {
                conformImplementsCount++;
            }
        }

        if (conformImplementsCount > 1) {
            findingsManager.addFinding(new Finding(modelInputToAdd, ParserFindingType.P004_SAME_MODULE_DUPLICATE_IMPLEMENTS,
                    "(Sub-)Module with different revisions supplied multiple times as conformance IMPLEMENTS."));
        }
    }

    public List<YangModel> getAllYangModels() {
        return Collections.unmodifiableList(yangModels);
    }

    /**
     * Returns all modules with the given name. May return multiple results if the module is in
     * the input multiple times (with different revisions, of course).
     */
    public List<YangModel> byModuleName(final String soughtModuleName) {

        return yangModels.stream().filter(ymi -> ymi.getModuleIdentity().getModuleName().equals(soughtModuleName)).collect(
                Collectors.toList());
    }

    /**
     * Returns an exact match for the supplied module name and revision (or null if no match was found).
     * <p>
     * The sought revision may be null, in which case the module will be tested for having no revision.
     */
    public YangModel exactMatch(final String soughtModuleName, final String soughtRevision) {

        return yangModels.stream().filter(ymi -> ymi.getModuleIdentity().getModuleName().equals(soughtModuleName)).filter(
                ymi -> {
                    if (soughtRevision == null && ymi.getModuleIdentity().getRevision() == null) {
                        return true;
                    }
                    if (soughtRevision != null && soughtRevision.equals(ymi.getModuleIdentity().getRevision())) {
                        return true;
                    }
                    return false;
                }).findAny().orElse(null);
    }

    /**
     * Returns exactly one match, or null if not found.
     * <p/>
     * Where the sought revision is UNKWOWN_REVISION, will return the first found module matching
     * the module name. Where this could be ambiguous for the caller, use byModuleName() instead
     * and iterate over the results to find the correct module.
     * <p/>
     * Where the sought revision is null, or an actual revision-date, will try to find an exact match.
     */
    public YangModel find(final ModuleIdentity moduleIdentity) {
        return find(moduleIdentity.getModuleName(), moduleIdentity.getRevision());
    }

    /**
     * Returns exactly one match, or null if not found.
     * <p/>
     * Where the sought revision is UNKWOWN_REVISION, will return the first found module matching
     * the module name. Where this could be ambiguous for the caller, use byModuleName() instead
     * and iterate over the results to find the correct module.
     * <p/>
     * Where the sought revision is null, or an actual revision-date, will try to find an exact match.
     */
    public YangModel find(final String soughtModuleName, final String soughtRevision) {

        return yangModels.stream().filter(ymi -> ymi.getModuleIdentity().getModuleName().equals(soughtModuleName)).filter(
                ymi -> {
                    if (ModuleIdentity.UNKWOWN_REVISION.equals(soughtRevision)) {
                        return true;
                    }
                    if (soughtRevision == null && ymi.getModuleIdentity().getRevision() == null) {
                        return true;
                    }
                    if (soughtRevision != null && soughtRevision.equals(ymi.getModuleIdentity().getRevision())) {
                        return true;
                    }
                    return false;
                }).findAny().orElse(null);
    }
}
