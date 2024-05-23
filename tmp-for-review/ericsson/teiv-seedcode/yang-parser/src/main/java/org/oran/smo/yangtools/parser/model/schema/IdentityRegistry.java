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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.model.util.YangIdentity;

/**
 * Keeps track of all identities in a given schema. For each identity, its base(s)
 * and derived identities are also stored.
 *
 * @author Mark Hollmann
 */
public class IdentityRegistry {

    /**
     * Map<identity, bases-of-the-identity>
     */
    private Map<YangIdentity, Set<YangIdentity>> identitiesAndBases = new HashMap<>();

    /**
     * Map<identity, identities-derived-from-the-identity>
     */
    private Map<YangIdentity, Set<YangIdentity>> identitiesAndDerived = new HashMap<>();

    public void clear() {
        identitiesAndBases.clear();
        identitiesAndDerived.clear();
    }

    /**
     * Adds an identity to the registry.
     */
    public void addIdentity(final YangIdentity yangIdentity) {
        identitiesAndBases.put(Objects.requireNonNull(yangIdentity), new HashSet<>());
        identitiesAndDerived.put(Objects.requireNonNull(yangIdentity), new HashSet<>());
    }

    /**
     * Adds base/derived information to two existing identity (so puts these into a
     * relation to each other).
     */
    public void addBaseIdentity(final YangIdentity derivedIdentity, final YangIdentity baseIdentity) {

        final Set<YangIdentity> bases = identitiesAndBases.get(Objects.requireNonNull(derivedIdentity));
        final Set<YangIdentity> derivates = identitiesAndDerived.get(Objects.requireNonNull(baseIdentity));

        if (bases != null && derivates != null) {
            bases.add(baseIdentity);
            derivates.add(derivedIdentity);
        }
    }

    public Set<YangIdentity> getIdentities() {
        return Collections.unmodifiableSet(identitiesAndBases.keySet());
    }

    public Set<YangIdentity> getBasesForIdentity(final YangIdentity derivedIdentity) {
        return identitiesAndBases.get(Objects.requireNonNull(derivedIdentity));
    }

    public Set<YangIdentity> getDerivatesOfIdentity(final YangIdentity baseIdentity) {
        return identitiesAndDerived.get(Objects.requireNonNull(baseIdentity));
    }

    /**
     * Returns the identity and all identities deriving from it (at all levels, not
     * just those directly deriving from the identity).
     */
    public Set<YangIdentity> getIdentityAndDerivedIdentitiesRecursively(final YangIdentity identity) {

        if (!identitiesAndDerived.containsKey(Objects.requireNonNull(identity))) {
            return Collections.<YangIdentity> emptySet();
        }

        final Set<YangIdentity> result = new HashSet<>();
        addIdentityAndDerivedIdentities(identity, result);
        return result;
    }

    private void addIdentityAndDerivedIdentities(final YangIdentity identity, final Set<YangIdentity> result) {

        if (result.contains(identity)) {		// prevents infinite loops
            return;
        }

        result.add(identity);
        identitiesAndDerived.get(identity).forEach(derived -> addIdentityAndDerivedIdentities(derived, result));
    }
}
