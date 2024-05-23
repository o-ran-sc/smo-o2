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
package org.oran.smo.yangtools.parser.model.statements.yang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;

/**
 * A class containing a number of constants in relation to the YANG core language.
 *
 * @author Mark Hollmann
 */
public abstract class CY {

    /*
     * ----------- core language statement names ---------------
     */

    public static final String ACTION = "action";
    public static final String ANYDATA = "anydata";
    public static final String ANYXML = "anyxml";
    public static final String ARGUMENT = "argument";
    public static final String AUGMENT = "augment";
    public static final String BASE = "base";
    public static final String BELONGS_TO = "belongs-to";
    public static final String BIT = "bit";
    public static final String CASE = "case";
    public static final String CHOICE = "choice";
    public static final String CONFIG = "config";
    public static final String CONTACT = "contact";
    public static final String CONTAINER = "container";
    public static final String DEFAULT = "default";
    public static final String DESCRIPTION = "description";
    public static final String DEVIATE = "deviate";
    public static final String DEVIATION = "deviation";
    public static final String ENUM = "enum";
    public static final String ERROR_APP_TAG = "error-app-tag";
    public static final String ERROR_MESSAGE = "error-message";
    public static final String EXTENSION = "extension";
    public static final String FEATURE = "feature";
    public static final String FRACTION_DIGITS = "fraction-digits";
    public static final String GROUPING = "grouping";
    public static final String IDENTITY = "identity";
    public static final String IF_FEATURE = "if-feature";
    public static final String IMPORT = "import";
    public static final String INCLUDE = "include";
    public static final String INPUT = "input";
    public static final String KEY = "key";
    public static final String LEAF = "leaf";
    public static final String LEAF_LIST = "leaf-list";
    public static final String LENGTH = "length";
    public static final String LIST = "list";
    public static final String MANDATORY = "mandatory";
    public static final String MAX_ELEMENTS = "max-elements";
    public static final String MIN_ELEMENTS = "min-elements";
    public static final String MODIFIER = "modifier";
    public static final String MODULE = "module";
    public static final String MUST = "must";
    public static final String NAMESPACE = "namespace";
    public static final String NOTIFICATION = "notification";
    public static final String ORDERED_BY = "ordered-by";
    public static final String ORGANIZATION = "organization";
    public static final String OUTPUT = "output";
    public static final String PATH = "path";
    public static final String PATTERN = "pattern";
    public static final String POSITION = "position";
    public static final String PREFIX = "prefix";
    public static final String PRESENCE = "presence";
    public static final String RANGE = "range";
    public static final String REFERENCE = "reference";
    public static final String REFINE = "refine";
    public static final String REQUIRE_INSTANCE = "require-instance";
    public static final String REVISION = "revision";
    public static final String REVISION_DATE = "revision-date";
    public static final String RPC = "rpc";
    public static final String STATUS = "status";
    public static final String SUBMODULE = "submodule";
    public static final String TYPE = "type";
    public static final String TYPEDEF = "typedef";
    public static final String UNIQUE = "unique";
    public static final String UNITS = "units";
    public static final String USES = "uses";
    public static final String VALUE = "value";
    public static final String WHEN = "when";
    public static final String YANG_VERSION = "yang-version";
    public static final String YIN_ELEMENT = "yin-element";

    public static final Set<String> ALL_YANG_CORE_STATEMENT_NAMES = new HashSet<>(Arrays.asList(ACTION, ANYDATA, ANYXML,
            ARGUMENT, AUGMENT, BASE, BELONGS_TO, BIT, CASE, CHOICE, CONFIG, CONTACT, CONTAINER, DEFAULT, DESCRIPTION,
            DEVIATE, DEVIATION, ENUM, ERROR_APP_TAG, ERROR_MESSAGE, EXTENSION, FEATURE, FRACTION_DIGITS, GROUPING, IDENTITY,
            IF_FEATURE, IMPORT, INCLUDE, INPUT, KEY, LEAF, LEAF_LIST, LENGTH, LIST, MANDATORY, MAX_ELEMENTS, MIN_ELEMENTS,
            MODIFIER, MODULE, MUST, NAMESPACE, NOTIFICATION, ORDERED_BY, ORGANIZATION, OUTPUT, PATH, PATTERN, POSITION,
            PREFIX, PRESENCE, RANGE, REFERENCE, REFINE, REQUIRE_INSTANCE, REVISION, REVISION_DATE, RPC, STATUS, SUBMODULE,
            TYPE, TYPEDEF, UNIQUE, UNITS, USES, VALUE, WHEN, YANG_VERSION, YIN_ELEMENT));

    /**
     * Returns whether the supplied statement name is defined by the Yang core language.
     */
    public static boolean isYangCoreStatementName(final String statementName) {
        return ALL_YANG_CORE_STATEMENT_NAMES.contains(statementName);
    }

    /*
     * --------- core YANG statements encoded as module-name/statements -------------
     */

    /**
     * There is no module name for YANG. The name chosen here is a fictitious, but illegal,
     * module name (as it contains a space character), to avoid a potential clash with an actual,
     * real, YANG module name.
     */
    public static final String YANG_CORE_MODULE_NAME = "YANG CORE";

