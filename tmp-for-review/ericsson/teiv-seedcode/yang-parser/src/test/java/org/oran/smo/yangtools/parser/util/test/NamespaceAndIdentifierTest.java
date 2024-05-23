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
package org.oran.smo.yangtools.parser.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

public class NamespaceAndIdentifierTest {

    @Test
    public void test_all_ok() {

        final NamespaceModuleIdentifier nsai1 = new NamespaceModuleIdentifier("namespace", "module", "identifier");

        assertTrue(nsai1.getNamespace().equals("namespace"));
        assertTrue(nsai1.getModuleName().equals("module"));
        assertTrue(nsai1.getIdentifier().equals("identifier"));
        assertTrue(nsai1.hashCode() == "identifier".hashCode());
        assertTrue(nsai1.toString().equals("namespace/module/identifier"));
        assertTrue(nsai1.equals(new NamespaceModuleIdentifier("namespace", "module", "identifier")));
        assertFalse(nsai1.equals(new NamespaceModuleIdentifier("namespace", "module", "identifier2")));
        assertFalse(nsai1.equals(new NamespaceModuleIdentifier("namespace2", "module2", "identifier")));
        assertFalse(nsai1.equals(""));
        assertFalse(nsai1.equals(null));

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordModuleMapping("module", "namespace");
        namespaceResolver.recordNamespaceMapping("namespace", "module");

        final NamespaceModuleIdentifier nsai2 = new NamespaceModuleIdentifier("namespace", null, "identifier");
        assertTrue(nsai2.getNamespace().equals("namespace"));
        assertTrue(nsai2.getModuleName() == null);
        assertTrue(nsai2.getIdentifier().equals("identifier"));
        assertTrue(nsai2.equals(new NamespaceModuleIdentifier("namespace", "module", "identifier")));
        assertFalse(nsai2.equals(new NamespaceModuleIdentifier(null, "module", "identifier")));

        nsai2.resolveModuleOrNamespace(namespaceResolver);
        assertTrue(nsai2.getModuleName().equals("module"));

        final NamespaceModuleIdentifier nsai3 = new NamespaceModuleIdentifier(null, "module", "identifier");
        assertTrue(nsai3.getNamespace() == null);
        assertTrue(nsai3.getModuleName().equals("module"));
        assertTrue(nsai3.getIdentifier().equals("identifier"));
        assertTrue(nsai3.equals(new NamespaceModuleIdentifier("namespace", "module", "identifier")));
        assertFalse(nsai3.equals(new NamespaceModuleIdentifier("namespace", null, "identifier")));

        nsai3.resolveModuleOrNamespace(namespaceResolver);
        assertTrue(nsai3.getNamespace().equals("namespace"));
    }
}
