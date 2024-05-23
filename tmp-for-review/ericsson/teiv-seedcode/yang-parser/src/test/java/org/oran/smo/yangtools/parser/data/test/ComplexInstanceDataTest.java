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

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.instance.ContainerInstance;
import org.oran.smo.yangtools.parser.data.instance.DataTreeBuilderPredicate;
import org.oran.smo.yangtools.parser.data.instance.LeafInstance;
import org.oran.smo.yangtools.parser.data.instance.LeafListInstance;
import org.oran.smo.yangtools.parser.data.instance.ListInstance;
import org.oran.smo.yangtools.parser.data.instance.RootInstance;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.YangInput;
import org.oran.smo.yangtools.parser.input.YangInputResolver;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class ComplexInstanceDataTest extends YangTestCommon {

    private static final String MODULE1 = "src/test/resources/data/instance-data/module1.yang";
    private static final String MODULE2 = "src/test/resources/data/instance-data/module2.yang";

    private static final String DATA1_XML = "src/test/resources/data/instance-data/data1.xml";
    private static final String DATA2_XML = "src/test/resources/data/instance-data/data2.xml";
    private static final String DATA1_JSON = "src/test/resources/data/instance-data/data1.json";
    private static final String DATA2_JSON = "src/test/resources/data/instance-data/data2.json";

    private static final String DATA3 = "src/test/resources/data/instance-data/data3.xml";
    private static final String DATA4 = "src/test/resources/data/instance-data/data4.xml";

    private static final String DATA5 = "src/test/resources/data/instance-data/data5.xml";
    private static final String DATA6 = "src/test/resources/data/instance-data/data6.xml";

    private static final String ERROR_DATA7 = "src/test/resources/data/instance-data/error-data7.xml";
    private static final String ERROR_DATA8 = "src/test/resources/data/instance-data/error-data8.xml";
    private static final String ERROR_DATA9 = "src/test/resources/data/instance-data/error-data9.xml";
    private static final String ERROR_DATA10 = "src/test/resources/data/instance-data/error-data10.json";

    private static final String NS1 = "test:module1";
    private static final String NS2 = "test:module2";

    @Test
    public void test_module1_data1_data2_xml() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(DATA1_XML, DATA2_XML));

        assertNoFindings();

        final RootInstance combinedInstanceDataRoot = yangDeviceModel.getCombinedInstanceDataRoot();

        // -------------- Simple stuff ----------------

        final ContainerInstance cont1data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont1");
        assertTrue(cont1data != null);
        cont1data.getPath();

        assertTrue(cont1data.getDataDomNode().getNamespace().equals("test:module1"));
        assertTrue(cont1data.getDataDomNode().getModuleName().equals("module1"));

        final LeafInstance leaf11data = getLeafInstance(cont1data, NS1, "leaf11");
        assertTrue(leaf11data != null);
        assertTrue(leaf11data.getValue().equals("42"));

        final LeafInstance leaf12data = getLeafInstance(cont1data, NS1, "leaf12");
        assertTrue(leaf12data != null);
        assertTrue(leaf12data.getValue().equals("58"));

        final LeafInstance leaf13data = getLeafInstance(cont1data, NS1, "leaf13");
        assertTrue(leaf13data != null);
        assertTrue(leaf13data.getValue().equals("Hello!"));

        final LeafInstance leaf14data = getLeafInstance(cont1data, NS1, "leaf14");
        assertTrue(leaf14data != null);
        assertTrue(leaf14data.getValue().equals(""));

        // -------------- A few NPs and defaults ----------------

        final ListInstance list2data4 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "4"));
        assertTrue(list2data4 != null);

        LeafInstance leaf21data = getLeafInstance(list2data4, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("4"));

        ContainerInstance cont22data = getContainerInstance(list2data4, NS1, "cont22");
        assertTrue(cont22data != null);

        LeafInstance leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("One"));

        LeafInstance leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data != null);
        assertTrue(leaf24data.getValue().equals("Two"));

        LeafInstance leaf25data = getLeafInstance(list2data4, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("Three"));

        List<LeafListInstance> leaflist26data = getLeafListInstances(list2data4, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 1);
        assertTrue(leaflist26data.get(0).getValue().equals("Six"));

        // ... ... ... ...

        final ListInstance list2data5 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "5"));
        assertTrue(list2data5 != null);

        leaf21data = getLeafInstance(list2data5, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("5"));

        cont22data = getContainerInstance(list2data5, NS1, "cont22");
        assertTrue(cont22data != null);

        leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("One"));

        leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data == null);

        leaf25data = getLeafInstance(list2data5, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListInstances(list2data5, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // ... ... ... ...

        final ListInstance list2data6 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "6"));
        assertTrue(list2data6 != null);

        leaf21data = getLeafInstance(list2data6, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("6"));

        cont22data = getContainerInstance(list2data6, NS1, "cont22");
        assertTrue(cont22data == null);

        leaf25data = getLeafInstance(list2data6, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListInstances(list2data6, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // --------------------- CDATA ---------------------------

        final ContainerInstance cont8data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont8");

        final LeafInstance leaf81data = getLeafInstance(cont8data, NS1, "leaf81");
        assertTrue(leaf81data != null);
        assertTrue(leaf81data.getValue().equals("should ignore whitespaces"));

        final LeafInstance leaf82data = getLeafInstance(cont8data, NS1, "leaf82");
        assertTrue(leaf82data != null);
        assertTrue(leaf82data.getValue().equals("should also ignore whitespaces"));

        final LeafInstance leaf83data = getLeafInstance(cont8data, NS1, "leaf83");
        assertTrue(leaf83data != null);
        assertTrue(leaf83data.getValue().equals("  should be 2 whitespaces either side  "));

        final LeafInstance leaf84data = getLeafInstance(cont8data, NS1, "leaf84");
        assertTrue(leaf84data != null);
        assertTrue(leaf84data.getValue().equals("Before  should be 2 whitespaces either side  After"));

        final LeafInstance leaf85data = getLeafInstance(cont8data, NS1, "leaf85");
        assertTrue(leaf85data != null);
        assertTrue(leaf85data.getValue().equals(""));

        final LeafInstance leaf86data = getLeafInstance(cont8data, NS1, "leaf86");
        assertTrue(leaf86data != null);
        assertTrue(leaf86data.getValue().equals("blurb more blurb"));
    }

    @Test
    public void test_module1_data1_data2_json() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(DATA1_JSON, DATA2_JSON));

        assertNoFindings();

        final RootInstance combinedInstanceDataRoot = yangDeviceModel.getCombinedInstanceDataRoot();

        // -------------- Simple stuff ----------------

        final ContainerInstance cont1data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont1");
        assertTrue(cont1data != null);
        cont1data.getPath();

        assertTrue(cont1data.getDataDomNode().getNamespace().equals("test:module1"));
        assertTrue(cont1data.getDataDomNode().getModuleName().equals("module1"));

        final LeafInstance leaf11data = getLeafInstance(cont1data, NS1, "leaf11");
        assertTrue(leaf11data != null);
        assertTrue(leaf11data.getValue().equals(new Long(42)));

        final LeafInstance leaf12data = getLeafInstance(cont1data, NS1, "leaf12");
        assertTrue(leaf12data != null);
        assertTrue(leaf12data.getDataDomNode().getNamespace().equals("test:module1"));
        assertTrue(leaf12data.getDataDomNode().getModuleName().equals("module1"));
        assertTrue(leaf12data.getValue().equals(new Long(58)));

        final LeafInstance leaf13data = getLeafInstance(cont1data, NS1, "leaf13");
        assertTrue(leaf13data != null);
        assertTrue(leaf13data.getValue().equals("Hello!"));

        final LeafInstance leaf14data = getLeafInstance(cont1data, NS1, "leaf14");
        assertTrue(leaf14data != null);
        assertTrue(leaf14data.getValue().equals(""));

        // -------------- A few NPs and defaults ----------------

        final ListInstance list2data4 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "4"));
        assertTrue(list2data4 != null);

        LeafInstance leaf21data = getLeafInstance(list2data4, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getDataDomNode().getNamespace().equals("test:module1"));
        assertTrue(leaf21data.getDataDomNode().getModuleName().equals("module1"));
        assertTrue(leaf21data.getValue().equals(new Long(4)));

        ContainerInstance cont22data = getContainerInstance(list2data4, NS1, "cont22");
        assertTrue(cont22data != null);

        LeafInstance leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("One"));

        LeafInstance leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data != null);
        assertTrue(leaf24data.getValue().equals("Two"));

        LeafInstance leaf25data = getLeafInstance(list2data4, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("Three"));

        List<LeafListInstance> leaflist26data = getLeafListInstances(list2data4, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 1);
        assertTrue(leaflist26data.get(0).getValue().equals("Six"));

        // ... ... ... ...

        final ListInstance list2data5 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "5"));
        assertTrue(list2data5 != null);

        leaf21data = getLeafInstance(list2data5, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals(new Long(5)));

        cont22data = getContainerInstance(list2data5, NS1, "cont22");
        assertTrue(cont22data != null);

        leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("One"));

        leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data == null);

        leaf25data = getLeafInstance(list2data5, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListInstances(list2data5, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // ... ... ... ...

        final ListInstance list2data6 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "6"));
        assertTrue(list2data6 != null);

        leaf21data = getLeafInstance(list2data6, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals(new Long(6)));

        cont22data = getContainerInstance(list2data6, NS1, "cont22");
        assertTrue(cont22data == null);

        leaf25data = getLeafInstance(list2data6, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListInstances(list2data6, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // --------------------- Unions ---------------------------

        final ContainerInstance cont5data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont5");

        final LeafInstance leaf51data = getLeafInstance(cont5data, NS1, "leaf51");
        assertTrue(leaf51data != null);
        assertTrue(leaf51data.getValue().equals(""));

        // --------------------- CDATA ---------------------------

        final ContainerInstance cont8data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont8");

        final LeafInstance leaf81data = getLeafInstance(cont8data, NS1, "leaf81");
        assertTrue(leaf81data != null);
        assertTrue(leaf81data.getValue().equals("should ignore whitespaces"));

        final LeafInstance leaf82data = getLeafInstance(cont8data, NS1, "leaf82");
        assertTrue(leaf82data != null);
        assertTrue(leaf82data.getValue().equals("should also ignore whitespaces"));

        final LeafInstance leaf83data = getLeafInstance(cont8data, NS1, "leaf83");
        assertTrue(leaf83data != null);
        assertTrue(leaf83data.getValue().equals("  should be 2 whitespaces either side  "));
    }

    @Test
    public void test_module1_module2_data3_data4() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2));
        parseAbsoluteYangData(Arrays.asList(DATA3, DATA4));

        assertNoFindings();

        final RootInstance combinedInstanceDataRoot = yangDeviceModel.getCombinedInstanceDataRoot();

        // -------------- A few NPs and defaults ----------------

        final ListInstance list2data35 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "35"));
        assertTrue(list2data35 != null);

        LeafInstance leaf21data = getLeafInstance(list2data35, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("35"));

        ContainerInstance cont22data = getContainerInstance(list2data35, NS1, "cont22");
        assertTrue(cont22data != null);

        LeafInstance leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("hello"));

        LeafInstance leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data != null);
        assertTrue(leaf24data.getValue().equals("world"));

        LeafInstance leaf25data = getLeafInstance(list2data35, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("sure"));

        List<LeafListInstance> leaflist26data = getLeafListInstances(list2data35, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 2);
        assertTrue(leaflist26data.get(0).getValue().equals("four"));
        assertTrue(leaflist26data.get(1).getValue().equals("five"));

        // ... ... ... ...

        final ListInstance list2data48 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "48"));
        assertTrue(list2data48 != null);

        leaf21data = getLeafInstance(list2data48, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("48"));

        cont22data = getContainerInstance(list2data48, NS1, "cont22");
        assertTrue(cont22data == null);

        leaf25data = getLeafInstance(list2data48, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListInstances(list2data48, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // -------------- Some choice handling ----------------

        final ContainerInstance cont3data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont3");
        assertTrue(cont3data != null);

        final LeafInstance leaf33data = getLeafInstance(cont3data, NS1, "leaf33");
        assertTrue(leaf33data == null);

        final LeafInstance leaf34data = getLeafInstance(cont3data, NS1, "leaf34");
        assertTrue(leaf34data == null);

        final ContainerInstance cont35data = getContainerInstance(cont3data, NS1, "cont35");
        assertTrue(cont35data == null);

        final LeafInstance leaf38data = getLeafInstance(cont3data, NS1, "leaf38");
        assertTrue(leaf38data == null);

        final ContainerInstance cont39data = getContainerInstance(cont3data, NS1, "cont39");
        assertTrue(cont39data != null);

        // -------------- for augmentation ----------------

        final ContainerInstance cont7data = getContainerInstance(combinedInstanceDataRoot, NS1, "cont7");
        assertTrue(cont7data != null);

        final LeafInstance leaf71data = getLeafInstance(cont7data, NS2, "leaf71");
        assertTrue(leaf71data == null);

        final LeafInstance leaf72data = getLeafInstance(cont7data, NS2, "leaf72");
        assertTrue(leaf72data != null);
        assertTrue(leaf72data.getValue().equals("-90"));

        assertTrue(leaf72data.getDataDomNode().getNamespace().equals("test:module2"));
        assertTrue(leaf72data.getDataDomNode().getModuleName().equals("module2"));
    }

    @Test
    public void test_module1_data5_data6() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(DATA5, DATA6));

        assertNoFindings();

        final RootInstance combinedInstanceDataRoot = yangDeviceModel.getCombinedInstanceDataRoot();

        // -------------- first list instance ----------------

        final ListInstance list2data501 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "501"));
        assertTrue(list2data501 != null);

        LeafInstance leaf21data = getLeafInstance(list2data501, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("501"));

        ContainerInstance cont22data = getContainerInstance(list2data501, NS1, "cont22");
        assertTrue(cont22data != null);

        LeafInstance leaf23data = getLeafInstance(cont22data, NS1, "leaf23");
        assertTrue(leaf23data != null);
        assertTrue(leaf23data.getValue().equals("hello"));

        LeafInstance leaf24data = getLeafInstance(cont22data, NS1, "leaf24");
        assertTrue(leaf24data != null);
        assertTrue(leaf24data.getValue().equals("hi"));

        LeafInstance leaf25data = getLeafInstance(list2data501, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("world"));

        List<Object> leaflist26data = getLeafListValues(list2data501, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 0);

        // -------------- second list instance ----------------

        final ListInstance list2data502 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "502"));
        assertTrue(list2data501 != null);

        leaf21data = getLeafInstance(list2data502, NS1, "leaf21");
        assertTrue(leaf21data != null);
        assertTrue(leaf21data.getValue().equals("502"));

        cont22data = getContainerInstance(list2data502, NS1, "cont22");
        assertTrue(cont22data == null);

        leaf25data = getLeafInstance(list2data502, NS1, "leaf25");
        assertTrue(leaf25data == null);

        leaflist26data = getLeafListValues(list2data502, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 4);
        assertTrue(leaflist26data.contains("red"));
        assertTrue(leaflist26data.contains("yellow"));
        assertTrue(leaflist26data.contains("green"));
        assertTrue(leaflist26data.contains("blue"));
    }

    @Test
    public void test_module1_data5_data6_datainput() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(DATA5, DATA6));

        assertNoFindings();

        final YangData yangDataInput1 = yangDeviceModel.getYangInstanceDataInputs().get(0);
        assertTrue(yangDataInput1.getFindings().isEmpty());
        yangDataInput1.toString();

        final YangData yangDataInput2 = yangDeviceModel.getYangInstanceDataInputs().get(1);
        assertTrue(yangDataInput2.getFindings().isEmpty());
    }

    @Test
    public void test_module1_errordata7() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(ERROR_DATA7));

        assertHasFindingOfType(ParserFindingType.P072_MISSING_KEY_VALUE.toString());
        assertHasFindingOfType(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString());
        assertHasFindingOfType(ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString());
    }

    @Test
    public void test_module1_errordata7_datainput() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(ERROR_DATA7));

        final YangData yangDataInput = yangDeviceModel.getYangInstanceDataInputs().get(0);
        final Set<String> findingTypes = yangDataInput.getFindings().stream().map(finding -> finding.getFindingType())
                .collect(Collectors.toSet());

        assertTrue(findingTypes.contains(ParserFindingType.P072_MISSING_KEY_VALUE.toString()));
        assertTrue(findingTypes.contains(ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString()));
        assertTrue(findingTypes.contains(ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString()));
    }

    @Test
    public void test_module1_provoke_exception() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));

        final YangInput yangInput = new YangInput() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public File getFile() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }

            @Override
            public String getMediaType() {
                return null;
            }
        };

        final YangInputResolver yangInputResolver = new YangInputResolver() {
            @Override
            public Set<YangInput> getResolvedYangInput() {
                return Collections.singleton(yangInput);
            }
        };

        yangDeviceModel.parseYangData(context, yangInputResolver, new DataTreeBuilderPredicate());

        assertHasFindingOfType(ParserFindingType.P000_UNSPECIFIED_ERROR.toString());
    }

    @Test
    public void test_module1_errordata8_errordata9() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(ERROR_DATA8, ERROR_DATA9));

        assertHasFindingOfType(ParserFindingType.P073_LEAF_VALUE_ALREADY_SET.toString());
        assertHasFindingOfType(ParserFindingType.P080_NULL_VALUE.toString());

        final RootInstance combinedInstanceDataRoot = yangDeviceModel.getCombinedInstanceDataRoot();

        final ListInstance list2data1 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "1"));
        assertTrue(list2data1 != null);

        LeafInstance leaf25data = getLeafInstance(list2data1, NS1, "leaf25");
        assertTrue(leaf25data == null);

        final ListInstance list2data2 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "2"));
        assertTrue(list2data2 != null);

        leaf25data = getLeafInstance(list2data2, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("abc 123"));

        final ListInstance list2data3 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "3"));
        assertTrue(list2data3 != null);

        leaf25data = getLeafInstance(list2data3, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("abc 456"));

        final ListInstance list2data4 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "4"));
        assertTrue(list2data4 != null);

        leaf25data = getLeafInstance(list2data4, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("abc 123"));

        final ListInstance list2data5 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue("leaf21",
                "5"));
        assertTrue(list2data5 != null);

        leaf25data = getLeafInstance(list2data5, NS1, "leaf25");
        assertTrue(leaf25data != null);
        assertTrue(leaf25data.getValue().equals("abc 123"));

        // ===================================

        final ListInstance list2data51 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "51"));
        assertTrue(list2data51 != null);

        List<LeafListInstance> leaflist26data = getLeafListInstances(list2data51, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 2);
        assertTrue(leaflist26data.get(0).getValue().equals("abc"));
        assertTrue(leaflist26data.get(1).getValue().equals("def"));

        final ListInstance list2data52 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "52"));
        assertTrue(list2data52 != null);

        leaflist26data = getLeafListInstances(list2data52, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 2);
        assertTrue(leaflist26data.get(0).getValue().equals("abc"));
        assertTrue(leaflist26data.get(1).getValue().equals("def"));

        final ListInstance list2data53 = getListInstanceData(combinedInstanceDataRoot, NS1, "list2", createKeyValue(
                "leaf21", "53"));
        assertTrue(list2data53 != null);

        leaflist26data = getLeafListInstances(list2data53, NS1, "leaflist26");
        assertTrue(leaflist26data != null);
        assertTrue(leaflist26data.size() == 2);
        assertTrue(leaflist26data.get(0).getValue().equals("abc"));
        assertTrue(leaflist26data.get(1).getValue().equals("def"));
    }

    @Test
    public void test_module1_errordata10_json() {

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1));
        parseAbsoluteYangData(Arrays.asList(ERROR_DATA10));

        assertHasFindingOfType(ParserFindingType.P080_NULL_VALUE.toString());
    }

    private static Map<String, String> createKeyValue(final String key, final String value) {
        final Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

}
