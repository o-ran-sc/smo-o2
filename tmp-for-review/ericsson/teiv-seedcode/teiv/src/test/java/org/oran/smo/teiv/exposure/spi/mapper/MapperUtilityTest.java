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

import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.oran.smo.teiv.utils.TiesConstants.ATTRIBUTES;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.jooq.impl.DSL.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.oran.smo.teiv.utils.exposure.PaginationVerifierTestUtil.verifyResponse;
import static org.jooq.impl.DSL.field;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.schema.MockSchemaLoader;

class MapperUtilityTest {
    private MapperUtility underTest;
    private static PaginationMetaData paginationMetaData;

    @BeforeEach
    void setUp() throws SchemaLoaderException {
        underTest = new MapperUtility();
        SchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
        paginationMetaData = new PaginationMetaData();
    }

    @Test
    void testMapEntity() {
        final Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
        result.add(DSL.using(SQLDialect.POSTGRES).newRecord(field("gNBDUId"), field("fdn"), field("dUpLMNId"), field(
                "gNBId"), field("id"), field("gNBIdLength")).values(null, "GNBDUFunction/6653097A7B47B082BA96245354BB5BE7",
                        Map.of("mcc", 456, "mnc", 82), "6653097A7B47B082BA96245354BB5BE7",
                        "GNBDUFunction:6653097A7B47B082BA96245354BB5BE7", 2));
        Assertions.assertEquals(generateResponse("GNBDUFunction", "6653097A7B47B082BA96245354BB5BE7"), underTest.mapEntity(
                SchemaRegistry.getEntityTypeByName("GNBDUFunction"), result));
    }

    @Test
    void testMapAllRelationships() {
        final Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
        result.add(DSL.using(SQLDialect.POSTGRES).newRecord(field(
                "ties_data.\"NRCellDU\".\"REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU\""), field(
                        "ties_data.\"NRCellDU\".\"REL_FK_provided-by-gnbduFunction\""), field(
                                "ties_data.\"NRCellDU\".\"id\""), field(
                                        "ties_data.\"NRCellDU\".\"REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU\""))
                .values("urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                        "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F", Collections.EMPTY_LIST));

        Assertions.assertEquals(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F",
                "urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.EMPTY_LIST))), underTest.mapRelationships(result, SchemaRegistry.getRelationTypeByName(
                        "GNBDUFUNCTION_PROVIDES_NRCELLDU")));
    }

    @Test
    void testMapRelationship() {
        final Record record = DSL.using(SQLDialect.POSTGRES).newRecord(field(
                "ties_data.\"NRCellDU\".\"REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU\""), field(
                        "ties_data.\"NRCellDU\".\"REL_FK_provided-by-gnbduFunction\""), field(
                                "ties_data.\"NRCellDU\".\"id\""), field(
                                        "ties_data.\"NRCellDU\".\"REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU\""))
                .values("urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                        "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F", Collections.EMPTY_LIST);

        final Result result = DSL.using(SQLDialect.POSTGRES).newResult();
        result.add(record);

        Assertions.assertEquals(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "9BCD297B8258F67908477D895636ED65", "B480427E8A0C0B8D994E437784BB382F",
                "urn:base64:R05CRFVGdW5jdGlvbjo5QkNEMjk3QjgyNThGNjc5MDg0NzdEODk1NjM2RUQ2NTpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.EMPTY_LIST))), underTest.mapRelationship(result, SchemaRegistry.getRelationTypeByName(
                        "GNBDUFUNCTION_PROVIDES_NRCELLDU")));
    }

    @Test
    void testMapComplexQuery() {
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

        result.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of(ATTRIBUTES, Map.of("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=91"), "id",
                "9BCD297B8258F67908477D895636ED65")), "o-ran-smo-teiv-ran:NRCellDU", List.of(Map.of("id",
                        "98C3A4591A37718E1330F0294E23B62A")))));

        try (MockedStatic<RequestContextHolder> requestContextHolderMockedStatic = Mockito.mockStatic(
                RequestContextHolder.class)) {
            requestContextHolderMockedStatic.when(RequestContextHolder::currentRequestAttributes).thenReturn(
                    new ServletRequestAttributes(new MockHttpServletRequest()));
            PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/pathTo/endPoint").offset(0).limit(5).build();
            paginationDTO.setTotalSize(2);
            result.putAll(paginationMetaData.getObjectList(paginationDTO));
            verifyResponse(result, underTest.mapComplexQuery(records, paginationDTO));
        }
    }

    @Test
    void testMapComplexQuery_SingleEntity() {
        final Result<Record> records = DSL.using(SQLDialect.POSTGRES).newResult();

        final String gNBDUName = String.format(TIES_DATA, "GNBDUFunction");

        records.add(DSL.using(SQLDialect.POSTGRES).newRecord(field(gNBDUName + ".id"), field(gNBDUName + ".fdn")).values(
                "9BCD297B8258F67908477D895636ED65",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=91"));

        Map<String, Object> reference = new HashMap<>();

        reference.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                "9BCD297B8258F67908477D895636ED65", ATTRIBUTES, Map.of("fdn",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=91"))))));

        try (MockedStatic<RequestContextHolder> requestContextHolderMockedStatic = Mockito.mockStatic(
                RequestContextHolder.class)) {
            requestContextHolderMockedStatic.when(RequestContextHolder::currentRequestAttributes).thenReturn(
                    new ServletRequestAttributes(new MockHttpServletRequest()));
            PageMetaData pageMetaDataSelf = new PageMetaData(PaginationDTO.builder().offset(0).limit(5).build());
            reference.put("self", pageMetaDataSelf);
            PaginationMetaData pmd = new PaginationMetaData();
            PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/pathTo/endPoint").offset(0).limit(5).build();
            paginationDTO.setTotalSize(0);
            reference.putAll(pmd.getObjectList(paginationDTO));
            verifyResponse(reference, underTest.mapComplexQuery(records, paginationDTO));

        }
    }
}
