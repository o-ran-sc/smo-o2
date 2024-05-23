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

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.utils.TiesConstants.ATTRIBUTES;
import static org.oran.smo.teiv.utils.TiesConstants.CONSUMER_DATA_PREFIX;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;

import lombok.RequiredArgsConstructor;

import org.oran.smo.teiv.schema.EntityType;

@RequiredArgsConstructor
public class EntityMapper extends ResponseMapper {

    final EntityType entityType;

    /**
     * Maps the query results to an api response
     *
     * @param result
     *     result of the query
     * @return response
     */
    @Override
    public Map<String, Object> map(final Result<Record> result) {
        final Map<String, Object> responseData = new HashMap<>();
        final Map<String, Object> mappedRecords = new HashMap<>();
        result.forEach(record -> Arrays.stream(record.fields()).forEach(field -> {
            if (getModelledName(field.getName()).equals(ID_COLUMN_NAME)) {
                responseData.put(getModelledName(field.getName()), record.getValue(field));
            } else if (getModelledName(field.getName()).startsWith(CONSUMER_DATA_PREFIX)) {
                responseData.put(getModelledName(field.getName()).substring(CONSUMER_DATA_PREFIX.length()), mapField(record,
                        field));
            } else {
                mappedRecords.put(getModelledName(field.getName()), mapField(record, field));
            }
        }));
        responseData.put(ATTRIBUTES, mappedRecords);
        return Map.of(entityType.getFullyQualifiedName(), List.of(responseData));
    }

}
