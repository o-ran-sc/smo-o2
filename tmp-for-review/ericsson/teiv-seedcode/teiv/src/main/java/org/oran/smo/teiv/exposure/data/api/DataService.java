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
package org.oran.smo.teiv.exposure.data.api;

import org.oran.smo.teiv.api.model.OranTeivDomains;
import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
import org.oran.smo.teiv.api.model.OranTeivEntityTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;

import java.util.Map;

public interface DataService {

    /**
     * Get all the available topology domain types.
     *
     * @param paginationDTO
     *     pagination data
     *
     * @return a collection of domain types
     */
    OranTeivDomains getDomainTypes(final PaginationDTO paginationDTO);

    /**
     * Get all entity types in a topology domain types.
     *
     * @param domain
     *     domain name
     * @param paginationDTO
     *     pagination data
     *
     * @return a collection of domain types
     */
    OranTeivEntityTypes getTopologyEntityTypes(final String domain, final PaginationDTO paginationDTO);

    /**
     * Get all relationship types in a topology domain types.
     *
     * @param domain
     *     domain name
     * @param paginationDTO
     *     pagination data
     *
     * @return a collection of relationship types
     */
    OranTeivRelationshipTypes getTopologyRelationshipTypes(final String domain, final PaginationDTO paginationDTO);

    /**
     * Get topology for entity type with specified id.
     *
     * @param entityType
     *     type of entity, e.g. NRCellDU
     * @param id
     *     unique identifier of entity
     *
     * @return a topology entity
     */
    Map<String, Object> getTopologyById(final String entityType, final String id);

    /**
     * Get topology entities that match target, scope and relationships filters.
     *
     * @param entityType
     *     type of entity, e.g. NRCellDU
     * @param targetFilter
     *     specifies the entity type and attributes to be returned to the REST response
     * @param scopeFilter
     *     specifies the attributes to match on
     * @param paginationDTO
     *     pagination data
     *
     * @return a collection of entity data and attributes
     */
    OranTeivEntitiesResponseMessage getTopologyByType(final String entityType, final String targetFilter,
            final String scopeFilter, final PaginationDTO paginationDTO);

    /**
     *
     * @param domain
     *     domain name
     * @param targetFilter
     *     specifies the entity type and attributes to be returned to the REST response
     * @param scopeFilter
     *     specifies the attributes to match on
     * @param paginationDTO
     *     pagination data
     *
     * @return a collection of entity data and attributes
     */
    OranTeivEntitiesResponseMessage getEntitiesByDomain(final String domain, final String targetFilter,
            final String scopeFilter, final PaginationDTO paginationDTO);

    /**
     * Get all relationships for entityType with specified id
     *
     * @param entityType
     *     type of entity, e.g. NRCellDU
     * @param id
     *     unique identifier of entity
     *
     * @return a collection of relationships for entity type
     */
    OranTeivRelationshipsResponseMessage getAllRelationshipsForObjectId(final String entityType, final String id,
            final PaginationDTO paginationDTO);

    /**
     * Get relationship with specified id
     *
     * @param relationshipType
     *     type of relationship
     * @param id
     *     unique identifier of the relationships
     *
     * @return a topology relationship
     */
    Map<String, Object> getRelationshipById(final String relationshipType, final String id);

    /**
     *
     * @param relationshipType
     *     type of relationship
     * @param scopeFilter
     *     specifies the attributes to match on
     * @param paginationDTO
     *     pagination data
     *
     * @return relationships by relationship type
     */
    OranTeivRelationshipsResponseMessage getRelationshipsByType(final String relationshipType, final String scopeFilter,
            final PaginationDTO paginationDTO);
}
