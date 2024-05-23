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

import java.util.Objects;

import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;

/**
 * A simple base class to encapsulate a namespace, module name and identifier. The class allows
 * for either namespace or module-name to be supplied, as sometimes the respective other is not
 * immediately available during processing. Method {@link resolveModuleOrNamespace} should be
 * used to populate the respective other.
 *
 * @author Mark Hollmann
 */
public class NamespaceModuleIdentifier {

    private String namespace;
    private String moduleName;
    private final String identifier;

    /**
     * Either namespace or module name should be supplied, and preferably both. There are valid usages
     * where both can remain empty, so not enforcing this.
     */
    public NamespaceModuleIdentifier(final String namespace, final String moduleName, final String identifier) {
        this.namespace = namespace;
        this.moduleName = moduleName;
        this.identifier = Objects.requireNonNull(identifier);
    }

    /**
     * Returns the namespace, or null in case the namespace was not originally supplied and no resolution has been done yet.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the module name, or null in case the module name was not originally supplied, and no resolution has been done
     * yet.
     */
    public String getModuleName() {
        return moduleName;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Resolves the namespace or module name, as required.
     */
    public void resolveModuleOrNamespace(final ModuleAndNamespaceResolver resolver) {

        if (moduleName == null && namespace != null) {
            moduleName = resolver.getModuleForNamespace(namespace);
        } else if (namespace == null && moduleName != null) {
            namespace = resolver.getNamespaceForModule(moduleName);
        }
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return getNamespace() + "/" + getModuleName() + "/" + getIdentifier();
    }

    @Override
    public boolean equals(final Object obj) {

        if (!(obj instanceof NamespaceModuleIdentifier)) {
            return false;
        }

        final NamespaceModuleIdentifier other = (NamespaceModuleIdentifier) obj;

        if (!this.identifier.equals(other.identifier)) {
            return false;
        }

        if (this.moduleName != null && other.moduleName != null) {
            return this.moduleName.equals(other.moduleName);
        }

        if (this.namespace != null && other.namespace != null) {
            return this.namespace.equals(other.namespace);
        }

        return false;
    }
}
