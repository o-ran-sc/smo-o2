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
INSERT INTO ties_model.execution_status("schema", "status") VALUES ('ties_data', 'success');

--missing CD_sourceIds column and its default value
CREATE TABLE IF NOT EXISTS ties_data."AntennaModule" (
	"id"			VARCHAR(511),
	"positionWithinSector"			TEXT,
	"electricalAntennaTilt"			BIGINT,
	"mechanicalAntennaBearing"			BIGINT,
	"antennaBeamWidth"			jsonb,
	"mechanicalAntennaTilt"			BIGINT,
	"antennaModelNumber"			TEXT,
	"totalTilt"			BIGINT,
	"geo-location"	geography
);

SELECT ties_data.create_constraint_if_not_exists(
	'AntennaModule',
 'PK_AntennaModule_id',
 'ALTER TABLE ties_data."AntennaModule" ADD CONSTRAINT "PK_AntennaModule_id" PRIMARY KEY ("id");'
);

--missing eNodeBPlmnId column
CREATE TABLE IF NOT EXISTS ties_data."ENodeBFunction" (
	"id"			VARCHAR(511),
	"eNBId"			BIGINT,
	"CD_sourceIds"			jsonb
);

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "eNBId" SET DEFAULT '11';

ALTER TABLE ONLY ties_data."ENodeBFunction" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

SELECT ties_data.create_constraint_if_not_exists(
	'ENodeBFunction',
 'PK_ENodeBFunction_id',
 'ALTER TABLE ties_data."ENodeBFunction" ADD CONSTRAINT "PK_ENodeBFunction_id" PRIMARY KEY ("id");'
);

--missing "ANTENNACAPABILITY_USED_BY_LTESECTORCARRIER" relationship
CREATE TABLE IF NOT EXISTS ties_data."AntennaCapability" (
	"id"			VARCHAR(511),
	"geranFqBands"			jsonb,
	"nRFqBands"			jsonb,
	"eUtranFqBands"			jsonb,
	"CD_sourceIds"			jsonb
);

ALTER TABLE ONLY ties_data."AntennaCapability" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

SELECT ties_data.create_constraint_if_not_exists(
	'AntennaCapability',
 'PK_AntennaCapability_id',
 'ALTER TABLE ties_data."AntennaCapability" ADD CONSTRAINT "PK_AntennaCapability_id" PRIMARY KEY ("id");'
);

CREATE TABLE IF NOT EXISTS ties_data."LTESectorCarrier" (
	"id"			VARCHAR(511),
	"sectorCarrierType"			TEXT,
	"CD_sourceIds"			jsonb,
	"REL_FK_provided-by-enodebFunction"			VARCHAR(511),
	"REL_ID_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"			VARCHAR(511),
	"REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER"			jsonb
);

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "CD_sourceIds" SET DEFAULT '[]';

ALTER TABLE ONLY ties_data."LTESectorCarrier" ALTER COLUMN "REL_CD_sourceIds_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER" SET DEFAULT '[]';

SELECT ties_data.create_constraint_if_not_exists(
	'LTESectorCarrier',
 'PK_LTESectorCarrier_id',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "PK_LTESectorCarrier_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'LTESectorCarrier',
 'UNIQUE_B9770D6C26DDA0173DB9690F6E3B42C111AF26E9',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "UNIQUE_B9770D6C26DDA0173DB9690F6E3B42C111AF26E9" UNIQUE ("REL_ID_ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'LTESectorCarrier',
 'FK_LTESectorCarrier_REL_FK_provided-by-enodebFunction',
 'ALTER TABLE ties_data."LTESectorCarrier" ADD CONSTRAINT "FK_LTESectorCarrier_REL_FK_provided-by-enodebFunction" FOREIGN KEY ("REL_FK_provided-by-enodebFunction") REFERENCES ties_data."ENodeBFunction" (id) ON DELETE CASCADE;'
);

COMMIT;


