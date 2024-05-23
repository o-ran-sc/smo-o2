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

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = YangParser.class)
class YangParserTest {

    @Autowired
    private YangParser yangParser;

    @Test
    void returnAllModuleReferencesTest() throws IOException {

        List<Module> moduleList = yangParser.returnAllModuleReferences();

        Assertions.assertEquals(5, moduleList.size());

        List<String> allModuleReferenceNames = moduleList.stream().map(Module::getName).sorted().toList();

        Assertions.assertEquals(allModuleReferenceNames, Stream.of("o-ran-smo-teiv-common-yang-extensions",
                "o-ran-smo-teiv-common-yang-types", "o-ran-smo-teiv-equipment", "o-ran-smo-teiv-rel-equipment-ran",
                "o-ran-smo-teiv-ran").sorted().toList());

    }
}
