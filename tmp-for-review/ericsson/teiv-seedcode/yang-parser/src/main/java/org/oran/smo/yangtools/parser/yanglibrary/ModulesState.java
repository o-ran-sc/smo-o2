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

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

/**
 * Represents the (deprecated) "modules-state" data node as defined in RFC 8525
 * (https://datatracker.ietf.org/doc/html/rfc8525)
 * and as originally defined in RFC 7895.
 *
 * @author Mark Hollmann
 */
public class ModulesState {

    private final YangDataDomNode mountPoint;

    private String moduleSetId;
    private List<Module> modules = new ArrayList<>();

    public ModulesState(final YangDataDomNode mountPoint) {
        this.mountPoint = mountPoint;
    }

    public YangDataDomNode getMountPoint() {
        return mountPoint;
    }

    public void setModuleSetId(final String moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public String getModuleSetId() {
        return moduleSetId;
    }

    public void addModule(final Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }
}
