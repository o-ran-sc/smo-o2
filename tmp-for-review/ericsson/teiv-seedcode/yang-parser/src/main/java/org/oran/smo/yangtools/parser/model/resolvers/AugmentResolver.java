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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.schema.SchemaProcessor;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YAugment;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.model.statements.yang.YWhen;
import org.oran.smo.yangtools.parser.model.util.StringHelper;

/**
 * Resolves 'augment' statements that sit at the root of YAMs.
 *
 * @author Mark Hollmann
 */
public abstract class AugmentResolver {

    /**
     * Resolves all augments by placing these into the correct part of the statement tree.
     * The DOM element tree is not modified.
     */
    public static void resolveAugments(final ParserExecutionContext context, final Schema schema) {

        boolean atLeastOneResolved = true;

        @SuppressWarnings("unchecked") final List<YAugment> allAugments = (List<YAugment>) Helper
                .findStatementsAtModuleRootInSchema(CY.STMT_AUGMENT, schema);

        /*
         * There are edge cases where an 'augment' refers to data nodes part of yet another 'augment'.
         * This means that the resolution of an augment will fail unless another augment is resolved
         * first. We handle this below by simply retrying.
         */
        while (atLeastOneResolved && !allAugments.isEmpty()) {

            atLeastOneResolved = false;

            for (int i = 0; i < allAugments.size();) {
                try {
                    final YAugment augment = allAugments.get(i);
                    final boolean resolved = resolveAugment(context, augment, schema);
                    /*
                     * If the augments was resolved we remove it from the list - otherwise move to
                     * the next one.
                     */
                    if (resolved) {
                        atLeastOneResolved = true;
                        allAugments.remove(i);
                    } else {
                        i++;
                    }
                } catch (final Exception ex) {
                    // Swallow and move to next. Best effort here, keep trying other augments.
                    i++;
                }
            }
        }

        /*
         * If after all that there are still 'augment' statement left, then these are not resolvable.
         */
        for (final YAugment augment : allAugments) {
            context.addFinding(new Finding(augment, ParserFindingType.P054_UNRESOLVABLE_PATH,
                    "Path to schema node '" + augment
                            .getAugmentTargetNode() + "', part of 'augment' statement, cannot be resolved."));
        }
    }

    /**
     * These are the statements that may be the target of an augmentation.
     * <p>
     * See RFC 7950, chapter 7.17
     */
    private static final Set<StatementModuleAndName> ALLOWABLE_TARGETS_OF_AUGMENT = new HashSet<>(Arrays.asList(
            CY.STMT_CONTAINER, CY.STMT_LIST, CY.STMT_CHOICE, CY.STMT_CASE, CY.STMT_INPUT, CY.STMT_OUTPUT,
            CY.STMT_NOTIFICATION));

    /**
     * These are the statements that can sit under the augment that we will merge-in under the target of the augmentation.
     */
    private static final Set<StatementModuleAndName> STATEMENTS_UNDER_AUGMENT_TO_HANDLE = new HashSet<>(Arrays.asList(
            CY.STMT_ACTION, CY.STMT_ANYDATA, CY.STMT_ANYXML, CY.STMT_CASE, CY.STMT_CHOICE, CY.STMT_CONTAINER,
            CY.STMT_LEAF_LIST, CY.STMT_LEAF, CY.STMT_LIST, CY.STMT_NOTIFICATION));

