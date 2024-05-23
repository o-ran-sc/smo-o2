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
package org.oran.smo.teiv.ingestion.validation;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;

import org.oran.smo.teiv.utils.TiesConstants;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TiesDbServiceForValidation {

    private final DSLContext dslContext;

    /**
     * Acquire a row level FOR UPDATE lock for a single entity instance.
     *
     * @param tableName
     *     The name of the table where the entity is stored
     * @param entityId
     *     Unique id of the entity
     */
    public void acquireEntityInstanceExclusiveLock(String tableName, String entityId) {
        Field<Object> idField = field(TiesConstants.ID_COLUMN_NAME);
        dslContext.select(idField).from(tableName).where(idField.eq(entityId)).forUpdate().execute();
    }

    public boolean executeValidationQuery(String tableName, String foreignKeyColumnName, String foreignKeyValue,
            long maxOccurrence) {
        int maxCount;
        try {
            maxCount = Math.toIntExact(maxOccurrence);
        } catch (ArithmeticException e) {
            log.error("Maximum cardinality can't be greater than {}, but it was {}", Integer.MAX_VALUE, maxOccurrence, e);
            return false;
        }
        Field<Object> foreignKeyColumn = field(name(foreignKeyColumnName));
        //spotless:off
        Record1<Boolean> result = dslContext
            .select(field(count(foreignKeyColumn).lessOrEqual(maxCount)))
            .from(tableName)
            .where(foreignKeyColumn.eq(foreignKeyValue))
            .groupBy(foreignKeyColumn)
            .fetchOne();
        //spotless:on
        return result.value1();
    }
}
