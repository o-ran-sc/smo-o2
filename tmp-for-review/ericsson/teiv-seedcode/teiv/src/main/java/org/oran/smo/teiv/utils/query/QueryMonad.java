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
package org.oran.smo.teiv.utils.query;

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getDbName;
import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.B_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.ATTRIBUTES;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.oran.smo.teiv.utils.path.TiesPathUtil.getTiesPathQuery;
import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.not;
import static org.jooq.impl.DSL.one;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oran.smo.teiv.schema.RelationshipDataLocation;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.utils.path.TiesPathQuery;
import org.oran.smo.teiv.utils.path.exception.PathParsingException;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;

@Builder
public class QueryMonad {

    private static final String SEMICOLON = ";";
    private static final String BAR = "|";
    private String managedObject;
    private String targets;
    private String scope;
    private String relationships;
    private String domain;

    /**
     * Builds a jooq query using the SchemaRegistry, based on the filters.
     *
     * @return the prepared jooq query
     */
    public Function<DSLContext, ResultQuery<Record>> withSchema(final PaginationDTO paginationDTO) {
        return context -> buildQuery(context, paginationDTO);
    }

    public Function<DSLContext, ResultQuery<Record1<Integer>>> countWithSchema() {
        return this::buildQueryCount;
    }

    private QueryElement constructRoot() {
        List<PathToken> targetTokens = targets != null ? processPath(targets, true) : new ArrayList<>();
        List<PathToken> scopeTokens = scope != null ? processPath(scope, false) : new ArrayList<>();

        return buildQueryTree(managedObject, targetTokens, scopeTokens, domain);
    }

    private ResultQuery<Record> buildQuery(DSLContext context, final PaginationDTO paginationDTO) {
        List<String> relationshipTypes = parseRelationships(relationships);

        return buildJooqQuery(context, constructRoot(), relationshipTypes, paginationDTO);
    }

    private SelectConditionStep<Record1<Integer>> buildQueryCount(DSLContext context) {
        List<String> relationshipTypes = parseRelationships(relationships);

        return buildJooqQueryCount(context, constructRoot(), relationshipTypes);
    }

    private static Select<Record> buildInnerQuery(DSLContext context, QueryElement root, List<String> relationships) {
        SelectSelectStep<Record> selectStep = buildSelectStep(context, root, root.getObjectType());

        boolean[] isValidHop = { false };
        Select<Record> query = buildFromStep(selectStep, root, relationships, isValidHop, root.getObjectType());
        Condition joinFilter = null;

        for (QueryElement child : root.getChildren()) {
            if (root.getObjectType() != null && !SchemaRegistry.hasDirectHopBetween(root.getObjectType(), child
                    .getObjectType(), relationships)) {
                continue;
            }

            joinFilter = setJoinFilter(root, joinFilter, child);
        }

        for (QueryElement child : root.getChildren()) {
            if (root.getObjectType() == null || SchemaRegistry.hasDirectHopBetween(root.getObjectType(), child
                    .getObjectType())) {
                query = query.union(buildUnionBody(context, root, child.getObjectType(), relationships, joinFilter,
                        isValidHop));
            }
        }

        buildJooqQueryCheckIfRootHasConnections(root, isValidHop[0]);

        query = buildWhereStep(query, root, joinFilter, relationships);

        return query;
    }

    private static SelectConditionStep<Record1<Integer>> buildJooqQueryCount(DSLContext context, QueryElement root,
            List<String> relationships) {

        Select<Record> query = buildInnerQuery(context, root, relationships);

        return context.selectCount().from(query.asTable("TiesPathQuery")).where(not(field("\"TiesPathQuery\"").isNull()));
    }

    private static SelectJoinStep<Record> buildJooqQuery(DSLContext context, QueryElement root, List<String> relationships,
            final PaginationDTO paginationDTO) {

        Select<Record> query = buildInnerQuery(context, root, relationships);

        return (SelectJoinStep<Record>) context.selectDistinct(asterisk()).from(query.asTable("TiesPathQuery")).orderBy(
                one().asc()).limit(paginationDTO.getOffset(), paginationDTO.getLimit());
    }

    private static Condition setJoinFilter(QueryElement root, Condition joinFilter, QueryElement child) {
        if (root.getObjectType() != null || child.getFilters() == null) {
            Field<Boolean> condition = field(String.format(TIES_DATA, getDbName(child.getObjectType())) + "." + String
                    .format(QUOTED_STRING, ID_COLUMN_NAME)).isNotNull();
            if (joinFilter != null && root.getObjectType() != null) {
                joinFilter = joinFilter.and(condition);
            } else if (joinFilter != null) {
                joinFilter = joinFilter.or(condition);
            } else {
                joinFilter = DSL.condition(condition);
            }
        }
        return joinFilter;
    }

    private static void buildJooqQueryCheckIfRootHasConnections(QueryElement root, Boolean isValidHop) {
        if ((root.hasChildren() && !isValidHop && root.getObjectType() != null) || (!root.hasChildren() && !root
                .isIncluded())) {
            throw TiesPathException.noConnectionFound(root.getObjectType());
        } else if ((root.hasChildren() && !isValidHop) || (!root.hasChildren() && !root.isIncluded())) {
            throw TiesPathException.noConnectionFoundWhenRootIsNull();
        }
    }

