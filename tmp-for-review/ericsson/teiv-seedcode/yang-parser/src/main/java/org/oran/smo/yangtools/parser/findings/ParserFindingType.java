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
package org.oran.smo.yangtools.parser.findings;

/**
 * A collection of finding types by the parser.
 * <p/>
 * Note that although these are defined as an enum, the finding type is of data type string. It is
 * therefore possible to use finding types declared outside of this enum.
 *
 * @author Mark Hollmann
 */
public enum ParserFindingType {

    // Basic processing errors

    /**
     * Usually thrown if there is a code error, typically a NPE caused by the model being really bad.
     */
    P000_UNSPECIFIED_ERROR,
    /**
     * Couldn't read the file. Or the stream.
     */
    P001_BASIC_FILE_READ_ERROR,
    /**
     * The type-safe class for an extension does not have the correct constructor.
     */
    P002_INVALID_EXTENSION_STATEMENT_CLASS,
    /**
     * The exact same input has been supplied twice. Usually means the exact same file has been
     * supplied twice for parsing.
     */
    P003_DUPLICATE_INPUT,
    /**
     * A module may only be implemented once by a server. It is possible that the same module, of
     * different revisions, is listed multiple times in the input - but then only one of them can
     * be conformance IMPLEMENT, the other ones must be conformance IMPORT.
     */
    P004_SAME_MODULE_DUPLICATE_IMPLEMENTS,
    /**
     * There is no module in the input that has a conformance of IMPLEMENTS. Hence there will be
     * no data nodes in the schema, which is rather pointless.
     */
    P005_NO_IMPLEMENTS,
    /**
     * There is a mismatch in conformance type between a module and its submodules.
     */
    P006_IMPLEMENT_IMPORT_MISMATCH,

    /**
     * Fail-fast. Denotes that during parsing some issues were found that are so severe that it
     * does not really make sense to keep processing the schema, and the parser exists out.
     */
    P009_FAIL_FAST,

    // Basic syntax errors not relating to any particular statement or language construct.

    /**
     * There are certain rules in YANG about how characters can be escaped in quoted text.
     */
    P011_INVALID_CHARACTER_ESCAPING_IN_QUOTED_TEXT,
    /**
     * Same, just for unquoted text.
     */
    P012_INVALID_CHARACTER_IN_UNQUOTED_TEXT,
    /**
     * Something wrong at the start of the document. May also indicate that the file is not
     * Yang at all but something else entirely.
     */
    P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT,
    /**
     * Usually due to a mismatch of curly braces.
     */
    P014_INVALID_SYNTAX_AT_DOCUMENT_END,
    /**
     * Bit of a catch-all finding for any syntax that is wrong in a document (not just Yang,
     * also XML/JSON documents).
     */
    P015_INVALID_SYNTAX_IN_DOCUMENT,
    /**
     * This statement is not allowed here.
     */
    P018_ILLEGAL_CHILD_STATEMENT,
    /**
     * A mandatory statement is missing.
     */
    P019_MISSING_REQUIRED_CHILD_STATEMENT,

    /*
     *
     */
    P025_INVALID_EXTENSION,

    // The following relate to prefix, import's etc.

