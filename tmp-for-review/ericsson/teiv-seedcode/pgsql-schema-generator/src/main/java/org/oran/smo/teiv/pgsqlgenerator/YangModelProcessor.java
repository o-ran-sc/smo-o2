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

import static org.oran.smo.teiv.pgsqlgenerator.Constants.A_SIDE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.BIGINT;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.B_SIDE;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.DECIMAL;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.JSONB;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.RELATION;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.TEXT;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.VARCHAR511;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ModuleAndFindingTypeAndSchemaNodePathFilterPredicate;
import org.oran.smo.yangtools.parser.input.FileBasedYangInputResolver;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.ietf.IetfExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.oran.OranExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivASide;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivBSide;
import org.oran.smo.yangtools.parser.model.statements.oran.YOranSmoTeivBiDirectionalTopologyRelationship;
import org.oran.smo.yangtools.parser.model.statements.threegpp.ThreeGppExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class YangModelProcessor {
    private final HashMap<String, String> dataTypeMapping;
    private final YangDeviceModel yangDeviceModel;
    private final ModifyableFindingSeverityCalculator severityCalculator;
    private final FindingsManager findingsManager;
    private final ParserExecutionContext context;
    private final ThreeGppExtensionsClassSupplier threeGppStatementFactory;
    private final IetfExtensionsClassSupplier ietfStatementFactory;
    private final OranExtensionsClassSupplier oranStatementFactory;

    public YangModelProcessor() {
        dataTypeMapping = createDataTypeMapping();
        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "ietf*,iana*;*;*"));

        threeGppStatementFactory = new ThreeGppExtensionsClassSupplier();
        ietfStatementFactory = new IetfExtensionsClassSupplier();
        oranStatementFactory = new OranExtensionsClassSupplier();
        context = new ParserExecutionContext(findingsManager, Arrays.asList(threeGppStatementFactory, oranStatementFactory,
                ietfStatementFactory));
        context.setFailFast(false);
        context.setSuppressFindingsOnUnusedSchemaNodes(true);
    }

    private HashMap<String, String> createDataTypeMapping() {
        HashMap<String, String> map = new HashMap<String, String>() {
            {
                put("string", TEXT);
                put("uint32", BIGINT);
                put("or-teiv-types:_3GPP_FDN_Type", TEXT);
                put("enumeration", TEXT);
                put("types3gpp:PLMNId", JSONB);
                put("[]", JSONB);
                put(JSONB, JSONB);
                put("[uses types3gpp:PLMNId]", JSONB);
                put("geo:geo-location", "geography");
                put("uint64", BIGINT);
                put("decimal64", DECIMAL);
                put("[uses or-teiv-types:CM_ID]", JSONB);
            }
        };
        return map;
    }

    public List<Entity> getEntitiesAndAttributesFromYang(List<File> pathToImplementing) {
        List<Entity> entities = new ArrayList<>();

        File rootFolder = pathToImplementing.get(0);
        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(List.of(rootFolder));
        List<YangModel> yangModels = resolver.getResolvedYangInput().stream().map(yangInput -> new YangModel(yangInput,
                ConformanceType.IMPLEMENT)).toList();

        yangDeviceModel.parseIntoYangModels(context, yangModels);

        yangModels.stream().forEach(yangModel -> {
            YModule yModule = yangModel.getYangModelRoot().getModule();
            System.out.println("Module Name: " + yModule.getModuleName());

            yModule.getLists().stream().forEach(yList -> {
                System.out.printf("\tEntity Name: %s \n", yList.getListName());
                List<Attribute> attributes = new ArrayList<>();
                List constraint = List.of(PrimaryKeyConstraint.builder().constraintName("PK_" + yList.getListName() + "_id")
                        .tableName(yList.getListName()).columnToAddConstraintTo("id").build());

                attributes.add(Attribute.builder().name("id").dataType(VARCHAR511).constraints(constraint).build());
                yList.getContainers().forEach(yContainer -> {
                    System.out.printf("\t\tContainer Name: %s \n", yContainer.getContainerName());
                    if (yContainer.getContainerName().equals("attributes")) {

                        yContainer.getLeafs().forEach(yLeaf -> {

                            System.out.printf("\t\t\tLeaf Name: %s \n", yLeaf.getLeafName());
                            System.out.printf("\t\t\t\tLeaf Type: %s \n", yLeaf.getType().getDataType());
                            System.out.printf("\t\t\t\tData Type: %s \n", dataTypeMapping.get(yLeaf.getType()
                                    .getDataType()));

                            if (yLeaf.getDefault() != null) {

                                attributes.add(Attribute.builder().name(yLeaf.getLeafName()).dataType(dataTypeMapping.get(
                                        yLeaf.getType().getDataType())).defaultValue(yLeaf.getDefault().getValue())
                                        .constraints(new ArrayList()).build());
                            } else {
                                attributes.add(Attribute.builder().name(yLeaf.getLeafName()).dataType(dataTypeMapping.get(
                                        yLeaf.getType().getDataType())).constraints(new ArrayList()).build());
                            }
                        });
                        yContainer.getLeafLists().forEach(yLeafList -> {

                            System.out.printf("\t\t\tLeaf Name: %s \n", yLeafList.getLeafListName());
                            System.out.printf("\t\t\t\tLeaf Type: %s \n", yLeafList.getType().getDataType());
                            System.out.printf("\t\t\t\tData Type: %s \n", dataTypeMapping.get(yLeafList.getType()
                                    .getDataType()));

                            attributes.add(Attribute.builder().name(yLeafList.getLeafListName()).dataType(JSONB)
                                    .constraints(new ArrayList()).build());
                        });
                        yContainer.getContainers().forEach(container -> {

                            System.out.printf("\t\t\tContainer Name: %s \n", container.getContainerName());
                            System.out.printf("\t\t\t\tContainer Type: %s \n", container.getUses());
                            System.out.printf("\t\t\t\tData Type: %s \n", dataTypeMapping.get(container.getUses()
                                    .toString()));

                            attributes.add(Attribute.builder().name(container.getContainerName()).dataType(dataTypeMapping
                                    .get(container.getUses().toString())).constraints(new ArrayList()).build());
                        });
                        yContainer.getUses().forEach(uses -> {

                            System.out.printf("\t\t\tUses Name: %s \n", uses.getDomElement().getValue());

                            attributes.add(Attribute.builder().name(uses.getDomElement().getValue().substring(uses
                                    .getDomElement().getValue().indexOf(':') + 1, uses.getDomElement().getValue().length()))
                                    .dataType(dataTypeMapping.get(uses.getDomElement().getValue())).constraints(
                                            new ArrayList()).build());
                        });
                    }
                });
                entities.add(Entity.builder().entityName(yList.getListName()).moduleReferenceName(yangModel
                        .getYangModelRoot().getModule().getModuleName()).attributes(attributes).build());
            });
        });
        return entities;
    }

    public List<Relationship> getRelationshipsFromYang(List<File> pathToImplementing) {
        List<Relationship> relationships = new ArrayList<>();

        File rootFolder = pathToImplementing.get(0);
        final FileBasedYangInputResolver resolver = new FileBasedYangInputResolver(List.of(rootFolder));
        List<YangModel> yangModels = resolver.getResolvedYangInput().stream().map(yangInput -> new YangModel(yangInput,
                ConformanceType.IMPLEMENT)).toList();

        yangDeviceModel.parseIntoYangModels(context, yangModels);

        yangModels.stream().forEach(yangModel -> {
            YModule yModule = yangModel.getYangModelRoot().getModule();
            System.out.println("Module Name: " + yModule.getModuleName());

            StatementModuleAndName biDirectionalTopologyRelationship = new StatementModuleAndName(
                    "o-ran-smo-teiv-common-yang-extensions", "biDirectionalTopologyRelationship");
            StatementModuleAndName biDirectionalTopologyRelationshipAside = new StatementModuleAndName(
                    "o-ran-smo-teiv-common-yang-extensions", "aSide");
            StatementModuleAndName biDirectionalTopologyRelationshipBside = new StatementModuleAndName(
                    "o-ran-smo-teiv-common-yang-extensions", "bSide");

            yModule.getChildren(biDirectionalTopologyRelationship).stream().map(
                    abstractStatement -> (YOranSmoTeivBiDirectionalTopologyRelationship) abstractStatement).forEach(
                            yOranSmoTeivBiDirectionalTopologyRelationship -> {
                                System.out.printf("\tRelationship Name: %s \n",
                                        yOranSmoTeivBiDirectionalTopologyRelationship.getRelationshipName());
                                YOranSmoTeivASide aSide = yOranSmoTeivBiDirectionalTopologyRelationship.getChildStatements()
                                        .stream().filter(abstractStatement -> abstractStatement.getChild(
                                                biDirectionalTopologyRelationshipAside) != null).toList().get(0).getChild(
                                                        biDirectionalTopologyRelationshipAside);

                                YOranSmoTeivBSide bSide = yOranSmoTeivBiDirectionalTopologyRelationship.getChildStatements()
                                        .stream().filter(abstractStatement -> abstractStatement.getChild(
                                                biDirectionalTopologyRelationshipBside) != null).toList().get(0).getChild(
                                                        biDirectionalTopologyRelationshipBside);

                                System.out.printf("\t\tA Side:\n\t\t\t Name: %s \n\t\t\t Type: %s \n", aSide
                                        .getParentStatement().getStatementIdentifier(), aSide.getTeivTypeName());
                                System.out.printf("\t\tB Side:\n\t\t\t Name %s \n\t\t\t Type %s \n", bSide
                                        .getParentStatement().getStatementIdentifier(), bSide.getTeivTypeName());

                                long aSideMinCardinality = 0;
                                long aSideMaxCardinality = 0;
                                long bSideMinCardinality = 0;
                                long bSideMaxCardinality = 0;
                                Optional<YangDomElement> bSideMandatory = yOranSmoTeivBiDirectionalTopologyRelationship
                                        .getChildStatements().stream().filter(abstractStatement -> abstractStatement
                                                .getChild(biDirectionalTopologyRelationshipBside) != null).toList().get(0)
                                        .getDomElement().getChildren().stream().filter(name -> name.getNameValue().contains(
                                                "mandatory true")).findAny();
                                Optional<YangDomElement> aSideMandatory = yOranSmoTeivBiDirectionalTopologyRelationship
                                        .getChildStatements().stream().filter(abstractStatement -> abstractStatement
                                                .getChild(biDirectionalTopologyRelationshipAside) != null).toList().get(0)
                                        .getDomElement().getChildren().stream().filter(name -> name.getNameValue().contains(
                                                "mandatory true")).findAny();
                                Optional<YangDomElement> bSideMinElement = yOranSmoTeivBiDirectionalTopologyRelationship
                                        .getChildStatements().stream().filter(abstractStatement -> abstractStatement
                                                .getChild(biDirectionalTopologyRelationshipBside) != null).toList().get(0)
                                        .getDomElement().getChildren().stream().filter(name -> name.getNameValue().contains(
                                                "min-elements 1")).findAny();
                                Optional<YangDomElement> aSideMinElement = yOranSmoTeivBiDirectionalTopologyRelationship
                                        .getChildStatements().stream().filter(abstractStatement -> abstractStatement
                                                .getChild(biDirectionalTopologyRelationshipAside) != null).toList().get(0)
                                        .getDomElement().getChildren().stream().filter(name -> name.getNameValue().contains(
                                                "min-elements 1")).findAny();

                                if (aSide.getParentStatement().getDomElement().getName() == "leaf") {
                                    if (aSideMandatory.isPresent() || aSideMinElement.isPresent()) {
                                        bSideMinCardinality = 1;
                                    } else {
                                        bSideMinCardinality = 0;
                                    }
                                    bSideMaxCardinality = 1;

                                }
                                if (aSide.getParentStatement().getDomElement().getName() == "leaf-list") {
                                    if (aSideMandatory.isPresent() || aSideMinElement.isPresent()) {
                                        bSideMinCardinality = 1;
                                    } else {
                                        bSideMinCardinality = 0;
                                    }
                                    bSideMaxCardinality = Long.MAX_VALUE;

                                }
                                if (bSide.getParentStatement().getDomElement().getName() == "leaf") {
                                    if (bSideMandatory.isPresent() || bSideMinElement.isPresent()) {
                                        aSideMinCardinality = 1;
                                    } else {
                                        aSideMinCardinality = 0;
                                    }
                                    aSideMaxCardinality = 1;
                                }
                                if (bSide.getParentStatement().getDomElement().getName() == "leaf-list") {
                                    if (bSideMandatory.isPresent() || bSideMinElement.isPresent()) {
                                        aSideMinCardinality = 1;
                                    } else {
                                        aSideMinCardinality = 0;
                                    }
                                    aSideMaxCardinality = Long.MAX_VALUE;
                                }

                                String relDataLocation = getRelationshipDataLocation(aSideMaxCardinality,
                                        bSideMaxCardinality, aSide.getValue(), bSide.getValue(),
                                        yOranSmoTeivBiDirectionalTopologyRelationship.getRelationshipName());

                                String aSideMoType = aSide.getValue().substring(aSide.getValue().indexOf(':') + 1, aSide
                                        .getValue().length());
                                String bSideMoType = bSide.getValue().substring(bSide.getValue().indexOf(':') + 1, bSide
                                        .getValue().length());

                                boolean connectSameEntity;
                                if (aSideMoType.equals(bSideMoType)) {
                                    connectSameEntity = true;
                                } else {
                                    connectSameEntity = false;
                                }

                                Relationship relationship = Relationship.builder().name(
                                        yOranSmoTeivBiDirectionalTopologyRelationship.getRelationshipName())
                                        .aSideAssociationName(aSide.getParentStatement().getStatementIdentifier())
                                        .aSideMOType(aSideMoType).aSideMinCardinality(aSideMinCardinality)
                                        .aSideMaxCardinality(aSideMaxCardinality).bSideAssociationName(bSide
                                                .getParentStatement().getStatementIdentifier()).bSideMOType(bSideMoType)
                                        .bSideMinCardinality(bSideMinCardinality).bSideMaxCardinality(bSideMaxCardinality)
                                        .relationshipDataLocation(relDataLocation).moduleReferenceName(yModule
                                                .getModuleName()).associationKind(("BI_DIRECTIONAL"))   // Hard coded for now
                                        .connectSameEntity(connectSameEntity).build();     // Hard coded for now
                                relationships.add(relationship);
                            });
        });
        return relationships;
    }

    /**
     * Identify where relationship data should be stored
     */
    private String getRelationshipDataLocation(long aSideMaxCardinality, long bSideMaxCardinality, String aSideMO,
            String bSideMO, String relName) {
        if (aSideMO.equals(bSideMO) || (aSideMaxCardinality > 1 && bSideMaxCardinality > 1)) {
            return RELATION;
        } else if ((aSideMaxCardinality == 1 && bSideMaxCardinality == 1) || (aSideMaxCardinality > 1 && bSideMaxCardinality == 1)) {
            return A_SIDE;
        } else if (aSideMaxCardinality == 1 && bSideMaxCardinality > 1) {
            return B_SIDE;
        } else {
            throw PgSchemaGeneratorException.assignRelationshipDataLocation(relName, new UnsupportedOperationException());
        }
    }
}
