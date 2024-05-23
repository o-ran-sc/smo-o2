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
import lombok.Getter;

import java.util.Map;

@Getter
public class PageMetaData {

    private final String href;

    public PageMetaData(final PaginationDTO paginationDTO) {
        this(paginationDTO.getOffset(), paginationDTO.getLimit(), paginationDTO);
    }

    public PageMetaData(final int offset, final PaginationDTO paginationDTO) {
        this(offset, paginationDTO.getLimit(), paginationDTO);
    }

    public PageMetaData(final int offset, final int limit, final PaginationDTO paginationDTO) {
        StringBuilder stringBuilder = new StringBuilder(paginationDTO.getBasePath() + String.format("?offset=%s&limit=%s",
                offset, limit));
        for (Map.Entry<String, String> entry : paginationDTO.getQueryParameters().entrySet()) {
            if (entry.getValue() != null) {
                stringBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        this.href = stringBuilder.toString();
    }
}
