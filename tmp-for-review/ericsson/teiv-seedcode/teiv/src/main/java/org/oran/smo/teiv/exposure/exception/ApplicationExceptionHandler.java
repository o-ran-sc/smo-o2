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
package org.oran.smo.teiv.exposure.exception;

import org.oran.smo.teiv.api.model.OranTeivErrorMessage;
import org.oran.smo.teiv.exception.TiesException;
import org.oran.smo.teiv.utils.query.exception.TiesPathException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseBody
    @ExceptionHandler(TiesException.class)
    public ResponseEntity<OranTeivErrorMessage> handleTiesException(final TiesException exception) {
        if (exception.getException() != null) {
            log.error(exception.getMessage(), exception.getException());
        }
        return new ResponseEntity<>(new OranTeivErrorMessage().message(exception.getMessage()).details(exception
                .getDetails()).status(exception.getStatus().name()), exception.getStatus());
    }

    @ResponseBody
    @ExceptionHandler(TiesPathException.class)
    public ResponseEntity<Object> handleTiesPathException(final TiesPathException exception) {
        if (exception.getResponse() != null) {
            return new ResponseEntity<>(exception.getResponse(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new OranTeivErrorMessage().message(exception.getMessage()).details(exception
                    .getDetails()).status(exception.getHttpStatus().name()), exception.getHttpStatus());
        }
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<OranTeivErrorMessage> handleGeneralException(final Exception ex) {
        log.error("Handling general exception", ex);
        return new ResponseEntity<>(new OranTeivErrorMessage().status(HttpStatus.INTERNAL_SERVER_ERROR.name()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<OranTeivErrorMessage> handleConstraintViolationException(ConstraintViolationException exception) {
        return new ResponseEntity<>(new OranTeivErrorMessage().message(exception.getMessage()).status(HttpStatus.BAD_REQUEST
                .name()), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new OranTeivErrorMessage().message(exception.getMessage()).status(HttpStatus.BAD_REQUEST
                .name()), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(new OranTeivErrorMessage().message(exception.getMessage()).status(HttpStatus.BAD_REQUEST
                .name()), HttpStatus.BAD_REQUEST);
    }
}
