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
package org.oran.smo.teiv.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;

public class CloudEventTestUtil {

    private CloudEventTestUtil() {
    }

    public static CloudEvent getCloudEvent(final String type, final String data) {
        return CloudEventBuilder.v1().withId("1.0").withSource(URI.create("http://localhost:8080/local-source"))
                .withDataContentType("application/yang-data+json").withDataSchema(URI.create(
                        "http://localhost:8080/schema/v1/hello-world")).withExtension("correlationid",
                                "test-correlation-id").withType("ran-logical-topology." + type).withData("application/json",
                                        URI.create("http://localhost/schema"), data.getBytes()).build();
    }

    public static CloudEvent getCloudEventFromJsonFile(final String path) {
        String jsonString = readJsonFileAsString(path);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);

            CloudEventBuilder cloudEventBuilder = CloudEventBuilder.fromSpecVersion(SpecVersion.parse(rootNode.get(
                    "specversion").asText()));
            cloudEventBuilder.withId(rootNode.get("id").asText());
            cloudEventBuilder.withSource(URI.create(rootNode.get("source").asText()));
            cloudEventBuilder.withType(rootNode.get("type").asText());
            cloudEventBuilder.withTime(OffsetDateTime.parse(rootNode.get("time").asText()));
            cloudEventBuilder.withDataContentType(rootNode.get("datacontenttype").asText());
            cloudEventBuilder.withDataSchema(URI.create(rootNode.get("dataschema").asText()));
            cloudEventBuilder.withData(rootNode.get("data").toString().getBytes());
            CloudEvent event = cloudEventBuilder.build();
            return event;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readJsonFileAsString(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
