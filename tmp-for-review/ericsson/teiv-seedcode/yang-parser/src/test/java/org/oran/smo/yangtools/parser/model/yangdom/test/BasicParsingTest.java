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
package org.oran.smo.yangtools.parser.model.yangdom.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YInput;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YRpc;
import org.oran.smo.yangtools.parser.model.statements.yang.YSubmodule;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.testutils.YangTestCommon;

public class BasicParsingTest extends YangTestCommon {

    @Test
    public void test_empty_file() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/empty-file.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_semicolon_only() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/semicolon-only.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_hello_world() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/hello-world.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_no_left_brace() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/no-left-brace.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_junk_at_end() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/junk-at-end.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("junk-at-end-module").size() == 1);
    }

    @Test
    public void test_block_comments() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/block-comments.yang"));
        assertNoFindings();

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("block-comments-module").size() == 1);
    }

    @Test
    public void test_line_comments() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/line-comments.yang"));
        assertNoFindings();

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("line-comment-module").size() == 1);
    }

    @Test
    public void test_simple_module() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/simple-module.yang"));

        final YModule simpleModule = getModule("simple-module");
        assertTrue(simpleModule != null);

        final YangDomElement moduleDomElement = simpleModule.getDomElement();
        assertTrue(moduleDomElement.toString().equals("module simple-module"));
        assertTrue(moduleDomElement.hashCode() == "'module simple-module'".hashCode());
        assertTrue(moduleDomElement.getTrimmedValueOrNull().equals("simple-module"));

        final YRpc rpc1 = getRpc(simpleModule, "rpc1");
        assertTrue(rpc1.getDomElement().getNameValue().equals("'rpc rpc1'"));
        assertTrue(rpc1.getDomElement().getNameValue().equals("'rpc rpc1'"));

        final YInput input1 = rpc1.getInput();
        assertTrue(input1.getDomElement().getNameValue().equals("'input'"));
        assertTrue(input1.getDomElement().getNameValue().equals("'input'"));
        assertTrue(input1.getDomElement().getTrimmedValueOrNull() == null);
        assertTrue(input1.getDomElement().getParentElement() == rpc1.getDomElement());

        assertTrue(input1.getDomElement().getChildren().size() == 2);
        input1.getDomElement().getChildren().get(1).remove();
        assertTrue(input1.getDomElement().getChildren().size() == 1);
        assertTrue(input1.getDomElement().getChildren().get(0).getValue().equals("leaf1"));

        assertTrue(input1.getDomElement().getSimplifiedPath().equals("/module=simple-module/rpc=rpc1/input"));

        final YLeaf leaf4 = getLeaf(simpleModule, "leaf4");
        assertTrue(leaf4.getDomElement().getSimplifiedPath().equals("/module=simple-module/leaf=leaf4"));

        assertTrue(leaf4.getWhens().get(0).getDomElement().getSimplifiedPath().equals(
                "/module=simple-module/leaf=leaf4/when=(../leaf3)"));

        assertTrue(getLeaf(simpleModule, "leaf5").getDescription().getValue().equals("Hello World"));
        assertTrue(getLeaf(simpleModule, "leaf6").getDescription().getValue().equals("Hello World"));
        assertTrue(getLeaf(simpleModule, "leaf7").getDescription().getValue().equals("Hello World!"));
    }

    @Test
    public void test_multiple_semicolons() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/multiple-semicolons.yang"));
        assertHasFindingOfType(ParserFindingType.P055_SUPERFLUOUS_STATEMENT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("multiple-semicolons").size() == 1);
    }

    @Test
    public void test_multiple_plus() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/multiple-plus.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
    }

    @Test
    public void test_quoted_plus_unquoted() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/quoted-plus-unquoted.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
    }

    @Test
    public void test_unquoted_plus_unquoted() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/unquoted-plus-unquoted.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_document_end_missing() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/document-end-missing.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 1);
    }

    @Test
    public void test_double_left_brace() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/double-left-brace.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_three_statements() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/three-statements.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_mult_statements_at_root() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/multiple-statements-at-root.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_weird_root() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/weird-root.yang"));
        assertHasFindingOfType(ParserFindingType.P013_INVALID_SYNTAX_AT_DOCUMENT_ROOT.toString());

        assertTrue(yangDeviceModel.getModuleRegistry().getAllYangModels().size() == 0);
    }

    @Test
    public void test_missing_and_ducplicate_statements() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-and-duplicate-statements.yang"));

        final YModule module = getModule("missing-and-duplicate-statements");
        assertTrue(module != null);

        final YContainer cont1 = getContainer(module, "cont1");
        assertDomElementHasFindingOfType(cont1.getDomElement(), ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());

        final YLeaf leaf1 = getLeaf(cont1, "leaf1");
        assertDomElementHasFindingOfType(leaf1.getDomElement(), ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString());
        assertStatementHasFindingOfType(leaf1.getMandatory(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        final YLeaf leaf2 = getLeaf(cont1, "leaf2");
        assertStatementHasFindingOfType(leaf2.getType().getBases().get(0), ParserFindingType.P052_INVALID_YANG_IDENTIFIER
                .toString());

        final YLeaf leaf3 = getLeaf(cont1, "leaf3");
        assertStatementHasFindingOfType(leaf3.getMandatory(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        final YContainer contWithWeirdCharacters = getContainer(module, "cont2_with_weird_%%_characters");
        assertStatementHasFindingOfType(contWithWeirdCharacters, ParserFindingType.P052_INVALID_YANG_IDENTIFIER.toString());

        final YContainer cont3 = getContainer(module, "cont3");
        assertDomElementHasFindingOfType(cont3.getDomElement().getChildren().get(0),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertDomElementHasFindingOfType(cont3.getDomElement().getChildren().get(1),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());

        assertStatementHasFindingOfType(module.getDeviations().get(0),
                ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());
    }

    @Test
    public void test_missing_import_name() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-import-name.yang"));

        final YModule module = getModule("missing-import-name");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-import-name").size() == 1);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
    }

    @Test
    public void test_missing_prefix_under_import() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix-under-import.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/basic-empty-module.yang"));

        final YModule module = getModule("missing-prefix-under-import");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-prefix-under-import").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-empty-module").size() == 1);

        assertStatementHasFindingOfType(module.getImports().get(0), ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT
                .toString());
    }

    @Test
    public void test_missing_prefix_name_under_import() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix-name-under-import.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/basic-empty-module.yang"));

        final YModule module = getModule("missing-prefix-name-under-import");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-prefix-name-under-import").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-empty-module").size() == 1);

        assertStatementHasFindingOfType(module.getImports().get(0).getPrefix(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_missing_include_name() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-include-name.yang"));

        final YModule module = getModule("missing-include-name");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-include-name").size() == 1);

        assertStatementHasFindingOfType(module.getIncludes().get(0), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
    }

    @Test
    public void test_missing_belongsto() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/basic-module-including.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/missing-belongsto.yang"));

        final YModule module = getModule("basic-module-including");
        assertTrue(module != null);
        final YSubmodule submodule = getSubModule("basic-submodule");
        assertTrue(submodule != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-module-including").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-submodule").size() == 1);

        assertStatementHasFindingOfType(submodule, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());
    }

    @Test
    public void test_missing_belongsto_name() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/basic-module-including.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/missing-belongsto-name.yang"));

        final YModule module = getModule("basic-module-including");
        assertTrue(module != null);
        final YSubmodule submodule = getSubModule("basic-submodule");
        assertTrue(submodule != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-module-including").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-submodule").size() == 1);

        assertStatementHasFindingOfType(submodule.getBelongsTo(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
    }

    @Test
    public void test_missing_prefix_under_belongsto() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/basic-module-including.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix-under-belongsto.yang"));

        final YModule module = getModule("basic-module-including");
        assertTrue(module != null);
        final YSubmodule submodule = getSubModule("basic-submodule");
        assertTrue(submodule != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-module-including").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-submodule").size() == 1);

        assertStatementHasFindingOfType(submodule.getBelongsTo(), ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT
                .toString());
    }

    @Test
    public void test_missing_prefix_name_under_belongsto() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/basic-module-including.yang",
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix-name-under-belongsto.yang"));

        final YModule module = getModule("basic-module-including");
        assertTrue(module != null);
        final YSubmodule submodule = getSubModule("basic-submodule");
        assertTrue(submodule != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-module-including").size() == 1);
        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("basic-submodule").size() == 1);

        assertStatementHasFindingOfType(submodule.getBelongsTo().getPrefix(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

    @Test
    public void test_missing_prefix() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix.yang"));

        final YModule module = getModule("missing-prefix");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-prefix").size() == 1);

        assertStatementHasFindingOfType(module, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());
    }

    @Test
    public void test_missing_prefix_name() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-prefix-name.yang"));

        final YModule module = getModule("missing-prefix-name");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-prefix-name").size() == 1);

        assertStatementHasFindingOfType(module.getPrefix(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
        assertFindingCount(1);
    }

    @Test
    public void test_missing_namespace() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-namespace.yang"));

        final YModule module = getModule("missing-namespace");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-namespace").size() == 1);

        assertStatementHasFindingOfType(module, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString());
        assertFindingCount(1);
    }

    @Test
    public void test_missing_namespace_name() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-namespace-name.yang"));

        final YModule module = getModule("missing-namespace-name");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-namespace-name").size() == 1);

        assertStatementHasFindingOfType(module.getNamespace(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertFindingCount(1);
    }

    @Test
    public void test_missing_yangversion_version() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/missing-yangversion-version.yang"));

        final YModule module = getModule("missing-yangversion-version");
        assertTrue(module != null);

        assertTrue(yangDeviceModel.getModuleRegistry().byModuleName("missing-yangversion-version").size() == 1);

        assertStatementHasFindingOfType(module.getYangVersion(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                .toString());
        assertFindingCount(1);
    }

    @Test
    public void test_dangling_block_comment() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/dangling-block-comment.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());
    }

    @Test
    public void test_dangling_double_quote() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/dangling-double-quote.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());
    }

    @Test
    public void test_dangling_single_quote() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/dangling-single-quote.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());
    }

    @Test
    public void test_dangling_plus_token() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/dangling-plus-token.yang"));
        assertHasFindingOfType(ParserFindingType.P014_INVALID_SYNTAX_AT_DOCUMENT_END.toString());
    }

    @Test
    public void test_incorrect_string_concatenation() {
        parseAbsoluteImplementsYangModels(Arrays.asList(
                "src/test/resources/model-yangdom/basic-parsing-test/incorrect-string-concatenation.yang"));
        assertHasFindingOfType(ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT.toString());
    }

}