    /**
     * In a YAM a declared prefix is not unique. This is a serious issue.
     */
    P031_PREFIX_NOT_UNIQUE,
    /**
     * A YAM does not have a revision. While this is allowed in Yang, it is very poor modeling.
     */
    P032_MISSING_REVISION,
    /**
     * A prefix is used and cannot be resolved. This is usually a typo in the prefix.
     */
    P033_UNRESOLVEABLE_PREFIX,
    /**
     * There is an 'import' statement, but the module has not been supplied in the input.
     */
    P034_UNRESOLVABLE_IMPORT,
    /**
     * Yang allows for multiple revisions of the same name to be part of the schema, as long as
     * only at most one of them is of conformance IMPLEMENT. In those situations, other modules
     * importing such modules must use an explicit revision-date.
     */
    P035_AMBIGUOUS_IMPORT,
    /**
     * A YAM is importing the same module (possibly of different revision) multiple times.
     */
    P036_MODULE_IMPORTED_MULTIPLE_TIMES,
    /**
     * A module includes a submodule, but the submodule has not been found in the input.
     */
    P037_UNRESOLVABLE_INCLUDE,
    /**
     * Multiple revisions of a subnmodule are in the input. Not allowed.
     */
    P038_AMBIGUOUS_INCLUDE,
    /**
     * A submodule refers to a module, but the module is not in the input.
     */
    P039_UNRESOLVABLE_BELONGS_TO,
    /**
     * Submodules include each other. Extremely bad modeling.
     */
    P040_CIRCULAR_INCLUDE_REFERENCES,
    /**
     * There is a mix of Yang 1 and Yang 1.1 versions between the module and its submodule(s).
     * We don't really care, but technically that's not allowed by the spec.
     */
    P041_DIFFERENT_YANG_VERSIONS_BETWEEN_MODULE_AND_SUBMODULES,
    /**
     * A YAM of conformance IMPORT is in the input but not referred-to from any other module.
     * Not really an issue, but should be removed.
     */
    P042_UNREFERENCED_IMPORTED_FILE,
    /**
     * If a module of different revisions is supplied more than once in the input, only one of
     * them can have conformance IMPLEMENT.
     */
    P043_SAME_MODULE_IMPLEMENTS_MORE_THAN_ONCE,
    /**
     * The same module is supplied as both conformance IMPLEMENT and IMPORT. That is not
     * necessarily a problem, it may well be intentioned to be that way.
     */
    P044_SAME_MODULE_IMPLEMENTS_AND_IMPORTS,
    /**
     * An 'include' resolves to a module, not a submodule.
     */
    P045_NOT_A_SUBMODULE,
    /**
     * An 'belongs-to' resolves to a submodule, not a module.
     */
    P046_NOT_A_MODULE,
    /**
     * There is a mismatch between the 'include' and 'belongs-to' statements between a module
     * and a submodule.
     */
    P047_SUBMODULE_OWNERSHIP_MISMATCH,
    /**
     * A submodule is in the input, but its owning module is not in the input.
     */
    P048_ORPHAN_SUBMODULE,
    /**
     * Two 'revision' statements (not latest) inside a YAM have the same date.
     */
    P049_DUPLICATE_REVISION,
    /**
     * Two 'revision' statements (latest) inside a YAM have the same date. This is potentially
     * a very serious problem. It indicates that the module content was updated, the revision
     * statement was copied/pasted, but the date not updated. As a consequence there might be
     * two modules in circulation having different content but the same revision date.
     */
    P050_DUPLICATE_LATEST_REVISION,

    // Other generic YANG issues

    /**
     * The cardinality of a statement is incorrect.
     */
    P051_INVALID_STATEMENT_CARDINALITY,
    /**
     * Syntax error on a Yang identifier. Yang only allows a relatively small set of non-alphanumeric
     * characters.
     */
    P052_INVALID_YANG_IDENTIFIER,
    /**
     * A value is used part of a statement, but the value is not valid in the context where it
     * is used.
     */
    P053_INVALID_VALUE,
    /**
     * A path could not be resolved. This is usually due to typos, or the model designer forgetting
     * to include all schema node names along the path. There is possibly also a prefix missing as
     * part of one of the path elements.
     */
    P054_UNRESOLVABLE_PATH,
    /**
     * Stuff in the YAM that need not be there.
     */
    P055_SUPERFLUOUS_STATEMENT,
    /**
     * TODO - check this.
     */
    P056_CONSTRAINT_NARROWED,
    /**
     * It is possible to change the data type of data nodes by means of a deviation. This is likely
     * to cause problems for clients that expect the original data type.
     */
    P057_DATA_TYPE_CHANGED,

    // Relating to instance data

    P064_ANNOTATION_USAGE,
    P065_CANNOT_CONVERT,
    P066_NO_SETTER,
    P067_NOT_SINGLE_INSTANCE,

    // Relating to instance data

