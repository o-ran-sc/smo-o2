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
package org.oran.smo.yangtools.parser.testutils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNodeAnnotationValue;
import org.oran.smo.yangtools.parser.data.instance.AbstractStructureInstance;
import org.oran.smo.yangtools.parser.data.instance.AnyDataInstance;
import org.oran.smo.yangtools.parser.data.instance.AnyXmlInstance;
import org.oran.smo.yangtools.parser.data.instance.ContainerInstance;
import org.oran.smo.yangtools.parser.data.instance.DataTreeBuilderPredicate;
import org.oran.smo.yangtools.parser.data.instance.LeafInstance;
import org.oran.smo.yangtools.parser.data.instance.LeafListInstance;
import org.oran.smo.yangtools.parser.data.instance.ListInstance;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObject;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonObjectMemberName;
import org.oran.smo.yangtools.parser.data.parser.JsonParser.JsonValue;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingFilterPredicate;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ModuleAndFindingTypeAndSchemaNodePathFilterPredicate;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.input.FileBasedYangInputResolver;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.schema.ModuleRegistry;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.ExtensionStatement;
import org.oran.smo.yangtools.parser.model.statements.StatementModuleAndName;
import org.oran.smo.yangtools.parser.model.statements.YangModelRoot;
import org.oran.smo.yangtools.parser.model.statements.ietf.IetfExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.oran.OranExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.threegpp.ThreeGppExtensionsClassSupplier;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YAction;
import org.oran.smo.yangtools.parser.model.statements.yang.YAugment;
import org.oran.smo.yangtools.parser.model.statements.yang.YCase;
import org.oran.smo.yangtools.parser.model.statements.yang.YChoice;
import org.oran.smo.yangtools.parser.model.statements.yang.YContainer;
import org.oran.smo.yangtools.parser.model.statements.yang.YDeviation;
import org.oran.smo.yangtools.parser.model.statements.yang.YFeature;
import org.oran.smo.yangtools.parser.model.statements.yang.YGrouping;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeaf;
import org.oran.smo.yangtools.parser.model.statements.yang.YLeafList;
import org.oran.smo.yangtools.parser.model.statements.yang.YList;
import org.oran.smo.yangtools.parser.model.statements.yang.YModule;
import org.oran.smo.yangtools.parser.model.statements.yang.YNotification;
import org.oran.smo.yangtools.parser.model.statements.yang.YRpc;
import org.oran.smo.yangtools.parser.model.statements.yang.YSubmodule;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.statements.yang.YTypedef;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

public class YangTestCommon {

    public static final String YANG_FILE_EXTENSION = ".yang";

    protected static final String TARGET_DIR = "target/test-output";

    private static final String YANG_TEST_FILES = "src/test/resources/model-statements-yang/";

    protected static final String ORIG_MODULES_PATH = "src/test/resources/_orig-modules/";

    protected static final String THREEGPP_YANG_EXT_PATH = ORIG_MODULES_PATH + "_3gpp-common-yang-extensions-2022-10-20.yang";
    protected static final String YANG_METADATA_PATH = ORIG_MODULES_PATH + "ietf-yang-metadata-2016-08-05.yang";
    protected static final String YANG_ORIGIN_PATH = ORIG_MODULES_PATH + "ietf-origin-2018-02-14.yang";
    protected static final String NETCONF_ACM_PATH = ORIG_MODULES_PATH + "ietf-netconf-acm-2018-02-14.yang";

    protected YangDeviceModel yangDeviceModel;
    protected ModifyableFindingSeverityCalculator severityCalculator;
    protected FindingsManager findingsManager;
    protected ParserExecutionContext context;

    @Before
    public void setUp() {
        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");

        severityCalculator = new ModifyableFindingSeverityCalculator();

        findingsManager = new FindingsManager(severityCalculator);
        findingsManager.addFilterPredicate(ModuleAndFindingTypeAndSchemaNodePathFilterPredicate.fromString(
                "ietf*,iana*;*;*"));

        createContext();

        context.setFailFast(false);
        context.setSuppressFindingsOnUnusedSchemaNodes(true);
    }

