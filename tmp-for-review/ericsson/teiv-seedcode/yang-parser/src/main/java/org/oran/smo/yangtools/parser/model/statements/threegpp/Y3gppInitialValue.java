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
package org.oran.smo.yangtools.parser.model.statements.threegpp;

import java.util.Arrays;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe 3GPP statement.
 *
 * @author Mark Hollmann
 */
public class Y3gppInitialValue extends ExtensionStatement {

    public Y3gppInitialValue(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    @Override
    public boolean orderUnderParentMatters() {
        return true;
    }

    public String getInitialValue() {
        return getValue() != null ? getValue() : "";
    }

    @Override
    public MaxCardinality getMaxCardinalityUnderParent() {
        return getParentStatement().is(CY.STMT_LEAF) ? MaxCardinality.ONE : MaxCardinality.MULTIPLE;
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_LEAF, CY.STMT_LEAF_LIST);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    protected void validate(final ParserExecutionContext context) {
        /*
         * From module _3gpp-common-yang-extensions:
         *
         * Specifies a value that the system will set for a leaf
         * leaf-list if a value is not specified for it when its parent list
         * or container is created. The value has no effect in any other
         * modification e.g. changing or removing the value.
         *
         * The description statement of the parent statement SHOULD contain
         * the label 'Initial-value: ' followed by the text from the argument.
         *
         * The statement MUST only be a substatement of a leaf or leaf-list.
         * The statement MUST NOT be present if the leaf or the leaf-list
         * has a default statement or the type used for the data node
         * has a default value.
         * The statement MUST NOT be used for config=false data or in an
         * action, rpc or notification.
         * Zero or one initial-value statements are allowed for a leaf parent
         * statement. Zero or more initial-value statements are allowed for a
         * leaf-list parent statement. If the leaf-list is ordered-by user, the
         * initial values are stored in the order they appear in the YANG definition.
         * NO substatements are allowed.
         *
         * Always consider using a YANG-default statement instead.
         *
         * Modification of the initial-value is a non-backwards-compatible change.
         *
         * The argument specifies a single initial value for a leaf or leaf-list.
         * The value MUST be part of the valuespace of the leaf/leaf-list.
         * It follows the same rules as the argument of the default statement.
         */
        validateArgumentNotNull(context);
        checkParentAlsoAllowDeviateOrRefine(context);

        if (getParentStatement().is(CY.STMT_LEAF)) {
            checkCardinalityUnderParent(context, 1);
        }
    }
}
