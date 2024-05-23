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
public class YChoice extends AbstractStatement {

    public YChoice(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public String getStatementIdentifier() {
        return getChoiceName();
    }

    @Override
    public boolean definesSchemaNode() {
        return true;
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_CHOICE;
    }

    public String getChoiceName() {
        return domElement.getValue();
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YAnydata> getAnydata() {
        return getChildren(CY.STMT_ANYDATA);
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YAnyxml> getAnyxmls() {
        return getChildren(CY.STMT_ANYXML);
    }

    public List<YCase> getCases() {
        return getChildren(CY.STMT_CASE);
    }

    public List<YChoice> getChoices() {
        return getChildren(CY.STMT_CHOICE);
    }

    public YConfig getConfig() {
        return getChild(CY.STMT_CONFIG);
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YContainer> getContainers() {
        return getChildren(CY.STMT_CONTAINER);
    }

    public YDefault getDefault() {
        return getChild(CY.STMT_DEFAULT);
    }

    public List<YIfFeature> getIfFeatures() {
        return getChildren(CY.STMT_IF_FEATURE);
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YLeaf> getLeafs() {
        return getChildren(CY.STMT_LEAF);
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YLeafList> getLeafLists() {
        return getChildren(CY.STMT_LEAF_LIST);
    }

    /**
     * This method will always return an empty list after parsing has completed, as any
     * shorthand case-notation will cause a case statement to be injected between the
     * choice and the shorthand statement.
     */
    public List<YList> getLists() {
        return getChildren(CY.STMT_LIST);
    }

    public YMandatory getMandatory() {
        return getChild(CY.STMT_MANDATORY);
    }

    public YStatus getStatus() {
        return getChild(CY.STMT_STATUS);
    }

    public List<YWhen> getWhens() {
        return getChildren(CY.STMT_WHEN);
    }

    protected void validate(final ParserExecutionContext context) {
        validateArgumentNotNullNotEmpty(context);
        validateIsYangIdentifier(context, getChoiceName());
    }
}
