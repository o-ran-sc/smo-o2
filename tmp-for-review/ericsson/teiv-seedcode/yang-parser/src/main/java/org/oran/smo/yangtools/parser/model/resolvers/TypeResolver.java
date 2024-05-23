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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YBit;
import org.oran.smo.yangtools.parser.model.statements.yang.YDefault;
import org.oran.smo.yangtools.parser.model.statements.yang.YEnum;
import org.oran.smo.yangtools.parser.model.statements.yang.YLength;
import org.oran.smo.yangtools.parser.model.statements.yang.YPosition;
import org.oran.smo.yangtools.parser.model.statements.yang.YRange;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.statements.yang.YTypedef;
import org.oran.smo.yangtools.parser.model.statements.yang.YValue;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper.YangDataType;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

/**
 * A class that can resolve usages of derived types to their base type.
 * <p/>
 * This class will correctly handle nested type resolution - that means, can handle a typedef
 * referring to another typedef (which can and does happen in reality).
 *
 * @author Mark Hollmann
 */
public abstract class TypeResolver {

    /**
     * Resolving usages of derived types means that all places where a "type" statement is used, and the type
     * referred-to is not one of the build-in types, that usage is replaced with the actual base type.
     */
    public static void resolveUsagesOfDerivedTypes(final ParserExecutionContext context, final Schema schema) {

        /*
         * Get all occurrences of the "type" statement. For each statement, check whether the type refers
         * to an in-build type - if not, it refers to a type defined within a "typedef". The "typedef"
         * itself will contain a "type" statement, that can likewise be an in-built type, or a derived type.
         *
         * The easiest way of doing this is to iterate a number of times over all the "type" statements, and
         * only to resolve them if the underlying typedef refers to a base type. We guard against infinite loops.
         */

        int iterationCount = 10;
        boolean atLeastOneResolved = true;

        while (iterationCount > 0 && atLeastOneResolved) {

            atLeastOneResolved = false;
            iterationCount--;

            /*
             * It is correct that the list of "type" statements is fetched every time here, and not once outside
             * the while-loop. The reason is that otherwise we would simply keep doing the same merge/replace 10 times,
             * and also replaced "type" statements are detached from the tree, so no need to do these again.
             */
            final List<YType> types = findTypeStatementsToConsider(schema);
            for (final YType type : types) {
                try {
                    atLeastOneResolved |= resolveTypeToBaseType(context, schema, type);
                } catch (final Exception ex) {
                    /* Swallow and move to next. Best effort here, keep trying other types. */
                }
            }

            if (iterationCount == 7) {
                final List<YType> typesWithExcessiveTypedefDepth = findTypeStatementsToConsider(schema);
                for (final YType type : typesWithExcessiveTypedefDepth) {
                    context.addFinding(new Finding(type, ParserFindingType.P112_EXCESSIVE_TYPEDEF_DEPTH,
                            "Statement refers to 'typedef' with nesting depth > 3."));
                    break;
                }
            }
        }

        /*
         * Done resolving. If some type statements are left with derived types they could not
         * be resolved because of circular dependencies.
         */
        final List<YType> stillUnresolvedTypeStatements = findTypeStatementsToConsider(schema);
        stillUnresolvedTypeStatements.forEach(yType -> context.addFinding(new Finding(yType,
                ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES,
                "Likely circular references between 'type' and 'typedef'. Use the quoted file and line number as starting point for investigation.")));

        /*
         * Perform a check to see which typedef statements have not been used or only used
         * once and create findings for these (possibly poor modeling).
         */
        @SuppressWarnings("unchecked") final List<YTypedef> allTypedefs = (List<YTypedef>) Helper.findStatementsInSchema(
                CY.STMT_TYPEDEF, schema);
        for (final YTypedef oneTypedef : allTypedefs) {
            final int used = getTypedefUsageCount(oneTypedef);
            if (used == 0) {
                context.addFinding(new Finding(oneTypedef, ParserFindingType.P114_TYPEDEF_NOT_USED,
                        "typedef statement '" + oneTypedef.getTypedefName() + "' not used."));
            } else if (used == 1) {
                context.addFinding(new Finding(oneTypedef, ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY,
                        "typedef statement '" + oneTypedef.getTypedefName() + "' used only once; consider inlining."));
            }
        }

        /*
         * It is possible that the replacement of typedefs has resulted in union-within-union.
         * This will be resolved so that there is only a single union.
         */
        atLeastOneResolved = true;

        while (atLeastOneResolved) {
            atLeastOneResolved = false;

            @SuppressWarnings("unchecked") final List<YType> allTypes = (List<YType>) Helper.findStatementsInSchema(
                    CY.STMT_TYPE, schema);
            for (final YType type : allTypes) {
                atLeastOneResolved |= resolveUnionInUnion(context, type);
            }
        }
    }

