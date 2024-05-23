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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;

import org.oran.smo.teiv.schema.RelationType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RelationshipsMapper extends ResponseMapper {

    final RelationType relationType;

    @Override
    public Map<String, Object> map(final Result<Record> result) {
        final Map<String, List<Object>> relationshipsMap = new HashMap<>();
        final List<Object> relationships = new ArrayList<>();
        result.stream().filter(record -> Arrays.stream(record.valuesRow().fields()).noneMatch(field -> field.getName()
                .equals("null"))).forEach(record -> relationships.add(createProperties(record, relationType)));
        relationshipsMap.put(relationType.getFullyQualifiedName(), relationships);
        return Collections.unmodifiableMap(relationshipsMap);
    }
}
