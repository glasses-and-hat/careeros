package com.careeros.common.exception;

import java.util.UUID;

/**
 * Thrown when an entity looked up by identifier does not exist.
 * Translated to HTTP 404 by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String resourceName, UUID id) {
        return new ResourceNotFoundException("%s with id '%s' was not found".formatted(resourceName, id));
    }
}
