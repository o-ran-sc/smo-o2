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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.parser.Token.TokenType;

/**
 * The sole purpose of this class is to tokenize a YAM (supplied as an input stream). Tokens are generated
 * for various constructs of special significance (for example, the {} characters). Comment sections are skipped.
 * String concatenation (usage of the + character) is also performed.
 * <p>
 * A new instance of this class must be created for each YAM parsed.
 *
 * @author Mark Hollmann
 */
public class YamTokenizer {

    private static final String EMPTY_STRING = "";

    private boolean yangVersionStatementAlreadyHandled = false;
    private StringParseRules stringParseRules = StringParseRules.YANG1;

    private int currentLine = 0;
    private int charCount = 0;

    private boolean inBlockComment = false;
    private boolean inDoubleQuoteString = false;
    private boolean inSingleQuoteString = false;
    private StringBuilder quotedString;

    private final List<Token> tokens = new ArrayList<>(10000);

    private final ParserExecutionContext context;
    private final YangModel yangModel;
    private final InputStream is;

    public YamTokenizer(final ParserExecutionContext context, final YangModel yangModel, final InputStream is)
            throws IOException {
        this.context = context;
        this.yangModel = yangModel;
        this.is = is;
    }

    public TokenIterator tokenize() throws IOException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        String str;

        while ((str = br.readLine()) != null) {
            currentLine++;

            charCount += str.length();
            charCount++;					// +1 for the new-line character that the readLine() method will swallow

            if (inDoubleQuoteString && str.trim().isEmpty()) {
                quotedString.append('\n');
            } else {
                while (!str.isEmpty()) {
                    str = processString(str);
                }
            }
        }

