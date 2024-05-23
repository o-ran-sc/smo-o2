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
package org.oran.smo.yangtools.parser.data.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.data.YangData;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomDocumentRoot.SourceDataType;
import org.oran.smo.yangtools.parser.data.dom.YangDataDomNode;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.resolvers.Helper;
import org.oran.smo.yangtools.parser.model.schema.ModuleRegistry;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YList;
import org.oran.smo.yangtools.parser.model.statements.yang.YType;
import org.oran.smo.yangtools.parser.model.util.DataTypeHelper;
import org.oran.smo.yangtools.parser.model.util.GrammarHelper;
import org.oran.smo.yangtools.parser.yanglibrary.IetfYangLibraryParser;

/**
 * Builds a type-safe Yang instance data tree from Yang data DOM trees.
 *
 * @author Mark Hollmann
 */
public class InstanceDataTreeBuilder {

    /**
     * Given a number of data DOM trees, merges these together and forms a (single) tree with type-safe
     * Yang instance data.
     * <p/>
     * This class requires the underlying Yang Model to be available, i.e. cannot be used with data only.
     * <p/>
     * If the input data was in JSON, module-name -&gt; namespace resolution must have been performed on
     * the data first.
     */
    public static RootInstance buildCombinedDataTree(final FindingsManager findingsManager, final List<YangData> yangDatas,
            final ModuleRegistry moduleRegistry, final DataTreeBuilderPredicate topLevelInstancePredicate) {

        /*
         * In a first step, the instance tree is build for each data file. Once this has been done the
         * trees will be merged together.
         */
        final List<RootInstance> rootInstances = new ArrayList<>();

        for (final YangData yangData : yangDatas) {

            if (yangData.getYangDataDomDocumentRoot() == null) {
                continue;
            }

            if (containsYangLibraryInstanceOnly(yangData) && !yangLibraryModelPresent(moduleRegistry)) {
                /*
                 * In case the data input only contains the data for the yang library, but the yang library
                 * module itself was not part of the model inputs, we will not attempt to generate the data
                 * instance tree, as this would fail - and the yang library is used as in effect BOM.
                 */
            } else {
                final RootInstance rootInstance = new RootInstance();
                /*
                 * Schema root is handled slightly different from child-handling further down the tree...
                 */
                final List<AbstractStatement> allDataNodesAtTopLevel = getAllDataNodesAndChoiceAtTopLevel(moduleRegistry);
                for (final YangDataDomNode dataDomNode : yangData.getYangDataDomDocumentRoot().getChildren()) {
                    if (topLevelInstancePredicate.test(dataDomNode)) {
                        processDomNode(findingsManager, dataDomNode, rootInstance, allDataNodesAtTopLevel);
                    }
                }

                rootInstances.add(rootInstance);
            }
        }

        /*
         * Now all of the trees are merged together. This is a "smart" merge - the contents of the containers
         * and lists are merged together; where content already exists and it is of the same value no finding
         * will be issued.
         */
        final RootInstance result = new RootInstance();
        for (final RootInstance rootInstance : rootInstances) {
            mergeInDataTree(findingsManager, result, rootInstance);
        }

        return result;
    }

    public static List<AbstractStatement> getAllDataNodesAndChoiceAtTopLevel(final ModuleRegistry moduleRegistry) {

        final List<AbstractStatement> result = new ArrayList<>();

        for (final YangModel yangModelFile : moduleRegistry.getAllYangModels()) {
            if (yangModelFile.getYangModelRoot().isModule()) {
                result.addAll(getAllDataNodesAndChoiceUnderStatement(yangModelFile.getYangModelRoot().getModule()));
            }
        }

        return result;
    }

    private static List<AbstractStatement> getAllDataNodesAndChoiceUnderStatement(final AbstractStatement statement) {
        return statement.getChildStatements().stream().filter(child -> child.definesDataNode() || child.is(CY.STMT_CHOICE))
                .collect(Collectors.toList());
    }

