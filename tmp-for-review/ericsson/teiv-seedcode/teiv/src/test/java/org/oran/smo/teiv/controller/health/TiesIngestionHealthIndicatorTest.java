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
package org.oran.smo.teiv.controller.health;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.oran.smo.teiv.availability.DependentServiceAvailabilityKafka;
import org.oran.smo.teiv.schema.PostgresSchemaLoader;
import org.oran.smo.teiv.startup.SchemaHandler;

@AutoConfigureMockMvc
@SpringBootTest(properties = { "spring.profiles.active=test,ingestion", "management.endpoint.health.probes.enabled=true",
        "management.endpoint.health.group.readiness.include=readinessState,tiesIngestion" })
public class TiesIngestionHealthIndicatorTest {
    private final String readinessProbePath = "/actuator/health/readiness";
    private final String livenessProbePath = "/actuator/health/liveness";
    private final String tiesIngestionProbePath = "/actuator/health/tiesIngestion";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private HealthStatus healthStatus;

    @SpyBean
    private DependentServiceAvailabilityKafka spiedDependentServiceAvailabilityKafka;

    private static final String SERVICE_NAME = "top-exp-inv-ingestion";

    @MockBean
    PostgresSchemaLoader postgresSchemaLoader;

    @AfterEach
    protected void tearDown() {
        healthStatus.setSchemaInitialized(false);
        doReturn(false).when(spiedDependentServiceAvailabilityKafka).checkService();
    }

    @Test
    void upAndHealthy() throws Exception {
        doReturn(true).when(spiedDependentServiceAvailabilityKafka).checkService();
        healthStatus.setSchemaInitialized(true);
        mvc.perform(get(readinessProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(
                content().json("{'status' : 'UP'}"));
        mvc.perform(get(livenessProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(
                content().json("{'status' : 'UP'}"));
        mvc.perform(get(tiesIngestionProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().json(String.format("{'status':'UP','details':{'UP': '%s is UP and Healthy.'}}",
                        SERVICE_NAME)));
    }

    @Test
    void downSchemaNotInitialized() throws Exception {
        healthStatus.setSchemaInitialized(false);
        doReturn(true).when(spiedDependentServiceAvailabilityKafka).checkService();
        performReadinessGroupProbesDownWithMessage(" Schema is yet to be initialized.");
    }

    @Test
    void downKafkaUnavailable() throws Exception {
        SchemaHandler schemaHandlerSpy = Mockito.spy(new SchemaHandler(postgresSchemaLoader, healthStatus));
        schemaHandlerSpy.initializeSchema();
        assertTrue(healthStatus.isSchemaInitialized());
        doReturn(false).when(spiedDependentServiceAvailabilityKafka).checkService();
        performReadinessGroupProbesDownWithMessage(" Kafka is unavailable.");
    }

    private void performReadinessGroupProbesDownWithMessage(String message) throws Exception {
        mvc.perform(get(livenessProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(
                content().json("{'status' : 'UP'}"));
        mvc.perform(get(readinessProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError())
                .andExpect(content().json("{'status' : 'DOWN'}"));
        mvc.perform(get(tiesIngestionProbePath).contentType(MediaType.APPLICATION_JSON)).andExpect(status()
                .is5xxServerError()).andExpect(content().json(String.format(
                        "{'status' : 'DOWN', 'details':{'Error':'%s is DOWN because:%s'}}", SERVICE_NAME, message)))
                .andReturn();
    }
}
