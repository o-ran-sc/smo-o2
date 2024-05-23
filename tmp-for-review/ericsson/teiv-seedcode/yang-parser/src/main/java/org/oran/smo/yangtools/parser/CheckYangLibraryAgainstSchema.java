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
package org.oran.smo.yangtools.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.yanglibrary.Datastore;
import org.oran.smo.yangtools.parser.yanglibrary.Module;
import org.oran.smo.yangtools.parser.yanglibrary.Submodule;
import org.oran.smo.yangtools.parser.yanglibrary.YangLibrary;

/**
 * Checks the contents of a YANG Library (instance data) against a schema (and thereby the modules
 * supplied by the client). This is useful to compare the modules supplied for a schema against a YL.
 *
 * @author Mark Hollmann
 */
public class CheckYangLibraryAgainstSchema {

    private final ParserExecutionContext parserContext;
    private final Schema schema;
    private final YangLibrary yangLibrary;

    public CheckYangLibraryAgainstSchema(final ParserExecutionContext parserContext, final Schema schema,
            final YangLibrary yangLibrary) {
        this.parserContext = parserContext;
        this.schema = schema;
        this.yangLibrary = yangLibrary;
    }

    /**
     * Checks the YL against the modules. Any findings will be added to the supplied findings manager.
     */
    public void performChecks() {

        /*
         * There has to be a running datastore.
         */
        final Datastore runningDatastore = yangLibrary.getRunningDatastore();
        if (runningDatastore == null) {
            parserContext.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                    "Could not find 'running' datastore in YANG library data."));
            return;
        }

        /*
         * Collect the identities of the modules listed in the Yang Library
         */
        final Set<Module> yangLibImplementingModules = runningDatastore.getImplementingModules();
        final Set<Module> yangLibImportOnlyModules = runningDatastore.getImportOnlyModules();
        final Set<Submodule> yangLibSubmodules = new HashSet<>();
        yangLibImplementingModules.forEach(module -> yangLibSubmodules.addAll(module.getSubmodules()));
        yangLibImportOnlyModules.forEach(module -> yangLibSubmodules.addAll(module.getSubmodules()));

        final Set<ModuleIdentity> yangLibImplementingModuleIdentities = yangLibImplementingModules.stream().map(
                Module::getModuleIdentity).collect(Collectors.toSet());
        final Set<ModuleIdentity> yangLibImportOnlyModuleIdentities = yangLibImportOnlyModules.stream().map(
                Module::getModuleIdentity).collect(Collectors.toSet());
        final Set<ModuleIdentity> yangLibSubmoduleIdentities = yangLibSubmodules.stream().map(Submodule::getModuleIdentity)
                .collect(Collectors.toSet());

        /*
         * Collect the identities of the modules supplied to the parser
         */
        final List<YangModel> yangModels = new ArrayList<>(schema.getModuleRegistry().getAllYangModels());

        final Set<ModuleIdentity> inputImplementingModuleIdentities = yangModels.stream().filter(input -> input
                .getYangModelRoot().isModule() && input.getConformanceType() == ConformanceType.IMPLEMENT).map(
                        YangModel::getModuleIdentity).collect(Collectors.toSet());
        final Set<ModuleIdentity> inputImportOnlyModuleIdentities = yangModels.stream().filter(input -> input
                .getYangModelRoot().isModule() && input.getConformanceType() == ConformanceType.IMPORT).map(
                        YangModel::getModuleIdentity).collect(Collectors.toSet());
        final Set<ModuleIdentity> inputSubmoduleIdentities = yangModels.stream().filter(input -> input.getYangModelRoot()
                .isSubmodule()).map(YangModel::getModuleIdentity).collect(Collectors.toSet());

        /*
         * Now we simply compare these. They must fully match up - no missing/superfluous YAMs; correct conformance type.
         */
        checkInputMatchesYangLibraryForModules(inputImplementingModuleIdentities, yangLibImplementingModuleIdentities,
                ConformanceType.IMPLEMENT);
        checkInputMatchesYangLibraryForModules(inputImportOnlyModuleIdentities, yangLibImportOnlyModuleIdentities,
                ConformanceType.IMPORT);
        checkInputMatchesYangLibraryForSubmodules(inputSubmoduleIdentities, yangLibSubmoduleIdentities);

        /*
         * The Yang Library will also list features and the namespaces. Check these match up.
         */
        checkFeaturesAndNamespaces();
    }

    /**
     * Cross-check the modules listed in Yang Library against the YAMs in the input.
     */
    private void checkInputMatchesYangLibraryForModules(final Set<ModuleIdentity> inputModuleIdentities,
            final Set<ModuleIdentity> yangLibModuleIdentities, final ConformanceType conformanceType) {

        final Set<ModuleIdentity> inInputButNotYangLib = new HashSet<>(inputModuleIdentities);
        inInputButNotYangLib.removeAll(yangLibModuleIdentities);

        inInputButNotYangLib.forEach(mi -> {
            final Finding finding = new Finding(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY,
                    "Module '" + mi.toString() + "' with conformance type '" + conformanceType
                            .toString() + "' is in the input, but not listed (or not with this conformance type) in the supplied YANG library.");
            parserContext.getFindingsManager().addFinding(finding);
        });

        final Set<ModuleIdentity> inYangLibButNotInput = new HashSet<>(yangLibModuleIdentities);
        inYangLibButNotInput.removeAll(inputModuleIdentities);

        inYangLibButNotInput.forEach(mi -> {
            final Finding finding = new Finding(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY,
                    "Module '" + mi.toString() + "' with conformance type '" + conformanceType
                            .toString() + "' is listed in the supplied YANG library, but could not be found (or not with this conformance type) in the input.");
            parserContext.getFindingsManager().addFinding(finding);
        });
    }

    /**
     * Cross-check the submodules listed in Yang Library against the YAMs in the input.
     */
    private void checkInputMatchesYangLibraryForSubmodules(final Set<ModuleIdentity> inputSubmoduleIdentities,
            final Set<ModuleIdentity> yangLibSubmoduleIdentities) {

        final Set<ModuleIdentity> inInputButNotYangLib = new HashSet<>(inputSubmoduleIdentities);
        inInputButNotYangLib.removeAll(yangLibSubmoduleIdentities);

        inInputButNotYangLib.forEach(mi -> {
            final Finding finding = new Finding(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY,
                    "Submodule '" + mi.toString() + "' is in the input, but not listed in the supplied YANG library.");
            parserContext.getFindingsManager().addFinding(finding);
        });

        final Set<ModuleIdentity> inYangLibButNotInput = new HashSet<>(yangLibSubmoduleIdentities);
        inYangLibButNotInput.removeAll(inputSubmoduleIdentities);

        inYangLibButNotInput.forEach(mi -> {
            final Finding finding = new Finding(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY,
                    "Submodule '" + mi
                            .toString() + "' is listed in the supplied YANG library, but could not be found in the input.");
            parserContext.getFindingsManager().addFinding(finding);
        });
    }

    /**
     * Checks the features listed inside the YANG Library against the features that are actually
     * declared in the models. Also checks the namespace matches.
     */
    private void checkFeaturesAndNamespaces() {

        final Set<Module> yangLibAllModules = yangLibrary.getRunningDatastore().getAllModules();
        final List<YangModel> inputAllModules = schema.getModuleRegistry().getAllYangModels();

        for (final Module yangLibOneModule : yangLibAllModules) {
            final ModuleIdentity yangLibOneModuleIdentity = yangLibOneModule.getModuleIdentity();

            for (final YangModel inputOneModule : inputAllModules) {
                if (yangLibOneModuleIdentity.equals(inputOneModule.getModuleIdentity())) {
                    checkFeaturesAndNamespace(yangLibOneModule, inputOneModule);
                    break;
                }
            }
        }
    }

    /**
     * Checks that the features for a given module listed in the Yang Library actually exist inside the YAM.
     * <p>
     * Also checks the namespaces match up.
     */
    private void checkFeaturesAndNamespace(final Module yangLibOneModule, final YangModel inputOneModule) {

        final Set<String> featuresDeclaredInInputModule = getFeaturesDeclaredInsideInputModule(inputOneModule);

        yangLibOneModule.getFeatures().forEach(featureListedInYangLib -> {
            if (!featuresDeclaredInInputModule.contains(featureListedInYangLib)) {
                final Finding finding = new Finding(ParserFindingType.P083_FEATURE_LISTED_IN_YANG_LIBRARY_NOT_FOUND,
                        "YANG Library entry for module '" + yangLibOneModule
                                .getName() + "' lists feature '" + featureListedInYangLib + "', but this feature was not found in the actual module (or its submodules).");
                parserContext.getFindingsManager().addFinding(finding);
            }
        });

        final String yangLibNamespace = yangLibOneModule.getNamespace();
        final String inputNamespace = inputOneModule.getPrefixResolver().getDefaultNamespaceUri();

        if (!yangLibNamespace.equals(inputNamespace)) {
            final Finding finding = new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                    "Namespace mismatch for module '" + yangLibOneModule
                            .getName() + "' between what is stated in the Yang Library and what is declaread in the module.");
            parserContext.getFindingsManager().addFinding(finding);
        }
    }

    /**
     * Returns the features that are defined inside a module, or any of its submodules.
     */
    private Set<String> getFeaturesDeclaredInsideInputModule(final YangModel yangModel) {
        final Set<String> declaredFeatures = new HashSet<>();

        yangModel.getYangModelRoot().getModule().getFeatures().forEach(yFeature -> declaredFeatures.add(yFeature
                .getFeatureName()));

        /*
         * Features can also be defined inside submodules.
         */
        yangModel.getYangModelRoot().getOwnedSubmodules().forEach(submoduleYangModelRoot -> {
            submoduleYangModelRoot.getSubmodule().getFeatures().forEach(yFeature -> declaredFeatures.add(yFeature
                    .getFeatureName()));
        });

        return declaredFeatures;
    }
}
