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
public class Submodule {

    private String name = "";
    private String revision = "";
    private List<String> schemaLocations = Collections.<String> emptyList();

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
     * Returns the revision of the submodule. If a revision has not been declared in the submodule (a
     * very rare occurrence, but a valid scenario), returns null (not an empty string).
     */
    public String getRevision() {
        return (revision == null || revision.isEmpty()) ? null : revision;
    }

    public ModuleIdentity getModuleIdentity() {
        return new ModuleIdentity(getName(), getRevision());
    }

    public void setSchemaLocations(final List<String> schemaLocations) {
        this.schemaLocations = Collections.unmodifiableList(schemaLocations);
    }

    public List<String> getSchemaLocations() {
        return Collections.unmodifiableList(schemaLocations);
    }

    /*
     * A utility mechanism that allows clients to attach application-specific data to this submodule.
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
