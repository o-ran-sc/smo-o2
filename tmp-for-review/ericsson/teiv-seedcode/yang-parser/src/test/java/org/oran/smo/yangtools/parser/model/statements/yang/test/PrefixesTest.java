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

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class PrefixesTest extends YangTestCommon {

    @Test
    public void testDuplicatePrefixes() {

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-statements-yang/prefixes-test/duplicate-prefixes-test-module.yang",
                YANG_ORIGIN_PATH, YANG_METADATA_PATH));

        assertHasFindingOfType(ParserFindingType.P031_PREFIX_NOT_UNIQUE.toString());

        final YModule module = getModule("duplicate-prefixes-test-module");
        assertStatementHasFindingOfType(getChild(getChild(module, CY.IMPORT, "ietf-yang-metadata"), CY.PREFIX),
                ParserFindingType.P031_PREFIX_NOT_UNIQUE.toString());
    }
}
