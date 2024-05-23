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
package org.oran.smo.teiv.pgsqlgenerator.schema.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.oran.smo.teiv.pgsqlgenerator.Constants;
import org.oran.smo.teiv.pgsqlgenerator.HashInfoEntity;

@SpringBootTest(classes = HashInfoDataGenerator.class)
@ActiveProfiles("test")
class HashInfoDataGeneratorTest {

    @Autowired
    private HashInfoDataGenerator hashInfoDataGenerator;

    // 64 character string
    final String longString = "looooooooooooooooooooooooooooooooooooooooooooooooooooooongString";
    final String hashValue = "4F29EB756894D5F3ADB9F615D1A00F1AE04A8C54";

    @Test
    void generateHashInfoRowTest() {
        // Given

        List<HashInfoEntity> expectedResult = List.of(HashInfoEntity.builder().name("Sector").hashedValue("Sector").type(
                Constants.TABLE).build(), HashInfoEntity.builder().name("azimuth").hashedValue("azimuth").type(
                        Constants.TABLE).build(), HashInfoEntity.builder().name(longString).hashedValue(hashValue).type(
                                Constants.COLUMN).build());

        //when
        populateHashInfoTable();

        //then
        List<HashInfoEntity> codeBookList = new ArrayList<>(hashInfoDataGenerator.getHashInfoRowsList());
        assertTrue(expectedResult.containsAll(codeBookList) && codeBookList.containsAll(expectedResult),
                "Expected and Actual code book entries differs");
    }

    private void populateHashInfoTable() {
        hashInfoDataGenerator.generateHashAndRegisterTableRow(Constants.NO_PREFIX, "Sector", Constants.TABLE);
        hashInfoDataGenerator.generateHashAndRegisterTableRow(Constants.NO_PREFIX, "azimuth", Constants.COLUMN);
        hashInfoDataGenerator.generateHashAndRegisterTableRow(Constants.NO_PREFIX, longString, Constants.COLUMN);
    }
}
