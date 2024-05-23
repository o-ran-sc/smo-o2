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
package org.oran.smo.teiv.utils.path;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter(AccessLevel.PACKAGE)
public class TiesPathQuery {
    private String tiesPathPrefix;
    private String normalizedParentPath;
    private String normalizedXpath;
    private List<String> containerNames;
    private List<DataLeaf> leavesData;
    private String ancestorSchemaNodeIdentifier = "";
    private String textFunctionConditionLeafName;
    private String textFunctionConditionValue;
    private List<String> booleanOperators;
    private List<String> comparativeOperators;
    private String containsFunctionConditionLeafName;
    private String containsFunctionConditionValue;
    private List<String> attributeNames;

    public boolean hasLeafConditions() {
        return leavesData != null;
    }

    public boolean hasContainsFunctionCondition() {
        return containsFunctionConditionLeafName != null;
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class DataLeaf {
        private final String name;
        private final Object value;
    }
}
