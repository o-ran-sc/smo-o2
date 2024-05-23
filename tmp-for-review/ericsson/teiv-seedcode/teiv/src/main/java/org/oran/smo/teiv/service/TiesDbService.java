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
package org.oran.smo.teiv.service;

import static org.jooq.impl.DSL.table;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.ingestion.DeadlockRetryPolicy;
import org.oran.smo.teiv.ingestion.validation.MaximumCardinalityViolationException;
import org.oran.smo.teiv.utils.RetryOperationUtils;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TiesDbService {
    private final DSLContext readDataDslContext;
    private final DSLContext writeDataDslContext;
    private final DeadlockRetryPolicy deadlockRetryPolicy;

    public void execute(List<Consumer<DSLContext>> dbOperations) {
        runMethodSafe(() -> {
            if (!dbOperations.isEmpty()) {
                RetryTemplate retryTemplate = RetryOperationUtils.getRetryTemplate(deadlockRetryPolicy, deadlockRetryPolicy
                        .getRetryBackoffMs());
                retryTemplate.execute(retryContext -> {
                    writeDataDslContext.transaction(configuration -> {
                        DSLContext transactionalContext = DSL.using(configuration);
                        dbOperations.forEach(op -> op.accept(transactionalContext));
                    });
                    return null;
                });
            }
            return null;
        });
    }

    protected Result<Record> selectAllRowsFromTable(final String tableName) {
        return runMethodSafe(() -> readDataDslContext.selectFrom(table(tableName)).fetch());
    }

    private <T> T runMethodSafe(Supplier<T> supp) {
        try {
            return supp.get();
        } catch (TiesException | MaximumCardinalityViolationException ex) {
            throw ex;
        } catch (TiesPathException ex) {
            log.error("Exception during query construction", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Sql exception during query execution", ex);
            throw TiesException.serverSQLException();
        }
    }
}
