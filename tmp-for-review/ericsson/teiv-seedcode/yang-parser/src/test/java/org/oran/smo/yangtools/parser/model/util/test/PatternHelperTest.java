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
package org.oran.smo.yangtools.parser.model.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.util.PatternHelper;

public class PatternHelperTest {

    @Test
    public void test___patterns() {

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("ab*c"), "ac"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("ab*c"), "abbc"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("$ab*c"), "$abbc"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("^ab*c"), "^abbc"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("ab*c$"), "abbc$"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("ab*c^"), "abbc^"));

        assertFalse(Pattern.matches(PatternHelper.toJavaPatternString("ab*c$"), "abbc"));
        assertFalse(Pattern.matches(PatternHelper.toJavaPatternString("ab*c^"), "abbc"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("[abc]+"), "acb"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("[^abc]+"), "def"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("xy^[abc]+"), "xy^a"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("xy^[^abc]+"), "xy^d"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("^[a\\^]+"), "^a^"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("^[\\^a]+"), "^a^"));
        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("^[a^]+"), "^a^"));

        assertTrue(Pattern.matches(PatternHelper.toJavaPatternString("^$^$"), "^$^$"));
    }
}