    private static SelectSelectStep<Record> buildSelectStep(DSLContext context, QueryElement root, String returnObject) {
        List<String> columnNames = getColumnList(root);
        if (returnObject == null) {
            return context.select(columnNames.stream().map(column -> field("null").cast(SchemaRegistry.getEntityTypeByName(
                    getModelledName(column.split("\\.")[1].replaceAll("\"", ""))).getField(column, null, returnObject)
                    .getType()).as(column)).collect(Collectors.toList()));
        }
        if (root.isManyToMany()) {
            return context.select(columnNames.stream().map(DSL::field).toList());
        } else if (root.isRelConnectingSameEntity()) {
            return context.selectDistinct(columnNames.stream().map(DSL::field).toList());
        } else {
            return context.select(columnNames.stream().map(column -> column.split("\\.")[1].replaceAll("\"", "").equals(
                    getDbName(returnObject)) ?
                            SchemaRegistry.getEntityTypeByName(returnObject).getField(column, null, null) :
                            field("null").cast(SchemaRegistry.getEntityTypeByName(getModelledName(column.split("\\.")[1]
                                    .replaceAll("\"", ""))).getField(column, null, returnObject).getType()).as(column))
                    .collect(Collectors.toList()));
        }
    }

    private static SelectSelectStep<Record> buildSelectStepForUnion(DSLContext context, QueryElement root,
            String returnObject) {
        List<String> columnNames = getColumnList(root);
        if (root.getObjectType() != null && root.getObjectType().equals(returnObject)) {
            return context.select(columnNames.stream().map(column -> field("null").cast(SchemaRegistry.getEntityTypeByName(
                    column.split("\\.")[1].replaceAll("\"", "")).getField(column, null, returnObject).getType()).as(column))
                    .collect(Collectors.toList()));
        } else {
            return buildSelectStep(context, root, returnObject);
        }
    }

    private static SelectJoinStep<Record> buildFromStep(SelectSelectStep<Record> selectStep, QueryElement root,
            List<String> relationships, boolean[] isValidHop, String returnObject) {
        if (root.getObjectType() == null) {
            SelectJoinStep<Record> fromStep = selectStep.from("(SELECT '') as dual_table");

            for (QueryElement child : root.getChildren()) {
                if (SchemaRegistry.isEntityPartOfRelationships(child.getObjectType(), relationships)) {
                    TableJoinConnection connection = new TableJoinConnection(B_SIDE, null, List.of(condition(field(String
                            .format(TIES_DATA, getDbName(child.getObjectType())) + "." + ID_COLUMN_NAME).eq(field(
                                    "(SELECT '')")))), false);

                    fromStep = buildFromStepSetJoin(child, returnObject, fromStep, connection);

                    isValidHop[0] = true;
                }
            }
            return fromStep;
        }
        SelectJoinStep<Record> fromStep = selectStep.from(String.format(TIES_DATA, getDbName(root.getObjectType())));
        for (QueryElement child : root.getChildren()) {
            for (TableJoinConnection connection : findJoinConnections(root, child, relationships)) {
                fromStep = getRecords(root, fromStep, child, connection);
                isValidHop[0] = true;
            }
        }
        return fromStep;
    }

    private static SelectJoinStep<Record> getRecords(QueryElement root, SelectJoinStep<Record> fromStep, QueryElement child,
            TableJoinConnection connection) {
        if (connection.relConnectingSameEntity) {
            fromStep = fromStep.join(connection.getTableName()).on(connection.getConditions().get(0)).or(connection
                    .getConditions().get(1));
        } else {
            if (!root.isManyToMany() && connection.getRelationshipDataLocation().equals(RELATION)) {
                fromStep = fromStep.join(connection.getTableName()).on(connection.getConditions().get(1));
            }
            if (!root.isManyToMany()) {
                fromStep = fromStep.leftJoin(String.format(TIES_DATA, getDbName(child.getObjectType()))).on(connection
                        .getConditions().get(0));
            } else {
                fromStep = fromStep.join(String.format(TIES_DATA, getDbName(child.getObjectType()))).on(connection
                        .getConditions().get(0));
            }
        }
        return fromStep;
    }

    private static SelectJoinStep<Record> buildFromStepSetJoin(QueryElement child, String returnObject,
            SelectJoinStep<Record> fromStep, TableJoinConnection connection) {
        if (child.getObjectType().equals(returnObject)) {
            return fromStep.rightJoin(String.format(TIES_DATA, getDbName(child.getObjectType()))).on(connection
                    .getConditions().get(0));
        } else {
            return fromStep.leftJoin(String.format(TIES_DATA, getDbName(child.getObjectType()))).on(connection
                    .getConditions().get(0));
        }
    }

    private static List<TableJoinConnection> findJoinConnections(QueryElement qe1, QueryElement qe2,
            List<String> relationships) {
        List<RelationType> relationTypes;
        if (qe1.isManyToMany() || qe1.isRelConnectingSameEntity()) {
            relationTypes = List.of(SchemaRegistry.getRelationTypeByName(qe1.getObjectType()));
        } else {
            relationTypes = SchemaRegistry.getRelationTypesBetweenEntities(qe1.getObjectType(), qe2.getObjectType(),
                    relationships);
        }
        return createJoinConnections(qe1, qe2, relationTypes);
    }

