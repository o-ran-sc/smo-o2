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

import org.oran.smo.yangtools.parser.model.util.GrammarHelper;

public class GrammarHelperTest {

    @Test
    public void test_contains_yang_whitespace() {

        assertTrue(GrammarHelper.containsYangWhitespace(null) == false);
        assertTrue(GrammarHelper.containsYangWhitespace("") == false);

        assertTrue(GrammarHelper.containsYangWhitespace(" ") == true);
        assertTrue(GrammarHelper.containsYangWhitespace("\t") == true);

        assertTrue(GrammarHelper.containsYangWhitespace("Hello World!") == true);
        assertTrue(GrammarHelper.containsYangWhitespace(" HelloWorld!") == true);
        assertTrue(GrammarHelper.containsYangWhitespace("HelloWorld! ") == true);
        assertTrue(GrammarHelper.containsYangWhitespace("HelloWorld!") == false);

        assertTrue(GrammarHelper.containsYangWhitespace("Hello\tWorld!") == true);
        assertTrue(GrammarHelper.containsYangWhitespace("\tHelloWorld!") == true);
        assertTrue(GrammarHelper.containsYangWhitespace("HelloWorld!\t") == true);
    }

    @Test
    public void test_is_yang_identifier() {

        assertTrue(GrammarHelper.isYangIdentifier(null) == false);
        assertTrue(GrammarHelper.isYangIdentifier("") == false);
        assertTrue(GrammarHelper.isYangIdentifier(" ") == false);
        assertTrue(GrammarHelper.isYangIdentifier("\t") == false);

        assertTrue(GrammarHelper.isYangIdentifier("_") == true);
        assertTrue(GrammarHelper.isYangIdentifier("A") == true);
        assertTrue(GrammarHelper.isYangIdentifier("Z") == true);
        assertTrue(GrammarHelper.isYangIdentifier("a") == true);
        assertTrue(GrammarHelper.isYangIdentifier("z") == true);

        assertTrue(GrammarHelper.isYangIdentifier("-") == false);
        assertTrue(GrammarHelper.isYangIdentifier("0") == false);
        assertTrue(GrammarHelper.isYangIdentifier("9") == false);
        assertTrue(GrammarHelper.isYangIdentifier(".") == false);
        assertTrue(GrammarHelper.isYangIdentifier(";") == false);
        assertTrue(GrammarHelper.isYangIdentifier("$") == false);
        assertTrue(GrammarHelper.isYangIdentifier("*") == false);
        assertTrue(GrammarHelper.isYangIdentifier("\\") == false);
        assertTrue(GrammarHelper.isYangIdentifier("/") == false);
        assertTrue(GrammarHelper.isYangIdentifier(":") == false);
        assertTrue(GrammarHelper.isYangIdentifier("à") == false);

        assertTrue(GrammarHelper.isYangIdentifier("__") == true);
        assertTrue(GrammarHelper.isYangIdentifier("_._") == true);
        assertTrue(GrammarHelper.isYangIdentifier("_......._") == true);

        assertTrue(GrammarHelper.isYangIdentifier("ABCDEFGHIJKLMNOPQRSTUVWXYZ") == true);
        assertTrue(GrammarHelper.isYangIdentifier("abcdefghijklmnopqrstuvwxyz") == true);
        assertTrue(GrammarHelper.isYangIdentifier("_0123456789") == true);

        assertTrue(GrammarHelper.isYangIdentifier("_ABC") == true);
        assertTrue(GrammarHelper.isYangIdentifier("ABC_") == true);
        assertTrue(GrammarHelper.isYangIdentifier("_ABC.DEF") == true);
        assertTrue(GrammarHelper.isYangIdentifier("ABC.def") == true);

        assertTrue(GrammarHelper.isYangIdentifier(" ABC.def") == false);
        assertTrue(GrammarHelper.isYangIdentifier("ABC. def") == false);
        assertTrue(GrammarHelper.isYangIdentifier("ABC.def ") == false);
        assertTrue(GrammarHelper.isYangIdentifier("ABC.à.def") == false);
    }

