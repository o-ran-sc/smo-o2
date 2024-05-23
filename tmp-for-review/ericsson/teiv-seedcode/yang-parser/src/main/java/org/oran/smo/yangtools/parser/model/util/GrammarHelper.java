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
package org.oran.smo.yangtools.parser.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to help with Yang grammar.
 *
 * @author Mark Hollmann
 */
public abstract class GrammarHelper {

    /**
     * Returns whether the supplied string contains a white-space according to the RFC.
     * White space characters are SPACE (0x20) and HORIZONTAL TAB (0x09).
     */
    public static boolean containsYangWhitespace(final String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) == 32 || value.charAt(i) == 9) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the supplied string is a valid YANG identifier according to the RFC.
     * <p>
     * A valid identifier must:
     * <p>
     * Start with '_' or 'A-Z' or 'a-z'
     * Followed by '_' or '-' or '.' or 'A-Z' or 'a-z' or '0-9'
     * <p>
     * Note especially that other unicode characters are not allowed, and that an identifier MUST NOT start with a digit.
     */
    public static boolean isYangIdentifier(final String stringToCheck) {

        if (stringToCheck == null || stringToCheck.isEmpty()) {
            return false;
        }

        final char firstChar = stringToCheck.charAt(0);
        if (!firstCharacterOkForYangIdentifier(firstChar)) {
            return false;
        }

        for (int i = 1; i < stringToCheck.length(); ++i) {
            final char c = stringToCheck.charAt(i);
            if (!otherCharacterOkForYangIdentifier(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean firstCharacterOkForYangIdentifier(final char c) {
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c == 95;			// A-Z a-z _
    }

    private static boolean otherCharacterOkForYangIdentifier(final char c) {
        return (c >= 65 && c <= 90) || (c >= 97 && c <= 122) || (c >= 48 && c <= 57) || c == '_' || c == '-' || c == '.';
    }

    /**
     * Returns whether the supplied string is an identifier reference according to the RFC.
     *
     * An identifier is either prefix:identifier or just an identifier.
     *
     * The rules for prefix are the same as for identifier.
     */
    public static boolean isYangIdentifierReference(final String stringToCheck) {

        if (stringToCheck == null || stringToCheck.isEmpty()) {
            return false;
        }

        if (stringToCheck.charAt(0) == ':' || stringToCheck.charAt(stringToCheck.length() - 1) == ':') {
            return false;
        }

        if (stringToCheck.contains(":")) {
            final String[] split = stringToCheck.split(":");
            if (split.length != 2) {
                return false;
            }
            return isYangIdentifier(split[0]) && isYangIdentifier(split[1]);
        } else {
            return isYangIdentifier(stringToCheck);
        }
    }

    /**
     * Given a string containing white-space separated strings, extracts those. Typically used where an
     * argument allows for a "space"-separated list of data node names, such as the 'key' or 'unique'
     * statements.
     * <p>
     * The RFC is contradictory: In chapter 7.8.2 it says that leaf names are separated by "space character",
     * but the official grammar definition in chapter 14 allows any whitespace (and line break). We err
     * on the side of caution and align with the official grammar.
     */
    public static List<String> parseToStringList(final String value) {

        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        final char[] asChars = value.toCharArray();

        if (!containsWhitespace(asChars)) {
            return Collections.singletonList(value);
        }

        final List<String> result = new ArrayList<>();

        StringBuilder sb = null;

        for (final char c : asChars) {
            if (sb != null) {
                if (isWhitespace(c)) {						// end of a single string reached
                    result.add(sb.toString());
                    sb = null;
                } else {
                    sb.append(c);							// add to the single string
                }
            } else {
                if (!isWhitespace(c)) {						// new string starts
                    sb = new StringBuilder(20);
                    sb.append(c);
                }
            }
        }

        if (sb != null) {					// value did not end with whitespace (it rarely does), so add remainder
            result.add(sb.toString());
        }

        return result;
    }

    private static boolean containsWhitespace(final char[] chars) {
        for (final char c : chars) {
            if (isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWhitespace(final char c) {
        return (c == ' ' || c == '\t' || c == '\n');
    }

    /**
     * Returns whether the supplied string can be legally used in YANG unquoted.
     * <p>
     * In general, any string can be expressed quoted, but for readability this is
     * omitted where possible.
     * <p>
     * From the RFC:
     *
     * An unquoted string is any sequence of characters that does not contain any space,
     * tab, carriage return, or line feed characters, a single or double quote character,
     * a semicolon (";"), braces ("{" or "}"), or comment sequences (double-slash,
     * slash-star, star-slash)
     *
     * Note that any keyword can legally appear as an unquoted string. Within an unquoted
     * string, every character is preserved. Note that this means that the backslash
     * character does not have any special meaning in an unquoted string.
     */
    public static boolean isUnquotableString(final String valueToCheck) {

        if (valueToCheck.isEmpty()) {
            return false;	// correct - empty string must always be represented as "" in YANG.
        }

        if (valueToCheck.contains("//") || valueToCheck.contains("/*") || valueToCheck.contains("*/")) {
            return false;
        }

        for (final char c : valueToCheck.toCharArray()) {
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\'' || c == '"' || c == ';' || c == '{' || c == '}' || c == '+') {
                return false;
            }
        }

        return true;
    }
}
