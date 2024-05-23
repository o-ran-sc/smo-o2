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
package org.oran.smo.yangtools.parser.model.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds mappings between module names and namespaces.
 *
 * @author Mark Hollmann
 */
public class ModuleAndNamespaceResolver {

    private final Map<String, String> moduleNameToNamespace = new HashMap<>();
    private final Map<String, String> namespaceToModuleName = new HashMap<>();

    /**
     * Records a mapping between a module or submodule name, and its namespace (in the case of
     * submodule, the namespace of the owning module).
     */
    public void recordModuleMapping(final String moduleName, final String namespace) {
        moduleNameToNamespace.put(Objects.requireNonNull(moduleName), Objects.requireNonNull(namespace));
    }

    /**
     * Records a mapping between a namespace and the module defining it.
     */
    public void recordNamespaceMapping(final String namespace, final String moduleName) {
        namespaceToModuleName.put(Objects.requireNonNull(namespace), Objects.requireNonNull(moduleName));
    }

    /**
     * Returns the namespace for the supplied module name. If the module name refers to a submodule, will
     * return the namespace of the module owning the submodule. Returns null if the module is unknown.
     */
    public String getNamespaceForModule(final String moduleName) {
        return moduleNameToNamespace.get(moduleName);
    }

    /**
     * Returns the module name for the supplied namespace. Returns null if the namespace is unknown.
     */
    public String getModuleForNamespace(final String namespace) {
        return namespaceToModuleName.get(namespace);
    }

    @Override
    public String toString() {
        return "Mappings: " + moduleNameToNamespace.toString();
    }

}
