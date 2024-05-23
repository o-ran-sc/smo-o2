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
package org.oran.smo.yangtools.parser.data.parser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.parser.JsonParser;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonArray;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObjectMemberName;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonPrimitive;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonValue;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class JsonParserTest extends YangTestCommon {

    private static final String DIR = "src/test/resources/data-parser/";

    @Test
    public void test_ok_json1() {

        final Object jsonRoot = parseJsonFile("json-ok1.json");

        assertNoFindings();

        assertTrue(jsonRoot instanceof JsonObject);
        final JsonObject jsonRootObject = (JsonObject) jsonRoot;

        final JsonValue attr1 = getJsonObjectMemberValue(jsonRootObject, "attr1");
        assertTrue(attr1 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr1).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr1).getValue()).equals("some string"));

        final JsonValue attr2 = getJsonObjectMemberValue(jsonRootObject, "attr2");
        assertTrue(attr2 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr2).getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) attr2).getValue()).equals(Boolean.TRUE));

        final JsonValue attr3 = getJsonObjectMemberValue(jsonRootObject, "attr3");
        assertTrue(attr3 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr3).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) attr3).getValue()).intValue() == 12);

        final JsonValue attr4 = getJsonObjectMemberValue(jsonRootObject, "attr4");
        assertTrue(attr4 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr4).getValue() == null);

        final JsonValue attr5 = getJsonObjectMemberValue(jsonRootObject, "attr5");
        assertTrue(attr5 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr5).getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) attr5).getValue()).equals(Boolean.FALSE));

        final JsonValue attr6 = getJsonObjectMemberValue(jsonRootObject, "attr6");
        assertTrue(attr6 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr6).getValue() instanceof Double);
        assertTrue(((Double) ((JsonPrimitive) attr6).getValue()).doubleValue() == 123456789.123456789d);

        final JsonValue attr7 = getJsonObjectMemberValue(jsonRootObject, "attr7");
        assertTrue(attr7 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr7).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) attr7).getValue()).longValue() == 1234567890123L);

        final JsonValue attr8 = getJsonObjectMemberValue(jsonRootObject, "attr8");
        assertTrue(attr8 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr8).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) attr8).getValue()).longValue() == Long.MAX_VALUE);

        final JsonValue attr9 = getJsonObjectMemberValue(jsonRootObject, "attr9");
        assertTrue(attr9 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr9).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr9).getValue()).equals("9223372036854775808"));

        final JsonValue attr10 = getJsonObjectMemberValue(jsonRootObject, "attr10");
        assertTrue(attr10 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr10).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) attr10).getValue()).longValue() == Long.MIN_VALUE);

        final JsonValue attr11 = getJsonObjectMemberValue(jsonRootObject, "attr11");
        assertTrue(attr11 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr11).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr11).getValue()).equals("-9223372036854775809"));

        // ======================

        final JsonValue attr12 = getJsonObjectMemberValue(jsonRootObject, "attr12");
        assertTrue(attr12 instanceof JsonObject);
        assertTrue(((JsonObject) attr12).getValuesByMember().size() == 0);

        final JsonValue attr13 = getJsonObjectMemberValue(jsonRootObject, "attr13");
        assertTrue(attr13 instanceof JsonObject);
        assertTrue(((JsonObject) attr13).getValuesByMember().size() == 2);

        final JsonObject attr12Object = (JsonObject) attr13;

        final JsonValue attr17 = getJsonObjectMemberValue(attr12Object, "attr17");
        assertTrue(attr17 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr17).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr17).getValue()).equals("hello"));

        final JsonValue attr18 = getJsonObjectMemberValue(attr12Object, "module:attr18");
        assertTrue(attr18 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr18).getValue() instanceof Double);
        assertTrue(((Double) ((JsonPrimitive) attr18).getValue()).doubleValue() == -1d);

        // ======================

        final JsonValue attr21 = getJsonObjectMemberValue(jsonRootObject, "attr21");
        assertTrue(attr21 instanceof JsonArray);
        assertTrue(((JsonArray) attr21).getValues().size() == 0);

        final JsonValue attr22 = getJsonObjectMemberValue(jsonRootObject, "attr22");
        assertTrue(attr22 instanceof JsonArray);
        assertTrue(((JsonArray) attr22).getValues().size() == 3);

        assertTrue(((JsonArray) attr22).getValues().get(0) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr22).getValues().get(0)).getValue() instanceof Double);
        assertTrue(((Double) ((JsonPrimitive) ((JsonArray) attr22).getValues().get(0)).getValue()).doubleValue() == 1d);

        assertTrue(((JsonArray) attr22).getValues().get(1) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr22).getValues().get(1)).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) ((JsonArray) attr22).getValues().get(1)).getValue()).longValue() == 2L);

        assertTrue(((JsonArray) attr22).getValues().get(2) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr22).getValues().get(2)).getValue() instanceof Double);
        assertTrue(((Double) ((JsonPrimitive) ((JsonArray) attr22).getValues().get(2)).getValue()).doubleValue() == -99d);
    }

    @Test
    public void test_ok_json2() {

        final Object jsonRoot = parseJsonFile("json-ok2.json");

        assertNoFindings();

        assertTrue(jsonRoot instanceof JsonObject);
        final JsonObject jsonRootObject = (JsonObject) jsonRoot;

        final JsonValue attr1 = getJsonObjectMemberValue(jsonRootObject, "attr1");
        assertTrue(attr1 instanceof JsonArray);
        assertTrue(((JsonArray) attr1).getValues().size() == 0);

        final JsonValue attr2 = getJsonObjectMemberValue(jsonRootObject, "attr2");
        assertTrue(attr2 instanceof JsonArray);
        assertTrue(((JsonArray) attr2).getValues().size() == 0);

        final JsonValue attr3 = getJsonObjectMemberValue(jsonRootObject, "attr3");
        assertTrue(attr3 instanceof JsonArray);
        assertTrue(((JsonArray) attr3).getValues().size() == 0);

        // ======================

        final JsonValue attr4 = getJsonObjectMemberValue(jsonRootObject, "attr4");
        assertTrue(attr4 instanceof JsonArray);
        assertTrue(((JsonArray) attr4).getValues().size() == 5);

        assertTrue(((JsonArray) attr4).getValues().get(0) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr4).getValues().get(0)).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) ((JsonArray) attr4).getValues().get(0)).getValue()).equals("hello"));

        assertTrue(((JsonArray) attr4).getValues().get(1) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr4).getValues().get(1)).getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) ((JsonArray) attr4).getValues().get(1)).getValue()).booleanValue() == true);

        assertTrue(((JsonArray) attr4).getValues().get(2) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr4).getValues().get(2)).getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) ((JsonArray) attr4).getValues().get(2)).getValue()).booleanValue() == false);

        assertTrue(((JsonArray) attr4).getValues().get(3) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr4).getValues().get(3)).getValue() == null);

        assertTrue(((JsonArray) attr4).getValues().get(4) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr4).getValues().get(4)).getValue() instanceof Double);
        assertTrue(((Double) ((JsonPrimitive) ((JsonArray) attr4).getValues().get(4)).getValue())
                .doubleValue() == 123456789.123456789d);

        // ======================

        final JsonValue attr5 = getJsonObjectMemberValue(jsonRootObject, "attr5");
        assertTrue(attr5 instanceof JsonArray);
        assertTrue(((JsonArray) attr5).getValues().size() == 4);

        assertTrue(((JsonArray) attr5).getValues().get(0) instanceof JsonArray);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(0)).getValues().size() == 1);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(0)).getValues().get(0) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) ((JsonArray) attr5).getValues().get(0)).getValues().get(0))
                .getValue() == null);

        assertTrue(((JsonArray) attr5).getValues().get(1) instanceof JsonArray);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().size() == 2);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().get(0) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().get(0))
                .getValue() == null);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().get(1) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().get(1))
                .getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) ((JsonArray) ((JsonArray) attr5).getValues().get(1)).getValues().get(1))
                .getValue()).booleanValue() == true);

        assertTrue(((JsonArray) attr5).getValues().get(2) instanceof JsonArray);
        assertTrue(((JsonArray) ((JsonArray) attr5).getValues().get(2)).getValues().size() == 0);

        assertTrue(((JsonArray) attr5).getValues().get(3) instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) ((JsonArray) attr5).getValues().get(3)).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) ((JsonArray) attr5).getValues().get(3)).getValue()).equals("hello"));

        // ======================

        final JsonValue attr6 = getJsonObjectMemberValue(jsonRootObject, "attr6");
        assertTrue(attr6 instanceof JsonArray);
        assertTrue(((JsonArray) attr6).getValues().size() == 2);

        assertTrue(((JsonArray) attr6).getValues().get(0) instanceof JsonArray);
        assertTrue(((JsonArray) ((JsonArray) attr6).getValues().get(0)).getValues().size() == 0);

        assertTrue(((JsonArray) attr6).getValues().get(1) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr6).getValues().get(1)).getValuesByMember().size() == 0);

        // ======================

        final JsonValue attr7 = getJsonObjectMemberValue(jsonRootObject, "attr7");
        assertTrue(attr7 instanceof JsonArray);
        assertTrue(((JsonArray) attr7).getValues().size() == 2);

        assertTrue(((JsonArray) attr7).getValues().get(0) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr7).getValues().get(0)).getValuesByMember().size() == 0);

        assertTrue(((JsonArray) attr7).getValues().get(1) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr7).getValues().get(1)).getValuesByMember().size() == 0);

        // ======================

        final JsonValue attr8 = getJsonObjectMemberValue(jsonRootObject, "attr8");
        assertTrue(attr8 instanceof JsonArray);
        assertTrue(((JsonArray) attr8).getValues().size() == 2);

        assertTrue(((JsonArray) attr8).getValues().get(0) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr8).getValues().get(0)).getValuesByMember().size() == 2);

        final JsonValue attr21 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr8).getValues().get(0), "attr21");
        assertTrue(attr21 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr21).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr21).getValue()).equals("hello"));

        final JsonValue attr22 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr8).getValues().get(0), "attr22");
        assertTrue(attr22 instanceof JsonArray);
        assertTrue(((JsonArray) attr22).getValues().size() == 0);

        assertTrue(((JsonArray) attr8).getValues().get(1) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr8).getValues().get(1)).getValuesByMember().size() == 0);

        // ======================

        final JsonValue attr9 = getJsonObjectMemberValue(jsonRootObject, "attr9");
        assertTrue(attr9 instanceof JsonArray);
        assertTrue(((JsonArray) attr9).getValues().size() == 2);
        assertTrue(((JsonArray) attr9).getValues().get(0) instanceof JsonObject);
        assertTrue(((JsonArray) attr9).getValues().get(1) instanceof JsonObject);
        assertTrue(((JsonObject) ((JsonArray) attr9).getValues().get(0)).getValuesByMember().size() == 2);
        assertTrue(((JsonObject) ((JsonArray) attr9).getValues().get(1)).getValuesByMember().size() == 0);

        final JsonValue attr31 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr9).getValues().get(0), "attr31");
        assertTrue(attr31 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr31).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) attr31).getValue()).equals("hello"));

        final JsonValue attr32 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr9).getValues().get(0), "attr32");
        assertTrue(attr32 instanceof JsonArray);
        assertTrue(((JsonArray) attr32).getValues().size() == 2);
        assertTrue(((JsonArray) attr32).getValues().get(0) instanceof JsonObject);
        assertTrue(((JsonArray) attr32).getValues().get(1) instanceof JsonObject);

        final JsonValue attr33 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr32).getValues().get(1), "attr33");
        assertTrue(attr33 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr33).getValue() instanceof Boolean);
        assertTrue(((Boolean) ((JsonPrimitive) attr33).getValue()).booleanValue() == true);

        final JsonValue attr34 = getJsonObjectMemberValue((JsonObject) ((JsonArray) attr32).getValues().get(1), "attr34");
        assertTrue(attr34 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr34).getValue() instanceof Long);
        assertTrue(((Long) ((JsonPrimitive) attr34).getValue()).longValue() == -99999999999L);
    }

    @Test
    public void test_ok_json3() {

        final Object jsonRoot = parseJsonFile("json-ok3.json");

        assertNoFindings();

        assertTrue(jsonRoot instanceof JsonObject);
        final JsonObject jsonRootObject = (JsonObject) jsonRoot;

        verifyObjectMemberStringValue(jsonRootObject, "attr1", "");
        verifyObjectMemberStringValue(jsonRootObject, "attr2", " ");
        verifyObjectMemberStringValue(jsonRootObject, "attr3", "\t");
        verifyObjectMemberStringValue(jsonRootObject, "attr4", "\n");
        verifyObjectMemberStringValue(jsonRootObject, "attr5", "\"");
        verifyObjectMemberStringValue(jsonRootObject, "attr6", "\\");
        verifyObjectMemberStringValue(jsonRootObject, "attr7", "/");
        verifyObjectMemberStringValue(jsonRootObject, "attr8", "\b");
        verifyObjectMemberStringValue(jsonRootObject, "attr9", "\f");
        verifyObjectMemberStringValue(jsonRootObject, "attr10", "\r");

        verifyObjectMemberStringValue(jsonRootObject, "attr21", " ");
    }

    @Test
    public void test_ok_json4() {

        final Object jsonRoot = parseJsonFile("json-ok4.json");

        assertNoFindings();

        assertTrue(jsonRoot instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) jsonRoot).getValue() instanceof String);
        assertTrue(((String) ((JsonPrimitive) jsonRoot).getValue()).equals("hello"));
    }

    @Test
    public void test_ok_json5() {

        final Object jsonRoot = parseJsonFile("json-ok5.json");

        assertNoFindings();

        assertTrue(jsonRoot instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) jsonRoot).getValue() == null);
    }

    @Test
    public void test_error_but_continue1() {

        final Object jsonRoot = parseJsonFile("json-error-but-continue1.json");

        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(jsonRoot instanceof JsonObject);
        final JsonObject jsonRootObject = (JsonObject) jsonRoot;

        verifyObjectMemberStringValue(jsonRootObject, "attr1", "comma missing after this string");
        verifyObjectMemberStringValue(jsonRootObject, "attr2", "comma after this string not needed");
    }

    @Test
    public void test_error_but_continue2() {

        final Object jsonRoot = parseJsonFile("json-error-but-continue2.json");

        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(jsonRoot instanceof JsonArray);
        final JsonArray jsonRootArray = (JsonArray) jsonRoot;

        verifyArrayMemberStringValue(jsonRootArray, 0, "comma missing after this string");
        verifyArrayMemberStringValue(jsonRootArray, 1, "comma after this string not needed");
    }

    @Test
    public void test_error1() {
        parseJsonFile("json-error1.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains(
                "Expected a quoted member name"));
    }

    @Test
    public void test_error2() {
        parseJsonFile("json-error2.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains("Expected name separator"));
    }

    @Test
    public void test_error3() {
        parseJsonFile("json-error3.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains("Expected a value"));
    }

    @Test
    public void test_error4() {
        parseJsonFile("json-error4.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains("Expected a value"));
    }

    @Test
    public void test_error5() {
        parseJsonFile("json-error5.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains("does not translate"));
    }

    @Test
    public void test_error6() {
        parseJsonFile("json-error6.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains(
                "Missing terminating double-quote character"));
    }

    @Test
    public void test_error7() {
        parseJsonFile("json-error7.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains("Missing escaped character"));
    }

    @Test
    public void test_error8() {
        parseJsonFile("json-error8.json");
        assertHasFindingOfType(ParserFindingType.P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains(
                "Unrecognized escaped character"));
    }

    @Test
    public void test_error9() {
        parseJsonFile("json-error9.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains(
                "Unicode character not correctly escaped"));
    }

    @Test
    public void test_error10() {
        parseJsonFile("json-error10.json");
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertTrue(findingsManager.getAllFindings().iterator().next().getMessage().contains(
                "Unicode character not correctly escaped"));
    }

    private void verifyObjectMemberStringValue(final JsonObject jsonObject, final String memberName,
            final String expected) {
        final JsonValue attr1 = getJsonObjectMemberValue(jsonObject, memberName);
        assertTrue(attr1 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr1).getValue() instanceof String);
        assertEquals(expected, (String) ((JsonPrimitive) attr1).getValue());
    }

    private void verifyArrayMemberStringValue(final JsonArray jsonArray, final int index, final String expected) {
        final JsonValue attr1 = jsonArray.getValues().get(index);
        assertTrue(attr1 instanceof JsonPrimitive);
        assertTrue(((JsonPrimitive) attr1).getValue() instanceof String);
        assertEquals(expected, (String) ((JsonPrimitive) attr1).getValue());
    }

    private Object parseJsonFile(final String fileName) {

        final File jsonFile = new File(DIR + fileName);
        final FileBasedYangInput fileBasedYangInput = new FileBasedYangInput(jsonFile);
        final YangData yangInstanceDataInput = new YangData(fileBasedYangInput);

        try {
            return new JsonParser(context, yangInstanceDataInput, fileBasedYangInput.getInputStream()).parse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_json_object_member_handling() {

        final JsonObject jsonObject = new JsonObject();

        jsonObject.putMember("member1", JsonPrimitive.valueOf(Boolean.TRUE));
        jsonObject.putMember("member2", JsonPrimitive.valueOf(Boolean.FALSE));
        jsonObject.putMember(new JsonObjectMemberName("member3"), JsonPrimitive.valueOf(null));

        assertTrue(jsonObject.getValues().get("member1").equals(JsonPrimitive.TRUE));
        assertTrue(jsonObject.getValues().get("member2").equals(JsonPrimitive.FALSE));
        assertTrue(jsonObject.getValues().get("member3").equals(JsonPrimitive.NULL));

        assertFalse(jsonObject.getValues().get("member1").equals(null));
        assertFalse(jsonObject.getValues().get("member2").equals(null));
        assertTrue(jsonObject.getValues().get("member3").equals(null));

        assertTrue(jsonObject.getValuesByMember().get(new JsonObjectMemberName("member1")).equals(JsonPrimitive.TRUE));
        assertTrue(jsonObject.getValuesByMember().get(new JsonObjectMemberName("member2")).equals(JsonPrimitive.FALSE));
        assertTrue(jsonObject.getValuesByMember().get(new JsonObjectMemberName("member3")).equals(JsonPrimitive.NULL));

        final JsonObjectMemberName jsonObjectMemberName99 = new JsonObjectMemberName("member99");

        assertTrue(jsonObjectMemberName99.equals(jsonObjectMemberName99));
        assertTrue(jsonObjectMemberName99.equals(new JsonObjectMemberName("member99")));
        assertTrue(jsonObjectMemberName99.equals("member99"));

        assertFalse(jsonObjectMemberName99.equals(null));
        assertFalse(jsonObjectMemberName99.equals(new JsonObjectMemberName("memberXXXXXX")));
        assertFalse(jsonObjectMemberName99.equals("memberXXXXX"));

        final JsonPrimitive booleanTrue = JsonPrimitive.valueOf(Boolean.TRUE);

        assertTrue(booleanTrue.equals(JsonPrimitive.valueOf(Boolean.TRUE)));
        assertTrue(booleanTrue.equals(Boolean.TRUE));
        assertTrue(booleanTrue.equals(true));
        assertFalse(booleanTrue.equals(null));

        final JsonPrimitive nullValue = JsonPrimitive.valueOf(null);

        assertTrue(nullValue.equals(JsonPrimitive.valueOf(null)));
        assertTrue(nullValue.equals(null));
        assertFalse(nullValue.equals(Boolean.TRUE));
    }

}
