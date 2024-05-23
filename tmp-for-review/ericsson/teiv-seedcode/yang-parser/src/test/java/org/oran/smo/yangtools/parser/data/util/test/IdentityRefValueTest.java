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
package org.oran.smo.yangtools.parser.data.util.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.data.util.IdentityRefValue;

public class IdentityRefValueTest {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_identity_ref_value_type() {

        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").getIdentityNamespace().equals("ns1"));
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").getIdentityModuleName().equals("module1"));
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").getIdentityName().equals("identity1"));

        new IdentityRefValue("ns1", "module1", "identity1").toString();

        /*
         * For XML
         */
        final PrefixResolver prefixResolver = new PrefixResolver();
        prefixResolver.addMapping("ns1", "urn:namespace1");
        prefixResolver.addMapping("ns2", "urn:namespace2");

        assertTrue(new IdentityRefValue("ns1:identity1", prefixResolver, "urn:namespace99").getIdentityNamespace().equals(
                "urn:namespace1"));
        assertTrue(new IdentityRefValue("ns1:identity1", prefixResolver, "urn:namespace99")
                .getIdentityModuleName() == null);
        assertTrue(new IdentityRefValue("ns1:identity1", prefixResolver, "urn:namespace99").getIdentityName().equals(
                "identity1"));

        assertTrue(new IdentityRefValue("identity1", prefixResolver, "urn:namespace99").getIdentityNamespace().equals(
                "urn:namespace99"));
        assertTrue(new IdentityRefValue("identity1", prefixResolver, "urn:namespace99").getIdentityModuleName() == null);
        assertTrue(new IdentityRefValue("identity1", prefixResolver, "urn:namespace99").getIdentityName().equals(
                "identity1"));

        assertTrue(new IdentityRefValue("ns99:identity1", prefixResolver, "urn:namespace99")
                .getIdentityNamespace() == null);
        assertTrue(new IdentityRefValue("ns99:identity1", prefixResolver, "urn:namespace99")
                .getIdentityModuleName() == null);
        assertTrue(new IdentityRefValue("ns99:identity1", prefixResolver, "urn:namespace99").getIdentityName().equals(
                "identity1"));

        /*
         * For JSON
         */
        assertTrue(new IdentityRefValue("module1:identity1", "module99").getIdentityNamespace() == null);
        assertTrue(new IdentityRefValue("module1:identity1", "module99").getIdentityModuleName().equals("module1"));
        assertTrue(new IdentityRefValue("module1:identity1", "module99").getIdentityName().equals("identity1"));

        assertTrue(new IdentityRefValue("identity1", "module99").getIdentityNamespace() == null);
        assertTrue(new IdentityRefValue("identity1", "module99").getIdentityModuleName().equals("module99"));
        assertTrue(new IdentityRefValue("identity1", "module99").getIdentityName().equals("identity1"));

        /*
         * Equality
         */
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(null) == false);
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new Integer(1)) == false);

        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue("ns1", "module1",
                "identityXXX")) == false);
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue("ns1", "moduleXXX",
                "identity1")) == false);
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue("nsXXX", "module1",
                "identity1")) == false);

        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue("ns1", "module1",
                "identity1")) == true);
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue("ns1", (String) null,
                "identity1")) == true);
        assertTrue(new IdentityRefValue("ns1", "module1", "identity1").equals(new IdentityRefValue(null, "module1",
                "identity1")) == true);

        assertTrue(new IdentityRefValue(null, "module1", "identity1").equals(new IdentityRefValue("ns1", (String) null,
                "identity1")) == false);
    }
}
