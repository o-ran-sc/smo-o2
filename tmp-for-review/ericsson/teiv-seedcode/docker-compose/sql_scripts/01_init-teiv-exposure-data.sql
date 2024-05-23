--
--  ============LICENSE_START=======================================================
--  Copyright (C) 2024 Ericsson
--  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
--  ================================================================================
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--
--        http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--
--  SPDX-License-Identifier: Apache-2.0
--  ============LICENSE_END=========================================================
--

BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

GRANT USAGE ON SCHEMA topology TO topology_exposure_user;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA topology TO topology_exposure_user;
GRANT SELECT ON ALL TABLES IN SCHEMA topology TO topology_exposure_user;

CREATE SCHEMA IF NOT EXISTS ties_data;
ALTER SCHEMA ties_data OWNER TO topology_exposure_user;
SET default_tablespace = '';
SET default_table_access_method = heap;

SET ROLE 'topology_exposure_user';

-- Function to create CONSTRAINT only if it does not exists
CREATE OR REPLACE FUNCTION ties_data.create_constraint_if_not_exists (
    t_name TEXT, c_name TEXT, constraint_sql TEXT
)
RETURNS void AS
$$
BEGIN
    IF NOT EXISTS (SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = t_name AND constraint_name = c_name) THEN
        EXECUTE constraint_sql;
    END IF;
END;
$$ language 'plpgsql';

-- Update data schema exec status
INSERT INTO ties_model.execution_status("schema", "status") VALUES ('ties_data', 'success');

