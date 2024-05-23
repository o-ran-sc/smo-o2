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
package org.oran.smo.teiv.ingestion;

import java.util.Map;

import org.jooq.exception.DataAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeadlockRetryPolicy extends SimpleRetryPolicy {
    public static final String POSTGRES_DEADLOCK_ERROR_CODE = "40P01";
    static final long serialVersionUID = 1;

    @Getter
    @Value("${database.retry-policies.deadlock.retry-backoff-ms}")
    private int retryBackoffMs;

    public DeadlockRetryPolicy(@Value("${database.retry-policies.deadlock.retry-attempts}") int maxRetryAttemps) {
        super(maxRetryAttemps, Map.of(DataAccessException.class, true));
    }

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable lastThrowable = context.getLastThrowable();
        if (lastThrowable instanceof DataAccessException) {
            return isThrowableCausedByDeadlock(lastThrowable) && super.canRetry(context);
        }
        return super.canRetry(context);
    }

    @Override
    public void close(RetryContext context) {
        if (context.getRetryCount() == super.getMaxAttempts() && isThrowableCausedByDeadlock(context.getLastThrowable())) {
            log.error("Reached the maximum number of retry attempts ({}) for a deadlock.", super.getMaxAttempts());
        }
        super.close(context);
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        super.registerThrowable(context, throwable);
        if (isThrowableCausedByDeadlock(throwable)) {
            log.warn("Deadlock occurred during the database transaction. Retry attempt: {}/{}. Cause: {}", context
                    .getRetryCount(), super.getMaxAttempts(), throwable.getMessage());
        }
    }

    private boolean isThrowableCausedByDeadlock(Throwable throwable) {
        if (throwable instanceof DataAccessException) {
            DataAccessException e = (DataAccessException) throwable;
            return e.sqlState().equals(POSTGRES_DEADLOCK_ERROR_CODE);
        }
        return false;
    }
}
