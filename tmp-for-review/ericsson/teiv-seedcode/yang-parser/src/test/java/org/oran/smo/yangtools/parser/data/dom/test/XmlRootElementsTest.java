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
package org.oran.smo.yangtools.parser.data.dom.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class XmlRootElementsTest extends YangTestCommon {

    private static final String DIR = "src/test/resources/data-dom/xml-test/";

    @Test
    public void testRootElement_Config() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-config.xml"));

        assertNoFindings();
        checkDataParsedCorrectly();
    }

    @Test
    public void testRootElement_InstanceDataSet() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-instance-data-set.xml"));

        assertNoFindings();
        checkDataParsedCorrectly();
    }

    @Test
    public void testRootElement_data() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-data.xml"));

        assertNoFindings();
        checkDataParsedCorrectly();
    }

    @Test
    public void testRootElement_assume_module_root_element() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-assume-module-root-element.xml"));

        assertHasFindingOfType(ParserFindingType.P071_INCORRECT_ROOT_ELEMENT_OF_DATA_FILE.toString());
        checkDataParsedCorrectly();
    }

    @Test
    public void testRootElement_InstanceDataSet_Wrong() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-instance-data-set-wrong.xml"));

        assertHasFindingOfType(ParserFindingType.P079_EMPTY_DATA_FILE.toString());
    }

    @Test
    public void testRootElement_RpcReply() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-rpc-reply.xml"));

        assertHasNotFindingOfType(ParserFindingType.P071_INCORRECT_ROOT_ELEMENT_OF_DATA_FILE.toString());
        checkDataParsedCorrectly();
    }

    @Test
    public void testRootElement_RpcReply_Wrong() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-rpc-reply-wrong-no-data.xml"));

        assertHasFindingOfType(ParserFindingType.P079_EMPTY_DATA_FILE.toString());
    }

    @Test
    public void testRootElement_no_namespace() {

        severityCalculator.suppressFinding(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        parseAbsoluteYangData(Arrays.asList(DIR + "root-data-no-namespace.xml"));

        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    private void checkDataParsedCorrectly() {

        final YangDataDomDocumentRoot root = yangDeviceModel.getYangInstanceDataInputs().get(0)
                .getYangDataDomDocumentRoot();

        final YangDataDomNode cont1 = getChildDataDomNode(root, "cont1");
        assertTrue(cont1.getNamespace().equals("test:module"));

        final YangDataDomNode leaf1 = getChildDataDomNode(cont1, "leaf1");
        assertTrue(leaf1.getNamespace().equals("test:module"));
        assertTrue(leaf1.getValue().equals(new String("42")));
        assertTrue(leaf1.getSourceDataType() == SourceDataType.XML);
    }
}
