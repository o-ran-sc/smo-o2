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
package org.oran.smo.yangtools.parser.yanglibrary;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.yanglibrary.Module.IetfYangLibraryConformanceType;

/**
 * A populater implementation that extracts data in accordance with RFC8525.
 *
 * @author Mark Hollmann
 */
public class RFC8525Populator implements YangLibraryPopulator {

    @Override
    public void populateModulesState(final ParserExecutionContext context, final ModulesState modulesState,
            final YangDataDomNode modulesStateDomNode) {

        modulesState.setModuleSetId(IetfYangLibraryParser.getValueOfChild(context, modulesStateDomNode, "module-set-id",
                "---unspecified value---"));
    }

    @Override
    public void populateModuleInModulesState(final ParserExecutionContext context, final Module module,
            final YangDataDomNode moduleDomNode) {

        module.setName(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "name", ""));
        module.setRevision(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "revision", ""));
        module.setNamespace(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "namespace", ""));
        module.setFeatures(IetfYangLibraryParser.getValueOfChildren(context, moduleDomNode, "feature"));
        module.setConformanceType(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "conformance-type", ""));

        if (module.getName().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(moduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing module name."));
        }
        if (module.getNamespace().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(moduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing module namespace."));
        }
        if (module.getConformanceType() == IetfYangLibraryConformanceType.UNKNOWN) {
            context.getFindingsManager().addFinding(new Finding(moduleDomNode,
                    ParserFindingType.P081_INCORRECT_YANG_LIBRARY_DATA.toString(),
                    "missing / incorrect module conformance-type."));
        }

        final String schemaLocation = IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "schema", "");
        if (schemaLocation != null && !schemaLocation.isEmpty()) {
            module.setSchemaLocations(Collections.singletonList(schemaLocation));
        }

        final List<YangDataDomNode> deviationDomNodes = IetfYangLibraryParser.getDomChildren(moduleDomNode, "deviation");
        module.setDeviatedByModuleNames(deviationDomNodes.stream().map(dli -> IetfYangLibraryParser.getValueOfChild(context,
                dli, "name", "")).collect(Collectors.toList()));
    }

    @Override
    public void populateSubmoduleInModulesState(final ParserExecutionContext context, final Submodule submodule,
            final YangDataDomNode submoduleDomNode) {

        submodule.setName(IetfYangLibraryParser.getValueOfChild(context, submoduleDomNode, "name", ""));
        submodule.setRevision(IetfYangLibraryParser.getValueOfChild(context, submoduleDomNode, "revision", ""));

        if (submodule.getName().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(submoduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing submodule name."));
        }

        final String subModuleSchemaLocation = IetfYangLibraryParser.getValueOfChild(context, submoduleDomNode, "schema",
                "");
        if (subModuleSchemaLocation != null && !subModuleSchemaLocation.isEmpty()) {
            submodule.setSchemaLocations(Collections.singletonList(subModuleSchemaLocation));
        }
    }

    @Override
    public void populateYangLibrary(final ParserExecutionContext context, final YangLibrary yangLibrary,
            final YangDataDomNode yangLibraryDomNode) {

        yangLibrary.setContentId(IetfYangLibraryParser.getValueOfChild(context, yangLibraryDomNode, "content-id",
                "---unspecified value---"));
    }

    @Override
    public void populateModuleSet(final ParserExecutionContext context, final ModuleSet moduleSet,
            final YangDataDomNode moduleSetDomNode) {

        moduleSet.setName(IetfYangLibraryParser.getValueOfChild(context, moduleSetDomNode, "name", ""));
        if (moduleSet.getName().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(moduleSetDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing module-set name."));
        }
    }

    @Override
    public void populateModuleInYangLibrary(final ParserExecutionContext context, final Module module,
            final YangDataDomNode moduleDomNode, final String conformanceType) {

        module.setName(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "name", ""));
        module.setRevision(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "revision", ""));
        module.setNamespace(IetfYangLibraryParser.getValueOfChild(context, moduleDomNode, "namespace", ""));
        module.setSchemaLocations(IetfYangLibraryParser.getValueOfChildren(context, moduleDomNode, "location"));
        module.setFeatures(IetfYangLibraryParser.getValueOfChildren(context, moduleDomNode, "feature"));
        module.setConformanceType(conformanceType);

        if (module.getName().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(moduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing module name."));
        }
        if (module.getNamespace().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(moduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing module namespace."));
        }

        module.setDeviatedByModuleNames(IetfYangLibraryParser.getValueOfChildren(context, moduleDomNode, "deviation"));
    }

    @Override
    public void populateSubmoduleInYangLibrary(final ParserExecutionContext context, final Submodule submodule,
            final YangDataDomNode submoduleDomNode) {

        submodule.setName(IetfYangLibraryParser.getValueOfChild(context, submoduleDomNode, "name", ""));
        submodule.setRevision(IetfYangLibraryParser.getValueOfChild(context, submoduleDomNode, "revision", ""));
        submodule.setSchemaLocations(IetfYangLibraryParser.getValueOfChildren(context, submoduleDomNode, "location"));

        if (submodule.getName().isEmpty()) {
            context.getFindingsManager().addFinding(new Finding(submoduleDomNode,
                    ParserFindingType.P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING.toString(), "Missing submodule name."));
        }
    }
}
