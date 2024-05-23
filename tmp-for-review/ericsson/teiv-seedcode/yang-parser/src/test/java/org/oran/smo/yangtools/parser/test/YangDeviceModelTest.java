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

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ModuleAndFindingTypeAndSchemaNodePathFilterPredicate;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class YangDeviceModelTest extends YangTestCommon {

    @Test
    public void test_correct_modules_supplied() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yang-device-model-test/module1.yang",
                "src/test/resources/basics/yang-device-model-test/module2.yang"));

        assertNoFindings();
    }

    @Test
    public void test_only_one_correct_module_supplied() {

        suppressAllExcept(Arrays.asList(ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString(),
                ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString(), ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE
                        .toString(), ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString()));

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yang-device-model-test/module1.yang"));

        assertHasFindingOfType(ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString());
        assertHasFindingOfType(ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString());
        assertHasFindingOfType(ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());
        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }

    @Test
    public void test_empty_module_not_filtered() {

        severityCalculator.suppressFinding(ParserFindingType.P005_NO_IMPLEMENTS.toString());

        /*
         * The module is empty, but no exception will be thrown.
         */
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/basics/yang-device-model-test/empty-file.yang"));

        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());
    }

    @Test
    public void test_empty_module_filtered() {

        /*
         * Will not be swallowed - a P013 cannot be suppressed.
         */
        severityCalculator.suppressFinding(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "empty-file*;*;*"));

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/basics/yang-device-model-test/empty-file.yang"));

        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());
    }

    @Test
    public void test_missing_prefix_not_filtered() {

        severityCalculator.suppressFinding(ParserFindingType.P005_NO_IMPLEMENTS.toString());

        /*
         * This is missing the prefix statement, which is absolutely vital. This will provoke a NPE during
         * parsing. The NPE should be swallowed, at the same time there should be a finding on the missing prefix.
         */

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/basics/yang-device-model-test/module-missing-prefix.yang"));

        assertHasFindingOfType(ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());
    }

    @Test
    public void test_missing_prefix_filtered() {

        severityCalculator.suppressFinding(ParserFindingType.P005_NO_IMPLEMENTS.toString());

        /*
         * As before, now filtered.
         */
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "module-missing-prefix*;*;*"));

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/basics/yang-device-model-test/module-missing-prefix.yang"));

        assertNoFindings();
    }

    @Test
    public void test_correct_modules_plus_missing_prefix_filtered() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        /*
         * The two modules are fine, the one with the missing prefix will be filtered.
         */
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "module-missing-prefix*;*;*"));

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yang-device-model-test/module1.yang",
                "src/test/resources/basics/yang-device-model-test/module2.yang",
                "src/test/resources/basics/yang-device-model-test/module-missing-prefix.yang"));

        assertNoFindings();
    }

    @Test
    public void test_one_correct_module_plus_missing_prefix_filtered() {

        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        /*
         * The fine-grained-filter takes care of the issues inside the module-missing-prefix,
         * the rest is still correctly processed.
         */
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "module-missing-prefix*;*;*"));

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yang-device-model-test/module1.yang",
                "src/test/resources/basics/yang-device-model-test/module-missing-prefix.yang"));

        assertHasFindingOfType(ParserFindingType.P033_UNRESOLVEABLE_PREFIX.toString());
        assertHasFindingOfType(ParserFindingType.P131_UNRESOLVABLE_GROUPING.toString());
        assertHasFindingOfType(ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());
        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }
}
