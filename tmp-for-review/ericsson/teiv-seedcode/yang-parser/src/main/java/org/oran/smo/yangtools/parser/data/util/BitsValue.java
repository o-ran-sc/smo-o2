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
package org.oran.smo.yangtools.parser.data.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.oran.smo.yangtools.parser.model.util.GrammarHelper;

/**
 * Holds the value of a data node instance of type "bits".
 *
 * @author Mark Hollmann
 */
public class BitsValue {

    private final Set<String> setBits;

    public BitsValue() {
        setBits = new HashSet<>();
    }

    public BitsValue(final String val) {
        setBits = new HashSet<>(GrammarHelper.parseToStringList(val.trim()));
    }

    public boolean isBitSet(final String bitName) {
        return setBits.contains(bitName);
    }

    public BitsValue setBit(final String val) {
        setBits.add(Objects.requireNonNull(val));
        return this;		// for chaining
    }

    public Set<String> getSetBits() {
        return Collections.unmodifiableSet(setBits);
    }

    @Override
    public String toString() {
        return "Bits set: " + setBits.toString();
    }

    @Override
    public int hashCode() {
        return setBits.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BitsValue && ((BitsValue) obj).setBits.equals(this.setBits);
    }
}
