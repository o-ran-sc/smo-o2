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
package org.oran.smo.teiv.utils.path;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class StrictErrorStrategy extends DefaultErrorStrategy {

    ParseCancellationException exception = null;

    @SneakyThrows
    @Override
    public Token recoverInline(Parser recognizer) {
        Token token = recognizer.getCurrentToken();
        String message = String.format("parse error at line %s, position %s right before %s ", token.getLine(), token
                .getCharPositionInLine(), getTokenErrorDisplay(token));
        exception = new ParseCancellationException(message);
        throw exception;
    }

    @Override
    public void sync(Parser recognizer) {
        /* do nothing to resync */}
}
