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
package org.oran.smo.teiv.exposure.tiespath.resolver;

import java.util.List;

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ContainerType;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import jakarta.annotation.Nullable;
import lombok.NonNull;

public interface PathResolver<T> {

    String NULL_ROOT_OBJECT = "*";

    T resolve(String rootObject, @NonNull String filter);

    default String getTopologyObject(String rootObject, List<String> containerNames) {
        int noOfContainers = containerNames.size();
        if (noOfContainers > 2) {
            throw TiesPathException.grammarError("More than two level deep path is not allowed");
        } else if (noOfContainers == 2) {
            return getTopologyObjectWhenTwoContainers(rootObject, containerNames.get(0));
        }
        return isRootObjectNullOrEmpty(rootObject) ? NULL_ROOT_OBJECT : rootObject;
    }

    default String getTopologyObjectWhenTwoContainers(String rootObject, String firstContainer) {
        if (isRootObjectNullOrEmpty(rootObject) || firstContainer.equals(rootObject)) {
            return firstContainer;
        } else {
            throw TiesPathException.grammarError(
                    "Target filter can only contain Root Object types mentioned in the path parameter");
        }
    }

    default boolean isRootObjectNullOrEmpty(String rootObjectType) {
        return rootObjectType == null || rootObjectType.isEmpty();
    }

    @Nullable
    default ContainerType getContainerType(List<String> containerNames) {
        return ContainerType.fromValue(containerNames.get(containerNames.size() - 1));
    }
}
