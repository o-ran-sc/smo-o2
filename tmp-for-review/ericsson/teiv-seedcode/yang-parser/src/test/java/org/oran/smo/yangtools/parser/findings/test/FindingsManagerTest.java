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
package org.oran.smo.yangtools.parser.findings.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingSeverity;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ModuleAndFindingTypeAndSchemaNodePathFilterPredicate;
import org.oran.smo.yangtools.parser.findings.ModuleAndSeverityFilterPredicate;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;

public class FindingsManagerTest {

    private YangDeviceModel yangDeviceModel;
    private ModifyableFindingSeverityCalculator severityCalculator;
    private FindingsManager findingsManager;
    private ParserExecutionContext context;

    @Before
    public void setUp() {
        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);
        context = new ParserExecutionContext(findingsManager);

        final List<YangModel> yangFiles = new ArrayList<>();
        yangFiles.add(new YangModel(new FileBasedYangInput(new File("src/test/resources/findings/module1.yang")),
                ConformanceType.IMPLEMENT));
        yangDeviceModel.parseIntoYangModels(context, yangFiles);
    }

    @Test
    public void test_severity_calculator() {

        final ModifyableFindingSeverityCalculator severityCalculator = new ModifyableFindingSeverityCalculator();

        severityCalculator.errorForFinding(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString());
        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());
        severityCalculator.infoForFinding(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX.toString());
        severityCalculator.suppressFinding(ParserFindingType.P104_USAGE_OF_DEPRECATED_ELEMENT.toString());

        severityCalculator.setSeverityForFindingType(ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES.toString(),
                FindingSeverity.WARNING);

        assertTrue(severityCalculator.calculateSeverity("something else") == FindingSeverity.ERROR);

        assertTrue(severityCalculator.calculateSeverity(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE
                .toString()) == FindingSeverity.ERROR);
        assertTrue(severityCalculator.calculateSeverity(ParserFindingType.P102_INVALID_STATUS
                .toString()) == FindingSeverity.WARNING);
        assertTrue(severityCalculator.calculateSeverity(ParserFindingType.P103_ILLEGAL_IF_FEATURE_SYNTAX
                .toString()) == FindingSeverity.INFO);
        assertTrue(severityCalculator.calculateSeverity(ParserFindingType.P104_USAGE_OF_DEPRECATED_ELEMENT
                .toString()) == FindingSeverity.SUPPRESS);

        assertTrue(severityCalculator.calculateSeverity(ParserFindingType.P111_CIRCULAR_TYPEDEF_REFERENCES
                .toString()) == FindingSeverity.WARNING);
    }

    @Test
    public void test_fine_grained_2options_all() {

        addFineGrainedFilter("*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_module1() {

        addFineGrainedFilter("module1;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_mod_star() {

        addFineGrainedFilter("mod*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_module1_and_others() {

        addFineGrainedFilter("module1,module2;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_moduleXXX_all() {

        addFineGrainedFilter("moduleXXX;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_2options_warnings() {

        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());

        addFineGrainedFilter("*;WARNING");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_info() {

        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());

        addFineGrainedFilter("*;INFO");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_2options_warnings_info() {

        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());

        addFineGrainedFilter("*;WARNING,INFO");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_module1_warnings() {

        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());

        addFineGrainedFilter("module1;WARNING");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_2options_moduleXXX_warnings() {

        severityCalculator.warningForFinding(ParserFindingType.P102_INVALID_STATUS.toString());

        addFineGrainedFilter("moduleXXX;WARNING");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_all() {

        addFineGrainedFilter("*;*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_module1() {

        addFineGrainedFilter("module1;*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_module1_moduleXXX() {

        addFineGrainedFilter("module1,moduleXXX;*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_moduleXXX() {

        addFineGrainedFilter("moduleXXX;*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_mod_star() {

        addFineGrainedFilter("mod*;*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_invalidstatus() {

        addFineGrainedFilter("*;P102_INVALID_STATUS;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_p1_star() {

        addFineGrainedFilter("*;P1*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_p0_star() {

        addFineGrainedFilter("*;P0*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_p000_star_invalidstatus() {

        addFineGrainedFilter("*;P000*,P102_INVALID_STATUS;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_module1_p1_star() {

        addFineGrainedFilter("module1;P1*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_moduleXXX_p1_star() {

        addFineGrainedFilter("moduleXXX;P1*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_moduelXXX_p1_star() {

        addFineGrainedFilter("moduleXXX;P1*;*");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_path_module_module1() {

        addFineGrainedFilter("*;*;/module=module1");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_path_module_moduleXXX() {

        addFineGrainedFilter("*;*;/module=moduleXXX");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_path_module_module1_leaf_leaf1() {

        addFineGrainedFilter("*;*;/module=module1/leaf=leaf1");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 0);
    }

    @Test
    public void test_fine_grained_3options_path_module_module1_leaf_leaf2() {

        addFineGrainedFilter("*;*;/module=module1/leaf=leaf2");

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        findingsManager.addFinding(new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_fine_grained_3options_path_module_module1_leaf_leaf1_not_on_leaf() {

        addFineGrainedFilter("*;*;/module=module1/leaf=leaf1");

        findingsManager.addFinding(new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "message"));
        assertTrue(findingsManager.getAllFindings().size() == 1);
    }

    @Test
    public void test_finding_general_finding() {

        final Finding finding1 = new Finding(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString(), "Missing Value");
        final Finding finding2 = new Finding(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString(), "Missing Value");

        final Finding finding3 = new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "Missing Value");
        final Finding finding4 = new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "Other Missing Value");

        assertTrue(finding1.getDataDomNode() == null);
        assertTrue(finding1.getFindingType().equals(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString()));
        assertTrue(finding1.getLineNumber() == 0);
        assertTrue(finding1.getMessage().equals("Missing Value"));
        assertTrue(finding1.getStatement() == null);
        assertTrue(finding1.getYangData() == null);
        assertTrue(finding1.getYangModel() == null);

        assertTrue(finding1.isYangModelRelated() == false);
        assertTrue(finding1.isInstanceDataRelated() == false);
        assertTrue(finding1.isGeneralFinding() == true);
        assertTrue(finding1.toString().equals(" P101_EMPTY_DOCUMENTATION_VALUE: Missing Value"));

        assertTrue(finding1.equals(null) == false);
        assertTrue(finding1.equals(finding2) == true);
        assertTrue(finding1.equals(finding3) == false);
        assertTrue(finding3.equals(finding4) == false);
    }

    @Test
    public void test_finding_module_finding() {

        final YLeaf leaf1 = yangDeviceModel.getModuleRegistry().getAllYangModels().get(0).getYangModelRoot().getModule()
                .getLeafs().get(0);

        final Finding finding8 = new Finding(leaf1, ParserFindingType.P102_INVALID_STATUS.toString(), "message");

        assertTrue(finding8.getDataDomNode() == null);
        assertTrue(finding8.getFindingType().equals(ParserFindingType.P102_INVALID_STATUS.toString()));
        assertTrue(finding8.getLineNumber() == 14);
        assertTrue(finding8.getMessage().equals("message"));
        assertTrue(finding8.getStatement() == leaf1);
        assertTrue(finding8.getYangData() == null);
        assertTrue(finding8.getYangModel() == yangDeviceModel.getModuleRegistry().getAllYangModels().get(0));

        assertTrue(finding8.isYangModelRelated() == true);
        assertTrue(finding8.isInstanceDataRelated() == false);
        assertTrue(finding8.isGeneralFinding() == false);
        assertTrue(finding8.toString().equals("module1.yang / line 14 P102_INVALID_STATUS: message"));
    }

    @Test
    public void test_getFiltered_allow_all() {

        final Finding finding1 = new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "message");
        Set<Finding> findings = Collections.singleton(finding1);

        final Set<Finding> filteredFindings = findingsManager.getFilteredFindings(findings);

        assertTrue(filteredFindings.size() == 1);
    }

    @Test
    public void test_getFiltered_suppless_all() {

        final Finding finding1 = new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "message");
        Set<Finding> findings = Collections.singleton(finding1);

        findingsManager.setSuppressAll(true);
        final Set<Finding> filteredFindings = findingsManager.getFilteredFindings(findings);

        assertTrue(filteredFindings.size() == 0);
    }

    @Test
    public void test_getFiltered_suppless_invalid_status() {

        final Finding finding1 = new Finding(ParserFindingType.P102_INVALID_STATUS.toString(), "message");
        Set<Finding> findings = Collections.singleton(finding1);

        severityCalculator.suppressFinding(ParserFindingType.P102_INVALID_STATUS.toString());
        final Set<Finding> filteredFindings = findingsManager.getFilteredFindings(findings);

        assertTrue(filteredFindings.size() == 0);
    }

    private void addFineGrainedFilter(final String filterString) {
        if (filterString.split(";").length == 2) {
            findingsManager.addFilterPredicate(ModuleAndSeverityFilterPredicate.fromString(filterString, findingsManager
                    .getFindingSeverityCalculator()));
        } else {
            findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                    filterString));
        }
    }
}
