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
package org.oran.smo.teiv.exposure.tiespath.refiner;

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.AndOrLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ContainerType;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.EmptyLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.FilterCriteria;

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.LogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeObject;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.TopologyObjectType;
import org.oran.smo.teiv.schema.EntityType;
import org.oran.smo.teiv.schema.RelationType;
import org.oran.smo.teiv.schema.SchemaRegistry;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.TargetObject;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import static org.oran.smo.teiv.utils.TiesConstants.ITEMS;

@UtilityClass
@Slf4j
public class BasePathRefinement {

    @SuppressWarnings("squid:S4144")
    public static void resolveWildCardObjectsInScopeAndTarget(FilterCriteria filterCriteria) {
        // change every * to the possible topology object
        // get the intersection of all possibilities to resolve the * (base set come from domain)
        // there are topology objects in TO or SO-s we only can work with them
        // there is no matching entity for the * throw error!
        // on multiple match connect with OR
        log.trace(filterCriteria.toString());
        throw new NotImplementedException(filterCriteria.toString());
    }

    /**
     * Resolve undefined topology object types to either relation or entity in scope and target.
     * In case there is no match or multiple match error is thrown.
     *
     * @param filterCriteria
     *     the filter criteria
     */
    public static void resolveUndefinedTopologyObjectTypes(FilterCriteria filterCriteria) {
        resolveUndefinedObjectTypesInTarget(filterCriteria);
        resolveUndefinedObjectTypesInScope(filterCriteria);
    }

    private static void resolveUndefinedObjectTypesInScope(FilterCriteria filterCriteria) {
        runOnTree(filterCriteria.getScope(), filterCriteria.getDomain(),
                BasePathRefinement::defineTopologyObjectTypeInScope);
    }

    private static void resolveUndefinedObjectTypesInTarget(FilterCriteria filterCriteria) {
        filterCriteria.getTargets().stream().filter(targetObject -> targetObject.getTopologyObjectType().equals(
                TopologyObjectType.UNDEFINED)).forEach(targetObject -> defineTopologyObjectTypeForTarget(targetObject,
                        filterCriteria.getDomain()));
    }

    private static void defineTopologyObjectTypeInScope(ScopeLogicalBlock scopeLogicalBlock, String domain) {
        if (scopeLogicalBlock.getScopeObject().getTopologyObjectType().equals(TopologyObjectType.UNDEFINED)) {
            boolean isResolved = false;
            if (SchemaRegistry.getEntityNamesByDomain(domain).contains(scopeLogicalBlock.getScopeObject()
                    .getTopologyObject())) {
                scopeLogicalBlock.getScopeObject().setTopologyObjectType(TopologyObjectType.ENTITY);
                isResolved = true;
            }
            if (SchemaRegistry.getRelationNamesByDomain(domain).contains(scopeLogicalBlock.getScopeObject()
                    .getTopologyObject())) {
                if (!isResolved) {
                    scopeLogicalBlock.getScopeObject().setTopologyObjectType(TopologyObjectType.RELATION);
                    isResolved = true;
                } else {
                    throw TiesPathException.ambiguousTopologyObject(scopeLogicalBlock.getScopeObject().getTopologyObject());
                }
            }
            if (!isResolved) {
                throw TiesPathException.invalidTopologyObject(scopeLogicalBlock.getScopeObject().getTopologyObject());
            }
        }
    }

    private static void defineTopologyObjectTypeForTarget(TargetObject targetObject, String domain) {
        boolean isResolved = false;
        if (SchemaRegistry.getEntityNamesByDomain(domain).contains(targetObject.getTopologyObject())) {
            targetObject.setTopologyObjectType(TopologyObjectType.ENTITY);
            isResolved = true;
        }
        if (SchemaRegistry.getRelationNamesByDomain(domain).contains(targetObject.getTopologyObject())) {
            if (!isResolved) {
                targetObject.setTopologyObjectType(TopologyObjectType.RELATION);
                isResolved = true;
            } else {
                throw TiesPathException.ambiguousTopologyObject(targetObject.getTopologyObject());
            }
        }
        if (!isResolved) {
            throw TiesPathException.invalidTopologyObject(targetObject.getTopologyObject());
        }
    }

