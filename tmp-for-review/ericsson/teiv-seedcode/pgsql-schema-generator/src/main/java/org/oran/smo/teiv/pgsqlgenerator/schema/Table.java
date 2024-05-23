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

public interface Table {

    /**
     * Gets table name.
     * Table name should match the CREATE statement in skeleton schema.
     *
     * @return the table name
     */
    String getTableName();

    /**
     * Gets columns for copy statement.
     * Column names should match the CREATE statement in skeleton schema.
     * It should adhere to syntax: "(\"column1\", \"column2\", ...)".
     *
     * @return the columns for copy statement
     */
    String getColumnsForCopyStatement();

    /**
     * Gets record for copy statement.
     * Each tuple in the record should be separated by tab space and ends with a "\n".
     *
     * @return the record for copy statement
     */
    String getRecordForCopyStatement();
}
