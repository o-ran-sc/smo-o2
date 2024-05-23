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
package org.oran.smo.yangtools.parser.util;

import org.oran.smo.yangtools.parser.PrefixResolver;

/**
 * Utility class to handle QName.
 *
 * @author Mark Hollmann
 */
public abstract class QNameHelper {

    /**
     * Returns whether the supplied qualified name has a prefix. A colon character ':' at the
     * very beginning of the string (which would be an illegal QName) will cause false to be
     * returned.
     */
    public static boolean hasPrefix(final String qName) {
        return qName != null && qName.indexOf(':') > 0;
    }

    /**
     * Given a QName, such as "foo:bar", extracts the prefix (here, "foo").
     * <p/>
     * If there is no prefix, PrefixResolver.NO_PREFIX will be returned. If this is not
     * desirable, use hasPrefix() instead.
     */
    public static String extractPrefix(final String qName) {
        /*
         * Note that a colon at position 0 is likewise considered to denote "no prefix"
         * (technically it is incorrect syntax).
         */
        final int indexOfColon = qName == null ? -1 : qName.indexOf(':');
        return indexOfColon > 0 ? qName.substring(0, indexOfColon) : PrefixResolver.NO_PREFIX;
    }

    /**
     * Given a QName, such as "foo:bar", extracts the name (here, "bar").
     */
    public static String extractName(final String qName) {
        final int indexOfColon = qName == null ? -1 : qName.indexOf(':');
        return indexOfColon > -1 ? qName.substring(indexOfColon + 1) : qName;
    }
}
