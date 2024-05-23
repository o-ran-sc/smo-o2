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
package org.oran.smo.teiv.availability;

import org.springframework.retry.support.RetryTemplate;

import org.oran.smo.teiv.exception.UnsatisfiedExternalDependencyException;
import org.oran.smo.teiv.utils.RetryOperationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DependentServiceAvailability {
    protected String serviceName;

    protected int retryIntervalMs;

    protected int retryAttempts;

    /**
     * Check if service can be reached
     *
     * @return true once service is reached, false if max retries exhausted
     */
    public boolean checkService() {
        RetryTemplate retryTemplate = RetryOperationUtils.getRetryTemplate(serviceName,
                UnsatisfiedExternalDependencyException.class, retryAttempts, retryIntervalMs);
        try {
            return retryTemplate.execute(retryContext -> isServiceAvailable());
        } catch (UnsatisfiedExternalDependencyException e) {
            log.error("Hit max retry attempts {}: {}", retryAttempts, e.getMessage());
        }
        return false; // exhausted retries
    }

    abstract boolean isServiceAvailable() throws UnsatisfiedExternalDependencyException;
}
