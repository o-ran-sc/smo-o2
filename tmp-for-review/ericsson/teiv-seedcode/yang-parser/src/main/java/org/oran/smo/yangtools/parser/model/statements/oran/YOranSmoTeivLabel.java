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
import java.util.regex.Pattern;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

public class YOranSmoTeivLabel extends ExtensionStatement {

    private int version;
    private int release;
    private int correction;

    public YOranSmoTeivLabel(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.VALUE;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__LABEL;
    }

    @Override
    public boolean argumentIsMandatory() {
        return true;
    }

    public String getLabel() {
        return getValue() != null ? getValue() : "";
    }

    public int getVersion() {
        return version;
    }

    public int getRelease() {
        return release;
    }

    public int getCorrection() {
        return correction;
    }

    private static final List<StatementModuleAndName> REQUIRED_PARENTS = Arrays.asList(CY.STMT_REVISION);

    @Override
    public boolean canBeChildOf(final StatementModuleAndName parentSman) {
        return REQUIRED_PARENTS.contains(parentSman);
    }

    private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");

    @Override
    protected void validate(final ParserExecutionContext context) {
        /*
         * From o-ran-smo-teiv-common-yang-extensions:
         *
         * The label can be used to give modules and submodules a semantic version, in addition to their revision.
         *
         * The format of the label is 'x.y.z' – expressed as pattern, it is [0-9]+\.[0-9]+\.[0-9]+
         *
         * The statement MUST only be a substatement of the revision statement.  Zero or one revision label statements
         * per parent statement are allowed.
         *
         * Revision labels MUST be unique amongst all revisions of a module or submodule.
         */
        validateArgumentNotNullNotEmpty(context);

        final String label = getLabel();
        if (!VALID_PATTERN.matcher(label).matches()) {
            context.addFinding(new Finding(this, ParserFindingType.P025_INVALID_EXTENSION,
                    "Label value must be in 'version.release.correction' format."));
        } else {
            version = Integer.parseInt(label.substring(0, label.indexOf('.')));
            release = Integer.parseInt(label.substring(label.indexOf('.') + 1, label.lastIndexOf('.')));
            correction = Integer.parseInt(label.substring(label.lastIndexOf('.') + 1));
        }

        checkParent(context);
        checkCardinalityUnderParent(context, 1);
    }
}
