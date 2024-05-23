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
package org.oran.smo.teiv.utils.query;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Select;

import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.one;

public class QueryMonadTestUtil {
    public static QueryMonad getQueryMonad(String entityType, String fields, String attributes, String relationships) {
        return QueryMonad.builder().managedObject(entityType).targets(fields).scope(attributes).relationships(relationships)
                .build();
    }

    public static Query createDistinctQuery(DSLContext context, Select query) {
        return context.selectDistinct(asterisk()).from(query.asTable("TiesPathQuery")).orderBy(one().asc()).limit(0, 5);
    }

    public static QueryMonad getQueryMonadWithDomain(String entityType, String fields, String attributes,
            String relationships, String domain) {
        return QueryMonad.builder().managedObject(entityType).targets(fields).scope(attributes).relationships(relationships)
                .domain(domain).build();
    }

}
