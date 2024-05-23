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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;

/**
 * This class represents a structural element, which can be either a true (singleton)
 * container, or an instance of a list.
 *
 * @author Mark Hollmann
 */
public abstract class AbstractStructureInstance extends AbstractDataInstance {

    /**
     * The child structure instances, if any (i.e. containers / lists under this container / list).
     */
    private final List<AbstractStructureInstance> structureChildren = new ArrayList<>();

    /**
     * The content values, if any (non-presence containers are usually empty), of the structure (i.e. leaf or leaf-list
     * data).
     */
    private final List<AbstractContentInstance> contentChildren = new ArrayList<>();

    /**
     * Constructor for a structure instance that was specified in data.
     */
    public AbstractStructureInstance(final AbstractStatement schemaNode, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parent) {
        super(schemaNode, dataDomNode, parent);
    }

    /**
     * Constructor for a structure instance that was created by default (i.e. not specified in data).
     */
    public AbstractStructureInstance(final AbstractStatement schemaNode, final AbstractStructureInstance parent) {
        super(schemaNode, parent);
    }

    public void addStructureChild(final AbstractStructureInstance structureChild) {
        structureChildren.add(structureChild);
    }

    public List<AbstractStructureInstance> getStructureChildren() {
        return structureChildren;
    }

    public void addContentChild(final AbstractContentInstance contentChild) {
        contentChildren.add(contentChild);
    }

    public List<AbstractContentInstance> getContentChildren() {
        return contentChildren;
    }

    /**
     * Returns the container instance of the given namespace and name. May return null if not found.
     */
    public ContainerInstance getContainerInstance(final String namespace, final String name) {
        return (ContainerInstance) structureChildren.stream().filter(new InstanceTester(namespace, name,
                ContainerInstance.class)).findFirst().orElse(null);
    }

    public boolean hasContainerInstance(final String namespace, final String name) {
        return structureChildren.stream().filter(new InstanceTester(namespace, name, ContainerInstance.class)).findFirst()
                .isPresent();
    }

    /**
     * Returns the leaf instance of the given namespace and name. May return null if not found.
     */
    public LeafInstance getLeafInstance(final String namespace, final String name) {
        return (LeafInstance) contentChildren.stream().filter(new InstanceTester(namespace, name, LeafInstance.class))
                .findFirst().orElse(null);
    }

    public boolean hasLeafInstance(final String namespace, final String name) {
        return contentChildren.stream().filter(new InstanceTester(namespace, name, LeafInstance.class)).findFirst()
                .isPresent();
    }

    /**
     * Returns all occurrences of list instances of the given namespace and name.
     */
    public List<ListInstance> getListInstances(final String namespace, final String name) {
        return (List<ListInstance>) structureChildren.stream().filter(new InstanceTester(namespace, name,
                ListInstance.class)).map(child -> (ListInstance) child).collect(Collectors.toList());
    }

    public ListInstance getListInstance(final String namespace, final String name, final Map<String, String> keyValues) {
        return structureChildren.stream().filter(new InstanceTester(namespace, name, ListInstance.class)).map(
                child -> (ListInstance) child).filter(listChild -> keyValues.equals(listChild.getKeyValues())).findFirst()
                .orElse(null);
    }

    public boolean hasListInstance(final String namespace, final String name) {
        return structureChildren.stream().filter(new InstanceTester(namespace, name, ListInstance.class)).findFirst()
                .isPresent();
    }

    public boolean hasListInstance(final String namespace, final String name, final Map<String, String> keyValues) {
        return structureChildren.stream().filter(new InstanceTester(namespace, name, ListInstance.class)).filter(
                child -> keyValues.equals(((ListInstance) child).getKeyValues())).findFirst().isPresent();
    }

    /**
     * Returns all occurrences of leaf-list instances of the given namespace and name.
     */
    public List<LeafListInstance> getLeafListInstances(final String namespace, final String name) {
        return (List<LeafListInstance>) contentChildren.stream().filter(new InstanceTester(namespace, name,
                LeafListInstance.class)).map(child -> (LeafListInstance) child).collect(Collectors.toList());
    }

    /**
     * Returns the values of leaf-list. Note that the order may be undefined where a merge of multiple inputs happened.
     */
    public List<Object> getLeafListValues(final String namespace, final String name) {
        return (List<Object>) contentChildren.stream().filter(new InstanceTester(namespace, name, LeafListInstance.class))
                .map(child -> ((LeafListInstance) child).getValue()).collect(Collectors.toList());
    }

    public boolean hasLeafListInstance(final String namespace, final String name) {
        return contentChildren.stream().filter(new InstanceTester(namespace, name, LeafListInstance.class)).findFirst()
                .isPresent();
    }

    public boolean hasLeafListInstance(final String namespace, final String name, final Object value) {
        return contentChildren.stream().filter(new InstanceTester(namespace, name, LeafListInstance.class)).filter(
                child -> value.equals(((LeafListInstance) child).getValue())).findFirst().isPresent();
    }

    /**
     * Returns the anydata instance of the given namespace and name. May return null if not found.
     */
    public AnyDataInstance getAnyDataInstance(final String namespace, final String name) {
        return (AnyDataInstance) contentChildren.stream().filter(new InstanceTester(namespace, name, AnyDataInstance.class))
                .findFirst().orElse(null);
    }

    public boolean hasAnyDataInstance(final String namespace, final String name) {
        return contentChildren.stream().filter(new InstanceTester(namespace, name, AnyDataInstance.class)).findFirst()
                .isPresent();
    }

    /**
     * Returns the anyxml instance of the given namespace and name. May return null if not found.
     */
    public AnyXmlInstance getAnyXmlInstance(final String namespace, final String name) {
        return (AnyXmlInstance) contentChildren.stream().filter(new InstanceTester(namespace, name, AnyXmlInstance.class))
                .findFirst().orElse(null);
    }

    public boolean hasAnyXmlInstance(final String namespace, final String name) {
        return contentChildren.stream().filter(new InstanceTester(namespace, name, AnyXmlInstance.class)).findFirst()
                .isPresent();
    }

    private class InstanceTester implements Predicate<AbstractDataInstance> {

        private final String namespace;
        private final String name;
        private final Class<? extends AbstractDataInstance> soughtClazz;

        public <T extends AbstractDataInstance> InstanceTester(final String namespace, final String name,
                final Class<T> soughtClazz) {
            this.namespace = namespace;
            this.name = name;
            this.soughtClazz = soughtClazz;
        }

        @Override
        public boolean test(final AbstractDataInstance dataInstance) {
            return name.equals(dataInstance.getName()) && namespace.equals(dataInstance.getNamespace()) && (dataInstance
                    .getClass().equals(soughtClazz));
        }
    }

    /**
     * Returns a human-readable string with the full path to the Container node.
     */
    public String getPath() {

        final List<AbstractStructureInstance> containersFromRoot = new ArrayList<>(10);
        AbstractStructureInstance runContainer = this;

        while (runContainer != null) {
            containersFromRoot.add(0, runContainer);
            runContainer = runContainer.getParent();
        }

        final StringBuilder sb = new StringBuilder();
        for (final AbstractStructureInstance container : containersFromRoot) {
            sb.append('/').append(container.getName());
        }

        return sb.toString();
    }

}
