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
package org.oran.smo.teiv.schema;

import org.oran.smo.teiv.exposure.spi.DataPersistanceService;
import org.oran.smo.teiv.exposure.spi.impl.DataPersistanceServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsumerDataCacheTest {

    private static final DataPersistanceService dataPersistanceService = mock(DataPersistanceServiceImpl.class);
    private final ConsumerDataCache underTest = new ConsumerDataCache(dataPersistanceService);

    @BeforeAll
    static void beforeAll() {
        when(dataPersistanceService.loadClassifiers()).thenReturn(Set.of("gnbdu-function-model:Rural",
                "gnbcucp-gnbcuup-model:Weekend"));
        when(dataPersistanceService.loadDecorators()).thenReturn(Map.of("gnbdu-function-model:location", DataType.PRIMITIVE,
                "gnbcucp-gnbcuup-model:metadata", DataType.CONTAINER));
    }

    @Test
    void testGetValidClassifiers() {
        assertEquals(Set.of("gnbdu-function-model:Rural"), underTest.getValidClassifiers("gnbdu-function-model:Rural"));
        assertEquals(Set.of("gnbdu-function-model:Rural"), underTest.getValidClassifiers("gnbdu-function-model:"));
        assertEquals(Set.of("gnbdu-function-model:Rural", "gnbcucp-gnbcuup-model:Weekend"), underTest.getValidClassifiers(
                ""));
        assertEquals(Collections.emptySet(), underTest.getValidClassifiers("gnbcucp-gnbcuup-model:Weekday"));
    }

    @Test
    void testGetClassifiers() {
        assertEquals(Set.of("gnbdu-function-model:Rural", "gnbcucp-gnbcuup-model:Weekend"), underTest.getClassifiers());
    }

    @Test
    void testGetDecorators() {
        assertEquals(Map.of("gnbdu-function-model:location", DataType.PRIMITIVE, "gnbcucp-gnbcuup-model:metadata",
                DataType.CONTAINER), underTest.getDecorators());
    }
}
