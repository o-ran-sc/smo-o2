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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.resolvers.TypeResolver;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

public class TypedefTest extends YangTestCommon {

    private static final String TEST_MODULE_NS = "test:typedef-test-module";
    private static final String TEST_MODULE_NAME = "typedef-test-module";

    private static final String TEST_IMPORTING_MODULE_NS = "test:typedef-test-importing-module";
    private static final String TEST_IMPORTING_MODULE_NAME = "typedef-test-importing-module";

    @Test
    public void testTypedef() {

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());

        context.setSuppressFindingsOnUnusedSchemaNodes(false);
        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-module.yang",
                "typedef-test/typedef-test-submodule.yang", "typedef-test/typedef-test-importing-module.yang"));

        final YModule module = getModule("typedef-test-module");
        final YModule importingModule = getModule("typedef-test-importing-module");

        // - - - - lots of nesting - - - - - - -

        assertNoFindingsOnStatement(getTypedef(module, "typedef1"));
        assertDomElementHasFindingOfType(getTypedef(module, "typedef5").getDomElement().getChildren().get(0),
                ParserFindingType.P112_EXCESSIVE_TYPEDEF_DEPTH.toString());

        final YContainer cont1 = getContainer(module, "cont1");

        final YLeaf leaf01 = getLeaf(cont1, "leaf01");
        assertOriginalTypedefStackSize(leaf01.getType(), 1);
        assertOriginalTypedef(leaf01.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef1"));

        final YLeaf leaf02 = getLeaf(cont1, "leaf02");
        assertOriginalTypedefStackSize(leaf02.getType(), 2);
        assertOriginalTypedef(leaf02.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf02.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef2"));

        final YLeaf leaf05 = getLeaf(cont1, "leaf05");
        assertOriginalTypedefStackSize(leaf05.getType(), 5);
        assertOriginalTypedef(leaf05.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf05.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef2"));
        assertOriginalTypedef(leaf05.getType(), 4, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef5"));

        // - - - - circular - - - - - - -

        assertStatementHasFindingOfType(getTypedef(module, "typedef7").getType(),
                ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef8").getType(),
                ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef9").getType(),
                ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES.toString());

        // - - - - bad typedef name - - - - - - -

        assertStatementHasFindingOfType(getTypedef(module, "uint32"), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertStatementHasFindingOfType(getTypedef(module, ""), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertStatementHasFindingOfType(getTypedef(module, "my custom type"), ParserFindingType.P052_INVALID_YANG_IDENTIFIER
                .toString());
        assertStatementHasFindingOfType(getTypedef(module, null), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());

        // - - - - not resolvable - - - - - - -

        assertStatementHasFindingOfType(getTypedef(module, "typedef11").getType(),
                ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef12").getType(),
                ParserFindingType.P116_NESTED_DERIVED_TYPE_NOT_RESOLVABLE.toString());

        // - - - - - resolvable, with and without prefix - - - - -

        assertNoFindingsOnStatement(getTypedef(module, "typedef13"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef14"));
        assertTrue(getTypedef(module, "typedef14").getType().getDataType().equals("uint32"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef15"));
        assertTrue(getTypedef(module, "typedef15").getType().getDataType().equals("uint32"));

        // - - - - - from submodule - - - - -

        assertNoFindingsOnStatement(getTypedef(module, "typedef16"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef17"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef91"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef92"));
        assertNoFindingsOnStatement(getTypedef(module, "typedef93"));
        assertTrue(getTypedef(module, "typedef16").getType().getDataType().equals("boolean"));
        assertTrue(getTypedef(module, "typedef17").getType().getDataType().equals("uint32"));

        assertTrue(getLeaf(importingModule, "leaf1").getType().getDataType().equals("uint32"));
        assertTrue(getLeaf(importingModule, "leaf2").getType().getDataType().equals("boolean"));
        assertTrue(getLeaf(importingModule, "leaf3").getType().getDataType().equals("uint32"));

        final YContainer cont2 = getContainer(module, "cont2");

        final YLeaf leaf21 = getLeaf(cont2, "leaf21");
        assertOriginalTypedefStackSize(leaf21.getType(), 3);
        assertOriginalTypedef(leaf21.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef92"));
        assertOriginalTypedef(leaf21.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef91"));
        assertOriginalTypedef(leaf21.getType(), 2, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef16"));

        final YLeaf leaf22 = getLeaf(cont2, "leaf22");
        assertOriginalTypedefStackSize(leaf22.getType(), 3);
        assertOriginalTypedef(leaf22.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef13"));
        assertOriginalTypedef(leaf22.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef93"));
        assertOriginalTypedef(leaf22.getType(), 2, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef17"));

        // - - - - - bad types - - - - -

        assertStatementHasFindingOfType(getTypedef(module, "typedef18").getType(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef19").getType(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef20").getType(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef21").getType(),
                ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());

        // - - - - - importing - - - - -

        final YLeaf leaf1 = getLeaf(importingModule, "leaf1");
        assertOriginalTypedefStackSize(leaf1.getType(), 1);
        assertOriginalTypedef(leaf1.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef13"));

        final YLeaf leaf2 = getLeaf(importingModule, "leaf2");
        assertOriginalTypedefStackSize(leaf2.getType(), 1);
        assertOriginalTypedef(leaf2.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef92"));

        final YLeaf leaf4 = getLeaf(importingModule, "leaf4");
        assertOriginalTypedefStackSize(leaf4.getType(), 3);
        assertOriginalTypedef(leaf4.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef13"));
        assertOriginalTypedef(leaf4.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef93"));
        assertOriginalTypedef(leaf4.getType(), 2, new NamespaceModuleIdentifier(TEST_IMPORTING_MODULE_NS,
                TEST_IMPORTING_MODULE_NAME, "typedef51"));

        final YContainer cont6 = getContainer(importingModule, "cont6");
        final YLeaf leaf5 = getLeaf(cont6, "leaf5");
        assertOriginalTypedefStackSize(leaf5.getType(), 2);
        assertOriginalTypedef(leaf5.getType(), new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef13"));
        assertOriginalTypedef(leaf5.getType(), 1, new NamespaceModuleIdentifier(TEST_MODULE_NS, TEST_MODULE_NAME,
                "typedef93"));
    }

    @Test
    public void test___typedef___not_used_or_used_once_only() {

        context.setSuppressFindingsOnUnusedSchemaNodes(true);
        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-not-used-once-used.yang"));

        final YModule module = getModule("typedef-test-not-used-once-used");

        assertStatementHasFindingOfType(getTypedef(module, "typedef1"), ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef2"), ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY
                .toString());
        assertNoFindingsOnStatement(getTypedef(module, "typedef3"));
    }

    @Test
    public void test_Typedef_no_submodule() {

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());

        context.setSuppressFindingsOnUnusedSchemaNodes(false);
        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-module.yang",
                "typedef-test/typedef-test-importing-module.yang"));

        assertHasFindingOfType(ParserFindingType.P037_UNRESOLVABLE_INCLUDE.toString());

        final YModule module = getModule("typedef-test-module");

        // - - - - - from submodule - - - - -

        assertStatementHasFindingOfType(getTypedef(module, "typedef16").getType(),
                ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());
        assertStatementHasFindingOfType(getTypedef(module, "typedef17").getType(),
                ParserFindingType.P113_UNRESOLVABLE_DERIVED_TYPE.toString());
    }

    @Test
    public void test_Typedef_restrictions() {

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-restrictions-module.yang"));

        final YModule module = getModule("typedef-test-restrictions-module");

        // - - - - - - no changes - - - - - -

        final YContainer cont1 = getContainer(module, "cont1");
        assertSubTreeNoFindings(cont1);

        assertTrue(getLeaf(cont1, "leaf1").getDefault().getValue().equals("Hello"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont1, "leaf1").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf1").getType().getLength().getBoundaries().get(0).lower == 10);
        assertTrue(getLeaf(cont1, "leaf1").getType().getLength().getBoundaries().get(0).upper == 20);
        assertTrue(getLeaf(cont1, "leaf1").getType().getPatterns().size() == 1);
        assertTrue(getLeaf(cont1, "leaf1").getType().getPatterns().get(0).getPattern().equals("ab*c"));

        assertTrue(getLeaf(cont1, "leaf2").getDefault().getDecimalDefaultValue().compareTo(BigDecimal.valueOf(35L)) == 0);
        assertTrue(getLeaf(cont1, "leaf2").getType().getDataType().equals("int32"));
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                30)) == 0);
        assertTrue(getLeaf(cont1, "leaf2").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                40)) == 0);

        assertTrue(getLeaf(cont1, "leaf3").getDefault().getValue().equals("one"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getDataType().equals("enumeration"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().size() == 3);
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(0).getEnumName().equals("zero"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(0).getValue().getValue().equals("10"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(1).getEnumName().equals("one"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(1).getValue().getValue().equals("11"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(2).getEnumName().equals("two"));
        assertTrue(getLeaf(cont1, "leaf3").getType().getEnums().get(2).getValue().getValue().equals("12"));

        assertTrue(getLeaf(cont1, "leaf4").getDefault() == null);
        assertTrue(getLeaf(cont1, "leaf4").getType().getDataType().equals("bits"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().size() == 3);
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(0).getBitName().equals("four"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(0).getPosition().getValue().equals("16"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(1).getBitName().equals("five"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(1).getPosition().getValue().equals("17"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(2).getBitName().equals("six"));
        assertTrue(getLeaf(cont1, "leaf4").getType().getBits().get(2).getPosition().getValue().equals("18"));

        // - - - - - - some overrides - - - - - -

        final YContainer cont2 = getContainer(module, "cont2");
        assertSubTreeNoFindings(cont2);

        assertTrue(getLeaf(cont2, "leaf7").getDefault().getValue().equals("World"));
        assertTrue(getLeaf(cont2, "leaf7").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont2, "leaf7").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont2, "leaf7").getType().getLength().getBoundaries().get(0).lower == 12);
        assertTrue(getLeaf(cont2, "leaf7").getType().getLength().getBoundaries().get(0).upper == 15);
        assertTrue(getLeaf(cont2, "leaf7").getType().getPatterns().size() == 2);
        assertTrue(getLeaf(cont2, "leaf7").getType().getPatterns().get(0).getPattern().equals("ab*c"));
        assertTrue(getLeaf(cont2, "leaf7").getType().getPatterns().get(1).getPattern().equals("de*f"));

        assertTrue(getLeaf(cont2, "leaf8").getDefault().getDecimalDefaultValue().compareTo(BigDecimal.valueOf(32L)) == 0);
        assertTrue(getLeaf(cont2, "leaf8").getType().getDataType().equals("int32"));
        assertTrue(getLeaf(cont2, "leaf8").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont2, "leaf8").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                30)) == 0);
        assertTrue(getLeaf(cont2, "leaf8").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                35)) == 0);

        assertTrue(getLeaf(cont2, "leaf9").getDefault().getValue().equals("zero"));
        assertTrue(getLeaf(cont2, "leaf9").getType().getDataType().equals("enumeration"));
        assertTrue(getLeaf(cont2, "leaf9").getType().getEnums().size() == 2);
        assertTrue(getLeaf(cont2, "leaf9").getType().getEnums().get(0).getEnumName().equals("zero"));
        assertTrue(getLeaf(cont2, "leaf9").getType().getEnums().get(0).getValue().getValue().equals("10"));
        assertTrue(getLeaf(cont2, "leaf9").getType().getEnums().get(1).getEnumName().equals("one"));
        assertTrue(getLeaf(cont2, "leaf9").getType().getEnums().get(1).getValue().getValue().equals("11"));

        assertTrue(getLeaf(cont2, "leaf10").getDefault().getValue().equals("six"));
        assertTrue(getLeaf(cont2, "leaf10").getType().getDataType().equals("bits"));
        assertTrue(getLeaf(cont2, "leaf10").getType().getBits().size() == 2);
        assertTrue(getLeaf(cont2, "leaf10").getType().getBits().get(0).getBitName().equals("five"));
        assertTrue(getLeaf(cont2, "leaf10").getType().getBits().get(0).getPosition().getValue().equals("17"));
        assertTrue(getLeaf(cont2, "leaf10").getType().getBits().get(1).getBitName().equals("six"));
        assertTrue(getLeaf(cont2, "leaf10").getType().getBits().get(1).getPosition().getValue().equals("18"));

        // - - - - - - some multi-level overrides - - - - - -

        final YContainer cont3 = getContainer(module, "cont3");
        assertSubTreeNoFindings(cont3);

        assertTrue(getLeaf(cont3, "leaf18").getDefault().getValue().equals("Moon"));
        assertTrue(getLeaf(cont3, "leaf18").getType().getDataType().equals("string"));
        assertTrue(getLeaf(cont3, "leaf18").getType().getLength().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont3, "leaf18").getType().getLength().getBoundaries().get(0).lower == 6);
        assertTrue(getLeaf(cont3, "leaf18").getType().getLength().getBoundaries().get(0).upper == 7);
        assertTrue(getLeaf(cont3, "leaf18").getType().getPatterns().size() == 3);
        assertTrue(getLeaf(cont3, "leaf18").getType().getPatterns().get(0).getPattern().equals("ab*c"));
        assertTrue(getLeaf(cont3, "leaf18").getType().getPatterns().get(1).getPattern().equals("gh*i"));
        assertTrue(getLeaf(cont3, "leaf18").getType().getPatterns().get(2).getPattern().equals("mn*o"));

        assertTrue(getLeaf(cont3, "leaf19").getDefault().getDecimalDefaultValue().compareTo(BigDecimal.valueOf(13L)) == 0);
        assertTrue(getLeaf(cont3, "leaf19").getType().getDataType().equals("int32"));
        assertTrue(getLeaf(cont3, "leaf19").getType().getRange().getBoundaries().size() == 1);
        assertTrue(getLeaf(cont3, "leaf19").getType().getRange().getBoundaries().get(0).lower.compareTo(BigDecimal.valueOf(
                16)) == 0);
        assertTrue(getLeaf(cont3, "leaf19").getType().getRange().getBoundaries().get(0).upper.compareTo(BigDecimal.valueOf(
                17)) == 0);

        assertTrue(getLeaf(cont3, "leaf20").getDefault().getValue().equals("one"));
        assertTrue(getLeaf(cont3, "leaf20").getType().getDataType().equals("enumeration"));
        assertTrue(getLeaf(cont3, "leaf20").getType().getEnums().size() == 1);
        assertTrue(getLeaf(cont3, "leaf20").getType().getEnums().get(0).getEnumName().equals("one"));
        assertTrue(getLeaf(cont3, "leaf20").getType().getEnums().get(0).getValue().getValue().equals("11"));

        // - - - - - - some illegal statements for restriction - - - - - -

        final YContainer cont4 = getContainer(module, "cont4");

        final YangDomElement leaf28domElement = getDomChild(cont4.getDomElement(), CY.LEAF, "leaf28");
        assertDomElementHasFindingOfType(leaf28domElement, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YangDomElement leaf29domElement = getDomChild(cont4.getDomElement(), CY.LEAF, "leaf29");
        assertDomElementHasFindingOfType(leaf29domElement, ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YangDomElement leaf30domElement = getDomChild(cont4.getDomElement(), CY.LEAF, "leaf30");
        final YangDomElement typeUnderLeaf30domElement = getDomChild(leaf30domElement, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderLeaf30domElement.getChildren().get(0),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertDomElementHasFindingOfType(typeUnderLeaf30domElement.getChildren().get(1),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        final YangDomElement leaf31domElement = getDomChild(cont4.getDomElement(), CY.LEAF, "leaf31");
        final YangDomElement typeUnderLeaf31domElement = getDomChild(leaf31domElement, CY.TYPE);
        assertDomElementHasFindingOfType(typeUnderLeaf31domElement.getChildren().get(0),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertDomElementHasFindingOfType(typeUnderLeaf31domElement.getChildren().get(1),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        // - - - - - - some legal and illegal length restrictions - - - - - -

        final YContainer cont5 = getContainer(module, "cont5");

        assertStatementHasFindingOfType(getLeaf(cont5, "leaf51").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf52").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf53").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf54").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf55").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf56").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf57").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf58").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont5, "leaf59").getType().getLength(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        assertSubTreeNoFindings(getLeaf(cont5, "leaf61"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf62"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf63"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf64"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf65"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf66"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf67"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf68"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf69"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf70"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf71"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf72"));
        assertSubTreeNoFindings(getLeaf(cont5, "leaf73"));

        // - - - - - - some legal and illegal range restrictions - - - - - -

        final YContainer cont8 = getContainer(module, "cont8");

        assertStatementHasFindingOfType(getLeaf(cont8, "leaf81").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf82").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf83").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf84").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf85").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf86").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf87").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf88").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());
        assertStatementHasFindingOfType(getLeaf(cont8, "leaf89").getType().getRange(),
                ParserFindingType.P117_ILLEGAL_DATA_TYPE_RESTRICTION.toString());

        assertSubTreeNoFindings(getLeaf(cont8, "leaf91"));
        assertSubTreeNoFindings(getLeaf(cont8, "leaf92"));
        assertSubTreeNoFindings(getLeaf(cont8, "leaf93"));
        assertSubTreeNoFindings(getLeaf(cont8, "leaf94"));
    }

    private static final String USED_TWICE_NS = "test:typedef-test-used-twice";
    private static final String USED_TWICE_MODULE_NAME = "typedef-test-used-twice";

    @Test
    public void test_Typedef_used_twice() {

        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-used-twice.yang"));

        final YModule module = getModule("typedef-test-used-twice");

        final YContainer cont1 = getContainer(module, "cont1");

        final YLeaf leaf2 = getLeaf(cont1, "leaf2");
        assertTrue(leaf2.getDefault() != null);
        assertTrue(leaf2.getDefault().getValue().equals("10"));
        assertOriginalTypedef(leaf2.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));

        final YLeaf leaf3 = getLeaf(cont1, "leaf3");
        assertTrue(leaf3.getDefault() != null);
        assertTrue(leaf3.getDefault().getValue().equals("5"));
        assertOriginalTypedef(leaf3.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));

        final YLeaf leaf4 = getLeaf(cont1, "leaf4");
        assertTrue(leaf4.getDefault() != null);
        assertTrue(leaf4.getDefault().getValue().equals("5"));
        assertOriginalTypedef(leaf4.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));

        final YLeaf leaf5 = getLeaf(cont1, "leaf5");
        assertTrue(leaf5.getDefault() != null);
        assertTrue(leaf5.getDefault().getValue().equals("20"));
        assertOriginalTypedef(leaf5.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf5.getType(), 1, new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef2"));

        final YLeaf leaf6 = getLeaf(cont1, "leaf6");
        assertTrue(leaf6.getDefault() != null);
        assertTrue(leaf6.getDefault().getValue().equals("20"));
        assertOriginalTypedef(leaf6.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf6.getType(), 1, new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef2"));

        final YLeaf leaf7 = getLeaf(cont1, "leaf7");
        assertTrue(leaf7.getDefault() != null);
        assertTrue(leaf7.getDefault().getValue().equals("5"));
        assertOriginalTypedef(leaf7.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf7.getType(), 1, new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef3"));

        final YLeaf leaf8 = getLeaf(cont1, "leaf8");
        assertTrue(leaf8.getDefault() != null);
        assertTrue(leaf8.getDefault().getValue().equals("5"));
        assertOriginalTypedef(leaf8.getType(), new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef1"));
        assertOriginalTypedef(leaf8.getType(), 1, new NamespaceModuleIdentifier(USED_TWICE_NS, USED_TWICE_MODULE_NAME,
                "typedef3"));
    }

    @Test
    public void test_Typedef_nested_union() {

        parseRelativeImplementsYangModels(Arrays.asList("typedef-test/typedef-test-nested-union.yang"));

        final YModule module = getModule("typedef-test-nested-union-module");

        final YContainer cont1 = getContainer(module, "cont1");

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11.getType().getDataType().equals("union"));
        assertTrue(leaf11.getType().getTypes().size() == 2);
        assertTrue(leaf11.getType().getTypes().get(0).getDataType().equals("int16"));
        assertTrue(leaf11.getType().getTypes().get(1).getDataType().equals("string"));

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12.getType().getDataType().equals("union"));
        assertTrue(leaf12.getType().getTypes().size() == 4);
        assertTrue(leaf12.getType().getTypes().get(0).getDataType().equals("int16"));
        assertTrue(leaf12.getType().getTypes().get(1).getDataType().equals("string"));
        assertTrue(leaf12.getType().getTypes().get(2).getDataType().equals("empty"));
        assertTrue(leaf12.getType().getTypes().get(3).getDataType().equals("binary"));

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13.getType().getDataType().equals("union"));
        assertTrue(leaf13.getType().getTypes().size() == 4);
        assertTrue(leaf13.getType().getTypes().get(0).getDataType().equals("empty"));
        assertTrue(leaf13.getType().getTypes().get(1).getDataType().equals("binary"));
        assertTrue(leaf13.getType().getTypes().get(2).getDataType().equals("int16"));
        assertTrue(leaf13.getType().getTypes().get(3).getDataType().equals("string"));

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14.getType().getDataType().equals("union"));
        assertTrue(leaf14.getType().getTypes().size() == 4);
        assertTrue(leaf14.getType().getTypes().get(0).getDataType().equals("int16"));
        assertTrue(leaf14.getType().getTypes().get(1).getDataType().equals("empty"));
        assertTrue(leaf14.getType().getTypes().get(2).getDataType().equals("binary"));
        assertTrue(leaf14.getType().getTypes().get(3).getDataType().equals("string"));

        final YLeaf leaf15 = getLeaf(cont1, "leaf15");
        assertTrue(leaf15.getType().getDataType().equals("union"));
        assertTrue(leaf15.getType().getTypes().size() == 6);
        assertTrue(leaf15.getType().getTypes().get(0).getDataType().equals("int16"));
        assertTrue(leaf15.getType().getTypes().get(1).getDataType().equals("empty"));
        assertTrue(leaf15.getType().getTypes().get(2).getDataType().equals("int32"));
        assertTrue(leaf15.getType().getTypes().get(3).getDataType().equals("boolean"));
        assertTrue(leaf15.getType().getTypes().get(4).getDataType().equals("binary"));
        assertTrue(leaf15.getType().getTypes().get(5).getDataType().equals("string"));

        final YContainer cont2 = getContainer(module, "cont2");

        final YLeaf leaf21 = getLeaf(cont2, "leaf21");
        assertTrue(leaf21.getType().getDataType().equals("union"));
        assertTrue(leaf21.getType().getTypes().size() == 2);
        assertTrue(leaf21.getType().getTypes().get(0).getDataType().equals("binary"));
        assertTrue(leaf21.getType().getTypes().get(1).getDataType().equals("empty"));

        final YLeaf leaf22 = getLeaf(cont2, "leaf22");
        assertTrue(leaf22.getType().getDataType().equals("union"));
        assertTrue(leaf22.getType().getTypes().size() == 3);
        assertTrue(leaf22.getType().getTypes().get(0).getDataType().equals("uint64"));
        assertTrue(leaf22.getType().getTypes().get(1).getDataType().equals("binary"));
        assertTrue(leaf22.getType().getTypes().get(2).getDataType().equals("empty"));

        final YLeaf leaf23 = getLeaf(cont2, "leaf23");
        assertTrue(leaf23.getType().getDataType().equals("union"));
        assertTrue(leaf23.getType().getTypes().size() == 3);
        assertTrue(leaf23.getType().getTypes().get(0).getDataType().equals("binary"));
        assertTrue(leaf23.getType().getTypes().get(1).getDataType().equals("empty"));
        assertTrue(leaf23.getType().getTypes().get(2).getDataType().equals("uint64"));

        final YLeaf leaf24 = getLeaf(cont2, "leaf24");
        assertTrue(leaf24.getType().getDataType().equals("union"));
        assertTrue(leaf24.getType().getTypes().size() == 1);
        assertTrue(leaf24.getType().getTypes().get(0).getDataType().equals("int32"));

        final YLeaf leaf25 = getLeaf(cont2, "leaf25");
        assertTrue(leaf25.getType().getDataType().equals("union"));
        assertTrue(leaf25.getType().getTypes().size() == 2);
        assertTrue(leaf25.getType().getTypes().get(0).getDataType().equals("uint64"));
        assertTrue(leaf25.getType().getTypes().get(1).getDataType().equals("int32"));

        final YLeaf leaf26 = getLeaf(cont2, "leaf26");
        assertTrue(leaf26.getType().getDataType().equals("union"));
        assertTrue(leaf26.getType().getTypes().size() == 3);
        assertTrue(leaf26.getType().getTypes().get(0).getDataType().equals("boolean"));
        assertTrue(leaf26.getType().getTypes().get(1).getDataType().equals("binary"));
        assertTrue(leaf26.getType().getTypes().get(2).getDataType().equals("empty"));

        final YLeaf leaf27 = getLeaf(cont2, "leaf27");
        assertTrue(leaf27.getType().getDataType().equals("union"));
        assertTrue(leaf27.getType().getTypes().size() == 4);
        assertTrue(leaf27.getType().getTypes().get(0).getDataType().equals("boolean"));
        assertTrue(leaf27.getType().getTypes().get(1).getDataType().equals("binary"));
        assertTrue(leaf27.getType().getTypes().get(2).getDataType().equals("empty"));
        assertTrue(leaf27.getType().getTypes().get(3).getDataType().equals("uint64"));

        final YLeaf leaf28 = getLeaf(cont2, "leaf28");
        assertTrue(leaf28.getType().getDataType().equals("union"));
        assertTrue(leaf28.getType().getTypes().size() == 6);
        assertTrue(leaf28.getType().getTypes().get(0).getDataType().equals("uint64"));
        assertTrue(leaf28.getType().getTypes().get(1).getDataType().equals("boolean"));
        assertTrue(leaf28.getType().getTypes().get(2).getDataType().equals("binary"));
        assertTrue(leaf28.getType().getTypes().get(3).getDataType().equals("empty"));
        assertTrue(leaf28.getType().getTypes().get(4).getDataType().equals("int32"));
        assertTrue(leaf28.getType().getTypes().get(5).getDataType().equals("string"));
    }

    private static void assertOriginalTypedefStackSize(final YType yType, final int size) {
        assertEquals(size, TypeResolver.getTypedefStack(yType).size());
    }

    private static void assertOriginalTypedef(final YType yType, final NamespaceModuleIdentifier soughtTypedefIdentity) {
        assertOriginalTypedef(yType, 0, soughtTypedefIdentity);
    }

    private static void assertOriginalTypedef(final YType yType, final int index,
            final NamespaceModuleIdentifier soughtTypedefIdentity) {

        final List<NamespaceModuleIdentifier> stack = TypeResolver.getTypedefStack(yType);
        if (index >= stack.size()) {
            fail("Typedef stack index " + index + " does not exist.");
        }

        final NamespaceModuleIdentifier nsai = stack.get(index);
        if (!nsai.toString().equals(soughtTypedefIdentity.toString())) {
            fail("Expected '" + soughtTypedefIdentity + "' but got '" + nsai + "'.");
        }
    }
}
