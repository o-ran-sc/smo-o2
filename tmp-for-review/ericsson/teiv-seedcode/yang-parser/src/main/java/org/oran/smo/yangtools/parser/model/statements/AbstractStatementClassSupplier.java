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

/**
 * Convenience base class containing a utility method for constructing java class names for
 * type-safe statement classes.
 *
 * @author Mark Hollmann
 */
public abstract class AbstractStatementClassSupplier implements StatementClassSupplier {

    /**
     * Given a Java package name, a class name prefix and the name of a statement, returns the Java class
     * representing the statement. The YANG statement name (possibly the name of an extension statement)
     * will be translated to CamelCase.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AbstractStatement> Class<T> getJavaClazzForStatement(final String javaPackage,
            final String clazzPrefix, final String statementName) {

        final StringBuilder sb = new StringBuilder(100);
        sb.append(javaPackage);
        sb.append(".");
        sb.append(clazzPrefix);
        appendStatementNameInCamelCase(sb, statementName);

        try {
            final Class<?> forName = Class.forName(sb.toString());
            return (Class<T>) forName;
        } catch (final Exception ex) {
            /* no-op */ }

        return null;
    }

    /**
     * Takes a statement name (which, by YANG convention, is all lowercase and contains hyphens) and converts it to
     * CamelCase:
     *
     * "choice" -> "Choice"
     * "belongs-to" -> "BelongsTo"
     */
    private static void appendStatementNameInCamelCase(final StringBuilder sb, final String statementName) {

        boolean upperCaseNextChar = true;
        for (int i = 0; i < statementName.length(); ++i) {
            final char c = statementName.charAt(i);
            if (c == '-') {
                upperCaseNextChar = true;
            } else if (upperCaseNextChar) {
                sb.append(Character.toUpperCase(c));
                upperCaseNextChar = false;
            } else {
                sb.append(c);
            }
        }
    }
}
