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
package org.oran.smo.yangtools.parser.input;

import java.util.Objects;

/**
 * A YangInput based on a String. The String object would typically be a complete YAM, or a
 * complete XML / JSON document.
 *
 * @author Mark Hollmann
 */
public class StringYangInput extends ByteArrayYangInput {

    public StringYangInput(final String data, final String name, final String mediaType) {
        super(data.getBytes(), Objects.requireNonNull(name), Objects.requireNonNull(mediaType));
    }
}
