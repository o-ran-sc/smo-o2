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

import static org.oran.smo.teiv.pgsqlgenerator.Constants.BUILT_IN_MODULE_ID;
import static org.oran.smo.teiv.pgsqlgenerator.Constants.DEFAULT_MODULE_STATUS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.input.ByteArrayYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;

@Slf4j
@Component
public class YangParser {

    @Value("${yang-model.source}")
    private String yangModelDirectory;

    /**
     * Store name, domains and namespace from yang modules
     *
     * @return list of module references
     */
    public List<Module> returnAllModuleReferences() throws IOException {
        YangDeviceModel yangDeviceModel = extractYangData();
        List<Module> modules = new ArrayList<>();
        for (final YangModel yangInput : yangDeviceModel.getModuleRegistry().getAllYangModels()) {
            final AbstractStatement moduleOrSubmodule = yangInput.getYangModelRoot().getModuleOrSubmodule();
            String moduleReferenceName = moduleOrSubmodule.getDomElement().getValue();
            String domain = getDomain(moduleOrSubmodule);
            String namespace = getNamespace(moduleOrSubmodule);
            List<String> includedModules = getIncludedModules(moduleOrSubmodule);
            modules.add(Module.builder().name(moduleReferenceName).namespace(namespace).domain(domain).includedModules(
                    includedModules).revision(yangInput.getModuleIdentity().getRevision()).content(new String(yangInput
                            .getYangInput().getInputStream().readAllBytes(), StandardCharsets.UTF_8)).ownerAppId(
                                    BUILT_IN_MODULE_ID).status(DEFAULT_MODULE_STATUS).build());
        }
        return modules;
    }

    private String getDomain(AbstractStatement moduleOrSubmodule) {
        AbstractStatement domainStatement = moduleOrSubmodule.getChild(new StatementModuleAndName(
                "o-ran-smo-teiv-common-yang-extensions", "domain"));
        return domainStatement != null && domainStatement.getDomElement() != null ?
                domainStatement.getDomElement().getValue() :
                "";
    }

    private String getNamespace(AbstractStatement moduleOrSubmodule) {
        for (AbstractStatement child : moduleOrSubmodule.getChildStatements()) {
            if (child.getDomElement().getName().equalsIgnoreCase("namespace")) {
                return child.getDomElement().getValue();
            }
        }
        return "";
    }

    /**
     * Extract all imported modules in a yang module
     */
    private List<String> getIncludedModules(AbstractStatement statement) {
        List<String> includedModules = new ArrayList<>();
        statement.getChildStatements().forEach(childStatement -> {
            String domStatement = childStatement.getDomElement().toString();
            if (domStatement.contains("import")) {
                includedModules.add(childStatement.getDomElement().getValue());
            }
        });
        return includedModules;
    }

    private YangDeviceModel extractYangData() {
        YangDeviceModel yangDeviceModel = new YangDeviceModel("r1");
        final List<YangModel> yangModelInputs = new ArrayList<>();
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
            Resource[] yangResources = resolver.getResources(yangModelDirectory + "/*.yang");
            for (Resource yangResource : yangResources) {
                yangModelInputs.add(new YangModel(new ByteArrayYangInput(yangResource.getContentAsByteArray(), Objects
                        .requireNonNull(yangResource.getFilename())), ConformanceType.IMPORT));
            }
        } catch (final IOException ex) {
            throw PgSchemaGeneratorException.extractYangDataException(ex);
        }

        final ModifyableFindingSeverityCalculator severityCalculator = new ModifyableFindingSeverityCalculator();
        final FindingsManager findingsManager = new FindingsManager(severityCalculator);
        final ParserExecutionContext context = new ParserExecutionContext(findingsManager);
        context.setIgnoreImportedProtocolAccessibleObjects(true);
        context.setFailFast(false);
        yangDeviceModel.parseIntoYangModels(context, yangModelInputs);
        return yangDeviceModel;
    }
}
