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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.instance.DataTreeBuilderPredicate;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class DataTreeBuilderPredicateTest extends YangTestCommon {

    @Test
    public void test_default_predicate() {

        final File instanceFile = new File("src/test/resources/data/data-tree-builder-predicate-test/two-namespaces.xml");
        final YangData yangDataFile = new YangData(new FileBasedYangInput(instanceFile));

        try (final InputStream inputStream = new FileInputStream(instanceFile)) {

            final FindingsManager findingsManager = new FindingsManager(new ModifyableFindingSeverityCalculator());
            final ParserExecutionContext context = new ParserExecutionContext(findingsManager);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(inputStream);

            document.getDocumentElement().normalize();

            final YangDataDomDocumentRoot documentRoot = new YangDataDomDocumentRoot(yangDataFile, SourceDataType.XML);
            documentRoot.buildFromXmlDocument(context, document);

            assertTrue(documentRoot.getChildren().size() == 2);

            final YangDataDomNode cont1 = documentRoot.getChildren().get(0);
            assertTrue(cont1.getName().equals("cont1"));
            assertTrue(cont1.getNamespace().equals("namespace1"));

            final YangDataDomNode cont2 = documentRoot.getChildren().get(1);
            assertTrue(cont2.getName().equals("cont2"));
            assertTrue(cont2.getNamespace().equals("namespace2"));

            final DataTreeBuilderPredicate dataTreeBuilderPredicate = new DataTreeBuilderPredicate();

            assertTrue(dataTreeBuilderPredicate.test(cont1) == true);
            assertTrue(dataTreeBuilderPredicate.test(cont2) == true);

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test_predicate_one_namespace() {

        final File instanceFile = new File("src/test/resources/data/data-tree-builder-predicate-test/two-namespaces.xml");
        final YangData yangDataFile = new YangData(new FileBasedYangInput(instanceFile));

        try (final InputStream inputStream = new FileInputStream(instanceFile)) {

            final FindingsManager findingsManager = new FindingsManager(new ModifyableFindingSeverityCalculator());
            final ParserExecutionContext context = new ParserExecutionContext(findingsManager);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(inputStream);

            document.getDocumentElement().normalize();

            final YangDataDomDocumentRoot documentRoot = new YangDataDomDocumentRoot(yangDataFile, SourceDataType.XML);
            documentRoot.buildFromXmlDocument(context, document);

            assertTrue(documentRoot.getChildren().size() == 2);

            final YangDataDomNode cont1 = documentRoot.getChildren().get(0);
            assertTrue(cont1.getName().equals("cont1"));
            assertTrue(cont1.getNamespace().equals("namespace1"));

            final YangDataDomNode cont2 = documentRoot.getChildren().get(1);
            assertTrue(cont2.getName().equals("cont2"));
            assertTrue(cont2.getNamespace().equals("namespace2"));

            final HashSet<String> hashSet = new HashSet<>(Collections.singletonList("namespace1"));
            final DataTreeBuilderPredicate dataTreeBuilderPredicate = new DataTreeBuilderPredicate(hashSet);

            assertTrue(dataTreeBuilderPredicate.test(cont1) == true);
            assertTrue(dataTreeBuilderPredicate.test(cont2) == false);

        } catch (Exception e) {
            fail();
        }
    }

}
