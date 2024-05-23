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
package org.oran.smo.yangtools.parser.model.statements.oran;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;

/**
 * Constants relating to ORAN modules.
 *
 * @author Mark Hollmann
 */
public abstract class CORAN {

    public static final String ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME = "o-ran-smo-teiv-common-yang-extensions";

    public static final String SMO_TEIV_BI_DIRECTIONAL_TOPOLOGY_RELATIONSHIP = "biDirectionalTopologyRelationship";
    public static final String SMO_TEIV_A_SIDE = "aSide";
    public static final String SMO_TEIV_B_SIDE = "bSide";
    public static final String SMO_TEIV_DOMAIN = "domain";
    public static final String SMO_TEIV_LABEL = "label";

    private static final List<String> FROM_MODULE_ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS = Arrays.asList(
            SMO_TEIV_BI_DIRECTIONAL_TOPOLOGY_RELATIONSHIP, SMO_TEIV_A_SIDE, SMO_TEIV_B_SIDE, SMO_TEIV_DOMAIN,
            SMO_TEIV_LABEL);

    public static final StatementModuleAndName ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__BI_DIRECTIONAL_TOPOLOGY_RELATIONSHIP = new StatementModuleAndName(
            ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME, SMO_TEIV_BI_DIRECTIONAL_TOPOLOGY_RELATIONSHIP);
    public static final StatementModuleAndName ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__A_SIDE = new StatementModuleAndName(
            ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME, SMO_TEIV_A_SIDE);
    public static final StatementModuleAndName ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__B_SIDE = new StatementModuleAndName(
            ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME, SMO_TEIV_B_SIDE);
    public static final StatementModuleAndName ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__DOMAIN = new StatementModuleAndName(
            ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME, SMO_TEIV_DOMAIN);
    public static final StatementModuleAndName ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS__LABEL = new StatementModuleAndName(
            ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME, SMO_TEIV_LABEL);

    public static final Map<String, List<String>> HANDLED_STATEMENTS = new HashMap<>();

    static {
        HANDLED_STATEMENTS.put(ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS_MODULE_NAME,
                FROM_MODULE_ORAN_SMO_TEIV_COMMON_YANG_EXTENSIONS);
    }
}
