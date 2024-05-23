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

import java.util.Objects;

import org.oran.smo.teiv.pgsqlgenerator.schema.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HashInfoEntity implements Table {
    private String name;
    private String hashedValue;
    private String type;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HashInfoEntity myClass = (HashInfoEntity) obj;
        return name.equals(myClass.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String getTableName() {
        return "hash_info";
    }

    @Override
    public String getColumnsForCopyStatement() {
        return "(\"name\", \"hashedValue\", \"type\")";
    }

    @Override
    public String getRecordForCopyStatement() {
        return this.getName() + "\t" + this.getHashedValue() + "\t" + this.getType() + "\n";
    }
}
