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

import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatementClassSupplier;

/**
 * Class supplier for extension statements defined by ORAN
 *
 * @author Mark Hollmann
 */
public class OranExtensionsClassSupplier extends AbstractStatementClassSupplier {

    @Override
    public Map<String, List<String>> getHandledStatements() {
        return CORAN.HANDLED_STATEMENTS;
    }

    @Override
    public <T extends AbstractStatement> Class<T> getJavaClazzForStatement(final String moduleName,
            final String extStatementName) {

        if (CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME.equals(moduleName)) {
            return handleSmoTeivExtensions(extStatementName);
        }

        return null;
    }

    private static final String ORAN_EXTENSIONS_JAVA_PACKAGE = OranExtensionsClassSupplier.class.getPackage().getName();
    private static final String ORAN_SMO_TEIV_EXTENSIONS_JAVA_CLASS_PREFIX = "YOranSmoTeiv";

    private <T extends AbstractStatement> Class<T> handleSmoTeivExtensions(final String extStatementName) {
        return getJavaClazzForStatement(ORAN_EXTENSIONS_JAVA_PACKAGE, ORAN_SMO_TEIV_EXTENSIONS_JAVA_CLASS_PREFIX,
                extStatementName);
    }
}
