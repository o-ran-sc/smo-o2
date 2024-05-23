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

/**
 * This enum serves a purpose similar to the corresponding leaf inside the YANG library
 * (urn:ietf:params:xml:ns:yang:ietf-yang-library). It indicates whether a module is purely
 * used as source of imports (e.g. because another module only needs its derived types and
 * groupings) or whether the whole module should be considered and the containers / lists
 * / etc. ("protocol-accessible objects") within it form part of the device model.
 * <p/>
 * Usually, a server will IMPLEMENT the vast majority of modules, and only in some rare
 * cases will only IMPORT from some.
 *
 * @author Mark Hollmann
 */
public enum ConformanceType {
    /**
     * The server implements this module, i.e. makes available all protocol-accessible
     * objects defined within.
     */
    IMPLEMENT,
    /**
     * The device only uses this module to import types and groupings; any containers /
     * lists / etc. defined within at the root of the module will be ignored.
     *
     * The conformance type IMPORT should not be confused with the YANG 'import' statement.
     */
    IMPORT
}
