package com.careeros.common.exception;

/**
 * Thrown when a request violates a domain invariant that is not covered by
 * bean validation (e.g. cross-field business rules). Translated to HTTP 400.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
