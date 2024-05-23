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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;

/**
 * A simple JSON parser. Created in order to not introduce 3PP dependencies.
 *
 * @author Mark Hollmann
 */
public class JsonParser {

    private int currentLine = 0;
    private int charCount = 0;

    private final List<ParseToken> tokens = new ArrayList<>(10000);

    private final ParserExecutionContext context;
    private final YangData yangData;
    private final InputStream is;

    public JsonParser(final ParserExecutionContext context, final YangData yangData, final InputStream is)
            throws IOException {
        this.context = context;
        this.yangData = yangData;
        this.is = Objects.requireNonNull(is);
    }

    /**
     * Parses the input. Returns a sub-class of JsonValue, typically JsonObject or JsonArray,
     * depending on the root element of the input. Will return null if the root element of
     * the input is not valid JSON or the input is empty.
     */
    public JsonValue parse() throws IOException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        String str;

        while ((str = br.readLine()) != null) {
            currentLine++;
            charCount += str.length();
            charCount++;					// +1 for the new-line character that the readLine() method will swallow
            processLine(str);
        }

        if (tokens.isEmpty()) {
            return null;
        }

        /*
         * We have all the tokens. Now build up the result.
         */

        final Iterator<ParseToken> iter = tokens.iterator();
        final ParseToken firstToken = iter.next();

        JsonValue result = null;

        switch (firstToken.type) {
            case BEGIN_ARRAY:
                result = new JsonArray(firstToken.line, firstToken.col);
                handleArray((JsonArray) result, iter);
                break;
            case BEGIN_OBJECT:
                result = new JsonObject(firstToken.line, firstToken.col);
                handleObject((JsonObject) result, iter);
                break;
            case QUOTED_STRING:
                result = JsonPrimitive.valueOf(firstToken.line, firstToken.col, firstToken.text);
                break;
            case UNQUOTED_STRING:
                result = JsonPrimitive.valueOf(firstToken.line, firstToken.col, convertUnquotedStringToValue(firstToken));
                break;
            default:
                issueFinding(firstToken.line, firstToken.col, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Expected root JSON element to be either an object, an array, or a primitive.");
        }

        return result;
    }

    public int getCharCount() {
        return charCount;
    }

