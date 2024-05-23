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
package org.oran.smo.yangtools.parser.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.YangInput;
import org.oran.smo.yangtools.parser.model.parser.TokenIterator;
import org.oran.smo.yangtools.parser.model.parser.YamTokenizer;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.statements.YangModelRoot;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomDocumentRoot;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.util.StackTraceHelper;

/**
 * Represents a single YANG model (a "YAM"). This will be either a module or a submodule.
 *
 * @author Mark Hollmann
 */
public class YangModel {

    /*
     * The underlying input. Typically a file with file extension ".yang", but could
     * also be an arbitrary input stream.
     */
    private final YangInput yangInput;

    private int lineCount;		// some useful information, not super-important.
    private int charCount;		// ditto

    private final ConformanceType conformanceType;

    /*
     * The prefix resolver track all prefixes defined in this YAM.
     */
    private ModulePrefixResolver prefixResolver;

    /*
     * The identity of this YAM. May be null if there are fundamental errors in the document.
     */
    private ModuleIdentity moduleIdentity;

    /*
     * The root of the Yang DOM tree.
     */
    private YangDomDocumentRoot yangDomDocumentRoot;

    /*
     * The root of the type-safe schema tree.
     */
    private YangModelRoot yangModelRoot;

    /*
     * Findings made in respect of this YAM, if any.
     */
    private Set<Finding> findings = null;

    public YangModel(final YangInput yangInput, final ConformanceType conformanceType) {
        this.yangInput = yangInput;
        this.conformanceType = conformanceType;
    }

