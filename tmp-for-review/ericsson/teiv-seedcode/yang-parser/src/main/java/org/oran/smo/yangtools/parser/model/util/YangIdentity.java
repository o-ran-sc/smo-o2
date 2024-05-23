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
 * Represents a YANG identity. An YANG identity is uniquely identified by its
 * namespace/module name and identifier. A YANG identity may have one or more "bases"
 * ("super-identities") and/or may have 0..n derived identities.
 *
 * @author Mark Hollmann
 */
public class YangIdentity extends NamespaceModuleIdentifier {

    public YangIdentity(final String namespace, final String moduleName, final String identifier) {
        super(Objects.requireNonNull(namespace), Objects.requireNonNull(moduleName), Objects.requireNonNull(identifier));
    }

    /**
     * The name of the identity
     */
    public String getIdentityName() {
        return getIdentifier();
    }

    /**
     * The name of the module in which the identity has been declared. If the
     * identity has been declared in a submodule, this is the name of the
     * module that owns the submodule.
     */
    public String getIdentityModuleName() {
        return getModuleName();
    }

    /**
     * The namespace of the module in which the identity has been declared. If the
     * identity has been declared in a submodule, this is the namespace of the
     * module that owns the submodule.
     */
    public String getIdentityNamespace() {
        return getNamespace();
    }

    @Override
    public String toString() {
        return "Identity " + getNamespace() + "/" + getModuleName() + "/" + getIdentifier();
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && other.getClass().getName().equals(YangIdentity.class.getName()) && this.toString().equals(
                other.toString());
    }
}
