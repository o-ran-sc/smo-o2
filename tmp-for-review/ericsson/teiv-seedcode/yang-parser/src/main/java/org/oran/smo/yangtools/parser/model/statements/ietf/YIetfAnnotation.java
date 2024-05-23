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
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * From module ietf-yang-metadata:
 *
 * This extension allows for defining metadata annotations in
 * YANG modules. The 'md:annotation' statement can appear only
 * at the top level of a YANG module or submodule, i.e., it
 * becomes a new alternative in the ABNF production rule for
 * 'body-stmts' (Section 14 in RFC 7950).
 *
 * The argument of the 'md:annotation' statement defines the name
 * of the annotation. Syntactically, it is a YANG identifier as
 * defined in Section 6.2 of RFC 7950.
 *
 * An annotation defined with this 'extension' statement inherits
 * the namespace and other context from the YANG module in which
 * it is defined.
 *
 * The data type of the annotation value is specified in the same
 * way as for a leaf data node using the 'type' statement.
 *
 * The semantics of the annotation and other documentation can be
 * specified using the following standard YANG substatements (all
 * are optional): 'description', 'if-feature', 'reference',
 * 'status', and 'units'.
 *
 * A server announces support for a particular annotation by
 * including the module in which the annotation is defined among
 * the advertised YANG modules, e.g., in a NETCONF <hello>
 * message or in the YANG library (RFC 7950). The annotation can
 * then be attached to any instance of a data node defined in any
 * YANG module that is advertised by the server.
 *
 * XML encoding and JSON encoding of annotations are defined in
 * RFC 7952.;
 *
 * @author Mark Hollmann
 */
public class YIetfAnnotation extends ExtensionStatement {

    public YIetfAnnotation(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CIETF.IETF_YANG_METADATA__ANNOTATION;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    public String getAnnotationName() {
        return getValue() != null ? getValue() : "";
    }

    @Override
    public MaxCardinality getMaxCardinalityUnderParent() {
        return MaxCardinality.MULTIPLE;
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_MODULE, CY.STMT_SUBMODULE);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    @Override
    protected void validate(final ParserExecutionContext context) {
        checkParent(context);

        validateArgumentNotNullNotEmpty(context);
        validateIsYangIdentifier(context, getValue());
    }

    @Override
    protected void subtreeProcessed(final ParserExecutionContext context) {
        /*
         * There must be exactly a single type statement under the annotation.
         */
        if (getChildren(CY.STMT_TYPE).size() != 1) {
            context.addFinding(new Finding(this, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString(),
                    "A 'type' statement is required for the annotation."));
        }
    }
}