    public YangInput getYangInput() {
        return yangInput;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getCharCount() {
        return charCount;
    }

    public ConformanceType getConformanceType() {
        return conformanceType;
    }

    /**
     * Returns the root of the type-safe statement tree. Note that the returned root is not the
     * representation of the 'module' (or 'submodule'). Returns null if during parsing the type-safe
     * statement was not built.
     */
    public YangModelRoot getYangModelRoot() {
        return yangModelRoot;
    }

    /**
     * Returns the root of the DOM element tree.
     */
    public YangDomDocumentRoot getYangDomDocumentRoot() {
        return yangDomDocumentRoot;
    }

    /**
     * The identity of this YAM. May return null if there are fundamental
     * syntax errors in the document (basically, this is not a valid YAM).
     */
    public ModuleIdentity getModuleIdentity() {
        return moduleIdentity;
    }

    /**
     * The prefix resolver used by this input. This will only be available after the module has been parsed.
     */
    public ModulePrefixResolver getPrefixResolver() {
        return prefixResolver;
    }

    /**
     * Parses a YAM and builds the DOM element tree, and the type-safe statement tree,
     * for this YAM.
     */
    public void parse(final ParserExecutionContext context, final ModuleAndNamespaceResolver namespaceResolver,
            final Schema owningSchema) {
        parse(context, namespaceResolver, owningSchema, true);
    }

    /**
     * Parses a YAM and builds the DOM element tree for this YAM. If so desired, also
     * builds the type-safe statement tree.
     */
    public void parse(final ParserExecutionContext context, final ModuleAndNamespaceResolver namespaceResolver,
            final Schema owningSchema, final boolean createTypeSafeStatementTree) {

        this.prefixResolver = new ModulePrefixResolver(namespaceResolver);

        try {
            /*
             * There might be issues during the parsing, especially syntax errors, somewhere
             * in the bowels of a model. This is likely to result in a NPE somewhere, which is
             * caught here.
             */

            final TokenIterator tokenIterator = tokenize(context);
            if (tokenIterator == null) {
                // Super-basic syntax errors, exit out!
                return;
            }

            /*
             * We create the DOM element tree.
             */
            yangDomDocumentRoot = new YangDomDocumentRoot(this, owningSchema);
            yangDomDocumentRoot.processTokens(context, tokenIterator);

            /*
             * The module identity, and all the prefixes, are extracted *before* the type-safe tree is
             * constructed. Reason: Some of the prefixes defined in the module may be used by extension
             * statements, and in order to instantiate these the prefixes used by these have to be resolvable
             * to their module names *during* the actual processing of the tree.
             */
            extractModuleIdentity(context, yangDomDocumentRoot);
            extractPrefixes(yangDomDocumentRoot);
            extractNamespace(yangDomDocumentRoot, namespaceResolver);

            if (createTypeSafeStatementTree) {
                /*
                 * We build the type-safe tree.
                 */
                yangModelRoot = new YangModelRoot(yangDomDocumentRoot, owningSchema);
                yangModelRoot.processYangDom(context, yangDomDocumentRoot);
            }

        } catch (final Exception ex) {
            context.addFinding(new Finding(this, ParserFindingType.P000_UNSPECIFIED_ERROR, ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }

    private TokenIterator tokenize(final ParserExecutionContext context) {

        TokenIterator tokenIterator = null;

        try (final InputStream inputStream = yangInput.getInputStream()) {

            final YamTokenizer tokenizer = new YamTokenizer(context, this, inputStream);
            tokenIterator = tokenizer.tokenize();

            this.lineCount = tokenizer.getLineCount();
            this.charCount = tokenizer.getCharCount();

        } catch (final IOException ioex) {
            context.addFinding(new Finding(this, ParserFindingType.P001_BASIC_FILE_READ_ERROR, ioex.getMessage()));
        } catch (final Exception ex) {
            context.addFinding(new Finding(this, ParserFindingType.P000_UNSPECIFIED_ERROR, ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }

        return tokenIterator;
    }

    private void extractPrefixes(final YangDomDocumentRoot docRoot) {

        if (docRoot.getChildren().isEmpty()) {
            return;
        }

        final YangDomElement moduleOrSubmodule = docRoot.getChildren().get(0);
        if (CY.MODULE.equals(moduleOrSubmodule.getName())) {
            /*
             * extract 'prefix' statement (if it exists, which it should as it is mandatory according to RFC).
             */
            final List<YangDomElement> prefixChildren = getDomChildrenOfName(moduleOrSubmodule, CY.PREFIX);
            if (prefixChildren.size() == 1 && prefixChildren.get(0).getValue() != null) {
                recordModuleMapping(prefixChildren.get(0).getValue(), this.moduleIdentity.getModuleName(),
                        this.moduleIdentity.getRevision());
            }
        } else {
            /*
             * It's a submodule, extract 'belongs-to' statement.
             */
            final List<YangDomElement> belongsToChildren = getDomChildrenOfName(moduleOrSubmodule, CY.BELONGS_TO);
            if (belongsToChildren.size() == 1) {
                final YangDomElement belongsToDomElement = belongsToChildren.get(0);
                final String belongsToModuleName = belongsToDomElement.getValue();

                final List<YangDomElement> prefixChildren = getDomChildrenOfName(belongsToDomElement, CY.PREFIX);
                if (prefixChildren.size() == 1 && prefixChildren.get(0).getValue() != null) {
                    recordSubmoduleMapping(prefixChildren.get(0).getValue(), belongsToModuleName);
                }
            }
        }

        /*
         * For both 'module' and 'submodule', handle 'import' statements.
         */
        final List<YangDomElement> importChildren = getDomChildrenOfName(moduleOrSubmodule, CY.IMPORT);
        for (final YangDomElement importDomElement : importChildren) {

            final String importedModuleName = importDomElement.getValue();
            String importedModuleRevision = null;

            final List<YangDomElement> revisionDateChildren = getDomChildrenOfName(importDomElement, CY.REVISION_DATE);
            if (revisionDateChildren.size() == 1 && !"".equals(revisionDateChildren.get(0).getValue())) {
                importedModuleRevision = revisionDateChildren.get(0).getValue();
            }

            final List<YangDomElement> prefixChildren = getDomChildrenOfName(importDomElement, CY.PREFIX);
            if (prefixChildren.size() == 1 && prefixChildren.get(0).getValue() != null) {
                recordImportMapping(prefixChildren.get(0).getValue(), importedModuleName, importedModuleRevision);
            }
        }
    }

    /**
     * Records a mapping for a submodule. A submodule will always refer to its owning module by name only, never by
     * revision.
     * <p>
     * Will also record the default mapping, i.e. which module shall be resolved when no prefix is used.
     */
    private void recordSubmoduleMapping(final String prefix, final String moduleName) {
        prefixResolver.addModuleMapping(Objects.requireNonNull(prefix), new ModuleIdentity(Objects.requireNonNull(
                moduleName)));
        prefixResolver.setDefaultModuleMapping(new ModuleIdentity(moduleName));
    }

    /**
     * Records a mapping for a module. A module usually has a revision, in rare cases it does not. If not, then
     * the module revision is truly "unspecified" (not "unknown").
     * <p>
     * Will also record the default mapping, i.e. which module shall be resolved when no prefix is used.
     */
    private void recordModuleMapping(final String prefix, final String moduleName, final String moduleRevision) {
        prefixResolver.addModuleMapping(Objects.requireNonNull(prefix), new ModuleIdentity(Objects.requireNonNull(
                moduleName), moduleRevision));
        prefixResolver.setDefaultModuleMapping(new ModuleIdentity(moduleName, moduleRevision));
    }

    /**
     * Records a mapping for an import statement. The 'import' may have omitted the revision, which can
     * mean either "whatever revision of the module is also in the input", or "a module without revision".
     */
    private void recordImportMapping(final String prefix, final String moduleName, final String moduleRevision) {

        if (moduleRevision == null) {
            /*
             * Unknown revision
             */
            prefixResolver.addModuleMapping(Objects.requireNonNull(prefix), new ModuleIdentity(Objects.requireNonNull(
                    moduleName)));
        } else {
            /*
             * Exact revision specified
             */
            prefixResolver.addModuleMapping(Objects.requireNonNull(prefix), new ModuleIdentity(Objects.requireNonNull(
                    moduleName), moduleRevision));
        }
    }

    private void extractNamespace(final YangDomDocumentRoot docRoot, final ModuleAndNamespaceResolver namespaceResolver) {

        if (docRoot.getChildren().isEmpty()) {
            return;
        }

        final YangDomElement moduleOrSubmodule = docRoot.getChildren().get(0);
        if (CY.MODULE.equals(moduleOrSubmodule.getName())) {
            /*
             * Get the 'namespace' statement (if it exists, which it should).
             */
            final List<YangDomElement> namespaceChildren = getDomChildrenOfName(moduleOrSubmodule, CY.NAMESPACE);
            if (namespaceChildren.size() == 1) {
                final YangDomElement namespaceDomElement = namespaceChildren.get(0);
                if (namespaceDomElement.getValue() != null) {
                    namespaceResolver.recordModuleMapping(this.moduleIdentity.getModuleName(), namespaceDomElement
                            .getValue());
                    namespaceResolver.recordNamespaceMapping(namespaceDomElement.getValue(), this.moduleIdentity
                            .getModuleName());
                }
            }
        }
    }

    /**
     * Extracts the module identity from the DOM tree, if possible. If there are fundamental issues
     * with this, the ModuleIdentity will not be extracted, and subsequently the module will not be
     * added to the registry.
     */
    private void extractModuleIdentity(final ParserExecutionContext context, final YangDomDocumentRoot docRoot) {

        /*
         * We are expecting a single child under root - that being 'module' or 'submodule'.
         */
        if (docRoot.getChildren().size() != 1) {
            return;
        }

        final YangDomElement moduleOrSubmoduleDomElement = docRoot.getChildren().get(0);
        if (!CY.MODULE.equals(moduleOrSubmoduleDomElement.getName()) && !CY.SUBMODULE.equals(moduleOrSubmoduleDomElement
                .getName())) {
            return;
        }

        /*
         * We are expecting the module to have a non-null name.
         */
        if (moduleOrSubmoduleDomElement.getValue() == null || moduleOrSubmoduleDomElement.getValue().isEmpty()) {
            return;
        }

        /*
         * We set the module identity here already now although we don't know the revision yet, as
         * some of the processing in this method may depend on at least the module name being set,
         * especially for filtering of findings.
         */
        final String moduleName = moduleOrSubmoduleDomElement.getValue();
        String revision = null;

        this.moduleIdentity = new ModuleIdentity(moduleName);

        /* From the RFC:
         *
         * "A module SHOULD have at least one "revision" statement. For every
         * published editorial change, a new one SHOULD be added in front of the
         * revisions sequence so that all revisions are in reverse chronological
         * order."
         *
         * That's a bit loose of course. Says SHOULD, so it could be omitted,
         * and the order is not guaranteed.
         */
        final List<YangDomElement> revisions = getDomChildrenOfName(moduleOrSubmoduleDomElement, CY.REVISION);

        if (revisions.isEmpty()) {
            context.addFinding(new Finding(docRoot.getYangModel(), ParserFindingType.P032_MISSING_REVISION,
                    "(Sub-)Module does not have a 'revision' statement."));
            this.moduleIdentity = new ModuleIdentity(moduleName, null);
        } else {
            /*
             * We reverse-sort the revisions to get the 'newest' (i.e. most recent) to the beginning of
             * the list since we don't trust module designers to get this right (somebody might put the
             * latest revision at the *end* of the list of revisions). This sort should
             * be just fine as the format of the revision has to be YYYY-MM-DD so lexical sorting works.
             */
            final Comparator<YangDomElement> byRevision = (YangDomElement r1, YangDomElement r2) -> {
                final String revision1 = r1.getTrimmedValueOrEmpty();
                final String revision2 = r2.getTrimmedValueOrEmpty();
                return revision2.compareTo(revision1);
            };
            revisions.sort(byRevision);

            revision = revisions.get(0).getValue();
            this.moduleIdentity = new ModuleIdentity(moduleName, revision);
        }
    }

    private List<YangDomElement> getDomChildrenOfName(final YangDomElement element, final String soughtDomElementName) {
        final List<YangDomElement> result = new ArrayList<>();

        for (final YangDomElement oneChild : element.getChildren()) {
            if (oneChild.getName().equals(soughtDomElementName)) {
                result.add(oneChild);
            }
        }

        return result;
    }

    public void addFinding(final Finding finding) {
        if (findings == null) {
            findings = new HashSet<>();
        }
        findings.add(finding);
    }

    public void removeFinding(final Finding finding) {
        if (findings != null) {
            findings.remove(finding);
        }
    }

    public Set<Finding> getFindings() {
        return findings == null ? Collections.<Finding> emptySet() : findings;
    }

    @Override
    public int hashCode() {
        return this.yangInput.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return this.yangInput.equals(((YangModel) other).yangInput);
        }

        return false;
    }

    @Override
    public String toString() {
        return moduleIdentity != null ? moduleIdentity.toString() : yangInput.getName();
    }
}
