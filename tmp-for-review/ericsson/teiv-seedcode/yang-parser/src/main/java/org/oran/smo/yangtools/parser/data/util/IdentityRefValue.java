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
package org.oran.smo.yangtools.parser.data.util;

import java.util.Objects;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Holds the value of a data node instance of type "identityref"
 *
 * @author Mark Hollmann
 */
public class IdentityRefValue {

    private final NamespaceModuleIdentifier value;

    public IdentityRefValue(final String namespace, final String moduleName, final String identityName) {
        this.value = new NamespaceModuleIdentifier(namespace, moduleName, Objects.requireNonNull(identityName));
    }

    /**
     * Constructor for data encoded in XML, where prefixes are used and a prefix resolver is available for the namespace
     * resolution.
     */
    public IdentityRefValue(final String val, final PrefixResolver prefixResolver, final String defaultNamespace) {

        final boolean hasPrefix = QNameHelper.hasPrefix(val);
        final String namespace = hasPrefix ?
                prefixResolver.resolveNamespaceUri(QNameHelper.extractPrefix(val)) :
                defaultNamespace;
        final String name = hasPrefix ? QNameHelper.extractName(val) : val;

        value = new NamespaceModuleIdentifier(namespace, null, name);
    }

    /**
     * Constructor for data encoded in JSON, where module names are used.
     */
    public IdentityRefValue(final String val, final String defaultModuleName) {

        final boolean hasPrefix = QNameHelper.hasPrefix(val);
        final String moduleName = hasPrefix ? QNameHelper.extractPrefix(val) : defaultModuleName;
        final String name = hasPrefix ? QNameHelper.extractName(val) : val;

        value = new NamespaceModuleIdentifier(null, moduleName, name);
    }

    /**
     * The name of the identity
     */
    public String getIdentityName() {
        return value.getIdentifier();
    }

    /**
     * The name of the module in which the identity has been declared. May return null if the value was encoded in XML.
     */
    public String getIdentityModuleName() {
        return value.getModuleName();
    }

    /**
     * The namespace of the module in which the identity has been declared. May return null if the value was encoded in
     * JSON.
     */
    public String getIdentityNamespace() {
        return value.getNamespace();
    }

    @Override
    public String toString() {
        return "IdentityRef value " + getIdentityNamespace() + "/" + getIdentityModuleName() + "/" + getIdentityName();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !obj.getClass().getName().equals(IdentityRefValue.class.getName())) {
            return false;
        }

        final IdentityRefValue other = (IdentityRefValue) obj;

        if (!this.getIdentityName().equals(other.getIdentityName())) {
            return false;
        }

        if (this.getIdentityModuleName() != null && other.getIdentityModuleName() != null && this
                .getIdentityNamespace() != null && other.getIdentityNamespace() != null) {
            return this.getIdentityModuleName().equals(other.getIdentityModuleName()) && this.getIdentityNamespace().equals(
                    other.getIdentityNamespace());
        }

        /*
         * The comparison is a little different to how this would usually be done. Depending on the encoding
         * of the input, either the namespace or the module name may be null. However, a client having constructed
         * such an object, will typically have knowledge of both (as they will know the model). So we will try both.
         */
        if (this.getIdentityModuleName() != null && other.getIdentityModuleName() != null) {
            return this.getIdentityModuleName().equals(other.getIdentityModuleName());
        }

        if (this.getIdentityNamespace() != null && other.getIdentityNamespace() != null) {
            return this.getIdentityNamespace().equals(other.getIdentityNamespace());
        }

        return false;
    }
}
