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
package org.oran.smo.teiv.schema;

import java.util.List;

import org.jooq.Field;

public interface Persistable {

    /**
     * Gets the fully qualified table name.
     * Format - <schemaName>."<tableName>"
     *
     * @return the table name
     */
    String getTableName();

    /**
     * Gets the column name used for storing the unique identifier.
     *
     * @return the id column name
     */
    String getIdColumnName();

    /**
     * Gets all the column names.
     *
     * @return the list of column names
     */
    List<String> getAttributeColumnsWithId();

    /**
     * Gets all the columns names as list of {@link Field}
     *
     * @return the list of {@link Field}
     */
    List<Field> getAllFieldsWithId();

    /**
     * Gets sourceIds column name as String
     *
     * @return the String value of sourceIds column
     */
    String getSourceIdsColumnName();
}
