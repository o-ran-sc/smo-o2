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
package org.oran.smo.yangtools.parser.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class FailFastTest extends YangTestCommon {

    @Test
    public void test_fail_fast_off() {

        context.setFailFast(false);

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/fail-fast-test/bad-module.yang"));

        assertHasFindingOfType(ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString());
        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());
        assertHasFindingOfType(ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString());

        assertTrue(context.getFindingsManager().hasFindingOfType(ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString()));

        assertHasNotFindingOfType(ParserFindingType.P009_FAIL_FAST.toString());
    }

    @Test
    public void test_fail_fast_on() {

        context.setFailFast(true);

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/fail-fast-test/bad-module.yang"));

        assertHasNotFindingOfType(ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString());
        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());
        assertHasNotFindingOfType(ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString());

        assertHasFindingOfType(ParserFindingType.P009_FAIL_FAST.toString());
    }

}
