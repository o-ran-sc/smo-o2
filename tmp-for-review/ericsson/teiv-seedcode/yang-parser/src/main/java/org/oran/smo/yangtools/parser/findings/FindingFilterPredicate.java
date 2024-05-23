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

import java.util.function.Predicate;

/**
 * Finding filter predicates are used during processing to filter out findings. The semantics of a finding
 * filter predicate are such that the test(Finding) method returns true if the finding shall be filtered out,
 * i.e. <b>not</b> retained for further processing.
 * <p>
 * If the predicate is used as part of stream processing, and the Stream.filter() method is used in order
 * to <b>retain</b> Finding instances, Predicate.negate() should be invoked.
 *
 * @author Mark Hollmann
 */
public interface FindingFilterPredicate extends Predicate<Finding> {
}
