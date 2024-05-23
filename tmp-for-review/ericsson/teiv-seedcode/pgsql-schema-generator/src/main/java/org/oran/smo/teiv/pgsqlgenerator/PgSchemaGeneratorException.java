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

import lombok.Getter;

@Getter
public class PgSchemaGeneratorException extends RuntimeException {
    private PgSchemaGeneratorException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public static PgSchemaGeneratorException extractYangDataException(Exception ex) {
        return new PgSchemaGeneratorException("Unable to load YAM!", ex);
    }

    public static PgSchemaGeneratorException prepareBaselineException(final String schemaName, Exception ex) { //
        return new PgSchemaGeneratorException(String.format("%s : Failed to copy skeleton schema file!", schemaName), ex);
    }

    public static PgSchemaGeneratorException readBaselineException(final String schemaName, Exception ex) {
        return new PgSchemaGeneratorException(String.format("%s : Failed to read baseline sql file!", schemaName), ex);
    }

    public static PgSchemaGeneratorException writeGeneratedSchemaException(final String schemaName, Exception ex) {
        return new PgSchemaGeneratorException(String.format("%s :  data writing failed!", schemaName), ex);
    }

    public static PgSchemaGeneratorException generateSHA1HashException(Exception ex) {
        return new PgSchemaGeneratorException("Error occurred while generating hash for hash_info table entry", ex);
    }

    public static PgSchemaGeneratorException extractMoTypeFromUrnException(final String urn, Exception ex) {
        return new PgSchemaGeneratorException(String.format("Unable to extract Managed object from urn - %s", urn), ex);
    }

    public static PgSchemaGeneratorException assignModuleRefException(final String relationshipName, Exception ex) {
        return new PgSchemaGeneratorException(String.format("ties.model : Unable to assign module reference to - %s",
                relationshipName), ex);
    }

    public static PgSchemaGeneratorException assignRelationshipDataLocation(final String relationshipName, Exception ex) {
        return new PgSchemaGeneratorException(String.format(
                "Unable to assign RelationshipDataLocation to - %s, unknown cardinalities", relationshipName), ex);
    }

    public static PgSchemaGeneratorException nbcChangeIdentifiedException(String errorMsg, Exception ex) {
        return new PgSchemaGeneratorException(String.format(
                "NBC change has been introduced: %s, please make sure you've enabled green-field installation!!%nFor more info please refer to README",
                errorMsg), ex);
    }

}
