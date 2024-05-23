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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementFactory;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement.MaxCardinality;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviate;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviate.DeviateType;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviation;
import org.oran.smo.yangtools.parser.model.statements.yang.YMaxElements;
import org.oran.smo.yangtools.parser.model.statements.yang.YMinElements;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.util.StringHelper;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Resolves 'deviation' statements.
 *
 * @author Mark Hollmann
 */
public abstract class DeviationResolver {

    /*
     * A note on how properties and statements are handled. Some properties for data nodes *always* exist,
     * even though their corresponding statements may not be listed in the YAM. For example, a data node
     * will always have a 'config' property, which may or may not be explicitly set by means of a statement
     * in the YAM. Technically, those properties cannot be ADDED or DELETED vi a deviate, only REPLACED.
     * This is not clear in the spec; but was clarified as part of a discussion on the netmod mailing list:
     * <p/>
     * "I think that config, mandatory, type, max-elements, min-elements cannot be added or
     *  deleted, only replaced, because they always exist."
     * <p/>
     * However, most YAM designers are not aware of this, and will use a deviate add/remove for these
     * properties. To avoid noise we are lenient in the code and will allow any of deviate
     * add/replace/remove for these properties.
     */
    private static final Set<StatementModuleAndName> PROPERTIES_THAT_ALWAYS_EXIST = new HashSet<>(Arrays.asList(
            CY.STMT_CONFIG, CY.STMT_MANDATORY, CY.STMT_TYPE, CY.STMT_MAX_ELEMENTS, CY.STMT_MIN_ELEMENTS));

    /**
     * Resolves all deviates by applying the deviate operation against the respective target nodes.
     */
    public static void resolveDeviates(final ParserExecutionContext context, final Schema schema) {

        @SuppressWarnings("unchecked") final List<YDeviation> allDeviations = (List<YDeviation>) Helper
                .findStatementsAtModuleRootInSchema(CY.STMT_DEVIATION, schema);

        /*
         * This is done in a certain order, to facilitate multiple deviations affecting
         * each other. E.g., a deviation may replace a property added by another deviation.
         */
        for (final YDeviation deviation : allDeviations) {
            resolveDeviation(context, deviation, YDeviate.DeviateType.ADD, schema);
        }
        for (final YDeviation deviation : allDeviations) {
            resolveDeviation(context, deviation, YDeviate.DeviateType.REPLACE, schema);
        }
        for (final YDeviation deviation : allDeviations) {
            resolveDeviation(context, deviation, YDeviate.DeviateType.DELETE, schema);
        }
        for (final YDeviation deviation : allDeviations) {
            resolveDeviation(context, deviation, YDeviate.DeviateType.NOT_SUPPORTED, schema);
        }
    }

    private static void resolveDeviation(final ParserExecutionContext context, final YDeviation deviation,
            final DeviateType deviateTypeToProcess, final Schema schema) {

        final String deviationTargetNode = deviation.getDeviationTargetNode();
        if (deviationTargetNode.isEmpty() || !deviationTargetNode.startsWith("/")) {
            /*
             * Pointless trying to resolve the path. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
             */
            return;
        }

        final AbstractStatement deviationTarget = findTargetSchemaNode(context, deviation, schema);
        if (deviationTarget == null) {
            /*
             * It is possible that the deviation is trying to remove parts of the tree that
             * have already been removed by a different deviation. If that's the case, we
             * simply continue.
             */
            if (deviateTypeToProcess == DeviateType.NOT_SUPPORTED && deviationRefersToPreviouslyRemovedSchemaNode(context,
                    deviation, schema)) {
                /* do nothing */
            } else {
                context.addFinding(new Finding(deviation, ParserFindingType.P054_UNRESOLVABLE_PATH,
                        "Path to schema node '" + deviation
                                .getDeviationTargetNode() + "', part of 'deviation' statement, cannot be resolved."));
            }

            return;
        }

        for (final YDeviate deviate : deviation.getDeviates()) {
            if (deviateTypeToProcess == deviate.getDeviateType()) {
                handleDeviate(context, deviate, deviationTarget);
            }
        }
    }

