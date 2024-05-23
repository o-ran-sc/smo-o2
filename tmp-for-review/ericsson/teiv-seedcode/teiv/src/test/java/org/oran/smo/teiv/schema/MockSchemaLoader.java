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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oran.smo.teiv.schema.DataType.BIGINT;
import static org.oran.smo.teiv.schema.DataType.CONTAINER;
import static org.oran.smo.teiv.schema.DataType.DECIMAL;
import static org.oran.smo.teiv.schema.DataType.GEOGRAPHIC;
import static org.oran.smo.teiv.schema.DataType.PRIMITIVE;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.RELATION;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.A_SIDE;
import static org.oran.smo.teiv.schema.RelationshipDataLocation.B_SIDE;

public class MockSchemaLoader extends SchemaLoader {

    @Override
    protected void loadBidiDbNameMapper() {

        Map<String, String> hashedNames = new HashMap<>();
        hashedNames.put("GNBDUFunction", "GNBDUFunction");
        hashedNames.put("GNBCUUPFunction", "GNBCUUPFunction");
        hashedNames.put("NRCellDU", "NRCellDU");
        hashedNames.put("NRSectorCarrier", "NRSectorCarrier");
        hashedNames.put("CloudNativeApplication", "CloudNativeApplication");
        hashedNames.put("AntennaCapability", "AntennaCapability");
        hashedNames.put("Sector", "Sector");
        hashedNames.put("AntennaModule", "AntennaModule");

        hashedNames.put("ANTENNAMODULE_INSTALLED_AT_SITE", "ANTENNAMODULE_INSTALLED_AT_SITE");
        hashedNames.put("GNBDUFUNCTION_PROVIDES_NRCELLDU", "GNBDUFUNCTION_PROVIDES_NRCELLDU");
        hashedNames.put("GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", "GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");
        hashedNames.put("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION",
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        hashedNames.put("NRSECTORCARRIER_USES_ANTENNACAPABILITY", "NRSECTORCARRIER_USES_ANTENNACAPABILITY");
        hashedNames.put("SECTOR_GROUPS_ANTENNAMODULE", "SECTOR_GROUPS_ANTENNAMODULE");
        hashedNames.put("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");

        hashedNames.put("id", "id");
        hashedNames.put("fdn", "fdn");
        hashedNames.put("cmId", "cmId");
        hashedNames.put("CD_sourceIds", "CD_sourceIds");

        // GNBDUFUNCTION
        hashedNames.put("dUpLMNId", "dUpLMNId");
        hashedNames.put("gNBIdLength", "gNBIdLength");
        hashedNames.put("gNBId", "gNBId");
        hashedNames.put("gNBDUId", "gNBDUId");
        hashedNames.put("REL_FK_managed-by-managedElement", "REL_FK_managed-by-managedElement");
        hashedNames.put("REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION", "REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");

        //NRCELLDU
        hashedNames.put("nCI", "nCI");
        hashedNames.put("cellLocalId", "cellLocalId");
        hashedNames.put("nRPCI", "nRPCI");
        hashedNames.put("nRTAC", "nRTAC");
        hashedNames.put("REL_FK_grouped-by-sector", "REL_FK_grouped-by-sector");
        hashedNames.put("REL_ID_SECTOR_GROUPS_NRCELLDU", "REL_ID_SECTOR_GROUPS_NRCELLDU");
        hashedNames.put("REL_FK_provided-by-gnbduFunction", "REL_FK_provided-by-gnbduFunction");
        hashedNames.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU", "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU");
        hashedNames.put("REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU",
                "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU");

        //NrSectorCarrier
        hashedNames.put("frequencyDL", "frequencyDL");
        hashedNames.put("frequencyUL", "frequencyUL");
        hashedNames.put("arfcnUL", "arfcnUL");
        hashedNames.put("essScLocalId", "essScLocalId");
        hashedNames.put("arfcnDL", "arfcnDL");
        hashedNames.put("REL_FK_used-by-nrCellDu", "REL_FK_used-by-nrCellDu");
        hashedNames.put("REL_ID_NRCELLDU_USES_NRSECTORCARRIER", "REL_ID_NRCELLDU_USES_NRSECTORCARRIER");
        hashedNames.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", "REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");
        hashedNames.put("REL_FK_used-antennaCapability", "REL_FK_used-antennaCapability");
        hashedNames.put("REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY", "REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY");

        //CloudNativeApplication
        hashedNames.put("name", "name");
        hashedNames.put("REL_FK_realised-managedElement", "REL_FK_realised-managedElement");
        hashedNames.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION",
                "REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION");
        hashedNames.put("REL_FK_comprised-by-cloudNativeSystem", "REL_FK_comprised-by-cloudNativeSystem");
        hashedNames.put("REL_ID_CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION",
                "REL_ID_CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION");
        hashedNames.put("REL_FK_deployed-on-namespace", "REL_FK_deployed-on-namespace");
        hashedNames.put("REL_ID_CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE",
                "REL_ID_CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE");

        //AntennaCapability
        hashedNames.put("nRFqBands", "nRFqBands");
        hashedNames.put("eUtranFqBands", "eUtranFqBands");
        hashedNames.put("geranFqBands", "geranFqBands");
        hashedNames.put("REL_FK_used-by-lteSectorCarrier", "REL_FK_used-by-lteSectorCarrier");

        //Sector
        hashedNames.put("sectorId", "sectorId");
        hashedNames.put("azimuth", "azimuth");

        //AntennaModule
        hashedNames.put("positionWithinSector", "positionWithinSector");
        hashedNames.put("antennaModelNumber", "antennaModelNumber");
        hashedNames.put("electricalAntennaTilt", "electricalAntennaTilt");
        hashedNames.put("mechanicalAntennaTilt", "mechanicalAntennaTilt");
        hashedNames.put("totalTilt", "totalTilt");
        hashedNames.put("mechanicalAntennaBearing", "mechanicalAntennaBearing");
        hashedNames.put("REL_ID_SECTOR_GROUPS_ANTENNAMODULE", "REL_ID_SECTOR_GROUPS_ANTENNAMODULE");
        hashedNames.put("REL_FK_installed-at-site", "REL_FK_installed-at-site");
        hashedNames.put("REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE", "REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE");

        //GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION
        hashedNames.put("aSide_GNBDUFunction", "aSide_GNBDUFunction");
        hashedNames.put("bSide_CloudNativeApplication", "bSide_CloudNativeApplication");

        //ANTENNAMODULE_REALISED_BY_ANTENNAMODULE
        hashedNames.put("aSide_AntennaModule", "aSide_AntennaModule");
        hashedNames.put("bSide_AntennaModule", "bSide_AntennaModule");

        Map<String, String> reverseHashedNames = new HashMap<>();
        reverseHashedNames.put("GNBDUFunction", "GNBDUFunction");
        reverseHashedNames.put("GNBCUUPFunction", "GNBCUUPFunction");
        reverseHashedNames.put("NRCellDU", "NRCellDU");
        reverseHashedNames.put("NRSectorCarrier", "NRSectorCarrier");
        reverseHashedNames.put("CloudNativeApplication", "CloudNativeApplication");
        reverseHashedNames.put("AntennaCapability", "AntennaCapability");
        reverseHashedNames.put("Sector", "Sector");
        reverseHashedNames.put("AntennaModule", "AntennaModule");

        reverseHashedNames.put("ANTENNAMODULE_INSTALLED_AT_SITE", "ANTENNAMODULE_INSTALLED_AT_SITE");
        reverseHashedNames.put("GNBDUFUNCTION_PROVIDES_NRCELLDU", "GNBDUFUNCTION_PROVIDES_NRCELLDU");
        reverseHashedNames.put("GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", "GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");
        reverseHashedNames.put("GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION",
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION");
        reverseHashedNames.put("NRSECTORCARRIER_USES_ANTENNACAPABILITY", "NRSECTORCARRIER_USES_ANTENNACAPABILITY");
        reverseHashedNames.put("SECTOR_GROUPS_ANTENNAMODULE", "SECTOR_GROUPS_ANTENNAMODULE");
        reverseHashedNames.put("ANTENNAMODULE_REALISED_BY_ANTENNAMODULE", "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE");

        reverseHashedNames.put("id", "id");
        reverseHashedNames.put("fdn", "fdn");
        reverseHashedNames.put("cmId", "cmId");
        reverseHashedNames.put("CD_sourceIds", "CD_sourceIds");

        // GNBDUFUNCTION
        reverseHashedNames.put("dUpLMNId", "dUpLMNId");
        reverseHashedNames.put("gNBIdLength", "gNBIdLength");
        reverseHashedNames.put("gNBId", "gNBId");
        reverseHashedNames.put("gNBDUId", "gNBDUId");
        reverseHashedNames.put("REL_FK_managed-by-managedElement", "REL_FK_managed-by-managedElement");
        reverseHashedNames.put("REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION",
                "REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");

        //NRCELLDU
        reverseHashedNames.put("nCI", "nCI");
        reverseHashedNames.put("cellLocalId", "cellLocalId");
        reverseHashedNames.put("nRPCI", "nRPCI");
        reverseHashedNames.put("nRTAC", "nRTAC");
        reverseHashedNames.put("REL_FK_grouped-by-sector", "REL_FK_grouped-by-sector");
        reverseHashedNames.put("REL_ID_SECTOR_GROUPS_NRCELLDU", "REL_ID_SECTOR_GROUPS_NRCELLDU");
        reverseHashedNames.put("REL_FK_provided-by-gnbduFunction", "REL_FK_provided-by-gnbduFunction");
        reverseHashedNames.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU", "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU");
        reverseHashedNames.put("REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU",
                "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU");

        //NrSectorCarrier
        reverseHashedNames.put("frequencyDL", "frequencyDL");
        reverseHashedNames.put("frequencyUL", "frequencyUL");
        reverseHashedNames.put("arfcnUL", "arfcnUL");
        reverseHashedNames.put("essScLocalId", "essScLocalId");
        reverseHashedNames.put("arfcnDL", "arfcnDL");
        reverseHashedNames.put("REL_FK_used-by-nrCellDu", "REL_FK_used-by-nrCellDu");
        reverseHashedNames.put("REL_ID_NRCELLDU_USES_NRSECTORCARRIER", "REL_ID_NRCELLDU_USES_NRSECTORCARRIER");
        reverseHashedNames.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER",
                "REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");
        reverseHashedNames.put("REL_FK_used-antennaCapability", "REL_FK_used-antennaCapability");
        reverseHashedNames.put("REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY",
                "REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY");

        //CloudNativeApplication
        reverseHashedNames.put("name", "name");
        reverseHashedNames.put("REL_FK_realised-managedElement", "REL_FK_realised-managedElement");
        reverseHashedNames.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION",
                "REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION");
        reverseHashedNames.put("REL_FK_comprised-by-cloudNativeSystem", "REL_FK_comprised-by-cloudNativeSystem");
        reverseHashedNames.put("REL_ID_CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION",
                "REL_ID_CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION");
        reverseHashedNames.put("REL_FK_deployed-on-namespace", "REL_FK_deployed-on-namespace");
        reverseHashedNames.put("REL_ID_CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE",
                "REL_ID_CLOUDNATIVEAPPLICATION_DEPLOYED_ON_NAMESPACE");

