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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class StringTokenizationTest extends YangTestCommon {

    @Test
    public void testYang1StringTokenization() {

        severityCalculator.suppressFinding(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString());

        parseRelativeImplementsYangModels(Arrays.asList(
                "string-tokenization-test/string-tokenization-yang1-test-module.yang"));

        final YangModel yangModelFile = yangDeviceModel.getModuleRegistry().byModuleName(
                "string-tokenization-yang1-test-module").get(0);

        assertTrue(yangModelFile != null);
        assertTrue(yangModelFile.getYangModelRoot().getYangVersion().equals("1"));

        final YModule module = getModule("string-tokenization-yang1-test-module");

        assertTrue(getContainer(module, "cont1") != null);
        assertTrue(getContainer(module, "cont1").getDescription().getValue().equals("valid YANG 1 double-quoted string"));

        assertTrue(getContainer(module, "cont2") != null);
        assertTrue(getContainer(module, "cont2").getDescription().getValue().equals(
                "valid YANG 1 double-quoted string with special characters \n \t \" \\ \\X \\_"));

        assertTrue(getContainer(module, "cont3") != null);
        assertTrue(getContainer(module, "cont3").getDescription().getValue().equals("valid YANG 1 double-quoted string"));

        assertTrue(getContainer(module, "cont4") != null);
        assertTrue(getContainer(module, "cont4").getDescription().getValue().equals("valid YANG 1 \ndouble-quoted string"));

        assertTrue(getContainer(module, "cont5") != null);
        assertTrue(getContainer(module, "cont5").getDescription().getValue().equals(""));

        assertTrue(getContainer(module, "cont6") != null);
        assertTrue(getContainer(module, "cont6").getDescription().getValue().equals(
                "invalid YANG 1 double-quoted string because of trailing backslash"));

        assertTrue(getContainer(module, "cont11") != null);
        assertTrue(getContainer(module, "cont11").getDescription().getValue().equals("valid YANG 1 single-quoted string"));

        assertTrue(getContainer(module, "cont12") != null);
        assertTrue(getContainer(module, "cont12").getDescription().getValue().equals(
                "valid YANG 1 single-quoted string \" \\n \\t \\\\ \\X"));

        assertTrue(getContainer(module, "cont13") != null);
        assertTrue(getContainer(module, "cont13").getDescription().getValue().equals("valid YANG 1\nsingle-quoted string"));

        assertTrue(getContainer(module, "cont14") != null);
        assertTrue(getContainer(module, "cont14").getDescription().getValue().equals(
                "valid YANG 1\n                           single-quoted string"));

        assertTrue(getContainer(module, "cont21") != null);
        assertTrue(getContainer(module, "cont21").getDescription().getValue().equals("valid_YANG_1_unquoted_string"));

        assertTrue(getContainer(module, "cont22") != null);
        assertTrue(getContainer(module, "cont22").getDescription().getValue().equals(
                "valid_YANG_1_unquoted_string_with_a_quote_at_the_end\""));

        assertFindingCount(1);
        assertHasFindingOfType(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertHasFinding(yangModelFile, 37, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
    }

    @Test
    public void testYang1dot1StringTokenization() {

        severityCalculator.suppressFinding(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString());

        parseRelativeImplementsYangModels(Arrays.asList(
                "string-tokenization-test/string-tokenization-yang1.1-test-module.yang"));

        final YangModel yangModelFile = yangDeviceModel.getModuleRegistry().byModuleName(
                "string-tokenization-yang1.1-test-module").get(0);

        assertTrue(yangModelFile != null);
        assertTrue(yangModelFile.getYangModelRoot().getYangVersion().equals("1.1"));

        final YModule module = getModule("string-tokenization-yang1.1-test-module");

        assertTrue(getContainer(module, "cont1") != null);
        assertTrue(getContainer(module, "cont1").getDescription().getValue().equals("valid YANG 1.1 double-quoted string"));

        assertTrue(getContainer(module, "cont2") != null);
        assertTrue(getContainer(module, "cont2").getDescription().getValue().equals(
                "invalid YANG 1.1 double-quoted string with special characters \n \t \" \\ \\X \\_"));

        assertTrue(getContainer(module, "cont3") != null);
        assertTrue(getContainer(module, "cont3").getDescription().getValue().equals("valid YANG 1.1 double-quoted string"));

        assertTrue(getContainer(module, "cont4") != null);
        assertTrue(getContainer(module, "cont4").getDescription().getValue().equals(
                "valid YANG 1.1 \ndouble-quoted string"));

        assertTrue(getContainer(module, "cont5") != null);
        assertTrue(getContainer(module, "cont5").getDescription().getValue().equals(""));

        assertTrue(getContainer(module, "cont6") != null);
        assertTrue(getContainer(module, "cont6").getDescription().getValue().equals(
                "invalid YANG 1.1 double-quoted string because of trailing backslash"));

        assertTrue(getContainer(module, "cont11") != null);
        assertTrue(getContainer(module, "cont11").getDescription().getValue().equals(
                "valid YANG 1.1 single-quoted string"));

        assertTrue(getContainer(module, "cont12") != null);
        assertTrue(getContainer(module, "cont12").getDescription().getValue().equals(
                "valid YANG 1.1 single-quoted string \" \\n \\t \\\\ \\X"));

        assertTrue(getContainer(module, "cont13") != null);
        assertTrue(getContainer(module, "cont13").getDescription().getValue().equals(
                "valid YANG 1.1\nsingle-quoted string"));

        assertTrue(getContainer(module, "cont14") != null);
        assertTrue(getContainer(module, "cont14").getDescription().getValue().equals(
                "valid YANG 1.1\n                           single-quoted string"));

        assertTrue(getContainer(module, "cont21") != null);
        assertTrue(getContainer(module, "cont21").getDescription().getValue().equals("valid_YANG_1_1_unquoted_string"));

        assertTrue(getContainer(module, "cont22") != null);
        assertTrue(getContainer(module, "cont22").getDescription().getValue().equals(
                "invalid_YANG_1_1_unquoted_string_with_a_quote_at_the_end\""));

        assertFindingCount(4);
        assertHasFindingOfType(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertHasFindingOfType(ParserFindingType.P012_INVALID_CHARACTER_IN_UNQUOTED_TEXT.toString());

        assertHasFinding(yangModelFile, 21, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertHasFinding(yangModelFile, 40, ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertHasFinding(yangModelFile, 67, ParserFindingType.P012_INVALID_CHARACTER_IN_UNQUOTED_TEXT.toString());
    }

}
