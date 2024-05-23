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

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;

/**
 * This class represents an entry of a leaf-list instance (not the complete leaf-list).
 *
 * @author Mark Hollmann
 */
public class LeafListInstance extends AbstractContentInstance {

    /**
     * Constructor for a leaf-list instance that carries data.
     */
    public LeafListInstance(final AbstractStatement leafListStatement, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent, final Object value) {
        super(leafListStatement, dataDomNode, parent, value);
    }

    /**
     * Constructor for a leaf-list instance that carries a default value.
     */
    public LeafListInstance(final AbstractStatement leafListStatement, final AbstractStructureInstance parent,
            final Object value) {
        super(leafListStatement, parent, value);
    }
}
