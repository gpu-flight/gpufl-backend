package com.gpuflight.gpuflbackend.exception;

/**
 * Custom exception for event processing errors.
 * This exception wraps various underlying errors that can occur during event processing,
 * including database operations, validation errors, and business logic failures.
 */
public class EventProcessingException extends RuntimeException {

    private final String eventType;
    private final String sessionId;

    public EventProcessingException(String message, String eventType, String sessionId) {
        super(message);
        this.eventType = eventType;
        this.sessionId = sessionId;
    }

    public EventProcessingException(String message, Throwable cause, String eventType, String sessionId) {
        super(message, cause);
        this.eventType = eventType;
        this.sessionId = sessionId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSessionId() {
        return sessionId;
    }
}