    /**
     * Resolves usage of a derived type to its underlying type.
     *
     * 1.) Find the actual base type (basically the sought typedef)
     * 2.) Clone the complete tree that hangs under the "type", under the found "typedef". The clone is necessary as
     * it may be used again by some other type or typedef, so we can't just shift the tree but must clone it.
     * 3.) Merge together the statements that hang under the original "type", and the cloned "type". Certain override
     * rules apply here.
     * 4.) Replace the original "type" with the clone that has been updated.
     *
     * Note that the cloned tree keeps its reference to its original prefix-resolver. This is really important as the
     * prefixes between the original YAML and the YAML in which the typedef is can be different!
     *
     * There is a small issue, however: We have to ensure that the resolution is done backwards, i.e. always only
     * resolve if the typedef itself points to a base type. Otherwise we run into problems with modules and prefixes.
     */
    private static boolean resolveTypeToBaseType(final ParserExecutionContext context, final Schema schema,
            final YType typeUsingDerivedType) {

        final AbstractStatement parentOfTypeStatement = typeUsingDerivedType.getParentStatement();

        final String derivedDataType = typeUsingDerivedType.getDataType();
        if (derivedDataType.isEmpty()) {
            /*
             * Pointless trying to resolve the data type. No point issuing a finding either, a
             * P015_INVALID_SYNTAX_IN_DOCUMENT would have been issued already.
             */
            setDerivedTypeNotResolvable(typeUsingDerivedType);
            return false;
        }

        /*
         * We look for the typedef. This could sit further up the tree, at the top of the module, in an included submodule.
         */
        final YTypedef typedef = Helper.findStatement(context, schema, typeUsingDerivedType, CY.STMT_TYPEDEF,
                derivedDataType);
        if (typedef == null) {
            setDerivedTypeNotResolvable(typeUsingDerivedType);
            context.addFinding(new Finding(typeUsingDerivedType, ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE,
                    "Cannot resolve typedef '" + typeUsingDerivedType.getDataType() + "'."));
            return false;
        }

        /*
         * Mark the typedef has been used.
         */
        increaseUsageCount(typedef);

        /*
         * Check for nested derived type usage, and if found resolve those first.
         */
        if (derivedTypeUsedWithinFoundTypedef(context, typeUsingDerivedType, typedef)) {
            return false;
        }

        /*
         * We have found the typedef, and the type within it is a build-in type. We attach some meta data to it first
         * to keep track of where it came from. Downstream tooling may find this information useful, as quite often
         * derived types have special semantics, and the resolving of these to their base type loses these semantics.
         */
        addOriginallyDefinedInYam(typedef.getType(), typedef.getYangModelRoot().getModuleOrSubModuleName());

        /*
         * The typedef may be used multiple times so we can't simply re-parent the YType statement tree - we must clone it.
         */
        final YType clonedTypeUnderTypedef = new YType(typedef, typedef.getType().getDomElement());
        clonedTypeUnderTypedef.cloneFrom(typedef.getType());

        /*
         * Type restrictions can be applied to various sub-statements of the type, which always must make
         * the allowable value set more restrictive (or be the same). We check for these here. See section
         * 9.4 in the RFC, and the paragraphs in the various sub-statement that discuss restrictions.
         */
        checkAndRestrictPatterns(context, typeUsingDerivedType, clonedTypeUnderTypedef);
        checkAndRestrictLength(context, typeUsingDerivedType, clonedTypeUnderTypedef);
        checkAndRestrictRange(context, typeUsingDerivedType, clonedTypeUnderTypedef);
        checkAndRestrictBits(context, typeUsingDerivedType, clonedTypeUnderTypedef);
        checkAndRestrictEnumeration(context, typeUsingDerivedType, clonedTypeUnderTypedef);

        /*
         * A possible default value under the typedef will be copied over to the leaf /
         * leaf-list / typedef, but only if that does not always have a default value. See
         * section 7.3.4 and 7.6.1 in the RFC.
         */
        if (typedef.getDefault() != null) {
            assignDefaultFromTypedefToWhereItIsUsed(typedef, typeUsingDerivedType);
        }

        /*
         * We are now removing the original 'type' statement (that referred to the derived type) and are
         * replacing it with the cloned and updated 'type' statement of the 'typedef'. That will then
         * also clean up the tree underneath the typedef where temporarily we had two 'type' statements.
         * Note that the replacement must be done such to keep the order of statements in the statement tree.
         * This is required to handle nested unions correctly.
         */
        parentOfTypeStatement.replaceChildInPlace(typeUsingDerivedType, clonedTypeUnderTypedef);

        /*
         * Keep a record of the reference for later analysis.
         */
        addTypedefReference(clonedTypeUnderTypedef, typedef);

        /*
         * If there is a 'range' statement under the type now we need to re-validate. Same with
         * 'length' statement.
         */
        if (clonedTypeUnderTypedef.getRange() != null) {
            clonedTypeUnderTypedef.getRange().validateBoundaries(context);
        }
        if (clonedTypeUnderTypedef.getLength() != null) {
            clonedTypeUnderTypedef.getLength().validateBoundaries(context);
        }

        return true;
    }

