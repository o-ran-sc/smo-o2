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
package org.oran.smo.yangtools.parser.yanglibrary.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.util.IdentityRefValue;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInputResolver;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.util.YangFeature;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.yanglibrary.Datastore;
import org.oran.smo.yangtools.parser.yanglibrary.IetfYangLibraryParser;
import org.oran.smo.yangtools.parser.yanglibrary.Module;
import org.oran.smo.yangtools.parser.yanglibrary.YangLibrary;

public class IetfYangLibraryParserTest extends YangTestCommon {

    @Test
    public void testRFC7895() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibrary = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertNoFindings(ietfYangLibrary.getFindingsManager().getAllFindings());

        assertTrue(yangLibrary != null);
        assertTrue(yangLibrary.getContentId().equals("10445"));
        assertTrue(yangLibrary.containsTopLevelSchema() == true);

        final Datastore runningDatastore = yangLibrary.getRunningDatastore();
        assertTrue(runningDatastore != null);

        assertTrue(runningDatastore.getAllModules().size() == 3);
        assertTrue(runningDatastore.getImplementingModules().size() == 2);
        assertTrue(runningDatastore.getImportOnlyModules().size() == 1);

        final Module testModule1 = runningDatastore.getImplementingModules().stream().filter(m -> m.getName().equals(
                "test-module1")).findFirst().get();

