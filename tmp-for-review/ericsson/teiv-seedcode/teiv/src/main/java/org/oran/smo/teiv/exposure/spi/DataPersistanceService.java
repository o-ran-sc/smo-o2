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
package org.oran.smo.teiv.exposure.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oran.smo.teiv.exposure.spi.impl.StoredSchema;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.DataType;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;

public interface DataPersistanceService {
    /**
     * Gets topology.
     *
     * @param entityType
     *     the entityType
     * @param id
     *     the id
     *
     * @return the topology
     */
    Map<String, Object> getTopology(EntityType entityType, String id);

    /**
     * Gets topology by type.
     *
     * @param entityName
     *     type of entity, e.g. NrCellDU
     * @param targetFilter
     *     specifies the entity type and attributes to be returned in the REST response
     * @param scopeFilter
     *     specifies the attributes to match on
     * @param relationshipsFilter
     *     specifies the relationships to match on
     * @param paginationDTO
     *     pagination data
     *
     * @return the all entities of type that match the filter criteria
     */
    Map<String, Object> getTopologyByType(final String entityName, final String targetFilter, final String scopeFilter,
            final PaginationDTO paginationDTO);

    /**
     * Gets all relationships for objectType with specified id.
     *
     * @param entityType
     *     the entity type
     * @param relationTypes
     *     the relation type
     * @param id
     *     the id
     * @param paginationDTO
     *     pagination data
     *
     * @return the all relationships
     */
    Map<String, Object> getAllRelationships(final EntityType entityType, final List<RelationType> relationTypes,
            final String id, final PaginationDTO paginationDTO);

    /**
     * Gets relationship by id
     *
     * @param id
     *     the relationType id
     * @param relationType
     *     the relationType
     *
     * @return relationship
     */
    Map<String, Object> getRelationshipWithSpecifiedId(final String id, final RelationType relationType);

    /**
     * Gets relationships for given edge.
     *
     * @param relationType
     *     represents the actual relationship type
     * @param scopeFilter
     *     for filtering
     * @param paginationDTO
     *     pagination data
     *
     * @return filtered relationships by given relationship type
     */
    Map<String, Object> getRelationshipsByType(final RelationType relationType, final String scopeFilter,
            final PaginationDTO paginationDTO);

    /**
     * Get topology entities by domain, using specified targetFilter as mandatory query parameter
     *
     * @param domain
     *     specifies the domain
     * @param targetFilter
     *     specifies the entity type and attributes to be returned in the REST response
     * @param scopeFilter
     *     specifies the attributes to match on
     * @param paginationDTO
     *     pagination data
     *
     * @return the all entities of type that match the filter criteria
     */
    Map<String, Object> getEntitiesByDomain(final String domain, final String targetFilter, final String scopeFilter,
            final PaginationDTO paginationDTO);

    /**
     * Load classifiers set.
     *
     * @return the classifiers
     */
    Set<String> loadClassifiers();

    /**
     * Load decorators map.
     *
     * @return the decorators
     */
    Map<String, DataType> loadDecorators();

    /**
     * Gets schemas.
     *
     * @param paginationDTO
     *     the pagination dto
     * @return the schemas
     */
    Map<String, Object> getSchemas(final PaginationDTO paginationDTO);

    /**
     * Gets schemas.
     *
     * @param domain
     *     the domain
     * @param paginationDTO
     *     the pagination dto
     * @return the schemas
     */
    Map<String, Object> getSchemas(final String domain, final PaginationDTO paginationDTO);

    /**
     * Gets schema.
     *
     * @param name
     *     the name
     * @return the schema
     */
    StoredSchema getSchema(final String name);

    /**
     * Post schema.
     *
     * @param name
     *     the name
     * @param namespace
     *     the namespace
     * @param domain
     *     the domain
     * @param includedModules
     *     the included modules
     * @param content
     *     the content
     * @param ownerAppId
     *     the owner App id
     */
    void postSchema(final String name, final String namespace, final String domain, final List<String> includedModules,
            final String content, final String ownerAppId);

    /**
     * Sets schema to deleting.
     */
    void setSchemaToDeleting(final String name);

    /**
     * Delete schema.
     */
    void deleteSchema(final String name);
}
