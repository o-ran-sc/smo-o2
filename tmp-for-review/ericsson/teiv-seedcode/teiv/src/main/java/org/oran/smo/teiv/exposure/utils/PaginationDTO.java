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
package org.oran.smo.teiv.exposure.utils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PaginationDTO {
    private int limit;
    private int offset;
    private int totalSize;
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> pathParameters = new HashMap<>();
    private String basePath;

    private PaginationDTO(int limit, int offset, Map<String, String> queryParameters, Map<String, String> pathParameters,
            String basePath) {
        this.limit = limit;
        this.offset = offset;
        this.queryParameters.putAll(queryParameters);
        this.pathParameters.putAll(pathParameters);
        this.basePath = basePath;
    }

    public static PaginationDTOBuilder builder() {
        return new PaginationDTOBuilder();
    }

    public static class PaginationDTOBuilder {
        private int limit;
        private int offset;
        private final Map<String, String> queryParameters = new HashMap<>();
        private final Map<String, String> pathParameters = new HashMap<>();
        private String basePath;

        PaginationDTOBuilder() {
        }

        public PaginationDTOBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public PaginationDTOBuilder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public PaginationDTOBuilder addQueryParameters(String key, String value) {
            queryParameters.put(key, value);
            return this;
        }

        public PaginationDTOBuilder addPathParameters(String key, String value) {
            pathParameters.put(key, value);
            return this;
        }

        public PaginationDTOBuilder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public PaginationDTO build() {
            return new PaginationDTO(limit, offset, queryParameters, pathParameters, basePath);
        }
    }
}
