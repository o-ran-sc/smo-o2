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

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaRegistry;

@NoArgsConstructor
public class Relationship extends ModuleObject {

    @Getter
    private String aSide;

    @Getter
    private String bSide;

    public Relationship(String module, String type, String id, String aSide, String bSide, List<String> sourceIds) {
        super(module, type, id, sourceIds);
        this.aSide = aSide;
        this.bSide = bSide;
    }

    @Override
    public void parseObject(final YangDataDomNode node) {
        final String name = node.getName();
        if (name.equals("aSide")) {
            aSide = node.getStringValue();
        }
        if (name.equals("bSide")) {
            bSide = node.getStringValue();
        }
        super.parseObject(node);
        node.getChildren().forEach(this::parseObject);
    }

    public String getStoringSideEntityId() {
        RelationType relationType = SchemaRegistry.getRelationTypeByName(getType());
        return switch (relationType.getRelationshipStorageLocation()) {
            case RELATION -> null;
            case A_SIDE -> getASide();
            case B_SIDE -> getBSide();
        };
    }

    public String getNotStoringSideEntityId() {
        RelationType relationType = SchemaRegistry.getRelationTypeByName(getType());
        return switch (relationType.getRelationshipStorageLocation()) {
            case RELATION -> null;
            case A_SIDE -> getBSide();
            case B_SIDE -> getASide();
        };
    }

}
