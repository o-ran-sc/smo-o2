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
package org.oran.smo.yangtools.parser.model.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.resolvers.Helper;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YCase;
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YInput;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YOutput;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.model.statements.yang.YSubmodule;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature.Token;
import org.oran.smo.yangtools.parser.model.util.YangFeature;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Processes the schema.
 *
 * @author Mark Hollmann
 */
public abstract class SchemaProcessor {

    // =================================== SUBMODULE HANDLING =====================================

    public static void resolveSubmodules(final Schema schema) {

        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            if (yangModelFile.getYangModelRoot().isSubmodule()) {
                mergeInSubmodule(schema, yangModelFile);
            }
        }
    }

    /**
     * Merges the content of submodules into their owning modules. This makes processing later on
     * considerably easier (e.g. much easier to find the target node for an augment or deviation).
     * <p/>
     * Only the statement tree is manipulated, the DOM tree remains unchanged.
     */
    private static void mergeInSubmodule(final Schema schema, final YangModel yangModel) {

        final YSubmodule submodule = yangModel.getYangModelRoot().getSubmodule();

        final String belongsToModuleName = submodule.getBelongsToValue();
        if (belongsToModuleName == null) {
            // No need for a finding, an invalid syntax finding would have previously issued.
            return;
        }

        final List<YangModel> moduleYangFiles = schema.getModuleRegistry().byModuleName(belongsToModuleName);
        if (moduleYangFiles.size() != 1) {
            // No need to record a finding - would have been caught previously.
            return;
        }

        /*
         * We are basically taking all the schema nodes and re-house them into the module. Note: The prefix-resolver
         * of all of these statements will not be adjusted, i.e. they will keep the prefix resolver of the submodule.
         * This is to handle a situation where the prefixes declared are different between those in the module and those
         * in the submodule(s) - which is frequently the case.
         */
        final YModule owningModule = moduleYangFiles.get(0).getYangModelRoot().getModule();
        if (owningModule == null) {
            // submodule owned by submodule, which is wrong; no need to record a finding - would have been caught previously.
            return;
        }

        owningModule.addChildren(submodule.getAnydata());
        owningModule.addChildren(submodule.getAnyxmls());
        owningModule.addChildren(submodule.getAugments());
        owningModule.addChildren(submodule.getChoices());
        owningModule.addChildren(submodule.getContainers());
        owningModule.addChildren(submodule.getDeviations());
        owningModule.addChildren(submodule.getExtensions());
        owningModule.addChildren(submodule.getFeatures());
        owningModule.addChildren(submodule.getGroupings());
        owningModule.addChildren(submodule.getIdentities());
        owningModule.addChildren(submodule.getLeafLists());
        owningModule.addChildren(submodule.getLeafs());
        owningModule.addChildren(submodule.getLists());
        owningModule.addChildren(submodule.getNotifications());
        owningModule.addChildren(submodule.getRpcs());
        owningModule.addChildren(submodule.getTypedefs());
        owningModule.addChildren(submodule.getUses());
    }

    // =================================== CASE HANDLING =====================================

    @SuppressWarnings("unchecked")
    public static void fixupOmittedCaseStatements(final Schema schema) {
        final List<YChoice> allChoices = (List<YChoice>) Helper.findStatementsInSchema(CY.STMT_CHOICE, schema);
        for (final YChoice oneChoice : allChoices) {
            injectCaseForShorthandedStatements(oneChoice);
        }
    }

    private static final Set<StatementModuleAndName> POSSIBLY_SHORTHANDED_STATEMENTS = new HashSet<>(Arrays.asList(
            CY.STMT_ANYDATA, CY.STMT_ANYXML, CY.STMT_CHOICE, CY.STMT_CONTAINER, CY.STMT_LEAF_LIST, CY.STMT_LEAF,
            CY.STMT_LIST));

    /**
     * Injects a 'case' statement above any data node-defining statement found directly under
     * a 'choice'. In Yang, this is referred-to as "shorthand". Example:
     * <p/>
     * choice my-choice {
     * container option1 { ... }
     * container option2 { ... }
     * }
     * <p/>
     * Usage of shorthand causes problems, as schema node paths referring to data nodes within
     * the choice must also use the identity of the (omitted) case statement. To simplify
     * navigation later on, we inject these missing case statements here. The above example is
     * effectively modified to:
     * <p/>
     * choice my-choice {
     * case option1 {
     * container option1 { ... }
     * }
     * case option2 {
     * container option2 { ... }
     * }
     * }
     * <p/>
     * Both the DOM tree and the statement tree are adjusted. The artificial 'case' statement
     * will be given the same name and line number as the statement defining the data node.
     * <p/>
     * The parent statement is usually a 'choice', but could be an 'augment' or 'grouping',
     * which inject into a 'choice'.
     */
    public static void injectCaseForShorthandedStatements(final AbstractStatement parentStatement) {

        final List<AbstractStatement> shorthandedStatements = parentStatement.getChildren(POSSIBLY_SHORTHANDED_STATEMENTS);

        for (final AbstractStatement shorthandedStatement : shorthandedStatements) {
            /*
             * The identifier of the case statement is equal to the identifier of the short-handed statement.
             */
            final String caseIdentifier = shorthandedStatement.getStatementIdentifier();
            /*
             * Create new DOM element for the 'case' statement first. It will sit below the parent
             * statement and has no children (yet). As line number, we will give it the line number
             * of the short-handed statement.
             */
            final YangDomElement caseDomElement = new YangDomElement(CY.CASE, caseIdentifier, parentStatement
                    .getDomElement(), shorthandedStatement.getDomElement().getLineNumber());
            /*
             * Now create the 'case' statement under the parent statement, and then move
             * the short-handed statement under the 'case'. That cleans up the statement tree.
             */
            final YCase newCase = new YCase(parentStatement, caseDomElement);
            newCase.addChild(shorthandedStatement);
            /*
             * We still need to clean up the DOM tree - we do something very similar here,
             * basically re-parenting the short-handed DOM element under the 'case' DOM element.
             */
            caseDomElement.reparent(shorthandedStatement.getDomElement());

            /*
             * The properties need to be copied over.
             */
            newCase.copyPropertiesFrom(shorthandedStatement);

            /*
             * Any 'if-feature' statements that sit under the shorthand statement also need to
             * be cloned to make the newly-created case just as much dependent on those.
             */
            final List<YIfFeature> ifFeaturesOfShorthand = shorthandedStatement.getChildren(CY.STMT_IF_FEATURE);
            for (final YIfFeature ifFeature : ifFeaturesOfShorthand) {
                final YIfFeature clonedIfFeature = new YIfFeature(newCase, ifFeature.getDomElement());
                clonedIfFeature.cloneFrom(ifFeature);
            }

            Helper.addGeneralInfoAppData(newCase, "originally omitted 'case' statement inserted for readability.");
        }
    }

    // =================================== INPUT / OUTPUT HANDLING =====================================

    public static void fixupMissingInputOutputStatements(final Schema schema) {
        fixupMissingInputOutputStatements(schema, CY.STMT_ACTION);
        fixupMissingInputOutputStatements(schema, CY.STMT_RPC);
    }

    /**
     * Some RPCs or actions do not use any input or output. However, it is still possible for data nodes to
     * be augmented-into RPCs and actions. For this to work, the targetNode path will list the input/output
     * statement. We simplify our processing later on if we inject any missing input/output statements now.
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractStatement> void fixupMissingInputOutputStatements(final Schema schema,
            final StatementModuleAndName actionOrRpcClazz) {

        final List<T> statements = (List<T>) Helper.findStatementsInSchema(actionOrRpcClazz, schema);
        for (final T actionOrRpc : statements) {

            if (actionOrRpc.getChild(CY.STMT_INPUT) == null) {
                /*
                 * Create new DOM element for the 'input' statement. It will sit below the 'action' (or 'rpc')
                 * statement and has no children. As line number, we will give it the line number of the parent.
                 */
                final YangDomElement inputDomElement = new YangDomElement(CY.INPUT, null, actionOrRpc.getDomElement(),
                        actionOrRpc.getDomElement().getLineNumber());
                /*
                 * And create the statement.
                 */
                final YInput yInput = new YInput(actionOrRpc, inputDomElement);
                /*
                 * Copy over properties as well.
                 */
                yInput.copyPropertiesFrom(actionOrRpc);
            }

            /*
             * Exact same now for the output.
             */
            if (actionOrRpc.getChild(CY.STMT_OUTPUT) == null) {
                final YangDomElement outputDomElement = new YangDomElement(CY.OUTPUT, null, actionOrRpc.getDomElement(),
                        actionOrRpc.getDomElement().getLineNumber());
                final YOutput yOutput = new YOutput(actionOrRpc, outputDomElement);
                yOutput.copyPropertiesFrom(actionOrRpc);
            }
        }
    }

    // =================================== DATA NODE HANDLING FOR IMPORT-ONLY MODULES =====================================

    public static void removeProtocolAccessibleObjects(final Schema schema) {
        /*
         * According to RFC, any statement that "implements any protocol-accessible objects" cannot remain in the
         * module, so we delete those. Such statements may have been augmented into a lower part of the tree, so we
         * need to navigate down the tree.
         */
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            removeProtocolAccessibleObjects(yangModelFile.getYangModelRoot().getModuleOrSubmodule());
        }
    }

    private static final Set<StatementModuleAndName> PROTOCOL_ACCESSIBLE_STATEMENTS = new HashSet<>(Arrays.asList(
            CY.STMT_ANYDATA, CY.STMT_ANYXML, CY.STMT_AUGMENT, CY.STMT_CHOICE, CY.STMT_CONTAINER, CY.STMT_DEVIATION,
            CY.STMT_LEAF, CY.STMT_LEAF_LIST, CY.STMT_LIST, CY.STMT_NOTIFICATION, CY.STMT_RPC, CY.STMT_USES));

    private static void removeProtocolAccessibleObjects(final AbstractStatement statement) {

        final List<AbstractStatement> children = statement.getNonExtensionChildStatements();
        for (final AbstractStatement child : children) {
            if (child.getEffectiveConformanceType() == ConformanceType.IMPORT && PROTOCOL_ACCESSIBLE_STATEMENTS.contains(
                    child.getStatementModuleAndName())) {
                statement.removeChild(child);
            } else if (child.definesSchemaNode()) {
                removeProtocolAccessibleObjects(child);
            }
        }
    }

    // =================================== IF-FEATURE HANDLING =====================================

    public static void removeDataNodesNotSatisfyingIfFeature(final ParserExecutionContext context, final Schema schema) {

        /*
         * There is a special case that requires some up-front logic: it is possible for
         * a feature itself to be constrained by if-feature. This does not mean that the
         * feature shall be removed. From the RFC:
         *
         * "In order for a server to support a feature that is dependent on any other
         * features (i.e., the feature has one or more "if-feature" substatements), the
         * server MUST also support all the dependent features."
         *
         * Consider this:
         *
         *   feature feature-abc;
         *   feature feature-def;
         *   feature feature-xyz {
         *       if-feature "feature-abc and feature-def";
         *   }
         *
         * What this means is that xyz can only be supported if both abc and def are
         * supported. The client that has supplied the YANG library, however, could have
         * gotten this wrong. So we need to check for that.
         */
        checkFeaturesConstrainedByIfFeatures(context, schema);

        /*
         * Now that this is done, remove statements as required...
         */
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            removeFromModule(context, schema, yangModelFile);
        }
    }

    private static void removeFromModule(final ParserExecutionContext context, final Schema schema,
            final YangModel yangModelFile) {

        final AbstractStatement moduleOrSubmodule = yangModelFile.getYangModelRoot().getModuleOrSubmodule();

        /*
         * We go through the statement tree and check each statement whether it's
         * if-feature is fulfilled or not.
         */
        handleStatementPossiblyConstrainedByIfFeature(context, schema, moduleOrSubmodule);
    }

    private static void handleStatementPossiblyConstrainedByIfFeature(final ParserExecutionContext context,
            final Schema schema, final AbstractStatement parent) {

        final List<AbstractStatement> children = new ArrayList<>(parent.getChildStatements());		// deep copy required
        for (final AbstractStatement child : children) {
            if (!child.is(CY.STMT_FEATURE) && !ifFeaturesUnderStatementAreSatisfied(context, schema, child)) {
                /*
                 * Remove the statement and keep a note of it!
                 */
                addChildRemovedDueToIfFeatureAppData(parent, child.getDomElement().getNameValue());
                if (child.getStatementIdentifier() != null && !child.getStatementIdentifier().isEmpty()) {
                    addRemovedChildIdentifierDueToIfFeature(parent, child.getStatementIdentifier());
                }

                parent.removeChild(child);
            } else {
                /*
                 * Go into the sub-tree.
                 */
                handleStatementPossiblyConstrainedByIfFeature(context, schema, child);
            }
        }
    }

    private static void checkFeaturesConstrainedByIfFeatures(final ParserExecutionContext context, final Schema schema) {

        for (final YangModel yangModel : schema.getModuleRegistry().getAllYangModels()) {

            final List<YFeature> featuresInYam = yangModel.getYangModelRoot().getModuleOrSubmodule().getChildren(
                    CY.STMT_FEATURE);

            for (final YFeature feature : featuresInYam) {
                if (ifFeaturesUnderStatementAreSatisfied(context, schema, feature)) {
                    continue;
                }

                /*
                 * Some if-feature is not satisfied. This means that the feature itself MUST NOT
                 * be supported. Better check!
                 */
                final String namespace = feature.getDomElement().getYangModel().getYangModelRoot().getNamespace();
                final String moduleName = feature.getDomElement().getYangModel().getYangModelRoot().getOwningSchema()
                        .getModuleNamespaceResolver().getModuleForNamespace(namespace);

                final YangFeature yangFeature = new YangFeature(namespace, moduleName, feature.getFeatureName());
                if (context.getSupportedFeatures().contains(yangFeature)) {
                    context.addFinding(new Finding(feature, ParserFindingType.P086_FEATURE_CANNOT_BE_SUPPORTED,
                            "Feature '" + feature
                                    .getFeatureName() + "' has been supplied as supported, but it's 'if-feature' statement evaluates to false."));
                }
            }
        }
    }

    private static boolean ifFeaturesUnderStatementAreSatisfied(final ParserExecutionContext context, final Schema schema,
            final AbstractStatement statement) {

        if (!statement.hasAtLeastOneChildOf(CY.STMT_IF_FEATURE)) {
            return true;
        }

        final List<YIfFeature> ifFeatures = statement.getChildren(CY.STMT_IF_FEATURE);

        /*
         * There can be multiple if-feature statements under a statement. They all must
         * be true for the overall result to be true.
         */
        for (final YIfFeature ifFeature : ifFeatures) {
            if (!ifFeatureIsSatisfied(context, schema, ifFeature)) {
                return false;
            }
        }

        return true;
    }

    private static boolean ifFeatureIsSatisfied(final ParserExecutionContext context, final Schema schema,
            final YIfFeature ifFeature) {

        final List<Token> tokens = ifFeature.getTokens();

        /*
         * If the tokens are not valid the logic further below where we simplify the
         * expression until we get a result will throw errors, so catch this problem
         * here.
         */
        if (!ifFeature.areTokensValid(context, tokens)) {
            return false;
        }

        if (tokens.size() == 1) {
            /*
             * Simple if-feature statement, just referring to a single feature, easy.
             */
            return isFeatureSupported(context, schema, ifFeature, tokens.get(0).name);
        }

        /*
         * More than one token in the if-feature. This means we need to apply boolean
         * logic to the string We build a logical expression first, and then simplify it
         * bit-by-bit.
         */
        final List<Operand> expression = buildExpressionFromTokens(context, schema, ifFeature, tokens);

        final Operand result = simplify(expression);
        return result == Operand.TRUE;
    }

    private static boolean isFeatureSupported(final ParserExecutionContext context, final Schema schema,
            final YIfFeature ifFeature, final String possiblyPrefixedFeatureName) {

        final String featureName = QNameHelper.extractName(possiblyPrefixedFeatureName);
        String namespace = null;

        if (QNameHelper.hasPrefix(possiblyPrefixedFeatureName)) {
            final String prefix = QNameHelper.extractPrefix(possiblyPrefixedFeatureName);
            final ModuleIdentity moduleIdentityForPrefix = ifFeature.getPrefixResolver().getModuleForPrefix(prefix);
            if (moduleIdentityForPrefix == null) {
                context.addFinding(new Finding(ifFeature, ParserFindingType.P033_UNRESOLVEABLE_PREFIX,
                        "Unresolvable prefix '" + prefix + "'."));
                return false;
            }

            final YangModel yangModel = schema.getModuleRegistry().find(moduleIdentityForPrefix);
            if (yangModel == null) {
                context.addFinding(new Finding(ifFeature, ParserFindingType.P034_UNRESOLVABLE_IMPORT,
                        "Cannot find '" + moduleIdentityForPrefix + "' in input."));
                return false;
            }

            namespace = yangModel.getYangModelRoot().getNamespace();
        } else {
            /*
             * No prefix, so refers to same module in which it was defined.
             */
            namespace = ifFeature.getDomElement().getYangModel().getYangModelRoot().getNamespace();
        }

        final String moduleName = ifFeature.getDomElement().getYangModel().getYangModelRoot().getOwningSchema()
                .getModuleNamespaceResolver().getModuleForNamespace(namespace);

        final YangFeature yangFeature = new YangFeature(namespace, moduleName, featureName);
        return context.getSupportedFeatures().contains(yangFeature);
    }

    // -------------------------- All the expression handling stuff here -------------------------------

    private enum Operand {
        NOT,
        OR,
        AND,
        TRUE,
        FALSE,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS;
    }

    private static List<Operand> buildExpressionFromTokens(final ParserExecutionContext context, final Schema schema,
            final YIfFeature ifFeature, final List<Token> tokens) {

        final List<Operand> result = new ArrayList<>(tokens.size());

        for (final Token token : tokens) {

            switch (token.type) {
                case NOT:
                    result.add(Operand.NOT);
                    break;
                case AND:
                    result.add(Operand.AND);
                    break;
                case OR:
                    result.add(Operand.OR);
                    break;
                case LEFT_PARENTHESIS:
                    result.add(Operand.LEFT_PARENTHESIS);
                    break;
                case RIGHT_PARENTHESIS:
                    result.add(Operand.RIGHT_PARENTHESIS);
                    break;
                default:
                    if (isFeatureSupported(context, schema, ifFeature, token.name)) {
                        result.add(Operand.TRUE);
                    } else {
                        result.add(Operand.FALSE);
                    }
            }
        }

        return result;
    }

    /**
     * A bit of magic here. We keep simplifying the expression until there are no
     * more operators left.
     */
    private static Operand simplify(final List<Operand> input) {

        final List<Operand> result = new ArrayList<>(input);

        /*
         * The RFC gives as precedence: parenthesis, not, and, or. So that is the order in which we resolve things.
         *
         * To simplify the list, we simplify all sub-expressions (those in parenthesis) first.
         */
        while (result.contains(Operand.LEFT_PARENTHESIS)) {
            /*
             * Find first occurrence of LEFT, and last occurrence or RIGHT. The contents of
             * that will be simplified and replaces the sub-expression.
             */
            final int indexOfLeft = result.indexOf(Operand.LEFT_PARENTHESIS);
            int parenthesisCount = 1;
            int indexOfRight = indexOfLeft;

            while (parenthesisCount > 0) {
                indexOfRight++;
                if (result.get(indexOfRight) == Operand.LEFT_PARENTHESIS) {
                    parenthesisCount++;
                } else if (result.get(indexOfRight) == Operand.RIGHT_PARENTHESIS) {
                    parenthesisCount--;
                }
            }

            /*
             * Get the content that is between the parenthesis. That content will be
             * simplified in a moment and replaces the sub-expression.
             */
            final List<Operand> sublist = new ArrayList<>(result.subList(indexOfLeft + 1, indexOfRight));
            final Operand replaceSubExpressionWith = simplify(sublist);

            /*
             * Remove the sub-expression and replace with the result of the simplification.
             */
            final int nrOfElementsToRemove = indexOfRight - indexOfLeft + 1;
            for (int i = 0; i < nrOfElementsToRemove; ++i) {
                result.remove(indexOfLeft);
            }
            result.add(indexOfLeft, replaceSubExpressionWith);
        }

        /*
         * Next precedence is "NOT"
         */
        while (result.contains(Operand.NOT)) {

            final int indexOfNot = result.indexOf(Operand.NOT);

            /*
             * We simply remove the 'NOT', and flip the next element.
             */
            result.remove(indexOfNot);
            result.set(indexOfNot, result.get(indexOfNot) == Operand.TRUE ? Operand.FALSE : Operand.TRUE);
        }

        /*
         * Next precedence is "AND"
         */
        while (result.contains(Operand.AND)) {

            final int indexOfAnd = result.indexOf(Operand.AND);

            /*
             * We logically AND together the elements before and after. Then we remove all
             * three elements and replace with the ANDed result.
             */
            final boolean resultOfAnd = (result.get(indexOfAnd - 1) == Operand.TRUE) && (result.get(
                    indexOfAnd + 1) == Operand.TRUE);

            result.remove(indexOfAnd - 1);
            result.remove(indexOfAnd - 1);
            result.remove(indexOfAnd - 1);
            result.add(indexOfAnd - 1, resultOfAnd ? Operand.TRUE : Operand.FALSE);
        }

        /*
         * And finally "OR"
         */
        while (result.contains(Operand.OR)) {

            final int indexOfOr = result.indexOf(Operand.OR);

            /*
             * We logically OR together the elements before and after. Then we remove all
             * three elements and replace with the ORed result.
             */
            final boolean resultOfOr = (result.get(indexOfOr - 1) == Operand.TRUE) || (result.get(
                    indexOfOr + 1) == Operand.TRUE);

            result.remove(indexOfOr - 1);
            result.remove(indexOfOr - 1);
            result.remove(indexOfOr - 1);
            result.add(indexOfOr - 1, resultOfOr ? Operand.TRUE : Operand.FALSE);
        }

        /*
         * At this point, there can only be a single entry left in the list.
         */

        return result.get(0);
    }

    private static final String IF_FEATURE_INFO = "IF_FEATURE_INFO";
    private static final String IF_FEATURE_REMOVED_CHILD_IDENTIFIERS = "REMOVED_CHILD_IDENTIFIERS";

    private static void addChildRemovedDueToIfFeatureAppData(final AbstractStatement parentStatement,
            final String nameOfRemovedChild) {
        Helper.addAppDataListInfo(parentStatement, IF_FEATURE_INFO,
                "Child statement " + nameOfRemovedChild + " removed as it's if-feature condition evaluated to false.");
    }

    public static List<String> getChildRemovedDueToIfFeatureAppDataForStatement(final AbstractStatement statement) {
        return Helper.getAppDataListInfo(statement, IF_FEATURE_INFO);
    }

    private static void addRemovedChildIdentifierDueToIfFeature(final AbstractStatement statement,
            final String identifier) {
        Helper.addAppDataListInfo(statement, IF_FEATURE_REMOVED_CHILD_IDENTIFIERS, identifier);
    }

    public static List<String> getRemovedChildIdentifiersDueToIfFeatureForStatement(final AbstractStatement statement) {
        return Helper.getAppDataListInfo(statement, IF_FEATURE_REMOVED_CHILD_IDENTIFIERS);
    }

    // =================================== FINDING HANDLING ON UNUSED SCHEMA NODES =====================================

    /**
     * Certain parts of the YAMs may not make it into the final schema:
     *
     * - Unused typedefs
     * - Unused groupings
     *
     * Also, the schema may be tweaked such that original schema nodes are not longer referenced when we do the following:
     *
     * - deviate replace
     * - uses refine
     * - if-feature evaluating to false
     *
     * So what we do is we go through the final schema, and jump back to the DOM nodes and record which of these are still
     * referenced; whatever is not referenced will then be looked-at in terms of findings and the findings removed. This
     * cuts down on "noise"
     */
    public static void removeFindingsOnUnusedSchemaNodes(final ParserExecutionContext context, final Schema schema) {

        final Set<YangDomElement> usedDomNodes = new HashSet<>(500000, 0.75f);

        /*
         * Collect the DOM nodes first.
         */
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            collectUsedDomNodes(yangModelFile, usedDomNodes);
        }

        /*
         * Now go through the DOM and have a look.
         */
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            removeFindingsFromUnusedDomNodes(context, yangModelFile, usedDomNodes);
        }
    }

    private static void collectUsedDomNodes(final YangModel yangModelFile, final Set<YangDomElement> usedDomNodes) {

        /*
         * Start off with the YAM root, and then work recursively down.
         */
        final AbstractStatement moduleOrSubmodule = yangModelFile.getYangModelRoot().getModuleOrSubmodule();
        usedDomNodes.add(moduleOrSubmodule.getDomElement());

        collectUsedDomNodes(usedDomNodes, moduleOrSubmodule);
    }

    private final static Set<StatementModuleAndName> CHILDREN_OF_STATEMENTS_TO_IGNORE = new HashSet<>(Arrays.asList(
            CY.STMT_GROUPING, CY.STMT_TYPEDEF));

    private static void collectUsedDomNodes(final Set<YangDomElement> usedDomNodes,
            final AbstractStatement parentStatement) {

        for (final AbstractStatement child : parentStatement.getChildStatements()) {

            usedDomNodes.add(child.getDomElement());

            /*
             * Certain statements we happily ignore when we encounter them in the schema. They
             * would have been resolved / merged-in by this stage and have no further role to
             * play in the schema..
             */
            if (CHILDREN_OF_STATEMENTS_TO_IGNORE.contains(child.getStatementModuleAndName())) {
                continue;
            }

            collectUsedDomNodes(usedDomNodes, child);
        }
    }

    private static void removeFindingsFromUnusedDomNodes(final ParserExecutionContext context,
            final YangModel yangModelFile, final Set<YangDomElement> usedDomNodes) {
        final YangDomElement moduleOrSubmodule = yangModelFile.getYangModelRoot().getModuleOrSubmodule().getDomElement();
        removeFindingsFromUnusedDomNodes(context, usedDomNodes, moduleOrSubmodule);
    }

    private static void removeFindingsFromUnusedDomNodes(final ParserExecutionContext context,
            final Set<YangDomElement> usedDomNodes, final YangDomElement parentDomElement) {

        for (final YangDomElement child : parentDomElement.getChildren()) {

            if (!usedDomNodes.contains(child)) {
                /*
                 * We have encountered a DOM node that is not referenced by the statement tree. Remove any findings on it.
                 */
                context.getFindingsManager().removeFindingsOnYangDomElement(child);
            }

            removeFindingsFromUnusedDomNodes(context, usedDomNodes, child);
        }
    }

    // =================================== STATUS HANDLING =====================================

    /**
     * Each statement will be assigned the effective status.
     */
    public static void assignStatus(final Schema schema) {
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            final String implicitStatus = YStatus.CURRENT;
            assignStatus(yangModelFile.getYangModelRoot().getModuleOrSubmodule(), implicitStatus);
        }
    }

    private static void assignStatus(final AbstractStatement statement, final String statusOfParent) {

        String statusToAssign = statusOfParent;

        /*
         * If there is a status statement that makes the status "more severe"
         * we use that instead. If the status is less restrictive we ignore that.
         */
        final YStatus statusChild = statement.getChild(CY.STMT_STATUS);
        if (statusChild != null) {
            if (statusChild.isObsolete()) {
                statusToAssign = YStatus.OBSOLETE;
            } else if (statusChild.isDeprecated() && statusOfParent.equals(YStatus.CURRENT)) {
                statusToAssign = YStatus.DEPRECATED;
            }
        }

        statement.setEffectiveStatus(statusToAssign);

        for (final AbstractStatement child : statement.getChildStatements()) {
            assignStatus(child, statusToAssign);
        }
    }

    // =================================== NAMESPACE HANDLING =====================================

    /**
     * Each statement within a YAM will get the namespace of the module.
     */
    public static void assignEffectiveNamespaces(final Schema schema) {
        for (final YangModel yangModel : schema.getModuleRegistry().getAllYangModels()) {
            final String namespace = yangModel.getYangModelRoot().getNamespace();
            yangModel.getYangModelRoot().assignEffectiveNamespaceToStatementTree(namespace);
        }
    }

    // =================================== CONFORMANCE TYPE HANDLING =====================================

    /**
     * The ConformanceType is applied to all statements within a YAM.
     */
    public static void assignEffectiveConformanceType(final Schema schema) {
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            final ConformanceType conformanceType = yangModelFile.getConformanceType();
            yangModelFile.getYangModelRoot().assignEffectiveConformanceTypeToStatementTree(conformanceType);
        }
    }

    /**
     * The effective config is applied to all statements within a YAM.
     */
    public static void assignConfig(final Schema schema) {
        for (final YangModel yangModelFile : schema.getModuleRegistry().getAllYangModels()) {
            yangModelFile.getYangModelRoot().assignEffectiveConfigToStatementTree(true);
        }
    }
}
