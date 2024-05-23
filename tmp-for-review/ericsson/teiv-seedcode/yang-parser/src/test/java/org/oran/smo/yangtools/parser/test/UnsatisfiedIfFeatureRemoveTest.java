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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YCase;
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.util.YangFeature;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class UnsatisfiedIfFeatureRemoveTest extends YangTestCommon {

    private static final String MODULE1 = "src/test/resources/basics/unsatisfied-if-feature-remove-test/module1.yang";
    private static final String MODULE2 = "src/test/resources/basics/unsatisfied-if-feature-remove-test/module2.yang";
    private static final String SUBMODULE3 = "src/test/resources/basics/unsatisfied-if-feature-remove-test/submodule3.yang";

    @Test
    public void test_module1_no_removal_no_supported_features() {

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 != null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 != null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 != null);
    }

    @Test
    public void test_module1_no_removal_with_supported_features() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature11"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature12"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature13"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature14"));

        context.setSupportedFeatures(supportedFeatures);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 != null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 != null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 != null);
    }

    @Test
    public void test_module1_with_removal_no_supported_features() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        assertTrue(getLeaf(cont1, "leaf11") == null);
        assertTrue(getLeaf(cont1, "leaf12") == null);
        assertTrue(getLeaf(cont1, "leaf13") == null);
        assertTrue(getLeaf(cont1, "leaf14") == null);

        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);
        final YChoice choice21 = getChoice(cont2, "choice21");
        assertTrue(choice21 != null);

        assertTrue(getCase(choice21, "case211") == null);
        assertTrue(getCase(choice21, "case212") == null);
        assertTrue(getCase(choice21, "case213") == null);
        assertTrue(getCase(choice21, "case214") == null);
        assertTrue(getCase(choice21, "leaf215") == null);

        final YContainer cont3 = getContainer(module, "cont3");
        assertTrue(cont3 != null);

        assertTrue(getLeaf(cont3, "leaf51") == null);
        assertTrue(getLeaf(cont3, "leaf52") == null);
        assertTrue(getLeaf(cont3, "leaf53") == null);
    }

    @Test
    public void test_module1_with_removal_with_supported_features() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature11"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature12"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature13"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature14"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 != null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 != null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 != null);
    }

    @Test
    public void test_module1_with_removal_with_feature11_only() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature11"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 != null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 == null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 == null);
    }

    @Test
    public void test_module1_with_removal_with_feature11_and_feature12() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature11"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature12"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 != null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 != null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 == null);
    }

    @Test
    public void test_module1_with_removal_with_feature12_and_feature13() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature12"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature13"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont1 = getContainer(module, "cont1");
        assertTrue(cont1 != null);

        final YLeaf leaf11 = getLeaf(cont1, "leaf11");
        assertTrue(leaf11 == null);

        final YLeaf leaf12 = getLeaf(cont1, "leaf12");
        assertTrue(leaf12 != null);

        final YLeaf leaf13 = getLeaf(cont1, "leaf13");
        assertTrue(leaf13 != null);

        final YLeaf leaf14 = getLeaf(cont1, "leaf14");
        assertTrue(leaf14 == null);
    }

    @Test
    public void test_module1_with_removal_with_feature13_and_feature14() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature13"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature14"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        final YModule module = getModule("module1");

        /*
         * Should have resulted in a finding - feature 14 depends on 11 and 12, and these are not set.
         */

        assertStatementHasFindingOfType(getFeature(module, "feature14"), ParserFindingType.P086_FEATURE_CANNOT_BE_SUPPORTED
                .toString());
    }

    @Test
    public void test_module1_with_removal_with_feature16() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature16"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);

        final YChoice choice21 = getChoice(cont2, "choice21");
        assertTrue(choice21 != null);

        assertTrue(choice21.getCases().size() == 1);

        final YCase case211 = getCase(choice21, "case211");
        assertTrue(case211 != null);
        assertTrue(getLeaf(case211, "leaf211") != null);
    }

    @Test
    public void test_module1_with_removal_with_feature18_and_feature19() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature18"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature19"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);

        final YChoice choice21 = getChoice(cont2, "choice21");
        assertTrue(choice21 != null);

        assertTrue(choice21.getCases().size() == 3);

        final YCase case213 = getCase(choice21, "case213");
        assertTrue(case213 != null);
        assertTrue(getLeaf(case213, "leaf213") != null);

        final YCase case214 = getCase(choice21, "case214");
        assertTrue(case214 != null);
        assertTrue(getLeaf(case214, "leaf214") != null);

        final YCase case215 = getCase(choice21, "leaf215");
        assertTrue(case215 != null);
        assertTrue(getLeaf(case215, "leaf215") != null);
    }

    @Test
    public void test_module1_with_removal_with_feature21() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature21"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont3 = getContainer(module, "cont3");
        assertTrue(cont3 != null);

        assertTrue(getLeaf(cont3, "leaf51") == null);
        assertTrue(getLeaf(cont3, "leaf52") == null);
        assertTrue(getLeaf(cont3, "leaf53") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature23() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature23"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont3 = getContainer(module, "cont3");
        assertTrue(cont3 != null);

        assertTrue(getLeaf(cont3, "leaf51") != null);
        assertTrue(getLeaf(cont3, "leaf52") == null);
        assertTrue(getLeaf(cont3, "leaf53") != null);

        assertTrue(getLeaf(cont3, "leaf53").getType().getEnums().size() == 2);
    }

    @Test
    public void test_module1_with_removal_with_feature21_and_feature23() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature21"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature23"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont3 = getContainer(module, "cont3");
        assertTrue(cont3 != null);

        assertTrue(getLeaf(cont3, "leaf51") != null);
        assertTrue(getLeaf(cont3, "leaf52") != null);
        assertTrue(getLeaf(cont3, "leaf53") != null);

        assertTrue(getLeaf(cont3, "leaf53").getType().getEnums().size() == 2);
    }

    @Test
    public void test_module1_with_removal_with_feature22_and_feature23() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature22"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature23"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont3 = getContainer(module, "cont3");
        assertTrue(cont3 != null);

        assertTrue(getLeaf(cont3, "leaf51") != null);
        assertTrue(getLeaf(cont3, "leaf52") == null);
        assertTrue(getLeaf(cont3, "leaf53") != null);

        assertTrue(getLeaf(cont3, "leaf53").getType().getEnums().size() == 3);
    }

    @Test
    public void test_module1_with_removal_with_feature31() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature31"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont4 = getContainer(module, "cont4");
        assertTrue(cont4 != null);

        assertTrue(getLeaf(cont4, "leaf91") != null);
        assertTrue(getLeaf(cont4, "leaf92") == null);
        assertTrue(getLeaf(cont4, "leaf93") == null);
        assertTrue(getLeaf(cont4, "leaf94") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature31_and_module2_feature11() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature31"));
        supportedFeatures.add(new YangFeature("urn:test:module2", "module2", "feature11"));	// !!!!! module2 !!!!!

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont4 = getContainer(module, "cont4");
        assertTrue(cont4 != null);

        assertTrue(getLeaf(cont4, "leaf91") != null);
        assertTrue(getLeaf(cont4, "leaf92") != null);
        assertTrue(getLeaf(cont4, "leaf93") != null);
        assertTrue(getLeaf(cont4, "leaf94") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature31_and_feature11_of_both_modules() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature31"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature11"));
        supportedFeatures.add(new YangFeature("urn:test:module2", "module2", "feature11"));	// !!!!! module2 !!!!!

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont4 = getContainer(module, "cont4");
        assertTrue(cont4 != null);

        assertTrue(getLeaf(cont4, "leaf91") != null);
        assertTrue(getLeaf(cont4, "leaf92") != null);
        assertTrue(getLeaf(cont4, "leaf93") != null);
        assertTrue(getLeaf(cont4, "leaf94") != null);
    }

    @Test
    public void test_module1_with_removal_with_feature51() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature51"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont5 = getContainer(module, "cont5");
        assertTrue(cont5 != null);

        assertTrue(getLeaf(cont5, "leaf51") != null);
        assertTrue(getLeaf(cont5, "leaf52") == null);
        assertTrue(getLeaf(cont5, "leaf53") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature52_and_feature58() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature52"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature58"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont5 = getContainer(module, "cont5");
        assertTrue(cont5 != null);

        assertTrue(getLeaf(cont5, "leaf51") == null);
        assertTrue(getLeaf(cont5, "leaf52") != null);
        assertTrue(getLeaf(cont5, "leaf53") != null);
    }

    @Test
    public void test_module1_with_removal_with_feature52() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature52"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        final YModule module = getModule("module1");

        assertStatementHasFindingOfType(getFeature(module, "feature52"), ParserFindingType.P086_FEATURE_CANNOT_BE_SUPPORTED
                .toString());
    }

    @Test
    public void test_module1_no_remove_thus_leaf61_does_exists() {

        context.setRemoveSchemaNodesNotSatisfyingIfFeature(false);		// !!!!!! FALSE !!!!!!

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        final YModule module = getModule("module1");

        final YContainer cont6 = getContainer(module, "cont6");
        assertTrue(cont6 != null);
        assertTrue(getLeaf(cont6, "leaf61") != null);
    }

    @Test
    public void test_module1_remove_thus_leaf61_not_exists() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        final YModule module = getModule("module1");

        final YContainer cont6 = getContainer(module, "cont6");
        assertTrue(cont6 != null);
        assertTrue(getLeaf(cont6, "leaf61") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature71() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature71"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") != null);
        assertTrue(getLeaf(cont7, "leaf72") == null);
        assertTrue(getLeaf(cont7, "leaf73") == null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature71_and_feature72() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature71"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature72"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") == null);
        assertTrue(getLeaf(cont7, "leaf72") == null);
        assertTrue(getLeaf(cont7, "leaf73") == null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature71_and_feature72_and_feature73() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature71"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature72"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature73"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") == null);
        assertTrue(getLeaf(cont7, "leaf72") != null);
        assertTrue(getLeaf(cont7, "leaf73") == null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature71_and_feature72_and_feature73_and_feature74() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature71"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature72"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature73"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature74"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") == null);
        assertTrue(getLeaf(cont7, "leaf72") != null);
        assertTrue(getLeaf(cont7, "leaf73") != null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature71_and_feature73_and_feature75() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature71"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature73"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature75"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") != null);
        assertTrue(getLeaf(cont7, "leaf72") == null);
        assertTrue(getLeaf(cont7, "leaf73") != null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

    @Test
    public void test_module1_with_removal_with_feature74() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature74"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") == null);
        assertTrue(getLeaf(cont7, "leaf72") == null);
        assertTrue(getLeaf(cont7, "leaf73") == null);
        assertTrue(getLeaf(cont7, "leaf74") != null);
    }

    @Test
    public void test_module1_with_removal_with_feature74_and_feature76() {

        final Set<YangFeature> supportedFeatures = new HashSet<>();
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature74"));
        supportedFeatures.add(new YangFeature("urn:test:module1", "module1", "feature76"));

        context.setSupportedFeatures(supportedFeatures);
        context.setRemoveSchemaNodesNotSatisfyingIfFeature(true);

        severityCalculator.suppressFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        parseAbsoluteImplementsYangModels(Arrays.asList(MODULE1, MODULE2, SUBMODULE3));

        assertNoFindings();

        final YModule module = getModule("module1");

        final YContainer cont7 = getContainer(module, "cont7");
        assertTrue(cont7 != null);

        assertTrue(getLeaf(cont7, "leaf71") == null);
        assertTrue(getLeaf(cont7, "leaf72") == null);
        assertTrue(getLeaf(cont7, "leaf73") == null);
        assertTrue(getLeaf(cont7, "leaf74") == null);
    }

}