    /**
     * During parsing of annotation data a mismatch between leaf/leaf-list data and annotations
     * was found.
     */
    P069_UNEXPECTED_JSON_VALUE,
    /**
     * During parsing of JSON data a JSON element of the wrong type was encountered (e.g. an
     * array where an object was expected)
     */
    P070_WRONG_JSON_VALUE_TYPE,
    /**
     * The root XML element of an XML file containing data is wrong. Only certain elements are
     * supported.
     */
    P071_INCORRECT_ROOT_ELEMENT_OF_DATA_FILE,
    /**
     * When building an instance data tree the key value for a list instance was not found.
     */
    P072_MISSING_KEY_VALUE,
    /**
     * During the merge of data from different sources into an instance data tree a leaf was
     * encountered whose value differs between the inputs.
     */
    P073_LEAF_VALUE_ALREADY_SET,
    /**
     * Data has been attempted to be set for a schema node that is not a data node.
     */
    P074_NOT_A_DATA_NODE,
    /**
     * Data has been supplied for a data node that has not been found in the schema.
     */
    P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND,
    /**
     * The same data node is declared more than once in the data (e.g., the same leaf is listed
     * twice in the data).
     */
    P076_DUPLICATE_INSTANCE_DATA,
    /**
     * There is a prefix in the XML file that has not been declared.
     */
    P077_UNRESOLVABLE_PREFIX,
    /**
     * Nothing in the data file.
     */
    P079_EMPTY_DATA_FILE,
    /**
     * A null value was encountered when building the instance data tree.
     */
    P080_NULL_VALUE,

    // Yang Library

    /**
     * Some data in the Yang Library is wrong.
     */
    P081_INCORRECT_YANG_LIBRARY_DATA,
    /**
     * A mandatory piece of information from the data.
     */
    P082_YANG_LIBRARY_MANDATORY_VALUE_MISSING,
    /**
     * There is a feature listed inside the Yang Library, but this feature has not been defined
     * in the corresponding module.
     */
    P083_FEATURE_LISTED_IN_YANG_LIBRARY_NOT_FOUND,
    /**
     * There is more than one YL in the input
     */
    P084_MULTIPLE_YANG_LIBRARIES_IN_INPUT,
    /**
     * What has been supplied as input in terms of modules does not match up with the modules
     * listed in the Yang Library.
     */
    P085_MISMATCH_BETWEEN_INPUT_MODULES_AND_YANG_LIBRARY,
    /**
     * A feature has been marked as supported, but it depends on other features that are not supported.
     */
    P086_FEATURE_CANNOT_BE_SUPPORTED,

    // Collector

    /**
     * The same module has been found twice by the collector.
     */
    P091_COLLECTOR_DUPLICATE_INPUT,
    /**
     * The collector found an input that does not appear to be a valid YAM.
     */
    P092_COLLECTOR_NOT_A_VALID_YAM,
    /**
     * A dependent module was not found by a collector.
     */
    P093_COLLECTOR_MODULE_NOT_FOUND,

    // NACM

    /**
     * Some data in NACM is wrong.
     */
    P096_INCORRECT_NACM_DATA,

    // Specific issues, general statements

    /**
     * A description statement has no content (which is a bit pointless)
     */
    P101_EMPTY_DOCUMENTATION_VALUE,
    /**
     * A schema node has an illegal status
     */
    P102_INVALID_STATUS,
    /**
     * The syntax of an 'if-feature' statement is wrong.
     */
    P103_ILLEGAL_IF_FEATURE_SYNTAX,
    /**
     * A deprecated statement is being used.
     */
    P104_USAGE_OF_DEPRECATED_ELEMENT,

    // Specific issues: typedef

    /**
     * typedefs have a circular dependency.
     */
    P111_CIRCULAR_TYPEDEF_REFERENCES,
    /**
     * There is a lot of nesting of typedefs. Typically makes the model unreadable.
     */
    P112_EXCESSIVE_TYPEDEF_DEPTH,
    /**
     * A derived type could not be resolved.
     */
    P113_UNRESOLVABLE_DERIVED_TYPE,
    /**
     * A typedef is not being used. Not necessarily an issue, but might indicate a buggy model.
     */
    P114_TYPEDEF_NOT_USED,
    /**
     * A typedef is only used once. Doesn't really make sense, why not simply inline the content?
     */
    P115_TYPEDEF_USED_ONCE_ONLY,
    /**
     * A derived type refers to yet another derived type, but that one is not resolvable.
     */
    P116_NESTED_DERIVED_TYPE_NOT_RESOLVABLE,
    /**
     * A derived type is restricting the base type in illegal manner.
     */
    P117_ILLEGAL_DATA_TYPE_RESTRICTION,

