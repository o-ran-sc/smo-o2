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
package org.oran.smo.yangtools.parser.model.statements.threegpp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;

/**
 * Constants relating to 3GPP modules.
 *
 * @author Mark Hollmann
 */
public abstract class C3GPP {

    public static final String THREEGPP_COMMON_YANG_EXTENSIONS_MODULE_NAME = "_3gpp-common-yang-extensions";

    public static final String INITIAL_VALUE = "initial-value";
    public static final String IN_VARIANT = "inVariant";
    public static final String NOT_NOTIFYABLE = "notNotifyable";

    private static final List<String> FROM_MODULE_THREEGPP_COMMON_YANG_EXTENSIONS = Arrays.asList(INITIAL_VALUE, IN_VARIANT,
            NOT_NOTIFYABLE);

    public static final StatementModuleAndName THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE = new StatementModuleAndName(
            THREEGPP_COMMON_YANG_EXTENSIONS_MODULE_NAME, INITIAL_VALUE);
    public static final StatementModuleAndName THREEGPP_COMMON_YANG_EXTENSIONS__IN_VARIANT = new StatementModuleAndName(
            THREEGPP_COMMON_YANG_EXTENSIONS_MODULE_NAME, IN_VARIANT);
    public static final StatementModuleAndName THREEGPP_COMMON_YANG_EXTENSIONS__NOT_NOTIFYABLE = new StatementModuleAndName(
            THREEGPP_COMMON_YANG_EXTENSIONS_MODULE_NAME, NOT_NOTIFYABLE);

    public static final Map<String, List<String>> HANDLED_STATEMENTS = new HashMap<>();

    static {
        HANDLED_STATEMENTS.put(THREEGPP_COMMON_YANG_EXTENSIONS_MODULE_NAME, FROM_MODULE_THREEGPP_COMMON_YANG_EXTENSIONS);
    }
}
