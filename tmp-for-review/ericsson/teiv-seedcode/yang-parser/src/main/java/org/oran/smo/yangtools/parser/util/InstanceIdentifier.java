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
package org.oran.smo.yangtools.parser.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;

/**
 * Represents instance-identifiers as specified in RFC7950, clause 9.13
 *
 * @author Mark Hollmann
 */
public class InstanceIdentifier {

    /**
     * Given an II encoded as specified in RFC7950, clause 9.13, returns the II. More specifically,
     * each part of the II must have a prefix that is resolvable to a namespace given the supplied
     * prefix resolver.
     * <p/>
     * Note especially this bit from the RFC: "All node names in an instance-identifier value MUST
     * be qualified with explicit namespace prefixes, and these prefixes MUST be declared in the
     * XML namespace scope in the instance-identifier's XML element."
     * <p/>
     * Since the input is XML, the module name for each step will remain unresolved. Use method
     * resolveModuleOrNamespace() to resolve the module names for each step. Not doing so may cause
     * problems when comparing II objects at a later stage.
     * <p/>
     * Will throw a RuntimeException if the syntax of the supplied string is wrong.
     */
    public static InstanceIdentifier parseXmlEncodedString(final String input, final PrefixResolver prefixResolver) {
        return parseEncodedString(Objects.requireNonNull(input), Objects.requireNonNull(prefixResolver));
    }

    /**
     * Given an II encoded as specified in RFC7951, clause 6.11, returns the II.
     * <p/>
     * Note especially this bit from the RFC: "The leftmost (top-level) data node
     * name is always in the namespace-qualified form. Any subsequent data node
     * name is in the namespace-qualified form if the node is defined in a module
     * other than its parent node, and the simple form is used otherwise. This
     * rule also holds for node names appearing in predicates."
     * <p/>
     * Since the input is JSON, the module namespace for each step will remain unresolved. Use method
     * resolveModuleOrNamespace() to resolve the namespace for each step. Not doing so may cause
     * problems when comparing II objects at a later stage.
     * <p/>
     * Will throw a RuntimeException if the syntax of the supplied string is wrong.
     */
    public static InstanceIdentifier parseJsonEncodedString(final String input) {
        return parseEncodedString(Objects.requireNonNull(input), null);
    }

    /**
     * Parses the supplied input string into an instance-identifier. If a prefix resolver
     * is supplied, this method will assume that the source of the input was XML, and will
     * treat data node prefixes as XML prefixes - otherwise, data node prefixes are assumed
     * to be module names.
     * <p/>
     * Will throw a RuntimeException if the syntax of the supplied string is wrong.
     */
    public static InstanceIdentifier parseEncodedString(final String input, final PrefixResolver prefixResolver) {

        try {
            final List<IiXPathToken> tokens = tokenize(input);
            final List<Step> steps = extractSteps(tokens, prefixResolver);
            return new InstanceIdentifier(steps);
        } catch (final IndexOutOfBoundsException ioobex) {
            throw new RuntimeException(
                    "Syntax error in input string. Check for incorrect syntax towards the end of the XPath expression, and missing brackets / quotes.");
        }
    }

    private static List<Step> extractSteps(final List<IiXPathToken> tokens, final PrefixResolver prefixResolver) {

        final List<Step> steps = new ArrayList<>();

        /*
         * Each part of the path is in the form:
         *
         * <slash><optional-prefix-and-colon><data-node-name><optional-predicate(s)>
         */

        int index = 0;
        while (true) {

            if (index == tokens.size()) {		// Nothing left to consume, we are done.
                return steps;
            }

            if (tokens.get(index).type != IiXPathToken.Type.SLASH) {
                throw new RuntimeException("Expected a slash at pos " + tokens.get(index).pos);
            }

            index++;

            /*
             * We extract the data node identifier. This will create a new Part object.
             */
            index = extractStepDataNodeIdentifier(tokens, index, prefixResolver, steps);

            /*
             * Extract the predicates, if any.
             */
            index = extractPredicates(tokens, index, prefixResolver, steps);
        }
    }

