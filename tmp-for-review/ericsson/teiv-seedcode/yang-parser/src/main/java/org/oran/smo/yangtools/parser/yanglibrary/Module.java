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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.ModuleIdentity;

/**
 * Represents the "module" data node as defined in RFC 8525.
 *
 * @author Mark Hollmann
 */
public class Module {

    private String conformanceType = "";
    private String name = "";
    private String revision = "";
    private String namespace = "";
    private List<String> schemaLocations = Collections.<String> emptyList();
    private List<Submodule> submodules = new ArrayList<>();
    private List<String> features;
    private List<String> deviatedByModuleNames;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRevision(final String revision) {
        this.revision = revision;
    }

    /**
     * Returns the revision of the module. If a revision has not been declared in the module (a very rare
     * occurrence, but a valid scenario), returns null (not an empty string).
     */
    public String getRevision() {
        return (revision == null || revision.isEmpty()) ? null : revision;
    }

    public ModuleIdentity getModuleIdentity() {
        return new ModuleIdentity(getName(), getRevision());
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setFeatures(final List<String> features) {
        this.features = Collections.unmodifiableList(features);
    }

    /**
     * Returns the list of features supported for this module.
     */
    public List<String> getFeatures() {
        return features != null ? Collections.unmodifiableList(features) : Collections.<String> emptyList();
    }

    public void setSchemaLocations(final List<String> schemaLocations) {
        this.schemaLocations = Collections.unmodifiableList(schemaLocations);
    }

    public List<String> getSchemaLocations() {
        return Collections.unmodifiableList(schemaLocations);
    }

    public void addSubmodule(final Submodule submodule) {
        submodules.add(submodule);
    }

    public List<Submodule> getSubmodules() {
        return Collections.unmodifiableList(submodules);
    }

    public void setDeviatedByModuleNames(final List<String> deviatedByModuleNames) {
        this.deviatedByModuleNames = new ArrayList<>(deviatedByModuleNames);
    }

    public List<String> getDeviatedByModuleNames() {
        return deviatedByModuleNames != null ?
                Collections.unmodifiableList(deviatedByModuleNames) :
                Collections.<String> emptyList();
    }

    public void setConformanceType(final String conformanceType) {
        this.conformanceType = conformanceType;
    }

    public static final String MODULE_IMPLEMENT = "implement";
    public static final String MODULE_IMPORT = "import";

    public IetfYangLibraryConformanceType getConformanceType() {
        switch (conformanceType) {
            case MODULE_IMPLEMENT:
                return IetfYangLibraryConformanceType.IMPLEMENT;
            case MODULE_IMPORT:
                return IetfYangLibraryConformanceType.IMPORT;
            default:
        }

        return IetfYangLibraryConformanceType.UNKNOWN;
    }

    public enum IetfYangLibraryConformanceType {
        IMPLEMENT,
        IMPORT,
        UNKNOWN;
    }

    /*
     * A utility mechanism that allows clients to attach application-specific data to this module.
     */
    private final Map<String, Object> appData = new HashMap<>();

    public void setAppData(final String key, final Object data) {
        appData.put(key, data);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAppData(final String key) {
        return (T) appData.get(key);
    }
}
