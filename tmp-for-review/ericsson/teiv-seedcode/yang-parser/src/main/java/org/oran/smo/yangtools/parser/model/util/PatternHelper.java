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

public abstract class PatternHelper {

    /**
     * Does a basic translation of a YANG REGEX (which is of XML schema flavour) to a Java REGEX flavour.
     * Does not handle category escape \p{X} or block escape \p{Is}
     * <p/>
     * See annex F of https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/
     */
    public static String toJavaPatternString(final String input) {

        /*
         * Notable differences between XSD REGEX syntax and Java REGEX syntax:
         *
         * The '^' and '$' are not used as head/tail anchors, so are interpreted as literals. But the ^ is
         * used as negation in character classes.
         *
         * In XSD REGEX, meta-characters are either . \ ? * + { } ( ) [ ]
         *
         * All of these must be escaped with the backslash \ to use these as literals. The following must
         * also be escaped to arrive at a literal:
         *
         * \n \r \t \\ \| \- \^
         *
         * Note that . refers to any character BUT NOT \n \r - this is markedly different from Java.
         */

        String result = cleanDollar(input);
        result = cleanRoof(result);

        /*
         * We are not handling all of the category escape \p{X} or block escape \p{Is} - this is quite complex
         * and so far these have never been seen in Yang 'pattern' statement.
         */

        return result;
    }

    protected static String cleanDollar(final String input) {

        if (!input.contains("$")) {
            return input;
        }

        /*
         * The $ character has no special meaning in XSD REGEX syntax. It is a literal. It should never be encountered
         * in escaped form in YANG.
         *
         * In Java, it denotes line-end - so we need to escape any $ character that we find to make it a literal.
         */

        return input.replace("$", "\\$");
    }

    protected static String cleanRoof(final String input) {

        if (!input.contains("^")) {
            return input;
        }

        /*
         * The ^ character has special meaning in XSD REGEX syntax only inside character classes, for example:
         *
         * [^a-f]
         *
         * In all other cases, it is a literal.
         *
         * In Java, it is also used inside character classes, but is also used to denote line-start. So if we
         * encounter the ^ character we escape it (unless at the start of a character class).
         */

        final StringBuilder sb = new StringBuilder(input.length());

        for (int i = 0, len = input.length(); i < len; ++i) {
            final char c = input.charAt(i);
            if (c == '^' && (i == 0 || (i > 0 && input.charAt(i - 1) != '['))) {
                sb.append("\\^");
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