    protected void createContext() {

        final ThreeGppExtensionsClassSupplier threeGppStatementFactory = new ThreeGppExtensionsClassSupplier();
        final IetfExtensionsClassSupplier ietfStatementFactory = new IetfExtensionsClassSupplier();
        final OranExtensionsClassSupplier oranStatementFactory = new OranExtensionsClassSupplier();

        context = new ParserExecutionContext(findingsManager, Arrays.asList(threeGppStatementFactory, oranStatementFactory,
                ietfStatementFactory));
    }

    protected void suppressAllExcept(final String findingType) {
        suppressAllExcept(Collections.singletonList(findingType));
    }

    protected void suppressAllExcept(final List<String> findingTypes) {

        final FindingFilterPredicate predicate = new FindingFilterPredicate() {
            @Override
            public boolean test(Finding t) {
                return !findingTypes.contains(t.getFindingType());
            }
        };

        findingsManager.addFilterPredicate(predicate);
    }

    protected void parseAbsoluteImplementsYangModels(final List<String> absoluteImplementsFilePaths) {
        parseImplementsYangModels(Collections.<String> emptyList(), absoluteImplementsFilePaths);
    }

    protected void parseRelativeImplementsYangModels(final List<String> relativeImplementsFilePaths) {
        parseImplementsYangModels(relativeImplementsFilePaths, Collections.<String> emptyList());
    }

    protected void parseImplementsYangModels(final List<String> relativeImplementsFilePaths,
            final List<String> absoluteImplementsFilePaths) {
        parseYangModels(relativeImplementsFilePaths, absoluteImplementsFilePaths, Collections.<String> emptyList(),
                Collections.<String> emptyList());
    }

    protected void parseRelativeYangModels(final List<String> relativeImplementsFilePaths,
            final List<String> relativeImportFilePaths) {
        parseYangModels(relativeImplementsFilePaths, Collections.<String> emptyList(), relativeImportFilePaths, Collections
                .<String> emptyList());
    }

    protected void parseAbsoluteYangModels(final List<String> absoluteImplementsFilePaths,
            final List<String> absoluteImportFilePaths) {
        parseYangModels(Collections.<String> emptyList(), absoluteImplementsFilePaths, Collections.<String> emptyList(),
                absoluteImportFilePaths);
    }

