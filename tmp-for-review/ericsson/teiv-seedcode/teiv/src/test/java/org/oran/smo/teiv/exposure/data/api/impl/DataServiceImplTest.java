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

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.mapper.MapperUtility;
import org.oran.smo.teiv.exposure.spi.mapper.PaginationMetaData;
import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.schema.MockSchemaLoader;

import org.oran.smo.teiv.api.model.OranTeivDomains;
import org.oran.smo.teiv.api.model.OranTeivDomainsItemsInner;
import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
import org.oran.smo.teiv.api.model.OranTeivEntityTypes;
import org.oran.smo.teiv.api.model.OranTeivEntityTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypes;
import org.oran.smo.teiv.api.model.OranTeivRelationshipTypesItemsInner;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.oran.smo.teiv.schema.SchemaRegistry.*;
import static org.oran.smo.teiv.utils.ResponseGenerator.generateResponse;
import static org.oran.smo.teiv.utils.TiesConstants.ITEMS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataServiceImplTest {

    private static DataPersistanceService mockedDataPersistanceService;
    private static DataServiceImpl underTest;
    private static final String REL_ID_PREFIX = "urn:base64:";
    private static MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeAll
    static void setUp() throws SchemaLoaderException {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        mockedDataPersistanceService = mock(DataPersistanceService.class);
        SchemaLoader mockedSchemaLoader = new MockSchemaLoader();
        mockedSchemaLoader.loadSchemaRegistry();
        underTest = new DataServiceImpl(mockedDataPersistanceService, new MapperUtility());
        mockGetTopologyById();
        mockGetRelationshipById();
        mockGetRelationshipByType();
        mockGetAllRelationships();
        mockGetTopologyByType();
        MockGetTopologyByTargetFilter();
    }

    @Test
    void testGetTopologyById() {
        Assertions.assertEquals(generateResponse("GNBDUFunction", "5A548EA9D166341776CA0695837E55D8"), underTest
                .getTopologyById("GNBDUFunction", "5A548EA9D166341776CA0695837E55D8"));
        Assertions.assertEquals(generateResponse("NRCellDU", "98C3A4591A37718E1330F0294E23B62A"), underTest.getTopologyById(
                "NRCellDU", "98C3A4591A37718E1330F0294E23B62A"));
        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getTopologyById("NRCellDU", "NOT_EXISTING"));
    }

    @Test
    void testGetTopologyByType() {
        OranTeivEntitiesResponseMessage expectedResponse1 = new OranTeivEntitiesResponseMessage();

        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath("/domains/RAN/entity-types/GNBDUFunction/entities")
                .offset(0).limit(2).addQueryParameters("scopeFilter",
                        "/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]")
                .addQueryParameters("targetFilter", "/attributes/fdn;/attributes/id").build();
        paginationDTO1.setTotalSize(2);

        expectedResponse1.setItems(List.of(Map.of("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=111", "id",
                "5BAE4346C241237AA8C74AE1259EF203"), Map.of("fdn",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=112",
                        "id", "6B55F987ED838C0FA58DC11AB19CD1D3")));

        OranTeivHref hrefFirst1 = new OranTeivHref();
        hrefFirst1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?offset=0&limit=2&scopeFilter=/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]&targetFilter=/attributes/fdn;/attributes/id");
        expectedResponse1.setFirst(hrefFirst1);
        OranTeivHref hrefNext1 = new OranTeivHref();
        hrefNext1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?offset=0&limit=2&scopeFilter=/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]&targetFilter=/attributes/fdn;/attributes/id");
        expectedResponse1.setNext(hrefNext1);
        OranTeivHref hrefPrev1 = new OranTeivHref();
        hrefPrev1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?offset=0&limit=2&scopeFilter=/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]&targetFilter=/attributes/fdn;/attributes/id");
        expectedResponse1.setPrev(hrefPrev1);
        OranTeivHref hrefSelf1 = new OranTeivHref();
        hrefSelf1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?offset=0&limit=2&scopeFilter=/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]&targetFilter=/attributes/fdn;/attributes/id");
        expectedResponse1.setSelf(hrefSelf1);
        OranTeivHref hrefLast1 = new OranTeivHref();
        hrefLast1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?offset=0&limit=2&scopeFilter=/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]&targetFilter=/attributes/fdn;/attributes/id");
        expectedResponse1.setLast(hrefLast1);
        expectedResponse1.setTotalCount(2);

        Assertions.assertEquals(expectedResponse1, underTest.getTopologyByType("GNBDUFunction",
                "/attributes/fdn;/attributes/id",
                "/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]", paginationDTO1));
    }

    @Test
    void testGetEntitiesByDomain() {
        OranTeivEntitiesResponseMessage expectedResult = new OranTeivEntitiesResponseMessage();

        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/RAN/entities").offset(0).limit(1)
                .addQueryParameters("targetFilter", "/GNBDUFunction/id").build();
        paginationDTO.setTotalSize(1);

        expectedResult.setItems(List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                "1050570EBB1315E1AE7A9FD5E1400A00")))));

        OranTeivHref hrefFirst1 = new OranTeivHref();
        hrefFirst1.setHref("/domains/RAN/entities?offset=0&limit=1&targetFilter=/GNBDUFunction/id");
        expectedResult.setFirst(hrefFirst1);
        OranTeivHref hrefNext1 = new OranTeivHref();
        hrefNext1.setHref("/domains/RAN/entities?offset=0&limit=1&targetFilter=/GNBDUFunction/id");
        expectedResult.setNext(hrefNext1);
        OranTeivHref hrefPrev1 = new OranTeivHref();
        hrefPrev1.setHref("/domains/RAN/entities?offset=0&limit=1&targetFilter=/GNBDUFunction/id");
        expectedResult.setPrev(hrefPrev1);
        OranTeivHref hrefSelf1 = new OranTeivHref();
        hrefSelf1.setHref("/domains/RAN/entities?offset=0&limit=1&targetFilter=/GNBDUFunction/id");
        expectedResult.setSelf(hrefSelf1);
        OranTeivHref hrefLast1 = new OranTeivHref();
        hrefLast1.setHref("/domains/RAN/entities?offset=0&limit=1&targetFilter=/GNBDUFunction/id");
        expectedResult.setLast(hrefLast1);
        expectedResult.setTotalCount(1);

        Assertions.assertEquals(expectedResult, underTest.getEntitiesByDomain("RAN", "/GNBDUFunction/id", null,
                paginationDTO));

    }

    @Test
    void testGetRelationshipById() {
        String id = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==";
        String notExistingId = "NOT_EXISTING";
        Map<String, Object> response = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
                generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A", id,
                        Collections.EMPTY_LIST)));

        Assertions.assertEquals(response, underTest.getRelationshipById("GNBDUFUNCTION_PROVIDES_NRCELLDU", id));
        Assertions.assertThrowsExactly(TiesException.class, () -> underTest.getRelationshipById(
                "GNBDUFUNCTION_PROVIDES_NRCELLDU", notExistingId));
    }

    @Test
    void testGetRelationshipsByType() {
        OranTeivRelationshipsResponseMessage response = new OranTeivRelationshipsResponseMessage();
        OranTeivRelationshipsResponseMessage response2 = new OranTeivRelationshipsResponseMessage();
        PaginationDTO paginationDTO = PaginationDTO.builder().basePath(
                "/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships").offset(0).limit(5).build();

        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships").offset(0)
                .limit(5).build();

        response.setItems(List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "BCA882C87D49687E731F9B3872EFDBD3",
                REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkxOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzE=",
                Collections.EMPTY_LIST)))));

        response2.setItems(List.of(Map.of("o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", List.of(
                generateResponse("A05C67D47D117C2DC5BDF5E00AE70", "2256120E73ADD4026A43A971DCE5C151",
                        REL_ID_PREFIX + "R05CQ1VVUEZ1bmN0aW9uOkJGRUVBQzJDRTYwMjczQ0IwQTc4MzE5Q0MyMDFBN0ZFOlJE=",
                        Collections.EMPTY_LIST)))));

        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setFirst(hrefFirst);
        OranTeivHref hrefFirst2 = new OranTeivHref();
        hrefFirst2.setHref(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships?offset=0&limit=5");
        response2.setFirst(hrefFirst2);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setNext(hrefNext);
        OranTeivHref hrefNext2 = new OranTeivHref();
        hrefNext2.setHref(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships?offset=0&limit=5");
        response2.setNext(hrefNext2);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setPrev(hrefPrev);
        OranTeivHref hrefPrev2 = new OranTeivHref();
        hrefPrev2.setHref(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships?offset=0&limit=5");
        response2.setPrev(hrefPrev2);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setSelf(hrefSelf);
        OranTeivHref hrefSelf2 = new OranTeivHref();
        hrefSelf2.setHref(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships?offset=0&limit=5");
        response2.setSelf(hrefSelf2);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships?offset=0&limit=5");
        response.setLast(hrefLast);
        OranTeivHref hrefLast2 = new OranTeivHref();
        hrefLast2.setHref(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships?offset=0&limit=5");
        response2.setLast(hrefLast2);

        response.setTotalCount(1);
        response2.setTotalCount(1);

        Assertions.assertEquals(response, underTest.getRelationshipsByType("GNBDUFUNCTION_PROVIDES_NRCELLDU", null,
                paginationDTO));

        Assertions.assertEquals(response2, underTest.getRelationshipsByType("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", null,
                paginationDTO2));
    }

    @Test
    void testAllRelationshipsForObjectId() {
        OranTeivRelationshipsResponseMessage response1 = new OranTeivRelationshipsResponseMessage();
        OranTeivRelationshipsResponseMessage response2 = new OranTeivRelationshipsResponseMessage();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships").offset(0).limit(
                        100).build();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships").offset(0).limit(
                        100).build();

        List<Object> mapList = new ArrayList<>();
        List<Object> items1 = new ArrayList<>();
        List<Object> items2 = new ArrayList<>();
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "BCA882C87D49687E731F9B3872EFDBD3",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6RVV0cmFuQ2VsbDpCQ0E4ODJDODdENDk2ODdFNzMxRjlCMzg3MkVGREJEMw==",
                Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "3256120E73ADD4026A43A971DCE5C151",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MQ==",
                Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTION_REALISED_BY_CLOUDNATIVESYSTEM", List.of(
                generateResponse("0525930249302B9649FC8F201EC4F7FC", "2256120E73ADD4026A43A971DCE5C151",
                        REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MR==",
                        Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:NRSECTORCARRIER_USES_ANTENNACAPABILITY", List.of(generateResponse(
                "3256120E73ADD4026A43A971DCE5C151", "3223120E73ADD4026A43A971DCE5C151",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MS==",
                Collections.EMPTY_LIST))));

        items1.add(mapList.get(0));
        items1.add(mapList.get(1));
        items1.add(mapList.get(2));

        items2.add(mapList.get(1));
        items2.add(mapList.get(3));

        response1.setItems(items1);
        response2.setItems(items2);

        OranTeivHref hrefFirst1 = new OranTeivHref();
        hrefFirst1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships?offset=0&limit=100");
        response1.setFirst(hrefFirst1);
        OranTeivHref hrefNext1 = new OranTeivHref();
        hrefNext1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships?offset=0&limit=100");
        response1.setNext(hrefNext1);
        OranTeivHref hrefPrev1 = new OranTeivHref();
        hrefPrev1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships?offset=0&limit=100");
        response1.setPrev(hrefPrev1);
        OranTeivHref hrefSelf1 = new OranTeivHref();
        hrefSelf1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships?offset=0&limit=100");
        response1.setSelf(hrefSelf1);
        OranTeivHref hrefLast1 = new OranTeivHref();
        hrefLast1.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships?offset=0&limit=100");
        response1.setLast(hrefLast1);
        response1.setTotalCount(3);

        OranTeivHref hrefFirst2 = new OranTeivHref();
        hrefFirst2.setHref(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships?offset=0&limit=100");
        response2.setFirst(hrefFirst2);
        OranTeivHref hrefNext2 = new OranTeivHref();
        hrefNext2.setHref(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships?offset=0&limit=100");
        response2.setNext(hrefNext2);
        OranTeivHref hrefPrev2 = new OranTeivHref();
        hrefPrev2.setHref(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships?offset=0&limit=100");
        response2.setPrev(hrefPrev2);
        OranTeivHref hrefSelf2 = new OranTeivHref();
        hrefSelf2.setHref(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships?offset=0&limit=100");
        response2.setSelf(hrefSelf2);
        OranTeivHref hrefLast2 = new OranTeivHref();
        hrefLast2.setHref(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships?offset=0&limit=100");
        response2.setLast(hrefLast2);
        response2.setTotalCount(1);

        Assertions.assertEquals(response1, underTest.getAllRelationshipsForObjectId("GNBDUFunction",
                "0525930249302B9649FC8F201EC4F7FC", paginationDTO1));
        Assertions.assertEquals(response2, underTest.getAllRelationshipsForObjectId("NRSectorCarrier",
                "3256120E73ADD4026A43A971DCE5C151", paginationDTO2));
    }

    @Test
    void testGetTopologyRelationshipTypes() {
        OranTeivRelationshipTypes expectedResponse = new OranTeivRelationshipTypes();
        List<OranTeivRelationshipTypesItemsInner> items = new ArrayList<>();
        OranTeivRelationshipTypesItemsInner item1 = new OranTeivRelationshipTypesItemsInner();
        OranTeivRelationshipTypesItemsInner item2 = new OranTeivRelationshipTypesItemsInner();
        OranTeivRelationshipTypesItemsInner item3 = new OranTeivRelationshipTypesItemsInner();
        OranTeivRelationshipTypesItemsInner item4 = new OranTeivRelationshipTypesItemsInner();
        OranTeivHref relationships1 = new OranTeivHref();
        OranTeivHref relationships2 = new OranTeivHref();
        OranTeivHref relationships3 = new OranTeivHref();
        OranTeivHref relationships4 = new OranTeivHref();

        item1.setName("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        relationships1.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships");
        item1.setRelationships(relationships1);

        item2.setName("GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");
        relationships2.setHref("/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER/relationships");

        item2.setRelationships(relationships2);

        item3.setName("NRCELLDU_USES_NRSECTORCARRIER");
        relationships3.setHref("/domains/RAN/relationship-types/NRCELLDU_USES_NRSECTORCARRIER/relationships");
        item3.setRelationships(relationships3);

        item4.setName("NRSECTORCARRIER_USES_ANTENNACAPABILITY");
        relationships4.setHref("/domains/RAN/relationship-types/NRSECTORCARRIER_USES_ANTENNACAPABILITY/relationships");
        item4.setRelationships(relationships4);

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);

        expectedResponse.setItems(items);
        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref("/domains/RAN/relationship-types?offset=0&limit=5");
        expectedResponse.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref("/domains/RAN/relationship-types?offset=0&limit=5");
        expectedResponse.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref("/domains/RAN/relationship-types?offset=0&limit=5");
        expectedResponse.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref("/domains/RAN/relationship-types?offset=0&limit=5");
        expectedResponse.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref("/domains/RAN/relationship-types?offset=0&limit=5");
        expectedResponse.setLast(hrefLast);
        expectedResponse.setTotalCount(4);

        PaginationDTO paginationDTO3 = PaginationDTO.builder().basePath("/domains/RAN/relationship-types").offset(0).limit(
                5).build();

        Assertions.assertEquals(expectedResponse, underTest.getTopologyRelationshipTypes("RAN", paginationDTO3));
    }

    @Test
    void testGetTopologyEntityTypes() {
        OranTeivEntityTypes expectedResponse = new OranTeivEntityTypes();
        List<OranTeivEntityTypesItemsInner> items = new ArrayList<>();

        OranTeivEntityTypesItemsInner item1 = new OranTeivEntityTypesItemsInner();
        OranTeivEntityTypesItemsInner item2 = new OranTeivEntityTypesItemsInner();

        item1.setName("AntennaCapability");
        OranTeivHref entities1 = new OranTeivHref();
        entities1.setHref("/domains/EQUIPMENT_TO_RAN/entity-types/AntennaCapability/entities");
        item1.setEntities(entities1);

        item2.setName("AntennaModule");
        OranTeivHref entities2 = new OranTeivHref();
        entities2.setHref("/domains/EQUIPMENT_TO_RAN/entity-types/AntennaModule/entities");
        item2.setEntities(entities2);

        items.add(item1);
        items.add(item2);
        expectedResponse.setItems(items);
        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref("/domains/EQUIPMENT_TO_RAN/entity-types?offset=0&limit=2");
        expectedResponse.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref("/domains/EQUIPMENT_TO_RAN/entity-types?offset=2&limit=2");
        expectedResponse.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref("/domains/EQUIPMENT_TO_RAN/entity-types?offset=0&limit=2");
        expectedResponse.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref("/domains/EQUIPMENT_TO_RAN/entity-types?offset=0&limit=2");
        expectedResponse.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref("/domains/EQUIPMENT_TO_RAN/entity-types?offset=6&limit=2");
        expectedResponse.setLast(hrefLast);
        expectedResponse.setTotalCount(8);

        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/EQUIPMENT_TO_RAN/entity-types").offset(0)
                .limit(2).build();

        Assertions.assertEquals(expectedResponse, underTest.getTopologyEntityTypes("EQUIPMENT_TO_RAN", paginationDTO));
    }

    @Test
    void testGetDomainTypes() {
        OranTeivDomains expectedResponse = new OranTeivDomains();
        OranTeivDomainsItemsInner item1 = new OranTeivDomainsItemsInner();
        OranTeivDomainsItemsInner item2 = new OranTeivDomainsItemsInner();

        item1.setName("CLOUD");
        OranTeivHref entityType1 = new OranTeivHref();
        OranTeivHref relationshipType1 = new OranTeivHref();
        entityType1.setHref("/domains/CLOUD/entity-types");
        relationshipType1.setHref("/domains/CLOUD/relationship-types");
        item1.setEntityTypes(entityType1);
        item1.setRelationshipTypes(relationshipType1);

        item2.setName("CLOUD_TO_RAN");
        OranTeivHref entityType2 = new OranTeivHref();
        OranTeivHref relationshipType2 = new OranTeivHref();
        entityType2.setHref("/domains/CLOUD_TO_RAN/entity-types");
        relationshipType2.setHref("/domains/CLOUD_TO_RAN/relationship-types");
        item2.setEntityTypes(entityType2);
        item2.setRelationshipTypes(relationshipType2);

        List<OranTeivDomainsItemsInner> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        expectedResponse.setItems(items);

        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref("/domains?offset=0&limit=2");
        expectedResponse.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref("/domains?offset=2&limit=2");
        expectedResponse.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref("/domains?offset=0&limit=2");
        expectedResponse.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref("/domains?offset=0&limit=2");
        expectedResponse.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref("/domains?offset=6&limit=2");
        expectedResponse.setLast(hrefLast);
        expectedResponse.setTotalCount(2);

        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains").offset(0).limit(2).build();
        paginationDTO.setTotalSize(8);

        Assertions.assertEquals(expectedResponse, underTest.getDomainTypes(paginationDTO));
    }

    private static void mockGetTopologyById() {
        EntityType entity1 = getEntityTypeByName("GNBDUFunction");
        EntityType entity2 = getEntityTypeByName("NRCellDU");

        when(mockedDataPersistanceService.getTopology(entity1, "5A548EA9D166341776CA0695837E55D8")).thenReturn(
                generateResponse("GNBDUFunction", "5A548EA9D166341776CA0695837E55D8"));
        when(mockedDataPersistanceService.getTopology(entity2, "98C3A4591A37718E1330F0294E23B62A")).thenReturn(
                generateResponse("NRCellDU", "98C3A4591A37718E1330F0294E23B62A"));
        when(mockedDataPersistanceService.getTopology(entity2, "NOT_EXISTING")).thenThrow(TiesException.class);
    }

    private static void mockGetTopologyByType() {
        Map<String, Object> mockedResponse1 = new HashMap<>();
        PaginationMetaData pmd1 = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath("/domains/RAN/entity-types/GNBDUFunction/entities")
                .offset(0).limit(2).addQueryParameters("targetFilter", "/attributes/fdn;/attributes/id").addQueryParameters(
                        "scopeFilter",
                        "/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]").build();
        paginationDTO1.setTotalSize(2);
        mockedResponse1.putAll(pmd1.getObjectList(paginationDTO1));

        mockedResponse1.put("items", List.of(Map.of("fdn",
                "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=111", "id",
                "5BAE4346C241237AA8C74AE1259EF203"), Map.of("fdn",
                        "SubNetwork=SolarSystem/SubNetwork=Earth/SubNetwork=Europe/SubNetwork=Hungary/GNBDUFunction=112",
                        "id", "6B55F987ED838C0FA58DC11AB19CD1D3")));
        mockedResponse1.putAll(pmd1.getObjectList(paginationDTO1));

        when(mockedDataPersistanceService.getTopologyByType("GNBDUFunction", "/attributes/fdn;/attributes/id",
                "/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]", paginationDTO1))
                        .thenReturn(mockedResponse1);
    }

    private static void MockGetTopologyByTargetFilter() {
        Map<String, Object> mockedResponse = new HashMap<>();
        Map<String, Object> query = new HashMap<>();

        query.put("targetFilter", "/GNBDUFunction/id");

        PaginationMetaData pmd1 = new PaginationMetaData();

        PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/domains/RAN/entities").offset(0).limit(1)
                .addQueryParameters("targetFilter", "/GNBDUFunction/id").build();

        paginationDTO.setTotalSize(1);
        mockedResponse.putAll(pmd1.getObjectList(paginationDTO));
        mockedResponse.put("query", query);

        mockedResponse.put("items", List.of(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                "1050570EBB1315E1AE7A9FD5E1400A00")))));

        when(mockedDataPersistanceService.getEntitiesByDomain("RAN", "/GNBDUFunction/id", null, paginationDTO)).thenReturn(
                mockedResponse);

    }

    private static void mockGetRelationshipById() {
        String id = REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpEMzIxNUUwODU3MEJFNTgzMzlDNzQ2MzYyNkI1MEUzNzpQUk9WSURFUzpOUkNlbGxEVTo5OEMzQTQ1OTFBMzc3MThFMTMzMEYwMjk0RTIzQjYyQQ==";
        String wrongId = "NOT_EXISTING";
        String relationshipName = "GNBDUFUNCTION_PROVIDES_NRCELLDU";
        Map<String, Object> response = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
                generateResponse("D3215E08570BE58339C7463626B50E37", "98C3A4591A37718E1330F0294E23B62A", id,
                        Collections.EMPTY_LIST)));

        when(mockedDataPersistanceService.getRelationshipWithSpecifiedId(id, SchemaRegistry.getRelationTypeByName(
                relationshipName))).thenReturn(response);
        when(mockedDataPersistanceService.getRelationshipWithSpecifiedId(wrongId, SchemaRegistry.getRelationTypeByName(
                relationshipName))).thenThrow(TiesException.class);
    }

    private static void mockGetRelationshipByType() {
        Map<String, Object> mockedResponse = new HashMap<>();
        Map<String, Object> items = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "BCA882C87D49687E731F9B3872EFDBD3",
                REL_ID_PREFIX + "R05CRFVGdW5jdGlvbjpHTkJEVUZ1bmN0aW9uLzkxOlBST1ZJREVTOk5SQ2VsbERVOk5SQ2VsbERVLzE=",
                Collections.EMPTY_LIST)));
        RelationType relationType = getRelationTypeByName("GNBDUFUNCTION_PROVIDES_NRCELLDU");

        PaginationMetaData pmd1 = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath(
                "/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships").offset(0).limit(5).build();
        paginationDTO1.setTotalSize(1);
        mockedResponse.putAll(pmd1.getObjectList(paginationDTO1));
        mockedResponse.put(ITEMS, List.of(items));

        when(mockedDataPersistanceService.getRelationshipsByType(relationType, null, PaginationDTO.builder().basePath(
                "/domains/RAN/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships").offset(0).limit(5)
                .build())).thenReturn(mockedResponse);

        Map<String, Object> mockedResponse2 = new HashMap<>();
        Map<String, Object> items2 = Map.of("o-ran-smo-teiv-equipment:ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", List.of(
                generateResponse("A05C67D47D117C2DC5BDF5E00AE70", "2256120E73ADD4026A43A971DCE5C151",
                        REL_ID_PREFIX + "R05CQ1VVUEZ1bmN0aW9uOkJGRUVBQzJDRTYwMjczQ0IwQTc4MzE5Q0MyMDFBN0ZFOlJE=",
                        Collections.EMPTY_LIST)));
        RelationType relationType2 = getRelationTypeByName("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");

        PaginationMetaData pmd2 = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships").offset(0)
                .limit(5).build();
        paginationDTO2.setTotalSize(1);
        mockedResponse2.putAll(pmd2.getObjectList(paginationDTO2));
        mockedResponse2.put(ITEMS, List.of(items2));

        when(mockedDataPersistanceService.getRelationshipsByType(relationType2, null, PaginationDTO.builder().basePath(
                "/domains/EQUIPMENT/relationship-types/ANTENNAMODULE_REALISED_BY_ANTENNAMODULE/relationships").offset(0)
                .limit(5).build())).thenReturn(mockedResponse2);
    }

    private static void mockGetAllRelationships() {
        final EntityType gnbduFunction = getEntityTypeByName("GNBDUFunction");
        final EntityType nrSectorCarrier = getEntityTypeByName("NRSectorCarrier");
        Map<String, Object> mockedResponse1 = new HashMap<>();
        Map<String, Object> mockedResponse2 = new HashMap<>();
        List<Object> mapList = new ArrayList<>();
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "BCA882C87D49687E731F9B3872EFDBD3",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6RVV0cmFuQ2VsbDpCQ0E4ODJDODdENDk2ODdFNzMxRjlCMzg3MkVGREJEMw==",
                Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", List.of(generateResponse(
                "0525930249302B9649FC8F201EC4F7FC", "3256120E73ADD4026A43A971DCE5C151",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MQ==",
                Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-cloud-to-ran:GNBDUFUNCTION_REALISED_BY_CLOUDNATIVESYSTEM", List.of(
                generateResponse("0525930249302B9649FC8F201EC4F7FC", "2256120E73ADD4026A43A971DCE5C151",
                        REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MR==",
                        Collections.EMPTY_LIST))));
        mapList.add(Map.of("o-ran-smo-teiv-ran:NRSECTORCARRIER_USES_ANTENNACAPABILITY", List.of(generateResponse(
                "3256120E73ADD4026A43A971DCE5C151", "3223120E73ADD4026A43A971DCE5C151",
                REL_ID_PREFIX + "RU5vZGVCRnVuY3Rpb246MDUyNTkzMDI0OTMwMkI5NjQ5RkM4RjIwMUVDNEY3RkM6UFJPVklERVM6TFRFU2VjdG9yQ2FycmllcjozMjU2MTIwRTczQURENDAyNkE0M0E5NzFEQ0U1QzE1MS==",
                Collections.EMPTY_LIST))));

        PaginationMetaData pmd1 = new PaginationMetaData();
        PaginationDTO paginationDTO1 = PaginationDTO.builder().basePath(
                "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships").offset(0).limit(
                        100).build();
        paginationDTO1.setTotalSize(3);
        mockedResponse1.putAll(pmd1.getObjectList(paginationDTO1));
        mockedResponse1.put(ITEMS, List.of(mapList.get(0), mapList.get(1), mapList.get(2)));

        PaginationMetaData pmd2 = new PaginationMetaData();
        PaginationDTO paginationDTO2 = PaginationDTO.builder().basePath(
                "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships").offset(0).limit(
                        100).build();
        paginationDTO2.setTotalSize(1);
        mockedResponse2.putAll(pmd2.getObjectList(paginationDTO2));
        mockedResponse2.put(ITEMS, List.of(mapList.get(1), mapList.get(3)));

        when(mockedDataPersistanceService.getAllRelationships(gnbduFunction, getRelationTypesByEntityName("GNBDUFunction"),
                "0525930249302B9649FC8F201EC4F7FC", PaginationDTO.builder().basePath(
                        "/domains/RAN/entity-types/GNBDUFunction/0525930249302B9649FC8F201EC4F7FC/relationships").offset(0)
                        .limit(100).build())).thenReturn(mockedResponse1);
        when(mockedDataPersistanceService.getAllRelationships(nrSectorCarrier, getRelationTypesByEntityName(
                "NRSectorCarrier"), "3256120E73ADD4026A43A971DCE5C151", PaginationDTO.builder().basePath(
                        "/domains/RAN/entity-types/NRSectorCarrier/3256120E73ADD4026A43A971DCE5C151/relationships").offset(
                                0).limit(100).build())).thenReturn(mockedResponse2);
    }
}
