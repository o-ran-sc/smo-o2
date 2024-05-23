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
package org.oran.smo.teiv.pgsqlgenerator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String NO_PREFIX = "";
    public static final String CONSUMER_DATA = "CD_";
    public static final String SOURCE_IDS = "sourceIds";
    public static final String REL_ID = "REL_ID_";
    public static final String REL_FK = "REL_FK_";
    public static final String REL_CD = "REL_CD_";
    public static final String FOREIGN_KEY = "FK_";
    public static final String PRIMARY_KEY = "PK_";
    public static final String NOT_NULL = "NOT_NULL_";
    public static final String UNIQUE = "UNIQUE_";
    public static final String A_SIDE_PREFIX = "aSide_";
    public static final String B_SIDE_PREFIX = "bSide_";
    public static final String ID = "id";
    public static final String COLUMN = "COLUMN";
    public static final String TABLE = "TABLE";
    public static final String CONSTRAINT = "CONSTRAINT";
    public static final String VARCHAR511 = "VARCHAR(511)";
    public static final String TEXT = "TEXT";
    public static final String DECIMAL = "DECIMAL";
    public static final String BIGINT = "BIGINT";
    public static final String INT = "INTEGER";
    public static final String JSONB = "jsonb";
    public static final String CREATE = "CREATE";
    public static final String ALTER = "ALTER";
    public static final String DEFAULT = "DEFAULT";
    public static final String ALTER_TABLE_TIES_DATA_S = "ALTER TABLE ties_data.\"%s\" ";
    public static final String ALTER_TABLE_TIES_DATA_S_ADD_CONSTRAINT_S = ALTER_TABLE_TIES_DATA_S + "ADD CONSTRAINT \"%s\" ";
    public static final String CLASSIFIERS = "classifiers";
    public static final String DECORATORS = "decorators";
    public static final String DEFAULT_MODULE_STATUS = "IN_USAGE";
    public static final String BUILT_IN_MODULE_ID = "BUILT_IN_MODULE";
    public static final String A_SIDE = "A_SIDE";
    public static final String B_SIDE = "B_SIDE";
    public static final String RELATION = "RELATION";
    public static final String GEO_LOCATION = "geo-location";
    public static final String GEOGRAPHY = "geography";
}
