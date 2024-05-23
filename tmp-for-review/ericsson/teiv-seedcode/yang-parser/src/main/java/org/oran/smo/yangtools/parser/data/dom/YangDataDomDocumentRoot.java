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
package org.oran.smo.yangtools.parser.data.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomDocumentRoot;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Root element for a YANG data DOM. Contains a tree structure of data originating from XML of JSON. The direct
 * children of this root object in effect represent the values or structure of top-level data nodes.
 * <p/>
 * Not to be confused with class {@link YangDomDocumentRoot}, which is the root of a *model* DOM.
 * <p/>
 * Will not perform a check against any YANG schema. Therefore, can also be used, if so desired, as a simple
 * multi-purpose XML/JSON parser if so desired.
 *
 * @author Mark Hollmann
 */
public class YangDataDomDocumentRoot extends YangDataDomNode {

    /**
     * Where the data originally came from. This can make a difference. In XML, the values are all
     * represented as strings; in JSON, a primitive value can be a String, Double or Boolean. If conversion
     * is required later on to a highly-typed Java object, a client must know where the data originally
     * came from.
     */
    public enum SourceDataType {
        XML,
        JSON
    }

    private final YangData yangData;

    private final SourceDataType sourceDataType;

    public YangDataDomDocumentRoot(final YangData yangData, final SourceDataType sourceDataType) {
        super();
        this.yangData = yangData;
        this.sourceDataType = sourceDataType;
    }

    @Override
    public YangData getYangData() {
        return yangData;
    }

    @Override
    public SourceDataType getSourceDataType() {
        return sourceDataType;
    }