    private static int extractStepDataNodeIdentifier(final List<IiXPathToken> tokens, final int index,
            final PrefixResolver prefixResolver, final List<Step> steps) {

        final NamespaceModuleIdentifier unprefixedName = getUnprefixedName(tokens, index, steps,
                STEP_DATA_NODE_ALLOWED_TERMINATING_TOKENS);
        final NamespaceModuleIdentifier prefixedName = unprefixedName != null ?
                null :
                getPrefixedName(tokens, index, prefixResolver, steps, STEP_DATA_NODE_ALLOWED_TERMINATING_TOKENS);

        if (unprefixedName == null && prefixedName == null) {
            throw new RuntimeException("Unresolvable data node name at pos " + tokens.get(index).pos);
        }

        steps.add(new Step(unprefixedName != null ? unprefixedName : prefixedName));

        return index + (unprefixedName != null ? 1 : 3);
    }

    private static int extractPredicates(final List<IiXPathToken> tokens, int index, final PrefixResolver prefixResolver,
            final List<Step> steps) {

        /*
         * A predicate may refer to:
         *
         * - A list entry, and the list is identified by one or more keys.
         *   Example: /ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']
         *
         * - A list entry, and the entry is identified by its position (since the list does not have keys):
         *   Example: /ex:stats/ex:port[3]
         *
         * - A leaf-list entry, identified by value (since configurable leaf-lists must be unique).
         *   Example: /ex:system/ex:services/ex:ssh/ex:cipher[.='blowfish-cbc']
         *
         * - A leaf-list entry, identified by position (since state leaf-lists may be non-unique).
         *   Example: /ex:stats/ex:cpuLoad[3]
         */

        if (index == tokens.size()) {		// end of tokens
            return index;
        }
        if (tokens.get(index).type != IiXPathToken.Type.SQUARE_BRACKET_OPEN) {		// there is no predicate
            return index;
        }

        final Step step = steps.get(steps.size() - 1);

        /*
         * Figure out the kind of predicate - keys, value or position
         */

        if (tokens.get(index + 1).type == IiXPathToken.Type.UNQUOTED_STRING && tokens.get(
                index + 2).type == IiXPathToken.Type.SQUARE_BRACKET_CLOSE) {
            /*
             * Position. The string must be parseable to an integer.
             *
             * NOTE that according to the XPath spec, position indexes start at 1, and not 0 as is customary in programming languages.
             */
            try {
                final int pos = Integer.parseInt(tokens.get(index + 1).value);
                if (pos < 1) {
                    throw new RuntimeException("Position '" + pos + "' is illegal. Position must be larger/equal to 1.");
                }
                step.setPredicateListEntryOrLeafListMemberIndex(pos);
                return index + 3;
            } catch (final NumberFormatException nfex) {
                throw new RuntimeException("Predicate '" + tokens.get(
                        index + 1).value + "' not parseable to a position value.");
            }
        }

        if (tokens.get(index + 1).type == IiXPathToken.Type.DOT && tokens.get(
                index + 2).type == IiXPathToken.Type.EQUALS && (tokens.get(
                        index + 3).type == IiXPathToken.Type.QUOTED_STRING || tokens.get(
                                index + 3).type == IiXPathToken.Type.UNQUOTED_STRING) && tokens.get(
                                        index + 4).type == IiXPathToken.Type.SQUARE_BRACKET_CLOSE) {
            /*
             * A value.
             */
            step.setPredicateLeafListMemberValue(tokens.get(index + 3).value);
            return index + 5;
        }

        /*
         * Must be one or more key values
         */
        while (true) {

            if (index == tokens.size()) {		// end of tokens
                return index;
            }
            if (tokens.get(index).type != IiXPathToken.Type.SQUARE_BRACKET_OPEN) {		// done with predicates
                return index;
            }

            index++;		// consume the [

            /*
             * The data node may be prefixed or not...
             */
            final NamespaceModuleIdentifier unprefixedName = getUnprefixedName(tokens, index, steps,
                    PREDICATE_DATA_NODE_ALLOWED_TERMINATING_TOKENS);
            final NamespaceModuleIdentifier prefixedName = unprefixedName != null ?
                    null :
                    getPrefixedName(tokens, index, prefixResolver, steps, PREDICATE_DATA_NODE_ALLOWED_TERMINATING_TOKENS);

            if (unprefixedName == null && prefixedName == null) {
                throw new RuntimeException("Unresolvable data node name at pos " + tokens.get(index).pos);
            }

            index += (unprefixedName != null ? 1 : 3);		// consume the data node name

            index++;		// consume the =    // note no need to check that the token is a =, as this is used as terminating token when extracting the data node name.

            /*
             * We are being lenient here. XPath spec says that LITERAL must be enclosed by single or double quotes.
             * We will allow unquoted strings as well, as a lot of people will get this wrong. For the instance-identifier
             * data type we can safely do this, as functions cannot be used inside an II.
             */
            if (tokens.get(index).type != IiXPathToken.Type.UNQUOTED_STRING && tokens.get(
                    index).type != IiXPathToken.Type.QUOTED_STRING) {
                throw new RuntimeException("Expected a single- or double-quoted string at pos " + tokens.get(index).pos);
            }

            final String stringefiedKeyValue = tokens.get(index).value;

            step.addPredicateKeyValue(unprefixedName != null ? unprefixedName : prefixedName, stringefiedKeyValue);

            index++;		// consume the value

            if (tokens.get(index).type != IiXPathToken.Type.SQUARE_BRACKET_CLOSE) {
                throw new RuntimeException("Expected a closing square brace ']' at pos " + tokens.get(index).pos);
            }

            index++;		// consume the ]
        }
    }

