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
package org.oran.smo.yangtools.parser.model.statements.yang.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement.StatementArgumentType;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YAnydata;
import org.oran.smo.yangtools.parser.model.statements.yang.YAnyxml;
import org.oran.smo.yangtools.parser.model.statements.yang.YArgument;
import org.oran.smo.yangtools.parser.model.statements.yang.YContact;
import org.oran.smo.yangtools.parser.model.statements.yang.YDescription;
import org.oran.smo.yangtools.parser.model.statements.yang.YExtension;
import org.oran.smo.yangtools.parser.model.statements.yang.YGrouping;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YNamespace;
import org.oran.smo.yangtools.parser.model.statements.yang.YOrganization;
import org.oran.smo.yangtools.parser.model.statements.yang.YPrefix;
import org.oran.smo.yangtools.parser.model.statements.yang.YYangVersion;
import org.oran.smo.yangtools.parser.model.statements.yang.YYinElement;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class ModuleTest extends YangTestCommon {

    @Test
    public void testIdentity() {

        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());

        parseRelativeImplementsYangModels(Arrays.asList("module-test/module-test.yang"));

        assertNoFindings();

        final YModule yModule = getModule("module-test");
        assertNotNull(yModule);

        assertEquals(StatementArgumentType.NAME, yModule.getArgumentType());
        assertEquals(CY.STMT_MODULE, yModule.getStatementModuleAndName());
        assertEquals("module-test", yModule.getModuleName());
        assertEquals("module-test", yModule.getStatementIdentifier());
        assertFalse(yModule.definesDataNode());
        assertFalse(yModule.definesSchemaNode());

        final YYangVersion yYangVersion = yModule.getYangVersion();
        assertNotNull(yYangVersion);

        assertEquals(StatementArgumentType.VALUE, yYangVersion.getArgumentType());
        assertEquals(CY.STMT_YANG_VERSION, yYangVersion.getStatementModuleAndName());
        assertFalse(yYangVersion.is10Version());
        assertTrue(yYangVersion.is11Version());
        assertFalse(yYangVersion.definesDataNode());
        assertFalse(yYangVersion.definesSchemaNode());

        final YNamespace yNamespace = yModule.getNamespace();
        assertNotNull(yNamespace);

        assertEquals(StatementArgumentType.URI, yNamespace.getArgumentType());
        assertEquals(CY.STMT_NAMESPACE, yNamespace.getStatementModuleAndName());
        assertEquals("test:module-test", yNamespace.getNamespace());
        assertFalse(yNamespace.definesDataNode());
        assertFalse(yNamespace.definesSchemaNode());

        final YPrefix yPrefix = yModule.getPrefix();
        assertNotNull(yPrefix);

        assertEquals(StatementArgumentType.VALUE, yPrefix.getArgumentType());
        assertEquals(CY.STMT_PREFIX, yPrefix.getStatementModuleAndName());
        assertEquals("this", yPrefix.getPrefix());
        assertFalse(yPrefix.definesDataNode());
        assertFalse(yPrefix.definesSchemaNode());

        final YContact yContact = yModule.getContact();
        assertNotNull(yContact);

        assertEquals(StatementArgumentType.TEXT, yContact.getArgumentType());
        assertEquals(CY.STMT_CONTACT, yContact.getStatementModuleAndName());
        assertEquals("test-user", yContact.getValue());
        assertFalse(yContact.definesDataNode());
        assertFalse(yContact.definesSchemaNode());

        final YOrganization yOrganization = yModule.getOrganization();
        assertNotNull(yOrganization);

        assertEquals(StatementArgumentType.TEXT, yOrganization.getArgumentType());
        assertEquals(CY.STMT_ORGANIZATION, yOrganization.getStatementModuleAndName());
        assertEquals("ORAN", yOrganization.getValue());
        assertFalse(yOrganization.definesDataNode());
        assertFalse(yOrganization.definesSchemaNode());

        final YDescription yDescription = yModule.getDescription();
        assertNotNull(yDescription);

        assertEquals(StatementArgumentType.TEXT, yDescription.getArgumentType());
        assertEquals(CY.STMT_DESCRIPTION, yDescription.getStatementModuleAndName());
        assertTrue(yDescription.getValue().startsWith("some description"));
        try {
            yDescription.getDescription();
            fail("Should have thrown.");
        } catch (final Exception expected) {
        }
        assertFalse(yDescription.definesDataNode());
        assertFalse(yDescription.definesSchemaNode());

        final List<YExtension> yExtensions = yModule.getExtensions();
        assertEquals(2, yExtensions.size());

        assertEquals(StatementArgumentType.NAME, yExtensions.get(0).getArgumentType());
        assertEquals(CY.STMT_EXTENSION, yExtensions.get(0).getStatementModuleAndName());
        assertEquals("my-extension1", yExtensions.get(0).getExtensionName());
        assertEquals("my-extension1", yExtensions.get(0).getStatementIdentifier());
        assertTrue(yExtensions.get(0).getStatus().isDeprecated());
        assertFalse(yExtensions.get(0).definesDataNode());
        assertFalse(yExtensions.get(0).definesSchemaNode());
        assertEquals("my-extension2", yExtensions.get(1).getExtensionName());

        final YArgument yArgument = yExtensions.get(0).getArgument();
        assertNotNull(yArgument);

        assertEquals(StatementArgumentType.NAME, yArgument.getArgumentType());
        assertEquals(CY.STMT_ARGUMENT, yArgument.getStatementModuleAndName());
        assertEquals("some-arg", yArgument.getArgumentName());
        assertFalse(yArgument.definesDataNode());
        assertFalse(yArgument.definesSchemaNode());

        final YYinElement yYinElement = yArgument.getYinElement();
        assertNotNull(yYinElement);

        assertEquals(StatementArgumentType.VALUE, yYinElement.getArgumentType());
        assertEquals(CY.STMT_YIN_ELEMENT, yYinElement.getStatementModuleAndName());
        assertFalse(yYinElement.definesDataNode());
        assertFalse(yYinElement.definesSchemaNode());

        final List<YAnydata> yAnydatas = yModule.getAnydata();
        assertEquals(2, yAnydatas.size());
        assertEquals("anydata1", yAnydatas.get(0).getName());
        assertEquals("anydata2", yAnydatas.get(1).getName());

        final List<YAnyxml> yAnyxmls = yModule.getAnyxmls();
        assertEquals(2, yAnyxmls.size());
        assertEquals("anyxml1", yAnyxmls.get(0).getName());
        assertEquals("anyxml2", yAnyxmls.get(1).getName());

        final List<YGrouping> yGrouping = yModule.getGroupings();
        assertEquals(2, yGrouping.size());
        assertEquals("grouping1", yGrouping.get(0).getGroupingName());
        assertEquals("grouping2", yGrouping.get(1).getGroupingName());

    }
}
