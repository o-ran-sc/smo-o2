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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.NumberHelper;

/**
 * Utility class that translates values expressed in lexical representation to a Java
 * Object. The type of the object returned depends on the YANG data type. Mapping is
 * as follows:
 * <p>
 * <ul>
 * <li>integer types -&gt; BigInteger</li>
 * <li>decimal64 -&gt; BigDecimal</li>
 * <li>string -&gt; String</li>
 * <li>boolean -&gt; Boolean</li>
 * <li>enumeration -&gt; String</li>
 * <li>bits -&gt; BitsValue</li>
 * <li>binary -&gt; BinaryValue</li>
 * </ul>
 * <b>Null</b> will be returned if:
 * <p>
 * <ul>
 * <li>The input value is null.</li>
 * <li>The YANG data type is not in the list above.</li>
 * <li>The value could not be converted.</li>
 * </ul>
 *
 * @author Mark Hollmann
 */
public abstract class ValueHelper {

    @SuppressWarnings("unchecked")
    static public <T> T fromLexicalRepresentation(final String lexicalRepresentation,
            final DataTypeHelper.YangDataType yangDataType) {

        if (lexicalRepresentation == null) {
            return null;
        }

        switch (yangDataType) {
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
                return (T) forInteger(lexicalRepresentation);
            case DECIMAL64:
                return (T) forDecimal64(lexicalRepresentation);
            case STRING:
                return (T) forString(lexicalRepresentation);
            case BOOLEAN:
                return (T) forBoolean(lexicalRepresentation);
            case ENUMERATION:
                return (T) forEnumeration(lexicalRepresentation);
            case BITS:
                return (T) forBits(lexicalRepresentation);
            case BINARY:
                return (T) forBinary(lexicalRepresentation);
            default:
                break;
        }

        return null;
    }

    static private BigInteger forInteger(final String lexicalRepresentation) {
        return NumberHelper.getIntegerDefaultValue(lexicalRepresentation);
    }

    static private BigDecimal forDecimal64(final String lexicalRepresentation) {
        return NumberHelper.getDecimalValue(lexicalRepresentation);
    }

    static private String forString(final String lexicalRepresentation) {
        return lexicalRepresentation;
    }

    static private Boolean forBoolean(final String lexicalRepresentation) {
        if ("true".equals(lexicalRepresentation)) {
            return Boolean.TRUE;
        }
        if ("false".equals(lexicalRepresentation)) {
            return Boolean.FALSE;
        }

        return null;
    }

    /**
     * 9.6.1: The lexical representation of an enumeration value is the assigned name string.
     */
    static private String forEnumeration(final String lexicalRepresentation) {
        if (lexicalRepresentation.isEmpty()) {
            return null;		// empty enum name is not a valid name.
        }
        return lexicalRepresentation;
    }

    /**
     * 9.7.2: The lexical representation of the bits type is a space-separated list of the
     * names of the bits that are set. A zero-length string thus represents a value
     * where no bits are set.
     */
    static private BitsValue forBits(final String lexicalRepresentation) {
        return new BitsValue(lexicalRepresentation);
    }

    /**
     * 9.8.2: Binary values are encoded with the base64 encoding scheme (see Section 4 in [RFC4648]).
     */
    static private BinaryValue forBinary(final String lexicalRepresentation) {
        return new BinaryValue(lexicalRepresentation);
    }
}
