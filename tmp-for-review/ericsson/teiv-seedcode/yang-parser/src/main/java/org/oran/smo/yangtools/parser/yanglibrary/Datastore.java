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
package org.oran.smo.yangtools.parser.yanglibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oran.smo.yangtools.parser.data.util.IdentityRefValue;
import org.oran.smo.yangtools.parser.model.util.YangFeature;

/**
 * Represents the "datastore" data node, part of a Yang Library.
 * <p/>
 * Does not have an explicit representation of the "schema" data node,
 * which is simply an aggregation on "module-set" instances.
 *
 * @author Mark Hollmann
 */
public class Datastore {

    /**
     * The name of the module defined by RFC 8342 (https://datatracker.ietf.org/doc/html/rfc8342) - "Network Management
     * Datastore Architecture"
     */
    public static final String IETF_DATASTORES_MODULE_NAME = "ietf-datastores";

    /**
     * The namespace of the module defined by RFC 8342
     */
    public static final String IETF_DATASTORES_NAMESPACE = "urn:ietf:params:xml:ns:yang:ietf-datastores";

    /**
     * The identity of the "running" datastore, as defined by RFC 8342
     */
    public static final IdentityRefValue RUNNING_DATASTORE_IDENTITY = new IdentityRefValue(IETF_DATASTORES_NAMESPACE,
            IETF_DATASTORES_MODULE_NAME, "running");

    private final IdentityRefValue datastoreName;

    private final String schemaName;

    private final List<ModuleSet> moduleSets;

    public Datastore(final IdentityRefValue datastoreName, final String schemaName, final List<ModuleSet> moduleSets) {
        this.datastoreName = datastoreName;
        this.schemaName = schemaName;
        this.moduleSets = new ArrayList<>(moduleSets);
    }

    /**
     * Returns the identity of the datastore.
     */
    public IdentityRefValue getDatastoreName() {
        return datastoreName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public List<ModuleSet> getModuleSets() {
        return Collections.unmodifiableList(moduleSets);
    }

    /**
     * Returns all IMPLEMENTING modules of this datastore, irrespective of the module-set in which they are.
     */
    public Set<Module> getImplementingModules() {
        final Set<Module> modules = new HashSet<>();
        moduleSets.forEach(ms -> modules.addAll(ms.getImplementingModules()));
        return modules;
    }

    /**
     * Returns all IMPORT-ONLY modules of this datastore, irrespective of the module-set in which they are.
     */
    public Set<Module> getImportOnlyModules() {
        final Set<Module> modules = new HashSet<>();
        moduleSets.forEach(ms -> modules.addAll(ms.getImportOnlyModules()));
        return modules;
    }

    /**
     * Returns all modules of this datastore, irrespective of the module-set in which they are.
     */
    public Set<Module> getAllModules() {
        final Set<Module> allModules = new HashSet<>();
        moduleSets.forEach(ms -> allModules.addAll(ms.getImplementingModules()));
        moduleSets.forEach(ms -> allModules.addAll(ms.getImportOnlyModules()));
        return allModules;
    }

    /**
     * Returns the features supported by this datastore. This is a convenience method;
     * the data is retrieved from the modules part of this datastore.
     */
    public Set<YangFeature> getSupportedFeatures() {

        final Set<YangFeature> result = new HashSet<>();

        getAllModules().forEach(module -> {
            final String namespace = module.getNamespace();
            module.getFeatures().forEach(featureName -> {
                result.add(new YangFeature(namespace, module.getName(), featureName));
            });
        });

        return result;
    }
}
