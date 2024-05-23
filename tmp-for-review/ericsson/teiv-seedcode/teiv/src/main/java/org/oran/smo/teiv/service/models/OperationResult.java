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
package org.oran.smo.teiv.service.models;

import java.util.HashMap;
import java.util.Map;

import org.oran.smo.teiv.service.cloudevent.data.Relationship;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_A_SIDE;
import static org.oran.smo.teiv.utils.TiesConstants.PROPERTY_B_SIDE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationResult {
    private String id;
    private String entryType;
    private Map<String, Object> content;

    public static OperationResult createFromRelationship(Relationship relationship) {
        Map<String, Object> relationshipSides = new HashMap<>();
        relationshipSides.put(PROPERTY_A_SIDE, relationship.getASide());
        relationshipSides.put(PROPERTY_B_SIDE, relationship.getBSide());
        return new OperationResult(relationship.getId(), relationship.getType(), relationshipSides);
    }
}
