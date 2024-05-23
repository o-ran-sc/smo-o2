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
package org.oran.smo.teiv.exposure.spi.impl;

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_MODEL;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.oran.smo.teiv.schema.DataType;
import org.oran.smo.teiv.utils.TiesConstants;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.tools.json.JSONArray;
import org.jooq.SelectConditionStep;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.RelationMappedRecordsDTO;
import org.oran.smo.teiv.exposure.spi.mapper.MapperUtility;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.utils.query.QueryMonad;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataPersistanceServiceImpl implements DataPersistanceService {

    private final DSLContext readDataDslContext;
    private final DSLContext writeDataDslContext;
    private final MapperUtility mapperUtility;

    @Override
    public Map<String, Object> getTopology(final EntityType entityType, final String eiId) {
        return runMethodSafe(() -> {
            final Result<Record> result = readDataDslContext.select(entityType.getAllFieldsWithId()).from(entityType
                    .getTableName()).where(field(ID_COLUMN_NAME).eq(eiId)).fetch();
            if (result.isEmpty()) {
                throw TiesException.resourceNotFoundException();
            }
            return mapperUtility.mapEntity(entityType, result);
        });
    }

    @Override
    public Map<String, Object> getTopologyByType(final String entityType, final String target, final String scope,
            final PaginationDTO paginationDTO) {
        final QueryMonad queryMonad = QueryMonad.builder().managedObject(entityType).targets(target).scope(scope).build();
        final ResultQuery<Record1<Integer>> queryCount = queryMonad.countWithSchema().apply(readDataDslContext);
        paginationDTO.setTotalSize(runMethodSafe(() -> (int) queryCount.fetch().get(0).getValue(0)));
        final ResultQuery<Record> query = queryMonad.withSchema(paginationDTO).apply(readDataDslContext);
        log.trace("SQL ::  " + query.getSQL());
        return runMethodSafe(() -> mapperUtility.mapComplexQuery(query.fetch(), paginationDTO));
    }

    @Override
    public Map<String, Object> getAllRelationships(final EntityType entityType, final List<RelationType> relationTypes,
            final String id, final PaginationDTO paginationDTO) {
        List<RelationMappedRecordsDTO> relationMappedRecordsDTOS = new ArrayList<>();
        int resultSize = 0;

        for (RelationType relationType : relationTypes) {
            String columnNameToFindBy;
            String columnNameToCheckIfNull;

            if (relationType.getASide().equals(entityType)) {
                columnNameToFindBy = relationType.aSideColumnName();
                columnNameToCheckIfNull = relationType.bSideColumnName();
            } else {
                columnNameToFindBy = relationType.bSideColumnName();
                columnNameToCheckIfNull = relationType.aSideColumnName();
            }

            Result<Record> result = runMethodSafe(() -> readDataDslContext.select(relationType.getBaseFieldsWithId()).from(
                    table(relationType.getTableName())).where(field(String.format(QUOTED_STRING, columnNameToFindBy)).eq(id)
                            .and(field(String.format(QUOTED_STRING, columnNameToCheckIfNull)).isNotNull())).fetch());

            if (result.isNotEmpty()) {
                result.forEach(record -> relationMappedRecordsDTOS.add(RelationMappedRecordsDTO.builder().relationType(
                        relationType).record(record).build()));
                resultSize += result.size();
            } else if (!checkIfEntityWithIdExists(entityType, id)) {
                throw TiesException.resourceNotFoundException();

            }

        }

        paginationDTO.setTotalSize(resultSize);
        return mapperUtility.wrapRelationships(relationMappedRecordsDTOS, relationTypes, readDataDslContext, paginationDTO);
    }

    @Override
    public Map<String, Object> getRelationshipWithSpecifiedId(final String id, final RelationType relationType) {
        return runMethodSafe(() -> {
            final Result<Record> result = readDataDslContext.select(relationType.getAllFieldsWithId()).from(table(
                    relationType.getTableName())).where(field(String.format(QUOTED_STRING, relationType.getIdColumnName()))
                            .eq(id)).fetch();
            if (result.isEmpty()) {
                throw TiesException.resourceNotFoundException();
            }
            return mapperUtility.mapRelationship(result, relationType);
        });
    }

    @Override
    public Map<String, Object> getRelationshipsByType(final RelationType relationType, final String scopeFilter,
            final PaginationDTO paginationDTO) {
        return runMethodSafe(() -> {
            String attribute1;
            String attribute2;
            if (!relationType.getRelationshipStorageLocation().equals(RELATION)) {
                attribute1 = relationType.getIdColumnName();
                attribute2 = relationType.aSideColumnName().equals("id") ?
                        relationType.bSideColumnName() :
                        relationType.aSideColumnName();
            } else {
                attribute1 = relationType.aSideColumnName();
                attribute2 = relationType.bSideColumnName();
            }
            final QueryMonad queryMonad = QueryMonad.builder().managedObject(relationType.getTableNameWithoutSchema())
                    .targets(String.format("/id;/attributes(%s,%s)", getModelledName(attribute1), getModelledName(
                            attribute2))).scope(scopeFilter).relationships(relationType.getName()).build();
            final ResultQuery<Record1<Integer>> queryCount = queryMonad.countWithSchema().apply(readDataDslContext);
            paginationDTO.setTotalSize(runMethodSafe(() -> (int) queryCount.fetch().get(0).getValue(0)));
            final ResultQuery<Record> query = queryMonad.withSchema(paginationDTO).apply(readDataDslContext);
            log.trace("SQL ::  " + query.getSQL());
            return mapperUtility.wrapMapObject(mapperUtility.mapRelationships(query.fetch(), relationType), paginationDTO);
        });
    }

    @Override
    public Map<String, Object> getEntitiesByDomain(final String domain, final String targetFilter, final String scopeFilter,
            final PaginationDTO paginationDTO) {
        final QueryMonad queryMonad = QueryMonad.builder().managedObject(null).targets(targetFilter).scope(scopeFilter)
                .domain(domain).build();
        final ResultQuery<Record> query = queryMonad.withSchema(paginationDTO).apply(readDataDslContext);
        final ResultQuery<Record1<Integer>> queryCount = queryMonad.countWithSchema().apply(readDataDslContext);
        paginationDTO.setTotalSize(runMethodSafe(() -> (int) queryCount.fetch().get(0).getValue(0)));
        log.trace("SQL ::  " + query.getSQL());
        return runMethodSafe(() -> mapperUtility.mapComplexQuery(query.fetch(), paginationDTO));
    }

    @Override
    public Set<String> loadClassifiers() {
        SelectConditionStep<Record> availableClassifiers = runMethodSafe(() -> readDataDslContext.select().from(String
                .format(TIES_MODEL, "classifiers")).join(String.format(TIES_MODEL, "module_reference")).on(field(
                        "\"moduleReferenceName\"").eq(field(String.format(TIES_MODEL, "module_reference") + ".name")))
                .where(field("status").like(TiesConstants.IN_USAGE)));
        Set<String> result = new HashSet<>();
        for (Record record : availableClassifiers) {
            result.add((String) record.get("name"));
        }
        return result;
    }

    @Override
    public Map<String, DataType> loadDecorators() {
        SelectConditionStep<Record> availableDecorators = runMethodSafe(() -> readDataDslContext.select().from(String
                .format(TIES_MODEL, "decorators")).join(String.format(TIES_MODEL, "module_reference")).on(field(
                        "\"moduleReferenceName\"").eq(field(String.format(TIES_MODEL, "module_reference") + ".name")))
                .where(field("status").like(TiesConstants.IN_USAGE)));
        Map<String, DataType> result = new HashMap<>();
        for (Record record : availableDecorators) {
            result.put((String) record.get("name"), DataType.fromDbDataType("" + record.get("dataType")));
        }
        return result;
    }

    @Override
    public Map<String, Object> getSchemas(PaginationDTO paginationDTO) {
        final List<StoredSchema> schemas = runMethodSafe(() -> readDataDslContext.select().from(table(String.format(
                TIES_MODEL, "module_reference"))).where(field("status").eq(TiesConstants.IN_USAGE)).fetchInto(
                        StoredSchema.class));

        return mapperUtility.wrapSchema(new ArrayList<>(schemas), paginationDTO);
    }

    @Override
    public Map<String, Object> getSchemas(String domain, PaginationDTO paginationDTO) {
        final List<StoredSchema> schemas = runMethodSafe(() -> readDataDslContext.select().from(table(String.format(
                TIES_MODEL, "module_reference"))).where(field("status").eq(TiesConstants.IN_USAGE)).fetchInto(
                        StoredSchema.class));

        return mapperUtility.wrapSchema(schemas.stream().filter(s -> s.getDomain() != null && s.getDomain().matches(domain))
                .toList(), paginationDTO);
    }

    @Override
    public StoredSchema getSchema(String name) {
        List<StoredSchema> schemas = readDataDslContext.select().from(table(String.format(TIES_MODEL, "module_reference")))
                .where(field("name").eq(name)).fetchInto(StoredSchema.class);
        if (schemas.isEmpty()) {
            return null;
        }

        schemas.get(0).setContent(new String(Base64.getDecoder().decode(schemas.get(0).getContent()),
                StandardCharsets.UTF_8));

        return schemas.get(0);
    }

    @Override
    public void postSchema(String name, String namespace, String domain, List<String> includedModules, String content,
            String ownerAppId) {
        writeDataDslContext.insertInto(table(String.format(TIES_MODEL, "module_reference"))).set(field("name"), name).set(
                field("namespace"), namespace).set(field("domain"), domain).set(field("\"includedModules\""), JSONB.valueOf(
                        JSONArray.toJSONString(includedModules))).set(field("content"), new String(Base64.getEncoder()
                                .encode(content.replaceAll("\\r\\n?", "\n").getBytes(StandardCharsets.UTF_8)),
                                StandardCharsets.UTF_8)).set(field("\"ownerAppId\""), ownerAppId).set(field("status"),
                                        TiesConstants.IN_USAGE).execute();
    }

    @Override
    public void setSchemaToDeleting(String name) {
        writeDataDslContext.update(table(String.format(TIES_MODEL, "module_reference"))).set(field("status"), "DELETING")
                .where(field("name").eq(name)).execute();
    }

    @Override
    public void deleteSchema(String name) {
        writeDataDslContext.deleteFrom(table(String.format(TIES_MODEL, "module_reference"))).where(field("name").eq(name))
                .execute();
    }

    private boolean checkIfEntityWithIdExists(final EntityType entityType, final String id) {
        return readDataDslContext.select(exists(select(field("1")).from(table(entityType.getTableName())).where(field(
                ID_COLUMN_NAME).eq(id)))).fetch().get(0).value1();
    }

    protected <T> T runMethodSafe(Supplier<T> supp) {
        try {
            return supp.get();
        } catch (TiesException ex) {
            throw ex;
        } catch (TiesPathException ex) {
            log.error("Exception during query construction", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Sql exception during query execution", ex);
            throw TiesException.serverSQLException();
        }
    }

}
