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
package org.oran.smo.teiv.exposure.model.api.impl;

import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivSchema;
import org.oran.smo.teiv.api.model.OranTeivSchemaList;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.impl.StoredSchema;
import org.oran.smo.teiv.exposure.spi.mapper.PageMetaData;
import org.oran.smo.teiv.exposure.model.api.ModelSchemaService;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.oran.smo.teiv.utils.TiesConstants.IN_USAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelSchemaServiceImpl implements ModelSchemaService {

    private final DataPersistanceService dataPersistanceService;

    @Override
    public void createSchema(MultipartFile yangFile) {
        log.trace("yang file: {}", yangFile);
    }

    @Override
    public OranTeivSchemaList getSchemas(final PaginationDTO paginationDTO) {
        return getSchemaListResponseMessage(dataPersistanceService.getSchemas(paginationDTO));
    }

    @Override
    public OranTeivSchemaList getSchemasInDomain(final String domainName, final PaginationDTO paginationDTO) {
        return getSchemaListResponseMessage(dataPersistanceService.getSchemas(domainName, paginationDTO));
    }

    @Override
    public String getSchemaByName(final String schemaName) {
        final StoredSchema schema = dataPersistanceService.getSchema(schemaName);
        if (schema == null || !schema.getStatus().equals(IN_USAGE)) {
            throw TiesException.invalidSchema(schemaName);
        }
        return schema.getContent();
    }

    @Override
    public void deleteSchema(String name) {
        final StoredSchema schema = dataPersistanceService.getSchema(name);
        if (schema == null) {
            throw TiesException.invalidSchema(name);
        }
        if (schema.getOwnerAppId().equals("BUILT_IN_MODULE")) {
            throw TiesException.schemaNotOwned(name);
        }
        dataPersistanceService.setSchemaToDeleting(name);
    }

    private OranTeivSchemaList getSchemaListResponseMessage(Map<String, Object> response) {
        OranTeivSchemaList result = new OranTeivSchemaList();
        List<OranTeivSchema> items = (List<OranTeivSchema>) response.get("items");
        PageMetaData self = (PageMetaData) response.get("self");
        PageMetaData first = (PageMetaData) response.get("first");
        PageMetaData prev = (PageMetaData) response.get("prev");
        PageMetaData next = (PageMetaData) response.get("next");
        PageMetaData last = (PageMetaData) response.get("last");
        Integer totalCount = (Integer) response.get("totalCount");

        result.setItems(items);

        OranTeivHref selfHref = new OranTeivHref();
        OranTeivHref firstHref = new OranTeivHref();
        OranTeivHref prevHref = new OranTeivHref();
        OranTeivHref nextHref = new OranTeivHref();
        OranTeivHref lastHref = new OranTeivHref();

        selfHref.setHref(self.getHref());
        firstHref.setHref(first.getHref());
        prevHref.setHref(prev.getHref());
        nextHref.setHref(next.getHref());
        lastHref.setHref(last.getHref());

        result.setSelf(selfHref);
        result.setFirst(firstHref);
        result.setPrev(prevHref);
        result.setNext(nextHref);
        result.setLast(lastHref);
        result.setTotalCount(totalCount);
        return result;
    }
}
