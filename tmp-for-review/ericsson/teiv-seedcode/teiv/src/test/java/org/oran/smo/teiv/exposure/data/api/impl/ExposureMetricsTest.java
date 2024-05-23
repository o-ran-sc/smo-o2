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
package org.oran.smo.teiv.exposure.data.api.impl;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.data.rest.controller.DataRestController;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.exposure.utils.RequestValidator;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.MockSchemaLoader;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExposureMetricsTest {

    private DataServiceImpl mockedDataService;
    private RequestValidator mockedRequestValidator;
    private CustomMetrics underTest;
    private DataRestController dataRestController;

    private final static String ACCEPT_TYPE = "application/json";
    private final static String DOMAIN_NAME = "RAN_LOGICAL";
    private final static String ENTITY_ID = "5A548EA9D166341776CA0695837E55D8";
    private final static String ENTITY_NAME = "GNBDUFunction";
    private final static String RELATION_TYPE = "GNBDUFUNCTION_PROVIDES_NRCELLDU";
    private final static PaginationDTO paginationDto = PaginationDTO.builder().basePath("").offset(0).limit(1).build();

    @BeforeEach
    void setUp() throws SchemaLoaderException {
        SchemaLoader mockedSchemaLoader = new MockSchemaLoader();
        mockedSchemaLoader.loadSchemaRegistry();
        mockedRequestValidator = mock(RequestValidator.class);
        mockedDataService = mock(DataServiceImpl.class);
        underTest = new CustomMetrics(new SimpleMeterRegistry());
        dataRestController = new DataRestController(mockedRequestValidator, mockedDataService, underTest);
    }

    @Test
    void testGetRelationshipsByEntityIdFailMetrics() {
        when(mockedDataService.getAllRelationshipsForObjectId(eq(ENTITY_NAME), eq(ENTITY_ID), any(PaginationDTO.class)))
                .thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getAllRelationshipsForEntityId(ACCEPT_TYPE, DOMAIN_NAME, ENTITY_NAME,
                ENTITY_ID, 0, 1), underTest.getNumUnsuccessfullyExposedRelationshipsByEntityId()::count);
    }

    @Test
    void testGetEntityByIdFailMetrics() {
        when(mockedDataService.getTopologyById(ENTITY_NAME, ENTITY_ID)).thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getTopologyById(ACCEPT_TYPE, DOMAIN_NAME, ENTITY_NAME, ENTITY_ID), underTest
                .getNumUnsuccessfullyExposedEntityById()::count);
    }

    @Test
    void testGetEntitiesByTypeFailMetrics() {
        when(mockedDataService.getTopologyByType(eq(ENTITY_NAME), anyString(), anyString(), any(PaginationDTO.class)))
                .thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getTopologyByEntityTypeName(ACCEPT_TYPE, DOMAIN_NAME, ENTITY_NAME, "", "", 0,
                1), underTest.getNumUnsuccessfullyExposedEntitiesByType()::count);
    }

    @Test
    void testGetEntitiesByDomainFailMetrics() {
        when(mockedDataService.getEntitiesByDomain(eq(DOMAIN_NAME), anyString(), anyString(), any(PaginationDTO.class)))
                .thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getEntitiesByDomain(ACCEPT_TYPE, DOMAIN_NAME, "", "", 0, 1), underTest
                .getNumUnsuccessfullyExposedEntitiesByDomain()::count);
    }

    @Test
    void testGetRelationshipByIdFailMetrics() {
        when(mockedDataService.getRelationshipById(RELATION_TYPE, ENTITY_ID)).thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getRelationshipById(ACCEPT_TYPE, DOMAIN_NAME, RELATION_TYPE, ENTITY_ID),
                underTest.getNumUnsuccessfullyExposedRelationshipById()::count);
    }

    @Test
    void testGetRelationshipsByTypeFailMetrics() {
        when(mockedDataService.getRelationshipsByType(eq(RELATION_TYPE), anyString(), any(PaginationDTO.class))).thenThrow(
                TiesException.class);
        assertMetrics(() -> dataRestController.getRelationshipsByType(ACCEPT_TYPE, DOMAIN_NAME, RELATION_TYPE, "", "", 0,
                1), underTest.getNumUnsuccessfullyExposedRelationshipsByType()::count);
    }

    @Test
    void testGetRelationshipTypesFailMetrics() {
        when(mockedDataService.getTopologyRelationshipTypes(eq(DOMAIN_NAME), any(PaginationDTO.class))).thenThrow(
                TiesException.class);
        assertMetrics(() -> dataRestController.getTopologyRelationshipTypes(ACCEPT_TYPE, DOMAIN_NAME, 0, 1), underTest
                .getNumUnsuccessfullyExposedRelationshipTypes()::count);
    }

    @Test
    void testGetDomainTypesFailMetrics() {
        when(mockedDataService.getDomainTypes(any(PaginationDTO.class))).thenThrow(TiesException.class);
        assertMetrics(() -> dataRestController.getAllDomains(ACCEPT_TYPE, 0, 1), underTest
                .getNumUnsuccessfullyExposedDomainTypes()::count);
    }

    @Test
    void testGetEntityTypesFailMetrics() {
        when(mockedDataService.getTopologyEntityTypes(eq(DOMAIN_NAME), any(PaginationDTO.class))).thenThrow(
                TiesException.class);
        assertMetrics(() -> dataRestController.getTopologyEntityTypes(ACCEPT_TYPE, DOMAIN_NAME, 0, 1), underTest
                .getNumUnsuccessfullyExposedEntityTypes()::count);
    }

    private <T> void assertMetrics(Supplier<T> dataServiceMethod, Supplier<T> failerSupplier) {
        Assertions.assertThrowsExactly(TiesException.class, dataServiceMethod::get);
        Assertions.assertEquals(1.0, failerSupplier.get());
    }
}
