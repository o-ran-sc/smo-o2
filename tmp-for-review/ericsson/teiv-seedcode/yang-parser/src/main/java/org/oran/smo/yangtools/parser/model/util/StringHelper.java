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
package org.oran.smo.yangtools.parser.model.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;

/**
 * Some utility methods used in various places, mostly for info texts.
 *
 * @author Mark Hollmann
 */
public abstract class StringHelper {

    public static String getModuleNameAndRevision(final String moduleName, final String revision) {

        final StringBuilder sb = new StringBuilder();

        sb.append("'");
        sb.append(Objects.requireNonNull(moduleName));

        if (revision != null && !revision.isEmpty()) {
            sb.append('/');
            sb.append(revision);
        }
        sb.append("'");

        return sb.toString();
    }

    public static String getModuleLineString(final AbstractStatement statement) {

        final StringBuilder sb = new StringBuilder();

        if (statement.getDomElement().getYangModel().getYangModelRoot().isModule()) {
            sb.append("module '");
        } else {
            sb.append("submodule '");
        }

        sb.append(statement.getDomElement().getYangModel().getYangModelRoot().getModuleOrSubModuleName());
        sb.append("' (line ");
        sb.append(statement.getDomElement().getLineNumber());
        sb.append(')');

        return sb.toString();
    }

    /**
     * Utility to assemble from a collection a nicely formatted string based on some separation sequences.
     * The order of elements will be taken from the collection iterator.
     */
    public static <T extends Object> String toString(final Collection<T> list, final String start, final String end,
            final String elemSep, final String elemStart, final String elemEnd) {

        final StringBuilder sb = new StringBuilder();

        if (start != null) {
            sb.append(start);
        }

        boolean first = true;
        final Iterator<T> iter = list.iterator();

        while (iter.hasNext()) {

            if (!first && elemSep != null) {
                sb.append(elemSep);
            }

            if (elemStart != null) {
                sb.append(elemStart);
            }

            sb.append(iter.next().toString());

            if (elemEnd != null) {
                sb.append(elemEnd);
            }

            first = false;
        }

        if (end != null) {
            sb.append(end);
        }

        return sb.toString();
    }
}
