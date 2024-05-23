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
package org.oran.smo.yangtools.parser.yanglibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.util.IdentityRefValue;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.YangInputResolver;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.yanglibrary.Module.IetfYangLibraryConformanceType;

/**
 * Parser for Yang Library instance data. Can handle both the "yang-library" and "modules-state" containers.
 *
 * @author Mark Hollmann
 */
public class IetfYangLibraryParser {

    /**
     * The module name for the yang-library module as defined in RFC 8525.
     */
    public static final String IETF_YANG_LIBRARY_MODULE_NAME = "ietf-yang-library";
    /**
     * The module namespace for the yang-library module as defined in RFC 8525.
     */
    public static final String IETF_YANG_LIBRARY_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-yang-library";

    public static final String YANG_LIBRARY_MODULES_STATE = "modules-state";
    public static final String YANG_LIBRARY_YANG_LIBRARY = "yang-library";

    private final List<YangLibraryPopulator> populators;
    private final FindingsManager findingsManager;
    private final ParserExecutionContext context;

    private static final ModuleAndNamespaceResolver RESOLVER = new ModuleAndNamespaceResolver();

    static {
        RESOLVER.recordModuleMapping(IETF_YANG_LIBRARY_MODULE_NAME, IETF_YANG_LIBRARY_NAMESPACE);
        RESOLVER.recordNamespaceMapping(IETF_YANG_LIBRARY_NAMESPACE, IETF_YANG_LIBRARY_MODULE_NAME);

        RESOLVER.recordModuleMapping(Datastore.IETF_DATASTORES_MODULE_NAME, Datastore.IETF_DATASTORES_NAMESPACE);
        RESOLVER.recordNamespaceMapping(Datastore.IETF_DATASTORES_NAMESPACE, Datastore.IETF_DATASTORES_MODULE_NAME);
    }

    /**
     * Creates a Yang Library parser using the default populator for RFC8525.
     */
    public IetfYangLibraryParser() {
        this(Collections.singletonList(new RFC8525Populator()));
    }

    /**
     * Creates a YL parser using the supplied populators. The client should make sure to supply a
     * populator for extraction of core Yang Library data according to RFC8525.
     */
    public IetfYangLibraryParser(final List<YangLibraryPopulator> populators) {
        this.populators = populators;

        findingsManager = new FindingsManager(new ModifyableFindingSeverityCalculator());
        context = new ParserExecutionContext(findingsManager);

        context.setCheckModulesAgainstYangLibrary(false);
    }

    /**
     * Returns the FindingsManager used during parsing. Issues encountered during parsing will be
     * captured as Findings.
     */
    public FindingsManager getFindingsManager() {
        return findingsManager;
    }

