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
package org.oran.smo.yangtools.parser.model.parser;

import java.util.List;

/**
 * @author Mark Hollmann
 */
public class TokenIterator {

    private final List<Token> tokens;
    private int index = 0;

    public TokenIterator(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token getToken(final int offsetToCurrentPos) {
        final int toFetch = index + offsetToCurrentPos;
        return toFetch < tokens.size() ? tokens.get(toFetch) : null;
    }

    public void advance(int i) {
        index += i;
    }

    public boolean done() {
        return index >= tokens.size();
    }
}
