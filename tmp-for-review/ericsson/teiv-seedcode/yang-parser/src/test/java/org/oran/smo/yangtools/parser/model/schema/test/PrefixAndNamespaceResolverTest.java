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

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.ModulePrefixResolver;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class PrefixAndNamespaceResolverTest extends YangTestCommon {

    @Test
    public void test_all_modules() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/prefix-and-namespace-test/module1.yang",
                "src/test/resources/model-schema/prefix-and-namespace-test/module2.yang",
                "src/test/resources/model-schema/prefix-and-namespace-test/module3.yang",
                "src/test/resources/model-schema/prefix-and-namespace-test/submodule4.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final ModuleAndNamespaceResolver namespaceResolver = yangDeviceModel.getTopLevelSchema()
                .getModuleNamespaceResolver();
        assertTrue(namespaceResolver.getNamespaceForModule("module1").equals("test:module1"));
        assertTrue(namespaceResolver.getNamespaceForModule("module2").equals("test:module2"));
        assertTrue(namespaceResolver.getNamespaceForModule("module3").equals("test:module3"));
        assertTrue(namespaceResolver.getNamespaceForModule("submodule4").equals("test:module1"));
        assertTrue(namespaceResolver.getNamespaceForModule("unknownmodule") == null);

        assertTrue(namespaceResolver.getModuleForNamespace("test:module1").equals("module1"));
        assertTrue(namespaceResolver.getModuleForNamespace("test:module2").equals("module2"));
        assertTrue(namespaceResolver.getModuleForNamespace("test:module3").equals("module3"));
        assertTrue(namespaceResolver.getModuleForNamespace("unknown namespace") == null);

        final ModulePrefixResolver prefixResolverModule1 = getModule("module1").getPrefixResolver();

        assertTrue(prefixResolverModule1.getDefaultModuleIdentity().getModuleName().equals("module1"));
        assertTrue(prefixResolverModule1.getModuleForPrefix("this").getModuleName().equals("module1"));
        assertTrue(prefixResolverModule1.getModuleForPrefix("mod2").getModuleName().equals("module2"));
        assertTrue(prefixResolverModule1.getModuleForPrefix("mod3").getModuleName().equals("module3"));
        assertTrue(prefixResolverModule1.getDefaultNamespaceUri().equals("test:module1"));
        assertTrue(prefixResolverModule1.resolveNamespaceUri("this").equals("test:module1"));
        assertTrue(prefixResolverModule1.resolveNamespaceUri("mod2").equals("test:module2"));
        assertTrue(prefixResolverModule1.resolveNamespaceUri("mod3").equals("test:module3"));

        final ModulePrefixResolver prefixResolverModule2 = getModule("module2").getPrefixResolver();

        assertTrue(prefixResolverModule2.getDefaultModuleIdentity().getModuleName().equals("module2"));
        assertTrue(prefixResolverModule2.getModuleForPrefix("this").getModuleName().equals("module2"));
        assertTrue(prefixResolverModule2.getModuleForPrefix("mod1").getModuleName().equals("module1"));
        assertTrue(prefixResolverModule2.getModuleForPrefix("mod3").getModuleName().equals("module3"));
        assertTrue(prefixResolverModule2.getDefaultNamespaceUri().equals("test:module2"));
        assertTrue(prefixResolverModule2.resolveNamespaceUri("this").equals("test:module2"));
        assertTrue(prefixResolverModule2.resolveNamespaceUri("mod1").equals("test:module1"));
        assertTrue(prefixResolverModule2.resolveNamespaceUri("mod3").equals("test:module3"));

        final ModulePrefixResolver prefixResolverModule3 = getModule("module3").getPrefixResolver();

        assertTrue(prefixResolverModule3.getDefaultModuleIdentity().getModuleName().equals("module3"));
        assertTrue(prefixResolverModule3.getModuleForPrefix("this").getModuleName().equals("module3"));
        assertTrue(prefixResolverModule3.getModuleForPrefix("mod1").getModuleName().equals("module1"));
        assertTrue(prefixResolverModule3.getModuleForPrefix("mod2").getModuleName().equals("module2"));
        assertTrue(prefixResolverModule3.getDefaultNamespaceUri().equals("test:module3"));
        assertTrue(prefixResolverModule3.resolveNamespaceUri("this").equals("test:module3"));
        assertTrue(prefixResolverModule3.resolveNamespaceUri("mod1").equals("test:module1"));
        assertTrue(prefixResolverModule3.resolveNamespaceUri("mod2").equals("test:module2"));

        final ModulePrefixResolver prefixResolverSubmodule4 = getSubModule("submodule4").getPrefixResolver();

        assertTrue(prefixResolverSubmodule4.getDefaultModuleIdentity().getModuleName().equals("module1"));
        assertTrue(prefixResolverSubmodule4.getModuleForPrefix("mod1").getModuleName().equals("module1"));
        assertTrue(prefixResolverSubmodule4.getModuleForPrefix("mod2").getModuleName().equals("module2"));
        assertTrue(prefixResolverSubmodule4.getModuleForPrefix("mod3").getModuleName().equals("module3"));
        assertTrue(prefixResolverSubmodule4.getDefaultNamespaceUri().equals("test:module1"));
        assertTrue(prefixResolverSubmodule4.resolveNamespaceUri("mod1").equals("test:module1"));
        assertTrue(prefixResolverSubmodule4.resolveNamespaceUri("mod2").equals("test:module2"));
        assertTrue(prefixResolverSubmodule4.resolveNamespaceUri("mod3").equals("test:module3"));
    }

}
