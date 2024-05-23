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
package org.oran.smo.yangtools.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A classic prefix resolver that can resolve prefixes to namespace URIs.
 *
 * @author Mark Hollmann
 */
public class PrefixResolver {

    /**
     * An artificial prefix to handle the default namespace URI.
     */
    public static final String NO_PREFIX = " NO_PREFIX ";

    private final Map<String, String> mappings = new HashMap<>();

    /**
     * Sets the default namespace URI.
     */
    public void setDefaultNamespaceUri(final String namespaceUri) {
        addMapping(NO_PREFIX, namespaceUri);
    }

    /**
     * Returns the default namespace URI. May return null.
     */
    public String getDefaultNamespaceUri() {
        return resolveNamespaceUri(NO_PREFIX);
    }

    /**
     * Adds a mapping from prefix to namespace URI.
     */
    public void addMapping(final String prefix, final String namespaceUri) {
        mappings.put(Objects.requireNonNull(prefix), Objects.requireNonNull(namespaceUri));
    }

    /**
     * Returns the namespace URI for the given prefix or null if no mapping exists for the prefix.
     */
    public String resolveNamespaceUri(final String prefix) {
        return mappings.containsKey(prefix) ? mappings.get(prefix) : null;
    }

    /**
     * Creates a clone of this prefix resolver. Subsequent modifications to this resolver will not be
     * reflected in the cloned resolver.
     */
    public PrefixResolver clone() {
        final PrefixResolver clone = new PrefixResolver();
        clone.mappings.putAll(this.mappings);
        return clone;
    }

    @Override
    public int hashCode() {
        return mappings.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof PrefixResolver)) {
            return false;
        }

        final PrefixResolver other = (PrefixResolver) obj;

        return this.mappings.equals(other.mappings);
    }
}
