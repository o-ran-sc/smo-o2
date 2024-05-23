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
public class YList extends AbstractStatement {

    public YList(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public String getStatementIdentifier() {
        return getListName();
    }

    @Override
    public boolean definesSchemaNode() {
        return true;
    }

    @Override
    public boolean definesDataNode() {
        return true;
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_LIST;
    }

    public String getListName() {
        return domElement.getValue();
    }

    public List<YAction> getActions() {
        return getChildren(CY.STMT_ACTION);
    }

    public List<YAnydata> getAnydata() {
        return getChildren(CY.STMT_ANYDATA);
    }

    public List<YAnyxml> getAnyxmls() {
        return getChildren(CY.STMT_ANYXML);
    }

    public List<YChoice> getChoices() {
        return getChildren(CY.STMT_CHOICE);
    }

    public YConfig getConfig() {
        return getChild(CY.STMT_CONFIG);
    }

    public List<YContainer> getContainers() {
        return getChildren(CY.STMT_CONTAINER);
    }

    public List<YGrouping> getGroupings() {
        return getChildren(CY.STMT_GROUPING);
    }

    public List<YIfFeature> getIfFeatures() {
        return getChildren(CY.STMT_IF_FEATURE);
    }

    public YKey getKey() {
        return getChild(CY.STMT_KEY);
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

    public YMaxElements getMaxElements() {
        return getChild(CY.STMT_MAX_ELEMENTS);
    }

    public YMinElements getMinElements() {
        return getChild(CY.STMT_MIN_ELEMENTS);
    }

    public List<YMust> getMusts() {
        return getChildren(CY.STMT_MUST);
    }

    public List<YNotification> getNotifications() {
        return getChildren(CY.STMT_NOTIFICATION);
    }

    public YOrderedBy getOrderedBy() {
        return getChild(CY.STMT_ORDERED_BY);
    }

    public YStatus getStatus() {
        return getChild(CY.STMT_STATUS);
    }

    public List<YTypedef> getTypedefs() {
        return getChildren(CY.STMT_TYPEDEF);
    }

    public List<YUnique> getUniques() {
        return getChildren(CY.STMT_UNIQUE);
    }

    public List<YUses> getUses() {
        return getChildren(CY.STMT_USES);
    }

    public List<YWhen> getWhens() {
        return getChildren(CY.STMT_WHEN);
    }

    protected void validate(final ParserExecutionContext context) {
        validateArgumentNotNullNotEmpty(context);
        validateIsYangIdentifier(context, getListName());
    }
}
