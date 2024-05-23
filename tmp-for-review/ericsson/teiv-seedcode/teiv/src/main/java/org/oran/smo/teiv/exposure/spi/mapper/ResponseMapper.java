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
package org.oran.smo.teiv.exposure.spi.mapper;

import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_A_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_B_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.SOURCE_IDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.exception.DataTypeException;

import org.oran.smo.teiv.schema.RelationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ResponseMapper {
    public abstract Map<String, Object> map(final Result<Record> result);

    protected Map<String, Object> createProperties(final Record record, RelationType relationType) {
        final Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(ID_COLUMN_NAME, String.valueOf(record.get(relationType.getTableName() + "." + String.format(
                QUOTED_STRING, relationType.getIdColumnName()))));
        dataMap.put(PROPERTY_A_SIDE, String.valueOf(record.get(relationType.getTableName() + "." + String.format(
                QUOTED_STRING, relationType.aSideColumnName()))));
        dataMap.put(PROPERTY_B_SIDE, String.valueOf(record.get(relationType.getTableName() + "." + String.format(
                QUOTED_STRING, relationType.bSideColumnName()))));

        Field<?> sourceIds = record.field(relationType.getTableName() + "." + String.format(QUOTED_STRING, relationType
                .getSourceIdsColumnName()));
        if (sourceIds != null) {
            dataMap.put(SOURCE_IDS, mapField(record, sourceIds));
        }

        return dataMap;
    }

    protected Object mapField(Record record, org.jooq.Field<?> field) {
        try {
            return !field.getType().equals(JSONB.class) ? record.getValue(field) : record.get(field, Map.class);
        } catch (DataTypeException e) {
            log.trace("Mapped as list", e);
            return record.get(field, List.class);
        }
    }
}
