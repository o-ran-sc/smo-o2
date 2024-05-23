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
package org.oran.smo.teiv.exposure.tiespath.refiner;

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ContainerType;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.QueryFunction;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.FilterCriteria;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeObject;
import org.oran.smo.teiv.schema.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.apache.commons.lang3.NotImplementedException;

public class PathToJooqRefinementTest {
    @Test
    void toJooqTest() {
        Assertions.assertThrows(NotImplementedException.class, () -> PathToJooqRefinement.toJooq(new FilterCriteria(
                "RAN_LOGICAL")));
    }

    @Test
    void logicalBlockToJooqTest() {
        ScopeObject scopeObject = new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES, "gNBIdLength",
                QueryFunction.EQ, "1", DataType.BIGINT);
        Assertions.assertThrows(NotImplementedException.class, () -> PathToJooqRefinement.logicalBlockToJooq(
                new ScopeLogicalBlock(scopeObject)));
    }
}
