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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Data
@Component
public class CustomMetrics {

    @Getter(AccessLevel.PRIVATE)
    private final AtomicLong tiesSubscriptionGaugeCounter = new AtomicLong(0L);

    private final MeterRegistry meterRegistry;

    private final Counter numReceivedCloudEventCreate;

    private final Counter numReceivedCloudEventMerge;

    private final Counter numReceivedCloudEventDelete;

    private final Counter numReceivedCloudEventSourceEntityDelete;

    private final Counter numReceivedCloudEventNotSupported;

    private final Timer cloudEventMergePersistTime;

    private final Timer cloudEventCreatePersistTime;

    private final Timer cloudEventDeletePersistTime;

    private final Timer cloudEventSourceEntityDeletePersistTime;

    private final Timer cloudEventMergeParseTime;

    private final Timer cloudEventCreateParseTime;

    private final Timer cloudEventDeleteParseTime;

    private final Timer cloudEventSourceEntityDeleteParseTime;

    private final Counter numSuccessfullyParsedMergeCloudEvents;

    private final Counter numSuccessfullyParsedDeleteCloudEvents;

    private final Counter numSuccessfullyParsedSourceEntityDeleteCloudEvents;

    private final Counter numSuccessfullyParsedCreateCloudEvents;

    private final Counter numUnsuccessfullyParsedMergeCloudEvents;

    private final Counter numUnsuccessfullyParsedCreateCloudEvents;

    private final Counter numUnsuccessfullyParsedDeleteCloudEvents;

    private final Counter numUnsuccessfullyParsedSourceEntityDeleteCloudEvents;

    private final Counter numSuccessfullyPersistedMergeCloudEvents;

    private final Counter numSuccessfullyPersistedCreateCloudEvents;

    private final Counter numSuccessfullyPersistedDeleteCloudEvents;

    private final Counter numSuccessfullyPersistedSourceEntityDeleteCloudEvents;

    private final Counter numUnsuccessfullyPersistedMergeCloudEvents;

    private final Counter numUnsuccessfullyPersistedCreateCloudEvents;

    private final Counter numUnsuccessfullyPersistedDeleteCloudEvents;

    private final Counter numUnsuccessfullyPersistedSourceEntityDeleteCloudEvents;

    private final Counter numUnsuccessfullyExposedRelationshipsByEntityId;

    private final Counter numUnsuccessfullyExposedEntityById;

    private final Counter numUnsuccessfullyExposedEntitiesByType;

    private final Counter numUnsuccessfullyExposedEntitiesByDomain;

    private final Counter numUnsuccessfullyExposedRelationshipById;

    private final Counter numUnsuccessfullyExposedRelationshipsByType;

    private final Counter numUnsuccessfullyExposedRelationshipTypes;

    private final Counter numUnsuccessfullyExposedEntityTypes;

    private final Counter numUnsuccessfullyExposedDomainTypes;

    private final Counter numIgnoredAttributes;

