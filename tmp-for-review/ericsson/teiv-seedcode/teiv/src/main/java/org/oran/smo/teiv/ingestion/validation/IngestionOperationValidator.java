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
package org.oran.smo.teiv.ingestion.validation;

import static org.oran.smo.teiv.utils.TiesConstants.INFINITE_MAXIMUM_CARDINALITY;
import java.util.List;

import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.service.cloudevent.data.ParsedCloudEventData;
import org.oran.smo.teiv.service.cloudevent.data.Relationship;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestionOperationValidator {
    enum MAXIMUM_CARDINALITY_CASE {
        ONE_ONE,
        ONE_CONST,
        ONE_INFINITE,
        CONST_ONE,
        CONST_CONST,
        CONST_INFINITE,
        INFINITE_ONE,
        INFINITE_CONST,
        INFINITE_INFINITE
    }

    private final TiesDbServiceForValidation tiesDbServiceForValidation;

    public void validate(ParsedCloudEventData parsedCloudEventData) throws MaximumCardinalityViolationException {
        validateRelationshipMaximumCardinality(parsedCloudEventData.getRelationships());
    }

    private void validateRelationshipMaximumCardinality(List<Relationship> relationshipList)
            throws MaximumCardinalityViolationException {
        for (Relationship relationship : relationshipList) {
            RelationType relationType = SchemaRegistry.getRelationTypeByName(relationship.getType());
            long aSideMax = relationType.getASideAssociation().getMaxCardinality();
            long bSideMax = relationType.getBSideAssociation().getMaxCardinality();
            MAXIMUM_CARDINALITY_CASE cardinalityCase = determineMaxCardinalityCase(aSideMax, bSideMax);

            boolean isValid = false;
            switch (relationType.getRelationshipStorageLocation()) {
                case RELATION:
                    isValid = validateRelationshipInSeparateTable(cardinalityCase, relationType, relationship);
                    break;
                case A_SIDE:
                    isValid = validateRelationshipInASideTable(cardinalityCase, relationType, relationship);
                    break;
                case B_SIDE:
                    isValid = validateRelationshipInBSideTable(cardinalityCase, relationType, relationship);
                    break;
                default:
                    throw new UnsupportedOperationException(String.format(
                            "Unhandled relationship storage location case: %s. The relationship is invalid.",
                            cardinalityCase));
            }
            if (isValid) {
                log.debug("Relationship (id={}) is valid.", relationship.getId());
            } else {
                throw new MaximumCardinalityViolationException(String.format(
                        "Maximum cardinality validation failed for relationship (id=%s).", relationship.getId()));
            }
        }
    }

    static MAXIMUM_CARDINALITY_CASE determineMaxCardinalityCase(long aSideMax, long bSideMax) {
        validateMaxCardinalityArguments(aSideMax, bSideMax);
        if (aSideMax == 1 && bSideMax == 1) {
            return MAXIMUM_CARDINALITY_CASE.ONE_ONE;
        } else if (aSideMax == 1 && bSideMax < INFINITE_MAXIMUM_CARDINALITY) {
            return MAXIMUM_CARDINALITY_CASE.ONE_CONST;
        } else if (aSideMax < INFINITE_MAXIMUM_CARDINALITY && bSideMax == 1) {
            return MAXIMUM_CARDINALITY_CASE.CONST_ONE;
        } else if (aSideMax == 1 && bSideMax >= INFINITE_MAXIMUM_CARDINALITY) {
            return MAXIMUM_CARDINALITY_CASE.ONE_INFINITE;
        } else if (aSideMax >= INFINITE_MAXIMUM_CARDINALITY && bSideMax == 1) {
            return MAXIMUM_CARDINALITY_CASE.INFINITE_ONE;
        } else if (aSideMax < INFINITE_MAXIMUM_CARDINALITY && bSideMax < INFINITE_MAXIMUM_CARDINALITY) {
            return MAXIMUM_CARDINALITY_CASE.CONST_CONST;
        } else if (aSideMax < INFINITE_MAXIMUM_CARDINALITY) {
            return MAXIMUM_CARDINALITY_CASE.CONST_INFINITE;
        } else if (bSideMax < INFINITE_MAXIMUM_CARDINALITY) {
            return MAXIMUM_CARDINALITY_CASE.INFINITE_CONST;
        } else {
            return MAXIMUM_CARDINALITY_CASE.INFINITE_INFINITE;
        }
    }

    private static void validateMaxCardinalityArguments(long aSideMax, long bSideMax) {
        if (aSideMax <= 0 || bSideMax <= 0) {
            throw new IllegalArgumentException(String.format("Invalid maximum cardinalities: aSideMax=%s, bSideMax=%s",
                    aSideMax, bSideMax));
        }
    }

    private boolean validateRelationshipInSeparateTable(MAXIMUM_CARDINALITY_CASE cardinalityCase, RelationType relationType,
            Relationship relationship) {
        switch (cardinalityCase) {
            case INFINITE_INFINITE:
                return true;
            case ONE_ONE, ONE_CONST, CONST_ONE, CONST_CONST:
                return validateASideCardinality(relationType, relationship) && validateBSideCardinality(relationType,
                        relationship);
            case ONE_INFINITE, CONST_INFINITE:
                return validateASideCardinality(relationType, relationship);
            case INFINITE_ONE, INFINITE_CONST:
                return validateBSideCardinality(relationType, relationship);
            default:
                log.error("Unhandled relationship cardinality case: {}. The relationship is invalid.", cardinalityCase);
                return false;
        }
    }

    private boolean validateASideCardinality(RelationType relationType, Relationship relationship) {
        return executeValidationQuery(relationType.getTableName(), relationType.bSideColumnName(), relationship.getBSide(),
                relationType.getBSide().getTableName(), relationType.getASideAssociation().getMaxCardinality());
    }

    private boolean validateBSideCardinality(RelationType relationType, Relationship relationship) {
        return executeValidationQuery(relationType.getTableName(), relationType.aSideColumnName(), relationship.getASide(),
                relationType.getASide().getTableName(), relationType.getBSideAssociation().getMaxCardinality());
    }

    private boolean validateRelationshipInASideTable(MAXIMUM_CARDINALITY_CASE cardinalityCase, RelationType relationType,
            Relationship relationship) {
        switch (cardinalityCase) {
            case INFINITE_ONE:
                return true;
            case ONE_ONE, CONST_ONE:
                return validateASideCardinality(relationType, relationship);
            default:
                throw new UnsupportedOperationException(String.format("Can not store cardinalityCase=%s on the A side.",
                        cardinalityCase));
        }
    }

    private boolean validateRelationshipInBSideTable(MAXIMUM_CARDINALITY_CASE cardinalityCase, RelationType relationType,
            Relationship relationship) {
        switch (cardinalityCase) {
            case ONE_INFINITE:
                return true;
            case ONE_ONE, ONE_CONST:
                return validateBSideCardinality(relationType, relationship);
            default:
                throw new UnsupportedOperationException(String.format("Can not store cardinalityCase=%s on the B side.",
                        cardinalityCase));
        }
    }

    private boolean executeValidationQuery(String tableName, String foreignKeyColumnName, String foreignKeyValue,
            String tableReferencedFromForeignKeyColumn, long maxOccurrence) {
        tiesDbServiceForValidation.acquireEntityInstanceExclusiveLock(tableReferencedFromForeignKeyColumn, foreignKeyValue);
        return tiesDbServiceForValidation.executeValidationQuery(tableName, foreignKeyColumnName, foreignKeyValue,
                maxOccurrence);
    }

}