CREATE TABLE IF NOT EXISTS ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" (
    "id"            VARCHAR(511),
    "aSide_AntennaModule"            VARCHAR(511),
    "bSide_AntennaCapability"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."AntennaCapability" (
    "id"            VARCHAR(511),
    "nRFqBands"            jsonb,
    "cmId"            jsonb,
    "geranFqBands"            jsonb,
    "fdn"            TEXT,
    "eUtranFqBands"            jsonb,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."AntennaModule" (
    "id"            VARCHAR(511),
    "totalTilt"            BIGINT,
    "cmId"            jsonb,
    "antennaBeamWidth"            jsonb,
    "positionWithinSector"            TEXT,
    "geo-location"            geography,
    "mechanicalAntennaBearing"            BIGINT,
    "fdn"            TEXT,
    "electricalAntennaTilt"            BIGINT,
    "mechanicalAntennaTilt"            BIGINT,
    "antennaModelNumber"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_grouped-by-sector"            VARCHAR(511),
    "REL_ID_SECTOR_GROUPS_ANTENNAMODULE"            VARCHAR(511),
    "REL_CD_sourceIds_SECTOR_GROUPS_ANTENNAMODULE"            jsonb,
    "REL_CD_classifiers_SECTOR_GROUPS_ANTENNAMODULE"            jsonb,
    "REL_CD_decorators_SECTOR_GROUPS_ANTENNAMODULE"            jsonb,
    "REL_FK_installed-at-site"            VARCHAR(511),
    "REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE"            VARCHAR(511),
    "REL_CD_sourceIds_ANTENNAMODULE_INSTALLED_AT_SITE"            jsonb,
    "REL_CD_classifiers_ANTENNAMODULE_INSTALLED_AT_SITE"            jsonb,
    "REL_CD_decorators_ANTENNAMODULE_INSTALLED_AT_SITE"            jsonb
);

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_sourceIds_SECTOR_GROUPS_ANTENNAMODULE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_classifiers_SECTOR_GROUPS_ANTENNAMODULE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_decorators_SECTOR_GROUPS_ANTENNAMODULE" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_sourceIds_ANTENNAMODULE_INSTALLED_AT_SITE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_classifiers_ANTENNAMODULE_INSTALLED_AT_SITE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "REL_CD_decorators_ANTENNAMODULE_INSTALLED_AT_SITE" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."CloudNamespace" (
    "id"            VARCHAR(511),
    "name"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_deployed-on-nodeCluster"            VARCHAR(511),
    "REL_ID_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER"            VARCHAR(511),
    "REL_CD_sourceIds_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER"            jsonb,
    "REL_CD_classifiers_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER"            jsonb,
    "REL_CD_decorators_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER"            jsonb
);

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "REL_CD_sourceIds_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "REL_CD_classifiers_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudNamespace" ALTER COLUMN "REL_CD_decorators_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."CloudSite" (
    "id"            VARCHAR(511),
    "name"            TEXT,
    "geo-location"            geography,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."CloudSite" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudSite" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudSite" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."CloudifiedNF" (
    "id"            VARCHAR(511),
    "name"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."CloudifiedNF" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudifiedNF" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."CloudifiedNF" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ENodeBFunction" (
    "id"            VARCHAR(511),
    "fdn"            TEXT,
    "cmId"            jsonb,
    "eNodeBPlmnId"            jsonb,
    "eNBId"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_managed-by-managedElement"            VARCHAR(511),
    "REL_ID_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION"            jsonb,
    "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION"            jsonb,
    "REL_CD_decorators_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION"            jsonb,
    "REL_FK_serving-physicalNF"            VARCHAR(511),
    "REL_ID_PHYSICALNF_SERVES_ENODEBFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_PHYSICALNF_SERVES_ENODEBFUNCTION"            jsonb,
    "REL_CD_classifiers_PHYSICALNF_SERVES_ENODEBFUNCTION"            jsonb,
    "REL_CD_decorators_PHYSICALNF_SERVES_ENODEBFUNCTION"            jsonb
);

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_decorators_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_sourceIds_PHYSICALNF_SERVES_ENODEBFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_classifiers_PHYSICALNF_SERVES_ENODEBFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "REL_CD_decorators_PHYSICALNF_SERVES_ENODEBFUNCTION" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."EUtranCell" (
    "id"            VARCHAR(511),
    "earfcndl"            BIGINT,
    "earfcnul"            BIGINT,
    "cmId"            jsonb,
    "tac"            BIGINT,
    "dlChannelBandwidth"            BIGINT,
    "fdn"            TEXT,
    "cellId"            BIGINT,
    "duplexType"            TEXT,
    "channelBandwidth"            BIGINT,
    "earfcn"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_provided-by-enodebFunction"            VARCHAR(511),
    "REL_ID_ENODEBFUNCTION_PROVIDES_EUTRANCELL"            VARCHAR(511),
    "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_EUTRANCELL"            jsonb,
    "REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_EUTRANCELL"            jsonb,
    "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_EUTRANCELL"            jsonb,
    "REL_FK_grouped-by-sector"            VARCHAR(511),
    "REL_ID_SECTOR_GROUPS_EUTRANCELL"            VARCHAR(511),
    "REL_CD_sourceIds_SECTOR_GROUPS_EUTRANCELL"            jsonb,
    "REL_CD_classifiers_SECTOR_GROUPS_EUTRANCELL"            jsonb,
    "REL_CD_decorators_SECTOR_GROUPS_EUTRANCELL"            jsonb
);

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_EUTRANCELL" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_EUTRANCELL" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_EUTRANCELL" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_sourceIds_SECTOR_GROUPS_EUTRANCELL" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_classifiers_SECTOR_GROUPS_EUTRANCELL" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."EUtranCell" ALTER COLUMN "REL_CD_decorators_SECTOR_GROUPS_EUTRANCELL" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."GNBCUCPFunction" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "pLMNId"            jsonb,
    "gNBIdLength"            BIGINT,
    "fdn"            TEXT,
    "gNBCUName"            TEXT,
    "gNBId"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_managed-by-managedElement"            VARCHAR(511),
    "REL_ID_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION"            jsonb,
    "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION"            jsonb,
    "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION"            jsonb,
    "REL_FK_serving-physicalNF"            VARCHAR(511),
    "REL_ID_PHYSICALNF_SERVES_GNBCUCPFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBCUCPFUNCTION"            jsonb,
    "REL_CD_classifiers_PHYSICALNF_SERVES_GNBCUCPFUNCTION"            jsonb,
    "REL_CD_decorators_PHYSICALNF_SERVES_GNBCUCPFUNCTION"            jsonb
);

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBCUCPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_classifiers_PHYSICALNF_SERVES_GNBCUCPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUCPFunction" ALTER COLUMN "REL_CD_decorators_PHYSICALNF_SERVES_GNBCUCPFUNCTION" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."GNBCUUPFunction" (
    "id"            VARCHAR(511),
    "fdn"            TEXT,
    "cmId"            jsonb,
    "gNBIdLength"            BIGINT,
    "gNBId"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_managed-by-managedElement"            VARCHAR(511),
    "REL_ID_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION"            jsonb,
    "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION"            jsonb,
    "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION"            jsonb,
    "REL_FK_serving-physicalNF"            VARCHAR(511),
    "REL_ID_PHYSICALNF_SERVES_GNBCUUPFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBCUUPFUNCTION"            jsonb,
    "REL_CD_classifiers_PHYSICALNF_SERVES_GNBCUUPFUNCTION"            jsonb,
    "REL_CD_decorators_PHYSICALNF_SERVES_GNBCUUPFUNCTION"            jsonb
);

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBCUUPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_classifiers_PHYSICALNF_SERVES_GNBCUUPFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBCUUPFunction" ALTER COLUMN "REL_CD_decorators_PHYSICALNF_SERVES_GNBCUUPFUNCTION" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."GNBDUFunction" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "gNBIdLength"            BIGINT,
    "dUpLMNId"            jsonb,
    "fdn"            TEXT,
    "gNBDUId"            BIGINT,
    "gNBId"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_managed-by-managedElement"            VARCHAR(511),
    "REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION"            jsonb,
    "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION"            jsonb,
    "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION"            jsonb,
    "REL_FK_serving-physicalNF"            VARCHAR(511),
    "REL_ID_PHYSICALNF_SERVES_GNBDUFUNCTION"            VARCHAR(511),
    "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBDUFUNCTION"            jsonb,
    "REL_CD_classifiers_PHYSICALNF_SERVES_GNBDUFUNCTION"            jsonb,
    "REL_CD_decorators_PHYSICALNF_SERVES_GNBDUFUNCTION"            jsonb
);

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_sourceIds_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_classifiers_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_decorators_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_sourceIds_PHYSICALNF_SERVES_GNBDUFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_classifiers_PHYSICALNF_SERVES_GNBDUFUNCTION" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "REL_CD_decorators_PHYSICALNF_SERVES_GNBDUFUNCTION" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."LTESectorCarrier" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "fdn"            TEXT,
    "sectorCarrierType"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_provided-by-enodebFunction"            VARCHAR(511),
    "REL_ID_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"            VARCHAR(511),
    "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"            jsonb,
    "REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"            jsonb,
    "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"            jsonb,
    "REL_FK_used-antennaCapability"            VARCHAR(511),
    "REL_ID_LTESECTORCARRIER_USES_ANTENNACAPABILITY"            VARCHAR(511),
    "REL_CD_sourceIds_LTESECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb,
    "REL_CD_classifiers_LTESECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb,
    "REL_CD_decorators_LTESECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb,
    "REL_FK_used-by-euTranCell"            VARCHAR(511),
    "REL_ID_EUTRANCELL_USES_LTESECTORCARRIER"            VARCHAR(511),
    "REL_CD_sourceIds_EUTRANCELL_USES_LTESECTORCARRIER"            jsonb,
    "REL_CD_classifiers_EUTRANCELL_USES_LTESECTORCARRIER"            jsonb,
    "REL_CD_decorators_EUTRANCELL_USES_LTESECTORCARRIER"            jsonb
);

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_classifiers_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_decorators_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_EUTRANCELL_USES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_classifiers_EUTRANCELL_USES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_decorators_EUTRANCELL_USES_LTESECTORCARRIER" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ManagedElement" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "fdn"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_deployed-as-cloudifiedNF"            VARCHAR(511),
    "REL_ID_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF"            VARCHAR(511),
    "REL_CD_sourceIds_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF"            jsonb,
    "REL_CD_classifiers_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF"            jsonb,
    "REL_CD_decorators_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF"            jsonb
);

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "REL_CD_sourceIds_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "REL_CD_classifiers_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ManagedElement" ALTER COLUMN "REL_CD_decorators_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" (
    "id"            VARCHAR(511),
    "aSide_NFDeployment"            VARCHAR(511),
    "bSide_CloudNamespace"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" (
    "id"            VARCHAR(511),
    "aSide_NFDeployment"            VARCHAR(511),
    "bSide_GNBCUCPFunction"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" (
    "id"            VARCHAR(511),
    "aSide_NFDeployment"            VARCHAR(511),
    "bSide_GNBCUUPFunction"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" (
    "id"            VARCHAR(511),
    "aSide_NFDeployment"            VARCHAR(511),
    "bSide_GNBDUFunction"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NFDeployment" (
    "id"            VARCHAR(511),
    "name"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_serviced-managedElement"            VARCHAR(511),
    "REL_ID_NFDEPLOYMENT_SERVES_MANAGEDELEMENT"            VARCHAR(511),
    "REL_CD_sourceIds_NFDEPLOYMENT_SERVES_MANAGEDELEMENT"            jsonb,
    "REL_CD_classifiers_NFDEPLOYMENT_SERVES_MANAGEDELEMENT"            jsonb,
    "REL_CD_decorators_NFDEPLOYMENT_SERVES_MANAGEDELEMENT"            jsonb,
    "REL_FK_comprised-by-cloudifiedNF"            VARCHAR(511),
    "REL_ID_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT"            VARCHAR(511),
    "REL_CD_sourceIds_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT"            jsonb,
    "REL_CD_classifiers_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT"            jsonb,
    "REL_CD_decorators_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT"            jsonb
);

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_sourceIds_NFDEPLOYMENT_SERVES_MANAGEDELEMENT" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_classifiers_NFDEPLOYMENT_SERVES_MANAGEDELEMENT" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_decorators_NFDEPLOYMENT_SERVES_MANAGEDELEMENT" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_sourceIds_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_classifiers_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NFDeployment" ALTER COLUMN "REL_CD_decorators_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" (
    "id"            VARCHAR(511),
    "aSide_NodeCluster"            VARCHAR(511),
    "bSide_CloudSite"            VARCHAR(511),
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NRCellCU" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "fdn"            TEXT,
    "nCI"            BIGINT,
    "nRTAC"            BIGINT,
    "plmnId"            jsonb,
    "cellLocalId"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_provided-by-gnbcucpFunction"            VARCHAR(511),
    "REL_ID_GNBCUCPFUNCTION_PROVIDES_NRCELLCU"            VARCHAR(511),
    "REL_CD_sourceIds_GNBCUCPFUNCTION_PROVIDES_NRCELLCU"            jsonb,
    "REL_CD_classifiers_GNBCUCPFUNCTION_PROVIDES_NRCELLCU"            jsonb,
    "REL_CD_decorators_GNBCUCPFUNCTION_PROVIDES_NRCELLCU"            jsonb
);

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "REL_CD_sourceIds_GNBCUCPFUNCTION_PROVIDES_NRCELLCU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "REL_CD_classifiers_GNBCUCPFUNCTION_PROVIDES_NRCELLCU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellCU" ALTER COLUMN "REL_CD_decorators_GNBCUCPFUNCTION_PROVIDES_NRCELLCU" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NRCellDU" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "nCI"            BIGINT,
    "nRPCI"            BIGINT,
    "fdn"            TEXT,
    "cellLocalId"            BIGINT,
    "nRTAC"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_grouped-by-sector"            VARCHAR(511),
    "REL_ID_SECTOR_GROUPS_NRCELLDU"            VARCHAR(511),
    "REL_CD_sourceIds_SECTOR_GROUPS_NRCELLDU"            jsonb,
    "REL_CD_classifiers_SECTOR_GROUPS_NRCELLDU"            jsonb,
    "REL_CD_decorators_SECTOR_GROUPS_NRCELLDU"            jsonb,
    "REL_FK_provided-by-gnbduFunction"            VARCHAR(511),
    "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU"            VARCHAR(511),
    "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU"            jsonb,
    "REL_CD_classifiers_GNBDUFUNCTION_PROVIDES_NRCELLDU"            jsonb,
    "REL_CD_decorators_GNBDUFUNCTION_PROVIDES_NRCELLDU"            jsonb
);

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_sourceIds_SECTOR_GROUPS_NRCELLDU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_classifiers_SECTOR_GROUPS_NRCELLDU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_decorators_SECTOR_GROUPS_NRCELLDU" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRCELLDU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_classifiers_GNBDUFUNCTION_PROVIDES_NRCELLDU" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRCellDU" ALTER COLUMN "REL_CD_decorators_GNBDUFUNCTION_PROVIDES_NRCELLDU" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NRSectorCarrier" (
    "id"            VARCHAR(511),
    "bSChannelBwDL"            BIGINT,
    "cmId"            jsonb,
    "arfcnDL"            BIGINT,
    "frequencyUL"            BIGINT,
    "fdn"            TEXT,
    "arfcnUL"            BIGINT,
    "frequencyDL"            BIGINT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_used-by-nrCellDu"            VARCHAR(511),
    "REL_ID_NRCELLDU_USES_NRSECTORCARRIER"            VARCHAR(511),
    "REL_CD_sourceIds_NRCELLDU_USES_NRSECTORCARRIER"            jsonb,
    "REL_CD_classifiers_NRCELLDU_USES_NRSECTORCARRIER"            jsonb,
    "REL_CD_decorators_NRCELLDU_USES_NRSECTORCARRIER"            jsonb,
    "REL_FK_provided-by-gnbduFunction"            VARCHAR(511),
    "REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER"            VARCHAR(511),
    "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER"            jsonb,
    "REL_CD_classifiers_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER"            jsonb,
    "REL_CD_decorators_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER"            jsonb,
    "REL_FK_used-antennaCapability"            VARCHAR(511),
    "REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY"            VARCHAR(511),
    "REL_CD_sourceIds_NRSECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb,
    "REL_CD_classifiers_NRSECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb,
    "REL_CD_decorators_NRSECTORCARRIER_USES_ANTENNACAPABILITY"            jsonb
);

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_sourceIds_NRCELLDU_USES_NRSECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_classifiers_NRCELLDU_USES_NRSECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_decorators_NRCELLDU_USES_NRSECTORCARRIER" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_sourceIds_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_classifiers_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_decorators_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_sourceIds_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_classifiers_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_decorators_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NodeCluster" (
    "id"            VARCHAR(511),
    "name"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."NodeCluster" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NodeCluster" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NodeCluster" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."PhysicalNF" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "geo-location"            geography,
    "name"            TEXT,
    "type"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb,
    "REL_FK_installed-at-site"            VARCHAR(511),
    "REL_ID_PHYSICALNF_INSTALLED_AT_SITE"            VARCHAR(511),
    "REL_CD_sourceIds_PHYSICALNF_INSTALLED_AT_SITE"            jsonb,
    "REL_CD_classifiers_PHYSICALNF_INSTALLED_AT_SITE"            jsonb,
    "REL_CD_decorators_PHYSICALNF_INSTALLED_AT_SITE"            jsonb
);

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "REL_CD_sourceIds_PHYSICALNF_INSTALLED_AT_SITE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "REL_CD_classifiers_PHYSICALNF_INSTALLED_AT_SITE" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."PhysicalNF" ALTER COLUMN "REL_CD_decorators_PHYSICALNF_INSTALLED_AT_SITE" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."Sector" (
    "id"            VARCHAR(511),
    "geo-location"            geography,
    "sectorId"            BIGINT,
    "azimuth"            DECIMAL,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."Sector" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."Sector" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."Sector" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."Site" (
    "id"            VARCHAR(511),
    "cmId"            jsonb,
    "geo-location"            geography,
    "name"            TEXT,
    "CD_sourceIds"            jsonb,
    "CD_classifiers"            jsonb,
    "CD_decorators"            jsonb
);

