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
package org.oran.smo.teiv.schema;

import java.util.Collections;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * Bidirectional Database name mapper
 */
@UtilityClass
public class BidiDbNameMapper {
    //Map<Name, DbName>
    private static Map<String, String> nameMap;
    //Map<DbName, Name>
    private static Map<String, String> dbNameMap;

    /**
     * Gets DB name used for the given name
     *
     * @param name
     * @return the mapped DB name
     */
    public static String getDbName(String name) {
        return nameMap.get(name);
    }

    /**
     * Gets the name for the given DB name
     *
     * @param dbName
     * @return the mapped name
     */
    public static String getModelledName(String dbName) {
        return dbNameMap.get(dbName);
    }

    /**
     * Loads the {@link BidiDbNameMapper} nameMap and dbNameMap
     */
    public static void initialize(Map<String, String> hashedNames, Map<String, String> reverseHashedNames) {
        BidiDbNameMapper.nameMap = Collections.unmodifiableMap(hashedNames);
        BidiDbNameMapper.dbNameMap = Collections.unmodifiableMap(reverseHashedNames);
    }
}
