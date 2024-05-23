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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.oran.smo.teiv.pgsqlgenerator.HashInfoEntity;
import org.oran.smo.teiv.pgsqlgenerator.PgSchemaGeneratorException;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Slf4j
public class HashInfoDataGenerator {

    private final Set<HashInfoEntity> hashInfoRowsList = new HashSet<>();

    /**
     * Generates a hash_info table entry for any table, column or constraint name passed and adds it to the hash_info data.
     *
     * @param prefix
     *     Any prefix if applicable for the entry
     * @param name
     *     Actual name of table, column or constraint generated from model service
     * @param type
     *     Type of entry table, column or constraint
     * @return Returns the generated name of table or column
     */
    public String generateHashAndRegisterTableRow(String prefix, String name, String type) {
        String hashedValue;
        if (prefix.length() + name.length() < 64) {
            hashInfoRowsList.add(HashInfoEntity.builder().name(prefix + name).hashedValue(prefix + name).type(type)
                    .build());
            return prefix + name;
        } else {
            hashedValue = generateSHA1Hash(prefix + name);
            hashInfoRowsList.add(HashInfoEntity.builder().name(prefix + name).hashedValue(prefix + hashedValue).type(type)
                    .build());
            return prefix + hashedValue;
        }
    }

    private String generateSHA1Hash(String input) {
        String hashedResult = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            hashedResult = DatatypeConverter.printHexBinary(digest).toUpperCase(Locale.US);
        } catch (NoSuchAlgorithmException exception) {
            throw PgSchemaGeneratorException.generateSHA1HashException(exception);
        }
        return hashedResult;
    }
}
