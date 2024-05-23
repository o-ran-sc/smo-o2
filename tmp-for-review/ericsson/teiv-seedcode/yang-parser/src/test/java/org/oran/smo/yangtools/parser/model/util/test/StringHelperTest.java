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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.util.StringHelper;

public class StringHelperTest {

    //    @Test
    //    public void test_to_double_quoted_string(){
    //
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("").equals("\"\""));
    //    	assertTrue(StringHelper.convertToDoubleQuotedString(" ").equals("\" \""));
    //
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("ABC").equals("\"ABC\""));
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("ABC ").equals("\"ABC \""));
    //    	assertTrue(StringHelper.convertToDoubleQuotedString(" ABC ").equals("\" ABC \""));
    //
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("AB\"C").equals("\"AB\\\"C\""));	// AB"C   -->   AB\"C
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("AB\nC").equals("\"AB\\nC\""));		// AB<CR>C   -->   AB\nC
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("AB\tC").equals("\"AB\\tC\""));		// AB<TAB>C   -->   AB\tC
    //    	assertTrue(StringHelper.convertToDoubleQuotedString("AB\\C").equals("\"AB\\\\C\""));		// AB\C   -->   AB\\C
    //    }

    @Test
    public void test_to_module_revision() {

        assertTrue(StringHelper.getModuleNameAndRevision("module1", null).equals("'module1'"));
        assertTrue(StringHelper.getModuleNameAndRevision("module1", "").equals("'module1'"));
        assertTrue(StringHelper.getModuleNameAndRevision("module1", "revision1").equals("'module1/revision1'"));
    }

    @Test
    public void test_list_to_string() {

        final List<String> list = Arrays.asList("ABC", "DEF");

        assertTrue(StringHelper.toString(list, null, null, null, null, null).equals("ABCDEF"));
        assertTrue(StringHelper.toString(list, "", "", "", "", "").equals("ABCDEF"));

        assertTrue(StringHelper.toString(list, "[", "]", "", "", "").equals("[ABCDEF]"));
        assertTrue(StringHelper.toString(list, "[", "]", ",", "", "").equals("[ABC,DEF]"));
        assertTrue(StringHelper.toString(list, "[", "]", ",", "{", "}").equals("[{ABC},{DEF}]"));

    }

}
