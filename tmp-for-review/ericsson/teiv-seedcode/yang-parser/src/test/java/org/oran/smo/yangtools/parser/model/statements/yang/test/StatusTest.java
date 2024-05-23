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

import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class StatusTest extends YangTestCommon {

    @Test
    public void test_status_module1() {

        parseRelativeImplementsYangModels(Arrays.asList("status-test/status-test-module1.yang"));

        final YModule module = getModule("status-test-module1");

        assertNoFindings();

        // -------------------- simple stuff --------------------

        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf11").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf12").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf13").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf21").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf22").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf23").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf31").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf32").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf33").getEffectiveStatus().equals(YStatus.OBSOLETE));

        // -------------------- simple, using groupings --------------------

        assertTrue(getLeaf(getContainer(module, "cont4"), "leaf97").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getLeaf(getContainer(module, "cont4"), "leaf98").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont4"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont5"), "leaf97").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont5"), "leaf98").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont5"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont6"), "leaf97").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont6"), "leaf98").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont6"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));
    }

    @Test
    public void test_status_module2() {

        parseRelativeImplementsYangModels(Arrays.asList("status-test/status-test-module2.yang"));

        final YModule module = getModule("status-test-module2");

        assertNoFindings();

        // -------------------- status under uses --------------------

        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf97").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf98").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont1"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf97").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf98").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont2"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf97").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf98").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont3"), "leaf99").getEffectiveStatus().equals(YStatus.OBSOLETE));

        // -------------------- status under grouping --------------------

        assertTrue(getLeaf(getContainer(module, "cont11"), "leaf91").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getLeaf(getContainer(module, "cont11"), "leaf92").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont11"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont12"), "leaf91").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont12"), "leaf92").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont12"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont13"), "leaf91").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont13"), "leaf92").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont13"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));
    }

    @Test
    public void test_status_module3() {

        parseRelativeImplementsYangModels(Arrays.asList("status-test/status-test-module3.yang"));

        final YModule module = getModule("status-test-module3");

        assertNoFindings();

        // -------------------- status under both grouping and uses --------------------

        assertTrue(getLeaf(getContainer(module, "cont31"), "leaf91").getEffectiveStatus().equals(YStatus.CURRENT));
        assertTrue(getLeaf(getContainer(module, "cont31"), "leaf92").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont31"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont32"), "leaf91").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont32"), "leaf92").getEffectiveStatus().equals(YStatus.DEPRECATED));
        assertTrue(getLeaf(getContainer(module, "cont32"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));

        assertTrue(getLeaf(getContainer(module, "cont33"), "leaf91").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont33"), "leaf92").getEffectiveStatus().equals(YStatus.OBSOLETE));
        assertTrue(getLeaf(getContainer(module, "cont33"), "leaf93").getEffectiveStatus().equals(YStatus.OBSOLETE));

    }

}
