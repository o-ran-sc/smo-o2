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
package org.oran.smo.teiv.utils;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class RetryOperationUtils {
    public static RetryTemplate getRetryTemplate(String listenerName, Class<? extends Throwable> className,
            int retryAttempts, int retryIntervalMs) {
        return RetryTemplate.builder().maxAttempts(retryAttempts).fixedBackoff(retryIntervalMs).retryOn(className)
                .withListener(getRetryListener(listenerName)).build();
    }

    public static RetryTemplate getRetryTemplate(RetryPolicy retryPolicy, int retryIntervalMs) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(retryIntervalMs);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    private static RetryListener getRetryListener(String listenerName) {
        return new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(final RetryContext retryContext,
                    final RetryCallback<T, E> retryCallback) {
                return true; // called before first retry
            }

            @Override
            public <T, E extends Throwable> void close(final RetryContext retryContext,
                    final RetryCallback<T, E> retryCallback, final Throwable throwable) {
                if (retryContext.getRetryCount() == 0) {
                    return;
                }
                log.warn("{}: Execution stopped after {} retry attempts", listenerName, retryContext.getRetryCount());
            }

            @Override
            public <T, E extends Throwable> void onError(final RetryContext retryContext,
                    final RetryCallback<T, E> retryCallback, final Throwable throwable) {
                log.error("Reached the {} retry number for {}, received the following error during the execution: {}",
                        retryContext.getRetryCount(), listenerName, throwable.getMessage());
            }
        };
    }
}
