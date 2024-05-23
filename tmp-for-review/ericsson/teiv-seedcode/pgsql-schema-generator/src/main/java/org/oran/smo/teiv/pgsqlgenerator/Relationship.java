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

import java.util.List;

import org.oran.smo.teiv.pgsqlgenerator.schema.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static org.oran.smo.teiv.pgsqlgenerator.Constants.CLASSIFIERS;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.DECORATORS;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.JSONB;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.SOURCE_IDS;

@Getter
@Builder
public class Relationship implements Table {
    private String name;
    private String aSideAssociationName;
    private String aSideMOType;
    private long aSideMinCardinality;
    private long aSideMaxCardinality;
    private String bSideAssociationName;
    private String bSideMOType;
    private long bSideMinCardinality;
    private long bSideMaxCardinality;
    private String associationKind;
    @Setter
    private String relationshipDataLocation;
    private boolean connectSameEntity;
    @Setter
    private String moduleReferenceName;
    @Builder.Default
    private List<ConsumerData> consumerData = List.of(ConsumerData.builder().name(SOURCE_IDS).dataType(JSONB).defaultValue(
            "[]").build(), ConsumerData.builder().name(CLASSIFIERS).dataType(JSONB).defaultValue("[]").build(), ConsumerData
                    .builder().name(DECORATORS).dataType(JSONB).defaultValue("{}").build());

    @Override
    public String getTableName() {
        return "relationship_info";
    }

    @Override
    public String getColumnsForCopyStatement() {
        return "(\"name\", \"aSideAssociationName\", \"aSideMOType\", \"aSideMinCardinality\", \"aSideMaxCardinality\", \"bSideAssociationName\", \"bSideMOType\", \"bSideMinCardinality\", \"bSideMaxCardinality\", \"associationKind\", \"relationshipDataLocation\", \"connectSameEntity\", \"moduleReferenceName\")";
    }

    @Override
    public String getRecordForCopyStatement() {
        return this.getName() + "\t" + this.getASideAssociationName() + "\t" + this.getASideMOType() + "\t" + this
                .getASideMinCardinality() + "\t" + this.getASideMaxCardinality() + "\t" + this
                        .getBSideAssociationName() + "\t" + this.getBSideMOType() + "\t" + this
                                .getBSideMinCardinality() + "\t" + this.getBSideMaxCardinality() + "\t" + this
                                        .getAssociationKind() + "\t" + this.getRelationshipDataLocation() + "\t" + this
                                                .isConnectSameEntity() + "\t" + this.getModuleReferenceName() + "\n";
    }
}