    private static List<TableJoinConnection> createJoinConnections(QueryElement qe1, QueryElement qe2,
            List<RelationType> relationTypes) {
        List<TableJoinConnection> joins = new ArrayList<>();
        for (RelationType relationType : relationTypes) {
            switch (relationType.getRelationshipStorageLocation()) {
                case A_SIDE:
                    joins.add(createASideJoin(relationType));
                    break;
                case B_SIDE:
                    joins.add(createBSideJoin(relationType));
                    break;
                case RELATION:
                    joins.add(createRelationJoin(qe1, qe2, relationType));
                    break;
            }
        }
        return joins;
    }

    private static TableJoinConnection createSameEntityJoin(QueryElement qe1, QueryElement qe2, RelationType relationType) {
        String table = relationType.getTableName() + ".";
        if (!qe1.isRelConnectingSameEntity()) {
            return new TableJoinConnection(relationType.getRelationshipStorageLocation(), relationType.getTableName(), List
                    .of(condition(field(table + String.format(QUOTED_STRING, relationType.bSideColumnName())).eq(field(
                            String.format(TIES_DATA, getDbName(qe2.getObjectType())) + "." + String.format(QUOTED_STRING,
                                    ID_COLUMN_NAME)))), condition(field(table + String.format(QUOTED_STRING, relationType
                                            .aSideColumnName())).eq(field(String.format(TIES_DATA, qe1
                                                    .getObjectType()) + "." + String.format(QUOTED_STRING,
                                                            ID_COLUMN_NAME))))), true);
        } else {
            return new TableJoinConnection(relationType.getRelationshipStorageLocation(), String.format(TIES_DATA,
                    getDbName(qe2.getObjectType())), List.of(condition(field(table + String.format(QUOTED_STRING,
                            relationType.bSideColumnName())).eq(field(String.format(TIES_DATA, getDbName(qe2
                                    .getObjectType())) + "." + String.format(QUOTED_STRING, ID_COLUMN_NAME)))), condition(
                                            field(table + String.format(QUOTED_STRING, relationType.aSideColumnName())).eq(
                                                    field(String.format(TIES_DATA, getDbName(qe2
                                                            .getObjectType())) + "." + String.format(QUOTED_STRING,
                                                                    ID_COLUMN_NAME))))), true);
        }
    }

    private static TableJoinConnection createASideJoin(RelationType relationType) {
        String table = relationType.getTableName() + ".";
        return new TableJoinConnection(relationType.getRelationshipStorageLocation(), null, List.of(condition(field(
                table + String.format(QUOTED_STRING, relationType.bSideColumnName())).eq(field(String.format(TIES_DATA,
                        getDbName(relationType.getBSide().getName())) + "." + String.format(QUOTED_STRING,
                                ID_COLUMN_NAME))))), false);
    }

    private static TableJoinConnection createBSideJoin(RelationType relationType) {
        String table = relationType.getTableName() + ".";
        return new TableJoinConnection(relationType.getRelationshipStorageLocation(), null, List.of(condition(field(
                table + String.format(QUOTED_STRING, relationType.aSideColumnName())).eq(field(String.format(TIES_DATA,
                        getDbName(relationType.getASide().getName())) + "." + String.format(QUOTED_STRING,
                                ID_COLUMN_NAME))))), false);
    }

    private static TableJoinConnection createRelationJoin(QueryElement qe1, QueryElement qe2, RelationType relationType) {
        String table = relationType.getTableName() + ".";
        if (relationType.isConnectsSameEntity()) {
            return createSameEntityJoin(qe1, qe2, relationType);
        } else if (!qe1.isManyToMany()) {
            return new TableJoinConnection(relationType.getRelationshipStorageLocation(), relationType.getTableName(), List
                    .of(condition(field(table + String.format(QUOTED_STRING, relationType.bSideColumnName())).eq(field(
                            String.format(TIES_DATA, getDbName(qe2.getObjectType())) + "." + String.format(QUOTED_STRING,
                                    ID_COLUMN_NAME)))), condition(field(table + String.format(QUOTED_STRING, relationType
                                            .aSideColumnName())).eq(field(String.format(TIES_DATA, qe1
                                                    .getObjectType()) + "." + String.format(QUOTED_STRING,
                                                            ID_COLUMN_NAME))))), false);
        } else {
            String colInManyToManyTable = relationType.getASide().getName().equals(qe2.getObjectType()) ?
                    relationType.aSideColumnName() :
                    relationType.bSideColumnName();
            return new TableJoinConnection(relationType.getRelationshipStorageLocation(), relationType.getTableName(), List
                    .of(condition(field(table + String.format(QUOTED_STRING, colInManyToManyTable)).eq(field(String.format(
                            TIES_DATA, getDbName(qe2.getObjectType())) + "." + String.format(QUOTED_STRING,
                                    ID_COLUMN_NAME))))), false);
        }
    }

    private static Select<Record> buildUnionBody(DSLContext context, QueryElement root, String returnObject,
            List<String> relationships, Condition joinFilter, boolean[] isValidHop) {
        SelectSelectStep<Record> selectStep = buildSelectStepForUnion(context, root, returnObject);
        SelectJoinStep<Record> fromStep = buildFromStep(selectStep, root, relationships, isValidHop, returnObject);
        return buildWhereStep(fromStep, root, joinFilter, relationships);
    }