    /**
     * Validates containers of targetObjects and scopeObjects through
     * checking whether the topologyObject with the selected containerType has the parameters that are listed in either
     * scopeObjects' leaf or targetObjects' params list.
     *
     * @param filterCriteria
     *     the filter criteria
     */
    public static void validateContainers(FilterCriteria filterCriteria) {
        filterCriteria.getTargets().stream().forEach(targetObject -> validateContainerWithMatchingParameters(targetObject
                .getContainer(), targetObject.getParams(), targetObject.getTopologyObject(), targetObject
                        .getTopologyObjectType(), Collections.emptyList()));
        runOnTree(filterCriteria.getScope(), filterCriteria.getDomain(), BasePathRefinement::validateScope);
    }

    private static void validateScope(LogicalBlock logicalBlock, String domain) {
        ScopeObject scopeObject = ((ScopeLogicalBlock) logicalBlock).getScopeObject();
        validateContainerWithMatchingParameters(scopeObject.getContainer(), new ArrayList<>(Arrays.asList(scopeObject
                .getLeaf())), scopeObject.getTopologyObject(), scopeObject.getTopologyObjectType(), scopeObject
                        .getInnerContainer());
    }

    private static void validateContainerWithMatchingParameters(ContainerType containerType, List<String> params,
            String topologyObject, TopologyObjectType topologyObjectType, List<String> innerContainer) {
        switch (containerType) {
            case ID:
                if (!params.isEmpty()) {
                    throw TiesPathException.grammarError("Adding parameters for id container is not supported");
                }
                break;
            case ATTRIBUTES:
                checkAttributesOfTopologyObject(params, topologyObject, topologyObjectType);
                break;
            case SOURCE_IDS:
                checkSourceIdTopologyObject(params, topologyObject);
                break;
            case ASSOCIATION:
                checkAssociationTopologyObject(params, topologyObject, topologyObjectType, innerContainer);
                break;
            default:
                break;
        }
    }

    private static void checkAssociationTopologyObject(List<String> params, String topologyObject,
            TopologyObjectType topologyObjectType, List<String> innerContainer) {
        if (innerContainer.isEmpty()) {
            throw TiesPathException.grammarError("Missing association name");
        }
        RelationType relation;
        switch (topologyObjectType) {
            case ENTITY:
                relation = SchemaRegistry.getRelationTypes().stream().filter(relationType -> (relationType.getASide()
                        .getName().equals(topologyObject) && relationType.getBSideAssociation().getName().equals(
                                innerContainer.get(0))) || (relationType.getBSide().getName().equals(
                                        topologyObject) && relationType.getASideAssociation().getName().equals(
                                                innerContainer.get(0)))).findFirst().orElseThrow(() -> TiesPathException
                                                        .invalidAssociation(topologyObject, innerContainer.get(0)));
                break;
            case RELATION:
                relation = SchemaRegistry.getRelationTypeByName(topologyObject);
                if (!relation.getASideAssociation().getName().equals(innerContainer.get(0)) && !relation
                        .getBSideAssociation().getName().equals(innerContainer.get(0))) {
                    throw TiesPathException.invalidAssociation(topologyObject, innerContainer.get(0));
                }

                break;
            default:
                throw TiesPathException.containerValidationWithUndefinedTopologyObjectType(topologyObject);
        }
        if (!params.isEmpty() && params.get(0) != null) {
            checkParamsForAssociation(relation, innerContainer.get(0), params);
        }
    }

