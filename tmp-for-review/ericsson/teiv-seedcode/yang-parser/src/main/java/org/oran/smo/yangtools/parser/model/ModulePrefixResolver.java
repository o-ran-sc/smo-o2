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
package org.oran.smo.yangtools.parser.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;

/**
 * A resolver that can handle prefixes, module names, and namespaces, and map
 * back and forth between these. Namespaces are of importance when it comes to
 * data; specifically, YANG data over NETCONF is XML encoded, requiring namespaces.
 * <p/>
 * However, this class should not be used for handling of data, as it does not
 * support setting the default namespace.
 *
 * @author Mark Hollmann
 */
public class ModulePrefixResolver extends PrefixResolver {

    private final ModuleAndNamespaceResolver globalNamespaceResolver;

    private final Map<String, ModuleIdentity> prefixToModulename = new HashMap<>();

    public ModulePrefixResolver(final ModuleAndNamespaceResolver globalNamespaceResolver) {
        this.globalNamespaceResolver = globalNamespaceResolver;
    }

    /*
     * ================ Here is all the module stuff =====================
     */

    /**
     * Records a mapping between a prefix and a module name. This mapping has typically
     * been extracted from the header of a YAM.
     */
    public void addModuleMapping(final String prefix, final ModuleIdentity moduleIdentity) {
        prefixToModulename.put(Objects.requireNonNull(prefix), Objects.requireNonNull(moduleIdentity));
    }

    /**
     * Records the default mapping for this prefix resolver, i.e. the module that is mapped
     * to "no prefix".
     */
    public void setDefaultModuleMapping(final ModuleIdentity moduleIdentity) {
        prefixToModulename.put(PrefixResolver.NO_PREFIX, Objects.requireNonNull(moduleIdentity));
    }

    /**
     * Returns the module representing the prefix. May return null if the prefix is unknown.
     */
    public ModuleIdentity getModuleForPrefix(final String prefix) {
        if (prefix == null) {
            return getDefaultModuleIdentity();
        }
        return prefixToModulename.get(prefix);
    }

    /**
     * Returns the module representing the default prefix. May be null if unknown.
     */
    public ModuleIdentity getDefaultModuleIdentity() {
        return prefixToModulename.get(PrefixResolver.NO_PREFIX);
    }

    /*
     * ================ Here is all the namespace stuff =====================
     */

    @Override
    public void setDefaultNamespaceUri(final String namespaceUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMapping(final String prefix, final String namespaceUri) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the default namespace URI for this module. May return null.
     */
    @Override
    public String getDefaultNamespaceUri() {
        final ModuleIdentity moduleIdentityForPrefix = getDefaultModuleIdentity();
        return moduleIdentityForPrefix == null ?
                null :
                globalNamespaceResolver.getNamespaceForModule(moduleIdentityForPrefix.getModuleName());
    }

    /**
     * Returns the namespace URI for the given prefix or null if no mapping exists for the prefix.
     */
    @Override
    public String resolveNamespaceUri(final String prefix) {
        final ModuleIdentity moduleIdentityForPrefix = getModuleForPrefix(prefix);
        return moduleIdentityForPrefix == null ?
                null :
                globalNamespaceResolver.getNamespaceForModule(moduleIdentityForPrefix.getModuleName());
    }

    /**
     * Returns the name of the module for the given namespace.
     */
    public String resolveModuleName(final String namespace) {
        return globalNamespaceResolver.getModuleForNamespace(namespace);
    }

    @Override
    public String toString() {
        return "Mappings: " + prefixToModulename.toString();
    }
}
