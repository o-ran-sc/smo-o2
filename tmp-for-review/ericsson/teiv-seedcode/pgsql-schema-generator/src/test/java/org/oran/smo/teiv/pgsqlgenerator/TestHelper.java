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
package org.oran.smo.teiv.pgsqlgenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

@Slf4j
public class TestHelper {

    public static List<String> extractTableNames(List<Table> tables) {
        return tables.stream().map(Table::getName).sorted().toList();
    }

    public static List<String> extractColumnNamesForATable(List<Column> columns) {
        return columns.stream().map(Column::getName).sorted().toList();
    }

    public static List<String> extractConstraintName(Collection<PostgresConstraint> postgresConstraints) {
        return postgresConstraints.stream().map(PostgresConstraint::getConstraintName).map(String::toUpperCase).sorted()
                .toList();
    }

    public static boolean checkIfColumnIsPrimaryKey(Collection<PostgresConstraint> postgresConstraints) {
        return postgresConstraints.stream().anyMatch(PrimaryKeyConstraint.class::isInstance);
    }

    public static Map<String, List<Table>> identifiedModelChangeMapping() {
        Map<String, List<Table>> storeIdentifiedChangesToModels = new HashMap<>();
        storeIdentifiedChangesToModels.put("CREATE", new ArrayList<>());
        storeIdentifiedChangesToModels.put("ALTER", new ArrayList<>());
        storeIdentifiedChangesToModels.put("DEFAULT", new ArrayList<>());
        return storeIdentifiedChangesToModels;
    }

    public static boolean filesCompareByLine(Path path1, Path path2, String... exclusions) throws IOException {
        try (BufferedReader br1 = Files.newBufferedReader(path1); BufferedReader br2 = Files.newBufferedReader(path2)) {
            String line1, line2;
            while ((line1 = br1.readLine()) != null) {
                line2 = br2.readLine();
                if (!line1.equals(line2) && !Arrays.stream(exclusions).toList().contains(line1)) {
                    return false;
                }
            }
            return br2.readLine() == null;
        }
    }

    public static void appendToFile(String fileName, String contentToAppend) throws IOException {
        Files.write(Paths.get(fileName), contentToAppend.getBytes(), StandardOpenOption.APPEND);
    }

    public static Set<String> readFile(String filePath) {
        Set<String> lines = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && Character.isLetterOrDigit(line.charAt(0)) && !line.startsWith("SELECT") && !line
                        .startsWith("FOREIGN KEY")) {
                    Assertions.assertTrue(lines.add(line), String.format("Duplicate identified in file - %s on line --> %s",
                            filePath, line));
                }
            }
        } catch (IOException exception) {
            throw PgSchemaGeneratorException.readBaselineException("ties.data", exception);
        }
        return lines;
    }

}
