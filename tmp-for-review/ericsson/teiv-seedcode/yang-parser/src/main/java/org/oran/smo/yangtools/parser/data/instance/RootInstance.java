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
package org.oran.smo.yangtools.parser.data.instance;

/**
 * Top-level object for a data instance tree.
 * <p/>
 * A data instance tree is build from parsed data, with the help of a Yang schema. While the data DOM
 * can live without a schema, the data instance tree requires a schema to be present that matches the
 * data.
 * <p/>
 * The data instance tree contains representations for all data nodes (leaf, leaf-list, container, list);
 * each of these point to the corresponding statement in the schema. For leaf and leaf-list, values are
 * captured as well.
 *
 * @author Mark Hollmann
 */
public class RootInstance extends AbstractStructureInstance {

    public RootInstance() {
        super(null, null);
    }

    public String getName() {
        return "/";
    }

    public String getNamespace() {
        return "/";
    }
}
