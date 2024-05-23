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
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
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
public class Y3gppNotNotifyable extends ExtensionStatement {

    public Y3gppNotNotifyable(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NO_ARG;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__NOT_NOTIFYABLE;
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_LEAF, CY.STMT_LEAF_LIST,
            CY.STMT_LIST, CY.STMT_CONTAINER, CY.STMT_ANYDATA, CY.STMT_ANYXML);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    @Override
    protected void validate(final ParserExecutionContext context) {
        /*
         * From module _3gpp-common-yang-extensions:
         *
         * Indicates that data change notifications shall not be sent
         * for this attribute. If the extension is not present and other
         * conditions are fulfilled data change notification should be sent.
         * If a list or container already has the notNotifyable
         * extension, that is also valid for all contained data nodes.
         *
         * The statement MUST only be a substatement of a leaf, leaf-list, list,
         * container statement that is contained within the 'attributes'
         * container of an IOC and that represents an attribute or sub-parts of
         * an attribute .
         *
         * Zero or one notNotifyable statement is allowed per parent statement.
         * NO substatements are allowed.
         *
         * Adding this statement is an NBC change, removing it is BC.";
         */
        checkParentAlsoAllowDeviateOrRefine(context);
        checkCardinalityUnderParent(context, 1);

        if (getValue() != null) {
            context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                    "'notNotifyable' extension does not allow for an argument."));
        }
    }
}
