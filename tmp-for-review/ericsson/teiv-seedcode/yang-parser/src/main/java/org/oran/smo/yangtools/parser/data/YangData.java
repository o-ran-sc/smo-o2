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
package org.oran.smo.yangtools.parser.data;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.parser.JsonParser;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.XmlParser;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.YangInput;
import org.oran.smo.yangtools.parser.util.StackTraceHelper;

/**
 * Represents a single YANG data file, i.e. instance data, <b>not a YANG model (YAM)</b>. The instance data
 * file can be XML or JSON. There are no dependencies onto a model when Yang data is parsed - so this class
 * can be used even without Yang models being present.
 *
 * @author Mark Hollmann
 */
public class YangData {

    /*
     * The underlying input. Typically a file with file extension ".xml" or ".json",
     * but could also be an arbitrary input stream.
     */
    private final YangInput yangInput;

    /*
     * The root of the Yang data DOM tree.
     */
    private YangDataDomDocumentRoot yangDataDomDocumentRoot;

    /*
     * Findings made in respect of this instance data file, if any.
     */
    private Set<Finding> findings = null;

    public YangData(final YangInput yangInput) {
        this.yangInput = yangInput;
    }

    public YangInput getYangInput() {
        return yangInput;
    }

    /**
     * Returns the document root of the XML data DOM for this input. Conceivably null
     * if the input was not valid XML/JSON.
     */
    public YangDataDomDocumentRoot getYangDataDomDocumentRoot() {
        return yangDataDomDocumentRoot;
    }

    public void addFinding(final Finding finding) {
        if (findings == null) {
            findings = new HashSet<>();
        }
        findings.add(finding);
    }

    public Set<Finding> getFindings() {
        return findings == null ? Collections.<Finding> emptySet() : findings;
    }

    /**
     * Parses the input into a data DOM tree.
     */
    public void parse(final ParserExecutionContext context) {

        if (yangInput.getMediaType().equals(YangInput.MEDIA_TYPE_YANG_DATA_JSON)) {
            parseJson(context);
        } else {
            parseXml(context);
        }
    }

    private void parseJson(final ParserExecutionContext context) {

        try (final InputStream inputStream = yangInput.getInputStream()) {

            final JsonParser jsonParser = new JsonParser(context, this, inputStream);
            final JsonObject rootJsonObject = (JsonObject) jsonParser.parse();

            yangDataDomDocumentRoot = new YangDataDomDocumentRoot(this, SourceDataType.JSON);
            yangDataDomDocumentRoot.buildFromJsonDocument(context, rootJsonObject);

        } catch (final Exception ex) {
            context.addFinding(new Finding(this, ParserFindingType.P000_UNSPECIFIED_ERROR.toString(), ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }

    private void parseXml(final ParserExecutionContext context) {

        try (final InputStream inputStream = yangInput.getInputStream()) {

            final Document document = XmlParser.createDocument(inputStream);
            document.getDocumentElement().normalize();

            yangDataDomDocumentRoot = new YangDataDomDocumentRoot(this, SourceDataType.XML);
            yangDataDomDocumentRoot.buildFromXmlDocument(context, document);

        } catch (final Exception ex) {
            context.addFinding(new Finding(this, ParserFindingType.P000_UNSPECIFIED_ERROR.toString(), ex.getClass()
                    .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }

    @Override
    public String toString() {
        return yangInput.getName();
    }
}
