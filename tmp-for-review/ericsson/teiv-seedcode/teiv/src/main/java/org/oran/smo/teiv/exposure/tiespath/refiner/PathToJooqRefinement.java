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

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.FilterCriteria;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.LogicalBlock;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;
import org.jooq.Condition;
import org.jooq.SelectJoinStep;

@UtilityClass
public class PathToJooqRefinement {

    /**
     * Converts LogicalBlocks of InnerLanguageDTO to SelectJoinStep
     *
     * @param filterCriteria
     *     the InnerLanguageDTO
     * @return the select join step
     */
    public static SelectJoinStep toJooq(FilterCriteria filterCriteria) {
        throw new NotImplementedException(filterCriteria.toString());
    }

    /**
     * Converts LogicalBlock to JooQ query part
     *
     * @param logicalBlock
     *     the LogicalBlock
     * @return the condition
     */
    public static Condition logicalBlockToJooq(LogicalBlock logicalBlock) {
        // convert LB to JOOQ condition recursive!
        throw new NotImplementedException(logicalBlock.toString());
    }
}
