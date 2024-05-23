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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.YBelongsTo;
import org.oran.smo.yangtools.parser.model.statements.yang.YImport;
import org.oran.smo.yangtools.parser.model.statements.yang.YInclude;
import org.oran.smo.yangtools.parser.model.statements.yang.YPrefix;
import org.oran.smo.yangtools.parser.model.statements.yang.YRevision;
import org.oran.smo.yangtools.parser.model.util.StringHelper;

/**
 * Checks for prefixes, imports, revisions, includes and belongs-to in order to catch
 * basic issues before further processing is done.
 *
 * @author Mark Hollmann
 */
public abstract class SchemaCheckModuleRelationships {

    public static void performChecks(final ParserExecutionContext context, final ModuleRegistry moduleRegistry) {

        checkPrefixesUnique(context, moduleRegistry);
        checkImportsUnique(context, moduleRegistry);
        checkImportsSatisfied(context, moduleRegistry);
        checkIncludesSatisfied(context, moduleRegistry);
        checkBelongsTosSatisfied(context, moduleRegistry);
        checkForDuplicateModules(context, moduleRegistry);
        checkForDuplicateRevisions(context, moduleRegistry);
        checkImplementsAndImports(context, moduleRegistry);
    }

    // ==============================================================================================================================================

    /**
     * For each YAM check that all prefixes declared within the YAM are unique. If this is not the case
     * then there will be ambiguity later on when prefixes are resolved.
     */
    private static void checkPrefixesUnique(final ParserExecutionContext context, final ModuleRegistry moduleRegistry) {

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            final Set<String> prefixesInYam = new HashSet<>();

            final YPrefix yPrefix = yangModel.getYangModelRoot().getPrefix();
            if (yPrefix != null) {
                prefixesInYam.add(yPrefix.getPrefix());
            }

            for (final YImport yImport : yangModel.getYangModelRoot().getImports()) {
                if (yImport.getPrefix() == null) {
                    /* No need for a finding, a invalid syntax finding would have previously issued. */
                    continue;
                }

                final String importPrefix = yImport.getPrefix().getValue();
                if (prefixesInYam.contains(importPrefix)) {
                    context.addFinding(new Finding(yImport.getPrefix(), ParserFindingType.P031_PREFIX_NOT_UNIQUE,
                            "Prefix '" + importPrefix + "' not unique in document."));
                }
                prefixesInYam.add(importPrefix);
            }
        }
    }

    // ==============================================================================================================================================

    /**
     * For each YAM check that each import is unique. Note that YANG 1.1 allows the same module to be
     * imported more than once, but then the revision has to be explicitly specified.
     */
    private static void checkImportsUnique(final ParserExecutionContext context, final ModuleRegistry moduleRegistry) {

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            final Set<String> importedModulesUsingRevision = new HashSet<>();
            final Set<String> importedModulesWithoutRevision = new HashSet<>();
            final Set<String> importedModulesNameAndRevision = new HashSet<>();

            for (final YImport yImport : yangModel.getYangModelRoot().getImports()) {

                final String importedModuleName = yImport.getImportedModuleName();
                if (importedModuleName == null) {
                    /* No need for a finding, a invalid syntax finding would have previously issued. */
                    continue;
                }

                final String importedModuleRevision = yImport.getRevisionDate() == null ?
                        null :
                        yImport.getRevisionDate().getValue();

                if (importedModuleRevision == null) {
                    checkOneImportWithoutRevisionIsUnique(context, yImport, importedModuleName,
                            importedModulesUsingRevision, importedModulesWithoutRevision);
                } else {
                    checkOneImportWithRevisionIsUnique(context, yImport, importedModuleName, importedModuleRevision,
                            importedModulesUsingRevision, importedModulesWithoutRevision, importedModulesNameAndRevision);
                }
            }
        }
    }

    private static void checkOneImportWithoutRevisionIsUnique(final ParserExecutionContext context, final YImport yImport,
            final String importedModuleName, final Set<String> importedModulesUsingRevision,
            final Set<String> importedModulesWithoutRevision) {

        /*
         * Imported without revision, i.e. use "any" version of the module. Check that there is not already
         * an import with an explicit revision.
         */
        if (importedModulesUsingRevision.contains(importedModuleName)) {
            context.addFinding(new Finding(yImport, ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES,
                    "Module '" + importedModuleName + "' imported more than once - once without revision, once with explicit revision."));
        }
        if (importedModulesWithoutRevision.contains(importedModuleName)) {
            context.addFinding(new Finding(yImport, ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES,
                    "Module '" + importedModuleName + "' imported multiple times without revision."));
        }

        importedModulesWithoutRevision.add(importedModuleName);
    }

    private static void checkOneImportWithRevisionIsUnique(final ParserExecutionContext context, final YImport yImport,
            final String importedModuleName, final String importedModuleRevision,
            final Set<String> importedModulesUsingRevision, final Set<String> importedModulesWithoutRevision,
            final Set<String> importedModulesNameAndRevision) {
        /*
         * Import with exact revision. Check not already imported without revision, and not doubly-imported.
         */
        if (importedModulesWithoutRevision.contains(importedModuleName)) {
            context.addFinding(new Finding(yImport, ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES,
                    "Module '" + importedModuleName + "' imported more than once - once without revision, once with explicit revision."));
        }

        final String key = importedModuleName + "/" + importedModuleRevision;

        if (importedModulesNameAndRevision.contains(key)) {
            context.addFinding(new Finding(yImport, ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES,
                    "Module '" + key + "' imported more than once."));
        }

        importedModulesUsingRevision.add(importedModuleName);
        importedModulesNameAndRevision.add(key);
    }

    // ==============================================================================================================================================

    /**
     * For each YAM check that all "import" statements can be satisfied,
     * i.e. the imported YAM is also available.
     */
    private static void checkImportsSatisfied(final ParserExecutionContext context, final ModuleRegistry moduleRegistry) {

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {
            for (final YImport yImport : yangModel.getYangModelRoot().getImports()) {

                final String importedModuleName = yImport.getImportedModuleName();
                final String importedModuleRevision = yImport.getRevisionDate() == null ?
                        null :
                        yImport.getRevisionDate().getValue();

                if (importedModuleName == null) {
                    //No need for a finding, a invalid syntax finding would have previously issued.
                    continue;
                }

                if (yangModel.getModuleIdentity().getModuleName().equals(importedModuleName)) {
                    context.addFinding(new Finding(yImport, ParserFindingType.P034_UNRESOLVABLE_IMPORT,
                            "Module '" + importedModuleName + "' imports itself."));
                } else {
                    if (importedModuleRevision == null) {
                        checkOneImportWithoutRevision(context, moduleRegistry, yImport, importedModuleName);
                    } else {
                        checkOneImportWithExplicitRevision(context, moduleRegistry, yImport, importedModuleName,
                                importedModuleRevision);
                    }
                }
            }
        }
    }

    private static void checkOneImportWithoutRevision(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry, final YImport yImport, final String importedModuleName) {
        /*
         * The import does not have a revisions. So try to find exactly one module of the given name in the input.
         */
        final List<YangModel> foundModulesAnyRevision = moduleRegistry.byModuleName(importedModuleName);
        switch (foundModulesAnyRevision.size()) {
            case 0:
                context.addFinding(new Finding(yImport, ParserFindingType.P034_UNRESOLVABLE_IMPORT,
                        "Module '" + importedModuleName + "' not found in input."));
                break;
            case 1:
                /*
                 * OK, the import uses "any" revision and exactly one revision of the module was found in the registry.
                 */
                break;
            default:
                /*
                 * This is ambiguous, so issue a finding.
                 */
                context.addFinding(new Finding(yImport, ParserFindingType.P035_AMBIGUOUS_IMPORT,
                        "Module '" + importedModuleName + "' has multiple revisions in the input, but desired exact revision not specified in the 'import' statement."));
        }
    }

    private static void checkOneImportWithExplicitRevision(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry, final YImport yImport, final String importedModuleName,
            final String importedModuleRevision) {
        /*
         * The import has a revisions. So try to find exactly one module of the given name and given revision in the input.
         */
        final YangModel foundModulesExactRevision = moduleRegistry.exactMatch(importedModuleName, importedModuleRevision);
        if (foundModulesExactRevision == null) {
            /*
             * That revision does not exist. Maybe a different revision is in the input?
             */
            final List<YangModel> foundModulesAnyRevision = moduleRegistry.byModuleName(importedModuleName);
            if (foundModulesAnyRevision.isEmpty()) {
                context.addFinding(new Finding(yImport, ParserFindingType.P034_UNRESOLVABLE_IMPORT,
                        "Module '" + importedModuleName + "' with revision '" + importedModuleRevision + "' not found in input."));
            } else {
                context.addFinding(new Finding(yImport, ParserFindingType.P034_UNRESOLVABLE_IMPORT,
                        "Module '" + importedModuleName + "' with revision '" + importedModuleRevision + "' not found in input, but a module with that name and revision '" + foundModulesAnyRevision
                                .get(0).getModuleIdentity().getRevision() + "' has been found."));
            }
        }
    }

    // ==============================================================================================================================================

    /**
     * For each submodule check that the "belongs-to" statement can be satisfied.
     */
    private static void checkBelongsTosSatisfied(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry) {

        final List<YangModel> subModules = moduleRegistry.getAllYangModels().stream().filter(yangModel -> yangModel
                .getYangModelRoot().isSubmodule()).collect(Collectors.toList());

        for (final YangModel subModuleYangModel : subModules) {

            final YBelongsTo belongsTo = subModuleYangModel.getYangModelRoot().getSubmodule().getBelongsTo();
            if (belongsTo == null) {
                /* No need for a finding, would have been previously issued as missing mandatory child statement. */
                continue;
            }

            final String belongsToModuleName = belongsTo.getBelongsToModuleName();
            if (belongsToModuleName == null) {
                /* No need for a finding, a invalid syntax finding would have previously issued. */
                continue;
            }

            final List<YangModel> modules = moduleRegistry.byModuleName(belongsToModuleName);
            if (modules.isEmpty()) {
                context.addFinding(new Finding(belongsTo, ParserFindingType.P039_UNRESOLVABLE_BELONGS_TO,
                        "Owning module '" + belongsToModuleName + "' not found in input."));
            } else {
                checkBelongsToAgainstModule(context, belongsTo, subModuleYangModel, modules.get(0));
            }
        }
    }

    private static void checkBelongsToAgainstModule(final ParserExecutionContext context, final YBelongsTo belongsTo,
            final YangModel subModuleModelInput, final YangModel owningModuleModelInput) {

        /*
         * Check the found module is actually a module, and that it actually references the submodule!
         */
        if (!owningModuleModelInput.getYangModelRoot().isModule()) {
            context.addFinding(new Finding(belongsTo, ParserFindingType.P046_NOT_A_MODULE, "'" + belongsTo
                    .getBelongsToModuleName() + "' is not a module."));
        } else {
            final String subModuleName = subModuleModelInput.getYangModelRoot().getModuleOrSubModuleName();
            boolean owningModuleIncludesTheSubmodule = false;
            for (final YInclude include : owningModuleModelInput.getYangModelRoot().getModule().getIncludes()) {
                if (subModuleName.equals(include.getIncludedSubModuleName())) {
                    owningModuleIncludesTheSubmodule = true;
                    break;
                }
            }
            if (!owningModuleIncludesTheSubmodule) {
                context.addFinding(new Finding(subModuleModelInput, ParserFindingType.P048_ORPHAN_SUBMODULE,
                        "Owning module '" + owningModuleModelInput.getYangModelRoot()
                                .getModuleOrSubModuleName() + "' does not 'include' this submodule."));
            }
        }
    }

    // ==============================================================================================================================================

    /**
     * Check up the usage of the Conformance Types.
     */
    private static void checkImplementsAndImports(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry) {

        int nrYangFilesThatImplement = 0;

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            if (yangModel.getConformanceType() == ConformanceType.IMPLEMENT) {
                nrYangFilesThatImplement++;
            }

            if (yangModel.getYangModelRoot().isSubmodule()) {
                final YBelongsTo belongsTo = yangModel.getYangModelRoot().getSubmodule().getBelongsTo();
                if (belongsTo == null) {
                    /* No need for a finding, a missing mandatory child statement would have issued already. */
                    return;
                }

                final String belongsToModuleName = belongsTo.getBelongsToModuleName();
                if (belongsToModuleName == null) {
                    /* No need for a finding, an invalid syntax finding would have issued already. */
                    return;
                }

                final List<YangModel> modules = moduleRegistry.byModuleName(belongsToModuleName);
                if (!modules.isEmpty() && yangModel.getConformanceType() != modules.get(0).getConformanceType()) {
                    context.addFinding(new Finding(belongsTo, ParserFindingType.P006_IMPLEMENT_IMPORT_MISMATCH,
                            "Submodule is " + yangModel.getConformanceType().toString() + " but owning module is " + modules
                                    .get(0).getConformanceType()));
                }
            }
        }

        if (nrYangFilesThatImplement == 0) {
            context.addFinding(new Finding(ParserFindingType.P005_NO_IMPLEMENTS,
                    "Need at least a single module that IMPLEMENTs."));
        }
    }

    // ==============================================================================================================================================

    /**
     * A module with the exact same name should not be more than once in the input as IMPLEMENT.
     */
    private static void checkForDuplicateModules(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry) {

        final Set<String> implementingModules = new HashSet<>();
        final Set<String> importedModules = new HashSet<>();

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            final String moduleName = yangModel.getModuleIdentity().getModuleName();

            if (yangModel.getConformanceType() == ConformanceType.IMPLEMENT) {

                if (implementingModules.contains(moduleName)) {
                    context.addFinding(new Finding(yangModel, ParserFindingType.P043_SAME_MODULE_IMPLEMENTS_MORE_THAN_ONCE,
                            "Module '" + moduleName + "' multiple times in the input with conformance type IMPLEMENT."));
                }
                if (importedModules.contains(moduleName)) {
                    context.addFinding(new Finding(yangModel, ParserFindingType.P044_SAME_MODULE_IMPLEMENTS_AND_IMPORTS,
                            "Module '" + moduleName + "' multiple times in the input, with both conformance types IMPLEMENT and IMPORT."));
                }
                implementingModules.add(moduleName);

            } else { // ConformanceType.IMPORT

                if (implementingModules.contains(moduleName)) {
                    context.addFinding(new Finding(yangModel, ParserFindingType.P044_SAME_MODULE_IMPLEMENTS_AND_IMPORTS,
                            "Module '" + moduleName + "' multiple times in the input, with both conformance types IMPLEMENT and IMPORT."));
                }
                importedModules.add(moduleName);
            }
        }
    }

    // ==============================================================================================================================================

    /**
     * A module should not have duplicate revisions.
     */
    private static void checkForDuplicateRevisions(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry) {

        for (final YangModel yangModel : moduleRegistry.getAllYangModels()) {

            final Set<String> uniqueRevisions = new HashSet<>();

            for (final YRevision yRevision : yangModel.getYangModelRoot().getRevisions()) {
                final String revision = yRevision.getValue();
                if (uniqueRevisions.contains(revision)) {
                    if (revision.equals(yangModel.getModuleIdentity().getRevision())) {
                        context.addFinding(new Finding(yangModel, ParserFindingType.P050_DUPLICATE_LATEST_REVISION,
                                "Latest revision '" + revision + "' exists more than once in the (sub-)module."));
                    } else {
                        context.addFinding(new Finding(yangModel, ParserFindingType.P049_DUPLICATE_REVISION,
                                "Prior revision '" + revision + "' exists more than once in the (sub-)module."));
                    }
                }
                uniqueRevisions.add(revision);
            }
        }
    }

    // ==============================================================================================================================================

    /**
     * For each module check that all "include" statements can be satisfied, i.e. are in the input. Also that the submodule
     * belongs to the module.
     */
    private static void checkIncludesSatisfied(final ParserExecutionContext context, final ModuleRegistry moduleRegistry) {

        final List<YangModel> modulesOnly = moduleRegistry.getAllYangModels().stream().filter(yangModel -> yangModel
                .getYangModelRoot().isModule()).collect(Collectors.toList());

        for (final YangModel yangModel : modulesOnly) {

            for (final YInclude yInclude : yangModel.getYangModelRoot().getModule().getIncludes()) {

                final String includedSubmoduleName = yInclude.getIncludedSubModuleName();
                if (includedSubmoduleName == null) {
                    // No need for a finding, an invalid syntax finding would have issued already.
                    continue;
                }

                final String includedSubmoduleRevision = yInclude.getRevisionDate() == null ?
                        null :
                        yInclude.getRevisionDate().getValue();

                if (includedSubmoduleRevision == null) {
                    checkOneIncludeWithoutRevision(context, moduleRegistry, yangModel, yInclude, includedSubmoduleName);
                } else {
                    checkOneIncludeWithExplicitRevision(context, moduleRegistry, yangModel, yInclude, includedSubmoduleName,
                            includedSubmoduleRevision);
                }
            }
        }
    }

    private static void checkOneIncludeWithoutRevision(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry, final YangModel yangModel, final YInclude yInclude,
            final String includedSubmoduleName) {
        /*
         * The include was done without revision, so try to find "any" revision.
         */
        final List<YangModel> subModulesFoundAnyRevision = moduleRegistry.byModuleName(includedSubmoduleName);

        switch (subModulesFoundAnyRevision.size()) {
            case 0:
                context.addFinding(new Finding(yInclude, ParserFindingType.P037_UNRESOLVABLE_INCLUDE,
                        "Submodule " + includedSubmoduleName + " not found in input."));
                break;
            case 1:
                checkIncludedSubmoduleAgainstModule(context, yInclude, subModulesFoundAnyRevision.get(0), yangModel);
                break;
            default:
                context.addFinding(new Finding(yInclude, ParserFindingType.P038_AMBIGUOUS_INCLUDE,
                        "Multiple revisions of submodule " + includedSubmoduleName + " found in input."));
                break;
        }
    }

    private static void checkOneIncludeWithExplicitRevision(final ParserExecutionContext context,
            final ModuleRegistry moduleRegistry, final YangModel yangModel, final YInclude yInclude,
            final String includedSubmoduleName, final String includedSubmoduleRevision) {
        /*
         * The include was done with revision, so try to find the exact revision.
         */
        final String moduleNameAndRevision = StringHelper.getModuleNameAndRevision(includedSubmoduleName,
                includedSubmoduleRevision);

        final YangModel subModuleFoundWithExactRevision = moduleRegistry.exactMatch(includedSubmoduleName,
                includedSubmoduleRevision);
        if (subModuleFoundWithExactRevision == null) {
            final List<YangModel> subModulesFoundAnyRevision = moduleRegistry.byModuleName(includedSubmoduleName);
            if (subModulesFoundAnyRevision.isEmpty()) {
                context.addFinding(new Finding(yInclude, ParserFindingType.P037_UNRESOLVABLE_INCLUDE,
                        "Submodule " + moduleNameAndRevision + " not found in input."));
            } else {
                /*
                 * Different revision found.
                 */
                context.addFinding(new Finding(yInclude, ParserFindingType.P037_UNRESOLVABLE_INCLUDE,
                        "Submodule " + moduleNameAndRevision + " not found in the input, but a submodule with that name and with revision '" + subModulesFoundAnyRevision
                                .get(0).getModuleIdentity().getRevision() + "' is in the input."));
            }
        } else {
            checkIncludedSubmoduleAgainstModule(context, yInclude, subModuleFoundWithExactRevision, yangModel);
        }
    }

    private static void checkIncludedSubmoduleAgainstModule(final ParserExecutionContext context,
            final YInclude includeStatement, final YangModel subModuleModelInput, final YangModel moduleModelInput) {

        final String yangVersionOfSubmodule = subModuleModelInput.getYangModelRoot().getYangVersion();
        final String yangVersionOfModule = moduleModelInput.getYangModelRoot().getYangVersion();

        /*
         * Make sure the submodule is actually a submodule.
         */
        if (!subModuleModelInput.getYangModelRoot().isSubmodule()) {
            context.addFinding(new Finding(includeStatement, ParserFindingType.P045_NOT_A_SUBMODULE,
                    "'" + subModuleModelInput.getYangModelRoot()
                            .getModuleOrSubModuleName() + "' is not a submodule and can therefore not be included."));
            return;
        }
        /*
         * YANG versions must match up between module and all its submodules.
         */
        if (!yangVersionOfModule.equals(yangVersionOfSubmodule)) {
            context.addFinding(new Finding(includeStatement,
                    ParserFindingType.P041_DIFFERENT_YANG_VERSIONS_BETWEEN_MODULE_AND_SUBMODULES,
                    "The yang versions differ between module and submodule(s)."));
        }
        /*
         * Make sure the submodule actually belongs to the module.
         */
        final YBelongsTo belongsTo = subModuleModelInput.getYangModelRoot().getSubmodule().getBelongsTo();
        if (belongsTo == null) {
            // No need for a finding, a missing mandatory child statement finding would have issued already.
            return;
        }

        final String belongsToModuleName = belongsTo.getBelongsToModuleName();
        if (belongsToModuleName == null) {
            // No need for a finding, an invalid syntax finding would have issued already.
            return;
        }

        if (!belongsToModuleName.equals(moduleModelInput.getYangModelRoot().getModuleOrSubModuleName())) {
            context.addFinding(new Finding(includeStatement, ParserFindingType.P047_SUBMODULE_OWNERSHIP_MISMATCH,
                    "The referenced submodule belongs to '" + belongsToModuleName + "', not this module here."));
        }
    }
}
