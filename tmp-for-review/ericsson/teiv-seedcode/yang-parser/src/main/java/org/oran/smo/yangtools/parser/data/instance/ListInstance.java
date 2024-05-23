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

import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;

/**
 * This class represents an instance of a list.
 *
 * @author Mark Hollmann
 */
public class ListInstance extends AbstractStructureInstance {

    /**
     * The names of the keys, if any.
     */
    private final List<String> keyNames;

    /**
     * The key(s) for the instance.
     */
    final Map<String, String> keyValues;

    private final String cachedToString;

    public ListInstance(final AbstractStatement schemaList, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent, final List<String> keyNames, final Map<String, String> keyValues) {
        super(schemaList, dataDomNode, parent);
        this.keyNames = keyNames;
        this.keyValues = keyValues;
        this.cachedToString = "(" + getNamespace() + "):" + getName() + keyValues;
    }

    /**
     * The names of the keys, in order in which they are defined. May be empty for key-less lists.
     */
    public List<String> getKeyNames() {
        return keyNames;
    }

    /**
     * The value of the key(s). May be empty for key-less lists.
     */
    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    @Override
    public String toString() {
        return cachedToString;
    }
}
