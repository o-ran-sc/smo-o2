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
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ModuleAndFindingTypeAndSchemaNodePathFilterPredicate;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YRevision;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class RevisionTest extends YangTestCommon {

    @Test
    public void testNoRevisionWithFailFastTrue() {

        context.setFailFast(true);

        parseRelativeImplementsYangModels(Arrays.asList("revision-test/revision-test-module-no-revision.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertTrue(yModule.getRevisions().size() == 0);
        assertHasFindingOfType(ParserFindingType.P032_MISSING_REVISION.toString());
        assertHasFindingOfType(ParserFindingType.P009_FAIL_FAST.toString());

        /*
         * Fail-fast is on so should suppress this one here.
         */
        assertHasNotFindingOfType(ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
    }

    @Test
    public void testNoRevisionWithFailFastFalse() {

        context.setFailFast(false);

        parseRelativeImplementsYangModels(Arrays.asList("revision-test/revision-test-module-no-revision.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertTrue(yModule.getRevisions().size() == 0);
        assertHasFindingOfType(ParserFindingType.P032_MISSING_REVISION.toString());

        assertHasNotFindingOfType(ParserFindingType.P009_FAIL_FAST.toString());
    }

    @Test
    public void testNoRevisionWithFineGrainedFilterAndFailFastTrue() {

        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "revision-test-module;*;*"));

        parseRelativeImplementsYangModels(Arrays.asList("revision-test/revision-test-module-no-revision.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertTrue(yModule.getRevisions().size() == 0);

        /*
         * These should both be filtered. There should not be a fail-fast either
         */
        assertHasNotFindingOfType(ParserFindingType.P032_MISSING_REVISION.toString());
        assertHasNotFindingOfType(ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
        assertHasNotFindingOfType(ParserFindingType.P009_FAIL_FAST.toString());
    }

    @Test
    public void testDuplicateRevisionNonLatest() {
        parseRelativeImplementsYangModels(Arrays.asList(
                "revision-test/revision-test-module-duplicate-revision-non-latest.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertHasFindingOfType(ParserFindingType.P049_DUPLICATE_REVISION.toString());
    }

    @Test
    public void testDuplicateRevisionLatest() {
        parseRelativeImplementsYangModels(Arrays.asList(
                "revision-test/revision-test-module-duplicate-revision-latest.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertHasFindingOfType(ParserFindingType.P050_DUPLICATE_LATEST_REVISION.toString());
    }

    @Test
    public void testInvalidRevision() {
        parseRelativeImplementsYangModels(Arrays.asList("revision-test/revision-test-module-invalid-revision.yang"));

        final YModule yModule = getModule("revision-test-module");
        assertTrue(yModule != null);

        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        final List<YRevision> revisions = yModule.getRevisions();

        assertTrue(revisions.size() == 8);

        assertNoFindingsOnStatement(revisions.get(0));
        assertStatementHasFindingOfType(revisions.get(1), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(2), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(3), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(4), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(5), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(6), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(revisions.get(7), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }
}
