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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.model.statements.StatementClassSupplier;
import org.oran.smo.yangtools.parser.model.util.YangFeature;

/**
 * The parser execution context directs the behaviour of the parser. It is vital that the
 * context is correctly setup by the client.
 *
 * @author Mark Hollmann
 */
public class ParserExecutionContext {

    /*
     * The findings manager will hold onto all findings issued by the parser. It should be
     * inspected after the parsing has finished for any problems.
     */
    private final FindingsManager findingsManager;

    /*
     * For extensions, a list of class suppliers. The Yang Parser has in-build support for
     * some IETF and 3GPP extensions. It is not necessary to list the statement class supplier
     * for Yang Core classes.
     *
     * In general, it is not mandatory to supply these for extensions; extensions will still be
     * correctly parsed and resolved; however, extension-specific validation will not be performed.
     */
    private final List<StatementClassSupplier> extensionCreators;

    /*
     * Custom processors may perform additional activities during different phases of the parse.
     */
    private final List<CustomProcessor> customProcessors;

    /*
     * Denotes that parsing shall stop after the initial parsing has finished, i.e. the
     * schema tree has been generated in memory. Some downstream tooling may desire this.
     */
    private boolean stopAfterInitialParse = false;

    /*
     * If the modules are is really bad shape it makes no sense to do fully process them - it only
     * results in a lot of findings. Better to fail parsing fast.
     */
    private boolean failFast = true;

    /*
     * The features supported by the server. This information typically comes from the YANG library.
     * If the Set is null, the supported feature information is considered unknown. Conversely, an
     * empty Set denotes that no features are supported. Information about the supported features is
     * required if 'if-feature' statements should be evaluated and the schema tree adjusted accordingly.
     */
    private Set<YangFeature> supportedFeatures = null;

    /*
     * Denotes whether during parsing any schema node shall be removed if a 'if-feature' condition is
     * not fulfilled. Requires the supported features to be set.
     */
    private boolean removeSchemaNodesNotSatisfyingIfFeature = false;

    /*
     * Denotes whether groupings and typedefs shall be resolved by the parser.
     */
    private boolean resolveDerivedTypesAndGroupings = true;

    /*
     * Denotes whether augmentations shall be resolved by the parser.
     */
    private boolean resolveAugments = true;

    /*
     * Denotes whether deviations shall be resolved by the parser.
     */
    private boolean resolveDeviations = true;

    /*
     * Denotes whether the contents of submodules shall be merged into their owning modules.
     */
    private boolean mergeSubmodulesIntoModules = true;

    /*
     * Denotes whether data nodes, or in general "protocol accessible" objects, shall be
     * ignored. This is used to remove from the schema certain schema nodes that are part of
     * modules of conformance type IMPORT-ONLY.
     */
    private boolean ignoreImportedProtocolAccessibleObjects = true;

    /*
     * Whether findings on unused schema nodes shall be reported. Sometimes, certain
     * constructs, such as groupings and derived types, are not used. Setting this flag to
     * TRUE will suppress findings on such constructs.
     */
    private boolean suppressFindingsOnUnusedSchemaNodes = false;

    /*
     * Denotes whether, during parsing, the input modules shall be checked against YANG
     * Library instance data (ie. have the exact same modules been supplied as those listed
     * in the YL?).
     */
    private boolean checkModulesAgainstYangLibrary = true;

    public ParserExecutionContext(final FindingsManager findingsManager) {
        this(findingsManager, Collections.<StatementClassSupplier> emptyList());
    }

    public ParserExecutionContext(final FindingsManager findingsManager,
            final List<StatementClassSupplier> extensionCreators) {
        this(findingsManager, extensionCreators, Collections.<CustomProcessor> emptyList());
    }

    public ParserExecutionContext(final FindingsManager findingsManager,
            final List<StatementClassSupplier> extensionCreators, final List<CustomProcessor> customProcessors) {
        this.findingsManager = Objects.requireNonNull(findingsManager);
        this.extensionCreators = Objects.requireNonNull(extensionCreators);
        this.customProcessors = Objects.requireNonNull(customProcessors);
    }

    public List<StatementClassSupplier> getExtensionCreators() {
        return extensionCreators;
    }

    public List<CustomProcessor> getCustomProcessors() {
        return customProcessors;
    }