    private static boolean resolveAugment(final ParserExecutionContext context, final YAugment augment,
            final Schema schema) {

        final String augmentTargetNode = augment.getAugmentTargetNode();
        if (augmentTargetNode.isEmpty()) {
            /*
             * Pointless trying to resolve the path. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already. We return TRUE
             * to pretend that we handled the augment as it makes no sense re-trying it over
             * and over again.
             */
            return true;
        }

        final AbstractStatement augmentedStatement = findTargetSchemaNode(context, augment, schema);
        if (augmentedStatement == null) {
            /*
             * Possibly not found because the schema node is missing since it will be
             * merged-in by another augment statement that will be processed later on.
             * So delay processing.
             */
            return false;
        }

        /*
         * Check that the target (schema node) of the augments can actually be augmented.
         */
        if (!ALLOWABLE_TARGETS_OF_AUGMENT.contains(augmentedStatement.getStatementModuleAndName())) {
            final String allowableTargetsAsString = StringHelper.toString(ALLOWABLE_TARGETS_OF_AUGMENT, "[", "]", ", ", "'",
                    "'");
            context.addFinding(new Finding(augment, ParserFindingType.P151_TARGET_NODE_CANNOT_BE_AUGMENTED,
                    "Statement '" + augmentedStatement.getStatementModuleAndName() + "' pointed to by '" + augment
                            .getAugmentTargetNode() + "' cannot be augmented (only statements " + allowableTargetsAsString + ")."));
            /*
             * We return TRUE to pretend that we handled the augment as it makes no sense
             * re-trying it over and over again.
             */
            return true;
        }

        /*
         * Special handling: if the target of the augmentation is a choice statement, then the RFC
         * allows the shorthand notation to be used (i.e. not to augment with a 'case' but some
         * other data node). Example:
         *
         * Augmenting module:
         * ==================
         *
         * augment /abc:cont1/foo-choice {
         *   container bar { ... }
         * }
         *
         * Augmented module:
         * =================
         *
         * container cont1 {
         *   choice foo-choice {
         *     ...
         *   }
         * }
         *
         * In the example, we would not want to directly place the container under the 'choice', as this
         * may cause problems elsewhere - instead, we inject a 'case' statement, so we will end up with
         * this here in the end which is cleaner:
         *
         * Augmenting module:
         * ==================
         *
         * augment /abc:cont1/foo-choice {
         *   case bar {
         *     container bar { ... }
         *   }
         * }
         */
        if (augmentedStatement.is(CY.STMT_CHOICE)) {
            SchemaProcessor.injectCaseForShorthandedStatements(augment);
        }

        /*
         * Collect all the statements that will be moved under the augment's target node in a moment
         * and mark these as having been augmented-in.
         *
         * Note: Any extensions sitting directly under the 'augment' statement are considered to
         * relate to the 'augment' itself, i.e. will NOT be moved under the target of the augments. The
         * RFC does not mention extensions at all when it comes to 'augments' (not even that is it
         * 'undefined') so the assumption here is that extensions can only ever be "added" to other
         * statements by using a "deviate add".
         */
        final List<AbstractStatement> statementsToMoveUnderTargetNode = augment.getChildren(
                STATEMENTS_UNDER_AUGMENT_TO_HANDLE);

        /*
         * Check that whatever sits under the 'augment' statement is actually allowed under the
         * target of the augment. We can use the CY class for this that gets us the allowed optional
         * children.
         */
        final List<String> allowedOptionalChildren = CY.getOptionalMultipleChildren(augmentedStatement.getStatementName());

        for (final AbstractStatement statementToMove : statementsToMoveUnderTargetNode) {
            if (!allowedOptionalChildren.contains(statementToMove.getStatementName())) {
                context.addFinding(new Finding(statementToMove, ParserFindingType.P151_TARGET_NODE_CANNOT_BE_AUGMENTED,
                        "Statement '" + statementToMove.getStatementName() + "' is not allowed under '" + augmentedStatement
                                .getStatementName() + "' and therefore cannot be augmented-in."));
                /*
                 * We return TRUE to pretend that we handled the augment as it makes no sense
                 * re-trying it over and over again.
                 */
                return true;
            }
        }

        /*
         * 'when' and 'if-feature' must be cloned as well.
         */
        handleWhenAndIfFeature(augment, statementsToMoveUnderTargetNode);

        /*
         * Also inherit down the status if needs be.
         */
        handleStatus(augment, statementsToMoveUnderTargetNode);

        /*
         * Mark the children as having been augmented-in.
         */
        for (final AbstractStatement oneStatement : statementsToMoveUnderTargetNode) {
            addAugmentingReference(oneStatement, augment);
            addAugmentAppData(oneStatement, "statement augmented-in by 'augment' in " + StringHelper.getModuleLineString(
                    augment));
        }

        /*
         * Now take all of the statements that are under the augment statement and re-parent
         * them under the target schema node of the augment. They retain their own namespace
         * and prefix resolver. The DOM element tree is not modified.
         */
        augmentedStatement.addChildren(statementsToMoveUnderTargetNode);

        return true;
    }

