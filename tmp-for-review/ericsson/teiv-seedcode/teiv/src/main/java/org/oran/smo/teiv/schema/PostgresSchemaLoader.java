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
package org.oran.smo.teiv.schema;

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getModelledName;
import static org.oran.smo.teiv.utils.TiesConstants.TEIV_DOMAIN;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA_SCHEMA;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_MODEL;
import static org.jooq.impl.DSL.field;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.exception.TiesException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgresSchemaLoader extends SchemaLoader {
    private final DSLContext readDataDslContext;
    private final ObjectMapper objectMapper;

    public PostgresSchemaLoader(DSLContext readDataDslContext, ObjectMapper objectMapper) {
        this.readDataDslContext = readDataDslContext;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void loadBidiDbNameMapper() {
        log.debug("Start loading bidirectional DB name mapper");
        SelectJoinStep<Record> records = readDataDslContext.select().from(String.format(TIES_MODEL, "hash_info"));
        Map<String, String> hash = new HashMap<>();
        Map<String, String> reverseHash = new HashMap<>();
        records.forEach(record -> {
            hash.put((String) record.get("name"), (String) record.get("hashedValue"));
            reverseHash.put((String) record.get("hashedValue"), (String) record.get("name"));
        });
        BidiDbNameMapper.initialize(hash, reverseHash);
        log.debug("BidiDBNameMapper initialized successfully");
    }

    @Override
    public void loadModules() throws SchemaLoaderException {
        log.debug("Start loading modules");
        SelectConditionStep<Record> moduleRecords = runMethodSafe(() -> readDataDslContext.select().from(String.format(
                TIES_MODEL, "module_reference")).where(field("domain").isNotNull()));
        Map<String, Module> moduleMap = new HashMap<>();
        for (Record moduleRecord : moduleRecords) {
            JSONB includedModules = (JSONB) moduleRecord.get("includedModules");
            try {
                List<String> modules = objectMapper.readValue(includedModules.data(), List.class);
                Module module = Module.builder().name((String) moduleRecord.get("name")).namespace((String) moduleRecord
                        .get("namespace")).domain((String) moduleRecord.get("domain")).includedModuleNames(modules).build();
                moduleMap.put(module.getName(), module);
            } catch (IOException e) {
                log.error("Exception occurred while retrieving included modules.", e);
                throw new SchemaLoaderException("Unable to load modules please check the logs for more details.", e);
            }
        }
        //root domain includes all the available domains.
        moduleMap.put(TEIV_DOMAIN, Module.builder().name(TEIV_DOMAIN).namespace(TEIV_DOMAIN).domain(TEIV_DOMAIN)
                .includedModuleNames(moduleMap.keySet()).build());
        SchemaRegistry.initializeModules(moduleMap);
        log.debug("Modules initialized successfully");
    }

    @Override
    public void loadEntityTypes() {
        log.debug("Start loading entities");
        Map<String, EntityType> entityTypeMap = new HashMap<>();
        Map<String, EntityType.EntityTypeBuilder> entityTypeBuilderMap = new HashMap<>();
        SelectJoinStep<Record> entityInfoRecords = runMethodSafe(() -> readDataDslContext.select().from(String.format(
                TIES_MODEL, "entity_info")));
        entityInfoRecords.forEach(entityInfoRecord -> {
            String name = (String) entityInfoRecord.get("name");
            EntityType.EntityTypeBuilder entityTypeBuilder = EntityType.builder().name(name).module(SchemaRegistry
                    .getModuleByName((String) entityInfoRecord.get("moduleReferenceName")));
            entityTypeBuilderMap.put(name, entityTypeBuilder);
        });
        //load attributes
        String tableName = "table_name";
        String columnName = "column_name";
        SelectConditionStep<Record3<Object, Object, Object>> record3s = runMethodSafe(() -> readDataDslContext.select(field(
                tableName), field(columnName), field("udt_name")).from("information_schema.columns").where(field(
                        "table_schema").equal(TIES_DATA_SCHEMA)));
        entityTypeBuilderMap.keySet().forEach(entityName -> {
            Map<String, DataType> fields = new HashMap<>();
            record3s.stream().filter(record3 -> entityName.equals(getModelledName((String) record3.get(tableName))))
                    .forEach(record3 -> {
                        String colName = getModelledName((String) record3.get(columnName));
                        fields.put(colName, DataType.fromDbDataType((String) record3.get("udt_name")));
                    });
            entityTypeBuilderMap.get(entityName).fields(Collections.unmodifiableMap(fields));
        });

        for (var entityTypeBuilder : entityTypeBuilderMap.entrySet()) {
            entityTypeMap.put(entityTypeBuilder.getKey(), entityTypeBuilder.getValue().build());
        }

        SchemaRegistry.initializeEntityTypes(entityTypeMap);
        log.debug("Entities initialized successfully");
    }

    @Override
    public void loadRelationTypes() {
        log.debug("Start loading relations");
        Map<String, RelationType> relationTypeMap = new HashMap<>();
        SelectJoinStep<Record> relationInfoResult = runMethodSafe(() -> readDataDslContext.select().from(String.format(
                TIES_MODEL, "relationship_info")));
        relationInfoResult.forEach(result -> {
            //build associations
            Association aSideAssociation = Association.builder().name(((String) result.get("aSideAssociationName")))
                    .minCardinality((long) (result.get("aSideMinCardinality"))).maxCardinality((long) (result.get(
                            "aSideMaxCardinality"))).build();
            Association bSideAssociation = Association.builder().name(((String) result.get("bSideAssociationName")))
                    .minCardinality((long) (result.get("bSideMinCardinality"))).maxCardinality((long) (result.get(
                            "bSideMaxCardinality"))).build();

            RelationType relationType = RelationType.builder().name((String) result.get("name")).aSideAssociation(
                    aSideAssociation).aSide(SchemaRegistry.getEntityTypeByName((String) result.get("aSideMOType")))
                    .bSideAssociation(bSideAssociation).bSide(SchemaRegistry.getEntityTypeByName((String) result.get(
                            "bSideMOType"))).relationshipStorageLocation(RelationshipDataLocation.valueOf((String) result
                                    .get("relationshipDataLocation"))).connectsSameEntity((Boolean) (result.get(
                                            "connectSameEntity"))).module(SchemaRegistry.getModuleByName((String) result
                                                    .get("moduleReferenceName"))).build();
            relationTypeMap.put(relationType.getName(), relationType);
        });

        //load registry
        SchemaRegistry.initializeRelationTypes(relationTypeMap);
        log.debug("Relations initialized successfully");
    }

    private <T> T runMethodSafe(Supplier<T> supp) {
        try {
            return supp.get();
        } catch (TiesException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Sql exception during query execution", ex);
            throw TiesException.serverSQLException();
        }
    }
}
