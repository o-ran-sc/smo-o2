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
package org.oran.smo.yangtools.parser.model.parser;

/**
 * @author Mark Hollmann
 */
public class Token {

    public static Token newLeftBraceToken(final int lineNumber) {
        final Token token = new Token();
        token.type = TokenType.LEFT_BRACE;
        token.lineNumber = lineNumber;
        token.value = "{";
        return token;
    }

    public static Token newRightBraceToken(final int lineNumber) {
        final Token token = new Token();
        token.type = TokenType.RIGHT_BRACE;
        token.lineNumber = lineNumber;
        token.value = "}";
        return token;
    }

    public static Token newSemiColonToken(final int lineNumber) {
        final Token token = new Token();
        token.type = TokenType.SEMI_COLON;
        token.lineNumber = lineNumber;
        token.value = ";";
        return token;
    }

    public static Token newPlusToken(final int lineNumber) {
        final Token token = new Token();
        token.type = TokenType.PLUS;
        token.lineNumber = lineNumber;
        token.value = "+";
        return token;
    }

    public static Token newQuotedStringToken(final int lineNumber, final String str) {
        final Token token = new Token();
        token.type = TokenType.QUOTED_STRING;
        token.lineNumber = lineNumber;
        token.value = str;
        return token;
    }

    public static Token newStringToken(final int lineNumber, final String str) {
        final Token token = new Token();
        token.type = TokenType.STRING;
        token.lineNumber = lineNumber;
        token.value = str;
        return token;
    }

    public int lineNumber = 0;
    public TokenType type = null;
    public String value = null;

    private Token() {
    }

    public enum TokenType {
        LEFT_BRACE,      // {
        RIGHT_BRACE,     // }
        SEMI_COLON,      // ;
        PLUS,            // +
        QUOTED_STRING,   // "some text" or 'some text'
        STRING           // any text, possibly containing spaces
    }
}
