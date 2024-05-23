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

import java.util.ArrayList;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ModulePrefixResolver;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.parser.Token;
import org.oran.smo.yangtools.parser.model.parser.TokenIterator;
import org.oran.smo.yangtools.parser.model.parser.Token.TokenType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;

/**
 * Represents a single schema node in the schema tree for a single YAM. Conceptually, a Yang DOM
 * can be compared to an XML DOM; that is, a structured file is broken up into a tree-representation
 * of the content.
 * <p/>
 * Every YANG statement is represented as DOM element. Every DOM element is comprised of two parts:
 * the name and the value.
 * <ul>
 * <li>The name-part is always the name of a YANG statement. Where the statement is part of the core
 * YANG language (as defined in RFC 7950) the name-part is simply the statement name (for example,
 * "leaf-list"). Where the statement is <i>usage</i> of an extension (as opposed to the <i>definition</i>
 * of an extension), the name-part is always a combination of a prefix and the name of the extension
 * (for example, "md:annotation" (see RFC 7952)).</li>
 * <li>The value-part depends on the statement; to be more precise, the semantics of the statement.
 * Most core YANG language statements require an argument, and the value-part will be the argument value.
 * For example, for a "leaf-list" statement, the value-part would be the name of the leaf-list (so,
 * for a "leaf-list my-super-leaf-list" the value-part of the DOM element would be "my-super-leaf-list").
 * However, not all statements support arguments (for example, the "input" statement), and for those
 * the value-part will be null. Also, not all extensions support arguments, and for those the value-part
 * will typically be null as well.</li>
 * </ul>
 *
 * @author Mark Hollmann
 */
public class YangDomElement {

    private final String name;
    private final String value;		// possibly null
    private String nameValue;

    private final int lineNumber;

    private YangDomElement parentElement;
    private final List<YangDomElement> children = new ArrayList<>();

    private final YangDomDocumentRoot documentRoot;

    public YangDomElement(final String name, final String value, final YangDomElement parentElement, final int lineNumber) {
        /*
         * We intern the name. Saves quite a bit of memory.
         */
        this.name = name.intern();
        this.value = value;
        this.parentElement = parentElement;
        this.lineNumber = lineNumber;

        if (parentElement != null) {
            parentElement.children.add(this);
            this.documentRoot = parentElement.getDocumentRoot();
        } else {
            this.documentRoot = (YangDomDocumentRoot) this;
        }
    }

    /**
     * Returns the name of the statement. This will be the name of a statement part of the
     * core YANG language, or the name of a prefixed extension.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the argument of the statement. The semantics of the returned value depend
     * on the statement. May return null.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns null if the value is null, otherwise the trimmed value.
     */
    public String getTrimmedValueOrNull() {
        return value == null ? null : value.trim();
    }

    /**
     * Returns empty string if the value is null, otherwise the trimmed value.
     */
    public String getTrimmedValueOrEmpty() {
        return value == null ? "" : value.trim();
    }

    public String getNameValue() {

        if (nameValue == null) {
            final StringBuilder sb = new StringBuilder();

            sb.append('\'');
            sb.append(name);
            if (value != null) {
                sb.append(' ');
                sb.append(value);
            }
            sb.append('\'');

            nameValue = sb.toString();
        }

        return nameValue;
    }

    public YangDomElement getParentElement() {
        return parentElement;
    }

    /**
     * Returns the list of child DOM elements, in the order in which they are in the YAM.
     * The returned must not be modified.
     */
    public List<YangDomElement> getChildren() {
        return children;
    }

