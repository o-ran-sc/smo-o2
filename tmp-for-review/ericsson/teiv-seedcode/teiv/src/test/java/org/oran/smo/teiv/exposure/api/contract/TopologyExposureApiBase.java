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
package org.oran.smo.teiv.exposure.api.contract;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.exposure.utils.RequestValidator;
import java.util.List;

import org.oran.smo.teiv.api.model.OranTeivSchema;
import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivSchemaList;
import org.oran.smo.teiv.exposure.api.contract.utils.RelationshipTestUtility;
import org.oran.smo.teiv.exposure.api.contract.utils.TopologyObjectTestUtility;
import org.oran.smo.teiv.exposure.data.api.impl.DataServiceImpl;
import org.oran.smo.teiv.exposure.data.rest.controller.DataRestController;
import org.oran.smo.teiv.exposure.exception.ApplicationExceptionHandler;
import org.oran.smo.teiv.exposure.model.api.impl.ModelSchemaServiceImpl;
import org.oran.smo.teiv.exposure.model.rest.controller.ModelSchemaRestController;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@ExtendWith(MockitoExtension.class)
public abstract class TopologyExposureApiBase {

    private static final ModelSchemaServiceImpl modelSchemaService = Mockito.mock(ModelSchemaServiceImpl.class);

    private static final DataServiceImpl dataService = Mockito.mock(DataServiceImpl.class);
    private static final CustomMetrics customMetrics = Mockito.mock(CustomMetrics.class);
    private static final RequestValidator requestValidator = Mockito.mock(RequestValidator.class);

    @InjectMocks
    private ModelSchemaRestController modelSchemaRestController;

    @InjectMocks
    private DataRestController dataRestController;

    @BeforeEach
    public void setup() {
        mockData();
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(modelSchemaRestController,
                dataRestController).setControllerAdvice(new ApplicationExceptionHandler());
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

    public void mockData() {
        RelationshipTestUtility.getMockForAllRelationshipsForObjectId(dataService);
        TopologyObjectTestUtility.getMockForAllTopologyObjectById(dataService);
    }

    @BeforeAll
    public static void mockModelSchemaData() {

        OranTeivSchema schemasMetaData = new OranTeivSchema();

        OranTeivHref href = new OranTeivHref();
        href.setHref("/schemas/o-ran-smo-teiv-cloud-to-ran/content");
        schemasMetaData.setName("o-ran-smo-teiv-cloud-to-ran");
        schemasMetaData.setDomain(List.of("RAN", "CLOUD"));
        schemasMetaData.setRevision("2023-06-26");
        schemasMetaData.setContent(href);

        List<OranTeivSchema> schemaList = List.of(schemasMetaData);

        OranTeivSchemaList schemas = new OranTeivSchemaList();
        schemas.setItems(schemaList);
        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref("/schemas?offset=0&limit=100");
        schemas.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref("/schemas?offset=0&limit=100");
        schemas.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref("/schemas?offset=0&limit=100");
        schemas.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref("/schemas?offset=0&limit=100");
        schemas.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref("/schemas?offset=0&limit=100");
        schemas.setLast(hrefLast);

        OranTeivSchemaList schemaQuery = new OranTeivSchemaList();
        schemaQuery.setItems(schemaList);
        OranTeivHref hrefFirst1 = new OranTeivHref();
        hrefFirst1.setHref("/schemas?domain=RAN&offset=0&limit=8");
        schemaQuery.setFirst(hrefFirst1);
        OranTeivHref hrefNext1 = new OranTeivHref();
        hrefNext1.setHref("/schemas?domain=RAN&offset=0&limit=8");
        schemaQuery.setNext(hrefNext1);
        OranTeivHref hrefPrev1 = new OranTeivHref();
        hrefPrev1.setHref("/schemas?domain=RAN&offset=0&limit=8");
        schemaQuery.setPrev(hrefPrev1);
        OranTeivHref hrefSelf1 = new OranTeivHref();
        hrefSelf1.setHref("/schemas?domain=RAN&offset=0&limit=8");
        schemaQuery.setSelf(hrefSelf1);
        OranTeivHref hrefLast1 = new OranTeivHref();
        hrefLast1.setHref("/schemas?domain=RAN&offset=0&limit=8");
        schemaQuery.setLast(hrefLast1);

        OranTeivSchemaList emptyResponse = new OranTeivSchemaList();
        emptyResponse.setItems(List.of());
        OranTeivHref hrefFirst2 = new OranTeivHref();
        hrefFirst2.setHref("/schemas?domain=LOGICAL&offset=0&limit=500");
        emptyResponse.setFirst(hrefFirst2);
        OranTeivHref hrefNext2 = new OranTeivHref();
        hrefNext2.setHref("/schemas?domain=LOGICAL&offset=0&limit=500");
        emptyResponse.setNext(hrefNext2);
        OranTeivHref hrefPrev2 = new OranTeivHref();
        hrefPrev2.setHref("/schemas?domain=LOGICAL&offset=0&limit=500");
        emptyResponse.setPrev(hrefPrev2);
        OranTeivHref hrefSelf2 = new OranTeivHref();
        hrefSelf2.setHref("/schemas?domain=LOGICAL&offset=0&limit=500");
        emptyResponse.setSelf(hrefSelf2);
        OranTeivHref hrefLast2 = new OranTeivHref();
        hrefLast2.setHref("/schemas?domain=LOGICAL&offset=0&limit=500");
        emptyResponse.setLast(hrefLast2);

        String modelSchema = "module o-ran-smo-teiv-cloud-to-ran { yang-version 1.1; }";

        Mockito.when(modelSchemaService.getSchemas(PaginationDTO.builder().offset(0).limit(100).basePath("/schemas")
                .build())).thenReturn(schemas).thenThrow(new RuntimeException());

        Mockito.when(modelSchemaService.getSchemaByName("o-ran-smo-teiv-cloud-to-ran")).thenReturn(modelSchema).thenThrow(
                new RuntimeException());

        Mockito.when(modelSchemaService.getSchemasInDomain("RAN", PaginationDTO.builder().offset(0).limit(8).basePath(
                "/schemas").addPathParameters("domain", "RAN").build())).thenReturn(schemaQuery).thenThrow(
                        new RuntimeException());

        Mockito.when(modelSchemaService.getSchemasInDomain("LOGICAL", PaginationDTO.builder().offset(0).limit(500).basePath(
                "/schemas").addPathParameters("domain", "LOGICAL").build())).thenReturn(emptyResponse).thenThrow(
                        new RuntimeException());
    }

    @AfterAll
    public static void teardown() {
        Mockito.reset(modelSchemaService);
    }
}
