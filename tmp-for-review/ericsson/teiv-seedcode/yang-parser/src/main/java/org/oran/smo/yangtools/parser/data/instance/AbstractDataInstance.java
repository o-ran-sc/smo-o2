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
 * Represents a piece of data in the data tree (e.g. a container, a leaf)
 *
 * @author Mark Hollmann
 */
public abstract class AbstractDataInstance {

    /**
     * The node in the schema that this data relates to. Will be null for the root instance.
     */
    private final AbstractStatement schemaNode;

    /**
     * The data DOM node that backs this data instance. Will be null for the root instance or
     * default values.
     */
    private final YangDataDomNode dataDomNode;

    /**
     * The parent structure instance (i.e., the parent container or list). Will be null for
     * the root instance.
     */
    private AbstractStructureInstance parent;

    /**
     * Constructor for an instance that has been specified in data.
     */
    public AbstractDataInstance(final AbstractStatement schemaNode, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent) {
        this.schemaNode = schemaNode;
        this.dataDomNode = Objects.requireNonNull(dataDomNode);
        this.parent = parent;
    }

    /**
     * Constructor for an instance that has been specified as default value.
     */
    public AbstractDataInstance(final AbstractStatement schemaNode, final AbstractStructureInstance parent) {
        this.schemaNode = schemaNode;
        this.dataDomNode = null;
        this.parent = parent;
    }

    public void reparent(final AbstractStructureInstance newParent) {
        parent = newParent;
    }

    /**
     * Returns the schema node (really, a data node) in the schema to which this data instance relates.
     */
    public AbstractStatement getSchemaNode() {
        return schemaNode;
    }

    /**
     * Returns the data DOM node. May be null if NP-container or default value.
     */
    public YangDataDomNode getDataDomNode() {
        return dataDomNode;
    }

    public String getName() {
        if (dataDomNode == null) {
            return schemaNode.getStatementIdentifier();
        }
        return dataDomNode.getName();
    }

    /**
     * Returns the namespace. Where a data DOM node exists, will return that - otherwise, will return the effective
     * namespace of the schema node. May return null.
     */
    public String getNamespace() {
        if (dataDomNode == null) {
            return schemaNode.getEffectiveNamespace();
        }
        return dataDomNode.getNamespace();
    }

    public AbstractStructureInstance getParent() {
        return parent;
    }
}
