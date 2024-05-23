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
package org.oran.smo.teiv.utils.exposure;

import org.oran.smo.teiv.exposure.spi.mapper.PageMetaData;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Map;

public class PaginationVerifierTestUtil {

    public static void verifyResponse(Map<String, Object> expected, Map<String, Object> actual) {
        String[] paginationKeys = { "next", "prev", "first", "last", "self" };
        for (String key : expected.keySet()) {
            if (Arrays.stream(paginationKeys).anyMatch(paginationKey -> paginationKey.equals(key))) {
                Assertions.assertEquals(((PageMetaData) expected.get(key)).getHref(), ((PageMetaData) actual.get(key))
                        .getHref());
            } else {
                Assertions.assertEquals(expected.get(key), actual.get(key));
            }
        }
    }

    public static void verifyResponse(ResponseEntity<Object> expected, ResponseEntity<Object> actual) {
        Map<String, Object> expectedBody = (Map<String, Object>) expected.getBody();
        Map<String, Object> actualBody = (Map<String, Object>) actual.getBody();
        String[] paginationKeys = { "next", "prev", "first", "last", "self" };
        for (String key : expectedBody.keySet()) {
            if (Arrays.stream(paginationKeys).anyMatch(paginationKey -> paginationKey.equals(key))) {
                Assertions.assertEquals(((PageMetaData) expectedBody.get(key)).getHref(), ((PageMetaData) actualBody.get(
                        key)).getHref());
            } else {
                Assertions.assertEquals(expectedBody.get(key), actualBody.get(key));
            }
        }
    }
}
