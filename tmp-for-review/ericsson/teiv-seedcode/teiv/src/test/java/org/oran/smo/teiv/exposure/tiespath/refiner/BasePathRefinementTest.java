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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ContainerType;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.EmptyLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.FilterCriteria;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.LogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.OrLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.QueryFunction;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeLogicalBlock;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.ScopeObject;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.TargetObject;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.TopologyObjectType;
import org.oran.smo.teiv.schema.DataType;
import org.oran.smo.teiv.schema.MockSchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoader;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import org.oran.smo.teiv.schema.SchemaRegistry;

import org.junit.jupiter.api.BeforeAll;

import static org.oran.smo.teiv.utils.TiesConstants.ITEMS;

class BasePathRefinementTest {

    @BeforeAll
    static void setUp() throws SchemaLoaderException {
        SchemaLoader mockedSchemaLoader = new MockSchemaLoader();
        mockedSchemaLoader.loadSchemaRegistry();
    }

    @Test
    void testResolveWildCardObjectsInScopeAndTarget() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        Assertions.assertThrows(NotImplementedException.class, () -> BasePathRefinement
                .resolveWildCardObjectsInScopeAndTarget(filterCriteria));
    }

    @Test
    void testResolveUndefinedTopologyObjects_Entity() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject = TargetObject.builder("GNBDUFunction").build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject)));
        ScopeObject scopeObject = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBId", QueryFunction.EQ, "1",
                DataType.BIGINT);
        LogicalBlock scope = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scope);

        try (MockedStatic<SchemaRegistry> utilities = Mockito.mockStatic(SchemaRegistry.class)) {
            utilities.when(() -> SchemaRegistry.getEntityNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFunction", "NRCellDU"));
            utilities.when(() -> SchemaRegistry.getRelationNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFUNCTION_PROVIDES_NRCELLDU"));

            BasePathRefinement.resolveUndefinedTopologyObjectTypes(filterCriteria);

            Assertions.assertEquals(TopologyObjectType.ENTITY, filterCriteria.getTargets().get(0).getTopologyObjectType());
            Assertions.assertEquals(TopologyObjectType.ENTITY, ((ScopeLogicalBlock) filterCriteria.getScope())
                    .getScopeObject().getTopologyObjectType());

            filterCriteria.setScope(EmptyLogicalBlock.getInstance());
            targetObject.setTopologyObjectType(TopologyObjectType.UNDEFINED);

            BasePathRefinement.resolveUndefinedTopologyObjectTypes(filterCriteria);

            Assertions.assertEquals(TopologyObjectType.ENTITY, filterCriteria.getTargets().get(0).getTopologyObjectType());
        }
    }

    @Test
    void testResolveUndefinedTopologyObjects_Relation() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject = TargetObject.builder("GNBDUFUNCTION_PROVIDES_NRCELLDU").build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject)));
        ScopeObject scopeObject = new ScopeObject("GNBDUFUNCTION_PROVIDES_NRCELLDU", ContainerType.ID, QueryFunction.EQ,
                "1", DataType.PRIMITIVE);
        LogicalBlock scope = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scope);

        try (MockedStatic<SchemaRegistry> utilities = Mockito.mockStatic(SchemaRegistry.class)) {
            utilities.when(() -> SchemaRegistry.getEntityNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFunction", "NRCellDU"));
            utilities.when(() -> SchemaRegistry.getRelationNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFUNCTION_PROVIDES_NRCELLDU"));

            BasePathRefinement.resolveUndefinedTopologyObjectTypes(filterCriteria);

            Assertions.assertEquals(TopologyObjectType.RELATION, filterCriteria.getTargets().get(0)
                    .getTopologyObjectType());
            Assertions.assertEquals(TopologyObjectType.RELATION, ((ScopeLogicalBlock) filterCriteria.getScope())
                    .getScopeObject().getTopologyObjectType());
        }
    }

    @Test
    void testResolveUndefinedTopologyObjects_InvalidTargetAndScope() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject = TargetObject.builder("InvalidObject").build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject)));
        ScopeObject scopeObject = new ScopeObject("InvalidObject", ContainerType.ATTRIBUTES, "gNBId", QueryFunction.EQ, "1",
                DataType.BIGINT);
        LogicalBlock scope = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scope);

        try (MockedStatic<SchemaRegistry> utilities = Mockito.mockStatic(SchemaRegistry.class)) {
            utilities.when(() -> SchemaRegistry.getEntityNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFunction", "NRCellDU", "EntityAndRelation"));
            utilities.when(() -> SchemaRegistry.getRelationNamesByDomain("RAN_LOGICAL")).thenReturn(Arrays.asList(
                    "GNBDUFUNCTION_PROVIDES_NRCELLDU", "EntityAndRelation"));
            // Error thrown because of invalid topology object in targetFilter
            Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.resolveUndefinedTopologyObjectTypes(
                    filterCriteria));

            targetObject.setTopologyObject("GNBDUFunction");

            // Error thrown because of invalid topology object in scopeFilter
            Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.resolveUndefinedTopologyObjectTypes(
                    filterCriteria));

            scopeObject.setTopologyObject("EntityAndRelation");

            // Error thrown because of topology object type is ambiguous
            Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.resolveUndefinedTopologyObjectTypes(
                    filterCriteria));

            targetObject.setTopologyObjectType(TopologyObjectType.UNDEFINED);
            targetObject.setTopologyObject("EntityAndRelation");

            // Error thrown because of topology object type is ambiguous
            Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.resolveUndefinedTopologyObjectTypes(
                    filterCriteria));
        }
    }

    @Test
    void testValidateContainers() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");

        TargetObject targetObject0 = TargetObject.builder("GNBDUFunction").container(ContainerType.ID).params(
                new ArrayList<>(Arrays.asList("gNBId"))).build();
        targetObject0.setTopologyObjectType(TopologyObjectType.ENTITY);
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject0)));

        // Reason: container:ID, params is not empty
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        TargetObject targetObject1 = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES).params(
                new ArrayList<>(Arrays.asList("gNBId", "gNBIdLength", "notValidAttribute1", "notValidAttribute2"))).build();
        targetObject1.setTopologyObjectType(TopologyObjectType.ENTITY);
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject1)));

        ScopeObject scopeObject = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBId", QueryFunction.EQ, "1",
                DataType.BIGINT);
        scopeObject.setTopologyObjectType(TopologyObjectType.ENTITY);
        LogicalBlock scopeLogicalBlock = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scopeLogicalBlock);

        // Reason: invalid attributes in ENTITY type targetObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        TargetObject targetObject2 = TargetObject.builder("GNBDUFUNCTION_PROVIDES_NRCELLDU").container(
                ContainerType.ATTRIBUTES).topologyObjectType(TopologyObjectType.RELATION).params(new ArrayList<>(Arrays
                        .asList("notValidAttribute1", "notValidAttribute2"))).build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject2)));

        // Reason: invalid attributes in RELATION type targetObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setLeaf("notValidAttribute");

        TargetObject targetObject3 = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES)
                .topologyObjectType(TopologyObjectType.ENTITY).params(new ArrayList<>(Arrays.asList("gNBId",
                        "gNBIdLength"))).build();
        targetObject3.setTopologyObjectType(TopologyObjectType.ENTITY);
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject3)));

        // Reason: invalid attributes in ENTITY type scopeObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setLeaf("gNBId");

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));

        TargetObject targetObject4 = TargetObject.builder("GNBDUFunction").container(ContainerType.SOURCE_IDS)
                .topologyObjectType(TopologyObjectType.ENTITY).params(new ArrayList<>(Arrays.asList("gNBId"))).build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject4)));

        // Reason: invalid source id param for ENTITY type targetObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        targetObject4.setParams(Collections.emptyList());

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));

        TargetObject targetObject5 = TargetObject.builder("GNBDUFUNCTION_PROVIDES_NRCELLDU").container(
                ContainerType.SOURCE_IDS).topologyObjectType(TopologyObjectType.RELATION).params(new ArrayList<>(Arrays
                        .asList("InvalidSourceIdParam"))).build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject5)));

        // Reason: invalid source id param for RELATION type targetObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        targetObject5.setParams(new ArrayList<>(Arrays.asList(ITEMS)));
        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));
    }

    @Test
    void testValidateContainers_Associations() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        ScopeObject scopeObject = new ScopeObject("GNBDUFunction", ContainerType.ASSOCIATION, "nCI", QueryFunction.EQ, "1",
                DataType.BIGINT);
        scopeObject.setInnerContainer(Arrays.asList("provided-by-gnbduFunction"));
        scopeObject.setTopologyObjectType(TopologyObjectType.ENTITY);
        LogicalBlock scopeLogicalBlock = new ScopeLogicalBlock(scopeObject);
        filterCriteria.setScope(scopeLogicalBlock);

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setLeaf(null);

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setLeaf("invalid");

        // Reason: invalid leaf for scopeObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setLeaf("nCI");
        scopeObject.setInnerContainer(Arrays.asList("invalid-association"));

        // Reason: invalid association added in innerContainer list for ENTITY type scopeObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setInnerContainer(Collections.emptyList());

        // Reason: no association name added in innerContainer list for scopeObject in case of association containerType
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setTopologyObject("GNBDUFUNCTION_PROVIDES_NRCELLDU");
        scopeObject.setTopologyObjectType(TopologyObjectType.RELATION);
        scopeObject.setInnerContainer(Arrays.asList("provided-by-gnbduFunction"));

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setTopologyObjectType(TopologyObjectType.UNDEFINED);

        // Reason: cannot validate container for UNDEFINED type topologyObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

        scopeObject.setTopologyObjectType(TopologyObjectType.RELATION);
        scopeObject.setInnerContainer(Arrays.asList("invalid-association"));

        // Reason: invalid association added in innerContainer list for RELATION type scopeObject
        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.validateContainers(filterCriteria));

    }

    @Test
    void testValidateScopeParametersDataType() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        Assertions.assertThrows(NotImplementedException.class, () -> BasePathRefinement.validateScopeParametersDataType(
                filterCriteria));
    }

    @Test
    void testCheckIfTargetMatchesWithScope() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        TargetObject targetObject1 = TargetObject.builder("GNBDUFunction").container(ContainerType.ATTRIBUTES).params(List
                .of("gNBId", "gNBIdLength")).build();
        TargetObject targetObject2 = TargetObject.builder("NRCellDU").container(ContainerType.ATTRIBUTES).params(List.of(
                "nCI")).build();
        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject1, targetObject2)));

        OrLogicalBlock orLogicalBlock1 = new OrLogicalBlock();
        ScopeObject scopeObject1 = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBIdLength",
                QueryFunction.EQ, "1", DataType.BIGINT);
        ScopeObject scopeObject2 = new ScopeObject("GNBDUFunction", ContainerType.ATTRIBUTES, "gNBId", QueryFunction.EQ,
                "8", DataType.BIGINT);

        ScopeLogicalBlock scopeLogicalBlock1 = new ScopeLogicalBlock(scopeObject1);
        ScopeLogicalBlock scopeLogicalBlock2 = new ScopeLogicalBlock(scopeObject2);
        orLogicalBlock1.setChildren(new ArrayList<>(Arrays.asList(scopeLogicalBlock1, scopeLogicalBlock2)));
        filterCriteria.setScope(orLogicalBlock1);

        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.checkIfTargetMatchesWithScope(
                filterCriteria));

        filterCriteria.setTargets(new ArrayList<>(Arrays.asList(targetObject1)));

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.checkIfTargetMatchesWithScope(filterCriteria));

        ScopeObject scopeObject3 = new ScopeObject("NRSectorCarrier", ContainerType.ATTRIBUTES, "arfcnUL", QueryFunction.EQ,
                "8", DataType.BIGINT);
        ScopeLogicalBlock scopeLogicalBlock3 = new ScopeLogicalBlock(scopeObject3);
        filterCriteria.setScope(scopeLogicalBlock3);

        Assertions.assertThrows(TiesPathException.class, () -> BasePathRefinement.checkIfTargetMatchesWithScope(
                filterCriteria));

        filterCriteria.setTargets(Collections.emptyList());

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.checkIfTargetMatchesWithScope(filterCriteria));

        filterCriteria.setScope(EmptyLogicalBlock.getInstance());

        Assertions.assertDoesNotThrow(() -> BasePathRefinement.checkIfTargetMatchesWithScope(filterCriteria));

    }

    @Test
    void testValidateQuery() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        Assertions.assertThrows(NotImplementedException.class, () -> BasePathRefinement.validateQuery(filterCriteria));
    }

    @Test
    void testRunOnTree() {
        FilterCriteria filterCriteria = new FilterCriteria("RAN_LOGICAL");
        OrLogicalBlock orLogicalBlock = new OrLogicalBlock();
        filterCriteria.setScope(orLogicalBlock);
        OrLogicalBlock orLogicalBlockChild1 = new OrLogicalBlock();
        OrLogicalBlock orLogicalBlockChild2 = new OrLogicalBlock();

        LogicalBlock scopeLogicalBlock1 = new ScopeLogicalBlock(new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES,
                "gNBIdLength", QueryFunction.EQ, "1", DataType.BIGINT));
        LogicalBlock scopeLogicalBlock2 = new ScopeLogicalBlock(new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES,
                "gNBIdLength", QueryFunction.EQ, "2", DataType.BIGINT));
        LogicalBlock scopeLogicalBlock3 = new ScopeLogicalBlock(new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES,
                "gNBIdLength", QueryFunction.EQ, "3", DataType.BIGINT));
        LogicalBlock scopeLogicalBlock4 = new ScopeLogicalBlock(new ScopeObject("GNDBUFunction", ContainerType.ATTRIBUTES,
                "gNBIdLength", QueryFunction.EQ, "4", DataType.BIGINT));

        orLogicalBlockChild1.setChildren(Arrays.asList(scopeLogicalBlock1, scopeLogicalBlock2));
        orLogicalBlockChild2.setChildren(Arrays.asList(scopeLogicalBlock3, scopeLogicalBlock4));
        orLogicalBlock.setChildren(Arrays.asList(orLogicalBlockChild1, orLogicalBlockChild2));

        BasePathRefinement.runOnTree(orLogicalBlock, filterCriteria.getDomain(), (ScopeLogicalBlock lb, String domain) -> lb
                .getScopeObject().setParameter("0"));

        Assertions.assertEquals("0", ((ScopeLogicalBlock) scopeLogicalBlock1).getScopeObject().getParameter());
        Assertions.assertEquals("0", ((ScopeLogicalBlock) scopeLogicalBlock2).getScopeObject().getParameter());
        Assertions.assertEquals("0", ((ScopeLogicalBlock) scopeLogicalBlock3).getScopeObject().getParameter());
        Assertions.assertEquals("0", ((ScopeLogicalBlock) scopeLogicalBlock4).getScopeObject().getParameter());
    }
}
