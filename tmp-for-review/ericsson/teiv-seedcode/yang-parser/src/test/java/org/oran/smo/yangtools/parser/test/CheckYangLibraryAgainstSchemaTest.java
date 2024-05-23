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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.instance.DataTreeBuilderPredicate;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInputResolver;
import org.oran.smo.yangtools.parser.input.YangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class CheckYangLibraryAgainstSchemaTest extends YangTestCommon {

    private static final String ROOT = "src/test/resources/basics/check-yl-against-schema/";

    private static final List<File> SIMPLE_MODULE = Collections.singletonList(new File(ROOT + "simple-module.yang"));

    private static final List<File> YANG_LIBRARY_MODULE_AND_DEPENDENCIES = Arrays.asList(new File(
            ROOT + "yang-library-module-and-dependencies"));

    private static final List<File> YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE = Arrays.asList(new File(
            ROOT + "yang-library-module-and-dependencies"), new File(ROOT + "simple-module.yang"));

    @Test
    public void test_make_sure_models_are_ok() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();
        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }
        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        assertNoFindings();
    }

    @Test
    public void test___yang_lib___input_simple_and_dependencies___yl_only_lists_simple() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-simple-module-only.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * The YL and dependencies are not listed in the data, so we expect findings for each of the dependent models (4x).
         */
        assertHasFindingOfType(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY.toString());
        assertFindingCount(4);
    }

    @Test
    public void test___yang_lib___input_simple_and_dependencies___yl_lists_all() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * All should be good - all modules supplied, all inside the data file.
         */
        assertNoFindings();
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple_and_dependencies() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * The YL-related modules are listed in the data, but don't exist in the input. Mismatch.
         */
        assertHasFindingOfType(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY.toString());
        assertFindingCount(4);
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-simple-module-only.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * Only the module listed in the YL, and only the module is in the input.
         */
        assertNoFindings();
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple___conformance_mismatch() {

        final Set<YangInput> resolvedYangInput1 = new FileBasedYangInputResolver(YANG_LIBRARY_MODULE_AND_DEPENDENCIES)
                .getResolvedYangInput();
        final Set<YangInput> resolvedYangInput2 = new FileBasedYangInputResolver(SIMPLE_MODULE).getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput1) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }
        for (final YangInput absoluteImplements : resolvedYangInput2) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPORT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed-conformance-mismatch.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * The conformance type for the "simple module" is wrong. Note that 2 findings will be issued for mismatches...
         */
        assertHasFindingOfType(ParserFindingType.P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY.toString());
        assertFindingCount(2);
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple___namespace_mismatch() {

        final Set<YangInput> resolvedYangInput1 = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE).getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();
        for (final YangInput absoluteImplements : resolvedYangInput1) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed-namespace-mismatch.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * The namespace for the "simple module" is wrong.
         */
        assertHasFindingOfType(ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA.toString());
        assertFindingCount(1);
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple___feature_mismatch() {

        final Set<YangInput> resolvedYangInput1 = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE).getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();
        for (final YangInput absoluteImplements : resolvedYangInput1) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed-feature-mismatch.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * The feature for the "simple module" is wrong.
         */
        assertHasFindingOfType(ParserFindingType.P083_FEATURE_LISTED_IN_YANG_LIBRARY_NOT_FOUND.toString());
        assertFindingCount(1);
    }

    @Test
    public void test___yang_lib___input_simple___yl_lists_simple___features_ok() {

        final Set<YangInput> resolvedYangInput1 = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE).getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();
        for (final YangInput absoluteImplements : resolvedYangInput1) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        // - - - - - - - - -

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-yang-library-all-listed-features-ok.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        assertNoFindings();
    }

    @Test
    public void test_all_supplied___module_data_only() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(
                YANG_LIBRARY_MODULE_AND_DEPENDENCIES_AND_SIMPLE_MODULE);
        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final YangInput absoluteImplements : resolvedYangInput) {
            yangFiles.add(new YangModel(absoluteImplements, ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        final FileBasedYangInputResolver dataResolver = new FileBasedYangInputResolver(Collections.singletonList(new File(
                ROOT + "data-module-data-only.xml")));

        yangDeviceModel.parseYangData(context, dataResolver, DataTreeBuilderPredicate.ALLOW_ALL);

        /*
         * No YL data supplied at all. The data should parse fine. Should be no findings.
         */
        assertNoFindings();
    }

}
