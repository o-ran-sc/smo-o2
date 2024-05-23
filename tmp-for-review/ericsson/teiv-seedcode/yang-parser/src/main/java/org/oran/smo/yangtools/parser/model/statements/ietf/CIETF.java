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
package org.oran.smo.yangtools.parser.model.statements.ietf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;

/**
 * Constants relating to IETF modules.
 *
 * @author Mark Hollmann
 */
public abstract class CIETF {

    public static final String IETF_YANG_SCHEMA_MOUNT_MODULE_NAME = "ietf-yang-schema-mount";
    public static final String IETF_YANG_METADATA_MODULE_NAME = "ietf-yang-metadata";
    public static final String IETF_NETCONF_ACM_MODULE_NAME = "ietf-netconf-acm";

    public static final String MOUNT_POINT = "mount-point";
    public static final String ANNOTATION = "annotation";
    public static final String DEFAULT_DENY_WRITE = "default-deny-write";
    public static final String DEFAULT_DENY_ALL = "default-deny-all";

    private static final List<String> FROM_MODULE_IETF_YANG_SCHEMA_MOUNT = Arrays.asList(MOUNT_POINT);
    private static final List<String> FROM_MODULE_IETF_YANG_METADATA = Arrays.asList(ANNOTATION);
    private static final List<String> FROM_MODULE_IETF_NETCONF_ACM = Arrays.asList(DEFAULT_DENY_WRITE, DEFAULT_DENY_ALL);

    public static final StatementModuleAndName IETF_YANG_SCHEMA_MOUNT__MOUNT_POINT = new StatementModuleAndName(
            IETF_YANG_SCHEMA_MOUNT_MODULE_NAME, MOUNT_POINT);
    public static final StatementModuleAndName IETF_YANG_METADATA__ANNOTATION = new StatementModuleAndName(
            IETF_YANG_METADATA_MODULE_NAME, ANNOTATION);
    public static final StatementModuleAndName IETF_NETCONF_ACM__DEFAULT_DENY_WRITE = new StatementModuleAndName(
            IETF_NETCONF_ACM_MODULE_NAME, DEFAULT_DENY_WRITE);
    public static final StatementModuleAndName IETF_NETCONF_ACM__DEFAULT_DENY_ALL = new StatementModuleAndName(
            IETF_NETCONF_ACM_MODULE_NAME, DEFAULT_DENY_ALL);

    public static final Map<String, List<String>> HANDLED_STATEMENTS = new HashMap<>();

    static {
        HANDLED_STATEMENTS.put(IETF_YANG_SCHEMA_MOUNT_MODULE_NAME, FROM_MODULE_IETF_YANG_SCHEMA_MOUNT);
        HANDLED_STATEMENTS.put(IETF_YANG_METADATA_MODULE_NAME, FROM_MODULE_IETF_YANG_METADATA);
        HANDLED_STATEMENTS.put(IETF_NETCONF_ACM_MODULE_NAME, FROM_MODULE_IETF_NETCONF_ACM);
    }
}
