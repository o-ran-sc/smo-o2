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
package org.oran.smo.yangtools.parser.model.statements.yang.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement.StatementArgumentType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature.Token;
import org.oran.smo.yangtools.parser.model.statements.yang.YIfFeature.Type;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;

public class IfFeatureTest extends YangTestCommon {

    @Test
    public void testIfFeature() {

        parseRelativeImplementsYangModels(Arrays.asList("if-feature-test/if-feature-test-module.yang"));

        final YModule module = getModule("if-feature-test-module");
        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);
        assertTrue(cont1.getIfFeatures().size() == 1);
        assertTrue(cont1.getIfFeatures().get(0).getValue().equals("feature1"));
        assertNoFindingsOnStatement(cont1);

        final YIfFeature yIfFeature = cont1.getIfFeatures().get(0);
        assertEquals(StatementArgumentType.NAME, yIfFeature.getArgumentType());
        assertEquals(CY.STMT_IF_FEATURE, yIfFeature.getStatementModuleAndName());

        assertFalse(yIfFeature.areTokensValid(context, null));
        assertFalse(yIfFeature.areTokensValid(context, Collections.emptyList()));
        assertFalse(yIfFeature.areTokensValid(context, Arrays.asList(new YIfFeature.Token(Type.RIGHT_PARENTHESIS, ")"))));
        assertFalse(yIfFeature.areTokensValid(context, Arrays.asList(new YIfFeature.Token(Type.LEFT_PARENTHESIS, "("))));
        assertFalse(yIfFeature.areTokensValid(context, Arrays.asList(new YIfFeature.Token(Type.NOT, "not"))));
        assertFalse(yIfFeature.areTokensValid(context, Arrays.asList(new YIfFeature.Token(Type.LEFT_PARENTHESIS, "("),
                new YIfFeature.Token(Type.RIGHT_PARENTHESIS, ")"))));
        assertNoFindingsOnStatement(yIfFeature);

        assertTrue(getLeaf(cont1, "leaf1") != null);
        assertTrue(getLeaf(cont1, "leaf1").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf1").getIfFeatures().get(0).getValue().equals("feature2"));
        assertNoFindingsOnStatement(getLeaf(cont1, "leaf1"));

        assertTrue(getLeaf(cont1, "leaf2") != null);
        assertTrue(getLeaf(cont1, "leaf2").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getIfFeatures().get(0).getValue().equals("feature1 or feature2"));
        assertNoFindingsOnStatement(getLeaf(cont1, "leaf2"));

        assertTrue(getLeaf(cont1, "leaf3") != null);
        assertTrue(getLeaf(cont1, "leaf3").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf3").getIfFeatures().get(0).getValue().equals(
                "feature1 and(feature2 or				not feature3)"));
        assertNoFindingsOnStatement(getLeaf(cont1, "leaf3"));

        assertTrue(getLeaf(cont1, "leaf4") != null);
        assertTrue(getLeaf(cont1, "leaf4").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf4").getIfFeatures().get(0).getValue().equals(
                "(feature1 or feature2)and feature3 or feature1"));
        assertNoFindingsOnStatement(getLeaf(cont1, "leaf4"));

        final List<Token> leaf4tokens = getLeaf(cont1, "leaf4").getIfFeatures().get(0).getTokens();
        assertTrue(leaf4tokens.size() == 9);
        assertTrue(leaf4tokens.get(0).type == Type.LEFT_PARENTHESIS);
        assertTrue(leaf4tokens.get(1).type == Type.FEATURE_NAME);
        assertTrue(leaf4tokens.get(1).name.equals("feature1"));
        assertTrue(leaf4tokens.get(2).type == Type.OR);
        assertTrue(leaf4tokens.get(3).type == Type.FEATURE_NAME);
        assertTrue(leaf4tokens.get(3).name.equals("feature2"));
        assertTrue(leaf4tokens.get(4).type == Type.RIGHT_PARENTHESIS);
        assertTrue(leaf4tokens.get(5).type == Type.AND);
        assertTrue(leaf4tokens.get(6).type == Type.FEATURE_NAME);
        assertTrue(leaf4tokens.get(6).name.equals("feature3"));
        assertTrue(leaf4tokens.get(7).type == Type.OR);
        assertTrue(leaf4tokens.get(8).type == Type.FEATURE_NAME);
        assertTrue(leaf4tokens.get(8).name.equals("feature1"));

        assertTrue(getLeaf(cont1, "leaf5") != null);
        assertTrue(getLeaf(cont1, "leaf5").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf5").getIfFeatures().get(0).getValue().equals("(feature1"));
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf5").getIfFeatures().get(0),
                ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());

        assertTrue(getLeaf(cont1, "leaf6") != null);
        assertTrue(getLeaf(cont1, "leaf6").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf6").getIfFeatures().get(0).getValue().equals("feature1 or"));
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf6").getIfFeatures().get(0),
                ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());

        assertTrue(getLeaf(cont1, "leaf7") != null);
        assertTrue(getLeaf(cont1, "leaf7").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf7").getIfFeatures().get(0).getValue().equals(
                "(feature1 or feature2)) or feature3 ("));
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf7").getIfFeatures().get(0),
                ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());

        assertTrue(getLeaf(cont1, "leaf8") != null);
        assertTrue(getLeaf(cont1, "leaf8").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf8").getIfFeatures().get(0).getValue().equals("()"));
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf8").getIfFeatures().get(0),
                ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());

        assertTrue(getLeaf(cont1, "leaf9") != null);
        assertTrue(getLeaf(cont1, "leaf9").getIfFeatures().size() == 1);
        assertTrue(getLeaf(cont1, "leaf9").getIfFeatures().get(0).getValue() == null);
        assertStatementHasFindingOfType(getLeaf(cont1, "leaf9").getIfFeatures().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        // - - - - - -

        assertEquals(0, YIfFeature.parseIfFeatureValueIntoTokens(null).size());

    }
}