        assertTrue(testModule1.getName().equals("test-module1"));
        assertTrue(testModule1.getNamespace().equals("com:foo:test-module1"));
        assertTrue(testModule1.getRevision().equals("2020-01-01"));
        assertTrue(testModule1.getSchemaLocations().size() == 1);
        assertTrue(testModule1.getSchemaLocations().get(0).equals("www.acme.com/test-module1.yang"));
        assertTrue(testModule1.getFeatures().size() == 3);
        assertTrue(testModule1.getFeatures().get(0).equals("feature1"));
        assertTrue(testModule1.getFeatures().get(1).equals("feature2"));
        assertTrue(testModule1.getFeatures().get(2).equals("feature3"));
        assertTrue(testModule1.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(testModule1.getSubmodules().size() == 1);
        assertTrue(testModule1.getSubmodules().get(0).getName().equals("test-module1-submodule"));
        assertTrue(testModule1.getSubmodules().get(0).getRevision().equals("2020-02-02"));
        assertTrue(testModule1.getSubmodules().get(0).getSchemaLocations().size() == 1);
        assertTrue(testModule1.getSubmodules().get(0).getSchemaLocations().get(0).equals(
                "www.acme.com/test-module1-submodule.yang"));
        assertTrue(testModule1.getDeviatedByModuleNames().size() == 1);
        assertTrue(testModule1.getDeviatedByModuleNames().get(0).equals("test-module1-ext"));

        final Module testModule1ext = runningDatastore.getImplementingModules().stream().filter(m -> m.getName().equals(
                "test-module1-ext")).findFirst().get();

        assertTrue(testModule1ext.getName().equals("test-module1-ext"));
        assertTrue(testModule1ext.getRevision().equals("2020-05-20"));
        assertTrue(testModule1ext.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);

        final Module importOnlyModule = runningDatastore.getImportOnlyModules().stream().findFirst().get();

        assertTrue(importOnlyModule.getName().equals("test-module2"));
        assertTrue(importOnlyModule.getNamespace().equals("com:foo:test-module2"));
        assertTrue(importOnlyModule.getRevision() == null);
        assertTrue(importOnlyModule.getSchemaLocations().size() == 0);
        assertTrue(importOnlyModule.getFeatures().size() == 1);
        assertTrue(importOnlyModule.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);

        final Set<YangFeature> supportedFeatures = runningDatastore.getSupportedFeatures();

        assertTrue(supportedFeatures.size() == 4);
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature1")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature2")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature3")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module2", "test-module2", "feature6")));
    }

    @Test
    public void testRFC7895_json() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895.json"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibrary = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertNoFindings(ietfYangLibrary.getFindingsManager().getAllFindings());

        assertTrue(yangLibrary != null);
        assertTrue(yangLibrary.getContentId().equals("10445"));
        assertTrue(yangLibrary.containsTopLevelSchema() == true);

        final Datastore runningDatastore = yangLibrary.getRunningDatastore();
        assertTrue(runningDatastore != null);

        assertTrue(runningDatastore.getAllModules().size() == 3);
        assertTrue(runningDatastore.getImplementingModules().size() == 2);
        assertTrue(runningDatastore.getImportOnlyModules().size() == 1);

        final Module testModule1 = runningDatastore.getImplementingModules().stream().filter(m -> m.getName().equals(
                "test-module1")).findFirst().get();

        assertTrue(testModule1.getName().equals("test-module1"));
        assertTrue(testModule1.getNamespace().equals("com:foo:test-module1"));
        assertTrue(testModule1.getRevision().equals("2020-01-01"));
        assertTrue(testModule1.getSchemaLocations().size() == 1);
        assertTrue(testModule1.getSchemaLocations().get(0).equals("www.acme.com/test-module1.yang"));
        assertTrue(testModule1.getFeatures().size() == 3);
        assertTrue(testModule1.getFeatures().get(0).equals("feature1"));
        assertTrue(testModule1.getFeatures().get(1).equals("feature2"));
        assertTrue(testModule1.getFeatures().get(2).equals("feature3"));
        assertTrue(testModule1.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(testModule1.getSubmodules().size() == 1);
        assertTrue(testModule1.getSubmodules().get(0).getName().equals("test-module1-submodule"));
        assertTrue(testModule1.getSubmodules().get(0).getRevision().equals("2020-02-02"));
        assertTrue(testModule1.getSubmodules().get(0).getSchemaLocations().size() == 1);
        assertTrue(testModule1.getSubmodules().get(0).getSchemaLocations().get(0).equals(
                "www.acme.com/test-module1-submodule.yang"));
        assertTrue(testModule1.getDeviatedByModuleNames().size() == 1);
        assertTrue(testModule1.getDeviatedByModuleNames().get(0).equals("test-module1-ext"));

        final Module testModule1ext = runningDatastore.getImplementingModules().stream().filter(m -> m.getName().equals(
                "test-module1-ext")).findFirst().get();

        assertTrue(testModule1ext.getName().equals("test-module1-ext"));
        assertTrue(testModule1ext.getRevision().equals("2020-05-20"));
        assertTrue(testModule1ext.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);

        final Module importOnlyModule = runningDatastore.getImportOnlyModules().stream().findFirst().get();

        assertTrue(importOnlyModule.getName().equals("test-module2"));
        assertTrue(importOnlyModule.getNamespace().equals("com:foo:test-module2"));
        assertTrue(importOnlyModule.getRevision() == null);
        assertTrue(importOnlyModule.getSchemaLocations().size() == 0);
        assertTrue(importOnlyModule.getFeatures().size() == 1);
        assertTrue(importOnlyModule.getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);

        final Set<YangFeature> supportedFeatures = runningDatastore.getSupportedFeatures();

        assertTrue(supportedFeatures.size() == 4);
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature1")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature2")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module1", "test-module1", "feature3")));
        assertTrue(supportedFeatures.contains(new YangFeature("com:foo:test-module2", "test-module2", "feature6")));
    }

    @Test
    public void testRFC7895_json_modulesetid_is_number_instead_of_string() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895-modulesetid_is_number.json"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(yangDataFiles));

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(),
                ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA.toString());
    }

    @Test
    public void testRFC7895_with_issues() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895-with-issues.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(yangDataFiles));

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(),
                ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA.toString());
    }

    @Test
    public void testRFC7895_with_empty_names() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895-empty-names.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(yangDataFiles));

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(),
                ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString());
    }

    @Test
    public void testRFC8525() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibraryInstance = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertNoFindings(ietfYangLibrary.getFindingsManager().getAllFindings());

        assertTrue(yangLibraryInstance != null);
        assertTrue(yangLibraryInstance.getContentId().equals("9876"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getSchemaName().equals("schema1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().size() == 2);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getName().equals("set1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().size() == 2);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getName().equals("test-module1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getRevision().equals("2020-01-01"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getNamespace().equals("com:foo:test-module1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().size() == 2);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().get(0).equals("www.acme.com/test-module1.yang"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().get(1).equals("www.modules.acme.com/test-module1.yang"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getName().equals("test-module1-submodule"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getRevision() == null);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getModuleIdentity().equals(new ModuleIdentity("test-module1-submodule", null)));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().size() == 3);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(0).equals("feature1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(1).equals("feature2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(2).equals("feature3"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getDeviatedByModuleNames().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getDeviatedByModuleNames().get(0).equals("test-module1-ext"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getName().equals("test-module1-ext"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getRevision().equals("2020-03-03"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0).getName()
                .equals("test-module2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getRevision() == null);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getNamespace().equals("com:foo:test-module2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getSubmodules().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getFeatures().size() == 0);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getName().equals("set2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImplementingModules().size() == 0);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0).getName()
                .equals("test-module6"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getRevision().equals("2019-06-06"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getNamespace().equals("com:foo:test-module6"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getSubmodules().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getFeatures().size() == 0);

        final Datastore operDatastore = yangLibraryInstance.getDatastore(new IdentityRefValue(
                "urn:ietf:params:xml:ns:yang:ietf-datastores", "ietf-datastores", "operational"));
        assertTrue(operDatastore != null);

        assertTrue(operDatastore.getModuleSets().get(0).getName().equals("set2"));
        assertTrue(operDatastore.getModuleSets().get(1).getName().equals("set3"));

        final Datastore unknownDatastore = yangLibraryInstance.getDatastore(new IdentityRefValue(
                "urn:ietf:params:xml:ns:yang:ietf-datastores", "ietf-datastores", "unknown"));
        assertTrue(unknownDatastore == null);
    }

    @Test
    public void testRFC8525_json() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525.json"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibraryInstance = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertNoFindings(ietfYangLibrary.getFindingsManager().getAllFindings());

        assertTrue(yangLibraryInstance != null);
        assertTrue(yangLibraryInstance.getContentId().equals("9876"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getSchemaName().equals("schema1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().size() == 2);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getName().equals("set1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().size() == 2);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getName().equals("test-module1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getRevision().equals("2020-01-01"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getNamespace().equals("com:foo:test-module1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().size() == 2);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().get(0).equals("www.acme.com/test-module1.yang"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSchemaLocations().get(1).equals("www.modules.acme.com/test-module1.yang"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getName().equals("test-module1-submodule"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getRevision() == null);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getSubmodules().get(0).getModuleIdentity().equals(new ModuleIdentity("test-module1-submodule", null)));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().size() == 3);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(0).equals("feature1"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(1).equals("feature2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getFeatures().get(2).equals("feature3"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getDeviatedByModuleNames().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0)
                .getDeviatedByModuleNames().get(0).equals("test-module1-ext"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPLEMENT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getName().equals("test-module1-ext"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(1)
                .getRevision().equals("2020-03-03"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0).getName()
                .equals("test-module2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getRevision() == null);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getNamespace().equals("com:foo:test-module2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getSubmodules().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(0).getImportOnlyModules().get(0)
                .getFeatures().size() == 0);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getName().equals("set2"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImplementingModules().size() == 0);

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().size() == 1);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getConformanceType() == Module.IetfYangLibraryConformanceType.IMPORT);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0).getName()
                .equals("test-module6"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getRevision().equals("2019-06-06"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getNamespace().equals("com:foo:test-module6"));
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getSchemaLocations().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getSubmodules().size() == 0);
        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().get(1).getImportOnlyModules().get(0)
                .getFeatures().size() == 0);

        final Datastore operDatastore = yangLibraryInstance.getDatastore(new IdentityRefValue(
                "urn:ietf:params:xml:ns:yang:ietf-datastores", "ietf-datastores", "operational"));
        assertTrue(operDatastore != null);

        assertTrue(operDatastore.getModuleSets().get(0).getName().equals("set2"));
        assertTrue(operDatastore.getModuleSets().get(1).getName().equals("set3"));
    }

    @Test
    public void testRFC8525_no_schema_no_datastore() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525-no-schema-no-datastore.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibraryInstance = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertTrue(yangLibraryInstance != null);
        assertTrue(yangLibraryInstance.getContentId().equals("9876"));

        assertTrue(yangLibraryInstance.getRunningDatastore().getModuleSets().size() == 3);
    }

    @Test
    public void testRFC8525_with_issues() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525-with-issues.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(yangDataFiles));

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(),
                ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA.toString());
    }

    @Test
    public void testRFC8525_empty_names() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525-empty-names.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibraryInstance = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(),
                ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString());

        assertTrue(yangLibraryInstance != null);
    }

    @Test
    public void test_duplicate_modules() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525_duplicates.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibrary = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertTrue(yangLibrary != null);
    }

    @Test
    public void test_empty_data() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-empty.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        List<YangLibrary> yangLibrary = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles));

        assertHasFindingOfType(ietfYangLibrary.getFindingsManager().getAllFindings(), ParserFindingType.P079_EMPTY_DATA_FILE
                .toString());

        assertTrue(yangLibrary.isEmpty());
    }

    @Test
    public void test_with_other_data() {

        final List<File> yangDataFiles = Arrays.asList(new File(
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-7895_and_other_data.xml"));

        final IetfYangLibraryParser ietfYangLibrary = new IetfYangLibraryParser();
        final YangLibrary yangLibrary = ietfYangLibrary.parseIntoYangLibraries(new FileBasedYangInputResolver(
                yangDataFiles)).get(0);

        assertNoFindings(ietfYangLibrary.getFindingsManager().getAllFindings());

        assertTrue(yangLibrary != null);
        assertTrue(yangLibrary.getContentId().equals("10445"));
        assertTrue(yangLibrary.getRunningDatastore().getModuleSets().get(0).getImplementingModules().size() == 1);
        assertTrue(yangLibrary.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0).getName()
                .equals("test-module1"));
        assertTrue(yangLibrary.getRunningDatastore().getModuleSets().get(0).getImplementingModules().get(0).getRevision()
                .equals("2020-01-01"));
    }

    @Test
    public void test_multiple_yang_libraries_in_input() {

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/_orig-modules/ietf-yang-library-2019-01-04.yang",
                "src/test/resources/_orig-modules/ietf-yang-types-2019-11-04.yang",
                "src/test/resources/_orig-modules/ietf-inet-types-2019-11-04.yang",
                "src/test/resources/_orig-modules/ietf-datastores-2018-02-14.yang"));
        parseAbsoluteYangData(Arrays.asList("src/test/resources/yanglibrary/root-instance-data-set-RFC-7895.xml",
                "src/test/resources/yanglibrary/root-instance-data-set-RFC-8525.xml"));

        assertHasFindingOfType(ParserFindingType.P084_MULTIPLE_YANG_LIBRARIES_IN_INPUT.toString());
    }
}
