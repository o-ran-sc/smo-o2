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
package org.oran.smo.yangtools.parser.model.statements.yang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.SimpleStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Type-safe Yang core statement.
 *
 * @author Mark Hollmann
 */
public class YIfFeature extends SimpleStatement {

    public YIfFeature(final AbstractStatement parentStatement, final YangDomElement domNode) {
        super(parentStatement, domNode);
    }

    @Override
    public StatementArgumentType getArgumentType() {
        return StatementArgumentType.NAME;
    }

    @Override
    public StatementModuleAndName getStatementModuleAndName() {
        return CY.STMT_IF_FEATURE;
    }

    protected void validate(final ParserExecutionContext context) {
        if (!validateArgumentNotNullNotEmpty(context)) {
            /* no point trying to perform more validation */
            return;
        }

        /*
         * The 'if-feature' is either simply the (possibly prefixed) name of a feature, or it has special syntax, which the RFC defines as:
         *
         * if-feature-expr = if-feature-term [sep or-keyword sep if-feature-expr]
         * if-feature-term = if-feature-factor [sep and-keyword sep if-feature-term]
         * if-feature-factor = not-keyword sep if-feature-factor / "(" optsep if-feature-expr optsep ")" / identifier-ref-arg
         *
         * (This actually looks wrong - basically, the (possibly prefixed) names of features, with 'and' 'or' 'not' thrown in, possibly in parenthesis)
         */
        final List<Token> tokens = parseIfFeatureValueIntoTokens(getValue());
        validateTokens(context, tokens, true);
    }

    public boolean areTokensValid(final ParserExecutionContext context, final List<Token> tokens) {
        return validateTokens(context, tokens, false);
    }

