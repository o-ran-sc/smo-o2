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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.HasLineAndColumn;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonArray;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObjectMemberName;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonPrimitive;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonValue;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Represents a node in the data tree. The node can be structural (container,
 * list) or content (leaf, leaf-list).
 *
 * @author Mark Hollmann
 */
public class YangDataDomNode {

    public final static String LINE_NUMBER_KEY_NAME = "lineNumber";
    public final static String COLUMN_NUMBER_KEY_NAME = "colNumber";

    protected final static String ROOT_SLASH = "/";

    private final String name;
    private String namespace;
    private String moduleName;

    private final Object value;

    private final int lineNumber;
    private final int columnNumber;

    private final PrefixResolver prefixResolver;		// only applies to XML

    private YangDataDomNode parentNode;
    private final List<YangDataDomNode> children = new ArrayList<>();

    private final YangDataDomDocumentRoot documentRoot;

    private List<YangDataDomNodeAnnotationValue> annotations = null;

    /**
     * Findings made in respect of this piece of data, if any.
     */
    private Set<Finding> findings = null;

    /**
     * Special constructor just for the data DOM document root.
     */
    protected YangDataDomNode() {
        this.name = ROOT_SLASH;
        this.namespace = ROOT_SLASH;
        this.moduleName = ROOT_SLASH;
        this.value = null;
        this.lineNumber = 0;
        this.columnNumber = 0;
        this.prefixResolver = new PrefixResolver();
        this.documentRoot = (YangDataDomDocumentRoot) this;
    }

    /**
     * Returns the name of the data node.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the namespace of the data node. May return null if data was JSON encoded and
     * namespaces were not resolved yet.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the module of the data node. May return null if data was XML encoded and
     * module names were not resolved yet.
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the source type of the data.
     */
    public SourceDataType getSourceDataType() {
        return getDocumentRoot().getSourceDataType();
    }