        //AntennaCapability
        reverseHashedNames.put("nRFqBands", "nRFqBands");
        reverseHashedNames.put("eUtranFqBands", "eUtranFqBands");
        reverseHashedNames.put("geranFqBands", "geranFqBands");
        reverseHashedNames.put("REL_FK_used-by-lteSectorCarrier", "REL_FK_used-by-lteSectorCarrier");

        //Sector
        reverseHashedNames.put("sectorId", "sectorId");
        reverseHashedNames.put("azimuth", "azimuth");

        //AntennaModule
        reverseHashedNames.put("positionWithinSector", "positionWithinSector");
        reverseHashedNames.put("antennaModelNumber", "antennaModelNumber");
        reverseHashedNames.put("electricalAntennaTilt", "electricalAntennaTilt");
        reverseHashedNames.put("mechanicalAntennaTilt", "mechanicalAntennaTilt");
        reverseHashedNames.put("totalTilt", "totalTilt");
        reverseHashedNames.put("mechanicalAntennaBearing", "mechanicalAntennaBearing");
        reverseHashedNames.put("REL_ID_SECTOR_GROUPS_ANTENNAMODULE", "REL_ID_SECTOR_GROUPS_ANTENNAMODULE");
        reverseHashedNames.put("REL_FK_installed-at-site", "REL_FK_installed-at-site");
        reverseHashedNames.put("REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE", "REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE");

