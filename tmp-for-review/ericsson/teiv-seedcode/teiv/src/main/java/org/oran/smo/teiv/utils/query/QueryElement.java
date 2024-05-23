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
package org.oran.smo.teiv.utils.query;

import org.oran.smo.teiv.utils.path.TiesPathQuery;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@NoArgsConstructor
class QueryElement {
    private String objectType;
    private List<String> attributes = null;
    private Map<TokenType, List<TiesPathQuery>> filters = null;
    private List<QueryElement> children = new ArrayList<>();
    private boolean isIncluded = false;
    private boolean isManyToMany = false;
    private boolean isRelConnectingSameEntity = false;

    public QueryElement(String objectType) {
        this.objectType = objectType;
    }

    public QueryElement(String objectType, boolean isIncluded) {
        this.objectType = objectType;
        this.isIncluded = isIncluded;
    }

    public void addAttribute(String attribute) {
        attributes.add(attribute);
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public void addFilter(TokenType type, TiesPathQuery filter) {
        filters.putIfAbsent(type, new ArrayList<>());
        filters.get(type).add(filter);

    }

    public boolean hasFilters() {
        return filters != null;
    }

    public void setFilters(Map<TokenType, List<TiesPathQuery>> filters) {
        this.filters = filters;
    }

    public void addChild(QueryElement child) {
        addChild(child, true);
    }

    public void addChild(QueryElement child, boolean safeAdd) {
        if (safeAdd && this.hasChild(child.objectType)) {
            return;
        }
        children.add(child);
    }

    public void removeChild(QueryElement child) {
        children.remove(child);
    }

    public boolean hasAttributes() {
        return attributes != null;
    }

    public List<TiesPathQuery> getFiltersOfTypes(List<TokenType> types) {
        return types.stream().flatMap(type -> filters.containsKey(type) ? filters.get(type).stream() : null).toList();
    }

    public boolean hasFilterOfType(TokenType type) {
        return filters.containsKey(type);
    }

    public boolean hasChild(String child) {
        return children.stream().anyMatch(c -> c.getObjectType().equals(child));
    }

    public QueryElement getChild(String objectType) {
        Optional<QueryElement> result = children.stream().filter(child -> child.getObjectType().equals(objectType))
                .findFirst();
        return result.orElse(null);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void include() {
        isIncluded = true;
    }

    public void setManyToMany() {
        isManyToMany = true;
    }

    public void setRelConnectingSameEntity() {
        isRelConnectingSameEntity = true;
    }
}
