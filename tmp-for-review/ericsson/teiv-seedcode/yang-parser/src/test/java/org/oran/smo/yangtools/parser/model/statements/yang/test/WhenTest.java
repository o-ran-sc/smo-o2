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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YWhen;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class WhenTest extends YangTestCommon {

    @Test
    public void testWhen() {
        parseRelativeImplementsYangModels(Arrays.asList("when-test/when-test.yang"));
        final YModule yModule = getModule("when-test");

        final YLeaf leafWithoutWhen = getLeafUnderContainer(yModule, "cont1", "leaf11");
        assertTrue(leafWithoutWhen.getWhens().isEmpty());
        assertNoFindingsOnStatement(leafWithoutWhen);

        final YLeaf leafWithWhen = getLeafUnderContainer(yModule, "cont1", "leaf12");
        final YWhen when1 = leafWithWhen.getWhens().get(0);
        assertTrue(when1 != null);
        assertEquals("../leaf11 = 'Hello'", when1.getValue());
        assertEquals(Boolean.FALSE, when1.appliesToParentSchemaNode());
        assertNoFindingsOnStatement(leafWithWhen);
        assertNoFindingsOnStatement(when1);

        final YLeaf augmentedLeafWithWhen = getLeafUnderContainer(yModule, "cont2", "leaf22");
        final YWhen when2 = augmentedLeafWithWhen.getWhens().get(0);
        assertTrue(when2 != null);
        assertEquals("/this:cont1/this:leaf11 = 'World'", when2.getValue());
        assertEquals(Boolean.TRUE, when2.appliesToParentSchemaNode());
        assertNoFindingsOnStatement(augmentedLeafWithWhen);
        assertNoFindingsOnStatement(when2);

        final YWhen newWhen = new YWhen(when2.getParentStatement(), when2.getDomElement());
        newWhen.cloneFrom(when2);
        assertTrue(newWhen.getParentStatement() == augmentedLeafWithWhen);
    }
}