    private static void handleDeviate(final ParserExecutionContext context, final YDeviate deviate,
            final AbstractStatement deviationTarget) {

        if (deviate.getDeviateType() == DeviateType.ADD || deviate.getDeviateType() == DeviateType.REPLACE) {
            /*
             * Run a check first to see whether the statements underneath the 'deviate add/replace' are
             * actually allowed as children of the statement that is the target of the deviation. The RFC
             * says that handling of extensions is not defined; we assume that any extension listed under
             * a deviate add/replace is meant to be handled such.
             */
            final List<String> statementsAllowedAsChild = deviationTarget.getStatementsAllowedAsChild();
            boolean illegalChildFound = false;

            for (final AbstractStatement childOfDeviate : deviate.getChildStatements()) {
                if (!childOfDeviate.isExtension() && !statementsAllowedAsChild.contains(childOfDeviate.getDomElement()
                        .getName())) {
                    illegalChildFound = true;
                    context.addFinding(new Finding(childOfDeviate, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT,
                            "Statement '" + childOfDeviate
                                    .getStatementName() + "' not a valid child statement of deviated statement '" + deviationTarget
                                            .getStatementName()));
                }
                if (childOfDeviate.isExtension() && !((ExtensionStatement) childOfDeviate).canBeChildOf(deviationTarget
                        .getStatementModuleAndName())) {
                    illegalChildFound = true;
                    context.addFinding(new Finding(childOfDeviate, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT,
                            "Extension '" + childOfDeviate
                                    .getStatementName() + "' not a valid child statement of deviated statement '" + deviationTarget
                                            .getStatementName()));
                }
            }

            if (illegalChildFound) {
                addDeviateInfo(deviate, "'deviate' has not been applied due to findings.");
                return;
            }
        }

        /*
         * Now manipulate the tree in accordance with the deviate operation.
         */
        switch (deviate.getDeviateType()) {
            case NOT_SUPPORTED:
                handleDeviateNotSupported(deviationTarget, deviate);
                break;
            case ADD:
                handleDeviateAdd(context, deviationTarget, deviate);
                break;
            case DELETE:
                handleDeviateDelete(context, deviationTarget, deviate);
                break;
            case REPLACE:
                handleDeviateReplace(context, deviationTarget, deviate);
                break;
        }

        addDeviateInfo(deviate, "'deviate' has been applied.");
    }

    private static void handleDeviateDelete(final ParserExecutionContext context, final AbstractStatement deviationTarget,
            final YDeviate deviate) {
        /*
         * From the RFC:
         *
         * The argument "delete" deletes properties from the target node. The
         * properties to delete are identified by substatements to the "delete"
         * statement. The substatement’s keyword MUST match a corresponding
         * keyword in the target node, and the argument’s string MUST be equal
         * to the corresponding keyword’s argument string in the target node.
         */

        /*
         * So we need to get the child statements of the "deviate delete" statement,
         * and try to match these up against the child statements of the deviated statement.
         * "Match up" means the statement type must be the exact same, and the statements
         * value must be the exact same.
         */
        for (final AbstractStatement childOfDeviate : deviate.getChildStatements()) {
            deleteStatementFromUnderDeviatedStatement(context, deviationTarget, childOfDeviate, deviate);
        }
    }

