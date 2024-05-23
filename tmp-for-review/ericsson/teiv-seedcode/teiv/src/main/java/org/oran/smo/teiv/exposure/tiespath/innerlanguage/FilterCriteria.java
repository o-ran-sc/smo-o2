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
package org.oran.smo.teiv.exposure.tiespath.innerlanguage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.SelectField;
import org.jooq.util.xml.jaxb.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Slf4j
public class FilterCriteria {
    private final String domain;
    private List<TargetObject> targets = new ArrayList<>();
    private LogicalBlock scope;

    public FilterCriteria(String domain) {
        this.domain = domain;
    }

    public Condition getCondition() {
        return scope.getCondition();
    }

    public Set<Table> getTables() {
        Set<Table> tables = new HashSet<>();
        tables.addAll(scope.getTables());

        targets.forEach(t -> tables.add(getTablesFromTarget(t)));

        return tables;
    }

    public Set<SelectField> getSelects() {
        Set<SelectField> selectFields = new HashSet<>();

        targets.forEach(t -> selectFields.add(getSelectFromTarget(t)));

        return selectFields;
    }

    private SelectField getSelectFromTarget(TargetObject t) {
        log.trace(t.toString());
        return null;
    }

    @SuppressWarnings("squid:S4144")
    private Table getTablesFromTarget(TargetObject t) {
        log.trace(t.toString());
        return null;
    }
}