    /**
     * Handles any 'when' or 'if-feature' under the augment.
     */
    private static void handleWhenAndIfFeature(final YAugment augment,
            final List<AbstractStatement> statementsToMoveUnderTargetNode) {
        /*
         * If the 'augment' has a 'when' clause this gets cloned to all statements within
         * the 'augment'. For example:
         *
         * Augmented module XYZ:
         * =====================
         *
         * container foo {
         *   leaf bar { type string; }
         * }
         *
         * Augmenting module
         * ==================
         *
         * augment /xyz:foo {
         *   when "bar = 'Hello!'";
         *
         *   leaf rock { type int32; }
         *   leaf roll { type int16; }
         * }
         *
         * We can't just drop the 'when' statement, it must be retained in order to make the
         * "rock" and "roll" leafs conditional. We want to be ending up with this here after
         * the augments has been resolved:
         *
         * Augmented module XYZ:
         * =====================
         *
         * container foo {
         *   leaf bar { type string; }
         *   leaf rock {
         *     when "bar = 'Hello!'";
         *     type int32;
         *   }
         *   leaf roll {
         *     type int16;
         *     when "bar = 'Hello!'";
         *   }
         * }
         *
         * Of course the issue now is that the 'when' statement inside the 'augment' refers to
         * the augment's target node, which is the container "foo". When we clone the 'when'
         * statement, we can only clone it to the *child* of the target node (here, the leafs).
         * The path inside the 'when' statement is now wrong - it applies to the parent of the
         * leafs. This is the reason why "appliesToParentSchemaNode" exists inside the YWhen class.
         *
         * So why not simply update the path of the 'when' statement? Because it is really hard
         * to do that. In this example here, it would be easy - simply place a "../" in front of
         * the path. However, the path could be really complex, involving multi-level navigation,
         * predicates, etc., and to correctly clean that up is a real challenge.
         *
         * Also note that the "when" statement is *added* to the child statement of the augment,
         * not *replaced*. That's a bit of a hack, as YANG only allows for a single occurrence
         * of 'when' for statements. However, it could conceivably be the case that each of the
         * statements amended thus has itself already a 'when' statement - and that must be
         * retained, so using a *replace* would be wrong. (And that's the reason why various
         * type-safe statement classes return 0..n 'when' statements, as opposed to 0..1.)
         */
        final YWhen whenUnderAugment = augment.getWhen();
        if (whenUnderAugment != null) {
            for (final AbstractStatement oneStatement : statementsToMoveUnderTargetNode) {
                final YWhen clonedWhen = new YWhen(oneStatement, whenUnderAugment.getDomElement());
                clonedWhen.cloneFrom(whenUnderAugment);
                clonedWhen.setAppliesToParentSchemaNode();
                addAugmentAppData(clonedWhen,
                        "This 'when' statement has been inherited from the 'when' statement that sits under the augment in " + StringHelper
                                .getModuleLineString(whenUnderAugment));
            }
        }

        /*
         * If the 'augment' has one or multiple 'if-feature' statements then these will be cloned
         * as well, similar to how this is done above. Only we don't have to worry about a path
         * here, we can simply clone the if-feature(s).
         */
        for (final YIfFeature ifFeature : augment.getIfFeatures()) {
            for (final AbstractStatement oneStatement : statementsToMoveUnderTargetNode) {
                final YIfFeature clonedIfFeature = new YIfFeature(oneStatement, ifFeature.getDomElement());
                clonedIfFeature.cloneFrom(ifFeature);
                addAugmentAppData(clonedIfFeature,
                        "This 'if-feature' statement has been inherited from the 'if-feature' statement that sits under the augment in " + StringHelper
                                .getModuleLineString(ifFeature));
            }
        }
    }

