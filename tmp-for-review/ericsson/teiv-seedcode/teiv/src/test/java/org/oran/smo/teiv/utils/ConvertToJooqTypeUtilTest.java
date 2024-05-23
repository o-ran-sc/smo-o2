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
package org.oran.smo.teiv.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jooq.JSONB;
import org.junit.Test;

import org.oran.smo.teiv.exception.InvalidFieldInYangDataException;
import org.oran.smo.teiv.utils.schema.Geography;

import static org.oran.smo.teiv.utils.ConvertToJooqTypeUtil.*;

public class ConvertToJooqTypeUtilTest {

    @Test
    public void testToJsonb() {
        assertEquals(JSONB.jsonb("{}"), toJsonb("{}"));
        assertEquals(JSONB.jsonb("{\"key\":\"value\"}"), toJsonb("{\"key\":\"value\"}"));
        assertEquals(JSONB.jsonb("[]"), toJsonb("[]"));
        assertEquals(JSONB.jsonb("[1]"), toJsonb("[1]"));
        assertEquals(JSONB.jsonb("[1,2,3]"), toJsonb("[1,2,3]"));
        assertEquals(JSONB.jsonb("[\"value1\",\"value2\"]"), toJsonb("[\"value1\",\"value2\"]"));
        assertEquals(JSONB.jsonb("[\"leading\",\"whitespaces\"]"), toJsonb("     [\"leading\",\"whitespaces\"]"));
        assertEquals(JSONB.jsonb("[\"a_string\"]"), toJsonb("a_string"));
        assertEquals(JSONB.jsonb("[\"23\"]"), toJsonb("23"));
        assertEquals(JSONB.jsonb("[54]"), toJsonb(54L));
        assertEquals(JSONB.jsonb("[92.13]"), toJsonb(92.13));
    }

    @Test
    public void testToGeography() throws InvalidFieldInYangDataException {
        assertEquals(new Geography(47.497913, 19.040236), toGeography(
                "{\"latitude\": 47.497913,\"longitude\": 19.040236}"));
        assertThrows(InvalidFieldInYangDataException.class, () -> toGeography("{invalidjson"));
        assertThrows(InvalidFieldInYangDataException.class, () -> toGeography(9));
    }
}
