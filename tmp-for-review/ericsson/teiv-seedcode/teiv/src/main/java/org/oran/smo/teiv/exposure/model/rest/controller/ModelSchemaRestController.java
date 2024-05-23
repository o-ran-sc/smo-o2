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
package org.oran.smo.teiv.exposure.model.rest.controller;

import org.oran.smo.teiv.api.SchemasApi;
import org.oran.smo.teiv.api.model.OranTeivSchemaList;
import org.oran.smo.teiv.exposure.model.api.ModelSchemaService;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.utils.TiesConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(TiesConstants.REQUEST_MAPPING)
@RequiredArgsConstructor
public class ModelSchemaRestController implements SchemasApi {

    private final ModelSchemaService modelSchemaService;

    @Override
    public ResponseEntity<Void> createSchema(String accept, String contentType, MultipartFile file) {
        modelSchemaService.createSchema(file);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Return a list of all schemas/schemas filtered by domain extracted from yang models
     */
    @Override
    public ResponseEntity<OranTeivSchemaList> getSchemas(@NotNull final String accept, @Valid final String domain,
            @Min(0) @Valid final Integer offset, @Min(1) @Max(500) @Valid final Integer limit) {
        final PaginationDTO.PaginationDTOBuilder builder = PaginationDTO.builder().offset(offset).limit(limit);
        if (Objects.isNull(domain)) {
            return new ResponseEntity<>(modelSchemaService.getSchemas(builder.basePath("/schemas").build()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(modelSchemaService.getSchemasInDomain(domain, builder.basePath("/schemas")
                    .addPathParameters("domain", domain).build()), HttpStatus.OK);
        }
    }

    /**
     * Return a schema by schema name
     */
    @Override
    @SneakyThrows
    public ResponseEntity<String> getSchemaByName(@NotNull final String accept, final String schemaName) {
        final String module = modelSchemaService.getSchemaByName(schemaName);
        return new ResponseEntity<>(module, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSchema(String accept, String schemaName) {
        modelSchemaService.deleteSchema(schemaName);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
