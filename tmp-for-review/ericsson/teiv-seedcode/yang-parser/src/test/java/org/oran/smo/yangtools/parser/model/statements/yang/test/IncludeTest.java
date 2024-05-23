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
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class IncludeTest extends YangTestCommon {

    @Test
    public void testIncludeWithoutRevisionSubmoduleNotInInput() {

        parseRelativeImplementsYangModels(Arrays.asList(
                "include-test/include-test-module-correct-submodule-no-revision.yang"));

        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());

        final YModule module = getModule("include-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getIncludes().get(0), ParserFindingType.P037_UNRESOLVABLE_INCLUDE
                .toString());
    }

    @Test
    public void testIncludeWithoutRevisionTwoSubmodulesInInput() {

        parseRelativeImplementsYangModels(Arrays.asList(
                "include-test/include-test-module-correct-submodule-no-revision.yang",
                "include-test/test-submodule-1999-01-01.yang", "include-test/test-submodule-2020-10-02.yang"));

        assertHasFindingOfType(ParserFindingType.P038_AMBIGUOUS_INCLUDE.toString());

        final YModule module = getModule("include-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getIncludes().get(0), ParserFindingType.P038_AMBIGUOUS_INCLUDE.toString());
    }

    @Test
    public void testIncludeIsModuleNotSubmodule() {

        parseRelativeImplementsYangModels(Arrays.asList(
                "include-test/include-test-module-correct-submodule-no-revision.yang",
                "include-test/other-test-module.yang"));

        assertHasFindingOfType(ParserFindingType.P045_NOT_A_SUBMODULE.toString());

        final YModule module = getModule("include-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getIncludes().get(0), ParserFindingType.P045_NOT_A_SUBMODULE.toString());
    }

    @Test
    public void testIncludeYangVersionMismatch() {

        parseRelativeImplementsYangModels(Arrays.asList(
                "include-test/include-test-module-correct-submodule-no-revision.yang",
                "include-test/test-submodule-1999-01-01.yang"));

        assertHasFindingOfType(ParserFindingType.P041_DIFFERENT_YANG_VERSIONS_BETWEEN_MODULE_AND_SUBMODULES.toString());

        final YModule module = getModule("include-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getIncludes().get(0),
                ParserFindingType.P041_DIFFERENT_YANG_VERSIONS_BETWEEN_MODULE_AND_SUBMODULES.toString());
    }

    @Test
    public void testIncludeSubmoduleBelongsToDifferentModule() {

        parseRelativeImplementsYangModels(Arrays.asList(
                "include-test/include-test-module-correct-submodule-no-revision.yang",
                "include-test/test-submodule-belongs-to-other-module.yang"));

        assertHasFindingOfType(ParserFindingType.P047_SUBMODULE_OWNERSHIP_MISMATCH.toString());

        final YModule module = getModule("include-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getIncludes().get(0), ParserFindingType.P047_SUBMODULE_OWNERSHIP_MISMATCH
                .toString());
    }

}
