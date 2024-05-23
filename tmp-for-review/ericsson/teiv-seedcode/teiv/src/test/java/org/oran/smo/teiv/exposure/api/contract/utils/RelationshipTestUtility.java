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
package org.oran.smo.teiv.exposure.api.contract.utils;

import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.api.model.OranTeivRelationshipsResponseMessage;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.data.api.DataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;

public class RelationshipTestUtility {

    private static final Map<String, Object> ITEMS = Map.of("o-ran-smo-teiv-ran:GNBDUFUNCTION_PROVIDES_NRCELLDU", List.of(
            Map.of("id", "urn:sha512:R05CRFVGdW5jdGlvbjpTdWJOZXR3b3JrPUV1cm9wZSxTdWJOZXR3", "aSide",
                    "urn:3gpp:dn:ManagedElement=1,GNBDUFunction=1", "bSide",
                    "urn:3gpp:dn:ManagedElement=1,GNBDUFunction=1,NRCellDU=1", "decorators", Map.of("location",
                            "Stockholm"), "classifiers", List.of("Rural"), "sourceIds", new ArrayList<>(), "metadata", Map
                                    .of("trustLevel", "RELIABLE"))));
    private static DataService dataService;

    public static void getMockForAllRelationshipsForObjectId(DataService dataService) {
        RelationshipTestUtility.dataService = dataService;
        mockRanGNBDUFunctionRelationships();
    }

    private static void mockRanGNBDUFunctionRelationships() {
        OranTeivRelationshipsResponseMessage gnbduMap = new OranTeivRelationshipsResponseMessage();

        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100");
        gnbduMap.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100");
        gnbduMap.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100");
        gnbduMap.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100");
        gnbduMap.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities/urn%3A3gpp%3Adn%3A%2FManagedElement%3D1%2FGNBDUFunction%3D1/relationships?offset=0&limit=100");
        gnbduMap.setLast(hrefLast);

        gnbduMap.setItems(List.of(ITEMS));

        when(dataService.getAllRelationshipsForObjectId(eq("GNBDUFunction"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio00001%2FGNBDUFunction%3D1"),
                any())).thenReturn(gnbduMap);

        when(dataService.getAllRelationshipsForObjectId(eq("5GCell"), eq("R05CRFVGdW5jdGlvbg"), any())).thenThrow(
                TiesException.unknownEntityType("5GCell", Collections.emptyList()));
    }
}
