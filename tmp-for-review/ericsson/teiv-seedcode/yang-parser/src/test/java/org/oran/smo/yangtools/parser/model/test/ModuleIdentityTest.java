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
package org.oran.smo.yangtools.parser.model.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.ModuleIdentity;

public class ModuleIdentityTest {

    @Test
    public void test_all_ok() {

        final ModuleIdentity module1 = new ModuleIdentity("module1");
        assertTrue(module1.getModuleName().equals("module1"));
        assertTrue(module1.getRevision().equals(ModuleIdentity.UNKWOWN_REVISION));
        assertTrue(module1.isUnknownRevision() == true);

        final ModuleIdentity module2 = new ModuleIdentity("module2", "2000-01-01");
        assertTrue(module2.getModuleName().equals("module2"));
        assertTrue(module2.getRevision().equals("2000-01-01"));
        assertTrue(module2.isUnknownRevision() == false);

        final ModuleIdentity module3 = new ModuleIdentity("module3", null);
        assertTrue(module3.getModuleName().equals("module3"));
        assertTrue(module3.getRevision() == null);
        assertTrue(module3.isUnknownRevision() == false);

        try {
            new ModuleIdentity("module1", "");
            fail("Should have thrown exception.");
        } catch (final Exception ignore) {
        }
    }

}
