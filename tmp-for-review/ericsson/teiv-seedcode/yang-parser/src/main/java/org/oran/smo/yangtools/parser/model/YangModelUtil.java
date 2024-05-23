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
package org.oran.smo.yangtools.parser.model;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.input.YangInput;
import org.oran.smo.yangtools.parser.model.schema.Schema;

public abstract class YangModelUtil {

    /**
     * Utility to extract the identity of a module from a YAM.
     * <p>
     * If the input is not a YAM, or is significantly corrupted or badly formatted, the module identity
     * cannot be extracted and null will be returned.
     */
    public static ModuleIdentity getYamModuleIdentity(final YangInput yangInput) {

        final ParserExecutionContext context = new ParserExecutionContext(new FindingsManager(
                new ModifyableFindingSeverityCalculator()));
        final Schema schema = new Schema();

        final YangModel yangModel = new YangModel(yangInput, ConformanceType.IMPLEMENT);
        yangModel.parse(context, schema.getModuleNamespaceResolver(), schema, false);

        return yangModel.getModuleIdentity();
    }
}
