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
package org.oran.smo.yangtools.parser.model.statements.oran;

import java.util.Arrays;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

public class YOranSmoTeivASide extends ExtensionStatement {

    public YOranSmoTeivASide(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__A_SIDE;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    public String getTeivTypeName() {
        return getValue() != null ? getValue() : "";
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_LEAF, CY.STMT_LEAF_LIST);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    @Override
    protected void validate(final ParserExecutionContext context) {
        /*
         * From o-ran-smo-teiv-common-yang-extensions:
         *
         * Defines the A-side of a relationship.
         *
         * The statement MUST only be a substatement of a 'leaf' or 'leaf-list'
         * statement, which itself must be a substatement of the
         * 'uni-directional-topology-relationship' statement.
         *
         * The data type of the parent 'leaf' or 'leaf-list' MUST be
         * 'instance-identifier'. Constraints MAY be used as part of the parent
         * 'leaf' or 'leaf-list' to enforce cardinality.
         *
         * The identifier of the parent 'leaf' or 'leaf-list' is used as name of
         * the role of the A-side of the relationship. The name of the role is
         * scoped to the type on which the A-side is defined and MUST be unique
         * within the scope.
         *
         * While the parent 'leaf' or 'leaf-list' does not result in a property of
         * the relationship, it is RECOMMENDED to avoid using the name of an
         * existing type property as role name to avoid potential ambiguities
         * between properties of a type, and roles of a relationship on the type.
         *
         * The argument is the name of the type on which the A-side resides. If the
         * type is declared in another module, the type must be prefixed, and a
         * corresponding 'import' statement be used to declare the prefix.";
         */
        validateArgumentNotNullNotEmpty(context);

        checkParent(context);
        checkCardinalityUnderParent(context, 1);
    }
}
