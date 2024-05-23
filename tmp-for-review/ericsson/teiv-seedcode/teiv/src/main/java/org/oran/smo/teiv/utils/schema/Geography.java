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
package org.oran.smo.teiv.utils.schema;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Geography {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Double latitude;
    private Double longitude;

    /**
     * Creates a Geography object from a standard (RFC 9179) YANG geo-location type.
     * Only "latitude" and "longitude" fields are supported. All other fields in the json are ignored.
     *
     * @param json
     *     A json that conforms to the "RFC 9179: A YANG Grouping for Geographic Location" standard.
     * @throws IOException
     *     when the json doesn't contain both "latitude" and "longitude" fields.
     */
    public Geography(String json) throws IOException {
        JsonParser jsonParser = objectMapper.readTree(json).traverse();

        while (jsonParser.nextToken() != null) {
            if ("latitude".equals(jsonParser.currentName())) {
                latitude = jsonParser.getDoubleValue();
            } else if ("longitude".equals(jsonParser.currentName())) {
                longitude = jsonParser.getDoubleValue();
            }
            if (latitude != null && longitude != null) {
                return;
            }
        }
        throw new IOException("Cannot find latitude and longitude fields in json: " + json);
    }

    @Override
    public String toString() {
        return "POINT(" + latitude + " " + longitude + ")";
    }

}
