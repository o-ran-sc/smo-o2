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
package org.oran.smo.teiv.exposure.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.MockSchemaLoader;

class RequestValidatorTest {

    private static RequestValidator requestValidator;

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        SchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
        requestValidator = new RequestValidator();
    }

    @Test
    void testValidateDomain() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateDomain("RAN"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateDomain("RAN_WRONG"));
    }

    @Test
    void testValidateEntityType() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateEntityType("GNBDUFunction"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateEntityType("InvalidEntity"));
    }

    @Test
    void testValidateEntityTypeInDomain() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateEntityTypeInDomain("GNBCUUPFunction", "RAN"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateEntityTypeInDomain(
                "GNBDU_FUNCTION", "EQUIPMENT"));
    }

    @Test
    void testValidateRelationshipType() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateRelationshipType("ANTENNAMODULE_INSTALLED_AT_SITE"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateRelationshipType(
                "ANTENNAMODULE_INSTALLED_ON_SITE"));
    }

    @Test
    void testValidateRelationshipTypeInDomain() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateRelationshipTypeInDomain(
                "ANTENNAMODULE_INSTALLED_AT_SITE", "EQUIPMENT"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateRelationshipTypeInDomain(
                "ANTENNAMODULE_INSTALLED_AT_SITE", "RAN"));
    }

    @Test
    void testValidateFiltersForRelationships() {
        Assertions.assertDoesNotThrow(() -> requestValidator.validateFiltersForRelationships(null, null));
        Assertions.assertDoesNotThrow(() -> requestValidator.validateFiltersForRelationships(null,
                "/GNBDUFunction/attributes[contains (@fdn, \"Hungary\")]"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateFiltersForRelationships(
                "/attributes", "/GNBDUFunction/attributes[contains (@fdn, \"Hungary\")]"));
        Assertions.assertThrowsExactly(TiesException.class, () -> requestValidator.validateFiltersForRelationships(null,
                "/attributes[contains (@fdn, \"Hungary\")]"));
    }

}
