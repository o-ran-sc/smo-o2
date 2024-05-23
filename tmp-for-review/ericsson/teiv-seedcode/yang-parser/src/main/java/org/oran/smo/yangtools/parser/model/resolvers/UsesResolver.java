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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.schema.SchemaProcessor;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YAugment;
import org.oran.smo.yangtools.parser.model.statements.yang.YGrouping;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YRefine;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.model.statements.yang.YUses;
import org.oran.smo.yangtools.parser.model.statements.yang.YWhen;
import org.oran.smo.yangtools.parser.model.util.StringHelper;

/**
 * A class that can resolve all usage of "uses" by including the referenced grouping.
 *
 * This class will correctly handle nested grouping resolution - that means, can handle a
 * grouping referring to another grouping (as it has a uses).
 *
 * @author Mark Hollmann
 */
public abstract class UsesResolver {

    /**
     * Resolving means all occurrences of "uses" are replaced by the grouping they are referring to.
     */
    public static void resolveUsagesOfUses(final ParserExecutionContext context, final Schema schema) {

        int iterationCount = 10;
        boolean atLeastOneResolved = true;

        while (iterationCount > 0 && atLeastOneResolved) {

            atLeastOneResolved = false;
            iterationCount--;

            /*
             * It is correct that the list of "uses" statements is fetched every time here, and not once outside
             * the while-loop. The reason is that otherwise we would simply keep doing the same merge/replace 10 times,
             * and also replaced "uses" statements are detached from the tree, so no need to do these again.
             */
            final List<YUses> allUses = findUsesToConsider(schema);
            for (final YUses uses : allUses) {
                try {
                    atLeastOneResolved |= resolveUses(context, schema, uses);
                } catch (final Exception ex) {
                    /* Swallow and move to next. Best effort here, keep trying other uses. */
                }
            }

            if (iterationCount == 7) {
                final List<YUses> usesWithExcessiveGroupingDepth = findUsesToConsider(schema);
                usesWithExcessiveGroupingDepth.forEach(yUses -> context.addFinding(new Finding(yUses,
                        ParserFindingType.P122_EXCESSIVE_USES_DEPTH,
                        "'uses' statement refers to 'grouping' with nesting depth > 3.")));
            }
        }

        /*
         * Done resolving. If some 'uses' are left they could not be resolved because of circular dependencies.
         */
        final List<YUses> allUses = findUsesToConsider(schema);
        allUses.forEach(yUses -> context.addFinding(new Finding(yUses, ParserFindingType.P121_CIRCULAR_USES_REFERENCES,
                "Likely circular references between 'uses' and 'grouping'. Use the quoted file and line number as starting point for investigation.")));

        /*
         * Finished with replacing all usages of grouping. Perform a check to see
         * which groupings have only be used once, or not used at all.
         */
        @SuppressWarnings("unchecked") final List<YGrouping> allGroupings = (List<YGrouping>) Helper.findStatementsInSchema(
                CY.STMT_GROUPING, schema);
        for (final YGrouping oneGrouping : allGroupings) {
            final int used = getGroupingUsageCount(oneGrouping);
            if (used == 0) {
                context.addFinding(new Finding(oneGrouping, ParserFindingType.P132_GROUPING_NOT_USED,
                        "grouping statement '" + oneGrouping.getGroupingName() + "' not used."));
            } else if (used == 1) {
                context.addFinding(new Finding(oneGrouping, ParserFindingType.P133_GROUPING_USED_ONCE_ONLY,
                        "grouping statement '" + oneGrouping.getGroupingName() + "' used only once; consider inlining."));
            }
        }
    }

