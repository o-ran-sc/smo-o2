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
 * This class represents an instance of a container.
 *
 * @author Mark Hollmann
 */
public class ContainerInstance extends AbstractStructureInstance {

    private final String cachedToString;

    /**
     * Constructor for a container instance that was specified in data.
     */
    public ContainerInstance(final AbstractStatement containerSchemaNode, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent) {
        super(containerSchemaNode, dataDomNode, parent);

        this.cachedToString = "(" + getNamespace() + "):" + getName();
    }

    /**
     * Constructor for a container instance that was created as it is a NP container.
     */
    public ContainerInstance(final AbstractStatement containerSchemaNode, final AbstractStructureInstance parent) {
        super(containerSchemaNode, parent);

        this.cachedToString = "(" + getNamespace() + "):" + getName();
    }

    @Override
    public String toString() {
        return cachedToString;
    }
}