    public CustomMetrics(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        numReceivedCloudEventCreate = Counter.builder("ties_ingestion_event_topology_create_total").register(meterRegistry);

        numReceivedCloudEventMerge = Counter.builder("ties_ingestion_event_topology_merge_total").register(meterRegistry);

        numReceivedCloudEventDelete = Counter.builder("ties_ingestion_event_topology_delete_total").register(meterRegistry);

        numReceivedCloudEventSourceEntityDelete = Counter.builder(
                "ties_ingestion_event_topology_source_entity_delete_total").register(meterRegistry);

        numReceivedCloudEventNotSupported = Counter.builder("ties_ingestion_event_topology_not_supported_total").register(
                meterRegistry);

        numSuccessfullyParsedMergeCloudEvents = Counter.builder("ties_ingestion_event_topology_merge_parse_success_total")
                .register(meterRegistry);

        numSuccessfullyParsedCreateCloudEvents = Counter.builder("ties_ingestion_event_topology_create_parse_success_total")
                .register(meterRegistry);

        numSuccessfullyParsedDeleteCloudEvents = Counter.builder("ties_ingestion_event_topology_delete_parse_success_total")
                .register(meterRegistry);

        numSuccessfullyParsedSourceEntityDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_source_entity_delete_parse_success_total").register(meterRegistry);

        numUnsuccessfullyParsedMergeCloudEvents = Counter.builder("ties_ingestion_event_topology_merge_parse_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyParsedCreateCloudEvents = Counter.builder("ties_ingestion_event_topology_create_parse_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyParsedDeleteCloudEvents = Counter.builder("ties_ingestion_event_topology_delete_parse_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyParsedSourceEntityDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_source_entity_delete_parse_fail_total").register(meterRegistry);

        numSuccessfullyPersistedMergeCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_merge_persist_success_total").register(meterRegistry);

        numSuccessfullyPersistedCreateCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_create_persist_success_total").register(meterRegistry);

        numSuccessfullyPersistedDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_delete_persist_success_total").register(meterRegistry);

        numSuccessfullyPersistedSourceEntityDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_source_entity_delete_persist_success_total").register(meterRegistry);

        numUnsuccessfullyPersistedMergeCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_merge_persist_fail_total").register(meterRegistry);

        numUnsuccessfullyPersistedCreateCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_create_persist_fail_total").register(meterRegistry);

        numUnsuccessfullyPersistedDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_delete_persist_fail_total").register(meterRegistry);

        numUnsuccessfullyPersistedSourceEntityDeleteCloudEvents = Counter.builder(
                "ties_ingestion_event_topology_source_entity_delete_persist_fail_total").register(meterRegistry);

        numUnsuccessfullyExposedRelationshipsByEntityId = Counter.builder(
                "ties_exposure_http_get_relationships_by_entity_id_fail_total").register(meterRegistry);

        numUnsuccessfullyExposedEntityById = Counter.builder("ties_exposure_http_get_entity_by_id_fail_total").register(
                meterRegistry);

        numUnsuccessfullyExposedEntitiesByType = Counter.builder("ties_exposure_http_get_entities_by_type_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyExposedEntitiesByDomain = Counter.builder("ties_exposure_http_get_entities_by_domain_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyExposedRelationshipById = Counter.builder("ties_exposure_http_get_relationship_by_id_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyExposedRelationshipsByType = Counter.builder(
                "ties_exposure_http_get_relationships_by_type_fail_total").register(meterRegistry);

        numUnsuccessfullyExposedRelationshipTypes = Counter.builder("ties_exposure_http_get_relationship_types_fail_total")
                .register(meterRegistry);

        numUnsuccessfullyExposedEntityTypes = Counter.builder("ties_exposure_http_get_entity_types_fail_total").register(
                meterRegistry);

        numUnsuccessfullyExposedDomainTypes = Counter.builder("ties_exposure_http_get_domain_types_fail_total").register(
                meterRegistry);

        cloudEventMergePersistTime = Timer.builder("ties_ingestion_event_topology_merge_persist_seconds").register(
                meterRegistry);

        cloudEventCreatePersistTime = Timer.builder("ties_ingestion_event_topology_create_persist_seconds").register(
                meterRegistry);

        cloudEventDeletePersistTime = Timer.builder("ties_ingestion_event_topology_delete_persist_seconds").register(
                meterRegistry);

        cloudEventSourceEntityDeletePersistTime = Timer.builder(
                "ties_ingestion_event_topology_source_entity_delete_persist_seconds").register(meterRegistry);

        cloudEventMergeParseTime = Timer.builder("ties_ingestion_event_topology_merge_parse_seconds").register(
                meterRegistry);

        cloudEventCreateParseTime = Timer.builder("ties_ingestion_event_topology_create_parse_seconds").register(
                meterRegistry);

        cloudEventDeleteParseTime = Timer.builder("ties_ingestion_event_topology_delete_parse_seconds").register(
                meterRegistry);

        cloudEventSourceEntityDeleteParseTime = Timer.builder(
                "ties_ingestion_event_topology_source_entity_delete_parse_seconds").register(meterRegistry);

        numIgnoredAttributes = Counter.builder("ties_ingestion_event_ignored_attributes_total").register(meterRegistry);
    }

    /**
     * It increments the metric that counts the received "create" type events from Kafka
     */
    public void incrementNumReceivedCloudEventCreate() {
        numReceivedCloudEventCreate.increment();
    }

    /**
     * It increments the metric that counts the received "merge" type events from Kafka
     */
    public void incrementNumReceivedCloudEventMerge() {
        numReceivedCloudEventMerge.increment();
    }

    /**
     * It increments the metric that counts the received "delete" type events from Kafka
     */
    public void incrementNumReceivedCloudEventDelete() {
        numReceivedCloudEventDelete.increment();
    }

    /**
     * It increments the metric that counts the received "source-entity-delete" type events from Kafka
     */
    public void incrementNumReceivedCloudEventSourceEntityDelete() {
        numReceivedCloudEventSourceEntityDelete.increment();
    }

    /**
     * It increments the metric that counts the received not supported or erroneous events from Kafka
     */
    public void incrementNumReceivedCloudEventNotSupported() {
        numReceivedCloudEventNotSupported.increment();
    }

    /**
     * It records a time for the cloudEventMergePersistTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventMergePersistTime(long nanoseconds) {
        cloudEventMergePersistTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventDeletePersistTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventDeletePersistTime(long nanoseconds) {
        cloudEventDeletePersistTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventSourceEntityDeletePersistTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventSourceEntityDeletePersistTime(long nanoseconds) {
        cloudEventSourceEntityDeletePersistTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventCreatePersistTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventCreatePersistTime(long nanoseconds) {
        cloudEventCreatePersistTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventMergeParseTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventMergeParseTime(long nanoseconds) {
        cloudEventMergeParseTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventCreateParseTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventCreateParseTime(long nanoseconds) {
        cloudEventCreateParseTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventDeleteParseTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventDeleteParseTime(long nanoseconds) {
        cloudEventDeleteParseTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It records a time for the cloudEventSourceEntityDeleteParseTime metric in nanoseconds
     *
     * @param nanoseconds
     *     time to record
     */
    public void recordCloudEventSourceEntityDeleteParseTime(long nanoseconds) {
        cloudEventSourceEntityDeleteParseTime.record(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * It increments the numSuccessfullyParsedCreateCloudEvents metric.
     */
    public void incrementNumSuccessfullyParsedCreateCloudEvents() {
        numSuccessfullyParsedCreateCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyParsedMergeCloudEvents metric.
     */
    public void incrementNumSuccessfullyParsedMergeCloudEvents() {
        numSuccessfullyParsedMergeCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyParsedDeleteCloudEvents metric.
     */
    public void incrementNumSuccessfullyParsedDeleteCloudEvents() {
        numSuccessfullyParsedDeleteCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyParsedSourceEntityDeleteCloudEvents metric.
     */
    public void incrementNumSuccessfullyParsedSourceEntityDeleteCloudEvents() {
        numSuccessfullyParsedSourceEntityDeleteCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyParsedCreateCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyParsedCreateCloudEvents() {
        numUnsuccessfullyParsedCreateCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyParsedMergeCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyParsedMergeCloudEvents() {
        numUnsuccessfullyParsedMergeCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyParsedDeleteCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyParsedDeleteCloudEvents() {
        numUnsuccessfullyParsedDeleteCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyParsedSourceEntityDeleteCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyParsedSourceEntityDeleteCloudEvents() {
        numUnsuccessfullyParsedSourceEntityDeleteCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyPersistedCreateCloudEvents metric.
     */
    public void incrementNumSuccessfullyPersistedCreateCloudEvents() {
        numSuccessfullyPersistedCreateCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyPersistedMergeCloudEvents metric.
     */
    public void incrementNumSuccessfullyPersistedMergeCloudEvents() {
        numSuccessfullyPersistedMergeCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyPersistedDeleteCloudEvents metric.
     */
    public void incrementNumSuccessfullyPersistedDeleteCloudEvents() {
        numSuccessfullyPersistedDeleteCloudEvents.increment();
    }

    /**
     * It increments the numSuccessfullyPersistedSourceEntityDeleteCloudEvents metric.
     */
    public void incrementNumSuccessfullyPersistedSourceEntityDeleteCloudEvents() {
        numSuccessfullyPersistedSourceEntityDeleteCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyPersistedCreateCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyPersistedCreateCloudEvents() {
        numUnsuccessfullyPersistedCreateCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyPersistedMergeCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyPersistedMergeCloudEvents() {
        numUnsuccessfullyPersistedMergeCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyPersistedDeleteCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyPersistedDeleteCloudEvents() {
        numUnsuccessfullyPersistedDeleteCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyPersistedSourceEntityDeleteCloudEvents metric.
     */
    public void incrementNumUnsuccessfullyPersistedSourceEntityDeleteCloudEvents() {
        numUnsuccessfullyPersistedSourceEntityDeleteCloudEvents.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedAllRelationshipsByEntityId metric.
     */
    public void incrementNumUnsuccessfullyExposedAllRelationshipsByEntityId() {
        numUnsuccessfullyExposedRelationshipsByEntityId.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedEntityById metric.
     */
    public void incrementNumUnsuccessfullyExposedEntityById() {
        numUnsuccessfullyExposedEntityById.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedEntitiesByType metric.
     */
    public void incrementNumUnsuccessfullyExposedEntitiesByType() {
        numUnsuccessfullyExposedEntitiesByType.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedEntitiesByDomain metric.
     */
    public void incrementNumUnsuccessfullyExposedEntitiesByDomain() {
        numUnsuccessfullyExposedEntitiesByDomain.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedRelationshipById metric.
     */
    public void incrementNumUnsuccessfullyExposedRelationshipById() {
        numUnsuccessfullyExposedRelationshipById.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedRelationshipsByType metric.
     */
    public void incrementNumUnsuccessfullyExposedRelationshipsByType() {
        numUnsuccessfullyExposedRelationshipsByType.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedRelationshipTypes metric.
     */
    public void incrementNumUnsuccessfullyExposedRelationshipTypes() {
        numUnsuccessfullyExposedRelationshipTypes.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedEntityTypes metric.
     */
    public void incrementNumUnsuccessfullyExposedEntityTypes() {
        numUnsuccessfullyExposedEntityTypes.increment();
    }

    /**
     * It increments the numUnsuccessfullyExposedDomainTypes metric.
     */
    public void incrementNumUnsuccessfullyExposedDomainTypes() {
        numUnsuccessfullyExposedDomainTypes.increment();
    }

    public void incrementNumReceivedTiesSubscriptions(int amountToAdd) {
        tiesSubscriptionGaugeCounter.addAndGet(amountToAdd);
    }

    public void resetNumReceivedTiesSubscriptions() {
        tiesSubscriptionGaugeCounter.set(0);
    }

    public void setNumReceivedTiesSubscriptions(int amount) {
        tiesSubscriptionGaugeCounter.set(amount);
    }

    public void incrementNumIgnoredAttributes() {
        numIgnoredAttributes.increment();
    }
}