    /**
     * Returns the value of this data DOM node. The data type of the returned object
     * depends on the input that was used to construct this object. If it was XML,
     * then the data type will always be String. If the input was JSON, then the data
     * type may be String, Double, or Boolean. May return null if explicitly set to
     * NIL in XML input or null in JSON input.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns a stringefied representation of the value. May return null. Note that
     * Double objects that are integer will not be returned in integer format, but
     * in double format.
     */
    public String getStringValue() {
        return value == null ? null : value.toString();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public YangDataDomNode getParentNode() {
        return parentNode;
    }

    public List<YangDataDomNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public YangDataDomDocumentRoot getDocumentRoot() {
        return documentRoot;
    }

    public YangData getYangData() {
        return getDocumentRoot().getYangData();
    }

    /**
     * For anydata and anyxml we need to re-construct the descendant tree as the client may wish to parse it further.
     */
    public String getReassembledChildren() {
        // TODO when really needed.
        return "";
    }

    /**
     * Return all child DOM nodes with the specified name and namespace/module.
     */
    public List<YangDataDomNode> getChildren(final String soughtNamespace, final String soughtModuleName,
            final String soughtName) {
        return children.stream().filter(child -> {
            if (!child.getName().equals(soughtName)) {
                return false;
            }
            if (child.getNamespace() != null && child.getNamespace().equals(soughtNamespace)) {
                return true;
            }
            if (child.getModuleName() != null && child.getModuleName().equals(soughtModuleName)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * Returns a single child DOM node with the specified name and namespace or module. Returns null if not found.
     */
    public YangDataDomNode getChild(final String soughtNamespace, final String soughtModuleName, final String soughtName) {

        for (final YangDataDomNode child : children) {
            if (child.getName().equals(soughtName)) {
                if (child.getNamespace() != null && child.getNamespace().equals(soughtNamespace)) {
                    return child;
                }
                if (child.getModuleName() != null && child.getModuleName().equals(soughtModuleName)) {
                    return child;
                }
            }
        }

        return null;
    }

    public PrefixResolver getPrefixResolver() {
        return prefixResolver;
    }

    public List<YangDataDomNodeAnnotationValue> getAnnotations() {
        return annotations == null ?
                Collections.<YangDataDomNodeAnnotationValue> emptyList() :
                Collections.unmodifiableList(annotations);
    }

    /**
     * Returns a human-readable string with the full path to the DOM node.
     */
    public String getPath() {

        final List<YangDataDomNode> dataDomNodes = new ArrayList<>(10);
        YangDataDomNode runDataDomNode = this;

        while (!(runDataDomNode instanceof YangDataDomDocumentRoot)) {
            dataDomNodes.add(0, runDataDomNode);
            runDataDomNode = runDataDomNode.getParentNode();
        }

        final StringBuilder sb = new StringBuilder();
        for (final YangDataDomNode domNode : dataDomNodes) {
            sb.append('/').append(domNode.getName());
        }

        return sb.toString();
    }

    /**
     * Depending on the source (JSON or XML) either module name or namespace will be missing after the
     * initial construction of the tree. This here will fix up the missing bits of information.
     */
    public void resolveModuleOrNamespace(final ModuleAndNamespaceResolver resolver) {

        if (moduleName == null && namespace != null) {
            moduleName = resolver.getModuleForNamespace(namespace);
        } else if (namespace == null && moduleName != null) {
            namespace = resolver.getNamespaceForModule(moduleName);
        }

        if (annotations != null) {
            annotations.forEach(anno -> anno.resolveModuleOrNamespace(resolver));
        }

        children.forEach(child -> child.resolveModuleOrNamespace(resolver));
    }

    public void addFinding(final Finding finding) {
        if (findings == null) {
            findings = new HashSet<>();
        }
        findings.add(finding);
    }

    /**
     * Returns the findings for this data DOM node. Returns empty set if no findings found.
     */
    public Set<Finding> getFindings() {
        return findings == null ? Collections.<Finding> emptySet() : findings;
    }

    @Override
    public String toString() {
        return value == null ? name : name + " " + value;
    }

    // ===================================== XML processing ==================================

    /**
     * Constructor for a data node instance encoded in XML.
     */
    public YangDataDomNode(final ParserExecutionContext context, final YangDataDomNode parentNode,
            final Element xmlDomElement) {

        parentNode.children.add(this);
        this.parentNode = parentNode;

        this.documentRoot = parentNode.getDocumentRoot();

        this.lineNumber = xmlDomElement.getUserData(LINE_NUMBER_KEY_NAME) == null ?
                0 :
                ((Integer) xmlDomElement.getUserData(LINE_NUMBER_KEY_NAME)).intValue();
        this.columnNumber = xmlDomElement.getUserData(COLUMN_NUMBER_KEY_NAME) == null ?
                0 :
                ((Integer) xmlDomElement.getUserData(COLUMN_NUMBER_KEY_NAME)).intValue();

        final List<Attr> xmlAttributes = getAttributesfromXmlElement(xmlDomElement);

        /*
         * Namespace handling.
         *
         * Before doing anything else, we need to extract prefix mappings from the XML element. These are
         * usually placed at the top of the document, but could be anywhere in the tree really.
         *
         * - If the element does not define any namespaces, then we use the prefix resolver of the parent
         *   DOM node to save on memory.
         * - If namespaces are defined, we create a new prefix resolver, clone the contents of the prefix
         *   resolver of the parent, and overwrite with whatever is defined here.
         *
         * An example in XML is as follows:
         *
         * <nacm xmlns="urn:ietf:params:xml:ns:yang:ietf-netconf-acm">
         *   <enable-nacm>true</enable-nacm>
         *   ... stuff ...
         * </nacm>
         *
         * The default namespace is always defined with xmlns="...namespace..."; a named namespace is
         * always defined with xmlns:somename="...namespace...".
         */
        if (hasNamespaceMappings(xmlAttributes)) {
            this.prefixResolver = parentNode.getPrefixResolver().clone();
            populateXmlPrefixResolver(xmlAttributes, prefixResolver);
        } else {
            this.prefixResolver = parentNode.getPrefixResolver();
        }

        /*
         * Annotation handling.
         *
         * RFC 7952 defines YANG annotations, which are encoded as XML attributes. Example:
         *
         * <foo
         *       xmlns:elm="http://example.org/example-last-modified"
         *       elm:last-modified="2015-09-16T10:27:35+02:00">
         *   ...
         * </foo>
         *
         * Above, the XML attribute "last-modified" denotes the value of the YANG annotation of the same name,
         * that is defined in namespace "http://example.org/example-last-modified".
         */
        extractAnnotationsFromXmlAttributes(context, xmlAttributes, prefixResolver);

        /*
         * Extract the name and namespace of the data node. The name may or not be prefixed. Example:
         *
         * <foo>1234</foo>
         * <bar:foo xmlns:bar="www.bar.com">1234</bar:foo>
         *
         * Note that a namespace does not have to be defined on the element itself - it could be defined
         * further up the tree (and that's the reason why the prefix resolver is cloned if necessary).
         */
        this.name = QNameHelper.extractName(xmlDomElement.getTagName());

        final String elemPrefix = QNameHelper.extractPrefix(xmlDomElement.getTagName());
        this.namespace = prefixResolver.resolveNamespaceUri(elemPrefix);

        if (namespace == null) {
            context.addFinding(new Finding(this, ParserFindingType.P077_UNRESOLVABLE_PREFIX.toString(),
                    "Prefix '" + elemPrefix + "' not resolvable to a namespace."));
        }

        /*
         * Extract the value, if any, of the element. If it is a container / list, then it will not
         * have a value. Example:
         *
         * <foo-container>
         *   <bar-list>
         *     <bar-name>name1</bar-name>
         *     <bar-state>ENABLED</bar-state>
         *   </bar-list>
         *   <bar-list>
         *     <bar-name>name1</bar-name>
         *     <bar-state>ENABLED</bar-state>
         *   </bar-list>
         * </foo-container>
         */
        this.value = getValueOfXmlElement(xmlDomElement, prefixResolver);
    }

    public void processXmlChildElements(final ParserExecutionContext context, final Element xmlDomElement) {
        /*
         * Go through all XML child elements
         */
        final NodeList childXmlNodes = xmlDomElement.getChildNodes();
        for (int i = 0; i < childXmlNodes.getLength(); ++i) {

            final Node childXmlNode = childXmlNodes.item(i);

            if (childXmlNode.getNodeType() == Node.ELEMENT_NODE) {
                final YangDataDomNode childYangDataDomNode = new YangDataDomNode(context, this, (Element) childXmlNode);
                childYangDataDomNode.processXmlChildElements(context, (Element) childXmlNode);
            }
        }
    }

    /**
     * Extracts all XML Attributes from the XML element that define prefix-to-namespace mappings.
     * Such XML attributes look as follows:
     * <p>
     * <supported-compression-types
     * xmlns="urn:rdns:o-ran:oammodel:pm"
     * xmlns:typese="urn:rdns:o-ran:oammodel:yang-types">
     * </supported-compression-types>
     */
    private static boolean hasNamespaceMappings(final List<Attr> xmlAttributes) {
        return xmlAttributes.stream().anyMatch(YangDataDomNode::attrDefinesPrefixMapping);
    }

    private static boolean attrDefinesPrefixMapping(final Attr attr) {
        return attr.getName().equals("xmlns") || attr.getName().startsWith("xmlns:");
    }

    /**
     * Given a prefix resolver, populates same with any namespace declarations
     * found amongst the supplied list of XML attributes.
     */
    protected static void populateXmlPrefixResolver(final List<Attr> xmlAttributes, final PrefixResolver prefixResolver) {

        for (final Attr attr : xmlAttributes) {
            final String attrName = attr.getName();
            final String attrValue = attr.getValue();

            if (attrName.equals("xmlns")) {
                prefixResolver.setDefaultNamespaceUri(attrValue.intern());
            } else if (attrName.startsWith("xmlns:")) {
                prefixResolver.addMapping(attrName.substring(6).intern(), attrValue.intern());
            }
        }
    }

    private void extractAnnotationsFromXmlAttributes(final ParserExecutionContext context, final List<Attr> xmlAttributes,
            final PrefixResolver prefixResolver) {

        if (xmlAttributes.isEmpty()) {
            return;
        }

        /*
         * We go over all XML attributes and whatever does not denote a namespace, or a nil value, we
         * assume is an annotation.
         */
        for (final Attr attr : xmlAttributes) {

            if (attrDefinesPrefixMapping(attr) || attrIsXsiNil(attr, prefixResolver)) {
                continue;
            }

            final String qName = attr.getName();

            final String attrPrefix = QNameHelper.extractPrefix(qName);
            final String attrNamespace = prefixResolver.resolveNamespaceUri(attrPrefix);
            if (attrNamespace == null) {
                context.addFinding(new Finding(this, ParserFindingType.P077_UNRESOLVABLE_PREFIX.toString(),
                        "Prefix '" + attrPrefix + "' not resolvable to a namespace."));
                continue;
            }

            if (this.annotations == null) {
                this.annotations = new ArrayList<>(2);
            }

            final String attrName = QNameHelper.extractName(qName);
            this.annotations.add(new YangDataDomNodeAnnotationValue(attrNamespace, null, attrName, attr.getValue()));
        }
    }

    /**
     * Returns the value of the element. Note this may be null.
     */
    private static String getValueOfXmlElement(final Element xmlDomElement, final PrefixResolver prefixResolver) {

        /*
         * Null-value handling. An element can be explicitly expressed as being null by using xsi:isNull. Never
         * seen in real life and not sure how it would make sense to have a null value in the data (just leave
         * out the XML element...). Example:
         *
         * <foo
         *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         *       xsi:nil="true" />
         *
         * Note that according to w3 the XSI namespace must be explicitly declared.
         */
        if (elementIsNil(xmlDomElement, prefixResolver)) {
            return null;
        }

        /*
         * If the XML element has data (i.e. it is a leaf or leaf-list), then there should be a text node
         * underneath (note: element.getNodeValue() is wrong to use).
         */
        final NodeList childXmlNodes = xmlDomElement.getChildNodes();
        final StringBuilder value = new StringBuilder();

        for (int i = 0; i < childXmlNodes.getLength(); ++i) {

            final Node childXmlNode = childXmlNodes.item(i);

            switch (childXmlNode.getNodeType()) {
                case Node.TEXT_NODE:
                    /*
                     * The text node will contain all of the control characters and the whitespaces; all of
                     * which needs to be stripped out to arrive at something that makes sense (or nothing).
                     */
                    cleanXmlText(childXmlNode.getNodeValue(), value);
                    break;

                case Node.CDATA_SECTION_NODE:

                    value.append(childXmlNode.getNodeValue());
                    break;

                case Node.ELEMENT_NODE:
                    /*
                     * An element node exists under this one here. That implies that this element here is a
                     * container / list, so it cannot have a value. Would be incorrect XML.
                     */
                    return null;
            }
        }

        return value.toString();
    }

    /**
     * Returns whether xsi:nil is present in the list of XML attributes.
     */
    private static boolean elementIsNil(final Element xmlDomElement, final PrefixResolver prefixResolver) {
        return getAttributesfromXmlElement(xmlDomElement).stream().anyMatch(attr -> attrIsXsiNilTrue(attr, prefixResolver));
    }

    /**
     * Returns whether the XML attribute is XSI NIL.
     */
    private static boolean attrIsXsiNil(final Attr attr, final PrefixResolver prefixResolver) {

        final String qName = attr.getName();

        final String attrName = QNameHelper.extractName(qName);
        final String attrPrefix = QNameHelper.extractPrefix(qName);
        final String attrNamespace = prefixResolver.resolveNamespaceUri(attrPrefix);

        return ("http://www.w3.org/2001/XMLSchema-instance".equals(attrNamespace) && "nil".equals(attrName));
    }

    /**
     * Returns whether the XML attribute denotes XSI NIL with value true.
     */
    private static boolean attrIsXsiNilTrue(final Attr attr, final PrefixResolver prefixResolver) {
        return "true".equalsIgnoreCase(attr.getValue()) && attrIsXsiNil(attr, prefixResolver);
    }

    protected static List<Attr> getAttributesfromXmlElement(final Element xmlDomElement) {

        List<Attr> result = null;

        final NamedNodeMap attributesNodeMap = xmlDomElement.getAttributes();
        for (int i = 0; i < attributesNodeMap.getLength(); ++i) {
            final Node item = attributesNodeMap.item(i);
            if (item instanceof Attr) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add((Attr) item);
            }
        }

        return result != null ? result : Collections.<Attr> emptyList();
    }

    /**
     * Strip out all control characters and leading and trailing whitespaces.
     */
    private static void cleanXmlText(final String inputString, final StringBuilder result) {

        boolean containsNonWhitespaceChars = false;
        boolean containsNewLine = false;

        final char[] charArray = inputString.toCharArray();
        for (int i = 0; i < charArray.length; ++i) {
            final char c = charArray[i];
            if (c == ' ' || c == '\t') {
                // all ok so far...
            } else if (c == '\n') {
                containsNewLine = true;
            } else {
                containsNonWhitespaceChars = true;
            }

            if (containsNewLine && containsNonWhitespaceChars) {
                break;
            }
        }

        if (!containsNonWhitespaceChars) {
            /*
             * Contains only whitespace and/or new-line, all of which should be swallowed. We don't add
             * anything to the result.
             */
            return;
        }

        /*
         * Right...need to clean the string so. If there is no newline in it then we simply trim the string
         * and we are done.
         */
        if (!containsNewLine) {
            result.append(inputString.trim());
            return;
        }

        /*
         * Uhh...newlines in it...must strip these out, and trim every line individually...and then re-assemble.
         */
        final List<String> lines = new ArrayList<>();

        try {
            final BufferedReader br = new BufferedReader(new StringReader(inputString));
            String str;
            while ((str = br.readLine()) != null) {
                str = str.trim();
                if (!str.isEmpty()) {
                    lines.add(str);
                }
            }
        } catch (Exception wontHappen) {
        }

        boolean first = true;
        for (final String line : lines) {
            if (!first) {
                result.append(' ');
            }
            result.append(line);
            first = false;
        }
    }

    // ====================================== JSON processing ===================================

    /**
     * Constructor for a data node instance encoded in JSON.
     */
    public YangDataDomNode(final ParserExecutionContext context, final YangDataDomNode parentNode, final String memberName,
            final JsonValue jsonValue) {

        parentNode.children.add(this);
        this.parentNode = parentNode;

        this.documentRoot = parentNode.getDocumentRoot();

        this.lineNumber = jsonValue.line;
        this.columnNumber = jsonValue.col;

        this.name = extractName(memberName);
        this.moduleName = extractModule(memberName, parentNode.getModuleName());
        this.namespace = null;
        this.value = jsonValue instanceof JsonPrimitive ? ((JsonPrimitive) jsonValue).getValue() : null;

        this.prefixResolver = parentNode.getPrefixResolver();
    }

    /**
     * Processing the child elements of the JSON object means that this instance here is either a container or a list.
     */
    protected void processJsonChildElements(final ParserExecutionContext context, final JsonObject jsonObject) {

        final Map<JsonObjectMemberName, JsonValue> members = jsonObject.getValuesByMember();

        /*
         * Handle the annotations, if any, for this container or list.
         */
        final JsonObject annotationsForThis = getAnnotationJsonObject(context, jsonObject, "@");
        extractAnnotationsFromJsonObject(context, annotationsForThis, this);

        /*
         * Handle any possible [null] element.
         */
        fixupEmptyHandling(jsonObject);

        /*
         * Process the data nodes
         */
        final Set<String> processedLeafAndLeafListMemberNames = new HashSet<>();

        for (final Entry<JsonObjectMemberName, JsonValue> mapEntry : members.entrySet()) {

            final String memberName = mapEntry.getKey().getMemberName();

            /*
             * Annotations will be handled separately as part of the data nodes, skip these here.
             */
            if (memberName.startsWith("@")) {
                continue;
            }

            /*
             * What we do now depends on the type of value:
             * - JsonObject = container
             * - JsonScalar = leaf
             * - JsonArray = list or leaf-list (need to peek at the array members)
             */
            final JsonValue value = mapEntry.getValue();

            if (value instanceof JsonPrimitive) {				// leaf
                /*
                 * It is a leaf.
                 */
                final YangDataDomNode leafDataDomNode = new YangDataDomNode(context, this, memberName,
                        (JsonPrimitive) value);
                final JsonObject leafAnnotations = getAnnotationJsonObject(context, jsonObject, "@" + memberName);
                extractAnnotationsFromJsonObject(context, leafAnnotations, leafDataDomNode);

                processedLeafAndLeafListMemberNames.add(memberName);

            } else if (value instanceof JsonObject) {		// container

                final YangDataDomNode containerDataDomNode = new YangDataDomNode(context, this, memberName,
                        (JsonObject) value);
                containerDataDomNode.processJsonChildElements(context, (JsonObject) value);

            } else {

                if (((JsonArray) value).getValues().isEmpty()) {

                    // empty leaf list, nothing to do (should really not be in the JSON file)

                } else if (allMembersAreJsonPrimitives((JsonArray) value)) {		// leaf-list

                    final List<JsonValue> values = ((JsonArray) value).getValues();
                    final JsonArray leafListMemberAnnotations = getAnnotationJsonArray(context, jsonObject,
                            "@" + memberName);
                    final List<JsonValue> annotationValues = leafListMemberAnnotations.getValues();

                    if (annotationValues.size() > values.size()) {
                        issueFindingOnJsonElement(context, ParserFindingType.P069_UNEXPECTED_JSON_VALUE.toString(),
                                "The size of the JSON array for the annotations is bigger than the size of the JSON array used for the leaf-list values.",
                                leafListMemberAnnotations);
                    }

                    for (int i = 0; i < values.size(); ++i) {
                        final YangDataDomNode leafListDataDomNode = new YangDataDomNode(context, this, memberName,
                                (JsonPrimitive) values.get(i));
                        if (annotationValues.size() > i && annotationValues.get(i) != null) {
                            extractAnnotationsFromJsonObject(context, (JsonObject) annotationValues.get(i),
                                    leafListDataDomNode);
                        }
                    }

                    processedLeafAndLeafListMemberNames.add(memberName);

                } else if (allMembersAreJsonObjects((JsonArray) value)) {		// list

                    ((JsonArray) value).getValues().forEach(member -> {
                        final YangDataDomNode childYangDataDomNode = new YangDataDomNode(context, this, memberName,
                                (JsonObject) member);
                        childYangDataDomNode.processJsonChildElements(context, (JsonObject) member);
                    });

                } else {		// wrong JSON

                    issueFindingOnJsonElement(context, ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString(),
                            "The JSON array members must all either be objects or be primitives, but not a mixture of those.",
                            value);
                }
            }
        }

        /*
         * We check for any orphaned annotations here.
         */
        for (final Entry<JsonObjectMemberName, JsonValue> mapEntry : members.entrySet()) {

            final String memberName = mapEntry.getKey().getMemberName();

            if (memberName.equals("@") || !memberName.startsWith("@")) {
                continue;
            }

            if (!processedLeafAndLeafListMemberNames.contains(memberName.substring(1))) {
                /*
                 * We have an annotation with a member name for which we do not have a leaf or leaf-list.
                 */
                issueFindingOnJsonElement(context, ParserFindingType.P069_UNEXPECTED_JSON_VALUE.toString(),
                        "Annotation '" + memberName + "' cannot be matched up against a leaf or leaf-list with name '" + memberName
                                .substring(1) + "'.", mapEntry.getKey());
            }
        }
    }

    /**
     * Replaces all occurrences of [null] (i.e. a JsonArray with a single primitive
     * member being null) with null (i.e. a primitive value being null).
     */
    private static void fixupEmptyHandling(final JsonObject jsonObject) {

        final Map<JsonObjectMemberName, JsonValue> members = jsonObject.getValuesByMember();
        for (final JsonObjectMemberName key : new ArrayList<>(members.keySet())) {

            final JsonValue memberValue = members.get(key);

            if (denotesEmpty(memberValue)) {
                jsonObject.putMember(key, JsonPrimitive.valueOf(memberValue.line, memberValue.col, null));
            } else if (memberValue instanceof JsonArray) {

                final JsonArray jsonArray = (JsonArray) memberValue;
                final List<JsonValue> arrayValues = jsonArray.getValues();

                for (int i = 0; i < arrayValues.size(); ++i) {
                    final JsonValue arrayValue = arrayValues.get(i);
                    if (denotesEmpty(arrayValue)) {
                        jsonArray.setValue(i, JsonPrimitive.valueOf(arrayValue.line, arrayValue.col, null));
                    }
                }
            }
        }
    }

    /**
     * Returns whether the supplied JSON value is [null], denoting an empty value (special syntax for data type 'empty').
     */
    private static boolean denotesEmpty(final JsonValue jsonValue) {

        if (jsonValue instanceof JsonArray) {
            final JsonArray jsonArray = (JsonArray) jsonValue;
            final List<JsonValue> values = jsonArray.getValues();

            if (values.size() == 1) {
                final JsonValue firstArrayMember = values.get(0);
                if (firstArrayMember instanceof JsonPrimitive) {
                    return ((JsonPrimitive) firstArrayMember).getValue() == null;
                }
            }
        }

        return false;
    }

    /**
     * Returns a JsonObject representing the annotations for the sought name.
     */
    private JsonObject getAnnotationJsonObject(final ParserExecutionContext context, final JsonObject parentJsonObject,
            final String soughtName) {

        final Optional<Entry<String, JsonValue>> anno = parentJsonObject.getValues().entrySet().stream().filter(
                entry -> entry.getKey().equals(soughtName)).findAny();

        if (!anno.isPresent()) {
            return new JsonObject();
        }

        /*
         * Annotations must always be encoded as JSON objects.
         */
        final JsonValue annoObject = anno.get().getValue();
        if (!(annoObject instanceof JsonObject)) {
            issueFindingOnJsonElement(context, ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString(),
                    "Expected a JSON object to hold the annotations.", annoObject);
            return new JsonObject();
        }

        return (JsonObject) annoObject;
    }

    /**
     * Returns a JsonArray representing the annotations for the sought name.
     */
    private JsonArray getAnnotationJsonArray(final ParserExecutionContext context, final JsonObject parentJsonObject,
            final String soughtName) {

        final Optional<Entry<String, JsonValue>> anno = parentJsonObject.getValues().entrySet().stream().filter(
                entry -> entry.getKey().equals(soughtName)).findAny();

        if (!anno.isPresent()) {
            return new JsonArray();
        }

        final JsonValue annoObject = anno.get().getValue();
        if (!(annoObject instanceof JsonArray)) {
            issueFindingOnJsonElement(context, ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString(),
                    "Expected a JSON array to hold the annotations.", annoObject);
            return new JsonArray();
        }

        /*
         * All the members must be either a primitive null, or a JsonObject.
         */
        final JsonArray jsonArray = (JsonArray) annoObject;
        final List<JsonValue> arrayValues = jsonArray.getValues();

        for (int i = 0; i < arrayValues.size(); ++i) {

            final JsonValue arrayMemberValue = arrayValues.get(i);

            if (arrayMemberValue instanceof JsonPrimitive && ((JsonPrimitive) arrayMemberValue).equals(
                    JsonPrimitive.NULL)) {
                // we generate an empty object to replace the null.
                jsonArray.setValue(i, new JsonObject());
            } else if (arrayMemberValue instanceof JsonObject) {
                // ok, expected
            } else {
                issueFindingOnJsonElement(context, ParserFindingType.P070_WRONG_JSON_VALUE_TYPE.toString(),
                        "Expected a JSON object as array element to hold the annotations.", arrayMemberValue);
                return new JsonArray();
            }
        }

        return jsonArray;
    }

    private void extractAnnotationsFromJsonObject(final ParserExecutionContext context,
            final JsonObject jsonObjectWithAnnotations, final YangDataDomNode owningDomNode) {

        if (jsonObjectWithAnnotations.getValues().isEmpty()) {
            return;
        }

        owningDomNode.annotations = new ArrayList<>();

        /*
         * Clean up [null] handling, will be needed where the annotation does not have an argument.
         */
        fixupEmptyHandling(jsonObjectWithAnnotations);

        /*
         * Iterate over the members of the object - these are the annotations.
         */
        final Map<JsonObjectMemberName, JsonValue> annoMembers = jsonObjectWithAnnotations.getValuesByMember();
        for (final Entry<JsonObjectMemberName, JsonValue> entry : annoMembers.entrySet()) {

            final String moduleAndAnnoName = entry.getKey().getMemberName();

            /*
             * According to RFC, the annotation name MUST be prefixed with the module - section 5.2.1 in RFC 7952...
             */
            final String moduleName = extractModule(moduleAndAnnoName, null);
            final String annoName = extractName(moduleAndAnnoName);

            if (moduleName == null) {
                issueFindingOnJsonElement(context, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                        "All members of the JSON object used for annotation values must be prefixed with the module name.",
                        entry.getKey());
                continue;
            }

            owningDomNode.annotations.add(new YangDataDomNodeAnnotationValue(null, moduleName, annoName,
                    ((JsonPrimitive) entry.getValue()).getValue()));
        }
    }

    private static String extractName(final String memberName) {
        return memberName.contains(":") ? memberName.split(":")[1] : memberName;
    }

    private static String extractModule(final String memberName, final String parentModuleName) {
        return memberName.contains(":") ? memberName.split(":")[0] : parentModuleName;
    }

    private static boolean allMembersAreJsonPrimitives(final JsonArray array) {
        final List<JsonValue> values = array.getValues();
        for (final JsonValue value : values) {
            if (!(value instanceof JsonPrimitive)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allMembersAreJsonObjects(final JsonArray array) {
        final List<JsonValue> values = array.getValues();
        for (final JsonValue value : values) {
            if (!(value instanceof JsonObject)) {
                return false;
            }
        }
        return true;
    }

    private void issueFindingOnJsonElement(final ParserExecutionContext context, final String findingType,
            final String message, final HasLineAndColumn problematicJsonElement) {
        context.addFinding(new Finding(getYangData(), findingType, message, problematicJsonElement.line,
                problematicJsonElement.col));
    }
}
