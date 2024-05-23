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

import org.oran.smo.teiv.schema.Association;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.Module;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.MockSchemaLoader;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.B_SIDE;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.jooq.impl.DSL.field;

class RelationshipsMapperTest {
    private static RelationshipsMapper relationshipsMapper;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        MockSchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
        RelationType relationType = RelationType.builder().name("GNBDUFUNCTION_PROVIDES_NRCELLDU").aSideAssociation(
                Association.builder().name("provided-nrCellDu").build()).aSide(EntityType.builder().name("GNBDUFUNCTION")
                        .build()).bSideAssociation(Association.builder().name("provided-by-gnbduFunction").build()).bSide(
                                EntityType.builder().name("NRCellDU").build()).relationshipStorageLocation(B_SIDE).module(
                                        Module.builder().name("o-ran-smo-teiv-ran").build()).build();
        relationshipsMapper = new RelationshipsMapper(relationType);
    }

    @Test
    void testMap() {
        final Result<Record> record = DSL.using(SQLDialect.POSTGRES).newResult();
        record.add(DSL.using(SQLDialect.POSTGRES).newRecord(field(
                "ties_data.\"NRCellDU\".\"REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU\""), field(
                        "ties_data.\"NRCellDU\".\"REL_FK_provided-by-gnbduFunction\""), field(
                                "ties_data.\"NRCellDU\".\"id\"")).values(
                                        "urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                                        "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F"));

        Assertions.assertEquals(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F",
                "urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg=="))),
                relationshipsMapper.map(record));
    }
}