    private static final List<IiXPathToken.Type> STEP_DATA_NODE_ALLOWED_TERMINATING_TOKENS = Arrays.asList(
            IiXPathToken.Type.SQUARE_BRACKET_OPEN, IiXPathToken.Type.SLASH);
    private static final List<IiXPathToken.Type> PREDICATE_DATA_NODE_ALLOWED_TERMINATING_TOKENS = Arrays.asList(
            IiXPathToken.Type.EQUALS);

    private static NamespaceModuleIdentifier getUnprefixedName(final List<IiXPathToken> tokens, final int index,
            final List<Step> steps, final List<IiXPathToken.Type> allowedTerminatingToken) {

        /*
         * An unprefixed name simply has a name, followed by either nothing, or a predicate, or the next step.
         */
        final boolean unprefixedName = (tokens.get(
                index).type == IiXPathToken.Type.UNQUOTED_STRING) && ((index + 1 == tokens
                        .size()) || allowedTerminatingToken.contains(tokens.get(index + 1).type));
        if (!unprefixedName) {
            return null;
        }

        if (steps.isEmpty()) {
            throw new RuntimeException("The first step of the path must have a prefix / module-name.");
        }

        final String dataNodeIdentifier = tokens.get(index).value;

        /*
         * We need to take the module name/namespace from the parent data node, i.e. the preceding step.
         */
        final NamespaceModuleIdentifier parentDataNodeNsmi = steps.get(steps.size() - 1).getDataNodeNsai();
        return new NamespaceModuleIdentifier(parentDataNodeNsmi.getNamespace(), parentDataNodeNsmi.getModuleName(),
                dataNodeIdentifier);
    }

