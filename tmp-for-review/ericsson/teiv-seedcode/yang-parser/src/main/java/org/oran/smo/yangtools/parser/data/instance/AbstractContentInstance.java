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

import java.util.Objects;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;

/**
 * Represents something that carries data, e.g. a leaf, a leaf-list instance.
 *
 * @author Mark Hollmann
 */
public abstract class AbstractContentInstance extends AbstractDataInstance {

    private final Object value;

    private final String cachedToString;

    /**
     * Constructor for a content instance that carries data.
     */
    public AbstractContentInstance(final AbstractStatement schemaNode, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent, final Object value) {
        super(schemaNode, dataDomNode, parent);

        this.value = Objects.requireNonNull(value);
        this.cachedToString = "(" + getNamespace() + "):" + getName() + "=" + Objects.toString(value);
    }

    /**
     * Constructor for a content instance that carries a default value.
     */
    public AbstractContentInstance(final AbstractStatement schemaNode, final AbstractStructureInstance parent,
            final Object value) {
        super(schemaNode, parent);

        this.value = value;
        this.cachedToString = "(" + getNamespace() + "):" + getName() + "=" + Objects.toString(value);
    }

    /**
     * The type of the returned value depends on how the value was originally encoded. If it
     * was encoded in XML, the value will be of type String. If it was encoded in JSON, it
     * will be of type String, Boolean or Double.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return cachedToString;
    }
}
