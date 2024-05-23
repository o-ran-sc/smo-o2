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
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YInclude extends AbstractStatement {

    public YInclude(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.MODULE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_INCLUDE;
    }

    public String getIncludedSubModuleName() {
        return domElement.getValue();
    }

    /**
     * Returns the 'revision-date' statement, if any, under the 'include'. To get the actual value of the include's
     * revision-date, invoke myInclude.getRevisionDateValue();
     */
    public YRevisionDate getRevisionDate() {
        return getChild(CY.STMT_REVISION_DATE);
    }

    /**
     * Returns the value, if any, of the 'revision-date' statement under the 'include'.
     */
    public String getRevisionDateValue() {
        final YRevisionDate yRevisionDate = getRevisionDate();
        return yRevisionDate == null ? null : yRevisionDate.getRevisionDateValue();
    }

    protected void validate(final ParserExecutionContext context) {
        validateArgumentNotNullNotEmpty(context);
        validateIsYangIdentifier(context, getIncludedSubModuleName());
    }
}
