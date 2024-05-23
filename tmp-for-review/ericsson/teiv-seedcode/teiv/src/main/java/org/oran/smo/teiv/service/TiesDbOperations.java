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
package org.oran.smo.teiv.service;

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getDbName;
import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.B_SIDE;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.utils.TiesConstants.FOREIGN_KEY_VIOLATION_ERROR_CODE;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.UNIQUE_CONSTRAINT_VIOLATION_CODE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.jsonExists;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.OTHER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.oran.smo.teiv.exception.IllegalManyToManyRelationshipUpdateException;
import org.oran.smo.teiv.exception.IllegalOneToManyRelationshipUpdateException;
import org.jooq.Configuration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

import org.jooq.Record;

import org.oran.smo.teiv.exception.InvalidFieldInYangDataException;
import org.oran.smo.teiv.exception.RelationshipIdCollisionException;
import org.oran.smo.teiv.exception.UniqueRelationshipIdConstraintException;
import org.oran.smo.teiv.ingestion.validation.IngestionOperationValidatorFactory;
import org.oran.smo.teiv.ingestion.validation.MaximumCardinalityViolationException;
import org.oran.smo.teiv.service.models.OperationResult;
import org.oran.smo.teiv.schema.DataType;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.RelationshipDataLocation;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.service.cloudevent.data.Entity;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;

import org.oran.smo.teiv.utils.ConvertToJooqTypeUtil;
import org.oran.smo.teiv.utils.TiesConstants;
import org.oran.smo.teiv.utils.schema.Geography;

@Component
@AllArgsConstructor
public class TiesDbOperations {

    private final TiesDbService tiesDbService;

    private final IngestionOperationValidatorFactory ingestionOperationValidatorFactory;

    private final RelationshipMergeValidator relationshipMergeValidator;

    /**
     * Insert or update a row in the given table.
     * <p>
     * Note: Free version of Jooq doesn't support spatial types like Geography. Instead of that the OTHER type is used.
     * Instead of that the OTHER type is used.
     * </p>
     *
     * @param context
     *     The context where the database operation should be executed
     * @param tableName
     *     Name of the database table to use.
     * @param values
     *     A map of column name and value pairs. The value is converted
     *     to the corresponding Postgres type based on the
     *     dynamic type of the value. For
     *     example: <"column1", 5L> means that the value 5 should be
     *     inserted to the column1 as a BIGINT.
     * @return The number of modified rows.
     */

    public int merge(DSLContext context, String tableName, Map<String, Object> values) {
        Map<String, Object> valuesToMerge = new HashMap<>(values.size());
        values.forEach((key, value) -> {
            if (value instanceof Geography geographyValue) {
                valuesToMerge.put(key, field("'" + geographyValue + "'", OTHER));
            } else {
                valuesToMerge.put(key, value);
            }
        });

        return context.insertInto(table(tableName)).set(valuesToMerge).onConflict(field(ID_COLUMN_NAME)).doUpdate().set(
                valuesToMerge).execute();
    }

    public List<OperationResult> deleteEntity(DSLContext context, EntityType entityType, String entityId) {
        List<OperationResult> result = new ArrayList<>();
        result.addAll(clearRelationshipsOnEntityDelete(context, entityType, entityId));

        int affectedRows = context.delete(table(entityType.getTableName())).where(field(ID_COLUMN_NAME).eq(entityId))
                .execute();
        if (affectedRows > 0) {
            result.add(new OperationResult(entityId, entityType.getName(), null));
        }
        return result;
    }

