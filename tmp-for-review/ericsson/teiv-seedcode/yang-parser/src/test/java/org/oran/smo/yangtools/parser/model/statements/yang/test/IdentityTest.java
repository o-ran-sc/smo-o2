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
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class IdentityTest extends YangTestCommon {

    @Test
    public void testIdentity() {

        parseRelativeImplementsYangModels(Arrays.asList("identity-test/identity-test-module1.yang"));

        final YModule module = getModule("identity-test-module1");

        assertNoFindings();

        assertTrue(module.getIdentities().get(0).getStatementIdentifier().equals("identity1"));
        assertTrue(module.getIdentities().get(0).getIdentityName().equals("identity1"));
        assertTrue(module.getIdentities().get(1).getBases().size() == 1);
        assertTrue(module.getIdentities().get(2).getIfFeatures().get(0).getValue().equals("blue-moon"));

        assertTrue(module.getIdentities().get(4).getStatus().isDeprecated());
        assertTrue(module.getIdentities().get(4).getBases().size() == 2);
        assertTrue(module.getIdentities().get(4).getBases().get(0).getValue().equals("identity1"));
        assertTrue(module.getIdentities().get(4).getBases().get(1).getValue().equals("this:identity4"));
    }
}
