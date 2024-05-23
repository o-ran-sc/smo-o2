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
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.schema.SchemaRegistry;

@RequiredArgsConstructor
@Slf4j
public class ComplexMapper extends ResponseMapper {

    /**
     * Maps the results of the queries created by the QueryModal to a REST response
     *
     * @param results
     *     results of the query
     * @return a map of the results
     */
    @Override
    public Map<String, Object> map(Result<Record> results) {
        final Map<String, List<Map<String, Object>>> response = new HashMap<>();
        results.forEach(result -> {
            Map<String, Object> record = new HashMap<>();
            Map<String, Object> mappedRecords = new HashMap<>();
            String currentObject = "";
            String objectName = "";
            for (Field field : result.fields()) {
                String[] nameTokens = field.getName().split("\\.");
                objectName = nameTokens[1].substring(1, nameTokens[1].length() - 1);

                if (!currentObject.isEmpty() && !objectName.equals(currentObject)) {
                    fillResult(currentObject, response, mappedRecords, record);
                    mappedRecords.clear();
                    record.clear();
                }
                fillRecord(nameTokens[2], record, mappedRecords, result, field);
                currentObject = objectName;
            }
            fillResult(currentObject, response, mappedRecords, record);
        });
        return Collections.unmodifiableMap(response);
    }

    private void fillRecord(String nameToken, Map<String, Object> record, Map<String, Object> mappedRecords, Record result,
            Field field) {
        if (nameToken.replace("\"", "").equals(ID_COLUMN_NAME)) {
            record.put(nameToken.replace("\"", ""), result.getValue(field));
        } else {
            mappedRecords.put(getModelledName(nameToken.replace("\"", "")), mapField(result, field));
        }
    }

    private void fillResult(String currentObject, Map<String, List<Map<String, Object>>> response,
            Map<String, Object> mappedRecords, Map<String, Object> record) {
        if (!mappedRecords.values().stream().allMatch(Objects::isNull) || !record.values().stream().allMatch(
                Objects::isNull)) {
            String qualifiedName = SchemaRegistry.getEntityTypeByName(getModelledName(currentObject))
                    .getFullyQualifiedName();
            response.putIfAbsent(qualifiedName, new ArrayList<>());
            if (!mappedRecords.values().stream().allMatch(Objects::isNull)) {
                record.put(ATTRIBUTES, new HashMap<>(mappedRecords));
            }
            response.get(qualifiedName).add(new HashMap<>(record));
        }
    }

}