    private static void deleteStatementFromUnderDeviatedStatement(final ParserExecutionContext context,
            final AbstractStatement deviationTarget, final AbstractStatement statementToDelete, final YDeviate deviate) {

        boolean matchFound = false;
        final List<? extends AbstractStatement> statementsOfTypeUnderDeviationTarget = deviationTarget.getChildren(
                statementToDelete.getStatementModuleAndName());

        for (final AbstractStatement childOfDeviationTarget : statementsOfTypeUnderDeviationTarget) {
            if (statementValuesAreSame(statementToDelete, childOfDeviationTarget)) {
                /*
                 * We do a check first to see how many instances of the statement would be left after we delete the statement.
                 * If 0, and the statement is mandatory, that's a finding. We ignore extensions, as extensions (by definition)
                 * can never be mandatory.
                 */
                final int newOccurenceCount = statementsOfTypeUnderDeviationTarget.size() - 1;
                if (newOccurenceCount == 0 && !statementToDelete.isExtension()) {
                    final String statementName = statementToDelete.getDomElement().getName();
                    if (deviationTarget.getMandatorySingleChildStatementNames().contains(statementName) || deviationTarget
                            .getMandatoryMultipleChildStatementNames().contains(statementName)) {
                        context.addFinding(new Finding(statementToDelete,
                                ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION,
                                "Cannot 'deviate delete' this statement as at least a single '" + statementToDelete
                                        .getStatementName() + "' statement is required under deviated statement '" + deviationTarget
                                                .getStatementName() + "'."));
                        return;
                    }
                }

                if (statementHasBeenAddedInByDeviateAdd(childOfDeviationTarget)) {
                    context.addFinding(new Finding(statementToDelete,
                            ParserFindingType.P165_DEVIATE_DELETE_OF_DEVIATED_STATEMENT, "Deletes a '" + statementToDelete
                                    .getStatementName() + "' statement that was previously added by a separate 'deviate add' operation."));
                } else if (statementHasBeenAddedInByDeviateReplace(childOfDeviationTarget)) {
                    context.addFinding(new Finding(statementToDelete,
                            ParserFindingType.P165_DEVIATE_DELETE_OF_DEVIATED_STATEMENT, "Deletes a '" + statementToDelete
                                    .getStatementName() + "' statement that was previously replaced by a separate 'deviate replace' operation."));
                }

                addDeviationHistory(deviationTarget, DeviationHistory.delete(childOfDeviationTarget, statementToDelete));
                addDeviateInfo(deviationTarget, "statement " + childOfDeviationTarget.getDomElement()
                        .getNameValue() + " in " + StringHelper.getModuleLineString(
                                childOfDeviationTarget) + " deleted by 'deviate delete' in " + StringHelper
                                        .getModuleLineString(statementToDelete));

                deviationTarget.removeChild(childOfDeviationTarget);

                matchFound = true;
                break;
            }
        }

        if (!matchFound) {
            context.addFinding(new Finding(statementToDelete, ParserFindingType.P161_INVALID_DEVIATE_OPERATION,
                    "Cannot 'deviate delete' this statement as the statement does not exist under the deviated statement."));
        }
    }

    private static boolean statementValuesAreSame(final AbstractStatement statement1, final AbstractStatement statement2) {
        final String value1 = statement1.getDomElement().getValue();
        final String value2 = statement2.getDomElement().getValue();
        return Objects.equals(value1, value2);
    }