    /**
     * Parses instance data into Yang Library instances. The supplied resolver must resolve to one or more
     * data inputs (typically files containing XML or JSON data). All Yang Library instances encountered
     * in the data will be extracted, not just the instance found at the top-level (ie., mount points are
     * also handled).
     * <p/>
     * Yang library instance data using the (deprecated) "modules-state" will be converted to a corresponding
     * representation of the "yang-library".
     * <p/>
     * Issues may be encountered during the parsing; after parsing, the findings manager should be inspected
     * for any issues.
     */
    public List<YangLibrary> parseIntoYangLibraries(final YangInputResolver instanceDataInputResolver) {

        findingsManager.clear();

        final List<YangData> yangDatas = instanceDataInputResolver.getResolvedYangInput().stream().map(YangData::new)
                .collect(Collectors.toList());

        /*
         * Parse in all of the data. The worst that can happen is that the XML or JSON is malformed, or that
         * prefixes cannot be resolved.
         */
        for (final YangData yangData : yangDatas) {
            yangData.parse(context);
        }

        if (!findingsManager.getAllFindings().isEmpty()) {
            findingsManager.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR,
                    "There were findings during parsing of the YANG instance data that may have resulted in the YANG Library data not being populated correctly."));
        }

        /*
         * We look at each input and try to find the relevant containers.
         */
        final List<YangLibrary> result = new ArrayList<>();

        for (final YangData yangData : yangDatas) {
            final YangDataDomDocumentRoot yangDataDomDocumentRoot = yangData.getYangDataDomDocumentRoot();
            if (yangDataDomDocumentRoot != null) {
                extractYangLibraryUnderDomNode(yangDataDomDocumentRoot, result);
            }
        }

        return result;
    }

    /**
     * Works like {@link parseIntoYangLibraries}, but uses the supplied data DOm as input of Yang Library data, and will
     * only extract the top-level Yang Library instance (if any).
     */
    public static YangLibrary getTopLevelYangLibrary(final YangDataDomDocumentRoot dataDomDocumentRoot) {

        final IetfYangLibraryParser yangLibraryParser = new IetfYangLibraryParser();
        final List<YangLibrary> extractedYangLibraries = new ArrayList<>();
        yangLibraryParser.extractYangLibraryUnderDomNode(dataDomDocumentRoot, extractedYangLibraries);

        return YangLibrary.getTopLevelSchema(extractedYangLibraries);
    }

    /**
     * Given a data DOM node (possibly being document root), tries to extract a YANG Library under the DOM node.
     */
    private void extractYangLibraryUnderDomNode(final YangDataDomNode domNode, List<YangLibrary> result) {

        final ModulesState modulesStateInstance = createModulesStateBranch(domNode);
        YangLibrary yangLibraryInstance = createYangLibraryBranch(domNode);

        /*
         * The modules-state is deprecated, so we translate to yang-library if needed. This
         * allows a client to always operate on the yang-library without having to worry
         * about the deprecated modules-state branch.
         *
         * Note we will never translate back from yang-library to modules-state, as this
         * would require us to figure out which is the correct set of modules (looking
         * at datastore and schema elements), which can be ambiguous - plus it is
         * deprecated...
         */
        if (modulesStateInstance != null && yangLibraryInstance == null) {
            yangLibraryInstance = translateModulesStateDataToYangLibraryData(modulesStateInstance);
        }

        if (yangLibraryInstance != null) {
            result.add(yangLibraryInstance);
        }

        /*
         * Recursively work down the DOM tree - the YANG library can conceivably be under a mount point
         * further down the tree.
         */
        domNode.getChildren().forEach(child -> extractYangLibraryUnderDomNode(child, result));
    }

    private static YangLibrary translateModulesStateDataToYangLibraryData(final ModulesState modulesState) {

        final YangLibrary yangLibraryInstance = new YangLibrary(modulesState.getMountPoint());
        yangLibraryInstance.setContentId(modulesState.getModuleSetId());

        final ModuleSet defaultModuleSet = new ModuleSet();
        defaultModuleSet.setName("module-set-auto-generated-by-yang-library-parser");

        for (final Module module : modulesState.getModules()) {
            if (module.getConformanceType() == IetfYangLibraryConformanceType.IMPLEMENT) {
                defaultModuleSet.addImplementingModule(module);
            } else if (module.getConformanceType() == IetfYangLibraryConformanceType.IMPORT) {
                defaultModuleSet.addImportOnlyModule(module);
            }
        }

        yangLibraryInstance.addDatastore(new Datastore(Datastore.RUNNING_DATASTORE_IDENTITY,
                "schema-auto-generated-by-yang-library-parser", Collections.singletonList(defaultModuleSet)));

        return yangLibraryInstance;
    }

    // ============================= Modules state branch ========================================

    private ModulesState createModulesStateBranch(final YangDataDomNode parentDomNode) {

        final YangDataDomNode modulesStateDomNode = getDomChild(parentDomNode, YANG_LIBRARY_MODULES_STATE);

        if (modulesStateDomNode != null) {
            final ModulesState modulesState = new ModulesState(parentDomNode);
            populateModulesState(modulesState, modulesStateDomNode);

            /*
             * The YANG model for RFC 7895 (modules-state) allows for semantic errors - for example, the
             * same module, of different revisions, could be marked as IMPLEMENTING. This is likely to be
             * causing problems for the client later on, so we do some validation here to try to catch
             * these issues as early as possible.
             */
            validateModulesState(modulesState);

            return modulesState;
        }

        return null;
    }

    private void populateModulesState(final ModulesState modulesState, final YangDataDomNode modulesStateDomNode) {

        populators.forEach(pop -> pop.populateModulesState(context, modulesState, modulesStateDomNode));

        for (final YangDataDomNode moduleChildDomNode : getDomChildren(modulesStateDomNode, "module")) {
            final Module module = new Module();
            modulesState.addModule(module);
            populateModuleInModulesState(module, moduleChildDomNode);
        }
    }

    private void populateModuleInModulesState(final Module module, final YangDataDomNode moduleDomNode) {

        populators.forEach(pop -> pop.populateModuleInModulesState(context, module, moduleDomNode));

        for (final YangDataDomNode submoduleDomNode : getDomChildren(moduleDomNode, "submodule")) {
            final Submodule submodule = new Submodule();
            module.addSubmodule(submodule);
            populators.forEach(pop -> pop.populateSubmoduleInModulesState(context, submodule, submoduleDomNode));
        }
    }

    private void validateModulesState(final ModulesState modulesState) {
        validateModulesStateForConformanceType(modulesState);
    }

    private void validateModulesStateForConformanceType(final ModulesState modulesState) {
        /*
         * Couple basic rules:
         *
         * A correct enum value must be used for 'conformance-type'.
         * The same module (by name) cannot have 'conformance-type implement'.
         * There must be at least a single 'conformance-type implement'.
         */
        final Set<String> implementingModuleNames = new HashSet<>();
        for (final Module module : modulesState.getModules()) {
            if (module.getConformanceType() == IetfYangLibraryConformanceType.UNKNOWN) {
                context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                        "Unknown conformance-type for module '" + module.getName() + "'."));
            } else if (module.getConformanceType() == IetfYangLibraryConformanceType.IMPLEMENT) {
                if (implementingModuleNames.contains(module.getName())) {
                    context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                            "Module '" + module.getName() + "' listed more than once with conformance-type 'implement'."));
                } else {
                    implementingModuleNames.add(module.getName());
                }
            }
        }

        if (implementingModuleNames.isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                    "YANG library does not have at least a single module entry with conformance-type 'implement'."));
        }
    }

    // ============================ YANG Library branch ======================================

    private YangLibrary createYangLibraryBranch(final YangDataDomNode parentDomNode) {

        try {
            final YangDataDomNode yangLibraryDomNode = getDomChild(parentDomNode, YANG_LIBRARY_YANG_LIBRARY);
            if (yangLibraryDomNode != null) {
                final YangLibrary yangLibrary = new YangLibrary(parentDomNode);
                populateYangLibrary(yangLibrary, yangLibraryDomNode);
                return yangLibrary;
            }
        } catch (final Exception ex) {
            context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                    "Incorrect YANG library."));
        }

        return null;
    }

    private void populateYangLibrary(final YangLibrary yangLibrary, final YangDataDomNode yangLibraryDomNode) {

        populators.forEach(pop -> pop.populateYangLibrary(context, yangLibrary, yangLibraryDomNode));

        final Map<String, ModuleSet> extractedModuleSets = new HashMap<>();

        for (final YangDataDomNode moduleSetDomNode : getDomChildren(yangLibraryDomNode, "module-set")) {
            final ModuleSet moduleSet = new ModuleSet();
            populateModuleSet(moduleSet, moduleSetDomNode);
            extractedModuleSets.put(moduleSet.getName(), moduleSet);
        }

        final Map<String, List<ModuleSet>> schemaToModuleSetMappings = getSchemaToModuleSetMappings(context,
                extractedModuleSets, yangLibraryDomNode);

        handleDatastoreElements(yangLibrary, schemaToModuleSetMappings, yangLibraryDomNode);
    }

    private void populateModuleSet(final ModuleSet moduleSet, final YangDataDomNode moduleSetDomNode) {

        populators.forEach(pop -> pop.populateModuleSet(context, moduleSet, moduleSetDomNode));

        for (final YangDataDomNode moduleDomNode : getDomChildren(moduleSetDomNode, "module")) {
            final Module module = new Module();
            moduleSet.addImplementingModule(module);
            populateModuleInYangLibrary(module, moduleDomNode, Module.MODULE_IMPLEMENT);
        }
        for (final YangDataDomNode moduleDomNode : getDomChildren(moduleSetDomNode, "import-only-module")) {
            final Module module = new Module();
            moduleSet.addImportOnlyModule(module);
            populateModuleInYangLibrary(module, moduleDomNode, Module.MODULE_IMPORT);
        }
    }

    private void populateModuleInYangLibrary(final Module module, final YangDataDomNode moduleDomNode,
            final String conformanceType) {

        populators.forEach(pop -> pop.populateModuleInYangLibrary(context, module, moduleDomNode, conformanceType));

        for (final YangDataDomNode submoduleDomNode : getDomChildren(moduleDomNode, "submodule")) {
            final Submodule submodule = new Submodule();
            module.addSubmodule(submodule);
            populators.forEach(pop -> pop.populateSubmoduleInYangLibrary(context, submodule, submoduleDomNode));
        }
    }

    /**
     * Returns a mapping of schema name -to- list of module sets
     */
    private static Map<String, List<ModuleSet>> getSchemaToModuleSetMappings(final ParserExecutionContext context,
            final Map<String, ModuleSet> extractedModuleSets, final YangDataDomNode yangLibraryDomNode) {

        final Map<String, List<ModuleSet>> result = new HashMap<>();

        for (final YangDataDomNode schemaDomNode : getDomChildren(yangLibraryDomNode, "schema")) {

            final String schemaName = getValueOfChild(context, schemaDomNode, "name", "");
            if (schemaName.isEmpty()) {
                context.getFindingsManager().addFinding(new Finding(
                        ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING, "Missing schema name."));
            }

            result.put(schemaName, new ArrayList<>());

            final List<String> moduleSetNames = getValueOfChildren(context, schemaDomNode, "module-set");

            for (final String moduleSetName : moduleSetNames) {
                final ModuleSet moduleSet = extractedModuleSets.get(moduleSetName);
                if (moduleSet != null) {
                    result.get(schemaName).add(moduleSet);
                } else {
                    context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                            "schema '" + schemaName + "' refers to non-existing module-set '" + moduleSetName + "'."));
                }
            }
        }

        /*
         * Special handling - sometimes whoever creates the Yang Library data forgets to use the "schema"
         * data node. We are nice here and simply auto-generate one, assigning all the module-sets found.
         */

        if (result.isEmpty()) {
            result.put("schema-auto-generated-by-yang-library-parser", new ArrayList<>(extractedModuleSets.values()));
        }

        return result;
    }

    private void handleDatastoreElements(final YangLibrary yangLibrary,
            final Map<String, List<ModuleSet>> schemaToModuleSetMappings, final YangDataDomNode yangLibraryDomNode) {

        for (final YangDataDomNode datastoreDomNode : getDomChildren(yangLibraryDomNode, "datastore")) {

            IdentityRefValue datastoreIdentityRef = null;

            final YangDataDomNode datastoreNameDomNode = getDomChild(datastoreDomNode, "name");
            if (datastoreNameDomNode != null) {

                final String datastoreIdentityRefAsString = Objects.toString(datastoreNameDomNode.getValue());

                /*
                 * The value of this leaf is an identity ref, e.g.:
                 *
                 * <datastore xmlns:ds="urn:ietf:params:xml:ns:yang:ietf-datastores">
                 *     <name>ds:running</name>
                 *     <schema>schema1</schema>
                 * </datastore>
                 *
                 * ...or, in JSON...
                 *
                 * "ietf-yang-library:datastore" : [
                 *   {
                 *     "name" : "ietf-datastores:running",
                 *     "schema" : "schema1"
                 *   }
                 * ]
                 */

                if (datastoreNameDomNode.getSourceDataType() == SourceDataType.XML) {
                    datastoreIdentityRef = new IdentityRefValue(datastoreIdentityRefAsString, datastoreNameDomNode
                            .getPrefixResolver(), datastoreNameDomNode.getNamespace());
                } else {
                    datastoreIdentityRef = new IdentityRefValue(datastoreIdentityRefAsString, datastoreNameDomNode
                            .getNamespace());
                }

                /*
                 * If the namespace is unknown (maybe the designer forgot to declare the namespace),
                 * we will assume the namespace of the "ietf-datastores" module.
                 */
                if (datastoreIdentityRef.getIdentityNamespace() == null && datastoreIdentityRef
                        .getIdentityModuleName() == null) {
                    context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                            "Unresolvable/missing prefix/module for <datastore> name '" + datastoreIdentityRefAsString + "'."));
                    datastoreIdentityRef = new IdentityRefValue(Datastore.IETF_DATASTORES_NAMESPACE,
                            Datastore.IETF_DATASTORES_MODULE_NAME, datastoreIdentityRefAsString);
                }

            } else {
                context.getFindingsManager().addFinding(new Finding(
                        ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING, "Missing datastore name."));
            }

            final String schemaName = getValueOfChild(context, datastoreDomNode, "schema", "");
            if (schemaName.isEmpty()) {
                context.getFindingsManager().addFinding(new Finding(
                        ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING,
                        "Missing value for leaf 'schema' for datastore."));
            }
            if (!schemaToModuleSetMappings.containsKey(schemaName)) {
                context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                        "datastore refers to non-existing schema '" + schemaName + "'."));
            } else {
                yangLibrary.addDatastore(new Datastore(datastoreIdentityRef, schemaName, schemaToModuleSetMappings.get(
                        schemaName)));
            }
        }

        /*
         * Special handling: Designer did not specify a datastore in the YANG Library. If there is only a
         * single schema we assume that this schema is the one to be used.
         */
        if (yangLibrary.getDatastores().isEmpty() && schemaToModuleSetMappings.size() == 1) {
            final Entry<String, List<ModuleSet>> theOnlySchemaToModuleSetMappingEntry = schemaToModuleSetMappings.entrySet()
                    .iterator().next();
            yangLibrary.addDatastore(new Datastore(Datastore.RUNNING_DATASTORE_IDENTITY,
                    theOnlySchemaToModuleSetMappingEntry.getKey(), theOnlySchemaToModuleSetMappingEntry.getValue()));
        }
    }

    static YangDataDomNode getDomChild(final YangDataDomNode parentDomNode, final String soughtName) {
        return parentDomNode.getChild(IETF_YANG_LIBRARY_NAMESPACE, IETF_YANG_LIBRARY_MODULE_NAME, soughtName);
    }

    static List<YangDataDomNode> getDomChildren(final YangDataDomNode parentDomNode, final String soughtName) {
        return parentDomNode.getChildren(IETF_YANG_LIBRARY_NAMESPACE, IETF_YANG_LIBRARY_MODULE_NAME, soughtName);
    }

    static String getValueOfChild(final ParserExecutionContext context, final YangDataDomNode parentDomNode,
            final String soughtName, final String defaultValue) {

        final YangDataDomNode child = getDomChild(parentDomNode, soughtName);
        final Object valueOfChild = child == null ? null : child.getValue();

        if (valueOfChild != null && !(valueOfChild instanceof String)) {
            context.getFindingsManager().addFinding(new Finding(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA,
                    "Expected a string value for '" + soughtName + "'."));
            return defaultValue;
        }

        return valueOfChild == null ? defaultValue : ((String) valueOfChild).trim();
    }

    static List<String> getValueOfChildren(final ParserExecutionContext context, final YangDataDomNode parentDomNode,
            final String soughtName) {
        return getDomChildren(parentDomNode, soughtName).stream().map(YangDataDomNode::getStringValue).collect(Collectors
                .toList());
    }
}
