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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * An input based on a byte buffer.
 *
 * @author Mark Hollmann
 */
public class ByteArrayYangInput implements YangInput {

    private final byte[] data;
    private final String name;
    private final String mediaType;

    public ByteArrayYangInput(final byte[] data, final String name) {
        this(data, name, MEDIA_TYPE_YANG);
    }

    public ByteArrayYangInput(final byte[] data, final String name, final String mediaType) {
        this.data = Objects.requireNonNull(data);
        this.name = Objects.requireNonNull(name);
        this.mediaType = Objects.requireNonNull(mediaType);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new ByteArrayInputStream(data);
        } catch (final Exception ex) {
            throw new RuntimeException("Cannot open input stream: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
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
            return this.name.equals(((ByteArrayYangInput) other).name) && Arrays.equals(this.data,
                    ((ByteArrayYangInput) other).data);
        }

        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
