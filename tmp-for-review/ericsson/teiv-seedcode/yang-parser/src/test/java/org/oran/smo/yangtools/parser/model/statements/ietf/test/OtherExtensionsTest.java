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
package org.oran.smo.yangtools.parser.model.statements.ietf.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.model.statements.ietf.CIETF;
import org.oran.smo.yangtools.parser.model.statements.ietf.YIetfAnnotation;
import org.oran.smo.yangtools.parser.model.statements.ietf.YIetfDefaultDenyAll;
import org.oran.smo.yangtools.parser.model.statements.ietf.YIetfDefaultDenyWrite;
import org.oran.smo.yangtools.parser.model.statements.threegpp.C3GPP;
import org.oran.smo.yangtools.parser.model.statements.threegpp.Y3gppInVariant;
import org.oran.smo.yangtools.parser.model.statements.threegpp.Y3gppInitialValue;
import org.oran.smo.yangtools.parser.model.statements.threegpp.Y3gppNotNotifyable;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class OtherExtensionsTest extends YangTestCommon {

    private static final String IETF_YANG_METADATA = "ietf-yang-metadata";
    private static final String THREE_GPP_COMMON_YANG_EXTENSIONS = "_3gpp-common-yang-extensions";
    private static final String IETF_NETCONF_ACM = "ietf-netconf-acm";

    @Test
    public void test_3gpp_extensions() {

        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-statements-other/other-extension-test.yang", THREEGPP_YANG_EXT_PATH,
                YANG_METADATA_PATH, NETCONF_ACM_PATH, "src/test/resources/_orig-modules/ietf-inet-types-2019-11-04.yang",
                "src/test/resources/_orig-modules/ietf-yang-types-2019-11-04.yang"));

        assertNoFindings();

        final YModule module = getModule("other-extension-test");
        assertTrue(module != null);

        final YIetfAnnotation annotation = getExtensionChild(module, IETF_YANG_METADATA, "annotation");
        assertTrue(annotation != null);
        assertTrue(annotation.getAnnotationName().equals("last-modified"));
        assertTrue(getChild(annotation, "type") != null);
        assertTrue(annotation.getStatementModuleAndName().equals(CIETF.IETF_YANG_METADATA__ANNOTATION));

        final YContainer cont1 = getContainer(module, "cont1");

        final YIetfDefaultDenyWrite defaultDenyWrite = getExtensionChild(cont1, IETF_NETCONF_ACM, "default-deny-write");
        assertTrue(defaultDenyWrite != null);
        assertTrue(defaultDenyWrite.getStatementModuleAndName().equals(CIETF.IETF_NETCONF_ACM__DEFAULT_DENY_WRITE));

        final YLeaf leaf1 = getLeaf(cont1, "leaf1");

        final Y3gppInVariant inVariant = getExtensionChild(leaf1, THREE_GPP_COMMON_YANG_EXTENSIONS, C3GPP.IN_VARIANT);
        assertTrue(inVariant != null);
        assertTrue(inVariant.getStatementModuleAndName().equals(C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__IN_VARIANT));

        final Y3gppInitialValue threegppInitialValue = getExtensionChild(leaf1, THREE_GPP_COMMON_YANG_EXTENSIONS,
                C3GPP.INITIAL_VALUE);
        assertTrue(threegppInitialValue != null);
        assertTrue(threegppInitialValue.getInitialValue().equals("Hello"));
        assertTrue(threegppInitialValue.getStatementModuleAndName().equals(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__INITIAL_VALUE));

        final Y3gppNotNotifyable threegppNotNotifyable = getExtensionChild(leaf1, THREE_GPP_COMMON_YANG_EXTENSIONS,
                C3GPP.NOT_NOTIFYABLE);
        assertTrue(threegppNotNotifyable != null);
        assertNull(threegppNotNotifyable.getValue());
        assertTrue(threegppNotNotifyable.getStatementModuleAndName().equals(
                C3GPP.THREEGPP_COMMON_YANG_EXTENSIONS__NOT_NOTIFYABLE));

        final YIetfDefaultDenyAll defaultDenyAll = getExtensionChild(leaf1, IETF_NETCONF_ACM, "default-deny-all");
        assertTrue(defaultDenyAll != null);
        assertTrue(defaultDenyAll.getStatementModuleAndName().equals(CIETF.IETF_NETCONF_ACM__DEFAULT_DENY_ALL));
    }

    @Test
    public void test_CI_C3() {
        assertTrue(CIETF.HANDLED_STATEMENTS.size() == 3);
        assertTrue(CIETF.HANDLED_STATEMENTS.containsKey("ietf-yang-schema-mount"));
        assertTrue(CIETF.HANDLED_STATEMENTS.containsKey("ietf-yang-metadata"));
        assertTrue(CIETF.HANDLED_STATEMENTS.containsKey("ietf-netconf-acm"));

        assertTrue(C3GPP.HANDLED_STATEMENTS.size() == 1);
        assertTrue(C3GPP.HANDLED_STATEMENTS.containsKey("_3gpp-common-yang-extensions"));
    }
}