    /**
     * Does what it says on the tin. Note that the 'uses' statement will be removed from the tree once it has been
     * resolved (and likewise it will remain in the statement tree if it cannot be resolved).
     */
    private static boolean resolveUses(final ParserExecutionContext context, final Schema schema,
            final YUses usesStatement) {

        final String groupingName = usesStatement.getUsesGroupingName();
        if (groupingName.isEmpty()) {
            /*
             * Pointless trying to resolve the grouping. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
             */
            setUsesNotResolvable(usesStatement);
            return false;
        }

        /*
         * Only now attempt to resolve grouping
         */
        final YGrouping foundGrouping = Helper.findStatement(context, schema, usesStatement, CY.STMT_GROUPING,
                groupingName);
        if (foundGrouping == null) {
            setUsesNotResolvable(usesStatement);
            context.addFinding(new Finding(usesStatement, ParserFindingType.P131_UNRESOLVABLE_GROUPING,
                    "Cannot resolve grouping '" + usesStatement.getUsesGroupingName() + "'."));
            return false;
        }

        /*
         * Mark the grouping has been used.
         */
        incGroupingUsageCount(foundGrouping);

        /*
         * Check for nested 'uses' within the 'grouping'. If found, this means that the contents of the
         * grouping itself must be resolved first.
         */
        if (usesExistWithinFoundGrouping(context, usesStatement, foundGrouping)) {
            return false;
        }

        /*
         * We first create a 1:1 clone of the grouping. Note that the prefix resolver stays the same, i.e. is the
         * prefix resolver from the module containing the found grouping statement. The cloned grouping statement
         * is a sibling of the found grouping so that we can apply refine/augments to it (the cloned grouping will
         * be removed again later on).
         */
        final YGrouping clonedGrouping = new YGrouping(foundGrouping.getParentStatement(), foundGrouping.getDomElement());
        clonedGrouping.cloneFrom(foundGrouping);

        for (final AbstractStatement oneChildOfClonedGrouping : clonedGrouping.getChildStatements()) {
            Helper.addGeneralInfoAppData(oneChildOfClonedGrouping, "statement placed here by 'uses' in " + StringHelper
                    .getModuleLineString(usesStatement) + " of grouping '" + usesStatement
                            .getUsesGroupingName() + "' from " + StringHelper.getModuleLineString(foundGrouping));
            addGroupingReference(oneChildOfClonedGrouping, foundGrouping);
        }

        /*
         * Handle any status statement under the 'uses' or 'grouping'.
         */
        handleStatus(usesStatement, clonedGrouping);

        /*
         * Handle 'refine'. These are used to update the contents of the grouping.
         */
        handleRefines(context, schema, usesStatement, clonedGrouping, foundGrouping);

        /*
         * We apply any "augments". The augments hangs under the "uses" statement; we simply re-parent
         * all statements that hang under augments into the correct location in the group, based on the
         * target node of the augments.
         */
        for (final YAugment augment : usesStatement.getAugments()) {
            handleAugment(context, schema, augment, clonedGrouping, foundGrouping, usesStatement);
        }

        /*
         * If there is an "if-feature" underneath the 'uses', then this if-feature will be
         * applied to each of the direct children of the cloned grouping statement. Note that
         * any if-feature will be *added*, not set (i.e. it's a merge operation of the
         * if-feature statements, not a replace.)
         */
        for (final YIfFeature origUsesOneIfFeature : usesStatement.getIfFeatures()) {
            for (final AbstractStatement childOfClonedGrouping : clonedGrouping.getChildStatements()) {
                final YIfFeature clonedIfFeature = new YIfFeature(childOfClonedGrouping, origUsesOneIfFeature
                        .getDomElement());
                clonedIfFeature.cloneFrom(origUsesOneIfFeature);
                // No need to explicitly add it - the YIfFeature constructor will add it as child already.
            }
        }

        /*
         * If there is a 'when' statement underneath the 'uses', then this 'when' will be applied to each of the
         * direct children of the cloned grouping statement. Note this is different from a 'when' statement that
         * is part of the augments for a uses. The original 'when' clause relates to the 'uses' statement, whose
         * data node is the parent of the uses. Hence the cloned when statements apply to the parent of the
         * respective data node, not the data node in the grouping itself!
         */
        final YWhen origUsesWhen = usesStatement.getWhen();
        if (origUsesWhen != null) {
            for (final AbstractStatement statementToApplyWhenTo : clonedGrouping.getChildStatements()) {
                final YWhen clonedWhen = new YWhen(statementToApplyWhenTo, origUsesWhen.getDomElement());
                clonedWhen.cloneFrom(origUsesWhen);
                clonedWhen.setAppliesToParentSchemaNode();
                // No need to explicitly add it - the YWhen constructor will add it as child already.
            }
        }

        /*
         * The cloned grouping is now complete. We now hang the contents of the cloned grouping underneath the parent
         * statement of the 'uses' (i.e., in effect replace the 'uses' statement with the contents of the cloned grouping).
         *
         * When we do this, the following child elements of the cloned grouping can be ignored:
         * - if-feature (not a valid child underneath grouping)
         * - grouping (nested, would have resolved any uses of the grouping beforehand)
         * - typedef (would have been resolved beforehand)
         * - uses (nested, would have been resolved beforehand)
         *
         * Note that we have to re-parent the cloned groupings child statements, of course. The prefix resolver is ok,
         * as it would have inherited down from the cloned grouping, and the cloned grouping got the prefix resolver
         * of the original 'grouping' statement.
         */
        final AbstractStatement parentOfUsesStatement = usesStatement.getParentStatement();

        parentOfUsesStatement.addChildren(clonedGrouping.getActions());
        parentOfUsesStatement.addChildren(clonedGrouping.getAnyxmls());
        parentOfUsesStatement.addChildren(clonedGrouping.getAnydata());
        parentOfUsesStatement.addChildren(clonedGrouping.getChoices());
        parentOfUsesStatement.addChildren(clonedGrouping.getContainers());
        parentOfUsesStatement.addChildren(clonedGrouping.getLeafs());
        parentOfUsesStatement.addChildren(clonedGrouping.getLeafLists());
        parentOfUsesStatement.addChildren(clonedGrouping.getLists());
        parentOfUsesStatement.addChildren(clonedGrouping.getNotifications());

        /*
         * Any finally, remove the cloned grouping as it is not needed anymore, and remove the original "uses" statement
         */
        clonedGrouping.getParentStatement().removeChild(clonedGrouping);
        usesStatement.getParentStatement().removeChild(usesStatement);

        return true;
    }

