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
package org.oran.smo.teiv.db;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;
import org.testcontainers.utility.TestcontainersConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RyukImageNameSubstitutor extends ImageNameSubstitutor {

    private final TestcontainersConfiguration configuration = TestcontainersConfiguration.getInstance();

    @Override
    public DockerImageName apply(DockerImageName original) {
        final String configuredRyukImage = configuration.getEnvVarOrProperty("substitutor.ryuk.container.image", "");

        if ("testcontainers/ryuk".equals(original.getRepository()) && "".equals(original.getRegistry())) {
            if (configuredRyukImage.isEmpty()) {
                // if you have configured the RyukImageNameSubstitutor class you probably want also a substitute configured
                log.info("No ryuk substitute is configured");
                return original;
            }

            log.debug("Substituting testcontainers/ryuk with {}", configuredRyukImage);
            return DockerImageName.parse(configuredRyukImage);
        }
        return original;
    }

    @Override
    protected String getDescription() {
        return getClass().getSimpleName();
    }
}
