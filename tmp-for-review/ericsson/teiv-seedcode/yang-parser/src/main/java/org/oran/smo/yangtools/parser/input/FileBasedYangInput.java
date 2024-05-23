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
package org.oran.smo.yangtools.parser.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * An input based on a file
 *
 * @author Mark Hollmann
 */
public class FileBasedYangInput implements YangInput {

    private final File file;

    public FileBasedYangInput(final File file) {
        this.file = Objects.requireNonNull(file);
        if (!file.isFile() || !file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (final Exception ex) {
            throw new RuntimeException("Cannot open input stream: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getMediaType() {
        if (file.getName().endsWith(".json")) {
            return MEDIA_TYPE_YANG_DATA_JSON;
        } else if (file.getName().endsWith(".xml")) {
            return MEDIA_TYPE_YANG_DATA_XML;
        }

        return MEDIA_TYPE_YANG;
    }

    @Override
    public int hashCode() {
        return file.getName().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return this.file.equals(((FileBasedYangInput) other).file);
        }

        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
