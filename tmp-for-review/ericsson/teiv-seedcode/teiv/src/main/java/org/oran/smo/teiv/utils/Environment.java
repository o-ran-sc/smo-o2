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
package org.oran.smo.teiv.utils;

/**
 * A utility class for retrieving environment/system values.
 *
 * @see System#getProperty(String)
 * @see System#getenv(String)
 */
public final class Environment {

    private Environment() {

    }

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a
     * {@link System}
     * environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then null is returned.
     *
     * @param propertyName
     *     the property to search for
     * @return the value of the searched property, otherwise {@code null}
     */
    public static String getEnvironmentValue(final String propertyName) {
        return System.getProperty(propertyName, System.getenv(propertyName));
    }

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a
     * {@link System}
     * environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then the default value provided is returned.
     *
     * @param propertyName
     *     the property name to search for
     * @param defaultValue
     *     the default value if no property exists
     * @return The value of the searched name, otherwise the {@code defaultValue}
     */
    public static String getEnvironmentValue(final String propertyName, final String defaultValue) {
        final String envVariable = System.getenv(propertyName);
        if (envVariable != null) {
            return envVariable;
        }
        return System.getProperty(propertyName, defaultValue);
    }
}
