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

import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.MockSchemaLoader;
import org.oran.smo.teiv.schema.Module;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.oran.smo.teiv.utils.TiesConstants.ATTRIBUTES;
import static org.jooq.impl.DSL.field;

class EntityMapperTest {
    private static EntityMapper entityMapper;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        EntityType entityType = EntityType.builder().name("GNBDUFunction").module(Module.builder().name(
                "o-ran-smo-teiv-ran").build()).build();
        entityMapper = new EntityMapper(entityType);
        SchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
    }

    @Test
    void testMap() {
        final Result<Record> record = DSL.using(SQLDialect.POSTGRES).newResult();
        Map<String, Object> result = new HashMap<>();

        record.add(DSL.using(SQLDialect.POSTGRES).newRecord(field("dUpLMNId"), field("fdn"), field("gNBDUId"), field(
                "gNBId"), field("cmId"), field("gNBIdLength"), field("id"), field("CD_sourceIds")).values(Map.of("mcc",
                        "456", "mnc", "82"),
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=95", 95,
                        95, "null", 2, "5970A12E0AF8B0FBE0B49290FE847F9B", List.of(
                                "urn:3gpp:dn:/SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=95",
                                "urn:cmHandle:/395221E080CCF0FD1924103B15873814")));

        result.put("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of(ATTRIBUTES, Map.of("dUpLMNId", Map.of("mcc", "456",
                "mnc", "82"), "fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=95", "gNBDUId",
                95, "gNBId", 95, "cmId", "null", "gNBIdLength", 2), "id", "5970A12E0AF8B0FBE0B49290FE847F9B", "sourceIds",
                List.of("urn:3gpp:dn:/SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=95",
                        "urn:cmHandle:/395221E080CCF0FD1924103B15873814"))));

        Assertions.assertEquals(result, entityMapper.map(record));
    }
}
