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
package org.oran.smo.teiv.pgsqlgenerator.schema.data;

import static org.oran.smo.teiv.pgsqlgenerator.Constants.A_SIDE_PREFIX;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.B_SIDE_PREFIX;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.COLUMN;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.CONSTRAINT;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.CONSUMER_DATA;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.FOREIGN_KEY;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.ID;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.NO_PREFIX;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.PRIMARY_KEY;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.REL_CD;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.REL_FK;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.REL_ID;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.TABLE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.UNIQUE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.VARCHAR511;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.A_SIDE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.B_SIDE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.RELATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.oran.smo.teiv.pgsqlgenerator.ForeignKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.Entity;
import org.oran.smo.teiv.pgsqlgenerator.PostgresConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.UniqueConstraint;
import org.oran.smo.teiv.pgsqlgenerator.schema.model.HashInfoDataGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class TableBuilder {

    private final HashInfoDataGenerator hashInfoDataGenerator;

    /**
     * Generate tables from entities and relationships.
     *
     * @param entities
     *     List of entities from model service
     * @param relationships
     *     List of relationships from model service
     * @return List relationships in the form of tables
     */
    public List<Table> getTables(List<Entity> entities, List<Relationship> relationships) {
        // Create table from entities and relationships
        Map<String, List<Column>> tableFromMSvc = new HashMap<>();

        entities.forEach(entity -> {
            String tableName = entity.getEntityName();
            String hashedTableName = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, tableName, TABLE);
            tableFromMSvc.put(hashedTableName, getColumnsByEntity(entity, hashedTableName));
        });

        relationships.forEach(relationship -> {
            String tableName = "";
            switch (relationship.getRelationshipDataLocation()) {
                case A_SIDE -> tableName = relationship.getASideMOType();
                case B_SIDE -> tableName = relationship.getBSideMOType();
                case RELATION -> tableName = relationship.getName();
                default -> log.error(String.format("Relationship with name %s cannot be mapped", relationship.getName()));
            }
            String hashedTableName = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, tableName, TABLE);

            tableFromMSvc.computeIfAbsent(hashedTableName, k -> new ArrayList<>()).addAll(getColumnsByRelationship(
                    relationship, tableName, hashedTableName));
        });

        return tableFromMSvc.entrySet().stream().map(entry -> Table.builder().name(entry.getKey()).columns(entry.getValue())
                .build()).collect(Collectors.toList());
    }

    private List<Column> getColumnsByEntity(Entity entity, String hashedTableName) {
        List<Column> entityCols = new ArrayList<>();
        entityCols.addAll(entity.getAttributes().stream().map(attribute -> {
            String attributeName = attribute.getName();
            String hashedAttributeName = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, attributeName,
                    COLUMN);
            return Column.builder().name(hashedAttributeName).dataType(attribute.getDataType()).postgresConstraints(
                    getConstraintsForColumn(entity.getEntityName(), hashedTableName, attribute.getConstraints(),
                            attributeName, hashedAttributeName)).defaultValue(attribute.getDefaultValue()).build();
        }).toList());
        entityCols.addAll(entity.getConsumerData().stream().map(cd -> {
            String hashedAttributeName = hashInfoDataGenerator.generateHashAndRegisterTableRow(CONSUMER_DATA, cd.getName(),
                    COLUMN);
            return Column.builder().name(hashedAttributeName).dataType(cd.getDataType()).defaultValue(cd.getDefaultValue())
                    .build();
        }).toList());
        return entityCols;
    }

    private List<Column> getColumnsByRelationship(Relationship relationship, String tableName, String hashedTableName) {
        if (relationship.getRelationshipDataLocation().equals(RELATION)) {
            return createRelationshipColumnsForRelationTables(relationship, tableName, hashedTableName);
        } else {
            return createRelationshipColumnsForEntityTables(relationship, tableName, hashedTableName);
        }
    }

    private List<Column> createRelationshipColumnsForEntityTables(Relationship rel, String tableName,
            String hashedTableName) {
        List<Column> relColumns = new ArrayList<>();

        final String associationEndpointName = tableName.equals(rel.getASideMOType()) ?
                rel.getASideAssociationName() :
                rel.getBSideAssociationName();
        final String relFk = REL_FK + associationEndpointName;
        final String relId = REL_ID + rel.getName();

        String hashedTableNameASide = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, rel.getASideMOType(),
                TABLE);
        String hashedTableNameBSide = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, rel.getBSideMOType(),
                TABLE);
        final String hashedReferenceTable = tableName.equals(hashedTableNameASide) ?
                hashedTableNameBSide :
                hashedTableNameASide;
        final String hashedRelId = hashInfoDataGenerator.generateHashAndRegisterTableRow(REL_ID, rel.getName(), COLUMN);
        final String hashedRelFk = hashInfoDataGenerator.generateHashAndRegisterTableRow(REL_FK, associationEndpointName,
                COLUMN);

        relColumns.add(Column.builder().name(hashedRelFk).dataType(VARCHAR511).postgresConstraints(new ArrayList<>(List.of(
                createForeignKeyConstraints(tableName, hashedTableName, hashedReferenceTable, relFk, hashedRelFk))))
                .build());

        relColumns.add(Column.builder().name(hashedRelId).dataType(VARCHAR511).postgresConstraints(new ArrayList<>(List.of(
                createUniqueConstraints(tableName, hashedTableName, relId, hashedRelId)))).build());
        rel.getConsumerData().forEach(cd -> {
            final String cdColumnName = hashInfoDataGenerator.generateHashAndRegisterTableRow(REL_CD, cd
                    .getName() + "_" + rel.getName(), COLUMN);
            relColumns.add(Column.builder().name(cdColumnName).dataType(cd.getDataType()).defaultValue(cd.getDefaultValue())
                    .build());
        });
        return relColumns;
    }

    private List<Column> createRelationshipColumnsForRelationTables(Relationship rel, String tableName,
            String hashedTableName) {

        String hashedASideMOType = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, rel.getASideMOType(),
                TABLE);
        String hashedBSideMOType = hashInfoDataGenerator.generateHashAndRegisterTableRow(NO_PREFIX, rel.getBSideMOType(),
                TABLE);

        final String relASide = A_SIDE_PREFIX + rel.getASideMOType();
        final String hashedRelASide = hashInfoDataGenerator.generateHashAndRegisterTableRow(A_SIDE_PREFIX, rel
                .getASideMOType(), COLUMN);
        final String relBSide = B_SIDE_PREFIX + rel.getBSideMOType();
        final String hashedRelBSide = hashInfoDataGenerator.generateHashAndRegisterTableRow(B_SIDE_PREFIX, rel
                .getBSideMOType(), COLUMN);
        List<Column> relColumns = new ArrayList<>(Arrays.asList(Column.builder().name(ID).dataType(VARCHAR511)
                .postgresConstraints(new ArrayList<>(List.of(createPrimaryKeyConstraints(tableName, hashedTableName, ID))))
                .build(), Column.builder().name(hashedRelASide).dataType(VARCHAR511).postgresConstraints(new ArrayList<>(
                        List.of(createForeignKeyConstraints(tableName, hashedTableName, hashedASideMOType, relASide,
                                hashedRelASide)))).build(), Column.builder().name(hashedRelBSide).dataType(VARCHAR511)
                                        .postgresConstraints(new ArrayList<>(List.of(createForeignKeyConstraints(tableName,
                                                hashedTableName, hashedBSideMOType, relBSide, hashedRelBSide)))).build()));
        rel.getConsumerData().forEach(cd -> {
            final String columnName = hashInfoDataGenerator.generateHashAndRegisterTableRow(CONSUMER_DATA, cd.getName(),
                    COLUMN);
            relColumns.add(Column.builder().name(columnName).dataType(cd.getDataType()).defaultValue(cd.getDefaultValue())
                    .build());
        });
        return relColumns;
    }

    private Collection<PostgresConstraint> getConstraintsForColumn(String tableName, String hashedTableName,
            Collection<Object> constraintsFromEModel, String attributeName, String hashedAttributeName) {
        Collection<PostgresConstraint> postgresConstraintCollection = new ArrayList<>();
        if (constraintsFromEModel != null) {
            if (constraintsFromEModel.stream().anyMatch(PrimaryKeyConstraint.class::isInstance)) {
                if (attributeName.equals(ID)) {
                    postgresConstraintCollection.add(createPrimaryKeyConstraints(tableName, hashedTableName,
                            attributeName));
                }
            }
            //            if (constraintsFromEModel.stream().anyMatch(<any-other-constraint>.class::isInstance)) {
            //                postgresConstraintCollection.add(createUniqueConstraints(tableName, hashedTableName, attributeName,
            //                        hashedAttributeName));
            //            }
        }
        return postgresConstraintCollection;
    }

    private PostgresConstraint createForeignKeyConstraints(String tableToAddForeignKeyTo,
            String hashedTableToAddForeignKeyTo, String hashedReferenceTable, String columnName, String hashedColumnName) {
        String constraintName = hashInfoDataGenerator.generateHashAndRegisterTableRow(FOREIGN_KEY,
                tableToAddForeignKeyTo + "_" + columnName, CONSTRAINT);
        return ForeignKeyConstraint.builder().constraintName(constraintName).tableName(hashedTableToAddForeignKeyTo)
                .referencedTable(hashedReferenceTable).columnToAddConstraintTo(hashedColumnName).build();
    }

    private PostgresConstraint createPrimaryKeyConstraints(String tableName, String hashedTableName, String columnName) {
        String constraintName = hashInfoDataGenerator.generateHashAndRegisterTableRow(PRIMARY_KEY,
                tableName + "_" + columnName, CONSTRAINT);
        return PrimaryKeyConstraint.builder().constraintName(constraintName).tableName(hashedTableName)
                .columnToAddConstraintTo(columnName).build();
    }

    private PostgresConstraint createUniqueConstraints(String tableName, String hashedTableName, String columnName,
            String hashedColumnName) {
        String constraintName = hashInfoDataGenerator.generateHashAndRegisterTableRow(UNIQUE, tableName + "_" + columnName,
                CONSTRAINT);
        return UniqueConstraint.builder().constraintName(constraintName).tableName(hashedTableName).columnToAddConstraintTo(
                hashedColumnName).build();
    }

}
