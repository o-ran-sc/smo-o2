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

import static org.oran.smo.teiv.schema.BidiDbNameMapper.getDbName;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.utils.TiesConstants.CONSUMER_DATA_PREFIX;
import static org.oran.smo.teiv.utils.TiesConstants.ID_COLUMN_NAME;
import static org.oran.smo.teiv.utils.TiesConstants.QUOTED_STRING;
import static org.oran.smo.teiv.utils.TiesConstants.REL_FK;
import static org.oran.smo.teiv.utils.TiesConstants.SOURCE_IDS;
import static org.oran.smo.teiv.utils.TiesConstants.TIES_DATA;
import static org.jooq.impl.DSL.field;

import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.JSONB;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RelationType implements Persistable {
    private static final String REL_ID_COL_PREFIX = "REL_ID_%s";
    private static final String REL_FK_COL_PREFIX = "REL_FK_%s";
    private static final String A_SIDE_PREFIX = "aSide_%s";
    private static final String B_SIDE_PREFIX = "bSide_%s";
    private static final String REL_SOURCE_IDS_COL_PREFIX = "REL_CD_sourceIds_%s";

    private String name;
    private Association aSideAssociation;
    private EntityType aSide;
    private Association bSideAssociation;
    private EntityType bSide;
    @Singular
    private Map<String, DataType> attributes;
    private boolean connectsSameEntity;
    private RelationshipDataLocation relationshipStorageLocation;
    private Module module;

    @Override
    public String getTableName() {
        return switch (relationshipStorageLocation) {
            case RELATION -> String.format(TIES_DATA, getDbName(name));
            case A_SIDE -> aSide.getTableName();
            case B_SIDE -> bSide.getTableName();
        };
    }

    @Override
    public String getIdColumnName() {
        if (relationshipStorageLocation.equals(RELATION)) {
            return ID_COLUMN_NAME;
        } else {
            return getDbName(String.format(REL_ID_COL_PREFIX, name));
        }
    }

    @Override
    public List<String> getAttributeColumnsWithId() {
        // attributes are yet to be supported for relations
        return List.of();
    }

    @Override
    public List<Field> getAllFieldsWithId() {
        return List.of(field(getTableName() + "." + String.format(QUOTED_STRING, aSideColumnName())), field(
                getTableName() + "." + String.format(QUOTED_STRING, bSideColumnName())), field(getTableName() + "." + String
                        .format(QUOTED_STRING, getIdColumnName())), field(getTableName() + "." + String.format(
                                QUOTED_STRING, getSourceIdsColumnName()), JSONB.class));
    }

    /**
     * Gets id, aSide and bSide column name of the relation.
     *
     * @return the list of {@link Field}
     */
    public List<Field> getBaseFieldsWithId() {
        return List.of(field(getTableName() + "." + String.format(QUOTED_STRING, aSideColumnName())), field(
                getTableName() + "." + String.format(QUOTED_STRING, bSideColumnName())), field(getTableName() + "." + String
                        .format(QUOTED_STRING, getIdColumnName())));
    }

    @Override
    public String getSourceIdsColumnName() {
        if (relationshipStorageLocation.equals(RELATION)) {
            return getDbName(CONSUMER_DATA_PREFIX + SOURCE_IDS);
        } else {
            return getDbName(String.format(REL_SOURCE_IDS_COL_PREFIX, name));
        }
    }

    /**
     * Gets the aSide column name of the relation.
     *
     * @return the aSide column name
     */
    public String aSideColumnName() {
        return switch (relationshipStorageLocation) {
            case RELATION -> getDbName(String.format(A_SIDE_PREFIX, aSide.getName()));
            case A_SIDE -> ID_COLUMN_NAME;
            case B_SIDE -> getDbName(String.format(REL_FK_COL_PREFIX, getBSideAssociation().getName()));
        };
    }

    /**
     * Gets the bSide column name of the relation.
     *
     * @return the bSide column name
     */
    public String bSideColumnName() {
        return switch (relationshipStorageLocation) {
            case RELATION -> getDbName(String.format(B_SIDE_PREFIX, bSide.getName()));
            case A_SIDE -> getDbName(String.format(REL_FK_COL_PREFIX, getASideAssociation().getName()));
            case B_SIDE -> ID_COLUMN_NAME;
        };
    }

    /**
     * Gets the fully qualified name of the entity.
     * Format - <moduleNameReference>:<relationName>
     *
     * @return the fully qualified name
     */
    public String getFullyQualifiedName() {
        return String.format("%s:%s", this.getModule().getName(), this.getName());
    }

    public String getTableNameWithoutSchema() {
        return switch (relationshipStorageLocation) {
            case RELATION -> name;
            case A_SIDE -> aSide.getName();
            case B_SIDE -> bSide.getName();
        };
    }

    /**
     * Gets the bSide relationship foreign key column name for the entity.
     *
     * @return the bSide foreign key column name, or null if not found.
     */
    public String getReferenceColumnOnBSide() {
        return getDbName(REL_FK + this.getBSideAssociation().getName());
    }

    public String getNotStoringSideTableName() {
        return switch (relationshipStorageLocation) {
            case RELATION -> null;
            case A_SIDE -> bSide.getTableName();
            case B_SIDE -> aSide.getTableName();
        };
    }

    public String getNotStoringSideEntityIdColumnNameInStoringSideTable() {
        return switch (relationshipStorageLocation) {
            case RELATION -> null;
            case A_SIDE -> this.bSideColumnName();
            case B_SIDE -> this.aSideColumnName();
        };
    }

    public String getStoringSideEntityType() {
        return switch (relationshipStorageLocation) {
            case RELATION -> null;
            case A_SIDE -> aSide.getName();
            case B_SIDE -> bSide.getName();
        };
    }

    public String getNotStoringSideEntityType() {
        return switch (relationshipStorageLocation) {
            case RELATION -> null;
            case A_SIDE -> bSide.getName();
            case B_SIDE -> aSide.getName();
        };
    }

}
