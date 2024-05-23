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
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YRefine extends AbstractStatement {

    public YRefine(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.TARGET_NODE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_REFINE;
    }

    public String getRefineTargetNode() {
        return domElement.getTrimmedValueOrEmpty();
    }

    public YConfig getConfig() {
        return getChild(CY.STMT_CONFIG);
    }

    public List<YDefault> getDefaults() {
        return getChildren(CY.STMT_DEFAULT);
    }

    public List<YIfFeature> getIfFeatures() {
        return getChildren(CY.STMT_IF_FEATURE);
    }

    public YMandatory getMandatory() {
        return getChild(CY.STMT_MANDATORY);
    }

    public YMinElements getMinElements() {
        return getChild(CY.STMT_MIN_ELEMENTS);
    }

    public YMaxElements getMaxElements() {
        return getChild(CY.STMT_MAX_ELEMENTS);
    }

    public List<YMust> getMusts() {
        return getChildren(CY.STMT_MUST);
    }

    public YPresence getPresence() {
        return getChild(CY.STMT_PRESENCE);
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        if (getRefineTargetNode().startsWith("/")) {
            context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                    "The identifier for the refine's target node is not relative (it must not start with '/')."));
        }
    }
}
