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
package org.oran.smo.yangtools.parser.data.instance;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;

/**
 * Predicate that is used when a Yang data DOM tree is translated to an instance tree. Used to filter the
 * top-level objects in the DOM. An example of this is where a client wishes to only process a subset of
 * the data DOM that has been read in.
 *
 * @author Mark Hollmann
 */
public class DataTreeBuilderPredicate implements Predicate<YangDataDomNode> {

    public static final DataTreeBuilderPredicate ALLOW_ALL = new DataTreeBuilderPredicate();

    private final Set<String> filterInNamespaces;

    public DataTreeBuilderPredicate() {
        this.filterInNamespaces = Collections.<String> emptySet();
    }

    public DataTreeBuilderPredicate(final Set<String> filterInNamespaces) {
        this.filterInNamespaces = Objects.requireNonNull(filterInNamespaces);
    }

    @Override
    public boolean test(final YangDataDomNode domNode) {
        return filterInNamespaces.isEmpty() || filterInNamespaces.contains(domNode.getNamespace());
    }

}
