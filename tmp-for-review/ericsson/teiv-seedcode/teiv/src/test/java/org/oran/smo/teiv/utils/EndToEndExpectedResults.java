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
package org.oran.smo.teiv.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.jooq.JSONB;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EndToEndExpectedResults {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final JsonNode rootNode;

    public EndToEndExpectedResults(final String jsonPath) throws IOException {
        rootNode = OBJECT_MAPPER.readTree(Files.readString(Paths.get(jsonPath)));
    }

    public Map<String, Object> get(final String entryId) {
        Map<String, Object> expectedValuesMap = new HashMap<>();
        JsonNode attributesNode = rootNode.required(entryId);
        attributesNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode valueNode = entry.getValue();
            if (valueNode.isContainerNode()) {
                expectedValuesMap.put(key, JSONB.jsonb(valueNode.toString()));
            } else if (valueNode.isTextual()) {
                expectedValuesMap.put(key, valueNode.asText());
            } else if (valueNode.isDouble()) {
                expectedValuesMap.put(key, valueNode.asDouble());
            } else if (valueNode.isNumber()) {
                expectedValuesMap.put(key, valueNode.asLong());
            } else if (valueNode.isBoolean()) {
                expectedValuesMap.put(key, valueNode.asBoolean());
            }
        });
        return expectedValuesMap;
    }

    public Map<String, Object> getAll() {
        return EndToEndTestUtil.processNode(rootNode);
    }
}
