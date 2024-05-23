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
package org.oran.smo.yangtools.parser.model.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.oran.smo.yangtools.parser.model.util.YangAnnotation;

/**
 * Keeps track of all annotations defined in a given schema.
 *
 * @author Mark Hollmann
 */
public class AnnotationRegistry {

    private List<YangAnnotation> annotations = new ArrayList<>();

    public void addAnnotation(final YangAnnotation annotation) {
        annotations.add(Objects.requireNonNull(annotation));
    }

    public List<YangAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }
}