    @Test
    public void test_is_yang_identifier_reference() {

        assertTrue(GrammarHelper.isYangIdentifierReference(null) == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference(" ") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("\t") == false);

        assertTrue(GrammarHelper.isYangIdentifierReference("_") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("A") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("Z") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("a") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("z") == true);

        assertTrue(GrammarHelper.isYangIdentifierReference(":") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference(":_") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("_:") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("::") == false);

        assertTrue(GrammarHelper.isYangIdentifierReference("A:A") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("_:A") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("A:_") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("A:a") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("z:A") == true);

        assertTrue(GrammarHelper.isYangIdentifierReference("Abc:Abc") == true);
        assertTrue(GrammarHelper.isYangIdentifierReference("A99:z00") == true);

        assertTrue(GrammarHelper.isYangIdentifierReference("Abc::Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc: :Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference(":Abc:Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc:Abc:") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc :Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc: Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc : Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference(" Abc:Abc") == false);
        assertTrue(GrammarHelper.isYangIdentifierReference("Abc:Abc ") == false);
    }

    @Test
    public void test_is_parse_to_string_list() {

        List<String> list = GrammarHelper.parseToStringList("");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList(" ");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList("     ");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList("\t");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList(" \t ");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList("\n");
        assertTrue(list.size() == 0);
        list = GrammarHelper.parseToStringList(" \n \t");
        assertTrue(list.size() == 0);

        list = GrammarHelper.parseToStringList("Abc");
        assertTrue(list.size() == 1);
        assertTrue(list.equals(Arrays.asList("Abc")));

        list = GrammarHelper.parseToStringList(" Abc");
        assertTrue(list.size() == 1);
        assertTrue(list.equals(Arrays.asList("Abc")));

        list = GrammarHelper.parseToStringList("Abc ");
        assertTrue(list.size() == 1);
        assertTrue(list.equals(Arrays.asList("Abc")));

        list = GrammarHelper.parseToStringList("Abc\n");
        assertTrue(list.size() == 1);
        assertTrue(list.equals(Arrays.asList("Abc")));

        list = GrammarHelper.parseToStringList("Abc Def");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("Abc\tDef");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("Abc\nDef");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("Abc   Def");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("  Abc Def");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("Abc Def\n");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", "Def")));

        list = GrammarHelper.parseToStringList("Abc :Def");
        assertTrue(list.size() == 2);
        assertTrue(list.equals(Arrays.asList("Abc", ":Def")));
    }

    @Test
    public void test_is_unquotable_string() {

        assertTrue(GrammarHelper.isUnquotableString("") == false);

        assertTrue(GrammarHelper.isUnquotableString("ABC") == true);
        assertTrue(GrammarHelper.isUnquotableString("a-yang-identifier") == true);

        assertTrue(GrammarHelper.isUnquotableString("// inline comment") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC // inline comment") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC /* start block comment") == false);
        assertTrue(GrammarHelper.isUnquotableString("/* start block comment") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC */ end block comment") == false);
        assertTrue(GrammarHelper.isUnquotableString("*/ end block comment") == false);

        assertTrue(GrammarHelper.isUnquotableString(" ") == false);
        assertTrue(GrammarHelper.isUnquotableString("\t") == false);
        assertTrue(GrammarHelper.isUnquotableString("\n") == false);
        assertTrue(GrammarHelper.isUnquotableString("\r") == false);
        assertTrue(GrammarHelper.isUnquotableString("'") == false);
        assertTrue(GrammarHelper.isUnquotableString("\"") == false);
        assertTrue(GrammarHelper.isUnquotableString(";") == false);
        assertTrue(GrammarHelper.isUnquotableString("{") == false);
        assertTrue(GrammarHelper.isUnquotableString("}") == false);
        assertTrue(GrammarHelper.isUnquotableString("+") == false);

        assertTrue(GrammarHelper.isUnquotableString("ABC DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC\tDEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC\nDEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC\rDEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC'DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC\"DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC;DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC{DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC}DEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABC+DEF") == false);

        assertTrue(GrammarHelper.isUnquotableString("ABCDEF ") == false);
        assertTrue(GrammarHelper.isUnquotableString("\tABCDEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("}ABCDEF") == false);
        assertTrue(GrammarHelper.isUnquotableString("ABCDEF+") == false);
    }

}
