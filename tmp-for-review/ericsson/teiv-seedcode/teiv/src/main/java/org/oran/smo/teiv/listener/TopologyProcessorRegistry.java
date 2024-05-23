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
package org.oran.smo.teiv.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oran.smo.teiv.CustomMetrics;
import org.oran.smo.teiv.utils.TiesConstants;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("ingestion")
public class TopologyProcessorRegistry {

    private final CustomMetrics metrics;
    private final CreateTopologyProcessor createTopologyProcessor;
    private final MergeTopologyProcessor mergeTopologyProcessor;
    private final DeleteTopologyProcessor deleteTopologyProcessor;
    private final SourceEntityDeleteTopologyProcessor sourceEntityDeleteTopologyProcessor;
    private final SourceEntityDeleteTopologyProcessorV1 sourceEntityDeleteTopologyProcessorV1;
    private final UnsupportedTopologyEventProcessor unsupportedTopologyEventProcessor;

    @Value("${feature_flags.use_alternate_delete_logic}")
    private boolean useAlternateDeleteLogic;

    public TopologyProcessor getProcessor(CloudEvent event) {
        String cloudEventType = getCloudEventType(event);
        switch (cloudEventType) {
            case TiesConstants.CLOUD_EVENT_WITH_TYPE_CREATE -> {
                log.debug("Create CloudEvent received with id: {}", event.getId());
                metrics.incrementNumReceivedCloudEventCreate();
                return createTopologyProcessor;
            }
            case TiesConstants.CLOUD_EVENT_WITH_TYPE_MERGE -> {
                log.debug("Merge CloudEvent received with id: {}", event.getId());
                metrics.incrementNumReceivedCloudEventMerge();
                return mergeTopologyProcessor;
            }
            case TiesConstants.CLOUD_EVENT_WITH_TYPE_DELETE -> {
                log.debug("Delete CloudEvent received with id: {}", event.getId());
                metrics.incrementNumReceivedCloudEventDelete();
                return deleteTopologyProcessor;
            }
            case TiesConstants.CLOUD_EVENT_WITH_TYPE_SOURCE_ENTITY_DELETE -> {
                log.debug("Source Entity Delete CloudEvent received with id: {}", event.getId());
                metrics.incrementNumReceivedCloudEventSourceEntityDelete();
                return useAlternateDeleteLogic ?
                        sourceEntityDeleteTopologyProcessorV1 :
                        sourceEntityDeleteTopologyProcessor;
            }
            default -> {
                metrics.incrementNumReceivedCloudEventNotSupported();
                log.error("Erroneous CloudEvent type: {}", cloudEventType);
                return unsupportedTopologyEventProcessor;
            }
        }
    }

    private String getCloudEventType(final CloudEvent event) {
        final String[] tokens = event.getType().split("\\.");
        if (tokens.length == 2) {
            return tokens[1];
        } else {
            return "UNKNOWN_EVENT";
        }
    }
}
