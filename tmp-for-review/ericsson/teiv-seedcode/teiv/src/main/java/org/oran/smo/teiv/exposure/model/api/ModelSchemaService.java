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
package org.oran.smo.teiv.exposure.model.api;

import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.api.model.OranTeivSchemaList;
import org.springframework.web.multipart.MultipartFile;

public interface ModelSchemaService {

    /**
     * Create schema.
     *
     * @param yangFile
     *     the yang content
     */
    void createSchema(final MultipartFile yangFile);

    /**
     * Get all schemas extracted from yang models
     *
     * @param paginationDTO
     *     pagination data
     *
     * @return a map of all schemas
     */
    OranTeivSchemaList getSchemas(final PaginationDTO paginationDTO);

    /**
     * Get all schemas in a domain
     *
     * @param domain
     *     domain name
     * @param paginationDTO
     *     pagination data
     *
     * @return a map of all schemas in a domain
     */
    OranTeivSchemaList getSchemasInDomain(final String domain, final PaginationDTO paginationDTO);

    /**
     * Get a yang schema name
     *
     * @param schema
     *     schema name
     *
     * @return a schema in a string format
     */
    String getSchemaByName(final String schema);

    /**
     * Delete schema.
     *
     * @param name
     *     the schema name
     */
    void deleteSchema(final String name);
}
