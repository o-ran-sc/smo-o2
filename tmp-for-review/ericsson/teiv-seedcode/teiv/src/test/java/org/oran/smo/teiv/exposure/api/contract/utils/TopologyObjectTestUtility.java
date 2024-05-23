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

import org.oran.smo.teiv.api.model.OranTeivEntitiesResponseMessage;
import org.oran.smo.teiv.api.model.OranTeivHref;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.data.api.DataService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TopologyObjectTestUtility {

    private static final Map<String, Object> ITEMS =

            Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                    "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1", "attributes", Map.of(
                            "gNBDUId", 1), "metadata", Map.of("trustLevel", "RELIABLE"))

            ));

    private static DataService dataService;

    public static void getMockForAllTopologyObjectById(DataService dataService) {
        TopologyObjectTestUtility.dataService = dataService;
        mockRanGNBDUFunctionTopologyObject();
        mockRanNRCellDUTopologyObject();
    }

    private static void mockRanGNBDUFunctionTopologyObject() {

        OranTeivEntitiesResponseMessage gnbduMap = new OranTeivEntitiesResponseMessage();
        OranTeivEntitiesResponseMessage gnbduQueryMap = new OranTeivEntitiesResponseMessage();
        OranTeivHref hrefFirst = new OranTeivHref();
        hrefFirst.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50");
        gnbduMap.setFirst(hrefFirst);
        OranTeivHref hrefNext = new OranTeivHref();
        hrefNext.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50");
        gnbduMap.setNext(hrefNext);
        OranTeivHref hrefPrev = new OranTeivHref();
        hrefPrev.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50");
        gnbduMap.setPrev(hrefPrev);
        OranTeivHref hrefSelf = new OranTeivHref();
        hrefSelf.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50");
        gnbduMap.setSelf(hrefSelf);
        OranTeivHref hrefLast = new OranTeivHref();
        hrefLast.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&offset=0&limit=50");
        gnbduMap.setLast(hrefLast);

        gnbduMap.setItems(List.of(ITEMS));

        OranTeivHref hrefFirstQuery = new OranTeivHref();
        hrefFirstQuery.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100");
        gnbduQueryMap.setFirst(hrefFirstQuery);
        OranTeivHref hrefNextQuery = new OranTeivHref();
        hrefNextQuery.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100");
        gnbduQueryMap.setNext(hrefNextQuery);
        OranTeivHref hrefPrevQuery = new OranTeivHref();
        hrefPrevQuery.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100");
        gnbduQueryMap.setPrev(hrefPrevQuery);
        OranTeivHref hrefSelfQuery = new OranTeivHref();
        hrefSelfQuery.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100");
        gnbduQueryMap.setSelf(hrefSelfQuery);
        OranTeivHref hrefLastQuery = new OranTeivHref();
        hrefLastQuery.setHref(
                "/domains/RAN/entity-types/GNBDUFunction/entities?targetFilter=%2Fattributes(gNBDUId)&scopeFilter=%2Fattributes[@gNBDUId=1]&offset=0&limit=100");
        gnbduQueryMap.setLast(hrefLastQuery);

        gnbduQueryMap.setItems(List.of(ITEMS));

        when(dataService.getTopologyById(eq("GNBDUFunction"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio00001%2FGNBDUFunction%3D1")))
                        .thenReturn(

                                Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                                        "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1",
                                        "attributes", Map.of("gNBId", 1, "gNBDUId", 1, "gNBIdLength", 2, "dUpLMNId", Map.of(
                                                "mcc", "110", "mnc", "210")), "decorators", Map.of("location", "Stockholm"),
                                        "classifiers", List.of("Rural"), "sourceIds", List.of(
                                                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1"),
                                        "metadata", Map.of("trustLevel", "RELIABLE")))));

        when(dataService.getTopologyById(eq("GNBDUFunction"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio00001%2FGNBDUFunction%3D2")))
                        .thenReturn(Map.of("o-ran-smo-teiv-ran:GNBDUFunction", List.of(Map.of("id",
                                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=2", "attributes",
                                Map.of("gNBId", 2, "gNBDUId", 2, "gNBIdLength", 2, "dUpLMNId", Map.of("mcc", "110", "mnc",
                                        "210")), "decorators", Map.of("location", "Stockholm"), "classifiers", List.of(
                                                "Rural"), "sourceIds", List.of(
                                                        "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=2"),
                                "metadata", Map.of("trustLevel", "RELIABLE")))));

        when(dataService.getTopologyById(eq("GBFunction"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio00001%2FGNBDUFunction%3D1")))
                        .thenThrow(TiesException.unknownEntityType("GBFunction", Collections.emptyList()));

        when(dataService.getTopologyById(eq("NRDU"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D1")))
                        .thenThrow(TiesException.unknownEntityType("NRDU", Collections.emptyList()));

        when(dataService.getTopologyByType(eq("GNBDUFunction"), endsWith("%2Fattributes(gNBDUId)"), any(), any()))
                .thenReturn(gnbduMap);

        when(dataService.getTopologyByType(eq("GNBDUFunction"), endsWith("%2Fattributes(gNBDUId)"), endsWith(
                "%2Fattributes[@gNBDUId=1]"), any())).thenReturn(gnbduQueryMap);

        when(dataService.getTopologyByType(eq("GNBBUFunction"), any(), any(), any())).thenThrow(TiesException
                .unknownEntityType("GNBBUFunction", Collections.emptyList()));
    }

    private static void mockRanNRCellDUTopologyObject() {
        when(dataService.getTopologyById(eq("NRCellDU"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D1")))
                        .thenReturn(Map.of("o-ran-smo-teiv-ran:NRCellDU", List.of(Map.of("id",
                                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=1",
                                "attributes", Map.of("nCI", 1, "nRPCI", 35, "nRTAC", 50, "cellLocalId", 91), "decorators",
                                Map.of("location", "Stockholm"), "classifiers", List.of("Rural"), "sourceIds", List.of(
                                        "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=1"),
                                "metadata", Map.of("trustLevel", "RELIABLE")))));

        when(dataService.getTopologyById(eq("NRCellDU"), eq(
                "urn%3A3gpp%3Adn%3A%2FMeContext%3DNR01%2FManagedElement%3DNR01gNodeBRadio%2FGNBDUFunction%3D1%2FNRCellDU%3D2")))
                        .thenReturn(Map.of("o-ran-smo-teiv-ran:NRCellDU", List.of(Map.of("id",
                                "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=2",
                                "attributes", Map.of("nCI", 5, "nRPCI", 35, "nRTAC", 50, "cellLocalId", 95), "decorators",
                                Map.of("location", "Stockholm"), "classifiers", List.of("Rural"), "sourceIds", List.of(
                                        "urn:3gpp:dn:MeContext=NR01,ManagedElement=NR01gNodeBRadio,GNBDUFunction=1,NRCellDU=2"),
                                "metadata", Map.of("trustLevel", "RELIABLE")))));
    }
}
