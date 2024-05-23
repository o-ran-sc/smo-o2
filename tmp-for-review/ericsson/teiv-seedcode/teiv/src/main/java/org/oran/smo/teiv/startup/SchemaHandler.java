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
package org.oran.smo.teiv.startup;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.controller.health.HealthStatus;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchemaHandler {
    private final SchemaLoader postgresSchemaLoader;
    private final HealthStatus healthStatus;

    /**
     * Loads the schema registry at application startup.
     */
    @Order(value = 10)
    @EventListener(value = ApplicationReadyEvent.class)
    public void initializeSchema() throws SchemaLoaderException {
        log.debug("Start schema initialization");
        healthStatus.setSchemaInitialized(false);
        postgresSchemaLoader.loadSchemaRegistry();
        log.info("Schema initialized successfully...");
        healthStatus.setSchemaInitialized(true);
    }
}
