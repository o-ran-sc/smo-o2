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

import java.util.Objects;

import org.oran.smo.yangtools.parser.model.statements.yang.CY;

/**
 * Holds information about the name of a statement, and the module where the statement has
 * been defined.
 *
 * @author Mark Hollmann
 */
public class StatementModuleAndName {

    private final String moduleName;
    private final String statementName;
    private final boolean isYangCoreStatement;

    /**
     * Creates a new SMAN. The module name is the name of the module defining the statement.
     */
    public StatementModuleAndName(final String moduleName, final String statementName) {
        this.moduleName = Objects.requireNonNull(moduleName);
        this.statementName = Objects.requireNonNull(statementName);
        this.isYangCoreStatement = CY.YANG_CORE_MODULE_NAME.equals(moduleName);
    }

    /**
     * Returns the module in which the statement has been defined. If the statement is part of
     * the core YANG language, the constant {@link CY.YANG_CORE_MODULE_NAME} will be returned.
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns whether this statement is part of the YANG core language.
     */
    public boolean isYangCoreStatement() {
        return isYangCoreStatement;
    }

    /**
     * Returns whether this statement is an extension statement.
     */
    public boolean isExtensionStatement() {
        return !isYangCoreStatement;
    }

    /**
     * Returns the statement name. Note that different modules may define
     * extensions having the same (statement-) name.
     */
    public String getStatementName() {
        return statementName;
    }

    @Override
    public int hashCode() {
        return statementName.hashCode();
    }

    @Override
    public String toString() {
        return isYangCoreStatement() ? statementName : moduleName + ":" + statementName;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof StatementModuleAndName)) {
            return false;
        }

        final StatementModuleAndName other = (StatementModuleAndName) obj;

        return this.statementName.equals(other.statementName) && this.moduleName.equals(other.moduleName);
    }
}
