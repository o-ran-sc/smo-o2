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
package org.oran.smo.teiv.service.cloudevent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.teiv.service.cloudevent.data.Entity;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;
import org.oran.smo.teiv.utils.YangParser;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudEventParser {
    private static final String ENTITIES = "entities";
    private static final String RELATIONSHIPS = "relationships";
    private final ObjectMapper objectMapper;

    public ParsedCloudEventData getCloudEventData(CloudEvent cloudEvent) {
        final CloudEventData cloudEventData = Objects.requireNonNull(cloudEvent.getData());
        JsonNode eventPayload;
        try {
            eventPayload = objectMapper.readValue(cloudEventData.toBytes(), JsonNode.class);
        } catch (IOException e) {
            log.error("Cannot parse CloudEvent data: ", e);
            return null;
        }
        final List<Entity> entities = new ArrayList<>();
        Boolean parsedEntities = processEntities(eventPayload, entities);
        if (parsedEntities.equals(Boolean.FALSE)) {
            return null;
        }

        final List<Relationship> relationships = new ArrayList<>();
        Boolean parsedRelationship = processRelationships(eventPayload, relationships);
        if (parsedRelationship.equals(Boolean.FALSE)) {
            return null;
        }

        return new ParsedCloudEventData(entities, relationships);
    }

    private Boolean processEntities(JsonNode eventPayload, List<Entity> entities) {
        JsonNode entitiesJsonNode = eventPayload.get(ENTITIES);
        if (entitiesJsonNode != null && (entitiesJsonNode.getNodeType() == JsonNodeType.ARRAY)) {
            for (JsonNode entityNode : entitiesJsonNode) {
                try {
                    parseEntities(entityNode, entities);
                } catch (IOException e) {
                    log.error("Cannot parse entity: " + entitiesJsonNode, e);
                    return false;
                }
            }
        }
        //TODO: Remove support for old format DnR events when RTA has been updated.
        if (entitiesJsonNode != null && (entitiesJsonNode.getNodeType() == JsonNodeType.OBJECT)) {
            try {
                parseEntities(entitiesJsonNode, entities);
            } catch (IOException e) {
                log.error("Cannot parse entity: " + entitiesJsonNode, e);
                return false;
            }
        }
        return true;
    }

    private Boolean processRelationships(JsonNode eventPayload, List<Relationship> relationships) {
        JsonNode relationshipJsonNode = eventPayload.get(RELATIONSHIPS);
        if (relationshipJsonNode != null && (relationshipJsonNode.getNodeType() == JsonNodeType.ARRAY)) {
            for (JsonNode relationshipNode : relationshipJsonNode) {
                try {
                    parseRelationships(relationshipNode, relationships);
                } catch (IOException e) {
                    log.error("Cannot parse relationship: " + relationshipNode, e);
                    return false;
                }
            }
        }
        //TODO: Remove support for old format DnR events when RTA has been updated.
        if (relationshipJsonNode != null && (relationshipJsonNode.getNodeType() == JsonNodeType.OBJECT)) {
            try {
                parseRelationships(relationshipJsonNode, relationships);
            } catch (IOException e) {
                log.error("Cannot parse relationship: " + relationshipJsonNode, e);
                return false;
            }
        }
        return true;
    }

    public void parseEntities(JsonNode entitiesJsonNode, List<Entity> entities) throws IOException {
        YangDataDomDocumentRoot entityDom = YangParser.getYangDataDomDocumentRoot(entitiesJsonNode);
        entityDom.getChildren().forEach(child -> {
            Entity entity = new Entity();
            entity.parseObject(child);
            entities.add(entity);
        });
    }

    private void parseRelationships(JsonNode relationshipNode, List<Relationship> relationships) throws IOException {
        YangDataDomDocumentRoot relDom = YangParser.getYangDataDomDocumentRoot(relationshipNode);
        relDom.getChildren().forEach(child -> {
            Relationship relationship = new Relationship();
            relationship.parseObject(child);
            relationships.add(relationship);
        });
    }
}