    /**
     * Handle a possible 'status' statement under the 'augment'. We must do this here during the merge of
     * the 'augment' content, as the 'augment' and its child 'status' will disappear from the schema tree,
     * hence the 'status' will be lost. To retain the information, we must clone the 'status' statement
     * into the contents of the 'augment'.
     *
     * Note there is some special handling - if the status is more restrictive under the augmented
     * statement then this would not be replaced. For example, if the status is DEPRECATED under the
     * augment, but it is explicitly OBSOLETE under a container being a child of the augment, this would
     * not be updated.
     */
    private static void handleStatus(final YAugment augment,
            final List<AbstractStatement> statementsToMoveUnderTargetNode) {

        final YStatus statusUnderAugment = augment.getStatus();
        if (statusUnderAugment == null) {
            return;
        }

        for (final AbstractStatement oneStatement : statementsToMoveUnderTargetNode) {

            final YStatus childExplicitStatus = oneStatement.getChild(CY.STMT_STATUS);
            boolean clone = true;

            if (childExplicitStatus == null) {
                /*
                 * There is no 'status' statement under the child, so then we will simply
                 * clone down the parent 'status' in a moment.
                 */
            } else if (childExplicitStatus.getStatusOrder() >= statusUnderAugment.getStatusOrder()) {
                /*
                 * There is an explicit 'status' statement under the child. If the child 'status'
                 * is more restrictive, or the same, as the 'status' of the parent we don't have
                 * to do anything, i.e. don't clone.
                 *
                 * For example, child is DEPRECATED, parent is CURRENT - hence child is more
                 * restrictive, so don't overwrite the 'status' (don't clone the parent 'status').
                 */
                clone = false;
            }

            if (clone) {
                /*
                 * Must clone, so first remove the 'status' statement under the child (if it exists).
                 */
                if (childExplicitStatus != null) {
                    oneStatement.removeChild(childExplicitStatus);
                }
                /*
                 * Now clone down the parent's (the augment's) 'status' into the child.
                 */
                final YStatus clonedStatus = new YStatus(oneStatement, statusUnderAugment.getDomElement());
                clonedStatus.cloneFrom(statusUnderAugment);
                addAugmentAppData(clonedStatus,
                        "This 'status' statement has been inherited from the 'status' statement that sits under the augment in " + StringHelper
                                .getModuleLineString(statusUnderAugment));
            }
        }
    }

    private static AbstractStatement findTargetSchemaNode(final ParserExecutionContext context, final YAugment augment,
            final Schema schema) {

        final AbstractStatement targetSchemaNode = Helper.findSchemaNode(context, augment, augment.getAugmentTargetNode(),
                schema);
        if (targetSchemaNode == null) {
            return null;
        }

        /*
         * Check whether the 'augment' and the target node sit inside the very same module. Poor modeling.
         */
        if (augment.getDomElement().getYangModel() == targetSchemaNode.getDomElement().getYangModel()) {
            context.addFinding(new Finding(augment, ParserFindingType.P152_AUGMENT_TARGET_NODE_IN_SAME_MODULE,
                    "Both 'augment' and it's target node sit in the same (sub-)module."));
        }

        return targetSchemaNode;
    }

    private static final String AUGMENTED_IN_REFERENCE = "AUGMENTED_IN_REFERENCE";

    private static void addAugmentingReference(final AbstractStatement statementAugmentedIn, final YAugment origAugment) {
        Helper.addAppDataListInfo(statementAugmentedIn, AUGMENTED_IN_REFERENCE, origAugment);
    }

    public static List<YAugment> getAugmentingReference(final AbstractStatement augmentedStatement) {
        return Helper.getAppDataListInfo(augmentedStatement, AUGMENTED_IN_REFERENCE);
    }

    private static final String AUGMENTED_IN_INFO = "AUGMENTED_IN_INFO";

    private static void addAugmentAppData(final AbstractStatement statement, final String info) {
        Helper.addAppDataListInfo(statement, AUGMENTED_IN_INFO, info);
    }

    public static List<String> getAugmentedInInfosForStatement(final AbstractStatement statement) {
        return Helper.getAppDataListInfo(statement, AUGMENTED_IN_INFO);
    }
}