    private static boolean derivedTypeUsedWithinFoundTypedef(final ParserExecutionContext context,
            final YType typeUsingDerivedType, final YTypedef typedef) {

        final List<YType> typesUnderFoundTypedef = getTypesFromUnderTypedef(typedef);

        boolean hasTypeThatIsNotResolveable = false;
        boolean hasTypeThatIsDerived = false;

        for (final YType typeInTypedef : typesUnderFoundTypedef) {

            if (isDerivedTypeNotResolvable(typeInTypedef)) {

                hasTypeThatIsNotResolveable = true;
                context.addFinding(new Finding(typeUsingDerivedType,
                        ParserFindingType.P116_NESTED_DERIVED_TYPE_NOT_RESOLVABLE,
                        "Referenced typedef '" + typeUsingDerivedType
                                .getDataType() + "' has nested unresolvable 'type' statement " + typeInTypedef
                                        .getDomElement().getNameValue() + "."));
            }

            if (DataTypeHelper.isDerivedType(typeInTypedef.getDataType())) {
                hasTypeThatIsDerived = true;
            }
        }

        if (hasTypeThatIsNotResolveable) {
            /*
             * If the typedef that this type refers to has a 'type' that cannot be resolved,
             * then this 'type' here likewise cannot be resolved.
             */
            setDerivedTypeNotResolvable(typeUsingDerivedType);
        }

        /*
         * The typedef itself references another typedef, so resolve the other
         * typedef first before attempting this one here.
         */
        return hasTypeThatIsDerived;
    }

    private static List<YType> getTypesFromUnderTypedef(final YTypedef typedef) {
        final List<YType> result = new ArrayList<>();

        result.add(typedef.getType());

        for (final YType unionMember : typedef.getType().getTypes()) {
            result.add(unionMember);
        }

        return result;
    }