    private static void checkParamsForAssociation(RelationType relationType, String associationName, List<String> params) {
        if ((relationType.getASideAssociation().getName().equals(associationName) && !params.stream().allMatch(
                param -> relationType.getASide().getFields().containsKey(param))) || (relationType.getBSideAssociation()
                        .getName().equals(associationName) && !params.stream().allMatch(param -> relationType.getBSide()
                                .getFields().containsKey(param)))) {
            throw TiesPathException.invalidParamsForAssociation(associationName);
        }
    }

    private static void checkSourceIdTopologyObject(List<String> params, String topologyObject) {
        if (params.stream().anyMatch(param -> !param.equals(ITEMS))) {
            throw TiesPathException.sourceIdNameError(topologyObject);
        }
    }

    private static void checkAttributesOfTopologyObject(List<String> params, String topologyObject,
            TopologyObjectType topologyObjectType) {
        switch (topologyObjectType) {
            case ENTITY:
                EntityType entityType = SchemaRegistry.getEntityTypeByName(topologyObject);
                List<String> notMatchingParams = params.stream().filter(a -> !entityType.getAttributeNames().contains(a))
                        .toList();
                if (!notMatchingParams.isEmpty()) {
                    throw TiesPathException.columnNamesError(topologyObject, notMatchingParams);
                }
                break;
            case RELATION:
                RelationType relationType = SchemaRegistry.getRelationTypeByName(topologyObject);
                List<String> notMatchingParams2 = params.stream().filter(a -> !relationType.getAttributes().containsKey(a))
                        .toList();
                if (!notMatchingParams2.isEmpty()) {
                    throw TiesPathException.columnNamesError(topologyObject, notMatchingParams2);
                }
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("squid:S4144")
    public static void validateScopeParametersDataType(FilterCriteria filterCriteria) {
        // can parameter parse as the given type, check given data type and stored dataType
        // throw error
        log.trace(filterCriteria.toString());
        throw new NotImplementedException(filterCriteria.toString());
    }

    //// End of syntax check

    /**
     * Check if target's topologyObjects match with scope's topologyObjects.
     *
     * @param filterCriteria
     *
     */
    @SuppressWarnings("squid:S4144")
    public static void checkIfTargetMatchesWithScope(FilterCriteria filterCriteria) {
        if (filterCriteria.getScope() instanceof EmptyLogicalBlock || filterCriteria.getTargets().isEmpty()) {
            return;
        }

        Set<String> scopeTopologyObjects = new HashSet<>();
        runOnTree(filterCriteria.getScope(), filterCriteria.getDomain(), (ScopeLogicalBlock lb,
                String domain) -> scopeTopologyObjects.add(lb.getScopeObject().getTopologyObject()));

        Set<String> targetTopologyObjects = filterCriteria.getTargets().stream().map(TargetObject::getTopologyObject)
                .collect(Collectors.toSet());

        if (targetTopologyObjects.size() == scopeTopologyObjects.size()) {
            scopeTopologyObjects.removeAll(targetTopologyObjects);
            if (!scopeTopologyObjects.isEmpty()) {
                throw TiesPathException.notMatchingScopeAndTargetFilter();
            }
        } else {
            throw TiesPathException.notMatchingScopeAndTargetFilter();
        }
    }

    @SuppressWarnings("squid:S4144")
    public static boolean validateQuery(FilterCriteria filterCriteria) {
        // remove invalid LB, when an AND block has an invalid LB set the AND block to invalid
        // when an or has an invalid block, replace the OR block with the other child
        // throw error when the last LB became invalid
        log.trace(filterCriteria.toString());
        throw new NotImplementedException(filterCriteria.toString());
    }

    public static void runOnTree(LogicalBlock logicalBlock, String domain, BiConsumer<ScopeLogicalBlock, String> func) {
        if (!logicalBlock.isValid() || logicalBlock instanceof EmptyLogicalBlock) {
            return;
        }
        if (logicalBlock instanceof AndOrLogicalBlock andOrLogicalBlock) {
            andOrLogicalBlock.getChildren().forEach(l -> runOnTree(l, domain, func));
            return;
        }
        func.accept((ScopeLogicalBlock) logicalBlock, domain);
    }

}
