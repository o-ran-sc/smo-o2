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
package org.oran.smo.teiv.exposure.tiespath.innerlanguage;

import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public enum ContainerType {
    ATTRIBUTES("attributes"),
    DECORATORS("decorators"),
    CLASSIFIERS("classifiers"),
    ID("id"),
    SOURCE_IDS("sourceIds"),
    ASSOCIATION("association");

    private final String value;

    ContainerType(String value) {
        this.value = value;
    }

    @Nullable
    public static ContainerType fromValue(String value) {
        for (ContainerType containerType : ContainerType.values()) {
            if (containerType.getValue().equals(value)) {
                return containerType;
            }
        }
        return null;
    }
}