    private static Select<Record> buildWhereStep(Select<Record> query, QueryElement root, Condition joinFilter,
            List<String> relationships) {
        Condition condition = DSL.noCondition();
        condition = buildWhereAnd(condition, root, relationships);
        for (QueryElement child : root.getChildren()) {
            condition = buildWhereAnd(condition, child, null);
        }
        if (!root.isManyToMany()) {
            condition = buildWhereOr(condition, root);
        }
        for (QueryElement child : root.getChildren()) {
            condition = buildWhereOr(condition, child);
        }
        if (joinFilter != null && root.getObjectType() != null) {
            condition = condition.and(joinFilter);
        } else if (joinFilter != null) {
            condition = condition.or(joinFilter);
        }
        return query.$where(condition);
    }

    private static Condition buildWhereAnd(Condition condition, QueryElement root, List<String> relationships) {
        if (root.hasFilters() && (root.hasFilterOfType(TokenType.AND) || root.hasFilterOfType(TokenType.NO_TYPE))) {
            condition = condition.and(DSL.and(Stream.concat(root.getFiltersOfTypes(List.of(TokenType.AND,
                    TokenType.NO_TYPE)).stream().flatMap(filter -> filter.hasLeafConditions() ?
                            filter.getLeavesData().stream().map(leaf -> field(String.format(TIES_DATA, getDbName(root
                                    .getObjectType())) + "." + String.format(QUOTED_STRING, getDbName(leaf.getName()))).eq(
                                            leaf.getValue())) :
                            null), root.getFiltersOfTypes(List.of(TokenType.AND, TokenType.NO_TYPE)).stream().map(
                                    filter -> filter.hasContainsFunctionCondition() ?
                                            field(String.format(TIES_DATA, getDbName(root.getObjectType())) + "." + String
                                                    .format(QUOTED_STRING, getDbName(filter
                                                            .getContainsFunctionConditionLeafName()))).contains(filter
                                                                    .getContainsFunctionConditionValue()) :
                                            DSL.noCondition())).toList()));
        }

        condition = getConditionsWithRelationships(condition, relationships);

        return condition;
    }

    private static Condition getConditionsWithRelationships(Condition condition, List<String> relationships) {
        if (relationships != null) {
            List<RelationType> relationTypes = new ArrayList<>();

            for (String relationship : relationships) {
                relationTypes.add(SchemaRegistry.getRelationTypeByName(relationship));
            }

            for (RelationType relationType : relationTypes) {
                for (Field<?> field : relationType.getBaseFieldsWithId()) {
                    condition = condition.and(field.isNotNull());
                }
            }
        }
        return condition;
    }

    private static Condition buildWhereOr(Condition condition, QueryElement root) {
        if (root.hasFilters() && root.hasFilterOfType(TokenType.OR)) {
            condition = condition.or(DSL.or(root.getFiltersOfTypes(List.of(TokenType.OR)).stream().map(filter -> DSL.and(
                    filter.hasLeafConditions() ?
                            filter.getLeavesData().stream().map(leaf -> field(String.format(TIES_DATA, getDbName(root
                                    .getObjectType())) + "." + String.format(QUOTED_STRING, getDbName(leaf.getName()))).eq(
                                            leaf.getValue())).toList() :
                            List.of(DSL.noCondition()))).toList()));
            condition = condition.or(DSL.or(root.getFiltersOfTypes(List.of(TokenType.OR)).stream().map(filter -> filter
                    .hasContainsFunctionCondition() ?
                            field(String.format(TIES_DATA, getDbName(root.getObjectType())) + "." + String.format(
                                    QUOTED_STRING, getDbName(filter.getContainsFunctionConditionLeafName()))).contains(
                                            filter.getContainsFunctionConditionValue()) :
                            DSL.noCondition()).toList()));
        }
        return condition;
    }

    private static List<String> getColumnList(QueryElement root) {
        List<String> columns = new ArrayList<>();

        if (root.isIncluded()) {
            if (!root.hasAttributes()) {
                columns.add(String.format(TIES_DATA, getDbName(root.getObjectType())) + "." + String.format(QUOTED_STRING,
                        ID_COLUMN_NAME));
            } else {
                if (root.getAttributes().isEmpty()) {
                    columns.addAll(SchemaRegistry.getEntityTypeByName(root.getObjectType()).getAttributeColumnsWithId());
                } else {
                    columns.addAll(root.getAttributes().stream().map(attribute -> String.format(TIES_DATA, getDbName(root
                            .getObjectType())) + "." + String.format(QUOTED_STRING, getDbName(attribute))).toList());
                    if (!columns.contains(String.format(TIES_DATA, getDbName(root.getObjectType())) + "." + String.format(
                            QUOTED_STRING, ID_COLUMN_NAME))) {
                        columns.add(String.format(TIES_DATA, getDbName(root.getObjectType())) + "." + String.format(
                                QUOTED_STRING, ID_COLUMN_NAME));
                    }
                }
            }
        }
        if (root.hasChildren()) {
            columns.addAll(root.getChildren().stream().flatMap(child -> getColumnList(child).stream()).toList());
        }

        return columns;
    }

