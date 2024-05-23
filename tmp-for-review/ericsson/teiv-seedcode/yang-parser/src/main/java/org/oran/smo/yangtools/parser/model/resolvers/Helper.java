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
package org.oran.smo.yangtools.parser.model.resolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.YangModelRoot;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Utility class for navigation
 *
 * @author Mark Hollmann
 */
public abstract class Helper {

    /**
     * Finds a schema node in the schema tree. Returns null if not found.
     *
     * The schema node identifier supplied may be absolute or relative. If it is absolute, navigation will
     * start at the root of the module that owns the original statement, or the module as indicated by the
     * prefix. If it is relative, navigation will start at the original statement.
     *
     * @param originalStatement
     *     The statement containing the schema node identifier (e.g. an augment statement)
     * @param schemaNodeIdentifier
     *     An absolute or relative schema node identifier.
     */
    public static AbstractStatement findSchemaNode(final ParserExecutionContext context,
            final AbstractStatement originalStatement, final String schemaNodeIdentifier, final Schema schema) {

        /*
         * For navigation it will be important in a moment if the schema node identifier is
         * absolute (at root) or relative (starts somewhere in the tree).
         */
        final boolean isAbsoluteSchemaNodeIdentifier = schemaNodeIdentifier.startsWith("/");

        /*
         * Quickly split them...
         */
        final String[] identifierParts = getIdentifierParts(isAbsoluteSchemaNodeIdentifier, schemaNodeIdentifier);

        /*
         * This is the starting point of our navigation. Might be at root of the same module
         * as the original statement, or root of a different module, or the original statement
         * itself.
         */
        AbstractStatement traversalStatement = findStartingSchemaNode(context, originalStatement,
                isAbsoluteSchemaNodeIdentifier, identifierParts[0], schema);
        if (traversalStatement == null) {
            return null;
        }

        /*
         * And now we simply follow the identifiers down the tree.
         */
        for (final String identifier : identifierParts) {

            final AbstractStatement foundChildStatement = findChildSchemaNode(context, traversalStatement, identifier,
                    originalStatement);
            if (foundChildStatement == null) {
                /*
                 * It is well possible that a schema node is not found because it hasn't been augmented-in
                 * yet, or already deviated-out. Issuing a finding here would therefore not be such a good
                 * idea, as it would be confusing (and potentially wrong).
                 */
                return null;
            }

            traversalStatement = foundChildStatement;
        }

        return traversalStatement;
    }

    /**
     * Splits the schema node identifier into individual parts.
     */
    public static String[] getIdentifierParts(final boolean isAbsoluteSchemaNodeIdentifier,
            final String schemaNodeIdentifier) {

        final String relativeSchemaNodeIdentifier = isAbsoluteSchemaNodeIdentifier ?
                schemaNodeIdentifier.substring(1) :
                schemaNodeIdentifier;

        /*
         * The relative schema node identifier is split up into parts. For example, path
         * "if:interfaces/if:interface/ethxipos:ethernet/l2vlanxipos:dot1q/l2vlanxipos:pvc"
         * is split up into:
         *
         * if:interfaces
         * if:interface
         * ethxipos:ethernet
         * l2vlanxipos:dot1q
         * l2vlanxipos:pvc
         *
         * We can split on the '/' character as this is not a valid character for a schema node
         * identifier and a schema node identifier cannot use predicates.
         */
        final String[] identifierParts = relativeSchemaNodeIdentifier.contains("/") ?
                relativeSchemaNodeIdentifier.split("/") :
                new String[] { relativeSchemaNodeIdentifier };
        return identifierParts;
    }

    /**
     * Given the first part of a schema node identifier (which can be absolute or relative) returns the schema
     * node that should serve as starting point of subsequent navigation.
     */
    public static AbstractStatement findStartingSchemaNode(final ParserExecutionContext context,
            final AbstractStatement originalStatement, final boolean isAbsoluteSchemaNodeIdentifier,
            final String firstIdentifierPart, final Schema schema) {

        if (!isAbsoluteSchemaNodeIdentifier) {
            /*
             * Relative path, that's easy: start at the original statement.
             */
            return originalStatement;
        }

        final YangModel currentYangFile = originalStatement.getDomElement().getYangModel();

        /*
         * The path is absolute, so the starting point for traversal will be at the root
         * of a module / submodule. Need to figure out which one...
         */
        if (!QNameHelper.hasPrefix(firstIdentifierPart)) {
            /*
             * No prefix, hence the module is the same module in which the original statement sits.
             */
            return originalStatement.getYangModelRoot().getModuleOrSubmodule();
        }

        /*
         * We have a prefix, so resolve that.
         */
        final String prefix = QNameHelper.extractPrefix(firstIdentifierPart);
        final ModuleIdentity moduleIdentity = currentYangFile.getPrefixResolver().getModuleForPrefix(prefix);

        if (moduleIdentity == null) {
            context.addFinding(new Finding(originalStatement, ParserFindingType.P033_UNRESOLVEABLE_PREFIX,
                    "Prefix '" + prefix + "' part of path '" + firstIdentifierPart + "' cannot be resolved."));
            return null;
        }

        final YangModel modelInputForModuleIdentity = schema.getModuleRegistry().find(moduleIdentity);
        if (modelInputForModuleIdentity == null) {	// avoid NPE if module not in input
            return null;
        }

        return modelInputForModuleIdentity.getYangModelRoot().getModuleOrSubmodule();
    }

