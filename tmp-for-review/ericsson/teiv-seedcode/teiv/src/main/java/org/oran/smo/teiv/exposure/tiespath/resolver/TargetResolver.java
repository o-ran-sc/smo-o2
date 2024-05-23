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
package org.oran.smo.teiv.exposure.tiespath.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.springframework.stereotype.Component;

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.exposure.tiespath.innerlanguage.TargetObject;
import org.oran.smo.teiv.utils.path.TiesPathQuery;
import org.oran.smo.teiv.utils.path.TiesPathUtil;
import org.oran.smo.teiv.utils.path.exception.PathParsingException;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TargetResolver implements PathResolver<List<TargetObject>> {

    /**
     * Process filter and root objectType . Populates List of Target Object
     *
     * @param rootObject
     *     rootObject in the path parameter
     * @param filter
     *     filter in the query parameter
     * @return Immutable list of filter objects
     */
    public List<TargetObject> resolve(String rootObject, @NonNull String filter) {
        preCheck(filter);
        if (filter.isEmpty()) {
            final String topologyObject = isRootObjectNullOrEmpty(rootObject) ? NULL_ROOT_OBJECT : rootObject;
            return List.of(TargetObject.builder(topologyObject).build());
        } else {
            List<TargetObject> targetObjects = new ArrayList<>();
            Arrays.stream(filter.split(";")).forEach(targetToken -> {
                final TiesPathQuery tiesPathQuery;
                try {
                    tiesPathQuery = TiesPathUtil.getTiesPathQuery(targetToken);
                } catch (ParseCancellationException | PathParsingException e) {
                    log.error("Parsing error on target {} :", targetToken, e);
                    throw TiesPathException.grammarError(e.getMessage());
                }

                final int noOfContainers = tiesPathQuery.getContainerNames().size();
                if (noOfContainers == 0) {
                    //invalid scenario
                    throw TiesException.serverException("Server unknown exception",
                            "Requested query could not be processed", null);
                }

                Optional.ofNullable(getContainerType(tiesPathQuery.getContainerNames())).ifPresentOrElse(
                        containerType -> targetObjects.add(TargetObject.builder(getTopologyObject(rootObject, tiesPathQuery
                                .getContainerNames())).container(containerType).params(tiesPathQuery.getAttributeNames())
                                .build()), () -> targetObjects.add(checkIfSingleContainerAndValidTopologyObject(rootObject,
                                        tiesPathQuery.getContainerNames(), tiesPathQuery.getAttributeNames())));
            });
            return Collections.unmodifiableList(targetObjects);
        }
    }

    private void preCheck(String target) {
        if (target.contains("|")) {
            throw TiesPathException.grammarError("OR (|) is not supported for target filter");
        }
    }

    private TargetObject checkIfSingleContainerAndValidTopologyObject(String rootObject, List<String> containerNames,
            List<String> attributeNames) {
        final int noOfContainers = containerNames.size();
        if (noOfContainers == 1 && (isRootObjectNullOrEmpty(rootObject) || rootObject.equals(containerNames.get(0)))) {
            assertAttributesApplicableForContainer(attributeNames);
            return TargetObject.builder(containerNames.get(0)).build();
        }
        throw TiesPathException.grammarError(
                "Invalid Container name or Root Object name does not match to the path parameter");
    }

    private void assertAttributesApplicableForContainer(List<String> attrNames) {
        if (!attrNames.isEmpty()) {
            throw TiesPathException.grammarError("Attributes cannot be associated at this level");
        }
    }
}
