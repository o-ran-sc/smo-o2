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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import org.oran.smo.yangtools.parser.input.FileBasedYangInputResolver;
import org.oran.smo.yangtools.parser.input.YangInput;

public class FileBasedYangInputResolverTest {

    private static final String ROOT = "src/test/resources/basics/file-based-resolver-test";

    private static final String FILE1 = ROOT + "/folder1/file1.yang";
    private static final String FILE2 = ROOT + "/folder1/file2.xml";
    private static final String FILE3 = ROOT + "/folder1/file3.txt";

    private static final String FILE4 = ROOT + "/folder2/file4.yang";
    private static final String FILE5 = ROOT + "/folder2/file5";

    private static final String DOES_NOT_EXISTS = ROOT + "/does-not-exist.yang";

    @Test
    public void test_ok_all() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT)));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 5);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE3);
        assertContainsFile(resolvedYangInput, FILE4);
        assertContainsFile(resolvedYangInput, FILE5);
    }

    @Test
    public void test_ok_folder1() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(
                ROOT + "/folder1")));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 3);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE3);
    }

    @Test
    public void test_ok_root_supplied_twice() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT), new File(
                ROOT)));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 5);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE3);
        assertContainsFile(resolvedYangInput, FILE4);
        assertContainsFile(resolvedYangInput, FILE5);
    }

    @Test
    public void test_ok_root_and_folder1_supplied() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT), new File(
                ROOT + "/folder1")));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 5);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE3);
        assertContainsFile(resolvedYangInput, FILE4);
        assertContainsFile(resolvedYangInput, FILE5);
    }

    @Test
    public void test_ok_folder1_and_folder2_supplied() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(
                ROOT + "/folder2"), new File(ROOT + "/folder1")));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 5);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE3);
        assertContainsFile(resolvedYangInput, FILE4);
        assertContainsFile(resolvedYangInput, FILE5);
    }

    @Test
    public void test_ok_yang_files() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT)), Arrays
                .asList(FileBasedYangInputResolver.FILE_EXTENSION_YANG));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 2);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE4);
    }

    @Test
    public void test_ok_yang_files_uppercase() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT)), Arrays
                .asList(FileBasedYangInputResolver.FILE_EXTENSION_YANG));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 2);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE4);
    }

    @Test
    public void test_ok_yang_and_xml_files() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(ROOT)), Arrays
                .asList(FileBasedYangInputResolver.FILE_EXTENSION_YANG, FileBasedYangInputResolver.FILE_EXTENSION_XML));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 3);

        assertContainsFile(resolvedYangInput, FILE1);
        assertContainsFile(resolvedYangInput, FILE2);
        assertContainsFile(resolvedYangInput, FILE4);
    }

    @Test
    public void test_err_file_does_not_exist() {

        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(Arrays.asList(new File(
                DOES_NOT_EXISTS)));

        final Set<YangInput> resolvedYangInput = resolver.getResolvedYangInput();
        assertTrue(resolvedYangInput.size() == 0);
    }

    private void assertContainsFile(Set<YangInput> resolvedYangInput, final String path) {

        final File sought = new File(path);

        for (final YangInput input : resolvedYangInput) {
            if (input.getFile().equals(sought)) {
                return;
            }
        }

        fail("Does not contain file " + path);
    }

}
