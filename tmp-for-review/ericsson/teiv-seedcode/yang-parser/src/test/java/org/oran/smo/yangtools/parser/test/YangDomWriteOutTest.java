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
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YAction;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.yangdom.OriginalFileNameOutputFileNameResolver;
import org.oran.smo.yangtools.parser.model.yangdom.OutputFileNameResolver;
import org.oran.smo.yangtools.parser.model.yangdom.OutputStreamResolver;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class YangDomWriteOutTest extends YangTestCommon {

    private static final String OUT_DIR = "target/test-output/out-files";

    @Test
    public void testSimpleWriteOut() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));

        final YModule module = getModule("module1");
        assertTrue(module != null);

        // write out with default resolver

        createCleanTargetDir();
        yangDeviceModel.getTopLevelSchema().writeOut(new File(OUT_DIR));

        assertTrue(new File(OUT_DIR, "module1-2020-09-30.yang").exists());

        // write out with file-name resolver

        createCleanTargetDir();
        yangDeviceModel.getTopLevelSchema().writeOut(new File(OUT_DIR), new OriginalFileNameOutputFileNameResolver());

        assertTrue(new File(OUT_DIR, "module1.yang").exists());

        // write out with custom resolver

        yangDeviceModel.getTopLevelSchema().writeOut(new File(OUT_DIR), new OutputFileNameResolver() {
            @Override
            public String getOutputFileNameForYangInput(YangModel yangModelInput) {
                return "out-module.yang";
            }
        });

        assertTrue(new File(OUT_DIR, "out-module.yang").exists());

        // read back in the module and make sure content is correct.

        setUp();
        parseAbsoluteImplementsYangModels(Arrays.asList(new File(OUT_DIR, "out-module.yang").getAbsolutePath()));

        final YModule module1 = getModule("module1");
        assertTrue(module1 != null);

        assertTrue(module1.getNamespace().getNamespace().equals("test:module1"));
        assertTrue(module1.getRevisions().get(0).getValue().equals("2020-09-30"));

        final YContainer cont1 = getContainer(module1, "cont1");
        assertTrue(cont1 != null);

        final YAction action11 = getChild(cont1, CY.ACTION, "action11");
        assertTrue(action11 != null);

        final YContainer cont112 = action11.getInput().getContainers().get(0);
        assertTrue(cont112 != null);
    }

    @Test
    public void testSimpleStreamOut() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));

        final YModule module = getModule("module1");
        assertTrue(module != null);

        // write out to stream

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final OutputStreamResolver resolver = new OutputStreamResolver() {
            @Override
            public OutputStream getOutputStreamForYangInput(YangModel yangModelInput) {
                return os;
            }
        };

        yangDeviceModel.getTopLevelSchema().writeOut(resolver);

        final byte[] streamResult = os.toByteArray();
        assertTrue(streamResult.length > 0);

        // write out the module to file

        createCleanTargetDir();
        yangDeviceModel.getTopLevelSchema().writeOut(new File(OUT_DIR), new OriginalFileNameOutputFileNameResolver());

        assertTrue(new File(OUT_DIR, "module1.yang").exists());

        // contents of file and byte[] must match exactly

        final FileInputStream fis = new FileInputStream(new File(OUT_DIR, "module1.yang"));
        final ByteArrayOutputStream fileBuffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[100000];

        while ((nRead = fis.read(data, 0, data.length)) != -1) {
            fileBuffer.write(data, 0, nRead);
        }
        fis.close();
        final byte[] fileContents = fileBuffer.toByteArray();

        assertTrue(streamResult.length == fileContents.length);
    }

    @Test
    public void testWriteOutToFileChangedOnly() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));

        final YModule module = getModule("module1");
        assertTrue(module != null);

        final YContainer cont1 = module.getContainers().get(0);
        cont1.getYangModelRoot().getDomDocumentRoot().setDomHasBeenModified();

        createCleanTargetDir();
        final List<YangModel> writeOutChanged = yangDeviceModel.getTopLevelSchema().writeOutChanged(new File(OUT_DIR),
                new OriginalFileNameOutputFileNameResolver());

        assertTrue(new File(OUT_DIR, "module1.yang").exists());
        assertTrue(writeOutChanged.size() == 1);
    }

    @Test
    public void testWriteOutToStreamChangedOnly() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));

        final YModule module = getModule("module1");
        assertTrue(module != null);

        final YContainer cont1 = module.getContainers().get(0);
        cont1.getYangModelRoot().getDomDocumentRoot().setDomHasBeenModified();

        // write out to stream

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final OutputStreamResolver resolver = new OutputStreamResolver() {
            @Override
            public OutputStream getOutputStreamForYangInput(YangModel yangModelInput) {
                return os;
            }
        };

        final List<YangModel> writeOutChanged = yangDeviceModel.getTopLevelSchema().writeOutChanged(resolver);
        assertTrue(writeOutChanged.size() == 1);

        final byte[] streamResult = os.toByteArray();
        assertTrue(streamResult.length > 0);
    }

    @Test
    public void testWriteOutChangedOnlyButModuleNotChanged() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));

        final YModule module = getModule("module1");
        assertTrue(module != null);

        // Don't mark as modified, should not be written out.

        //    	final YContainer cont1 = module.getContainers().get(0);
        //    	cont1.getYangModelRoot().getDomDocumentRoot().setDomHasBeenModified();

        createCleanTargetDir();
        final List<YangModel> writeOutChanged = yangDeviceModel.getTopLevelSchema().writeOutChanged(new File(OUT_DIR),
                new OriginalFileNameOutputFileNameResolver());

        assertFalse(new File(OUT_DIR, "module1.yang").exists());
        assertTrue(writeOutChanged.size() == 0);
    }

    @Test
    public void testWeirdStringsAndCharactersWriteOut() throws IOException {

        parseAbsoluteImplementsYangModels(Arrays.asList("src/test/resources/basics/yangdom-write-out-test/module1.yang"));
        assertHasFindingOfType(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertHasFindingOfType(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString());

        final YModule module = getModule("module1");
        assertTrue(module != null);

        checkDescriptions(module);

        // write out with file-name resolver

        createCleanTargetDir();
        yangDeviceModel.getTopLevelSchema().writeOut(new File(OUT_DIR), new OriginalFileNameOutputFileNameResolver());

        assertTrue(new File(OUT_DIR, "module1.yang").exists());

        // read back in the module and make sure content is correct.

        setUp();
        parseAbsoluteImplementsYangModels(Arrays.asList(new File(OUT_DIR, "module1.yang").getAbsolutePath()));

        /*
         * The writing-out of the strings should have cleaned up any illegal escaping, so we shouldn't see this again.
         */
        assertHasFindingOfType(ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE.toString());

        final YModule module1 = getModule("module1");
        assertTrue(module1 != null);

        checkDescriptions(module1);
    }

    private void checkDescriptions(final YModule module) {
        final YContainer cont2 = getContainer(module, "cont2");
        assertTrue(cont2 != null);

        final YLeaf leaf21 = getLeaf(cont2, "leaf21");
        assertTrue(leaf21.getDescription().getValue().equals("simple string with space character"));

        final YLeaf leaf22 = getLeaf(cont2, "leaf22");
        assertTrue(leaf22.getDescription().getValue().equals("simple string\twith tab character"));

        final YLeaf leaf23 = getLeaf(cont2, "leaf23");
        assertTrue(leaf23.getDescription().getValue().equals("simple string with \" escaped quote character"));

        final YLeaf leaf24 = getLeaf(cont2, "leaf24");
        assertTrue(leaf24.getDescription().getValue().equals("simple string with \\' escaped single-quote character"));

        final YLeaf leaf25 = getLeaf(cont2, "leaf25");
        assertTrue(leaf25.getDescription().getValue().equals("exact characters\ttab and quote \""));

        final YLeaf leaf26 = getLeaf(cont2, "leaf26");
        assertTrue(leaf26.getDescription().getValue().equals(
                "exact characters stretching over\n   multiple lines - note three spaces at start of line"));

        final YLeaf leaf27 = getLeaf(cont2, "leaf27");
        assertTrue(leaf27.getDescription().getValue().equals("+"));

        final YLeaf leaf28 = getLeaf(cont2, "leaf28");
        assertTrue(leaf28.getDescription().getValue().equals(";"));

        final YLeaf leaf29 = getLeaf(cont2, "leaf29");
        assertTrue(leaf29.getDescription().getValue().equals(";;"));

        final YLeaf leaf30 = getLeaf(cont2, "leaf30");
        assertTrue(leaf30.getDescription().getValue().equals("Hello+World!"));

        final YLeaf leaf31 = getLeaf(cont2, "leaf31");
        assertTrue(leaf31.getDescription().getValue().equals("on next line"));

        final YLeaf leaf32 = getLeaf(cont2, "leaf32");
        assertTrue(leaf32.getDescription().getValue().equals("some on this line some on the next line"));

        final YLeaf leaf33 = getLeaf(cont2, "leaf33");
        assertTrue(leaf33.getDescription().getValue().equals("First paragraph. \nSecond paragraph."));

        final YLeaf leaf34 = getLeaf(cont2, "leaf34");
        assertTrue(leaf34.getDescription().getValue().equals("this_is_//_not_a_comment"));

        final YLeaf leaf35 = getLeaf(cont2, "leaf35");
        assertTrue(leaf35.getDescription().getValue().equals("this_is_/*_not_a_comment"));

        final YLeaf leaf36 = getLeaf(cont2, "leaf36");
        assertTrue(leaf36.getDescription().getValue().equals("this_is_*/_not_a_comment"));

        final YLeaf leaf37 = getLeaf(cont2, "leaf37");
        assertTrue(leaf37.getDescription().getValue().equals(""));

        final YLeaf leaf38 = getLeaf(cont2, "leaf38");
        assertTrue(leaf38.getDescription().getValue().equals("{"));

        final YLeaf leaf39 = getLeaf(cont2, "leaf39");
        assertTrue(leaf39.getDescription().getValue().equals("}"));

        final YLeaf leaf40 = getLeaf(cont2, "leaf40");
        assertTrue(leaf40.getDescription().getValue().equals("\""));
    }

    private void createCleanTargetDir() {

        final File dir = new File(OUT_DIR);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        final File[] listFiles = dir.listFiles();
        for (final File file : listFiles) {
            file.delete();
        }
    }
}