        /*
         * Make sure there are no dangling quoted strings and block comments at the end of the document.
         */
        if (inBlockComment) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END,
                    "Document ends with an unclosed block comment. Be sure to close block comments with '*/'."));
            return null;
        }
        if (inDoubleQuoteString || inSingleQuoteString) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END,
                    "Document ends with non-terminated single- or double-quoted string."));
            return null;
        }

        /*
         * Make sure that the last token is not a + token
         */
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == TokenType.PLUS) {
            context.addFinding(new Finding(yangModel, ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END,
                    "Document ends with a '+' symbol."));
            return null;
        }

        final List<Token> result = new ArrayList<>(tokens.size());

        /*
         * Now we clean up the tokens and concatenate string tokens with their corresponding plus tokens.
         * That takes care of constructs such as "'Hello ' + 'World!'" in the model.
         */
        for (int i = 0; i < tokens.size(); ++i) {

            final Token oneToken = tokens.get(i);

            if (oneToken.type == TokenType.QUOTED_STRING) {
                /*
                 * Concatenate quote strings that have + symbols between them
                 */
                final StringBuilder sb = new StringBuilder(10000);
                sb.append(oneToken.value);
                final int lineNumberStart = oneToken.lineNumber;

                while (i + 1 < tokens.size()) {
                    if (tokens.get(i + 1).type == TokenType.PLUS) {
                        if (tokens.get(i + 2).type == TokenType.QUOTED_STRING) {
                            // regular concatenation
                            sb.append(tokens.get(i + 2).value);
                            i += 2;
                        } else if (tokens.get(i + 2).type == TokenType.STRING) {
                            /*
                             * We have the following: "Hello " + World. Technically disallowed by the spec, but we allow it.
                             */
                            context.addFinding(new Finding(yangModel, tokens.get(i + 2).lineNumber,
                                    ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                                    "The '+' symbol is followed by an unquoted string (the string must be quoted for the '+' to work)."));
                            sb.append(tokens.get(i + 2).value);
                            i += 2;
                        } else if (tokens.get(i + 2).type == TokenType.PLUS) {
                            /*
                             * We have a "+ +" in the document. Illegal syntax, but we allow it.
                             */
                            context.addFinding(new Finding(yangModel, tokens.get(i + 1).lineNumber,
                                    ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                                    "The '+' symbol is repeated. Remove one of them."));
                            i++;
                        } else {
                            /*
                             * Something else unexpected. Hard finding.
                             */
                            context.addFinding(new Finding(yangModel, tokens.get(i + 1).lineNumber,
                                    ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                                    "The '+' symbol is not followed by quoted string."));
                            return null;
                        }
                    } else {
                        break;
                    }
                }

                result.add(Token.newStringToken(lineNumberStart, sb.toString()));

            } else if (oneToken.type == TokenType.PLUS) {
                /*
                 * A plus token cannot just exist by itself, it must always sit between quoted strings.
                 */
                context.addFinding(new Finding(yangModel, oneToken.lineNumber,
                        ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString(),
                        "Encountered standalone '+' symbol (are the strings on either side of it quoted?)."));
                return null;

            } else if (oneToken.type == TokenType.SEMI_COLON) {
                result.add(oneToken);
                /*
                 * Consume any repeated semicolons. That's not a finding, but simply poor model editing.
                 */
                while (i + 1 < tokens.size()) {
                    if (tokens.get(i + 1).type != TokenType.SEMI_COLON) {
                        break;
                    }
                    context.addFinding(new Finding(yangModel, tokens.get(i + 1).lineNumber,
                            ParserFindingType.P055_SUPERFLUOUS_STATEMENT.toString(), "Multiple semicolons."));
                    i++;
                }
            } else {
                /*
                 * An unquoted string, or a left/right brace. Retain as-is.
                 */
                result.add(oneToken);
            }
        }

        return new TokenIterator(result);
    }

    public int getCharCount() {
        return charCount;
    }

    public int getLineCount() {
        return currentLine;
    }

    /**
     * Process the supplied string. Whatever part of the string could not be processed will be returned for iterative
     * processing.
     */
    private String processString(final String str) {

        if (inBlockComment) {
            /*
             * Must try to find character sequence star-slash that ends the block comment.
             */
            final int indexOf = str.indexOf("*/");
            if (indexOf < 0) {
                // not found, the whole string is part of the comment, so we are done processing this string here
                return EMPTY_STRING;
            }

            // found, so end the comment and return everything after the comment for further processing.
            inBlockComment = false;
            return str.substring(indexOf + 2);

        } else if (inDoubleQuoteString) {
            /*
             * Must try to find the closing quote character. Must be careful with character escaping.
             * We trim anyway (idiotic YANG rule about indentation).
             */
            final String trimmed = str.trim();

            for (int i = 0; i < trimmed.length(); ++i) {
                final char charAt = trimmed.charAt(i);

                if (charAt == '"') {
                    // done with this double-quoted string. Create token and return leftovers, if any
                    tokens.add(Token.newQuotedStringToken(currentLine, quotedString.toString()));
                    handleYangVersionStatement();

                    inDoubleQuoteString = false;
                    return trimmed.substring(i + 1);
                }
                if (charAt == '\\') {
                    // possible escaping
                    i++;
                    if (i >= trimmed.length()) {
                        /*
                         * The backslash character is the last character on the line. This is always wrong. We will be nice,
                         * though, and assume what the user means is that the next line continues this line, which it does
                         * anyway. So basically we swallow the character and continue on with the next line.
                         */
                        issueFinding(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                                "Cannot have single backslash character at end of line.");
                        return EMPTY_STRING;
                    }

                    final char nextChar = trimmed.charAt(i);

                    if (nextChar == '\\') {
                        quotedString.append('\\');
                    } else if (nextChar == 'n') {
                        quotedString.append('\n');
                    } else if (nextChar == 't') {
                        quotedString.append('\t');
                    } else if (nextChar == '"') {
                        quotedString.append('"');
                    } else {
                        /*
                         * RFC 6020 (YANG 1) does not explicitly say that any other character is disallowed.
                         * In contrast, RFC 7950 (YANG 1.1) explicitly states that any other character is
                         * not allowed. In either case, we handle it gracefully by appending the backslash
                         * and the character literally. But finding will only be issued for YANG 1.1 modules.
                         */
                        quotedString.append('\\');
                        quotedString.append(nextChar);
                        if (stringParseRules == StringParseRules.YANG1DOT1) {
                            issueFinding(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                                    "Invalid character escaping (\\" + nextChar + ") inside double-quoted string.");
                        }
                    }
                } else {
                    quotedString.append(charAt);
                }
            }

            // end of quoted string not found yet, this line is fully consumed, continue...
            quotedString.append(' ');
            return EMPTY_STRING;

        } else if (inSingleQuoteString) {
            /*
             * We are within a string enclosed in single quotes ('). According to RFC6020:
             * A single-quoted string (enclosed within ’ ’) preserves each character
             * within the quotes. A single quote character cannot occur in a
             * single-quoted string, even when preceded by a backslash.
             *
             * Try to find ending single quote
             */
            final int indexOfSingleQuote = str.indexOf('\'');
            if (indexOfSingleQuote < 0) {
                // not found, so the quoted text must stretch multiple lines. So we simply copy over the
                // remainder of the string as-is (no trimming) plus a newline and done with this string here.
                quotedString.append(str);
                quotedString.append('\n');
                return EMPTY_STRING;
            }
            // found, so go to the end of the quoted string, create token and then return the leftovers.
            quotedString.append(str.substring(0, indexOfSingleQuote));
            tokens.add(Token.newQuotedStringToken(currentLine, quotedString.toString()));
            handleYangVersionStatement();

            inSingleQuoteString = false;
            return str.substring(indexOfSingleQuote + 1);

        } else {
            /*
             * Something else. Let's trim it first to remove all whitespace noise at beginning and end.
             */
            final String trimmed = str.trim();
            if (trimmed.isEmpty()) {
                return EMPTY_STRING;
            }

            /*
             * Check for comments
             */
            if (trimmed.length() >= 2 && trimmed.startsWith("//")) {
                // A single-comment line, we are done with the rest of the string.
                return EMPTY_STRING;
            }
            if (trimmed.length() >= 2 && trimmed.startsWith("/*")) {
                // block comment starts
                inBlockComment = true;
                return trimmed.substring(2);
            }

            /*
             * Check for special characters and create tokens as required
             */
            if (trimmed.charAt(0) == '{') {
                tokens.add(Token.newLeftBraceToken(currentLine));
                return trimmed.substring(1);
            }
            if (trimmed.charAt(0) == '}') {
                tokens.add(Token.newRightBraceToken(currentLine));
                return trimmed.substring(1);
            }
            if (trimmed.charAt(0) == ';') {
                tokens.add(Token.newSemiColonToken(currentLine));
                return trimmed.substring(1);
            }
            if (trimmed.charAt(0) == '+') {
                tokens.add(Token.newPlusToken(currentLine));
                return trimmed.substring(1);
            }

            /*
             * Check for beginning of double-quote or single-quote string
             */
            if (trimmed.charAt(0) == '"') {
                quotedString = new StringBuilder(100);
                inDoubleQuoteString = true;
                String remainder = trimmed.substring(1);
                /*
                 * In case there are any spaces or tabs directly after the double-quote, these are retained.
                 */
                while (!remainder.isEmpty() && (remainder.charAt(0) == ' ' || remainder.charAt(0) == '\t')) {
                    quotedString.append(remainder.charAt(0));
                    remainder = remainder.substring(1);
                }

                return remainder;
            }
            if (trimmed.charAt(0) == '\'') {
                quotedString = new StringBuilder(100);
                inSingleQuoteString = true;
                return trimmed.substring(1);
            }

            /*
             * Some other string not in quotes. Consume one character at a time until we
             * either reach the end of the string or a whitespace character or a special
             * character. Note no character escaping allowed.
             */
            final StringBuilder unquotedString = new StringBuilder(100);
            for (int i = 0; i < trimmed.length(); ++i) {
                final char charAt = trimmed.charAt(i);

                if (charAt == ';' || charAt == '{') {
                    // reached the end
                    tokens.add(Token.newStringToken(currentLine, unquotedString.toString()));
                    handleYangVersionStatement();
                    return trimmed.substring(i);

                } else if (charAt == '"' || charAt == '\'') {
                    /*
                     * This rule has changed in YANG 1.1. Prior to that, double-quotes and single-quotes
                     * were ok inside unquoted text; starting with YANG 1.1, these are not acceptable
                     * anymore. Either case we continue (best effort).
                     */
                    if (stringParseRules == StringParseRules.YANG1DOT1) {
                        issueFinding(ParserFindingType.P012_INVALID_CHARACTER_IN_UNQUOTED_TEXT,
                                "Single-quote or double-quote character not allowed inside non-quoted string.");
                    }

                } else if (Character.isWhitespace(charAt)) {
                    // done with this unquoted string
                    tokens.add(Token.newStringToken(currentLine, unquotedString.toString()));
                    handleYangVersionStatement();
                    return trimmed.substring(i + 1);
                }

                unquotedString.append(charAt);
            }

            // reached string-end, then done as well.
            tokens.add(Token.newStringToken(currentLine, unquotedString.toString()));
            handleYangVersionStatement();
            return EMPTY_STRING;
        }
    }

    /**
     * In order to apply YANG 1 or YANG 1.1. parse rules we need to know whether this is a
     * YANG 1 or YANG 1.1 module. The only way of finding this out is by looking at the
     * "yang-version" statement. This method tries to find this statement, and its value
     * ("1" or "1.1").
     */
    private void handleYangVersionStatement() {

        if (yangVersionStatementAlreadyHandled) {
            return;
        }

        /*
         * We try to find tokens as follows:
         * - Second-last token is a string token with value 'yang-version'
         * - Last token is a string token with value 1 or 1.0 or 1.1.
         *
         * Note: "1.0" is not a valid YANG version according to RFC. But what are the chances
         * of somebody using it...
         */

        final int tokensSize = tokens.size();
        if (tokensSize < 2) {
            return;
        }

        final Token secondLastToken = tokens.get(tokensSize - 2);
        if ((secondLastToken.type == TokenType.QUOTED_STRING || secondLastToken.type == TokenType.STRING) && secondLastToken.value
                .equals("yang-version")) {

            final Token lastToken = tokens.get(tokensSize - 1);
            if ((lastToken.type == TokenType.QUOTED_STRING || lastToken.type == TokenType.STRING)) {
                switch (lastToken.value) {
                    case "1":
                    case "1.0":
                        stringParseRules = StringParseRules.YANG1;
                        yangVersionStatementAlreadyHandled = true;
                        break;
                    case "1.1":
                        stringParseRules = StringParseRules.YANG1DOT1;
                        yangVersionStatementAlreadyHandled = true;
                        break;
                }
            }
        }
    }

    private void issueFinding(final ParserFindingType findingType, final String message) {
        context.addFinding(new Finding(yangModel, currentLine, findingType.toString(), message));
    }

    /**
     * The string parse rules have slightly changed between YANG 1 and YANG 1.1.
     */
    private enum StringParseRules {
        YANG1,
        YANG1DOT1
    }
}