    private boolean validateTokens(final ParserExecutionContext context, final List<Token> tokens,
            final boolean issueFindings) {

        if (tokens == null || tokens.isEmpty()) {
            if (issueFindings) {
                context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                        "Missing feature name."));
            }
            return false;
        }

        /*
         * Check the tokens - usually only a single token will exist (old Yang 1.0 syntax), but we never know...
         */
        int parenthesisOpenCount = 0;
        int featureNameCount = 0;

        Type previousTokenType = null;

        for (final Token token : tokens) {

            if (token.type == Type.LEFT_PARENTHESIS) {
                parenthesisOpenCount++;
            } else if (token.type == Type.RIGHT_PARENTHESIS) {
                parenthesisOpenCount--;
                if (parenthesisOpenCount < 0) {
                    if (issueFindings) {
                        context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                                "Unexpected closing parenthesis."));
                    }
                    return false;
                }
            } else if (token.type == Type.FEATURE_NAME) {
                featureNameCount++;
            }

            if (previousTokenType == null) {
                checkToken(context, token, VALID_TOKENS_AT_START, issueFindings);
            } else if (previousTokenType == Type.FEATURE_NAME) {
                checkToken(context, token, VALID_TOKENS_AFTER_FEATURE_NAME, issueFindings);
            } else if (previousTokenType == Type.AND || previousTokenType == Type.OR) {
                checkToken(context, token, VALID_TOKENS_AFTER_AND_OR, issueFindings);
            } else if (previousTokenType == Type.NOT) {
                checkToken(context, token, VALID_TOKENS_AFTER_NOT, issueFindings);
            } else if (previousTokenType == Type.LEFT_PARENTHESIS) {
                checkToken(context, token, VALID_TOKENS_AFTER_LEFT_PARENTHESIS, issueFindings);
            } else { // (previousTokenType == Type.RIGHT_PARENTHESIS) {
                checkToken(context, token, VALID_TOKENS_AFTER_RIGHT_PARENTHESIS, issueFindings);
            }

            previousTokenType = token.type;
        }

        /*
         * Last token must be either a feature-name or a right parenthesis.
         */
        final Token lastToken = tokens.get(tokens.size() - 1);
        if (lastToken.type != Type.FEATURE_NAME && lastToken.type != Type.RIGHT_PARENTHESIS) {
            if (issueFindings) {
                context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                        "Unexpected end of if-feature expression."));
            }
            return false;
        }

        /*
         * There must be at least one feature, and opening/closing parenthesis must be balanced.
         */
        if (featureNameCount == 0) {
            if (issueFindings) {
                context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                        "Missing feature name."));
            }
            return false;
        }
        if (parenthesisOpenCount != 0) {
            if (issueFindings) {
                context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                        "Parenthesis not balanced."));
            }
            return false;
        }

        return true;
    }

    private static final List<Type> VALID_TOKENS_AT_START = Arrays.asList(Type.FEATURE_NAME, Type.LEFT_PARENTHESIS,
            Type.NOT);
    private static final List<Type> VALID_TOKENS_AFTER_FEATURE_NAME = Arrays.asList(Type.AND, Type.OR,
            Type.RIGHT_PARENTHESIS);
    private static final List<Type> VALID_TOKENS_AFTER_AND_OR = Arrays.asList(Type.FEATURE_NAME, Type.NOT,
            Type.LEFT_PARENTHESIS);
    private static final List<Type> VALID_TOKENS_AFTER_NOT = Arrays.asList(Type.FEATURE_NAME, Type.LEFT_PARENTHESIS);
    private static final List<Type> VALID_TOKENS_AFTER_LEFT_PARENTHESIS = Arrays.asList(Type.FEATURE_NAME, Type.NOT,
            Type.LEFT_PARENTHESIS);
    private static final List<Type> VALID_TOKENS_AFTER_RIGHT_PARENTHESIS = Arrays.asList(Type.AND, Type.OR,
            Type.RIGHT_PARENTHESIS);

    private void checkToken(final ParserExecutionContext context, final Token token, final List<Type> mustBeOfType,
            final boolean issueFindings) {
        if (!mustBeOfType.contains(token.type) && issueFindings) {
            context.addFinding(new Finding(this, ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString(),
                    "Illegal syntax '" + token.name + "'."));
        }
    }

    public List<Token> getTokens() {
        return parseIfFeatureValueIntoTokens(this.getValue());
    }

    public static List<Token> parseIfFeatureValueIntoTokens(final String input) {

        if (input == null) {
            return Collections.<Token> emptyList();
        }

        boolean inString = false;
        StringBuilder sb = null;
        final List<Token> nodes = new ArrayList<>();

        for (final char c : input.toCharArray()) {

            if (inString) {
                if (c == ' ' || c == '\t' || c == '\n' || c == '(' || c == ')') {
                    // String is over
                    inString = false;
                    nodes.add(tokenFromString(sb.toString()));
                    if (c == '(') {
                        nodes.add(new Token(Type.LEFT_PARENTHESIS, "("));
                    } else if (c == ')') {
                        nodes.add(new Token(Type.RIGHT_PARENTHESIS, ")"));
                    }
                } else {
                    // still in string, so add to it.
                    sb.append(c);
                }
            } else {
                // currently not in string

                if (c == ' ' || c == '\t' || c == '\n') {
                    // Have encountered whitespace, ignore.
                } else if (c == '(') {
                    nodes.add(new Token(Type.LEFT_PARENTHESIS, "("));
                } else if (c == ')') {
                    nodes.add(new Token(Type.RIGHT_PARENTHESIS, ")"));
                } else {
                    // some other character, so start of some string
                    inString = true;
                    sb = new StringBuilder(100);
                    sb.append(c);
                }
            }
        }

        if (inString) {
            nodes.add(tokenFromString(sb.toString()));
        }

        return nodes;
    }

    public enum Type {
        FEATURE_NAME,
        AND,
        OR,
        NOT,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS
    }

    public static class Token {
        public final Type type;
        public final String name;

        public Token(final Type type, final String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Token && (((Token) obj).type == this.type) && (((Token) obj).name.equals(this.name));
        }
    }

    private static Token tokenFromString(final String string) {
        switch (string) {
            case "and":
                return new Token(Type.AND, "and");
            case "or":
                return new Token(Type.OR, "or");
            case "not":
                return new Token(Type.NOT, "not");
            default:
        }
        return new Token(Type.FEATURE_NAME, string);
    }
}