    /**
     * Handle a possible 'status' statement under the 'uses' or 'grouping'. We must do this here during
     * the merge of the 'grouping' content, as the 'grouping' and the 'uses' and their possible child
     * 'status' will disappear from the schema tree, hence the 'status' will be lost. To retain the
     * information, we must clone the 'status' statement into the contents of the 'grouping'.
     *
     * Note there is some special handling - if the status is more restrictive under the used
     * statement then this would not be replaced. For example, if the status is DEPRECATED under the
     * 'uses', but it is explicitly OBSOLETE under a container being a child of the 'grouping', this would
     * not be updated.
     */
    private static void handleStatus(final YUses uses, final YGrouping clonedGrouping) {

        final YStatus statusUnderUses = uses.getStatus();
        final YStatus statusUnderGrouping = clonedGrouping.getStatus();

        if (statusUnderUses == null && statusUnderGrouping == null) {
            return;
        }

        /*
         * So this gets a bit tricky. There can be a 'status' statement either under the 'uses',
         * or under the 'grouping', or possibly both.
         */
        YStatus overrideStatus = null;
        if (statusUnderUses != null && statusUnderGrouping != null) {
            /*
             * We are interested in pushing-down the more severe status, so we need to compare these to
             * find which one it is.
             */
            overrideStatus = statusUnderUses.getStatusOrder() > statusUnderGrouping.getStatusOrder() ?
                    statusUnderUses :
                    statusUnderGrouping;

        } else if (statusUnderUses != null) {

            overrideStatus = statusUnderUses;

        } else if (statusUnderGrouping != null) {

            overrideStatus = statusUnderGrouping;
        }

        /*
         * Now apply the override 'status' to the contents of the 'grouping'.
         */
        for (final AbstractStatement childOfClonedGrouping : clonedGrouping.getChildStatements()) {

            if (childOfClonedGrouping.is(CY.STMT_STATUS)) {
                continue;
            }

            final YStatus childExplicitStatus = childOfClonedGrouping.getChild(CY.STMT_STATUS);
            boolean clone = true;

            if (childExplicitStatus == null) {
                /*
                 * There is no 'status' statement under the child, so then we will simply
                 * clone down the parent 'status' in a moment.
                 */
            } else if (childExplicitStatus.getStatusOrder() >= overrideStatus.getStatusOrder()) {
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
                    childOfClonedGrouping.removeChild(childExplicitStatus);
                }
                /*
                 * Now clone down the parent's (the uses's or grouping's) 'status' into the child.
                 */
                final YStatus clonedStatus = new YStatus(childOfClonedGrouping, overrideStatus.getDomElement());
                clonedStatus.cloneFrom(overrideStatus);
                addUsesResolutionAppData(clonedStatus,
                        "This 'status' statement has been inherited from the 'uses'/'grouping' statement.");
            }
        }
    }

    private static final List<StatementModuleAndName> TARGETS_ALLOWED_FOR_AUGMENTATION = Arrays.asList(CY.STMT_CONTAINER,
            CY.STMT_LIST, CY.STMT_CHOICE, CY.STMT_CASE, CY.STMT_INPUT, CY.STMT_OUTPUT, CY.STMT_NOTIFICATION);

    private static final Set<StatementModuleAndName> STATEMENTS_UNDER_AUGMENT_TO_HANDLE = new HashSet<>(Arrays.asList(
            CY.STMT_ACTION, CY.STMT_ANYDATA, CY.STMT_ANYXML, CY.STMT_CASE, CY.STMT_CHOICE, CY.STMT_CONTAINER,
            CY.STMT_LEAF_LIST, CY.STMT_LEAF, CY.STMT_LIST, CY.STMT_NOTIFICATION));

    private static void handleAugment(final ParserExecutionContext context, final Schema schema, final YAugment augment,
            final YGrouping clonedGrouping, final YGrouping foundGrouping, final YUses usesStatement) {

        final String augmentTargetNode = augment.getAugmentTargetNode();
        if (augmentTargetNode.isEmpty() || augmentTargetNode.startsWith("/")) {
            /*
             * Pointless trying to resolve the path. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
             */
            return;
        }

        /*
         * First thing check the status of the 'augment'. If it is OBSOLETE nothing gets merged in.
         */
        final String augmentStatus = augment.getStatus() != null ? augment.getStatus().getValue() : YStatus.CURRENT;
        if (augmentStatus.equals(YStatus.OBSOLETE)) {
            Helper.addGeneralInfoAppData(augment,
                    "'augment' not applied to grouping as the augment is marked as OBSOLETE.");
            return;
        }

        final AbstractStatement targetSchemaNodeOfAugment = Helper.findSchemaNode(context, clonedGrouping,
                augmentTargetNode, schema);
        if (targetSchemaNodeOfAugment == null) {
            context.addFinding(new Finding(augment.getDomElement(), ParserFindingType.P054_UNRESOLVABLE_PATH.toString(),
                    "Cannot find schema node with path '" + augment
                            .getAugmentTargetNode() + "' relative to the 'uses' statement."));
            return;
        }

        /*
         * Make sure what is being augmented is actually allowed according to the RFC.
         */
        if (!TARGETS_ALLOWED_FOR_AUGMENTATION.contains(targetSchemaNodeOfAugment.getStatementModuleAndName())) {
            context.addFinding(new Finding(augment.getDomElement(), ParserFindingType.P123_INVALID_USES_AUGMENT_TARGET_NODE
                    .toString(), "Statement '" + targetSchemaNodeOfAugment
                            .getStatementName() + "' pointed to by '" + augment
                                    .getAugmentTargetNode() + "' cannot be augmented."));
            return;
        }

        /*
         * Collect all the statements that will be added to the augment's target node in a moment.
         */
        final List<AbstractStatement> statementsToAddAsChildrenOfTargetNode = augment.getChildren(
                STATEMENTS_UNDER_AUGMENT_TO_HANDLE);
        for (final AbstractStatement statementToAddAsChildOfTargetNode : statementsToAddAsChildrenOfTargetNode) {
            Helper.addGeneralInfoAppData(statementToAddAsChildOfTargetNode,
                    "augmented-in into used grouping '" + foundGrouping
                            .getGroupingName() + "' by 'uses' statement in " + StringHelper.getModuleLineString(
                                    usesStatement));
        }

        /*
         * This is where things get interesting. The target node could be a CHOICE, and the children nodes could be data
         * definition statements (short-hand notation). In this scenario we want to interject an artificial CASE statement
         * to clean up the schema tree. Otherwise, other augments (or deviations) may not work subsequently.
         */
        if (targetSchemaNodeOfAugment.is(CY.STMT_CHOICE)) {

            SchemaProcessor.injectCaseForShorthandedStatements(augment);

            /*
             * The direct children of choice may have have changed (case interjected), so we
             * need to re-fetch these before further processing.
             */
            statementsToAddAsChildrenOfTargetNode.clear();
            statementsToAddAsChildrenOfTargetNode.addAll(augment.getChildren(STATEMENTS_UNDER_AUGMENT_TO_HANDLE));
        }

        /*
         * If the augment has a 'when' clause this gets applied to all statements within the 'grouping'.
         *
         * Note that the "when" statement is *added*, not *replaced*. That's a bit of a hack, as YANG only allows for a single
         * "when" for statements. However, it could conceivably be the case that each of the statements amended thus has
         * itself already a "when" clause - so using a *replace* would be wrong.
         */
        if (augment.getWhen() != null) {
            for (final AbstractStatement childToAdd : statementsToAddAsChildrenOfTargetNode) {
                final YWhen clonedWhen = new YWhen(childToAdd, augment.getWhen().getDomElement());
                clonedWhen.cloneFrom(augment.getWhen());
                clonedWhen.setAppliesToParentSchemaNode();
            }
        }

        /*
         * If the augment has one or multiple "if-feature" statements then these will be applied to each
         * of the augment's statements individually.
         */
        for (final YIfFeature ifFeature : augment.getIfFeatures()) {
            for (final AbstractStatement childToAdd : statementsToAddAsChildrenOfTargetNode) {
                final YIfFeature clonedIfFeature = new YIfFeature(childToAdd, ifFeature.getDomElement());
                clonedIfFeature.cloneFrom(ifFeature);
            }
        }

        /*
         * If the 'augment' has a status then this status must likewise be applied to the
         * children. Really, it can only conceivable have CURRENT or DEPRECATED.
         */
        if (augmentStatus.equals(YStatus.DEPRECATED)) {
            for (final AbstractStatement childToAdd : statementsToAddAsChildrenOfTargetNode) {
                /*
                 * Rules:
                 *
                 * 1. Child does not have a status -> then it gets one (same as status on the augment).
                 * 2. Child has status and it is the same as that on augment -> do nothing.
                 * 3. Child has status and it is OBSOLETE -> do nothing.
                 */
                final YStatus childStatus = childToAdd.getChild(CY.STMT_STATUS);
                if (childStatus == null) {
                    final YStatus clonedStatus = new YStatus(childToAdd, augment.getStatus().getDomElement());
                    clonedStatus.cloneFrom(augment.getStatus());
                } else if (childStatus.getValue().equals(augmentStatus) || childStatus.isObsolete()) {
                    // do nothing.
                }
            }
        }

        /*
         * And now simply move all the statements from under the augment under the target node.
         */
        targetSchemaNodeOfAugment.addChildren(statementsToAddAsChildrenOfTargetNode);
    }

    /**
     * Check if the found grouping itself contains any "uses", and/or any "uses" that are not resolvable.
     */
    private static boolean usesExistWithinFoundGrouping(final ParserExecutionContext context, final YUses usesStatement,
            final YGrouping grouping) {

        final List<YUses> usesWithinGrouping = new ArrayList<>();
        Helper.findStatementsInSubtree(grouping, CY.STMT_USES, usesWithinGrouping);

        boolean groupingContainsNonResolveableUses = false;
        for (final YUses usesWithin : usesWithinGrouping) {
            if (isUsesNotResolvable(usesWithin)) {

                groupingContainsNonResolveableUses = true;

                /*
                 * We issue additional findings here to help the user figure out which
                 * nested 'uses' is/are causing the problem.
                 */
                context.addFinding(new Finding(usesStatement, ParserFindingType.P134_NESTED_USES_NOT_RESOLVABLE,
                        "Referenced grouping '" + usesStatement
                                .getUsesGroupingName() + "' has nested unresolvable 'uses' statement " + usesWithin
                                        .getDomElement().getNameValue() + "."));
            }
        }

        if (groupingContainsNonResolveableUses) {
            /*
             * If the found grouping has itself a 'uses' that is not resolvable, then this 'uses'
             * here likewise cannot be resolved.
             */
            setUsesNotResolvable(usesStatement);
        }

        return usesWithinGrouping.size() > 0;
    }

    private static void handleRefines(final ParserExecutionContext context, final Schema schema, final YUses usesStatement,
            final YGrouping clonedGrouping, final YGrouping foundGrouping) {
        /*
         * We refine the contents of the group, if so required. Note that the 'refine' statement hangs
         * under the 'uses' statement.
         */
        for (final YRefine refine : usesStatement.getRefines()) {

            final String refineTargetNode = refine.getRefineTargetNode();
            if (refineTargetNode.isEmpty() || refineTargetNode.startsWith("/")) {
                /*
                 * Pointless trying to resolve the path. No point issuing a finding either, a
                 * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
                 */
                continue;
            }

            final AbstractStatement refinedStatement = Helper.findSchemaNode(context, clonedGrouping, refineTargetNode,
                    schema);
            if (refinedStatement == null) {
                context.addFinding(new Finding(refine, ParserFindingType.P054_UNRESOLVABLE_PATH,
                        "Cannot find schema node with path '" + refineTargetNode + "' for refine of grouping '" + foundGrouping
                                .getGroupingName() + "'."));
                continue;
            }

            refineYangStatements(context, refinedStatement, usesStatement, refine);
            refineExtensionStatements(refinedStatement, refine);
        }
    }

    private static void refineExtensionStatements(final AbstractStatement refinedStatement, final YRefine refine) {
        /*
         * Extensions have to be handled. The RFC does not stipulate how these are to be handled. The
         * working assumption here is that 'replace' semantics shall apply to all of these. In other
         * words, extensions of a given type (identified through the extension name and its owning module
         * name) replace any instance of the same type.
         *
         * We first collect all extensions that are refined, and keep a note of their "type"
         * (combination of module name + extension name).
         */
        final List<AbstractStatement> extensionsUnderRefine = new ArrayList<>();
        final Set<String> moduleNameAndExtensionNameOfExtensionsUnderRefine = new HashSet<>();

        refine.getExtensionChildStatements().forEach(extensionStatement -> {
            extensionsUnderRefine.add(extensionStatement);
            final String moduleNameAndExtensionName = getModuleNameAndExtensionName(extensionStatement);
            moduleNameAndExtensionNameOfExtensionsUnderRefine.add(moduleNameAndExtensionName);
            Helper.addGeneralInfoAppData(extensionStatement, "refines previous extension statement(s) of the same type.");
        });

        if (extensionsUnderRefine.isEmpty()) {
            return;
        }

        /*
         * Now collect all extensions instances that sit under the refined statement, and that are of
         * the same type as any of those sitting under 'refine'.
         */
        final List<AbstractStatement> extensionsUnderRefinedStatementToRemove = new ArrayList<>();

        refinedStatement.getExtensionChildStatements().forEach(extensionStatement -> {
            final String moduleNameAndExtensionName = getModuleNameAndExtensionName(extensionStatement);
            if (moduleNameAndExtensionNameOfExtensionsUnderRefine.contains(moduleNameAndExtensionName)) {
                Helper.addGeneralInfoAppData(refinedStatement, "previous extension statement " + extensionStatement
                        .getDomElement().getNameValue() + " removed as it has been refined by 'uses'.");
                extensionsUnderRefinedStatementToRemove.add(extensionStatement);
            }
        });

        /*
         * And now simply remove from the refined statement the replaced extensions, and add all
         * the extension statements that sit under the refine statement.
         */
        refinedStatement.removeChildren(extensionsUnderRefinedStatementToRemove);
        refinedStatement.addChildren(extensionsUnderRefine);
    }

    /**
     * Given an extension, returns a concatenation of the name of the module owning the
     * extension definition, and the name of the extension.
     */
    private static String getModuleNameAndExtensionName(final ExtensionStatement extensionStatement) {

        final String extensionModulePrefix = extensionStatement.getExtensionModulePrefix();
        final String extensionStatementName = extensionStatement.getExtensionStatementName();

        final ModuleIdentity owningModuleModuleIdentity = extensionStatement.getPrefixResolver().getModuleForPrefix(
                extensionModulePrefix);
        if (owningModuleModuleIdentity == null) {
            return null;
        }

        return owningModuleModuleIdentity.getModuleName() + ":::" + extensionStatementName;
    }

    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_MANDATORY = new HashSet<>(Arrays.asList(CY.LEAF,
            CY.ANYDATA, CY.ANYXML, CY.CHOICE));
    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_DEFAULT = new HashSet<>(Arrays.asList(CY.LEAF,
            CY.LEAF_LIST, CY.CHOICE));
    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_PRESENCE = new HashSet<>(Arrays.asList(CY.CONTAINER));
    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_MUST = new HashSet<>(Arrays.asList(CY.LEAF, CY.LEAF_LIST,
            CY.LIST, CY.CONTAINER, CY.ANYDATA, CY.ANYXML));
    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_MIN_MAX_ELEMENTS = new HashSet<>(Arrays.asList(
            CY.LEAF_LIST, CY.LIST));
    private static final Set<String> ALLOWABLE_ELEMENTS_FOR_REFINE_IF_FEATURE = new HashSet<>(Arrays.asList(CY.LEAF,
            CY.LEAF_LIST, CY.LIST, CY.CONTAINER, CY.CHOICE, CY.CASE, CY.ANYDATA, CY.ANYXML));

    private static void refineYangStatements(final ParserExecutionContext context, final AbstractStatement refinedStatement,
            final YUses uses, final YRefine refine) {
        /*
         * We refine the contents of the group, if so required. For this, we simply grab the 'refine' statement, and
         * apply its content to whatever schema node it should be applied to. Note that the 'refine' statement hangs
         * under the 'uses' statement.
         *
         * The RFC is pretty clear about what statements are "replaced" and "added", see 7.13.2.
         */
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getDescription(), null);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getReference(), null);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getConfig(), null);
        refineReplaceChildren(context, uses, refine, refinedStatement, refine.getDefaults(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_DEFAULT);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getMandatory(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_MANDATORY);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getPresence(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_PRESENCE);
        refineAddChildren(context, uses, refine, refinedStatement, refine.getMusts(), ALLOWABLE_ELEMENTS_FOR_REFINE_MUST);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getMinElements(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_MIN_MAX_ELEMENTS);
        refineReplaceChild(context, uses, refine, refinedStatement, refine.getMaxElements(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_MIN_MAX_ELEMENTS);
        refineAddChildren(context, uses, refine, refinedStatement, refine.getIfFeatures(),
                ALLOWABLE_ELEMENTS_FOR_REFINE_IF_FEATURE);
    }

    private static void refineReplaceChild(final ParserExecutionContext context, final YUses uses, final YRefine refine,
            final AbstractStatement refinedStatement, final AbstractStatement statementUnderRefine,
            final Set<String> allowableElementsAsRefinedStatement) {

        if (statementUnderRefine == null) {
            return;
        }

        refineReplaceChildren(context, uses, refine, refinedStatement, Collections.singletonList(statementUnderRefine),
                allowableElementsAsRefinedStatement);
    }

    private static <T extends AbstractStatement> void refineReplaceChildren(final ParserExecutionContext context,
            final YUses uses, final YRefine refine, final AbstractStatement refinedStatement,
            final List<T> statementsUnderRefine, final Set<String> allowableElementsAsRefinedStatement) {

        if (statementsUnderRefine.isEmpty()) {
            return;
        }

        if (allowableElementsAsRefinedStatement != null && !allowableElementsAsRefinedStatement.contains(refinedStatement
                .getDomElement().getName())) {
            /*
             * We only issue a finding on the first occurrence to prevent spamming of findings.
             */
            context.addFinding(new Finding(uses.getParentStatement(), ParserFindingType.P124_INVALID_REFINE_TARGET_NODE,
                    "Statement '" + statementsUnderRefine.get(0)
                            .getStatementName() + "' cannot be used to refine a '" + refinedStatement
                                    .getStatementName() + "'."));
            return;
        }

        /*
         * Special case: The 'refine' statement allows for multiple instances of 'default' underneath, but that would
         * only be allowed if the refined schema node is a leaf-list. Otherwise there can only be a single instance
         * of default (leaf, choice). Same with the reverse, of course.
         */
        if (statementsUnderRefine.get(0).is(CY.STMT_DEFAULT)) {
            final int nrDefaults = statementsUnderRefine.size();

            if ((refinedStatement.is(CY.STMT_LEAF) || refinedStatement.is(CY.STMT_CHOICE)) && nrDefaults > 1) {
                /*
                 * Note the finding gets issued on the *second* occurrence of 'default' (the first is correct!)
                 */
                context.addFinding(new Finding(uses.getParentStatement(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "There can only be a single instance of 'default' under " + refine.getDomElement()
                                .getNameValue() + " as the refine's target node is a leaf or choice."));
                return;
            }
        }

        /*
         * Replace all existing instances of the statements, and keep a note of it.
         */
        for (final AbstractStatement childOfRefinedStatement : refinedStatement.getChildren(statementsUnderRefine.get(0)
                .getStatementModuleAndName())) {
            Helper.addGeneralInfoAppData(refinedStatement, "previous statement " + childOfRefinedStatement.getDomElement()
                    .getNameValue() + " removed as it has been refined by 'uses'.");
        }

        for (final AbstractStatement refineWithStatement : statementsUnderRefine) {
            Helper.addGeneralInfoAppData(refineWithStatement, "refines previous statement(s).");
        }

        refinedStatement.replaceChildrenWith(statementsUnderRefine);
    }

    private static <T extends AbstractStatement> void refineAddChildren(final ParserExecutionContext context,
            final YUses uses, final YRefine refine, final AbstractStatement refinedStatement,
            final List<T> statementsUnderRefine, final Set<String> allowableElementsAsRefinedStatement) {

        if (statementsUnderRefine.isEmpty()) {
            return;
        }

        if (allowableElementsAsRefinedStatement != null && !allowableElementsAsRefinedStatement.contains(refinedStatement
                .getDomElement().getName())) {
            /*
             * We only issue a finding on the first occurrence to prevent spamming of findings.
             */
            context.addFinding(new Finding(uses.getParentStatement(), ParserFindingType.P124_INVALID_REFINE_TARGET_NODE,
                    "Statement '" + statementsUnderRefine.get(0)
                            .getStatementName() + "' cannot be used to refine a '" + refinedStatement
                                    .getStatementName() + "'."));
            return;
        }

        /*
         * Simply add the statements.
         */
        for (final AbstractStatement refineWithStatement : statementsUnderRefine) {
            Helper.addGeneralInfoAppData(refineWithStatement, "refines previous statement(s).");
        }

        refinedStatement.addChildren(statementsUnderRefine);
    }

    /**
     * Returns all 'uses' statements that should be considered. In effect, all 'uses'
     * statements that (still) sit in the tree and which have not been ruled out to be unresolvable.
     */
    @SuppressWarnings("unchecked")
    private static List<YUses> findUsesToConsider(final Schema schema) {
        final List<YUses> allUses = (List<YUses>) Helper.findStatementsInSchema(CY.STMT_USES, schema);
        return allUses.stream().filter(yUses -> !isUsesNotResolvable(yUses)).collect(Collectors.toList());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String USES_RESOLUTION_INFO = "USES_RESOLUTION_INFO";

    private static void addUsesResolutionAppData(final AbstractStatement statement, final String info) {
        Helper.addAppDataListInfo(statement, USES_RESOLUTION_INFO, info);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String GROUPING_USAGE_COUNT = "GROUPING_USAGE_COUNT";

    private static void incGroupingUsageCount(final YGrouping grouping) {
        final Integer usageCount = grouping.getCustomAppData(GROUPING_USAGE_COUNT);
        if (usageCount == null) {
            grouping.setCustomAppData(GROUPING_USAGE_COUNT, Integer.valueOf(1));
        } else {
            grouping.setCustomAppData(GROUPING_USAGE_COUNT, Integer.valueOf(usageCount.intValue() + 1));
        }
    }

    private static int getGroupingUsageCount(final YGrouping grouping) {
        final Integer usageCount = grouping.getCustomAppData(GROUPING_USAGE_COUNT);
        return usageCount == null ? 0 : usageCount.intValue();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String USES_NOT_RESOLVABLE = "USES_NOT_RESOLVABLE";

    private static void setUsesNotResolvable(final YUses yUses) {
        yUses.setCustomAppData(USES_NOT_RESOLVABLE);
    }

    private static boolean isUsesNotResolvable(final YUses yUses) {
        return yUses.hasCustomAppData(USES_NOT_RESOLVABLE);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String GROUPING_REFERENCE = "GROUPING_REFERENCE";

    private static void addGroupingReference(final AbstractStatement statement, final YGrouping origGrouping) {
        Helper.addAppDataListInfo(statement, GROUPING_REFERENCE, origGrouping);
    }

    public static List<YGrouping> getGroupingReference(final AbstractStatement statement) {
        return Helper.getAppDataListInfo(statement, GROUPING_REFERENCE);
    }
}