        //GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION
        reverseHashedNames.put("aSide_GNBDUFunction", "aSide_GNBDUFunction");
        reverseHashedNames.put("bSide_CloudNativeApplication", "bSide_CloudNativeApplication");

        //ANTENNAMODULE_REALISED_BY_ANTENNAMODULE
        reverseHashedNames.put("aSide_AntennaModule", "aSide_AntennaModule");
        reverseHashedNames.put("bSide_AntennaModule", "bSide_AntennaModule");

        BidiDbNameMapper.initialize(hashedNames, reverseHashedNames);
    }

    @Override
    protected void loadModules() {
        Module ranLogicalModule = Module.builder().name("o-ran-smo-teiv-ran").namespace("urn:o-ran:smo-teiv-ran").domain(
                "RAN").build();

        Module ranEquipmentModule = Module.builder().name("o-ran-smo-teiv-equipment").namespace(
                "urn:o-ran:smo-teiv-equipment").domain("EQUIPMENT").build();

        Module ranCloudModule = Module.builder().name("o-ran-smo-teiv-cloud").namespace("urn:o-ran:smo-teiv-cloud").domain(
                "CLOUD").build();

        Module ranLogicalToEquipmentModule = Module.builder().name("o-ran-smo-teiv-equipment-to-ran").namespace(
                "urn:o-ran:smo-teiv-equipment-to-ran").domain("EQUIPMENT_TO_RAN").includedModuleName("o-ran-smo-teiv-ran")
                .includedModuleName("o-ran-smo-teiv-equipment").build();

        Module ranLogicalToCloudModule = Module.builder().name("o-ran-smo-teiv-cloud-to-ran").namespace(
                "urn:o-ran:smo-teiv-cloud-to-ran").domain("CLOUD_TO_RAN").includedModuleName("o-ran-smo-teiv-ran")
                .includedModuleName("o-ran-smo-teiv-cloud").build();

        Module ranOamModule = Module.builder().name("o-ran-smo-teiv-oam").namespace("urn:o-ran:smo-teiv-oam").domain("OAM")
                .build();

        Module ranOamToLogicalModule = Module.builder().name("o-ran-smo-teiv-oam-to-ran").namespace(
                "urn:o-ran:smo-teiv-oam-to-ran").domain("OAM_TO_RAN").includedModuleName("o-ran-smo-teiv-oam")
                .includedModuleName("o-ran-smo-teiv-ran").build();

        Module ranOamToCloudModule = Module.builder().name("o-ran-smo-teiv-oam-to-cloud").namespace(
                "urn:o-ran:smo-teiv-oam-to-cloud").domain("OAM_TO_CLOUD").includedModuleName("o-ran-smo-teiv-oam")
                .includedModuleName("o-ran-smo-teiv-cloud").build();

        List<Module> modules = List.of(ranLogicalModule, ranEquipmentModule, ranCloudModule, ranLogicalToEquipmentModule,
                ranLogicalToCloudModule, ranOamModule, ranOamToLogicalModule, ranOamToCloudModule);
        Map<String, Module> moduleMap = new HashMap<>();
        modules.forEach(module -> moduleMap.put(module.getName(), module));
        SchemaRegistry.initializeModules(moduleMap);
    }

    @Override
    protected void loadEntityTypes() {
        EntityType gnbduFunction = EntityType.builder().name("GNBDUFunction").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-ran")).fields(getGnbduFunctionFields()).build();
        EntityType gnbcuupFunction = EntityType.builder().name("GNBCUUPFunction").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-ran")).fields(getGnbcuupFunctionFields()).build();

        EntityType nrCellDU = EntityType.builder().name("NRCellDU").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-ran")).fields(getNrCellDuFields()).build();

        EntityType nrSectorCarrier = EntityType.builder().name("NRSectorCarrier").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-ran")).fields(getNrSectorCarrierFields()).build();

        EntityType gNBCUCPFunction = EntityType.builder().name("GNBCUCPFunction").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-cloud-to-ran")).fields(getGNBCUCPFunctionFields()).build();

        EntityType managedElement = EntityType.builder().name("ManagedElement").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-oam")).fields(getManagedElementFields()).build();

        EntityType cloudNativeApplication = EntityType.builder().name("CloudNativeApplication").module(SchemaRegistry
                .getModuleByName("o-ran-smo-teiv-cloud")).fields(getCloudNativeApplicationFields()).build();

        EntityType antennaCapability = EntityType.builder().name("AntennaCapability").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-ran")).fields(getAntennaCapabilityFields()).build();

        EntityType sector = EntityType.builder().name("Sector").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-equipment-to-ran")).fields(getSectorFields()).build();

        EntityType antennaModule = EntityType.builder().name("AntennaModule").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-equipment")).fields(getAntennaModuleFields()).build();

        EntityType site = EntityType.builder().name("Site").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-equipment")).fields(getSiteFields()).build();

        EntityType cloudSite = EntityType.builder().name("CloudSite").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-cloud")).fields(getCloudSiteFields()).build();

        EntityType nodeCluster = EntityType.builder().name("NodeCluster").module(SchemaRegistry.getModuleByName(
                "o-ran-smo-teiv-cloud")).fields(getNodeClusterFields()).build();

        List<EntityType> entityTypes = List.of(gnbduFunction, gnbcuupFunction, nrCellDU, nrSectorCarrier, gNBCUCPFunction,
                managedElement, cloudNativeApplication, antennaCapability, sector, antennaModule, site, cloudSite,
                nodeCluster);
        Map<String, EntityType> entityTypeMap = new HashMap<>();
        entityTypes.forEach(entityType -> entityTypeMap.put(entityType.getName(), entityType));
        SchemaRegistry.initializeEntityTypes(entityTypeMap);
    }

    @Override
    protected void loadRelationTypes() {

        RelationType antennaModuleInstalledAtSite = RelationType.builder().name("ANTENNAMODULE_INSTALLED_AT_SITE")
                .aSideAssociation(getRelationshipAssociation("installed-at-site", 0, 9223372036854775807L)).aSide(
                        SchemaRegistry.getEntityTypeByName("AntennaModule")).bSideAssociation(getRelationshipAssociation(
                                "installed-antennaModule", 0, 1)).bSide(SchemaRegistry.getEntityTypeByName("Site"))
                .connectsSameEntity(false).relationshipStorageLocation(A_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-equipment")).build();

        RelationType gnbduFunctionProvidesNrcelldu = RelationType.builder().name("GNBDUFUNCTION_PROVIDES_NRCELLDU")
                .aSideAssociation(getRelationshipAssociation("provided-nrCellDu", 1, 1)).aSide(SchemaRegistry
                        .getEntityTypeByName("GNBDUFunction")).bSideAssociation(getRelationshipAssociation(
                                "provided-by-gnbduFunction", 0, 9223372036854775807L)).bSide(SchemaRegistry
                                        .getEntityTypeByName("NRCellDU")).connectsSameEntity(false)
                .relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName("o-ran-smo-teiv-ran")).build();

        RelationType gnbduFunctionProvidesNrsectorcarrier = RelationType.builder().name(
                "GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER").aSideAssociation(getRelationshipAssociation(
                        "provided-nrSectorCarrier", 1, 1)).aSide(SchemaRegistry.getEntityTypeByName("GNBDUFunction"))
                .bSideAssociation(getRelationshipAssociation("provided-by-gnbduFunction", 0, 9223372036854775807L)).bSide(
                        SchemaRegistry.getEntityTypeByName("NRSectorCarrier")).connectsSameEntity(false)
                .relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName("o-ran-smo-teiv-ran")).build();

        RelationType gnbduFunctionRealisedByCloudnativeapplication = RelationType.builder().name(
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION").aSideAssociation(getRelationshipAssociation(
                        "realised-by-cloudNativeApplication", 0, 9223372036854775807L)).aSide(SchemaRegistry
                                .getEntityTypeByName("GNBDUFunction")).bSideAssociation(getRelationshipAssociation(
                                        "realised-gnbduFunction", 0, 9223372036854775807L)).bSide(SchemaRegistry
                                                .getEntityTypeByName("CloudNativeApplication")).connectsSameEntity(false)
                .relationshipStorageLocation(RELATION).module(SchemaRegistry.getModuleByName("o-ran-smo-teiv-cloud-to-ran"))
                .build();

        RelationType nrSectorcarrierUsesAntennacapability = RelationType.builder().name(
                "NRSECTORCARRIER_USES_ANTENNACAPABILITY").aSideAssociation(getRelationshipAssociation(
                        "used-antennaCapability", 0, 9223372036854775807L)).aSide(SchemaRegistry.getEntityTypeByName(
                                "NRSectorCarrier")).bSideAssociation(getRelationshipAssociation("used-by-nrSectorCarrier",
                                        0, 1)).bSide(SchemaRegistry.getEntityTypeByName("AntennaCapability"))
                .connectsSameEntity(false).relationshipStorageLocation(A_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-ran")).build();

        RelationType sectorGroupsAntennamodule = RelationType.builder().name("SECTOR_GROUPS_ANTENNAMODULE")
                .aSideAssociation(getRelationshipAssociation("grouped-antennaModule", 0, 1)).aSide(SchemaRegistry
                        .getEntityTypeByName("Sector")).bSideAssociation(getRelationshipAssociation("grouped-by-sector", 0,
                                9223372036854775807L)).bSide(SchemaRegistry.getEntityTypeByName("AntennaModule"))
                .connectsSameEntity(false).relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-equipment-to-ran")).build();

        RelationType sectorGroupsNrcelldu = RelationType.builder().name("SECTOR_GROUPS_NRCELLDU").aSideAssociation(
                getRelationshipAssociation("grouped-nrCellDu", 0, 1)).aSide(SchemaRegistry.getEntityTypeByName("Sector"))
                .bSideAssociation(getRelationshipAssociation("grouped-by-sector", 0, 9223372036854775807L)).bSide(
                        SchemaRegistry.getEntityTypeByName("NRCellDU")).connectsSameEntity(false)
                .relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-equipment-to-ran")).build();

        RelationType nrCellDuUsesNrSectorcarrier = RelationType.builder().name("NRCELLDU_USES_NRSECTORCARRIER")
                .aSideAssociation(getRelationshipAssociation("used-nrSectorCarrier", 0, 1)).aSide(SchemaRegistry
                        .getEntityTypeByName("NRCellDU")).bSideAssociation(getRelationshipAssociation("used-by-nrCellDu", 0,
                                9223372036854775807L)).bSide(SchemaRegistry.getEntityTypeByName("NRSectorCarrier"))
                .connectsSameEntity(false).relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-ran")).build();

        RelationType managedElementManagesGNBCUCPFunction = RelationType.builder().name(
                "MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION").aSideAssociation(getRelationshipAssociation(
                        "managed-gnbcucpFunction", 1, 1)).aSide(SchemaRegistry.getEntityTypeByName("ManagedElement"))
                .bSideAssociation(getRelationshipAssociation("managed-by-managedElement", 0, 9223372036854775807L)).bSide(
                        SchemaRegistry.getEntityTypeByName("GNBCUCPFunction")).connectsSameEntity(false)
                .relationshipStorageLocation(B_SIDE).module(SchemaRegistry.getModuleByName("o-ran-smo-teiv-oam-to-ran"))
                .build();

        RelationType nodeClusterLocatedAtCloudSite = RelationType.builder().name("NODECLUSTER_LOCATED_AT_CLOUDSITE")
                .aSideAssociation(getRelationshipAssociation("located-at-cloudSite", 0, 9223372036854775807L)).aSide(
                        SchemaRegistry.getEntityTypeByName("NodeCluster")).bSideAssociation(getRelationshipAssociation(
                                "location-of-nodeCluster", 1, 1)).bSide(SchemaRegistry.getEntityTypeByName("CloudSite"))
                .connectsSameEntity(false).relationshipStorageLocation(A_SIDE).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-cloud")).build();

        RelationType antennaModuleRealisedByAntennaModule = RelationType.builder().name(
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE").aSideAssociation(getRelationshipAssociation(
                        "realised-by-antennaModule", 0, 9223372036854775807L)).aSide(SchemaRegistry.getEntityTypeByName(
                                "AntennaModule")).bSideAssociation(getRelationshipAssociation("realised-antennaModule", 0,
                                        9223372036854775807L)).bSide(SchemaRegistry.getEntityTypeByName("AntennaModule"))
                .connectsSameEntity(true).relationshipStorageLocation(RELATION).module(SchemaRegistry.getModuleByName(
                        "o-ran-smo-teiv-equipment")).build();

        List<RelationType> relationTypes = List.of(gnbduFunctionProvidesNrcelldu, gnbduFunctionProvidesNrsectorcarrier,
                gnbduFunctionRealisedByCloudnativeapplication, nrSectorcarrierUsesAntennacapability,
                sectorGroupsAntennamodule, sectorGroupsNrcelldu, nrCellDuUsesNrSectorcarrier,
                managedElementManagesGNBCUCPFunction, nodeClusterLocatedAtCloudSite, antennaModuleInstalledAtSite,
                antennaModuleRealisedByAntennaModule);
        Map<String, RelationType> relationTypeMap = new HashMap<>();
        relationTypes.forEach(relationType -> relationTypeMap.put(relationType.getName(), relationType));
        SchemaRegistry.initializeRelationTypes(relationTypeMap);
    }

    private Map<String, DataType> getGnbduFunctionFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("gNBDUId", BIGINT);
        fields.put("gNBId", BIGINT);
        fields.put("gNBIdLength", BIGINT);
        fields.put("dUpLMNId", CONTAINER);
        fields.put("id", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("fdn", PRIMITIVE);
        fields.put("REL_FK_managed-by-managedElement", PRIMITIVE);
        fields.put("REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getGnbcuupFunctionFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("gNBId", BIGINT);
        fields.put("gNBIdLength", BIGINT);
        fields.put("id", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("fdn", PRIMITIVE);
        fields.put("REL_FK_managed-by-managedElement", PRIMITIVE);
        fields.put("REL_ID_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getNrCellDuFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("fdn", PRIMITIVE);
        fields.put("nCI", BIGINT);
        fields.put("cellLocalId", BIGINT);
        fields.put("id", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("nRPCI", BIGINT);
        fields.put("nRTAC", BIGINT);
        fields.put("REL_FK_grouped-by-sector", PRIMITIVE);
        fields.put("REL_ID_SECTOR_GROUPS_NRCELLDU", PRIMITIVE);
        fields.put("REL_FK_provided-by-gnbduFunction", PRIMITIVE);
        fields.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getNrSectorCarrierFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("frequencyDL", BIGINT);
        fields.put("fdn", PRIMITIVE);
        fields.put("frequencyUL", BIGINT);
        fields.put("arfcnUL", BIGINT);
        fields.put("id", PRIMITIVE);
        fields.put("essScLocalId", BIGINT);
        fields.put("cmId", CONTAINER);
        fields.put("arfcnDL", BIGINT);
        fields.put("REL_FK_used-by-nrCellDu", PRIMITIVE);
        fields.put("REL_ID_NRCELLDU_USES_NRSECTORCARRIER", PRIMITIVE);
        fields.put("REL_FK_provided-by-gnbduFunction", PRIMITIVE);
        fields.put("REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER", PRIMITIVE);
        fields.put("REL_FK_used-antennaCapability", PRIMITIVE);
        fields.put("REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getCloudNativeApplicationFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("name", PRIMITIVE);
        fields.put("REL_FK_realised-managedElement", PRIMITIVE);
        fields.put("REL_ID_MANAGEDELEMENT_REALISED_BY_CLOUDNATIVEAPPLICATION", PRIMITIVE);
        fields.put("REL_FK_comprised-by-cloudNativeSystem", PRIMITIVE);
        fields.put("REL_ID_CLOUDNATIVESYSTEM_COMPRISES_CLOUDNATIVEAPPLICATION", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getAntennaCapabilityFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("nRFqBands", CONTAINER);
        fields.put("id", PRIMITIVE);
        fields.put("eUtranFqBands", CONTAINER);
        fields.put("cmId", CONTAINER);
        fields.put("geranFqBands", CONTAINER);
        fields.put("fdn", PRIMITIVE);
        fields.put("REL_FK_used-by-lteSectorCarrier", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getSectorFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("sectorId", BIGINT);
        fields.put("id", PRIMITIVE);
        fields.put("azimuth", DECIMAL);

        return fields;
    }

    private Map<String, DataType> getAntennaModuleFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("positionWithinSector", PRIMITIVE);
        fields.put("antennaModelNumber", PRIMITIVE);
        fields.put("electricalAntennaTilt", BIGINT);
        fields.put("mechanicalAntennaTilt", BIGINT);
        fields.put("totalTilt", BIGINT);
        fields.put("id", PRIMITIVE);
        fields.put("mechanicalAntennaBearing", BIGINT);
        fields.put("fdn", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("REL_FK_grouped-by-sector", PRIMITIVE);
        fields.put("REL_ID_SECTOR_GROUPS_ANTENNAMODULE", PRIMITIVE);
        fields.put("REL_FK_installed-at-site", PRIMITIVE);
        fields.put("REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getSiteFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("name", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getCloudSiteFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("name", PRIMITIVE);
        fields.put("geo-location", GEOGRAPHIC);

        return fields;
    }

    private Map<String, DataType> getNodeClusterFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("name", PRIMITIVE);

        return fields;
    }

    private Map<String, DataType> getManagedElementFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("fdn", PRIMITIVE);
        fields.put("cmId", CONTAINER);
        fields.put("REL_FK_deployed-as-cloudNativeSystem", PRIMITIVE);
        fields.put("REL_ID_MANAGEDELEMENT_DEPLOYED_AS_CLOUDNATIVESYSTEM", CONTAINER);

        return fields;
    }

    private Map<String, DataType> getGNBCUCPFunctionFields() {
        Map<String, DataType> fields = new HashMap<>();
        fields.put("id", PRIMITIVE);
        fields.put("fdn", PRIMITIVE);
        fields.put("pLMNId", CONTAINER);
        fields.put("gNBCUName", PRIMITIVE);
        fields.put("gNBId", BIGINT);
        fields.put("cmId", CONTAINER);
        fields.put("gNBIdLength", BIGINT);
        fields.put("REL_FK_managed-by-managedElement", PRIMITIVE);
        fields.put("REL_ID_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION", CONTAINER);

        return fields;
    }

    private Association getRelationshipAssociation(String associationName, long minCardinality, long maxCardinality) {
        return Association.builder().name((associationName)).minCardinality(minCardinality).maxCardinality(maxCardinality)
                .build();
    }
}
