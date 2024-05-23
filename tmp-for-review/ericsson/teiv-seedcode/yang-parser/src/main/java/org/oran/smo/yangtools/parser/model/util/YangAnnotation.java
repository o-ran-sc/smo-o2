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
package org.oran.smo.yangtools.parser.model.util;

import java.util.Objects;

import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

/**
 * Denotes a single annotation declared in a module. The annotation has been declared in accordance with RFC 7952. It
 * has special significance as it likely to become part of the YANG core language at some stage.
 *
 * @author Mark Hollmann
 */
public class YangAnnotation extends NamespaceModuleIdentifier {

    public YangAnnotation(final String owningModuleNamespace, final String owningModule, final String annotationName) {
        super(Objects.requireNonNull(owningModuleNamespace), Objects.requireNonNull(owningModule), Objects.requireNonNull(
                annotationName));
    }

    /**
     * The name of the annotation
     */
    public String getAnnotationName() {
        return getIdentifier();
    }

    /**
     * The module owning the annotation. If the annotation has been declared in a
     * submodule, this is the name of the module that owns the submodule.
     */
    public String getAnnotationModuleName() {
        return getModuleName();
    }

    /**
     * The namespace of the module in which the annotation has been declared. If the
     * annotation has been declared in a submodule, this is the namespace of the
     * module that owns the submodule.
     */
    public String getAnnotationNamespace() {
        return getNamespace();
    }

    @Override
    public String toString() {
        return "Annotation " + getAnnotationNamespace() + "/" + getAnnotationModuleName() + "/" + getAnnotationName();
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && other.getClass().getName().equals(YangAnnotation.class.getName()) && this.toString().equals(
                other.toString());
    }
}
