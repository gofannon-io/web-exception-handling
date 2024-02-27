/*
 * Copyright (c) 2024. gwenlr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gofannon.webexceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_MESSAGE = "Oups, Houston, we have a problem";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return toErrorResponse(ex.getStatusCode(), ex.getReason());
    }


    @ExceptionHandler(PrivateAccessException.class)
    public ResponseEntity<String> handlePrivateAccessException(PrivateAccessException ex) {
        return toErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleRemainingException(Exception ex) {
        ResponseStatus responseStatusAnnotation = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatusAnnotation != null) {
            return toErrorResponse(responseStatusAnnotation.code(), responseStatusAnnotation.reason());
        }
        return toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_MESSAGE);
    }

    private static ResponseEntity<String> toErrorResponse(HttpStatus httpStatus, String reason) {
        return toErrorResponse(httpStatus.value(), reason);
    }

    private static ResponseEntity<String> toErrorResponse(HttpStatusCode httpStatusCode, String reason) {
        return toErrorResponse(httpStatusCode.value(), reason);
    }

    private static ResponseEntity<String> toErrorResponse(int httpStatusCode, String reason) {
        String escapedReason = reason == null ? DEFAULT_MESSAGE : reason.replaceAll("\"", "\\\"");
        return ResponseEntity.status(httpStatusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "message": "%s"
                        }
                        """.formatted(escapedReason).trim());
    }
}
