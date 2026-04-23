package com.smartcampus.exception;

/*
 * This exception is thrown when trying to delete a room that still has sensors inside it.
 * it will be mapped to HTTP 409 Conflict
 */

public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
