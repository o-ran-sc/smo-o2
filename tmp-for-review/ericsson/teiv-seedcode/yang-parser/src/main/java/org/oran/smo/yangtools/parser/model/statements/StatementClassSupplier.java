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
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.yang.YangCoreClassSupplier;

/**
 * Implementations of this interface can supply type-safe classes for statements. Implementation
 * classes are typically invoked when the YANG statement tree is being built from the YANG DOM tree.
 * For YANG core statements as defined in RFC 7950, {@link YangCoreClassSupplier} will be used. Other
 * implementations typically handle extensions.
 *
 * @author Mark Hollmann
 */
public interface StatementClassSupplier {

    /**
     * Returns information about the statements that this implementation can handle.
     * <p>
     * The map keys are the names of modules. The map value is a list of statement names
     * that are defined within that module, and which the implementation can handle.
     */
    Map<String, List<String>> getHandledStatements();

    /**
     * Returns the Java class that represents the YANG statement.
     * <p/>
     * Implementations will return either a specific class that can handle the statement or null.
     * <p/>
     * If the statement represents an extension, either {@link ExtensionStatement} or a subclass of
     * {@link ExtensionStatement} must be returned. Failure to do so will lead to parse errors.
     * <p/>
     * The supplied method <b>must</b> have a constructor as follows, and invoke super:
     * <p/>
     * <pre>
     * public class MyStatement extends ExtensionStatement {
     *
     * public MyStatement(final AbstractStatement parentStatement, final YangDomElement domElement) {
     * super(parentStatement, domElement);
     * ...
     * }
     * }
     * </pre>
     */
    <T extends AbstractStatement> Class<T> getJavaClazzForStatement(String moduleName, String statementName);
}
