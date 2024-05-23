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
package org.oran.smo.teiv.utils.path;

import org.oran.smo.teiv.antlr4.tiesPathBaseListener;
import org.oran.smo.teiv.antlr4.tiesPathParser;
import org.oran.smo.teiv.utils.path.exception.PathParsingException;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.RuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TiesPathBuilder extends tiesPathBaseListener {

    private static final String OPEN_BRACKET = "[";

    private static final String CLOSE_BRACKET = "]";

    private final TiesPathQuery tiesPathQuery = new TiesPathQuery();

    private final List<TiesPathQuery.DataLeaf> leavesData = new ArrayList<>();

    private final StringBuilder normalizedTiesPathBuilder = new StringBuilder();

    private final List<String> containerNames = new ArrayList<>();

    private final List<String> booleanOperators = new ArrayList<>();

    private final List<String> comparativeOperators = new ArrayList<>();

    private List<String> attributeNames = new ArrayList<>();

    TiesPathQuery build() {
        tiesPathQuery.setNormalizedXpath(normalizedTiesPathBuilder.toString());
        tiesPathQuery.setContainerNames(containerNames);
        tiesPathQuery.setBooleanOperators(booleanOperators);
        tiesPathQuery.setComparativeOperators(comparativeOperators);
        tiesPathQuery.setAttributeNames(attributeNames);
        return tiesPathQuery;
    }

    @SneakyThrows
    @Override
    public void exitInvalidPostFix(final tiesPathParser.InvalidPostFixContext ctx) {
        throw new PathParsingException(ctx.getText());
    }

    @Override
    public void exitParent(final tiesPathParser.ParentContext ctx) {
        tiesPathQuery.setNormalizedParentPath(normalizedTiesPathBuilder.toString());
    }

    @SneakyThrows
    @Override
    public void exitIncorrectPrefix(final tiesPathParser.IncorrectPrefixContext ctx) {
        throw new PathParsingException("Path can only start with one slash (/)");
    }

    @SneakyThrows
    @Override
    public void exitLeafCondition(final tiesPathParser.LeafConditionContext ctx) {
        final Object comparisonValue;
        if (ctx.IntegerLiteral() != null) {
            comparisonValue = Integer.valueOf(ctx.IntegerLiteral().getText());
        } else if (ctx.StringLiteral() != null) {
            comparisonValue = unwrapQuotedString(ctx.StringLiteral().getText());
        } else {
            throw new PathParsingException("Unsupported comparison value encountered in expression" + ctx.getText());
        }
        leafContext(ctx.leafName(), comparisonValue);
    }

    @Override
    public void exitBooleanOperators(final tiesPathParser.BooleanOperatorsContext ctx) {
        if (ctx.getText().equals("or"))
            throw new PathParsingException("Boolean operator 'or' is not supported, at " + ctx.getStart()
                    .getCharPositionInLine());
        booleanOperators.add(ctx.getText());
    }

    @Override
    public void exitComparativeOperators(final tiesPathParser.ComparativeOperatorsContext ctx) {
        if (!ctx.getText().equals("="))
            throw new PathParsingException(String.format("Comparative operator '%s' is not supported, at %d", ctx.getText(),
                    ctx.getStart().getCharPositionInLine()));
        comparativeOperators.add(ctx.getText());
    }

    @Override
    public void enterMultipleLeafConditions(final tiesPathParser.MultipleLeafConditionsContext ctx) {
        normalizedTiesPathBuilder.append(OPEN_BRACKET);
        leavesData.clear();
        booleanOperators.clear();
        comparativeOperators.clear();
    }

    @Override
    public void exitMultipleLeafConditions(final tiesPathParser.MultipleLeafConditionsContext ctx) {
        normalizedTiesPathBuilder.append(CLOSE_BRACKET);
        tiesPathQuery.setLeavesData(leavesData);
    }

    @Override
    public void exitContainsFunctionCondition(final tiesPathParser.ContainsFunctionConditionContext ctx) {
        tiesPathQuery.setContainsFunctionConditionLeafName(ctx.leafName().getText());
        tiesPathQuery.setContainsFunctionConditionValue(unwrapQuotedString(ctx.StringLiteral().getText()));
    }

    @Override
    public void enterListElementRef(final tiesPathParser.ListElementRefContext ctx) {
        normalizedTiesPathBuilder.append(OPEN_BRACKET);
    }

    @Override
    public void exitListElementRef(final tiesPathParser.ListElementRefContext ctx) {
        normalizedTiesPathBuilder.append(CLOSE_BRACKET);
    }

    @Override
    public void exitContainerName(final tiesPathParser.ContainerNameContext ctx) {
        final String containerName = ctx.getText();
        normalizedTiesPathBuilder.append("/").append(containerName);
        containerNames.add(containerName);
    }

    @Override
    public void exitFieldLeaf(final tiesPathParser.FieldLeafContext ctx) {
        attributeNames = ctx.leafName().stream().map(RuleContext::getText).collect(Collectors.toList());
    }

    private void leafContext(final tiesPathParser.LeafNameContext ctx, final Object comparisonValue) {
        leavesData.add(new TiesPathQuery.DataLeaf(ctx.getText(), comparisonValue));
        appendCondition(normalizedTiesPathBuilder, ctx.getText(), comparisonValue);
    }

    private void appendCondition(final StringBuilder currentNormalizedPathBuilder, final String name, final Object value) {
        final char lastCharacter = currentNormalizedPathBuilder.charAt(currentNormalizedPathBuilder.length() - 1);
        final boolean isStartOfExpression = lastCharacter == '[';
        if (!isStartOfExpression) {
            currentNormalizedPathBuilder.append(" ").append(getLastElement(booleanOperators)).append(" ");
        }
        currentNormalizedPathBuilder.append("@").append(name).append(getLastElement(comparativeOperators)).append("'")
                .append(value.toString().replace("'", "''")).append("'");
    }

    private static String getLastElement(final List<String> listOfStrings) {
        return listOfStrings.get(listOfStrings.size() - 1);
    }

    private static String unwrapQuotedString(final String wrappedString) {
        final boolean wasWrappedInSingleQuote = wrappedString.startsWith("'");
        final String value = stripFirstAndLastCharacter(wrappedString);
        if (wasWrappedInSingleQuote) {
            return value.replace("''", "'");
        } else {
            return value.replace("\"\"", "\"");
        }
    }

    private static String stripFirstAndLastCharacter(final String wrappedString) {
        return wrappedString.substring(1, wrappedString.length() - 1);
    }
}