    public YangDomDocumentRoot getDocumentRoot() {
        return documentRoot;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public YangModel getYangModel() {
        return getDocumentRoot().getYangModel();
    }

    public ModulePrefixResolver getPrefixResolver() {
        return getYangModel().getPrefixResolver();
    }

    /**
     * The given DOM element is added as child to this element, and removed from its previous parent. This is typically
     * done when injecting additional statements into the statement tree.
     */
    public void reparent(final YangDomElement childElement) {
        if (childElement.parentElement != null) {
            childElement.parentElement.children.remove(childElement);
        }
        this.children.add(childElement);
        childElement.parentElement = this;
    }

    /**
     * Removes this DOM element from under its parent. This in effect detaches the DOM element, and its complete
     * sub-tree, from the parent.
     */
    public void remove() {
        if (parentElement == null) {
            return;
        }

        parentElement.children.remove(this);
        parentElement = null;
    }

    /**
     * Processes the token stream and recursively builds the DOM tree.
     */
    void processTokens(final ParserExecutionContext context, final TokenIterator iter) {

        /*
         * We are at the beginning of an element, hence there is a left-bracket to start. We don't
         * need to explicitly check that, other code logic will makes sure that this method is
         * invoked such that the next token is a left brace. We can safely skip it and advance
         * the iterator.
         */
        iter.advance(1);

        /*
         * It's possible (though unlikely) that we have encountered a sequence "{}" - which
         * doesn't really make sense, but we are nice about it and handle it leniently.
         */
        if (!iter.done() && iter.getToken(0).type == TokenType.RIGHT_BRACE) {
            context.addFinding(new Finding(getYangModel(), iter.getToken(0).lineNumber,
                    ParserFindingType.P055_SUPERFLUOUS_STATEMENT.toString(),
                    "Encountered '{}', which does nothing. Replace with ';' or un-comment the contents."));
            iter.advance(1);
            return;
        }

        while (true) {

            /*
             * Wups - iterator exhausted? Thats wrong.
             */
            if (iter.done()) {
                context.addFinding(new Finding(getYangModel(), ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END,
                        "Unexpected end of document. A closing curly brace is probably missing."));
                return;
            }

            final Token token1 = iter.getToken(0);

            /*
             * Are we at the end of this element, i.e have processed all statements
             * underneath (denoted by closing curly brace)? Then exit out here.
             */
            if (token1.type == TokenType.RIGHT_BRACE) {
                iter.advance(1);
                return;
            }

            final Token token2 = iter.getToken(1);
            final Token token3 = iter.getToken(2);

            /**
             * The following can be the case now (all valid):
             *
             * A.) <statement> <argument> ;
             * B.) <statement> <argument> { ... stuff ... }
             * C.) <statement> ;
             * D.) <statement> { ... stuff ... }
             * E.) ;
             *
             * We could also have this here (all invalid):
             * F.) {
             * G.) <statement> }
             */
            if (token1.type == TokenType.STRING && token2 != null && token2.type == TokenType.STRING && token3 != null && token3.type == TokenType.SEMI_COLON) {
                /*
                 * Case A) Simple statement with argument and finished with ; so no nesting. We consume all
                 * three tokens.
                 *
                 * Example: "max-elements 100 ;"
                 */
                iter.advance(3);
                new YangDomElement(token1.value, token2.value, this, token1.lineNumber);

            } else if (token1.type == TokenType.STRING && token2 != null && token2.type == TokenType.STRING && token3 != null && token3.type == TokenType.LEFT_BRACE) {
                /*
                 * Case B) Statement and argument followed by curly opening braces. Only consume two tokens
                 * (not the opening brace). Recurse down the tree.
                 *
                 * Example: "leaf my-leaf { type string; }"
                 */
                iter.advance(2);
                final YangDomElement newYangDomElement = new YangDomElement(token1.value, token2.value, this,
                        token1.lineNumber);
                newYangDomElement.processTokens(context, iter);

            } else if (token1.type == TokenType.STRING && token2 != null && token2.type == TokenType.SEMI_COLON) {
                /*
                 * Case C) Simple statement without argument, no nesting. We consume the two tokens.
                 *
                 * Example: "input ;"        (lame example, one wouldn't usually see this in a model...)
                 */
                iter.advance(2);
                new YangDomElement(token1.value, null, this, token1.lineNumber);

            } else if (token1.type == TokenType.STRING && token2 != null && token2.type == TokenType.LEFT_BRACE) {
                /*
                 * Case D) Statement only, followed by curly opening braces with more content. No argument
                 * so we only consume first token only (not the opening brace). Recurse down the tree.
                 *
                 * Example: "input { leaf my-leaf { ... }}"
                 */
                iter.advance(1);
                final YangDomElement newYangDomElement = new YangDomElement(token1.value, null, this, token1.lineNumber);
                newYangDomElement.processTokens(context, iter);

            } else if (token1.type == TokenType.SEMI_COLON) {
                /*
                 * Case E) - unnecessary semicolon, swallow it - technically it is invalid syntax,
                 * but we are lenient.
                 */
                iter.advance(1);
                context.addFinding(new Finding(getYangModel(), token1.lineNumber,
                        ParserFindingType.P055_SUPERFLUOUS_STATEMENT.toString(), "The extra semicolon is unnecessary."));

            } else if (token1.type == TokenType.LEFT_BRACE) {
                /*
                 * Case F) - unexpected left-brace - should not be here.
                 */
                iter.advance(1);
                context.addFinding(new Finding(getYangModel(), token1.lineNumber,
                        ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(), "Unexpected opening curly brace."));

            } else {

                context.addFinding(new Finding(getYangModel(), token1.lineNumber,
                        ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                        "Unexpected content '" + token1.value + "'."));
                return;
            }
        }
    }

    public String getSimplifiedPath() {
        final StringBuilder sb = new StringBuilder(200);
        prependPath(sb);
        return sb.toString();
    }

    private void prependPath(final StringBuilder sb) {
        if (!name.equals(CY.MODULE) && !name.equals(CY.SUBMODULE) && parentElement != null) {
            parentElement.prependPath(sb);
        }

        sb.append('/');
        sb.append(name);
        if (value != null) {

            sb.append('=');

            final boolean isPath = name.equals(CY.AUGMENT) || name.equals(CY.DEVIATION) || name.equals(CY.USES) || name
                    .equals(CY.WHEN) || name.equals(CY.MUST);
            if (isPath) {
                sb.append('(');
                sb.append(value);
                sb.append(')');
            } else {
                sb.append(value);
            }
        }
    }

    /*
     * This is really not needed as it is the same as in Object - however, created to
     * enforce that equality check always operates based on objects, and nothing else.
     */
    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }

    @Override
    public String toString() {
        return value == null ? name : name + " " + value;
    }

    @Override
    public int hashCode() {
        return getNameValue().hashCode();
    }
}
