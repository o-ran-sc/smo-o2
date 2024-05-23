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
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.util.GrammarHelper;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YEnum extends AbstractStatement {

    public YEnum(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public String getStatementIdentifier() {
        return getEnumName();
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_ENUM;
    }

    public String getEnumName() {
        return domElement.getValue();
    }

    public List<YIfFeature> getIfFeatures() {
        return getChildren(CY.STMT_IF_FEATURE);
    }

    public YStatus getStatus() {
        return getChild(CY.STMT_STATUS);
    }

    public YValue getValue() {
        return getChild(CY.STMT_VALUE);
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        /*
         * Note that the RFC states that an enum can be "any string". Not sure how much this makes
         * sense, as this would allow spaces...we check for that as this would cause problems
         * somewhere, no doubt.
         */
        if (GrammarHelper.containsYangWhitespace(getEnumName())) {
            context.addFinding(new Finding(this, ParserFindingType.P141_WHITESPACE_IN_ENUM,
                    "Usage of whitespace character(s) in enum '" + getEnumName() + "'."));
        } else if (containsWeirdCharacters(getEnumName())) {
            context.addFinding(new Finding(this, ParserFindingType.P142_UNUSUAL_CHARACTERS_IN_ENUM,
                    "enum '" + getEnumName() + "' contains unusual characters."));
        }
    }

    private static boolean containsWeirdCharacters(final String enumName) {

        if (enumName == null) {
            return false;
        }

        for (final char c : enumName.toCharArray()) {
            if (c == '_' || c == '.' || c == '-' || (c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
                // all ok
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void subtreeProcessed(final ParserExecutionContext context) {
        if (getValue() == null) {
            context.addFinding(new Finding(this, ParserFindingType.P143_ENUM_WITHOUT_VALUE,
                    "enum '" + getEnumName() + "' does not have a value (bad practice)."));
        }
    }
}