    /**
     * Returns whether this input only contains the YANG instance data.
     */
    private static boolean containsYangLibraryInstanceOnly(final YangData yangData) {

        if (yangData.getYangDataDomDocumentRoot() == null) {
            return false;
        }

        final List<YangDataDomNode> childrenUnderRoot = yangData.getYangDataDomDocumentRoot().getChildren();
        if (childrenUnderRoot.size() != 1) {
            return false;
        }

        final YangDataDomNode yangDataDomNode = childrenUnderRoot.get(0);

        if (!IetfYangLibraryParser.IETF_YANG_LIBRARY_NAMESPACE.equals(yangDataDomNode.getNamespace())) {
            return false;
        }

        return IetfYangLibraryParser.YANG_LIBRARY_MODULES_STATE.equals(yangDataDomNode
                .getName()) || IetfYangLibraryParser.YANG_LIBRARY_YANG_LIBRARY.equals(yangDataDomNode.getName());
    }

    /**
     * Returns whether any of the YANG library containers exist in the data nodes tree - so basically
     * if the yang-library model was in the input.
     */
    private static boolean yangLibraryModelPresent(final ModuleRegistry moduleRegistry) {

        final List<AbstractStatement> allDataNodesAtTopLevel = getAllDataNodesAndChoiceAtTopLevel(moduleRegistry);

        final Optional<AbstractStatement> YangLibContainer = allDataNodesAtTopLevel.stream().filter(statement -> statement
                .is(CY.STMT_CONTAINER)).filter(statement -> IetfYangLibraryParser.IETF_YANG_LIBRARY_NAMESPACE.equals(
                        statement.getEffectiveNamespace())).filter(statement -> {
                            final String containerName = statement.getStatementIdentifier();
                            return IetfYangLibraryParser.YANG_LIBRARY_MODULES_STATE.equals(
                                    containerName) || IetfYangLibraryParser.YANG_LIBRARY_YANG_LIBRARY.equals(containerName);
                        }).findFirst();

        return YangLibContainer.isPresent();
    }

    private static void processDomNode(final FindingsManager findingsManager, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parentInstance, final List<AbstractStatement> candidateDataNodes) {

        final AbstractStatement matchingDataNode = Helper.findSchemaDataNode(candidateDataNodes, dataDomNode.getNamespace(),
                dataDomNode.getName());
        if (matchingDataNode == null) {
            /*
             * Well possible that the prefix is wrong / missing on the XML element, and hence the
             * namespace of the DOM node is wrong and can't be found. Check if a schema node with
             * the same name exists to give the user better feedback.
             */
            final AbstractStatement childWithSameName = Helper.findSchemaDataNode(candidateDataNodes, dataDomNode
                    .getName());

            if (childWithSameName != null) {
                findingsManager.addFinding(new Finding(dataDomNode,
                        ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString(),
                        "No corresponding schema node was found in the model for data instance '" + dataDomNode
                                .getPath() + "' in namespace '" + dataDomNode
                                        .getNamespace() + "', but there exists a schema node with the same name in namespace '" + childWithSameName
                                                .getEffectiveNamespace() + "'. Adjust namespace of the data instance."));
            } else {
                findingsManager.addFinding(new Finding(dataDomNode,
                        ParserFindingType.P075_CORRESPONDING_SCHEMA_NODE_NOT_FOUND.toString(),
                        "No corresponding schema node was found in the model for data instance '" + dataDomNode
                                .getPath() + "' (ns='" + dataDomNode.getNamespace() + "')."));
            }
            return;
        }

        if (matchingDataNode.is(CY.STMT_CONTAINER)) {
            processContainer(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        } else if (matchingDataNode.is(CY.STMT_LEAF)) {
            processLeaf(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        } else if (matchingDataNode.is(CY.STMT_LEAF_LIST)) {
            processLeafList(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        } else if (matchingDataNode.is(CY.STMT_LIST)) {
            processList(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        } else if (matchingDataNode.is(CY.STMT_ANYXML)) {
            processAnyxml(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        } else if (matchingDataNode.is(CY.STMT_ANYDATA)) {
            processAnydata(findingsManager, dataDomNode, parentInstance, matchingDataNode);
        }
    }

    private static void processContainer(final FindingsManager findingsManager, final YangDataDomNode dataDomNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement container) {

        final ContainerInstance containerInstance = new ContainerInstance(container, dataDomNode, parentInstance);
        if (parentInstance.hasContainerInstance(containerInstance.getNamespace(), containerInstance.getName())) {
            findingsManager.addFinding(new Finding(dataDomNode, ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString(),
                    "Container '" + dataDomNode.getPath() + "' already defined in this input."));
            return;
        }

        parentInstance.addStructureChild(containerInstance);

        final List<AbstractStatement> allDataNodesUnderContainerStatement = getAllDataNodesAndChoiceUnderStatement(
                container);
        for (final YangDataDomNode childDomNode : dataDomNode.getChildren()) {
            processDomNode(findingsManager, childDomNode, containerInstance, allDataNodesUnderContainerStatement);
        }
    }

    private static void processLeaf(final FindingsManager findingsManager, final YangDataDomNode domNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement leaf) {

        Object leafValue = domNode.getValue();
        if (leafValue == null && domNode.getSourceDataType() == SourceDataType.JSON) {
            leafValue = adjustNullValueForEmpty(leaf);
        }
        if (leafValue == null) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P080_NULL_VALUE.toString(), "Leaf '" + domNode
                    .getPath() + "' does not have a value."));
            return;
        }

        final LeafInstance leafInstance = new LeafInstance(leaf, domNode, parentInstance, leafValue);
        if (parentInstance.hasLeafInstance(leafInstance.getNamespace(), leafInstance.getName())) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString(),
                    "Leaf '" + domNode.getPath() + "' already defined in this input."));
            return;
        }

