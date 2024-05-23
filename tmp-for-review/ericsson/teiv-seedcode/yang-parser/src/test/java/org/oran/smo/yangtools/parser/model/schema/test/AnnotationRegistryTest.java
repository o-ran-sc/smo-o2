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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.schema.AnnotationRegistry;
import org.oran.smo.yangtools.parser.model.util.YangAnnotation;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class AnnotationRegistryTest extends YangTestCommon {

    @Test
    public void test_all_modules() {

        final List<String> absoluteImplementsFilePath = Arrays.asList(
                "src/test/resources/model-schema/annotation-registry-test/module1.yang",
                "src/test/resources/model-schema/annotation-registry-test/module2.yang", YANG_METADATA_PATH,
                YANG_ORIGIN_PATH);
        final List<String> absoluteImportsFilePath = Collections.<String> emptyList();

        parseAbsoluteYangModels(absoluteImplementsFilePath, absoluteImportsFilePath);

        assertNoFindings();

        final AnnotationRegistry annotationRegistry = yangDeviceModel.getTopLevelSchema().getAnnotationRegistry();

        final List<YangAnnotation> annotations = annotationRegistry.getAnnotations();

        assertTrue(annotations.contains(new YangAnnotation("test:module1", "module1", "created")));
        assertTrue(annotations.contains(new YangAnnotation("test:module1", "module1", "last-modified")));

        assertTrue(annotations.contains(new YangAnnotation("test:module2", "module2", "modified-user")));

        assertTrue(annotations.contains(new YangAnnotation("urn:ietf:params:xml:ns:yang:ietf-origin", "ietf-origin",
                "origin")));

        assertTrue(annotations.size() == 4);
    }

}
