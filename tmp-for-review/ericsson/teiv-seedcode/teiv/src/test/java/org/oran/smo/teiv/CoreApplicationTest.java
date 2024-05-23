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
package org.oran.smo.teiv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.oran.smo.teiv.startup.SchemaHandler;
import org.oran.smo.teiv.utils.TiesConstants;
import jakarta.validation.ConstraintViolationException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CoreApplicationTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SchemaHandler schemaHandler;

    private final String ACCEPT_TYPE = "application/json";

    @Test
    void testMetricsAvailable() throws Exception {
        // spotless:off
        final MvcResult result = mvc.perform(get("/actuator/prometheus")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_not_supported_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_parse_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_parse_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_parse_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_parse_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_parse_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_parse_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_persist_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_persist_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_persist_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_persist_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_persist_success_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_persist_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_parse_seconds"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_parse_seconds"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_parse_seconds"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_create_persist_seconds"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_merge_persist_seconds"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_ingestion_event_topology_delete_persist_seconds"));

        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_domain_types_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_entity_types_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_relationship_types_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_entity_by_id_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_entities_by_type_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_entities_by_domain_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_relationship_by_id_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_relationships_by_type_fail_total"));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("ties_exposure_http_get_relationships_by_entity_id_fail_total"));
        // spotless:on
    }

    @ParameterizedTest()
    @CsvSource(value = { "getSchemas:/schemas", "getAllDomains:/domains",
            "getAllRelationshipsForEntityId:/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities/1/relationships",
            "getEntitiesByDomain:/domains/RAN_LOGICAL/entities?targetFilter=%2FNRCellDU%2Fattributes%2FnCI",
            "getRelationshipsByType:/domains/RAN_LOGICAL/relationship-types/GNBDUFUNCTION_PROVIDES_NRCELLDU/relationships",
            "getTopologyByEntityTypeName:/domains/RAN_LOGICAL/entity-types/GNBDUFunction/entities",
            "getTopologyEntityTypes:/domains/RAN_LOGICAL/entity-types",
            "getTopologyRelationshipTypes:/domains/RAN_LOGICAL/relationship-types" }, delimiter = ':')
    public void testPaginationRelatedEndpoints(String method, String url) throws Exception {
        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("offset", "-1").accept(ACCEPT_TYPE)).andExpect(status()
                .isBadRequest()).andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException())
                        .getMessage(), method + ".offset: must be greater than or equal to 0"));
        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("limit", "0").accept(ACCEPT_TYPE)).andExpect(status()
                .isBadRequest()).andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException())
                        .getMessage(), method + ".limit: must be greater than or equal to 1"));
        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("limit", "501").accept(ACCEPT_TYPE)).andExpect(status()
                .isBadRequest()).andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException())
                        .getMessage(), method + ".limit: must be less than or equal to 500"));

        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("offset", "0").accept(ACCEPT_TYPE)).andExpect(result -> {
            if (result.getResponse().getStatus() != HttpStatus.OK.value())
                assertNotEquals(ConstraintViolationException.class, Objects.requireNonNull(result.getResolvedException())
                        .getClass());
        });
        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("limit", "1").accept(ACCEPT_TYPE)).andExpect(result -> {
            if (result.getResponse().getStatus() != HttpStatus.OK.value())
                assertNotEquals(ConstraintViolationException.class, Objects.requireNonNull(result.getResolvedException())
                        .getClass());
        });
        mvc.perform(get(TiesConstants.REQUEST_MAPPING + url).param("limit", "500").accept(ACCEPT_TYPE)).andExpect(
                result -> {
                    if (result.getResponse().getStatus() != HttpStatus.OK.value())
                        assertNotEquals(ConstraintViolationException.class, Objects.requireNonNull(result
                                .getResolvedException()).getClass());
                });
    }
}