        parentInstance.addContentChild(leafInstance);
    }

    private static void processAnydata(final FindingsManager findingsManager, final YangDataDomNode domNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement schemaLeaf) {

        final String nodeValue = domNode.getReassembledChildren();

        final AnyDataInstance anyDataInstance = new AnyDataInstance(schemaLeaf, domNode, parentInstance, nodeValue);
        if (parentInstance.hasAnyDataInstance(anyDataInstance.getNamespace(), anyDataInstance.getName())) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString(),
                    "Anydata '" + domNode.getPath() + "' already defined in this input."));
            return;
        }

        parentInstance.addContentChild(anyDataInstance);
    }

    private static void processAnyxml(final FindingsManager findingsManager, final YangDataDomNode domNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement schemaLeaf) {

        final String nodeValue = domNode.getReassembledChildren();

        final AnyXmlInstance anyXmlInstance = new AnyXmlInstance(schemaLeaf, domNode, parentInstance, nodeValue);
        if (parentInstance.hasAnyXmlInstance(anyXmlInstance.getNamespace(), anyXmlInstance.getName())) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString(),
                    "Anyxml '" + domNode.getPath() + "' already defined in this input."));
            return;
        }

        parentInstance.addContentChild(anyXmlInstance);
    }

    private static void processLeafList(final FindingsManager findingsManager, final YangDataDomNode domNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement leafList) {

        Object leafListValue = domNode.getValue();
        if (leafListValue == null && domNode.getSourceDataType() == SourceDataType.JSON) {
            leafListValue = adjustNullValueForEmpty(leafList);
        }
        if (leafListValue == null) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P080_NULL_VALUE.toString(),
                    "Leaf-list '" + domNode.getPath() + "' does not have a value."));
            return;
        }

        final LeafListInstance leafListInstance = new LeafListInstance(leafList, domNode, parentInstance, leafListValue);

        /*
         * leaf-list is a bit different. The RFC states that values have to be unique in config data, so we need to check for that.
         */
        if (leafList.isEffectiveConfigTrue()) {
            if (parentInstance.hasLeafListInstance(leafListInstance.getNamespace(), leafListInstance.getName(),
                    leafListInstance.getValue())) {
                findingsManager.addFinding(new Finding(domNode, ParserFindingType.P073_LEAF_VALUE_ALREADY_SET.toString(),
                        "'config true' leaf-list '" + domNode
                                .getPath() + "' instance with value '" + leafListValue + "' already defined in this input."));
                return;
            }
        }

        parentInstance.addContentChild(leafListInstance);
    }

    private static void processList(final FindingsManager findingsManager, final YangDataDomNode domNode,
            final AbstractStructureInstance parentInstance, final AbstractStatement list) {
        /*
         * Create the list and check it doesn't exist yet. Then hook it up,
         * and go recursively down the tree.
         */
        final ListInstance listInstance = createListInstance(findingsManager, parentInstance, domNode, list);
        if (listInstance == null) {
            /*
             * No need for extra finding, would have been issued when creating the list instance.
             */
            return;
        }
        if (parentInstance.hasListInstance(listInstance.getNamespace(), listInstance.getName(), listInstance
                .getKeyValues())) {
            findingsManager.addFinding(new Finding(domNode, ParserFindingType.P076_DUPLICATE_INSTANCE_DATA.toString(),
                    "List '" + domNode.getPath() + "' with key '" + listInstance
                            .getKeyValues() + "' already defined in this input."));
            return;
        }
        parentInstance.addStructureChild(listInstance);

        final List<AbstractStatement> allDataNodesUnderListStatement = getAllDataNodesAndChoiceUnderStatement(list);
        for (final YangDataDomNode childDomNode : domNode.getChildren()) {
            processDomNode(findingsManager, childDomNode, listInstance, allDataNodesUnderListStatement);
        }
    }

    private static ListInstance createListInstance(final FindingsManager findingsManager,
            final AbstractStructureInstance parentStructure, final YangDataDomNode dataDomNode,
            final AbstractStatement list) {

        /*
         * So it's a YANG list, get key(s), as these are important to identify the correct instance.
         */
        final YList yangList = (YList) list;

        final List<String> keyNames = yangList.getKey() != null ?
                GrammarHelper.parseToStringList(yangList.getKey().getValue()) :
                Collections.<String> emptyList();
        final Map<String, String> keyValues = new HashMap<>();

        for (final String keyName : keyNames) {
            final String value = getValueOfKeyLeaf(dataDomNode, keyName);
            if (value == null) {
                /*
                 * Note that RFC7950 states:
                 *
                 * "All key leafs MUST be given values when a list entry is created." So if we don't have a value that is an error.
                 */
                findingsManager.addFinding(new Finding(dataDomNode, ParserFindingType.P072_MISSING_KEY_VALUE.toString(),
                        "No value, or null, supplied for key leaf '" + keyName + "' for list instance '" + dataDomNode
                                .getPath() + "'."));
                return null;
            }
            keyValues.put(keyName, value);
        }

        return new ListInstance(list, dataDomNode, parentStructure, keyNames, keyValues);
    }

    /**
     * Returns the value of the key leaf with the given name. Note that null
     * will be returned if the key does not exist, or has an explicit null value.
     */
    private static String getValueOfKeyLeaf(final YangDataDomNode dataDomNode, final String keyName) {

        for (final YangDataDomNode child : dataDomNode.getChildren()) {
            if (child.getName().equals(keyName)) {
                return child.getStringValue();
            }
        }

        return null;
    }

    /**
     * In JSON, an instance of a data node of type "empty" is encoded as '"my-leaf" : [null]', resulting
     * in a DOM node with a null value. If the leaf in question is of type empty, then we will convert
     * this to an empty string (as in NETCONF), so to allow further processing.
     */
    private static String adjustNullValueForEmpty(final AbstractStatement leafOrLeafList) {

        final YType type = leafOrLeafList.getChild(CY.STMT_TYPE);
        /*
         * Sanity check - should never happen, unless the schema has defined a leaf / leaf-list
         * without type (this would have been issued as a finding a long time ago).
         */
        if (type == null) {
            return null;
        }

        /*
         * Handle union as well - although having an empty as part of a union does not make much sense?
         */
        final List<YType> types = DataTypeHelper.isUnionType(type.getDataType()) ?
                type.getTypes() :
                Collections.singletonList(type);

        for (final YType oneType : types) {
            if (DataTypeHelper.isEmptyType(oneType.getDataType())) {
                return "";
            }
        }

        /*
         * So, not an empty. Guess its really a null value, so.
         */
        return null;
    }

    /**
     * Merges the content of the source tree into the content of the target tree. This
     * behaves in the same way as the NETCONF "merge" operation.
     */
    private static void mergeInDataTree(final FindingsManager findingsManager,
            final AbstractStructureInstance targetParentStructure, final AbstractStructureInstance sourceParentStructure) {

        /*
         * Do the leafs and leaf-lists first.
         */
        for (final AbstractContentInstance sourceLeafOrLeafList : sourceParentStructure.getContentChildren()) {

            if (sourceLeafOrLeafList instanceof LeafInstance) {
                /*
                 * If the exact same leaf already exists, with the same value, then we are ok with that.
                 */
                final LeafInstance leafInstanceInTarget = targetParentStructure.getLeafInstance(sourceLeafOrLeafList
                        .getNamespace(), sourceLeafOrLeafList.getName());
                if (leafInstanceInTarget != null) {
                    final Object sourceValue = ((LeafInstance) sourceLeafOrLeafList).getValue();
                    final Object targetValue = leafInstanceInTarget.getValue();
                    if (!Objects.equals(sourceValue, targetValue)) {
                        findingsManager.addFinding(new Finding(sourceLeafOrLeafList.getDataDomNode(),
                                ParserFindingType.P073_LEAF_VALUE_ALREADY_SET.toString(),
                                "A different value for leaf '" + leafInstanceInTarget.getDataDomNode()
                                        .getPath() + "' has already been set by input '" + leafInstanceInTarget
                                                .getDataDomNode().getYangData().getYangInput()
                                                .getName() + "' (" + sourceValue + " vs. " + targetValue + ")."));
                        continue;
                    }
                } else {		// leaf does not exist in target, then merge
                    targetParentStructure.addContentChild(sourceLeafOrLeafList);
                    sourceLeafOrLeafList.reparent(targetParentStructure);
                }
            } else if (sourceLeafOrLeafList instanceof LeafListInstance) {
                /*
                 * RFC states that a merge of these is such that existing instances are
                 * ignored, i.e.only add instance if it does not exist yet.
                 */
                final boolean leafListWithSameValueExistsInTarget = targetParentStructure.hasLeafListInstance(
                        sourceLeafOrLeafList.getNamespace(), sourceLeafOrLeafList.getName(), sourceLeafOrLeafList
                                .getValue());
                if (!leafListWithSameValueExistsInTarget) {	// leaf-list with this value does not exist in target, then merge
                    targetParentStructure.addContentChild(sourceLeafOrLeafList);
                    sourceLeafOrLeafList.reparent(targetParentStructure);
                }
            }

            // TODO in the future: anydata and anyxml
        }

        /*
         * Now do the containers and lists. This is a bit more complex - basically, where a
         * container/list does not exist in the target, the whole tree is merged over. Otherwise
         * recursion has to happen downwards.
         */
        for (final AbstractStructureInstance sourceContainerOrList : sourceParentStructure.getStructureChildren()) {

            AbstractStructureInstance sameInstanceInTarget = null;

            if (sourceContainerOrList instanceof ContainerInstance) {
                sameInstanceInTarget = targetParentStructure.getContainerInstance(sourceContainerOrList.getNamespace(),
                        sourceContainerOrList.getName());
            } else if (sourceContainerOrList instanceof ListInstance) {
                sameInstanceInTarget = targetParentStructure.getListInstance(sourceContainerOrList.getNamespace(),
                        sourceContainerOrList.getName(), ((ListInstance) sourceContainerOrList).getKeyValues());
            }

            if (sameInstanceInTarget != null) {
                mergeInDataTree(findingsManager, sameInstanceInTarget, sourceContainerOrList);
            } else {
                targetParentStructure.addStructureChild(sourceContainerOrList);
                sourceContainerOrList.reparent(targetParentStructure);
            }
        }
    }
}
