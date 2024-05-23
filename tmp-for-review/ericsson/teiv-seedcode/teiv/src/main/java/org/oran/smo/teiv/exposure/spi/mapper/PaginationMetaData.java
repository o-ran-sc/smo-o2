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

import java.util.HashMap;
import java.util.Map;

public class PaginationMetaData {

    public Map<String, Object> getObjectList(final PaginationDTO paginationDTO) {
        Map<String, Object> innerResponse = new HashMap<>();
        PageMetaData self = new PageMetaData(paginationDTO);
        innerResponse.put("self", self);

        innerResponse.put("next", hasNextPage(paginationDTO) ?
                new PageMetaData(Math.min(paginationDTO.getOffset() + paginationDTO.getLimit(), paginationDTO
                        .getTotalSize()), paginationDTO) :
                self);

        innerResponse.put("last", hasNextPage(paginationDTO) ?
                new PageMetaData(calculateLastPageOffset(paginationDTO), paginationDTO) :
                self);

        innerResponse.put("first", new PageMetaData(0, paginationDTO));

        innerResponse.put("prev", hasPrevPage(paginationDTO) ?
                new PageMetaData(Math.max(paginationDTO.getOffset() - paginationDTO.getLimit(), 0), paginationDTO) :
                self);

        innerResponse.put("totalCount", paginationDTO.getTotalSize());

        return innerResponse;
    }

    private boolean hasNextPage(final PaginationDTO paginationDTO) {
        return (paginationDTO.getOffset() + paginationDTO.getLimit()) < paginationDTO.getTotalSize() && paginationDTO
                .getTotalSize() > 0;
    }

    private boolean hasPrevPage(final PaginationDTO paginationDTO) {
        return paginationDTO.getOffset() > 0 && paginationDTO.getTotalSize() > 0;
    }

    private int calculateLastPageOffset(final PaginationDTO paginationDTO) {
        int diff = paginationDTO.getTotalSize() - paginationDTO.getOffset();

        if (diff % paginationDTO.getLimit() == 0) {
            return (diff / paginationDTO.getLimit() - 1) * paginationDTO.getLimit() + paginationDTO.getOffset();
        }

        return (diff / paginationDTO.getLimit()) * paginationDTO.getLimit() + paginationDTO.getOffset();

    }
}
