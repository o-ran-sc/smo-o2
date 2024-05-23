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
package org.oran.smo.yangtools.parser.model.statements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.PrefixResolver;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.ModuleIdentity;
import org.oran.smo.yangtools.parser.model.ModulePrefixResolver;
import org.oran.smo.yangtools.parser.model.statements.yang.CY;
import org.oran.smo.yangtools.parser.model.statements.yang.YConfig;
import org.oran.smo.yangtools.parser.model.statements.yang.YDescription;
import org.oran.smo.yangtools.parser.model.statements.yang.YErrorMessage;
import org.oran.smo.yangtools.parser.model.statements.yang.YReference;
import org.oran.smo.yangtools.parser.model.statements.yang.YStatus;
import org.oran.smo.yangtools.parser.model.util.GrammarHelper;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomDocumentRoot;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;
import org.oran.smo.yangtools.parser.util.QNameHelper;

/**
 * Base class for all statements. Once a YAM has been parsed into a Yang DOM, the statement
 * tree will be build. Every statement in the model, no matter if core or extension, is
 * represented by an instance of this class.
 *
 * @author Mark Hollmann
 */
public abstract class AbstractStatement {

    /**
     * The root of the model owning this statement.
     */
    protected YangModelRoot modelRoot;

    /**
     * The underlying DOM element
     */
    protected final YangDomElement domElement;

    /**
     * Parent and children of this statement, respectively. Both can change as result of various
     * operations, such as augment, uses, deviate.
     */
    protected AbstractStatement parent;
    protected final List<AbstractStatement> children = new ArrayList<>();

    /**
     * The module and statement name
     */
    private StatementModuleAndName statementModuleAndName = null;

    /**
     * The namespace in which this statement effectively sits. This is important for statements as
     * part of groupings/typedef that get pulled-in (the effective namespace will be the namespace
     * of the module using the grouping/typedef, not the namespace of the module in which the
     * grouping/typedef is defined).
     */
    private String effectiveNamespace = null;

    /**
     * The effective status of this statement.
     */
    private String effectiveStatus = null;

    /**
     * The effective ConformanceType of this statement. Only of significant interest if protocol-
     * accessible objects of conformance "import-only" are actually retained in the schema (which
     * is usually undesirable)
     */
    private ConformanceType effectiveConformanceType = null;

    /**
     * The effective config value of this statement.
     */
    private boolean effectiveConfig = true;

    /**
     * Findings made in respect of this statement, if any.
     */
    private Set<Finding> findings = null;

    /**
     * Gives an app using the parser the possibility to attach custom data to this statement.
     * May be used by tooling.
     */
    private Map<String, Object> customAppData = null;

    /**
     * Special constructor for root. Only the YangModelRoot constructor will invoke this.
     */
    protected AbstractStatement(final YangDomDocumentRoot domDocumentRoot) {
        this.modelRoot = (YangModelRoot) this;
        this.domElement = domDocumentRoot;

        this.parent = null;
    }

    public AbstractStatement(final AbstractStatement parentStatement, final YangDomElement domElement) {
        this.modelRoot = parentStatement.getYangModelRoot();
        this.domElement = domElement;

        this.parent = parentStatement;
        parentStatement.children.add(this);
    }

    public YangModelRoot getYangModelRoot() {
        return modelRoot;
    }

    public AbstractStatement getParentStatement() {
        return parent;
    }

    /**
     * Returns the child statements of this statement. Do not modify the returned list.
     */
    public List<AbstractStatement> getChildStatements() {
        return children;
    }

    public boolean isExtension() {
        return getStatementModuleAndName().isExtensionStatement();
    }

    /**
     * Returns all child statements that are extensions.
     */
    public List<ExtensionStatement> getExtensionChildStatements() {

        List<ExtensionStatement> result = null;

        for (final AbstractStatement child : children) {
            if (child.isExtension()) {
                if (result == null) {
                    result = new ArrayList<>(4);
                }
                result.add((ExtensionStatement) child);
            }
        }

        return result != null ? result : Collections.emptyList();
    }

