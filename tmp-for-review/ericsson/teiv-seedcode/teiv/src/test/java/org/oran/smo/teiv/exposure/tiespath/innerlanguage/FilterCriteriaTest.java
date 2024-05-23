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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jooq.SelectField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.oran.smo.teiv.schema.DataType;

class FilterCriteriaTest {
    @Test
    void testFilterCriteria() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES).params(List
                .of("gNBId")).build();
        filterCriteria.setTargets(List.of(targetObject));
        ScopeObject scopeObject = new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES, "gNBIDLength",
                QueryFunction.EQ, "1", DataType.BIGINT);
        ScopeLogicalBlock logicalBlock = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(logicalBlock);

        Assertions.assertEquals(1, filterCriteria.getTargets().size());
        Assertions.assertEquals("RAN_LOGICAL", filterCriteria.getDomain());
        Assertions.assertEquals(QueryFunction.EQ, ((ScopeLogicalBlock) filterCriteria.getScope()).getScopeObject()
                .getQueryFunction());
    }

    @Test
    void testGetTables() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES).params(List
                .of("gNBId", "gNBIdLength")).build();
        filterCriteria.setTargets(Arrays.asList(targetObject));
        OrLogicalBlock orLogicalBlock = new OrLogicalBlock();
        ScopeObject scopeObject1 = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBIdLength",
                QueryFunction.EQ, "1", DataType.BIGINT);
        ScopeObject scopeObject2 = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBId", QueryFunction.EQ,
                "8", DataType.BIGINT);
        ScopeLogicalBlock scopeLogicalBlock1 = new ScopeLogicalBlock(scopeObject1);
        ScopeLogicalBlock scopeLogicalBlock2 = new ScopeLogicalBlock(scopeObject2);
        orLogicalBlock.setChildren(Arrays.asList(scopeLogicalBlock1, scopeLogicalBlock2));
        filterCriteria.setScope(orLogicalBlock);

        Set<SelectField> expected = new HashSet<>();
        expected.add(null);
        Assertions.assertEquals(expected, filterCriteria.getTables());
    }

    @Test
    void testGetSelects() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");

        TargetObject targetObject = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES).params(List
                .of("gNBId", "gNBIdLength")).build();
        filterCriteria.setTargets(Arrays.asList(targetObject));

        Set<SelectField> expected = new HashSet<>();
        expected.add(null);
        Assertions.assertEquals(expected, filterCriteria.getSelects());
    }

    @Test
    void testGetCondition() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        ScopeObject scopeObject = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, QueryFunction.EQ, "1",
                DataType.BIGINT);
        ScopeLogicalBlock scopeLogicalBlock = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scopeLogicalBlock);

        Assertions.assertEquals(null, filterCriteria.getCondition());
    }
}
