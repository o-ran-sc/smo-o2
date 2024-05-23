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
package org.oran.smo.teiv.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseGenerator {

    /**
     * Generates a response
     *
     * @param entityType
     * @param id
     * @return response
     */
    public static Map<String, Object> generateResponse(String entityType, String id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> attributesMap = new HashMap<>();
        switch (entityType) {
            case "GNBDUFunction" -> {
                response.put("o-ran-smo-teiv-ran:GNBDUFunction", List.of(responseData));
                responseData.put("id", "GNBDUFunction:" + id);
                attributesMap.put("gNBDUId", null);
                attributesMap.put("fdn", "GNBDUFunction/" + id);
                attributesMap.put("dUpLMNId", Map.of("mcc", 456, "mnc", 82));
                attributesMap.put("gNBId", id);
                attributesMap.put("gNBIdLength", 2);
            }
            case "NRCellDU" -> {
                response.put("o-ran-smo-teiv-ran:NRCellDU", List.of(responseData));
                responseData.put("id", "NRCellDU:" + id);
                attributesMap.put("fdn", "NRCellDU/" + id);
                attributesMap.put("nCI", id);
                attributesMap.put("id", "NRCellDU:" + id);
                attributesMap.put("nRTAC", 456);
                attributesMap.put("nRPCI", 789);
            }
        }
        responseData.put("attributes", attributesMap);
        return response;
    }

    /**
     * Generates response for relationships
     *
     * @param aSide
     *     url for aSide
     * @param bSide
     *     url for bSide
     * @param id
     *     of relationship
     * @return response
     */
    public static Map<String, Object> generateResponse(String aSide, String bSide, String id, List sourceIds) {
        final Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", id);
        dataMap.put("aSide", aSide);
        dataMap.put("bSide", bSide);
        dataMap.put("sourceIds", sourceIds);
        return dataMap;
    }

    public static Map<String, Object> generateResponse(String aSide, String bSide, String id) {
        final Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", id);
        dataMap.put("aSide", aSide);
        dataMap.put("bSide", bSide);
        return dataMap;
    }

}
