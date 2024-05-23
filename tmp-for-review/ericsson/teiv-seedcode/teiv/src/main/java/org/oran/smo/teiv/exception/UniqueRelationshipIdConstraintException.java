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
package org.oran.smo.teiv.exception;

import java.text.MessageFormat;

import org.oran.smo.teiv.service.cloudevent.data.Relationship;

public class UniqueRelationshipIdConstraintException extends IllegalRelationshipUpdateException {

    private static final String DESCRIPTION = "A relationship with the given id already exists, cannot process the incoming relationship: id={0}, aSide={1}, bSide={2}. It's not possible to update the sides of an existing relationship, because the relationship ID should be derived from the identities of both sides";

    public UniqueRelationshipIdConstraintException(Relationship relationship) {
        super(MessageFormat.format(DESCRIPTION, relationship.getId(), relationship.getASide(), relationship.getBSide()));
    }
}