    /**
     * Process an object. The supplied index is the START_OBJECT element. Returns the position after the END_OBJECT.
     */
    private void handleObject(final JsonObject jsonObject, final Iterator<ParseToken> iter) {

        ParseToken token = iter.next();
        boolean valueSeparatorLastLoop = true;

        while (true) {

            if (token.type == ParseTokenType.END_OBJECT) {
                return;				// Done with this object.
            }

            /*
             * There must have been a separator last time around...but we are lenient and continue.
             */
            if (!valueSeparatorLastLoop) {
                issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Expected a value separator (',') before this member.");
            }

            if (token.type != ParseTokenType.QUOTED_STRING) {
                issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Expected a quoted member name or object-end.");
                return;
            }

            final JsonObjectMemberName member = new JsonObjectMemberName(token.line, token.col, token.text);

            token = iter.next();
            if (token.type != ParseTokenType.NAME_SEP) {
                issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Expected name separator character (':').");
                return;
            }

            token = iter.next();

            /*
             * Now let's have a look to see what the value is...
             */
            switch (token.type) {
                case BEGIN_ARRAY:
                    final JsonArray memberIsArray = new JsonArray(token.line, token.col);
                    jsonObject.putMember(member, memberIsArray);
                    handleArray(memberIsArray, iter);
                    break;
                case BEGIN_OBJECT:
                    final JsonObject memberIsObject = new JsonObject(token.line, token.col);
                    jsonObject.putMember(member, memberIsObject);
                    handleObject(memberIsObject, iter);
                    break;
                case QUOTED_STRING:
                    jsonObject.putMember(member, JsonPrimitive.valueOf(token.line, token.col, token.text));
                    break;
                case UNQUOTED_STRING:
                    jsonObject.putMember(member, JsonPrimitive.valueOf(token.line, token.col, convertUnquotedStringToValue(
                            token)));
                    break;
                default:
                    issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                            "Expected a value or object or array.");
                    return;
            }

            /*
             * Swallow any value separator
             */
            token = iter.next();

            if (token.type == ParseTokenType.VALUE_SEP) {
                token = iter.next();
                valueSeparatorLastLoop = true;
            } else {
                valueSeparatorLastLoop = false;
            }
        }
    }

    private void handleArray(final JsonArray jsonArray, final Iterator<ParseToken> iter) {

        ParseToken token = iter.next();
        boolean valueSeparatorLastLoop = true;

        while (true) {

            if (token.type == ParseTokenType.END_ARRAY) {
                return;			// Done with this array.
            }

            /*
             * There must have been a separator last time around...but we are lenient and continue.
             */
            if (!valueSeparatorLastLoop) {
                issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Expected a value separator (',') before this member.");
            }

            /*
             * Now let's have a look to see what the value is...
             */
            switch (token.type) {
                case BEGIN_ARRAY:		// Array within array
                    final JsonArray memberIsArray = new JsonArray(token.line, token.col);
                    jsonArray.addValue(memberIsArray);
                    handleArray(memberIsArray, iter);
                    break;
                case BEGIN_OBJECT:
                    final JsonObject arrayMemberIsObject = new JsonObject(token.line, token.col);
                    jsonArray.addValue(arrayMemberIsObject);
                    handleObject(arrayMemberIsObject, iter);
                    break;
                case QUOTED_STRING:
                    jsonArray.addValue(JsonPrimitive.valueOf(token.line, token.col, token.text));
                    break;
                case UNQUOTED_STRING:
                    jsonArray.addValue(JsonPrimitive.valueOf(token.line, token.col, convertUnquotedStringToValue(token)));
                    break;
                default:
                    issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                            "Expected a value or object or array-end.");
                    return;
            }

            /*
             * Swallow any value separator
             */
            token = iter.next();

            if (token.type == ParseTokenType.VALUE_SEP) {
                token = iter.next();
                valueSeparatorLastLoop = true;
            } else {
                valueSeparatorLastLoop = false;
            }
        }
    }

    private static final String S_NULL = "null";
    private static final String S_TRUE = "true";
    private static final String S_FALSE = "false";

    private static final double MAX_DOUBLE_REPRESENTATION = Math.pow(2, 53) - 1;
    private static final double MIN_DOUBLE_REPRESENTATION = (Math.pow(2, 53) * -1) + 1;

    private Object convertUnquotedStringToValue(final ParseToken token) {

        if (S_NULL.equals(token.text)) {
            return null;
        }
        if (S_TRUE.equals(token.text)) {
            return Boolean.TRUE;
        }
        if (S_FALSE.equals(token.text)) {
            return Boolean.FALSE;
        }

        /*
         * Has to be a number. In the vast majority of cases the number will be integer, i.e. not floating point.
         * This triggers an optimization whereby a Long object is produced instead of a Double, as it is considerably
         * easier for a client to work with integers as opposed to floating-points.
         */
        try {
            final long parseLong = Long.parseLong(token.text);
            /*
             * If we come to here this means that the conversion to long has succeeded. If the value is integer but
             * too large to fit into a long, an exception would have been thrown.
             */
            return Long.valueOf(parseLong);
        } catch (final NumberFormatException ignore) {
        }

        /*
         *
         * So it's floating point, or an integer larger than what fits into a long, and we have to generate a Double
         * However, special consideration must be paid to a stipulation in RFC 7951:
         *
         * A value of the "int8", "int16", "int32", "uint8", "uint16", or "uint32" type is represented as a JSON number.
         *
         * A value of the "int64", "uint64", or "decimal64" type is represented as a JSON string whose content is the
         * lexical representation of the corresponding YANG type as specified in Sections 9.2.1 and 9.3.1 of [RFC7950].
         *
         * For example, if the type of the leaf "foo" in Section 5.1 was "uint64" instead of "uint8", the instance would
         * have to be encoded as "foo": "123"
         *
         * The special handling of 64-bit numbers follows from the I-JSON recommendation to encode numbers exceeding the
         * IEEE 754-2008 double-precision range [IEEE754-2008] as strings; see Section 2.2 in [RFC7493].
         */

        /*
         * This here, however, is a generic JSON parser, and according to RFC7493, values within range
         * [-(2**53)+1, (2**53)-1] are allowed to be represented as number, everything else must be a string.
         *
         * The problem is that we do *not* get any exception from the standard Java classes when we translate a String to a
         * Double with loss of precision, and it is surprisingly difficult to figure this out. As there is a chance that a
         * JSON producer will ignore the rule about Doubles / Strings, and generate a large numeric value as number, we
         * check for this scenario and if it happens we simply return the token as string, i.e. we align with the RFC, and
         * expect a consumer of the output of this JSON parser to likewise be standards-aware and compliant and to be able
         * to handle Double, Long and String objects as a source of a numeric value.
         */
        try {
            final Double valueOf = Double.valueOf(token.text);
            if (valueOf > MAX_DOUBLE_REPRESENTATION || valueOf < MIN_DOUBLE_REPRESENTATION) {
                return token.text;
            }
            return valueOf;
        } catch (NumberFormatException nfex) {
            issueFinding(token, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                    "value '" + token.text + "' does not translate to null / true / false / a number.");
        }

        return null;
    }

    private static final Character C_SPACE = Character.valueOf((char) 32);		// Space
    private static final Character C_HOR_TAB = Character.valueOf((char) 9);		// Horizontal Tab
    private static final Character C_LF = Character.valueOf((char) 10);			// Line feed or New line
    private static final Character C_CR = Character.valueOf((char) 13);			// Carriage return

    private static final Set<Character> WHITE_SPACES = new HashSet<>();

    static {
        WHITE_SPACES.add(C_SPACE);
        WHITE_SPACES.add(C_HOR_TAB);
        WHITE_SPACES.add(C_LF);
        WHITE_SPACES.add(C_CR);
    }

    private static final char LEFT_SQUARE_BRACKET = '[';
    private static final char RIGHT_SQUARE_BRACKET = ']';
    private static final char LEFT_CURLY_BRACKET = '{';
    private static final char RIGHT_CURLY_BRACKET = '}';
    private static final char COLON = ':';
    private static final char COMMA = ',';
    private static final char QUOTATION_MARK = '"';

    private static final char REVERSE_SOLIDUS = '\\';
    private static final char SOLIDUS = '/';

    /**
     * Process the supplied line.
     */
    private void processLine(final String str) {

        int index = 0;

        while (true) {

            if (index >= str.length()) {
                /*
                 * End of line, we are done here.
                 */
                return;
            }

            final char charAt = str.charAt(index);

            if (WHITE_SPACES.contains(charAt)) {
                // whitespace, ignore
                index++;
            } else if (charAt == LEFT_SQUARE_BRACKET) {
                index++;
                tokens.add(ParseToken.newBeginArray(currentLine, index));
            } else if (charAt == RIGHT_SQUARE_BRACKET) {
                index++;
                tokens.add(ParseToken.newEndArray(currentLine, index));
            } else if (charAt == LEFT_CURLY_BRACKET) {
                index++;
                tokens.add(ParseToken.newBeginObject(currentLine, index));
            } else if (charAt == RIGHT_CURLY_BRACKET) {
                index++;
                tokens.add(ParseToken.newEndObject(currentLine, index));
            } else if (charAt == COLON) {
                index++;
                tokens.add(ParseToken.newNameSep(currentLine, index));
            } else if (charAt == COMMA) {
                index++;
                tokens.add(ParseToken.newValueSep(currentLine, index));
            } else if (charAt == QUOTATION_MARK) {
                index = extractQuotedString(str, index);
            } else {
                index = extractUnquotedString(str, index);
            }
        }
    }

    private static final Set<Character> UNQUOTED_STRING_TERMINATING_CHARACTERS = new HashSet<>();

    static {
        UNQUOTED_STRING_TERMINATING_CHARACTERS.addAll(WHITE_SPACES);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(LEFT_SQUARE_BRACKET);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(RIGHT_SQUARE_BRACKET);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(LEFT_CURLY_BRACKET);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(RIGHT_CURLY_BRACKET);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(COMMA);
        UNQUOTED_STRING_TERMINATING_CHARACTERS.add(COLON);
    }

    /**
     * Extracts an unquoted string. Returns the index position in the string after the end of the string.
     */
    private int extractUnquotedString(String str, int startCol) {

        final StringBuilder unquotedString = new StringBuilder();
        int index = startCol;

        while (true) {

            if (index >= str.length()) {
                /*
                 * End of line, hence we have reached the end of the unquoted string.
                 */
                tokens.add(ParseToken.newUnquotedString(currentLine, startCol, unquotedString.toString()));
                return Integer.MAX_VALUE;
            }

            /*
             * Read a character. If it is whitespace, or any of the special characters, we are done with the unquoted string.
             */
            final char charAt = str.charAt(index);

            if (UNQUOTED_STRING_TERMINATING_CHARACTERS.contains(charAt)) {
                tokens.add(ParseToken.newUnquotedString(currentLine, startCol, unquotedString.toString()));
                return index;
            }

            unquotedString.append(charAt);
            index++;
        }
    }

    /**
     * Extracts a quoted string. Returns the index position in the string after the closing double-quote
     */
    private int extractQuotedString(String str, final int startCol) {

        final StringBuilder quotedString = new StringBuilder();
        int index = startCol + 1;		// swallow starting " character

        while (true) {

            if (index >= str.length()) {
                /*
                 * We have reached the end of the string without encountering a closing
                 * double-quote! We issue a finding, but create a string anyway.
                 */
                issueFinding(currentLine, startCol, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                        "Missing terminating double-quote character.");
                tokens.add(ParseToken.newQuotedString(currentLine, startCol, quotedString.toString()));
                return Integer.MAX_VALUE;
            }

            final char charAt = str.charAt(index);

            if (charAt == QUOTATION_MARK) {
                /*
                 * Found the closing double-quote, done.
                 */
                tokens.add(ParseToken.newQuotedString(currentLine, startCol, quotedString.toString()));
                return index + 1;
            }

            if (charAt == REVERSE_SOLIDUS) {
                /*
                 * Some escaped character.
                 */
                index = appendEscapedCharacter(str, index, quotedString);
            } else {
                /*
                 * Regular character
                 */
                quotedString.append(charAt);
                index++;
            }
        }
    }

    private int appendEscapedCharacter(String str, int index, StringBuilder quotedString) {

        index++;		// Swallow the backslash

        /*
         * Something escaped. RFC7159 clearly stipulates what can be escaped:
         *
         * escape (
         * %x22 /          ; "    quotation mark  U+0022
         * %x5C /          ; \    reverse solidus U+005C
         * %x2F /          ; /    solidus         U+002F
         * %x62 /          ; b    backspace       U+0008
         * %x66 /          ; f    form feed       U+000C
         * %x6E /          ; n    line feed       U+000A
         * %x72 /          ; r    carriage return U+000D
         * %x74 /          ; t    tab             U+0009
         * %x75 4HEXDIG )  ; uXXXX                U+XXXX
         *
         * Note that SOLIDUS can also exist unescaped.
         */

        /*
         * Make sure there is another character...
         */
        if (index >= str.length()) {
            issueFinding(currentLine, index, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                    "Missing escaped character.");
            return Integer.MAX_VALUE;
        }

        final char nextChar = str.charAt(index);

        if (nextChar == 'u') {
            /*
             * Unicode. Handled separately.
             */
            return appendUnicodeCharacter(str, index, quotedString);
        }

        /*
         * Some other character, with simple escaping.
         */
        if (nextChar == QUOTATION_MARK) {
            quotedString.append(QUOTATION_MARK);
        } else if (nextChar == REVERSE_SOLIDUS) {
            quotedString.append(REVERSE_SOLIDUS);
        } else if (nextChar == SOLIDUS) {
            quotedString.append(SOLIDUS);
        } else if (nextChar == 'b') {
            quotedString.append('\b');
        } else if (nextChar == 'f') {
            quotedString.append('\f');
        } else if (nextChar == 'n') {
            quotedString.append('\n');
        } else if (nextChar == 'r') {
            quotedString.append('\r');
        } else if (nextChar == 't') {
            quotedString.append('\t');
        } else {
            /*
             * Unrecognized character. We are lenient and allow it through, but still issue a finding.
             */
            quotedString.append(nextChar);
            issueFinding(currentLine, index, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                    "Unrecognized escaped character '\\" + nextChar + "'.");
        }

        return index + 1;
    }

    private static final Map<Character, Integer> CONVERTED = new HashMap<>();

    static {
        CONVERTED.put('0', 0);
        CONVERTED.put('1', 1);
        CONVERTED.put('2', 2);
        CONVERTED.put('3', 3);
        CONVERTED.put('4', 4);
        CONVERTED.put('5', 5);
        CONVERTED.put('6', 6);
        CONVERTED.put('7', 7);
        CONVERTED.put('8', 8);
        CONVERTED.put('9', 9);
        CONVERTED.put('a', 10);
        CONVERTED.put('b', 11);
        CONVERTED.put('c', 12);
        CONVERTED.put('d', 13);
        CONVERTED.put('e', 14);
        CONVERTED.put('f', 15);
        CONVERTED.put('A', 10);
        CONVERTED.put('B', 11);
        CONVERTED.put('C', 12);
        CONVERTED.put('D', 13);
        CONVERTED.put('E', 14);
        CONVERTED.put('F', 15);
    }

    private int appendUnicodeCharacter(String str, int index, StringBuilder quotedString) {
        index++;		// Swallow the u character

        /*
         * We must have at least 4 hex characters after the u character.
         */
        if (index + 4 >= str.length()) {
            issueFinding(currentLine, index, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                    "Unicode character not correctly escaped.");
            return Integer.MAX_VALUE;
        }

        final char char1 = str.charAt(index++);
        final char char2 = str.charAt(index++);
        final char char3 = str.charAt(index++);
        final char char4 = str.charAt(index++);

        if (!CONVERTED.containsKey(char1) || !CONVERTED.containsKey(char2) || !CONVERTED.containsKey(char3) || !CONVERTED
                .containsKey(char4)) {
            issueFinding(currentLine, index, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
                    "Unicode character not correctly escaped.");
            return Integer.MAX_VALUE;
        }

        final int unicodeChar = (CONVERTED.get(char1) * 4096) + (CONVERTED.get(char2) * 256) + (CONVERTED.get(
                char3) * 16) + (CONVERTED.get(char4) * 1);

        quotedString.append((char) unicodeChar);

        return index;
    }

    private void issueFinding(final ParseToken token, final ParserFindingType findingType, final String message) {
        issueFinding(token.line, token.col, findingType, message);
    }

    private void issueFinding(final int line, final int col, final ParserFindingType findingType, final String message) {
        if (context != null && yangData != null) {
            context.addFinding(new Finding(yangData, findingType.toString(), message, line, col));
        }
    }

    private enum ParseTokenType {
        BEGIN_ARRAY,
        END_ARRAY,
        BEGIN_OBJECT,
        END_OBJECT,
        NAME_SEP,
        VALUE_SEP,
        QUOTED_STRING,
        UNQUOTED_STRING
    }

    private static class ParseToken {
        public final int line;
        public final int col;
        public final ParseTokenType type;
        public final String text;

        private ParseToken(final int line, final int col, final ParseTokenType type, final String text) {
            this.line = line;
            this.col = col;
            this.type = type;
            this.text = Objects.requireNonNull(text);
        }

        public static ParseToken newBeginArray(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.BEGIN_ARRAY, "[");
        }

        public static ParseToken newEndArray(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.END_ARRAY, "]");
        }

        public static ParseToken newBeginObject(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.BEGIN_OBJECT, "{");
        }

        public static ParseToken newEndObject(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.END_OBJECT, "}");
        }

        public static ParseToken newNameSep(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.NAME_SEP, ":");
        }

        public static ParseToken newValueSep(int line, int col) {
            return new ParseToken(line, col, ParseTokenType.VALUE_SEP, ",");
        }

        public static ParseToken newQuotedString(int line, int col, String val) {
            return new ParseToken(line, col, ParseTokenType.QUOTED_STRING, val);
        }

        public static ParseToken newUnquotedString(int line, int col, String val) {
            return new ParseToken(line, col, ParseTokenType.UNQUOTED_STRING, val);
        }
    }

    public abstract static class HasLineAndColumn {
        public final int line;
        public final int col;

        public HasLineAndColumn(final int line, final int col) {
            this.line = line;
            this.col = col;
        }
    }

    /**
     * The name of a member of a JsonObject.
     */
    public static class JsonObjectMemberName extends HasLineAndColumn {

        private final String memberName;

        public JsonObjectMemberName(final String memberName) {
            this(0, 0, memberName);
        }

        public JsonObjectMemberName(final int line, final int col, final String memberName) {
            super(line, col);
            /*
             * Member names will be interned. It is highly likely that there will be considerable
             * duplication in member names in the JSON. This will cut down on the memory usage
             * substantially if the JSON is very large.
             */
            this.memberName = Objects.requireNonNull(memberName).intern();
        }

        public String getMemberName() {
            return memberName;
        }

        @Override
        public int hashCode() {
            return memberName.hashCode();
        }

        @Override
        public String toString() {
            return memberName;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof String) {
                return this.memberName.equals((String) obj);
            }

            return obj instanceof JsonObjectMemberName && this.memberName.equals(((JsonObjectMemberName) obj).memberName);
        }
    }

    /**
     * A JsonValue may be a primitive value, or an object, or an array.
     */
    public abstract static class JsonValue extends HasLineAndColumn {
        public JsonValue(final int line, final int col) {
            super(line, col);
        }
    }

    public static class JsonPrimitive extends JsonValue {

        private static final String EMPTY_STRING = "";

        /*
         * These can be used for quick comparisons for commonly used values.
         */
        public static final JsonPrimitive NULL = new JsonPrimitive(0, 0, null);
        public static final JsonPrimitive TRUE = new JsonPrimitive(0, 0, Boolean.TRUE);
        public static final JsonPrimitive FALSE = new JsonPrimitive(0, 0, Boolean.FALSE);
        public static final JsonPrimitive ZERO = new JsonPrimitive(0, 0, Long.valueOf(0));
        public static final JsonPrimitive ONE = new JsonPrimitive(0, 0, Long.valueOf(1));
        public static final JsonPrimitive EMPTY = new JsonPrimitive(0, 0, EMPTY_STRING);

        public final Object value;

        public static JsonPrimitive valueOf(final Object obj) {
            return valueOf(0, 0, obj);
        }

        public static JsonPrimitive valueOf(final int line, final int col, final Object obj) {
            if (EMPTY_STRING.equals(obj)) {
                return new JsonPrimitive(line, col, EMPTY_STRING);
            } else {
                return new JsonPrimitive(line, col, obj);
            }
        }

        private JsonPrimitive(final int line, final int col, final Object value) {
            super(line, col);
            this.value = value;
        }

        /**
         * Returns the primitive value, which can be one of: String, Boolean, Double, Long, null.
         * <p>
         * Note that for numeric values the returned object will be of type String if the value is outside
         * the allowable range for java.lang.Long and java.lang.Double.
         * <p>
         * Consumers should be prepared to handle Long, Double and String values for numbers,
         * and to convert as appropriate.
         */
        public Object getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        /**
         * The equality check on JsonPrimitive objects allows for the supplied object to be of any type.
         */
        @Override
        public boolean equals(final Object obj) {

            final Object otherValue = obj instanceof JsonPrimitive ? ((JsonPrimitive) obj).getValue() : obj;

            if (this.value == null && otherValue == null) {
                return true;
            }

            return this.value != null && this.value.equals(otherValue);
        }
    }

    public static class JsonObject extends JsonValue {

        private final Map<JsonObjectMemberName, JsonValue> valuesByMember = new HashMap<>();
        private final Map<String, JsonValue> values = new HashMap<>();

        public JsonObject() {
            this(0, 0);
        }

        public JsonObject(final int line, final int col) {
            super(line, col);
        }

        public void putMember(final String member, final Object obj) {
            final JsonValue jsonValue = getJsonValueFromObject(obj);
            valuesByMember.put(new JsonObjectMemberName(member), jsonValue);
            values.put(member, jsonValue);
        }

        public void putMember(final JsonObjectMemberName memberName, final Object obj) {
            final JsonValue jsonValue = getJsonValueFromObject(obj);
            valuesByMember.put(memberName, Objects.requireNonNull(jsonValue));
            values.put(memberName.getMemberName(), jsonValue);
        }

        private static JsonValue getJsonValueFromObject(final Object obj) {
            if (obj == null) {
                return JsonPrimitive.NULL;
            } else if (obj instanceof JsonValue) {
                return (JsonValue) obj;
            } else if (obj instanceof Boolean || obj instanceof Number || obj instanceof String) {
                return JsonPrimitive.valueOf(obj);
            }

            throw new RuntimeException("Can't handle data type " + obj.getClass().getSimpleName());
        }

        public Map<JsonObjectMemberName, JsonValue> getValuesByMember() {
            return Collections.unmodifiableMap(valuesByMember);
        }

        public Map<String, JsonValue> getValues() {
            return Collections.unmodifiableMap(values);
        }
    }

    public static class JsonArray extends JsonValue {

        private final List<JsonValue> values = new ArrayList<>();

        public JsonArray() {
            this(0, 0);
        }

        public JsonArray(final int line, final int col) {
            super(line, col);
        }

        public void addValue(final JsonValue value) {
            values.add(Objects.requireNonNull(value));
        }

        public void setValue(final int index, final JsonValue value) {
            values.set(index, Objects.requireNonNull(value));
        }

        public List<JsonValue> getValues() {
            return Collections.unmodifiableList(values);
        }
    }
}
