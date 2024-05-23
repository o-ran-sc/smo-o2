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

import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.MockSchemaLoader;

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
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.jooq.impl.DSL.field;

class ComplexMapperTest {
    private static ComplexMapper complexMapper;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        MockSchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
        complexMapper = new ComplexMapper();
    }

    @Test
    void testMap() {
        final Result<Record> records = DSL.using(SQLDialect.POSTGRES).newResult();

        final String gNBDUName = String.format(TIES_DATA, "GNBDUFunction");
        final String nRCellDUName = String.format(TIES_DATA, "NRCellDU");

        records.add(DSL.using(SQLDialect.POSTGRES).newRecord(field(gNBDUName + ".id"), field(gNBDUName + ".fdn"), field(
                nRCellDUName + ".id")).values("9BCD297B8258F67908477D895636ED65",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=91",
                        null));
        records.add(DSL.using(SQLDialect.POSTGRES).newRecord(field(gNBDUName + ".id"), field(gNBDUName + ".fdn"), field(
                nRCellDUName + ".id")).values(null, null, "98C3A4591A37718E1330F0294E23B62A"));

        Map<String, Object> result = new HashMap<>();

        result.put("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of(ATTRIBUTES, Map.of("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=91"), "id",
                "9BCD297B8258F67908477D895636ED65")));

        result.put("o-ran-smo-teiv-ran:NRCellDU", List.of(Map.of("id", "98C3A4591A37718E1330F0294E23B62A")));

        Assertions.assertEquals(result, complexMapper.map(records));
    }

}
