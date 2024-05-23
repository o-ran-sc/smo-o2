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

import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YType extends AbstractStatement {

    public YType(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_TYPE;
    }

    @Override
    public boolean orderUnderParentMatters() {
        return true;
    }

    public String getDataType() {
        return domElement.getTrimmedValueOrEmpty();
    }

    public List<YBase> getBases() {
        return getChildren(CY.STMT_BASE);
    }

    public List<YBit> getBits() {
        return getChildren(CY.STMT_BIT);
    }

    public List<YEnum> getEnums() {
        return getChildren(CY.STMT_ENUM);
    }

    public YFractionDigits getFractionDigits() {
        return getChild(CY.STMT_FRACTION_DIGITS);
    }

    public YLength getLength() {
        return getChild(CY.STMT_LENGTH);
    }

    public YPath getPath() {
        return getChild(CY.STMT_PATH);
    }

    public List<YPattern> getPatterns() {
        return getChildren(CY.STMT_PATTERN);
    }

    public YRange getRange() {
        return getChild(CY.STMT_RANGE);
    }

    public YRequireInstance getRequireInstance() {
        return getChild(CY.STMT_REQUIRE_INSTANCE);
    }

    public List<YType> getTypes() {
        return getChildren(CY.STMT_TYPE);
    }
}
