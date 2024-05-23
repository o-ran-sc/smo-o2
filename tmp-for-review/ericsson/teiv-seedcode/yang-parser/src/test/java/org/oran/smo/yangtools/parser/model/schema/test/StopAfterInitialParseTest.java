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
package org.oran.smo.yangtools.parser.model.schema.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class StopAfterInitialParseTest extends YangTestCommon {

    @Test
    public void test_no_processing_delay() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/stop-after-initial-parse-test/module1.yang",
                "src/test/resources/model-schema/stop-after-initial-parse-test/module2.yang");

        parseAbsoluteYangModels(absoluteImplementsFilePath, Collections.<String> emptyList());

        assertNoFindings();

        final YModule module1 = getModule("module1");
        assertTrue(module1 != null);

        final YContainer cont1 = getContainer(module1, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf2") != null);
        assertTrue(getLeaf(cont1, "leaf99") != null);
    }

    @Test
    public void test_processing_delay() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        // this here is different

        context.setStopAfterInitialParse(true);

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/stop-after-initial-parse-test/module1.yang",
                "src/test/resources/model-schema/stop-after-initial-parse-test/module2.yang");

        parseAbsoluteYangModels(absoluteImplementsFilePath, Collections.<String> emptyList());

        assertNoFindings();

        final YModule module1 = getModule("module1");
        assertTrue(module1 != null);

        final YContainer cont1 = getContainer(module1, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf2") != null);

        // this here is different

        assertTrue(getLeaf(cont1, "leaf99") == null);

        yangDeviceModel.getTopLevelSchema().processParsedYangModules(context);

        assertTrue(getLeaf(cont1, "leaf99") != null);

        // processing second time should not work:

        try {
            yangDeviceModel.getTopLevelSchema().processParsedYangModules(context);
            fail("Expected an exception.");
        } catch (IllegalStateException ex) {
            // expected
        } catch (Exception ex) {
            fail("Expected an IllegalStateException.");
        }
    }

}
