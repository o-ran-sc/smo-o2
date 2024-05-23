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

import java.io.File;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNodeAnnotationValue;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class JsonTest extends YangTestCommon {

    @Test
    public void test_json_ok1() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_ok1.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);
        assertNoFindings();

        final YangDataDomDocumentRoot root = yangInstanceDataInput.getYangDataDomDocumentRoot();

        final YangDataDomNode cont1 = getChildDataDomNode(root, "cont1");
        assertTrue(cont1.getModuleName().equals("module1"));

        final YangDataDomNode leaf11 = getChildDataDomNode(cont1, "leaf11");
        assertTrue(leaf11.getModuleName().equals("module1"));
        assertTrue(leaf11.getValue().equals(new String("hello")));

        final YangDataDomNode leaf12 = getChildDataDomNode(cont1, "leaf12");
        assertTrue(leaf12.getModuleName().equals("module1"));
        assertTrue(leaf12.getValue().equals(new String("")));

        final YangDataDomNode leaf13 = getChildDataDomNode(cont1, "leaf13");
        assertTrue(leaf13.getValue() == null);

        final YangDataDomNode leaf14 = getChildDataDomNode(cont1, "leaf14");
        assertTrue(leaf14.getValue().equals(new Long(123)));

        final YangDataDomNode leaf15 = getChildDataDomNode(cont1, "leaf15");
        assertTrue(leaf15.getValue().equals(new Boolean(true)));

        final YangDataDomNode leaf21index0 = getChildDataDomNode(cont1, "leaflist21", 0);
        assertTrue(leaf21index0.getModuleName().equals("module1"));
        assertTrue(leaf21index0.getValue().equals(new String("one")));

        final YangDataDomNode leaf21index1 = getChildDataDomNode(cont1, "leaflist21", 1);
        assertTrue(leaf21index1.getValue().equals(new String("two")));

        final YangDataDomNode leaf22index0 = getChildDataDomNode(cont1, "leaflist22", 0);
        assertTrue(leaf22index0.getValue().equals(new String("")));

        final YangDataDomNode leaf22index1 = getChildDataDomNode(cont1, "leaflist22", 1);
        assertTrue(leaf22index1.getValue() == null);

        final YangDataDomNode leaf23index0 = getChildDataDomNode(cont1, "leaflist23", 0);
        assertTrue(leaf23index0.getValue() == null);

        final YangDataDomNode leaf23index1 = getChildDataDomNode(cont1, "leaflist23", 1);
        assertTrue(leaf23index1.getValue().equals(new Boolean(true)));

        final YangDataDomNode leaf24index0 = getChildDataDomNode(cont1, "leaflist24", 0);
        assertTrue(leaf24index0.getValue().equals(new Long(10)));

        final YangDataDomNode leaf24index1 = getChildDataDomNode(cont1, "leaflist24", 1);
        assertTrue(leaf24index1.getValue().equals(new Long(-999)));

        final YangDataDomNode leaf24index2 = getChildDataDomNode(cont1, "leaflist24", 2);
        assertTrue(leaf24index2.getValue().equals(new Double(1234567890.123456)));

        // =======================================

        final YangDataDomNode cont2 = getChildDataDomNode(root, "cont2");
        assertTrue(cont2.getModuleName().equals("module2"));

        final YangDataDomNode leaf31 = getChildDataDomNode(cont2, "leaf31");
        assertTrue(leaf31.getModuleName().equals("module2"));
        assertTrue(leaf31.getValue().equals(new String("world")));

        final YangDataDomNode leaf32 = getChildDataDomNode(cont2, "leaf32");
        assertTrue(leaf32.getModuleName().equals("module99"));
        assertTrue(leaf32.getValue().equals(new Boolean(false)));

        final YangDataDomNode cont3 = getChildDataDomNode(cont2, "cont3");
        assertTrue(cont3.getModuleName().equals("module2"));

        final YangDataDomNode leaf33 = getChildDataDomNode(cont3, "leaf33");
        assertTrue(leaf33.getValue() == null);

        final YangDataDomNode leaflist34index0 = getChildDataDomNode(cont3, "leaflist34", 0);
        assertTrue(leaflist34index0.getValue().equals(new String("one")));

        final YangDataDomNode leaflist34index1 = getChildDataDomNode(cont3, "leaflist34", 1);
        assertTrue(leaflist34index1.getValue().equals(new String("two")));

        final YangDataDomNode leaflist35index0 = getChildDataDomNode(cont3, "leaflist35", 0);
        assertTrue(leaflist35index0.getValue() == null);

        final YangDataDomNode leaflist35index1 = getChildDataDomNode(cont3, "leaflist35", 1);
        assertTrue(leaflist35index1.getValue().equals(new Boolean(true)));

        // =======================================

        final YangDataDomNode list5index0 = getChildDataDomNode(root, "list5", 0);
        assertTrue(list5index0.getModuleName().equals("module5"));

        final YangDataDomNode list5index0leaf51 = getChildDataDomNode(list5index0, "leaf51");
        assertTrue(list5index0leaf51.getValue().equals(new Long(1234)));

        final YangDataDomNode list5index0leaf52 = getChildDataDomNode(list5index0, "leaf52");
        assertTrue(list5index0leaf52.getValue() == null);

        final YangDataDomNode list5index0leaf53index0 = getChildDataDomNode(list5index0, "leaflist53", 0);
        assertTrue(list5index0leaf53index0.getValue().equals(new Boolean(false)));

        final YangDataDomNode list5index0leaf53index1 = getChildDataDomNode(list5index0, "leaflist53", 1);
        assertTrue(list5index0leaf53index1.getValue().equals(new Boolean(true)));

        final YangDataDomNode list5index1 = getChildDataDomNode(root, "list5", 1);
        assertTrue(list5index1.getModuleName().equals("module5"));

        final YangDataDomNode list5index1leaf51 = getChildDataDomNode(list5index1, "leaf51");
        assertTrue(list5index1leaf51.getValue().equals(new Long(1235)));

        final YangDataDomNode list5index1leaf52 = getChildDataDomNode(list5index1, "leaf52");
        assertTrue(list5index1leaf52.getValue().equals(new String("hello")));

        final YangDataDomNode list5index1leaf53index0 = getChildDataDomNode(list5index1, "leaflist53", 0);
        assertTrue(list5index1leaf53index0.getValue().equals(new Boolean(false)));

        final YangDataDomNode list5index1leaf53index1 = getChildDataDomNode(list5index1, "leaflist53", 1);
        assertTrue(list5index1leaf53index1.getValue().equals(new Boolean(false)));
    }

    @Test
    public void test_json_ok2() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_ok2.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);
        assertNoFindings();

        final YangDataDomDocumentRoot root = yangInstanceDataInput.getYangDataDomDocumentRoot();

        final YangDataDomNode cont6 = getChildDataDomNode(root, "cont6");
        assertTrue(cont6.getModuleName().equals("module6"));
        assertTrue(cont6.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue cont6anno1 = getDataDomNodeAnnotation(cont6, "anno1");
        assertTrue(cont6anno1.getModuleName().equals("anno-module1"));
        assertTrue(cont6anno1.getName().equals("anno1"));
        assertTrue(cont6anno1.getValue().equals(new String("hello")));

        final YangDataDomNodeAnnotationValue cont6anno2 = getDataDomNodeAnnotation(cont6, "anno2");
        assertTrue(cont6anno2.getModuleName().equals("anno-module1"));
        assertTrue(cont6anno2.getName().equals("anno2"));
        assertTrue(cont6anno2.getValue().equals(new Long(12345)));

        final YangDataDomNode leaf61 = getChildDataDomNode(cont6, "leaf61");
        assertTrue(leaf61.getValue().equals(new String("hello")));
        assertTrue(leaf61.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue leaf61anno61 = getDataDomNodeAnnotation(leaf61, "anno61");
        assertTrue(leaf61anno61.getModuleName().equals("anno-module6"));
        assertTrue(leaf61anno61.getName().equals("anno61"));
        assertTrue(leaf61anno61.getValue().equals(new Boolean(true)));

        final YangDataDomNodeAnnotationValue leaf61anno62 = getDataDomNodeAnnotation(leaf61, "anno62");
        assertTrue(leaf61anno62.getModuleName().equals("anno-module6"));
        assertTrue(leaf61anno62.getName().equals("anno62"));
        assertTrue(leaf61anno62.getValue() == null);

        final YangDataDomNode leaf62 = getChildDataDomNode(cont6, "leaf62");
        assertTrue(leaf62.getValue().equals(new String("")));
        assertTrue(leaf62.getAnnotations().size() == 0);

        final YangDataDomNode leaflist81Index0 = getChildDataDomNode(cont6, "leaflist81", 0);
        assertTrue(leaflist81Index0.getValue().equals(new String("one")));
        assertTrue(leaflist81Index0.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue leaflist81Index0anno81 = getDataDomNodeAnnotation(leaflist81Index0, "anno81");
        assertTrue(leaflist81Index0anno81.getModuleName().equals("anno-module8"));
        assertTrue(leaflist81Index0anno81.getName().equals("anno81"));
        assertTrue(leaflist81Index0anno81.getValue().equals(new Boolean(false)));

        final YangDataDomNodeAnnotationValue leaflist81Index0anno82 = getDataDomNodeAnnotation(leaflist81Index0, "anno82");
        assertTrue(leaflist81Index0anno82.getModuleName().equals("anno-module8"));
        assertTrue(leaflist81Index0anno82.getName().equals("anno82"));
        assertTrue(leaflist81Index0anno82.getValue().equals(new Long(-99987)));

        final YangDataDomNode leaflist81Index1 = getChildDataDomNode(cont6, "leaflist81", 1);
        assertTrue(leaflist81Index1.getValue().equals(new String("two")));
        assertTrue(leaflist81Index1.getAnnotations().size() == 0);

        final YangDataDomNode leaflist81Index2 = getChildDataDomNode(cont6, "leaflist81", 2);
        assertTrue(leaflist81Index2.getValue().equals(new String("three")));
        assertTrue(leaflist81Index2.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue leaflist81Index2anno81 = getDataDomNodeAnnotation(leaflist81Index2, "anno81");
        assertTrue(leaflist81Index2anno81.getModuleName().equals("anno-module8"));
        assertTrue(leaflist81Index2anno81.getName().equals("anno81"));
        assertTrue(leaflist81Index2anno81.getValue().equals(new Boolean(true)));

        final YangDataDomNodeAnnotationValue leaflist81Index2anno82 = getDataDomNodeAnnotation(leaflist81Index2, "anno82");
        assertTrue(leaflist81Index2anno82.getModuleName().equals("anno-module8"));
        assertTrue(leaflist81Index2anno82.getName().equals("anno82"));
        assertTrue(leaflist81Index2anno82.getValue().equals(new Double(12345678.12345678)));

        final YangDataDomNode list7index0 = getChildDataDomNode(root, "list7", 0);
        assertTrue(list7index0.getModuleName().equals("module7"));
        assertTrue(list7index0.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue list7index0anno71 = getDataDomNodeAnnotation(list7index0, "anno71");
        assertTrue(list7index0anno71.getModuleName().equals("anno-module1"));
        assertTrue(list7index0anno71.getName().equals("anno71"));
        assertTrue(list7index0anno71.getValue().equals(new String("hello")));

        final YangDataDomNodeAnnotationValue list7index0anno72 = getDataDomNodeAnnotation(list7index0, "anno72");
        assertTrue(list7index0anno72.getModuleName().equals("anno-module2"));
        assertTrue(list7index0anno72.getName().equals("anno72"));
        assertTrue(list7index0anno72.getValue() == null);
    }

    @Test
    public void test_json_error1_wrong_json_type_for_annotations_should_be_object() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error1.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString());
    }

    @Test
    public void test_json_error2_wrong_json_types_in_array() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error2.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString());
    }

    @Test
    public void test_json_error3_wrong_json_type_for_annotation_should_be_array() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error3.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString());
    }

    @Test
    public void test_json_error4_wrong_json_type_for_annotation_should_be_array_of_objects() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error4.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString());
    }

    @Test
    public void test_json_error5_anno_array_too_large() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error5.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P069_UNEXPECTED_JSON_VALUE.toString());
    }

    @Test
    public void test_json_error6_unknown_leaf() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error6.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P069_UNEXPECTED_JSON_VALUE.toString());
    }

    @Test
    public void test_json_error7_unknown_leaf() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error7.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P069_UNEXPECTED_JSON_VALUE.toString());
    }

    @Test
    public void test_json_error8_anno_member_has_no_module() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error8.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_json_error9_missing_module_name() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_error9.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        yangInstanceDataInput.parse(context);

        assertContextHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_json_ok3() {

        final File jsonFile = new File("src/test/resources/data-dom/json-test/json_ok3.json");
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordModuleMapping("module1", "ns1");
        namespaceResolver.recordModuleMapping("module2", "ns2");
        namespaceResolver.recordModuleMapping("module3", "ns3");
        namespaceResolver.recordModuleMapping("module4", "ns4");
        namespaceResolver.recordNamespaceMapping("ns1", "module1");
        namespaceResolver.recordNamespaceMapping("ns2", "module2");
        namespaceResolver.recordNamespaceMapping("ns3", "module3");
        namespaceResolver.recordNamespaceMapping("ns4", "module4");

        yangInstanceDataInput.parse(context);
        yangInstanceDataInput.getYangDataDomDocumentRoot().resolveModuleOrNamespace(namespaceResolver);

        assertNoFindings();

        final YangDataDomDocumentRoot root = yangInstanceDataInput.getYangDataDomDocumentRoot();

        final YangDataDomNode cont1 = getChildDataDomNode(root, "cont1");
        assertTrue(cont1.getModuleName().equals("module1"));
        assertTrue(cont1.getNamespace().equals("ns1"));
        assertTrue(cont1.getAnnotations().size() == 2);

        final YangDataDomNodeAnnotationValue cont1anno1 = getDataDomNodeAnnotation(cont1, "anno1");
        assertTrue(cont1anno1.getModuleName().equals("module3"));
        assertTrue(cont1anno1.getNamespace().equals("ns3"));
        assertTrue(cont1anno1.getName().equals("anno1"));
        assertTrue(cont1anno1.getValue().equals(new String("hello")));

        final YangDataDomNodeAnnotationValue cont1anno2 = getDataDomNodeAnnotation(cont1, "anno2");
        assertTrue(cont1anno2.getModuleName().equals("module4"));
        assertTrue(cont1anno2.getNamespace().equals("ns4"));
        assertTrue(cont1anno2.getName().equals("anno2"));
        assertTrue(cont1anno2.getValue().equals(new Long(12345)));

        final YangDataDomNode leaf11 = getChildDataDomNode(cont1, "leaf11");
        assertTrue(leaf11.getModuleName().equals("module1"));
        assertTrue(leaf11.getNamespace().equals("ns1"));
        assertTrue(leaf11.getName().equals("leaf11"));
        assertTrue(leaf11.getValue().equals(new String("hello")));

        final YangDataDomNode leaf12 = getChildDataDomNode(cont1, "leaf12");
        assertTrue(leaf12.getModuleName().equals("module2"));
        assertTrue(leaf12.getNamespace().equals("ns2"));
        assertTrue(leaf12.getName().equals("leaf12"));
        assertTrue(leaf12.getValue().equals(new String("")));

        final YangDataDomNode leaflist5Index0 = getChildDataDomNode(cont1, "leaflist5", 0);
        assertTrue(leaflist5Index0.getModuleName().equals("module1"));
        assertTrue(leaflist5Index0.getNamespace().equals("ns1"));
        assertTrue(leaflist5Index0.getName().equals("leaflist5"));
        assertTrue(leaflist5Index0.getValue().equals(new String("one")));
    }

}
