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

public class AnyDataInstance extends AbstractContentInstance {

    /**
     * The value of the anyxml.
     */
    private final String value;

    private final String cachedToString;

    public AnyDataInstance(final AbstractStatement schemaLeaf, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent, final String value) {
        super(schemaLeaf, dataDomNode, parent, value);
        this.value = value;
        this.cachedToString = "(" + getNamespace() + "):" + getName();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return cachedToString;
    }
}
