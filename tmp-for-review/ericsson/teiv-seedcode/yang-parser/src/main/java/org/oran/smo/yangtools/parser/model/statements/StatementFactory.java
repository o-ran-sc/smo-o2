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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.statements.yang.YangCoreClassSupplier;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.util.StackTraceHelper;

/**
 * Factory for creating instances of type-safe classes representing Yang statements.
 *
 * @author Mark Hollmann
 */
public abstract class StatementFactory {

    private static final Map<String, Constructor<? extends AbstractStatement>> CoreTypesConstructors = new HashMap<>();

    private static final YangCoreClassSupplier yangCoreClassSupplier = new YangCoreClassSupplier();

    /**
     * Creates an instance of a core Yang statement.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractStatement> T createYangCoreStatement(final ParserExecutionContext context,
            final YangDomElement domElement, final AbstractStatement parentStatement) {

        final String yangStatementName = domElement.getName();
        try {
            if (!CoreTypesConstructors.containsKey(yangStatementName)) {
                final Class<? extends AbstractStatement> clazz = yangCoreClassSupplier.getJavaClazzForStatement(null,
                        yangStatementName);
                final Constructor<? extends AbstractStatement> constructor = clazz.getConstructor(AbstractStatement.class,
                        YangDomElement.class);
                CoreTypesConstructors.put(yangStatementName, constructor);
            }

            final Constructor<? extends AbstractStatement> constructor = CoreTypesConstructors.get(yangStatementName);
            final AbstractStatement newInstance = constructor.newInstance(parentStatement, domElement);

            return (T) newInstance;

        } catch (final Exception ex) {
            context.addFinding(new Finding(domElement, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                    "Not a valid YANG statement: " + yangStatementName));
        }

        return null;
    }

    /**
     * Creates an instance of an extension statement.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ExtensionStatement> T createYangExtensionStatement(final ParserExecutionContext context,
            final YangDomElement domNode, final AbstractStatement parentStatement) {

        final String[] prefixAndStatement = domNode.getName().split(":");
        final String prefix = prefixAndStatement[0];
        final String extStatementName = prefixAndStatement[1];

        final ModuleIdentity moduleIdentity = parentStatement.getPrefixResolver().getModuleForPrefix(prefix);
        if (moduleIdentity == null) {
            context.addFinding(new Finding(domNode, ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString(),
                    "Prefix '" + prefixAndStatement[0] + "' for the extension statement not resolvable."));
            return null;
        }

        final String extModuleName = moduleIdentity.getModuleName();

        try {
            final Class<? extends ExtensionStatement> clazz = getClassForYangExtension(context, extModuleName,
                    extStatementName);
            final Constructor<? extends ExtensionStatement> constructor = clazz.getConstructor(AbstractStatement.class,
                    YangDomElement.class);

            final ExtensionStatement newInstance = constructor.newInstance(parentStatement, domNode);

            return (T) newInstance;
        } catch (final NoSuchMethodException nsmex) {
            context.addFinding(new Finding(domNode, ParserFindingType.P002_INVALID_EXTENSION_STATEMENT_CLASS.toString(),
                    "Extension statement class constructor wrong."));
        } catch (final Exception ex) {
            context.addFinding(new Finding(domNode, ParserFindingType.P000_UNSPECIFIED_ERROR.toString(),
                    "During instantiation of extension statement: " + ex.getMessage() + " - trace: " + StackTraceHelper
                            .getStackTraceInfo(ex)));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractStatement> T cloneYangStatement(final T statementToClone,
            final AbstractStatement parentOfClone) {

        final YangDomElement domElementOfStatementToClone = statementToClone.getDomElement();
        final Class<? extends AbstractStatement> clazz = statementToClone.getClass();

        try {
            final Constructor<? extends AbstractStatement> constructor = clazz.getConstructor(AbstractStatement.class,
                    YangDomElement.class);
            final AbstractStatement newInstance = constructor.newInstance(parentOfClone, domElementOfStatementToClone);

            return (T) newInstance;

        } catch (final Exception ex) {
            // no-op, cannot happen
            return null;
        }
    }

    /**
     * Given the module name where an extension is defined, and the extension name, returns either a dedicated
     * class that can handle the statement, or the catch-all {@link ExtensionStatement} class.
     */
    @SuppressWarnings("unchecked")
    private static <T extends ExtensionStatement> Class<T> getClassForYangExtension(final ParserExecutionContext context,
            final String moduleName, final String statementName) {

        for (final StatementClassSupplier supplier : context.getExtensionCreators()) {
            final Class<ExtensionStatement> extensionClazz = supplier.getJavaClazzForStatement(moduleName, statementName);
            if (extensionClazz != null) {
                return (Class<T>) extensionClazz;
            }
        }

        /*
         * No supplier found that can handle the extensions, we return extension catch-all base class.
         */
        return (Class<T>) ExtensionStatement.class;
    }
}
