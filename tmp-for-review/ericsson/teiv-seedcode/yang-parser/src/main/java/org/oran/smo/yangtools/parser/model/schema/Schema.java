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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.resolvers.AugmentResolver;
import org.oran.smo.yangtools.parser.model.resolvers.DeviationResolver;
import org.oran.smo.yangtools.parser.model.resolvers.TypeResolver;
import org.oran.smo.yangtools.parser.model.resolvers.UsesResolver;
import org.oran.smo.yangtools.parser.model.statements.ietf.CIETF;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YBase;
import org.oran.smo.yangtools.parser.model.statements.yang.YBelongsTo;
import org.oran.smo.yangtools.parser.model.statements.yang.YIdentity;
import org.oran.smo.yangtools.parser.model.util.YangAnnotation;
import org.oran.smo.yangtools.parser.model.util.YangIdentity;
import org.oran.smo.yangtools.parser.model.yangdom.DefaultOutputFileNameResolver;
import org.oran.smo.yangtools.parser.model.yangdom.OutputFileNameResolver;
import org.oran.smo.yangtools.parser.model.yangdom.OutputStreamResolver;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomWriter;
import org.oran.smo.yangtools.parser.util.QNameHelper;
import org.oran.smo.yangtools.parser.util.StackTraceHelper;

/**
 * Represents a YANG schema. A schema is the totally of all modules advertized through
 * a single instance of a Yang Library. In a given server, it is possible to mount
 * additional schemas (via the schema-mount mechanism), which requires Yang Library
 * instances to be present under the mount-point. This would result in additional
 * instances of this class having to be created.
 * <p/>
 * Where a server does not use schema-mount, an instance of this class in effect
 * represents the complete schema of the server.
 *
 * @author Mark Hollmann
 */
public class Schema {

    /**
     * The module registry contains the modules that are part of this schema.
     */
    private final ModuleRegistry moduleRegistry = new ModuleRegistry();

    /**
     * The identity registry keeps track of all identities in the schema.
     */
    private final IdentityRegistry identityRegistry = new IdentityRegistry();

    /**
     * The annotation registry keeps track of all annotations in the schema.
     */
    private final AnnotationRegistry annotationRegistry = new AnnotationRegistry();

    /**
     * The module/namespace resolver for this schema.
     */
    private final ModuleAndNamespaceResolver moduleNamespaceResolver = new ModuleAndNamespaceResolver();

    /**
     * Prevents double-processing of the schema.
     */
    private boolean hasBeenProcessed = false;

    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    public IdentityRegistry getIdentityRegistry() {
        return identityRegistry;
    }

    public AnnotationRegistry getAnnotationRegistry() {
        return annotationRegistry;
    }

    public ModuleAndNamespaceResolver getModuleNamespaceResolver() {
        return moduleNamespaceResolver;
    }

    /*
     * These are findings we would not want to lose, as they are quite fundamental.
     */
    private static final List<String> FINDINGS_WE_ALWAYS_WANT = Arrays.asList(ParserFindingType.P000_UNSPECIFIED_ERROR
            .toString(), ParserFindingType.P001_BASIC_FILE_READ_ERROR.toString(),
            ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString(),
            ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());