    /**
     * A typedef can have a default value. If the derived type does not have a default, such a default must be copied over.
     */
    private static void assignDefaultFromTypedefToWhereItIsUsed(final YTypedef typedef, final YType originalTypeStatement) {

        AbstractStatement parent = originalTypeStatement.getParentStatement();

        if (parent.is(CY.STMT_TYPE)) {
            /*
             * type-under-type, i.e. union.
             */
            parent = parent.getParentStatement();
        }

        /*
         * Safety check so we don't end up adding a default value to a statement that
         * doesn't support default as child statement.
         */
        if (parent.is(CY.STMT_LEAF) || parent.is(CY.STMT_LEAF_LIST) || parent.is(CY.STMT_TYPEDEF)) {
            if (parent.hasAtLeastOneChildOf(CY.STMT_DEFAULT)) {
                /*
                 * The parent statement of where the derived type is used has already a default value, so
                 * the default value that hangs under the found typedef should be ignored.
                 */
            } else {
                /*
                 * We simple clone the default value from the found typedef under where the type is
                 * being used.
                 */
                final YDefault defaultUnderFoundTypedef = typedef.getDefault();
                final YDefault clonedDefault = new YDefault(parent, defaultUnderFoundTypedef.getDomElement());
                clonedDefault.cloneFrom(defaultUnderFoundTypedef);
            }
        }
    }

    private static void checkAndRestrictPatterns(final ParserExecutionContext context, final YType typeUsingTypedef,
            final YType clonedTypeUnderTypedef) {

        if (typeUsingTypedef.getPatterns().isEmpty()) {
            /*
             * There is no 'pattern' under the type that uses the derived type; nothing to do.
             */
            return;
        }

        if (!DataTypeHelper.isStringType(clonedTypeUnderTypedef.getDataType())) {
            context.addFinding(new Finding(typeUsingTypedef.getParentStatement(),
                    ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                    "Base type of derived type is not 'string'; hence cannot use 'pattern' as substatement to restrict the type."));
            return;
        }

        /*
         * Patterns work slightly different compared to length or range. It is possible to specify multiple
         * patterns as part of a type, and they must logically ALL (not ANY) be true. This means that the
         * patterns from the derived type and the using-type are simply added together. So all we have to do
         * is move any pattern from the using-type into the cloned derived type.
         */
        clonedTypeUnderTypedef.addChildren(typeUsingTypedef.getPatterns());
    }

    private static void checkAndRestrictLength(final ParserExecutionContext context, final YType typeUsingTypedef,
            final YType clonedTypeUnderTypedef) {

        if (typeUsingTypedef.getLength() == null) {
            /*
             * There is no 'length' under the type that uses the derived type, hence there is no restriction
             * currently, hence no need to check if any length is more restrictive or the same.
             */
            return;
        }

        if (!DataTypeHelper.isStringType(clonedTypeUnderTypedef.getDataType()) && !DataTypeHelper.isBinaryType(
                clonedTypeUnderTypedef.getDataType())) {
            context.addFinding(new Finding(typeUsingTypedef.getParentStatement(),
                    ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                    "Base type of derived type is not 'string' or 'binary'; hence cannot use 'length' as substatement to restrict the type."));
            return;
        }

        if (clonedTypeUnderTypedef.getLength() != null) {
            /*
             * A 'length' has been defined as part of the derived type. We must check that the length defined
             * on the using type is more restrictive, or at least equal.
             *
             * To do this, we make sure that all boundaries defined by the using type fit into any (single)
             * boundary defined by the derived type. For example, this is here is fine:
             *
             * Derived type: [0-10],[30-35],[80-99]
             * Using type:   [0-5],[30-31],[33-35]
             *
             * Conversely, this here is not OK:
             *
             * Derived type: [0-10],[30-35],[80-99]
             * Using type:   [0-20],[30-90
             */
            final List<YLength.BoundaryPair> derivedTypeBoundaries = clonedTypeUnderTypedef.getLength().getBoundaries();
            final List<YLength.BoundaryPair> usingTypeBoundaries = typeUsingTypedef.getLength().getBoundaries();

            for (final YLength.BoundaryPair oneUsingTypeBoundary : usingTypeBoundaries) {
                if (!fitsIntoLengthBoundary(oneUsingTypeBoundary, derivedTypeBoundaries)) {
                    context.addFinding(new Finding(typeUsingTypedef.getLength(),
                            ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                            "When using a derived type and specifying 'length', the allowed length can only become more restrictive, not wider."));
                }
            }
        }

        /*
         * We replace any 'length' from under the cloned type, and replace it with the length under the
         * type that uses the typedef. That's fine, as the cloned type will in a moment replace the
         * type that uses the typedef.
         */
        clonedTypeUnderTypedef.replaceChildrenWith(typeUsingTypedef.getLength());
    }

