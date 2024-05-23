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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.util.QNameHelper;

public class QNameHelperTest {

    @Test
    public void test_all_ok() {

        assertTrue(QNameHelper.hasPrefix("ns1:name1") == true);
        assertTrue(QNameHelper.hasPrefix(":name1") == false);
        assertTrue(QNameHelper.hasPrefix("name1") == false);
        assertTrue(QNameHelper.hasPrefix("") == false);
        assertTrue(QNameHelper.hasPrefix(":") == false);
        assertTrue(QNameHelper.hasPrefix(null) == false);

        assertTrue(QNameHelper.extractPrefix("ns1:name1").equals("ns1"));
        assertTrue(QNameHelper.extractPrefix(":name1").equals(PrefixResolver.NO_PREFIX));
        assertTrue(QNameHelper.extractPrefix("name1").equals(PrefixResolver.NO_PREFIX));
        assertTrue(QNameHelper.extractPrefix("").equals(PrefixResolver.NO_PREFIX));
        assertTrue(QNameHelper.extractPrefix(":").equals(PrefixResolver.NO_PREFIX));
        assertTrue(QNameHelper.extractPrefix(null).equals(PrefixResolver.NO_PREFIX));

        assertTrue(QNameHelper.extractName("ns1:name1").equals("name1"));
        assertTrue(QNameHelper.extractName(":name1").equals("name1"));
        assertTrue(QNameHelper.extractName("name1").equals("name1"));
        assertTrue(QNameHelper.extractName("").equals(""));
        assertTrue(QNameHelper.extractName(":").equals(""));
        assertTrue(QNameHelper.extractName(null) == null);
    }

}
