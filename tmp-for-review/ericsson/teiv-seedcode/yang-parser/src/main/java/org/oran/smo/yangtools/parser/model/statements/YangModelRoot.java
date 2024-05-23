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
package org.oran.smo.yangtools.parser.model.statements;

import java.util.ArrayList;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModulePrefixResolver;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.statements.yang.YImport;
import org.oran.smo.yangtools.parser.model.statements.yang.YInclude;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YNamespace;
import org.oran.smo.yangtools.parser.model.statements.yang.YPrefix;
import org.oran.smo.yangtools.parser.model.statements.yang.YRevision;
import org.oran.smo.yangtools.parser.model.statements.yang.YSubmodule;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomDocumentRoot;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Root element for a YANG statement tree. Every YAM will result in a tree of type-safe statements.
 * Usages of groupings and typedefs, augmentations and deviations, and merge-in of submodule content,
 * will all be performed on the tree (depending on settings in the context).
 * <p/>
 * For each YAM, there will be both a Yang DOM element tree (see {@link YangDomDocumentRoot}) and a
 * YANG statement tree. However, these are not the exact same, and it is important to understand the
 * difference:
 * <p/>
 * <ul>
 * <li>The Yang DOM element tree is an exact 1:1 representation of the original YAM. It is the result
 * of the parse of the YAM. During runtime of the parser it does not change, with some marked
 * exceptions (see {@link YangDomDocumentRoot}).</li>
 * <li>The YANG statement tree is the end result of applying various Yang statements (such as augment)
 * to the YAM. This means that the YANG statement tree for a given YAM may end up pointing to DOM
 * elements located in a different YAM (eg. the YAM that contains the 'augment' statement).</li>
 * </ul>
 * <p/>
 * For compliant YAMs, an instance of this class will only ever have a single child statement (of type
 * YModule or YSubmodule).
 *
 * @author Mark Hollmann
 */
public class YangModelRoot extends AbstractStatement {

    /**
     * Pointer to the root of the corresponding Yang DOM element tree.
     */
    private final YangDomDocumentRoot domDocumentRoot;

    private final Schema owningSchema;

    private YModule module = null;
    private YSubmodule submodule = null;

    /**
     * This is the model root of the owner of this YAM. This is either the module itself (i.e. "this")
     * or the module owning this submodule.
     */
    private YangModelRoot owningYangModelRoot = this;

    /**
     * The submodules owned by this module here, if any.
     */
    private List<YangModelRoot> ownedSubmodules = new ArrayList<>();

    public YangModelRoot(final YangDomDocumentRoot domDocumentRoot, final Schema owningSchema) {
        super(domDocumentRoot);
        this.domDocumentRoot = domDocumentRoot;
        this.owningSchema = owningSchema;
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NO_ARG;
    }

    public YangDomDocumentRoot getDomDocumentRoot() {
        return domDocumentRoot;
    }

    public Schema getOwningSchema() {
        return owningSchema;
    }

    public YangModel getYangModel() {
        return domDocumentRoot.getYangModel();
    }

    /**
     * Will tweak the pointer to point at the module that owns this module here.
     */
    public void setOwningYangModelRoot(final YangModelRoot owningYangModelRoot) {
        this.owningYangModelRoot = owningYangModelRoot;
        if (owningYangModelRoot != null) {
            this.owningYangModelRoot.ownedSubmodules.add(this);
        }
    }

    /**
     * Returns the module owning this submodule. Returns "this" if this YAM is a module.
     */
    public YangModelRoot getOwningYangModelRoot() {
        return owningYangModelRoot;
    }

    /**
     * Returns the submodules owned by this module. Will be empty if this YAM here is a
     * submodule, or if this module has no submodules.
     */
    public List<YangModelRoot> getOwnedSubmodules() {
        return ownedSubmodules;
    }

    /**
     * The namespace of the module, or the namespace of the owning module if this here is a submodule.
     * May return null if a namespace has not been defined on the module, or the submodule is orphaned.
     */
    public String getNamespace() {

        if (isModule()) {
            final YNamespace yNamespace = module.getNamespace();
            return yNamespace != null ? yNamespace.getValue() : null;
        }

        return owningYangModelRoot != null ? owningYangModelRoot.getNamespace() : null;
    }

    @Override
    public ModulePrefixResolver getPrefixResolver() {
        return getYangModel().getPrefixResolver();
    }

    @Override
    protected void validate(ParserExecutionContext context) {
        // nothing to validate
    }

    public YModule getModule() {
        return module;
    }

    public YSubmodule getSubmodule() {
        return submodule;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> T getModuleOrSubmodule() {
        return module != null ? (T) module : (T) submodule;
    }

    /**
     * Processes the DOM and starts building the statement tree in accordance with the DOM.
     */
    public void processYangDom(final ParserExecutionContext context, final YangDomDocumentRoot domDocRoot) {

        final List<YangDomElement> childrenOfDocRoot = domDocRoot.getChildren();
        if (childrenOfDocRoot.size() != 1) {
            context.addFinding(new Finding(getYangModel(), ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
                    "Expected single statement ('module' or 'submodule')."));
            return;
        }

        final YangDomElement moduleElement = childrenOfDocRoot.get(0).getName().equals("module") ?
                childrenOfDocRoot.get(0) :
                null;
        final YangDomElement submoduleElement = childrenOfDocRoot.get(0).getName().equals("submodule") ?
                childrenOfDocRoot.get(0) :
                null;

        if (moduleElement != null) {
            module = new YModule(this, moduleElement);
            module.process(context);
        } else if (submoduleElement != null) {
            submodule = new YSubmodule(this, submoduleElement);
            submodule.process(context);
        } else {
            context.addFinding(new Finding(getYangModel(), ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
                    "Expected either 'module' or 'submodule' as root-statement in the document."));
        }
    }

    public boolean isModule() {
        return module != null;
    }

    public boolean isSubmodule() {
        return submodule != null;
    }

    /**
     * The module / submodule name will always be non-null.
     */
    public String getModuleOrSubModuleName() {
        return isModule() ? module.getModuleName() : submodule.getSubmoduleName();
    }

    public List<YRevision> getRevisions() {
        return isModule() ? module.getRevisions() : submodule.getRevisions();
    }

    public List<YImport> getImports() {
        return isModule() ? module.getImports() : submodule.getImports();
    }

    public List<YInclude> getIncludes() {
        return isModule() ? module.getIncludes() : submodule.getIncludes();
    }

    /**
     * Returns the prefix statement of the module, or the prefix statement underneath the 'belongs-to' if
     * it is a submodule. May return null if the prefix statement does not exist (which would be a very
     * basic error).
     */
    public YPrefix getPrefix() {
        return isModule() ?
                module.getPrefix() :
                submodule.getBelongsTo() != null ? submodule.getBelongsTo().getPrefix() : null;
    }

    public String getYangVersion() {
        if (isModule()) {
            return module.getYangVersion() == null ? "1" : module.getYangVersion().getValue();
        }

        return submodule.getYangVersion() == null ? "1" : submodule.getYangVersion().getValue();
    }
}
