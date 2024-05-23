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

import static org.oran.smo.teiv.pgsqlgenerator.Constants.ALTER;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.CREATE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.DEFAULT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import org.oran.smo.teiv.pgsqlgenerator.Column;
import org.oran.smo.teiv.pgsqlgenerator.Table;

@Component
public class ModelComparator {

    private Map<String, List<Table>> identifiedChangesToModels;

    /**
     * Identifies differences between baseline and generated models.
     *
     * @param tablesFromModelService
     *     Model information from Model Service
     * @param tablesFromBaselineSql
     *     Model information from the baseline
     * @return A map with identified changes to models
     */
    public Map<String, List<Table>> identifyDifferencesInBaselineAndGenerated(List<Table> tablesFromModelService,
            List<Table> tablesFromBaselineSql) {
        //TODO: Throw error if there is table from model service doesn't contain any info in baseline
        identifiedChangesToModels = identifiedModelChangeMapping();

        List<String> tableNamesOfBaseline = extractTableNames(tablesFromBaselineSql);
        List<String> tableNameOfModelSvc = extractTableNames(tablesFromModelService);

        if (!tableNameOfModelSvc.equals(tableNamesOfBaseline)) {
            storeNewTables(tablesFromModelService, tableNamesOfBaseline, tableNameOfModelSvc);
        }
        compareAndStoreChangesToColumns(tablesFromModelService, tablesFromBaselineSql);

        return Collections.unmodifiableMap(identifiedChangesToModels);
    }

    private Map<String, List<Table>> identifiedModelChangeMapping() {
        Map<String, List<Table>> storeIdentifiedChangesToModels = new HashMap<>();
        storeIdentifiedChangesToModels.put(CREATE, new ArrayList<>());
        storeIdentifiedChangesToModels.put(ALTER, new ArrayList<>());
        storeIdentifiedChangesToModels.put(DEFAULT, new ArrayList<>());
        return storeIdentifiedChangesToModels;
    }

    /**
     * Check if all tables in extracted data from module service are same as what's in baseline schema Store identified with
     * a "CREATE" key
     */
    private void storeNewTables(List<Table> tablesFromModelService, List<String> tableNamesOfBaseline,
            List<String> tableNameOfGenerated) {
        List<String> differences = tableNameOfGenerated.stream().filter(element -> !tableNamesOfBaseline.contains(element))
                .toList();
        differences.forEach(tableName -> tablesFromModelService.stream().filter(table -> table.getName().equals(tableName))
                .findFirst().ifPresent(table -> identifiedChangesToModels.get(CREATE).add(Table.builder().name(table
                        .getName()).columns(table.getColumns()).build())));
    }

    /**
     * Compare columns of each table from module service with columns of each table from baseline schema
     */
    private void compareAndStoreChangesToColumns(List<Table> tablesFromModelService, List<Table> tablesFromBaselineSql) {
        tablesFromModelService.forEach(tableFromModelService -> {
            tablesFromBaselineSql.stream().filter(baselineTable -> tableFromModelService.getName().equals(baselineTable
                    .getName())).findFirst().ifPresent(baselineTable -> {

                        List<Column> columnsInBaseline = new ArrayList<>(baselineTable.getColumns());
                        List<Column> columnsFromModuleSvc = new ArrayList<>(tableFromModelService.getColumns());

                        columnsInBaseline.sort(Comparator.comparing(Column::getName));
                        columnsFromModuleSvc.sort(Comparator.comparing(Column::getName));

                        // Check for new columns in table
                        if (columnsFromModuleSvc.size() > columnsInBaseline.size()) {
                            storeNewColumns(tableFromModelService.getName(), columnsInBaseline, columnsFromModuleSvc);
                        }
                        detectAndStoreDefaultValueChanges(tableFromModelService.getName(), columnsInBaseline,
                                columnsFromModuleSvc);
                    });
        });
    }

    private List<String> extractTableNames(List<Table> tables) {
        return tables.stream().map(Table::getName).sorted().toList();
    }

    /**
     * Check if new columns are introduced by comparing data from module service and baseline schema
     */
    private void storeNewColumns(String tableName, List<Column> columnsInBaseline, List<Column> columnsFromModuleSvc) {
        List<Column> newColumns = columnsFromModuleSvc.stream().filter(columnInGenerated -> !getListOfAllColumns(
                columnsInBaseline).contains(columnInGenerated.getName())).toList();
        identifiedChangesToModels.get(ALTER).add(Table.builder().name(tableName).columns(newColumns.stream().map(
                column -> Column.builder().name(column.getName()).dataType(column.getDataType()).postgresConstraints(column
                        .getPostgresConstraints()).build()).toList()).build());
        List<Column> columnsWithDefaultValues = newColumns.stream().filter(columnIdentified -> columnIdentified
                .getDefaultValue() != null).toList();
        if (!columnsWithDefaultValues.isEmpty()) {
            identifiedChangesToModels.get(DEFAULT).add(Table.builder().name(tableName).columns(columnsWithDefaultValues)
                    .build());
        }
    }

    /**
     * Check if default values for all columns in tables from module service are same as what's in baseline schema Store
     * identified with a "DEFAULT" key
     */
    private void detectAndStoreDefaultValueChanges(String tableName, List<Column> columnsInBaseline,
            List<Column> columnsFromModuleSvc) {
        List<Column> list = new ArrayList<>();
        columnsInBaseline.forEach(columnInBaseline -> {
            columnsFromModuleSvc.forEach(columnInGenerated -> {
                if (columnInGenerated.getName().equals(columnInBaseline.getName()) && !Objects.equals(columnInGenerated
                        .getDefaultValue(), columnInBaseline.getDefaultValue())) {
                    list.add(columnInGenerated);
                }
            });
        });
        if (!list.isEmpty()) {
            identifiedChangesToModels.get(DEFAULT).add(Table.builder().name(tableName).columns(list).build());
        }
    }

    private List<String> getListOfAllColumns(List<Column> columns) {
        List<String> allColumns = new ArrayList<>();
        for (Column col : columns) {
            allColumns.add(col.getName().replace("\"", ""));
        }
        return allColumns;
    }

}