    // Specific issues: uses / grouping

    /**
     * uses have a circular dependency.
     */
    P121_CIRCULAR_USES_REFERENCES,
    /**
     * There is a lot of nesting of uses/grouping. Typically makes the model unreadable.
     */
    P122_EXCESSIVE_USES_DEPTH,
    /**
     * An 'augment' part of a 'uses' augments a schema node that may not be augmented.
     */
    P123_INVALID_USES_AUGMENT_TARGET_NODE,
    /**
     * A 'refine' part of a 'uses' refines a schema node that may not be refined.
     */
    P124_INVALID_REFINE_TARGET_NODE,
    /**
     * A uses statement could be resolved to a grouping.
     */
    P131_UNRESOLVABLE_GROUPING,
    /**
     * A grouping is not being used.
     */
    P132_GROUPING_NOT_USED,
    /**
     * A grouping is used only once (why not inline?)
     */
    P133_GROUPING_USED_ONCE_ONLY,
    /**
     * A 'uses' points to a 'grouping' that itself has a 'uses', and that 'uses' is not resolvable.
     */
    P134_NESTED_USES_NOT_RESOLVABLE,

    // Specific issues: enum / bits

    /**
     * There is a whitespace in the name of an enum member. While this is technically allowed by
     * the RFC, it is very bad modeling, as it goes against the coding convention of every major
     * programming language and it will lead to problems downstream somewhere.
     */
    P141_WHITESPACE_IN_ENUM,
    /**
     * An enum member name contains characters that are usually disallowed as part of enums in
     * major programming languages. Expect issues downstream.
     */
    P142_UNUSUAL_CHARACTERS_IN_ENUM,
    /**
     * An enum member does not have a value. This is allowed in Yang as the values can be auto-generated,
     * but it is dangerous as a new / removed enum inside the enumeration will cause the auto-generated
     * values to change in an incompatible manner.
     */
    P143_ENUM_WITHOUT_VALUE,
    /**
     * A bit does not have a position. This is allowed in Yang as the positions can be auto-generated,
     * but it is dangerous as a new / removed bits cause the auto-generated positions to change in an
     * incompatible manner.
     */
    P144_BIT_WITHOUT_POSITION,

    // Specific issues: augmentation

    /**
     * An 'augment's target schema node may not be augmented.
     */
    P151_TARGET_NODE_CANNOT_BE_AUGMENTED,
    /**
     * The 'augment' and its target sit in the same module. Bad modeling.
     */
    P152_AUGMENT_TARGET_NODE_IN_SAME_MODULE,

    // Specific issues: deviation

    /**
     * Invalid operation for a deviate.
     */
    P161_INVALID_DEVIATE_OPERATION,
    /**
     * The 'deviation' and its target sit in the same module. Bad modeling.
     */
    P162_DEVIATION_TARGET_NODE_IN_SAME_MODULE,
    /**
     * There are two 'deviate replace' statements that modify the exact same schema node.
     */
    P163_AMBIGUOUS_DEVIATE_REPLACE_OF_SAME_STATEMENT,
    /**
     * A statement that has been added previously has now been replaced.
     */
    P164_DEVIATE_REPLACE_OF_DEVIATE_ADDED_STATEMENT,
    /**
     * A statement that has been modified via a deviation has now also beed deleted.
     */
    P165_DEVIATE_DELETE_OF_DEVIATED_STATEMENT,
    /**
     * The deviate would result in a statement cardinality validation.
     */
    P166_DEVIATE_RESULTS_IN_CHILD_CARDINALITY_VIOLATION,
    /**
     * Certain statements cannot be added or deleted, only replaced. This is a hint.
     */
    P167_CANNOT_USE_UNDER_DEVIATE_ADD_OR_DELETE

    /*
     * Values 200+ reserved for downstream tooling, so don't go beyond P199
     */
}
