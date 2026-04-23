package com.smartcampus.exception;

/*
 * This exception is thrown when trying to add a reading to a sensor that is in MAINTENANCE state
 * it will be mapped to HTTP 403 Forbidden
 */

public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
