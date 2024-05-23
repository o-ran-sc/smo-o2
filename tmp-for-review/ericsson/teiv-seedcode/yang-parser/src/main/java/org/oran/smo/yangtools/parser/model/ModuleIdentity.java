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
package org.oran.smo.yangtools.parser.model;

import java.util.Objects;

/**
 * Encodes the identity of a YAM.
 *
 * @author Mark Hollmann
 */
public class ModuleIdentity {

    public static final String UNKWOWN_REVISION = "__UNKNOWN__";

    private final String moduleName;
    private final String revision;

    /**
     * Constructor for YAMs whose revision are not known. This typically happens with 'import'
     * statements where only a module name is specified, but not the actual revision. Note that
     * an "unknown" revision is not the same as a non-existing revision.
     */
    public ModuleIdentity(final String moduleName) {
        this.moduleName = Objects.requireNonNull(moduleName);
        this.revision = UNKWOWN_REVISION;
    }

    /**
     * Constructor for YAMs whose name and revision are known. The revision may be null (but not
     * empty), as in their wisdom the creators of YANG decided it is ok for a YAM to not have a
     * revision - and there actually a few YAMs in the wild that do not declare a revision.
     */
    public ModuleIdentity(final String moduleName, final String revision) {
        this.moduleName = Objects.requireNonNull(moduleName);
        this.revision = revision;

        if ("".equals(revision)) {
            throw new IllegalArgumentException();
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the revision of the YAM. May return any of the following:
     * <ul>
     * <li>The actual known revision of a module (e.g. "2020-10-27")</li>
     * <li>null if the module does not declare a revision (happens rarely, but is possible)</li>
     * <li>The constant UNKWOWN_REVISION if the revision of the module is not known</li>
     * </ul>
     */
    public String getRevision() {
        return revision;
    }

    public boolean isUnknownRevision() {
        return UNKWOWN_REVISION.equals(revision);
    }

    @Override
    public int hashCode() {
        return moduleName.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (other instanceof ModuleIdentity) {
            if (!this.moduleName.equals(((ModuleIdentity) other).moduleName)) {
                return false;
            }
            if (this.revision == null && ((ModuleIdentity) other).revision == null) {
                return true;
            }
            if (this.revision != null && this.revision.equals(((ModuleIdentity) other).revision)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        if (isUnknownRevision()) {
            return moduleName + "/<unknown-rev>";
        }
        if (revision == null) {
            return moduleName + "/<no-rev>";
        }
        return moduleName + "/" + revision;
    }
}
