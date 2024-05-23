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
package org.oran.smo.yangtools.parser;

import org.oran.smo.yangtools.parser.model.schema.Schema;

/**
 * Implementations of this interface are called during certain phases of parsing.
 * In general, implementation will perform additional processing on the schema tree,
 * typically to handle extensions.
 *
 * @author Mark Hollmann
 */
public interface CustomProcessor {

    /**
     * Invoked after a schema has been fully parsed but before it has been processed. Resolution
     * of various constructs (e.g. groupings) has not yet been performed at this stage; likewise
     * status and namespace of elements have not been set.
     */
    default void onPreSchemaProcessed(ParserExecutionContext context, Schema schema) {
    }

    /**
     * Invoked after a schema has been fully parsed and processed. If fail-fast is enabled
     * and relevant findings have been issued, this callback will not be invoked.
     */
    default void onPostSchemaProcessed(ParserExecutionContext context, Schema schema) {
    }
}