    /**
     * Causes the supplied inputs to be parsed into a tree of schema nodes. After parsing, the trees
     * are available by invoking getYangModelRoot() on each input.
     * <p>
     * Unless "stopAfterInitialParse" has been set to true on the context, this method will also
     * process the contents of the schema and perform various resolutions.
     * <p>
     * Irrespective of the "stopAfterInitialParse" setting, custom processors will be invoked with the
     * onPreSchemaProcessed call-back.
     */
    public void parseIntoSchema(final ParserExecutionContext context, final List<YangModel> yangModels) {

        FINDINGS_WE_ALWAYS_WANT.forEach(ft -> context.getFindingsManager().addNonSuppressableFindingType(ft));

        if (context.failFast()) {
            FAIL_FAST_FINDINGS.forEach(ft -> context.getFindingsManager().addNonSuppressableFindingType(ft));
        }

        final Set<YangModel> uniqueYangModels = getUniqueInputs(context, yangModels);

        for (final YangModel yangModel : uniqueYangModels) {
            yangModel.parse(context, moduleNamespaceResolver, this);

            /*
             * Don't add the module to the registry if there are fundamental issues
             * with it, as denoted by a missing ModuleIdentity.
             */
            if (yangModel.getModuleIdentity() != null) {
                moduleRegistry.addModule(context.getFindingsManager(), yangModel);
            }
        }

        try {
            /*
             * First thing: link-up the modules and their submodules, and also update the namespace
             * resolver with the namespaces of the submodules (as this could not be previously done
             * during direct parsing of the submodule).
             */
            relateSubmodulesToModulesAndUpdateNamespaceResolver();

            /*
             * Before we proceed we make sure that all imports / includes / belongs-to can be fulfilled.
             * That cuts down on the number of checks we have to do later on for missing modules etc.
             */
            SchemaCheckModuleRelationships.performChecks(context, moduleRegistry);

            /*
             * Before proceeding, we check whether there are already any findings that should cause us to fail-fast.
             */
            if (context.failFast() && context.getFindingsManager().hasFindingOfAnyOf(FAIL_FAST_FINDINGS)) {
                context.getFindingsManager().retainFindingsOfType(FAIL_FAST_FINDINGS);
                context.addFinding(new Finding(ParserFindingType.P009_FAIL_FAST,
                        "Parsing has been stopped early due to significant findings with the input. Address these findings first, and then retry your operation."));
                return;
            }

            /*
             * Run onPreSchemaProcessed hook
             */
            context.getCustomProcessors().forEach(proc -> {
                try {
                    proc.onPreSchemaProcessed(context, this);
                } catch (final Exception ex) {
                    context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR,
                            "Custom processor exception: " + ex.getMessage() + " - trace: " + StackTraceHelper
                                    .getStackTraceInfo(ex)));
                }
            });

