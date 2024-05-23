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
package org.oran.smo.yangtools.parser.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.model.schema.ModuleAndNamespaceResolver;
import org.oran.smo.yangtools.parser.util.InstanceIdentifier;
import org.oran.smo.yangtools.parser.util.InstanceIdentifier.Step;
import org.oran.smo.yangtools.parser.util.NamespaceModuleIdentifier;

public class InstanceIdentifierTest {

    @Test
    public void test___xml___ok() {

        final PrefixResolver prefixResolver = new PrefixResolver();
        prefixResolver.addMapping("ns1", "namespace1");
        prefixResolver.addMapping("ns2", "namespace2");
        prefixResolver.addMapping("ns3", "namespace3");

        final InstanceIdentifier ii0 = InstanceIdentifier.parseXmlEncodedString("", prefixResolver);
        assertTrue(ii0.getSteps().isEmpty());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii1 = InstanceIdentifier.parseXmlEncodedString(" \t", prefixResolver);
        assertTrue(ii1.getSteps().isEmpty());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii2 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1", prefixResolver);
        assertEquals(1, ii2.getSteps().size());

        assertEquals("node1", ii2.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii2.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii2.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii2.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii2.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii2.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii3 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1/ns2 : node2", prefixResolver);
        assertEquals(2, ii3.getSteps().size());

        assertEquals("node1", ii3.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii3.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii3.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii3.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii3.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii3.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        assertEquals("node2", ii3.getSteps().get(1).getDataNodeNsai().getIdentifier());
        assertEquals("namespace2", ii3.getSteps().get(1).getDataNodeNsai().getNamespace());
        assertNull(ii3.getSteps().get(1).getDataNodeNsai().getModuleName());
        assertNull(ii3.getSteps().get(1).getPredicateKeyValues());
        assertNull(ii3.getSteps().get(1).getPredicateLeafListMemberValue());
        assertNull(ii3.getSteps().get(1).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii4 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1/node2", prefixResolver);		// technically that's illegal syntax, but we will encounter it in the wild.
        assertEquals(2, ii4.getSteps().size());

        assertEquals("node1", ii4.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii4.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii4.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii4.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii4.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii4.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        assertEquals("node2", ii4.getSteps().get(1).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii4.getSteps().get(1).getDataNodeNsai().getNamespace());
        assertNull(ii4.getSteps().get(1).getDataNodeNsai().getModuleName());
        assertNull(ii4.getSteps().get(1).getPredicateKeyValues());
        assertNull(ii4.getSteps().get(1).getPredicateLeafListMemberValue());
        assertNull(ii4.getSteps().get(1).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii5 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[ns2:leaf = 'A.BC']",
                prefixResolver);
        assertEquals(1, ii5.getSteps().size());

        assertEquals("node1", ii5.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii5.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii5.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii5.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii5.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        final Map<NamespaceModuleIdentifier, String> ii5KeyValues0 = ii5.getSteps().get(0).getPredicateKeyValues();
        assertNotNull(ii5KeyValues0);
        assertEquals(1, ii5KeyValues0.size());

        assertTrue(ii5KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("A.BC", ii5KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii6 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[leaf=\"'AB.C'\"]",
                prefixResolver);		// technically that's illegal syntax, but we will encounter it in the wild.
        assertEquals(1, ii6.getSteps().size());

        assertEquals("node1", ii6.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii6.getSteps().get(0).getDataNodeNsai().getNamespace());

        final Map<NamespaceModuleIdentifier, String> ii6KeyValues0 = ii6.getSteps().get(0).getPredicateKeyValues();
        assertNotNull(ii6KeyValues0);
        assertEquals(1, ii6KeyValues0.size());

        assertTrue(ii6KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace1", null, "leaf")));
        assertEquals("'AB.C'", ii6KeyValues0.get(new NamespaceModuleIdentifier("namespace1", null, "leaf")));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii7 = InstanceIdentifier.parseXmlEncodedString(
                "/ns1:node1[ns2:leaf=\"ABC\"][ns3:other-leaf=''] ", prefixResolver);
        assertEquals(1, ii7.getSteps().size());

        assertEquals("node1", ii7.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii7.getSteps().get(0).getDataNodeNsai().getNamespace());

        final Map<NamespaceModuleIdentifier, String> ii7KeyValues0 = ii7.getSteps().get(0).getPredicateKeyValues();
        assertNotNull(ii7KeyValues0);
        assertEquals(2, ii7KeyValues0.size());

        assertTrue(ii7KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("ABC", ii7KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertTrue(ii7KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace3", null, "other-leaf")));
        assertEquals("", ii7KeyValues0.get(new NamespaceModuleIdentifier("namespace3", null, "other-leaf")));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii8 = InstanceIdentifier.parseXmlEncodedString(
                "/ns1:node1[ns2:leaf='ABC'] [other-leaf=XYZ]", prefixResolver);		// technically that's illegal syntax, but we will encounter it in the wild.
        assertEquals(1, ii8.getSteps().size());

        assertEquals("node1", ii8.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii8.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii8.getSteps().get(0).getDataNodeNsai().getModuleName());

        final Map<NamespaceModuleIdentifier, String> ii8KeyValues0 = ii8.getSteps().get(0).getPredicateKeyValues();
        assertNotNull(ii8KeyValues0);
        assertEquals(2, ii8KeyValues0.size());

        assertTrue(ii8KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("ABC", ii8KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertTrue(ii8KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace1", null, "other-leaf")));
        assertEquals("XYZ", ii8KeyValues0.get(new NamespaceModuleIdentifier("namespace1", null, "other-leaf")));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii9 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[4]", prefixResolver);
        assertEquals(1, ii9.getSteps().size());

        assertEquals("node1", ii9.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii9.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii9.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii9.getSteps().get(0).getPredicateLeafListMemberValue());

        assertNotNull(ii9.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());
        assertEquals(new Integer(4), ii9.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii10 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[.='A B C']", prefixResolver);
        assertEquals(1, ii10.getSteps().size());

        assertEquals("node1", ii10.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii10.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii10.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii10.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        assertNotNull(ii10.getSteps().get(0).getPredicateLeafListMemberValue());
        assertEquals("A B C", ii10.getSteps().get(0).getPredicateLeafListMemberValue());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii11 = InstanceIdentifier.parseXmlEncodedString(
                "/ns1:node1/ns2:node2[5]/ns3:node3[.='ABC']/ns1:node1[ns2:leaf2='XYZ']/ns3:node3[ns1:leaf1='EFG'][ns2:leaf2='RST']",
                prefixResolver);
        assertEquals(5, ii11.getSteps().size());

        assertEquals("node1", ii11.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii11.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii11.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii11.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());
        assertNull(ii11.getSteps().get(0).getPredicateLeafListMemberValue());

        assertEquals("node2", ii11.getSteps().get(1).getDataNodeNsai().getIdentifier());
        assertEquals("namespace2", ii11.getSteps().get(1).getDataNodeNsai().getNamespace());
        assertNull(ii11.getSteps().get(1).getPredicateKeyValues());
        assertNull(ii11.getSteps().get(1).getPredicateLeafListMemberValue());
        assertEquals(new Integer(5), ii11.getSteps().get(1).getPredicateListEntryOrLeafListMemberIndex());

        assertEquals("node3", ii11.getSteps().get(2).getDataNodeNsai().getIdentifier());
        assertEquals("namespace3", ii11.getSteps().get(2).getDataNodeNsai().getNamespace());
        assertNull(ii11.getSteps().get(2).getPredicateKeyValues());
        assertNull(ii11.getSteps().get(0).getPredicateLeafListMemberValue());
        assertEquals("ABC", ii11.getSteps().get(2).getPredicateLeafListMemberValue());

        assertEquals("node1", ii11.getSteps().get(3).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii11.getSteps().get(3).getDataNodeNsai().getNamespace());
        assertNull(ii11.getSteps().get(3).getPredicateListEntryOrLeafListMemberIndex());
        assertNull(ii11.getSteps().get(3).getPredicateLeafListMemberValue());
        assertEquals("XYZ", ii11.getSteps().get(3).getPredicateKeyValues().get(new NamespaceModuleIdentifier("namespace2",
                null, "leaf2")));

        assertEquals("node3", ii11.getSteps().get(4).getDataNodeNsai().getIdentifier());
        assertEquals("namespace3", ii11.getSteps().get(4).getDataNodeNsai().getNamespace());
        assertNull(ii11.getSteps().get(4).getPredicateListEntryOrLeafListMemberIndex());
        assertNull(ii11.getSteps().get(4).getPredicateLeafListMemberValue());
        assertEquals("EFG", ii11.getSteps().get(4).getPredicateKeyValues().get(new NamespaceModuleIdentifier("namespace1",
                null, "leaf1")));
        assertEquals("RST", ii11.getSteps().get(4).getPredicateKeyValues().get(new NamespaceModuleIdentifier("namespace2",
                null, "leaf2")));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii12 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[ns2:leaf='\\\"[]=./']",
                prefixResolver);
        assertEquals(1, ii12.getSteps().size());

        final Map<NamespaceModuleIdentifier, String> ii12KeyValues0 = ii12.getSteps().get(0).getPredicateKeyValues();
        assertTrue(ii12KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("\\\"[]=./", ii12KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
    }

    @Test
    public void test___xml___resolver___ok() {

        final PrefixResolver prefixResolver = new PrefixResolver();
        prefixResolver.addMapping("ns1", "namespace1");
        prefixResolver.addMapping("ns2", "namespace2");

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordNamespaceMapping("namespace1", "module1");
        namespaceResolver.recordNamespaceMapping("namespace2", "module2");
        namespaceResolver.recordModuleMapping("module1", "namespace1");
        namespaceResolver.recordModuleMapping("module2", "namespace2");

        // - - - - - - - - - - - -

        final InstanceIdentifier ii1 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1", prefixResolver);
        ii1.resolveModuleOrNamespace(namespaceResolver);

        assertEquals(1, ii1.getSteps().size());

        assertEquals("node1", ii1.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii1.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertEquals("module1", ii1.getSteps().get(0).getDataNodeNsai().getModuleName());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii2 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1[ns2:leaf='ABC']",
                prefixResolver);
        ii2.resolveModuleOrNamespace(namespaceResolver);

        assertEquals(1, ii2.getSteps().size());

        assertEquals("node1", ii2.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii2.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertEquals("module1", ii2.getSteps().get(0).getDataNodeNsai().getModuleName());

        final Map<NamespaceModuleIdentifier, String> ii2KeyValues0 = ii2.getSteps().get(0).getPredicateKeyValues();
        assertTrue(ii2KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("ABC", ii2KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertTrue(ii2KeyValues0.containsKey(new NamespaceModuleIdentifier(null, "module2", "leaf")));
        assertEquals("ABC", ii2KeyValues0.get(new NamespaceModuleIdentifier(null, "module2", "leaf")));
    }

    @Test
    public void test___json___ok() {

        final InstanceIdentifier ii1 = InstanceIdentifier.parseJsonEncodedString("");
        assertTrue(ii1.getSteps().isEmpty());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii2 = InstanceIdentifier.parseJsonEncodedString("/module1:node1");
        assertEquals(1, ii2.getSteps().size());

        assertEquals("node1", ii2.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("module1", ii2.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii2.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii2.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii2.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii2.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii3 = InstanceIdentifier.parseJsonEncodedString("/module1:node1/module2:node2");
        assertEquals(2, ii3.getSteps().size());

        assertEquals("node1", ii3.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("module1", ii3.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii3.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii3.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii3.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii3.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        assertEquals("node2", ii3.getSteps().get(1).getDataNodeNsai().getIdentifier());
        assertEquals("module2", ii3.getSteps().get(1).getDataNodeNsai().getModuleName());
        assertNull(ii3.getSteps().get(1).getDataNodeNsai().getNamespace());
        assertNull(ii3.getSteps().get(1).getPredicateKeyValues());
        assertNull(ii3.getSteps().get(1).getPredicateLeafListMemberValue());
        assertNull(ii3.getSteps().get(1).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii4 = InstanceIdentifier.parseJsonEncodedString("/module1:node1/node2");
        assertEquals(2, ii4.getSteps().size());

        assertEquals("node1", ii4.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("module1", ii4.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii4.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertNull(ii4.getSteps().get(0).getPredicateKeyValues());
        assertNull(ii4.getSteps().get(0).getPredicateLeafListMemberValue());
        assertNull(ii4.getSteps().get(0).getPredicateListEntryOrLeafListMemberIndex());

        assertEquals("node2", ii4.getSteps().get(1).getDataNodeNsai().getIdentifier());
        assertEquals("module1", ii4.getSteps().get(1).getDataNodeNsai().getModuleName());
        assertNull(ii4.getSteps().get(1).getDataNodeNsai().getNamespace());
        assertNull(ii4.getSteps().get(1).getPredicateKeyValues());
        assertNull(ii4.getSteps().get(1).getPredicateLeafListMemberValue());
        assertNull(ii4.getSteps().get(1).getPredicateListEntryOrLeafListMemberIndex());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii8 = InstanceIdentifier.parseJsonEncodedString(
                "/module1:node1[module2:leaf='ABC'][other-leaf=XYZ]");
        assertEquals(1, ii8.getSteps().size());

        assertEquals("node1", ii8.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("module1", ii8.getSteps().get(0).getDataNodeNsai().getModuleName());
        assertNull(ii8.getSteps().get(0).getDataNodeNsai().getNamespace());

        final Map<NamespaceModuleIdentifier, String> ii8KeyValues0 = ii8.getSteps().get(0).getPredicateKeyValues();
        assertNotNull(ii8KeyValues0);
        assertEquals(2, ii8KeyValues0.size());

        assertTrue(ii8KeyValues0.containsKey(new NamespaceModuleIdentifier(null, "module2", "leaf")));
        assertEquals("ABC", ii8KeyValues0.get(new NamespaceModuleIdentifier(null, "module2", "leaf")));
        assertTrue(ii8KeyValues0.containsKey(new NamespaceModuleIdentifier(null, "module1", "other-leaf")));
        assertEquals("XYZ", ii8KeyValues0.get(new NamespaceModuleIdentifier(null, "module1", "other-leaf")));
    }

    @Test
    public void test___json___resolver___ok() {

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordNamespaceMapping("namespace1", "module1");
        namespaceResolver.recordNamespaceMapping("namespace2", "module2");
        namespaceResolver.recordModuleMapping("module1", "namespace1");
        namespaceResolver.recordModuleMapping("module2", "namespace2");

        // - - - - - - - - - - - -

        final InstanceIdentifier ii1 = InstanceIdentifier.parseJsonEncodedString("/module1:node1");
        ii1.resolveModuleOrNamespace(namespaceResolver);

        assertEquals(1, ii1.getSteps().size());

        assertEquals("node1", ii1.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii1.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertEquals("module1", ii1.getSteps().get(0).getDataNodeNsai().getModuleName());

        // - - - - - - - - - - - -

        final InstanceIdentifier ii2 = InstanceIdentifier.parseJsonEncodedString("/module1:node1[module2:leaf='ABC']");
        ii2.resolveModuleOrNamespace(namespaceResolver);

        assertEquals(1, ii2.getSteps().size());

        assertEquals("node1", ii2.getSteps().get(0).getDataNodeNsai().getIdentifier());
        assertEquals("namespace1", ii2.getSteps().get(0).getDataNodeNsai().getNamespace());
        assertEquals("module1", ii2.getSteps().get(0).getDataNodeNsai().getModuleName());

        final Map<NamespaceModuleIdentifier, String> ii2KeyValues0 = ii2.getSteps().get(0).getPredicateKeyValues();
        assertTrue(ii2KeyValues0.containsKey(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertEquals("ABC", ii2KeyValues0.get(new NamespaceModuleIdentifier("namespace2", null, "leaf")));
        assertTrue(ii2KeyValues0.containsKey(new NamespaceModuleIdentifier(null, "module2", "leaf")));
        assertEquals("ABC", ii2KeyValues0.get(new NamespaceModuleIdentifier(null, "module2", "leaf")));
    }

    @Test
    public void test___errors___npe() {

        final PrefixResolver prefixResolver = new PrefixResolver();

        try {
            InstanceIdentifier.parseXmlEncodedString(null, prefixResolver);
            fail("Expected NullPointerException");
        } catch (final NullPointerException ex) {
        } catch (final Exception ex) {
            fail("Expected NullPointerException");
        }

        try {
            InstanceIdentifier.parseXmlEncodedString("", null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException ex) {
        } catch (final Exception ex) {
            fail("Expected NullPointerException");
        }

        try {
            InstanceIdentifier.parseXmlEncodedString(null, null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException ex) {
        } catch (final Exception ex) {
            fail("Expected NullPointerException");
        }

        try {
            InstanceIdentifier.parseJsonEncodedString(null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException ex) {
        } catch (final Exception ex) {
            fail("Expected NullPointerException");
        }
    }

    @Test
    public void test___xml___errors() {

        final PrefixResolver prefixResolver = new PrefixResolver();
        prefixResolver.addMapping("ns1", "namespace1");
        prefixResolver.addMapping("ns2", "namespace2");
        prefixResolver.addMapping("ns3", "namespace3");

        expectException("=", prefixResolver, "slash");
        expectException("leaf1", prefixResolver, "slash");
        expectException("ns1:leaf1", prefixResolver, "slash");
        expectException(".", prefixResolver, "slash");
        expectException("[]", prefixResolver, "slash");

        expectException("/", prefixResolver, "syntax");
        expectException("/ns1:", prefixResolver, "syntax");

        expectException("/=", prefixResolver, "data node");
        expectException("/ns1::", prefixResolver, "data node");
        expectException("/ns1:leaf1/:", prefixResolver, "data node");
        expectException("/ns1:leaf1/ns2==", prefixResolver, "data node");

        expectException("/leaf1", prefixResolver, "first step");

        expectException("/nsX:leaf1", prefixResolver, "prefix");

        expectException("/ns1:list1[a]", prefixResolver, "not parseable");
        expectException("/ns1:list1[-1]", prefixResolver, "larger/equal to 1");
        expectException("/ns1:list1[0]", prefixResolver, "larger/equal to 1");

        expectException("/ns1:list1[ns2:leaf2:'abc']", prefixResolver, "data node");
        expectException("/ns1:list1[ns2:leaf2=='abc']", prefixResolver, "single- or double-quoted string");
        expectException("/ns1:list1[ns2:leaf2='abc'[", prefixResolver, "closing square brace");

        expectException("/ns1:list1[ns2:leaf2=ab'c]", prefixResolver, "quote");
        expectException("/ns1:list1[ns2:leaf2=ab\"c]", prefixResolver, "quote");

        expectException("/ns1:list1[ns2:leaf2='abc", prefixResolver, "terminated");
        expectException("/ns1:list1[ns2:leaf2=\"abc", prefixResolver, "terminated");

        expectException("/ns1:list1[ns2:leaf2", prefixResolver, "syntax");
        expectException("/ns1:list1[ns2:leaf2=", prefixResolver, "syntax");
        expectException("/ns1:list1[ns2:leaf2=''", prefixResolver, "syntax");
    }

    private void expectException(final String xPath, final PrefixResolver prefixResolver, final String expectedString) {
        try {
            InstanceIdentifier.parseXmlEncodedString(xPath, prefixResolver);
            fail("Expected exception.");
        } catch (final Exception ex) {
            if (!ex.getMessage().contains(expectedString)) {
                fail("Expected exception to contain string '" + expectedString + "'; exception message is: " + ex
                        .getMessage());
            }
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test___instance_identifier___equals() {

        final PrefixResolver prefixResolver = new PrefixResolver();
        prefixResolver.addMapping("ns1", "namespace1");
        prefixResolver.addMapping("ns2", "namespace2");
        prefixResolver.addMapping("ns9", "namespace1");

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordNamespaceMapping("namespace1", "module1");
        namespaceResolver.recordNamespaceMapping("namespace2", "module2");
        namespaceResolver.recordModuleMapping("module1", "namespace1");
        namespaceResolver.recordModuleMapping("module2", "namespace2");

        // - - - - - - - - - - - -

        final InstanceIdentifier ii1 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1", prefixResolver);

        assertTrue(ii1.equals(ii1));
        assertFalse(ii1.equals(null));
        assertFalse(ii1.equals(""));

        assertTrue(ii1.equals(InstanceIdentifier.parseXmlEncodedString("/ns1:node1", prefixResolver)));
        assertTrue(ii1.equals(InstanceIdentifier.parseXmlEncodedString("/ns9:node1", prefixResolver)));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii2 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1/ns2:node2/node3",
                prefixResolver);

        assertTrue(ii2.equals(InstanceIdentifier.parseXmlEncodedString("/ns1:node1/ns2:node2/ns2:node3", prefixResolver)));
        assertTrue(ii2.equals(InstanceIdentifier.parseXmlEncodedString("/ns9:node1/ns2:node2/node3", prefixResolver)));

        // - - - - - - - - - - - -

        final InstanceIdentifier ii3 = InstanceIdentifier.parseXmlEncodedString("/ns1:node1/ns2:node2/node3",
                prefixResolver);
        assertFalse(ii3.equals(InstanceIdentifier.parseJsonEncodedString("/module1:node1/module2:node2/module2:node3")));
        ii3.resolveModuleOrNamespace(namespaceResolver);
        assertTrue(ii3.equals(InstanceIdentifier.parseJsonEncodedString("/module1:node1/module2:node2/module2:node3")));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test___step___equals() {

        final ModuleAndNamespaceResolver namespaceResolver = new ModuleAndNamespaceResolver();
        namespaceResolver.recordNamespaceMapping("namespace1", "module1");
        namespaceResolver.recordNamespaceMapping("namespace2", "module2");
        namespaceResolver.recordNamespaceMapping("namespace3", "module3");
        namespaceResolver.recordModuleMapping("module1", "namespace1");
        namespaceResolver.recordModuleMapping("module2", "namespace2");
        namespaceResolver.recordModuleMapping("module3", "namespace3");

        final NamespaceModuleIdentifier nsaiNs = new NamespaceModuleIdentifier("namespace1", null, "node1");

        final Step step1 = new Step(nsaiNs);

        assertTrue(step1.equals(step1));
        assertTrue(step1.equals(new Step(nsaiNs)));
        assertTrue(step1.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1"))));
        assertFalse(step1.equals(null));
        assertFalse(step1.equals(""));
        assertFalse(step1.equals(new Step(new NamespaceModuleIdentifier(null, "module1", "node1"))));

        step1.resolveModuleOrNamespace(namespaceResolver);
        assertTrue(step1.equals(new Step(new NamespaceModuleIdentifier(null, "module1", "node1"))));

        // - - - - - - - - - - -

        final NamespaceModuleIdentifier nsaiPred2 = new NamespaceModuleIdentifier("namespace2", null, "leaf2");

        final Step step2 = new Step(nsaiNs);
        step2.addPredicateKeyValue(nsaiPred2, "Hello");

        assertTrue(step2.equals(step2));
        assertTrue(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                nsaiPred2, "Hello")));
        assertTrue(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                new NamespaceModuleIdentifier("namespace2", null, "leaf2"), "Hello")));
        assertFalse(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1"))));
        assertFalse(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                nsaiPred2, "hello")));
        assertFalse(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                nsaiPred2, "XXX")));

        assertFalse(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                new NamespaceModuleIdentifier(null, "module2", "leaf2"), "Hello")));
        step2.resolveModuleOrNamespace(namespaceResolver);
        assertTrue(step2.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                new NamespaceModuleIdentifier(null, "module2", "leaf2"), "Hello")));

        // - - - - - - - - - - -

        final NamespaceModuleIdentifier nsaiPred3 = new NamespaceModuleIdentifier("namespace3", null, "leaf3");

        final Step step3 = new Step(nsaiNs);
        step3.addPredicateKeyValue(nsaiPred2, "Hello");
        step3.addPredicateKeyValue(nsaiPred3, "World");

        assertTrue(step3.equals(step3));
        assertTrue(step3.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                nsaiPred2, "Hello").addPredicateKeyValue(nsaiPred3, "World")));
        assertTrue(step3.equals(new Step(new NamespaceModuleIdentifier("namespace1", null, "node1")).addPredicateKeyValue(
                nsaiPred3, "World").addPredicateKeyValue(nsaiPred2, "Hello")));

        // - - - - - - - - - - -

        final Step step4 = new Step(nsaiNs);
        step4.setPredicateLeafListMemberValue("value1");

        assertTrue(step4.equals(step4));
        assertTrue(step4.equals(new Step(nsaiNs).setPredicateLeafListMemberValue("value1")));
        assertFalse(step4.equals(new Step(nsaiNs)));
        assertFalse(step4.equals(new Step(nsaiNs).setPredicateLeafListMemberValue("valueXxxx")));
        assertFalse(step4.equals(new Step(nsaiNs).addPredicateKeyValue(nsaiPred2, "Hello")));

        // - - - - - - - - - - -

        final Step step5 = new Step(nsaiNs);
        step5.setPredicateListEntryOrLeafListMemberIndex(1);

        assertTrue(step5.equals(step5));
        assertTrue(step5.equals(new Step(nsaiNs).setPredicateListEntryOrLeafListMemberIndex(1)));
        assertFalse(step5.equals(new Step(nsaiNs)));
        assertFalse(step5.equals(new Step(nsaiNs).setPredicateListEntryOrLeafListMemberIndex(2)));
        assertFalse(step5.equals(new Step(nsaiNs).addPredicateKeyValue(nsaiPred2, "Hello")));
        assertFalse(step5.equals(new Step(nsaiNs).setPredicateLeafListMemberValue("value1")));
    }

    @Test
    public void test___step___exceptions() {

        try {
            new Step(null);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        final NamespaceModuleIdentifier nsai = new NamespaceModuleIdentifier("namespace1", null, "node1");

        try {
            new Step(nsai).addPredicateKeyValue(null, "value1");
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).addPredicateKeyValue(nsai, null);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).addPredicateKeyValue(null, null);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateLeafListMemberValue(null);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateListEntryOrLeafListMemberIndex(0);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).addPredicateKeyValue(nsai, "value1").setPredicateLeafListMemberValue("value1");
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateLeafListMemberValue("value1").addPredicateKeyValue(nsai, "value1");
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateListEntryOrLeafListMemberIndex(10).addPredicateKeyValue(nsai, "value1");
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateListEntryOrLeafListMemberIndex(10).setPredicateLeafListMemberValue("value1");
            fail("Expected exception.");
        } catch (final Exception expected) {
        }

        try {
            new Step(nsai).setPredicateLeafListMemberValue("value1").setPredicateListEntryOrLeafListMemberIndex(10);
            fail("Expected exception.");
        } catch (final Exception expected) {
        }
    }
}
