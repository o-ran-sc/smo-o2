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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.parser.JsonParser;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonArray;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonPrimitive;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonValue;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;
import org.oran.smo.yangtools.parser.data.parser.JsonWriter;

public class JsonWriterTest extends YangTestCommon {

    @Test
    public void test_write_simple_json1() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf(null);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals(JsonPrimitive.NULL));
    }

    @Test
    public void test_write_simple_json2() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf(true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals(JsonPrimitive.TRUE));
    }

    @Test
    public void test_write_simple_json3() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf(1);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals(JsonPrimitive.ONE));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_write_simple_json4() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf("");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals(""));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_write_simple_json5() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf("abcd\n\b\t\f\r\\\"/");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals("abcd\n\b\t\f\r\\\"/"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_write_simple_json6() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf("✉🌕");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals("✉🌕"));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_write_simple_json7() throws IOException {

        final JsonPrimitive valueOf = JsonPrimitive.valueOf("©®");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(valueOf, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonPrimitive);
        assertTrue(rootValue.equals("©®"));
    }

    @Test
    public void test_write_array1() throws IOException {

        final JsonArray array = new JsonArray();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(array, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonArray);
        assertTrue(((JsonArray) rootValue).getValues().isEmpty());
    }

    @Test
    public void test_write_array2() throws IOException {

        final JsonArray array = new JsonArray();
        array.addValue(JsonPrimitive.valueOf(0));
        array.addValue(JsonPrimitive.valueOf(false));
        array.addValue(JsonPrimitive.valueOf(null));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(array, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonArray);
        assertTrue(((JsonArray) rootValue).getValues().size() == 3);
        assertTrue(((JsonArray) rootValue).getValues().get(0).equals(JsonPrimitive.ZERO));
        assertTrue(((JsonArray) rootValue).getValues().get(1).equals(JsonPrimitive.FALSE));
        assertTrue(((JsonArray) rootValue).getValues().get(2).equals(JsonPrimitive.NULL));
    }

    @Test
    public void test_write_object1() throws IOException {

        final JsonObject object = new JsonObject();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(object, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonObject);
        assertTrue(((JsonObject) rootValue).getValues().isEmpty());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_write_object2() throws IOException {

        final JsonObject object = new JsonObject();

        object.putMember("one", 1);
        object.putMember("two", null);
        object.putMember("three", true);
        object.putMember("four", JsonPrimitive.valueOf("abc"));
        object.putMember("five", "def");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(object, baos);

        final JsonValue rootValue = new JsonParser(null, null, new ByteArrayInputStream(baos.toByteArray())).parse();

        assertTrue(rootValue instanceof JsonObject);
        final Map<String, JsonValue> values = ((JsonObject) rootValue).getValues();

        assertTrue(values.get("one").equals(JsonPrimitive.ONE));
        assertTrue(values.get("two").equals(JsonPrimitive.NULL));
        assertTrue(values.get("three").equals(JsonPrimitive.TRUE));
        assertTrue(values.get("four").equals("abc"));
        assertTrue(values.get("five").equals("def"));
    }

}
