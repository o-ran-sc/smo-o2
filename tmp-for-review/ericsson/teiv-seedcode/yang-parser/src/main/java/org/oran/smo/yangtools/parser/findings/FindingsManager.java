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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.AbstractStatement;
import org.oran.smo.yangtools.parser.model.yangdom.YangDomElement;

/**
 * Holds on to all findings issued during processing.
 *
 * @author Mark Hollmann
 */
public class FindingsManager {

    private final Set<Finding> findings = new HashSet<>();

    /*
     * The severity calculator for findings.
     */
    private final FindingSeverityCalculator findingSeverityCalculator;
    /*
     * This is the predicate to handle the global "suppress all"
     */
    private final SuppressAllFilterPredicate suppressAllFilterPredicate;
    /*
     * This is the predicate to handle findings suppressed due to severity calculator.
     */
    private final SeverityCalculatorFilterPredicate severityCalculatorFilterPredicate;
    /*
     * A number of predicates used to filter-out findings.
     */
    private final List<FindingFilterPredicate> filterPredicates = new ArrayList<>();
    /*
     * Finding types that can never be suppressed.
     */
    private final Set<String> nonSuppressableFindingTypes = new HashSet<>();

    public FindingsManager(final FindingSeverityCalculator findingSeverityCalculator) {

        this.findingSeverityCalculator = findingSeverityCalculator;

        this.suppressAllFilterPredicate = new SuppressAllFilterPredicate();
        this.filterPredicates.add(suppressAllFilterPredicate);

        this.severityCalculatorFilterPredicate = new SeverityCalculatorFilterPredicate(findingSeverityCalculator);
        this.filterPredicates.add(severityCalculatorFilterPredicate);
    }

    public FindingSeverityCalculator getFindingSeverityCalculator() {
        return findingSeverityCalculator;
    }

    /**
     * Add a finding unless it should be filtered.
     */
    public void addFinding(final Finding finding) {
        if (!nonSuppressableFindingTypes.contains(finding.getFindingType()) && shouldSuppress(finding)) {
            return;
        }

        findings.add(finding);

        /*
         * The finding also gets attached to various objects. This is useful for downstream tooling;
         * for example, for a tool that displays findings on statements.
         */
        if (finding.getStatement() != null) {
            finding.getStatement().addFinding(finding);
        }

        if (finding.getDataDomNode() != null) {
            finding.getDataDomNode().addFinding(finding);
        }

        if (finding.getYangModel() != null) {
            finding.getYangModel().addFinding(finding);
        }

        if (finding.getYangData() != null) {
            finding.getYangData().addFinding(finding);
        }
    }

    public void addFindings(final Collection<Finding> findings) {
        findings.forEach(this::addFinding);
    }

    public Set<Finding> getAllFindings() {
        return findings;
    }

    /**
     * Removes all findings from this FindingsManager.
     */
    public void clear() {
        findings.clear();
    }

    /**
     * Returns whether the finding would be suppressed, based on the supplied finding type. May be
     * used as performance improvement to avoid complex processing that may result in findings being
     * issued, just for these to be subsequently suppressed.
     */
    public boolean isFindingTypeGloballySuppressed(final String findingType) {
        return suppressAllFilterPredicate.allSuppressed() || severityCalculatorFilterPredicate.findingTypeSuppressed(
                findingType);
    }

    /**
     * Adds a finding type that cannot be suppressed. Any finding added to this findings
     * manager of any of the non-suppressable findings will never be filtered-out.
     */
    public void addNonSuppressableFindingType(final String findingType) {
        nonSuppressableFindingTypes.add(Objects.requireNonNull(findingType));
    }

    /**
     * Adds a custom filter predicate to this findings manager.
     */
    public void addFilterPredicate(final FindingFilterPredicate pred) {
        filterPredicates.add(Objects.requireNonNull(pred));
    }

    /**
     * If true, will cause all findings to be suppressed. However, certain findings are considered so serious
     * that it is not possible to suppress these, and any attempt to do so will be ignored.
     */
    public void setSuppressAll(final boolean val) {
        suppressAllFilterPredicate.setSuppressAll(val);
    }

    /**
     * Applies the filter currently set in this FindingsManager to the supplied findings.
     * Only findings passing the filter will be returned.
     */
    public Set<Finding> getFilteredFindings(final Set<Finding> findingsToFilter) {
        Objects.requireNonNull(findingsToFilter);
        return findingsToFilter.stream().filter(f -> !shouldSuppress(f)).collect(Collectors.toSet());
    }

    /*
     * Whether a finding should be suppressed. May not reliably work where a finding
     * is issued very early during the parse phase when the identity of a module is
     * not known yet.
     */
    private boolean shouldSuppress(final Finding finding) {

        for (final FindingFilterPredicate pred : filterPredicates) {
            if (pred.test(finding)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all findings from this FindingsManager, except those of the finding type(s) supplied.
     */
    public void retainFindingsOfType(final List<String> findingTypesToRetain) {
        Objects.requireNonNull(findingTypesToRetain);
        final Set<Finding> retainedFindings = findings.stream().filter(f -> findingTypesToRetain.contains(f
                .getFindingType())).collect(Collectors.toSet());
        findings.clear();
        findings.addAll(retainedFindings);
    }

    /**
     * Returns whether a finding of the specified type exists in this FindingsManager.
     */
    public boolean hasFindingOfType(final String findingType) {
        Objects.requireNonNull(findingType);
        return findings.stream().anyMatch(f -> f.getFindingType().equals(findingType));
    }

    /**
     * Returns whether a finding of any of the specified types exist in this FindingsManager.
     */
    public boolean hasFindingOfAnyOf(final List<String> findingTypes) {
        Objects.requireNonNull(findingTypes);
        return findings.stream().anyMatch(f -> findingTypes.contains(f.getFindingType()));
    }

    /**
     * Removes any finding that has been reported against the supplied YANG DOM element. Returns
     * true if at least a single finding was removed.
     */
    public boolean removeFindingsOnYangDomElement(final YangDomElement domElement) {

        List<Finding> toBeRemoved = null;

        for (final Finding f : findings) {

            final YangDomElement findingDomElement = f.getDomElement();
            if (domElement != findingDomElement) {
                continue;
            }

            /*
             * We record the finding as to-be-removed.
             */
            if (toBeRemoved == null) {
                toBeRemoved = new ArrayList<>();
            }
            toBeRemoved.add(f);
        }

        /*
         * We also need to remove the finding from the statement or the input, if so attached,
         * to avoid it showing up in other places.
         */
        if (toBeRemoved != null) {
            for (final Finding f : toBeRemoved) {

                final AbstractStatement onStatement = f.getStatement();
                if (onStatement != null) {
                    onStatement.removeFinding(f);
                }

                final YangModel onYangModel = f.getYangModel();
                if (onYangModel != null) {
                    onYangModel.removeFinding(f);
                }

                findings.remove(f);
            }
        }

        return toBeRemoved != null;
    }
}
