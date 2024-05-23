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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.PostgresConstraint;
import org.oran.smo.teiv.pgsqlgenerator.ForeignKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.NotNullConstraint;
import org.oran.smo.teiv.pgsqlgenerator.PrimaryKeyConstraint;
import org.oran.smo.teiv.pgsqlgenerator.Table;
import org.oran.smo.teiv.pgsqlgenerator.UniqueConstraint;

import static org.oran.smo.teiv.pgsqlgenerator.Constants.CREATE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.ALTER;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.ID;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.ALTER_TABLE_TIES_DATA_S;

@Slf4j
@Component
public class DataSchemaHelper {

    /**
     * Generates SQL statements for schema alterations based on identified changes.
     *
     * @param differences
     *     Map of identified changes to models
     * @return StringBuilder containing SQL statements
     */
    public StringBuilder generateSchemaFromDifferences(Map<String, List<Table>> differences) {
        StringBuilder generatedSchema = new StringBuilder();
        if (differences.isEmpty()) {
            log.info("No differences identified!!");
        } else {
            for (Map.Entry<String, List<Table>> entry : differences.entrySet()) {
                switch (entry.getKey()) {
                    case CREATE -> generatedSchema.append(generateCreateStatementsFromDifferences(entry.getValue()));
                    case ALTER -> generatedSchema.append(generateAlterStatementsFromDifferences(entry.getValue()));
                    default -> generatedSchema.append(generateDefaultStatementsFromDifferences(entry.getValue()));
                }
            }
        }
        return generatedSchema;
    }

    /**
     * Generates SQL statements for CREATE TABLE from differences.
     */
    private StringBuilder generateCreateStatementsFromDifferences(List<Table> tables) {
        StringBuilder storeSchemaForCreateStatements = new StringBuilder();
        StringBuilder storeAlterStatementsForPrimaryKeyConstraints = new StringBuilder();
        StringBuilder storeAlterStatementsForAllOtherConstraints = new StringBuilder();
        for (Table table : tables) {
            storeAlterStatementsForPrimaryKeyConstraints.append(generateAlterStatementsForPrimaryKeyConstraints(table
                    .getColumns()));
            storeAlterStatementsForAllOtherConstraints.append(generateAlterStatementsForAllOtherConstraints(table
                    .getColumns()));
            storeSchemaForCreateStatements.append(generateCreateTableStatements(table.getColumns(), table.getName()));
        }
        storeSchemaForCreateStatements.append(storeAlterStatementsForPrimaryKeyConstraints).append(
                storeAlterStatementsForAllOtherConstraints);
        return storeSchemaForCreateStatements;
    }

    /**
     * Generates SQL statements for creating new tables.
     */
    private StringBuilder generateCreateTableStatements(List<Column> newColumns, String tableName) {

        StringBuilder storeTableSchema = new StringBuilder(String.format("CREATE TABLE IF NOT EXISTS ties_data.\"%s\" (%n",
                tableName));
        StringBuilder storeColumns = new StringBuilder();
        StringBuilder storeDefaultValues = new StringBuilder();

        for (Column newColumn : newColumns) {
            if (newColumn.getDefaultValue() != null) {
                storeDefaultValues.append(generateDefaultValueStatements(newColumn, tableName));
            }
            // id column must come in the top of the table
            if (newColumn.getName().equals(ID)) {
                storeTableSchema.append(String.format("\t\"%s\"\t\t\t%s,%n", newColumn.getName(), newColumn.getDataType()));
            } else {
                storeColumns.append(generateCreateColumnStatements(newColumn));
            }
        }
        storeColumns.deleteCharAt(storeColumns.lastIndexOf(","));
        storeTableSchema.append(storeColumns).append(");\n\n");
        return storeTableSchema.append(storeDefaultValues);
    }

    /**
     * Generate CREATE sql statements for columns who have no constraints, default value or enums defined.
     */
    private StringBuilder generateCreateColumnStatements(Column newColumn) {
        return new StringBuilder(String.format("\t\"%s\"\t\t\t%s,%n", newColumn.getName(), newColumn.getDataType()));
    }

    /**
     * Generate ALTER sql statements for attributes with default values.
     */
    private StringBuilder generateDefaultValueStatements(Column newColumn, String tableName) {
        return new StringBuilder(String.format(
                "ALTER TABLE ONLY ties_data.\"%s\" ALTER COLUMN \"%s\" SET DEFAULT '%s';%n%n", tableName, newColumn
                        .getName(), newColumn.getDefaultValue()));
    }

    /**
     * Write sql statements for UNIQUE, NOT NULL and FOREIGN KEY constraints.
     */
    private StringBuilder generateAlterStatementsForAllOtherConstraints(List<Column> columns) {
        StringBuilder storeOtherAlterStatements = new StringBuilder();

        columns.stream().flatMap(newColumn -> newColumn.getPostgresConstraints().stream()).filter(
                constraint -> !(constraint instanceof PrimaryKeyConstraint)).forEach(constraint -> storeOtherAlterStatements
                        .append(generateConstraintStatement(constraint)));

        return storeOtherAlterStatements;
    }

