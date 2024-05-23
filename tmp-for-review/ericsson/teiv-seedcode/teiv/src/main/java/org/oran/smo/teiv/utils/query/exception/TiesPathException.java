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
package org.oran.smo.teiv.utils.query.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TiesPathException extends RuntimeException {

    private final String message;
    private final String details;
    private final HttpStatus httpStatus;
    private final transient List<Object> response;
    private static final List<Object> defaultResponse = Collections.emptyList();

    public static TiesPathException invalidRelationshipName(final String relationship) {
        return clientException("Invalid relationship name", String.format("%s is not a known relationship", relationship));
    }

    public static TiesPathException noConnectionFound(final String object) {
        return clientException("Objects are not related", String.format(
                "No relationship can be found between %s and scopeFilter elements", object));
    }

    public static TiesPathException noConnectionFoundWhenRootIsNull() {
        return clientException("Objects are not related",
                "None of the elements in the targetFilter is part of connection provided in relationships filter");
    }

    public static TiesPathException grammarError(final String message) {
        return clientException("Grammar error", message);
    }

    public static TiesPathException columnNameError(String entity, String column) {
        return clientException("Grammar Error", String.format("%s is not a valid attribute of %s", column, entity));
    }

    public static TiesPathException columnNamesError(String entity, List<String> columns) {
        if (columns.size() == 1) {
            return columnNameError(entity, columns.get(0));
        } else {
            return clientException("Invalid parameter error", String.format("%s are not valid attributes of %s", String
                    .join(", ", columns), entity));
        }
    }

    public static TiesPathException sourceIdNameError(String entity) {
        return clientException("Invalid parameter error", String.format("Invalid source id parameter provided for %s",
                entity));
    }

    private static TiesPathException clientException(final String message, final String details) {
        return new TiesPathException(message, details, HttpStatus.BAD_REQUEST, null);
    }

    public static TiesPathException idAmongAttributesError() {
        return clientException("Grammar Error", "ID is not considered to be an attribute");
    }

    public static TiesPathException entityNameError(String entity) {
        return clientException("Grammar Error", String.format("%s is not a valid entity", entity));
    }

    public static TiesPathException invalidTopologyObject(String topologyObject) {
        return clientException("Invalid topology object", String.format(
                "%s did not match any topology objects in the given domain", topologyObject));
    }

    public static TiesPathException ambiguousTopologyObject(String topologyObject) {
        return clientException("Invalid topology object", String.format(
                "%s is ambiguous, %s matches multiple topology object types", topologyObject, topologyObject));
    }

    public static TiesPathException invalidAssociation(String topologyObject, String associationName) {
        return clientException("Invalid association name", String.format(
                "%s is not a valid association name for topology object %s", associationName, topologyObject));
    }

    public static TiesPathException invalidParamsForAssociation(String associationName) {
        return clientException("Invalid parameters for association", String.format(
                "Invalid parameters provided for association %s", associationName));
    }

    public static TiesPathException containerValidationWithUndefinedTopologyObjectType(String topologyObject) {
        return clientException("Container validation error", String.format(
                "Container validation is not possible for undefined %s", topologyObject));
    }

    public static TiesPathException notMatchingScopeAndTargetFilter() {
        return clientException("Filter Error", "TopologyObjects given in scopeFilter and targetFilter are not matching");
    }

    private static TiesPathException clientExceptionWithDefaultResponse(final String message, final String details) {
        return new TiesPathException(message, details, HttpStatus.BAD_REQUEST, defaultResponse);
    }

    private TiesPathException(final String message, final String details, final HttpStatus httpStatus,
            final List<Object> response) {
        this.message = message;
        this.details = details;
        this.httpStatus = httpStatus;
        this.response = response != null ? new ArrayList<>(response) : null;
    }
}
