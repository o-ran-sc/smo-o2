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
package org.oran.smo.yangtools.parser.findings;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * A finding is something that is worth reporting back to the client. It is not necessarily
 * always a fault in the mode.
 *
 * @author Mark Hollmann
 */
public class Finding {

    private final AbstractStatement statement;		// may be null
    private final YangDomElement domElement;		// may be null
    private final YangModel yangModel;				// may be null
    private final int lineNumber;					// may be zero
    private final int columnNumber;					// may be zero

    private final YangDataDomNode dataDomNode;				// may be null
    private final YangData yangData;		// may be null

    private final String findingType;
    private final String message;
    private final String errorMessageText;			// may be null

    public Finding(final ParserFindingType findingType, final String message) {
        this(findingType.toString(), message);
    }

    public Finding(final AbstractStatement statement, final ParserFindingType findingType, final String message) {
        this(statement, findingType.toString(), message);
    }

    public Finding(final AbstractStatement statement, final String findingType, final String message) {
        this(statement, findingType, message, null);
    }

    public Finding(final YangDomElement domElement, final String findingType, final String message) {
        this(domElement, findingType, message, null);
    }

    public Finding(final YangModel yangModelFile, final ParserFindingType findingType, final String message) {
        this(yangModelFile, 0, findingType.toString(), message);
    }

    public Finding(final YangDataDomNode dataDomNode, final String findingType, final String message) {
        this(dataDomNode, findingType, message, null);
    }

    public Finding(final YangData yangDataFile, final String findingType, final String message) {
        this(yangDataFile, findingType, message, 0, 0);
    }

    // ============================================================

    public Finding(final String findingType, final String message) {
        this.statement = null;
        this.domElement = null;
        this.yangModel = null;
        this.lineNumber = 0;
        this.columnNumber = 0;
        this.dataDomNode = null;
        this.yangData = null;
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = null;
    }

    public Finding(final AbstractStatement statement, final String findingType, final String message,
            final String errorMessageText) {
        this.statement = statement;
        this.domElement = statement.getDomElement();
        this.yangModel = statement.getDomElement().getYangModel();
        this.lineNumber = statement.getDomElement().getLineNumber();
        this.columnNumber = 0;
        this.dataDomNode = null;
        this.yangData = null;
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = errorMessageText;
    }

    public Finding(final YangDomElement domElement, final String findingType, final String message,
            final String errorMessageText) {
        this.statement = null;
        this.domElement = domElement;
        this.yangModel = domElement.getYangModel();
        this.lineNumber = domElement.getLineNumber();
        this.columnNumber = 0;
        this.dataDomNode = null;
        this.yangData = null;
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = errorMessageText;
    }

    public Finding(final YangDataDomNode dataDomNode, final String findingType, final String message,
            final String errorMessageText) {
        this.statement = null;
        this.domElement = null;
        this.yangModel = null;
        this.lineNumber = dataDomNode.getLineNumber();
        this.columnNumber = dataDomNode.getColumnNumber();
        this.dataDomNode = dataDomNode;
        this.yangData = dataDomNode.getYangData();
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = errorMessageText;
    }

    public Finding(final YangModel yangModelFile, final int lineNumber, final String findingType, final String message) {
        this.statement = null;
        this.domElement = null;
        this.yangModel = yangModelFile;
        this.lineNumber = lineNumber;
        this.columnNumber = 0;
        this.dataDomNode = null;
        this.yangData = null;
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = null;
    }

    public Finding(final YangData yangDataFile, final String findingType, final String message, final int line,
            final int col) {
        this.statement = null;
        this.domElement = null;
        this.yangModel = null;
        this.lineNumber = line;
        this.columnNumber = col;
        this.dataDomNode = null;
        this.yangData = yangDataFile;
        this.findingType = findingType;
        this.message = message;
        this.errorMessageText = null;
    }

    /**
     * The statement that the finding relates to. May return null.
     */
    public AbstractStatement getStatement() {
        return statement;
    }

    /**
     * The YANG DOM element the finding relates to. May return null.
     */
    public YangDomElement getDomElement() {
        return domElement;
    }

    /**
     * The Yang Model that has the finding. May (very rarely) return null.
     */
    public YangModel getYangModel() {
        return yangModel;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * The data input (usually an XML or JSON file) the finding relates to. May return null.
     */
    public YangData getYangData() {
        return yangData;
    }

    /**
     * The data DOM node that the finding relates to. May return null.
     */
    public YangDataDomNode getDataDomNode() {
        return dataDomNode;
    }

    public String getFindingType() {
        return findingType;
    }

    public String getMessage() {
        return message;
    }

    /**
     * May return null.
     */
    public String getErrorMessageText() {
        return errorMessageText;
    }

    public boolean isYangModelRelated() {
        return yangModel != null;
    }

    public boolean isInstanceDataRelated() {
        return yangData != null;
    }

    /**
     * A general finding does not relate to a particular model or data - prime
     * example for this is P000 being issued when a NPE is caught somewhere.
     */
    public boolean isGeneralFinding() {
        return yangModel == null && yangData == null;
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof Finding)) {
            return false;
        }

        final Finding other = (Finding) obj;

        if (!this.findingType.equals(other.findingType)) {
            return false;
        }
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.columnNumber != other.columnNumber) {
            return false;
        }
        if (!this.message.equals(other.message)) {
            return false;
        }

        if (this.yangModel != null && other.yangModel != null) {
            return this.yangModel.equals(other.yangModel);
        }

        if (this.yangModel != null || other.yangModel != null) {
            return false;
        }

        if (this.yangData != null && other.yangData != null) {
            return this.yangData.equals(other.yangData);
        }

        return (this.yangData == null && other.yangData == null);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder(300);

        if (yangModel != null) {
            sb.append(yangModel.getYangInput().getName());
        } else if (yangData != null) {
            sb.append(yangData.getYangInput().getName());
        }

        if (lineNumber != 0) {
            sb.append(" / line ").append(lineNumber);
        }
        if (columnNumber != 0) {
            sb.append(" / char ").append(columnNumber);
        }
        sb.append(" ");
        sb.append(findingType);
        sb.append(": ");
        sb.append(message);

        if (errorMessageText != null) {
            sb.append(" (").append(errorMessageText).append(')');
        }

        return sb.toString();
    }
}
