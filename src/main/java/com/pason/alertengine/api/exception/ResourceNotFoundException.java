package com.pason.alertengine.api.exception;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found in the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
