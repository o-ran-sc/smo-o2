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
package org.oran.smo.yangtools.parser.model.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.util.YangIdentity;

public class YangIdentityTest {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_all_ok() {

        final YangIdentity YangIdentity1 = new YangIdentity("namespace1", "module1", "name1");
        final YangIdentity YangIdentity2 = new YangIdentity("namespace1", "module1", "name1");

        assertTrue(YangIdentity1.equals(YangIdentity1));
        assertTrue(YangIdentity1.equals(YangIdentity2));
        assertTrue(YangIdentity2.equals(YangIdentity1));
        assertFalse(YangIdentity1.equals(null));
        assertFalse(YangIdentity1.equals("whatever"));
        assertFalse(YangIdentity1.equals(new YangIdentity("namespace2", "module2", "name2")));

        final YangIdentity YangIdentity3 = new YangIdentity("namespace1", "module1", "name2");
        assertTrue(YangIdentity3.getIdentityName().equals("name2"));
        assertTrue(YangIdentity3.getIdentityModuleName().equals("module1"));
        assertTrue(YangIdentity3.getIdentityNamespace().equals("namespace1"));
    }

    @Test
    public void test_failures() {

        try {
            new YangIdentity(null, null, null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new YangIdentity("ns", null, null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new YangIdentity(null, "name", null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}
    }

}
