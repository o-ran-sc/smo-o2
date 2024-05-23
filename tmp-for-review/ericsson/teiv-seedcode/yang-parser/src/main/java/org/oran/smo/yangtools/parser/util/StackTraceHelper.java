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
package org.oran.smo.yangtools.parser.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for error handling - sometimes, if a model is really in bad shape,
 * some NPEs may be thrown (or other exceptions) - this way the point in the code
 * can be logged.
 *
 * @author Mark Hollmann
 */
public abstract class StackTraceHelper {

    /**
     * Returns the first few lines of the stacktrace attached to the exception in a string.
     */
    public static String getStackTraceInfo(final Exception ex) {

        final StackTraceElement[] stackTraceElements = ex.getStackTrace();

        if (stackTraceElements == null || stackTraceElements.length == 0) {
            return "no stacktrace information available.";
        }

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        final int framesToPrint = Math.min(stackTraceElements.length, 7);
        for (int i = 0; i < framesToPrint; ++i) {
            if (i > 0) {
                printWriter.print(", ");
            }
            printWriter.print("[" + i + "] at " + stackTraceElements[i]);
        }

        return stringWriter.toString();
    }
}
