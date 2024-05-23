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
package org.oran.smo.teiv.utils;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.teiv.api.model.OranTeivSchema;

@ExtendWith(MockitoExtension.class)
class YangParserTest {

    @InjectMocks
    YangParser yangParser;

    @Test
    void testExtractYangData() {
        //when
        String deviceModelIdentity = yangParser.extractYangData().getDeviceModelIdentity();
        List<YangModel> validYangModelInputsList = yangParser.extractYangData().getModuleRegistry().getAllYangModels();
        //then
        Assertions.assertEquals("r1", deviceModelIdentity);
        //        Assertions.assertEquals(10, validYangModelInputsList.size());
    }

    @Test
    void testReturnAllTiesSchemas() {
        //when
        List<OranTeivSchema> OranTeivSchemasMetaDataList = YangParser.returnAllTiesSchemas(yangParser.extractYangData());
        //then
        Assertions.assertEquals(10, OranTeivSchemasMetaDataList.size());
    }

    @Test
    void testReturnSchemaByName() {
        //when
        String responseForIncorrectSchema = YangParser.returnSchemaByName(yangParser.extractYangData(),
                "o-ran-smo-teiv-ran-oam");
        String responseForCorrectSchema = YangParser.returnSchemaByName(yangParser.extractYangData(), "o-ran-smo-teiv-oam");
        //then
        Assertions.assertEquals("", responseForIncorrectSchema);
        Assertions.assertTrue(responseForCorrectSchema.contains("o-ran-smo-teiv-oam"));
    }

}
