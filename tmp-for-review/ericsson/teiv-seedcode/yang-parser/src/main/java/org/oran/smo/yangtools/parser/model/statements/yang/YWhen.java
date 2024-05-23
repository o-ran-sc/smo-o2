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

import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.SimpleStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YWhen extends SimpleStatement {

    private boolean appliesToParentSchemaNode = false;

    public YWhen(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.CONDITION;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_WHEN;
    }

    public String getXpathExpression() {
        return getValue();
    }

    public void setAppliesToParentSchemaNode() {
        appliesToParentSchemaNode = true;
    }

    public boolean appliesToParentSchemaNode() {
        return appliesToParentSchemaNode;
    }

    @Override
    public void cloneFrom(final AbstractStatement orig) {
        this.appliesToParentSchemaNode = ((YWhen) orig).appliesToParentSchemaNode;
        super.cloneFrom(orig);
    }
}
