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
package org.oran.smo.teiv.service.cloudevent.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

import java.util.ArrayList;
import java.util.List;

import static org.oran.smo.teiv.utils.TiesConstants.SOURCE_IDS;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ModuleObject {

    private String module;
    private String type;
    private String id;
    private List<String> sourceIds;

    public void parseObject(final YangDataDomNode node) {
        final String name = node.getName();
        if (node.getChildren().isEmpty()) {
            if (name.equals("id")) {
                id = node.getStringValue();
            } else if ((SOURCE_IDS).equals(name)) {
                addSourceIds(node.getStringValue());
            }
        } else {
            if (node.getParentNode() != null && node.getParentNode().getModuleName().equals("/")) {
                module = node.getModuleName();
                type = node.getName();
            }
        }
    }

    public void addSourceIds(String sourceIds) {
        if (this.sourceIds == null) {
            this.sourceIds = new ArrayList<>();
        }
        this.sourceIds.add(sourceIds);
    }
}