    private StringBuilder generateAlterStatementsForPrimaryKeyConstraints(List<Column> columns) {
        StringBuilder storePKAlterStatements = new StringBuilder();

        columns.stream().flatMap(newColumn -> newColumn.getPostgresConstraints().stream()).filter(
                PrimaryKeyConstraint.class::isInstance).forEach(constraint -> storePKAlterStatements.append(
                        generateConstraintStatement(constraint)));

        return storePKAlterStatements;
    }

    private String generateConstraintStatement(PostgresConstraint postgresConstraint) {
        String constraintSql = generateConstraintSql(postgresConstraint);
        return String.format("SELECT ties_data.create_constraint_if_not_exists(%n\t'%s',%n '%s',%n '%s;'%n);%n%n",
                postgresConstraint.getTableToAddConstraintTo(), postgresConstraint.getConstraintName(), constraintSql);
    }

    private String generateConstraintSql(PostgresConstraint postgresConstraint) {
        if (postgresConstraint instanceof PrimaryKeyConstraint) {
            return String.format(ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S + "PRIMARY KEY (\"%s\")", postgresConstraint
                    .getTableToAddConstraintTo(), postgresConstraint.getConstraintName(), postgresConstraint
                            .getColumnToAddConstraintTo());
        } else if (postgresConstraint instanceof ForeignKeyConstraint) {
            return String.format(
                    ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S + "FOREIGN KEY (\"%s\") REFERENCES ties_data.\"%s\" (id) ON DELETE CASCADE",
                    postgresConstraint.getTableToAddConstraintTo(), postgresConstraint.getConstraintName(),
                    postgresConstraint.getColumnToAddConstraintTo(), ((ForeignKeyConstraint) postgresConstraint)
                            .getReferencedTable());
        } else if (postgresConstraint instanceof UniqueConstraint) {
            return String.format(ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S + "UNIQUE (\"%s\")", postgresConstraint
                    .getTableToAddConstraintTo(), postgresConstraint.getConstraintName(), postgresConstraint
                            .getColumnToAddConstraintTo());
        } else if (postgresConstraint instanceof NotNullConstraint) {
            return String.format(ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S + "NOT NULL (\"%s\")", postgresConstraint
                    .getTableToAddConstraintTo(), postgresConstraint.getConstraintName(), postgresConstraint
                            .getColumnToAddConstraintTo());
        } else {
            return "";
        }
    }

    /**
     * Generates SQL statements for ALTER TABLE from mapped entity attributes.
     */
    private StringBuilder generateAlterStatementsFromDifferences(List<Table> tables) {
        StringBuilder storeSchemaForAlterStatements = new StringBuilder();
        StringBuilder storeAlterStatementsForPrimaryKeyConstraints = new StringBuilder();
        StringBuilder storeAlterStatementsForAllOtherConstraints = new StringBuilder();
        for (Table table : tables) {
            storeSchemaForAlterStatements.append(generateAlterStatements(table.getColumns(), table.getName()));
            storeAlterStatementsForPrimaryKeyConstraints.append(generateAlterStatementsForPrimaryKeyConstraints(table
                    .getColumns()));
            storeAlterStatementsForAllOtherConstraints.append(generateAlterStatementsForAllOtherConstraints(table
                    .getColumns()));
        }
        return storeSchemaForAlterStatements.append(storeAlterStatementsForPrimaryKeyConstraints).append(
                storeAlterStatementsForAllOtherConstraints);
    }

    /**
     * Generates SQL statements for altering tables based on mapped entity attributes.
     */
    private StringBuilder generateAlterStatements(List<Column> columns, String tableName) {

        StringBuilder storeSchema = new StringBuilder();
        for (Column newColumn : columns) {
            if (newColumn.getName().equals("geo-location")) {
                newColumn.setDataType("\"geography\"");
            }
            storeSchema.append(generateAlterTableStatements(newColumn, tableName));
        }
        return storeSchema;
    }

    /**
     * Generates ALTER SQL statements to add new default values in newly identified columns.
     */
    private StringBuilder generateDefaultStatementsFromDifferences(List<Table> tables) {
        StringBuilder storeSchemaForDefaultStatements = new StringBuilder();
        for (Table table : tables) {
            StringBuilder storeSchema = new StringBuilder();
            for (Column newColumn : table.getColumns()) {
                if (newColumn.getDefaultValue() != null) {
                    storeSchema.append(generateDefaultValueStatements(newColumn, table.getName()));
                }
            }
            storeSchemaForDefaultStatements.append(storeSchema);
        }
        return storeSchemaForDefaultStatements;
    }

    /**
     * Generate ALTER sql statements for attributes who have no constraints, default value or enums defined.
     */
    private StringBuilder generateAlterTableStatements(Column newColumn, String tableName) {
        return new StringBuilder(String.format(ALTER_TABLE_TIES_DATA_S + "ADD COLUMN IF NOT EXISTS \"%s\" %s;%n%n",
                tableName, newColumn.getName(), newColumn.getDataType()));
    }
}
