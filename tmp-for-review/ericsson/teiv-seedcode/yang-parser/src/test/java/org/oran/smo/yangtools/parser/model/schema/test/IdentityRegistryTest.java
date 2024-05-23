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
package org.oran.smo.yangtools.parser.model.schema.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.schema.IdentityRegistry;
import org.oran.smo.yangtools.parser.model.util.YangIdentity;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class IdentityRegistryTest extends YangTestCommon {

    @Test
    public void test_all_modules() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/identity-registry-test/module1.yang",
                "src/test/resources/model-schema/identity-registry-test/module2.yang",
                "src/test/resources/model-schema/identity-registry-test/module3.yang",
                "src/test/resources/model-schema/identity-registry-test/submodule4.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final IdentityRegistry identityRegistry = yangDeviceModel.getTopLevelSchema().getIdentityRegistry();

        // ---- module 1 ---- bases ------

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity11"))
                .size() == 0);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity12"))
                .size() == 0);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity13"))
                .size() == 0);

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity14"))
                .size() == 3);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity14")).contains(
                new YangIdentity("test:module1", "module1", "identity11")));
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity14")).contains(
                new YangIdentity("test:module1", "module1", "identity12")));
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity14")).contains(
                new YangIdentity("test:module1", "module1", "identity13")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity15"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity15")).contains(
                new YangIdentity("test:module1", "module1", "identity14")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity61"))
                .size() == 2);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity61")).contains(
                new YangIdentity("test:module1", "module1", "identity14")));
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity61")).contains(
                new YangIdentity("test:module1", "module1", "identity42")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity99"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity99")).contains(
                new YangIdentity("test:module2", "module2", "identity99")));

        // ---- module 2 ---- bases ------

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity21"))
                .size() == 0);

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity22"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity22")).contains(
                new YangIdentity("test:module2", "module2", "identity21")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity23"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity23")).contains(
                new YangIdentity("test:module1", "module1", "identity11")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module2", "module2", "identity99"))
                .size() == 0);

        // ---- module 3 ---- bases ------

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity31"))
                .size() == 0);

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity32"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity32")).contains(
                new YangIdentity("test:module3", "module3", "identity31")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity33"))
                .size() == 1);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity33")).contains(
                new YangIdentity("test:module2", "module2", "identity21")));

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module3", "module3", "identity99"))
                .size() == 0);

        // ---- submodule 4 ---- bases ------

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity41"))
                .size() == 0);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity42"))
                .size() == 0);

        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity43"))
                .size() == 2);
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity43")).contains(
                new YangIdentity("test:module1", "module1", "identity41")));
        assertTrue(identityRegistry.getBasesForIdentity(new YangIdentity("test:module1", "module1", "identity43")).contains(
                new YangIdentity("test:module1", "module1", "identity42")));

        // ---- module 1 ---- derivates ------

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity11"))
                .size() == 2);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity11"))
                .contains(new YangIdentity("test:module1", "module1", "identity14")));
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity11"))
                .contains(new YangIdentity("test:module2", "module2", "identity23")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity12"))
                .size() == 1);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity12"))
                .contains(new YangIdentity("test:module1", "module1", "identity14")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity13"))
                .size() == 1);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity13"))
                .contains(new YangIdentity("test:module1", "module1", "identity14")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity14"))
                .size() == 2);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity14"))
                .contains(new YangIdentity("test:module1", "module1", "identity15")));
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity14"))
                .contains(new YangIdentity("test:module1", "module1", "identity61")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity15"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity61"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity99"))
                .size() == 0);

        // ---- module 2 ---- derivates ------

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity21"))
                .size() == 2);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity21"))
                .contains(new YangIdentity("test:module2", "module2", "identity22")));
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity21"))
                .contains(new YangIdentity("test:module3", "module3", "identity33")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity22"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity23"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity99"))
                .size() == 1);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module2", "module2", "identity99"))
                .contains(new YangIdentity("test:module1", "module1", "identity99")));

        // ---- module 3 ---- derivates ------

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module3", "module3", "identity31"))
                .size() == 1);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module3", "module3", "identity31"))
                .contains(new YangIdentity("test:module3", "module3", "identity32")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module3", "module3", "identity32"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module3", "module3", "identity33"))
                .size() == 0);

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module3", "module3", "identity99"))
                .size() == 0);

        // ---- submodule 4 ---- derivates ------

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity41"))
                .size() == 1);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity41"))
                .contains(new YangIdentity("test:module1", "module1", "identity43")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity42"))
                .size() == 2);
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity42"))
                .contains(new YangIdentity("test:module1", "module1", "identity43")));
        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity42"))
                .contains(new YangIdentity("test:module1", "module1", "identity61")));

        assertTrue(identityRegistry.getDerivatesOfIdentity(new YangIdentity("test:module1", "module1", "identity43"))
                .size() == 0);

        // ---------------- check the derived trees ---------------

        Set<YangIdentity> identityAndDerivedIdentities = identityRegistry.getIdentityAndDerivedIdentitiesRecursively(
                new YangIdentity("test:module1", "module1", "identity11"));

        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity11")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity14")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity15")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity61")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module2", "module2", "identity23")));
        assertTrue(identityAndDerivedIdentities.size() == 5);

        identityAndDerivedIdentities = identityRegistry.getIdentityAndDerivedIdentitiesRecursively(new YangIdentity(
                "test:module1", "module1", "identity12"));

        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity12")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity14")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity15")));
        assertTrue(identityAndDerivedIdentities.contains(new YangIdentity("test:module1", "module1", "identity61")));
        assertTrue(identityAndDerivedIdentities.size() == 4);

        identityAndDerivedIdentities = identityRegistry.getIdentityAndDerivedIdentitiesRecursively(new YangIdentity(
                "unknown-namespace", "unknown-module", "unknown-identity"));
        assertTrue(identityAndDerivedIdentities.size() == 0);
    }

}
