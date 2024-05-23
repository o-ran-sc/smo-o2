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
package org.oran.smo.teiv.service;

import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;

import org.springframework.stereotype.Component;
import org.jooq.Record;

@Component
public class RelationshipMergeValidator {

    public boolean anotherRelationshipAlreadyExistsOnStoringSideEntity(Record manySideRow, RelationType relationType,
            Relationship relationship) {
        return !manySideRow.get(relationType.getIdColumnName()).equals(relationship.getId());
    }

    public boolean relationshipAlreadyExistsWithDifferentNotStoringSideEntity(Record manySideRow, Relationship relationship,
            RelationType relationType) {
        return manySideRow.get(ID_COLUMN_NAME).equals(relationship.getStoringSideEntityId()) && !manySideRow.get(
                relationType.getNotStoringSideEntityIdColumnNameInStoringSideTable()).equals(relationship
                        .getNotStoringSideEntityId());
    }

}