ALTER TABLE ONLY ties_data."Site" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."Site" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."Site" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

SELECT ties_data.create_constraint_if_not_exists(
    'ANTENNAMODULE_SERVES_ANTENNACAPABILITY',
 'PK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaCapability',
 'PK_AntennaCapability_id',
 'ALTER TABLE ties_data."AntennaCapability" ADD CONSTRAINT "PK_AntennaCapability_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaModule',
 'PK_AntennaModule_id',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "PK_AntennaModule_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'CloudNamespace',
 'PK_CloudNamespace_id',
 'ALTER TABLE ties_data."CloudNamespace" ADD CONSTRAINT "PK_CloudNamespace_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'CloudSite',
 'PK_CloudSite_id',
 'ALTER TABLE ties_data."CloudSite" ADD CONSTRAINT "PK_CloudSite_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'CloudifiedNF',
 'PK_CloudifiedNF_id',
 'ALTER TABLE ties_data."CloudifiedNF" ADD CONSTRAINT "PK_CloudifiedNF_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ENodeBFunction',
 'PK_ENodeBFunction_id',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "PK_ENodeBFunction_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'EUtranCell',
 'PK_EUtranCell_id',
 'ALTER TABLE ties_data."EUtranCell" ADD CONSTRAINT "PK_EUtranCell_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUCPFunction',
 'PK_GNBCUCPFunction_id',
 'ALTER TABLE ties_data."GNBCUCPFunction" ADD CONSTRAINT "PK_GNBCUCPFunction_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUUPFunction',
 'PK_GNBCUUPFunction_id',
 'ALTER TABLE ties_data."GNBCUUPFunction" ADD CONSTRAINT "PK_GNBCUUPFunction_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBDUFunction',
 'PK_GNBDUFunction_id',
 'ALTER TABLE ties_data."GNBDUFunction" ADD CONSTRAINT "PK_GNBDUFunction_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'PK_LTESectorCarrier_id',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "PK_LTESectorCarrier_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ManagedElement',
 'PK_ManagedElement_id',
 'ALTER TABLE ties_data."ManagedElement" ADD CONSTRAINT "PK_ManagedElement_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE',
 'PK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_id',
 'ALTER TABLE ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ADD CONSTRAINT "PK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION',
 'PK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_id',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ADD CONSTRAINT "PK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION',
 'PK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_id',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ADD CONSTRAINT "PK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBDUFUNCTION',
 'PK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_id',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ADD CONSTRAINT "PK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDeployment',
 'PK_NFDeployment_id',
 'ALTER TABLE ties_data."NFDeployment" ADD CONSTRAINT "PK_NFDeployment_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NODECLUSTER_LOCATED_AT_CLOUDSITE',
 'PK_NODECLUSTER_LOCATED_AT_CLOUDSITE_id',
 'ALTER TABLE ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ADD CONSTRAINT "PK_NODECLUSTER_LOCATED_AT_CLOUDSITE_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellCU',
 'PK_NRCellCU_id',
 'ALTER TABLE ties_data."NRCellCU" ADD CONSTRAINT "PK_NRCellCU_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellDU',
 'PK_NRCellDU_id',
 'ALTER TABLE ties_data."NRCellDU" ADD CONSTRAINT "PK_NRCellDU_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'PK_NRSectorCarrier_id',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "PK_NRSectorCarrier_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NodeCluster',
 'PK_NodeCluster_id',
 'ALTER TABLE ties_data."NodeCluster" ADD CONSTRAINT "PK_NodeCluster_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'PhysicalNF',
 'PK_PhysicalNF_id',
 'ALTER TABLE ties_data."PhysicalNF" ADD CONSTRAINT "PK_PhysicalNF_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'Sector',
 'PK_Sector_id',
 'ALTER TABLE ties_data."Sector" ADD CONSTRAINT "PK_Sector_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'Site',
 'PK_Site_id',
 'ALTER TABLE ties_data."Site" ADD CONSTRAINT "PK_Site_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ANTENNAMODULE_SERVES_ANTENNACAPABILITY',
 'FK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_aSide_AntennaModule',
 'ALTER TABLE ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_aSide_AntennaModule" FOREIGN KEY ("aSide_AntennaModule") REFERENCES ties_data."AntennaModule" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ANTENNAMODULE_SERVES_ANTENNACAPABILITY',
 'FK_AB3CEA707D389B107F1D10BC724542418E02ABEC',
 'ALTER TABLE ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_AB3CEA707D389B107F1D10BC724542418E02ABEC" FOREIGN KEY ("bSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaModule',
 'FK_AntennaModule_REL_FK_grouped-by-sector',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "FK_AntennaModule_REL_FK_grouped-by-sector" FOREIGN KEY ("REL_FK_grouped-by-sector") REFERENCES ties_data."Sector" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaModule',
 'UNIQUE_AntennaModule_REL_ID_SECTOR_GROUPS_ANTENNAMODULE',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "UNIQUE_AntennaModule_REL_ID_SECTOR_GROUPS_ANTENNAMODULE" UNIQUE ("REL_ID_SECTOR_GROUPS_ANTENNAMODULE");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaModule',
 'FK_AntennaModule_REL_FK_installed-at-site',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "FK_AntennaModule_REL_FK_installed-at-site" FOREIGN KEY ("REL_FK_installed-at-site") REFERENCES ties_data."Site" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'AntennaModule',
 'UNIQUE_AntennaModule_REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "UNIQUE_AntennaModule_REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE" UNIQUE ("REL_ID_ANTENNAMODULE_INSTALLED_AT_SITE");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'CloudNamespace',
 'FK_CloudNamespace_REL_FK_deployed-on-nodeCluster',
 'ALTER TABLE ties_data."CloudNamespace" ADD CONSTRAINT "FK_CloudNamespace_REL_FK_deployed-on-nodeCluster" FOREIGN KEY ("REL_FK_deployed-on-nodeCluster") REFERENCES ties_data."NodeCluster" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'CloudNamespace',
 'UNIQUE_C72E1EF93E1AC8FA53D20808E775FF012ACB46F0',
 'ALTER TABLE ties_data."CloudNamespace" ADD CONSTRAINT "UNIQUE_C72E1EF93E1AC8FA53D20808E775FF012ACB46F0" UNIQUE ("REL_ID_CLOUDNAMESPACE_DEPLOYED_ON_NODECLUSTER");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ENodeBFunction',
 'FK_ENodeBFunction_REL_FK_managed-by-managedElement',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "FK_ENodeBFunction_REL_FK_managed-by-managedElement" FOREIGN KEY ("REL_FK_managed-by-managedElement") REFERENCES ties_data."ManagedElement" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ENodeBFunction',
 'UNIQUE_F33037EE8037D0606D15FFB45EE8A27FD6DE30EE',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "UNIQUE_F33037EE8037D0606D15FFB45EE8A27FD6DE30EE" UNIQUE ("REL_ID_MANAGEDELEMENT_MANAGES_ENODEBFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ENodeBFunction',
 'FK_ENodeBFunction_REL_FK_serving-physicalNF',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "FK_ENodeBFunction_REL_FK_serving-physicalNF" FOREIGN KEY ("REL_FK_serving-physicalNF") REFERENCES ties_data."PhysicalNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ENodeBFunction',
 'UNIQUE_ENodeBFunction_REL_ID_PHYSICALNF_SERVES_ENODEBFUNCTION',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "UNIQUE_ENodeBFunction_REL_ID_PHYSICALNF_SERVES_ENODEBFUNCTION" UNIQUE ("REL_ID_PHYSICALNF_SERVES_ENODEBFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'EUtranCell',
 'FK_EUtranCell_REL_FK_provided-by-enodebFunction',
 'ALTER TABLE ties_data."EUtranCell" ADD CONSTRAINT "FK_EUtranCell_REL_FK_provided-by-enodebFunction" FOREIGN KEY ("REL_FK_provided-by-enodebFunction") REFERENCES ties_data."ENodeBFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'EUtranCell',
 'UNIQUE_EUtranCell_REL_ID_ENODEBFUNCTION_PROVIDES_EUTRANCELL',
 'ALTER TABLE ties_data."EUtranCell" ADD CONSTRAINT "UNIQUE_EUtranCell_REL_ID_ENODEBFUNCTION_PROVIDES_EUTRANCELL" UNIQUE ("REL_ID_ENODEBFUNCTION_PROVIDES_EUTRANCELL");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'EUtranCell',
 'FK_EUtranCell_REL_FK_grouped-by-sector',
 'ALTER TABLE ties_data."EUtranCell" ADD CONSTRAINT "FK_EUtranCell_REL_FK_grouped-by-sector" FOREIGN KEY ("REL_FK_grouped-by-sector") REFERENCES ties_data."Sector" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'EUtranCell',
 'UNIQUE_EUtranCell_REL_ID_SECTOR_GROUPS_EUTRANCELL',
 'ALTER TABLE ties_data."EUtranCell" ADD CONSTRAINT "UNIQUE_EUtranCell_REL_ID_SECTOR_GROUPS_EUTRANCELL" UNIQUE ("REL_ID_SECTOR_GROUPS_EUTRANCELL");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUCPFunction',
 'FK_GNBCUCPFunction_REL_FK_managed-by-managedElement',
 'ALTER TABLE ties_data."GNBCUCPFunction" ADD CONSTRAINT "FK_GNBCUCPFunction_REL_FK_managed-by-managedElement" FOREIGN KEY ("REL_FK_managed-by-managedElement") REFERENCES ties_data."ManagedElement" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUCPFunction',
 'UNIQUE_249F73FF1F4316A56DEF4424FA43C2064FFBE4DD',
 'ALTER TABLE ties_data."GNBCUCPFunction" ADD CONSTRAINT "UNIQUE_249F73FF1F4316A56DEF4424FA43C2064FFBE4DD" UNIQUE ("REL_ID_MANAGEDELEMENT_MANAGES_GNBCUCPFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUCPFunction',
 'FK_GNBCUCPFunction_REL_FK_serving-physicalNF',
 'ALTER TABLE ties_data."GNBCUCPFunction" ADD CONSTRAINT "FK_GNBCUCPFunction_REL_FK_serving-physicalNF" FOREIGN KEY ("REL_FK_serving-physicalNF") REFERENCES ties_data."PhysicalNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUCPFunction',
 'UNIQUE_GNBCUCPFunction_REL_ID_PHYSICALNF_SERVES_GNBCUCPFUNCTION',
 'ALTER TABLE ties_data."GNBCUCPFunction" ADD CONSTRAINT "UNIQUE_GNBCUCPFunction_REL_ID_PHYSICALNF_SERVES_GNBCUCPFUNCTION" UNIQUE ("REL_ID_PHYSICALNF_SERVES_GNBCUCPFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUUPFunction',
 'FK_GNBCUUPFunction_REL_FK_managed-by-managedElement',
 'ALTER TABLE ties_data."GNBCUUPFunction" ADD CONSTRAINT "FK_GNBCUUPFunction_REL_FK_managed-by-managedElement" FOREIGN KEY ("REL_FK_managed-by-managedElement") REFERENCES ties_data."ManagedElement" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUUPFunction',
 'UNIQUE_BDB349CDF0C4055902881ECCB71F460AE1DD323E',
 'ALTER TABLE ties_data."GNBCUUPFunction" ADD CONSTRAINT "UNIQUE_BDB349CDF0C4055902881ECCB71F460AE1DD323E" UNIQUE ("REL_ID_MANAGEDELEMENT_MANAGES_GNBCUUPFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUUPFunction',
 'FK_GNBCUUPFunction_REL_FK_serving-physicalNF',
 'ALTER TABLE ties_data."GNBCUUPFunction" ADD CONSTRAINT "FK_GNBCUUPFunction_REL_FK_serving-physicalNF" FOREIGN KEY ("REL_FK_serving-physicalNF") REFERENCES ties_data."PhysicalNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBCUUPFunction',
 'UNIQUE_GNBCUUPFunction_REL_ID_PHYSICALNF_SERVES_GNBCUUPFUNCTION',
 'ALTER TABLE ties_data."GNBCUUPFunction" ADD CONSTRAINT "UNIQUE_GNBCUUPFunction_REL_ID_PHYSICALNF_SERVES_GNBCUUPFUNCTION" UNIQUE ("REL_ID_PHYSICALNF_SERVES_GNBCUUPFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBDUFunction',
 'FK_GNBDUFunction_REL_FK_managed-by-managedElement',
 'ALTER TABLE ties_data."GNBDUFunction" ADD CONSTRAINT "FK_GNBDUFunction_REL_FK_managed-by-managedElement" FOREIGN KEY ("REL_FK_managed-by-managedElement") REFERENCES ties_data."ManagedElement" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBDUFunction',
 'UNIQUE_08DFEFAF56EDDE43CBDC336F459D28C6518D3E1D',
 'ALTER TABLE ties_data."GNBDUFunction" ADD CONSTRAINT "UNIQUE_08DFEFAF56EDDE43CBDC336F459D28C6518D3E1D" UNIQUE ("REL_ID_MANAGEDELEMENT_MANAGES_GNBDUFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBDUFunction',
 'FK_GNBDUFunction_REL_FK_serving-physicalNF',
 'ALTER TABLE ties_data."GNBDUFunction" ADD CONSTRAINT "FK_GNBDUFunction_REL_FK_serving-physicalNF" FOREIGN KEY ("REL_FK_serving-physicalNF") REFERENCES ties_data."PhysicalNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'GNBDUFunction',
 'UNIQUE_GNBDUFunction_REL_ID_PHYSICALNF_SERVES_GNBDUFUNCTION',
 'ALTER TABLE ties_data."GNBDUFunction" ADD CONSTRAINT "UNIQUE_GNBDUFunction_REL_ID_PHYSICALNF_SERVES_GNBDUFUNCTION" UNIQUE ("REL_ID_PHYSICALNF_SERVES_GNBDUFUNCTION");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'FK_LTESectorCarrier_REL_FK_provided-by-enodebFunction',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "FK_LTESectorCarrier_REL_FK_provided-by-enodebFunction" FOREIGN KEY ("REL_FK_provided-by-enodebFunction") REFERENCES ties_data."ENodeBFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'UNIQUE_B9770D6C26DDA0173DB9690F6E3B42C111AF26E9',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "UNIQUE_B9770D6C26DDA0173DB9690F6E3B42C111AF26E9" UNIQUE ("REL_ID_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'FK_LTESectorCarrier_REL_FK_used-antennaCapability',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "FK_LTESectorCarrier_REL_FK_used-antennaCapability" FOREIGN KEY ("REL_FK_used-antennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'UNIQUE_5D5FEB6B4B09D5D42A589753C684994CD0B96E88',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "UNIQUE_5D5FEB6B4B09D5D42A589753C684994CD0B96E88" UNIQUE ("REL_ID_LTESECTORCARRIER_USES_ANTENNACAPABILITY");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'FK_LTESectorCarrier_REL_FK_used-by-euTranCell',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "FK_LTESectorCarrier_REL_FK_used-by-euTranCell" FOREIGN KEY ("REL_FK_used-by-euTranCell") REFERENCES ties_data."EUtranCell" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'LTESectorCarrier',
 'UNIQUE_LTESectorCarrier_REL_ID_EUTRANCELL_USES_LTESECTORCARRIER',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "UNIQUE_LTESectorCarrier_REL_ID_EUTRANCELL_USES_LTESECTORCARRIER" UNIQUE ("REL_ID_EUTRANCELL_USES_LTESECTORCARRIER");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ManagedElement',
 'FK_ManagedElement_REL_FK_deployed-as-cloudifiedNF',
 'ALTER TABLE ties_data."ManagedElement" ADD CONSTRAINT "FK_ManagedElement_REL_FK_deployed-as-cloudifiedNF" FOREIGN KEY ("REL_FK_deployed-as-cloudifiedNF") REFERENCES ties_data."CloudifiedNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'ManagedElement',
 'UNIQUE_E7BC94037DB5B94B7E863A10BEA20C2D4C3C307C',
 'ALTER TABLE ties_data."ManagedElement" ADD CONSTRAINT "UNIQUE_E7BC94037DB5B94B7E863A10BEA20C2D4C3C307C" UNIQUE ("REL_ID_MANAGEDELEMENT_DEPLOYED_AS_CLOUDIFIEDNF");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE',
 'FK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_aSide_NFDeployment',
 'ALTER TABLE ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ADD CONSTRAINT "FK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_aSide_NFDeployment" FOREIGN KEY ("aSide_NFDeployment") REFERENCES ties_data."NFDeployment" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE',
 'FK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_bSide_CloudNamespace',
 'ALTER TABLE ties_data."NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE" ADD CONSTRAINT "FK_NFDEPLOYMENT_DEPLOYED_ON_CLOUDNAMESPACE_bSide_CloudNamespace" FOREIGN KEY ("bSide_CloudNamespace") REFERENCES ties_data."CloudNamespace" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_aSide_NFDeployment',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_aSide_NFDeployment" FOREIGN KEY ("aSide_NFDeployment") REFERENCES ties_data."NFDeployment" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_bSide_GNBCUCPFunction',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBCUCPFUNCTION_bSide_GNBCUCPFunction" FOREIGN KEY ("bSide_GNBCUCPFunction") REFERENCES ties_data."GNBCUCPFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_aSide_NFDeployment',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_aSide_NFDeployment" FOREIGN KEY ("aSide_NFDeployment") REFERENCES ties_data."NFDeployment" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_bSide_GNBCUUPFunction',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBCUUPFUNCTION_bSide_GNBCUUPFunction" FOREIGN KEY ("bSide_GNBCUUPFunction") REFERENCES ties_data."GNBCUUPFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBDUFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_aSide_NFDeployment',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_aSide_NFDeployment" FOREIGN KEY ("aSide_NFDeployment") REFERENCES ties_data."NFDeployment" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDEPLOYMENT_SERVES_GNBDUFUNCTION',
 'FK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_bSide_GNBDUFunction',
 'ALTER TABLE ties_data."NFDEPLOYMENT_SERVES_GNBDUFUNCTION" ADD CONSTRAINT "FK_NFDEPLOYMENT_SERVES_GNBDUFUNCTION_bSide_GNBDUFunction" FOREIGN KEY ("bSide_GNBDUFunction") REFERENCES ties_data."GNBDUFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDeployment',
 'FK_NFDeployment_REL_FK_serviced-managedElement',
 'ALTER TABLE ties_data."NFDeployment" ADD CONSTRAINT "FK_NFDeployment_REL_FK_serviced-managedElement" FOREIGN KEY ("REL_FK_serviced-managedElement") REFERENCES ties_data."ManagedElement" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDeployment',
 'UNIQUE_NFDeployment_REL_ID_NFDEPLOYMENT_SERVES_MANAGEDELEMENT',
 'ALTER TABLE ties_data."NFDeployment" ADD CONSTRAINT "UNIQUE_NFDeployment_REL_ID_NFDEPLOYMENT_SERVES_MANAGEDELEMENT" UNIQUE ("REL_ID_NFDEPLOYMENT_SERVES_MANAGEDELEMENT");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDeployment',
 'FK_NFDeployment_REL_FK_comprised-by-cloudifiedNF',
 'ALTER TABLE ties_data."NFDeployment" ADD CONSTRAINT "FK_NFDeployment_REL_FK_comprised-by-cloudifiedNF" FOREIGN KEY ("REL_FK_comprised-by-cloudifiedNF") REFERENCES ties_data."CloudifiedNF" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NFDeployment',
 'UNIQUE_NFDeployment_REL_ID_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT',
 'ALTER TABLE ties_data."NFDeployment" ADD CONSTRAINT "UNIQUE_NFDeployment_REL_ID_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT" UNIQUE ("REL_ID_CLOUDIFIEDNF_COMPRISES_NFDEPLOYMENT");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NODECLUSTER_LOCATED_AT_CLOUDSITE',
 'FK_NODECLUSTER_LOCATED_AT_CLOUDSITE_aSide_NodeCluster',
 'ALTER TABLE ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ADD CONSTRAINT "FK_NODECLUSTER_LOCATED_AT_CLOUDSITE_aSide_NodeCluster" FOREIGN KEY ("aSide_NodeCluster") REFERENCES ties_data."NodeCluster" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NODECLUSTER_LOCATED_AT_CLOUDSITE',
 'FK_NODECLUSTER_LOCATED_AT_CLOUDSITE_bSide_CloudSite',
 'ALTER TABLE ties_data."NODECLUSTER_LOCATED_AT_CLOUDSITE" ADD CONSTRAINT "FK_NODECLUSTER_LOCATED_AT_CLOUDSITE_bSide_CloudSite" FOREIGN KEY ("bSide_CloudSite") REFERENCES ties_data."CloudSite" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellCU',
 'FK_NRCellCU_REL_FK_provided-by-gnbcucpFunction',
 'ALTER TABLE ties_data."NRCellCU" ADD CONSTRAINT "FK_NRCellCU_REL_FK_provided-by-gnbcucpFunction" FOREIGN KEY ("REL_FK_provided-by-gnbcucpFunction") REFERENCES ties_data."GNBCUCPFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellCU',
 'UNIQUE_NRCellCU_REL_ID_GNBCUCPFUNCTION_PROVIDES_NRCELLCU',
 'ALTER TABLE ties_data."NRCellCU" ADD CONSTRAINT "UNIQUE_NRCellCU_REL_ID_GNBCUCPFUNCTION_PROVIDES_NRCELLCU" UNIQUE ("REL_ID_GNBCUCPFUNCTION_PROVIDES_NRCELLCU");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellDU',
 'FK_NRCellDU_REL_FK_grouped-by-sector',
 'ALTER TABLE ties_data."NRCellDU" ADD CONSTRAINT "FK_NRCellDU_REL_FK_grouped-by-sector" FOREIGN KEY ("REL_FK_grouped-by-sector") REFERENCES ties_data."Sector" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellDU',
 'UNIQUE_NRCellDU_REL_ID_SECTOR_GROUPS_NRCELLDU',
 'ALTER TABLE ties_data."NRCellDU" ADD CONSTRAINT "UNIQUE_NRCellDU_REL_ID_SECTOR_GROUPS_NRCELLDU" UNIQUE ("REL_ID_SECTOR_GROUPS_NRCELLDU");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellDU',
 'FK_NRCellDU_REL_FK_provided-by-gnbduFunction',
 'ALTER TABLE ties_data."NRCellDU" ADD CONSTRAINT "FK_NRCellDU_REL_FK_provided-by-gnbduFunction" FOREIGN KEY ("REL_FK_provided-by-gnbduFunction") REFERENCES ties_data."GNBDUFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRCellDU',
 'UNIQUE_NRCellDU_REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU',
 'ALTER TABLE ties_data."NRCellDU" ADD CONSTRAINT "UNIQUE_NRCellDU_REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU" UNIQUE ("REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'FK_NRSectorCarrier_REL_FK_used-by-nrCellDu',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "FK_NRSectorCarrier_REL_FK_used-by-nrCellDu" FOREIGN KEY ("REL_FK_used-by-nrCellDu") REFERENCES ties_data."NRCellDU" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'UNIQUE_NRSectorCarrier_REL_ID_NRCELLDU_USES_NRSECTORCARRIER',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "UNIQUE_NRSectorCarrier_REL_ID_NRCELLDU_USES_NRSECTORCARRIER" UNIQUE ("REL_ID_NRCELLDU_USES_NRSECTORCARRIER");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'FK_NRSectorCarrier_REL_FK_provided-by-gnbduFunction',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "FK_NRSectorCarrier_REL_FK_provided-by-gnbduFunction" FOREIGN KEY ("REL_FK_provided-by-gnbduFunction") REFERENCES ties_data."GNBDUFunction" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'UNIQUE_872BE05F1989443F2595D99A77BC03733B2D1E2F',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "UNIQUE_872BE05F1989443F2595D99A77BC03733B2D1E2F" UNIQUE ("REL_ID_GNBDUFUNCTION_PROVIDES_NRSECTORCARRIER");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'FK_NRSectorCarrier_REL_FK_used-antennaCapability',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "FK_NRSectorCarrier_REL_FK_used-antennaCapability" FOREIGN KEY ("REL_FK_used-antennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'NRSectorCarrier',
 'UNIQUE_EDF7D5C78EF6505848B1679B714D7831F5863991',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "UNIQUE_EDF7D5C78EF6505848B1679B714D7831F5863991" UNIQUE ("REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY");'
);

SELECT ties_data.create_constraint_if_not_exists(
    'PhysicalNF',
 'FK_PhysicalNF_REL_FK_installed-at-site',
 'ALTER TABLE ties_data."PhysicalNF" ADD CONSTRAINT "FK_PhysicalNF_REL_FK_installed-at-site" FOREIGN KEY ("REL_FK_installed-at-site") REFERENCES ties_data."Site" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
    'PhysicalNF',
 'UNIQUE_PhysicalNF_REL_ID_PHYSICALNF_INSTALLED_AT_SITE',
 'ALTER TABLE ties_data."PhysicalNF" ADD CONSTRAINT "UNIQUE_PhysicalNF_REL_ID_PHYSICALNF_INSTALLED_AT_SITE" UNIQUE ("REL_ID_PHYSICALNF_INSTALLED_AT_SITE");'
);






COMMIT;
