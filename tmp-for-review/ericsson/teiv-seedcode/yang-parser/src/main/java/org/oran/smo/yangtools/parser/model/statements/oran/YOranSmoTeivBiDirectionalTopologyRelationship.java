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
package org.oran.smo.yangtools.parser.model.statements.oran;

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

public class YOranSmoTeivBiDirectionalTopologyRelationship extends ExtensionStatement {

    public YOranSmoTeivBiDirectionalTopologyRelationship(final AbstractStatement parentStatement,
            final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__BI_DIRECTIONAL_TOPOLOGY_RELATIONSHIP;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    @Override
    public MaxCardinality getMaxCardinalityUnderParent() {
        return MaxCardinality.MULTIPLE;
    }

    public String getRelationshipName() {
        return getValue() != null ? getValue() : "";
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_MODULE);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    @Override
    protected void validate(final ParserExecutionContext context) {
        /*
         * From o-ran-smo-teiv-common-yang-extensions:
         *
         * Defines a bi-directional relationship in the topology.
         *
         * A bi-directional-association (UDA) is a relationship comprising of an
         * A-side and a B-side. The A-side is considered the originating side of
         * the relationship; the B-side is considered the terminating side of the
         * relationship. The order of A-side and B-side is of importance and MUST
         * NOT be changed once defined.
         *
         * Both A-side and B-side are defined on a type, and are given a role. A
         * type may have multiple originating and/or terminating sides of a
         * relationship, all distinguished by role name.
         *
         * The statement MUST only be a substatement of the 'module' statement.
         * Multiple 'bi-directional-topology-relationship' statements are allowed
         * per parent statement.
         *
         * Substatements to the 'bi-directional-topology-relationship' define the
         * A-side and the B-side, respectively, and optionally properties of the
         * relationship. Data nodes of types 'leaf' and 'leaf-list' are used for
         * this purpose. One of the data nodes MUST be annotated with the 'aSide'
         * extension; another data node MUST be annotated with the 'bSide'
         * extension. Other data nodes define properties of the relationship.
         *
         * The argument is the name of the relationship. The relationship name is
         * scoped to the namespace of the declaring module and MUST be unique
         * within the scope.";
         */
        validateArgumentNotNullNotEmpty(context);

        checkParent(context);
    }

    @Override
    protected void subtreeProcessed(final ParserExecutionContext context) {
        /*
         * We are checking that we have an a-side and a b-side.
         */
        final long aSides = getChildStatements().stream().filter(stmt -> stmt.is(CY.STMT_LEAF) || stmt.is(
                CY.STMT_LEAF_LIST)).filter(stmt -> stmt.hasAtLeastOneChildOf(
                        CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__A_SIDE)).count();
        final long bSides = getChildStatements().stream().filter(stmt -> stmt.is(CY.STMT_LEAF) || stmt.is(
                CY.STMT_LEAF_LIST)).filter(stmt -> stmt.hasAtLeastOneChildOf(
                        CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__B_SIDE)).count();

        if (aSides != 1) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "A 'bi-directional relationship' must have as child exactly a single leaf or leaf-list annotated with 'aSide'."));
        }
        if (bSides != 1) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "A 'bi-directional relationship' must have as child exactly a single leaf or leaf-list annotated with 'bSide'."));
        }
    }
}
