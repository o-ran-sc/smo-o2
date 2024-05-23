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
package org.oran.smo.yangtools.parser.model.statements;

import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YExtension;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Represents an instance of an extension statement; that is, <i>usage</i> of an extension (as
 * opposed to <i>definition</i> of an extension - extensions are defined by means of the
 * {@link YExtension} class).
 * <p/>
 * This is a generic catch-all class for extension instances. Implementations of {@link StatementClassSupplier}
 * are typically used to create instances of type-safe classes representing extensions.
 *
 * @author Mark Hollmann
 */
public class ExtensionStatement extends SimpleStatement {

    public ExtensionStatement(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NO_ARG;
    }

    /**
     * Returns the prefix of the extension.
     */
    public String getExtensionModulePrefix() {
        return domElement.getName().split(":")[0];
    }

    /**
     * Returns the name of the extension
     */
    public String getExtensionStatementName() {
        return domElement.getName().split(":")[1];
    }

    /**
     * Returns the value (argument) of the extension, if any (ie. can be null).
     */
    public String getValue() {
        return domElement.getValue();
    }

    /**
     * Denotes whether a possible argument to the extension is mandatory to be supplied.
     * By default this method this method will return FALSE as it is not possible to
     * predict the semantics of each extensions. Sub-classes that require their argument
     * to be present should override this method accordingly.
     */
    public boolean argumentIsMandatory() {
        return false;
    }

    /**
     * Returns the number of instances this extension can possible have under its parent
     * statement. By default, this method will return ONE as this is typically the case
     * with extensions. Sub-types may override.
     */
    public MaxCardinality getMaxCardinalityUnderParent() {
        return MaxCardinality.ONE;
    }

    public enum MaxCardinality {
        ONE,
        MULTIPLE
    }

    /**
     * Returns whether the extension can be a valid child of the supplied parent statement
     * (which is usually a core Yang statement, but could be another extension). Type-safe
     * extension subclasses should override this method.
     * <p/>
     * Since we don't know any better here, the default assumption is that the extension can
     * sit anywhere.
     */
    public boolean canBeChildOf(final StatementModuleAndName parentStatement) {
        return true;
    }

    @Override
    protected void validate(ParserExecutionContext context) {
        /*
         * Validation will always be extension-specific. The assumption
         * that these all need arguments is wrong, of course.
         */
    }

    protected void checkParent(final ParserExecutionContext context) {
        final AbstractStatement parent = getParentStatement();

        if (!canBeChildOf(parent.getStatementModuleAndName())) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "Extension statement '" + getExtensionStatementName() + "' is not allowed under '" + parent
                            .getStatementName()));
        }
    }

    protected void checkParentAlsoAllowDeviateOrRefine(final ParserExecutionContext context) {
        final AbstractStatement parent = getParentStatement();

        if (parent.is(CY.STMT_DEVIATE) || parent.is(CY.STMT_REFINE)) {
            return;
        }
        if (!canBeChildOf(parent.getStatementModuleAndName())) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "Extension statement '" + getExtensionStatementName() + "' is not allowed under '" + parent
                            .getStatementName()));
        }
    }

    protected void checkCardinalityUnderParent(final ParserExecutionContext context, final int max) {
        final AbstractStatement parentStatement = getParentStatement();
        final List<? extends AbstractStatement> childrenOfThisType = parentStatement.getChildren(this
                .getStatementModuleAndName());
        if (childrenOfThisType.size() > max) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "The allowed maximum cardinality for this extension statement is " + max + "."));
        }
    }
}