    public List<OperationResult> deleteRelationshipByManySideEntityId(DSLContext context, String manySideEntityId,
            String manySideEntityIdColumn, RelationType relationType) {
        String oneSideEntityIdColumn = relationType.getRelationshipStorageLocation().equals(B_SIDE) ?
                relationType.aSideColumnName() :
                relationType.bSideColumnName();

        List<OperationResult> relationshipList = context.select(field(String.format(QUOTED_STRING, relationType
                .getIdColumnName()), String.class)).from(table(relationType.getTableName())).where(field(String.format(
                        QUOTED_STRING, manySideEntityIdColumn)).eq(manySideEntityId)).forUpdate().fetchInto(String.class)
                .stream().filter(Objects::nonNull).map(id -> new OperationResult(id, extractTypeFromColumn(
                        oneSideEntityIdColumn), null)).collect(Collectors.toList());
        if (relationshipList.isEmpty()) {
            return relationshipList;
        } else {
            int updateResult = context.update(table(relationType.getTableName())).setNull(field(String.format(QUOTED_STRING,
                    relationType.getIdColumnName()))).setNull(field(String.format(QUOTED_STRING, oneSideEntityIdColumn)))
                    .set(field(String.format(QUOTED_STRING, relationType.getSourceIdsColumnName())), ConvertToJooqTypeUtil
                            .toJsonb(List.of())).where(field(String.format(QUOTED_STRING, manySideEntityIdColumn)).eq(
                                    manySideEntityId)).execute();
            return updateResult > 0 ? relationshipList : List.of();
        }

    }

    public Optional<OperationResult> deleteRelationFromEntityTableByRelationId(DSLContext context, String relationshipId,
            RelationType relationType) {
        String oneSideEntityIdColumn = relationType.getRelationshipStorageLocation().equals(B_SIDE) ?
                relationType.aSideColumnName() :
                relationType.bSideColumnName();

        int affectedRows = context.update(table(relationType.getTableName())).setNull(field(String.format(QUOTED_STRING,
                relationType.getIdColumnName()))).setNull(field(String.format(QUOTED_STRING, oneSideEntityIdColumn))).set(
                        field(String.format(QUOTED_STRING, relationType.getSourceIdsColumnName())), ConvertToJooqTypeUtil
                                .toJsonb(List.of())).where(field(String.format(QUOTED_STRING, relationType
                                        .getIdColumnName())).eq(relationshipId)).execute();
        return affectedRows > 0 ?
                Optional.of(new OperationResult(relationshipId, extractTypeFromColumn(oneSideEntityIdColumn), null)) :
                Optional.empty();
    }

    public List<OperationResult> deleteManyToManyRelationByEntityId(DSLContext context, String tableName, String entityId,
            String aSideColumnName, String bSideColumnName) {
        List<String> deletedIds = context.delete(table(tableName)).where(field(String.format(QUOTED_STRING,
                aSideColumnName)).eq(entityId).or(field(String.format(QUOTED_STRING, bSideColumnName)).eq(entityId)))
                .returning(field(TiesConstants.ID_COLUMN_NAME)).fetch().getValues(field(TiesConstants.ID_COLUMN_NAME),
                        String.class);

        return deletedIds.stream().map(id -> new OperationResult(id, extractTypeFromTable(tableName), null)).collect(
                Collectors.toList());
    }

    public Optional<OperationResult> deleteManyToManyRelationByRelationId(DSLContext context, String tableName,
            String relationshipId) {

        int affectedRows = context.delete(table(tableName)).where(field(ID_COLUMN_NAME).eq(relationshipId)).execute();
        return affectedRows > 0 ?
                Optional.of(new OperationResult(relationshipId, extractTypeFromTable(tableName), null)) :
                Optional.empty();
    }

    public List<OperationResult> executeEntityAndRelationshipMergeOperations(ParsedCloudEventData parsedCloudEventData)
            throws InvalidFieldInYangDataException, MaximumCardinalityViolationException {
        List<Consumer<DSLContext>> dbOperations = new ArrayList<>();
        List<OperationResult> results = new ArrayList<>();

        for (Entity entity : parsedCloudEventData.getEntities()) {
            dbOperations.add(getEntityOperation(entity, results));
        }
        for (Relationship relationship : parsedCloudEventData.getRelationships()) {
            dbOperations.addAll(getRelationshipOperations(relationship, results));
        }
        dbOperations.add(dslContext -> ingestionOperationValidatorFactory.createValidator(dslContext).validate(
                parsedCloudEventData));
        tiesDbService.execute(dbOperations);
        return results;
    }

    public List<String> selectByCmHandle(DSLContext context, String tableName, String cmHandle) {
        String path = "$.** ? (@.cmHandle == \"" + cmHandle + "\")";
        return context.select(field(String.format(QUOTED_STRING, "id"), String.class)).from(tableName).where(jsonExists(
                field(String.format(QUOTED_STRING, "cmId"), JSON.class), path)).fetch().getValues(field(String.format(
                        QUOTED_STRING, "id"), String.class));
    }