    private static void handleDeviateAdd(final ParserExecutionContext context, final AbstractStatement deviationTarget,
            final YDeviate deviate) {
        /*
         * From the RFC:
         *
         * "The argument "add" adds properties to the target node. The
         * properties to add are identified by substatements to the "deviate"
         * statement. If a property can only appear once, the property MUST NOT
         * exist in the target node."
         */

        /*
         * The following can only exists once, hence MUST NOT already exist under the deviated schema node.
         */
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_CONFIG);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MANDATORY);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MAX_ELEMENTS);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MIN_ELEMENTS);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_TYPE);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_UNITS);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_DEFAULT);
        /*
         * The following can exist more than once, hence there can be statements under the deviated statement already.
         */
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MUST);
        addUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_UNIQUE);

        /*
         * Also make sure to deviate-in extensions. No special handling applies to these, as the
         * RFC stipulates this to be extension-specific.
         */
        handleDeviateAddForExtensions(context, deviationTarget, deviate);
    }

    private static void handleDeviateAddForExtensions(final ParserExecutionContext context,
            final AbstractStatement deviatedStatement, final YDeviate deviate) {
        /*
         * For extensions we don't know upfront what the exact module/statement names are.
         * We must figure these out first, before we start adding these:
         */
        final Set<StatementModuleAndName> extensionModulesAndNames = deviate.getExtensionChildStatements().stream().map(
                ext -> ext.getStatementModuleAndName()).collect(Collectors.toSet());

        extensionModulesAndNames.forEach(sman -> addUnderDeviatedStatement(context, deviatedStatement, deviate, sman));
    }

    /**
     * Given a statement type, verifies that the deviated statement does not already have as child this
     * statement, and assuming that this is not the case, adds it / them.
     * <p>
     * Note: Will gracefully handle a 'deviate add' of properties, i.e. allow these, although according
     * to RFC these should only ever be replaced.
     */
    private static <T extends AbstractStatement> void addUnderDeviatedStatement(final ParserExecutionContext context,
            final AbstractStatement deviationTarget, final YDeviate deviate, final StatementModuleAndName soughtChildType) {

        /*
         * Get the instances of the statement type.
         */
        final List<T> statementsOfTypeUnderDeviate = deviate.getChildren(soughtChildType);
        final List<T> statementsOfTypeUnderTargetStatement = deviationTarget.getChildren(soughtChildType);

        if (statementsOfTypeUnderDeviate.isEmpty()) {
            return;
        }

        /*
         * Figure out whether there can be multiple instances of the statement under the target.
         */
        final AbstractStatement oneChildUnderDeviate = deviate.getChild(soughtChildType);
        boolean canHaveMultiple;

        if (oneChildUnderDeviate.isExtension()) {
            canHaveMultiple = ((ExtensionStatement) oneChildUnderDeviate)
                    .getMaxCardinalityUnderParent() == MaxCardinality.MULTIPLE;
        } else {
            final String coreStatementName = soughtChildType.getStatementName();
            canHaveMultiple = deviationTarget.getMandatoryMultipleChildStatementNames().contains(
                    coreStatementName) || deviationTarget.getOptionalMultipleChildStatementNames().contains(
                            coreStatementName);
        }

        if (!statementsOfTypeUnderTargetStatement.isEmpty() && !canHaveMultiple) {
            /*
             * A statement of this type already exists under the target, and there cannot be more than one.
             */
            for (final AbstractStatement statementUnderDeviate : statementsOfTypeUnderDeviate) {
                context.addFinding(new Finding(statementUnderDeviate, ParserFindingType.P161_INVALID_DEVIATE_OPERATION,
                        "Cannot 'deviate add' statement '" + statementUnderDeviate
                                .getStatementName() + "' as it already exists under the deviated statement. Use a 'deviate replace' instead."));
            }
            return;
        }

        /*
         * Check whether the max cardinality would be violated by adding the statements.
         */
        final int newCount = statementsOfTypeUnderDeviate.size() + statementsOfTypeUnderTargetStatement.size();
        if (newCount > 1 && !canHaveMultiple) {
            context.addFinding(new Finding(statementsOfTypeUnderDeviate.get(0),
                    ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION,
                    "Cannot 'deviate add' this statement as at most one '" + statementsOfTypeUnderDeviate.get(0)
                            .getStatementName() + "' statement is allowed under under '" + deviationTarget
                                    .getStatementName() + "'."));
            return;
        }

        /*
         * Good - either a statement of this type does not already exist under the deviation target, or
         * this fact doesn't matter. Then simply clone it so that it ends up under the deviated statement.
         *
         * Really, cloning is not necessary - we could simply move the statement. However, if there is
         * downstream tooling that visualizes the schema it would be nice to retain the original content
         * of the deviation so that a user can see it. Also useful for debugging.
         */
        for (final T statementUnderDeviate : statementsOfTypeUnderDeviate) {
            final T clonedStatementPlacedUnderDeviationTarget = StatementFactory.cloneYangStatement(statementUnderDeviate,
                    deviationTarget);
            clonedStatementPlacedUnderDeviationTarget.cloneFrom(statementUnderDeviate);

            addDeviationHistory(clonedStatementPlacedUnderDeviationTarget, DeviationHistory.add(statementUnderDeviate));
            addDeviateInfo(clonedStatementPlacedUnderDeviationTarget,
                    "statement added by 'deviate add' in module " + StringHelper.getModuleLineString(
                            statementUnderDeviate));
        }
    }

    private static void handleDeviateReplace(final ParserExecutionContext context, final AbstractStatement deviationTarget,
            final YDeviate deviate) {
        /*
         * From the RFC:
         *
         * The argument "replace" replaces properties of the target node. The
         * properties to replace are identified by substatements to the
         * "deviate" statement. The properties to replace MUST exist in the
         * target node.
         */

        /*
         * Before we do the actual replacement, we check for shrinking of boundaries.
         */
        checkForNarrowedMinMaxElements(context, deviationTarget.getChild(CY.STMT_MIN_ELEMENTS), deviate.getMinElements(),
                deviationTarget.getChild(CY.STMT_MAX_ELEMENTS), deviate.getMaxElements());
        /*
         * ...and check for data type replacements...
         */
        checkForReplacedDataType(context, deviationTarget.getChild(CY.STMT_TYPE), deviate.getType());

        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_CONFIG);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_DEFAULT);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MANDATORY);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MAX_ELEMENTS);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MIN_ELEMENTS);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_MUST);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_TYPE);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_UNIQUE);
        replaceUnderDeviatedStatement(context, deviationTarget, deviate, CY.STMT_UNITS);

        handleReplaceDeviateForExtensions(context, deviationTarget, deviate);
    }

    private static void handleReplaceDeviateForExtensions(final ParserExecutionContext context,
            final AbstractStatement deviatedStatement, final YDeviate deviate) {
        /*
         * For extensions we don't know upfront what the exact module/statement names are.
         * We must figure these out first, before we start replacing these:
         */
        final Set<StatementModuleAndName> extensionModulesAndNames = deviate.getExtensionChildStatements().stream().map(
                ext -> ext.getStatementModuleAndName()).collect(Collectors.toSet());

        extensionModulesAndNames.forEach(sman -> replaceUnderDeviatedStatement(context, deviatedStatement, deviate, sman));
    }

    private static void checkForReplacedDataType(final ParserExecutionContext context, final YType oldType,
            final YType newType) {

        if (newType == null) {
            return;
        }

        final Set<String> oldDataTypes = new HashSet<>();
        final Set<String> newDataTypes = new HashSet<>();

        collectDataTypes(oldDataTypes, oldType);
        collectDataTypes(newDataTypes, newType);

        if (!oldDataTypes.equals(newDataTypes)) {
            context.addFinding(new Finding(newType, ParserFindingType.P057_DATA_TYPE_CHANGED,
                    "Data type has changed from " + oldDataTypes.toString() + " to " + newDataTypes.toString() + "."));
        }
    }

    private static void collectDataTypes(final Set<String> dataTypes, final YType type) {
        if (type.getDataType().equals("union")) {
            for (final YType unionMemberType : type.getTypes()) {
                collectDataTypes(dataTypes, unionMemberType);
            }
        } else {
            dataTypes.add(type.getDataType());
        }
    }

    private static void checkForNarrowedMinMaxElements(final ParserExecutionContext context,
            final YMinElements oldMinElements, final YMinElements newMinElements, final YMaxElements oldMaxElements,
            final YMaxElements newMaxElements) {

        final long oldMinValue = oldMinElements == null ? 0L : oldMinElements.getMinValue();
        final long newMinValue = newMinElements == null ? 0L : newMinElements.getMinValue();

        if (newMinValue > oldMinValue) {
            context.addFinding(new Finding(newMinElements, ParserFindingType.P056_CONSTRAINT_NARROWED,
                    "Replacement raises min-elements boundary from '" + oldMinValue + "' to '" + newMinValue + "'."));
        }

        final boolean oldMaxIsUnbounded = oldMaxElements == null ? true : oldMaxElements.isUnbounded();
        final boolean newMaxIsUnbounded = newMaxElements == null ? true : newMaxElements.isUnbounded();
        final long oldMaxValue = oldMaxIsUnbounded ? 0L : oldMaxElements.getMaxValue();
        final long newMaxValue = newMaxIsUnbounded ? 0L : newMaxElements.getMaxValue();

        if (oldMaxIsUnbounded && !newMaxIsUnbounded) {
            context.addFinding(new Finding(newMaxElements, ParserFindingType.P056_CONSTRAINT_NARROWED,
                    "Replacement lowers max-elements boundary from 'unbounded' to '" + newMaxValue + "'."));
        } else if (!oldMaxIsUnbounded && !newMaxIsUnbounded && newMaxValue < oldMaxValue) {
            context.addFinding(new Finding(newMaxElements, ParserFindingType.P056_CONSTRAINT_NARROWED,
                    "Replacement lowers max-elements boundary from '" + oldMaxValue + "' to '" + newMaxValue + "'."));
        }
    }

    /**
     * Given a statement type, verifies that the deviated statement *does* have as child this
     * statement, and assuming that this is the case, removes the original child and replaces
     * it with the deviate's child. Note: exception is made for properties that *always* exist.
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractStatement> void replaceUnderDeviatedStatement(final ParserExecutionContext context,
            final AbstractStatement deviationTarget, final YDeviate deviate, final StatementModuleAndName soughtChildType) {

        /*
         * Establish whether an instance of the supplied class exists under the "deviate" and/or under the deviated statement.
         */
        final List<T> statementsOfTypeUnderDeviate = deviate.getChildren(soughtChildType);
        final List<T> statementsOfTypeUnderDeviationTarget = deviationTarget.getChildren(soughtChildType);

        if (statementsOfTypeUnderDeviate.isEmpty()) {
            return;
        }

        final StatementModuleAndName statementModuleAndName = statementsOfTypeUnderDeviate.get(0)
                .getStatementModuleAndName();

        if (statementsOfTypeUnderDeviationTarget.isEmpty() && !PROPERTIES_THAT_ALWAYS_EXIST.contains(
                statementModuleAndName)) {
            context.addFinding(new Finding(statementsOfTypeUnderDeviate.get(0),
                    ParserFindingType.P161_INVALID_DEVIATE_OPERATION,
                    "Cannot 'deviate replace' statement '" + statementsOfTypeUnderDeviate.get(0)
                            .getStatementName() + "' as it does not exist under the deviated statement."));
            return;
        }

        /*
         * Check whether cardinality would be violated by doing the replace.
         */
        final int newOccurencesOfStatementType = statementsOfTypeUnderDeviate.size();
        if (statementModuleAndName.isYangCoreStatement() && newOccurencesOfStatementType > 1) {
            if (!deviationTarget.getMandatoryMultipleChildStatementNames().contains(statementModuleAndName
                    .getStatementName()) && !deviationTarget.getOptionalMultipleChildStatementNames().contains(
                            statementModuleAndName.getStatementName())) {
                context.addFinding(new Finding(statementsOfTypeUnderDeviate.get(0),
                        ParserFindingType.P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION,
                        "Cannot 'deviate replace' this statement as at most one '" + statementModuleAndName
                                .getStatementName() + "' statement is allowed under '" + deviationTarget
                                        .getStatementName() + "'."));
                return;
            }
        }

        /*
         * Exists in both, so remove the original statement(s), and add the statements under deviate. We do a
         * quick check first, though, to see whether the statements that are being replaced likewise have been
         * deviated-in previously.
         */
        boolean replacingPreviouslyReplaced = false;
        boolean replacingPreviouslyAdded = false;

        for (final T oneReplacedStatement : statementsOfTypeUnderDeviationTarget) {
            if (statementHasBeenAddedInByDeviateReplace(oneReplacedStatement)) {
                replacingPreviouslyReplaced = true;
            } else if (statementHasBeenAddedInByDeviateAdd(oneReplacedStatement)) {
                replacingPreviouslyAdded = true;
            }

            addDeviateInfo(deviationTarget, "Previous statement/property " + oneReplacedStatement.getDomElement()
                    .getNameValue() + " in " + StringHelper.getModuleLineString(
                            deviationTarget) + " replaced by 'deviate replace' in " + StringHelper.getModuleLineString(
                                    deviate));
        }

        deviationTarget.removeChildren(statementsOfTypeUnderDeviationTarget);

        for (final T statementUnderDeviate : statementsOfTypeUnderDeviate) {

            if (replacingPreviouslyReplaced) {
                context.addFinding(new Finding(statementUnderDeviate,
                        ParserFindingType.P163_AMBIGUOUS_DEVIATE_REPLACE_OF_SAME_STATEMENT,
                        "Replaces another '" + statementUnderDeviate
                                .getStatementName() + "' statement that has replaced the original statement by a separate 'deviate replace' operation."));
            } else if (replacingPreviouslyAdded) {
                context.addFinding(new Finding(statementUnderDeviate,
                        ParserFindingType.P164_DEVIATE_REPLACE_OF_DEVIATE_ADDED_STATEMENT,
                        "Replaces another '" + statementUnderDeviate
                                .getStatementName() + "' statement that has been previously added by a separate 'deviate add' operation."));
            }

            final T clonedStatementPlacedUnderDeviationTarget = StatementFactory.cloneYangStatement(statementUnderDeviate,
                    deviationTarget);
            clonedStatementPlacedUnderDeviationTarget.cloneFrom(statementUnderDeviate);

            addDeviationHistory(clonedStatementPlacedUnderDeviationTarget, DeviationHistory.replace(
                    (List<AbstractStatement>) statementsOfTypeUnderDeviationTarget, statementUnderDeviate));
            addDeviateInfo(clonedStatementPlacedUnderDeviationTarget,
                    "Replacement for previous statement or (possibly implicit) property, replaced by 'deviate replace' in " + StringHelper
                            .getModuleLineString(statementUnderDeviate));
        }
    }

    private static void handleDeviateNotSupported(final AbstractStatement deviationTarget, final YDeviate deviate) {

        /*
         * We add some additional information to the parent that will help the user to make sense of
         * what has happened. We also keep a record of the statement that we have removed. This is useful
         * later on if there is another statement referring to the deleted statement, because then we can
         * be smart about it and not issue findings.
         */
        addDeviationHistory(deviationTarget.getParentStatement(), DeviationHistory.notSupported(deviationTarget, deviate));
        addDeviateInfo(deviationTarget.getParentStatement(), "Statement " + deviationTarget.getDomElement()
                .getNameValue() + " in " + StringHelper.getModuleLineString(
                        deviationTarget) + " marked as 'not-supported' by deviation in " + StringHelper.getModuleLineString(
                                deviate) + " and thus removed.");

        /*
         * That's easy - the schema node (the found statement) is not supported, so simply remove it.
         */
        deviationTarget.getParentStatement().removeChild(deviationTarget);
    }

    /**
     * Finds the schema node that is being deviated.
     */
    private static AbstractStatement findTargetSchemaNode(final ParserExecutionContext context, final YDeviation deviation,
            final Schema schema) {

        final AbstractStatement targetSchemaNode = Helper.findSchemaNode(context, deviation, deviation
                .getDeviationTargetNode(), schema);
        if (targetSchemaNode == null) {
            return null;
        }

        /*
         * Check whether they both sit inside the very same module
         */
        if (deviation.getDomElement().getYangModel() == targetSchemaNode.getDomElement().getYangModel()) {
            context.addFinding(new Finding(deviation, ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE,
                    "Both 'deviation' and it's target node sit in the same (sub-)module."));
        }

        return targetSchemaNode;
    }

    /**
     * We try to figure out whether a 'deviate not-supported' refers to something in the
     * tree that has previously been removed. That's a bit involved, but well possible...
     */
    private static boolean deviationRefersToPreviouslyRemovedSchemaNode(final ParserExecutionContext context,
            final YDeviation deviation, final Schema schema) {

        final String deviationTargetNode = deviation.getDeviationTargetNode();
        if (deviationTargetNode.isEmpty() || !deviationTargetNode.startsWith("/")) {
            /*
             * Pointless trying to resolve the path. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
             */
            return false;
        }

        /*
         * We split the path, and get the starting point for navigation.
         */
        final String[] identifierParts = Helper.getIdentifierParts(true, deviationTargetNode);
        AbstractStatement traversalStatement = Helper.findStartingSchemaNode(context, deviation, true, identifierParts[0],
                schema);
        if (traversalStatement == null) {
            return false;
        }

        /*
         * Go down the tree until we get stuck...
         */
        for (final String possiblyPrefixedIdentifier : identifierParts) {
            final AbstractStatement foundChildStatement = Helper.findChildSchemaNode(context, traversalStatement,
                    possiblyPrefixedIdentifier, deviation);
            if (foundChildStatement == null) {
                /*
                 * OK, so this is the point where we could not follow the path anymore. We extract the
                 * identifier that we were actually looking for, and check whether such an identifier
                 * has been marked as previously not-supported.
                 */
                final String unprefixedIdentifierSought = QNameHelper.extractName(possiblyPrefixedIdentifier);

                final List<DeviationHistory> removedStatementsHistory = getDeviationHistory(traversalStatement).stream()
                        .filter(dh -> dh.deviateType == DeviateType.NOT_SUPPORTED).collect(Collectors.toList());
                for (final DeviationHistory history : removedStatementsHistory) {
                    if (unprefixedIdentifierSought.equals(history.deviatedStatements.get(0).getStatementIdentifier())) {
                        return true;
                    }
                }

                /*
                 * There is nothing in the history, so truly not found.
                 */
                return false;
            }

            traversalStatement = foundChildStatement;
        }

        /*
         * We really should never ever be getting here.
         */
        return false;
    }

    private static final String DEVIATION_INFO = "DEVIATION_INFO";

    private static void addDeviateInfo(final AbstractStatement statement, final String info) {
        Helper.addAppDataListInfo(statement, DEVIATION_INFO, info);
    }

    public static List<String> getDeviationInfosForStatement(final AbstractStatement statement) {
        return Helper.getAppDataListInfo(statement, DEVIATION_INFO);
    }

    private static final String DEVIATION_HISTORY = "DEVIATION_HISTORY";

    private static void addDeviationHistory(final AbstractStatement onStatement, final DeviationHistory deviationHistory) {
        Helper.addAppDataListInfo(onStatement, DEVIATION_HISTORY, deviationHistory);
    }

    public static List<DeviationHistory> getDeviationHistory(final AbstractStatement onStatement) {
        return Helper.getAppDataListInfo(onStatement, DEVIATION_HISTORY);
    }

    private static boolean statementHasBeenAddedInByDeviateAdd(final AbstractStatement statement) {
        final List<DeviationHistory> deviationHistory = getDeviationHistory(statement);
        return deviationHistory.size() > 0 && deviationHistory.get(0).deviateType == DeviateType.ADD;
    }

    private static boolean statementHasBeenAddedInByDeviateReplace(final AbstractStatement statement) {
        final List<DeviationHistory> deviationHistory = getDeviationHistory(statement);
        return deviationHistory.size() > 0 && deviationHistory.get(0).deviateType == DeviateType.REPLACE;
    }

    public static class DeviationHistory {

        static DeviationHistory notSupported(final AbstractStatement notSupportedChild, final YDeviate yDeviate) {
            return new DeviationHistory(YDeviate.DeviateType.NOT_SUPPORTED, Collections.singletonList(notSupportedChild),
                    yDeviate);
        }

        static DeviationHistory delete(final AbstractStatement deletedChild, final AbstractStatement deviatingStatement) {
            return new DeviationHistory(YDeviate.DeviateType.DELETE, Collections.singletonList(deletedChild),
                    deviatingStatement);
        }

        static DeviationHistory add(final AbstractStatement deviatingStatement) {
            return new DeviationHistory(YDeviate.DeviateType.ADD, Collections.<AbstractStatement> emptyList(),
                    deviatingStatement);
        }

        static DeviationHistory replace(final List<AbstractStatement> replacedStatements,
                final AbstractStatement deviatingStatement) {
            return new DeviationHistory(YDeviate.DeviateType.REPLACE, replacedStatements, deviatingStatement);
        }

        /**
         * The type of deviation applied.
         */
        public final YDeviate.DeviateType deviateType;

        /**
         * The statement that was deviated. This will be:
         * <p>
         * not-supported: The single statement (which will be a data node) that was removed.
         * delete: The single statement (typically a property) that was removed.
         * add: empty, no content.
         * replace: The statements (0..n) that were replaced.
         *
         * that were added, removed, or were changed. If the statements were
         * removed, the statement will not have a parent anymore.
         */
        public final List<AbstractStatement> deviatedStatements;

        /**
         * The statement that performed the deviation, i.e. the 'deviate' statement itself,
         * or a child statement of 'deviate'.
         */
        public final AbstractStatement deviatingStatement;

        private DeviationHistory(final DeviateType deviateType, final List<AbstractStatement> deviatedStatements,
                final AbstractStatement deviatingStatement) {
            this.deviateType = deviateType;
            this.deviatedStatements = deviatedStatements;
            this.deviatingStatement = deviatingStatement;
        }
    }
}