    /**
     * Given a statement containing some kind of child schema identifier (posibly prefixed), finds that child with that
     * identity under the statement.
     */
    public static AbstractStatement findChildSchemaNode(final ParserExecutionContext context,
            final AbstractStatement parentStatement, final String possiblyPrefixedIdentifier,
            final AbstractStatement originalStatement) {

        final YangModel currentYangFile = originalStatement.getDomElement().getYangModel();

        String soughtNamespace = null;
        final String soughtIdentifier = QNameHelper.extractName(possiblyPrefixedIdentifier);

        if (QNameHelper.hasPrefix(possiblyPrefixedIdentifier)) {

            final String prefix = QNameHelper.extractPrefix(possiblyPrefixedIdentifier);

            final ModuleIdentity moduleIdentity = currentYangFile.getPrefixResolver().getModuleForPrefix(prefix);
            if (moduleIdentity == null) {
                context.addFinding(new Finding(originalStatement, ParserFindingType.P033_UNRESOLVEABLE_PREFIX,
                        "Prefix '" + prefix + "' part of path '" + possiblyPrefixedIdentifier + "' cannot be resolved."));
                return null;
            }

            soughtNamespace = currentYangFile.getPrefixResolver().resolveNamespaceUri(prefix);
        }

        for (final AbstractStatement child : parentStatement.getChildStatements()) {
            if (child.definesSchemaNode() && soughtIdentifier.equals(child.getStatementIdentifier())) {
                if (soughtNamespace == null || soughtNamespace.equals(child.getEffectiveNamespace())) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Extracts all occurrences of a specific YANG statement from a schema.
     */
    public static List<?> findStatementsInSchema(final StatementModuleAndName soughtStatementModuleAndName,
            final Schema schema) {

        final List<AbstractStatement> result = new ArrayList<>();

        for (final YangModel yaml : schema.getModuleRegistry().getAllYangModels()) {
            final AbstractStatement moduleOrSubmodule = yaml.getYangModelRoot().getModuleOrSubmodule();
            findStatementsInSubtree(moduleOrSubmodule, soughtStatementModuleAndName, result);
        }

        return result;
    }

    /**
     * Extracts all occurrences of a specific YANG statement underneath the supplied statement, and possibly including the
     * statement itself.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractStatement> void findStatementsInSubtree(final AbstractStatement statement,
            final StatementModuleAndName soughtStatementModuleAndName, final List<T> result) {
        if (statement.getStatementModuleAndName().equals(soughtStatementModuleAndName)) {
            result.add((T) statement);
        }
        for (final AbstractStatement child : statement.getChildStatements()) {
            findStatementsInSubtree(child, soughtStatementModuleAndName, result);
        }
    }

    /**
     * Returns all occurrences of the supplied statement module/name that sit at the root of all modules in the schema.
     */
    public static List<?> findStatementsAtModuleRootInSchema(final StatementModuleAndName soughtStatementModuleAndName,
            final Schema schema) {

        final List<AbstractStatement> result = new ArrayList<>();

        for (final YangModel yaml : schema.getModuleRegistry().getAllYangModels()) {
            final List<AbstractStatement> childStatements = yaml.getYangModelRoot().getModuleOrSubmodule()
                    .getChildStatements();
            for (final AbstractStatement child : childStatements) {
                if (child.getStatementModuleAndName().equals(soughtStatementModuleAndName)) {
                    result.add((AbstractStatement) child);
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractStatement> T findStatement(final ParserExecutionContext context, final Schema schema,
            final AbstractStatement originalStatement, final StatementModuleAndName soughtSman,
            final String possiblyPrefixedSoughtNodeIdentifier) {

        String prefix = "";
        ModuleIdentity moduleIdentityOfSoughtStatement;
        String unprefixedSoughtNodeIdentifier;

        if (QNameHelper.hasPrefix(possiblyPrefixedSoughtNodeIdentifier)) {
            prefix = QNameHelper.extractPrefix(possiblyPrefixedSoughtNodeIdentifier);
            moduleIdentityOfSoughtStatement = originalStatement.getPrefixResolver().getModuleForPrefix(prefix);
            unprefixedSoughtNodeIdentifier = QNameHelper.extractName(possiblyPrefixedSoughtNodeIdentifier);

            if (moduleIdentityOfSoughtStatement == null) {
                context.addFinding(new Finding(originalStatement, ParserFindingType.P033_UNRESOLVEABLE_PREFIX,
                        "Prefix '" + prefix + "' not resolvable to a (sub-)module name."));
                return null;
            }

        } else {
            /*
             * Statement has no prefix, hence statement must sit in the same module as the original statement. Note that
             * "original" here means the original place where the statement has occurred - which may be different from
             * where the statement is here now, due to uses / grouping (which copies the contents of the group into
             * potentially a different module.)
             */
            moduleIdentityOfSoughtStatement = originalStatement.getDomElement().getYangModel().getModuleIdentity();
            unprefixedSoughtNodeIdentifier = possiblyPrefixedSoughtNodeIdentifier;
        }

        /*
         * What we do now depends on the module in which the sought statement is located. If it is in the same module,
         * it can sit anywhere in the model upwards from where the original statement is (could even possibly
         * be the previous statement in the document). So we need to work our way up the tree to try to find it.
         * If it is in a different module, then it must be at the root of the module.
         */
        if (moduleIdentityOfSoughtStatement.equals(originalStatement.getYangModelRoot().getYangModel()
                .getModuleIdentity())) {
            /*
             * In same module, or an included submodule. Work our way up the tree until we find it (or don't, then check included modules).
             */
            AbstractStatement parentStatement = originalStatement;

            while (true) {
                parentStatement = parentStatement.getParentStatement();
                if (parentStatement == null) {
                    return (T) findStatementAtModuleRootOrIncludedSubmodule(originalStatement.getYangModelRoot(),
                            soughtSman, unprefixedSoughtNodeIdentifier);
                }
                for (final AbstractStatement child : parentStatement.getChildStatements()) {
                    if (child.is(soughtSman) && unprefixedSoughtNodeIdentifier.equals(child.getStatementIdentifier())) {
                        return (T) child;
                    }
                }
            }
        } else {
            /*
             * In different module. Must be at root of that other module, or included submodules.
             */
            final YangModel yangInputContainingSoughtStatement = schema.getModuleRegistry().find(
                    moduleIdentityOfSoughtStatement);
            if (yangInputContainingSoughtStatement == null) {	// avoid NPE if module not in input
                context.addFinding(new Finding(originalStatement, ParserFindingType.P033_UNRESOLVEABLE_PREFIX,
                        "Prefix '" + prefix + "' resolves to '" + moduleIdentityOfSoughtStatement + "' but this is either not found in the input or is ambiguous."));
                return null;
            }
            return (T) findStatementAtModuleRootOrIncludedSubmodule(yangInputContainingSoughtStatement.getYangModelRoot(),
                    soughtSman, unprefixedSoughtNodeIdentifier);
        }
    }

    /**
     * Tries to find the statement at the root of the module, or at the root of any included submodule.
     */
    private static AbstractStatement findStatementAtModuleRootOrIncludedSubmodule(final YangModelRoot modelRoot,
            final StatementModuleAndName soughtSman, final String unprefixedSoughtNodeIdentifier) {

        if (modelRoot.isModule()) {
            return findStatementAtModuleRoot(modelRoot, soughtSman, unprefixedSoughtNodeIdentifier);
        } else {
            /*
             * It's a submodule, navigate to owning module first.
             */
            return findStatementAtModuleRoot(modelRoot.getOwningYangModelRoot(), soughtSman,
                    unprefixedSoughtNodeIdentifier);
        }
    }

    private static AbstractStatement findStatementAtModuleRoot(final YangModelRoot moduleModelRoot,
            final StatementModuleAndName soughtSman, final String unprefixedSoughtNodeIdentifier) {

        /*
         * Try to find statement at the root of the module.
         */
        AbstractStatement foundStatement = findStatementUnderParent(moduleModelRoot.getModule(), soughtSman,
                unprefixedSoughtNodeIdentifier);
        if (foundStatement != null) {
            return foundStatement;
        }

        /*
         * Not found here, check submodules of the module.
         */
        for (final YangModelRoot ownedSubmodule : moduleModelRoot.getOwnedSubmodules()) {
            foundStatement = findStatementUnderParent(ownedSubmodule.getSubmodule(), soughtSman,
                    unprefixedSoughtNodeIdentifier);
            if (foundStatement != null) {
                return foundStatement;
            }
        }

        return null;
    }

    private static AbstractStatement findStatementUnderParent(final AbstractStatement moduleOrSubmodule,
            final StatementModuleAndName soughtSman, final String soughtNodeIdentifier) {

        for (final AbstractStatement child : moduleOrSubmodule.getChildStatements()) {
            if (child.is(soughtSman) && soughtNodeIdentifier.equals(child.getStatementIdentifier())) {
                return child;
            }
        }

        return null;
    }

    /**
     * Given a set of statements, finds a data node of a given ns/name within those statements. choice / case are followed.
     */
    public static AbstractStatement findSchemaDataNode(final List<AbstractStatement> statements,
            final String soughtNamespace, final String soughtName) {

        for (final AbstractStatement statement : statements) {

            if (statement.definesDataNode() && statement.getEffectiveNamespace().equals(soughtNamespace) && soughtName
                    .equals(statement.getStatementIdentifier())) {
                return statement;
            } else if (statement.is(CY.STMT_CHOICE)) {

                for (final AbstractStatement yCase : statement.getChildren(CY.STMT_CASE)) {
                    final AbstractStatement dataNodeUnderChoiceCase = findSchemaDataNode(yCase.getChildStatements(),
                            soughtNamespace, soughtName);
                    if (dataNodeUnderChoiceCase != null) {
                        return dataNodeUnderChoiceCase;
                    }
                }

            }
        }

        return null;
    }

    /**
     * Given a set of statements, finds a data node of a given name within those statements. choice / case are followed.
     */
    public static AbstractStatement findSchemaDataNode(final List<AbstractStatement> statements, final String soughtName) {

        for (final AbstractStatement statement : statements) {

            if (statement.definesDataNode() && soughtName.equals(statement.getStatementIdentifier())) {
                return statement;
            } else if (statement.is(CY.STMT_CHOICE)) {

                for (final AbstractStatement yCase : statement.getChildren(CY.STMT_CASE)) {
                    final AbstractStatement dataNodeUnderChoiceCase = findSchemaDataNode(yCase.getChildStatements(),
                            soughtName);
                    if (dataNodeUnderChoiceCase != null) {
                        return dataNodeUnderChoiceCase;
                    }
                }

            }
        }

        return null;
    }

    /**
     * Given a statement, returns all data nodes under the statement. Data nodes underneath
     * choice / case, possibly nested, are likewise returned.
     */
    public static List<AbstractStatement> getChildDataNodes(final AbstractStatement parent) {

        List<AbstractStatement> result = null;

        for (final AbstractStatement child : parent.getChildStatements()) {
            if (child.definesDataNode()) {

                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(child);

            } else if (child.is(CY.STMT_CHOICE)) {

                /*
                 * Handle shorthand notation:
                 */
                final List<AbstractStatement> dataNodesUnderChoice = getChildDataNodes(child);
                if (!dataNodesUnderChoice.isEmpty()) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.addAll(dataNodesUnderChoice);
                }

                /*
                 * Handle cases:
                 */
                for (final AbstractStatement yCase : child.getChildren(CY.STMT_CASE)) {
                    final List<AbstractStatement> dataNodesUnderCase = getChildDataNodes(yCase);
                    if (!dataNodesUnderCase.isEmpty()) {
                        if (result == null) {
                            result = new ArrayList<>();
                        }
                        result.addAll(dataNodesUnderCase);
                    }
                }
            }
        }

        return result == null ? Collections.<AbstractStatement> emptyList() : result;
    }

    public static final String GENERAL_INFO_APP_DATA = "GENERAL_INFO_APP_DATA";

    public static void addGeneralInfoAppData(final AbstractStatement statement, final String info) {
        addAppDataListInfo(statement, GENERAL_INFO_APP_DATA, info);
    }

    public static List<String> getGeneralInfoAppData(final AbstractStatement statement) {
        return getAppDataListInfo(statement, GENERAL_INFO_APP_DATA);
    }

    /**
     * Adds opaque application information to a list identified by the key. If the information
     * already exists in the list, will not be added again.
     */
    public static void addAppDataListInfo(final AbstractStatement statement, final String key, final Object info) {

        List<Object> infos = Objects.requireNonNull(statement).getCustomAppData(key);
        if (infos == null) {
            infos = new ArrayList<>(1);
            statement.setCustomAppData(key, infos);
        }
        if (!infos.contains(info)) {
            infos.add(info);
        }
    }

    /**
     * Returns the application information for the given key. Returns an empty list (not null) if the
     * application information has not been specified for the statement.
     */
    public static <T> List<T> getAppDataListInfo(final AbstractStatement statement, final String key) {
        final List<T> infos = Objects.requireNonNull(statement).getCustomAppData(key);
        return infos == null ? Collections.<T> emptyList() : infos;
    }
}
