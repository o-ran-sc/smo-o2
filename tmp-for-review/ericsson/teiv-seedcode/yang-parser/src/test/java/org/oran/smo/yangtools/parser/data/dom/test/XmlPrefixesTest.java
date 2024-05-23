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
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.w3c.dom.Document;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.parser.XmlParser;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;

import junit.framework.Assert;

public class XmlPrefixesTest {

    @Test
    public void test_MultipleNamespaces() {

        final File instanceFile = new File("src/test/resources/data-dom/xml-test/multiple_namespaces.xml");
        final YangData yangDataFile = new YangData(new FileBasedYangInput(instanceFile));

        try (final InputStream inputStream = new FileInputStream(instanceFile)) {

            final FindingsManager findingsManager = new FindingsManager(new ModifyableFindingSeverityCalculator());
            final ParserExecutionContext context = new ParserExecutionContext(findingsManager);

            final Document document = XmlParser.createDocument(inputStream);
            document.getDocumentElement().normalize();

            final YangDataDomDocumentRoot root = new YangDataDomDocumentRoot(yangDataFile, SourceDataType.XML);
            root.buildFromXmlDocument(context, document);

            final ModuleAndNamespaceResolver resolver = new ModuleAndNamespaceResolver();
            resolver.recordNamespaceMapping("namespace0", "module0");
            resolver.recordNamespaceMapping("namespace1", "module1");
            resolver.recordNamespaceMapping("namespace2", "module2");
            resolver.recordNamespaceMapping("namespace4", "module4");
            resolver.recordNamespaceMapping("namespace9", "module9");
            resolver.recordNamespaceMapping("www.foo.com", "foo");

            root.resolveModuleOrNamespace(resolver);

            assertTrue(root.getChildren().size() == 1);

            final YangDataDomNode cont1 = root.getChildren().get(0);
            assertTrue(cont1.getName().equals("cont1"));
            assertTrue(cont1.getNamespace().equals("namespace0"));
            assertTrue(cont1.getModuleName().equals("module0"));
            assertTrue(cont1.getAnnotations().size() == 0);
            assertTrue(cont1.getChildren().size() == 5);
            assertTrue(cont1.toString().equals("cont1"));
            assertTrue(cont1.getPath().equals("/cont1"));

            final YangDataDomNode leaf1 = cont1.getChildren().get(0);
            assertTrue(leaf1.getName().equals("leaf1"));
            assertTrue(leaf1.getNamespace().equals("namespace0"));
            assertTrue(leaf1.getModuleName().equals("module0"));
            assertTrue(leaf1.getStringValue().equals("42"));
            assertTrue(leaf1.getAnnotations().size() == 0);
            assertTrue(leaf1.getChildren().size() == 0);

            final YangDataDomNode leaf2 = cont1.getChildren().get(1);
            assertTrue(leaf2.getName().equals("leaf2"));
            assertTrue(leaf2.getNamespace().equals("namespace1"));
            assertTrue(leaf2.getModuleName().equals("module1"));
            assertTrue(leaf2.getStringValue().equals("43"));
            assertTrue(leaf2.getAnnotations().size() == 0);
            assertTrue(leaf2.getChildren().size() == 0);

            final YangDataDomNode leaf3 = cont1.getChildren().get(2);
            assertTrue(leaf3.getName().equals("leaf3"));
            assertTrue(leaf3.getNamespace().equals("namespace2"));
            assertTrue(leaf3.getModuleName().equals("module2"));
            assertTrue(leaf3.getStringValue().equals("44"));
            assertTrue(leaf3.getAnnotations().size() == 0);
            assertTrue(leaf3.getChildren().size() == 0);

            final YangDataDomNode cont2 = cont1.getChildren().get(3);
            assertTrue(cont2.getName().equals("cont2"));
            assertTrue(cont2.getNamespace().equals("namespace4"));
            assertTrue(cont2.getModuleName().equals("module4"));
            assertTrue(cont2.getAnnotations().size() == 0);
            assertTrue(cont2.getChildren().size() == 6);

            final YangDataDomNode leaf4 = cont2.getChildren().get(0);
            assertTrue(leaf4.getName().equals("leaf4"));
            assertTrue(leaf4.getNamespace().equals("namespace4"));
            assertTrue(leaf4.getModuleName().equals("module4"));
            assertTrue(leaf4.getStringValue().equals("40"));
            assertTrue(leaf4.getAnnotations().size() == 0);
            assertTrue(leaf4.getChildren().size() == 0);

            final YangDataDomNode leaf5 = cont2.getChildren().get(1);
            assertTrue(leaf5.getName().equals("leaf5"));
            assertTrue(leaf5.getNamespace().equals("namespace1"));
            assertTrue(leaf5.getStringValue().equals("50"));
            assertTrue(leaf5.getAnnotations().size() == 0);
            assertTrue(leaf5.getChildren().size() == 0);

            final YangDataDomNode leaf6 = cont2.getChildren().get(2);
            assertTrue(leaf6.getName().equals("leaf6"));
            assertTrue(leaf6.getNamespace().equals("namespace9"));
            assertTrue(leaf6.getStringValue().equals("60"));
            assertTrue(leaf6.getAnnotations().size() == 0);
            assertTrue(leaf6.getChildren().size() == 0);

            final YangDataDomNode leaf7 = cont2.getChildren().get(3);
            assertTrue(leaf7.getName().equals("leaf7"));
            assertTrue(leaf7.getNamespace().equals("namespace4"));
            assertTrue(leaf7.getStringValue() == null);
            assertTrue(leaf7.getAnnotations().size() == 0);
            assertTrue(leaf7.getChildren().size() == 0);

            final YangDataDomNode leaf8 = cont2.getChildren().get(4);
            assertTrue(leaf8.getName().equals("leaf8"));
            assertTrue(leaf8.getNamespace().equals("namespace4"));
            assertTrue(leaf8.getModuleName().equals("module4"));
            assertTrue(leaf8.getStringValue().equals(""));
            assertTrue(leaf8.getAnnotations().size() == 0);
            assertTrue(leaf8.getChildren().size() == 0);

            final YangDataDomNode leaf9 = cont2.getChildren().get(5);
            assertTrue(leaf9.getName().equals("leaf9"));
            assertTrue(leaf9.getNamespace().equals("namespace4"));
            assertTrue(leaf9.getStringValue().equals("90"));
            assertTrue(leaf9.getAnnotations().size() == 1);
            assertTrue(leaf9.getChildren().size() == 0);

            final YangDataDomNode cont3 = cont1.getChildren().get(4);
            assertTrue(cont3.getName().equals("cont3"));
            assertTrue(cont3.getNamespace().equals("namespace2"));
            assertTrue(cont3.getAnnotations().size() == 0);
            assertTrue(cont3.getChildren().size() == 7);

            final YangDataDomNode leaf10 = cont3.getChildren().get(0);
            assertTrue(leaf10.getName().equals("leaf10"));
            assertTrue(leaf10.getNamespace().equals("namespace0"));
            assertTrue(leaf10.getStringValue().equals("10"));
            assertTrue(leaf10.getAnnotations().size() == 3);
            assertTrue(leaf10.getAnnotations().get(0).getName().equals("myanno"));
            assertTrue(leaf10.getAnnotations().get(0).getNamespace().equals("www.foo.com"));
            assertTrue(leaf10.getAnnotations().get(0).getModuleName().equals("foo"));
            assertTrue(leaf10.getAnnotations().get(0).getValue().equals("anno-value1"));
            assertTrue(leaf10.getAnnotations().get(1).getName().equals("myanno2"));
            assertTrue(leaf10.getAnnotations().get(1).getNamespace().equals("www.foo.com"));
            assertTrue(leaf10.getAnnotations().get(1).getValue().equals("anno-value2"));
            assertTrue(leaf10.getAnnotations().get(2).getName().equals("unknownanno"));
            assertTrue(leaf10.getAnnotations().get(2).getNamespace().equals("www.foo.com"));
            assertTrue(leaf10.getAnnotations().get(2).getValue().equals("unknown"));
            assertTrue(leaf10.getChildren().size() == 0);

            final YangDataDomNode leaf11 = cont3.getChildren().get(1);
            assertTrue(leaf11.getName().equals("leaf11"));
            assertTrue(leaf11.getNamespace() == null);
            assertTrue(leaf11.getModuleName() == null);
            assertTrue(leaf11.getStringValue().equals("??"));
            assertTrue(leaf11.getAnnotations().size() == 0);
            assertTrue(leaf11.getChildren().size() == 0);
            assertTrue(leaf11.getFindings().size() == 1);
            assertTrue(leaf11.getFindings().iterator().next().getFindingType().equals(
                    ParserFindingType.P077_UNRESOLVABLE_PREFIX.toString()));

            final YangDataDomNode leaf12 = cont3.getChildren().get(2);
            assertTrue(leaf12.getName().equals("leaf12"));
            assertTrue(leaf12.getNamespace().equals("namespace0"));
            assertTrue(leaf12.getStringValue().equals("12"));
            assertTrue(leaf12.getAnnotations().size() == 0);
            assertTrue(leaf12.getChildren().size() == 0);
            assertTrue(leaf12.getFindings().size() == 1);
            assertTrue(leaf12.getFindings().iterator().next().getFindingType().equals(
                    ParserFindingType.P077_UNRESOLVABLE_PREFIX.toString()));

            final YangDataDomNode leaf13 = cont3.getChildren().get(3);
            assertTrue(leaf13.getName().equals("leaf13"));
            assertTrue(leaf13.getNamespace().equals("namespace1"));
            assertTrue(leaf13.getStringValue().equals("13"));
            assertTrue(leaf13.getAnnotations().size() == 0);
            assertTrue(leaf13.getChildren().size() == 0);
            assertTrue(leaf13.getFindings().size() == 0);

            final YangDataDomNode leaf14 = cont3.getChildren().get(4);
            assertTrue(leaf14.getName().equals("leaf14"));
            assertTrue(leaf14.getNamespace().equals("namespace1"));
            assertTrue(leaf14.getStringValue().equals("1 4"));
            assertTrue(leaf14.getAnnotations().size() == 0);
            assertTrue(leaf14.getChildren().size() == 0);
            assertTrue(leaf14.getFindings().size() == 0);

            final YangDataDomNode leaf15 = cont3.getChildren().get(5);
            assertTrue(leaf15.getName().equals("leaf15"));
            assertTrue(leaf15.getNamespace().equals("namespace1"));
            assertTrue(leaf15.getStringValue().equals("First line Second line Third line"));

            final YangDataDomNode leaf21 = cont3.getChildren().get(6);
            assertTrue(leaf21.getName().equals("leaf21"));
            assertTrue(leaf21.getNamespace().equals("namespace0"));
            assertTrue(leaf21.getStringValue().equals("11\n 22\t\t"));

        } catch (final Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void test_prefixes_on_instance_data_set() {

        final File instanceFile = new File("src/test/resources/data-dom/xml-test/prefixes_on_instance_data_set.xml");
        final YangData yangDataFile = new YangData(new FileBasedYangInput(instanceFile));

        try (final InputStream inputStream = new FileInputStream(instanceFile)) {

            final FindingsManager findingsManager = new FindingsManager(new ModifyableFindingSeverityCalculator());
            final ParserExecutionContext context = new ParserExecutionContext(findingsManager);

            final Document document = XmlParser.createDocument(inputStream);
            document.getDocumentElement().normalize();

            final YangDataDomDocumentRoot documentRoot = new YangDataDomDocumentRoot(yangDataFile, SourceDataType.XML);
            documentRoot.buildFromXmlDocument(context, document);

            assertTrue(documentRoot.getChildren().size() == 1);

            final YangDataDomNode cont1 = documentRoot.getChildren().get(0);
            assertTrue(cont1.getName().equals("cont1"));
            assertTrue(cont1.getNamespace().equals("namespace0"));

            final YangDataDomNode leaf1 = cont1.getChildren().get(0);
            assertTrue(leaf1.getName().equals("leaf1"));
            assertTrue(leaf1.getNamespace().equals("namespace0"));

            final YangDataDomNode leaf2 = cont1.getChildren().get(1);
            assertTrue(leaf2.getName().equals("leaf2"));
            assertTrue(leaf2.getNamespace().equals("namespace1"));

            final YangDataDomNode leaf3 = cont1.getChildren().get(2);
            assertTrue(leaf3.getName().equals("leaf3"));
            assertTrue(leaf3.getNamespace().equals("namespace2"));

        } catch (final Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }
}
