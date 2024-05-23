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

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.SimpleStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YStatus extends SimpleStatement {

    public static final String CURRENT = "current";
    public static final String DEPRECATED = "deprecated";
    public static final String OBSOLETE = "obsolete";

    public YStatus(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_STATUS;
    }

    public boolean isCurrent() {
        return CURRENT.equals(getValue());
    }

    public boolean isDeprecated() {
        return DEPRECATED.equals(getValue());
    }

    public boolean isObsolete() {
        return OBSOLETE.equals(getValue());
    }

    /**
     * Returns an integer that may be used to compare the status value for two data nodes.
     * The general rules are: CURRENT &lt; DEPRECATED &lt; OBSOLETE. The value returned
     * has no meaning in itself and especially should not be used to identity a certain
     * status value; it is only meaningful when compared to another value returned from
     * this method.
     */
    public int getStatusOrder() {
        return getStatusOrder(getValue());
    }

    /**
     * Returns an integer that may be used to compare the status value for two data nodes.
     * The general rules are: CURRENT &lt; DEPRECATED &lt; OBSOLETE. The value returned
     * has no meaning in itself and especially should not be used to identity a certain
     * status value; it is only meaningful when compared to another value returned from
     * this method.
     */
    public static int getStatusOrder(final String status) {
        switch (status) {
            case CURRENT:
                return 20;
            case DEPRECATED:
                return 30;
            case OBSOLETE:
                return 40;
        }

        return 0;
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        if (!isCurrent() && !isDeprecated() && !isObsolete()) {
            context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                    "'" + getValue() + "' is not a valid status. Use one of: current, deprecated, obsolete."));
        }
    }
}
