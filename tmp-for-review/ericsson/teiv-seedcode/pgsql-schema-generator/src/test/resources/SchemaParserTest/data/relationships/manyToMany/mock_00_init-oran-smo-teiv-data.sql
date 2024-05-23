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

CREATE TABLE IF NOT EXISTS ties_data."Sector" (
	"id"			 VARCHAR(511),
	"azimuth"			DECIMAL,
	"sectorId"			 jsonb,
	"geo-location"			"geography"
);

CREATE TABLE IF NOT EXISTS ties_data."Namespace" (
	"id"			 VARCHAR(511),
	"name"			TEXT
);

CREATE TABLE IF NOT EXISTS ties_data."REL_serviced-sector_serving-namespace" (
	"id"			VARCHAR(511),
	"aSide_Sector"			VARCHAR(511),
	"bSide_Namespace"			VARCHAR(511)
);

SELECT ties_data.create_constraint_if_not_exists(
	'Sector',
 'PK_Sector_id',
 'ALTER TABLE ties_data."Sector" ADD CONSTRAINT "PK_Sector_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'Namespace',
 'PK_Namespace_id',
 'ALTER TABLE ties_data."Namespace" ADD CONSTRAINT "PK_Sector_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'REL_serviced-sector_serving-namespace',
 'PK_REL_serviced-sector_serving-namespace_id',
 'ALTER TABLE ties_data."REL_serviced-sector_serving-namespace" ADD CONSTRAINT "PK_REL_serviced-sector_serving-namespace_id" PRIMARY KEY ("id");'
);

SELECT ties_data.create_constraint_if_not_exists(
	'REL_serviced-sector_serving-namespace',
 'FK_REL_serviced-sector_serving-namespace_aSide_Sector',
 'ALTER TABLE ties_data."REL_serviced-sector_serving-namespace" ADD CONSTRAINT FK_REL_serviced-sector_serving-namespace_aSide_Sector FOREIGN KEY ("aSide_Sector") REFERENCES ties_data."Sector" (id) ON DELETE CASCADE;')

SELECT ties_data.create_constraint_if_not_exists(
	'REL_serviced-sector_serving-namespace',
 'FK_REL_serviced-sector_serving-namespace_bSide_Namespace',
 'ALTER TABLE ties_data."REL_serviced-sector_serving-namespace" ADD CONSTRAINT FK_REL_serviced-sector_serving-namespace_bSide_Namespace FOREIGN KEY ("bSide_Namespace") REFERENCES ties_data."Namespace" (id) ON DELETE CASCADE;')

COMMIT;
