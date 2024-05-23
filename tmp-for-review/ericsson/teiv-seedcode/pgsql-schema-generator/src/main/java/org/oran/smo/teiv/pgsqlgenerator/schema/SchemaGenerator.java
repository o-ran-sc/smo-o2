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
package org.oran.smo.teiv.pgsqlgenerator.schema;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.util.Assert;

import org.oran.smo.teiv.pgsqlgenerator.Entity;
import org.oran.smo.teiv.pgsqlgenerator.Module;
import org.oran.smo.teiv.pgsqlgenerator.PgSchemaGeneratorException;
import org.oran.smo.teiv.pgsqlgenerator.Relationship;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SchemaGenerator {
    protected File schema;
    protected String sqlStatements;

    /**
     * Template for generating the schema
     */
    public final void generate(List<Module> modules, List<Entity> entities, List<Relationship> relationships) {
        prepareSchema();
        setSqlStatements(modules, entities, relationships);
        writeSqlStatementsToSchema();
    }

    protected abstract void prepareSchema();

    protected abstract void setSqlStatements(List<Module> modules, List<Entity> entities, List<Relationship> relationships);

    private void writeSqlStatementsToSchema() {
        if (!isEmpty(sqlStatements)) {
            Assert.notNull(schema, "Schema file is null");
            try (FileOutputStream outputStream = new FileOutputStream(schema, true)) {
                byte[] strToBytes = sqlStatements.getBytes(StandardCharsets.UTF_8);
                outputStream.write(strToBytes);
            } catch (IOException exception) {
                if (schema.getName().endsWith("data.sql")) {
                    throw PgSchemaGeneratorException.writeGeneratedSchemaException("ties.data", exception);
                } else {
                    throw PgSchemaGeneratorException.writeGeneratedSchemaException("ties.model", exception);
                }
            }
        } else {
            log.warn("No SQL statements to write to schema");
        }
    }
}
