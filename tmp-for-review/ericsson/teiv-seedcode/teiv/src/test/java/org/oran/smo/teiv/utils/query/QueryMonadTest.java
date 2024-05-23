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
package org.oran.smo.teiv.utils.query;

import org.oran.smo.teiv.exposure.utils.PaginationDTO;
import org.oran.smo.teiv.schema.SchemaLoaderException;
import org.oran.smo.teiv.schema.MockSchemaLoader;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.oran.smo.teiv.schema.SchemaRegistry.*;
import static org.oran.smo.teiv.utils.TiesConstants.*;
import static org.oran.smo.teiv.utils.query.QueryMonadTestUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;

class QueryMonadTest {
    private static DSLContext context;

    @BeforeAll
    public static void setUp() throws SchemaLoaderException {
        MockSchemaLoader mockSchemaLoader = new MockSchemaLoader();
        mockSchemaLoader.loadSchemaRegistry();
        context = DSL.using(new MockConnection(m -> new MockResult[1]));
    }

    @Test
    void test1i_noAttributesNorFieldsGood() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String attributes = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, attributes, relationships);

        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test2i_attributesGood() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes";
        String attributes = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, attributes, relationships);
        Select<?> builtWithQM = (Select<?>) underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(
                context);
        Select<?> reference = (Select<?>) createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")), field(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")).as(String.format(TIES_DATA,
                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")), field(String.format(
                                                TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                                                        "dUpLMNId")).as(String.format(TIES_DATA,
                                                                "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                                                                        "dUpLMNId")), field(String.format(TIES_DATA,
                                                                                "GNBDUFunction") + "." + String.format(
                                                                                        QUOTED_STRING, "gNBDUId")).as(String
                                                                                                .format(TIES_DATA,
                                                                                                        "GNBDUFunction") + "." + String
                                                                                                                .format(QUOTED_STRING,
                                                                                                                        "gNBDUId")),
                field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBDUId")).as(String
                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBDUId")), field(String
                                .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBIdLength")).as(
                                        String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                                                "gNBIdLength")), field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "cmId")).as(
                                                                String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                                                        .format(QUOTED_STRING, "cmId"))).from(String.format(
                                                                                TIES_DATA, "GNBDUFunction")));

        List<Field<?>> fieldsActual = builtWithQM.getSelect();
        List<Field<?>> fieldsExpected = reference.getSelect();
        Assertions.assertTrue(fieldsActual.containsAll(fieldsExpected));
    }

    @Test
    void test2ii_attributesTypoException() {
        String entityType = "GNBDUFunction";
        String fields = "/attribute/fdn; /attributes/id; /attributes/gNBId";
        String filter = "/attributes[contains(@fdn, \"93\")]  |  /attributes[@gNBId=4000259]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test2iii_attributesMissingSlashException() {
        String entityType = "GNBDUFunction";
        String fields = "attributes/fdn; /attributes/id; /attributes/gNBId";
        String filter = "/attributes[contains(@fdn, \"93\")]  |  /attributes[@gNBId=4000259]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test3i_attributesFdnGood() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes(fdn)";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")), field(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, ID_COLUMN_NAME)).as(String.format(
                                        TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, ID_COLUMN_NAME)))
                .from(String.format(TIES_DATA, "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test3ii_attributesFdnGood() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes/fdn";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")), field(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, ID_COLUMN_NAME)).as(String.format(
                                        TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, ID_COLUMN_NAME)))
                .from(String.format(TIES_DATA, "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test3iii_attributesFdnMissingSlashException() {
        String entityType = "GNBDUFunction";
        String fields = "/attributefdn";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    //should be empty response
    @Test
    void test3iv_attributesFdnTypoColumm() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes/fnd";
        String attributes = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, attributes, relationships);
        Select<?> builtWithQM = (Select<?>) underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(
                context);
        Select<?> reference = (Select<?>) createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + ".id").as(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                        "id")), field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn"))
                                .as(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")),
                field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "dUpLMNId")).as(String
                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "dUpLMNId")), field(String
                                .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBDUId")).as(
                                        String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                                                "gNBDUId")), field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                                        .format(QUOTED_STRING, "gNBId")).as(String.format(TIES_DATA,
                                                                "GNBDUFunction") + "." + String.format(QUOTED_STRING,
                                                                        "gNBId")), field(String.format(TIES_DATA,
                                                                                "GNBDUFunction") + "." + String.format(
                                                                                        QUOTED_STRING, "gNBIdLength")).as(
                                                                                                String.format(TIES_DATA,
                                                                                                        "GNBDUFunction") + "." + String
                                                                                                                .format(QUOTED_STRING,
                                                                                                                        "gNBIdLength")),
                field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "cmId")).as(String
                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "cmId"))).from(String
                                .format(TIES_DATA, "GNBDUFunction")));

        List<Field<?>> fieldsActual = builtWithQM.getSelect();
        List<Field<?>> fieldsExpected = reference.getSelect();
        Assertions.assertTrue(fieldsActual.containsAll(fieldsExpected));
    }

    @Test
    void test3v_attributesFdnMissingOpeningBracketException() {
        String entityType = "GNBDUFunction";
        String fields = "/attributefdn)";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test3vi_attributesFdnMissingClosingBracketException() {
        String entityType = "GNBDUFunction";
        String fields = "/attribute(fdn";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test4i_attributesFdnIdGood() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes(fdn)";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")), field(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(
                                                TIES_DATA, "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test4ii_attributesFdnIdGood() {
        String entityType = "GNBDUFunction";
        String fields = "/attributes/fdn";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "fdn")), field(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(
                                                TIES_DATA, "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test4iii_attributesFdnIdMissingCommaException() {
        String entityType = "GNBDUFunction";
        String fields = "/attribute(fdn id)";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test4iv_attributesFdnIdMissingSemiColonException() {
        String entityType = "GNBDUFunction";
        String fields = "/attribute/fdn /attribute/id";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test4v_attributesFdnIdMissingSecondSlashException() {
        String entityType = "GNBDUFunction";
        String fields = "/attribute/fdn; attribute/id";
        String filter = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test5i_attributesContainsGood() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String attributes = "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, fields, attributes, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).where(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                        .format(QUOTED_STRING, "fdn")).contains("/SubNetwork=Ireland/")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/attributescontains (@fdn, \"/SubNetwork=Ireland/\")]",
            "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\")",
            "/attributes[contains (fdn, \"/SubNetwork=Ireland/\")]",
            "/attributes[contans (@fdn, \"/SubNetwork=Ireland/\")]",
            "/attributes[contains @fdn, \"/SubNetwork=Ireland/\")]",
            "/attributes[contains (@fdn, \"/SubNetwork=Ireland/\"]",
            "/attributes[contains (@fdn \"/SubNetwork=Ireland/\")]", "/attributes[contains (@fdn, /SubNetwork=Ireland/\")]",
            "/attributes[contains (@fdn, \"/SubNetwork=Ireland/)]" })
    void test5ii_ix_attributesException(String scope) {
        String entityType = "GNBDUFunction";
        String target = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test5x_attributesColumnTypoEmptyList() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[contains (@fnd, \"/SubNetwork=Ireland/\")]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test5xi_attributesContainsWrongValueEmptyList() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[contains (@fdn, \"/SubNetwork Ireland/\")]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).where(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                        .format(QUOTED_STRING, "fdn")).contains("/SubNetwork Ireland/")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test6i_attributesEqualsGood() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[@gNBIdLength=3]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).where(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                        .format(QUOTED_STRING, "gNBIdLength")).eq(3)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test6iii_attributesMissingEquationSignException() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[@gNBIdLength 3]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test7i_attributesEqualsGood() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String attributes = "/attributes[@gNBIdLength=3 and @gNBId=111]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, fields, attributes, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).where((field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                        .format(QUOTED_STRING, "gNBIdLength")).eq(3)).and(field(String.format(TIES_DATA,
                                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBId")).eq(111))));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test7iii_attributesMissingAndException() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String filter = "/attributes[@gNBIdLength=3  @gNBId=-1]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test7iv_attributesTypoAndException() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String filter = "/attributes[@gNBIdLength=3 adn @gNBId=111]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test8i_attributesEqualBarEqualGood() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String attributes = "/attributes[@gNBIdLength=3 and @gNBId=111] | /attributes[@gNBIdLength=3 and @gNBId=112]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, fields, attributes, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).where(((field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                        .format(QUOTED_STRING, "gNBIdLength")).eq(3)).and(field(String.format(TIES_DATA,
                                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "gNBId")).eq(111)))
                                                        .or((field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                                                                .format(QUOTED_STRING, "gNBIdLength")).eq(3)).and(field(
                                                                        String.format(TIES_DATA,
                                                                                "GNBDUFunction") + "." + String.format(
                                                                                        QUOTED_STRING, "gNBId")).eq(
                                                                                                112)))));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test8ii_attributesEqualBarEqualMissingBarException() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String filter = "/attributes[@gNBIdLength=3 and @gNBId=111]  /attributes[@gNBIdLength=3 and @gNBId=112]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test9i_attributesContainEqualGood() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String attributes = "/attributes[contains (@fdn, \"SubNetwork=Ireland\")] ; /attributes[@gNBIdLength=3]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, fields, attributes, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test9ii_attributesMissingSemiColonException() {
        String entityType = "GNBDUFunction";
        String fields = "";
        String filter = "/attributes[contains (@fdn, \"SubNetwork=Ireland\")]  /attributes[@gNBIdLength=3]";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, fields, filter, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test10i_targetGood() {
        String entityType = "GNBDUFunction";
        String target = "/NRCellDU";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("NRCellDU")
                .getField("id", null, null).getType()).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(
                                TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                        "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id")).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(TIES_DATA,
                                        "NRCellDU")).on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String
                                                .format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String
                                                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                QUOTED_STRING, "id"))))).where(joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test10ii_targetTypoGood() {
        String entityType = "GNBDUFunction";
        String target = "/NRCelldu";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test11i_targetHopsGood() {
        String entityType = "GNBDUFunction";
        String target = "/NRCellDU ; /NRSectorCarrier";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull()).and(field(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(
                        QUOTED_STRING, "id")).isNotNull());
        // spotless:off
        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("NRCellDU").getField("id",null,null).getType()).as(String.format(
                TIES_DATA,
                "NRCellDU") + "." + String.format(QUOTED_STRING, "id")), field("null").cast(getEntityTypeByName("NRSectorCarrier").getField("id",null,null).getType()).as(String.format(
                TIES_DATA,
                "NRSectorCarrier") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                "GNBDUFunction")).
                leftJoin(String.format(TIES_DATA, "NRCellDU")).on(condition(field(String
                .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
            .leftJoin(String.format(TIES_DATA, "NRSectorCarrier")).on(condition(field(String.format(TIES_DATA,
                "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))).where(joinFilter)
            .union(context.select(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                    "id")).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING, "id")), field(
                    "null").cast(getEntityTypeByName("NRSectorCarrier").getField("id",null,null).getType()).as(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                    "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).
                    leftJoin(String.format(TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                    "NRCellDU") + "." + String.format(QUOTED_STRING,
                    "REL_FK_provided-by-gnbduFunction")).eq(field(String
                    .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))).
                    leftJoin(String.format(TIES_DATA, "NRSectorCarrier")).on(condition(field(String.format(TIES_DATA,
                    "NRSectorCarrier") + "." + String.format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(
                    String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter)).union(context.select(field("null").cast(getEntityTypeByName("NRCellDU").getField("id",null,null).getType()).as(String.format(
                    TIES_DATA,
                    "NRCellDU") + "." + String.format(QUOTED_STRING, "id")), field(String.format(TIES_DATA,
                    "NRSectorCarrier") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(QUOTED_STRING, "id")))
                .from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(TIES_DATA,
                    "NRCellDU")).on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String
                    .format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction")
                    + "." + String.format(QUOTED_STRING, "id"))))).leftJoin(String.format(TIES_DATA,
                    "NRSectorCarrier")).on(condition(field(String.format(TIES_DATA,
                    "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                    "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." +
                    String.format(QUOTED_STRING, "id"))))).where(joinFilter)));
        // spotless:on
        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test11ii_targetHopTypo() {
        String entityType = "GNBDUFunction";
        String target = "/NRCellDU ; /NRSectorCarier";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("NRCellDU")
                .getField("id", null, null).getType()).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(
                                TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                        "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id")).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(TIES_DATA,
                                        "NRCellDU")).on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String
                                                .format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String
                                                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                QUOTED_STRING, "id"))))).where(joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test12i_TargetGoodWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU";
        String target = "";
        String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Condition relationFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).isNotNull()).and(field(String.format(TIES_DATA,
                        "NRCellDU") + "." + String.format(QUOTED_STRING, "id")).isNotNull()).and(field(String.format(
                                TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                        "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String.format(
                                        TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))
                .where(relationFilter).and(joinFilter).union(context.select(field("null").cast(getEntityTypeByName(
                        "GNBDUFunction").getField("id", null, null).getType()).as(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                        "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String
                                                .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(
                                                                TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                        QUOTED_STRING, "id")))).where(relationFilter).and(
                                                                                joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test12ii_relationshipWrongException() {
        String entityType = "GNBDUFunction";
        String target = "/NRCellDU";
        String scope = "";
        String relationships = "USES";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Invalid relationship name");
    }

    @Test
    void test13i_RelationshipsGoodWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU | /CloudNativeApplication";
        String target = "";
        String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU, GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull()).and(field(String.format(TIES_DATA, "CloudNativeApplication") + "." + String.format(
                        QUOTED_STRING, "id")).isNotNull());

        Condition relationFilter1 = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).isNotNull()).and(field(String.format(TIES_DATA,
                        "NRCellDU") + "." + String.format(QUOTED_STRING, "id")).isNotNull()).and(field(String.format(
                                TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                        "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU")).isNotNull());

        Condition relationFilter2 = condition(field(String.format(TIES_DATA,
                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(QUOTED_STRING,
                        "aSide_GNBDUFunction")).isNotNull()).and(field(String.format(TIES_DATA,
                                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(QUOTED_STRING,
                                        "bSide_CloudNativeApplication")).isNotNull()).and(field(String.format(TIES_DATA,
                                                "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(
                                                        QUOTED_STRING, "id")).isNotNull());

        // spotless:off
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction"))
                .eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))).
                join(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION"))
            .on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String
                .format(QUOTED_STRING, "aSide_GNBDUFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." +
                String.format(QUOTED_STRING, "id")))).leftJoin(String.format(TIES_DATA, "CloudNativeApplication"))
            .on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(
                QUOTED_STRING, "bSide_CloudNativeApplication")).eq(field(String.format(TIES_DATA, "CloudNativeApplication") + "." + String.format(QUOTED_STRING, "id"))))

            .where(relationFilter1).and(relationFilter2).and(joinFilter).union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField("id",null,null).getType()).as(String
                    .format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).
                    leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String.format(TIES_DATA,
                    "NRCellDU") + "." + String.format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))
                .join(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION"))
                .on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." +
                    String.format(QUOTED_STRING, "aSide_GNBDUFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                    .format(QUOTED_STRING, "id")))).leftJoin(String.format(TIES_DATA,
                    "CloudNativeApplication"))
                .on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String
                    .format(QUOTED_STRING, "bSide_CloudNativeApplication")).eq(field(String.format(TIES_DATA,
                    "CloudNativeApplication") + "." + String
                    .format(QUOTED_STRING, "id"))))

                .where(relationFilter1).and(relationFilter2).and(joinFilter)).union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField("id",null,null).getType()).as(String.format(
                    TIES_DATA, "GNBDUFunction") +
                    "." + String.format(QUOTED_STRING, "id")))
                .from(String.format(TIES_DATA, "GNBDUFunction"))
                .leftJoin(String.format(TIES_DATA, "NRCellDU")).
                    on(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction"))
                    .eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))
                .join(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION"))
                .on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String.format(QUOTED_STRING, "aSide_GNBDUFunction"))
                    .eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))
                .leftJoin(String.format(TIES_DATA, "CloudNativeApplication")).on(field(String.format(TIES_DATA, "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION") + "." + String
                    .format(QUOTED_STRING, "bSide_CloudNativeApplication"))
                    .eq(field(String.format(TIES_DATA, "CloudNativeApplication") + "." + String.format(QUOTED_STRING, "id"))))
                .where(relationFilter1).and(relationFilter2).and(joinFilter)));
        // spotless:on
        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test13ii_RelationshipsOnlyOneGoodWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU | /CloudNativeApplication";
        String target = "";
        String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());
        Condition relationFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).isNotNull()).and(field(String.format(TIES_DATA,
                        "NRCellDU") + "." + String.format(QUOTED_STRING, "id")).isNotNull()).and(field(String.format(
                                TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                        "REL_ID_GNBDUFUNCTION_PROVIDES_NRCELLDU")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String.format(
                                        TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))))
                .where(relationFilter).and(joinFilter).union(context.select(field("null").cast(getEntityTypeByName(
                        "GNBDUFunction").getField("id", null, null).getType()).as(String.format(TIES_DATA,
                                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                        "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(field(String
                                                .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(
                                                                TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                        QUOTED_STRING, "id")))).where(relationFilter).and(
                                                                                joinFilter)).union(context.select(field(
                                                                                        "null").cast(getEntityTypeByName(
                                                                                                "GNBDUFunction").getField(
                                                                                                        "id", null, null)
                                                                                                        .getType()).as(
                                                                                                                String.format(
                                                                                                                        TIES_DATA,
                                                                                                                        "GNBDUFunction") + "." + String
                                                                                                                                .format(QUOTED_STRING,
                                                                                                                                        "id")))
                                                                                        .from(String.format(TIES_DATA,
                                                                                                "GNBDUFunction")).leftJoin(
                                                                                                        String.format(
                                                                                                                TIES_DATA,
                                                                                                                "NRCellDU"))
                                                                                        .on(field(String.format(TIES_DATA,
                                                                                                "NRCellDU") + "." + String
                                                                                                        .format(QUOTED_STRING,
                                                                                                                "REL_FK_provided-by-gnbduFunction"))
                                                                                                                        .eq(field(
                                                                                                                                String.format(
                                                                                                                                        TIES_DATA,
                                                                                                                                        "GNBDUFunction") + "." + String
                                                                                                                                                .format(QUOTED_STRING,
                                                                                                                                                        "id"))))
                                                                                        .where(relationFilter).and(
                                                                                                joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test13iii_RelationshipsInvalidWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU | /CloudNativeApplication";
        String target = "";
        String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU_2";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Invalid relationship name");
    }

    @Test
    void test13iv_RelationshipsInvalidWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU | /CloudNativeApplication";
        String target = "";
        String relationships = "GNBDUFUNCTION_PROVIDES_NRCELLDU GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Invalid relationship name");
    }

    @Test
    void test14i_ScopeGoodWithHop() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(condition(field(String
                                        .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField(
                        "id", null, null).getType()).as(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String
                                        .format(TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                                "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(
                                                                TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                        QUOTED_STRING, "id"))))).where(joinFilter)));
        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test14ii_ScopeInvalidWithHop() {
        String entityType = "GNBDUFunction";
        String scope = "/NRCellDU_2";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test14iii_ScopeInvalidWithHop() {
        String entityType = "GNBDUFunction";
        String scope = "/AntennaCapability";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test15i_FilterOrGoodWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/NRSectorCarrier | /NRCellDU";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(
                QUOTED_STRING, "id")).isNotNull()).and(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id")).isNotNull());
        // spotless:off
        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))
            .from(String.format(TIES_DATA, "GNBDUFunction"))
            .leftJoin(String.format(TIES_DATA, "NRSectorCarrier"))
            .on(condition(field(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." +
                String.format(QUOTED_STRING, "id")))))
            .leftJoin(String.format(TIES_DATA, "NRCellDU"))
            .on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
            .where(joinFilter)
            .union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField("id",null,null).getType()).as(String.format(
                    TIES_DATA, "GNBDUFunction") + "." +
                    String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction"))
                .leftJoin(String.format(TIES_DATA, "NRSectorCarrier"))
                .on(condition(field(String.format(TIES_DATA, "NRSectorCarrier") + "." + String.format(
                    QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .leftJoin(String.format(TIES_DATA, "NRCellDU"))
                .on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                    "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") +
                    "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter))
            .union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField("id",null,null).getType()).as(String.format(
                    TIES_DATA, "GNBDUFunction") + "." +
                    String.format(QUOTED_STRING, "id")))
                .from(String.format(TIES_DATA, "GNBDUFunction"))
                .leftJoin(String.format(TIES_DATA, "NRSectorCarrier"))
                .on(condition(field(String.format(TIES_DATA, "NRSectorCarrier") + "." +
                    String.format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(
                    String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .leftJoin(String.format(TIES_DATA, "NRCellDU"))
                .on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                    "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA, "GNBDUFunction") + "." + String
                    .format(QUOTED_STRING, "id")))))
                .where(joinFilter)));
        // spotless:on
        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test15ii_ScopeOrOneGoodWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/AntennaCapability | /NRCellDU";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(condition(field(String
                                        .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField(
                        "id", null, null).getType()).as(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String
                                        .format(TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                                "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(
                                                                TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                        QUOTED_STRING, "id"))))).where(joinFilter)));
        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test15iii_ScopeTypoWithHops() {
        String entityType = "GNBDUFunction";
        String scope = "/AntennaCapability2 | /NRCellDU";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")).leftJoin(String.format(TIES_DATA, "NRCellDU")).on(condition(field(String
                                        .format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field("null").cast(getEntityTypeByName("GNBDUFunction").getField(
                        "id", null, null).getType()).as(String.format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String
                                        .format(TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                                "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(
                                                                TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                        QUOTED_STRING, "id"))))).where(joinFilter)));
        Assertions.assertEquals(reference, builtWithQM);
    }

    @ParameterizedTest
    @ValueSource(strings = { "//attributes", "///attributes" })
    void test16i_targetSlashes(String target) {
        String entityType = "GNBDUFunction";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @ParameterizedTest
    @ValueSource(strings = { "//attributes", "///attributes" })
    void test16i_scopeSlashes(String scope) {
        String entityType = "GNBDUFunction";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @ParameterizedTest
    @ValueSource(strings = { "/attributes[@gNBIdLength>3]", "/attributes[@gNBIdLength<3]" })
    void test17i_scopeConditions(String scope) {
        String entityType = "GNBDUFunction";
        String target = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test18i_scopeOr() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attribute[@gNBIdLength=3 or @gNBIdLength=4]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test19i_targetWrongColumn() {
        String entityType = "GNBDUFunction";
        String target = "/attribute(gnbidLength)";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test19i_scopeWrongColumn() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attribute[@gnbiLength=3]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test20i_wrongTargetEntity() {
        String entityType = "GNBDUFunction";
        String target = "/AntennaCapability";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Objects are not related");
    }

    @Test
    void test21_manyToOneTest() {
        String entityType = "NRSectorCarrier";
        String target = "/AntennaCapability";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "AntennaCapability") + "." + String.format(
                QUOTED_STRING, "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("NRCellDU")
                .getField("id", null, null).getType()).as(String.format(TIES_DATA, "AntennaCapability") + "." + String
                        .format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "NRSectorCarrier")).leftJoin(String
                                .format(TIES_DATA, "AntennaCapability")).on(condition(field(String.format(TIES_DATA,
                                        "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_used-antennaCapability")).eq(field(String.format(TIES_DATA,
                                                        "AntennaCapability") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field(String.format(TIES_DATA, "AntennaCapability") + "." + String
                        .format(QUOTED_STRING, "id")).as(String.format(TIES_DATA, "AntennaCapability") + "." + String
                                .format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "NRSectorCarrier")).leftJoin(
                                        String.format(TIES_DATA, "AntennaCapability")).on(condition(field(String.format(
                                                TIES_DATA, "NRSectorCarrier") + "." + String.format(QUOTED_STRING,
                                                        "REL_FK_used-antennaCapability")).eq(field(String.format(TIES_DATA,
                                                                "AntennaCapability") + "." + String.format(QUOTED_STRING,
                                                                        "id"))))).where(joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test22i_processTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/id/fdn";
        String scope = "/attributes[@gNBId=3000696]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test22ii_processTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/fdn";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test22iii_processTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/dummyText/fdn";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test23i_processTargetAttribute() {
        String entityType = "GNBDUFunction";
        String target = "/id";
        String scope = "";
        String relationships = "";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "GNBDUFunction")));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test23ii_processTargetAttribute() {
        String entityType = "GNBDUFunction";
        String target = "/NRCellDU/id";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);
        Query builtWithQM = qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("NRCellDU")
                .getField("id", null, null).getType()).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(
                                TIES_DATA, "NRCellDU")).on(condition(field(String.format(TIES_DATA,
                                        "NRCellDU") + "." + String.format(QUOTED_STRING,
                                                "REL_FK_provided-by-gnbduFunction")).eq(field(String.format(TIES_DATA,
                                                        "GNBDUFunction") + "." + String.format(QUOTED_STRING, "id")))))
                .where(joinFilter).union(context.select(field(String.format(TIES_DATA, "NRCellDU") + "." + String.format(
                        QUOTED_STRING, "id")).as(String.format(TIES_DATA, "NRCellDU") + "." + String.format(QUOTED_STRING,
                                "id"))).from(String.format(TIES_DATA, "GNBDUFunction")).leftJoin(String.format(TIES_DATA,
                                        "NRCellDU")).on(condition(field(String.format(TIES_DATA, "NRCellDU") + "." + String
                                                .format(QUOTED_STRING, "REL_FK_provided-by-gnbduFunction")).eq(field(String
                                                        .format(TIES_DATA, "GNBDUFunction") + "." + String.format(
                                                                QUOTED_STRING, "id"))))).where(joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test24i_addTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/id(fdn)";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test24ii_addTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/attributes(id)";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test24iii_addTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/attributes(notExistingColumn)";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test24iiii_addTargetAttributeError() {
        String entityType = "GNBDUFunction";
        String target = "/attr(fdn)";
        String scope = "";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test25_checkIdAttributesError() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attr[@fdn=\"Solar\"]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test26i_checkIdExpressionsError() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[@id=\"123\"]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test26ii_checkIdExpressionsError() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/id[@gNBId=12]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test26iii_checkIdExpressionsError() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/id[contains (@fdn,\"12\")]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test26iiii_checkIdExpressionsError() {
        String entityType = "GNBDUFunction";
        String target = "";
        String scope = "/attributes[contains (@id,\"987\")]";
        String relationships = "";

        QueryMonad qm = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test27i_getTopologyByTargetFilterError() {
        String entityType = null;
        String target = "/CloudSite/attributes/";
        String scope = "";
        String relationships = "";
        String domain = "RAN_LOGICAL";

        QueryMonad qm = getQueryMonadWithDomain(entityType, target, scope, relationships, domain);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test27ii_getTopologyByTargetFilterError() {
        String entityType = null;
        String target = "/attributes";
        String scope = "";
        String relationships = "SECTOR_GROUPS_ANTENNAMODULE";
        String domain = "RAN";

        QueryMonad qm = getQueryMonadWithDomain(entityType, target, scope, relationships, domain);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Objects are not related");
    }

    @Test
    void test27iii_getTopologyByTargetFilterError() {
        String entityType = null;
        String target = "/NRCellDU";
        String scope = "";
        String relationships = "SECTOR_GROUPS_ANTENNAMODULE";
        String domain = "RAN";

        QueryMonad qm = getQueryMonadWithDomain(entityType, target, scope, relationships, domain);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Objects are not related");
    }

    @Test
    void test27iiii_getTopologyByTargetFilterError() {
        String entityType = null;
        String target = "/attributes/name";
        String scope = "";
        String relationships = "";
        String domain = "RAN";

        QueryMonad qm = getQueryMonadWithDomain(entityType, target, scope, relationships, domain);

        assertThatThrownBy(() -> qm.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar error");
    }

    @Test
    void test27iiiii_getTopologyByTargetFilterError() {
        String entityType = "CloudNativeApplication";
        String target1 = "/attributes/id";
        String target2 = "/attributes(id)";
        String scope = "";
        String relationships = "";
        String domain = "RAN_LOGICAL";

        QueryMonad qm1 = getQueryMonadWithDomain(entityType, target1, scope, relationships, domain);
        QueryMonad qm2 = getQueryMonadWithDomain(entityType, target2, scope, relationships, domain);

        assertThatThrownBy(() -> qm1.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");

        assertThatThrownBy(() -> qm2.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Grammar Error");
    }

    @Test
    void test28i_getTopologyByTargetFilter() {
        String entityType = null;
        String target = "/Sector/id";
        String scope = "";
        String relationships = "";
        String domain = "EQUIPMENT_TO_RAN";

        QueryMonad underTest = getQueryMonadWithDomain(entityType, target, scope, relationships, domain);
        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Query reference = createDistinctQuery(context, context.select(field("null").cast(getEntityTypeByName("Sector")
                .getField("id", null, null).getType()).as(String.format(TIES_DATA, "Sector") + "." + String.format(
                        QUOTED_STRING, "id"))).from("(SELECT '') as dual_table").leftJoin(String.format(TIES_DATA,
                                "Sector")).on(condition(field(String.format(TIES_DATA, "Sector") + "." + "id").eq(field(
                                        "(SELECT '')")))).where(field(String.format(TIES_DATA, "Sector") + "." + String
                                                .format(QUOTED_STRING, "id")).isNotNull()).union(context.select(field(String
                                                        .format(TIES_DATA, "Sector") + "." + String.format(QUOTED_STRING,
                                                                "id")).as(String.format(TIES_DATA, "Sector") + "." + String
                                                                        .format(QUOTED_STRING, "id"))).from(
                                                                                "(SELECT '') as dual_table").rightJoin(
                                                                                        String.format(TIES_DATA, "Sector"))
                                                        .on(condition(field(String.format(TIES_DATA, "Sector") + "." + "id")
                                                                .eq(field("(SELECT '')")))).where(field(String.format(
                                                                        TIES_DATA, "Sector") + "." + String.format(
                                                                                QUOTED_STRING, "id")).isNotNull()))

        );

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test50_relConnectingSameEntityTest() {
        String entityType = "AntennaModule";
        String target = "/AntennaModule";
        String scope = "";
        String relationships = "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);

        Query builtWithQM = underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context);

        Condition joinFilter = condition(field(String.format(TIES_DATA, "AntennaModule") + "." + String.format(
                QUOTED_STRING, "id")).isNotNull());

        Condition relationFilter = condition(field(String.format(TIES_DATA,
                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String.format(QUOTED_STRING, "aSide_AntennaModule"))
                        .isNotNull()).and(field(String.format(TIES_DATA,
                                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String.format(QUOTED_STRING,
                                        "bSide_AntennaModule")).isNotNull()).and(field(String.format(TIES_DATA,
                                                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String.format(
                                                        QUOTED_STRING, "id")).isNotNull());

        Query reference = createDistinctQuery(context, context.select(field(String.format(TIES_DATA,
                "AntennaModule") + "." + String.format(QUOTED_STRING, "id")).as(String.format(TIES_DATA,
                        "AntennaModule") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                "AntennaModule")).join(String.format(TIES_DATA, "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE"))
                .on(field(String.format(TIES_DATA, "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String.format(
                        QUOTED_STRING, "bSide_AntennaModule")).eq(field(String.format(TIES_DATA,
                                "AntennaModule") + "." + String.format(QUOTED_STRING, "id")))).or(field(String.format(
                                        TIES_DATA, "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String.format(
                                                QUOTED_STRING, "aSide_AntennaModule")).eq(field(String.format(TIES_DATA,
                                                        "AntennaModule") + "." + String.format(QUOTED_STRING, "id"))))
                .where(relationFilter).and(joinFilter).union(context.select(field("null").cast(getEntityTypeByName(
                        "AntennaModule").getField("id", null, null).getType()).as(String.format(TIES_DATA,
                                "AntennaModule") + "." + String.format(QUOTED_STRING, "id"))).from(String.format(TIES_DATA,
                                        "AntennaModule")).join(String.format(TIES_DATA,
                                                "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE")).on(field(String.format(
                                                        TIES_DATA, "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String
                                                                .format(QUOTED_STRING, "bSide_AntennaModule")).eq(field(
                                                                        String.format(TIES_DATA,
                                                                                "AntennaModule") + "." + String.format(
                                                                                        QUOTED_STRING, "id")))).or(field(
                                                                                                String.format(TIES_DATA,
                                                                                                        "ANTENNAMODULE_REALISED_BY_ANTENNAMODULE") + "." + String
                                                                                                                .format(QUOTED_STRING,
                                                                                                                        "aSide_AntennaModule"))
                                                                                                                                .eq(field(
                                                                                                                                        String.format(
                                                                                                                                                TIES_DATA,
                                                                                                                                                "AntennaModule") + "." + String
                                                                                                                                                        .format(QUOTED_STRING,
                                                                                                                                                                "id"))))
                        .where(relationFilter).and(joinFilter)));

        Assertions.assertEquals(reference, builtWithQM);
    }

    @Test
    void test50_relConnectingSameEntityTest_unrelatedEntities() {
        String entityType = "AntennaModule";
        String target = "/AntennaModule";
        String scope = "";
        String relationships = "GNBDUFUNCTION_REALISED_BY_CLOUDNATIVEAPPLICATION";

        QueryMonad underTest = getQueryMonad(entityType, target, scope, relationships);

        assertThatThrownBy(() -> underTest.withSchema(PaginationDTO.builder().offset(0).limit(5).build()).apply(context))
                .isInstanceOf(TiesPathException.class).hasMessage("Objects are not related");
    }

}
