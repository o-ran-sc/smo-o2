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

import java.io.File;
import java.io.InputStream;

/**
 * Implementations of this interface can provide input to the parser. The input may be a YAM
 * (model), or perhaps an XML/JSON document (data) that aligns with a model.
 *
 * @author Mark Hollmann
 */
public interface YangInput {

    public static final String MEDIA_TYPE_YANG = "application/yang";
    public static final String MEDIA_TYPE_YANG_DATA_XML = "application/yang-data+xml";
    public static final String MEDIA_TYPE_YANG_DATA_JSON = "application/yang-data+json";

    /**
     * Returns the name of this input.
     */
    String getName();

    /**
     * Returns the file, if any, backing this input. May return null if there is no file
     * (e.g. because the contents are streamed from memory).
     */
    default File getFile() {
        return null;
    }

    /**
     * Returns an input stream for the contents of the input.
     * <p>
     * The stream will be closed by the parser when finished with the input.
     * <p>
     * <b>Note:</b> this method may be called multiple times on the same object. If the content
     * is returned from a "live" stream (perhaps coming over a network), implementations
     * <b>must</b> buffer the complete contents of the live stream into a suitable temporary object
     * (memory, file), and then return (possibly multiple times) an appropriate stream based on
     * that temporary object.
     */
    InputStream getInputStream();

    /**
     * Returns the media type of this input.
     */
    String getMediaType();
}
