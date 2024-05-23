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
package org.oran.smo.teiv.pgsqlgenerator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.oran.smo.teiv.pgsqlgenerator.schema.Table;
import lombok.Getter;
import lombok.Builder;
import lombok.Setter;

@Getter
@Builder
public class Module implements Table {
    private String name;
    private String namespace;
    private String domain;
    private String revision;
    private String content;
    private String ownerAppId;
    private String status;
    @Setter
    @Builder.Default
    private Collection<String> includedModules = List.of();

    @Override
    public String getTableName() {
        return "module_reference";
    }

    @Override
    public String getColumnsForCopyStatement() {
        return "(\"name\", \"namespace\", \"domain\", \"includedModules\", \"revision\", \"content\", \"ownerAppId\", \"status\")";
    }

    @Override
    public String getRecordForCopyStatement() {
        return this.getName() + "\t" + this.getNamespace() + "\t" + (!this.getDomain().isEmpty() ?
                this.getDomain() :
                "\\N") + "\t" + this.getIncludedModules().stream().map(moduleRef -> "\"" + moduleRef + "\"")
                        .toList() + "\t" + this.getRevision() + "\t" + Base64.getEncoder().encodeToString(this.getContent()
                                .replaceAll("\\r\\n?", "\n").getBytes(StandardCharsets.UTF_8)) + "\t" + this
                                        .getOwnerAppId() + "\t" + this.getStatus() + "\n";
    }
}
