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

import java.util.List;
import java.util.Map;

import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatementClassSupplier;

/**
 * Class supplier for extensions defined in various IETF RFCs.
 *
 * @author Mark Hollmann
 */
public class IetfExtensionsClassSupplier extends AbstractStatementClassSupplier {

    @Override
    public Map<String, List<String>> getHandledStatements() {
        return CIETF.HANDLED_STATEMENTS;
    }

    @Override
    public <T extends AbstractStatement> Class<T> getJavaClazzForStatement(final String moduleName,
            final String extStatementName) {

        if (CIETF.IETF_YANG_SCHEMA_MOUNT_MODULE_NAME.equals(moduleName)) {
            return handleIetfYangSchemaMountExtensions(extStatementName);
        }

        if (CIETF.IETF_YANG_METADATA_MODULE_NAME.equals(moduleName)) {
            return handleIetfYangMetadataExtensions(extStatementName);
        }

        if (CIETF.IETF_NETCONF_ACM_MODULE_NAME.equals(moduleName)) {
            return handleIetfNetconfAcmExtensions(extStatementName);
        }

        return null;
    }

    private static final String IETF_EXTENSIONS_JAVA_PACKAGE = YIetfMountPoint.class.getPackage().getName();
    private static final String IETF_EXTENSIONS_JAVA_CLASS_PREFIX = "YIetf";

    private <T extends AbstractStatement> Class<T> handleIetfYangSchemaMountExtensions(final String extStatementName) {
        return getJavaClazzForStatement(IETF_EXTENSIONS_JAVA_PACKAGE, IETF_EXTENSIONS_JAVA_CLASS_PREFIX, extStatementName);
    }

    private <T extends AbstractStatement> Class<T> handleIetfYangMetadataExtensions(final String extStatementName) {
        return getJavaClazzForStatement(IETF_EXTENSIONS_JAVA_PACKAGE, IETF_EXTENSIONS_JAVA_CLASS_PREFIX, extStatementName);
    }

    private <T extends AbstractStatement> Class<T> handleIetfNetconfAcmExtensions(final String extStatementName) {
        return getJavaClazzForStatement(IETF_EXTENSIONS_JAVA_PACKAGE, IETF_EXTENSIONS_JAVA_CLASS_PREFIX, extStatementName);
    }
}
