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
package org.oran.smo.yangtools.parser.data.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

/**
 * A simple XML parser. Keeps tracks of lines/columns in the XML as well for purposes of debugging.
 *
 * @author Mark Hollmann
 */
public abstract class XmlParser {

    public static Document createDocument(final InputStream is) throws IOException, SAXException {

        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();
            final Myhandler handler = new Myhandler(doc);

            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

            saxParser.parse(is, handler);

            return doc;

        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
        }
    }

    private static class Myhandler extends DefaultHandler2 {

        private final Document doc;

        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder sb = new StringBuilder();

        private Locator locator;

        public Myhandler(final Document doc) {
            this.doc = doc;
        }

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                throws SAXException {

            createTextNodeIfNeeded();

            final Element element = doc.createElement(qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                element.setAttribute(attributes.getQName(i), attributes.getValue(i));
            }
            element.setUserData(YangDataDomNode.LINE_NUMBER_KEY_NAME, Integer.valueOf(this.locator.getLineNumber()), null);
            element.setUserData(YangDataDomNode.COLUMN_NUMBER_KEY_NAME, Integer.valueOf(this.locator.getColumnNumber()),
                    null);
            elementStack.push(element);
        }

        @Override
        public void startCDATA() throws SAXException {
            /*
             * Flush out any text before the CDATA that may have accumulated
             */
            createTextNodeIfNeeded();
        }

        @Override
        public void endCDATA() throws SAXException {
            createCDataNodeIfNeeded();
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {

            createTextNodeIfNeeded();

            final Element closedElement = elementStack.pop();
            if (elementStack.isEmpty()) { // Is this the root element?
                doc.appendChild(closedElement);
            } else {
                final Element parentEl = elementStack.peek();
                parentEl.appendChild(closedElement);
            }
        }

        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException {
            sb.append(ch, start, length);
        }

        private void createTextNodeIfNeeded() {
            if (sb.length() > 0) {
                final Element element = elementStack.peek();
                final Node textNode = doc.createTextNode(sb.toString());
                element.appendChild(textNode);
                sb.setLength(0);
            }
        }

        private void createCDataNodeIfNeeded() {
            if (sb.length() > 0) {
                final Element element = elementStack.peek();
                final Node cdataNode = doc.createCDATASection(sb.toString());
                element.appendChild(cdataNode);
                sb.setLength(0);
            }
        }
    }

}
