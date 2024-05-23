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
package org.oran.smo.yangtools.parser.model.statements.yang.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YSubmodule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class BelongsToTest extends YangTestCommon {

    @Test
    public void testBelongsToOwningModuleNotInInput() {

        parseRelativeImplementsYangModels(Arrays.asList("belongs-to-test/test-submodule.yang"));

        assertHasFindingOfType(ParserFindingType.P039_UNRESOLVABLE_BELONGS_TO.toString());

        final YSubmodule submodule = getSubModule("test-submodule");
        assertTrue(submodule != null);

        assertStatementHasFindingOfType(submodule.getBelongsTo(), ParserFindingType.P039_UNRESOLVABLE_BELONGS_TO
                .toString());
    }

    @Test
    public void testBelongsToOwningModuleNotTheOwner() {

        parseRelativeImplementsYangModels(Arrays.asList("belongs-to-test/test-submodule.yang",
                "belongs-to-test/test-module.yang"));

        assertHasFindingOfType(ParserFindingType.P048_ORPHAN_SUBMODULE.toString());

        final YSubmodule submodule = getSubModule("test-submodule");
        assertTrue(submodule != null);
    }

    @Test
    public void testBelongsToOwningModuleNotAmodule() {

        parseRelativeImplementsYangModels(Arrays.asList("belongs-to-test/test-submodule.yang",
                "belongs-to-test/test-submodule2.yang"));

        assertHasFindingOfType(ParserFindingType.P046_NOT_A_MODULE.toString());

        final YSubmodule submodule = getSubModule("test-submodule2");
        assertTrue(submodule != null);

        assertStatementHasFindingOfType(submodule.getBelongsTo(), ParserFindingType.P046_NOT_A_MODULE.toString());
    }

}