    public void buildFromXmlDocument(final ParserExecutionContext context, final Document document) {

        /*
         * DOM Document root is a special case. It is not an element as such. It only
         * has a single element as "child"; this single element is the actual root XML
         * element.
         */
        final Element rootXmlElement = document.getDocumentElement();
        final String rootXmlElementName = QNameHelper.extractName(rootXmlElement.getTagName());

        /*
         * It is perfectly valid and possible and realistic for the XML root element to declare
         * namespaces, so we populate the prefix resolver of the root here.
         */
        final List<Attr> rootElementXmlAttributes = getAttributesfromXmlElement(rootXmlElement);
        populateXmlPrefixResolver(rootElementXmlAttributes, getPrefixResolver());

        /*
         * There is always confusion about the root XML element - and it usually depends on the input.
         * Sometimes, data is parsed that sits inside a file that a programmer has prepared; sometimes
         * it could be a NETCONF reply, etc.
         *
         * We are trying to be as lenient as possible, and we support the following:
         *
         *
         *
         * 1.) As root element <config>, for example:
         *
         * <config>
         *   <netconf-state xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring">
         *     ...stuff ...
         *
         *
         *
         * 2.) RFC 9195 ("A File Format for YANG Instance Data")
         *
         * Lately, we are aligning with draft-ietf-netmod-yang-instance-file-format-10,
         * which looks like this:
         *
         * <?xml version="1.0" encoding="UTF-8"?>
         * <instance-data-set xmlns="urn:ietf:params:xml:ns:yang:ietf-yang-instance-data">
         *   <name>read-only-acm-rules</name>
         *   <content-schema>
         *     <module>ietf-netconf-acm@2018-02-14</module>
         *   </content-schema>
         *   <revision>
         *     <date>1776-07-04</date>
         *     <description>Initial version</description>
         *   </revision>
         *   <description>Access control rules for a read-only role.</description>
         *   <content-data>
         *     <nacm xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-acm">
         *       ... stuff ...
         *   </content-data>
         * </instance-data-set>
         *
         *
         *
         * 3.) A NETCONF RPC reply:
         *
         * <rpc-reply message-id="101" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
         *   <data>
         *     <netconf-state xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring">
         *       ... stuff ...
         *
         *
         *
         * 4.) Just <data> as root:
         *
         * <data>
         *   <netconf-state xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring">
         *     ... stuff ...
         *
         *
         *
         * 5.) Data directly at the root:
         *
         * <netconf-state xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring">
         *   ... stuff ...
         */

        final List<Element> xmlElementsToProcess = new ArrayList<>();

        if (rootXmlElementName.equals("config")) {

            xmlElementsToProcess.addAll(getChildElementNodes(rootXmlElement));

        } else if (rootXmlElementName.equals("instance-data-set")) {
            /*
             * Need to find the <content-data> element underneath first. Whatever is
             * underneath that, will be handled.
             */
            final Element contentDataElement = getNamedChildElement(rootXmlElement, "content-data");
            if (contentDataElement != null) {
                /*
                 * We must add handle whatever prefixes have been defined on the <content-data> element.
                 * Never seen in the wild, but who knows....
                 */
                final List<Attr> contentDataXmlAttributes = getAttributesfromXmlElement(contentDataElement);
                populateXmlPrefixResolver(contentDataXmlAttributes, getPrefixResolver());

                xmlElementsToProcess.addAll(getChildElementNodes(contentDataElement));
            }
        } else if (rootXmlElementName.equals("rpc-reply")) {
            /*
             * Need to find the <data> element underneath first. Whatever is
             * underneath that, will be handled.
             */
            final Element dataElement = getNamedChildElement(rootXmlElement, "data");
            if (dataElement != null) {
                /*
                 * We must add handle whatever prefixes have been defined on the <data> element.
                 */
                final List<Attr> dataXmlAttributes = getAttributesfromXmlElement(dataElement);
                populateXmlPrefixResolver(dataXmlAttributes, getPrefixResolver());

                xmlElementsToProcess.addAll(getChildElementNodes(dataElement));
            }
        } else if (rootXmlElementName.equals("data")) {

            xmlElementsToProcess.addAll(getChildElementNodes(rootXmlElement));

        } else {
            /*
             * Right... assume so that the root element is immediately data.
             */
            context.addFinding(new Finding(yangData, ParserFindingType.P071_INCORRECT_ROOT_ELEMENT_OF_DATA_FILE.toString(),
                    "Expected <instance-data-set> (or <config> or <rpc-reply> or <data>) as first element in data file. Assume root element represents data. If this is not the case, this will likely lead to other findings."));

            xmlElementsToProcess.add(rootXmlElement);
        }

        if (xmlElementsToProcess.isEmpty()) {
            context.addFinding(new Finding(yangData, ParserFindingType.P079_EMPTY_DATA_FILE.toString(),
                    "The instance data input seems to be empty."));
        }

        /*
         * Now process them.
         */
        for (final Element childXmlNode : xmlElementsToProcess) {
            final YangDataDomNode childYangDataDomNode = new YangDataDomNode(context, this, childXmlNode);
            childYangDataDomNode.processXmlChildElements(context, childXmlNode);
        }

        /*
         * It quite frequently (at least for hand-drafted XML files) happens that namespaces are not declared
         * at the top of the document. We make sure that the children all have a valid namespace, otherwise
         * there will be more processing errors later on, so highlight this here.
         */
        for (final YangDataDomNode child : getChildren()) {
            if (child.getNamespace() == null) {
                context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                        "The top-level data nodes must have a namespace, but none is declared."));
            }
        }
    }

    /**
     * Given the root XML element of a document, searches for a named child element, and return that child's children.
     */
    private Element getNamedChildElement(final Element element, final String soughtChildName) {

        final Collection<? extends Element> childrenOfRoot = getChildElementNodes(element);
        for (final Element child : childrenOfRoot) {
            if (soughtChildName.equals(QNameHelper.extractName(child.getTagName()))) {
                return child;
            }
        }

        return null;
    }

    private Collection<? extends Element> getChildElementNodes(final Element element) {

        final List<Element> result = new ArrayList<>();

        final NodeList childXmlNodes = element.getChildNodes();
        for (int i = 0; i < childXmlNodes.getLength(); ++i) {
            final Node childXmlNode = childXmlNodes.item(i);
            if (childXmlNode.getNodeType() == Node.ELEMENT_NODE) {
                result.add((Element) childXmlNode);
            }
        }

        return result;
    }

    // ============================ JSON handling ============================

    public void buildFromJsonDocument(final ParserExecutionContext context, final JsonObject rootJsonObject) {

        /*
         * No fluffing around with namespaces here, of course.
         */

        processJsonChildElements(context, rootJsonObject);

        /*
         * We make sure that the children all have a valid module name, otherwise there will be
         * more processing errors later on, so highlight this here.
         */
        for (final YangDataDomNode child : getChildren()) {
            if (child.getModuleName().equals(ROOT_SLASH)) {
                context.addFinding(new Finding(this, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                        "The name of all top-level data nodes name must be prefixed with the name of the module that owns the data node."));
            }
        }
    }
}
