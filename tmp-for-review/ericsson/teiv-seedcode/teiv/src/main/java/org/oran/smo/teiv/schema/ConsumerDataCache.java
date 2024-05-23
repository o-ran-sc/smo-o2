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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerDataCache {

    private final DataPersistanceService dataPersistanceService;

    @Cacheable("classifiers")
    public Set<String> getClassifiers() {
        return Collections.unmodifiableSet(dataPersistanceService.loadClassifiers());
    }

    @Cacheable("decorators")
    public Map<String, DataType> getDecorators() {
        return Collections.unmodifiableMap(dataPersistanceService.loadDecorators());
    }

    @Cacheable("validClassifiers")
    public Set<String> getValidClassifiers(String partialClassifier) {
        return getClassifiers().stream().filter(c -> c.contains(partialClassifier)).collect(Collectors.toSet());
    }

    @CacheEvict(value = { "classifiers", "decorators", "validClassifiers" }, allEntries = true)
    @Scheduled(fixedRateString = "${spring.caching.consumer-data-ttl-ms}")
    public void emptyConsumerDataCaches() {
        log.debug("Emptying consumer data caches");
    }
}
