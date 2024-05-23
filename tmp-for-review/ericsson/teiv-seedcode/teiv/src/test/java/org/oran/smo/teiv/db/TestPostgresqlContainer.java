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

import java.io.IOException;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class TestPostgresqlContainer extends PostgreSQLContainer<TestPostgresqlContainer> {

    private static TestPostgresqlContainer container;

    private TestPostgresqlContainer(DockerImageName image) {
        super(image);
    }

    public static TestPostgresqlContainer getInstance() {
        if (container == null) {
            container = new TestPostgresqlContainer(DockerImageName.parse("postgis/postgis:13-3.4-alpine")
                    .asCompatibleSubstituteFor("postgres"));

            container.withCopyFileToContainer(MountableFile.forClasspathResource(
                    "pgsqlschema/00_init-oran-smo-teiv-data.sql"), "/pgsqlschema/00_init-oran-smo-teiv-data.sql");
            container.withCopyFileToContainer(MountableFile.forClasspathResource(
                    "pgsqlschema/01_init-oran-smo-teiv-model.sql"), "/pgsqlschema/01_init-oran-smo-teiv-model.sql");
            container.withCopyFileToContainer(MountableFile.forClasspathResource("data/data.sql"), "/02_data.sql");
            container.setCommand("postgres", "-c", "max_connections=2000");

            container.start();
            try {
                container.execInContainer("psql", "-U", "test", "-w", "-f", "/pgsqlschema/00_init-oran-smo-teiv-data.sql",
                        "--set=pguser=\"test\";");
                container.execInContainer("psql", "-U", "test", "-w", "-f", "/pgsqlschema/01_init-oran-smo-teiv-model.sql",
                        "--set=pguser=\"test\";");
            } catch (UnsupportedOperationException | IOException | InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return container;
    }

    public static void loadSampleData() {
        try {
            container.execInContainer("psql", "-U", "test", "-w", "-f", "/02_data.sql", "--set=pguser=\"test\";");
        } catch (UnsupportedOperationException | IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
