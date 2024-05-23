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
package org.oran.smo.yangtools.parser.data.dom;

import java.util.Objects;

import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

/**
 * Encodes an annotation value.
 *
 * @author Mark Hollmann
 */
public class YangDataDomNodeAnnotationValue extends NamespaceModuleIdentifier {

    private final Object value;

    public YangDataDomNodeAnnotationValue(final String namespace, final String moduleName, final String annotationName,
            final Object value) {
        super(namespace, moduleName, Objects.requireNonNull(annotationName));
        this.value = value;
    }

    /**
     * May be null if the data input was JSON and resolution has not been done yet.
     */
    public String getNamespace() {
        return super.getNamespace();
    }

    /**
     * May be null if the data input was XML and resolution has not been done yet.
     */
    public String getModuleName() {
        return super.getModuleName();
    }

    public String getName() {
        return super.getIdentifier();
    }

    /**
     * Returns the value of the annotation. The encoding of the value is handled in the same way
     * how it is done for the data DOM node (i.e. it distinguishes between XML and JSON source).
     * May return null (annotation has no argument).
     */
    public Object getValue() {
        return value;
    }
}
