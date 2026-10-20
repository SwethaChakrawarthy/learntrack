package com.learntrack.notification.event;

/** A message that can never be processed (bad JSON, missing fields). Not retried. */
public class InvalidEventException extends RuntimeException {
    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
