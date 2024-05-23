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
package org.oran.smo.teiv.exposure.data.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import org.oran.smo.teiv.api.model.OranTeivDomains;
import org.oran.smo.teiv.api.model.OranTeivDomainsItemsInner;
import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
import org.oran.smo.teiv.api.model.OranTeivEntityTypes;
import org.oran.smo.teiv.api.model.OranTeivEntityTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;
import org.oran.smo.teiv.exposure.data.api.DataService;
import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.mapper.MapperUtility;
import org.oran.smo.teiv.exposure.spi.mapper.PageMetaData;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {
    private final DataPersistanceService dataPersistanceService;
    private final MapperUtility mapperUtility;

    @Override
    public OranTeivDomains getDomainTypes(final PaginationDTO paginationDTO) {
        final Map<String, Object> result = mapperUtility.wrapList(new ArrayList<>(SchemaRegistry.getDomains()),
                paginationDTO);
        final List<OranTeivDomainsItemsInner> items = new ArrayList<>();
        final List<Object> itemsList = (List<Object>) result.get("items");
        itemsList.forEach(domain -> items.add(OranTeivDomainsItemsInner.builder().name(domain.toString()).entityTypes(
                OranTeivHref.builder().href(paginationDTO.getBasePath() + "/" + domain + "/entity-types").build())
                .relationshipTypes(OranTeivHref.builder().href(paginationDTO
                        .getBasePath() + "/" + domain + "/relationship-types").build()).build()));
        return getDomainsResponseMessage(result, items, paginationDTO.getLimit(), paginationDTO.getOffset());
    }

    @Override
    public OranTeivEntityTypes getTopologyEntityTypes(final String domain, final PaginationDTO paginationDTO) {
        final Map<String, Object> result = mapperUtility.wrapList(new ArrayList<>(SchemaRegistry.getEntityNamesByDomain(
                domain)), paginationDTO);
        final List<OranTeivEntityTypesItemsInner> items = new ArrayList<>();
        final List<String> entities = SchemaRegistry.getEntityNamesByDomain(domain);
        entities.stream().forEach(entity -> {
            OranTeivEntityTypesItemsInner inner = new OranTeivEntityTypesItemsInner();
            OranTeivHref entitiesHref = new OranTeivHref();
            entitiesHref.setHref(paginationDTO.getBasePath() + "/" + entity + "/entities");
            inner.setName(entity);
            inner.setEntities(entitiesHref);
            items.add(inner);
        });
        return getEntityTypesResponseMessage(result, items, paginationDTO.getLimit(), paginationDTO.getOffset());
    }

    @Override
    public OranTeivRelationshipTypes getTopologyRelationshipTypes(final String domain, final PaginationDTO paginationDTO) {
        final Map<String, Object> result = mapperUtility.wrapList(new ArrayList<>(SchemaRegistry.getRelationNamesByDomain(
                domain)), paginationDTO);
        final List<OranTeivRelationshipTypesItemsInner> items = new ArrayList<>();
        final List<String> relationships = SchemaRegistry.getRelationNamesByDomain(domain);
        relationships.stream().forEach(relationship -> {
            OranTeivRelationshipTypesItemsInner inner = new OranTeivRelationshipTypesItemsInner();
            OranTeivHref relationshipsHref = new OranTeivHref();
            relationshipsHref.setHref(paginationDTO.getBasePath() + "/" + relationship + "/relationships");
            inner.setName(relationship);
            inner.setRelationships(relationshipsHref);
            items.add(inner);
        });
        return getRelationshipTypesResponseMessage(result, items, paginationDTO.getLimit(), paginationDTO.getOffset());
    }

    @Override
    public Map<String, Object> getTopologyById(final String entityName, final String id) {
        final EntityType entityType = SchemaRegistry.getEntityTypeByName(entityName);
        return dataPersistanceService.getTopology(entityType, id);
    }

    @Override
    public OranTeivEntitiesResponseMessage getTopologyByType(final String entityName, final String target,
            final String scope, final PaginationDTO paginationDTO) {
        final Map<String, Object> response = dataPersistanceService.getTopologyByType(entityName, target, scope,
                paginationDTO);
        return getEntitiesResponseMessage(response);
    }

    @Override
    public OranTeivEntitiesResponseMessage getEntitiesByDomain(final String domain, final String fields,
            final String filters, final PaginationDTO paginationDTO) {
        final Map<String, Object> response = dataPersistanceService.getEntitiesByDomain(domain, fields, filters,
                paginationDTO);
        return getEntitiesResponseMessage(response);
    }

    @Override
    public OranTeivRelationshipsResponseMessage getAllRelationshipsForObjectId(final String entityName, final String id,
            final PaginationDTO paginationDTO) {
        final EntityType entityType = SchemaRegistry.getEntityTypeByName(entityName);
        final List<RelationType> relationTypes = SchemaRegistry.getRelationTypesByEntityName(entityName);
        final Map<String, Object> response = dataPersistanceService.getAllRelationships(entityType, relationTypes, id,
                paginationDTO);
        return getRelationshipsResponseMessage(response);
    }

    @Override
    public Map<String, Object> getRelationshipById(final String relationName, final String id) {
        return dataPersistanceService.getRelationshipWithSpecifiedId(id, SchemaRegistry.getRelationTypeByName(
                relationName));
    }

    @Override
    public OranTeivRelationshipsResponseMessage getRelationshipsByType(final String relationName, final String scopeFilter,
            final PaginationDTO paginationDTO) {
        final Map<String, Object> response = dataPersistanceService.getRelationshipsByType(SchemaRegistry
                .getRelationTypeByName(relationName), scopeFilter, paginationDTO);
        return getRelationshipsResponseMessage(response);
    }

    private OranTeivEntitiesResponseMessage getEntitiesResponseMessage(Map<String, Object> response) {
        OranTeivEntitiesResponseMessage result = new OranTeivEntitiesResponseMessage();
        List<Object> items = (List<Object>) response.get("items");
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

    private OranTeivRelationshipsResponseMessage getRelationshipsResponseMessage(Map<String, Object> response) {
        OranTeivRelationshipsResponseMessage result = new OranTeivRelationshipsResponseMessage();
        List<Object> items = (List<Object>) response.get("items");
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

    private OranTeivDomains getDomainsResponseMessage(Map<String, Object> response, List<OranTeivDomainsItemsInner> items,
            final Integer limit, final Integer offset) {
        PageMetaData self = (PageMetaData) response.get("self");
        PageMetaData first = (PageMetaData) response.get("first");
        PageMetaData prev = (PageMetaData) response.get("prev");
        PageMetaData next = (PageMetaData) response.get("next");
        PageMetaData last = (PageMetaData) response.get("last");

        return OranTeivDomains.builder().items(items.subList(offset, Math.min(offset + limit, items.size()))).first(
                OranTeivHref.builder().href(first.getHref()).build()).prev(OranTeivHref.builder().href(prev.getHref())
                        .build()).self(OranTeivHref.builder().href(self.getHref()).build()).next(OranTeivHref.builder()
                                .href(next.getHref()).build()).last(OranTeivHref.builder().href(last.getHref()).build())
                .totalCount(items.size()).build();
    }

    private OranTeivEntityTypes getEntityTypesResponseMessage(Map<String, Object> response,
            List<OranTeivEntityTypesItemsInner> items, final Integer limit, final Integer offset) {
        PageMetaData self = (PageMetaData) response.get("self");
        PageMetaData first = (PageMetaData) response.get("first");
        PageMetaData prev = (PageMetaData) response.get("prev");
        PageMetaData next = (PageMetaData) response.get("next");
        PageMetaData last = (PageMetaData) response.get("last");

        return OranTeivEntityTypes.builder().items(items.subList(offset, Math.min(offset + limit, items.size()))).first(
                OranTeivHref.builder().href(first.getHref()).build()).prev(OranTeivHref.builder().href(prev.getHref())
                        .build()).self(OranTeivHref.builder().href(self.getHref()).build()).next(OranTeivHref.builder()
                                .href(next.getHref()).build()).last(OranTeivHref.builder().href(last.getHref()).build())
                .totalCount(items.size()).build();
    }

    private OranTeivRelationshipTypes getRelationshipTypesResponseMessage(Map<String, Object> response,
            List<OranTeivRelationshipTypesItemsInner> items, final Integer limit, final Integer offset) {
        PageMetaData self = (PageMetaData) response.get("self");
        PageMetaData first = (PageMetaData) response.get("first");
        PageMetaData prev = (PageMetaData) response.get("prev");
        PageMetaData next = (PageMetaData) response.get("next");
        PageMetaData last = (PageMetaData) response.get("last");

        return OranTeivRelationshipTypes.builder().items(items.subList(offset, Math.min(offset + limit, items.size())))
                .first(OranTeivHref.builder().href(first.getHref()).build()).prev(OranTeivHref.builder().href(prev
                        .getHref()).build()).self(OranTeivHref.builder().href(self.getHref()).build()).next(OranTeivHref
                                .builder().href(next.getHref()).build()).last(OranTeivHref.builder().href(last.getHref())
                                        .build()).totalCount(items.size()).build();
    }
}
