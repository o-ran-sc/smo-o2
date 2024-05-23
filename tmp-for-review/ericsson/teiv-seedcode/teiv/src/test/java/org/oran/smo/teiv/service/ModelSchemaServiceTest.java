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

import org.oran.smo.teiv.api.model.OranTeivSchema;
import org.oran.smo.teiv.api.model.OranTeivSchemaList;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.model.api.impl.ModelSchemaServiceImpl;
import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.impl.DataPersistanceServiceImpl;
import org.oran.smo.teiv.exposure.spi.impl.StoredSchema;
import org.oran.smo.teiv.exposure.spi.mapper.MapperUtility;
import org.oran.smo.teiv.exposure.spi.mapper.PageMetaData;
import org.oran.smo.teiv.exposure.spi.mapper.PaginationMetaData;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oran.smo.teiv.utils.TiesConstants.IN_USAGE;
import static org.oran.smo.teiv.utils.exposure.PaginationVerifierTestUtil.verifyResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelSchemaServiceTest {

    private static final DataPersistanceService dataPersistanceService = mock(DataPersistanceServiceImpl.class);
    private static final MapperUtility mapperUtility = new MapperUtility();
    private final ModelSchemaServiceImpl service = new ModelSchemaServiceImpl(dataPersistanceService);

    @BeforeAll
    static void beforeAll() {
        StoredSchema ranLogicalToCloud = new StoredSchema();
        ranLogicalToCloud.setName("o-ran-smo-teiv-cloud-to-ran");
        ranLogicalToCloud.setNamespace("urn:o-ran:smo-teiv-cloud-to-ran");
        ranLogicalToCloud.setDomain("CLOUD_TO_RAN");
        ranLogicalToCloud.setRevision("2023-10-24");
        ranLogicalToCloud.setStatus(IN_USAGE);

        StoredSchema ranEquipment = new StoredSchema();
        ranEquipment.setName("o-ran-smo-teiv-equipment");
        ranEquipment.setNamespace("urn:o-ran:smo-teiv-equipment");
        ranEquipment.setDomain("EQUIPMENT");
        ranEquipment.setRevision("2023-06-26");
        ranEquipment.setStatus(IN_USAGE);

        StoredSchema ranOamToCloud = new StoredSchema();
        ranOamToCloud.setName("o-ran-smo-teiv-oam-to-cloud");
        ranOamToCloud.setNamespace("urn:o-ran:smo-teiv-oam-to-cloud");
        ranOamToCloud.setDomain("OAM_TO_CLOUD");
        ranOamToCloud.setRevision("2023-10-24");
        ranOamToCloud.setStatus(IN_USAGE);

        StoredSchema ranOamToLogical = new StoredSchema();
        ranOamToLogical.setName("o-ran-smo-teiv-oam-to-ran");
        ranOamToLogical.setNamespace("urn:o-ran:smo-teiv-oam-to-ran");
        ranOamToLogical.setDomain("OAM_TO_RAN");
        ranOamToLogical.setRevision("2023-10-24");
        ranOamToLogical.setStatus(IN_USAGE);

        StoredSchema ranCloud = new StoredSchema();
        ranCloud.setName("o-ran-smo-teiv-cloud");
        ranCloud.setNamespace("urn:o-ran:smo-teiv-cloud");
        ranCloud.setDomain("CLOUD");
        ranCloud.setRevision("2023-06-26");
        ranCloud.setStatus(IN_USAGE);

        StoredSchema ranOam = new StoredSchema();
        ranOam.setName("o-ran-smo-teiv-oam");
        ranOam.setNamespace("urn:o-ran:smo-teiv-oam");
        ranOam.setDomain("OAM");
        ranOam.setRevision("2023-06-26");
        ranOam.setContent("yang model o-ran-smo-teiv-oam {}");
        ranOam.setStatus(IN_USAGE);
        ranOam.setOwnerAppId("BUILT_IN_MODULE");

        when(dataPersistanceService.getSchemas(PaginationDTO.builder().offset(0).limit(8).build())).thenReturn(mapperUtility
                .wrapSchema(Arrays.asList(ranLogicalToCloud, ranEquipment, ranOamToCloud, ranOamToLogical, ranCloud,
                        ranOam), PaginationDTO.builder().offset(0).limit(8).build()));
        when(dataPersistanceService.getSchema("o-ran-smo-teiv-oam")).thenReturn(ranOam);
        when(dataPersistanceService.getSchema("o-ran-smo-teiv")).thenReturn(null);

        when(dataPersistanceService.getSchemas("ties_logical", PaginationDTO.builder().basePath("/schemas/ties_logical")
                .offset(0).limit(8).build())).thenReturn(mapperUtility.wrapSchema(new ArrayList<>(), PaginationDTO.builder()
                        .basePath("/schemas/ties_logical").offset(0).limit(8).build()));
        when(dataPersistanceService.getSchemas("CLOUD", PaginationDTO.builder().basePath("/schemas/CLOUD").offset(0).limit(
                8).build())).thenReturn(mapperUtility.wrapSchema(List.of(ranCloud), PaginationDTO.builder().basePath(
                        "/schemas/CLOUD").offset(0).limit(8).build()));
        when(dataPersistanceService.getSchemas("ran*", PaginationDTO.builder().basePath("/schemas/ran*").offset(0).limit(8)
                .build())).thenReturn(mapperUtility.wrapSchema(Arrays.asList(ranLogicalToCloud, ranEquipment, ranOamToCloud,
                        ranOamToLogical, ranCloud, ranOam), PaginationDTO.builder().basePath("/schemas/ran*").offset(0)
                                .limit(8).build()));
    }

    @Test
    void testGetSchemas() throws IOException {
        //when
        OranTeivSchemaList schemaItems = service.getSchemas(PaginationDTO.builder().offset(0).limit(8).build());
        //then
        List<OranTeivSchema> schemasMetaData = schemaItems.getItems();
        Assertions.assertEquals(6, schemasMetaData.size());

        MapperUtility mapperUtility = new MapperUtility();
        List<Object> resultException = new ArrayList<>(schemasMetaData);
        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/schemas").offset(100).limit(5).build();
        paginationDTO.setTotalSize(5);
        Assertions.assertThrows(TiesException.class, () -> mapperUtility.wrapList(resultException, paginationDTO));

        Map<String, Object> fullResult = new HashMap<>();
        fullResult.put("items", schemasMetaData);

        PaginationMetaData pmd = new PaginationMetaData();

        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath("/schemas").offset(0).limit(15).build();
        paginationDTO2.setTotalSize(6);
        fullResult.putAll(pmd.getObjectList(paginationDTO2));

        verifyResponse(fullResult, mapperUtility.wrapList(resultException, PaginationDTO.builder().basePath("/schemas")
                .offset(0).limit(15).build()));

        Map<String, Object> fullResult2 = new HashMap<>();
        fullResult2.put("items", schemasMetaData.subList(0, 5));

        PageMetaData pageMetaDataSelf2 = new PageMetaData(0, PaginationDTO.builder().limit(5).basePath("/schemas").build());
        PageMetaData pageMetaDataFirst2 = new PageMetaData(0, PaginationDTO.builder().limit(5).basePath("/schemas")
                .build());
        PageMetaData pageMetaDataPrev2 = new PageMetaData(0, PaginationDTO.builder().limit(5).basePath("/schemas").build());
        PageMetaData pageMetaDataNext2 = new PageMetaData(5, PaginationDTO.builder().limit(5).basePath("/schemas").build());
        PageMetaData pageMetaDataLast2 = new PageMetaData(5, PaginationDTO.builder().limit(5).basePath("/schemas").build());

        fullResult2.put("self", pageMetaDataSelf2);
        fullResult2.put("first", pageMetaDataFirst2);
        fullResult2.put("prev", pageMetaDataPrev2);
        fullResult2.put("next", pageMetaDataNext2);
        fullResult2.put("last", pageMetaDataLast2);

        verifyResponse(fullResult2, mapperUtility.wrapList(resultException, PaginationDTO.builder().basePath("/schemas")
                .offset(0).limit(5).build()));
    }

    @Test
    void testGetSchemasByName() {
        //when
        String incorrectSchemaName = "o-ran-smo-teiv";
        String responseForCorrectSchemaName = service.getSchemaByName("o-ran-smo-teiv-oam");
        //then
        Assertions.assertThrowsExactly(TiesException.class, () -> service.getSchemaByName(incorrectSchemaName));
        Assertions.assertTrue(responseForCorrectSchemaName.contains("o-ran-smo-teiv-oam"));
    }

    @Test
    void testGetSchemasInDomain() {
        //when
        List<OranTeivSchema> schemasInIncorrectDomain = (List<OranTeivSchema>) service.getSchemasInDomain("ties_logical",
                PaginationDTO.builder().basePath("/schemas/ties_logical").offset(0).limit(8).build()).getItems();
        List<OranTeivSchema> schemasInDomainRanCloud = (List<OranTeivSchema>) service.getSchemasInDomain("CLOUD",
                PaginationDTO.builder().basePath("/schemas/CLOUD").offset(0).limit(8).build()).getItems();
        List<OranTeivSchema> schemasInDomainPartiallyMatchingRan = (List<OranTeivSchema>) service.getSchemasInDomain("ran*",
                PaginationDTO.builder().basePath("/schemas/ran*").offset(0).limit(8).build()).getItems();
        //then
        Assertions.assertEquals(List.of(), schemasInIncorrectDomain);
        Assertions.assertEquals(1, schemasInDomainRanCloud.size());
        Assertions.assertEquals(6, schemasInDomainPartiallyMatchingRan.size());
    }

    @Test
    void testCreateSchema() {
        service.createSchema(new MockMultipartFile("yangModule.yang", "yangContent".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testDeleteSchema() {
        Assertions.assertEquals("Invalid schema name", Assertions.assertThrowsExactly(TiesException.class, () -> service
                .deleteSchema("schemaToDelete")).getMessage());

        Assertions.assertEquals("Forbidden", Assertions.assertThrowsExactly(TiesException.class, () -> service.deleteSchema(
                "o-ran-smo-teiv-oam")).getMessage());
    }
}
