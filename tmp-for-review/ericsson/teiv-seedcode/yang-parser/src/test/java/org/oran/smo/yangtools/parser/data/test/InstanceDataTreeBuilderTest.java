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
package org.oran.smo.yangtools.parser.data.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.instance.ContainerInstance;
import org.oran.smo.yangtools.parser.data.instance.LeafInstance;
import org.oran.smo.yangtools.parser.data.instance.LeafListInstance;
import org.oran.smo.yangtools.parser.data.instance.ListInstance;
import org.oran.smo.yangtools.parser.data.instance.RootInstance;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class InstanceDataTreeBuilderTest extends YangTestCommon {

    @Test
    public void test_single_data_file() {

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/data/instance-data-tree-builder-test/module1.yang"));
        parseAbsoluteYangData(Arrays.asList(
                "src/test/resources/data/instance-data-tree-builder-test/data-in-single-file.xml"));

        assertNoFindings();

        checkDataParsedCorrectly();
    }

    private void checkDataParsedCorrectly() {
        final YModule module = getModule("module1");
        assertTrue(module != null);

        final RootInstance rootInstance = yangDeviceModel.getCombinedInstanceDataRoot();
        assertTrue(rootInstance != null);

        final ContainerInstance cont1Instance = getContainerInstance(rootInstance, "test:module", "cont1");
        assertTrue(cont1Instance != null);

        final Map<String, String> map1 = new HashMap<>();
        map1.put("leaf111", "key-value-1");

        final ListInstance list11InstanceKeyValue1 = getListInstanceData(cont1Instance, "test:module", "list11", map1);
        assertTrue(list11InstanceKeyValue1 != null);

        final LeafInstance leaf111Instance = getLeafInstance(list11InstanceKeyValue1, "test:module", "leaf111");
        assertTrue(leaf111Instance != null);
        assertTrue(leaf111Instance.getValue().equals("key-value-1"));

        final LeafInstance leaf112Instance = getLeafInstance(list11InstanceKeyValue1, "test:module", "leaf112");
        assertTrue(leaf112Instance != null);
        assertTrue(leaf112Instance.getValue().equals("some string"));

        final List<LeafListInstance> leaf113Instance = getLeafListInstances(list11InstanceKeyValue1, "test:module",
                "leaflist113");
        assertTrue(leaf113Instance != null);
        assertTrue(leaf113Instance.size() == 3);
        assertTrue(leaf113Instance.get(0).getValue().equals("20"));
        assertTrue(leaf113Instance.get(1).getValue().equals("30"));
        assertTrue(leaf113Instance.get(2).getValue().equals("40"));

        LeafInstance leaf12Instance = getLeafInstance(cont1Instance, "test:module", "leaf12");
        assertTrue(leaf12Instance != null);
        assertTrue(leaf12Instance.getValue().equals("42"));
    }
}
