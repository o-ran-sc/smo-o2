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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A resolver that recursively searches through a list of files and directory paths and
 * identifies all files having any of the specified file extensions.
 * <p/>
 * Duplicate files will not be returned.
 *
 * @author Mark Hollmann
 */
public class FileBasedYangInputResolver implements YangInputResolver {

    public static final String FILE_EXTENSION_YANG = "yang";
    public static final String FILE_EXTENSION_XML = "xml";
    public static final String FILE_EXTENSION_JSON = "json";

    private final List<String> fileExtensionsToConsider;
    private final List<File> filesAndDirectoriesToConsider;

    public FileBasedYangInputResolver(final List<File> filesAndDirectoriesToConsider) {
        this.fileExtensionsToConsider = Collections.<String> emptyList();
        this.filesAndDirectoriesToConsider = Objects.requireNonNull(filesAndDirectoriesToConsider);
    }

    public FileBasedYangInputResolver(final List<File> filesAndDirectoriesToConsider,
            final List<String> fileExtensionsToConsider) {
        this.filesAndDirectoriesToConsider = Objects.requireNonNull(filesAndDirectoriesToConsider);
        this.fileExtensionsToConsider = Objects.requireNonNull(fileExtensionsToConsider);
    }

    @Override
    public Set<YangInput> getResolvedYangInput() {

        final Set<YangInput> result = new HashSet<>();

        for (final File file : filesAndDirectoriesToConsider) {
            resolveFile(result, file);
        }

        return result;
    }

    private void resolveFile(final Set<YangInput> result, final File file) {

        if (file.exists()) {
            if (file.isFile()) {
                filterAgainstFileExtensions(result, file);
            } else {
                for (final File subFile : file.listFiles()) {
                    resolveFile(result, subFile);
                }
            }
        }
    }

    private void filterAgainstFileExtensions(final Set<YangInput> result, final File file) {

        final String fileName = file.getName();
        final int lastIndexOf = fileName.lastIndexOf('.');
        final String fileExtension = lastIndexOf > -1 ?
                fileName.substring(lastIndexOf + 1).toLowerCase(Locale.ENGLISH) :
                null;

        if (fileExtensionsToConsider.isEmpty() || (fileExtension != null && fileExtensionsToConsider.contains(
                fileExtension))) {
            result.add(new FileBasedYangInput(file));
        }
    }

}