    private static boolean fitsIntoLengthBoundary(final YLength.BoundaryPair boundaryToCheck,
            final List<YLength.BoundaryPair> allowedBoundaries) {

        final long lowerToCheck = boundaryToCheck.lower;
        final long upperToCheck = boundaryToCheck.upper;

        for (final YLength.BoundaryPair oneAllowedBoundary : allowedBoundaries) {
            if (oneAllowedBoundary.lower <= lowerToCheck && oneAllowedBoundary.upper >= upperToCheck) {
                return true;
            }
        }

        return false;
    }

    private static void checkAndRestrictRange(final ParserExecutionContext context, final YType typeUsingTypedef,
            final YType clonedTypeUnderTypedef) {

        if (typeUsingTypedef.getRange() == null) {
            /*
             * There is no 'range' under the type that uses the typedef, hence
             * there is no restriction, hence nothing to check or do.
             */
            return;
        }

        if (!DataTypeHelper.isYangNumericType(clonedTypeUnderTypedef.getDataType())) {
            context.addFinding(new Finding(typeUsingTypedef.getParentStatement(),
                    ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                    "Base type of derived type is not a numeric type; hence cannot use 'range' as substatement to restrict the type."));
            return;
        }

        if (clonedTypeUnderTypedef.getRange() != null) {
            /*
             * Logic is very similar to how 'length' is handled, so see comments further above...
             */
            final List<YRange.BoundaryPair> derivedTypeBoundaries = clonedTypeUnderTypedef.getRange().getBoundaries();
            final List<YRange.BoundaryPair> usingTypeBoundaries = typeUsingTypedef.getRange().getBoundaries(
                    clonedTypeUnderTypedef);

            for (final YRange.BoundaryPair oneUsingTypeBoundary : usingTypeBoundaries) {
                if (!fitsIntoRangeBoundary(oneUsingTypeBoundary, derivedTypeBoundaries)) {
                    context.addFinding(new Finding(typeUsingTypedef.getRange(),
                            ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                            "When using a derived type and specifying 'range', the allowed range can only become more restrictive, not wider."));
                }
            }
        }

        /*
         * We replace any 'range' from under the cloned type, and replace it with the range under the
         * type that uses the typedef. That's fine, as the cloned type will in a moment replace the
         * type that uses the typedef.
         */
        clonedTypeUnderTypedef.replaceChildrenWith(typeUsingTypedef.getRange());
    }

