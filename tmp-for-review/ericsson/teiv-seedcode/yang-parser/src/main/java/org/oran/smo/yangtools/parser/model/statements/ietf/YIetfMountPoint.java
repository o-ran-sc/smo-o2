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
public class YIetfMountPoint extends ExtensionStatement {

    public YIetfMountPoint(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CIETF.IETF_YANG_SCHEMA_MOUNT__MOUNT_POINT;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    public String getLabel() {
        return getValue() != null ? getValue() : "";
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_LIST, CY.STMT_CONTAINER);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    @Override
    protected void validate(final ParserExecutionContext context) {
        /*
         * From module ietf-yang-schema-mount:
         *
         * The argument 'label' is a YANG identifier, i.e., it is of the
         * type 'yang:yang-identifier'.
         *
         * The 'mount-point' statement MUST NOT be used in a YANG
         * version 1 module, neither explicitly nor via a 'uses'
         * statement.
         * The 'mount-point' statement MAY be present as a substatement
         * of 'container' and 'list' and MUST NOT be present elsewhere.
         * There MUST NOT be more than one 'mount-point' statement in a
         * given 'container' or 'list' statement.
         *
         * If a mount point is defined within a grouping, its label is
         * bound to the module where the grouping is used.
         *
         * A mount point defines a place in the node hierarchy where
         * other data models may be attached.  A server that implements a
         * module with a mount point populates the
         * '/schema-mounts/mount-point' list with detailed information on
         * which data models are mounted at each mount point.
         *
         * Note that the 'mount-point' statement does not define a new
         * data node.
         */
        validateArgumentNotNullNotEmpty(context);

        checkParentAlsoAllowDeviateOrRefine(context);
        checkCardinalityUnderParent(context, 1);
    }
}
