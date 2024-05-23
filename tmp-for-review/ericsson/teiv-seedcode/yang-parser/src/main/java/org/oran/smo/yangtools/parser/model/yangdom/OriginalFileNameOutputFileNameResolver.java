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
package org.oran.smo.yangtools.parser.model.yangdom;

import org.oran.smo.yangtools.parser.model.YangModel;

/**
 * This implementation returns as file name the original file used for the YANG input.
 * Note that this only works if the original input was actually a file (as opposed to,
 * for example, an stream).
 *
 * @author Mark Hollmann
 */
public class OriginalFileNameOutputFileNameResolver implements OutputFileNameResolver {

    @Override
    public String getOutputFileNameForYangInput(final YangModel yangModel) {
        return yangModel.getYangInput().getFile().getName();
    }

}