    public static final StatementModuleAndName STMT_ACTION = new StatementModuleAndName(YANG_CORE_MODULE_NAME, ACTION);
    public static final StatementModuleAndName STMT_ANYDATA = new StatementModuleAndName(YANG_CORE_MODULE_NAME, ANYDATA);
    public static final StatementModuleAndName STMT_ANYXML = new StatementModuleAndName(YANG_CORE_MODULE_NAME, ANYXML);
    public static final StatementModuleAndName STMT_ARGUMENT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, ARGUMENT);
    public static final StatementModuleAndName STMT_AUGMENT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, AUGMENT);
    public static final StatementModuleAndName STMT_BASE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, BASE);
    public static final StatementModuleAndName STMT_BELONGS_TO = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            BELONGS_TO);
    public static final StatementModuleAndName STMT_BIT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, BIT);
    public static final StatementModuleAndName STMT_CASE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, CASE);
    public static final StatementModuleAndName STMT_CHOICE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, CHOICE);
    public static final StatementModuleAndName STMT_CONFIG = new StatementModuleAndName(YANG_CORE_MODULE_NAME, CONFIG);
    public static final StatementModuleAndName STMT_CONTACT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, CONTACT);
    public static final StatementModuleAndName STMT_CONTAINER = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            CONTAINER);
    public static final StatementModuleAndName STMT_DEFAULT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, DEFAULT);
    public static final StatementModuleAndName STMT_DESCRIPTION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            DESCRIPTION);
    public static final StatementModuleAndName STMT_DEVIATE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, DEVIATE);
    public static final StatementModuleAndName STMT_DEVIATION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            DEVIATION);
    public static final StatementModuleAndName STMT_ENUM = new StatementModuleAndName(YANG_CORE_MODULE_NAME, ENUM);
    public static final StatementModuleAndName STMT_ERROR_APP_TAG = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            ERROR_APP_TAG);
    public static final StatementModuleAndName STMT_ERROR_MESSAGE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            ERROR_MESSAGE);
    public static final StatementModuleAndName STMT_EXTENSION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            EXTENSION);
    public static final StatementModuleAndName STMT_FEATURE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, FEATURE);
    public static final StatementModuleAndName STMT_FRACTION_DIGITS = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            FRACTION_DIGITS);
    public static final StatementModuleAndName STMT_GROUPING = new StatementModuleAndName(YANG_CORE_MODULE_NAME, GROUPING);
    public static final StatementModuleAndName STMT_IDENTITY = new StatementModuleAndName(YANG_CORE_MODULE_NAME, IDENTITY);
    public static final StatementModuleAndName STMT_IF_FEATURE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            IF_FEATURE);
    public static final StatementModuleAndName STMT_IMPORT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, IMPORT);
    public static final StatementModuleAndName STMT_INCLUDE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, INCLUDE);
    public static final StatementModuleAndName STMT_INPUT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, INPUT);
    public static final StatementModuleAndName STMT_KEY = new StatementModuleAndName(YANG_CORE_MODULE_NAME, KEY);
    public static final StatementModuleAndName STMT_LEAF = new StatementModuleAndName(YANG_CORE_MODULE_NAME, LEAF);
    public static final StatementModuleAndName STMT_LEAF_LIST = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            LEAF_LIST);
    public static final StatementModuleAndName STMT_LENGTH = new StatementModuleAndName(YANG_CORE_MODULE_NAME, LENGTH);
    public static final StatementModuleAndName STMT_LIST = new StatementModuleAndName(YANG_CORE_MODULE_NAME, LIST);
    public static final StatementModuleAndName STMT_MANDATORY = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            MANDATORY);
    public static final StatementModuleAndName STMT_MAX_ELEMENTS = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            MAX_ELEMENTS);
    public static final StatementModuleAndName STMT_MIN_ELEMENTS = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            MIN_ELEMENTS);
    public static final StatementModuleAndName STMT_MODIFIER = new StatementModuleAndName(YANG_CORE_MODULE_NAME, MODIFIER);
    public static final StatementModuleAndName STMT_MODULE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, MODULE);
    public static final StatementModuleAndName STMT_MUST = new StatementModuleAndName(YANG_CORE_MODULE_NAME, MUST);
    public static final StatementModuleAndName STMT_NAMESPACE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            NAMESPACE);
    public static final StatementModuleAndName STMT_NOTIFICATION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            NOTIFICATION);
    public static final StatementModuleAndName STMT_ORDERED_BY = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            ORDERED_BY);
    public static final StatementModuleAndName STMT_ORGANIZATION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            ORGANIZATION);
    public static final StatementModuleAndName STMT_OUTPUT = new StatementModuleAndName(YANG_CORE_MODULE_NAME, OUTPUT);
    public static final StatementModuleAndName STMT_PATH = new StatementModuleAndName(YANG_CORE_MODULE_NAME, PATH);
    public static final StatementModuleAndName STMT_PATTERN = new StatementModuleAndName(YANG_CORE_MODULE_NAME, PATTERN);
    public static final StatementModuleAndName STMT_POSITION = new StatementModuleAndName(YANG_CORE_MODULE_NAME, POSITION);
    public static final StatementModuleAndName STMT_PREFIX = new StatementModuleAndName(YANG_CORE_MODULE_NAME, PREFIX);
    public static final StatementModuleAndName STMT_PRESENCE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, PRESENCE);
    public static final StatementModuleAndName STMT_RANGE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, RANGE);
    public static final StatementModuleAndName STMT_REFERENCE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            REFERENCE);
    public static final StatementModuleAndName STMT_REFINE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, REFINE);
    public static final StatementModuleAndName STMT_REQUIRE_INSTANCE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            REQUIRE_INSTANCE);
    public static final StatementModuleAndName STMT_REVISION = new StatementModuleAndName(YANG_CORE_MODULE_NAME, REVISION);
    public static final StatementModuleAndName STMT_REVISION_DATE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            REVISION_DATE);
    public static final StatementModuleAndName STMT_RPC = new StatementModuleAndName(YANG_CORE_MODULE_NAME, RPC);
    public static final StatementModuleAndName STMT_STATUS = new StatementModuleAndName(YANG_CORE_MODULE_NAME, STATUS);
    public static final StatementModuleAndName STMT_SUBMODULE = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            SUBMODULE);
    public static final StatementModuleAndName STMT_TYPE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, TYPE);
    public static final StatementModuleAndName STMT_TYPEDEF = new StatementModuleAndName(YANG_CORE_MODULE_NAME, TYPEDEF);
    public static final StatementModuleAndName STMT_UNIQUE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, UNIQUE);
    public static final StatementModuleAndName STMT_UNITS = new StatementModuleAndName(YANG_CORE_MODULE_NAME, UNITS);
    public static final StatementModuleAndName STMT_USES = new StatementModuleAndName(YANG_CORE_MODULE_NAME, USES);
    public static final StatementModuleAndName STMT_VALUE = new StatementModuleAndName(YANG_CORE_MODULE_NAME, VALUE);
    public static final StatementModuleAndName STMT_WHEN = new StatementModuleAndName(YANG_CORE_MODULE_NAME, WHEN);
    public static final StatementModuleAndName STMT_YANG_VERSION = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            YANG_VERSION);
    public static final StatementModuleAndName STMT_YIN_ELEMENT = new StatementModuleAndName(YANG_CORE_MODULE_NAME,
            YIN_ELEMENT);

    public static final Set<StatementModuleAndName> ALL_YANG_CORE_STATEMENTS = new HashSet<>(Arrays.asList(STMT_ACTION,
            STMT_ANYDATA, STMT_ANYXML, STMT_ARGUMENT, STMT_AUGMENT, STMT_BASE, STMT_BELONGS_TO, STMT_BIT, STMT_CASE,
            STMT_CHOICE, STMT_CONFIG, STMT_CONTACT, STMT_CONTAINER, STMT_DEFAULT, STMT_DESCRIPTION, STMT_DEVIATE,
            STMT_DEVIATION, STMT_ENUM, STMT_ERROR_APP_TAG, STMT_ERROR_MESSAGE, STMT_EXTENSION, STMT_FEATURE,
            STMT_FRACTION_DIGITS, STMT_GROUPING, STMT_IDENTITY, STMT_IF_FEATURE, STMT_IMPORT, STMT_INCLUDE, STMT_INPUT,
            STMT_KEY, STMT_LEAF, STMT_LEAF_LIST, STMT_LENGTH, STMT_LIST, STMT_MANDATORY, STMT_MAX_ELEMENTS,
            STMT_MIN_ELEMENTS, STMT_MODIFIER, STMT_MODULE, STMT_MUST, STMT_NAMESPACE, STMT_NOTIFICATION, STMT_ORDERED_BY,
            STMT_ORGANIZATION, STMT_OUTPUT, STMT_PATH, STMT_PATTERN, STMT_POSITION, STMT_PREFIX, STMT_PRESENCE, STMT_RANGE,
            STMT_REFERENCE, STMT_REFINE, STMT_REQUIRE_INSTANCE, STMT_REVISION, STMT_REVISION_DATE, STMT_RPC, STMT_STATUS,
            STMT_SUBMODULE, STMT_TYPE, STMT_TYPEDEF, STMT_UNIQUE, STMT_UNITS, STMT_USES, STMT_VALUE, STMT_WHEN,
            STMT_YANG_VERSION, STMT_YIN_ELEMENT));

    public static boolean isYangCoreStatement(final StatementModuleAndName statementModuleAndName) {
        return ALL_YANG_CORE_STATEMENTS.contains(statementModuleAndName);
    }

    private static final Map<String, StatementModuleAndName> STATEMENT_NAME_TO_STATEMENT = new HashMap<>();

    static {
        ALL_YANG_CORE_STATEMENTS.forEach(sman -> STATEMENT_NAME_TO_STATEMENT.put(sman.getStatementName(), sman));
    }

    /**
     * Given the statement name, will return the corresponding statement. Will return null if the statement
     * is not one of the core YANG statements.
     */
    public static StatementModuleAndName getStatementForName(final String statementName) {
        return STATEMENT_NAME_TO_STATEMENT.get(statementName);
    }

    /*
     * -------- children handling, i.e. the statements that can occur under other statements --------
     */

    private static final List<String> NO_CHILDREN = Collections.<String> emptyList();
    private static final List<String> DESCRIPTION_AND_REFERENCE_CHILDREN = Arrays.asList(DESCRIPTION, REFERENCE);

    private static final Map<String, List<String>> optionalSingleChildren = new HashMap<>();
    private static final Map<String, List<String>> optionalMultipleChildren = new HashMap<>();
    private static final Map<String, List<String>> mandatorySingleChildren = new HashMap<>();
    private static final Map<String, List<String>> mandatoryMultipleChildren = new HashMap<>();

    /**
     * Returns a list of statements that may occur 0..1 under the supplied statement.
     */
    public static final List<String> getOptionalSingleChildren(final String forYangCoreStatement) {

        final List<String> result = optionalSingleChildren.get(forYangCoreStatement);
        /*
         * We are kind and explicitly allow both 'description' and 'reference' on any statement
         * that does not have an explicit list of children. According to RFC this isn't really
         * syntactically correct, but sometimes model designers will add descriptions and
         * references although they don't have to (or where they shouldn't) and we are lenient
         * towards that.
         */
        return result == null ? DESCRIPTION_AND_REFERENCE_CHILDREN : result;
    }

    /**
     * Returns a list of statements that may occur 0..n under the supplied statement.
     */
    public static final List<String> getOptionalMultipleChildren(final String forYangCoreStatement) {
        final List<String> result = optionalMultipleChildren.get(forYangCoreStatement);
        return result == null ? NO_CHILDREN : result;
    }

    /**
     * Returns a list of statements that must occur exactly once under the supplied statement.
     */
    public static final List<String> getMandatorySingleChildren(final String forYangCoreStatement) {
        final List<String> result = mandatorySingleChildren.get(forYangCoreStatement);
        return result == null ? NO_CHILDREN : result;
    }

    /**
     * Returns a list of statements that must occur once or more under the supplied statement.
     */
    public static final List<String> getMandatoryMultipleChildren(final String forYangCoreStatement) {
        final List<String> result = mandatoryMultipleChildren.get(forYangCoreStatement);
        return result == null ? NO_CHILDREN : result;
    }

    static {

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| description  | 7.21.3  | 0..1 |
        	| grouping     | 7.12    | 0..n |
        	| if-feature   | 7.20.2  | 0..n |
        	| input        | 7.14.2  | 0..1 |
        	| output       | 7.14.3  | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| typedef      | 7.3     | 0..n |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(ACTION, Arrays.asList(DESCRIPTION, INPUT, OUTPUT, REFERENCE, STATUS));
        optionalMultipleChildren.put(ACTION, Arrays.asList(GROUPING, IF_FEATURE, TYPEDEF));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| config       | 7.21.1  | 0..1 |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| mandatory    | 7.6.5   | 0..1 |
        	| must         | 7.5.3   | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(ANYDATA, Arrays.asList(CONFIG, DESCRIPTION, MANDATORY, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(ANYDATA, Arrays.asList(IF_FEATURE, MUST));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| config       | 7.21.1  | 0..1 |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| mandatory    | 7.6.5   | 0..1 |
        	| must         | 7.5.3   | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(ANYXML, Arrays.asList(CONFIG, DESCRIPTION, MANDATORY, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(ANYXML, Arrays.asList(IF_FEATURE, MUST));

        /*
        	+--------------+----------+-------------+
        	| substatement | section  | cardinality |
        	+--------------+----------+-------------+
        	| yin-element  | 7.19.2.2 | 0..1 |
        	+--------------+----------+-------------+
         */
        optionalSingleChildren.put(ARGUMENT, Arrays.asList(DESCRIPTION, REFERENCE, YIN_ELEMENT));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| action       | 7.15    | 0..n |
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| case         | 7.9.2   | 0..n |
        	| choice       | 7.9     | 0..n |
        	| container    | 7.5     | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| notification | 7.16    | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| uses         | 7.13    | 0..n |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(AUGMENT, Arrays.asList(DESCRIPTION, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(AUGMENT, Arrays.asList(ACTION, ANYDATA, ANYXML, CASE, CHOICE, CONTAINER, IF_FEATURE,
                LEAF, LEAF_LIST, LIST, NOTIFICATION, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| prefix       | 7.1.4   |    1        |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(BELONGS_TO, DESCRIPTION_AND_REFERENCE_CHILDREN);
        mandatorySingleChildren.put(BELONGS_TO, Arrays.asList(PREFIX));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| position     | 9.7.4.2 | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(BIT, Arrays.asList(DESCRIPTION, POSITION, REFERENCE, STATUS));
        optionalMultipleChildren.put(BIT, Arrays.asList(IF_FEATURE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| choice       | 7.9     | 0..n |
        	| container    | 7.5     | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| uses         | 7.13    | 0..n |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(CASE, Arrays.asList(DESCRIPTION, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(CASE, Arrays.asList(ANYDATA, ANYXML, CHOICE, CONTAINER, IF_FEATURE, LEAF, LEAF_LIST,
                LIST, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| case         | 7.9.2   | 0..n |
        	| choice       | 7.9     | 0..n |
        	| config       | 7.21.1  | 0..1 |
        	| container    | 7.5     | 0..n |
        	| default      | 7.9.3   | 0..1 |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| mandatory    | 7.9.4   | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(CHOICE, Arrays.asList(CONFIG, DEFAULT, DESCRIPTION, MANDATORY, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(CHOICE, Arrays.asList(ANYDATA, ANYXML, CASE, CHOICE, CONTAINER, IF_FEATURE, LEAF,
                LEAF_LIST, LIST));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| action       | 7.15    | 0..n |
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| choice       | 7.9     | 0..n |
        	| config       | 7.21.1  | 0..1 |
        	| container    | 7.5     | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| grouping     | 7.12    | 0..n |
        	| if-feature   | 7.20.2  | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| must         | 7.5.3   | 0..n |
        	| notification | 7.16    | 0..n |
        	| presence     | 7.5.5   | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| typedef      | 7.3     | 0..n |
        	| uses         | 7.13    | 0..n |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(CONTAINER, Arrays.asList(CONFIG, DESCRIPTION, PRESENCE, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(CONTAINER, Arrays.asList(ACTION, ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING,
                IF_FEATURE, LEAF, LEAF_LIST, LIST, MUST, NOTIFICATION, TYPEDEF, USES));

        /*
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(DESCRIPTION, NO_CHILDREN);

        /*
        	+--------------+--------------+-------------+
        	| substatement | section      | cardinality |
        	+--------------+--------------+-------------+
        	| config       | 7.21.1       |  0..1  |
        	| default      | 7.6.4, 7.7.4 |  0..n  |
        	| mandatory    | 7.6.5        |  0..1  |
        	| max-elements | 7.7.6        |  0..1  |
        	| min-elements | 7.7.5        |  0..1  |
        	| must         | 7.5.3        |  0..n  |
        	| type         | 7.4          |  0..1  |
        	| unique       | 7.8.3        |  0..n  |
        	| units        | 7.3.3        |  0..1  |
        	+--------------+--------------+-------------+
         */
        optionalSingleChildren.put(DEVIATE, Arrays.asList(CONFIG, DESCRIPTION, MANDATORY, MAX_ELEMENTS, MIN_ELEMENTS,
                REFERENCE, TYPE, UNITS));
        optionalMultipleChildren.put(DEVIATE, Arrays.asList(DEFAULT, MUST, UNIQUE));

        /*
        	+--------------+----------+-------------+
        	| substatement | section  | cardinality |
        	+--------------+----------+-------------+
        	| description  | 7.21.3   |  0..1  |
        	| deviate      | 7.20.3.2 |  1..n  |
        	| reference    | 7.21.4   |  0..1  |
        	+--------------+----------+-------------+
         */
        optionalSingleChildren.put(DEVIATION, Arrays.asList(DESCRIPTION, REFERENCE));
        mandatoryMultipleChildren.put(DEVIATION, Arrays.asList(DEVIATE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| description  | 7.21.3  |  0..1  |
        	| if-feature   | 7.20.2  |  0..n  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	| value        | 9.6.4.2 |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(ENUM, Arrays.asList(DESCRIPTION, REFERENCE, STATUS, VALUE));
        optionalMultipleChildren.put(ENUM, Arrays.asList(IF_FEATURE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| argument     | 7.19.2  |  0..1  |
        	| description  | 7.21.3  |  0..1  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(EXTENSION, Arrays.asList(ARGUMENT, DESCRIPTION, REFERENCE, STATUS));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| description  | 7.21.3  |  0..1  |
        	| if-feature   | 7.20.2  |  0..n  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(FEATURE, Arrays.asList(DESCRIPTION, REFERENCE, STATUS));
        optionalMultipleChildren.put(FEATURE, Arrays.asList(IF_FEATURE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| action       | 7.15    |  0..n  |
        	| anydata      | 7.10    |  0..n  |
        	| anyxml       | 7.11    |  0..n  |
        	| choice       | 7.9     |  0..n  |
        	| container    | 7.5     |  0..n  |
        	| description  | 7.21.3  |  0..1  |
        	| grouping     | 7.12    |  0..n  |
        	| leaf         | 7.6     |  0..n  |
        	| leaf-list    | 7.7     |  0..n  |
        	| list         | 7.8     |  0..n  |
        	| notification | 7.16    |  0..n  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	| typedef      | 7.3     |  0..n  |
        	| uses         | 7.13    |  0..n  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(GROUPING, Arrays.asList(DESCRIPTION, REFERENCE, STATUS));
        optionalMultipleChildren.put(GROUPING, Arrays.asList(ACTION, ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING, LEAF,
                LEAF_LIST, LIST, NOTIFICATION, TYPEDEF, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| base         | 7.18.2  |  0..n  |
        	| description  | 7.21.3  |  0..1  |
        	| if-feature   | 7.20.2  |  0..n  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(IDENTITY, Arrays.asList(DESCRIPTION, REFERENCE, STATUS));
        optionalMultipleChildren.put(IDENTITY, Arrays.asList(BASE, IF_FEATURE));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.21.3  |  0..1  |
        	| prefix        | 7.1.4   |    1   |
        	| reference     | 7.21.4  |  0..1  |
        	| revision-date | 7.1.5.1 |  0..1  |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(IMPORT, Arrays.asList(DESCRIPTION, REFERENCE, REVISION_DATE));
        mandatorySingleChildren.put(IMPORT, Arrays.asList(PREFIX));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.21.3  |  0..1  |
        	| reference     | 7.21.4  |  0..1  |
        	| revision-date | 7.1.5.1 |  0..1  |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(INCLUDE, Arrays.asList(DESCRIPTION, REFERENCE, REVISION_DATE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    |  0..n  |
        	| anyxml       | 7.11    |  0..n  |
        	| choice       | 7.9     |  0..n  |
        	| container    | 7.5     |  0..n  |
        	| grouping     | 7.12    |  0..n  |
        	| leaf         | 7.6     |  0..n  |
        	| leaf-list    | 7.7     |  0..n  |
        	| list         | 7.8     |  0..n  |
        	| must         | 7.5.3   |  0..n  |
        	| typedef      | 7.3     |  0..n  |
        	| uses         | 7.13    |  0..n  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(INPUT, Arrays.asList(DESCRIPTION, REFERENCE));
        optionalMultipleChildren.put(INPUT, Arrays.asList(ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING, LEAF, LEAF_LIST,
                LIST, MUST, TYPEDEF, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| config       | 7.21.1  |  0..1  |
        	| default      | 7.6.4   |  0..1  |
        	| description  | 7.21.3  |  0..1  |
        	| if-feature   | 7.20.2  |  0..n  |
        	| mandatory    | 7.6.5   |  0..1  |
        	| must         | 7.5.3   |  0..n  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	| type         | 7.6.3   |    1   |
        	| units        | 7.3.3   |  0..1  |
        	| when         | 7.21.5  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(LEAF, Arrays.asList(CONFIG, DEFAULT, DESCRIPTION, MANDATORY, REFERENCE, STATUS, UNITS,
                WHEN));
        optionalMultipleChildren.put(LEAF, Arrays.asList(IF_FEATURE, MUST));
        mandatorySingleChildren.put(LEAF, Arrays.asList(TYPE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| config       | 7.21.1  |  0..1  |
        	| default      | 7.7.4   |  0..n  |
        	| description  | 7.21.3  |  0..1  |
        	| if-feature   | 7.20.2  |  0..n  |
        	| max-elements | 7.7.6   |  0..1  |
        	| min-elements | 7.7.5   |  0..1  |
        	| must         | 7.5.3   |  0..n  |
        	| ordered-by   | 7.7.7   |  0..1  |
        	| reference    | 7.21.4  |  0..1  |
        	| status       | 7.21.2  |  0..1  |
        	| type         | 7.4     |    1   |
        	| units        | 7.3.3   |  0..1  |
        	| when         | 7.21.5  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(LEAF_LIST, Arrays.asList(CONFIG, DESCRIPTION, MAX_ELEMENTS, MIN_ELEMENTS, ORDERED_BY,
                REFERENCE, STATUS, UNITS, WHEN));
        optionalMultipleChildren.put(LEAF_LIST, Arrays.asList(DEFAULT, IF_FEATURE, MUST));
        mandatorySingleChildren.put(LEAF_LIST, Arrays.asList(TYPE));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.19.3  | 0..1 |
        	| error-app-tag | 7.5.4.2 | 0..1 |
        	| error-message | 7.5.4.1 | 0..1 |
        	| reference     | 7.19.4  | 0..1 |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(LENGTH, Arrays.asList(DESCRIPTION, ERROR_APP_TAG, ERROR_MESSAGE, REFERENCE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| action       | 7.15    |  0..n  |
        	| anydata      | 7.10    |  0..n  |
        	| anyxml       | 7.11    |  0..n  |
        	| choice       | 7.9     |  0..n  |
        	| config       | 7.19.1  |  0..1  |
        	| container    | 7.5     |  0..n  |
        	| description  | 7.19.3  |  0..1  |
        	| grouping     | 7.11    |  0..n  |
        	| if-feature   | 7.18.2  |  0..n  |
        	| key          | 7.8.2   |  0..1  |
        	| leaf         | 7.6     |  0..n  |
        	| leaf-list    | 7.7     |  0..n  |
        	| list         | 7.8     |  0..n  |
        	| max-elements | 7.7.4   |  0..1  |
        	| min-elements | 7.7.3   |  0..1  |
        	| must         | 7.5.3   |  0..n  |
        	| notification | 7.5.3   |  0..n  |
        	| ordered-by   | 7.7.5   |  0..1  |
        	| reference    | 7.19.4  |  0..1  |
        	| status       | 7.19.2  |  0..1  |
        	| typedef      | 7.3     |  0..n  |
        	| unique       | 7.8.3   |  0..n  |
        	| uses         | 7.12    |  0..n  |
        	| when         | 7.19.5  |  0..1  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(LIST, Arrays.asList(CONFIG, DESCRIPTION, KEY, MAX_ELEMENTS, MIN_ELEMENTS, ORDERED_BY,
                REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(LIST, Arrays.asList(ACTION, ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING, IF_FEATURE,
                LEAF, LEAF_LIST, LIST, MUST, NOTIFICATION, TYPEDEF, UNIQUE, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    |  0..n  |
        	| anyxml       | 7.11    |  0..n  |
        	| augment      | 7.17    |  0..n  |
        	| choice       | 7.9     |  0..n  |
        	| contact      | 7.1.8   |  0..1  |
        	| container    | 7.5     |  0..n  |
        	| description  | 7.21.3  |  0..1  |
        	| deviation    | 7.20.3  |  0..n  |
        	| extension    | 7.19    |  0..n  |
        	| feature      | 7.20.1  |  0..n  |
        	| grouping     | 7.12    |  0..n  |
        	| identity     | 7.18    |  0..n  |
        	| import       | 7.1.5   |  0..n  |
        	| include      | 7.1.6   |  0..n  |
        	| leaf         | 7.6     |  0..n  |
        	| leaf-list    | 7.7     |  0..n  |
        	| list         | 7.8     |  0..n  |
        	| namespace    | 7.1.3   |    1   |
        	| notification | 7.16    |  0..n  |
        	| organization | 7.1.7   |  0..1  |
        	| prefix       | 7.1.4   |    1   |
        	| reference    | 7.21.4  |  0..1  |
        	| revision     | 7.1.9   |  0..n  |
        	| rpc          | 7.14    |  0..n  |
        	| typedef      | 7.3     |  0..n  |
        	| uses         | 7.13    |  0..n  |
        	| yang-version | 7.1.2   |    1   |
        	+--------------+---------+-------------+

        	Note that in YANG 1.0 the cardinality for the 'yang-version' statement is '0..1'. To remain
        	backwards-compatible, this statement remains optional during parsing.
         */
        optionalSingleChildren.put(MODULE, Arrays.asList(CONTACT, DESCRIPTION, ORGANIZATION, REFERENCE, YANG_VERSION));
        optionalMultipleChildren.put(MODULE, Arrays.asList(ANYDATA, ANYXML, AUGMENT, CHOICE, CONTAINER, DEVIATION,
                EXTENSION, FEATURE, GROUPING, IDENTITY, IMPORT, INCLUDE, LEAF, LEAF_LIST, LIST, NOTIFICATION, REVISION, RPC,
                TYPEDEF, USES));
        mandatorySingleChildren.put(MODULE, Arrays.asList(NAMESPACE, PREFIX));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.21.3  | 0..1 |
        	| error-app-tag | 7.5.4.2 | 0..1 |
        	| error-message | 7.5.4.1 | 0..1 |
        	| reference     | 7.21.4  | 0..1 |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(MUST, Arrays.asList(DESCRIPTION, ERROR_APP_TAG, ERROR_MESSAGE, REFERENCE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| choice       | 7.9     | 0..n |
        	| container    | 7.5     | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| grouping     | 7.12    | 0..n |
        	| if-feature   | 7.20.2  | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| must         | 7.5.3   | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| typedef      | 7.3     | 0..n |
        	| uses         | 7.13    | 0..n |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(NOTIFICATION, Arrays.asList(DESCRIPTION, REFERENCE, STATUS));
        optionalMultipleChildren.put(NOTIFICATION, Arrays.asList(ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING, IF_FEATURE,
                LEAF, LEAF_LIST, LIST, MUST, TYPEDEF, USES));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    |  0..n  |
        	| anyxml       | 7.11    |  0..n  |
        	| choice       | 7.9     |  0..n  |
        	| container    | 7.5     |  0..n  |
        	| grouping     | 7.12    |  0..n  |
        	| leaf         | 7.6     |  0..n  |
        	| leaf-list    | 7.7     |  0..n  |
        	| list         | 7.8     |  0..n  |
        	| must         | 7.5.3   |  0..n  |
        	| typedef      | 7.3     |  0..n  |
        	| uses         | 7.13    |  0..n  |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(OUTPUT, Arrays.asList(DESCRIPTION, REFERENCE));
        optionalMultipleChildren.put(OUTPUT, Arrays.asList(ANYDATA, ANYXML, CHOICE, CONTAINER, GROUPING, LEAF, LEAF_LIST,
                LIST, MUST, TYPEDEF, USES));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.21.3  |  0..1  |
        	| error-app-tag | 7.5.4.2 |  0..1  |
        	| error-message | 7.5.4.1 |  0..1  |
        	| modifier      | 9.4.6   |  0..1  |
        	| reference     | 7.21.4  |  0..1  |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(PATTERN, Arrays.asList(DESCRIPTION, ERROR_APP_TAG, ERROR_MESSAGE, MODIFIER, REFERENCE));

        /*
        	+---------------+---------+-------------+
        	| substatement  | section | cardinality |
        	+---------------+---------+-------------+
        	| description   | 7.21.3  |  0..1  |
        	| error-app-tag | 7.5.4.2 |  0..1  |
        	| error-message | 7.5.4.1 |  0..1  |
        	| reference     | 7.21.4  |  0..1  |
        	+---------------+---------+-------------+
         */
        optionalSingleChildren.put(RANGE, Arrays.asList(DESCRIPTION, ERROR_APP_TAG, ERROR_MESSAGE, REFERENCE));

        /*
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(REFERENCE, NO_CHILDREN);

        /*
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(REFINE, Arrays.asList(CONFIG, DESCRIPTION, MANDATORY, MAX_ELEMENTS, MIN_ELEMENTS,
                PRESENCE, REFERENCE));
        optionalMultipleChildren.put(REFINE, Arrays.asList(DEFAULT, IF_FEATURE, MUST));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| description  | 7.21.3  | 0..1 |
        	| grouping     | 7.12    | 0..n |
        	| if-feature   | 7.20.2  | 0..n |
        	| input        | 7.14.2  | 0..1 |
        	| output       | 7.14.3  | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| typedef      | 7.3     | 0..n |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(RPC, Arrays.asList(DESCRIPTION, INPUT, OUTPUT, REFERENCE, STATUS));
        optionalMultipleChildren.put(RPC, Arrays.asList(GROUPING, IF_FEATURE, TYPEDEF));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| anydata      | 7.10    | 0..n |
        	| anyxml       | 7.11    | 0..n |
        	| augment      | 7.17    | 0..n |
        	| belongs-to   | 7.2.2   |   1  |
        	| choice       | 7.9     | 0..n |
        	| contact      | 7.1.8   | 0..1 |
        	| container    | 7.5     | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| deviation    | 7.20.3  | 0..n |
        	| extension    | 7.19    | 0..n |
        	| feature      | 7.20.1  | 0..n |
        	| grouping     | 7.12    | 0..n |
        	| identity     | 7.18    | 0..n |
        	| import       | 7.1.5   | 0..n |
        	| include      | 7.1.6   | 0..n |
        	| leaf         | 7.6     | 0..n |
        	| leaf-list    | 7.7     | 0..n |
        	| list         | 7.8     | 0..n |
        	| notification | 7.16    | 0..n |
        	| organization | 7.1.7   | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| revision     | 7.1.9   | 0..n |
        	| rpc          | 7.14    | 0..n |
        	| typedef      | 7.3     | 0..n |
        	| uses         | 7.13    | 0..n |
        	| yang-version | 7.1.2   |  1   |
        	+--------------+---------+-------------+

        	Note that in YANG 1.0 the cardinality for the 'yang-version' statement is '0..1'. To remain
        	backwards-compatible, this statement remains optional during parsing.
         */
        optionalSingleChildren.put(SUBMODULE, Arrays.asList(CONTACT, DESCRIPTION, ORGANIZATION, REFERENCE, YANG_VERSION));
        optionalMultipleChildren.put(SUBMODULE, Arrays.asList(ANYDATA, ANYXML, AUGMENT, CHOICE, CONTAINER, DEVIATION,
                EXTENSION, FEATURE, GROUPING, IDENTITY, IMPORT, INCLUDE, LEAF, LEAF_LIST, LIST, NOTIFICATION, REVISION, RPC,
                TYPEDEF, USES));
        mandatorySingleChildren.put(SUBMODULE, Arrays.asList(BELONGS_TO));

        /*
        	+------------------+---------+-------------+
        	| substatement     | section | cardinality |
        	+------------------+---------+-------------+
        	| base             | 7.18.2  | 0..n |
        	| bit              | 9.7.4   | 0..n |
        	| enum             | 9.6.4   | 0..n |
        	| fraction-digits  | 9.3.4   | 0..1 |
        	| length           | 9.4.4   | 0..1 |
        	| path             | 9.9.2   | 0..1 |
        	| pattern          | 9.4.5   | 0..n |
        	| range            | 9.2.4   | 0..1 |
        	| require-instance | 9.9.3   | 0..1 |
        	| type             | 7.4     | 0..n |
        	+------------------+---------+-------------+
         */
        optionalSingleChildren.put(TYPE, Arrays.asList(DESCRIPTION, FRACTION_DIGITS, LENGTH, PATH, RANGE,
                REQUIRE_INSTANCE));
        optionalMultipleChildren.put(TYPE, Arrays.asList(BASE, BIT, ENUM, PATTERN, TYPE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| default      | 7.3.4   | 0..1 |
        	| description  | 7.21.3  | 0..1 |
        	| reference    | 7.21.4  | 0..1 |
        	| status       | 7.21.2  | 0..1 |
        	| type         | 7.3.2   |   1  |
        	| units        | 7.3.3   | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(TYPEDEF, Arrays.asList(DEFAULT, DESCRIPTION, REFERENCE, STATUS, UNITS));
        mandatorySingleChildren.put(TYPEDEF, Arrays.asList(TYPE));

        /*
        	+--------------+---------+-------------+
        	| substatement | section | cardinality |
        	+--------------+---------+-------------+
        	| augment      | 7.17    | 0..n |
        	| description  | 7.21.3  | 0..1 |
        	| if-feature   | 7.20.2  | 0..n |
        	| reference    | 7.21.4  | 0..1 |
        	| refine       | 7.13.2  | 0..n |
        	| status       | 7.21.2  | 0..1 |
        	| when         | 7.21.5  | 0..1 |
        	+--------------+---------+-------------+
         */
        optionalSingleChildren.put(USES, Arrays.asList(DESCRIPTION, REFERENCE, STATUS, WHEN));
        optionalMultipleChildren.put(USES, Arrays.asList(AUGMENT, IF_FEATURE, REFINE));
    }

    public static final Map<String, List<String>> HANDLED_STATEMENTS = new HashMap<>();

    static {
        HANDLED_STATEMENTS.put(YANG_CORE_MODULE_NAME, new ArrayList<>(ALL_YANG_CORE_STATEMENT_NAMES));
    }
}
