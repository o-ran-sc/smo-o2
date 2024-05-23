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
package org.oran.smo.yangtools.parser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.instance.DataTreeBuilderPredicate;
import org.oran.smo.yangtools.parser.data.instance.InstanceDataTreeBuilder;
import org.oran.smo.yangtools.parser.data.instance.RootInstance;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.YangInputResolver;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.schema.ModuleRegistry;
import org.oran.smo.yangtools.parser.model.schema.Schema;
import org.oran.smo.yangtools.parser.util.StackTraceHelper;
import org.oran.smo.yangtools.parser.yanglibrary.IetfYangLibraryParser;
import org.oran.smo.yangtools.parser.yanglibrary.YangLibrary;

/**
 * A utility class that may be used to parse both Yang models and Yang data. This class should not
 * really be used as it assumes that there is a single schema for a server, which doesn't hold true
 * anymore due to schema mount. It has been retained as it is widely used by clients, and the vast
 * majority of servers don't use or even support schema mount.
 *
 * @author Mark Hollmann
 */
public class YangDeviceModel {

    private final String deviceModelIdentity;
    private List<YangData> yangDatas;

    private RootInstance combinedInstanceDataRoot = new RootInstance();

    /**
     * The top-level schema for this server.
     */
    private final Schema topLevelSchema;

    public YangDeviceModel(final String deviceModelIdentity) {
        this.deviceModelIdentity = deviceModelIdentity;
        this.topLevelSchema = new Schema();
    }

    public ModuleRegistry getModuleRegistry() {
        return topLevelSchema.getModuleRegistry();
    }

    public List<YangData> getYangInstanceDataInputs() {
        return yangDatas != null ? yangDatas : Collections.<YangData> emptyList();
    }

    public String getDeviceModelIdentity() {
        return deviceModelIdentity;
    }

    public Schema getTopLevelSchema() {
        return topLevelSchema;
    }

    public RootInstance getCombinedInstanceDataRoot() {
        return combinedInstanceDataRoot;
    }

    public void parseIntoYangModels(final ParserExecutionContext context, final List<YangModel> yangModels) {
        topLevelSchema.parseIntoSchema(context, yangModels);
    }

    /**
     * Parses the supplied Yang Data into their data DOMs and builds the data instance tree. If Yang Library
     * data has been supplied it will be checked against the supplied modules (if so desired by the context).
     */
    public void parseYangData(final ParserExecutionContext context, final YangInputResolver instanceDataInputResolver,
            final DataTreeBuilderPredicate topLevelInstancePredicate) {

        this.yangDatas = instanceDataInputResolver.getResolvedYangInput().stream().map(YangData::new).collect(Collectors
                .toList());

        int nrTopLevelYangLibrariesFound = 0;
        YangLibrary foundYangLibrary = null;

        /*
         * This builds the DOM for each of the instance data inputs. The root
         * of the data DOM is held inside the YangData object.
         */
        for (final YangData yangData : yangDatas) {
            try {
                yangData.parse(context);
                yangData.getYangDataDomDocumentRoot().resolveModuleOrNamespace(topLevelSchema.getModuleNamespaceResolver());

                /*
                 * We check for the Yang Library straight away here to avoid having to re-parse the data.
                 */
                final YangLibrary extracted = IetfYangLibraryParser.getTopLevelYangLibrary(yangData
                        .getYangDataDomDocumentRoot());
                if (extracted != null) {
                    nrTopLevelYangLibrariesFound++;
                    foundYangLibrary = extracted;
                }

            } catch (final Exception ex) {
                context.addFinding(new Finding(yangData, ParserFindingType.P000_UNSPECIFIED_ERROR.toString(),
                        "While parsing instance data '" + yangData.getYangInput().getName() + "' - " + ex.getClass()
                                .getSimpleName() + ": " + ex.getMessage() + " - trace: " + StackTraceHelper
                                        .getStackTraceInfo(ex)));
            }
        }

        if (nrTopLevelYangLibrariesFound > 1) {

            context.addFinding(new Finding(ParserFindingType.P084_MULTIPLE_YANG_LIBRARIES_IN_INPUT,
                    "Multiple instances of YANG Library detected. This makes parsing of instance data ambiguous and may lead to other findings being issued."));

        } else if (nrTopLevelYangLibrariesFound == 1 && context.checkModulesAgainstYangLibrary()) {
            /*
             * If we have been given the YANG Library as part of data we check the
             * contents of the YANG library against the modules that we have been given.
             */
            new CheckYangLibraryAgainstSchema(context, topLevelSchema, foundYangLibrary).performChecks();
        }

        /*
         * This builds the instance tree from the data DOM. For this to work, the underlying models
         * must be available, otherwise the logic does not know whether it deals with containers or
         * lists, and likewise cannot enforce constraints.
         */
        try {
            combinedInstanceDataRoot = InstanceDataTreeBuilder.buildCombinedDataTree(context.getFindingsManager(),
                    yangDatas, topLevelSchema.getModuleRegistry(), topLevelInstancePredicate);
        } catch (final Exception ex) {
            context.addFinding(new Finding(ParserFindingType.P000_UNSPECIFIED_ERROR,
                    "While building combined data tree - " + ex.getClass().getSimpleName() + ": " + ex
                            .getMessage() + " - trace: " + StackTraceHelper.getStackTraceInfo(ex)));
        }
    }
}
