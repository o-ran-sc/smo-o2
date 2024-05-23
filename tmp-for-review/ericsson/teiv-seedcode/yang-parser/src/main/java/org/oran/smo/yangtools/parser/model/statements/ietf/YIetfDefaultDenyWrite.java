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
package org.oran.smo.yangtools.parser.model.statements.ietf;

import java.util.Arrays;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe IETF statement.
 *
 * @author Mark Hollmann
 */
public class YIetfDefaultDenyWrite extends ExtensionStatement {

    public YIetfDefaultDenyWrite(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NO_ARG;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CIETF.IETF_NETCONF_ACM__DEFAULT_DENY_WRITE;
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
         * From module ietf-netconf-acm:
         *
         * Used to indicate that the data model node
         * represents a sensitive security system parameter.
         *
         * If present, the NETCONF server will only allow the designated
         * 'recovery session' to have write access to the node.  An
         * explicit access control rule is required for all other users.
         *
         * If the NACM module is used, then it must be enabled (i.e.,
         * /nacm/enable-nacm object equals 'true'), or this extension
         * is ignored.
         *
         * The 'default-deny-write' extension MAY appear within a data
         * definition statement.  It is ignored otherwise.
         */
        validateArgumentIsNull(context);

        checkParentAlsoAllowDeviateOrRefine(context);
        checkCardinalityUnderParent(context, 1);
    }
}
