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
package org.oran.smo.yangtools.parser.model.yangdom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.util.GrammarHelper;

/**
 * Use this class to write out a Yang DOM to a file or output stream. This is typically used where
 * a YAM is parsed into a DOM, the DOM is manipulated (for example, statements added or removed),
 * and the result is written out again.
 *
 * @author Mark Hollmann
 */
public abstract class YangDomWriter {

    /**
     * Writes the DOM into a file as denoted by the resolver.
     */
    public static void writeOut(final YangModel yangModel, final OutputFileNameResolver resolver,
            final File targetDirectory) throws IOException {

        final String fileName = resolver.getOutputFileNameForYangInput(yangModel);
        final File outFile = new File(targetDirectory, fileName);
        targetDirectory.mkdirs();
        outFile.createNewFile();

        final FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        writeOut(yangModel, fileOutputStream);
        fileOutputStream.close();
    }

    /**
     * Writes the DOM into an output stream as denoted by the resolver. Note the stream will
     * not be automatically closed afterwards - it is up to the client to do so.
     */
    public static void writeOut(final YangModel yangModel, final OutputStreamResolver resolver) throws IOException {
        final OutputStream outputStream = resolver.getOutputStreamForYangInput(yangModel);
        writeOut(yangModel, outputStream);
    }

    /**
     * Writes the DOM into an output stream, using UTF-8 character set.
     */
    public static void writeOut(final YangModel yangModel, final OutputStream outputStream) throws IOException {

        final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        final YangDomDocumentRoot domDocumentRoot = yangModel.getYangModelRoot().getDomDocumentRoot();
        writeDomNode(writer, domDocumentRoot.getChildren().get(0), "");

        writer.flush();
        writer.close();
    }

    private static final Set<String> ADD_COMMENT_AT_END_FOR = new HashSet<>(Arrays.asList(CY.CONTAINER, CY.LIST,
            CY.GROUPING));

    private static void writeDomNode(final Writer writer, final YangDomElement domElement, final String indent)
            throws IOException {

        writer.write(indent);
        writer.write(domElement.getName());

        if (domElement.getValue() != null) {
            writer.write(' ');
            writeString(writer, domElement.getValue());
        }

        final List<YangDomElement> children = domElement.getChildren();
        if (children.isEmpty()) {
            writer.write(";\n");
        } else {
            writer.write(" {\n");
            final String newIndent = indent + "  ";
            for (final YangDomElement child : children) {
                writeDomNode(writer, child, newIndent);
            }
            writer.write(indent);

            if (ADD_COMMENT_AT_END_FOR.contains(domElement.getName())) {
                writer.write("} // end '");
                writer.write(domElement.getName());
                writer.write(' ');
                writer.write(domElement.getValue());
                writer.write("'\n");
            } else {
                writer.write("}\n");
            }
        }
    }

    private static void writeString(final Writer writer, final String value) throws IOException {

        if (GrammarHelper.isUnquotableString(value)) {
            /*
             * If there are no characters in the string that might need special handling we can simply write
             * out the string. And no, we don't care about this stupid stipulation about wrapping at 80 characters.
             */
            writer.write(value);
            return;
        }

        /*
         * The string contains characters that must be escaped. Write as double-quoted string, with escaping
         * where required.
         */
        writer.write(convertToDoubleQuotedString(value));
    }

    /**
     * The supplied string is converted to a double-quoted string in accordance with the YANG rules.
     */
    private static String convertToDoubleQuotedString(final String input) {

        final StringBuilder sb = new StringBuilder();

        sb.append('"');

        for (final char c : Objects.requireNonNull(input).toCharArray()) {
            switch (c) {
                case '\n':
                    sb.append('\\').append('n');
                    break;
                case '\t':
                    sb.append('\\').append('t');
                    break;
                case '"':
                    sb.append('\\').append('"');
                    break;
                case '\\':
                    sb.append('\\').append('\\');
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('"');

        return sb.toString();
    }
}
