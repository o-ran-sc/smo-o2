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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YDeviate extends AbstractStatement {

    public YDeviate(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    public String getDeviateOperation() {
        return domElement.getTrimmedValueOrEmpty();
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_DEVIATE;
    }

    /**
     * Returns the deviate type or null if the deviate type is unknown / wrong.
     */
    public DeviateType getDeviateType() {

        switch (getDeviateOperation()) {
            case "not-supported":
                return DeviateType.NOT_SUPPORTED;
            case "add":
                return DeviateType.ADD;
            case "replace":
                return DeviateType.REPLACE;
            case "delete":
                return DeviateType.DELETE;
            default:
        }

        return null;
    }

    public YConfig getConfig() {
        return getChild(CY.STMT_CONFIG);
    }

    public List<YDefault> getDefaults() {
        return getChildren(CY.STMT_DEFAULT);
    }

    public YMandatory getMandatory() {
        return getChild(CY.STMT_MANDATORY);
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

    public YType getType() {
        return getChild(CY.STMT_TYPE);
    }

    public List<YUnique> getUniques() {
        return getChildren(CY.STMT_UNIQUE);
    }

    public YUnits getUnits() {
        return getChild(CY.STMT_UNITS);
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        final DeviateType deviateType = getDeviateType();
        if (deviateType == null) {
            context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT, "'" + domElement
                    .getValue() + "' is not valid as argument for deviate. Use one of: not-supported, add, replace, delete."));
        }
    }

    private static final Set<String> NOT_ALLOWED_FOR_ADD_OR_DELETE = new HashSet<>(Arrays.asList(CY.CONFIG, CY.MANDATORY,
            CY.TYPE, CY.MAX_ELEMENTS, CY.MIN_ELEMENTS));

    @Override
    protected void subtreeProcessed(final ParserExecutionContext context) {

        final DeviateType deviateType = getDeviateType();
        if (deviateType == null) {
            return;
        }

        final List<AbstractStatement> nonExtensionChildStatements = getNonExtensionChildStatements();

        switch (deviateType) {
            case NOT_SUPPORTED:
                if (!nonExtensionChildStatements.isEmpty()) {
                    for (final AbstractStatement child : nonExtensionChildStatements) {
                        context.addFinding(new Finding(child, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT,
                                "Statement '" + child
                                        .getStatementName() + "' cannot occur under 'deviate not-supported'."));
                    }
                }
                break;
            case ADD:
            case REPLACE:
            case DELETE:
                if (getChildStatements().isEmpty()) {
                    context.addFinding(new Finding(this, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT,
                            "Statements are required under " + this.getDomElement().getNameValue() + "."));
                }
                break;
        }

        /*
         * After some discussion on the IETF netmod mailing list, a decision was made that some
         * properties always exist and hence can never be ADDed or DELETEd:
         *
         * "I think that config, mandatory, type, max-elements, min-elements cannot be added or
         *  deleted, only replaced, because they always exist."
         *
         * "default, must, unique, units are all optional, and hence may be added, replaced,
         *  or deleted"
         */
        if (deviateType == DeviateType.ADD || deviateType == DeviateType.DELETE) {
            for (final AbstractStatement nonExtensionChild : nonExtensionChildStatements) {
                if (NOT_ALLOWED_FOR_ADD_OR_DELETE.contains(nonExtensionChild.getStatementName())) {
                    context.addFinding(new Finding(nonExtensionChild,
                            ParserFindingType.P167_CANNOT_USE_UNDER_DEVIATE_ADD_OR_DELETE,
                            "This statement cannot be used under 'deviate add/delete' as it refers to a property that always exists. Use a 'deviate replace' instead."));
                }
            }
        }
    }

    public enum DeviateType {
        NOT_SUPPORTED,
        ADD,
        REPLACE,
        DELETE
    }
}
