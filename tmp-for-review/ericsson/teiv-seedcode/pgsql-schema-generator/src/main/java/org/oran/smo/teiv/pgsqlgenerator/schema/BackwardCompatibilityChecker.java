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
package org.oran.smo.teiv.pgsqlgenerator.schema;

import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.ForeignKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PgSchemaGeneratorException;
import org.oran.smo.teiv.pgsqlgenerator.PostgresConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BackwardCompatibilityChecker {

    @Value("${green-field-installation}")
    private boolean isGreenFieldInstallation;

    public void checkForNBCChangesInModel(List<Relationship> relationshipsInBaselineSql,
            List<Relationship> relationshipsFromModelSvc) {
        if (!isGreenFieldInstallation) {
            relationshipsInBaselineSql.forEach(baselineRel -> {
                Optional<Relationship> matchingRelationship = relationshipsFromModelSvc.stream().filter(rel -> rel.getName()
                        .equals(baselineRel.getName())).findFirst();
                matchingRelationship.ifPresentOrElse(modelSvcRel -> {
                    if (!(baselineRel.getBSideMinCardinality() == modelSvcRel.getBSideMinCardinality() && baselineRel
                            .getBSideMaxCardinality() == modelSvcRel.getBSideMaxCardinality() && baselineRel
                                    .getASideMinCardinality() == modelSvcRel.getASideMinCardinality() && baselineRel
                                            .getASideMaxCardinality() == modelSvcRel.getASideMaxCardinality())) {
                        throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                                "modified cardinalities for relationship(%s)", baselineRel.getName()),
                                new UnsupportedOperationException());
                    }
                }, () -> {
                    throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                            "modified/removed relationship(%s) present in baseline", baselineRel.getName()),
                            new UnsupportedOperationException());
                });
            });
        } else {
            log.info("No NBC checks done as green field installation is enabled");
        }
    }

    public void checkForNBCChangesInData(List<Table> tablesInBaselineSql, List<Table> tablesFromModelSvc) {
        if (!isGreenFieldInstallation) {
            tablesInBaselineSql.forEach(baselineTable -> {
                Optional<Table> matchingTable = tablesFromModelSvc.stream().filter(modelTable -> modelTable.getName()
                        .equals(baselineTable.getName())).findFirst();
                matchingTable.ifPresentOrElse(table -> {
                    verifyTableColumns(baselineTable.getColumns(), table.getColumns(), table.getName());
                }, () -> {
                    throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                            "modified/removed table(%s) present in baseline", baselineTable.getName()),
                            new UnsupportedOperationException());
                });
            });
        } else {
            log.info("No NBC checks done as green field installation is enabled");
        }
    }

    private void verifyTableColumns(List<Column> columnsInBaseline, List<Column> columnsInModelSvc, String tableName) {
        columnsInBaseline.forEach(baselineColumn -> {
            Optional<Column> matchingColumn = columnsInModelSvc.stream().filter(modelColumn -> modelColumn.getName().equals(
                    baselineColumn.getName())).findFirst();
            matchingColumn.ifPresentOrElse(modelColumn -> {
                validateColumnConstraints(baselineColumn, modelColumn, tableName);
                validateColumnDataType(baselineColumn, modelColumn, tableName);
            }, () -> {
                throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                        "modified/removed column(%s.%s) present in baseline", tableName, baselineColumn.getName()),
                        new UnsupportedOperationException());
            });
        });
    }

    private void validateColumnConstraints(Column baselineColumn, Column modelColumn, String tableName) {
        for (PostgresConstraint constraint : baselineColumn.getPostgresConstraints()) {
            Optional<PostgresConstraint> matchingConstraint = modelColumn.getPostgresConstraints().stream().filter(
                    constraint1 -> constraint1.getConstraintName().equals(constraint.getConstraintName())).findFirst();

            matchingConstraint.ifPresentOrElse(constraint1 -> {
                if (!constraint.getTableToAddConstraintTo().equals(constraint1.getTableToAddConstraintTo()) && !constraint
                        .getColumnToAddConstraintTo().equals(constraint1.getColumnToAddConstraintTo()) && !constraint
                                .getConstraintName().equals(constraint1.getConstraintName())) {
                    throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                            "modified/removed constraint for column(%s.%s) present in baseline", tableName, baselineColumn
                                    .getName()), new UnsupportedOperationException());
                }
                if (constraint instanceof ForeignKeyConstraint && !constraint.getTableToAddConstraintTo().equals(constraint1
                        .getTableToAddConstraintTo())) {
                    throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                            "modified/removed constraint for column(%s.%s) present in baseline", tableName, baselineColumn
                                    .getName()), new UnsupportedOperationException());
                }
            }, () -> {
                throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                        "modified/removed constraint for column(%s.%s) present in baseline", tableName, baselineColumn
                                .getName()), new UnsupportedOperationException());
            });
        }
    }

    private void validateColumnDataType(Column baselineColumn, Column modelColumn, String tableName) {
        if (!baselineColumn.getDataType().equals(modelColumn.getDataType())) {
            throw PgSchemaGeneratorException.nbcChangeIdentifiedException(String.format(
                    "modified/removed datatype for column(%s.%s) present in baseline", tableName, baselineColumn.getName()),
                    new UnsupportedOperationException());
        }
    }

}
