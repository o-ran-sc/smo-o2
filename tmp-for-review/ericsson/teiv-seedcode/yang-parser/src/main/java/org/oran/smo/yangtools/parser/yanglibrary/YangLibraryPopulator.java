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

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

/**
 * Implementations of this interface will extract Yang Library-related data from data DOM nodes and
 * populate various objects relating to yang library, eg. modules. Implementations are expected to
 * not throw exceptions, but to add findings to the supplied context if errors are encountered.
 * <p/>
 * Clients will typically implement this interface if the Yang Library contains data in addition to
 * what is defined in RFC 8525; for example, vendor-proprietary data relating to modules.
 * <p/>
 * A default implementation is provided in {@link RFC8525Populator} which handles all the data nodes
 * as defined in RFC 8525.
 *
 * @author Mark Hollmann
 */
public interface YangLibraryPopulator {

    /**
     * Callback for population of the ModulesState object.
     */
    default void populateModulesState(final ParserExecutionContext context, final ModulesState modulesState,
            final YangDataDomNode modulesStateDomNode) {
    }

    /**
     * Callback for populating the Module object, from modules-state branch.
     */
    default void populateModuleInModulesState(final ParserExecutionContext context, final Module module,
            final YangDataDomNode moduleDomNode) {
    }

    /**
     * Callback for populating the Submodule object, from modules-state branch.
     */
    default void populateSubmoduleInModulesState(final ParserExecutionContext context, final Submodule submodule,
            final YangDataDomNode submoduleDomNode) {
    }

    /**
     * Callback for population of the YangLibrary object.
     */
    default void populateYangLibrary(final ParserExecutionContext context, final YangLibrary yangLibrary,
            final YangDataDomNode yangLibraryDomNode) {
    }

    /**
     * Callback for population of the ModuleSet object.
     */
    default void populateModuleSet(final ParserExecutionContext context, final ModuleSet moduleSet,
            final YangDataDomNode moduleSetDomNode) {
    }

    /**
     * Callback for populating the Module object, from yang-library branch.
     */
    default void populateModuleInYangLibrary(final ParserExecutionContext context, final Module module,
            final YangDataDomNode moduleDomNode, final String conformanceType) {
    }

    /**
     * Callback for populating the Submodule object, from yang-library branch.
     */
    default void populateSubmoduleInYangLibrary(final ParserExecutionContext context, final Submodule submodule,
            final YangDataDomNode submoduleDomNode) {
    }
}