    public FindingsManager getFindingsManager() {
        return findingsManager;
    }

    public void addFinding(final Finding finding) {
        findingsManager.addFinding(finding);
    }

    /**
     * Whether parsing shall stop after the initial parsing has finished, i.e. the
     * schema tree has been generated in memory.
     */
    public void setStopAfterInitialParse(final boolean val) {
        this.stopAfterInitialParse = val;
    }

    public boolean shouldStopAfterInitialParse() {
        return stopAfterInitialParse;
    }

    /**
     * Fail-fast is a mechanism whereby certain findings by the parser will lead to the abort
     * of further processing. Typically, the input must be fixed-up before the parsing can succeed.
     */
    public void setFailFast(final boolean val) {
        this.failFast = val;
    }

    public boolean failFast() {
        return failFast;
    }

    /**
     * Set the features supported by the server. Required if 'if-feature' statements shall be evaluated.
     */
    public void setSupportedFeatures(final Set<YangFeature> val) {
        this.supportedFeatures = val;
    }

    public Set<YangFeature> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Whether schema nodes shall be removed by the parser if their if-feature condition is not fulfilled.
     * Requires supported-features to be supplied as well.
     */
    public void setRemoveSchemaNodesNotSatisfyingIfFeature(final boolean val) {
        this.removeSchemaNodesNotSatisfyingIfFeature = val;
    }

    /**
     * Whether schema nodes shall be removed by the parser if their if-feature condition is not fulfilled. Requires
     * supported-features to be supplied as well.
     */
    public boolean removeSchemaNodesNotSatisfyingIfFeature() {
        return removeSchemaNodesNotSatisfyingIfFeature;
    }

    /**
     * Sets whether derived types and groupings shall be resolved by the parser.
     */
    public void setResolveDerivedTypesAndGroupings(final boolean val) {
        this.resolveDerivedTypesAndGroupings = val;
    }

    public boolean resolveDerivedTypesAndGroupings() {
        return resolveDerivedTypesAndGroupings;
    }

    /**
     * Whether augmentations shall be merged-in.
     */
    public void setResolveAugments(final boolean val) {
        this.resolveAugments = val;
    }

    public boolean resolveAugments() {
        return resolveAugments;
    }

    /**
     * Whether deviations shall be applied.
     */
    public void setResolveDeviations(final boolean val) {
        this.resolveDeviations = val;
    }

    public boolean resolveDeviations() {
        return resolveDeviations;
    }

    /**
     * Whether submodules shall be merged into their owning modules.
     */
    public void setMergeSubmodulesIntoModules(final boolean val) {
        this.mergeSubmodulesIntoModules = val;
    }

    public boolean mergeSubmodulesIntoModules() {
        return mergeSubmodulesIntoModules;
    }

    /**
     * Whether data nodes of IMPORT-ONLY modules shall be ignored.
     */
    public void setIgnoreImportedProtocolAccessibleObjects(final boolean ignoreImportedProtocolAccessibleObjects) {
        this.ignoreImportedProtocolAccessibleObjects = ignoreImportedProtocolAccessibleObjects;
    }

    public boolean ignoreImportedProtocolAccessibleObjects() {
        return ignoreImportedProtocolAccessibleObjects;
    }

    /**
     * Denotes whether findings on schema nodes that are not in use shall be
     * removed after parsing.
     */
    public boolean shouldSuppressFindingsOnUnusedSchemaNodes() {
        return suppressFindingsOnUnusedSchemaNodes;
    }

    public void setSuppressFindingsOnUnusedSchemaNodes(boolean val) {
        this.suppressFindingsOnUnusedSchemaNodes = val;
    }

    /**
     * Whether the input modules shall be checked against YANG Library data during parsing.
     */
    public void setCheckModulesAgainstYangLibrary(boolean checkModulesAgainstYangLibrary) {
        this.checkModulesAgainstYangLibrary = checkModulesAgainstYangLibrary;
    }

    public boolean checkModulesAgainstYangLibrary() {
        return checkModulesAgainstYangLibrary;
    }

    private final List<String> infos = new ArrayList<>();

    public void addInfo(final String info) {
        infos.add(info);
    }

    public List<String> getInfos() {
        return Collections.unmodifiableList(infos);
    }
}
