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
package org.oran.smo.yangtools.parser.model.util.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.util.YangAnnotation;

public class YangAnnotationTest {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_all_ok() {

        final YangAnnotation YangAnnotation1 = new YangAnnotation("namespace1", "module1", "name1");
        final YangAnnotation YangAnnotation2 = new YangAnnotation("namespace1", "module1", "name1");

        assertTrue(YangAnnotation1.equals(YangAnnotation1));
        assertTrue(YangAnnotation1.equals(YangAnnotation2));
        assertTrue(YangAnnotation2.equals(YangAnnotation1));
        assertFalse(YangAnnotation1.equals(null));
        assertFalse(YangAnnotation1.equals("whatever"));
        assertFalse(YangAnnotation1.equals(new YangAnnotation("namespace2", "module2", "name2")));

        final YangAnnotation YangAnnotation3 = new YangAnnotation("namespace1", "module1", "name2");
        assertTrue(YangAnnotation3.getAnnotationName().equals("name2"));
        assertTrue(YangAnnotation3.getAnnotationModuleName().equals("module1"));
        assertTrue(YangAnnotation3.getAnnotationNamespace().equals("namespace1"));
    }

    @Test
    public void test_failures() {

        try {
            new YangAnnotation(null, null, null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new YangAnnotation("ns", null, null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new YangAnnotation(null, "name", null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}
    }

}
