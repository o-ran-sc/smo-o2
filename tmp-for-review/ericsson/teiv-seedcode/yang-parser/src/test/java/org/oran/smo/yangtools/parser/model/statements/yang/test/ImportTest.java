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

public class ImportTest extends YangTestCommon {

    private static final String TEST_DIR = "src/test/resources/model-statements-yang/import-test/";

    @Test
    public void testTwoImportsWithoutRevision() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "import-twice-no-revisions-test-module.yang",
                YANG_METADATA_PATH));

        assertHasFindingOfType(ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES.toString());

        final YModule module = getModule("import-twice-no-revisions-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(1), ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES
                .toString());
    }

    @Test
    public void testFirstImportNoRevision() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "import-twice-first-no-revision-test-module.yang",
                YANG_METADATA_PATH));

        assertHasFindingOfType(ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES.toString());

        final YModule module = getModule("import-twice-first-no-revision-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(1), ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES
                .toString());
    }

    @Test
    public void testSecondImportNoRevision() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "import-twice-second-no-revision-test-module.yang",
                YANG_METADATA_PATH));

        assertHasFindingOfType(ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES.toString());

        final YModule module = getModule("import-twice-second-no-revision-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(1), ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES
                .toString());
    }

    @Test
    public void testBothHaveSameRevision() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "import-twice-same-revisions-test-module.yang",
                YANG_METADATA_PATH));

        assertHasFindingOfType(ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES.toString());

        final YModule module = getModule("import-twice-same-revisions-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(1), ParserFindingType.P036_MODULE_IMPORTED_MULTIPLE_TIMES
                .toString());
    }

    @Test
    public void testImportsItself() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "import-itself-test-module.yang"));

        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());

        final YModule module = getModule("import-itself-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }

    @Test
    public void testUnresolvableNoRevisionImportNotInInput() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "unresolvable-import-no-revision-test-module.yang"));

        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());

        final YModule module = getModule("unresolvable-import-no-revision-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }

    @Test
    public void testAmbiguousNoRevisionImport() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "unresolvable-import-ambiguous-test-module.yang",
                ORIG_MODULES_PATH + "ietf-yang-types-2010-09-24.yang",
                ORIG_MODULES_PATH + "ietf-yang-types-2019-11-04.yang"));

        assertHasFindingOfType(ParserFindingType.P035_AMBIGUOUS_IMPORT.toString());

        final YModule module = getModule("unresolvable-import-ambiguous-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P035_AMBIGUOUS_IMPORT.toString());
    }

    @Test
    public void testUnresolvableWithRevisionImportNotInInput() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "unresolvable-import-with-revision-test-module.yang"));

        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());

        final YModule module = getModule("unresolvable-import-with-revision-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }

    @Test
    public void testUnresolvableWithWrongRevisionImport() {

        parseAbsoluteImplementsYangModels(Arrays.asList(TEST_DIR + "unresolvable-import-wrong-revision-test-module.yang",
                ORIG_MODULES_PATH + "ietf-yang-types-2010-09-24.yang"));

        assertHasFindingOfType(ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());

        final YModule module = getModule("unresolvable-import-wrong-revision-test-module");
        assertTrue(module != null);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P034_UNRESOLVABLE_IMPORT.toString());
    }

}