    private static List<PathToken> processPath(String path, boolean isSemiColonAllowed) {
        return processPath(path, isSemiColonAllowed, null);
    }

    private static List<PathToken> processPath(String path, boolean isSemiColonAllowed, TokenType lastToken) {
        List<PathToken> tiesPath = new ArrayList<>();

        if (path.isEmpty()) {
            return tiesPath;
        }

        if (!isSemiColonAllowed && path.indexOf(';') > -1) {
            throw TiesPathException.grammarError(String.format("Character ';' is not allowed at %d", path.indexOf(';')));
        }

        if (path.contains(BAR) || path.contains(SEMICOLON)) {
            String delimiter = findDelimiter(path);

            String[] tokens = path.split(Pattern.quote(delimiter), 2);
            TokenType type = delimiter.equals(BAR) ? TokenType.OR : TokenType.AND;
            addPathSafe(tiesPath, tokens[0], type);
            tiesPath.addAll(processPath(tokens[1], isSemiColonAllowed, type));
        } else {
            addPathSafe(tiesPath, path, lastToken != null ? lastToken : TokenType.NO_TYPE);
        }
        return tiesPath;
    }

    private static String findDelimiter(String path) {
        if (path.contains(BAR) && path.contains(SEMICOLON)) {
            return path.indexOf(BAR) < path.indexOf(SEMICOLON) ? SEMICOLON : BAR;
        } else {
            return path.contains(BAR) ? BAR : SEMICOLON;
        }
    }

    private static void addPathSafe(List<PathToken> tiesPath, String path, TokenType type) {
        try {
            tiesPath.add(new PathToken(type, getTiesPathQuery(path)));
        } catch (ParseCancellationException e) {
            throw TiesPathException.grammarError(e.getMessage());
        } catch (PathParsingException e) {
            throw TiesPathException.grammarError(e.getDetails());
        }
    }

