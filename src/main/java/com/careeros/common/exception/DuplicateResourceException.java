package com.careeros.common.exception;

/**
 * Thrown when an operation would violate a domain-level uniqueness rule
 * (e.g. a company name that already exists, a job posting hash collision).
 * Translated to HTTP 409 by the global exception handler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