    private static NamespaceModuleIdentifier getPrefixedName(final List<IiXPathToken> tokens, final int index,
            final PrefixResolver prefixResolver, final List<Step> steps,
            final List<IiXPathToken.Type> allowedTerminatingToken) {

        /*
         * An prefixed name is in the form <string><colon><string>, followed by either nothing, or a predicate, or the next step.
         */
        final boolean prefixedName = (tokens.get(index).type == IiXPathToken.Type.UNQUOTED_STRING) && (tokens.get(
                index + 1).type == IiXPathToken.Type.COLON) && (tokens.get(
                        index + 2).type == IiXPathToken.Type.UNQUOTED_STRING) && ((index + 3 == tokens
                                .size()) || allowedTerminatingToken.contains(tokens.get(index + 3).type));
        if (!prefixedName) {
            return null;
        }

        final String prefixOrModuleName = tokens.get(index).value;
        final String dataNodeIdentifier = tokens.get(index + 2).value;

        /*
         * The prefix can be either a module name (input was JSON) or an actual XML prefix (input was XML).
         */
        final String moduleName = prefixResolver == null ? prefixOrModuleName : null;
        final String namespace = prefixResolver != null ? prefixResolver.resolveNamespaceUri(prefixOrModuleName) : null;

        if (moduleName == null && namespace == null) {
            throw new RuntimeException("Unresolvable prefix '" + prefixOrModuleName + "' at pos " + tokens.get(index).pos);
        }

        return new NamespaceModuleIdentifier(namespace, moduleName, dataNodeIdentifier);
    }

    /**
     * A step in an Instance Identifier. A step is identified by a data node (itself identified through a
     * module-name/namespace, and a data node identifier), and possibly a predicate.
     */
    public static class Step {

        private final NamespaceModuleIdentifier nsai;

        private Map<NamespaceModuleIdentifier, String> predicateKeyValues;

        private String predicateLeafListMemberValue;

        private Integer predicateListEntryOrLeafListMemberIndex;

        public Step(final NamespaceModuleIdentifier nsai) {
            this.nsai = Objects.requireNonNull(nsai);
        }

        /**
         * Returns the NSAI of the data node. Note that depending on the input, either the module name or
         * namespace may be null, unless resolved (see method resolveModuleOrNamespace() in InstanceIdentifier).
         */
        public NamespaceModuleIdentifier getDataNodeNsai() {
            return nsai;
        }

        public Step addPredicateKeyValue(final NamespaceModuleIdentifier nsai, final String value) {
            if (predicateLeafListMemberValue != null || predicateListEntryOrLeafListMemberIndex != null) {
                throw new RuntimeException(
                        "A step can only, at most, contain either: predicate(s), or leaf-list value, or list entry index.");
            }

            if (predicateKeyValues == null) {
                predicateKeyValues = new HashMap<>();
            }
            predicateKeyValues.put(Objects.requireNonNull(nsai), Objects.requireNonNull(value));
            return this;
        }

        /**
         * Returns the predicate key values. Each predicate is identified by the data node NSAI, and a value.
         * When comparing values, data conversion may have to be performed by a client. Note that depending
         * on the input, either the module name or namespace of the data node NSAI may be null, unless
         * resolved (see method resolveModuleOrNamespace() in InstanceIdentifier).
         * <p>
         * Returns null if there are no key values as predicate.
         */
        public Map<NamespaceModuleIdentifier, String> getPredicateKeyValues() {
            return predicateKeyValues;
        }

        public Step setPredicateLeafListMemberValue(final String leafListMemberValue) {
            if (predicateKeyValues != null || predicateListEntryOrLeafListMemberIndex != null) {
                throw new RuntimeException(
                        "A step can only, at most, contain either: predicate(s), or leaf-list value, or list entry index.");
            }

            this.predicateLeafListMemberValue = Objects.requireNonNull(leafListMemberValue);
            return this;
        }

        /**
         * Returns the value, if any, of a leaf-list member tested for as part of the predicate. When comparing
         * values, data conversion may have to be performed by a client.
         */
        public String getPredicateLeafListMemberValue() {
            return predicateLeafListMemberValue;
        }

        public Step setPredicateListEntryOrLeafListMemberIndex(final int listEntryOrLeafListMemberIndex) {
            if (predicateLeafListMemberValue != null || predicateKeyValues != null) {
                throw new RuntimeException(
                        "A step can only, at most, contain either: predicate(s), or leaf-list value, or list entry index.");
            }

            if (listEntryOrLeafListMemberIndex < 1) {
                throw new RuntimeException(
                        "The index value must be >= 1 (in XPath, the index of the first element is 1, not 0).");
            }

            this.predicateListEntryOrLeafListMemberIndex = listEntryOrLeafListMemberIndex;
            return this;
        }

