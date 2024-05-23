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
package org.oran.smo.yangtools.parser.data.util.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.oran.smo.yangtools.parser.data.util.BinaryValue;

public class BinaryValueTest {

    @Test
    public void test_binary_value_type() {

        assertTrue(new BinaryValue(new byte[0]).getBinaryValue().length == 0);
        assertTrue(new BinaryValue(new byte[10]).getBinaryValue().length == 10);

        assertTrue(new String(new BinaryValue("aGVsbG8=").getBinaryValue()).equals("hello"));

        try {
            new BinaryValue("!£$%^&");
            fail("Expected exception");
        } catch (Exception expected) {
        }
    }
}
