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
package org.oran.smo.yangtools.parser.model.statements.oran.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement.StatementArgumentType;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement.MaxCardinality;
import org.oran.smo.yangtools.parser.model.statements.oran.CORAN;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivASide;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivBSide;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivBiDirectionalTopologyRelationship;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivLabel;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeafList;
import org.oran.smo.yangtools.parser.model.statements.yang.YList;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class OranSmoTeivExtensionsTest extends YangTestCommon {

    protected static final String SMO_TIES_COMMON_YANG_EXT = "src/test/resources/_orig-modules/o-ran-smo-teiv-common-yang-extensions@2023-12-12.yang";
    protected static final String SMO_TIES_COMMON_YANG_TYPES = "src/test/resources/_orig-modules/o-ran-smo-teiv-common-yang-types@2023-12-12.yang";

    @Test
    public void test_oran_extensions() {

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-statements-oran/oran-smo-teiv-extension-test.yang", SMO_TIES_COMMON_YANG_EXT,
                SMO_TIES_COMMON_YANG_TYPES));

        final YModule module = getModule("oran-smo-teiv-extension-test");
        assertTrue(module != null);

        final YOranSmoTeivLabel label0 = module.getRevisions().get(0).getChild(
                CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__LABEL);
        final YOranSmoTeivLabel label1 = module.getRevisions().get(1).getChild(
                CORAN.ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__LABEL);

        assertNotNull(label0);
        assertNotNull(label1);

        assertEquals("2.3.4", label0.getLabel());
        assertEquals(2, label0.getVersion());
        assertEquals(3, label0.getRelease());
        assertEquals(4, label0.getCorrection());

        assertEquals("06.34.12345", label1.getLabel());
        assertEquals(6, label1.getVersion());
        assertEquals(34, label1.getRelease());
        assertEquals(12345, label1.getCorrection());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel1 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-1");
        assertNotNull(rel1);
        assertNoFindingsOnStatement(rel1);

        assertEquals("o-ran-smo-teiv-common-yang-extensions", rel1.getStatementModuleAndName().getModuleName());
        assertEquals("biDirectionalTopologyRelationship", rel1.getStatementModuleAndName().getStatementName());
        assertFalse(rel1.getStatementModuleAndName().isYangCoreStatement());
        assertTrue(rel1.getStatementModuleAndName().isExtensionStatement());
        assertEquals(StatementArgumentType.NAME, rel1.getArgumentType());
        assertEquals(MaxCardinality.MULTIPLE, rel1.getMaxCardinalityUnderParent());
        assertEquals("relationship-1", rel1.getRelationshipName());

        final YLeaf leafAside = getLeaf(rel1, "leaf-a-side");
        assertNotNull(leafAside);
        assertNoFindingsOnStatement(leafAside);
        final YOranSmoTeivASide aSide = getChild(leafAside, "or-teiv-ext:aSide");
        assertNotNull(aSide);
        assertNoFindingsOnStatement(aSide);

        assertEquals("o-ran-smo-teiv-common-yang-extensions", aSide.getStatementModuleAndName().getModuleName());
        assertEquals("aSide", aSide.getStatementModuleAndName().getStatementName());
        assertFalse(aSide.getStatementModuleAndName().isYangCoreStatement());
        assertTrue(aSide.getStatementModuleAndName().isExtensionStatement());
        assertEquals(StatementArgumentType.NAME, aSide.getArgumentType());
        assertEquals(MaxCardinality.ONE, aSide.getMaxCardinalityUnderParent());
        assertEquals("role-a-side", aSide.getTeivTypeName());

        final YLeafList leafBside = getLeafList(rel1, "leaf-b-side");
        assertNotNull(leafBside);
        assertNoFindingsOnStatement(leafBside);
        final YOranSmoTeivBSide bSide = getChild(leafBside, "or-teiv-ext:bSide");
        assertNotNull(bSide);
        assertNoFindingsOnStatement(bSide);

        assertEquals("o-ran-smo-teiv-common-yang-extensions", bSide.getStatementModuleAndName().getModuleName());
        assertEquals("bSide", bSide.getStatementModuleAndName().getStatementName());
        assertFalse(bSide.getStatementModuleAndName().isYangCoreStatement());
        assertTrue(bSide.getStatementModuleAndName().isExtensionStatement());
        assertEquals(StatementArgumentType.NAME, bSide.getArgumentType());
        assertEquals(MaxCardinality.ONE, bSide.getMaxCardinalityUnderParent());
        assertEquals("role-b-side", bSide.getTeivTypeName());

        final YLeaf idLeaf = getLeaf(rel1, "id");
        assertNotNull(idLeaf);
        final YLeaf leafProp = getLeaf(rel1, "leaf-prop");
        assertNotNull(leafProp);
        final YLeafList leafListProp = getLeafList(rel1, "leaf-list-prop");
        assertNotNull(leafListProp);
        assertNotNull(getChild(rel1, "key"));

        // - - - - error cases - - - - -

        final YOranSmoTeivBiDirectionalTopologyRelationship rel2 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-2");
        assertNotNull(rel2);
        assertStatementHasFindingOfType(rel2, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel3 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-3");
        assertNotNull(rel3);
        assertStatementHasFindingOfType(rel3, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel4 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-4");
        assertNotNull(rel4);
        assertStatementHasFindingOfType(rel4, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel5 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-5");
        assertNotNull(rel5);
        assertStatementHasFindingOfType(rel5, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel6 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-6");
        assertNotNull(rel6);
        assertStatementHasFindingOfType(rel6, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YContainer cont1 = getContainer(module, "cont1");
        final YOranSmoTeivBiDirectionalTopologyRelationship rel7 = getChild(cont1,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-7");
        assertNotNull(rel7);
        assertStatementHasFindingOfType(rel7, ParserFindingType.P025_INVALID_EXTENSION.toString());

        final YOranSmoTeivBiDirectionalTopologyRelationship rel8 = getChild(module,
                "or-teiv-ext:biDirectionalTopologyRelationship", "relationship-8");
        assertNotNull(rel8);
        assertStatementHasFindingOfType(rel8, ParserFindingType.P025_INVALID_EXTENSION.toString());
        final YContainer cont2 = getContainer(rel8, "cont2");
        final YOranSmoTeivASide cont2aSide = getChild(cont2, "or-teiv-ext:aSide");
        assertStatementHasFindingOfType(cont2aSide, ParserFindingType.P025_INVALID_EXTENSION.toString());
        final YList list2 = getList(rel8, "list2");
        final YOranSmoTeivBSide list2bSide = getChild(list2, "or-teiv-ext:bSide");
        assertStatementHasFindingOfType(list2bSide, ParserFindingType.P025_INVALID_EXTENSION.toString());
    }
}