    private static boolean fitsIntoRangeBoundary(final YRange.BoundaryPair boundaryToCheck,
            final List<YRange.BoundaryPair> allowedBoundaries) {

        final BigDecimal lowerToCheck = boundaryToCheck.lower;
        final BigDecimal upperToCheck = boundaryToCheck.upper;

        for (final YRange.BoundaryPair oneAllowedBoundary : allowedBoundaries) {
            if (oneAllowedBoundary.lower.compareTo(lowerToCheck) <= 0 && oneAllowedBoundary.upper.compareTo(
                    upperToCheck) >= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * A 'bits' data type has been restricted. The restricted bits must be a subset of the original
     * type, and the position values must match up. See chapter 9.7 in the RFC.
     */
    private static void checkAndRestrictBits(final ParserExecutionContext context, final YType typeUsingTypedef,
            final YType clonedTypeUnderTypedef) {

        if (typeUsingTypedef.getBits().isEmpty()) {
            return;
        }

        if (DataTypeHelper.getYangDataType(clonedTypeUnderTypedef.getDataType()) != YangDataType.BITS) {
            context.addFinding(new Finding(typeUsingTypedef.getParentStatement(),
                    ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                    "Base type of derived type is not 'bits'; hence cannot use 'bit' as substatement to restrict the type."));
            return;
        }

        /*
         * We first establish what the positions are (implicit or explicit) for all bits.
         */
        final Map<String, Long> positionOfBitsInDerivedType = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), clonedTypeUnderTypedef, null);

        /*
         * If there is a bit-restriction, the RFC states that the bit under the type-using-typdef will
         * retain all position values if the bit is listed. In other words, the position does not have
         * to be explicitly supplied. This will cause significant issues in a moment when we perform the
         * restriction. We there fore explicitly create position statements to overcome this.
         */
        typeUsingTypedef.getBits().forEach(yBit -> {

            if (yBit.getPosition() == null) {

                final Long positionOfBit = positionOfBitsInDerivedType.get(yBit.getBitName());
                /*
                 * We create a new DOM node and 'position' statement on-the-fly under the 'bit' element.
                 */
                final YangDomElement artificialPositionDomElement = new YangDomElement(CY.POSITION, positionOfBit == null ?
                        "0" :
                        positionOfBit.toString(), yBit.getDomElement(), yBit.getDomElement().getLineNumber());
                new YPosition(yBit, artificialPositionDomElement);
            }
        });

        /*
         * Now that the 'bit' statements under the type-using-typedef all have a 'position', we can compare
         * these. According to RFC, the restriction must be a sub-set of the bits defined in the typedef
         * and the position values must match up.
         */
        final Map<String, Long> positionOfBitsInTypeUsingTypedef = DataTypeHelper.calculatePositionOfBits(context
                .getFindingsManager(), typeUsingTypedef, null);

        for (final YBit bit : typeUsingTypedef.getBits()) {
            final String bitName = bit.getBitName();

            if (!positionOfBitsInDerivedType.containsKey(bitName)) {
                context.addFinding(new Finding(bit, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                        "Bit '" + bitName + "' does not exist in derived type."));
            } else {

                final long bitPositionInTypeUsingTypedef = positionOfBitsInTypeUsingTypedef.get(bitName).longValue();
                if (positionOfBitsInDerivedType.get(bitName).longValue() != bitPositionInTypeUsingTypedef) {
                    context.addFinding(new Finding(bit, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                            "'position' mismatch for bit '" + bitName + "'. In derived type: '" + positionOfBitsInDerivedType
                                    .get(bitName) + "'; in type using the derived type: '" + bitPositionInTypeUsingTypedef + "'."));
                }
            }
        }

        /*
         * And eventually replace the bits statements (due to this replace, we had
         * to create artificial 'position' statements further above).
         */
        clonedTypeUnderTypedef.replaceChildrenWith(typeUsingTypedef.getBits());
    }

    /**
     * Does the exact same as the previous method, only for enumerations.
     */
    private static void checkAndRestrictEnumeration(final ParserExecutionContext context, final YType typeUsingTypedef,
            final YType clonedDerivedType) {

        if (typeUsingTypedef.getEnums().isEmpty()) {
            return;
        }

        if (DataTypeHelper.getYangDataType(clonedDerivedType.getDataType()) != YangDataType.ENUMERATION) {
            context.addFinding(new Finding(typeUsingTypedef.getParentStatement(),
                    ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                    "Base type of derived type is not 'enumeration'; hence cannot use 'enum' as substatement to restrict the type."));
            return;
        }

        final Map<String, Long> valueOfEnumsInDerivedType = DataTypeHelper.calculateValuesOfEnums(context
                .getFindingsManager(), clonedDerivedType, null);

        /*
         * If there is a enum-restriction, the RFC states that the enum under the type-using-typdef will
         * retain all 'value' values if the enum is listed. In other words, the value does not have
         * to be explicitly supplied. This will cause significant issues in a moment when we perform the
         * restriction. We there fore explicitly create value statements to overcome this.
         */
        typeUsingTypedef.getEnums().forEach(yEnum -> {

            if (yEnum.getValue() == null) {

                final Long valueOfEnum = valueOfEnumsInDerivedType.get(yEnum.getEnumName());
                /*
                 * We create a new DOM node and 'value' statement on-the-fly under the 'enum' element.
                 */
                final YangDomElement artificialPositionDomElement = new YangDomElement(CY.VALUE, valueOfEnum == null ?
                        "0" :
                        valueOfEnum.toString(), yEnum.getDomElement(), yEnum.getDomElement().getLineNumber());
                new YValue(yEnum, artificialPositionDomElement);
            }
        });

        final Map<String, Long> valueOfEnumsInTypeUsingTypedef = DataTypeHelper.calculateValuesOfEnums(context
                .getFindingsManager(), typeUsingTypedef, null);

        /*
         * According to RFC, the restriction must be a sub-set of the enums defined in the typedef - and the values must match up (or be omitted).
         */
        for (final YEnum oneEnum : typeUsingTypedef.getEnums()) {

            final String enumName = oneEnum.getEnumName();

            if (!valueOfEnumsInDerivedType.containsKey(enumName)) {
                context.addFinding(new Finding(oneEnum, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                        "Enum '" + enumName + "' does not exist in derived type."));
            } else {

                final long enumValueInTypeUsingTypedef = valueOfEnumsInTypeUsingTypedef.get(enumName).longValue();

                if (valueOfEnumsInDerivedType.get(enumName).longValue() != enumValueInTypeUsingTypedef) {
                    context.addFinding(new Finding(oneEnum, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION,
                            "'value' mismatch for enum '" + enumName + "'. In derived type: '" + valueOfEnumsInDerivedType
                                    .get(enumName) + "'; in type using the derived type: '" + enumValueInTypeUsingTypedef + "'."));
                }
            }
        }

        /*
         * And eventually replace the enum statements.
         */
        clonedDerivedType.replaceChildrenWith(typeUsingTypedef.getEnums());
    }

    /**
     * Returns all 'type' statements that should be considered. In effect, all 'type' statements that
     * are not build-in YANG types and which have not been ruled out to be un-resolvable.
     */
    @SuppressWarnings("unchecked")
    private static List<YType> findTypeStatementsToConsider(final Schema schema) {

        final List<YType> yTypes = (List<YType>) Helper.findStatementsInSchema(CY.STMT_TYPE, schema);

        return yTypes.stream().filter(yType -> !DataTypeHelper.isBuiltInType(yType.getDataType())).filter(
                yType -> !isDerivedTypeNotResolvable(yType)).collect(Collectors.toList());
    }

    /**
     * Resolves any usages of union-in-union
     */
    private static boolean resolveUnionInUnion(final ParserExecutionContext context, final YType yType) {

        /*
         * This type has to be a union, and it must have sub-types also of union, for this to fire.
         */
        if (!DataTypeHelper.isUnionType(yType.getDataType())) {
            return false;
        }

        final boolean hasChildUnion = yType.getTypes().stream().filter(childType -> DataTypeHelper.isUnionType(childType
                .getDataType())).findAny().isPresent();
        if (!hasChildUnion) {
            return false;
        }

        /*
         * The sub-unions need to be resolved. There is a complication here in respect of the order of
         * union statements. Basically, the general order must be maintained. Consider this input:
         *
         * type union {
         *      type int16;
         *      type union {
         *           type string;
         *           type enumeration;
         *      }
         *      type uint64;
         * }
         *
         * ... then the resolved union should look like this:
         *
         * type union {
         *      type int16;
         *      type string;
         *      type enumeration;
         *      type uint64;
         * }
         *
         * This means we have to be careful with the order of the type
         * statements when re-arranging these.
         */

        final List<YType> collectedTypeStatements = new ArrayList<>();
        yType.getTypes().forEach(childType -> {
            if (DataTypeHelper.isUnionType(childType.getDataType())) {
                /*
                 * A union, so we need to add the children type statements of the union type.
                 */
                collectedTypeStatements.addAll(childType.getTypes());
            } else {
                /*
                 * Not a union, so simple add to the collected types
                 */
                collectedTypeStatements.add(childType);
            }
        });

        /*
         * And now simply replace the original children with the collected types. That will effectively
         * remove the (now resolved) child-union from the statement tree.
         */
        yType.replaceChildrenWith(collectedTypeStatements);

        return true;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String DERIVED_TYPE_NOT_RESOLVABLE = "DERIVED_TYPE_NOT_RESOLVABLE";

    private static void setDerivedTypeNotResolvable(final YType yType) {
        yType.setCustomAppData(DERIVED_TYPE_NOT_RESOLVABLE);
    }

    private static boolean isDerivedTypeNotResolvable(final YType yType) {
        return yType.hasCustomAppData(DERIVED_TYPE_NOT_RESOLVABLE);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String TYPEDEF_USAGE_COUNT = "TYPEDEF_USAGE_COUNT";

    private static int getTypedefUsageCount(final YTypedef typedef) {
        final Integer usageCount = typedef.getCustomAppData(TYPEDEF_USAGE_COUNT);
        return usageCount == null ? 0 : usageCount.intValue();
    }

    private static void increaseUsageCount(final YTypedef yTypedef) {
        final Integer usageCount = yTypedef.getCustomAppData(TYPEDEF_USAGE_COUNT);
        if (usageCount == null) {
            yTypedef.setCustomAppData(TYPEDEF_USAGE_COUNT, Integer.valueOf(1));
        } else {
            yTypedef.setCustomAppData(TYPEDEF_USAGE_COUNT, Integer.valueOf(usageCount.intValue() + 1));
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String TYPEDEF_REFERENCE = "TYPEDEF_REFERENCE";
    private static final String TYPEDEF_STACK = "TYPEDEF_STACK";

    private static void addTypedefReference(final YType clonedType, final YTypedef origTypedef) {
        Helper.addAppDataListInfo(clonedType, TYPEDEF_REFERENCE, origTypedef);

        /*
         * We keep track of the stack of typedefs. We only do this if the typedef is at the root
         * of the YAM, otherwise it is "private" and can't be used outside of the module.
         */
        final AbstractStatement parentOfOrigTypedef = origTypedef.getParentStatement();

        if (parentOfOrigTypedef.is(CY.STMT_MODULE) || parentOfOrigTypedef.is(CY.STMT_SUBMODULE)) {
            final String moduleName = parentOfOrigTypedef.getYangModelRoot().getOwningYangModelRoot().getModule()
                    .getModuleName();
            final String namespace = parentOfOrigTypedef.getYangModelRoot().getNamespace();
            final NamespaceModuleIdentifier nsmi = new NamespaceModuleIdentifier(namespace, moduleName, origTypedef
                    .getTypedefName());

            Helper.addAppDataListInfo(clonedType, TYPEDEF_STACK, nsmi);
        }
    }

    public static List<YTypedef> getTypedefReference(final YType yType) {
        return Helper.getAppDataListInfo(yType, TYPEDEF_REFERENCE);
    }

    /**
     * Returns 0..n entries denoting the chain of typedefs that were resolved for the supplied type.
     * Index [0] denotes the typedef that defines the original type.
     *
     * Note that if the typedef is declared on a submodule, the name of its owning module will be
     * returned. To avoid confusion, it is recommended to use the namespace when looking for a
     * particular module.
     */
    public static List<NamespaceModuleIdentifier> getTypedefStack(final YType yType) {
        return Helper.getAppDataListInfo(yType, TYPEDEF_STACK);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static final String ORIGINALLY_DEFINED_IN_YAM = "ORIGINALLY_DEFINED_IN_YAM";

    private static void addOriginallyDefinedInYam(final YType yType, final String yamName) {
        yType.setCustomAppData(ORIGINALLY_DEFINED_IN_YAM, yamName);
    }

    /**
     * Returns the name of the YAM where this type was originally defined. May return null in
     * which case the type did not refer to a derived type.
     */
    public static String getOriginallyDefinedInYamName(final YType yType) {
        return yType.getCustomAppData(ORIGINALLY_DEFINED_IN_YAM);
    }
}