    private static List<String> parseRelationships(final String relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return new ArrayList<>();
        }
        String[] relationshipTokens = relationships.split(",");
        List<String> relationshipsList = new ArrayList<>();
        Arrays.stream(relationshipTokens).forEach(relationship -> {
            String trimmed = relationship.trim();
            if (SchemaRegistry.getRelationNames().contains(trimmed)) {
                relationshipsList.add(trimmed);
            } else {
                throw TiesPathException.invalidRelationshipName(trimmed);
            }
        });
        return relationshipsList;
    }

    private static QueryElement buildQueryTree(String objectType, List<PathToken> targets, List<PathToken> scopes,
            String domain) {
        QueryElement root = new QueryElement(objectType);
        RelationType relationType = SchemaRegistry.getRelationTypeByName(objectType);
        if (objectType != null && relationType != null) {
            if (relationType.isConnectsSameEntity()) {
                root.setRelConnectingSameEntity();
            } else if (RELATION.equals(relationType.getRelationshipStorageLocation())) {
                root.setManyToMany();
            }
        }

        processTargets(root, targets, domain);
        processScopes(root, scopes, domain);

        return root;
    }

    private static void processTargets(QueryElement root, List<PathToken> targets, String domain) {
        String prevToken = null;
        String prevPrevToken = null;

        if (targets.isEmpty()) {
            root.include();
        }

        for (PathToken target : targets) {
            QueryElement[] currentElement = { root };
            boolean isValidHop = true;
            for (int i = 0; i < target.getValue().getContainerNames().size(); ++i) {
                String container = target.getValue().getContainerNames().get(i);
                isValidHop = processTargetContainer(root, currentElement, container, prevToken, domain, prevPrevToken);
                if (!isValidHop) {
                    break;
                }
                prevPrevToken = prevToken;
                prevToken = container;
            }
            if (isValidHop) {
                addTargetAttributes(currentElement[0], target);
            }
            prevToken = null;
        }

        if (root.getObjectType() == null && root.getChildren().isEmpty()) {
            throw TiesPathException.grammarError("No match for targetFilter condition");
        }
    }

    private static boolean processTargetContainer(QueryElement root, QueryElement[] currentElement, String container,
            String prevToken, String domain, String prevPrevToken) {
        if (SchemaRegistry.isValidEntityName(container)) {
            if (prevToken != null) {
                throw TiesPathException.grammarError(String.format("Missing ';' or '|' before %s", container));
            }
            if (root.getObjectType() != null && !SchemaRegistry.hasDirectHopBetween(currentElement[0].getObjectType(),
                    container)) {
                return false;
            }
            if (root.getObjectType() == null && !SchemaRegistry.getEntityNamesByDomain(domain).contains(container)) {
                throw TiesPathException.grammarError(String.format("%s is not part of %s domain", container, domain));
            }
            currentElement[0].addChild(new QueryElement(container, true));
            currentElement[0] = currentElement[0].getChild(container);
            prevToken = container;
        } else if (root.getObjectType() != null && currentElement[0].getObjectType().equals(root.getObjectType()) && !root
                .hasChildren()) {
            root.include();
        }

        processTargetAttribute(container, prevToken, currentElement[0]);

        processTargetContainerIfItIsAttributes(root, currentElement, container, prevToken, domain);

        processTargetContainerIfItIsID(root, currentElement, container, prevToken, domain);

        processTargetContainerPrevPrevTokenNull(root, container, prevToken, prevPrevToken);

        return true;
    }

    private static void processTargetContainerPrevPrevTokenNull(QueryElement root, String container, String prevToken,
            String prevPrevToken) {
        if (prevPrevToken == null && prevToken != null && prevToken.equals(ATTRIBUTES) && !container.equals(
                ID_COLUMN_NAME) && !container.equals(ATTRIBUTES)) {
            List<QueryElement> doesNotHaveAttribute = new ArrayList<>();
            for (QueryElement child : root.getChildren()) {
                processTargetAttribute(container, prevToken, child);
                if (child.getAttributes().isEmpty()) {
                    doesNotHaveAttribute.add(child);
                }
            }
            if (root.getObjectType() == null) {
                removeChildFromRoot(doesNotHaveAttribute, root);
            }
        } else if (prevPrevToken == null && prevToken != null && prevToken.equals(ATTRIBUTES) && container.equals(
                ID_COLUMN_NAME)) {
            throw TiesPathException.idAmongAttributesError();
        }
    }

    private static void removeChildFromRoot(List<QueryElement> childrenToRemove, QueryElement root) {
        for (QueryElement child : childrenToRemove) {
            root.removeChild(child);
        }

    }

    private static void processTargetContainerIfItIsAttributes(QueryElement root, QueryElement[] currentElement,
            String container, String prevToken, String domain) {
        if (container.equals(ATTRIBUTES) && root.getObjectType() == null && prevToken == null) {
            for (String entity : SchemaRegistry.getEntityNamesByDomain(domain)) {
                if (currentElement[0].getChild(entity) == null) {
                    QueryElement entityInDomain = new QueryElement(entity);
                    addTargetContainer(entityInDomain, prevToken);
                    currentElement[0].addChild(entityInDomain);
                }
            }
        } else if (container.equals(ATTRIBUTES)) {
            addTargetContainer(currentElement[0], prevToken);
        }
    }

    private static void processTargetContainerIfItIsID(QueryElement root, QueryElement[] currentElement, String container,
            String prevToken, String domain) {
        if (container.equals(ID_COLUMN_NAME) && root.getObjectType() == null && prevToken == null) {
            for (String entity : SchemaRegistry.getEntityNamesByDomain(domain)) {
                if (currentElement[0].getChild(entity) == null && SchemaRegistry.doesEntityContainsAttribute(entity,
                        container)) {
                    QueryElement entityInDomain = new QueryElement(entity, true);
                    currentElement[0].addChild(entityInDomain);
                    addTargetContainer(entityInDomain, prevToken);
                    addTargetAttribute(entityInDomain, container);
                }
            }
        }

    }

    private static void processTargetAttribute(String container, String prevToken, QueryElement currentElement) {
        if (SchemaRegistry.doesEntityContainsAttribute(currentElement.getObjectType(), container)) {
            if (container.equals(ID_COLUMN_NAME) && prevToken == null) {
                addTargetContainer(currentElement, null);
                addTargetAttribute(currentElement, container);

            } else if (container.equals(ID_COLUMN_NAME)) {
                if (SchemaRegistry.isValidEntityName(prevToken)) {
                    addTargetContainer(currentElement, prevToken);
                    addTargetAttribute(currentElement, container);
                } else {
                    throw TiesPathException.idAmongAttributesError();
                }
            } else {
                if (prevToken == null) {
                    throw TiesPathException.grammarError(String.format("Missing 'attributes' before %s", container));
                } else if (prevToken.equals(ATTRIBUTES)) {
                    addTargetAttribute(currentElement, container);

                } else {
                    throw TiesPathException.grammarError(String.format("/%s/%s is not accepted", prevToken, container));
                }
            }
        }
    }

    private static void addTargetContainer(QueryElement element, String prevToken) {
        if (ATTRIBUTES.equals(prevToken)) {
            throw TiesPathException.grammarError(String.format("Missing ';' or '|' before %s", prevToken));
        }
        if (!element.hasAttributes()) {
            element.setAttributes(new ArrayList<>());
            element.include();
        }
    }

    private static void addTargetAttribute(QueryElement element, String container) {
        if (!element.hasAttributes()) {
            throw TiesPathException.grammarError(String.format("Missing 'attributes' before %s", container));
        }
        element.addAttribute(container);
    }

    private static void addTargetAttributes(QueryElement element, PathToken target) {
        if (target.getValue().getNormalizedXpath().equals("/" + ID_COLUMN_NAME) && !target.getValue().getAttributeNames()
                .isEmpty()) {
            throw TiesPathException.grammarError("/" + ID_COLUMN_NAME + "/" + target.getValue().getAttributeNames().get(
                    0) + " is not accepted.");
        }
        if (element.getObjectType() != null) {
            addTargetAttributesIfObjectTypeIsNotNull(element, target);

        } else {
            addTargetAttributesIfObjectTypeIsNull(element, target);

        }
    }

    private static void addTargetAttributesIfObjectTypeIsNotNull(QueryElement element, PathToken target) {
        for (String attribute : target.getValue().getAttributeNames()) {
            if (!element.isManyToMany() && attribute.equals(ID_COLUMN_NAME)) {
                throw TiesPathException.idAmongAttributesError();
            }
            if (!element.isManyToMany() && !SchemaRegistry.doesEntityContainsAttribute(element.getObjectType(),
                    attribute) && !SchemaRegistry.getRelationNamesByEntityName(element.getObjectType()).contains(attribute
                            .replace("REL_ID_", "")) && !SchemaRegistry.getRelationNames().contains(element
                                    .getObjectType()) && !SchemaRegistry.getAssociationNamesByEntityName(element
                                            .getObjectType()).contains(attribute.replace("REL_FK_", ""))) {
                throw TiesPathException.columnNameError(element.getObjectType(), attribute);
            }
            if (!element.hasAttributes()) {
                throw TiesPathException.grammarError(String.format("Missing 'attributes' before (%s...", target.getValue()
                        .getAttributeNames().get(0)));
            }
            element.addAttribute(attribute);
        }
    }

    private static void addTargetAttributesIfObjectTypeIsNull(QueryElement element, PathToken target) {
        Set<QueryElement> hasNoAttribute = new HashSet<>();
        for (String attribute : target.getValue().getAttributeNames()) {
            if (attribute.equals(ID_COLUMN_NAME)) {
                throw TiesPathException.idAmongAttributesError();
            }

            for (QueryElement entity : element.getChildren()) {
                if (SchemaRegistry.doesEntityContainsAttribute(entity.getObjectType(), attribute)) {
                    entity.addAttribute(attribute);
                    hasNoAttribute.remove(entity);
                } else {
                    hasNoAttribute.add(entity);
                }
            }
        }
        for (QueryElement child : hasNoAttribute) {
            element.removeChild(child);
        }

    }

    private static void processScopes(QueryElement root, List<PathToken> scopes, String domain) {
        String prevToken;

        for (PathToken scope : scopes) {
            QueryElement[] currentElement = { root };
            prevToken = null;
            boolean isValidHop = true;
            for (int i = 0; i < scope.getValue().getContainerNames().size(); ++i) {
                String container = scope.getValue().getContainerNames().get(i);
                isValidHop = processScopeContainer(currentElement, container, prevToken, domain);
                prevToken = container;
            }
            if ((prevToken != null && isValidHop) && (scope.getValue().hasLeafConditions() || scope.getValue()
                    .hasContainsFunctionCondition()) && scope.value.getNormalizedParentPath().isEmpty() && root
                            .getObjectType() == null) {

                checkIdExpressions(scope.getValue().getContainsFunctionConditionLeafName(), prevToken, scope.getValue()
                        .getLeavesData());

                processScopesIfRootsObjectTypeIsNull(currentElement[0].getChildren(), scope, prevToken);

            } else if (isValidHop && (scope.getValue().hasLeafConditions() || scope.getValue()
                    .hasContainsFunctionCondition())) {

                checkConditions(currentElement[0], scope);

                if (!root.isManyToMany() && !root.isRelConnectingSameEntity() && scope.getValue()
                        .hasContainsFunctionCondition() && !SchemaRegistry.doesEntityContainsAttribute(currentElement[0]
                                .getObjectType(), scope.getValue().getContainsFunctionConditionLeafName())) {
                    throw TiesPathException.columnNameError(currentElement[0].getObjectType(), scope.getValue()
                            .getContainsFunctionConditionLeafName());
                }

                checkIdAttributes(currentElement[0], prevToken);

                String leafName = scope.getValue().getContainsFunctionConditionLeafName();

                checkIdExpressions(leafName, prevToken, scope.getValue().getLeavesData());

                currentElement[0].addFilter(scope.getType(), scope.getValue());
            }
        }
    }

    private static void checkIdAttributes(QueryElement currentElement, String prevToken) {
        if (!currentElement.hasFilters() && !prevToken.equals(ID_COLUMN_NAME)) {
            throw TiesPathException.grammarError("Missing 'attributes' before '[''");
        }
    }

    private static void checkIdExpressions(String leafName, String prevToken, List<TiesPathQuery.DataLeaf> leavesData) {
        if (leavesData != null) {
            List<TiesPathQuery.DataLeaf> leavesDataWithId = leavesData.stream().filter(leaf -> leaf.getName().equals(
                    ID_COLUMN_NAME)).toList();
            if ((!leavesDataWithId.isEmpty() && !prevToken.equals(ID_COLUMN_NAME)) || ((leavesData
                    .size() > 1 || leavesDataWithId.size() != 1) && prevToken.equals(ID_COLUMN_NAME))) {
                throw TiesPathException.grammarError("Expression is not accepted");
            }
        }

        if (leafName != null && (leafName.equals(ID_COLUMN_NAME) && !prevToken.equals(
                ID_COLUMN_NAME)) || leafName != null && (!leafName.equals(ID_COLUMN_NAME) && prevToken.equals(
                        ID_COLUMN_NAME))) {
            throw TiesPathException.grammarError("Expression is not accepted");
        }
    }

    private static boolean processScopeContainer(QueryElement[] currentElement, String container, String prevToken,
            String domain) {
        if (SchemaRegistry.isValidEntityName(container)) {
            if (prevToken != null) {
                throw TiesPathException.grammarError(String.format("Missing ';' or '|' before %s", container));
            }
            if (currentElement[0].getObjectType() != null && !currentElement[0].isManyToMany() && !currentElement[0]
                    .isRelConnectingSameEntity() && !SchemaRegistry.hasDirectHopBetween(currentElement[0].getObjectType(),
                            container)) {
                return false;
            }
            if (currentElement[0].getObjectType() == null && !SchemaRegistry.getEntityNamesByDomain(domain).contains(
                    container)) {
                throw TiesPathException.grammarError(String.format("%s is not part of %s domain", container, domain));
            }
            currentElement[0].addChild(new QueryElement(container));
            currentElement[0] = currentElement[0].getChild(container);
        }
        processScopeContainerIfContainerIsAttributesOrId(currentElement[0], container, prevToken);

        return true;
    }

    private static void processScopeContainerIfContainerIsAttributesOrId(QueryElement currentElement, String container,
            String prevToken) {
        if ((container.equals(ATTRIBUTES) || container.equals(ID_COLUMN_NAME)) && prevToken == null && currentElement
                .getObjectType() == null) {
            for (QueryElement entity : currentElement.getChildren()) {
                addScopeFilter(entity, container, prevToken);
            }
        } else if (container.equals(ATTRIBUTES) || container.equals(ID_COLUMN_NAME)) {
            addScopeFilter(currentElement, container, prevToken);
        }
    }

    private static void addScopeFilter(QueryElement element, String container, String prevToken) {
        if (container.equals(prevToken)) {
            throw TiesPathException.grammarError(String.format("Missing ';' or '|' before %s", container));
        }
        if (!element.hasFilters()) {
            element.setFilters(new EnumMap<>(TokenType.class));
        }
    }

    private static void checkConditions(QueryElement element, PathToken scope) {
        if (element.getObjectType() != null) {
            if (scope.getValue().hasLeafConditions()) {
                for (TiesPathQuery.DataLeaf leaf : scope.getValue().getLeavesData()) {
                    if (!element.isManyToMany() && !element.isRelConnectingSameEntity() && !SchemaRegistry
                            .doesEntityContainsAttribute(element.getObjectType(), leaf.getName())) {
                        throw TiesPathException.columnNameError(element.getObjectType(), leaf.getName());
                    }
                }
            }
            if (!element.isManyToMany() && !element.isRelConnectingSameEntity() && scope.getValue()
                    .hasContainsFunctionCondition() && !SchemaRegistry.doesEntityContainsAttribute(element.getObjectType(),
                            scope.getValue().getContainsFunctionConditionLeafName())) {
                throw TiesPathException.columnNameError(element.getObjectType(), scope.getValue()
                        .getContainsFunctionConditionLeafName());
            }
        }

    }

    private static void processScopesIfRootsObjectTypeIsNull(List<QueryElement> elements, PathToken scope,
            String prevToken) {
        processScopesIfRootsObjectTypeIsNullHasLeafCondition(elements, scope, prevToken);
        processScopesIfRootsObjectTypeIsNullHasContainsCondition(elements, scope, prevToken);
    }

    private static void processScopesIfRootsObjectTypeIsNullHasLeafCondition(List<QueryElement> elements, PathToken scope,
            String prevToken) {

        if (scope.getValue().hasLeafConditions()) {
            int counter = 0;
            boolean anyOfElementsContainsLeaf = true;
            for (QueryElement element : elements) {
                for (TiesPathQuery.DataLeaf leaf : scope.getValue().getLeavesData()) {
                    checkIdAttributes(element, prevToken);
                    if (!SchemaRegistry.doesEntityContainsAttribute(element.getObjectType(), leaf.getName())) {
                        anyOfElementsContainsLeaf = false;
                        break;
                    }
                }
                if (anyOfElementsContainsLeaf) {
                    element.addFilter(TokenType.OR, scope.getValue());
                    counter++;
                }

                anyOfElementsContainsLeaf = true;
            }
            if (counter == 0) {
                throw TiesPathException.grammarError(
                        "None of the entities in domain has the attribute that was provided in scopeFilter");
            }
        }
    }

    private static void processScopesIfRootsObjectTypeIsNullHasContainsCondition(List<QueryElement> elements,
            PathToken scope, String prevToken) {

        if (scope.getValue().hasContainsFunctionCondition()) {
            boolean anyOfElementsContainsLeaf = false;
            for (QueryElement element : elements) {
                checkIdAttributes(element, prevToken);
                if (SchemaRegistry.doesEntityContainsAttribute(element.getObjectType(), scope.getValue()
                        .getContainsFunctionConditionLeafName())) {
                    element.addFilter(TokenType.OR, scope.getValue());
                    anyOfElementsContainsLeaf = true;
                }
            }
            if (!anyOfElementsContainsLeaf) {
                throw TiesPathException.grammarError(
                        "None of the entities in domain has the attribute that was provided in scopeFilter");
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class PathToken {
        private TokenType type;
        private TiesPathQuery value;
    }

    @Getter
    @AllArgsConstructor
    private static class TableJoinConnection {
        private RelationshipDataLocation relationshipDataLocation;
        private String tableName;
        private List<Condition> conditions;
        private boolean relConnectingSameEntity;
    }
}
