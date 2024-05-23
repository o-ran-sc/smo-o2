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

import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.oran.smo.teiv.utils.exposure.PaginationVerifierTestUtil.verifyResponse;

public class PaginationMetaDataTest {

    @Test
    void testIsItFirst() {

        Map<String, Object> result = new HashMap<>();
        try (MockedStatic<RequestContextHolder> requestContextHolderMockedStatic = Mockito.mockStatic(
                RequestContextHolder.class)) {
            requestContextHolderMockedStatic.when(RequestContextHolder::currentRequestAttributes).thenReturn(
                    new ServletRequestAttributes(new MockHttpServletRequest()));

            PaginationMetaData paginationMetaData1 = new PaginationMetaData();
            PageMetaData pageMetaDataSelf = new PageMetaData(0, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataNext = new PageMetaData(5, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataLast = new PageMetaData(50, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataPrev = new PageMetaData(0, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataFirst = new PageMetaData(0, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());

            result.put("self", pageMetaDataSelf);
            result.put("first", pageMetaDataFirst);
            result.put("prev", pageMetaDataPrev);
            result.put("next", pageMetaDataNext);
            result.put("last", pageMetaDataLast);

            final PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/pathTo/endPoint").offset(0).limit(5)
                    .build();
            paginationDTO.setTotalSize(55);
            verifyResponse(result, paginationMetaData1.getObjectList(paginationDTO));
        }
    }

    @Test
    void testIsItLast() {

        Map<String, Object> result = new HashMap<>();
        try (MockedStatic<RequestContextHolder> requestContextHolderMockedStatic = Mockito.mockStatic(
                RequestContextHolder.class)) {
            requestContextHolderMockedStatic.when(RequestContextHolder::currentRequestAttributes).thenReturn(
                    new ServletRequestAttributes(new MockHttpServletRequest()));

            PaginationMetaData paginationMetaData1 = new PaginationMetaData();
            PageMetaData pageMetaDataSelf = new PageMetaData(60, 10, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataNext = new PageMetaData(60, 10, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataLast = new PageMetaData(60, 10, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataPrev = new PageMetaData(50, 10, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataFirst = new PageMetaData(0, 10, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());

            result.put("self", pageMetaDataSelf);
            result.put("next", pageMetaDataNext);
            result.put("last", pageMetaDataLast);
            result.put("prev", pageMetaDataPrev);
            result.put("first", pageMetaDataFirst);

            final PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/pathTo/endPoint").offset(60).limit(10)
                    .build();
            paginationDTO.setTotalSize(70);
            verifyResponse(result, paginationMetaData1.getObjectList(paginationDTO));
        }
    }

    @Test
    void testMiddleTable() {

        Map<String, Object> result = new HashMap<>();
        try (MockedStatic<RequestContextHolder> requestContextHolderMockedStatic = Mockito.mockStatic(
                RequestContextHolder.class)) {
            requestContextHolderMockedStatic.when(RequestContextHolder::currentRequestAttributes).thenReturn(
                    new ServletRequestAttributes(new MockHttpServletRequest()));

            PaginationMetaData paginationMetaData1 = new PaginationMetaData();
            PageMetaData pageMetaDataSelf = new PageMetaData(10, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataPrev = new PageMetaData(5, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataNext = new PageMetaData(15, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataFirst = new PageMetaData(0, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());
            PageMetaData pageMetaDataLast = new PageMetaData(65, 5, PaginationDTO.builder().basePath("/pathTo/endPoint")
                    .build());

            result.put("self", pageMetaDataSelf);
            result.put("prev", pageMetaDataPrev);
            result.put("next", pageMetaDataNext);
            result.put("first", pageMetaDataFirst);
            result.put("last", pageMetaDataLast);

            final PaginationDTO paginationDTO = PaginationDTO.builder().basePath("/pathTo/endPoint").offset(10).limit(5)
                    .build();
            paginationDTO.setTotalSize(70);
            verifyResponse(result, paginationMetaData1.getObjectList(paginationDTO));
        }
    }
}
