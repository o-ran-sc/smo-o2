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

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;

import static org.oran.smo.teiv.utils.TiesConstants.QUERY;
import static org.oran.smo.teiv.utils.TiesConstants.SCOPE_FILTER;
import static org.oran.smo.teiv.utils.TiesConstants.TARGET_FILTER;

@AllArgsConstructor
public class QueryMetaData {

    private String targetFilter;
    private String scopeFilter;

    public Map<String, Object> getObjectList() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> innerResponse = new HashMap<>();

        boolean hasResponse = false;

        if (!targetFilter.isEmpty()) {
            innerResponse.put(TARGET_FILTER, targetFilter);
            hasResponse = true;

        }
        if (!scopeFilter.isEmpty()) {
            innerResponse.put(SCOPE_FILTER, scopeFilter);
            hasResponse = true;
        }
        if (hasResponse) {
            response.put(QUERY, innerResponse);
        }
        return response;
    }

}
