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

import static org.jooq.impl.DSL.field;
import static org.oran.smo.teiv.schema.BidiDbNameMapper.getDbName;
import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.schema.DataType.BIGINT;
import static org.oran.smo.teiv.schema.DataType.CONTAINER;
import static org.oran.smo.teiv.schema.DataType.DECIMAL;
import static org.oran.smo.teiv.schema.DataType.GEOGRAPHIC;
import static org.oran.smo.teiv.utils.TiesConstants.CONSUMER_DATA_PREFIX;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.REL_PREFIX;
import static org.oran.smo.teiv.utils.TiesConstants.ST_TO_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.ST_TO_STRING_COLUMN_WITH_TABLE_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.oran.smo.teiv.utils.TiesConstants.SOURCE_IDS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.JSONB;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class EntityType implements Persistable {
    @EqualsAndHashCode.Include
    private final String name;
    private final Map<String, DataType> fields;
    private final Module module;

    @Override
    public String getTableName() {
        return String.format(TIES_DATA, getDbName(this.name));
    }

    @Override
    public String getIdColumnName() {
        return getTableName() + "." + ID_COLUMN_NAME;
    }

    @Override
    public List<String> getAttributeColumnsWithId() {
        final List<String> columnList = new ArrayList<>();
        this.fields.forEach((fieldName, dataType) -> {
            if ((fieldName.startsWith(REL_PREFIX)) || fieldName.startsWith(CONSUMER_DATA_PREFIX)) {
                return;
            }
            final String tableString = getTableName() + ".";
            columnList.add(tableString + String.format(QUOTED_STRING, getDbName(fieldName)));
        });
        return columnList;
    }

    @Override
    public List<Field> getAllFieldsWithId() {
        final List<Field> fieldList = new ArrayList<>();
        this.fields.forEach((fieldName, dataType) -> {
            if (fieldName.startsWith(REL_PREFIX)) {
                return;
            }
            if (GEOGRAPHIC.equals(dataType)) {
                fieldList.add(field(String.format(ST_TO_STRING, getDbName(fieldName))).as(getDbName(fieldName)));
            } else if (CONTAINER.equals(dataType)) {
                fieldList.add(field(String.format(QUOTED_STRING, getDbName(fieldName)), JSONB.class).as(getDbName(
                        fieldName)));
            } else {
                fieldList.add(field(String.format(QUOTED_STRING, getDbName(fieldName))).as(getDbName(fieldName)));
            }
        });
        return fieldList;
    }

    public List<String> getAttributeNames() {
        return this.fields.keySet().stream().filter(field -> (!field.startsWith(REL_PREFIX) || !field.startsWith(
                CONSUMER_DATA_PREFIX))).toList();
    }

    /**
     * Gets the fully qualified name of the entity. Format - <moduleNameReference>:<entityName>
     *
     * @return the fully qualified name
     */
    public String getFullyQualifiedName() {
        return String.format("%s:%s", module.getName(), name);
    }

    public Field getField(String column, String prefix, String tableName) {
        final String columnName = column.contains(".") ? column.split("\\.")[2].replace("\"", "") : column;
        final String alias = (prefix != null ? (prefix + ".") : "") + column;
        final String tableString = tableName != null ? (String.format(TIES_DATA, tableName) + ".") : "";

        if (!this.fields.containsKey(getModelledName(columnName))) {
            return null;
        }

        DataType type = this.fields.get(getModelledName(columnName));

        if (type.equals(GEOGRAPHIC) && column.contains(TIES_DATA_SCHEMA)) {
            return field(String.format(ST_TO_STRING_COLUMN_WITH_TABLE_NAME, column), String.class).as(alias);
        } else if (type.equals(GEOGRAPHIC)) {
            return field(tableString + String.format(ST_TO_STRING, column), String.class).as(alias);
        } else if (type.equals(CONTAINER)) {
            return field(tableString + column, JSONB.class).as(alias);
        } else if (type.equals(DECIMAL)) {
            return field(tableString + column, BigDecimal.class).as(alias);
        } else if (type.equals(BIGINT)) {
            return field(tableString + column, Long.class).as(alias);
        } else {
            return field(tableString + column, String.class).as(alias);
        }
    }

    @Override
    public String getSourceIdsColumnName() {
        return getDbName(CONSUMER_DATA_PREFIX + SOURCE_IDS);
    }
}