        /**
         * Returns the index, if any, of the list entry or leaf-list entry. Returns null if there is no
         * index defined as part of the predicate.
         * <p/>
         * <b>NOTE that according to the XPath spec position indexes start at 1, and not 0 as is customary
         * in programming languages.</b> When testing against arrays or lists in Java, the returned value
         * must therefore be reduced by one.
         */
        public Integer getPredicateListEntryOrLeafListMemberIndex() {
            return predicateListEntryOrLeafListMemberIndex;
        }

        public void resolveModuleOrNamespace(final ModuleAndNamespaceResolver namespaceResolver) {
            nsai.resolveModuleOrNamespace(namespaceResolver);
            if (predicateKeyValues != null) {
                predicateKeyValues.keySet().forEach(k -> k.resolveModuleOrNamespace(namespaceResolver));
            }
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Step)) {
                return false;
            }

            final Step other = (Step) obj;

            if (!this.nsai.equals(other.nsai)) {
                return false;
            }

            if (!Objects.equals(this.predicateKeyValues, other.predicateKeyValues)) {
                return false;
            }

            if (!Objects.equals(this.predicateLeafListMemberValue, other.predicateLeafListMemberValue)) {
                return false;
            }

            return Objects.equals(this.predicateListEntryOrLeafListMemberIndex,
                    other.predicateListEntryOrLeafListMemberIndex);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("step node=").append(nsai);
            if (predicateKeyValues != null) {
                sb.append("; keys/values=").append(predicateKeyValues);
            }
            if (predicateLeafListMemberValue != null) {
                sb.append("; value=").append(predicateLeafListMemberValue);
            }
            if (predicateListEntryOrLeafListMemberIndex != null) {
                sb.append("; index=").append(predicateListEntryOrLeafListMemberIndex);
            }
            return sb.toString();
        }
    }

    private static List<IiXPathToken> tokenize(final String s) {

        final List<IiXPathToken> result = new ArrayList<>(50);

        int charPos = 0;
        char c;
        while (charPos < s.length()) {

            c = s.charAt(charPos);

            switch (c) {
                case '/':
                    result.add(IiXPathToken.newSlash(charPos));
                    break;
                case ':':
                    result.add(IiXPathToken.newColon(charPos));
                    break;
                case '[':
                    result.add(IiXPathToken.newSquareBracketOpen(charPos));
                    break;
                case ']':
                    result.add(IiXPathToken.newSquareBracketClose(charPos));
                    break;
                case '=':
                    result.add(IiXPathToken.newEquals(charPos));
                    break;
                case '.':
                    result.add(IiXPathToken.newDot(charPos));
                    break;
                case ' ':
                case '\t':
                    /*
                     * We really don't expect whitespaces in the II, but according to XPath spec these are actually allowed.
                     */
                    break;
                default:
                    charPos = tokenizeExtractString(s, charPos, result);
                    break;
            }

            charPos++;
        }

        return result;
    }

    /**
     * Returns the last character consumed.
     */
    private static int tokenizeExtractString(final String s, int charPos, final List<IiXPathToken> result) {

        /*
         * The production for LITERAL is quite clear in the XPath spec - it is either single-quoted-enclosed text,
         * or double-quoted-enclosed text. No escaping is applied/allowed within the LITERAL.
         *
         * This method also extracts QNames, so we also allow unquoted strings. Different tokens are produced thus.
         */

        final boolean stringIsSingleQuoted = (s.charAt(charPos) == '\'');
        final boolean stringIsDoubleQuoted = (s.charAt(charPos) == '"');

        if (stringIsSingleQuoted || stringIsDoubleQuoted) {
            charPos++;
        }

        char c;
        final StringBuilder sb = new StringBuilder();

        while (charPos < s.length()) {
            c = s.charAt(charPos);

            switch (c) {
                case '\'':
                    if (stringIsSingleQuoted) {									// Done. Swallow the trailing ' character
                        result.add(IiXPathToken.newQuotedStringToken(charPos, sb.toString()));
                        return charPos;
                    }
                    if (stringIsDoubleQuoted) {		// OK - single quote in double-quoted string.
                        sb.append('\'');
                    } else {
                        throw new RuntimeException("Single quote character may not appear at position " + charPos);
                    }
                    break;
                case '"':
                    if (stringIsDoubleQuoted) {		// Done. Swallow the trailing " character
                        result.add(IiXPathToken.newQuotedStringToken(charPos, sb.toString()));
                        return charPos;
                    }
                    if (stringIsSingleQuoted) {		// OK - double quote in single-quoted string.
                        sb.append('"');
                    } else {
                        throw new RuntimeException("Double quote character may not appear at position " + charPos);
                    }
                    break;
                case ' ':
                case '\t':
                case '/':
                case ':':
                case '[':
                case ']':
                case '=':
                case '.':
                    if (stringIsSingleQuoted || stringIsDoubleQuoted) {
                        sb.append(c);
                    } else {
                        /*
                         * We are done here.
                         */
                        result.add(IiXPathToken.newUnquotedStringToken(charPos, sb.toString()));
                        return charPos - 1;
                    }
                    break;
                default:
                    sb.append(c);
                    break;
            }

            charPos++;
        }

        if (stringIsSingleQuoted || stringIsDoubleQuoted) {
            throw new RuntimeException("Single/double-quoted string not correctly terminated.");
        }

        result.add(IiXPathToken.newUnquotedStringToken(charPos, sb.toString()));

        return charPos;
    }

    private static class IiXPathToken {

        private static IiXPathToken newSlash(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.SLASH;
            token.pos = pos;
            token.value = "/";
            return token;
        }

        private static IiXPathToken newColon(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.COLON;
            token.pos = pos;
            token.value = ":";
            return token;
        }

        private static IiXPathToken newSquareBracketOpen(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.SQUARE_BRACKET_OPEN;
            token.pos = pos;
            token.value = "[";
            return token;
        }

        private static IiXPathToken newSquareBracketClose(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.SQUARE_BRACKET_CLOSE;
            token.pos = pos;
            token.value = "]";
            return token;
        }

        private static IiXPathToken newEquals(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.EQUALS;
            token.pos = pos;
            token.value = "=";
            return token;
        }

        private static IiXPathToken newDot(final int pos) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.DOT;
            token.pos = pos;
            token.value = ".";
            return token;
        }

        private static IiXPathToken newQuotedStringToken(final int pos, final String str) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.QUOTED_STRING;
            token.pos = pos;
            token.value = str;
            return token;
        }

        private static IiXPathToken newUnquotedStringToken(final int pos, final String str) {
            final IiXPathToken token = new IiXPathToken();
            token.type = Type.UNQUOTED_STRING;
            token.pos = pos;
            token.value = str;
            return token;
        }

        private Type type = null;
        private int pos = 0;
        private String value = null;

        private IiXPathToken() {
        }

        private enum Type {
            SLASH,
            COLON,
            SQUARE_BRACKET_OPEN,
            SQUARE_BRACKET_CLOSE,
            EQUALS,
            DOT,
            QUOTED_STRING,
            UNQUOTED_STRING;
        }
    }

    private final List<Step> steps;

    public InstanceIdentifier(final List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getSteps() {
        return steps;
    }

    /**
     * Depending on whether prefixes or module names were used as part of the II, the respective
     * other may not have been resolved. Use this method to perform the resolution.
     */
    public void resolveModuleOrNamespace(final ModuleAndNamespaceResolver namespaceResolver) {
        steps.forEach(s -> s.resolveModuleOrNamespace(namespaceResolver));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InstanceIdentifier)) {
            return false;
        }

        final InstanceIdentifier other = (InstanceIdentifier) obj;
        return this.steps.equals(other.steps);
    }

    @Override
    public String toString() {
        return "II " + steps.toString();
    }
}