    /**
     * Returns all child statements that are not extensions (ie. they are core YANG statements).
     */
    public List<AbstractStatement> getNonExtensionChildStatements() {

        if (children.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AbstractStatement> result = new ArrayList<>();
        for (final AbstractStatement child : children) {
            if (!child.isExtension()) {
                result.add(child);
            }
        }

        return result;
    }

    public YangDomElement getDomElement() {
        return domElement;
    }

    /**
     * Returns the prefix resolver for this statement. The prefix resolver is the exact same as that
     * of the underlying DOM element. Different statements within the same schema tree may use a
     * different prefix resolver - this is typically then the case where statements are augmented-into/
     * used-by a module, and the original prefix context must be retained.
     */
    public ModulePrefixResolver getPrefixResolver() {
        return domElement.getPrefixResolver();
    }

    /**
     * Returns the name of the statement. If the statement is an extension, only the
     * name of the extension will be returned, not the prefix.
     */
    public String getStatementName() {
        return getStatementModuleAndName().getStatementName();
    }

    /**
     * Returns the statement name and the module defining it.
     */
    public StatementModuleAndName getStatementModuleAndName() {

        /*
         * All type-safe classes representing YANG core language statements, and some extension classes,
         * will override this. This logic should only ever fire if a client has manually created a YANG
         * core statement, or a factory was not available for an extension statement.
         */
        if (statementModuleAndName == null) {

            final String statementPrefix = QNameHelper.extractPrefix(domElement.getName());
            final String statementName = QNameHelper.extractName(domElement.getName());

            if (PrefixResolver.NO_PREFIX.equals(statementPrefix)) {
                if (CY.isYangCoreStatementName(statementName)) {
                    statementModuleAndName = CY.getStatementForName(statementName);
                } else {
                    /*
                     * Probably a spelling mistake, or possibly the absolute root '/' which is not a real
                     * statement. We still create a SMAN.
                     */
                    statementModuleAndName = new StatementModuleAndName(CY.YANG_CORE_MODULE_NAME, statementName);
                }
            } else {
                /*
                 * Extension
                 */
                final ModuleIdentity moduleForPrefix = getPrefixResolver().getModuleForPrefix(statementPrefix);
                if (moduleForPrefix == null) {
                    /*
                     * Unresolvable prefix, would have been caught elsewhere. We use the prefix as module name
                     * which is wrong, of course, but at least this method here will never return null and
                     * cause a NPE somewhere else...
                     */
                    statementModuleAndName = new StatementModuleAndName(statementPrefix, statementName);
                } else {
                    /*
                     * All is well.
                     */
                    statementModuleAndName = new StatementModuleAndName(moduleForPrefix.getModuleName(), statementName);
                }
            }
        }

        return statementModuleAndName;
    }

    public void addFinding(final Finding finding) {
        if (findings == null) {
            findings = new HashSet<>();
        }
        findings.add(finding);
    }

    public void removeFinding(final Finding finding) {
        if (findings != null) {
            findings.remove(finding);
        }
    }

    /**
     * Returns the findings for this statement. Returns an empty set if no findings found.
     */
    public Set<Finding> getFindings() {
        return findings == null ? Collections.<Finding> emptySet() : findings;
    }

    /**
     * Add arbitrary app-specific data to this statement. Typically used by tooling.
     */
    public void setCustomAppData(final String key) {
        setCustomAppData(key, null);
    }

    /**
     * Add arbitrary app-specific data to this statement. Typically used by tooling.
     */
    public void setCustomAppData(final String key, final Object value) {
        if (customAppData == null) {
            customAppData = new HashMap<>();
        }
        customAppData.put(key, value);
    }

    /**
     * Returns the value, if any, for app-specific data. Where the application data value is
     * null, hasCustomAppData() should be used instead.
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomAppData(final String key) {
        return customAppData == null ? null : (T) customAppData.get(key);
    }

    public boolean hasCustomAppData(final String key) {
        return customAppData == null ? false : customAppData.containsKey(key);
    }

    /**
     * Returns the identifier defined by the statement. Overridden by those statements that
     * have an identifier.
     */
    public String getStatementIdentifier() {
        return "";
    }

    /**
     * Overridden by statements that define "schema nodes". See RFC, chapter 3 (terminology):
     *
     * "schema node: A node in the schema tree. One of action, container, leaf,
     * leaf-list, list, choice, case, rpc, input, output, notification, anydata,
     * and anyxml."
     */
    public boolean definesSchemaNode() {
        return false;
    }

    /**
     * Overridden by statements that define "data nodes". See RFC, chapter 3 (terminology):
     *
     * "data node: A node in the schema tree that can be instantiated in a
     * data tree. One of container, leaf, leaf-list, list, anydata, and
     * anyxml."
     */
    public boolean definesDataNode() {
        return false;
    }

    /**
     * The statement argument types. Values are similar to the ones shown in chapter 13.1, but do not align fully.
     */
    public enum StatementArgumentType {
        /** The statement does not have an argument. */
        NO_ARG,
        /** The argument is a name; for example, the name of a leaf. */
        NAME,
        /** The argument is a value. */
        VALUE,
        /** The argument is free text. */
        TEXT,
        /** The argument is a condition */
        CONDITION,
        /** The argument is a (sub-)module name */
        MODULE,
        /** The argument is a URI */
        URI,
        /** The argument is a date. */
        DATE,
        /** The argument is a target node. */
        TARGET_NODE,
        /** The argument is a tag. */
        TAG
    }

    /**
     * Returns the argument type
     */
    public abstract StatementArgumentType getArgumentType();

    /**
     * Denotes whether the order of multiple statements of this type under the same parent
     * matters - i.e., if the order was to be changed in the model, would this result in a
     * semantic change? For the vast majority of statements this does not matter, but sometimes
     * it does (think multiple default statements under a leaf-list).
     */
    public boolean orderUnderParentMatters() {
        return false;
    }

    /**
     * Returns the effective status of the statement. This value will be calculated once all
     * groupings, deviations and augmentations have been processed.
     */
    public String getEffectiveStatus() {
        return effectiveStatus == null ? YStatus.CURRENT : effectiveStatus;
    }

    public void setEffectiveStatus(final String effectiveStatus) {
        this.effectiveStatus = effectiveStatus;
    }

    public boolean isEffectiveStatusCurrent() {
        return YStatus.CURRENT.equals(getEffectiveStatus());
    }

    public boolean isEffectiveStatusDeprecated() {
        return YStatus.DEPRECATED.equals(getEffectiveStatus());
    }

    public boolean isEffectiveStatusObsolete() {
        return YStatus.OBSOLETE.equals(getEffectiveStatus());
    }

    public boolean isEffectiveConfigTrue() {
        return effectiveConfig;
    }

    public boolean is(final StatementModuleAndName statementModuleAndName) {
        return getStatementModuleAndName().equals(statementModuleAndName);
    }

    /**
     * Returns the 'description' statement, if any, that is a child of this statement.
     * Use getDescriptionValue() to get the actual value of the description.
     */
    public YDescription getDescription() {
        return getChild(CY.STMT_DESCRIPTION);
    }

    /**
     * Returns the value of the 'description' statement, if any, that is a child of this statement.
     */
    public String getDescriptionValue() {
        final YDescription yDescription = getDescription();
        return yDescription == null ? null : yDescription.getValue();
    }

    /**
     * Returns the 'reference' statement, if any, that is a child of this statement.
     */
    public YReference getReference() {
        return getChild(CY.STMT_REFERENCE);
    }

    /**
     * Returns the value of the 'reference' statement, if any, that is a child of this statement.
     */
    public String getReferenceValue() {
        final YReference yReference = getReference();
        return yReference == null ? null : yReference.getValue();
    }

    /**
     * Returns the error message text of this statement, or the value of a 'error-message'
     * child statement. Otherwise returns null.
     */
    public String getErrorMessageText() {
        if (is(CY.STMT_ERROR_MESSAGE)) {
            return domElement.getValue();
        }

        final YErrorMessage child = getChild(CY.STMT_ERROR_MESSAGE);
        return child != null ? child.getValue() : null;
    }

    /**
     * Returns the statement names that are allowed as children of this statement according to the
     * RFC. Extension classes should override this method.
     */
    public List<String> getStatementsAllowedAsChild() {
        final List<String> allowedStatements = new ArrayList<>(20);
        allowedStatements.addAll(getMandatorySingleChildStatementNames());
        allowedStatements.addAll(getMandatoryMultipleChildStatementNames());
        allowedStatements.addAll(getOptionalSingleChildStatementNames());
        allowedStatements.addAll(getOptionalMultipleChildStatementNames());
        return allowedStatements;
    }

    public final List<String> getOptionalSingleChildStatementNames() {
        return CY.getOptionalSingleChildren(this.domElement.getName());
    }

    public final List<String> getOptionalMultipleChildStatementNames() {
        return CY.getOptionalMultipleChildren(this.domElement.getName());
    }

    public final List<String> getMandatorySingleChildStatementNames() {
        return CY.getMandatorySingleChildren(this.domElement.getName());
    }

    public final List<String> getMandatoryMultipleChildStatementNames() {
        return CY.getMandatoryMultipleChildren(this.domElement.getName());
    }

    /**
     * Processes this statement by recursively creating child statements.
     */
    protected void process(final ParserExecutionContext context) {

        /*
         * The normal case is that this statement is a build-in YANG statement.
         * However, it could very well also be an extension statement.
         */

        if (isExtension()) {
            /*
             * If it is an extension, then really any statement can be below it. There is no RFC-defined way
             * how this can be expressed in YANG, so really we must be able to handle everything. Hence,
             * extract whatever is underneath the extension - if it makes sense or not is a different issue,
             * and a problem of the extension.
             */
            extractAllChildStatementsFromDomExceptExtensions(context);
            extractChildExtensions(context);

            validate(context);

        } else {
            /*
             * It is a build-in YANG statement. We extract all possible (optional, mandatory) child statements.
             */
            final List<String> mandatoryChildSingletonStatementNames = getMandatorySingleChildStatementNames();
            final List<String> mandatoryChildMultipleStatementNames = getMandatoryMultipleChildStatementNames();
            final List<String> optionalChildSingletonStatementNames = getOptionalSingleChildStatementNames();
            final List<String> optionalChildMultipleStatementNames = getOptionalMultipleChildStatementNames();

            extractMandatorySingletonChildStatementsFromDom(context, mandatoryChildSingletonStatementNames);
            extractMandatoryMultipleChildStatementsFromDom(context, mandatoryChildMultipleStatementNames);
            extractOptionalSingletonChildStatementsFromDom(context, optionalChildSingletonStatementNames);
            extractOptionalMultipleChildStatementsFromDom(context, optionalChildMultipleStatementNames);

            extractChildExtensions(context);

            validate(context);

            /*
             * Check at this point that there are not any statements here that are not expected according to RFC.
             */
            for (final YangDomElement childDomElement : domElement.getChildren()) {
                final String childName = childDomElement.getName();

                if (optionalChildSingletonStatementNames.contains(childName) || optionalChildMultipleStatementNames
                        .contains(childName) || mandatoryChildSingletonStatementNames.contains(
                                childName) || mandatoryChildMultipleStatementNames.contains(childName)) {
                    /*
                     * All ok, it is a statement that we are expecting and according to the RFC is fine to be here.
                     */
                } else if (childName.contains(":")) {
                    /*
                     * That's ok as well, it is an extension statement, ignore.
                     */
                } else {
                    /*
                     * Some other in-build YANG statement that should not be a child of this statement.
                     */
                    if (CY.isYangCoreStatementName(childName)) {
                        context.addFinding(new Finding(childDomElement, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT
                                .toString(), "Statement '" + childName + "' is not allowed under '" + domElement
                                        .getName() + "'."));
                    } else {
                        context.addFinding(new Finding(childDomElement, ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT
                                .toString(), "'" + childName + "' is not part of the core YANG language."));
                    }
                }
            }
        }

        /*
         * Indicate that processing of this statement, and all sub-statements, is finished.
         */
        subtreeProcessed(context);
    }

    /**
     * Performs statement-specific validation. Statements sometimes override.
     */
    protected void validate(final ParserExecutionContext context) {
        /*
         * The vast majority of statements require some kind of
         * argument, so check that. For the few statements that
         * don't require this, the method will be overridden.
         */
        validateArgumentNotNullNotEmpty(context);
    }

    /**
     * May be overriden by subclasses if they wish to perform any further processing after the
     * statement and all its children have been fully processed.
     */
    protected void subtreeProcessed(final ParserExecutionContext context) {
    }

    /**
     * Extracts from the DOM tree all statements no matter what they are, except extensions
     */
    private void extractAllChildStatementsFromDomExceptExtensions(final ParserExecutionContext context) {
        for (final YangDomElement childDomElement : domElement.getChildren()) {
            if (!childDomElement.getName().contains(":")) {
                final AbstractStatement childStatement = StatementFactory.createYangCoreStatement(context, childDomElement,
                        this);
                childStatement.process(context);
            }
        }
    }

    /**
     * Extracts from the DOM all child nodes of the specified statement names. Each of these
     * may be present 0..1, i.e. once-only.
     */
    private void extractOptionalSingletonChildStatementsFromDom(final ParserExecutionContext context,
            final List<String> optionalSingletonChildStatementNames) {

        if (optionalSingletonChildStatementNames.isEmpty()) {
            return;
        }

        final Set<String> namesOfStatementsAlreadyExtracted = new HashSet<>();

        for (final YangDomElement childDomElement : domElement.getChildren()) {
            final String domElementName = childDomElement.getName();

            if (optionalSingletonChildStatementNames.contains(domElementName)) {

                if (!namesOfStatementsAlreadyExtracted.contains(domElementName)) {
                    namesOfStatementsAlreadyExtracted.add(domElementName);
                    final AbstractStatement childStatement = StatementFactory.createYangCoreStatement(context,
                            childDomElement, this);
                    childStatement.process(context);
                } else {
                    /*
                     * The same statement exists more than once, but is only allowed once. We generate the
                     * finding on the parent.
                     */
                    context.addFinding(new Finding(domElement, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString(),
                            "Child statement '" + domElementName + "' cannot exist more than once under '" + domElement
                                    .getName() + "'."));
                }
            }
        }
    }

    /**
     * Extracts from the DOM all child nodes of the specified statement names. Each of these may be
     * present 0..n, i.e. multiple times (or not at all).
     */
    private void extractOptionalMultipleChildStatementsFromDom(final ParserExecutionContext context,
            final List<String> optionalMultipleChildStatementNames) {

        if (optionalMultipleChildStatementNames.isEmpty()) {
            return;
        }

        for (final YangDomElement childDomElement : domElement.getChildren()) {
            final String domElementNode = childDomElement.getName();

            if (optionalMultipleChildStatementNames.contains(domElementNode)) {
                final AbstractStatement childStatement = StatementFactory.createYangCoreStatement(context, childDomElement,
                        this);
                childStatement.process(context);
            }
        }
    }

    /**
     * Extracts from the DOM all child nodes of the specified statement names. Each of these must be
     * present 1..1, i.e. exactly once.
     */
    private void extractMandatorySingletonChildStatementsFromDom(final ParserExecutionContext context,
            final List<String> mandatorySingletonChildStatementNames) {

        if (mandatorySingletonChildStatementNames.isEmpty()) {
            return;
        }

        final Set<String> namesOfStatementsYetToExtract = new HashSet<>(mandatorySingletonChildStatementNames);

        for (final YangDomElement childDomElement : domElement.getChildren()) {
            final String domElementName = childDomElement.getName();

            if (mandatorySingletonChildStatementNames.contains(domElementName)) {

                if (namesOfStatementsYetToExtract.contains(domElementName)) {
                    namesOfStatementsYetToExtract.remove(domElementName);
                    final AbstractStatement childStatement = StatementFactory.createYangCoreStatement(context,
                            childDomElement, this);
                    childStatement.process(context);
                } else {
                    /*
                     * The same statement exists more than once, but is only allowed once. We generate the
                     * finding on the parent.
                     */
                    context.addFinding(new Finding(domElement, ParserFindingType.P018_ILLEGAL_CHILD_STATEMENT.toString(),
                            "Child statement '" + domElementName + "' cannot exist more than once under '" + domElement
                                    .getName() + "'."));
                }
            }
        }

        if (!namesOfStatementsYetToExtract.isEmpty()) {
            for (final String notSuppliedStatement : namesOfStatementsYetToExtract) {
                context.addFinding(new Finding(this, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT.toString(),
                        "Statement '" + notSuppliedStatement + "' required under '" + domElement.getName() + "'."));
            }
        }
    }

    /**
     * Extracts from the DOM all child nodes of the specified statement names. Each of these must be
     * present 1..n, i.e. at least once, but possibly more often.
     */
    private void extractMandatoryMultipleChildStatementsFromDom(final ParserExecutionContext context,
            final List<String> mandatoryMultipleChildStatementNames) {

        if (mandatoryMultipleChildStatementNames.isEmpty()) {
            return;
        }

        final Set<String> namesOfStatementsYetToExtract = new HashSet<>(mandatoryMultipleChildStatementNames);

        for (final YangDomElement childDomElement : domElement.getChildren()) {
            final String domElementNode = childDomElement.getName();

            if (mandatoryMultipleChildStatementNames.contains(domElementNode)) {
                namesOfStatementsYetToExtract.remove(domElementNode);
                final AbstractStatement childStatement = StatementFactory.createYangCoreStatement(context, childDomElement,
                        this);
                childStatement.process(context);
            }
        }

        if (!namesOfStatementsYetToExtract.isEmpty()) {
            for (final String notSuppliedStatement : namesOfStatementsYetToExtract) {
                context.addFinding(new Finding(this, ParserFindingType.P019_MISSING_REQUIRED_CHILD_STATEMENT,
                        "Statement '" + notSuppliedStatement + "' required at least once under '" + domElement
                                .getName() + "'."));
            }
        }
    }

    /**
     * Extract any extension statement.
     */
    private void extractChildExtensions(final ParserExecutionContext context) {

        for (final YangDomElement childDomElement : domElement.getChildren()) {
            if (childDomElement.getName().contains(":")) {
                final AbstractStatement extensionStatement = StatementFactory.createYangExtensionStatement(context,
                        childDomElement, this);
                if (extensionStatement != null) {
                    extensionStatement.process(context);
                }
            }
        }
    }

    /**
     * Assigns the supplied namespace to all statements in this sub-tree. A namespace is really
     * only relevant for data nodes, but w simply apply the namespace to all statements to make
     * our life easier.
     */
    public void assignEffectiveNamespaceToStatementTree(final String namespace) {
        effectiveNamespace = namespace;
        for (final AbstractStatement child : children) {
            child.assignEffectiveNamespaceToStatementTree(namespace);
        }
    }

    /**
     * Returns the effective namespace of the statement. Statements (usually data nodes) within a
     * type-safe statement tree may have different namespaces due to augmentations having been
     * merged in.
     */
    public final String getEffectiveNamespace() {
        return effectiveNamespace;
    }

    /**
     * Assigns the supplied ConformanceType to all statements in this sub-tree. Again, really
     * only of interest of data nodes, but we simply assign it to all statements.
     */
    public void assignEffectiveConformanceTypeToStatementTree(final ConformanceType conformanceType) {
        effectiveConformanceType = conformanceType;
        for (final AbstractStatement child : children) {
            child.assignEffectiveConformanceTypeToStatementTree(conformanceType);
        }
    }

    /**
     * Returns the effective conformance type of the statement. Statements (usually data nodes)
     * within a type-safe statement tree may have different conformance due to augmentations
     * having been merged in.
     */
    public ConformanceType getEffectiveConformanceType() {
        return effectiveConformanceType;
    }

    /**
     * Assigns the config value of the parent to this statement, unless the statement has
     * a 'config' statement itself as child, in which case that will be used from now on.
     */
    public void assignEffectiveConfigToStatementTree(final boolean configValueOfParent) {

        /*
         * Config value may switch.
         */
        boolean configValueToUse = configValueOfParent;

        final YConfig yConfig = getChild(CY.STMT_CONFIG);
        if (yConfig != null && configValueToUse == true) {
            /*
             * This statement has a config statement. So use this value from now on for this statement
             * and all statements below. Note that switching back from 'config false' to 'config true' is
             * not allowed, and we will effectively ignore that.
             */
            configValueToUse = yConfig.isConfigTrue();
        }

        /*
         * Certain statements can never carry configuration data. For those statements,
         * the config value will always be forced to false, no matter if they have an
         * explicit 'config' underneath.
         */
        if (is(CY.STMT_ACTION) || is(CY.STMT_RPC) || is(CY.STMT_NOTIFICATION)) {
            configValueToUse = false;
        }

        effectiveConfig = configValueToUse;

        for (final AbstractStatement child : children) {
            child.assignEffectiveConfigToStatementTree(configValueToUse);
        }
    }

    /**
     * Returns whether this statement has at least one child instance of the specified statement.
     */
    public boolean hasAtLeastOneChildOf(final StatementModuleAndName statementModuleAndName) {
        for (final AbstractStatement child : children) {
            if (child.getStatementModuleAndName().equals(statementModuleAndName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all children of this statement of the specified module and statement name.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> List<T> getChildren(final StatementModuleAndName soughtSman) {
        final List<T> result = new ArrayList<>();
        for (final AbstractStatement child : children) {
            if (child.getStatementModuleAndName().equals(soughtSman)) {
                result.add((T) child);
            }
        }
        return result;
    }

    /**
     * Returns all children of the specified statement module/names.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> List<T> getChildren(
            final Set<StatementModuleAndName> soughtStatementModulesAndNames) {
        final List<T> result = new ArrayList<>();
        for (final AbstractStatement child : children) {
            if (soughtStatementModulesAndNames.contains(child.getStatementModuleAndName())) {
                result.add((T) child);
            }
        }
        return result;
    }

    /**
     * Returns the first child of this statement of the specified module and statement name, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStatement> T getChild(final StatementModuleAndName soughtStatementModuleAndName) {
        for (final AbstractStatement child : children) {
            if (child.getStatementModuleAndName().equals(soughtStatementModuleAndName)) {
                return (T) child;
            }
        }
        return null;
    }

    /**
     * Clones the children of this statement. Will result in a recursive invocation, i.e.
     * clones the complete subtree. The cloning keeps the original prefix resolvers in place.
     * Note that findings on the statements will not be cloned.
     */
    public void cloneFrom(final AbstractStatement orig) {

        copyPropertiesFrom(orig);

        /*
         * Clone the children. This will also clone extensions.
         */
        for (final AbstractStatement childToClone : orig.children) {
            final AbstractStatement clonedStatement = StatementFactory.cloneYangStatement(childToClone, this);
            clonedStatement.cloneFrom(childToClone);
        }
    }

    /**
     * Copies the statement properties from the supplied statement into this statement.
     */
    public void copyPropertiesFrom(final AbstractStatement orig) {

        /*
         * Clone-over the contents of any app data.
         */
        if (orig.customAppData != null) {
            for (final Entry<String, Object> origEntry : orig.customAppData.entrySet()) {
                if (origEntry.getValue() instanceof List) {
                    this.setCustomAppData(origEntry.getKey(), new ArrayList<>((List<?>) origEntry.getValue()));		// deep copy
                } else {
                    this.setCustomAppData(origEntry.getKey(), origEntry.getValue());
                }
            }
        }

        this.effectiveConformanceType = orig.effectiveConformanceType;
        this.effectiveConfig = orig.effectiveConfig;
        this.effectiveNamespace = orig.effectiveNamespace;
        this.effectiveStatus = orig.effectiveStatus;
    }

    /**
     * All existing children of the same type of statement are removed, and replaced by the supplied statement.
     */
    public <T extends AbstractStatement> void replaceChildrenWith(final T replaceWith) {
        removeAllChildrenOfStatement(replaceWith.getStatementModuleAndName());
        addChild(replaceWith);
    }

    /**
     * All existing children of the same statement are removed, and replaced by the supplied statements.
     */
    public <T extends AbstractStatement> void replaceChildrenWith(final List<T> replaceWith) {
        if (!replaceWith.isEmpty()) {
            removeAllChildrenOfStatement(replaceWith.get(0).getStatementModuleAndName());
        }
        addChildren(replaceWith);
    }

    /**
     * All existing children of the supplied type of statement are removed
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractStatement> void removeAllChildrenOfStatement(
            final StatementModuleAndName statementModuleAndName) {
        for (final T child : (List<T>) getChildren(statementModuleAndName)) {
            removeChild(child);
        }
    }

    /**
     * Replace one specific child statement with another.
     */
    public <T extends AbstractStatement> void replaceChild(final T toBeReplaced, final T replaceWith) {
        removeChild(toBeReplaced);
        addChild(replaceWith);
    }

    /**
     * Replace one specific child statement with another at the exact same place in the statement tree.
     */
    public <T extends AbstractStatement> void replaceChildInPlace(final T toBeReplaced, final T replaceWith) {
        final int oldPos = removeChild(toBeReplaced);
        addChild(replaceWith, oldPos);
    }

    /**
     * Removes a number of statements as children from this statement.
     */
    public <T extends AbstractStatement> void removeChildren(final List<T> statementsToRemove) {
        for (final T statementToRemove : statementsToRemove) {
            removeChild(statementToRemove);
        }
    }

    /**
     * Removes a specific child statement. Returns the index position at which the child was placed in the statement tree.
     */
    public <T extends AbstractStatement> int removeChild(final T statementToRemove) {

        final int indexOf = this.children.indexOf(statementToRemove);

        /*
         * The child is unlinked from this object here. The child becomes in effect detached from the tree and is
         * not-reachable anymore. (It should also be GC'ed fairly soon.)
         */
        statementToRemove.parent = null;
        this.children.remove(statementToRemove);

        return indexOf;
    }

    /**
     * Add a number of statements as children to this statement. If the statements are part of the
     * statement tree they will be detached from their parent first (i.e. will be effectively
     * re-parented).
     */
    public <T extends AbstractStatement> void addChildren(final List<T> statementsToAdd) {
        for (final T statementToAdd : statementsToAdd) {
            addChild(statementToAdd);
        }
    }

    /**
     * Add a statement as child to this statement. If the statement is part of the statement tree
     * it will be detached from its parent first (i.e. will be effectively re-parented).
     */
    public <T extends AbstractStatement> void addChild(final T statementToAdd) {
        addChild(statementToAdd, this.children.size());
    }

    /**
     * Add a statement as child to this statement at the specified position in the statement tree.
     * If the statement is part of the statement tree it will be detached from its parent first
     * (i.e. will be effectively re-parented).
     */
    public <T extends AbstractStatement> void addChild(final T statementToAdd, final int pos) {
        /*
         * Unlink the statement that is being added from it's current parent and hook it under "this" here.
         * The YangModelRoot must be likewise updated, as ownership in the tree has changed. Note: The
         * prefix resolver is NOT modified, as the prefixes may be different between the original definition
         * and this tree here.
         */
        if (statementToAdd.parent != null) {
            statementToAdd.parent.children.remove(statementToAdd);
        }
        this.children.add(pos, statementToAdd);
        statementToAdd.parent = this;
        statementToAdd.modelRoot = this.modelRoot;
    }

    /**
     * A result of TRUE means validation succeeded (all ok).
     */
    protected boolean validateArgumentIsTrueOrFalse(final ParserExecutionContext context) {
        final boolean validateNotNull = validateNotNull(context, domElement.getValue(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "statement '" + getStatementName() + "' requires 'true' or 'false' as argument.");
        final boolean validateIsTrueOrFalse = validateIsTrueOrFalse(context, domElement.getValue(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "statement '" + getStatementName() + "' requires 'true' or 'false' as argument.");
        return (validateNotNull && validateIsTrueOrFalse);
    }

    /**
     * A result of TRUE means validation succeeded (all ok).
     */
    protected boolean validateArgumentNotNullNotEmpty(final ParserExecutionContext context) {
        final boolean validateNotNull = validateNotNull(context, domElement.getValue(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "statement '" + getStatementName() + "' requires an argument.");
        final boolean validateNotEmpty = validateNotEmpty(context, domElement.getValue(),
                ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "The argument for statement '" + getStatementName() + "' cannot be empty.");
        return (validateNotNull && validateNotEmpty);
    }

    /**
     * A result of TRUE means validation succeeded (all ok).
     */
    protected boolean validateArgumentNotNull(final ParserExecutionContext context) {
        return validateNotNull(context, domElement.getValue(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "statement '" + getStatementName() + "' requires an argument.");
    }

    /**
     * A result of TRUE means validation succeeded (all ok).
     */
    protected boolean validateArgumentIsNull(final ParserExecutionContext context) {
        return validateIsNull(context, domElement.getValue(), ParserFindingType.P015_INVALID_SYNTAX_IN_DOCUMENT,
                "statement '" + getStatementName() + "' does not take an argument.");
    }

    /**
     * A result of TRUE means validation succeeded (all ok).
     */
    protected boolean validateDocumentationArgumentNotEmpty(final ParserExecutionContext context) {
        return validateNotEmpty(context, domElement.getValue(), ParserFindingType.P101_EMPTY_DOCUMENTATION_VALUE,
                "statement '" + getStatementName() + "' requires some text as argument.");
    }

    private boolean validateNotNull(final ParserExecutionContext context, final String valueToCheck,
            final ParserFindingType findingType, final String message) {
        if (valueToCheck == null) {
            issueFinding(context, findingType, message);
            return false;
        }
        return true;
    }

    private boolean validateIsNull(final ParserExecutionContext context, final String valueToCheck,
            final ParserFindingType findingType, final String message) {
        if (valueToCheck != null) {
            issueFinding(context, findingType, message);
            return false;
        }
        return true;
    }

    private boolean validateNotEmpty(final ParserExecutionContext context, final String valueToCheck,
            final ParserFindingType findingType, final String message) {
        if (valueToCheck != null && valueToCheck.trim().isEmpty()) {
            issueFinding(context, findingType, message);
            return false;
        }
        return true;
    }

    private boolean validateIsTrueOrFalse(final ParserExecutionContext context, final String valueToCheck,
            final ParserFindingType findingType, final String message) {
        if (valueToCheck != null && !valueToCheck.equals("true") && !valueToCheck.equals("false")) {
            issueFinding(context, findingType, message);
            return false;
        }
        return true;
    }

    protected void validateIsYangIdentifier(final ParserExecutionContext context, final String valueToCheck) {

        if (valueToCheck == null || valueToCheck.isEmpty()) {
            return;
        }

        final boolean isYangIdentifier = GrammarHelper.isYangIdentifier(valueToCheck);
        if (!isYangIdentifier) {
            issueFinding(context, ParserFindingType.P052_INVALID_YANG_IDENTIFIER,
                    "'" + valueToCheck + "' is not a valid YANG identifier.");
        }
    }

    protected void validateIsYangIdentifierReference(final ParserExecutionContext context, final String valueToCheck) {

        if (valueToCheck == null || valueToCheck.isEmpty()) {
            return;
        }

        final boolean isYangIdentifierReference = GrammarHelper.isYangIdentifierReference(valueToCheck);
        if (!isYangIdentifierReference) {
            issueFinding(context, ParserFindingType.P052_INVALID_YANG_IDENTIFIER,
                    "'" + valueToCheck + "' is not a valid YANG identifier-reference.");
        }
    }

    private void issueFinding(final ParserExecutionContext context, final ParserFindingType findingType,
            final String message) {
        context.addFinding(new Finding(this, findingType, message));
    }

    @Override
    public String toString() {
        return domElement.toString();
    }
}
