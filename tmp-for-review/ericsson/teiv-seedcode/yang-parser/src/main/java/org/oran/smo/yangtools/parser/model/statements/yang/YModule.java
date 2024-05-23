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
package org.oran.smo.yangtools.parser.model.statements.yang;

import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YModule extends AbstractStatement {

    public YModule(final AbstractStatement modelRoot, final YangDomElement domNode) {
        super(modelRoot, domNode);
    }

    @Override
    public String getStatementIdentifier() {
        return getModuleName();
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_MODULE;
    }

    /**
     * The module name will always be non-null.
     */
    public String getModuleName() {
        return domElement.getValue();
    }

    public List<YAnydata> getAnydata() {
        return getChildren(CY.STMT_ANYDATA);
    }

    public List<YAnyxml> getAnyxmls() {
        return getChildren(CY.STMT_ANYXML);
    }

    public List<YAugment> getAugments() {
        return getChildren(CY.STMT_AUGMENT);
    }

    public List<YChoice> getChoices() {
        return getChildren(CY.STMT_CHOICE);
    }

    public YContact getContact() {
        return getChild(CY.STMT_CONTACT);
    }

    public List<YContainer> getContainers() {
        return getChildren(CY.STMT_CONTAINER);
    }

    public List<YDeviation> getDeviations() {
        return getChildren(CY.STMT_DEVIATION);
    }

    public List<YExtension> getExtensions() {
        return getChildren(CY.STMT_EXTENSION);
    }

    public List<YFeature> getFeatures() {
        return getChildren(CY.STMT_FEATURE);
    }

    public List<YGrouping> getGroupings() {
        return getChildren(CY.STMT_GROUPING);
    }

    public List<YIdentity> getIdentities() {
        return getChildren(CY.STMT_IDENTITY);
    }

    public List<YImport> getImports() {
        return getChildren(CY.STMT_IMPORT);
    }

    public List<YInclude> getIncludes() {
        return getChildren(CY.STMT_INCLUDE);
    }

    public List<YLeaf> getLeafs() {
        return getChildren(CY.STMT_LEAF);
    }

    public List<YLeafList> getLeafLists() {
        return getChildren(CY.STMT_LEAF_LIST);
    }

    public List<YList> getLists() {
        return getChildren(CY.STMT_LIST);
    }

    /**
     * Returns the 'namespace' statement, if any, under the 'module'. To get the actual value of the module's
     * namespace, invoke myModule.getNamespaceValue();
     */
    public YNamespace getNamespace() {
        return getChild(CY.STMT_NAMESPACE);
    }

    /**
     * Returns the value, if any, of the 'namespace' statement under the 'module'.
     */
    public String getNamespaceValue() {
        final YNamespace yNamespace = getNamespace();
        return yNamespace == null ? null : yNamespace.getNamespace();
    }

    public List<YNotification> getNotifications() {
        return getChildren(CY.STMT_NOTIFICATION);
    }

    public YOrganization getOrganization() {
        return getChild(CY.STMT_ORGANIZATION);
    }

    /**
     * Returns the 'prefix' statement, if any, under the 'module'. To get the actual value of the module's
     * prefix, invoke myModule.getPrefixValue();
     */
    public YPrefix getPrefix() {
        return getChild(CY.STMT_PREFIX);
    }

    /**
     * Returns the value, if any, of the 'prefix' statement under the module.
     */
    public String getPrefixValue() {
        final YPrefix yPrefix = getPrefix();
        return yPrefix == null ? null : yPrefix.getPrefix();
    }

    public List<YRevision> getRevisions() {
        return getChildren(CY.STMT_REVISION);
    }

    public List<YRpc> getRpcs() {
        return getChildren(CY.STMT_RPC);
    }

    public List<YTypedef> getTypedefs() {
        return getChildren(CY.STMT_TYPEDEF);
    }

    public List<YUses> getUses() {
        return getChildren(CY.STMT_USES);
    }

    public YYangVersion getYangVersion() {
        return getChild(CY.STMT_YANG_VERSION);
    }

    public boolean is10Version() {
        final YYangVersion yYangVersion = getYangVersion();
        return yYangVersion == null ? false : yYangVersion.is10Version();
    }

    public boolean is11Version() {
        final YYangVersion yYangVersion = getYangVersion();
        return yYangVersion == null ? false : yYangVersion.is11Version();
    }

    protected void validate(final ParserExecutionContext context) {
        validateArgumentNotNullNotEmpty(context);
        validateIsYangIdentifier(context, getModuleName());
    }
}
