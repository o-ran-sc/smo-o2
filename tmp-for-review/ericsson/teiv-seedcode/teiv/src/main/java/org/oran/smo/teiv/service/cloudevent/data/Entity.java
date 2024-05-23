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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;

public class Entity extends ModuleObject {

    @Getter
    private final Map<String, Object> attributes;

    public Entity(String module, String type, String id, Map<String, Object> attributes, List<String> sourceIds) {
        super(module, type, id, sourceIds);
        this.attributes = new HashMap<>(attributes);
    }

    public Entity() {
        this.attributes = new HashMap<>();
    }

    @Override
    public void parseObject(final YangDataDomNode node) {
        final String name = node.getName();
        if ("attributes".equals(name)) {
            setAttributes(node);
        } else {
            super.parseObject(node);
            node.getChildren().forEach(this::parseObject);
        }
    }

    private void setAttributes(final YangDataDomNode node) {
        node.getChildren().forEach(this::handleJsonObjects);
        attributes.keySet().forEach(name -> {
            final Object v = attributes.get(name);
            if (v instanceof List) {
                attributes.put(name, v.toString());
            }
        });
    }

    private boolean isJsonObject(final YangDataDomNode node) {
        return !node.getChildren().isEmpty();
    }

    private void handleJsonObjects(final YangDataDomNode node) {
        final Object object = isJsonObject(node) ? jsonToString(node) : node.getValue();
        final String name = node.getName();
        if (attributes.containsKey(name)) {
            Object v = attributes.get(name);
            if (v instanceof List) {
                ((List) v).add(object);
            } else {
                List<Object> l = new ArrayList<>();
                l.add(v);
                l.add(object);
                attributes.put(name, l);
            }
        } else {
            attributes.put(name, object);
        }
    }

    private String jsonToString(final YangDataDomNode node) {
        if (node.getChildren().isEmpty()) {
            final Object value = node.getValue();
            if (value instanceof String) {
                return getQuotedString(value.toString());
            }
            return String.valueOf(value);
        } else {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Iterator<YangDataDomNode> it = node.getChildren().iterator(); it.hasNext(); first = false) {
                final YangDataDomNode actual = it.next();
                if (!first) {
                    sb.append(",");
                }
                sb.append(getQuotedString(actual.getName())).append(":").append(jsonToString(actual));
            }
            return sb.append("}").toString();
        }
    }

    private String getQuotedString(final String str) {
        return String.format(QUOTED_STRING, str);
    }
}
