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
import java.util.List;

/**
 * Represents the "module-set" data node as defined in RFC 8525.
 *
 * @author Mark Hollmann
 */
public class ModuleSet {

    private String name = "";
    private final List<Module> implementingModules = new ArrayList<>();
    private final List<Module> importOnlyModules = new ArrayList<>();

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addImplementingModule(final Module module) {
        implementingModules.add(module);
    }

    public List<Module> getImplementingModules() {
        return Collections.unmodifiableList(implementingModules);
    }

    public void addImportOnlyModule(final Module module) {
        importOnlyModules.add(module);
    }

    public List<Module> getImportOnlyModules() {
        return Collections.unmodifiableList(importOnlyModules);
    }
}
