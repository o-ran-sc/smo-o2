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

DROP SCHEMA IF EXISTS ties_model cascade;
CREATE SCHEMA IF NOT EXISTS ties_model;
ALTER SCHEMA ties_model OWNER TO :pguser;
SET default_tablespace = '';
SET default_table_access_method = heap;

SET ROLE :'pguser';

CREATE TABLE IF NOT EXISTS ties_model.execution_status (
    "schema"                 VARCHAR(127) PRIMARY KEY,
    "status"          VARCHAR(127)
);

CREATE TABLE IF NOT EXISTS ties_model.hash_info (
    "name"                 VARCHAR(511) PRIMARY KEY,
    "hashedValue"          VARCHAR(511),
    "type"                 VARCHAR(511)
);

CREATE TABLE IF NOT EXISTS ties_model.module_reference (
    "name"                   VARCHAR(511) PRIMARY KEY,
    "namespace"              VARCHAR(511),
    "domain"             VARCHAR(511),
    "includedModules"        jsonb,
    "revision"       VARCHAR(511),
    "content"               TEXT,
    "ownerAppId"       VARCHAR(511),
    "status"       VARCHAR(127)
);

CREATE TABLE IF NOT EXISTS ties_model.entity_info (
    "name"                   VARCHAR(511) PRIMARY KEY,
    "moduleReferenceName"    VARCHAR(511),
    FOREIGN KEY ("moduleReferenceName") REFERENCES ties_model.module_reference ("name") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ties_model.relationship_info (
    "name"      VARCHAR(511) PRIMARY KEY,
    "aSideAssociationName"    TEXT,
    "aSideMOType"             TEXT,
    "aSideMinCardinality"     BIGINT,
    "aSideMaxCardinality"     BIGINT,
    "bSideAssociationName"    TEXT,
    "bSideMOType"             TEXT,
    "bSideMinCardinality"     BIGINT,
    "bSideMaxCardinality"     BIGINT,
    "associationKind"    TEXT,
    "relationshipDataLocation"          TEXT,
    "connectSameEntity"       BOOLEAN,
    "moduleReferenceName"     TEXT,
    FOREIGN KEY ("moduleReferenceName") REFERENCES ties_model.module_reference ("name") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ties_model.decorators (
    "name"                   VARCHAR(511) PRIMARY KEY,
    "dataType"                   VARCHAR(511),
    "moduleReferenceName"    VARCHAR(511),
    FOREIGN KEY ("moduleReferenceName") REFERENCES ties_model.module_reference ("name") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ties_model.classifiers (
    "name"                   VARCHAR(511) PRIMARY KEY,
    "moduleReferenceName"    VARCHAR(511),
    FOREIGN KEY ("moduleReferenceName") REFERENCES ties_model.module_reference ("name") ON DELETE CASCADE
);

-- Update model schema exec status
INSERT INTO ties_model.execution_status("schema", "status") VALUES ('ties_model', 'success');

COPY ties_model.hash_info("name", "hashedValue", "type") FROM stdin;
\.

COPY ties_model.module_reference("name", "namespace", "domain", "includedModules", "revision", "content", "ownerAppId", "status") FROM stdin;
\.

COPY ties_model.entity_info("name", "moduleReferenceName") FROM stdin;
\.

COPY ties_model.relationship_info("name", "aSideAssociationName", "aSideMOType", "aSideMinCardinality", "aSideMaxCardinality", "bSideAssociationName", "bSideMOType", "bSideMinCardinality", "bSideMaxCardinality", "associationKind", "relationshipDataLocation", "connectSameEntity", "moduleReferenceName") FROM stdin;
ENODEBFUNCTION_PROVIDES_LTESECTORCARRIER	provided-lteSectorCarrier	ENodeBFunction	1	1	provided-by-enodebFunction	LTESectorCarrier	0	100	BI_DIRECTIONAL	B_SIDE	false	o-ran-smo-teiv-ran
LTESECTORCARRIER_USES_ANTENNACAPABILITY	used-antennaCapability	LTESectorCarrier	0	1	used-by-lteSectorCarrier	AntennaCapability	0	1	BI_DIRECTIONAL	A_SIDE	false	o-ran-smo-teiv-ran
\.

;

COMMIT;