    protected void parseYangModels(final List<String> relativeImplementsFilePaths,
            final List<String> absoluteImplementsFilePaths, final List<String> relativeImportsFilePaths,
            final List<String> absoluteImportsFilePaths) {

        final List<YangModel> yangFiles = new ArrayList<>();

        for (final String relativeImplementsFilePath : relativeImplementsFilePaths) {
            yangFiles.add(new YangModel(new FileBasedYangInput(new File(YANG_TEST_FILES + relativeImplementsFilePath)),
                    ConformanceType.IMPLEMENT));
        }
        for (final String absoluteImplementsFilePath : absoluteImplementsFilePaths) {
            yangFiles.add(new YangModel(new FileBasedYangInput(new File(absoluteImplementsFilePath)),
                    ConformanceType.IMPLEMENT));
        }

        for (final String relativeImportsFilePath : relativeImportsFilePaths) {
            yangFiles.add(new YangModel(new FileBasedYangInput(new File(YANG_TEST_FILES + relativeImportsFilePath)),
                    ConformanceType.IMPORT));
        }
        for (final String absoluteImportsFilePath : absoluteImportsFilePaths) {
            yangFiles.add(new YangModel(new FileBasedYangInput(new File(absoluteImportsFilePath)), ConformanceType.IMPORT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        /*
         * There should NEVER be a P000 finding, this would indicate a null objects wasn't handled somewhere.
         */
        assertHasNotFindingOfType(ParserFindingType.P000_UNSPECIFIED_ERROR.toString());
    }

    protected void parseRelativeYangData(final List<String> relativeFilePaths) {
        final List<File> collect = relativeFilePaths.stream().map(relpath -> new File(YANG_TEST_FILES + relpath)).collect(
                Collectors.toList());
        yangDeviceModel.parseYangData(context, new FileBasedYangInputResolver(collect), new DataTreeBuilderPredicate());
    }

    protected void parseAbsoluteYangData(final List<String> absoluteFilePaths) {
        final List<File> collect = absoluteFilePaths.stream().map(abspath -> new File(abspath)).collect(Collectors
                .toList());
        yangDeviceModel.parseYangData(context, new FileBasedYangInputResolver(collect), new DataTreeBuilderPredicate());
    }

    /**
     * Get module from yangDeviceModel
     */
    public YModule getModule(final String moduleName) {
        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();

        for (final YangModel yangModelFile : moduleRegistry.getAllYangModels()) {
            final YangModelRoot yangModelRoot = yangModelFile.getYangModelRoot();
            final YModule yModule = yangModelRoot.getModule();
            if ((yModule != null) && (yModule.getModuleName().equals(moduleName))) {
                return yModule;
            }
        }
        return null;
    }

    /**
     * Get sub-module from yangDeviceModel
     */
    public YSubmodule getSubModule(final String subModuleName) {

        final ModuleRegistry moduleRegistry = yangDeviceModel.getModuleRegistry();
        for (final YangModel yangModelFile : moduleRegistry.getAllYangModels()) {
            final YangModelRoot yangModelRoot = yangModelFile.getYangModelRoot();
            final YSubmodule ySubModule = yangModelRoot.getSubmodule();
            if (ySubModule != null && ySubModule.getSubmoduleName().equals(subModuleName)) {
                return ySubModule;
            }
        }
        return null;
    }

    public static YRpc getRpc(final AbstractStatement parent, final String rpcName) {
        return getChild(parent, CY.RPC, rpcName);
    }

    public static YContainer getContainer(final AbstractStatement parent, final String containerName) {
        return getChild(parent, CY.CONTAINER, containerName);
    }

    public static YList getList(final AbstractStatement parent, final String listName) {
        return getChild(parent, CY.LIST, listName);
    }

    public static YNotification getNotification(final AbstractStatement parent, final String notificationName) {
        return getChild(parent, CY.NOTIFICATION, notificationName);
    }

    public static YChoice getChoice(final AbstractStatement parent, final String choiceName) {
        return getChild(parent, CY.CHOICE, choiceName);
    }

    public static YCase getCase(final YChoice parent, final String caseName) {
        return getChild(parent, CY.CASE, caseName);
    }

    public static YGrouping getGrouping(final YModule module, final String groupingName) {
        return getChild(module, CY.GROUPING, groupingName);
    }

    public static YLeaf getLeaf(final AbstractStatement parent, final String leafName) {
        return getChild(parent, CY.LEAF, leafName);
    }

    public static YAction getAction(final AbstractStatement parent, final String actionName) {
        return getChild(parent, CY.ACTION, actionName);
    }

    public static YLeafList getLeafList(final AbstractStatement parent, final String leafListName) {
        return getChild(parent, CY.LEAF_LIST, leafListName);
    }

    public YTypedef getTypedefForModule(final String moduleName, final String typedefName) {
        final YModule module = getModule(moduleName);
        return (YTypedef) (module == null ? null : getChild(module, CY.TYPEDEF, typedefName));
    }

    public static YTypedef getTypedef(final AbstractStatement parent, final String typedefName) {
        return getChild(parent, CY.TYPEDEF, typedefName);
    }

    public static YAugment getAugment(final AbstractStatement parent, final String path) {
        return getChild(parent, CY.AUGMENT, path);
    }

    public static YFeature getFeature(final AbstractStatement parent, final String featureName) {
        return getChild(parent, CY.FEATURE, featureName);
    }

    public static YDeviation getDeviation(final AbstractStatement parent, final String path) {
        return (YDeviation) parent.getChildStatements().stream().filter(stmt -> stmt.is(CY.STMT_DEVIATION)).filter(
                stmt -> stmt.getDomElement().getValue().equals(path)).findAny().orElse(null);
    }

    public ExtensionStatement getExtension(final AbstractStatement parent, final String owningModuleName,
            final String extensionName, final String argument) {
        for (final ExtensionStatement extensionStatement : parent.getExtensionChildStatements()) {
            final String prefix = extensionStatement.getExtensionModulePrefix();
            final ModuleIdentity owningModule = extensionStatement.getPrefixResolver().getModuleForPrefix(prefix);
            if (owningModuleName.equals(owningModule.getModuleName()) && extensionStatement.getExtensionStatementName()
                    .equals(extensionName)) {
                if (argument == null || (argument.equals(extensionStatement.getValue()))) {
                    return extensionStatement;
                }
            }
        }
        return null;
    }

    public YContainer getContainerUnderContainer(final AbstractStatement parent, final String containerName1,
            final String containerName2) {
        final YContainer cont1 = getContainer(parent, containerName1);
        return cont1 == null ? null : getContainer(cont1, containerName2);
    }

    public YLeafList getLeafListUnderContainer(final AbstractStatement parent, final String containerName,
            final String leafListName) {
        final YContainer cont1 = getContainer(parent, containerName);
        return cont1 == null ? null : getLeafList(cont1, leafListName);
    }

    public YLeafList getLeafListUnderList(final AbstractStatement parent, final String listName,
            final String leafListName) {
        final YList list1 = getList(parent, listName);
        return list1 == null ? null : getLeafList(list1, leafListName);
    }

    public YLeaf getLeafUnderContainer(final AbstractStatement parent, final String containerName, final String leafName) {
        final YContainer cont1 = getContainer(parent, containerName);
        return cont1 == null ? null : getLeaf(cont1, leafName);
    }

    public YLeaf getLeafUnderList(final AbstractStatement parent, final String listName, final String leafName) {
        final YList list = getList(parent, listName);
        return list == null ? null : getLeaf(list, leafName);
    }

    public YList getListUnderContainer(final AbstractStatement parent, final String containerName, final String listName) {
        final YContainer cont1 = getContainer(parent, containerName);
        return cont1 == null ? null : getList(cont1, listName);
    }

    /**
     * Get leaflist from container (container-list-leaflist)
     */
    public YLeafList getLeafListFromContainerList(final YModule yModule, final String containerName,
            final String leafListName) {

        for (final YContainer container : yModule.getContainers()) {
            if (container.getContainerName().equals(containerName)) {
                for (final YList list : container.getLists()) {
                    for (final YLeafList leafList : list.getLeafLists()) {
                        if (leafList.getLeafListName().equals(leafListName)) {
                            return leafList;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * Get leaf from container within another container (container-container-leaf)
     */
    public YLeaf getLeafFromContainerContainer(final YModule yModule, final String containerName1,
            final String containerName2, final String leafName) {
        for (final YContainer container : yModule.getContainers()) {
            if (container.getContainerName().equals(containerName1)) {
                for (final YContainer innerContainer : container.getContainers()) {
                    if (innerContainer.getContainerName().equals(containerName2)) {
                        return getLeaf(innerContainer, leafName);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get leaf from List within a container (container-list-leaf)
     */
    public YLeaf getLeafFromContainerList(final YModule yModule, final String containerName, final String listName,
            final String leafName) {
        final YList yList = getListUnderContainer(yModule, containerName, listName);
        for (final YLeaf leaf : yList.getLeafs()) {
            if (leaf.getLeafName().equals(leafName)) {
                return leaf;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> T getChild(final AbstractStatement parent, final String childType) {

        if (parent == null) {
            return null;
        }

        for (final AbstractStatement child : parent.getChildStatements()) {
            if (child.getDomElement().getName().equals(childType)) {
                return (T) child;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractStatement> T getChild(final AbstractStatement parent, final String childType,
            final String childName) {

        if (parent == null) {
            return null;
        }

        for (final AbstractStatement child : parent.getChildStatements()) {
            if (child.getDomElement().getName().equals(childType)) {
                if (childName == null && child.getDomElement().getValue() == null) {
                    return (T) child;
                } else if (childName != null && childName.equals(child.getDomElement().getValue())) {
                    return (T) child;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> T getExtensionChild(final AbstractStatement parent, final String module,
            final String extName) {

        if (parent == null) {
            return null;
        }

        for (final ExtensionStatement child : parent.getExtensionChildStatements()) {
            final StatementModuleAndName childSman = child.getStatementModuleAndName();
            if (childSman.getModuleName().equals(module) && childSman.getStatementName().equals(extName)) {
                return (T) child;
            }
        }

        return null;
    }

    public YangDomElement getDomChild(final YangDomElement domParent, final String domChildName,
            final String domChildValue) {

        if (domParent == null) {
            return null;
        }

        for (final YangDomElement domChild : domParent.getChildren()) {
            if (domChild.getName().equals(domChildName) && domChild.getValue().equals(domChildValue)) {
                return domChild;
            }
        }

        return null;
    }

    public YangDomElement getDomChild(final YangDomElement domParent, final String domChildName) {

        if (domParent == null) {
            return null;
        }

        for (final YangDomElement domChild : domParent.getChildren()) {
            if (domChild.getName().equals(domChildName)) {
                return domChild;
            }
        }

        return null;
    }

    /**
     * Get identityref type from leaf (leaf-type)
     */
    public YType getIdentityType(final YLeaf leaf) {
        for (final YType type : leaf.getType().getTypes()) {
            if (type.getDataType().equals("identityref")) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get a leaf from container by passing type.
     */
    public YLeaf getLeafOfTypeFromContainer(final YContainer container, final String type) {
        for (final YLeaf yleaf : container.getLeafs()) {
            if ((yleaf.getType().getDataType()).equals(type)) {
                return yleaf;
            }
        }
        return null;
    }

    /**
     * Get container from container (container-container)
     */

    /**
     * Get case from choice within a container (container-choice-case)
     */
    public YCase getCaseFromChoiceContainer(final YModule yModule, final String containerName, final String choiceName,
            final String caseName) {
        final YChoice choice = getChoiceFromContainer(yModule, containerName, choiceName);
        for (final YCase ycase : choice.getCases()) {
            if (ycase.getCaseName().equals(caseName)) {
                return ycase;
            }
        }
        return null;
    }

    /**
     * Get choice from a container
     */
    public YChoice getChoiceFromContainer(final YModule yModule, final String containerName, final String choiceName) {
        final YContainer container = getContainer(yModule, containerName);
        for (final YChoice choice : container.getChoices()) {
            if (choice.getChoiceName().equals(choiceName)) {
                return choice;
            }
        }
        return null;
    }

    protected LeafInstance getLeafInstance(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        return parent.getLeafInstance(namespace, name);
    }

    protected ContainerInstance getContainerInstance(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        return parent.getContainerInstance(namespace, name);
    }

    protected List<LeafListInstance> getLeafListInstances(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        if (!parent.hasLeafListInstance(namespace, name)) {
            return Collections.<LeafListInstance> emptyList();
        }
        return parent.getLeafListInstances(namespace, name);
    }

    protected List<Object> getLeafListValues(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        return parent.getLeafListValues(namespace, name);
    }

    protected AnyDataInstance getAnyDataInstance(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        return parent.getAnyDataInstance(namespace, name);
    }

    protected AnyXmlInstance getAnyXmlInstance(final AbstractStructureInstance parent, final String namespace,
            final String name) {
        return parent.getAnyXmlInstance(namespace, name);
    }

    protected ListInstance getListInstanceData(AbstractStructureInstance parent, String ns, String name,
            Map<String, String> keyValues) {

        if (!parent.hasListInstance(ns, name)) {
            return null;
        }

        return (ListInstance) parent.getListInstances(ns, name).stream().filter(inst -> keyValues.equals(
                ((ListInstance) inst).getKeyValues())).findAny().orElse(null);
    }

    protected static YangDataDomNode getChildDataDomNode(final YangDataDomNode parent, final String name) {
        return parent.getChildren().stream().filter(child -> name.equals(child.getName())).findAny().orElse(null);
    }

    protected static YangDataDomNode getChildDataDomNode(final YangDataDomNode parent, final String name,
            final String value) {
        return parent.getChildren().stream().filter(child -> name.equals(child.getName()) && value.equals(child.getValue()))
                .findAny().orElse(null);
    }

    protected static YangDataDomNode getChildDataDomNode(final YangDataDomNode parent, final String name, final int index) {
        final List<YangDataDomNode> collect = parent.getChildren().stream().filter(child -> name.equals(child.getName()))
                .collect(Collectors.toList());
        return index < collect.size() ? collect.get(index) : null;
    }

    protected static YangDataDomNodeAnnotationValue getDataDomNodeAnnotation(final YangDataDomNode domNode,
            final String name) {
        return domNode.getAnnotations().stream().filter(anno -> name.equals(anno.getName())).findAny().orElse(null);
    }

    /**
     * Check no findings found other than input argument
     */
    public boolean hasFindingsOfTypeOtherThan(final String findingType) {
        for (final Finding finding : context.getFindingsManager().getAllFindings()) {
            if (!finding.getFindingType().equals(findingType)) {
                return true;
            }
        }
        return false;
    }

    public void assertSubTreeNoFindings(final AbstractStatement statement) {
        assertNoFindingsOnStatement(statement);
        for (final AbstractStatement child : statement.getChildStatements()) {
            assertSubTreeNoFindings(child);
        }
    }

    public void assertNoFindings() {
        assertNoFindings(findingsManager.getAllFindings());
    }

    public void assertNoFindingsOnStatement(final AbstractStatement statement) {
        assertNoFindings(statement.getFindings());
    }

    public void assertNoFindings(final Set<Finding> findings) {
        if (findings.isEmpty()) {
            return;
        }

        System.err.println("Findings count is " + findings.size() + ", not empty as expected.");
        printFindings(findings);
        fail();
    }

    public void assertOneFindingOnly() {
        assertFindingCount(1);
    }

    public void assertOneFindingOnlyOnStatement(final AbstractStatement statement) {
        assertFindingCountOnStatement(statement, 1);
    }

    public void assertOneFindingOnly(final Set<Finding> findings) {
        assertFindingCount(findings, 1);
    }

    public void assertFindingCount(final int count) {
        assertFindingCount(findingsManager.getAllFindings(), count);
    }

    public void assertFindingCountOnStatement(final AbstractStatement statement, final int count) {
        assertFindingCount(statement.getFindings(), count);
    }

    public void assertFindingCount(final Set<Finding> findings, final int count) {
        if (findings.size() == count) {
            return;
        }

        System.err.println("Findings count is " + findings.size() + ", not as expected " + count);
        printFindings(findings);
        fail();
    }

    public void assertOneFindingOnly(final String findingType) {
        assertSingleFindingOfType(findingsManager.getAllFindings(), findingType);
    }

    public void assertStatementHasSingleFindingOfType(final AbstractStatement statement, final String findingType) {
        assertSingleFindingOfType(statement.getFindings(), findingType);
    }

    public void assertSingleFindingOfType(final Set<Finding> findings, final String findingType) {
        assertFindingCount(findings, 1);
        assertHasFindingOfType(findings, findingType);
    }

    public void assertHasFinding(final YangModel yangModelFile, final int lineNumber, final String findingType) {
        for (final Finding finding : context.getFindingsManager().getAllFindings()) {
            if (finding.getLineNumber() == lineNumber && finding.getFindingType().equals(findingType) && finding
                    .getYangModel() == yangModelFile) {
                return;
            }
        }
        fail();
    }

    public void assertHasFindingOfType(final String findingType) {
        assertHasFindingOfType(context.getFindingsManager().getAllFindings(), findingType);
    }

    public void assertHasNotFindingOfType(final String findingType) {
        assertHasNotFindingOfType(context.getFindingsManager().getAllFindings(), findingType);
    }

    public void assertStatementHasFindingOfType(final AbstractStatement statement, final String findingType) {
        assertHasFindingOfType(statement.getFindings(), findingType);
    }

    public void assertStatementHasNotFindingOfType(final AbstractStatement statement, final String findingType) {
        assertHasNotFindingOfType(statement.getFindings(), findingType);
    }

    public void assertDomElementHasFindingOfType(final YangDomElement domElement, final String findingType) {
        final Set<Finding> filtered = context.getFindingsManager().getAllFindings().stream().filter(finding -> domElement
                .getYangModel() == finding.getYangModel() && domElement.getLineNumber() == finding.getLineNumber()).collect(
                        Collectors.toSet());

        assertHasFindingOfType(filtered, findingType);
    }

    public void assertDomElementHasNotFindingOfType(final YangDomElement domElement, final String findingType) {
        final Set<Finding> filtered = context.getFindingsManager().getAllFindings().stream().filter(finding -> domElement
                .getYangModel() == finding.getYangModel() && domElement.getLineNumber() == finding.getLineNumber()).collect(
                        Collectors.toSet());

        assertHasNotFindingOfType(filtered, findingType);
    }

    public void assertDomElementHasNoFindings(final YangDomElement domElement) {
        Set<Finding> filtered = context.getFindingsManager().getAllFindings().stream().filter(finding -> domElement
                .getYangModel() == finding.getYangModel() && domElement.getLineNumber() == finding.getLineNumber()).collect(
                        Collectors.toSet());

        assertNoFindings(filtered);
    }

    public void assertHasFindingOfTypeAndContainsMessage(final String findingType, final String message) {

        final Optional<Finding> findAny = context.getFindingsManager().getAllFindings().stream().filter(finding -> finding
                .getFindingType().equals(findingType)).filter(finding -> finding.getMessage().contains(message)).findAny();

        if (!findAny.isPresent()) {
            System.err.println("Does not have finding of type " + findingType + " containing message " + message);
            printFindings(context.getFindingsManager().getAllFindings());
            fail();
        }
    }

    public void assertHasFindingOfType(final Set<Finding> findings, final String findingType) {
        if (hasFindingOfType(findings, findingType)) {
            return;
        }

        System.err.println("Does not have finding of type " + findingType);
        printFindings(findings);
        fail();
    }

    public void assertHasNotFindingOfType(final Set<Finding> findings, final String findingType) {

        if (!hasFindingOfType(findings, findingType)) {
            return;
        }

        System.err.println("Has finding of type " + findingType);
        printFindings(findings);
        fail();
    }

    public void assertContextHasFindingOfType(final String findingType) {
        assertTrue(contextHasFindingsOfTypes(Collections.singletonList(findingType)));
    }

    public void assertContextHasFindingsOfTypes(final List<String> findingTypes) {
        assertTrue(contextHasFindingsOfTypes(findingTypes));
    }

    public void assertStatementHasFindingsOfTypes(final AbstractStatement statement, final List<String> findingTypes) {
        assertTrue(statementHasFindingsOfTypes(statement, findingTypes));
    }

    public void assertHasFindingsOfTypes(final Set<Finding> findings, final List<String> findingTypes) {
        assertTrue(hasFindingsOfTypes(findings, findingTypes));
    }

    public boolean contextHasFindingOfType(final String findingType) {
        return hasFindingOfType(context.getFindingsManager().getAllFindings(), findingType);
    }

    public boolean statementHasFindingOfType(final AbstractStatement statement, final String findingType) {
        return hasFindingOfType(statement.getFindings(), findingType);

    }

    public boolean hasFindingOfType(final Set<Finding> findings, final String findingType) {
        for (final Finding finding : findings) {
            if (finding.getFindingType().equals(findingType)) {
                return true;
            }
        }
        return false;
    }

    public boolean contextHasFindingsOfTypes(final List<String> findingTypes) {
        return hasFindingsOfTypes(context.getFindingsManager().getAllFindings(), findingTypes);
    }

    public boolean statementHasFindingsOfTypes(final AbstractStatement statement, final List<String> findingTypes) {
        return hasFindingsOfTypes(statement.getFindings(), findingTypes);

    }

    public boolean hasFindingsOfTypes(final Set<Finding> findings, final List<String> findingTypes) {

        for (final String findingType : findingTypes) {
            if (!hasFindingOfType(findings, findingType)) {
                return false;
            }
        }
        return true;
    }

    public void printFindings() {
        printFindings(findingsManager.getAllFindings());
    }

    public void printFindingsForStatement(final AbstractStatement statement) {
        printFindings(statement.getFindings());
    }

    public void printFindings(final Set<Finding> findings) {

        final List<String> collect = findings.stream().map(Finding::toString).collect(Collectors.toList());
        Collections.sort(collect);
        collect.forEach(System.err::println);
    }

    // ================================== JSON stuff =======================================

    protected static JsonValue getJsonObjectMemberValue(final JsonObject jsonObject, final String memberName) {

        final Set<Entry<JsonObjectMemberName, JsonValue>> entrySet = jsonObject.getValuesByMember().entrySet();
        for (final Entry<JsonObjectMemberName, JsonValue> entry : entrySet) {

            if (memberName.equals(entry.getKey().getMemberName())) {
                return entry.getValue();
            }
        }

        return null;
    }

}
