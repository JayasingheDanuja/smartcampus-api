package com.smartcampus.exception;

/*
 * this exception is used when a related resource(foreign key)
 * is not found in POST request
 * it will return HTTP422 (Unprocessable Entity)
 */

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
