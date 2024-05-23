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

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.spi.impl.StoredSchema;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;

import java.util.Collections;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Service;

import org.oran.smo.teiv.exposure.spi.RelationMappedRecordsDTO;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MapperUtility {

    /**
     * Maps the query results to an api response
     *
     * @param entityType
     *     entityType
     * @param result
     *     result of the query
     *
     * @return response
     */
    public Map<String, Object> mapEntity(final EntityType entityType, final Result<Record> result) {
        return new EntityMapper(entityType).map(result);
    }

    /**
     * Maps all relationships
     *
     * @param result
     * @param relationType
     * @return all relationships
     */
    public Map<String, Object> mapRelationships(final Result<Record> result, final RelationType relationType) {
        return new RelationshipsMapper(relationType).map(result);
    }

    /**
     * Maps one relationship
     *
     * @param result
     * @param relationType
     * @return one relationship
     */
    public Map<String, Object> mapRelationship(final Result<Record> result, final RelationType relationType) {
        return new RelationshipMapper(relationType).map(result);
    }

    /**
     * Maps the results of the queries created by the QueryModal to a REST response
     *
     * @param results
     *     results of the query
     * @param paginationDTO
     *     pagination data
     *
     * @return a map of the results
     */
    public Map<String, Object> mapComplexQuery(final Result<Record> results, final PaginationDTO paginationDTO) {
        return wrapResponse(results, new ComplexMapper(), paginationDTO);
    }

    public Map<String, Object> wrapMapObject(final Map<String, Object> results, final PaginationDTO paginationDTO) {

        validateOffset(paginationDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("items", List.of(results));

        PaginationMetaData paginationMetaData = new PaginationMetaData();
        response.putAll(paginationMetaData.getObjectList(paginationDTO));

        response.put("query", queryPart(paginationDTO));
        return response;
    }

    public Map<String, Object> wrapList(final List<Object> objectsList, final PaginationDTO paginationDTO) {
        Map<String, Object> response = new HashMap<>();
        paginationDTO.setTotalSize(objectsList.size());

        if (objectsList.size() <= paginationDTO.getOffset() && !objectsList.isEmpty()) {
            throw TiesException.invalidValueException("Offset", objectsList.size() - 1, true);
        } else {
            response.put("items", objectsList.subList(paginationDTO.getOffset(), Math.min(paginationDTO
                    .getOffset() + paginationDTO.getLimit(), objectsList.size())));
        }

        PaginationMetaData paginationMetaData = new PaginationMetaData();
        response.putAll(paginationMetaData.getObjectList(paginationDTO));

        return response;
    }

    public Map<String, Object> wrapSchema(final List<StoredSchema> schemaList, final PaginationDTO paginationDTO) {
        List<Map<String, Object>> response = new ArrayList<>();

        for (StoredSchema schema : schemaList) {
            Map<String, Object> innerResponse = new HashMap<>();
            innerResponse.put("name", schema.getName());
            innerResponse.put("domain", schema.getDomain() == null ?
                    Collections.emptyList() :
                    Collections.singletonList(schema.getDomain()));
            innerResponse.put("revision", schema.getRevision());
            innerResponse.put("content", Collections.singletonMap("href", "/schemas/" + schema.getName() + "/content"));
            response.add(innerResponse);
        }

        return wrapList(new ArrayList<>(response), paginationDTO);
    }

    private Map<String, Object> wrapResponse(final Result<Record> results, final ResponseMapper responseMapper,
            final PaginationDTO paginationDTO) {
        validateOffset(paginationDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("items", List.of(responseMapper.map(results)));

        PaginationMetaData paginationMetaData = new PaginationMetaData();
        response.putAll(paginationMetaData.getObjectList(paginationDTO));

        response.put("query", queryPart(paginationDTO));
        return response;
    }

    private Map<String, Object> queryPart(final PaginationDTO paginationDTO) {
        Map<String, Object> response = new HashMap<>();
        response.putAll(paginationDTO.getPathParameters());
        Map<String, String> notNullQueryParameters = paginationDTO.getQueryParameters().entrySet().stream().filter(
                entry -> entry.getValue() != null).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry
                        .getValue()));
        response.putAll(notNullQueryParameters);

        return response;
    }

    public Map<String, Object> wrapRelationships(List<RelationMappedRecordsDTO> records, List<RelationType> relationTypes,
            DSLContext readDataDslContext, final PaginationDTO paginationDTO) {
        Map<String, Object> response = new HashMap<>();

        if (paginationDTO.getTotalSize() <= paginationDTO.getOffset() && !records.isEmpty()) {
            throw TiesException.invalidValueException("Offset", records.size() - 1, true);
        } else {
            //TODO: Refactor the logic since RelationMappedRecordsDTO already contains the relation type
            List<RelationMappedRecordsDTO> pagedRecords = records.subList(paginationDTO.getOffset(), Math.min(paginationDTO
                    .getOffset() + paginationDTO.getLimit(), records.size()));
            Map<String, Result<Record>> batch = new HashMap<>();
            for (RelationMappedRecordsDTO record : pagedRecords) {
                RelationType relTypeForCurrentRecord = record.getRelationType();
                if (!batch.containsKey(relTypeForCurrentRecord.getFullyQualifiedName())) {
                    batch.put(relTypeForCurrentRecord.getFullyQualifiedName(), readDataDslContext.newResult());
                }
                batch.get(relTypeForCurrentRecord.getFullyQualifiedName()).add(record.getRecord());
            }
            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Map.Entry<String, Result<Record>> entry : batch.entrySet()) {
                mappedResults.add(mapRelationships(entry.getValue(), relationTypes.stream().filter(relType -> relType
                        .getFullyQualifiedName().equals(entry.getKey())).findFirst().orElseThrow()));
            }
            response.put("items", mappedResults);

        }

        PaginationMetaData paginationMetaData = new PaginationMetaData();
        response.putAll(paginationMetaData.getObjectList(paginationDTO));

        return response;
    }

    private void validateOffset(PaginationDTO paginationDTO) {
        if (paginationDTO.getTotalSize() < paginationDTO.getOffset()) {
            throw TiesException.invalidValueException("Offset", paginationDTO.getTotalSize() - 1, true);
        }
    }
}
