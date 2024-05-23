--
-- ============LICENSE_START=======================================================
-- Copyright (C) 2024 Ericsson
-- Modifications Copyright (C) 2024 OpenInfra Foundation Europe
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- SPDX-License-Identifier: Apache-2.0
-- ============LICENSE_END=========================================================
--

BEGIN;

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

GRANT USAGE ON SCHEMA topology to :pguser;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA topology TO :pguser;
GRANT SELECT ON ALL TABLES IN SCHEMA topology TO :pguser;

CREATE SCHEMA IF NOT EXISTS ties_data;
ALTER SCHEMA ties_data OWNER TO :pguser;
SET default_tablespace = '';
SET default_table_access_method = heap;

SET ROLE :'pguser';

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
INSERT INTO ties_model.entity_info("schema", "status") VALUES ('ties_data', 'success');

CREATE TABLE IF NOT EXISTS ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" (
	"id"			VARCHAR(511),
	"aSide_AntennaCapability"			VARCHAR(511),
	"bSide_AntennaCapability"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" (
	"id"			VARCHAR(511),
	"aSide_AntennaModule"			VARCHAR(511),
	"bSide_AntennaCapability"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" (
	"id"			VARCHAR(511),
	"aSide_AntennaModule"			VARCHAR(511),
	"bSide_AntennaModule"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."AntennaCapability" (
	"id"			VARCHAR(511),
	"geranFqBands"			jsonb,
	"nRFqBands"			jsonb,
	"eUtranFqBands"			jsonb,
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."AntennaModule" (
	"id"			VARCHAR(511),
	"positionWithinSector"			TEXT,
	"electricalAntennaTilt"			BIGINT,
	"mechanicalAntennaBearing"			BIGINT,
	"antennaBeamWidth"			jsonb,
	"mechanicalAntennaTilt"			BIGINT,
	"antennaModelNumber"			TEXT,
	"totalTilt"			BIGINT,
	"geo-location"	geography,
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."AntennaModule" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ENodeBFunction" (
	"id"			VARCHAR(511),
	"eNBId"			BIGINT,
	"eNodeBPlmnId"			jsonb,
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "eNBId" SET DEFAULT '11';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."GNBDUFunction" (
	"id"			VARCHAR(511),
	"dUpLMNId"			jsonb,
	"gNBDUId"			BIGINT,
	"gNBIdLength"			BIGINT,
	"gNBId"			BIGINT,
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb,
);

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."GNBDUFunction" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."LTESectorCarrier" (
	"id"			VARCHAR(511),
	"sectorCarrierType"			TEXT,
	"CD_sourceIds"			jsonb,
	"REL_FK_provided-by-enodebFunction"			VARCHAR(511),
	"REL_ID_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"			VARCHAR(511),
	"REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"			jsonb,
	"REL_FK_used-antennaCapability"			VARCHAR(511),
	"REL_ID_LTESECTORCARRIER_USES_ANTENNACAPABILITY"			VARCHAR(511),
	"REL_CD_sourceIds_LTESECTORCARRIER_USES_ANTENNACAPABILITY"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb,
	"REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" jsonb,
    "REL_CD_classifiers_LTESECTORCARRIER_USES_ANTENNACAPABILITY" jsonb,
    "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" jsonb,
    "REL_CD_decorators_LTESECTORCARRIER_USES_ANTENNACAPABILITY" jsonb
);

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_classifiers_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_classifiers_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_decorators_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_decorators_LTESECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."NRSectorCarrier" (
	"id"			VARCHAR(511),
	"frequencyDL"			BIGINT,
	"arfcnDL"			BIGINT,
	"arfcnUL"			BIGINT,
	"bSChannelBwDL"			BIGINT,
	"frequencyUL"			BIGINT,
	"CD_sourceIds"			jsonb,
	"REL_FK_used-antennaCapability"			VARCHAR(511),
	"REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY"			VARCHAR(511),
	"REL_CD_sourceIds_NRSECTORCARRIER_USES_ANTENNACAPABILITY"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb,
	"REL_CD_classifiers_NRSECTORCARRIER_USES_ANTENNACAPABILITY" jsonb,
    "REL_CD_decorators_NRSECTORCARRIER_USES_ANTENNACAPABILITY" jsonb
);

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_sourceIds_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_classifiers_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."NRSectorCarrier" ALTER COLUMN "REL_CD_decorators_NRSECTORCARRIER_USES_ANTENNACAPABILITY" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" (
	"id"			VARCHAR(511),
	"aSide_AntennaCapability"			VARCHAR(511),
	"bSide_AntennaCapability"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" (
	"id"			VARCHAR(511),
	"aSide_AntennaCapability"			VARCHAR(511),
	"bSide_AntennaCapability"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

CREATE TABLE IF NOT EXISTS ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" (
	"id"			VARCHAR(511),
	"aSide_AntennaCapability"			VARCHAR(511),
	"bSide_AntennaCapability"			VARCHAR(511),
	"CD_sourceIds"			jsonb,
	"CD_classifiers"			jsonb,
	"CD_decorators"			jsonb
);

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ALTER COLUMN "CD_classifiers" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ALTER COLUMN "CD_decorators" SET DEFAULT '{}';

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY',
 'PK_ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNAMODULE_SERVES_ANTENNACAPABILITY',
 'PK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNAMODULE_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNAMODULE_SERVES_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNAMODULE_USES_ANTENNAMODULE',
 'PK_ANTENNAMODULE_USES_ANTENNAMODULE_id',
 'ALTER TABLE ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ADD CONSTRAINT "PK_ANTENNAMODULE_USES_ANTENNAMODULE_id" PRIMARY KEY ("id");'
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
	'ENodeBFunction',
 'PK_ENodeBFunction_id',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "PK_ENodeBFunction_id" PRIMARY KEY ("id");'
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
	'NRSectorCarrier',
 'PK_NRSectorCarrier_id',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "PK_NRSectorCarrier_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY',
 'PK_ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY',
 'PK_ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_USES_ANTENNACAPABILITY',
 'PK_ANTENNACAPABILITY_USES_ANTENNACAPABILITY_id',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ADD CONSTRAINT "PK_ANTENNACAPABILITY_USES_ANTENNACAPABILITY_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY',
 'FK_59989F20BF725E08F0327C7C555B1CF7B4F8661C',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ADD CONSTRAINT "FK_59989F20BF725E08F0327C7C555B1CF7B4F8661C" FOREIGN KEY ("aSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY',
 'FK_8B87C4FA55D9F0A4164C35942D91E2E4E0B6140D',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_REALISED_BY_ANTENNACAPABILITY" ADD CONSTRAINT "FK_8B87C4FA55D9F0A4164C35942D91E2E4E0B6140D" FOREIGN KEY ("bSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY',
 'FK_F407B43F3D94FFE0CBDF7E2BFAEAD9A4047DF252',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_F407B43F3D94FFE0CBDF7E2BFAEAD9A4047DF252" FOREIGN KEY ("aSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY',
 'FK_73581B1E05EA3AC4C029F090C067BD88029F1195',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_PROVIDES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_73581B1E05EA3AC4C029F090C067BD88029F1195" FOREIGN KEY ("bSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY',
 'FK_3613A8C84275763DE5294B04DB6E6BF74E9412F4',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_3613A8C84275763DE5294B04DB6E6BF74E9412F4" FOREIGN KEY ("aSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY',
 'FK_C21710428B73A144CC8DE4063CB24D4852D2EFE1',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_SERVES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_C21710428B73A144CC8DE4063CB24D4852D2EFE1" FOREIGN KEY ("bSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_USES_ANTENNACAPABILITY',
 'FK_0F27D391D210980029D4A879344BFC1DF7E56047',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_0F27D391D210980029D4A879344BFC1DF7E56047" FOREIGN KEY ("aSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNACAPABILITY_USES_ANTENNACAPABILITY',
 'FK_C66086ACCFA9455ED438AE9381A7E4B0668EA027',
 'ALTER TABLE ties_data."ANTENNACAPABILITY_USES_ANTENNACAPABILITY" ADD CONSTRAINT "FK_C66086ACCFA9455ED438AE9381A7E4B0668EA027" FOREIGN KEY ("bSide_AntennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
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
	'ANTENNAMODULE_USES_ANTENNAMODULE',
 'FK_ANTENNAMODULE_USES_ANTENNAMODULE_aSide_AntennaModule',
 'ALTER TABLE ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ADD CONSTRAINT "FK_ANTENNAMODULE_USES_ANTENNAMODULE_aSide_AntennaModule" FOREIGN KEY ("aSide_AntennaModule") REFERENCES ties_data."AntennaModule" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'ANTENNAMODULE_USES_ANTENNAMODULE',
 'FK_ANTENNAMODULE_USES_ANTENNAMODULE_bSide_AntennaModule',
 'ALTER TABLE ties_data."ANTENNAMODULE_USES_ANTENNAMODULE" ADD CONSTRAINT "FK_ANTENNAMODULE_USES_ANTENNAMODULE_bSide_AntennaModule" FOREIGN KEY ("bSide_AntennaModule") REFERENCES ties_data."AntennaModule" (id) ON DELETE CASCADE;'
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
	'NRSectorCarrier',
 'FK_NRSectorCarrier_REL_FK_used-antennaCapability',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "FK_NRSectorCarrier_REL_FK_used-antennaCapability" FOREIGN KEY ("REL_FK_used-antennaCapability") REFERENCES ties_data."AntennaCapability" (id) ON DELETE CASCADE;'
);

SELECT ties_data.create_constraint_if_not_exists(
	'NRSectorCarrier',
 'UNIQUE_EDF7D5C78EF6505848B1679B714D7831F5863991',
 'ALTER TABLE ties_data."NRSectorCarrier" ADD CONSTRAINT "UNIQUE_EDF7D5C78EF6505848B1679B714D7831F5863991" UNIQUE ("REL_ID_NRSECTORCARRIER_USES_ANTENNACAPABILITY");'
);

COMMIT;