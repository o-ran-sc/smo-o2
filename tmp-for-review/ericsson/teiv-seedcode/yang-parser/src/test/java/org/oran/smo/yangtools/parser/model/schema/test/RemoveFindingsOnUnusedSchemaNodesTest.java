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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class RemoveFindingsOnUnusedSchemaNodesTest extends YangTestCommon {

    @Test
    public void test___simple___do_not_suppress_findings() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/remove-findings-on-unused-schema-nodes/simple/simple-module.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        context.setSuppressFindingsOnUnusedSchemaNodes(false);

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        /*
         * We expect a number of findings
         */
        final YModule yModule = getModule("simple-module");

        // ----- The typedef -----

        final YangDomElement typedef01 = getDomChild(yModule.getDomElement(), CY.TYPEDEF, "typedef01");
        assertDomElementHasFindingOfType(typedef01, ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        final YangDomElement typeUnderTypedef01 = getDomChild(typedef01, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderTypedef01, ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());

        // ----- The grouping -----

        final YangDomElement grouping01 = getDomChild(yModule.getDomElement(), CY.GROUPING, "grouping01");
        assertDomElementHasFindingOfType(grouping01, ParserFindingType.P132_GROUPING_NOT_USED.toString());

        final YangDomElement statementUnderGrouping01 = getDomChild(grouping01, "unknown-statement");
        assertDomElementHasFindingOfType(statementUnderGrouping01, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        // ----- The leaf02 -----

        final YangDomElement cont1 = getDomChild(yModule.getDomElement(), CY.CONTAINER, "cont1");
        final YangDomElement leaf02 = getDomChild(cont1, CY.LEAF, "leaf02");
        final YangDomElement typeUnderLeaf02 = getDomChild(leaf02, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderLeaf02, ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());

        // ----- The leaf03 -----

        final YangDomElement leaf03 = getDomChild(cont1, CY.LEAF, "leaf03");
        final YangDomElement typeUnderLeaf03 = getDomChild(leaf03, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderLeaf03, ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());

        // ----- The deviation -----

        final YangDomElement deviation = getDomChild(yModule.getDomElement(), CY.DEVIATION);
        assertDomElementHasFindingOfType(deviation, ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());
    }

    @Test
    public void test___simple___suppress_findings() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/remove-findings-on-unused-schema-nodes/simple/simple-module.yang");
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        context.setSuppressFindingsOnUnusedSchemaNodes(true);

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        /*
         * We expect a number of findings
         */
        final YModule yModule = getModule("simple-module");

        // ----- The typedef -----

        final YangDomElement typedef01 = getDomChild(yModule.getDomElement(), CY.TYPEDEF, "typedef01");
        assertDomElementHasFindingOfType(typedef01, ParserFindingType.P114_TYPEDEF_NOT_USED.toString());

        final YangDomElement typeUnderTypedef01 = getDomChild(typedef01, CY.TYPE);
        assertDomElementHasNoFindings(typeUnderTypedef01);

        // ----- The grouping -----

        final YangDomElement grouping01 = getDomChild(yModule.getDomElement(), CY.GROUPING, "grouping01");
        assertDomElementHasFindingOfType(grouping01, ParserFindingType.P132_GROUPING_NOT_USED.toString());

        final YangDomElement statementUnderGrouping01 = getDomChild(grouping01, "unknown-statement");
        assertDomElementHasNoFindings(statementUnderGrouping01);

        // ----- The leaf02 -----

        final YangDomElement cont1 = getDomChild(yModule.getDomElement(), CY.CONTAINER, "cont1");
        final YangDomElement leaf02 = getDomChild(cont1, CY.LEAF, "leaf02");
        final YangDomElement typeUnderLeaf02 = getDomChild(leaf02, CY.TYPE);
        assertDomElementHasNoFindings(typeUnderLeaf02);

        // ----- The leaf03 -----

        final YangDomElement leaf03 = getDomChild(cont1, CY.LEAF, "leaf03");
        final YangDomElement typeUnderLeaf03 = getDomChild(leaf03, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderLeaf03, ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());

        // ----- The deviation -----

        final YangDomElement deviation = getDomChild(yModule.getDomElement(), CY.DEVIATION);
        assertDomElementHasFindingOfType(deviation, ParserFindingType.P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE.toString());
    }
}
