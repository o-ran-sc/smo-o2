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
package org.oran.smo.yangtools.parser.model.yangdom;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.parser.Token;
import org.oran.smo.yangtools.parser.model.parser.TokenIterator;
import org.oran.smo.yangtools.parser.model.parser.Token.TokenType;
import org.oran.smo.yangtools.parser.model.schema.Schema;

/**
 * Root element for a YANG DOM. Serves as entry point into the DOM and has no
 * representation in the YAM.
 * <p/>
 * For compliant YAMs, will only ever have a single child DOM element (with a name
 * of 'module' or 'submodule').
 *
 * @author Mark Hollmann
 */
public class YangDomDocumentRoot extends YangDomElement {

    private final YangModel yangModel;

    /**
     * The schema that owns this YANG DOM.
     */
    private final Schema owningSchema;

    /**
     * Indicates that a change has been made to the DOM (perhaps due to modifications being made).
     */
    private boolean domModified;

    public YangDomDocumentRoot(final YangModel yangModel, final Schema owningSchema) {
        super("/", "/", null, 0);
        this.yangModel = yangModel;
        this.owningSchema = owningSchema;
    }

    @Override
    public YangModel getYangModel() {
        return yangModel;
    }

    public Schema getOwningSchema() {
        return owningSchema;
    }

    public void setDomHasBeenModified() {
        domModified = true;
    }

    public boolean domHasBeenModified() {
        return domModified;
    }

    /**
     * Processes the token stream and recursively builds the DOM tree.
     */
    @Override
    public void processTokens(final ParserExecutionContext context, final TokenIterator iter) {
        /*
         * Document root is a special case. It does not start or end with a brace. The expected format is:
         *
         * <statement> <argument> { ... stuff ... }
         *
         * And the only statement there should ever be is a "module" or "sub-module" statement.
         */

        if (iter.done()) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
                    "Document seems empty."));
            return;
        }

        final Token token1 = iter.getToken(0);
        final Token token2 = iter.getToken(1);
        final Token token3 = iter.getToken(2);

        if (token2 == null || token3 == null) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
                    "Missing content at the beginning of the document."));
            return;
        }

        if (!token1.value.equals("module") && !token1.value.equals("submodule")) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
                    "Expected 'module' or 'submodule' at the beginning of the document."));
            return;
        }

        if (token3.type == TokenType.LEFT_BRACE) {
            final YangDomElement newYangDomNode = new YangDomElement(token1.value, token2.value, this, token1.lineNumber);
            iter.advance(2);
            newYangDomNode.processTokens(context, iter);
        } else {
            context.addFinding(new Finding(yangModel, token3.lineNumber,
                    ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString(),
                    "Expected opening brace '{' after (sub)module name."));
            return;
        }

        if (!iter.done()) {
            final Token leftoverToken = iter.getToken(0);
            context.addFinding(new Finding(yangModel, leftoverToken.lineNumber,
                    ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString(),
                    "Unexpected content at end of document. Check curly braces balance throughout document."));
        }
    }
}
