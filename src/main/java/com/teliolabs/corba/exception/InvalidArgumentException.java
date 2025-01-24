package com.teliolabs.corba.exception;

/**
 * Custom exception to handle invalid arguments passed to methods.
 */
public class InvalidArgumentException extends RuntimeException {

    /**
     * Constructs a new InvalidArgumentException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidArgumentException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidArgumentException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidArgumentException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public InvalidArgumentException(Throwable cause) {
        super(cause);
    }
}
