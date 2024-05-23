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
package org.oran.smo.yangtools.parser.data.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Holds the value for a data node instance of type "binary".
 *
 * @author Mark Hollmann
 */
public class BinaryValue {

    private byte[] binaryValue = new byte[0];

    public BinaryValue(final byte[] val) {
        binaryValue = Objects.requireNonNull(val);
    }

    public BinaryValue(final String base64encoded) {
        try {
            binaryValue = Base64.getDecoder().decode(base64encoded);
        } catch (final IllegalArgumentException ignored) {
            throw new RuntimeException("A Base64 value could not be decoded to binary.");
        }
    }

    public byte[] getBinaryValue() {
        return binaryValue;
    }

    @Override
    public String toString() {
        return Arrays.toString(binaryValue);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(binaryValue);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BinaryValue && Arrays.equals(((BinaryValue) obj).binaryValue, this.binaryValue);
    }
}