    public List<String> selectByCmHandleFormSourceIds(DSLContext context, String tableName, String cmHandle) {
        String path = String.format("$[*] ? (@ == \"urn:cmHandle:/%s\")", cmHandle);
        return context.select(field(String.format(QUOTED_STRING, "id"), String.class)).from(tableName).where(jsonExists(
                field(String.format(QUOTED_STRING, "CD_sourceIds"), JSON.class), path)).fetch().getValues(field(String
                        .format(QUOTED_STRING, "id"), String.class));
    }

    private Consumer<DSLContext> getEntityOperation(Entity entity, List<OperationResult> results)
            throws InvalidFieldInYangDataException {
        EntityType entityType = SchemaRegistry.getEntityTypeByName(entity.getType());
        Map<String, DataType> fieldsFromModel = entityType.getFields();
        Map<String, Object> dbMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : entity.getAttributes().entrySet()) {
            DataType dataType = fieldsFromModel.get(entry.getKey());
            if (dataType == null) {
                throw new InvalidFieldInYangDataException(String.format(
                        "Received field: %s isn't a valid field of entity type: %s", entry.getKey(), entity.getType()));
            }
            switch (dataType) {
                case GEOGRAPHIC -> dbMap.put(getDbName(entry.getKey()), ConvertToJooqTypeUtil.toGeography(entry
                        .getValue()));
                case CONTAINER -> dbMap.put(getDbName(entry.getKey()), ConvertToJooqTypeUtil.toJsonb(entry.getValue()));
                case PRIMITIVE, DECIMAL, BIGINT -> dbMap.put(getDbName(entry.getKey()), entry.getValue());
            }
        }
        dbMap.put(ID_COLUMN_NAME, entity.getId());
        if (entity.getSourceIds() != null) {
            dbMap.put(entityType.getSourceIdsColumnName(), ConvertToJooqTypeUtil.toJsonb(entity.getSourceIds()));
        }
        return dslContext -> {
            int affectedRows = merge(dslContext, entityType.getTableName(), dbMap);
            if (affectedRows > 0) {
                dbMap.remove(ID_COLUMN_NAME);
                dbMap.remove(entityType.getSourceIdsColumnName());
                results.add(new OperationResult(entity.getId(), entity.getType(), dbMap));
            }
        };
    }

    private List<Consumer<DSLContext>> getRelationshipOperations(Relationship relationship, List<OperationResult> results) {
        List<Consumer<DSLContext>> relationshipOperations = new ArrayList<>();
        RelationType relationType = SchemaRegistry.getRelationTypeByName(relationship.getType());
        RelationshipDataLocation relationshipDataLocation = relationType.getRelationshipStorageLocation();

        Map<String, Object> dbMap = new HashMap<>();
        dbMap.put(relationType.getIdColumnName(), relationship.getId());
        dbMap.put(relationType.aSideColumnName(), relationship.getASide());
        dbMap.put(relationType.bSideColumnName(), relationship.getBSide());
        if (relationship.getSourceIds() != null) {
            dbMap.put(relationType.getSourceIdsColumnName(), ConvertToJooqTypeUtil.toJsonb(relationship.getSourceIds()));
        }

        if (relationshipDataLocation == RELATION) {
            relationshipOperations.add(outer -> {
                try {
                    outer.dsl().transaction((Configuration nested) -> mergeManyToManyRelationship(nested.dsl(),
                            relationship, results, relationType, dbMap));
                } catch (DataAccessException e) {
                    if (e.sqlState().equals(FOREIGN_KEY_VIOLATION_ERROR_CODE)) {
                        createMissingEntities(outer, relationship, relationType, results);
                        mergeManyToManyRelationship(outer, relationship, results, relationType, dbMap);
                    } else {
                        throw e;
                    }
                }
            });
        } else {
            relationshipOperations.add(dslContext -> mergeOneToManyOrOneToOneRelationship(dslContext, relationship, results,
                    relationType, dbMap));
        }

        return relationshipOperations;
    }

    private void mergeOneToManyOrOneToOneRelationship(DSLContext dslContext, Relationship relationship,
            List<OperationResult> results, RelationType relationType, Map<String, Object> dbMap) {
        AtomicBoolean isManySideEntityMissingAtTheBeginning = new AtomicBoolean(false);
        try {
            dslContext.dsl().transaction((Configuration nested) -> updateOneToManyRelationship(nested.dsl(), relationship,
                    relationType, dbMap).ifPresentOrElse(results::add, () -> {
                        Record manySideRow = selectByIdForUpdate(relationType.getTableName(), ID_COLUMN_NAME, relationship
                                .getStoringSideEntityId(), dslContext);
                        if (manySideRow == null) {
                            isManySideEntityMissingAtTheBeginning.set(true);
                            createMissingStoringSideEntity(dslContext, relationship, relationType);
                            addEntityToOperationResults(results, relationship.getStoringSideEntityId(), relationType
                                    .getStoringSideEntityType());
                            updateOneToManyRelationship(dslContext, relationship, relationType, dbMap).ifPresentOrElse(
                                    results::add, () -> {
                                        throw new IllegalOneToManyRelationshipUpdateException(relationship, true);
                                    });
                        } else {
                            handleOneToManyRelationshipFaults(manySideRow, relationship, relationType);
                        }
                    }));
        } catch (DataAccessException e) {
            if (e.sqlState().equals(UNIQUE_CONSTRAINT_VIOLATION_CODE)) {
                throw new UniqueRelationshipIdConstraintException(relationship);
            } else if (e.sqlState().equals(FOREIGN_KEY_VIOLATION_ERROR_CODE)) {
                if (isManySideEntityMissingAtTheBeginning.get()) {
                    createMissingStoringSideEntity(dslContext, relationship, relationType);
                    addEntityToOperationResults(results, relationship.getStoringSideEntityId(), relationType
                            .getStoringSideEntityType());
                }
                createMissingNotStoringSideEntity(dslContext, relationship, relationType);
                addEntityToOperationResults(results, relationship.getNotStoringSideEntityId(), relationType
                        .getNotStoringSideEntityType());
                updateOneToManyRelationship(dslContext, relationship, relationType, dbMap).ifPresentOrElse(results::add,
                        () -> {
                            throw new IllegalOneToManyRelationshipUpdateException(relationship, false);
                        });
            } else {
                throw e;
            }
        }
    }

    private Optional<OperationResult> updateOneToManyRelationship(DSLContext dslContext, Relationship relationship,
            RelationType relationType, Map<String, Object> values) {
        Condition condition = field(ID_COLUMN_NAME).eq(relationship.getStoringSideEntityId()).and(field(name(relationType
                .getIdColumnName())).isNull().or(field(name(relationType.getIdColumnName())).eq(relationship.getId()).and(
                        field(name(relationType.getNotStoringSideEntityIdColumnNameInStoringSideTable())).eq(relationship
                                .getNotStoringSideEntityId()))));

        int numberOfUpdatedRows = dslContext.update(table(relationType.getTableName())).set(values).where(condition)
                .execute();

        return numberOfUpdatedRows != 0 ?
                Optional.of(OperationResult.createFromRelationship(relationship)) :
                Optional.empty();

    }

    private void handleOneToManyRelationshipFaults(Record manySideRow, Relationship relationship,
            RelationType relationType) {
        if (relationshipMergeValidator.anotherRelationshipAlreadyExistsOnStoringSideEntity(manySideRow, relationType,
                relationship)) {
            String manySideEntityType = relationType.getStoringSideEntityType();
            String exceptionMessage = String.format(
                    "Another relationship with id %s of type %s already exists on entity with id %s of type %s, can't override it with new relationship with id %s",
                    manySideRow.get(relationType.getIdColumnName()), relationType.getName(), relationship
                            .getStoringSideEntityId(), manySideEntityType, relationship.getId());
            throw new MaximumCardinalityViolationException(exceptionMessage);
        } else if (relationshipMergeValidator.relationshipAlreadyExistsWithDifferentNotStoringSideEntity(manySideRow,
                relationship, relationType)) {
            throw new RelationshipIdCollisionException(relationship);
        }
    }

    private void mergeManyToManyRelationship(DSLContext dslContext, Relationship relationship,
            List<OperationResult> results, RelationType relationType, Map<String, Object> dbMap) {
        int affectedRows = dslContext.insertInto(table(relationType.getTableName())).set(dbMap).onConflict(field(
                relationType.getIdColumnName())).doUpdate().set(dbMap).where(field(relationType.getTableName() + "." + name(
                        relationType.aSideColumnName())).eq(relationship.getASide()).and(field(relationType
                                .getTableName() + "." + name(relationType.bSideColumnName())).eq(relationship.getBSide())))
                .execute();
        if (affectedRows > 0) {
            results.add(OperationResult.createFromRelationship(relationship));
        } else {
            throw new IllegalManyToManyRelationshipUpdateException(relationship);
        }
    }

    private void createMissingEntities(DSLContext dslContext, Relationship relationship, RelationType relationType,
            List<OperationResult> results) {
        String aSideTableName = relationType.getASide().getTableName();
        String aSideId = relationship.getASide();
        String bSideTableName = relationType.getBSide().getTableName();
        String bSideId = relationship.getBSide();

        if (createMissingEntity(aSideTableName, ID_COLUMN_NAME, aSideId, dslContext) == 1) {
            results.add(new OperationResult(aSideId, relationType.getASide().getName(), new HashMap<>()));
        }
        if (createMissingEntity(bSideTableName, ID_COLUMN_NAME, bSideId, dslContext) == 1) {
            results.add(new OperationResult(bSideId, relationType.getBSide().getName(), new HashMap<>()));
        }
    }

    private List<OperationResult> clearRelationshipsOnEntityDelete(DSLContext dslContext, EntityType entityType,
            String entityId) {
        List<OperationResult> deletedIds = new ArrayList<>();
        List<RelationType> relationTypes = SchemaRegistry.getRelationTypesByEntityName(entityType.getName());

        for (RelationType relationType : relationTypes) {
            String columnNameForWhereCondition = (relationType.getASide().getName().equals(entityType.getName())) ?
                    relationType.aSideColumnName() :
                    relationType.bSideColumnName();
            if (relationType.getRelationshipStorageLocation() == RELATION) {
                deletedIds.addAll(deleteManyToManyRelationByEntityId(dslContext, relationType.getTableName(), entityId,
                        relationType.aSideColumnName(), relationType.bSideColumnName()));

            } else {
                deletedIds.addAll(deleteRelationshipByManySideEntityId(dslContext, entityId, columnNameForWhereCondition,
                        relationType));
            }
        }
        return deletedIds;
    }

    private int createMissingEntity(String entityTableName, String entityIdColumnName, String entityId,
            DSLContext dslContext) {
        return dslContext.insertInto(table(entityTableName)).set(field(name(entityIdColumnName)), entityId)
                .onConflictDoNothing().execute();
    }

    private void createMissingNotStoringSideEntity(DSLContext dslContext, Relationship relationship,
            RelationType relationType) {
        createMissingEntity(relationType.getNotStoringSideTableName(), ID_COLUMN_NAME, relationship
                .getNotStoringSideEntityId(), dslContext);
    }

    private void createMissingStoringSideEntity(DSLContext dslContext, Relationship relationship,
            RelationType relationType) {
        createMissingEntity(relationType.getTableName(), ID_COLUMN_NAME, relationship.getStoringSideEntityId(), dslContext);
    }

    private Record selectByIdForUpdate(String tableName, String idFieldName, String manySideEntityId,
            DSLContext dslContext) {
        return dslContext.selectFrom(table(tableName)).where(field(idFieldName).eq(manySideEntityId)).forUpdate()
                .fetchOne();
    }

    private void addEntityToOperationResults(List<OperationResult> results, String entityId, String entityType) {
        OperationResult result = new OperationResult(entityId, entityType, Map.of());
        if (!results.contains(result)) {
            results.add(result);
        }
    }

    private String extractTypeFromTable(String tableName) {
        return tableName.split("\\\"")[1];
    }

    private String extractTypeFromColumn(String oneSideEntityIdColumn) {
        String associationName = getModelledName(oneSideEntityIdColumn).replace("REL_FK_", "");
        return SchemaRegistry.getRelationNameByAssociationName(associationName);
    }
}
