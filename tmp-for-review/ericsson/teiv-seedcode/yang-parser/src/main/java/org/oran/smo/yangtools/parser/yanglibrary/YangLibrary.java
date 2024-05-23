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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.util.IdentityRefValue;

/**
 * Represents an instance of a Yang Library as defined in RFC 8525 (https://datatracker.ietf.org/doc/html/rfc8525).
 * <p/>
 * Note that most servers are not using NMDA and will typically make use of the (deprecated) "modules-state"
 * container. Also see {@link ModulesState} class.
 *
 * @author Mark Hollmann
 */
public class YangLibrary {

    private String contentId = "";

    private final List<Datastore> datastores = new ArrayList<>();

    /**
     * Denotes the point in the schema tree in which this YANG Library is mounted.
     * This value may be null, denoting the top-level Yang Library.
     */
    private final YangDataDomNode mountPoint;

    public YangLibrary(final YangDataDomNode mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(final String contentId) {
        this.contentId = contentId;
    }

    public List<Datastore> getDatastores() {
        return Collections.unmodifiableList(datastores);
    }

    public void addDatastore(final Datastore datastore) {
        datastores.add(datastore);
    }

    /**
     * Returns the point under which the library is mounted. May return null,
     * indicating the top-level schema.
     */
    public YangDataDomNode getMountPoint() {
        return mountPoint;
    }

    /**
     * Returns whether this YANG Library describes the top-level schema;
     * that is, the schema at the root of the schema tree.
     */
    public boolean containsTopLevelSchema() {
        return mountPoint == null || mountPoint instanceof YangDataDomDocumentRoot;
    }

    /**
     * Returns whether this YANG Library describes a mounted schema; that
     * is, the schema has been mounted at some point in the schema tree
     * (but not the root).
     */
    public boolean containsMountedSchema() {
        return !containsTopLevelSchema();
    }

    /**
     * Returns the "running" datastore. Returns null if no running datastore was
     * found (this should not happen - servers must support the running datastore).
     */
    public Datastore getRunningDatastore() {
        return getDatastore(Datastore.RUNNING_DATASTORE_IDENTITY);
    }

    /**
     * Returns the specified datastore. Returns null if the datastore was not found.
     */
    public Datastore getDatastore(final IdentityRefValue identityRef) {
        return datastores.stream().filter(ds -> identityRef.equals(ds.getDatastoreName())).findFirst().orElse(null);
    }

    /**
     * Returns the top-level schema from among all the YANG Library instances supplied.
     * Returns null if the top-level schema could not be found.
     */
    public static YangLibrary getTopLevelSchema(final List<YangLibrary> yangLibraries) {
        return yangLibraries.stream().filter(YangLibrary::containsTopLevelSchema).findAny().orElse(null);
    }
}
