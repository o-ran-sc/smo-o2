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
package org.oran.smo.teiv.exposure.data.rest.controller;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.api.model.OranTeivDomains;
import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivEntityTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivEntityTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypes;
import org.oran.smo.teiv.api.model.OranTeivDomainsItemsInner;
import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.data.api.impl.DataServiceImpl;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.exposure.utils.RequestValidator;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataRestControllerTest {
    private static DataServiceImpl dataService;
    private static DataRestController underTest;
    private static RequestValidator requestValidator;
    private static CustomMetrics customMetrics;
    private final String ACCEPT_TYPE = "application/json";
    private static final String EIID_PREFIX = "urn:base64:";

    @BeforeAll
    static void setUp() {
        dataService = mock(DataServiceImpl.class);
        requestValidator = mock(RequestValidator.class);
        customMetrics = mock(CustomMetrics.class);
        underTest = new DataRestController(requestValidator, dataService, customMetrics);
        mockTopologyById();
        mockGetTopologyRelationshipTypes();
        mockTopologyEntityTypes();
        mockGetRelationshipById();
        mockGetRelationshipByType();
        mockGetAllRelationshipsForEntityId();
        mockGetDomainTypes();
        mockGetTopologyEntityTypes();
        mockTopologyByType();
    }

    @BeforeEach
    void clearInvocations() {
        Mockito.clearInvocations(requestValidator);
    }

    @Test
    void testGetTopologyById() {
        assertEquals(ResponseEntity.ok(generateResponse("GNBDUFunction", "5A548EA9D166341776CA0695837E55D8")), underTest
                .getTopologyById(ACCEPT_TYPE, "RAN_LOGICAL", "GNBDUFunction", "5A548EA9D166341776CA0695837E55D8"));
        assertEquals(ResponseEntity.ok(generateResponse("NRCellDU", "98C3A4591A37718E1330F0294E23B62A")), underTest
                .getTopologyById(ACCEPT_TYPE, "RAN_LOGICAL", "NRCellDU", "98C3A4591A37718E1330F0294E23B62A"));
        assertThrows(TiesException.class, () -> underTest.getTopologyById(ACCEPT_TYPE, "RAN_LOGICAL", "NRCellDU",
                "NOT_EXISTING"));

        verify(requestValidator, Mockito.times(3)).validateDomain("RAN_LOGICAL");
        verify(requestValidator, Mockito.times(1)).validateEntityType("GNBDUFunction");
        verify(requestValidator, Mockito.times(2)).validateEntityType("NRCellDU");
        verify(requestValidator, Mockito.times(1)).validateEntityTypeInDomain("GNBDUFunction", "RAN_LOGICAL");
        verify(requestValidator, Mockito.times(2)).validateEntityTypeInDomain("NRCellDU", "RAN_LOGICAL");
    }

    @Test
    void testGetTopologyEntityTypes() {
        OranTeivEntityTypesItemsInner element1 = new OranTeivEntityTypesItemsInner();
        element1.setName("Site");
        OranTeivEntityTypesItemsInner element2 = new OranTeivEntityTypesItemsInner();
        element1.setName("AntennaModule");
        OranTeivEntityTypesItemsInner element3 = new OranTeivEntityTypesItemsInner();
        element1.setName("PhysicalNetworkAppliance");

        List<OranTeivEntityTypesItemsInner> entityTypes = new ArrayList<>();
        entityTypes.add(element1);
        entityTypes.add(element2);
        entityTypes.add(element3);
        OranTeivEntityTypes response = new OranTeivEntityTypes();
        response.setItems(entityTypes);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains/RAN_EQUIPMENT/entity-types?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyEntityTypes(ACCEPT_TYPE, "RAN_EQUIPMENT",
                0, 5));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyEntityTypes("*/*", "RAN_EQUIPMENT", 0,
                5));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyEntityTypes(
                "application/json, application/problem+json", "RAN_EQUIPMENT", 0, 5));

        verify(requestValidator, Mockito.times(3)).validateDomain("RAN_EQUIPMENT");
    }

    @Test
    void testGetTopologyRelationshipTypes() {
        OranTeivRelationshipTypesItemsInner element1 = new OranTeivRelationshipTypesItemsInner();
        element1.setName("NRSECTORCARRIER_USES_ANTENNACAPABILITY");
        OranTeivRelationshipTypesItemsInner element2 = new OranTeivRelationshipTypesItemsInner();
        element2.setName("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        OranTeivRelationshipTypesItemsInner element3 = new OranTeivRelationshipTypesItemsInner();
        element3.setName("GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        OranTeivRelationshipTypesItemsInner element4 = new OranTeivRelationshipTypesItemsInner();
        element4.setName("EUTRANCELL_USES_LTESECTORCARRIER");
        OranTeivRelationshipTypesItemsInner element5 = new OranTeivRelationshipTypesItemsInner();
        element5.setName("MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION");

        List<OranTeivRelationshipTypesItemsInner> relationshipTypes = new ArrayList<>();
        relationshipTypes.add(element1);
        relationshipTypes.add(element2);
        relationshipTypes.add(element3);
        relationshipTypes.add(element4);
        relationshipTypes.add(element5);

        OranTeivRelationshipTypes response = new OranTeivRelationshipTypes();
        response.setItems(relationshipTypes);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains/RAN_LOGICAL/relationship-types?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyRelationshipTypes(ACCEPT_TYPE,
                "RAN_LOGICAL", 0, 5));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyRelationshipTypes("*/*", "RAN_LOGICAL", 0,
                5));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyRelationshipTypes(
                "application/json, application/problem+json", "RAN_LOGICAL", 0, 5));
    }

    @Test
    void testGetDomainTypes() {
        OranTeivDomainsItemsInner element1 = new OranTeivDomainsItemsInner();
        element1.setName("RAN_LOGICAL_TO_EQUIPMENT");
        OranTeivDomainsItemsInner element2 = new OranTeivDomainsItemsInner();
        element2.setName("RAN_LOGICAL");
        OranTeivDomainsItemsInner element3 = new OranTeivDomainsItemsInner();
        element3.setName("RAN_EQUIPMENT");

        List<OranTeivDomainsItemsInner> domains = new ArrayList<>();
        domains.add(element1);
        domains.add(element2);
        domains.add(element3);

        OranTeivDomains response = new OranTeivDomains();
        response.setItems(domains);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getAllDomains(ACCEPT_TYPE, 0, 3));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getAllDomains("*/*", 0, 3));
        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getAllDomains(
                "application/json, application/problem+json", 0, 3));

    }

    @Test
    void testGetRelationshipById() {
        Map<String, Object> response = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
                generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A",
                        EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==",
                        Collections.emptyList())));

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getRelationshipById(ACCEPT_TYPE, "RAN_LOGICAL",
                "GNBDUFUNCTION_PROVIDES_NRCELLDU",
                EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ=="));

        verify(requestValidator, Mockito.times(1)).validateDomain("RAN_LOGICAL");
        verify(requestValidator, Mockito.times(1)).validateRelationshipType("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        verify(requestValidator, Mockito.times(1)).validateRelationshipTypeInDomain("GNBDUFUNCTION_PROVIDES_NRCELLDU",
                "RAN_LOGICAL");
    }

    @Test
    void testGetAllRelationshipsForEntityId() {
        List<Object> mapList = new ArrayList<>();
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "D3215E08570BE58339C7463626B50E37", "B480427E8A0C0B8D994E437784BB382F",
                EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.emptyList()))));
        mapList.add(Map.of("o-ran-smo-teiv-equipment-to-ran:SECTOR_GROUPS_NRCELLDU", List.of(generateResponse(
                "F5128C172A70C4FCD4739650B06DE9E2", "B480427E8A0C0B8D994E437784BB382F",
                EIID_PREFIX + "U2VjdG9yOkY1MTI4QzE3MkE3MEM0RkNENDczOTY1MEIwNkRFOUUyOkdST1VQUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.emptyList()))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:NRCELLDU_USES_NRSECTORCARRIER", List.of(generateResponse(
                "B480427E8A0C0B8D994E437784BB382F", "E49D942C16E0364E1E0788138916D70C",
                EIID_PREFIX + "TlJDZWxsRFU6QjQ4MDQyN0U4QTBDMEI4RDk5NEU0Mzc3ODRCQjM4MkY6VVNFUzpOUlNlY3RvckNhcnJpZXI6RTQ5RDk0MkMxNkUwMzY0RTFFMDc4ODEzODkxNkQ3MEM=",
                Collections.emptyList()))));

        OranTeivRelationshipsResponseMessage response = new OranTeivRelationshipsResponseMessage();

        response.setItems(mapList);
        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/entity-types/NRCellDU/entities/B480427E8A0C0B8D994E437784BB382F/relationships?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getAllRelationshipsForEntityId(ACCEPT_TYPE,
                "RAN_LOGICAL", "NRCellDU", "B480427E8A0C0B8D994E437784BB382F", 0, 5));

        verify(requestValidator, Mockito.times(1)).validateDomain("RAN_LOGICAL");
        verify(requestValidator, Mockito.times(1)).validateEntityType("NRCellDU");
        verify(requestValidator, Mockito.times(1)).validateEntityTypeInDomain("NRCellDU", "RAN_LOGICAL");
    }

    @Test
    void testGetRelationshipsByType() {
        underTest.getRelationshipsByType(ACCEPT_TYPE, "RAN_LOGICAL", "GNBDUFUNCTION_PROVIDES_NRCELLDU", null, null, 0, 5);

        verify(requestValidator, Mockito.times(1)).validateDomain("RAN_LOGICAL");
        verify(requestValidator, Mockito.times(1)).validateRelationshipType("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        verify(requestValidator, Mockito.times(1)).validateRelationshipTypeInDomain("GNBDUFUNCTION_PROVIDES_NRCELLDU",
                "RAN_LOGICAL");

        OranTeivRelationshipsResponseMessage response = new OranTeivRelationshipsResponseMessage();

        response.setItems(List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "91", "1", EIID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkxOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzE=",
                Collections.emptyList()), generateResponse("92", "2",
                        EIID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkyOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzI=",
                        Collections.emptyList())))));
        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getRelationshipsByType(ACCEPT_TYPE, "RAN_LOGICAL",
                "GNBDUFUNCTION_PROVIDES_NRCELLDU", null, null, 0, 5));
    }

    @Test
    void testGetTopologyByTargetFilter() {
        underTest.getEntitiesByDomain(ACCEPT_TYPE, "RAN_LOGICAL", null, null, 0, 2);

        verify(requestValidator, Mockito.times(1)).validateDomain("RAN_LOGICAL");
    }

    @Test
    void testGetTopologyByType() {

        OranTeivEntitiesResponseMessage response = new OranTeivEntitiesResponseMessage();
        Map<String, Object> query = new HashMap<>();

        query.put("scopeFilter", "/attributes[contains (@fnd, \"/SubNetwork=Ireland/\")]");

        response.setItems(List.of(Map.of("GNBDUFunction", List.of(Map.of("id", "5FE67725576EDA7937752E7965164C2E"), Map.of(
                "id", "9BCD297B8258F67908477D895636ED65"), Map.of("id", "8E249BCFAEC86C03D9ADD27FA9748254")))));
        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities?scopeFilter=/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]&offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        Assertions.assertEquals(ResponseEntity.ok(response), underTest.getTopologyByEntityTypeName(ACCEPT_TYPE,
                "RAN_LOGICAL", "GNBDUFunction", "", "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]", 0, 3));

        verify(requestValidator, Mockito.times(1)).validateDomain("RAN_LOGICAL");
        verify(requestValidator, Mockito.times(1)).validateEntityType("GNBDUFunction");
        verify(requestValidator, Mockito.times(1)).validateEntityTypeInDomain("GNBDUFunction", "RAN_LOGICAL");

        assertThrows(TiesException.class, () -> underTest.getTopologyByEntityTypeName(ACCEPT_TYPE, "RAN_LOGICAL",
                "CloudSite", "", "/attributes[contains (@fdn, \"/SubNetwork Ireland/\")]", 0, 3));
        assertThrows(TiesException.class, () -> underTest.getTopologyByEntityTypeName(ACCEPT_TYPE, "RAN_LOGICAL",
                "GNBDUFunction2", "", "/attributes[contains (@fdn, \"/SubNetwork Ireland/\")]", 0, 3));
        assertThrows(TiesPathException.class, () -> underTest.getTopologyByEntityTypeName(ACCEPT_TYPE, "RAN_LOGICAL",
                "GNBDUFunction", "", "/attributes[contains ( fdn, \"/SubNetwork Ireland/\")]", 0, 3));

    }

    private static void mockTopologyById() {
        when(dataService.getTopologyById("GNBDUFunction", "5A548EA9D166341776CA0695837E55D8")).thenReturn(generateResponse(
                "GNBDUFunction", "5A548EA9D166341776CA0695837E55D8"));
        when(dataService.getTopologyById("NRCellDU", "98C3A4591A37718E1330F0294E23B62A")).thenReturn(generateResponse(
                "NRCellDU", "98C3A4591A37718E1330F0294E23B62A"));
        when(dataService.getTopologyById("NRCellDU", "NOT_EXISTING")).thenThrow(TiesException.class);
    }

    private static void mockTopologyEntityTypes() {
        OranTeivEntityTypesItemsInner element1 = new OranTeivEntityTypesItemsInner();
        element1.setName("Site");
        OranTeivEntityTypesItemsInner element2 = new OranTeivEntityTypesItemsInner();
        element1.setName("AntennaModule");
        OranTeivEntityTypesItemsInner element3 = new OranTeivEntityTypesItemsInner();
        element1.setName("PhysicalNetworkAppliance");

        List<OranTeivEntityTypesItemsInner> entityTypes = new ArrayList<>();
        entityTypes.add(element1);
        entityTypes.add(element2);
        entityTypes.add(element3);
        OranTeivEntityTypes response = new OranTeivEntityTypes();
        response.setItems(entityTypes);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains/RAN_EQUIPMENT/entity-types?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        when(dataService.getTopologyEntityTypes("RAN_EQUIPMENT", PaginationDTO.builder().offset(0).limit(5).build()))
                .thenReturn(response);
    }

    private static void mockGetRelationshipById() {
        Map<String, Object> response = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
                generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A",
                        EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==",
                        Collections.emptyList())));

        when(dataService.getRelationshipById("GNBDUFUNCTION_PROVIDES_NRCELLDU",
                EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ=="))
                        .thenReturn(response);
    }

    private static void mockGetRelationshipByType() {
        PaginationDTO paginationDTO = PaginationDTO.builder().offset(0).limit(5).addPathParameters("relationshipType",
                "GNBDUFUNCTION_PROVIDES_NRCELLDU").addPathParameters("domain", "RAN_LOGICAL").addQueryParameters(
                        "scopeFilter", null).basePath(
                                "/domains/RAN_LOGICAL/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships")
                .build();

        OranTeivRelationshipsResponseMessage response = new OranTeivRelationshipsResponseMessage();
        response.setItems(List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "91", "1", EIID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkxOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzE=",
                Collections.emptyList()), generateResponse("92", "2",
                        EIID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkyOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzI=",
                        Collections.emptyList())))));

        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        when(dataService.getRelationshipsByType("GNBDUFUNCTION_PROVIDES_NRCELLDU", null, paginationDTO)).thenReturn(
                response);
    }

    private static void mockGetAllRelationshipsForEntityId() {
        OranTeivRelationshipsResponseMessage response = new OranTeivRelationshipsResponseMessage();

        List<Object> mapList = new ArrayList<>();
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "D3215E08570BE58339C7463626B50E37", "B480427E8A0C0B8D994E437784BB382F",
                EIID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.emptyList()))));
        mapList.add(Map.of("o-ran-smo-teiv-equipment-to-ran:SECTOR_GROUPS_NRCELLDU", List.of(generateResponse(
                "F5128C172A70C4FCD4739650B06DE9E2", "B480427E8A0C0B8D994E437784BB382F",
                EIID_PREFIX + "U2VjdG9yOkY1MTI4QzE3MkE3MEM0RkNENDczOTY1MEIwNkRFOUUyOkdST1VQUzpOUkNlbGxEVTpCNDgwNDI3RThBMEMwQjhEOTk0RTQzNzc4NEJCMzgyRg==",
                Collections.emptyList()))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:NRCELLDU_USES_NRSECTORCARRIER", List.of(generateResponse(
                "B480427E8A0C0B8D994E437784BB382F", "E49D942C16E0364E1E0788138916D70C",
                EIID_PREFIX + "TlJDZWxsRFU6QjQ4MDQyN0U4QTBDMEI4RDk5NEU0Mzc3ODRCQjM4MkY6VVNFUzpOUlNlY3RvckNhcnJpZXI6RTQ5RDk0MkMxNkUwMzY0RTFFMDc4ODEzODkxNkQ3MEM=",
                Collections.emptyList()))));

        PaginationDTO paginationDTO1 = PaginationDTO.builder().addPathParameters("domain", "RAN_LOGICAL").addPathParameters(
                "entityType", "NRCellDU").addPathParameters("id", "B480427E8A0C0B8D994E437784BB382F").basePath(String
                        .format("/domains/%s/entity-types/%s/entities/%s/relationships", "RAN_LOGICAL", "NRCellDU",
                                "B480427E8A0C0B8D994E437784BB382F")).offset(0).limit(5).build();

        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/entity-types/NRCellDU/entities/B480427E8A0C0B8D994E437784BB382F/relationships?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        response.setItems(mapList);
        when(dataService.getAllRelationshipsForObjectId("NRCellDU", "B480427E8A0C0B8D994E437784BB382F", paginationDTO1))
                .thenReturn(response);
    }

    private static void mockGetDomainTypes() {
        OranTeivDomainsItemsInner element1 = new OranTeivDomainsItemsInner();
        element1.setName("RAN_LOGICAL_TO_EQUIPMENT");
        OranTeivDomainsItemsInner element2 = new OranTeivDomainsItemsInner();
        element2.setName("RAN_LOGICAL");
        OranTeivDomainsItemsInner element3 = new OranTeivDomainsItemsInner();
        element3.setName("RAN_EQUIPMENT");

        List<OranTeivDomainsItemsInner> domains = new ArrayList<>();
        domains.add(element1);
        domains.add(element2);
        domains.add(element3);

        OranTeivDomains response = new OranTeivDomains();
        response.setItems(domains);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        when(dataService.getDomainTypes(PaginationDTO.builder().offset(0).basePath("/domains").limit(3).build()))
                .thenReturn(response);
    }

    private static void mockGetTopologyEntityTypes() {
        OranTeivEntityTypesItemsInner element1 = new OranTeivEntityTypesItemsInner();
        element1.setName("Site");
        OranTeivEntityTypesItemsInner element2 = new OranTeivEntityTypesItemsInner();
        element1.setName("AntennaModule");
        OranTeivEntityTypesItemsInner element3 = new OranTeivEntityTypesItemsInner();
        element1.setName("PhysicalNetworkAppliance");

        List<OranTeivEntityTypesItemsInner> entityTypes = new ArrayList<>();
        entityTypes.add(element1);
        entityTypes.add(element2);
        entityTypes.add(element3);
        OranTeivEntityTypes response = new OranTeivEntityTypes();
        response.setItems(entityTypes);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains/RAN_EQUIPMENT/entity-types?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath("/domains/RAN_EQUIPMENT/entity-types")
                .addPathParameters("domain", "RAN_EQUIPMENT").offset(0).limit(5).build();

        when(dataService.getTopologyEntityTypes("RAN_EQUIPMENT", paginationDTO1)).thenReturn(response);
    }

    private static void mockGetTopologyRelationshipTypes() {
        OranTeivRelationshipTypesItemsInner element1 = new OranTeivRelationshipTypesItemsInner();
        element1.setName("NRSECTORCARRIER_USES_ANTENNACAPABILITY");
        OranTeivRelationshipTypesItemsInner element2 = new OranTeivRelationshipTypesItemsInner();
        element2.setName("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        OranTeivRelationshipTypesItemsInner element3 = new OranTeivRelationshipTypesItemsInner();
        element3.setName("GNBCUUPFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        OranTeivRelationshipTypesItemsInner element4 = new OranTeivRelationshipTypesItemsInner();
        element4.setName("EUTRANCELL_USES_LTESECTORCARRIER");
        OranTeivRelationshipTypesItemsInner element5 = new OranTeivRelationshipTypesItemsInner();
        element5.setName("MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION");

        List<OranTeivRelationshipTypesItemsInner> relationshipTypes = new ArrayList<>();
        relationshipTypes.add(element1);
        relationshipTypes.add(element2);
        relationshipTypes.add(element3);
        relationshipTypes.add(element4);
        relationshipTypes.add(element5);

        OranTeivRelationshipTypes response = new OranTeivRelationshipTypes();
        response.setItems(relationshipTypes);

        OranTeivHref href = new OranTeivHref();
        href.setHref("/domains/RAN_LOGICAL/relationship-types?offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath("/domains/RAN_LOGICAL/relationship-types").offset(0)
                .limit(5).addPathParameters("domain", "RAN_LOGICAL").build();

        when(dataService.getTopologyRelationshipTypes("RAN_LOGICAL", paginationDTO1)).thenReturn(response);
    }

    private static void mockTopologyByType() {
        OranTeivEntitiesResponseMessage response = new OranTeivEntitiesResponseMessage();
        Map<String, Object> query = new HashMap<>();

        query.put("scopeFilter", "/attributes[contains (@fnd, \"/SubNetwork=Ireland/\")]");

        PaginationDTO paginationDTO = PaginationDTO.builder().offset(0).limit(3).addPathParameters("domain", "RAN_LOGICAL")
                .addQueryParameters("targetFilter", "").addQueryParameters("scopeFilter",
                        "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]").basePath(
                                "/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities").build();

        response.setItems(List.of(Map.of("GNBDUFunction", List.of(Map.of("id", "5FE67725576EDA7937752E7965164C2E"), Map.of(
                "id", "9BCD297B8258F67908477D895636ED65"), Map.of("id", "8E249BCFAEC86C03D9ADD27FA9748254")))));
        OranTeivHref href = new OranTeivHref();
        href.setHref(
                "/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities?scopeFilter=/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]&offset=0&limit=5");
        response.setSelf(href);
        response.setFirst(href);
        response.setPrev(href);
        response.setNext(href);
        response.setLast(href);

        when(dataService.getTopologyByType("GNBDUFunction", "", "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]",
                paginationDTO)).thenReturn(response);

        doThrow(TiesException.class).when(requestValidator).validateEntityTypeInDomain(eq("CloudSite"), eq("RAN_LOGICAL"));
        doThrow(TiesException.class).when(requestValidator).validateEntityTypeInDomain(eq("GNBDUFunction2"), eq(
                "RAN_LOGICAL"));

        when(dataService.getTopologyByType("GNBDUFunction", "", "/attributes[contains ( fdn, \"/SubNetwork Ireland/\")]",
                PaginationDTO.builder().offset(0).limit(3).addQueryParameters("targetFilter", "").addQueryParameters(
                        "scopeFilter", "/attributes[contains ( fdn, \"/SubNetwork Ireland/\")]").basePath(
                                "/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities").addPathParameters("domain",
                                        "RAN_LOGICAL").build())).thenThrow(TiesPathException.grammarError("'@' missing"));
    }
}