            /*
             * Now we process, which will resolve groupings, typedefs, deviations, augments
             */
            if (!context.shouldStopAfterInitialParse()) {
                processParsedYangModules(context);
            }

        } catch (final Exception ex) {
            /*
             * If this happens there is a most likely a NPE somewhere as a result of a seriously
             * bad model, make sure to issue the finding so that we can debug.
             */
            context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR, ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }

    /*
     * These findings will cause us to issue a FAIL-FAST finding. It is likely that
     * further processing of the schema would lead to significant issues.
     */
    private static final List<String> FAIL_FAST_FINDINGS = Arrays.asList(ParserFindingType.P000_UNSPECIFIED_ERROR
            .toString(), ParserFindingType.P001_BASIC_FILE_READ_ERROR.toString(), ParserFindingType.P003_DUPLICATE_INPUT
                    .toString(), ParserFindingType.P004_SAME_MODULE_DUPLICATE_IMPLEMENTS.toString(),
            ParserFindingType.P005_NO_IMPLEMENTS.toString(), ParserFindingType.P006_IMPLEMENT_IMPORT_MISMATCH.toString(),
            ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString(),
            ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString(), ParserFindingType.P031_PREFIX_NOT_UNIQUE
                    .toString(), ParserFindingType.P032_MISSING_REVISION.toString(), ParserFindingType.P035_AMBIGUOUS_IMPORT
                            .toString(), ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES.toString(),
            ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString(), ParserFindingType.P038_AMBIGUOUS_INCLUDE.toString(),
            ParserFindingType.P039_UNRESOLVABLE_BELONGS_TO.toString(), ParserFindingType.P040_CIRCULAR_INCLUDE_REFERENCES
                    .toString(), ParserFindingType.P041_DIFFERENT_YANG_VERSIONS_BETWEEN_MODULE_AND_SUBMODULES.toString(),
            ParserFindingType.P043_SAME_MODULE_IMPLEMENTS_MORE_THAN_ONCE.toString(),
            ParserFindingType.P044_SAME_MODULE_IMPLEMENTS_AND_IMPORTS.toString(), ParserFindingType.P045_NOT_A_SUBMODULE
                    .toString(), ParserFindingType.P046_NOT_A_MODULE.toString(),
            ParserFindingType.P047_SUBMODULE_OWNERSHIP_MISMATCH.toString(), ParserFindingType.P048_ORPHAN_SUBMODULE
                    .toString(), ParserFindingType.P050_DUPLICATE_LATEST_REVISION.toString());

    /**
     * Process the parsed schema tree. This will tweak the schema tree in a number of ways, which depends
     * on the context. Normally, resolution of submodules, groupings, typedefs, deviations and augmentation
     * is performed. Also, the namespace, status and config value of each node is computed, and various
     * registries are populated.
     * <p>
     * Normally, there is no need to invoke this method, as it will be invoked internally after the modules
     * have been parsed; however, where the context denotes to stop parsing after the initial read, this
     * method must be explicitly called to process the schema (if so desired).
     */
    public void processParsedYangModules(final ParserExecutionContext context) {

        if (hasBeenProcessed) {
            throw new IllegalStateException("Schema has already been processed.");
        }

        hasBeenProcessed = true;

        try {
            /*
             * The content of submodules is merged into the owning modules.
             */
            if (context.mergeSubmodulesIntoModules()) {
                SchemaProcessor.resolveSubmodules(this);
            }

            /*
             * Handle the case short-hand notation. Important that this is
             * done before further processing.
             */
            SchemaProcessor.fixupOmittedCaseStatements(this);

            /*
             * Handle any missing input/output statements.
             */
            SchemaProcessor.fixupMissingInputOutputStatements(this);

            /*
             * Assign the status to everything. This is needed in some processing in a moment. The
             * status will be adjusted later on, after typedefs and groupings have been resolved.
             */
            SchemaProcessor.assignStatus(this);

            /*
             * Any usage of a derived type is resolved to their respective base types. Usage of
             * grouping are also resolved.
             */
            if (context.resolveDerivedTypesAndGroupings()) {
                TypeResolver.resolveUsagesOfDerivedTypes(context, this);
                UsesResolver.resolveUsagesOfUses(context, this);
            }

            /*
             * Namespace and conformance type must be assigned at this stage, as in the next steps
             * augmentations may be applied which will move statements between trees - so in a given
             * module we can now have data nodes of namespaces different from that of the module.
             * Same is true for the conformance type.
             */
            SchemaProcessor.assignEffectiveNamespaces(this);
            SchemaProcessor.assignEffectiveConformanceType(this);

            /*
             * Merge-in augmentations and deviations
             */
            if (context.resolveAugments()) {
                AugmentResolver.resolveAugments(context, this);
            }
            if (context.resolveDeviations()) {
                DeviationResolver.resolveDeviates(context, this);
            }

            /*
             * Only now will we handle the import-only modules. The reason for this is an edge case
             * where an 'implementing' module augment/deviates multiple other modules, and some of
             * those other modules are conformance 'import-only'. If import-only data nodes are
             * removed before the augments/deviations are handled, the augment/deviate will fail
             * and issue a finding which is really "noise".
             */
            if (context.ignoreImportedProtocolAccessibleObjects()) {
                SchemaProcessor.removeProtocolAccessibleObjects(this);
            }

            /*
             * The status needs to be re-calculated, as there can have been changes to the schema
             * tree due to uses / deviation / augment.
             */
            SchemaProcessor.assignStatus(this);

            /*
             * Also can assign the effective config value here now.
             */
            SchemaProcessor.assignConfig(this);

            /*
             * Now that everything is merged-in and resolved, we can remove any schema node that does
             * not satisfy an if-feature statement. Again, we are doing this relatively late as an
             * augment/deviation may refer to a data node that is removed due to if-feature, and we
             * don't want the augment/deviate to fail unnecessarily.
             */
            if (context.removeSchemaNodesNotSatisfyingIfFeature()) {
                if (context.getSupportedFeatures() == null) {
                    context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR,
                            "Cannot remove schema nodes whose if-feature is not satisfied, as information about the features supported has not been supplied."));
                } else {
                    SchemaProcessor.removeDataNodesNotSatisfyingIfFeature(context, this);
                }
            }

            /*
             * If so desired, remove any findings on schema nodes that are not referenced. This improves
             * the output - if part of a model is not being used, or if parts of the model have been removed
             * due to unsatisfied if-feature or maybe deviations, then the client can choose to not get
             * any findings on those parts of the model.
             */
            if (context.shouldSuppressFindingsOnUnusedSchemaNodes()) {
                SchemaProcessor.removeFindingsOnUnusedSchemaNodes(context, this);
            }

            /*
             * Build the identity registry.
             */
            buildIdentityRegistry();

            /*
             * Collect all annotations that have been declared.
             */
            populateAnnotationRegistry();

            /*
             * Run onPostSchemaProcessed hook
             */
            context.getCustomProcessors().forEach(proc -> {
                try {
                    proc.onPostSchemaProcessed(context, this);
                } catch (final Exception ex) {
                    context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR,
                            "Custom processor exception: " + ex.getMessage() + " - trace: " + StackTraceHelper
                                    .getStackTraceInfo(ex)));
                }
            });

        } catch (final Exception ex) {
            /*
             * Usually a NPE due to a really bad model, create finding so we can debug.
             */
            context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR, ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }

    private static Set<YangModel> getUniqueInputs(final ParserExecutionContext context, final List<YangModel> yangModels) {

        final Set<YangModel> uniqueYangModels = new HashSet<>();

        for (final YangModel yangModel : yangModels) {
            if (uniqueYangModels.contains(yangModel)) {
                context.addFinding(new Finding(ParserFindingType.P003_DUPLICATE_INPUT, "Model Input '" + yangModel
                        .getYangInput().getName() + "' supplied more than once. Remove duplicate from input."));
            } else {
                uniqueYangModels.add(yangModel);
            }
        }

        return uniqueYangModels;
    }

    /**
     * Link up the modules and their submodules so that these can be easily navigated later on.
     */
    private void relateSubmodulesToModulesAndUpdateNamespaceResolver() {

        moduleRegistry.getAllYangModels().stream().filter(input -> input.getYangModelRoot().isSubmodule()).forEach(
                submoduleInput -> {

                    final YBelongsTo yBelongsTo = submoduleInput.getYangModelRoot().getSubmodule().getBelongsTo();
                    final List<YangModel> owningModule = yBelongsTo != null ?
                            moduleRegistry.byModuleName(yBelongsTo.getBelongsToModuleName()) :
                            Collections.<YangModel> emptyList();

                    if (owningModule.size() == 1 && owningModule.get(0).getYangModelRoot().isModule()) {
                        /*
                         * We now have both the submodule and the module that owns it. We can hook
                         * them up so we can easily navigate between them. We also now know the namespace
                         * of the submodule (it is the same as that of the owning module) and can set
                         * the submodule namespace.
                         */
                        submoduleInput.getYangModelRoot().setOwningYangModelRoot(owningModule.get(0).getYangModelRoot());
                        moduleNamespaceResolver.recordModuleMapping(submoduleInput.getYangModelRoot().getSubmodule()
                                .getSubmoduleName(), owningModule.get(0).getYangModelRoot().getNamespace());
                    } else {
                        /*
                         * There is a basic problem linking up the submodule with its owning module.
                         */
                        submoduleInput.getYangModelRoot().setOwningYangModelRoot(null);
                    }
                });
    }

    /**
     * Builds the IdentityRegistry from the schema.
     */
    public void buildIdentityRegistry() {

        identityRegistry.clear();

        /*
         * Pass 1 - simply add all identities to the registry.
         */
        moduleRegistry.getAllYangModels().forEach(yangModel -> {
            final String namespace = yangModel.getYangModelRoot().getNamespace();
            final String moduleName = moduleNamespaceResolver.getModuleForNamespace(namespace);

            final List<YIdentity> identities = yangModel.getYangModelRoot().getModuleOrSubmodule().getChildren(
                    CY.STMT_IDENTITY);
            identities.forEach(yIdentity -> {
                identityRegistry.addIdentity(new YangIdentity(namespace, moduleName, yIdentity.getIdentityName()));
            });
        });

        /*
         * Pass 2 - now handle the bases
         */
        moduleRegistry.getAllYangModels().forEach(yangModel -> {
            final String namespace = yangModel.getYangModelRoot().getNamespace();
            final String moduleName = moduleNamespaceResolver.getModuleForNamespace(namespace);

            final List<YIdentity> identities = yangModel.getYangModelRoot().getModuleOrSubmodule().getChildren(
                    CY.STMT_IDENTITY);
            identities.forEach(yIdentity -> {

                final YangIdentity oneIdentity = new YangIdentity(namespace, moduleName, yIdentity.getIdentityName());

                for (final YBase yBase : yIdentity.getBases()) {

                    final String base = yBase.getValue();
                    String baseNamespace = null;

                    if (base == null) {
                        continue;
                    }

                    if (QNameHelper.hasPrefix(base)) {
                        final ModuleIdentity moduleIdentity = yIdentity.getDomElement().getPrefixResolver()
                                .getModuleForPrefix(QNameHelper.extractPrefix(base));
                        if (moduleIdentity != null) {
                            final YangModel baseYangModel = moduleRegistry.find(moduleIdentity);
                            baseNamespace = baseYangModel.getYangModelRoot().getNamespace();
                        }
                    } else {
                        /*
                         * no prefix, namespace same as that for the other identity.
                         */
                        baseNamespace = namespace;
                    }

                    identityRegistry.addBaseIdentity(oneIdentity, new YangIdentity(baseNamespace, moduleNamespaceResolver
                            .getModuleForNamespace(baseNamespace), QNameHelper.extractName(base)));
                }
            });
        });
    }

    private void populateAnnotationRegistry() {

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            yangModel.getYangModelRoot().getModuleOrSubmodule().getExtensionChildStatements().forEach(
                    extensionStatement -> {

                        final String prefix = extensionStatement.getExtensionModulePrefix();
                        final String extensionStatementName = extensionStatement.getExtensionStatementName();
                        final ModuleIdentity moduleOwningExtensionDefinition = yangModel.getPrefixResolver()
                                .getModuleForPrefix(prefix);

                        if (CIETF.ANNOTATION.equals(extensionStatementName) && CIETF.IETF_YANG_METADATA_MODULE_NAME.equals(
                                moduleOwningExtensionDefinition.getModuleName())) {

                            final String annotationNamespace = yangModel.getYangModelRoot().getNamespace();
                            final String annotationModuleName = yangModel.getYangModelRoot().getOwningYangModelRoot()
                                    .getModule().getModuleName();
                            final String annotationName = extensionStatement.getValue();

                            annotationRegistry.addAnnotation(new YangAnnotation(annotationNamespace, annotationModuleName,
                                    annotationName));
                        }
                    });
        }
    }

    /**
     * Writes out all YANG modules to the specified directory. This is typically done when the DOM has been
     * manipulated (for example, after findings have been fixed) and the resulting DOM should be written
     * out again.
     */
    public void writeOut(final File targetDirectory) throws IOException {
        writeOut(targetDirectory, new DefaultOutputFileNameResolver());
    }

    public void writeOut(final File targetDirectory, final OutputFileNameResolver resolver) throws IOException {
        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {
            YangDomWriter.writeOut(yangModel, resolver, targetDirectory);
        }
    }

    public void writeOut(final OutputStreamResolver resolver) throws IOException {
        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {
            YangDomWriter.writeOut(yangModel, resolver);
        }
    }

    /**
     * Writes out to file(s) those YANG modules that have been marked as having changed.
     * <p>
     * Modules that have been written out are returned as FYI to the client.
     */
    public List<YangModel> writeOutChanged(final File targetDirectory, final OutputFileNameResolver resolver)
            throws IOException {

        final List<YangModel> writtenModelInputs = new ArrayList<>();
        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {
            if (yangModel.getYangModelRoot().getDomDocumentRoot().domHasBeenModified()) {
                YangDomWriter.writeOut(yangModel, resolver, targetDirectory);
                writtenModelInputs.add(yangModel);
            }
        }

        return writtenModelInputs;
    }

    /**
     * Writes out to output stream(s) those YANG modules that have been marked as having changed.
     * <p>
     * Modules that have been written out are returned as FYI to the client.
     */
    public List<YangModel> writeOutChanged(final OutputStreamResolver resolver) throws IOException {

        final List<YangModel> writtenModelInputs = new ArrayList<>();
        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {
            if (yangModel.getYangModelRoot().getDomDocumentRoot().domHasBeenModified()) {
                YangDomWriter.writeOut(yangModel, resolver);
                writtenModelInputs.add(yangModel);
            }
        }

        return writtenModelInputs;
    }
}
