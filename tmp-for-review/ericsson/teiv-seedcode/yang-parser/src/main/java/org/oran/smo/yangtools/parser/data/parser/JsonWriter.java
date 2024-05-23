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
package org.oran.smo.yangtools.parser.data.parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonArray;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonPrimitive;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonValue;

/**
 * A simple writer of JSON data.
 *
 * @author Mark Hollmann
 */
public abstract class JsonWriter {

    public static void write(final JsonValue value, final OutputStream os) throws IOException {
        final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Objects.requireNonNull(os), Charset.forName(
                "UTF-8")));
        write(Objects.requireNonNull(value), bw, 0);
        bw.close();
    }

    private static void write(final JsonValue value, final BufferedWriter bw, final int indent) throws IOException {
        if (value instanceof JsonPrimitive) {
            writeJsonPrimitive((JsonPrimitive) value, bw, indent);
        } else if (value instanceof JsonArray) {
            writeJsonArray((JsonArray) value, bw, indent);
        } else if (value instanceof JsonObject) {
            writeJsonObject((JsonObject) value, bw, indent);
        }
    }

    private static void writeJsonPrimitive(final JsonPrimitive jsonPrimitive, final BufferedWriter bw, final int indent)
            throws IOException {

        final Object value = jsonPrimitive.getValue();

        if (value == null) {
            bw.append("null");
        } else if (value instanceof String) {
            writeString(bw, (String) value);
        } else {
            bw.append(Objects.toString(value));
        }
    }

    private static void writeJsonArray(final JsonArray jsonArray, final BufferedWriter bw, final int indent)
            throws IOException {

        final List<JsonValue> jsonValues = jsonArray.getValues();
        boolean first = true;

        if (jsonValues.isEmpty()) {
            bw.append("[]");
            return;
        }

        bw.append('[');
        bw.newLine();

        for (final JsonValue jsonValue : jsonValues) {
            if (!first) {
                bw.append(" ,");
                bw.newLine();
            }
            writeIndent(bw, indent + 4);
            write(jsonValue, bw, indent + 4);
            first = false;
        }

        bw.newLine();
        writeIndent(bw, indent);
        bw.append(']');
    }

    private static void writeJsonObject(final JsonObject jsonObject, final BufferedWriter bw, final int indent)
            throws IOException {
        final Map<String, JsonValue> members = jsonObject.getValues();
        boolean first = true;

        if (members.isEmpty()) {
            bw.append("{}");
            return;
        }

        bw.append('{');
        bw.newLine();

        for (final Entry<String, JsonValue> entry : members.entrySet()) {
            if (!first) {
                bw.append(" ,");
                bw.newLine();
            }
            writeIndent(bw, indent + 2);
            writeString(bw, entry.getKey());
            bw.append(" : ");
            write(entry.getValue(), bw, indent + 4);
            first = false;
        }

        bw.newLine();
        writeIndent(bw, indent);
        bw.append('}');
    }

    private static final String ZEROS = "0000";

    private static void writeString(final BufferedWriter bw, final String theString) throws IOException {
        bw.append('"');

        for (int i = 0; i < theString.length(); ++i) {
            final char charAt = theString.charAt(i);

            if (charAt == '"' || charAt == '\\' || charAt == '/') {
                bw.append('\\');
                bw.append(charAt);
            } else if (charAt == '\b') {
                bw.append("\\b");
            } else if (charAt == '\f') {
                bw.append("\\f");
            } else if (charAt == '\n') {
                bw.append("\\n");
            } else if (charAt == '\r') {
                bw.append("\\r");
            } else if (charAt == '\t') {
                bw.append("\\t");
            } else if (charAt < 32 || charAt > 126) {
                /*
                 * We escape with unicode
                 */
                bw.append("\\u");
                final String hexString = Integer.toHexString((int) charAt);
                bw.append(ZEROS.substring(hexString.length()));
                bw.append(hexString);
            } else {
                bw.append(charAt);
            }
        }

        bw.append('"');
    }

    private static final String SPACES = "                                                                                                                                                 ";

    private static void writeIndent(final BufferedWriter bw, final int indent) throws IOException {
        if (indent == 0) {
            return;
        }
        bw.append(SPACES.substring(0, indent));
    }
}
