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
package org.oran.smo.yangtools.parser.input.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import org.oran.smo.yangtools.parser.input.FileBasedYangInput;

public class FileBasedYangInputTest {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_all_ok() {

        final File file = new File("src/test/resources/basics/yang-input-test/module1-2020-01-01.yang");

        final FileBasedYangInput input1 = new FileBasedYangInput(file);

        assertTrue(input1.getFile() == file);
        assertTrue(input1.getName().equals("module1-2020-01-01.yang"));
        assertTrue(input1.getInputStream() != null);

        final FileBasedYangInput input2 = new FileBasedYangInput(file);
        assertTrue(input1.equals(input1));
        assertTrue(input1.equals(input2));
        assertFalse(input1.equals(null));
        assertFalse(input1.equals(""));
        assertFalse(input1.equals(new File("otherFile")));
    }

    @Test
    public void test_failures() {

        try {
            new FileBasedYangInput(null);
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new FileBasedYangInput(new File("unknown file"));
            fail();
        } catch (final Throwable th) {
            /* ignore */}

        try {
            new FileBasedYangInput(new File("src/test"));
            fail();
        } catch (final Throwable th) {
            /* ignore */}
    }

}
