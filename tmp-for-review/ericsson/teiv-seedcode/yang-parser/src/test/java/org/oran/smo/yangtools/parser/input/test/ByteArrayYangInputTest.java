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
package org.oran.smo.yangtools.parser.input.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.input.ByteArrayYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;

public class ByteArrayYangInputTest {

    private YangDeviceModel yangDeviceModel;
    private ModifyableFindingSeverityCalculator severityCalculator;
    private FindingsManager findingsManager;
    private ParserExecutionContext context;

    @Test
    public void test_byte_array_input() {

        final String moduleContent = getModuleContent();
        final byte[] bytes = moduleContent.getBytes();
        final ByteArrayYangInput byteArrayYangInput = new ByteArrayYangInput(bytes, "test");

        // First run

        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);
        context = new ParserExecutionContext(findingsManager);

        List<YangModel> yangModelInputs = new ArrayList<>();
        yangModelInputs.add(new YangModel(byteArrayYangInput, ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangModelInputs);

        YModule module = yangModelInputs.get(0).getYangModelRoot().getModule();
        assertTrue(module.getModuleName().equals("byte-array-test-module"));

        YContainer cont1 = module.getContainers().get(0);
        assertTrue(cont1.getContainerName().equals("cont1"));

        YLeaf leaf1 = cont1.getLeafs().get(0);
        assertTrue(leaf1.getLeafName().equals("leaf1"));

        // Set up the whole lot again for second run, except for the ByteArrayYangInput, to test repeated parsing.

        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);
        context = new ParserExecutionContext(findingsManager);

        yangModelInputs = new ArrayList<>();
        yangModelInputs.add(new YangModel(byteArrayYangInput, ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangModelInputs);

        module = yangModelInputs.get(0).getYangModelRoot().getModule();
        assertTrue(module.getModuleName().equals("byte-array-test-module"));

        cont1 = module.getContainers().get(0);
        assertTrue(cont1.getContainerName().equals("cont1"));

        cont1.getLeafs().get(0);
        assertTrue(leaf1.getLeafName().equals("leaf1"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_byte_array_input_equals() {

        final byte[] bytes1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final byte[] bytes2 = new byte[] { 9, 8, 7, 6 };

        final ByteArrayYangInput input1 = new ByteArrayYangInput(bytes1, "test");
        final ByteArrayYangInput input2 = new ByteArrayYangInput(bytes1, "test");
        final ByteArrayYangInput input3 = new ByteArrayYangInput(bytes1, "other");
        final ByteArrayYangInput input4 = new ByteArrayYangInput(bytes2, "test");

        assertTrue(input1.equals(input2) == true);
        assertTrue(input1.equals(null) == false);
        assertTrue(input1.equals("") == false);
        assertTrue(input1.equals(input3) == false);
        assertTrue(input1.equals(input4) == false);

        assertTrue(input1.getName().equals("test"));
        assertTrue(input1.getFile() == null);
    }

    private static String getModuleContent() {

        final StringBuilder sb = new StringBuilder(1000);

        sb.append("module byte-array-test-module {\n");
        sb.append("\n");
        sb.append("    namespace \"test:byte-array-test-module\";\n");
        sb.append("    prefix \"this\";\n");
        sb.append("\n");
        sb.append("    revision \"2021-04-06\" {\n");
        sb.append("        description \"initial revision\";\n");
        sb.append("    }\n");
        sb.append("\n");
        sb.append("container cont1 {\n");
        sb.append("\n");
        sb.append("		leaf leaf1 {\n");
        sb.append("			type int8;\n");
        sb.append("		}\n");
        sb.append("	}n");
        sb.append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
